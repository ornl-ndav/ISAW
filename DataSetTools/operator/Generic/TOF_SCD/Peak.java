/*
 * File:  Peak.java
 *
 * Copyright (C) 2002, Peter Peterson
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
 * Revision 1.27  2008/01/29 19:15:06  rmikk
 * Now extends IPeak.
 * Implements several createPeak methods and read/writePeak methods
 *   (untested)
 *
 * Revision 1.26  2005/03/03 22:02:40  dennis
 * Fixed "nearedge()" method, so that any peak that is outside of the
 * specified range is flagged.
 *
 * Revision 1.25  2005/03/02 22:55:21  dennis
 * Removed unused private field t.
 * Fixed "typo" in nearedge() method that compared channel number
 * with min y-value on detector instead of with min channel number.
 *
 * Revision 1.24  2004/03/15 03:28:39  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.23  2004/03/01 06:06:29  dennis
 * Added additional error checking in the hkl_to_real() method.
 * 1. The peak is marked as invalid (wavelength = 0), if in lab
 *    coordinates the x component of Q is greater than or equal
 *    to zero.  Such peaks are not real.
 * 2. The peak is marked as invalid (wavelength = 0), if it would
 *    be on the opposite side of the sample from the detector.  Such
 *    peaks are constructed by "Integrate" when many possible hkl
 *    values are generated to cover a volume of space containing the
 *    data from the detector.  Some of these, when rotated into lab
 *    coordinates would correspond to a detector on the opposite side
 *    of the sample from the actual detector.  These peaks were later
 *    projected onto the detector and showed up as spurious peaks.
 * This fixes a bug in the integration process, pointed out by John
 * Cowan, where the same hkl values showed up twice in slightly
 * different positions on the detector.
 *
 * Revision 1.22  2004/01/24 20:31:15  bouzekc
 * Removed/commented out unused variables/imports.
 *
 * Revision 1.21  2003/12/15 02:29:12  bouzekc
 * Removed unused imports.
 *
 * Revision 1.20  2003/08/05 21:35:34  dennis
 * Added comment on shifting time channel by one when translating
 * to external form in toString().
 *
 * Revision 1.19  2003/07/28 20:01:37  dennis
 * clone() method now also copies the UB and inverse UB matrix,
 * if they are set.
 *
 * Revision 1.18  2003/07/22 15:16:23  dennis
 * Replaced local "value" of h/mn, by more accurate value from
 * tof_calc.java
 *
 * Revision 1.17  2003/06/11 19:39:53  dennis
 * Modified getZofTime(,) to use the XScale method getI(time), which
 * does a binary search for the bin containing the time, rather than
 * just doing a sequential search.
 *
 * Revision 1.16  2003/06/02 16:30:30  pfpeterson
 * Always creates a new orientation matrix when chi,phi,omega are changed
 * and changed some comments.
 *
 * Revision 1.15  2003/05/14 20:14:05  pfpeterson
 * More changes to get detectors not at -90deg to work. Now code
 * produces exactly same result as FORTRAN.
 *
 * Revision 1.14  2003/05/13 20:18:30  pfpeterson
 * Fixed a javadoc.
 *
 * Revision 1.13  2003/05/09 14:12:27  pfpeterson
 * Fixed bug in converting hkl to real-space when the detector is not
 * at +/-90 degrees.
 *
 * Revision 1.12  2003/04/11 15:35:43  pfpeterson
 * Added a method to return (Q/2PI)-vector for use in lsqrs.
 *
 * Revision 1.11  2003/03/20 22:01:05  pfpeterson
 * First implementation of equals(Object). However, method will sometimes
 * return an incorrect true b/c not *all* of the instance variables are
 * checked, just many of the important ones.
 *
 * Revision 1.10  2003/01/31 22:54:00  pfpeterson
 * Added a new method which allows updating the value of hkl without then
 * recalculating the real-space and pixel representations. This is useful
 * for setting hkl to zero.
 *
 * Revision 1.9  2003/01/31 19:17:14  pfpeterson
 * Added mutator method for the real space representation.
 *
 * Revision 1.8  2003/01/30 21:06:36  pfpeterson
 * Really is an object now. Added more error checking and methods to
 * calculate between pixel, real-space, and hkl (both directions).
 * Also some formatting changes (sorry).
 *
 * Revision 1.7  2002/11/27 23:22:20  pfpeterson
 * standardized header
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import DataSetTools.dataset.XScale;
import DataSetTools.math.tof_calc;

/**
 * Methods for getting information about atoms dependent on the
 * isotope.
 */

public class Peak  implements IPeak_IPNS_out{
  // instance variables
  private int       seqnum   = 0;
  private float     h        = 0;
  private float     k        = 0;
  private float     l        = 0;
  private float     x        = Float.NaN;
  private float     y        = Float.NaN;
  private float     z        = Float.NaN;
  private float     xcm      = 0f;
  private float     ycm      = 0f;
  private float     wl       = 0f;
  private int       ipkobs   = 0;
  private float     inti     = 0f;
  private float     sigi     = 0f;
  private int       reflag   = 0;
  private int       nrun     = 0;
  private int       detnum   = 0;
  private float     nearedge = 0;
  private float     detA     = 0f;
  private float     detA2    = 0f;
  private float     detD     = 0f;
  private float     chi      = 0f;
  private float     phi      = 0f;
  private float     omega    = 0f;
  private float     monct    = 0f;
  private float[]   calib    = null;
  private float[][] UB       = null;
  private float[][] invUB    = null;
  private float[][] ROT      = {{1f,0f,0f},{0f,1f,0f},{0f,0f,1f}};
  private float     L1       = 0f;
  private float     T0       = 0f;
  private float     T1       = 0f;
  private XScale    xscale   = null;
  
