/*
 * File:  SCDQxyz.java 
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2002/07/31 16:28:32  dennis
 * Calculate vector Q for a diffractometer in a common frame
 * of reference relative to a crystal.  The laboratory frame
 * of reference is mapped back to a common frame by reversing
 * the rotations defined by Phi, Chi and Omega.
 *
 *
 */

package DataSetTools.operator.DataSet.Information.XAxis;

import  java.io.*;
import  java.util.*;
import  java.text.*; 
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;

/**
 *  This operator uses the chi, phi and omega attributes of a single crystal
 *  diffractometer DataSet to produce a string giving the values of Qx, Qy, Qz
 *  for a specific bin in a histogram, in a frame of reference attached to the
 *  crystal, ( chi = 0, phi = 0 and omega = 0 ).
 */

public class SCDQxyz extends  XAxisInformationOp 
                              implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */
  public SCDQxyz( ) 
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
  public SCDQxyz( DataSet ds, int i, float tof )
  {
    this();                        

    Parameter parameter = getParameter(0); 
    parameter.setValue( new Integer(i) );
    
    parameter = getParameter(1); 
    parameter.setValue( new Float(tof) );
    
    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case, SCDQxyz 
   */
   public String getCommand()
   {
     return "SCDQxyz";
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
     return "Qx,Qy,Qz";
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
     DataSet ds  = this.getDataSet();
     Data    d   = ds.getData_entry(i);

     DetectorPosition pos = (DetectorPosition)
                             d.getAttributeValue( Attribute.DETECTOR_POS );

     Vector3D pt    = new Vector3D();
     Vector3D i_vec = new Vector3D( 1, 0, 0 );
     Vector3D k_vec = new Vector3D( 0, 0, 1 );
/*
     Tran3D one_eighty_z = new Tran3D();             // "FIX" detector position
     one_eighty_z.setRotation( 180, k_vec );         //  by rotating 180 deg
     float xyz_det[] = pos.getCartesianCoords();     //  about z axis. Not 
     pt.set( xyz_det[0], xyz_det[1], xyz_det[2] );   //  needed if runfile OK.
     one_eighty_z.apply_to( pt, pt );
     pos.setCartesianCoords(pt.get()[0], pt.get()[1], pt.get()[2]);
*/
     float initial_path = 
             ((Float)d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue(); 
 
     Position3D q_pos = tof_calc.DiffractometerVecQ(pos, initial_path, x);

                                              // q is now in the fixed coord
                                              // system of the lab.  Rotate it
                                              // back to phi,chi,omega = 0,0,0 
     float omega = ((Float)ds.getAttributeValue(Attribute.SAMPLE_OMEGA))
                         .floatValue();
     float phi   = ((Float)ds.getAttributeValue(Attribute.SAMPLE_PHI))
                         .floatValue();
     float chi   = ((Float)ds.getAttributeValue(Attribute.SAMPLE_CHI))
                         .floatValue();

     Tran3D omegaR = new Tran3D();
     Tran3D phiR   = new Tran3D();
     Tran3D chiR   = new Tran3D();
     Tran3D combinedR = new Tran3D();

     phiR.setRotation( -phi, k_vec );       // "unwrap" the rotations to return
     chiR.setRotation( -chi, i_vec );       // to laboratory frame of reference.     omegaR.setRotation( +omega, k_vec );   

     combinedR.setIdentity();
     combinedR.multiply_by( phiR );
     combinedR.multiply_by( chiR );
     combinedR.multiply_by( omegaR );

     float xyz[] = q_pos.getCartesianCoords();
     pt.set( xyz[0], xyz[1], xyz[2] );
     combinedR.apply_to( pt, pt );
     q_pos.setCartesianCoords(pt.get()[0], pt.get()[1], pt.get()[2]);

     NumberFormat f = NumberFormat.getInstance();
     
     return f.format(xyz[0]) + "," + f.format(xyz[1]) + "," + f.format(xyz[2]);
   }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();
    int   i    = ( (Integer)(getParameter(0).getValue()) ).intValue();
    float tof  = ( (Float)(getParameter(1).getValue()) ).floatValue();

    return PointInfo( tof, i );
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DateTime Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SCDQxyz new_op = new SCDQxyz( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
