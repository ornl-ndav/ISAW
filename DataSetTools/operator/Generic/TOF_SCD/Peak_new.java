/*
 * File:  Peak_new.java 
 *             
 * Copyright (C) 2004, Peter Peterson, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.17  2007/02/22 15:14:43  rmikk
 * Added a static utility method to create a Peak_new object from a data set,
 *    detectorID, and col,row and timechan
 *
 * Revision 1.16  2006/11/05 01:52:31  dennis
 * Minor efficiency improvement (eliminated redundant call to
 * Vector3D.get() method.)
 *
 * Revision 1.15  2006/02/12 21:13:11  dennis
 * First stage of clean up to finish evolution from original
 * IPNS specific peaks object, to more general, more clearly
 * defined peaks object.
 *
 * Revision 1.14  2006/02/12 04:17:24  rmikk
 * Removed a useless else statement
 *
 * Revision 1.13  2006/02/12 03:45:38  dennis
 * Fixed calculation in update_xcm_ycm_wl().
 * Commented out calls to super(), since this version is actually
 * indepnendent of it's super class.  This should be changed to not
 * be derived from the Peak() class at all.
 *
 * Revision 1.12  2006/01/17 22:44:10  rmikk
 * Fixed the clone method to ensure that the UB and hkl values correspond
 *   to the parent peak
 *
 * Revision 1.11  2006/01/17 19:30:31  rmikk
 * Eliminated CalcHKL
 * Added a few more fields to be copied in the clone
 *
 * Revision 1.10  2006/01/17 18:22:21  dennis
 * The constructor that accepts a grid, now gets the detnum
 * field set correctly.
 *
 * Revision 1.9  2006/01/17 04:15:50  dennis
 * Removed two debug prints.
 *
 * Revision 1.8  2006/01/16 04:38:54  rmikk
 * Added Sample orientation as an argument of a constructor so that a
 *   peak need not be connected to any data
 * Improved the clone method
 *
 * Revision 1.7  2006/01/15 02:07:17  rmikk
 * Now uses The sampleorientation class to calculate ROT and invROT.  Can
 * only be changed by Peak_new
 *
 * Revision 1.6  2006/01/12 19:07:09  rmikk
 * Added code to use Peak_new's data instead of Peak's data
 *
 * Revision 1.5  2005/08/03 19:34:12  rmikk
 * Updated the gridNum variable so it will show on the output
 *
 * Revision 1.4  2005/01/10 15:28:50  dennis
 * Removed unused imports.
 *
 * Revision 1.3  2004/07/30 14:28:00  rmikk
 * Eliminated unused Variables
 *
 * Revision 1.2  2004/07/30 14:11:07  rmikk
 * Fixed javadoc error
 *
 * Revision 1.1  2004/07/14 16:07:49  rmikk
 * Initial Checkin.
 * This class finds peaks when detectors are not vertical
 *
 */

package DataSetTools.operator.Generic.TOF_SCD;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.*;

import java.text.DecimalFormat;

import Command.ScriptUtil;
import DataSetTools.dataset.*;
import DataSetTools.math.tof_calc;
import DataSetTools.instruments.*;
import DataSetTools.trial.*;

/**
 * This class contains variables describing a voxel(peak) in three formats:
 * real(xcm,ycm,wl), pixel( x,y,z {col,row,time chan}) and hkl. It also stores
 * information to convert between these forms. The full list of conversion 
 * information is: UB matrix, chi,phi, omega,timeAdjustment, xscale or TO and 
 * T1, initial path, and grid type information. The grid type information is
 * used to detect the center, #row,#cols, width, height, and orientation of 
 * the detector.
 * 
 * Internally, just the x,y,z format is considered accurate.  Since all but 
 * the UB matrix is required for any instance of a peak, all the other forms are
 * calculated. the values corresponding to the other forms are set, they are
 * converted to x,y,z.
 *
 * The calibration data can be changed and any output should reflect this 
 * change.
 *   
 */