  /* --------------------Constructor Methods-------------------- */
  /**
   *  A Peak with the null constructor.
   */
  public Peak( ){
    super();
  }
  
  /**
   *  Create a peak specifing only the pixel position, real
   *  position, and intensity.
   *
   *  @param seqnum The sequence number
   *  @param x      The pixel column the peak is in.
   *  @param y      The pixel row the peak is in.
   *  @param z      The time bin the peak is in.
   *  @param ipkobs The intesity of the peak.
   *  @param nrun   The run number.
   *  @param detnum The detector number.
   */
  public Peak( int seqnum, float x, float y, float z, 
               int ipkobs, int nrun, int detnum){
    this.seqnum(seqnum);
    this.pixel(x,y,z);
    this.ipkobs(ipkobs);
    this.detnum(detnum);
    //this(seqnum,x,y,z,ipkobs,detnum);
    this.nrun(nrun);
  }

  /* ------------------- accessor and mutator methods -------------------- */
  /**
   *  Accessor method for the sequence number.
   */
  public int seqnum(){
    return this.seqnum;
  }

  /**
   *  Mutator method for the sequence number.
   */
  public int seqnum(int SEQNUM){
    this.seqnum=SEQNUM;
    return this.seqnum();
  }
  
  /**
   * Mutator method for index
   *
   * @param propagate whether or not to propagate the change in hkl to
   * the other representations.
   */
  public void sethkl( float H, float K, float L, boolean propagate){
    this.h=H;
    this.k=K;
    this.l=L;

    if(!propagate) return;

    if(invUB!=null){
      this.hkl_to_real();
      if(calib!=null){
        this.real_to_pixel();
      }
    }
  }

  /**
   * Mutator method for index
   */
  public void sethkl( float H, float K, float L){
    this.sethkl(H,K,L,true);
  }
  
  /**
   *  Accessor method for h
   */
  public float h(){
    return this.h;
  }
  
  /**
   *  Accessor method for k
   */
  public float k(){
    return this.k;
  }
  
  /**
   *  Accessor method for l
   */
  public float l(){
    return this.l;
  }
  
  /**
   *  Accessor method for the pixel column.
   */
  public float x(){
    return this.x;
  }
  
  /**
   *  Mutator method for the pixel position
   */
  public void pixel(float X, float Y, float Z){
    this.x=X;
    this.y=Y;
    this.z=Z;
    
    if(calib!=null){
      this.pixel_to_real();
      if(UB!=null){
        this.real_to_hkl();
      }
    }
  }
  
  /**
   * Accessor method for setting what times the peak lies
   * between. Something must be done to pixel_to_real to recreate
   * this.
   */
  public void times(float t0, float t1){
    this.T0=t0;
    this.T1=t1;

    if(calib!=null){
      this.pixel_to_real();
      if(UB!=null){
        this.real_to_hkl();
      }
    }
  }
  
  public float time(){
     if(Float.isNaN( z )||Float.isNaN( T0 )|| Float.isNaN( T1 ))
        return Float.NaN;
     float f = z-(float)Math.floor( z );
     
     return f*T0+(1-f)*T1;
     
  }
  public void time( XScale time){
    this.xscale=time;
  }

  /**
   *  Accessor method for the pixel row.
   */
  public float y(){
    return this.y;
  }

  /**
   *  Accessor method for the time bin.
   */
  public float z(){
    return this.z;
  }
  
  /**
   * Mutator method for real-space representation
   */
  public void real(float XCM, float YCM, float WL){
    this.xcm=XCM;
    this.ycm=YCM;
    this.wl=WL;
    
    if(calib!=null)
      this.real_to_pixel();
    if(UB!=null)
      this.real_to_hkl();
  }

  /**
   *  Accessor method for the horizontal position which is returned in cm.
   */
  public float xcm(){
    return this.xcm;
  }
  
  /**
   *  Accessor method for the vertical position which is returned in cm.
   */
  public float ycm(){
    return this.ycm;
  }

  /**
   *  Accessor method for the wavelength.
   */
  public float wl(){
    return this.wl;
  }

  /**
   *  Accessor method for the intensity.
   */
  public int ipkobs(){
    return this.ipkobs;
  }

  /**
   *  Mutator method for the intensity.
   */
  public int ipkobs(int IPKOBS){
    this.ipkobs=IPKOBS;
    return this.ipkobs();
  }
  
  /**
   *  Accessor method for the integrated intensity
   */
  public float inti(){
    return this.inti;
  }

  /**
   *  Mutator method for the integrated intensity
   */
  public float inti(float INTI){
    this.inti=INTI;
    return this.inti();
  }
  
  /**
   *  Accessor method for the uncertainty in integrated intensity
   */
  public float sigi(){
    return this.sigi;
  }

