/* 
 * File: Peak_new.java
 *
 * Copyright (C) 2008, Ruth Mikkelson, Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 * NOTE: This class is a simplified version of the Peak.java class 
 *       written by Peter Peterson.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package DataSetTools.operator.Generic.TOF_SCD;

import java.text.DecimalFormat;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Position3D;
import gov.anl.ipns.MathTools.Geometry.Tran3D;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import DataSetTools.dataset.IDataGrid;
import DataSetTools.instruments.SampleOrientation;
import DataSetTools.math.tof_calc;

public class Peak_new implements IPeak_IPNS_out
{
  // This first group of fields that determine the peak are IMMUTABLE.
  // They should NOT be changed after the peak has been constructed.
  private int               run_num;
  private float             monct;
  private IDataGrid         grid;               // grid units are in meters
  private SampleOrientation sample_orientation; 
  private float             l1;                 // initial path (meters)
  private float             t_zero;
  
  private float             col,
                            row,
                            chan;
  
  private float             wl;
  
  private float             qx;   // NOTE: These MUST be CALCULATED at 
  private float             qy;   //       construction time, based on
  private float             qz;   //       grid, col, row, l1 and wl 

  //
  // The following group of fields are MUTABLE
  //
  private String facility_name   = "NO_FACILITY";
  private String instrument_name = "NO_INSTRUMENT";

  private float[][]  UB = null;
  private float      h_index = 0.0f;
  private float      k_index = 0.0f;
  private float      l_index = 0.0f;

  private int        seqnum = 0;  // NOTE: THIS SHOULD BE REMOVED
  private int        ipkobs = 0;
  private float      inti   = 0;
  private float      sigi   = 0;
  private int        reflag = 0;
  
  /* --------------------Constructor Methods-------------------- */
  /**
   *  Construct a new peak object using explicitly specified values for
   *  all of the immutable fields in the object. 
   *
   *  @param run_num      The run number that produced this peak
   *  @param monct        The monitor count for this run
   *  @param col          The col where the peak is located. NOTE: This may
   *                      be a fractional value giving the estimated peak 
   *                      centroid.
   *  @param row          The row where the peak is located: NOTE: This may
   *                      be a fractional value giving the estimated peak 
   *                      centroid.
   *  @param chan         The time channel (starting at 0) value of the peak
   *  @param grid         The grid information about the corresponding 
   *                      detector
   *  @param orientation  Sample Orientation object for the particular
   *                      goniometer type used.  This provides the chi, phi 
   *                      and omega angles, as well as the matrices providing
   *                      the goniometer rotation and inverse goniometer 
   *                      rotation for the specific type of goniometer.
   *  @param tof          The corrected time-of-flight in microseconds at 
   *                      which this peak occurs.
   *  @param initial_path The length(m) of the primary flight path.
   *  @param t_zero       The calibrated time shift.  The corrected time of
   *                      flight it T + t_zero where T is the measured time
   *                      of flight.  The wavelength is assumed to have
   *                      been calculated using the corrected time of flight.
   *                      That is, the tof parameter must be T + t_zero.
   */
  public Peak_new( int               run_num,
                   float             monct,
                   float             col, 
                   float             row, 
                   float             chan, 
                   IDataGrid         grid,
                   SampleOrientation orientation, 
                   float             tof,
                   float             initial_path,
                   float             t_zero )
  {
    this.run_num       = run_num;
    this.monct         = monct;
    this.col           = col;
    this.row           = row;
    this.chan          = chan;
    this.grid          = grid;
    sample_orientation = orientation;
    float l2 = grid.position( row, col ).length();
    float wavelength = tof_calc.Wavelength( initial_path+l2, tof );
    this.wl            = wavelength; 
    this.l1            = initial_path;
    this.t_zero        = t_zero;
    Vector3D vec_q = tof_calc.DiffractometerVecQ( grid.position( row, col ), 
                                                  initial_path, tof );
    // Temporarily deal with Q' = Q/(2PI).  TODO fix this
    this.qx = (float)(vec_q.getX()/Math.PI/2);
    this.qy = (float)(vec_q.getY()/Math.PI/2);
    this.qz = (float)(vec_q.getZ()/Math.PI/2);
  }
  
  @Override
  public float L1()
  {
    return l1;
  }

  @Override
  public float[][] UB()
  {
    if ( UB == null )
      return null;
    else
      return LinearAlgebra.copy(  UB  );
  }

  @Override
  public void UB( float[][] UB ) throws IllegalArgumentException
  {
    if ( UB == null )    // reset current UB matrix to null
    {
      this.UB = UB;
      return;
    }
     
    if ( UB.length != 3 )
      throw new IllegalArgumentException("ERROR:UB matrix without 3 rows");
    
    for ( int i = 0; i < 3; i++ )
      if ( UB[i] == null || UB.length != 3 )
        throw new IllegalArgumentException("ERROR:Bad row in UB matrix");

    if ( h_index != 0 || k_index != 0 || l_index != 0 )
      throw new IllegalArgumentException("Can't specify UB if h,k,l !=0,0,0");

    set_HKL_from_UB( UB );
    
    this.UB = LinearAlgebra.copy( UB );
  }

  
  private void set_HKL_from_UB( float[][] UB )
  {
    float UB_times_2PI[][] = new float[3][3];
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        UB_times_2PI[row][col] = UB[row][col];
                               // TODO fix this.  For now Q = Q' = Q/(2PI)
                               // (float)( 2 * Math.PI * UB[row][col] );

    Tran3D inv_orientation_tran = new Tran3D( UB_times_2PI );
    inv_orientation_tran.invert();
    double[] q_vals = getUnrotQ();
    Vector3D q_vec = new Vector3D( (float)q_vals[0], 
                                   (float)q_vals[1],
                                   (float)q_vals[2] );
    Vector3D hkl_vec = new Vector3D();
    inv_orientation_tran.apply_to( q_vec, hkl_vec );
    h_index = hkl_vec.getX();
    k_index = hkl_vec.getY();
    l_index = hkl_vec.getZ();
  }
  
  @Override
  public float chi()
  {
    return sample_orientation.getChi();
  }
  
  @Override
  public float phi()
  {
    return sample_orientation.getPhi();
  }
  
  @Override
  public float omega()
  {
    return sample_orientation.getOmega();
  }
 
   
  public IPeak createNewPeakxyz( float x, float y, float z, float tof )
  { 
    Peak_new new_peak = new Peak_new( run_num,
                                      monct,
                                      x, y, z, 
                                      grid,
                                      sample_orientation, 
                                      tof,
                                      l1,
                                      t_zero );
    new_peak.facility_name   = facility_name;
    new_peak.instrument_name = instrument_name;

    new_peak.UB = UB;           // Should this be a deep copy to a new matrix?
                                // What form is the UB?  As in matrix file
                                // or transposed, etc.
                                // if UB != null, calculate new h,k,l for this
                                // x,y,z, else copy over h,k,l
    if ( UB != null )
      new_peak.set_HKL_from_UB( UB );
    else
    {
      new_peak.h_index = h_index;
      new_peak.k_index = k_index;
      new_peak.l_index = l_index;
    }
                                // TODO should the following be copied over?
    new_peak.seqnum = seqnum;
    new_peak.ipkobs = ipkobs;
    new_peak.inti   = inti;
    new_peak.sigi   = sigi;
    new_peak.reflag = reflag;
    return new_peak;
  }
  
  @Override
  public int detnum()
  {
    return grid.ID();
  }

  public IDataGrid getGrid()
  {
    return grid;
  }
  
  public SampleOrientation getSampleOrientation()
  {
    return sample_orientation;
  }
  
  @Override
  public float[] getQ()
  {
    float[] qxyz = new float[3];
    qxyz[0] = qx;
    qxyz[1] = qy;
    qxyz[2] = qz;
    return qxyz;
  }

  @Override
  public double[] getUnrotQ()    // TODO Why is this double?
  {
    Vector3D q_vec = new Vector3D( qx, qy, qz );
    Tran3D   tran  = sample_orientation.getGoniometerRotationInverse();
    tran.apply_to( q_vec, q_vec );
    float[] q_vals = q_vec.get();

    double[] q_vals_d = new double[3];
    for ( int i = 0; i < 3; i++ )
      q_vals_d[i] = q_vals[i];

    return q_vals_d;
  }

  @Override
  public float h()
  {
    return h_index;
  }
  
  @Override
  public float k()
  {
    return k_index;
  }

  @Override
  public float l()
  {
    return l_index;
  }
  
  @Override
  public float inti()
  {
    return inti;
  }

  @Override
  public float inti( float inti )
  {
    this.inti = inti;
    return inti;
  }

  @Override
  public int ipkobs()
  {
    return ipkobs;
  }

  @Override
  public int ipkobs( int pkObs )
  {
    this.ipkobs = pkObs;
    return ipkobs;
  }

  @Override
  public float monct()
  {
    return monct;
  }
  
  @Override
  public float monct( float monitorCount )
  {
    this.monct = monitorCount;
    return monct;
  }
  
  @Override
  public float nearedge()  // TODO should be named edgeDistance
  {
    int i_row = (int)row;
    int i_col = (int)col;
    int max_row = grid.num_rows();
    int max_col = grid.num_cols();
                                          // consider invalid row/col numbers
                                          // to be on the edge.
    if ( i_row < 1 || i_row > max_row )
      return 0;
    
    if ( i_col < 1 || i_col > max_col )
      return 0;
    
    int row_dist = Math.min( i_row-1, max_row-i_row );
    int col_dist = Math.min( i_col-1, max_col-i_col );
      
    return Math.min( row_dist, col_dist );
  }

  @Override
  public int nrun()
  {
    return run_num;
  }

  @Override
  public int reflag()
  {
    return reflag;
  }

  @Override
  public int reflag( int flag )
  {
    reflag = flag;
    return flag;
  }

  @Override
  public int seqnum()
  {
    // System.out.println("Obsolete seqnum() method called");
    return seqnum;
  }

  @Override
  public int seqnum( int seq )
  {
    // System.out.println("Obsolete seqnum(seq) method called");
    seqnum = seq;
    return seqnum;
  }

  @Override
  public void setFacility( String facilityName )
  {
    this.facility_name = facilityName;
  }

  @Override
  public void setInstrument( String instrumentName )
  {
    this.instrument_name = instrumentName;
  }

  @Override
  public void sethkl( float h, float k, float l )
      throws IllegalArgumentException
  {
    if ( UB != null )
      throw new IllegalArgumentException("Peak_new: can't set hkl if UB!=null");
    h_index = h;
    k_index = k;
    l_index = l;
  }

  @Override
  public float sigi()
  {
    return sigi;
  }

  @Override
  public float sigi( float sig )
  {
    sigi = sig;
    return sigi;
  }

  @Override
  public float time()
  {
    Vector3D position = grid.position( row, col ); 
    float l2 = position.length();
    
    return tof_calc.TOFofWavelength( l1+l2, wl );
  }

  @Override
  public float wl()
  {
    return wl;
  }
  
  /**
   *  Calculate the d-spacing corresponding to the current peak.  For now
   *  this returns 1/q instead of 2PI/q
   */
  public float d()
  {
    Vector3D vec_q = new Vector3D( qx, qy, qz );
//  return (float)(Math.PI * 2 / vec_q.length() );   // d "should be 2PI/q"
    return 1.0f/vec_q.length();                      // but users of this 
                                                     // just expect 1/q
  }                                                  // TODO: Fix this

  @Override
  public float x()
  {
    return col;
  }

  @Override
  public float y()
  {
    return row;
  }

  @Override
  public float z()
  {
    return chan;
  }
  
  public float[] NeXus_coordinates()
  {
    Vector3D pixel_pos = grid.position( row, col );
    Position3D det_pos = new Position3D();
                                                 // NOTE: Since x,y,z are currently
                                                 // in IPNS coords, the xyz coords
                                                 // are permuted. TODO fix this
    det_pos.setCartesianCoords( pixel_pos.getY(), 
                                pixel_pos.getZ(), 
                                pixel_pos.getX() );

    return det_pos.getSphericalCoords();
  }
  
  @Override
  public float xcm()
  {
    return grid.x( row, col ) * 100;   // assuming grid size is in meters
  }

  @Override
  public float ycm()
  {
    return grid.y( row, col ) * 100;  // assuming grid size is in meters
  }


  /**
   *  Accessor method for the detector angle in degrees
   */
  public float detA()
  {
    Vector3D position = grid.position();
    double angle_radians = Math.atan2( position.getY(), position.getX() );
    return (float)(angle_radians * 180.0/Math.PI);
  }
  
  /**
   *  Accessor method for the second detector angle in degrees
   */
  public float detA2()
  {
    Vector3D position = grid.position();
    double length = position.length();
    double angle_radians = Math.asin( position.getZ()/length );
    return (float)(angle_radians * 180.0/Math.PI);
  }
  
  /**
   *  Accessor method for the detector distance which is given in cm.
   */
  public float detD()
  {
    return 100 * grid.position().length();
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
    
    rs="3"+format(seqnum,6)+format(h(),4)+format(k(),4)+format(l(),4);
    if(Float.isNaN(x())){
      rs=rs+"     "+x();
    }else{
      rs=rs+format(df_se_tw.format(x()),7);
    }
    if(Float.isNaN(y())){
      rs=rs+"     "+y();
    }else{
      rs=rs+format(df_se_tw.format(y()),7);
    }
    if(Float.isNaN(z())){
      rs=rs+"     "+z();
    }else{
      rs=rs+format(df_se_tw.format(z()+1),7); // internally z is stored as one 
                                            // less than the file representation
    }
    rs=rs+format(df_se_tw.format(xcm()),7)
      +format(df_se_tw.format(ycm()),7)
      +format(df_ei_fo.format(wl()),8)
      //+format(df_ei_fo.format(t),8)
      +format(ipkobs,6)
      +format(df_ni_tw.format(inti()),9)
      +format(df_ni_tw.format(sigi()),9)
      +format(reflag,5)+format(nrun(),6)+format(detnum(),3);
    
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
}