public class Peak_new extends Peak {
  // instance variables
  private int       seqnum   = 0;
  private float     h        = 0;
  private float     k        = Float.NaN;
  private float     l        = Float.NaN;
  private float     x        = Float.NaN;
  private float     y        = Float.NaN;
  private float     z        = Float.NaN;
  private float     t        = 0f;//time
  private float     wl       = Float.NaN;
  private int       ipkobs   = 0;
  private float     inti     = 0f;
  private float     sigi     = 0f;
  private int       reflag   = 0;
  private int       nrun     = 0;
  private float     nearedge = 0;
  private float     detA     = Float.NaN;
  private float     detA2    = Float.NaN;
  private float     chi      = Float.NaN;
  private float     phi      = Float.NaN;
  private float     omega    = Float.NaN;
  private float     monct    = 0f;
  private IDataGrid grid = null;
  private float[][] UB       = null;
  private float[][] invUB    ;
  private float[][] ROT      ;
  private float[][] invROT   ;
  private float     L1       = Float.NaN;
  private float     T0       = Float.NaN;
  private float     T1       = Float.NaN;

  private SampleOrientation orient = null;

  private float     timeAdjustment = 0f;  //Added to T0.T1 or xscale entries
                                          // To get actual times
  private XScale    xscale     = null;
  private Vector3D  peakPt     = null;
  private boolean   needUpdate = true;
  