  /**
   *  Mutator method for the uncertainty in integrated intensity
   */
  public float sigi(float SIGI){
    this.sigi=SIGI;
    return this.sigi();
  }
  
  /**
   *  Accessor method for the reflection flag.
   */
  public int reflag(){
    return this.reflag;
  }

  /**
   *  Mutator method for the reflection flag.
   */
  public int reflag(int REFLAG){
    this.reflag=REFLAG;
    return this.reflag();
  }

  /**
   *  Accessor method for the run number.
   */
  public int nrun(){
    return this.nrun;
  }

  /**
   *  Mutator method for the run number.
   */
  public int nrun(int NRUN){
    this.nrun=NRUN;
    return this.nrun();
    }
  
  /**
   *  Accessor method for the detector number.
   */
  public int detnum(){
    return this.detnum;
  }

  /**
   *  Mutator method for the detector number.
   */
  public int detnum(int DETNUM){
    this.detnum=DETNUM;
    return this.detnum();
  }
  
  /**
   * Accessor method for the integrated monitor intensity
   */
  public float monct(){
    return this.monct;
  }
  
  /**
   * Mutator method for the integrated monitor intensity
   */
  public float monct(float MONCT){
    this.monct=MONCT;
    return this.monct();
  }
  
  /**
   *  Accessor method for the pixel distance from edge.
   */
  public float nearedge(){
    return this.nearedge;
  }
  
  /**
   *  Mutator method for the pixel distance from edge. If there is
   *  anything wrong with any of the pixel values this is set to -1.
   */
  public float nearedge(int MINX, int MAXX,int MINY, int MAXY,
                        int MINZ, int MAXZ){

    if( this.x==-1 || this.y==-1 || this.z==-1 ){
      this.nearedge=-1;
      return this.nearedge;
    }

    if ( this.x < MINX || this.x > MAXX ||      // x out of bounds
         this.y < MINY || this.y > MAXY ||      // y out of bounds
         this.z < MINZ || this.z > MAXZ  )      // z out of bounds
    {
      this.nearedge = -1;
      return this.nearedge;
    }
    
    float dx = this.x - MINX;     // find dx = min distance of x to MINX, MAXX
    if ( dx > MAXX - this.x)
      dx = MAXX - this.x;

    float dy = this.y - MINY;     // find dy = min distance of y to MINY, MAXY
    if ( dy > MAXY - this.y )
      dy = MAXY - this.y;
    
    float dz = this.z - MINZ;     // find dz = min distance of x to MINZ, MAXZ
    if ( dz > MAXZ - this.z )
      dz = MAXZ - this.z;
    
    float min = dx;               // set min to min of dx, dy, dz

    if ( dy < min ) 
      min = dy;

    if ( dz < min ) 
      min = dz;
    
    //System.out.println("("+this.x+","+this.y+","+this.z+")"+min);
    
    this.nearedge = min;
    return this.nearedge();
  }
  
  /**
   *  Accessor method for the detector angle in degrees
   */
  public float detA(){
    return this.detA;
  }

  /**
   *  Mutator method for the detector angle in degrees
   */
  public float detA(float DETA){
    this.detA=DETA;
    if(this.calib!=null){
      this.pixel_to_real();
      if(this.UB!=null){
        this.real_to_hkl();
      }
    }
    return this.detA();
  }
  
  /**
   *  Accessor method for the second detector angle in degrees
   */
  public float detA2(){
    return this.detA2;
  }

  /**
   *  Mutator method for the second detector angle given in degrees
   */
  public float detA2(float DETA){
    this.detA2=DETA;
    if(this.calib!=null){
      this.pixel_to_real();
      if(this.UB!=null){
        this.real_to_hkl();
      }
    }
    return this.detA2();
  }
  
  /**
   *  Accessor method for the detector distance which is given in cm.
   */
  public float detD(){
    return this.detD;
  }

  /**
   *  Mutator method for the detector distance which is given in cm.
   */
  public float detD(float DETD){
    this.detD=DETD;
    if(this.calib!=null){
      this.pixel_to_real();
      if(this.UB!=null){
        this.real_to_hkl();
      }
    }
    return this.detD();
  }
  
  /**
   *  Accessor method for the primary flight path
   */
  public float L1(){
    return this.L1;
  }

  /**
   *  Mutator method for the primary flight path in meters.
   */
  public void L1(float path){
    this.L1=path;
    if(this.calib!=null){
      this.pixel_to_real();
      if(this.UB!=null){
        this.real_to_hkl();
      }
    }
  }
  
  /**
   *  Accessor method for the sample chi
   */
  public float chi(){
    return this.chi;
  }
  
  /**
   *  Accessor method for the sample phi
   */
  public float phi(){
    return this.phi;
  }
  
  /**
   *  Accessor method for the sample omega
   */
  public float omega(){
    return this.omega;
  }
  
  /**
   *  Mutator method for the sample orientation. This will
   *  automatically update the values of hkl.
   */
  public void sample_orient(float CHI, float PHI, float OMEGA){
    this.chi=CHI;
    this.phi=PHI;
    this.omega=OMEGA;

    // create a new rotation matrix
    this.ROT=makeROT(this.chi,this.phi,this.omega);

    // update the inverse of the rotated UB matrix
    if(this.UB!=null){
      this.invUB=
        LinearAlgebra.getInverse(LinearAlgebra.mult(this.ROT,this.UB));
      this.real_to_hkl();
    }
  }
  
