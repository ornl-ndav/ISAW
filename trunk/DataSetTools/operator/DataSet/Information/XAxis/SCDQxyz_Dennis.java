/*
 * File:  SCDQxyz_Dennis.java 
 *             
 * Copyright (C) 2002, Dennis Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.4  2003/02/18 20:22:16  dennis
 * Switched to use SampleOrientation attribute instead of separate
 * phi, chi and omega values.
 *
 * Revision 1.3  2003/01/07 22:33:06  dennis
 * Changed to use routine makeEulerRotationInverse() from tof_calc to
 * build the matrix that "unwinds" the goniometer rotations.
 *
 * Revision 1.2  2002/11/27 23:18:10  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/10/15 19:58:14  dennis
 * SCDQxyz calculation based on original version from July 2002.
 * getResult() returns Position3D object and PointInfo() calls
 * getResult() like SCDQxzy.  Currently does NOT use area detector
 * calibration data, but will work for an arbitrary collection of
 * spectra, as will be needed for two or more separate area
 * detectors.  Includes a boolean "debug" flag that will cause
 * intermediate results and the CHI PHI OMEGA "unwinding" transform
 * to be printed.
 *
 */

package DataSetTools.operator.DataSet.Information.XAxis;

import  java.io.*;
import  java.util.*;
import  java.text.*; 
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.instruments.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
 *  This operator uses the chi, phi and omega attributes of a single crystal
 *  diffractometer DataSet to produce a string giving the values of Qx, Qy, Qz
 *  for a specific bin in a histogram, in a frame of reference attached to the
 *  crystal, ( chi = 0, phi = 0 and omega = 0 ).
 */

public class SCDQxyz_Dennis extends  XAxisInformationOp 
                              implements Serializable
{
  public boolean debug = false;

  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */
  public SCDQxyz_Dennis( ) 
  {
    super( "Find Qx, Qy, Qz" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds    The DataSet to which the operation is applied
   *  @param  i     index of the Data block to use 
   *  @param  tof   the time-of-flight at which Qx,Qy,Qz is to be obtained
   */
  public SCDQxyz_Dennis( DataSet ds, int i, float tof )
  {
    this();                        

    IParameter parameter = getParameter(0); 
    parameter.setValue( new Integer(i) );
    
    parameter = getParameter(1); 
    parameter.setValue( new Float(tof) );
    
    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case, SCDQxyz_Dennis 
   */
   public String getCommand()
   {
     return "SCDQxyz_Dennis";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Data block index", new Integer(0) );
    addParameter( parameter );

    parameter = new Parameter( "TOF(us)" , new Float(0) );
    addParameter( parameter );
  }


  /* -------------------------- PointInfoLabel --------------------------- */
  /**
   * Get string label for the xaxis information.
   *
   *  @param  x    the x-value for which the axis label is to be obtained.
   *  @param  i    the index of the Data block that will be used for obtaining
   *               the label.
   *
   *  @return  String describing the information provided by X_Info(),
   *           "Qx,Qy,Qz".
   */
   public String PointInfoLabel( float x, int i )
   {
     return "DM:Qx,Qy,Qz";
   }


  /* ------------------------------ PointInfo ----------------------------- */
  /**
   * Get Qx,Qy,Qz at the specified point.
   *
   *  @param  x    the x-value (tof) for which the axis information is to be 
   *               obtained.
   *
   *  @param  i    the index of the Data block for which the axis information
   *               is to be obtained.
   *
   *  @return  information for the x axis at the specified x.
   */
   public String PointInfo( float x, int i )
   {
      // set the parameters for getResult
      getParameter(0).setValue(new Integer(i));
      getParameter(1).setValue(new Float(x));

      // set up a number format to display the result
      NumberFormat fmt = NumberFormat.getInstance();
      fmt.setMinimumFractionDigits(3);
      fmt.setMaximumFractionDigits(3);

      // let getResult calculate Q
      Position3D Qpos=(Position3D)this.getResult();
      if(Qpos==null) return "N/A";
      float[] Q=Qpos.getCartesianCoords();

      return fmt.format(Q[0])+","+fmt.format(Q[1])+","+fmt.format(Q[2]);
   }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                       // get the current data set
     DataSet ds = this.getDataSet();
     int   i    = ( (Integer)(getParameter(0).getValue()) ).intValue();
     float tof  = ( (Float)(getParameter(1).getValue()) ).floatValue();

     Data    d   = ds.getData_entry(i);

     DetectorPosition pos = (DetectorPosition)
                             d.getAttributeValue( Attribute.DETECTOR_POS );
     if ( pos == null )
       return new ErrorString("Missing DETECTOR POSITION attribute");

     Vector3D pt    = new Vector3D();
     Vector3D i_vec = new Vector3D( 1, 0, 0 );
     Vector3D k_vec = new Vector3D( 0, 0, 1 );
    
     Float initial_path_F = (Float)d.getAttributeValue(Attribute.INITIAL_PATH);
     if ( initial_path_F == null )
       return new ErrorString("Missing INITIAL PATH attribute");
     float initial_path = initial_path_F.floatValue();

     Position3D q_pos = tof_calc.DiffractometerVecQ(pos, initial_path, tof);

                                              // q is now in the fixed coord
                                              // system of the lab.  Rotate it
                                              // back to phi,chi,omega = 0,0,0
     SampleOrientation orientation =
        (SampleOrientation)ds.getAttributeValue(Attribute.SAMPLE_ORIENTATION);

     if (orientation == null) 
       return new ErrorString("Missing SampleOrientation attribute");

     Tran3D combinedR =orientation.getGoniometerRotationInverse();

     float xyz[] = q_pos.getCartesianCoords();
     pt.set( xyz[0], xyz[1], xyz[2] );

     if ( debug )
     {
       System.out.println("Dennis's+++++++++++++++++++++++++++++++++++++++++");
       System.out.println("det pos =       " + pos ); 
       float det_xyz[] = pos.getCartesianCoords();
       System.out.println("det pos,x,y,z = " + det_xyz[0] +
                          "   "              + det_xyz[1] +
                          "   "              + det_xyz[2] );
       System.out.println("q_pos =       " + q_pos );
       System.out.println("q_pos,x,y,z = "+xyz[0]+"   "+xyz[1]+"   "+xyz[2] );
       System.out.println("Dennis' combined R = \n" + combinedR );
     }

     combinedR.apply_to( pt, pt );
     q_pos.setCartesianCoords(pt.get()[0], pt.get()[1], pt.get()[2]);

     if ( debug )
     {
       System.out.println("After unwinding PHI, CHI, OMEGA");
       xyz = q_pos.getCartesianCoords();
       System.out.println("q_pos =       " + q_pos );
       System.out.println("q_pos,x,y,z = "+xyz[0]+"   "+xyz[1]+"   "+xyz[2] );
     }

     return q_pos;
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DateTime Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SCDQxyz_Dennis new_op = new SCDQxyz_Dennis( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
