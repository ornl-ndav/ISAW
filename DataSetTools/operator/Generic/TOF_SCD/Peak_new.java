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

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Position3D;
import gov.anl.ipns.MathTools.Geometry.Tran3D;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import DataSetTools.dataset.IDataGrid;
import DataSetTools.instruments.SNS_SampleOrientation;
import DataSetTools.instruments.SampleOrientation;
import DataSetTools.math.tof_calc;

/**
 *  This class is a second pass at representing the key data for peaks,
 *  inspired by the original Peaks object put together by Peter Peterson.
 *  The goals were to make it simpler to use (fewer things mutable, more
 *  maintainable), and to use the detector grid concept, so that detectors
 *  and calibrations could better deal with area detectors in arbitrary
 *  positions and orientations.  Since this had to be implemented without
 *  breaking existing functionality, some further refinements to this
 *  concept are on hold, pending a complete separation of the new peak
 *  class from the original Peak class.  Currently there is an interface,
 *  IPeak that extracts some of the common functionality, but also partly
 *  binds the two implementations together.  Some of the work remaining to
 *  be done includes:
 * -- The calculation of nearedge MUST be be changed to allow for
 *    having bounds other than 1 & size.  Should the parameter-less
 *    nearedge be replaced by nearedge(minx,maxx,miny,maxy) in both
 *    the interface and in Peak_new?
 * -- methods detA, detA2, detD are not in interface but were needed
 *    for some places where Peak_new was used.  These should be removed
 *    as Peak and Peak_new are separated.
 * -- methods xcm, ycm are not needed for new format.  They should be
 *    removed from interface and Peak_new, as Peak and Peak_new are
 *    separated.
 * -- NeXus_coordinates method may belong in the interface, or perhaps
 *    in some utility class, if it doesn't get put in the interface.
 * -- Methods x,y,z should be renamed col, row, channel.
 * -- How should the instrument and facility name be handled?  Currently
 *    there are just getter's and setters for this in Peak_new but the
 *    getters are NOT in the interface.  These could be useful to have in
 *    the Peak_new class, IF we will need to deal with peaks from multiple
 *    instruments and multiple facilities.  In the mean time this just
 *    temporarily holds the values so that the information can be put
 *    at the top of the peaks file.
 * -- Methods to get and set the sequence number could be removed from
 *    the interface and from Peak_new, since the sequence numbers are
 *    just set by the order the peaks are written.
 * -- Methods to get the sample orientation and the grid were needed
 *    in practice, so they should probably be added to the interface.
 * -- The UB matrix could probably be removed IPeak and Peak_new, and
 *    have any methods that rely on this, just take a UB matrix as
 *    an additional parameter, rather than storing it in the peak object.
 * -- Currently, the t_zero shift is just stored (and returned by) the
 *    peak, but is not used by the peak.  Should this be removed?  It is
 *    handy when printing the peaks file, so that t_zero can be listed
 *    at the top, but that is all it is used for.
 * -- The new peaks file format should probably have l1 removed from the
 *    block of information at the start of a group of peaks, since
 *    that information is also recorded along with t_zero at the start
 *    of the file.
 * -- The d() method currently returns 1/|q| instead of 2pi/|q|, since
 *    that is what some old code expects.  This should be changed to
 *    return 2pi/|q| and code using this should be adjusted accordingly.
 * -- Currently, only the getUnrotQ() method returns double precision
 *    values.  That should probably be changed to return single, but
 *    the interface and old Peak object would also need to be changed.
 *    If the Peak_new and Peak can be separated, then either the
 *    getUnrotQ() could be changed to return floats, or the Peak_new
 *    class could be switched entirely to double.
 */