  /**
   * Mutator method for the calibration. This will automatically
   * update the values of xcm, ycm, wl, and hkl.
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
    
    this.pixel_to_real();
    if(this.UB!=null){
      this.real_to_hkl();
    }
  }
  
  /**
   * Accessor method for the orientation matrix
   */
  public float[][] UB(){
    return this.UB;
  }

  /**
   * Mutator method method for the orientation matrix. This will
   * automatically update the values of hkl.
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

    // find the inverse
    if(this.ROT!=null){
      this.invUB=
        LinearAlgebra.getInverse(LinearAlgebra.mult(this.ROT,this.UB));
    }else{
      this.invUB=LinearAlgebra.getInverse(this.UB);
    }

    // update hkl
    this.real_to_hkl();
  }

  /* --------------------- private methods --------------------- */
  /**
   * Method to allow easier update of real space calculation
   */
  private void pixel_to_real(){
    if(calib==null) return;
    
    if(calib.length==5){ // this is the old method
      this.xcm=calib[1]*(this.x()-0.5f)+calib[3];
      this.ycm=calib[2]*(this.y()-0.5f)+calib[4];
    }else{               // don't know what to do with other calibrations
      return;
    }
    
    if(this.L1>0f){
      //double h=(1E6*6.62606876E-34); // in kg*cm*A/us
      //double m=1.67492716E-27;       // in kg
      //double hom=0.3955974;          // h/m in cm*A/us 
      double hom = tof_calc.ANGST_PER_US_PER_MM / 10;  // more accurate
      float l1=this.L1*100F;
      float l2=this.detD(); // in cm
      
      // find the time
      float deltaT=0f;
      if(this.xscale!=null){
        int Tslice=(int)this.z;
        if(Tslice<this.xscale.getNum_x()-1){
          deltaT=this.xscale.getX(Tslice+1)-this.xscale.getX(Tslice);
        }else{
          deltaT=this.xscale.getX(Tslice)-this.xscale.getX(Tslice-1);
        }
      }else{
        deltaT=this.T1-this.T0;
      }
      float remain=(this.z+1f)%1f;
      float time=0f;
      if(this.xscale!=null)
        time=this.xscale.getX((int)this.z)+remain*deltaT;
      else
        time=this.T0+remain*deltaT;
      
      // shift the time to by the calibrated offset
      time=calib[0]+time;
      
      // if any of the values are not properly defined set the wl to zero
      if( l1==0f || l2==0f || this.xcm()==0f || this.ycm()==0f || time==0
          || Float.isNaN(l1) || Float.isNaN(l2) || Float.isNaN(this.xcm())
          || Float.isNaN(this.ycm()) || Float.isNaN(time) ){
        this.wl=0f;
        return;
      }
      
      // calculate the corrected path length
      double ld=l2*l2+this.xcm*this.xcm+this.ycm*this.ycm;
      float l=l1+(float)Math.sqrt(ld);
    
      //System.out.print("T="+time+"  L="+l+"  ");
      this.wl=(float)(hom*time/l);
      //System.out.println("WL="+this.wl);
    }else{
      System.out.println("L1=0");
      return;
    }
  }
  
  private void real_to_pixel(){
    if(calib==null) return;

    if(calib.length==5){ // this is the old method
      this.x=0.5f+(this.xcm-calib[3])/calib[1];
      this.y=0.5f+(this.ycm-calib[4])/calib[2];
    }else{               // don't know what to do with other calibrations
      return;
    }

    if(this.L1>0f){
      // double hom  = 0.3955974; // h/m in cm*A/us
      double hom = tof_calc.ANGST_PER_US_PER_MM / 10;  // more accurate
      float l1    = this.L1*100F;
      float l2    = this.detD(); // in cm
      
      // if any of the values are not properly defined set the z to zero
      if( l1==0f || l2==0f || this.xcm()==0f || this.ycm()==0f ){
        this.z=0f;
        return;
      }
      
      // calculate the corrected path length
      double ld=l2*l2+this.xcm*this.xcm+this.ycm*this.ycm;
      float l=l1+(float)Math.sqrt(ld);
    
      // calculate time from wavelength
      float time=(float)((this.wl*l/hom)-calib[0]);

      // calculate time boundaries around this
      //System.out.println("XSCALE="+this.xscale);
      this.z=getZofTime(time,this.xscale);
      this.T0=(int)this.z;
      this.T1=this.T0+1f;
      //System.out.println("TIME["+this.T0+"]="+xscale.getX((int)this.T0));
      if(xscale!=null && this.z!=-1f ){
        this.T0=xscale.getX((int)this.T0);
        this.T1=xscale.getX((int)this.T1);
        // now fill in z
        this.z=this.z+(time-this.T0)/(this.T1-this.T0);
      }else{
        this.T0=0f;
        this.T1=0f;
        this.z=-1;
      }
    }else{
      System.out.println("L1=0");
      return;
    }
  }