  /* --------------------Constructor Methods-------------------- */
  /**
   *  A Peak with the null constructor.
   *  @param x   The x(col) value of the peak
   *  @param y   The y(row) value of the peak
   *  @param z   The z( time channel starting at 0) value of the peak
   *  @param grid  The grid information about the corresponding detector
   *  @param orient          Sample Orientation object for the particular
   *                         goniometer type used.  This provides the
   *                         chi, phi, omega, goniometer roation and
   *                         inverse goniometer rotation for the specific
   *                         type of goniometer.
   *  @param timeAdjustment  The time(for calibration) added to the times
   *                         in the xscale to get actual times
   *  @param xscale   The xscale for the times
   *  @param initialPath  the length(m) of the primary flight path.
   */
  public Peak_new( float x, 
                   float y, 
                   float z, 
                   IDataGrid         grid,
                   SampleOrientation orient, 
                   float             timeAdjustment,
                   XScale            xscale, 
                   float             initialPath ){ 
      this.x = x;
      this.y = y;
      this.z = z;
      this.orient = orient;
      this.Grid( grid );                // needed to set the super class detnum
      this.L1 = initialPath;
      this.timeAdjustment = timeAdjustment;
      
      this.chi=orient.getChi();
      this.phi= orient.getPhi();
      this.omega= orient.getOmega();
      time( xscale);

      needUpdate = false;
      setT0T1( z, xscale);
      if( grid != null ){
         Vector3D pos = grid.position();
         float[] pp = pos.get();
         
         detA2=( (float)(180.0f*Math.asin( pp[2]/pos.length())/Math.PI));
         
         detA=(float)(180*Math.atan2(pp[1],pp[0])/Math.PI);
         setUpRot( grid);
         update_xcm_ycm_wl();
      }
  }
/**
 *  Creates a new Peaks_new object from the information below
 *  
 * @param DS  The DataSet
 * @param GridID  The detector ID for the grid
 * @param x       The x posision, col, of the peak starting at 1
 * @param y       The y position, row, of the peak starting at 1
 * @param z       The time channel of the peak starting at 0
 * @return      The newly created Peaks object
 */
 public static Peak_new getNewPeak_xyz( DataSet DS, int GridID, float x, float y, float z, int seqNum) throws IllegalArgumentException{
    if(DS == null)
       throw new IllegalArgumentException(" Null DataSet");
    IDataGrid grid = Grid_util.getAreaGrid( DS, GridID);
    if( grid == null){
       throw new IllegalArgumentException(" No such Grid ID for this data set");
      
    }
    SampleOrientation  sampOrient = ( SampleOrientation )DS.getAttributeValue(  DataSetTools.dataset.Attribute.SAMPLE_ORIENTATION );
    if( sampOrient == null)
       throw new IllegalArgumentException( "This Dataset has no Sample Orientation");
    Float T = ((Float)DS.getAttributeValue( DataSetTools.dataset.Attribute.T0_SHIFT ) );
    float T0, initialPath;
    if( T == null )
        T0 = 0f;
     else 
        T0 = T.floatValue();
   XScale xscl = DS.getData_entry( 0 ).getX_scale();
   if( xscl == null)
      throw new IllegalArgumentException( "This DataSet does not have an XScale");
   Float I = ((Float)DS.getData_entry( 0 ).getAttributeValue( DataSetTools.dataset.Attribute.INITIAL_PATH ) );
   if( I == null )
      initialPath = 0f;
   else
      initialPath = I.floatValue();
   Peak_new Res = new Peak_new( x,y,z, grid, sampOrient, T0, xscl, initialPath);
   
   Res.seqnum( seqNum);
   
   int[] Runs = (int[])DS.getAttributeValue( Attribute.RUN_NUM);
   if( (Runs == null) ||(Runs.length <=0))
      Runs = (int[])DS.getData_entry(0).getAttributeValue( Attribute.RUN_NUM);
   
   if( Runs != null) if( Runs.length > 0)
      Res.nrun( Runs[0]);
   
   int row = (int)(y+.5);
   int col =(int) (x+.5);
   int chan =(int)( z+.5);
   float pkIntensity = 0;
   if( (row >0))if( col >0) if( chan >=0)
      if( row <=grid.num_rows())if( col <=grid.num_cols()){
         Data D = grid.getData_entry(row, col);
         float[] Ys= D.getY_values();
         if( Ys != null)if( chan < Ys.length)
            pkIntensity = Ys[chan];
      }
   Res.ipkobs((int) pkIntensity );  
   
   
   return Res;
   
 }
 /*
public static Peak_new getNewPeak_hkl( DataSet DS, int GridID, float h, float k, float l, float[][]UB){
    
    IDataGrid grid = Grid_util.getAreaGrid( DS, GridID);
    if( grid == null)
       return null;
    SampleOrientation  sampOrient = ( SampleOrientation )DS.getAttributeValue(  DataSetTools.dataset.Attribute.SAMPLE_ORIENTATION );
    if( sampOrient == null)
       return null;
    Float T = ((Float)DS.getAttributeValue( DataSetTools.dataset.Attribute.T0_SHIFT ) );
    float T0, initialPath;
    if( T == null )
        T0 = 0f;
     else 
        T0 = T.floatValue();
   XScale xscl = DS.getData_entry( 0 ).getX_scale();
   Float I = ((Float)DS.getData_entry( 0 ).getAttributeValue( DataSetTools.dataset.Attribute.INITIAL_PATH ) );
   if( I == null )
      initialPath = 0f;
   else
      initialPath = I.floatValue();
   float[] Q = new float[3];
   
   return new Peak_new( x,y,z, grid, sampOrient, T0, xscl, initialPath);
 }
 */
 private void update_xcm_ycm_wl(){ 
   // NOTE xcm, ycm now calculated when needed
   this.wl =DataSetTools.math.tof_calc.Wavelength(
                    L1 + grid.position(y,x).length(), this.t );
 }
 

