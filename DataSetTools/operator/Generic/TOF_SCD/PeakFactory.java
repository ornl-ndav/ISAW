/*
 * File:  PeakFactory.java
 *
 * Copyright (C) 2003, Peter Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.6  2004/03/15 03:28:39  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.5  2004/01/24 20:31:16  bouzekc
 * Removed/commented out unused variables/imports.
 *
 * Revision 1.4  2003/12/15 02:29:12  bouzekc
 * Removed unused imports.
 *
 * Revision 1.3  2003/10/17 02:19:06  bouzekc
 * Updated javadocs and fixed javadoc errors.
 *
 * Revision 1.2  2003/01/31 19:16:46  pfpeterson
 * Added a line to set the monitor count of the generated peaks.
 *
 * Revision 1.1  2003/01/30 21:07:23  pfpeterson
 * Added to CVS.
 *
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import gov.anl.ipns.MathTools.LinearAlgebra;
import DataSetTools.dataset.XScale;

/**
 * Factory class for building Peaks.
 *
 * @see DataSetTools.operator.Generic.TOF_SCD.Peak
 */

public class PeakFactory{
  // instance variables
  private int       nrun     = 0;
  private int       detnum   = 0;
  private float     detA     = 0f;
  private float     detA2    = 0f;
  private float     detD     = 0f;
  private float     chi      = 0f;
  private float     phi      = 0f;
  private float     omega    = 0f;
  private float     monct    = 0f;
  private XScale    xscale   = null;
  private float[]   calib    = null;
  private float[][] UB       = null;
  private float     L1       = 0f;
  private float     T0       = 0f;
  private float     T1       = 0f;
  
  /**
   *  Builds a Peak with the default constructor.
   */
  /*public PeakFactory(){
    super();
    }*/
  
  /**
   *  Create a Peak specifing only the pixel position, real
   *  position, and intensity.
   *
   *  @param nrun   The run number.
   *  @param detnum The detector number.
   *  @param L1     The primary flight path.
   *  @param detD   The detector distance.
   *  @param detA   The detector angle.
   *  @param detA2  The second detector angle.
   */
  public PeakFactory( int nrun, int detnum, float L1,
                      float detD, float detA, float detA2){
    this.detnum=detnum;
    this.nrun=nrun;
    this.L1=L1;
    this.detD=detD;
    this.detA=detA;
    this.detA2=detA2;
  }

  /**
   * Returns a Peak with parameters 0, 0, 0, 0, 0, run number, detector number.
   *
   * @see DataSetTools.operator.Generic.TOF_SCD.Peak
   */
  public Peak getInstance(){
    Peak peak=new Peak(0, 0, 0, 0, 0, nrun, detnum);

    peak.L1(this.L1);
    peak.detA(this.detA);
    peak.detA2(this.detA2);
    peak.detD(this.detD);
    peak.detnum(this.detnum);
    peak.sample_orient(this.chi,this.phi,this.omega);
    if(this.xscale!=null)
      peak.time(this.xscale);
    else
      peak.times(this.T0,this.T1);
    peak.calib(calib);
    peak.UB(UB);
    peak.monct(this.monct);

    return peak;
  }

  /**
   * Calls this.getInstance() and calls peak.times( t0, t1 ) and 
   * peak.pixel( x, y, z ) on the resultant Peak.
   *
   * @see DataSetTools.operator.Generic.TOF_SCD.Peak
   */
  public Peak getPixelInstance( float x, float y, float z, float t0, float t1){
    Peak peak=this.getInstance();

    peak.times(t0,t1);
    peak.pixel(x,y,z);

    return peak;
  }

  /**
   * Calls this.getInstance() and calls peak.sethkl(h, k, l).
   *
   * @see DataSetTools.operator.Generic.TOF_SCD.Peak
   */
  public Peak getHKLInstance( float h, float k, float l){
    Peak peak=this.getInstance();

    peak.sethkl(h,k,l);

    return peak;
  }

  /* ------------------- accessor and mutator methods -------------------- */
  /**
   * Accessor method for setting what times the peak lies
   * between. Something must be done to pixel_to_real to recreate
   * this.
   *
   * @param t0 Time one.
   * @param t1 Time two.
   */
  public void times(float t0, float t1){
    this.T0=t0;
    this.T1=t1;
  }
  
  /**
   * Mutator method for the internal XScale.
   *
   * @param xscale The new XScale to set.
   */
  public void time(XScale xscale){
    this.xscale=xscale;
  }

  /**
   *  Mutator method for the run number.
   *
   *  @param NRUN The new run number.
   */
  public void nrun(int NRUN){
    this.nrun=NRUN;
  }

  /**
   *  Mutator method for the detector number.
   *
   *  @param DETNUM The new detector number.
   */
  public void detnum(int DETNUM){
    this.detnum=DETNUM;
  }
  
  /**
   * Mutator method for the integrated monitor intensity.
   *
   * @param MONCT The new monitor intensity.
   */
  public void monct(float MONCT){
    this.monct=MONCT;
  }
  
  /**
   *  Mutator method for the detector angle.
   *
   *  @param DETA The new detector angle.
   */
  public void detA(float DETA){
    this.detA=DETA;
  }
  
  /**
   *  Mutator method for the second detector angle.
   *
   *  @param DETA The new second detector angle.
   */
  public void detA2(float DETA){
    this.detA2=DETA;
  }

  /**
   *  Mutator method for the detector distance.
   *
   *  @param DETD The new detector distance.
   */
  public void detD(float DETD){
    this.detD=DETD;
  }

  /**
   *  Mutator method for the primary flight path.
   *
   *  @param path The new flight path.
   */
  public void L1(float path){
    this.L1=path;
  }
  
  /**
   *  Mutator method for the sample orientation.
   *
   *  @param CHI   Angle chi.
   *  @param PHI   Angle phi.
   *  @param OMEGA Angle omega.
   */
  public void sample_orient(float CHI, float PHI, float OMEGA){
    this.chi=CHI;
    this.phi=PHI;
    this.omega=OMEGA;
  }
  
  /**
   * Mutator method for the calibration. 
   *
   * @param CALIB The new calibration array.
   */
  public void calib(float[] CALIB){
    if(CALIB==null){
      this.calib=null;
      return;
    }
    this.calib=new float[CALIB.length];
    for( int i=0 ; i<CALIB.length ; i++ ){
      this.calib[i]=CALIB[i];
    }
  }

  /**
   * Mutator method method for the orientation matrix. This will
   * automatically update the values of hkl.
   *
   * @param ub The new orientation matrix.
   */
  public void UB(float[][] ub){
    // check that the value we want to set to is valid
    if(ub==null) return;
    if(! LinearAlgebra.isSquare(ub)) return;
    if(ub.length!=3) return;

    // create a new matrix if necessary
    if(this.UB==null) this.UB=new float[3][3];

    // copy the values into the local version
    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        this.UB[i][j]=ub[i][j];
      }
    }
  }

  /**
   *  Format the toString method to be useful for diagnostics.
   *
   *  @return A String containing run number, detector number, primary flight
   *  path, detector distance, detector angle one, and detector angle two (in
   *  that order).
   */
  public String toString( ){
    String rs="";	
    
    rs=rs+"nrun="+this.nrun+" ";
    rs=rs+"detnum="+this.detnum+" ";
    rs=rs+"L1="+this.L1+" ";
    rs=rs+"detD="+this.detD+" ";
    rs=rs+"detA="+this.detA+" ";
    rs=rs+"detA2="+this.detA2+" ";
    return rs;
  }
}