  private void real_to_hkl(){
    // get the Q-vector/2pi
    float[] Qvec=getQ();
    if(Qvec==null) return;

    // zero out the three indices
    this.h=0f;
    this.k=0f;
    this.l=0f;

    // calculate hkl
    for( int i=0 ; i<3 ; i++ ){
      this.h=this.h+invUB[0][i]*Qvec[i];
      this.k=this.k+invUB[1][i]*Qvec[i];
      this.l=this.l+invUB[2][i]*Qvec[i];
    }
  }

  private void hkl_to_real(){

    final double  small = 1.0E-06;
    float[] pos   = new float[3];

    // get the orientation matrix with the euler angles dealt with
    float[][] UBR=LinearAlgebra.mult(this.ROT,this.UB);

    // remove sample orientation and UB matrix
    pos[0]=UBR[0][0]*h+UBR[0][1]*k+UBR[0][2]*l;
    pos[1]=UBR[1][0]*h+UBR[1][1]*k+UBR[1][2]*l;
    pos[2]=UBR[2][0]*h+UBR[2][1]*k+UBR[2][2]*l;

    // pos[] now has the scaled "Q" vector in lab coordinates, so in order
    // to be valid, we need Qx < 0.  In particular, 
    // if Qx = 0.0, then sin(theta), theta and two-theta are all zero,
    // in which case return WL=0.0. 
    // More generally if Qx >= 0, then the Q vector is not valid, so return
    // WL and positions as 0.
    if (pos[0] > small) {
      this.xcm=0f;
      this.ycm=0f;
      this.wl=0f;
      return;
    }

    // calculate wavelength of incident neutron                          
    //   HH is the square of the magnitude of the diffraction vector.
    //   WL = 2 d sin(theta)
    //      = (2 / d*) sin(theta)
    //      = (2 / sqrt(HH)) * (XD / sqrt(HH))
    //      = 2.0 * XD / HH
    // 
    float hh = pos[0]*pos[0]+pos[1]*pos[1]+pos[2]*pos[2];
    this.wl  = -2f*pos[0]/hh;     // wl is positive, since pos[0] = Qx < 0 

    // check that the wavelength is non-zero
    if(Math.abs(this.wl)<small){
      this.xcm=0f;
      this.ycm=0f;
      this.wl=0f;
      return;
    }

    // Calculate XCM and YCM detector coordinates. First translate
    // the origin from the reciprocal lattice origin to the center of
    // the sphere of reflection.
    double xdp = pos[0]+(1f/this.wl);
    double ydp = 0.;
    double zdp = 0.;

    // rotate to a detector angle of zero                                
    double ang = this.detA*Math.PI/180.;
    double xt = xdp;
    double yt = pos[1];
    xdp =     xt*Math.cos(ang) + yt*Math.sin(ang);
    ydp = -1.*xt*Math.sin(ang) + yt*Math.cos(ang);
    zdp = pos[2];

    // check that the rotated detector is in the +x direction.  If not, then
    // the scattering vector was directed away from the actual detector,
    // so set the values to zero.

    if( xdp <= small ){
      this.xcm=0f;
      this.ycm=0f;
      this.wl=0f;
      return;
    }

    // calculate XCM and YCM                                             
    this.xcm = (float)(-(ydp/xdp)*this.detD);
    this.ycm = (float)((zdp/xdp)*this.detD);
  }

  /**
   * Get the value of Z given a time and xscale
   */
  private static float getZofTime(float time, XScale xscale){
    //System.out.println("00:"+time);
    if( time==0f || xscale==null ) return -1;

    //System.out.println("01:"+xscale);

    if(time<xscale.getStart_x() || time>xscale.getEnd_x() )
      return -1;
    //System.out.println("02");

    int index = xscale.getI(time);
    if ( time == xscale.getX(index) )
      return index;
    else
      return index - 1;
  }

  /**
   * Despite its name this actually calculates 1/d (Qvec/2PI).
   */
  public float[] getQ(){
    float[] post_d = new float[3];
    float[] post_a = new float[3];
    float[] post_l = new float[3];
    
    // convert to 1/d
    double r=Math.sqrt(this.xcm*this.xcm+this.ycm*this.ycm
                       +this.detD*this.detD);
    if(Double.isNaN(r) || Float.isNaN(this.wl)) return null;
    if(r==0. || this.wl==0f) return null;
    post_d[0]=(float)(this.detD/(r*this.wl));
    post_d[1]=(float)(-1.*this.xcm/(r*this.wl));
    post_d[2]=(float)(this.ycm/(r*this.wl));
    
    // rotate the detector to where it actually is
   
    double deta = this.detA*Math.PI/180.;
    double cosa=Math.cos(deta);
    double sina=Math.sin(deta);
    post_a[0]=(float)( post_d[0]*cosa - post_d[1]*sina);
    post_a[1]=(float)( post_d[0]*sina + post_d[1]*cosa);
    post_a[2]=post_d[2];
      
    // translate the origin
    post_l[0]=post_a[0]-(float)(1./this.wl);
    post_l[1]=post_a[1];
    post_l[2]=post_a[2];

    return post_l;
  }
  
