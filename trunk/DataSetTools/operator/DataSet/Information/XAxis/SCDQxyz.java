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
 * Revision 1.3  2002/09/25 16:45:06  pfpeterson
 * Changed algorithm to be A.J. Schultz's. This has the benefit of
 * using the SCD calibration if read into the Data's AttributeList.
 * Also moved the calculation into getResult() which returns a
 * Position3D of the Q-vector. The PointInfo method then just formats
 * the result.
 *
 * Revision 1.2  2002/09/19 16:01:30  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
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
import  DataSetTools.instruments.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
 *  This operator uses the chi, phi and omega attributes of a single crystal
 *  diffractometer DataSet to produce a string giving the values of Qx, Qy, Qz
 *  for a specific bin in a histogram, in a frame of reference attached to the
 *  crystal, ( chi = 0, phi = 0 and omega = 0 ).
 */

public class SCDQxyz extends  XAxisInformationOp 
                              implements Serializable
{
    private static final double PI = Math.PI;

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


  /* ---------------------------- getResult ------------------------------- */
  /**
   * Calculates the Q vector for the given time and spectrum. 
   *
   * @returns Position3D of the calculated Q.
   */
  public Object getResult(){
     DataSet ds = this.getDataSet();
     int   i    = ((Integer)(getParameter(0).getValue())).intValue();
     float x    = ((Float)(getParameter(1).getValue())).floatValue();
     Data    d  = ds.getData_entry(i);

     float[] Q={0f,0f,0f};
     float[] calib=(float[])d.getAttributeValue(Attribute.SCD_CALIB);
     float init_path=
         ((Float)d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
     float detD=((Float)
             d.getAttributeValue(Attribute.DETECTOR_CEN_DISTANCE)).floatValue();
     float detA=((Float)
             d.getAttributeValue(Attribute.DETECTOR_CEN_ANGLE)).floatValue();

     // set up the conversion to real-space
     double distance=0.;
     float wl=0f;
     
     Q[2]=x; // time is the time no matter what
     if(calib!=null){ // use the SCD calibration to get to real space
         // set up the initial position
         DetInfoListAttribute detI=
             (DetInfoListAttribute)d.getAttribute(Attribute.DETECTOR_INFO_LIST);
         DetectorInfo det=((DetectorInfo[])detI.getValue())[0];
         Q[0]=det.getColumn();
         Q[1]=det.getRow();
         
         // convert to real-space, the 100 is to convert from cm to m
         Q[0]=(calib[1]*(Q[0]-0.5f)+calib[3])/100f;
         Q[1]=(calib[2]*(Q[1]-0.5f)+calib[4])/100f;
         Q[2]=calib[0]+Q[2];
     }else{ // use the detector position from the file
         DetectorPosition pos=
             (DetectorPosition)d.getAttributeValue(Attribute.DETECTOR_POS);
         float[] coords=pos.getCylindricalCoords();
         // the methods below assume cm, not meters
         Q[0]=-1f*(float)(coords[0]*Math.sin(coords[1]-detA*PI/180.));
         Q[1]=coords[2];
     }
     // now convert the wavelength
     distance=init_path+Math.sqrt(detD*detD+(Q[0]*Q[0]+Q[1]*Q[1]));
     wl=tof_calc.Wavelength((float)distance,Q[2]);
     Q[2]=wl;

     // get the sample orientation
     float chi=((Float)
                ds.getAttributeValue(Attribute.SAMPLE_CHI)).floatValue();
     float phi=((Float)
                ds.getAttributeValue(Attribute.SAMPLE_PHI)).floatValue();
     float omega=((Float)
                  ds.getAttributeValue(Attribute.SAMPLE_OMEGA)).floatValue();
     
     // do the actual conversion to 1/d
     Q=cmtoqs(detA,detD,wl,Q);
     if(Q==null) return null;
     Q=rotSample(chi,phi,omega,Q);
     if(Q==null) return null;


     // now add the 2PI to get Q
     for( int j=0 ; j<3 ; j++ ){
         Q[j]=(float)(2*PI*Q[j]);
     }

     Position3D Qpos=new Position3D();
     Qpos.setCartesianCoords(Q[0],Q[1],Q[2]);

     return Qpos;
   }


  /* ------------------------------ PointInfo ----------------------------- */
  /**
   * Get Qx,Qy,Qz at the specified point. This calls getResult for the
   * actual calculation.
   *
   *  @param  x    the x-value (tof) for which the axis information is to be 
   *               obtained.
   *
   *  @param  i    the index of the Data block for which the axis information
   *               is to be obtained.
   *
   *  @return  information for the x axis at the specified x.
   */
  public String PointInfo( float x, int i ){
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

  /**
   * Convert the real-space position to Q
   */
  private float[] cmtoqs(float deta, float detd, float wl, float pos[]){
      float[] post_d=new float[3];
      float[] post_a=new float[3];
      float[] post_l=new float[3];

      // convert to 1/d
      double r=Math.sqrt(pos[0]*pos[0]+pos[1]*pos[1]+detd*detd);
      if(Double.isNaN(r) || Float.isNaN(pos[2])) return null;
      if(r==0. || pos[2]==0f) return null;
      post_d[0]=(float)(-1.*detd/(r*pos[2]));
      post_d[1]=(float)(pos[0]/(r*pos[2]));
      post_d[2]=(float)(pos[1]/(r*pos[2]));
      
      // rotate the detector to where it actually is
      double cosa=Math.cos(-deta*PI/180.);
      double sina=Math.sin(-deta*PI/180.);
      post_a[0]=(float)(    post_d[0]*cosa+post_d[1]*sina);
      post_a[1]=(float)(-1.*post_d[0]*sina+post_d[1]*cosa);
      post_a[2]=post_d[2];

      // translate the origin
      post_l[0]=post_a[0]+(float)(1./wl);
      post_l[1]=post_a[1];
      post_l[2]=post_a[2];
      
      return post_l;
  }

  /**
   * Rotate the sample orientation out of the Q-orientation
   */
  private float[] rotSample( float chi, float phi, float omega, float[] pos){
      float[] post_ome=new float[3];
      float[] post_chi=new float[3];
      float[] post_phi=new float[3];
      
      // reverse omega rotation
      double coso=Math.cos(omega*PI/180.);
      double sino=Math.sin(omega*PI/180.);
      post_ome[0]=(float)(pos[0]*coso-pos[1]*sino);
      post_ome[1]=(float)(pos[0]*sino+pos[1]*coso);
      post_ome[2]=pos[2];
      
      // reverse the chi rotation
      double cosc=Math.cos(chi*PI/180.);
      double sinc=Math.sin(chi*PI/180.);
      post_chi[0]=post_ome[0];
      post_chi[1]=(float)(post_ome[1]*cosc-post_ome[2]*sinc);
      post_chi[2]=(float)(post_ome[1]*sinc+post_ome[2]*cosc);
      
      // reverse the phi rotation
      double cosp=Math.cos(phi*PI/180.);
      double sinp=Math.sin(phi*PI/180.);
      post_phi[0]=(float)(    post_chi[0]*cosp+post_chi[1]*sinp);
      post_phi[1]=(float)(-1.*post_chi[0]*sinp+post_chi[1]*cosp);
      post_phi[2]=post_chi[2];
      
      return post_phi;
  }


}