public class Peak_new implements IPeak_IPNS_out
{
  public static final String UNSPECIFIED = "UNSPECIFIED";

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
  private String facility_name   = UNSPECIFIED;
  private String instrument_name = UNSPECIFIED;

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
   *                      If a null value is passed in, a sample orientation
   *                      with phi=chi=omega=0 (i.e. the identity transform)
   *                      will be constructed by default.
   *  @param tof          The corrected time-of-flight in microseconds at 
   *                      which this peak occurs.
   *  @param initial_path The length(m) of the primary flight path.
   *  @param t_zero       The calibrated time shift.  The corrected time of
   *                      flight it T_measured + t_zero where T_measured
   *                      is the measured time of flight.  The wavelength 
   *                      is calculated from the specified time of flight, 
   *                      so the tof parameter MUST be passed in as 
   *                      T_measured + t_zero.  The t_zero parameter is NOT
   *                      used in this class, it is only required for 
   *                      information purposes.  It can be obtained from a
   *                      peak object, so that code using the peak object 
   *                      can find the corresponding measured time-of-flight.
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

    if ( orientation != null )
      sample_orientation = orientation;
    else
      sample_orientation = new SNS_SampleOrientation(0,0,0);
    
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


  /**
   *  Return the value of the t_zero shift that was specified when the 
   *  peak was constructed. 
   */
  public float T0()
  {
    return t_zero;
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
 

  @Override   
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


  /**
   *  Get the detector grid for this peak.
   *
   *  @return a reference to the grid.
   */ 
  public IDataGrid getGrid()
  {
    return grid;
  }
  
 
  /**
   *  Get SampleOrientation object for this peak.
   *
   *  @return a reference to the sample orientation.
   */
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


  /**
   *  Accessor method the get the facility name for this peak.
   *
   *  @return The string that was set as the facility name for this peak
   *          or UNSPECIFIED is none was set.
   */
  public String getFacility()
  {
    return this.facility_name;
  }


  @Override
  public void setInstrument( String instrumentName )
  {
    this.instrument_name = instrumentName;
  }


  /**
   *  Accessor method the get the instrument name for this peak.
   *
   *  @return The string that was set as the instrument name for this peak
   *          or UNSPECIFIED is none was set.
   */
  public String getInstrument()
  {
    return this.instrument_name;
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
   *  this returns 1/|q| instead of 2PI/|q|
   *
   *  @return the calculated d-spacing value corresponding to this peak.
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


  /**
   *  Get an array listing the NeXus coordinates of the pixel for this peak.
   *  
   *  @return An array of three values, r ( the sample to pixel distance ),
   *          az (the NeXus azimuthal angle) and two theta (the scattering 
   *          angle)
   */  
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
   *  Accessor method for the IPNS first detector angle in degrees, aka detA.
   *
   *  @return the angle between the beam direction and a vector from the
   *          sample to the detector, "in the horizontal plane".
   */
  public float detA()
  {
    Vector3D position = grid.position();
    double angle_radians = Math.atan2( position.getY(), position.getX() );
    return (float)(angle_radians * 180.0/Math.PI);
  }

  
  /**
   *  Accessor method for the IPNS second detector angle in degrees, aka detA2.
   *
   *  @return the angle between a vector from the sample to the detector,
   *          and the horizontal plane.
   */
  public float detA2()
  {
    Vector3D position = grid.position();
    double length = position.length();
    double angle_radians = Math.asin( position.getZ()/length );
    return (float)(angle_radians * 180.0/Math.PI);
  }
  

  /**
   *  Accessor method for the distance to the center of the detector,
   *  which is given in cm.
   *
   *  @return the sample to detector center distance in cm.
   */
  public float detD()
  {
    return 100 * grid.position().length();
  }
  

  /* ------------------- toString method -------------------- */
  /**
   *  Produce a String representation of this peak's specific
   *  information in the form of an "old" peaks file as was used
   *  with the IPNS SCD.
   */
  public String toString( )
  {
    return String.format( "3 %5d %3.0f %3.0f %3.0f" +
                          " %6.2f %6.2f %6.2f"      +
                          " %6.2f %6.2f %7.4f"      +
                          " %5d %8.2f %8.2f"        +
                          " %4d %5d %2d", 
                          seqnum, h_index, k_index, l_index,
                          col, row, (chan +1),
                          xcm(), ycm(), wl,
                          ipkobs, inti, sigi,
                          reflag, run_num, grid.ID() );
  }

}