  /**
   * Despite its name this actually calculates 1/d (Qvec/2PI).
   */
  public float[] getQ1(){//Gave slightly different results than the above
                         // even if detA2 was 0
    float[] post_d;
    float[] post_a = new float[3];
    float[] post_l = new float[3];
    
    // rotate center by detA2 in x-z plane. Then go xcm,ycm in -y dir an z dir
    //  to find corresponding position of peak.
    double deta2 = detA2/180*Math.PI;
    Vector3D Pos= new Vector3D( (float)(detD*Math.cos( deta2 )),
                                 -xcm,
                                (float)(detD*Math.sin( deta2 ))+ycm);
    
    //Convert to length = wl. 
    float r= Pos.length()*wl;
    Pos.multiply( 1/r );
    
    post_d = Pos.get();
    
    //Next rotate detA about x axis
    double deta = this.detA*Math.PI/180.;
    double cosa=Math.cos(deta);
    double sina=Math.sin(deta);
    post_a[0]=(float)( post_d[0]*cosa - post_d[1]*sina);
    post_a[1]=(float)( post_d[0]*sina + post_d[1]*cosa);
    post_a[2]=post_d[2];
      
    // translate the origin
    post_l[0]=post_a[0]-(float)(1./this.wl);
    post_l[1]=post_a[1];
    post_l[2]=post_a[2];

    return post_l;
  }

  /**
   * Returns 1/d vector (Q/2PI) in crystal reference frame.
   */
  public double[] getUnrotQ(){
    float[]   Q=this.getQ();
    float[][] invROT=LinearAlgebra.getInverse(this.ROT);
    double[]  unrotQ={0.,0.,0.};

    for( int i=0; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        unrotQ[i]=unrotQ[i]+(double)(invROT[i][j]*Q[j]);
      }
    }

    return unrotQ;
  }

  /**
   * This generates the rotation matrix to go from hkl to real (sample
   * to lab).
   *
   * @param chi angle in degrees
   * @param phi angle in degrees
   * @param omega angle in degrees
   */
  private static float[][] makeROT(float chi, float phi, float omega){
    float[][] ROT={{1f,0f,0f},
                   {0f,1f,0f},
                   {0f,0f,1f}};

    // calculate the rotation
    Tran3D tran3d=null;
    tran3d=tof_calc.makeEulerRotation(phi,chi,-omega);
    float[][] tranrot=tran3d.get();

    //copy the rotation matrix into the return matrix (just the important bits)
    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        ROT[i][j]=tranrot[i][j];
      }
    }

    return ROT;
  }

  /* --------------------- clone method --------------------- */
  /**
   *  Produce a copy of the object.
   */
  public Object clone(){
    Peak peak=new Peak(seqnum,x,y,z,ipkobs,nrun,detnum);
    //Peak peak=new Peak(seqnum,h,k,l,x,y,z,xcm,ycm,wl,ipkobs,inti,sigi,reflag,nrun,detnum);
    peak.sethkl(h,k,l);
    peak.inti(inti);
    peak.sigi(sigi);
    peak.reflag(reflag);
    peak.nearedge=this.nearedge();
    peak.detA(this.detA());
    peak.detA2(this.detA2());
    peak.detD(this.detD());
    peak.L1=this.L1;
    peak.T0=this.T0;
    peak.xscale=this.xscale; // passing a reference
    peak.T1=this.T1;
    peak.monct(this.monct());
    peak.sample_orient(this.chi(),this.phi(),this.omega());
    if(this.calib==null){
      peak.calib=null;
    }else{
      peak.calib=new float[this.calib.length];
      for( int i=0 ; i<this.calib.length ; i++ ){
        peak.calib[i]=this.calib[i];
      }
    }
    peak.xcm=this.xcm;
    peak.ycm=this.ycm;
    peak.wl=this.wl;

    if ( UB != null )                  
      peak.UB( UB );    // also copy the UB matrix, which also sets UB inverse

    return peak;
  }
  
  /* --------------------- equals method --------------------- */
  /**
   * Returns true if the Object is a Peak, with the same hkl, real
   * space representation, pixel representation, and intensities.
   */
  public boolean equals(Object obj){
    if(obj==null) return false; // one of the general rules according to java
    if(!(obj instanceof Peak)) return false; // object must be a peak

    Peak other=null;
    try{ // convert for convenience
      other=(Peak)obj;
    }catch(ClassCastException e){
      return false;
    }
    if(other==null) return false; // something went wrong in casting

    // check peak index
    if(this.h != other.h) return false;
    if(this.k != other.k) return false;
    if(this.l != other.l) return false;

    // check real space
    if(this.xcm != other.xcm) return false;
    if(this.ycm != other.ycm) return false;
    if(this.wl  != other.wl ) return false;

    // check pixel space
    if(this.x != other.x) return false;
    if(this.y != other.y) return false;
    if(this.z != other.z) return false;

    // check intensities
    if(this.ipkobs != other.ipkobs) return false;
    if(this.inti   != other.inti  ) return false;
    if(this.sigi   != other.sigi  ) return false;

    return true;
  }

  /* ------------------- toString method -------------------- */
  /**
   *  Format the toString method to be the full version of how it
   *  can be specified. This is in the Schultz format.
   */
  public String toString( ){
    String rs="";	
    
    DecimalFormat df_se_tw=new DecimalFormat("###0.00");
    DecimalFormat df_ei_fo=new DecimalFormat("##0.0000");
    DecimalFormat df_ni_tw=new DecimalFormat("#####0.00");
    
    rs="3"+format(seqnum,6)+format(h,4)+format(k,4)+format(l,4);
    if(Float.isNaN(x)){
      rs=rs+"     "+x;
    }else{
      rs=rs+format(df_se_tw.format(x),7);
    }
    if(Float.isNaN(y)){
      rs=rs+"     "+y;
    }else{
      rs=rs+format(df_se_tw.format(y),7);
    }
    if(Float.isNaN(z)){
      rs=rs+"     "+z;
    }else{
      rs=rs+format(df_se_tw.format(z+1),7); // internally z is stored as one 
                                            // less than the file representation
    }
    rs=rs+format(df_se_tw.format(xcm),7)
      +format(df_se_tw.format(ycm),7)
      +format(df_ei_fo.format(wl),8)
      //+format(df_ei_fo.format(t),8)
      +format(ipkobs,6)
      +format(df_ni_tw.format(inti),9)
      +format(df_ni_tw.format(sigi),9)
      +format(reflag,5)+format(nrun,6)+format(detnum,3);
    
    return rs;
  }
  
  /**
   * Format an integer by padding on the left.
   */
  static private String format(int number, int length){
    String rs=new Integer(number).toString();
    while(rs.length()<length){
      rs=" "+rs;
    }
    return rs;
  }
  
  /**
   * Format a float by padding on the left.
   */
  static private String format(float number, int length){
    String rs=format(Math.round(number),length);
    while(rs.length()<length){
      rs=" "+rs;
    }
    return rs;
  }
  

  /**
   * Format a string by padding on the left.
   */
  static private String format(String rs, int length){
    while(rs.length()<length){
      rs=" "+rs;
    }
    return rs;
  }