 private void setUpRot(IDataGrid grid){
    ROT = null;
    invROT =null;
    if( grid == null) return;
    
    float[][] rot = orient.getGoniometerRotation().get();
    
    float[][]invrot =orient.getGoniometerRotationInverse().get();
    ROT = new float[3][3];
    invROT = new float[3][3];
    for( int i=0; i<3;i++)
      for( int j=0; j<3;j++){

        ROT[i][j]=rot[i][j];
        invROT[i][j] = invrot[i][j];
     }
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
  
  
  public float detA2( float d2 ){
    return detA2;
  }
  

  public float detA2(){
    return detA2;
  }


  public float detD(){
     return grid.position().length()*100;
  }
  

  /**
   *  Assumes that the center is in scattering plane
   */
  public float detA(){
    return detA; 
   
   /* float[] f= grid.position().get();
    return (float)( Math.asin( 100*f[1]/detD())/Math.PI*180);
  */
  }

  
  public void sethkl( float H, float K, float L,boolean propogate){
    this.h=H;
    this.k=K;
    this.l=L;
    if( (!propogate) ||( UB== null)){
      return;
    }

    float[] Q = new float[3];
    for( int i=0; i<3;i++)
       Q[i] = UB[i][0]*H+UB[i][1]*K+UB[i][2]*L;
    Vector3D QQ = new Vector3D( (float)(Q[0]*2*Math.PI),
                                (float)(Q[1]*2*Math.PI),
                                (float)(Q[2]*2*Math.PI) );
    float[] RC = VecQToTOF.RCofQVec( QQ, grid);
    this.y = RC[0];
    this.x = RC[1];
    this.t = VecQToTOF.TofofQVec( QQ,grid, L1+grid.position( y,x).length());
    update_xcm_ycm_wl();
    this. z= xscale.getI_GLB(t);
  }
  
  
  /**
   * Mutator method for index
   *
   * @param H   The H value
   * @param K   The K value
   * @param L   The L value
   * NOTE: These values will be propogated unless UB==null
   * the other representations.
   */
  public void sethkl( float H, float K, float L){
    sethkl(K, K, L, true);
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
    setT0T1( z, xscale);
    
    this.pixel_to_real();
    if(UB!=null)
      this.real_to_hkl();
    needUpdate=false;
   }
  
  
  /**
   * Accessor method for setting what times the peak lies
   * between. Something must be done to pixel_to_real to recreate
   * this.
   */
  public void times(float t0, float t1){
    this.T0=t0;
    this.T1=t1;
    needUpdate = true;
  }

  
  /**
    *  Set the xscale and T0 and T1 to correspond to the current z
    */
  public void time( XScale time){
    this.xscale=time;
    setT0T1( z, time);
  }
  

  // Sets T0  and T1  to the bin boudaries of the xscale with channel z.
  private void setT0T1(float z, XScale time){
    if( !Float.isNaN(z) && (time != null)){
           float t0= time.getX((int)z);
           float t1 =time.getX((int)z+1);
           times(t0,t1);
           this.t= t0 +(t1-t0)*(z-(int)z);
        }
  }

  
  /**
    *  Sets the calibrated time adjustment that is added to the nominal times
    *  from xscale.
    *  @param dt  The new value for the calibrated time adjustment
    */
  public float timeAdjust( float dt){
    this.timeAdjustment = dt;
    needUpdate=true;
    return dt;
  }
  

  /**
    *  Returns the calibrated time adjustment value.  This time is added to
    *  the times in the xscale to get actual times
    */
  public float timeAdjust(){
    return this.timeAdjustment;
  }


  /**
    *  Sets the grid to be used as a conversion value
    *  @param  grid   The new grid value
    */
  public void Grid( IDataGrid  grid){
    needUpdate = true;
    this.grid = grid;
    
    if( grid != null ){
         Vector3D pos = grid.position();
         //pos.multiply( -1.0f);
         float[] pp = pos.get();
         detA2=( (float)(180.0f*Math.asin( pp[2]/pos.length())/Math.PI));
         
         detA=(float)(180*Math.atan2(pp[1],pp[0])/Math.PI);
         setUpRot(grid);
         update_xcm_ycm_wl();
      }  
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
   * Sets the x,y, and z values to correspond to XCM, YCM, and WL values
   * 
   */
  public void real(float XCM, float YCM, float WL){
	this.x = grid.col(XCM/100, YCM/100);
	this.y = grid.row(XCM/100, YCM/100);
    
    peakPt = getPeakPosition();
    this.wl=WL;
    
    this.real_to_pixel();
    this.real_to_hkl();
    needUpdate=false;
  }
  

  /**
    * Returns the xcm, ycm, and wl corresponding to this peak
    */
  public float[] get_real(){
    if(needUpdate)get_hkl();
    float[] Res = new float[3];
    Res[0] = xcm();
    Res[1] = ycm();
    Res[2] = wl;
    return Res;
  }


   /**
     *  Returns the h,k, and l values corresponding to this peak.  
     *  If the UB matrix has not been set then the Identity UB matrix is used
     */
   public float[] get_hkl(){
     if( needUpdate){
       pixel_to_real();
       real_to_hkl();
     }
    needUpdate=false;
     float[] Res = new float[3];
     Res[0]=h;
     Res[1] = k;
     Res[2] = l;
     return Res;
   }

   
  public float h(){
     if( needUpdate)get_hkl();
     return h;
   }


  public float k(){
    if( needUpdate)get_hkl();
    return k;
  }  


  public float l(){
    if( needUpdate)get_hkl();
    return l;
  }


  public float xcm(){
    return 100*grid.x(y,x);
  }


  public float ycm(){
    return 100*grid.y(y,x);
  }


  public float wl(){
    if( needUpdate)get_hkl();
    return wl;
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
    return grid.ID();
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

    float dx=Math.abs((float)MINX-this.x);
    float dy=Math.abs((float)MINY-this.y);
    float dz=Math.abs((float)MINY-this.z);
    float min=1000.0f;
    
    if(dx>Math.abs((float)MAXX-this.x)){
      dx=Math.abs((float)MAXX-this.x);
    }
    if(dy>Math.abs((float)MAXY-this.y)){
      dy=Math.abs((float)MAXY-this.y);
    }
    if(dz>Math.abs((float)MAXZ-this.z)){
      dz=Math.abs((float)MAXZ-this.z);
    }
    
    if(dx<min) min=dx;
    if(dy<min) min=dy;
    if(dz<min) min=dz;
    
    this.nearedge=min;
    return this.nearedge();
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
      needUpdate=true;
      this.L1=path;
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
    needUpdate =true;
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
    
    needUpdate = true;  
    if(ub==null){
      this.UB= null;
      invUB = null;
      return; 
    }
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
  }


  /* --------------------- private methods --------------------- */
  /**
   * Method to allow easier update of real space calculation
   */
  private void pixel_to_real(){
    if( grid == null)
      return;
    if( !Float.isNaN(this.x)){
   
    peakPt = getPeakPosition();
    }
    if(!Float.isNaN(this.L1)&&!Float.isNaN(T0)&&
                                               !Float.isNaN(T1)){
      float wl1 = CalcWL(timeAdjustment,x,y,z,T0,T1,L1);
      if( !Float.isNaN(wl1))
         wl =wl1;
    }else{
      System.out.println("L1=0");
      return;
    }
  }


  private float CalcWL( float tt, 
                        float x, 
                        float y, 
                        float z, 
                        float T0,
                        float T1, float L1){
       if(Float.isNaN(tt)) return Float.NaN;
       if(Float.isNaN(x))  return Float.NaN;
       if(Float.isNaN(y))  return Float.NaN;
       if(Float.isNaN(z))  return Float.NaN;
       if(Float.isNaN(T0)) return Float.NaN;
       if(Float.isNaN(T1)) return Float.NaN;
       if(Float.isNaN(L1)) return Float.NaN;
       if(peakPt==null)    return Float.NaN;
      
       float time = timeAdjustment+(1-z+(int)z)*T0+ (z-(int)z)*T1;     
       float wl= tof_calc.Wavelength( peakPt.length()+L1,time );
     
       return wl;
   }
   
   
  private void real_to_pixel(){
   
    if( Float.isNaN( xcm() ))
       return;
    //if( grid == null) return;
    if(grid != null){ // this is the old method
      this.x=grid.col(this.xcm()/100f,this.ycm()/100f);
      this.y=grid.row(this.xcm()/100f,this.ycm()/100f);
    }
    wl_to_z();
  }
  
  
  /**
   *  Attempts to set up some z value(keeps fixed) from wl, etc.
   *
   */
  private void wl_to_z(){
    if(peakPt != null)
    if(!Float.isNaN(z)) return;
    if(!Float.isNaN(this.L1)&& !Float.isNaN(wl) ){
                                        
      float time = tof_calc.TOFofWavelength(peakPt.length()+L1,wl);
      time = time-timeAdjustment;
      int indx=-1;
      if(xscale == null){
        if( !Float.isNaN(z)){
          T0 = time -(z-(int)z);
          T1 =T0+1;
        }else{
          z=0;
          T0=time;
          T1=T0+1;
        }
      }else{
        indx = xscale.getI_GLB(time);
        T0 = xscale.getX(indx);
        T1 = xscale.getX(indx+1);
        z =  indx + (time - T0)/(T1-T0);
      }
      
    }else{
      System.out.println("L1=0");
      return;
    }
  }


  private void real_to_hkl(){
    // get the Q-vector/2pi
    
    if(Float.isNaN(xcm()))
        return;
    if( UB== null) 
       return;
    if(ROT == null) 
       return;
    float[] Qvec=getQ();
    
    if(Qvec==null)
       return;

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

 
  /**
   * Despite its name this actually calculates 1/d (Qvec/2PI).
   */
  private float[] getQ(){
     peakPt = this.getPeakPosition();
     
     if(peakPt== null)
      return null;
     if( Float.isNaN(L1))
       return null;
     if( Float.isNaN(wl))
       return null;
     if( Float.isNaN(z))
       return null;
     if( Float.isNaN(T0))return null;
     if(Float.isNaN(T1)) return null;
     if( wl <=0)
       return null; 
     //float time1 =DataSetTools.math.tof_calc.TOFofWavelength(
     //   grid.position(y,x).length()+L1, wl);
    float zz = z;
    float time = (1-zz+(int)zz)*T0+ //xscale.getX((int)zz)+
      (zz-(int)zz)*T1;//xscale.getX(1+(int)zz);
   //*** added time adjustments
    Vector3D T = DataSetTools.math.tof_calc.DiffractometerVecQ(
        peakPt,L1,time+timeAdjustment );
    T.multiply((float)(1.0f/2/Math.PI));
    return T.get();
  }
  
  
  /**
   * Returns 1/d vector (Q/2PI) in crystal reference frame.
   */
   public double[] getUnrotQ(){
    if( needUpdate)get_hkl();
    float[]   Q=this.getQ();
    
    if( Q == null) return null;
       //float[][] invROT=LinearAlgebra.getInverse(this.ROT);
    double[]  unrotQ={0.,0.,0.};

    for( int i=0; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        unrotQ[i]=unrotQ[i]+(double)(invROT[i][j]*Q[j]);
      }
    }
    return unrotQ;
  }
 

  /* --------------------- clone method --------------------- */
  /**
   *  Produce a copy of the object.
   */
  public Object clone(){
    //if( needUpdate)get_hkl();
   Peak_new peak=new Peak_new(x,y,z,grid,orient,
       timeAdjustment, xscale, L1);
    peak.ipkobs(ipkobs);
    peak.nrun(nrun);
    peak.inti(inti);
    peak.sigi(sigi);
    peak.reflag(reflag);
    peak.seqnum(seqnum);
    peak.nearedge=this.nearedge();
    peak.t=this.t;
    peak.monct = monct;
    peak.nearedge = nearedge;
    peak.monct(this.monct);
    
    if ( UB != null )                  
      peak.UB( UB );    // also copy the UB matrix, which also sets UB inverse
    else{
      peak.UB(null);
       peak.sethkl( this.h, this.k, this.l, false); 
       }
    return peak;
  }
  

  /* --------------------- equals method --------------------- */
  /**
   * Returns true if the Object is a Peak, with the same hkl, real
   * space representation, pixel representation, and intensities.
   */
  public boolean equals(Object obj){
    if(obj==null) return false; // one of the general rules according to java
    if(!(obj instanceof Peak_new)) return false; // object must be a peak

   Peak_new other=null;
    try{ // convert for convenience
      other=(Peak_new)obj;
    }catch(ClassCastException e){
      return false;
    }
    if(other==null) return false; // something went wrong in casting
    
    // check pixel space
    if(this.x != other.x) return false;
    if(this.y != other.y) return false;
    if(this.z != other.z) return false;

    // check intensities
    if(this.ipkobs != other.ipkobs) return false;
    if(this.inti   != other.inti  ) return false;
    if(this.sigi   != other.sigi  ) return false;
    if( this.detnum() !=other.detnum()) return false;
    if( this.nrun != other.nrun) return false;
    return true;
  }


  /* ------------------- toString method -------------------- */
  /**
   *  Format the toString method to be the full version of how it
   *  can be specified. This is in the Schultz format.
   */
  public String toString( ){
    String rs="";	
    if( needUpdate)get_hkl();//adjust all fields to correspond to x,y,z
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

    rs=rs+format(df_se_tw.format(xcm()),7)
      +format(df_se_tw.format(ycm()),7)
      +format(df_ei_fo.format(wl),8)
      //+format(df_ei_fo.format(t),8)
      +format(ipkobs,6)
      +format(df_ni_tw.format(inti),9)
      +format(df_ni_tw.format(sigi),9)
      +format(reflag,5)+format(nrun,6)+format(detnum(),3);
    
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
  
  
  private Vector3D  getPeakPosition(){
    return grid.position(y,x);
  }
    
  public static void main( String[] args){
     java.util.Vector peaks = (java.util.Vector)(new ReadPeaks( "tae70.integrate")).getResult();
     if( peaks == null){
        System.out.println("No Peaks");
        return;
     }
     DataSet DS=null;
     try{
        DataSet[] DSS =ScriptUtil.load( "c:/Isaw/SampleRuns/scd011050.run");
        DS = DSS[DSS.length-1];
     }catch(Exception s){
        System.out.println("Cannot read file "+s.toString());
        return;
     }
     
     for( int i=0; i< peaks.size(); i++){
        Peak pk = (Peak)peaks.elementAt(i);
        int gridNum = pk.detnum();
        IDataGrid grid = Grid_util.getAreaGrid( DS, gridNum);
        System.out.print(pk.h()+","+pk.k()+","+pk.l()+","+pk.xcm()+","+pk.ycm()+","+pk.wl()+",");
        Vector3D v = grid.position( pk.y(), pk.x());
        v.multiply(1/v.length());
        float scatAng = (float)Math.acos(v.dot( new Vector3D(1.0f,0f,0f)));
        double theta =2*180/Math.PI*Math.asin( Math.sin(scatAng/2)/2/pk.wl());
        System.out.println( scatAng+","+theta);
     }
     
  }
  public static void main1( String[] args){
    
    DataSetTools.dataset.UniformXScale xscl= 
                         new DataSetTools.dataset.UniformXScale(200,950,100);
    
    UniformGrid grid = new UniformGrid( 21,"m",
                              new Vector3D(.3f,.3f,0f), 
                              new Vector3D(1f,-1f,0f),
                              new Vector3D(0f,0f,1f),
                              .2f, .2f, .01f, 50, 50 );
    Peak_new pk1=new Peak_new( 1.2f,3.5f,4.1f,grid,
                               new IPNS_SCD_SampleOrientation(0f,0f,0f), 
                               12f, xscl, 5f);
    
    float[][]UB ={{1.0f,0f,0f},{0f,1f,0f},{0f,0f,1f}};
  
    pk1.UB(UB);
    
    System.out.println("-------------------Peak_new--------------");
    System.out.println(pk1.toString());

   float[] hkl = pk1.get_hkl();
   float[] f= pk1.get_real();
   pk1.real( f[0],f[1],f[2]);
   
   System.out.println("A"+pk1.x()+","+pk1.y()+","+pk1.z());
   System.out.println(pk1.toString());
   pk1.sethkl( hkl[0],hkl[1],hkl[2]);
  
   System.out.println("B"+pk1.x()+","+pk1.y()+","+pk1.z());
   System.out.println(pk1.toString());
   UB[0][0]=0f; UB[1][1]=0f; UB[2][2]=0f;
   UB[0][1]=1f; UB[1][2]=1f;  UB[2][0]=1f;
   pk1.UB(UB);
   hkl = pk1.get_hkl();
   f= pk1.get_real();
   System.out.println("-------------------Peak_new--------------");
   System.out.println(pk1.toString());
   pk1.real( f[0],f[1],f[2]);
   
      System.out.println("A"+pk1.x()+","+pk1.y()+","+pk1.z());
      System.out.println(pk1.toString());
      pk1.sethkl( hkl[0],hkl[1],hkl[2]);
  
      System.out.println("B"+pk1.x()+","+pk1.y()+","+pk1.z());
      System.out.println(pk1.toString());
    }
}