/* (non-Javadoc)
 * @see DataSetTools.operator.Generic.TOF_SCD.IPeak#createNewPeakhkl(float, float, float, float[][])
 */
@Override
public IPeak createNewPeakhkl( float h , float k , float l , float[][] UB ) {

   Peak p_new =(Peak)clone();
   p_new.UB(UB);
   p_new.sethkl( h , k , l );
   return p_new;
}

/* (non-Javadoc)
 * @see DataSetTools.operator.Generic.TOF_SCD.IPeak#createNewPeakxyz(float, float, float)
 */
@Override
public IPeak createNewPeakxyz( float x , float y , float z ) {

   // TODO Auto-generated method stub   
   Peak p_new =(Peak)clone();
  
   p_new.pixel( x,y,z );
   return p_new;
   
}

public static String getLine( InputStream f){
   String line = "";
   int c;
   try{
    for(c= f.read(); c <32 && c>=0;c =f.read()){}
      if( c >=32){
         line+=(char)c;
      }else
         return null;//end of file
   for( c=f.read(); c>=32;c=f.read())
      line +=(char)c;
   }catch(Exception ss){
      return null;
   }
   return line;
}

/* (non-Javadoc)
 * @see DataSetTools.operator.Generic.TOF_SCD.IPeak#readPeak(java.io.InputStream, java.lang.String, java.lang.String)
 */
@Override
public IPeak readPeak( InputStream f , String Facility , String Instrument ) {

   if( Facility != null && !Facility.equals("IPNS"))
      return null;
   if( Instrument != null && !Instrument.equals("SCD"))
      return null;
   
   
   String line = getLine( f);
   String[] items = line.split( "[ ]++ " );
   if( items == null || items.length < 1)
      return null;
   while( items[0].trim().equals( "0" )){
      line = getLine( f);
      items = line.split( "[ ]++ " );
      if( items == null || items.length < 1)
         return null;
      if( !items[0].toUpperCase().equals( "NRUN" ))
         return null;
   }
   Peak pk =(Peak)this.clone();;
   while( ! items[0].trim().equals("2")){
      if( items[0].equals( "3" ))
      try{
         pk = new Peak(new Integer(items[0].trim()).intValue(),
                  Float.NaN,Float.NaN,Float.NaN,-1,
                  new Integer(items[1].trim()).intValue(),
                  new Integer(items[2].trim()).intValue());
         pk.sample_orient( new Float(items[6]).floatValue() ,
                  new Float(items[7]).floatValue()  , 
                 new Float(items[8]).floatValue()  );
         pk.monct( new Float(items[9]).floatValue()  );
         pk.detA(new Float(items[3]).floatValue()  );
         pk.detA2(new Float(items[4]).floatValue()  );
         pk.detD(new Float(items[5]).floatValue()  );
         
         line = getLine(f);
         if( line == null)
            return null;
         items = line.split( "[ ]++" );
         if( items == null || items.length < 1){
            items = new String[1];
            items[0]="x";
         }
            
         
      }catch(Exception s){
         return null;
      }
      else{
         line = getLine(f);
         if( line == null)
            return null;
         items = line.split( "[ ]++" );
         if( items == null || items.length < 1){
            items = new String[1];
            items[0]="x";
         }
      }
   }
     
   if( items.length !=17)
      return null;
   pk.seqnum( (new Integer(items[1].trim())).intValue());
   pk.sethkl( (new Float(items[2].trim()).floatValue()) ,
            (new Float(items[3].trim()).floatValue()), 
            (new Float(items[4].trim()).floatValue()) );
   pk.pixel((new Float(items[5].trim()).floatValue()) ,
            (new Float(items[6].trim()).floatValue()), 
            (new Float(items[7].trim()).floatValue()) );
   pk.real( new Float(items[8].trim()).floatValue() ,
            (new Float(items[9].trim()).floatValue()), 
            (new Float(items[10].trim()).floatValue()) );
            
   pk.ipkobs( (new Integer(items[11].trim())).intValue() );
   pk.inti( (new Float(items[12].trim()).floatValue()) );
   pk.sigi( (new Float(items[13].trim()).floatValue()) );
   pk.reflag( (new Integer(items[14].trim())).intValue() );
   
   
   return pk;
}

/* (non-Javadoc)
 * @see DataSetTools.operator.Generic.TOF_SCD.IPeak#readPeak(java.io.InputStream)
 */
@Override
public IPeak readPeak( InputStream f ) {
   
   return readPeak( f, null, null);

   // TODO Auto-generated method stub
  
}


/* (non-Javadoc)
 * @see DataSetTools.operator.Generic.TOF_SCD.IPeak#setFacility(java.lang.String)
 */
@Override
public void setFacility( String facilityName ) {

   if( facilityName != null && !facilityName.equals( "IPNS" ))
      throw new IllegalArgumentException("These peaks are only for IPNS");
   
}

/* (non-Javadoc)
 * @see DataSetTools.operator.Generic.TOF_SCD.IPeak#setInstrument(java.lang.String)
 */
@Override
public void setInstrument( String instrumentName ) {

   if( instrumentName != null && !instrumentName.equals("SCD"))
      throw new IllegalArgumentException("These peaks are only for SCD instruments at IPNS");
      
   
}

/* (non-Javadoc)
 * @see DataSetTools.operator.Generic.TOF_SCD.IPeak#writePeak(java.io.OutputStream, DataSetTools.operator.Generic.TOF_SCD.IPeak, java.lang.String, java.lang.String)
 */
@Override
public void writePeak( OutputStream f , IPeak prevPeak , String Facility ,
         String Instrument ) {


   if( Facility != null && !Facility.equals("IPNS"))
      throw new IllegalArgumentException("These peaks are only for IPNS");
   if( Instrument != null && !Instrument.equals("SCD"))
      throw new IllegalArgumentException("These peaks are only for SCD instruments at IPNS");
   
   writePeak( f, prevPeak);
   
}


private void writeCommon( OutputStream f){
   try{ 
      f.write( "0  NRUN DETNUM    DETA   DETA2    DETD     CHI     PHI   OMEGA   MONCNT\n".getBytes() );
      

      String format ="1 %i5 %i6 %f7.2 %f7.2 %f7.2 %f7.2 %f7.2 %f7.2 %f7.2 %i8\n";
      String S =String.format( format ,nrun(),detnum(),detA(),detA2(),detD(), chi(),phi(),
                     omega(),monct());
      f.write(  S.getBytes() );
      
      f.write( "2  SEQN   H   K   L      X      Y      Z    XCM    YCM      WL   IPK     INTI     SIGI RFLG  NRUN DN\n".getBytes() );
   
   }catch(Exception ss){
      System.out.println("Error printing header for detID ="+detnum);
   }
   
}
private boolean differ( float f1, float f2){
   if( Float.isNaN( f1 ) && Float.isNaN( f2 ))
      return true;
   if( f1 !=f2)
      return false;
   return true;
}

private boolean differ( int n1, int n2){
   if( n1 == n2)
      return true;
   return false;
}
private boolean needWriteHeader( Peak prevPeak){
   if( differ(chi(),prevPeak.chi()))
      return true;
   if( differ(phi(),prevPeak.phi()))
      return true;
   if( differ(omega(),prevPeak.omega()))
      return true;
   if( differ(nrun(),prevPeak.nrun()))
      return true;

   if( differ(detA(),prevPeak.detA()))
      return true;
   if( differ(detA2(),prevPeak.detA2()))
      return true;
   if( differ(detD(),prevPeak.detD()))
      return true;
   if( differ(detnum(),prevPeak.detnum()))
      return true;

   if( differ(monct(),prevPeak.monct()))
      return true;
   return false;
}

private int rr( float h){
     return (int) Math.floor( h+.5);
}
/* (non-Javadoc)
 * @see DataSetTools.operator.Generic.TOF_SCD.IPeak#writePeak(java.io.OutputStream, DataSetTools.operator.Generic.TOF_SCD.IPeak)
 */
@Override
public void writePeak( OutputStream f , IPeak prevPeak ) {
    if( !(prevPeak instanceof Peak))
       throw new IllegalArgumentException("Can only write IPNS SCD peaks with this class");
    
    if( needWriteHeader( (Peak)prevPeak))
       writeCommon(f);
    String format ="%i1 %i6 %i3 %i3 %i3 %f6.2 %f6.2 %f6.2 %f6.2 %f6.2 %f7.4 %i5"+
        " %f8.2 %i %i5 %i2\n";
    String S =String.format( format ,3, seqnum(), rr(h()),rr(k()),rr(l()),x(), 
             y(),z(),xcm(),ycm(),wl(),ipkobs(),inti(),sigi(),reflag(),nrun(),
             detnum());
    try{
       f.write( S.getBytes());
    }catch(Exception ss){
       System.out.println("Error writing peak "+ seqnum()+" . "+ ss.toString());
    }
   
}
}
