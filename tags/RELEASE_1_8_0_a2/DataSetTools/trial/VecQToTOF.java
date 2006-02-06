/*
 * File: VecQToTOF.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.23  2006/01/17 03:44:30  dennis
 *  Made all instance variables private.
 *
 *  Revision 1.22  2004/08/11 15:02:53  dennis
 *  Removed unneeded debug print.
 *
 *  Revision 1.21  2004/07/14 16:48:22  dennis
 *  Modified the method intensityAtQ() to work for DataSets containing
 *  functions as well as histograms.
 *
 *  Revision 1.20  2004/07/01 16:55:14  rmikk
 *  Added new Static methods for converting between the Q Vector, x,y,time
 *  and xcm,ycm, and wl, using Grids instead of DataSets.  These methods 
 *  start with Q in lab coordinates
 *
 *  Revision 1.19  2004/05/03 16:26:14  dennis
 *  Removed unused local variable.
 *
 *  Revision 1.18  2004/03/15 06:10:54  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.17  2004/03/15 03:28:45  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.16  2004/03/10 17:52:44  dennis
 *  Put print in if (debug) statement
 *
 *  Revision 1.15  2004/03/01 06:16:14  dennis
 *  Fixed bug in QtoRowColChan().  Now properly returns null if
 *  call to QtoRowColTOF() returns null.
 *
 *  Revision 1.14  2004/02/28 21:23:07  dennis
 *  Made constructors public so this class can be used by more classes.
 *  Further simplified calculation of transform from Q to Row, Col, TOF.
 *
 *  Revision 1.13  2004/01/24 23:42:31  dennis
 *  Changed a few variable names to improve readability.
 *
 *  Revision 1.12  2004/01/07 15:04:39  dennis
 *  Fixed copyright information.
 *
 *  Revision 1.11  2004/01/05 23:02:37  dennis
 *    Simplified calculation of real space scattering direction corresponding
 *  to a specified Q vector.  (Calculation now based on colision being
 *  elastic, so k, k' and scattering direction vector form an isosceles
 *  triangle.)
 *    Also, IntensityAtQ( q_vec ) now returns -1 if the scattering vector
 *  does not intersect the detector.
 *
 *  Revision 1.10  2003/08/11 22:14:29  dennis
 *  Now shifts tof by calibrated t0 value.
 *
 *  Revision 1.9  2003/07/14 13:25:58  dennis
 *  The constructor now throws an instantiation error if the
 *  requested area detector was not found.
 *
 *  Revision 1.8  2003/06/04 19:11:50  dennis
 *  Fixed bug in calculation of row, col, TOF based on Q.
 *  QToXcmYcmWl() now returns x, y values in centimeters.
 *  Fixed bug with finding the nth area detector.  (This
 *  should be further improved.)
 *
 *  Revision 1.7  2003/06/04 13:56:20  dennis
 *  Now supports two area detectors in one DataSet. Added constructor
 *    VecQToTOF( ds, det_num ).
 *  The constructors now throw InstantiationErrors if the DataSet is
 *  not a time-of-flight DataSet or if it does not have the needed
 *  attributes.  Also added convenience methods to get the underlying
 *  DataGrid and the goniometer rotation matrices:
 *    getDataGrid()
 *    getGoniometerRotation()
 *    getGoniometerRotationInverse()
 *
 *  Revision 1.6  2003/06/03 16:33:02  dennis
 *  Separated calculation of interpolated intensity from calculation of
 *  corresponding detector position info.  Added methods:
 *  QtoRowColTOF()
 *  QtoRowColChan()
 *  QtoXcmYcmWl()
 *
 *  Revision 1.5  2003/05/25 20:09:06  dennis
 *  1. The method to calculate the intensity at a point now interpolates
 *     using the values at the centers of eight neighboring bins.
 *  2. Constructor now checks that the DataSet is a time of flight DataSet.
 *  3  Constructor saves one copy of the time bins, IF the Data blocks
 *     share a common time scale, so that the time bins don't need to be
 *     recalculated, or obtained from each Data block when needed.
 *
 *  Revision 1.4  2003/05/24 21:49:10  dennis
 *  Major improvements:
 *  1. No longer converts it's DataSet to Q, so different x-scales for
 *     each pixel are no longer needed.  Also, it just needs a reference
 *     to the Data associated with the DataGrid, so the Data blocks are
 *     not cloned.  This reduces memory requirements.
 *  2. Now uses a UniformGrid from the DataSet, rather than maintaining its
 *     own set of references to the Data blocks.
 *  3. Now works with the first area detector from any DataSet, including
 *     the new area detector on SCD.  (Still needs minor changes to work
 *     with DataSets containing data from multiple area detectors.)
 *
 *  Revision 1.3  2003/02/18 20:19:53  dennis
 *  Switched to use SampleOrientation attribute instead of separate
 *  phi, chi and omega values.
 *
 *  Revision 1.2  2003/01/08 21:50:09  dennis
 *  Now calculated detector center, height, width & number of pixels from
 *  two corner pixels, assuming a square detector and that the spectra are
 *  ordered starting from the lower left hand corner.  This should work for
 *  both the old and new detectors on SCD.
 *
 *  Revision 1.1  2003/01/08 17:41:59  dennis
 *  This class handles the mapping from a vector Q value back to the
 *  time-of-flight data for SCD.  This initial version only works for "old"
 *  SCD DataSets.
 *
 */

package DataSetTools.trial;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Numeric.*;

import DataSetTools.math.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.instruments.*;

/** 
 *  This class is responsible for mapping a vector Q point to an interpolated
 *  point in the original time-of-flight DataSet for ONE area detector.
 *  It also provides methods to find the interpolated intensity given a 
 *  q vector, get the goniometer rotations used and get the underlying
 *  DataGrid for the detector. 
 */

public class VecQToTOF
{
  private  static final Vector3D unit_k = new Vector3D( 1, 0, 0 );
 
  private  boolean debug = false;  

  private IDataGrid grid;

  private Tran3D   goniometerR,
                   goniometerRinv;

  private Vector3D det_center;  // center position of detector.
  private Vector3D u, v, n;     // vectors defining coordinate axes on face of
                                // detector and perpendicular to the detector.
  private int      n_rows,
                   n_cols;

  private float    det_width,
                   det_height;

  private float    initial_path,
                   t0;

  private float    x_vals[] = null;

  private boolean  same_xscale = false;

  private Vector3D q_center;    // unit q_vector in direction of q from
                                // center of detector

  private float    min_q_dot;   // dot product of unit q vector in direction of
                                // corner pixel. (Used to quickly discard most
                                // cases where q would not have come from this 
                                // detector.

  /* --------------------------- constructor ------------------------- */
  /**
   *  Construct a VecQToTOF conversion object, from the first 
   *  area detector DataGrid from in the specified DataSet.
   *  The DataGrid must have had it's Data block references set, and the
   *  Data blocks must be in terms of time-of-flight.  It must also have
   *  a SampleOrientation defined and the initial flight path defined.
   *
   *  @param  ds   The DataSet from which the first area detector's DataGrid
   *               is used.
   */
  public VecQToTOF( DataSet ds )
  {
    this( ds, 1 );
  }


  /* --------------------------- constructor ------------------------- */
  /** 
   *  Construct a VecQToTOF conversion object, from the specified 
   *  area detector DataGrid from in the specified DataSet.
   *  The DataGrid must have had it's Data block references set, and the
   *  Data blocks must be in terms of time-of-flight.  It must also have
   *  a SampleOrientation defined and the initial flight path defined.
   *
   *  @param  ds   The DataSet from which the first area detector's DataGrid
   *               is used.
   *
   *  @param  det_num  Should be an integer, 1,2,3... which specifies whether
   *                   the first, second, third, etc. area detector from the
   *                   DataSet is used.
   */
  public VecQToTOF( DataSet ds, int det_num )
  {
    if ( ds == null )
      throw new InstantiationError(
                "ERROR: DataSet null in VecQToTOF constructor" );

    String units = ds.getX_units();
    if ( !units.equalsIgnoreCase( "Time(us)" ) )
      throw new InstantiationError(
                "ERROR: need time-of-flight DataSet in VecQToTOF constructor" );
    
    int n_data = ds.getNum_entries();
    int num_area_detectors_found = 0;

    Data d = null;
    PixelInfoList pil;
    Attribute attr;
    boolean area_detector_found  = false;
    boolean right_detector_found = false;
    int data_index = 1;
    while ( !right_detector_found && data_index < n_data )
    {
      while ( !area_detector_found && data_index < n_data )
      {
        d = ds.getData_entry( data_index );
        attr = d.getAttribute( Attribute.PIXEL_INFO_LIST );
        if ( attr != null && attr instanceof PixelInfoListAttribute )
        {
          pil  = (PixelInfoList)attr.getValue();
          grid = pil.pixel(0).DataGrid(); 
          if ( grid.num_rows() > 1 && grid.num_cols() > 1 )
          {
            area_detector_found = true;
            num_area_detectors_found++;
            if ( det_num == num_area_detectors_found )
              right_detector_found = true;
                                                              // skip the 
            data_index += grid.num_rows() * grid.num_cols();  // other pixels
          }                                                   // in this grid
        }
        else 
        {
          System.out.println(
             "ERROR: need PixelInfoList attribute in VecQToTOF constructor");
          return;
        }
        data_index++;
      }
      if ( !right_detector_found )
        area_detector_found = false;    // start looking for the next area det
    }

    if ( !right_detector_found )
    {
      if ( debug )
        System.out.println("Didn't find ith area detector, i = " + det_num );
      throw new InstantiationError("ERROR:Didn't find area det#"+det_num);
    }

    if ( !grid.isData_entered() )
      if ( !grid.setData_entries( ds ) )
      throw new InstantiationError(
                "ERROR: Can't set Data grid entries in VecQToTOF constructor");

    attr = d.getAttribute( Attribute.INITIAL_PATH );
    if ( attr != null )
      initial_path = (float)(attr.getNumericValue());
    else
      throw new InstantiationError(
                "ERROR: Need initial path in VecQToTOF constructor");

    attr = d.getAttribute(Attribute.T0_SHIFT);
    if ( attr != null )
      t0 = (float)attr.getNumericValue();
    else
      t0 = 0;

//    System.out.println("DataGrid is : " + grid );
//    System.out.println("initial path: " + initial_path);

    n_rows = grid.num_rows();
    n_cols = grid.num_cols();
                             // to make searching quicker, save one copy of the
                             // bin boundaries for the TOF for this detector

    same_xscale = true;
    XScale xscale = grid.getData_entry( 1, 1 ).getX_scale();
    int col;
    int row = 1;
    while ( same_xscale && row <= n_rows )
    { 
      col = 1;
      while ( same_xscale && col <= n_cols ) 
      {
        if ( xscale != grid.getData_entry( row, col ).getX_scale() )
          same_xscale = false; 
        col++;
      }
      row++;
    }
    if ( same_xscale )
      x_vals = grid.getData_entry( 1, 1 ).getX_scale().getXs();

    u = grid.x_vec();
    v = grid.y_vec();
    n = grid.z_vec();
    det_width  = grid.width();
    det_height = grid.height();

    Vector3D corner1 = grid.position( 1, 1 );    
    Vector3D corner2 = grid.position( grid.num_rows(), grid.num_cols() );
    Vector3D temp = new Vector3D( corner1 );
    temp.subtract( corner2 );
/*
    System.out.println("n_rows,cols = " + n_rows + ", " + n_cols );
    System.out.println("u = " + u );
    System.out.println("v = " + v ); 
    System.out.println("n = " + n ); 
    System.out.println("det_width, height = " + det_width + 
                                         ", " + det_height  );
*/
    det_center = new Vector3D( corner1 );   // find "center" as average of
    det_center.multiply( 0.5f );            // two opposite corners
    temp.set( corner2 );
    temp.multiply( 0.5f );
    det_center.add( temp );

    SampleOrientation orientation =
        (SampleOrientation)ds.getAttributeValue(Attribute.SAMPLE_ORIENTATION);

    goniometerR    = orientation.getGoniometerRotation();
    goniometerRinv = orientation.getGoniometerRotationInverse();    

//    System.out.println("R    = " + goniometerR );
//    System.out.println("Rinv = " + goniometerRinv );

    // To allow for quickly discarding q's that couldn't come from this 
    // detector & chi,phi,omega, we keep a unit vector in the direction of
    // the q vector corresponding to the the detector center(q_center) as well 
    // as the dot product between q_center and a unit vector in the 
    // direction of a q vector corresponding to the detector corner(q_corner).
    // This dot product is min_q_dot.  An arbitrary q_vec is discarded if
    // the dot product between q_vec and q_center is less than min_q_dot.
                                             
                                                     // first find q_center
    q_center = new Vector3D( det_center );    
    q_center.normalize();
    q_center.subtract( unit_k ); 
    q_center.normalize();
    goniometerRinv.apply_to( q_center, q_center );
                                                     // then find q_corner at
                                                     // both corners.
    Vector3D q_corner1 = new Vector3D( corner1 );
    q_corner1.normalize();
    q_corner1.subtract( unit_k );
    q_corner1.normalize();
    goniometerRinv.apply_to( q_corner1, q_corner1 );

    Vector3D q_corner2 = new Vector3D( corner2 );
    q_corner2.normalize();
    q_corner2.subtract( unit_k );
    q_corner2.normalize();
    goniometerRinv.apply_to( q_corner2, q_corner2 );
                                                     // finally get min_q_dot
    min_q_dot = Math.min( q_center.dot(q_corner1), q_center.dot(q_corner2) );
  }   


 /* ---------------------------- getDataGrid ----------------------------- */
  
  public IDataGrid getDataGrid()
  {
    return grid;
  }


  /* ------------------------- getGoniometerRotation ---------------------- */
 
  public Tran3D getGoniometerRotation()
  {
    return goniometerR;
  }

  /* ---------------------- getGoniometerRotationInverse -------------------- */
  
  public Tran3D getGoniometerRotationInverse()
  {
    return goniometerRinv;
  }


 /* -------------------------- QtoXcmYcmWl ------------------------------- */
 /**
  *  Transform from vector Q to Xcm, Ycm, wavelength.
  *
  *  @param   q_vec   The q vector to be mapped back to the detector
  *
  *  @return  An array containing the Xcm, Ycm and wavelength 
  *  values corresponding to this q_vector.  If the q vector did not come 
  *  from this detector this will return null.
  */
  public float[] QtoXcmYcmWl( Vector3D q_vec )
  {
    float result[] = QtoRowColTOF( q_vec );
    if ( result == null )
      return null;

    float row = result[0];
    float col = result[1];
    float tof = result[2];
 
    result[0] = grid.x( row, col ) * 100;         // convert to cm
    result[1] = grid.y( row, col ) * 100;

    float final_path = grid.position( row, col ).length();
    result[2] = tof_calc.Wavelength( initial_path + final_path, tof );

    return result;
  }


 /* -------------------------- QtoRowColChan ------------------------------- */
 /**
  *  Transform from vector Q to fractional row, column, channel.
  *
  *  @param   q_vec   The q vector to be mapped back to the detector
  *
  *  @return  An array containing the fractional row, column and channel 
  *  values corresponding to this q_vector.  Integer row and column numbers
  *  are assumed to range from 1 to n_rows and 1 to n_cols.  The pixel
  *  positions are assumed to be at the pixel centers, so the fractional
  *  row and column values range from 0.5 to n_rows+0.5 and from
  *  0.5 to n_cols+0.5.  The channel number returned is the channel number
  *  in the Data block for the pixel containing the fractional pixel position.
  *  If the q vector did not come from this detector, or if the Data blocks
  *  have not been set for this Data grid, this will return null.
  */
  public float[] QtoRowColChan( Vector3D q_vec )
  {
    float result[] = QtoRowColTOF( q_vec );
    if ( result == null )
      return null;
    
    if ( !same_xscale )
    {
      int row = Math.round( result[0] );
      int col = Math.round( result[1] );
      Data d = grid.getData_entry( row, col );
      x_vals = d.getX_scale().getXs();
    }
                                    // shift by calibrated t0, before doing
                                    // lookup in the time scale
    float tof = result[2] - t0;
    int index = arrayUtil.get_index_of( tof, x_vals );
    
    if ( index < 0 || index >= x_vals.length-1 )
      return null;

    float fractional_channel = ( tof             - x_vals[index] ) /
                               ( x_vals[index+1] - x_vals[index] );

    result[2] = index + fractional_channel;

    return result;
  }


 /* -------------------------- QtoRowColTOF ------------------------------- */
 /**
  *  Transform from vector Q to fractional row, column, time-of-flight.
  *
  *  NOTE: The time-of-flight is the "theoretical"
  *  time of flight required to get the specified q vector.  If this value
  *  is used to look up a time channel in an "x" scale, then the calibrated
  *  shift in time, "t0" must be subtracted from the value returned by this
  *  method.
  *
  *  @param   q_vec   The q vector to be mapped back to the detector
  *
  *  @return  An array containing the fractional row, column and time-of-flight
  *  values corresponding to this q_vector.  Integer row and column numbers
  *  are assumed to range from 1 to n_rows and 1 to n_cols.  The pixel 
  *  positions are assumed to be at the pixel centers, so the fractional
  *  row and column values range from 0.5 to n_rows+0.5 and from 
  *  0.5 to n_cols+0.5.  The time-of-flight is NOT restricted to valid values
  *  for the current Data.  If the q vector did not come from this detector 
  *  this will return null.  
  */
  public float[] QtoRowColTOF( Vector3D q_vec )
  {
    if ( debug )
    {
      System.out.println("**************************************************");
      System.out.println("Using Grid #" + grid.ID() );
    }

    float threshold = q_vec.dot( q_center );
    if ( threshold <= 0 )
      return null;
  
    float mag_q = q_vec.length();
    threshold = threshold/mag_q;
    if ( threshold < min_q_dot )
      return null;

    Vector3D q_lab = new Vector3D();                 // q in lab coordinates
    goniometerR.apply_to( q_vec, q_lab );

    if ( debug )
    {
      System.out.println("q_vec = " + q_vec );
      System.out.println("q_lab = " + q_lab );
    }

    // The following simple calculation of k' from q is easily derived from 
    // the vector equation: q = k'-k.  First, we assume that the 
    // magnitudes of k and k' are equal, and that k is in the direction of
    // the positive x axis.  It follows that the x component of q must be 
    // negative for a valid solution to exist.  Rearranging the equation,
    // we have k' = q + k.  Since k is in the positive x direction, k' matches
    // q except for a change in the x component of q.  The vector diagram
    // for q = k'-k forms an isoceles triangle, with two of the angles 
    // being theta.  Drop a perpendicular from the vertex between k & k'
    // to center of the side q.  Then clearly, ||k|| = ||q||/(2 cos(theta)).
    // Also, cos(theta) = (q dot (-k)) /(||q|| ||k||), and since k is a multiple
    // of the unit vector i in the x direction, this simplifies to 
    // cos(theta) = -qx / ||q||.  Replacing cos(theta) by this expression 
    // in the equation giving ||k|| in terms of ||q|| and simplifying yields
    // ||k|| = - ||q||^2/(2 qx).  Since k' = q + k and k is in the positive
    // x direction, we just need to add ||k|| to the x component of q to get k'
    //
    float q[] = q_lab.get();
    if ( q[0] >= 0 )          // no solution, since q is in the wrong direction
      return null;

    float mag_k = -(mag_q * mag_q) / (2*q[0]);
    Vector3D k_prime = new Vector3D( q[0] + mag_k, q[1], q[2] );
    
    float t = det_center.dot(n)/k_prime.dot(n);
    if ( t <= 0 )                // no solution, since the beam missed the
      return null;               // detector plane
    
    Vector3D det_point = new Vector3D( k_prime );
    det_point.multiply( t );
    Vector3D pix_position = new Vector3D( det_point );

    det_point.subtract( det_center );
    float u_comp = det_point.dot( u );
    float v_comp = det_point.dot( v );
    if ( debug )
    {
      System.out.println("k_prime = " + k_prime );
      System.out.println("t = " + t );
      System.out.println("u, v components = " + u_comp + ", " + v_comp );
    }
                                              // get "fractional" and integer
                                              // row and col values for this
                                              // pixel.
    float f_row = grid.row( u_comp, v_comp );

    int row = Math.round( f_row );
    if ( row < 1 || row > n_rows )
      return null;

    float f_col = grid.col( u_comp, v_comp );

    int col = Math.round( f_col ); 
    if ( col < 1 || col > n_cols )
      return null;

    float final_path = pix_position.length();
    pix_position.normalize();
    float angle = (float)( Math.acos( pix_position.dot( unit_k ) ));

    if ( debug )
    {
      System.out.println("f_row = " + f_row );
      System.out.println("f_col = " + f_col );
      System.out.println("Scattering angle = " + angle);
    }

    float tof = tof_calc.TOFofDiffractometerQ( angle,
                                               initial_path+final_path,
                                               mag_q );
    float result[] = new float[3];
    result[0] = f_row;
    result[1] = f_col;
    result[2] = tof;
    return result;
}


/* -------------------------- intensityAtQ ------------------------------ */
/**
 *  Calculate the interpolated intensity based on a specified q vector.  
 *  The DataGrid given to the constructor must have had it's Data block
 *  references set at construction time, in order for this to work.  If
 *  The specified q vector does not come from this DataGrid, this returns
 *  0.
 *
 *  @param q_vec  The vector q for which the intensity is to be interpolated.
 *
 *  @return The interpolated intensity based on eight neighboring data 
 *  cells, or -1 if the the q_vector does not correspond to this DataGrid.
 */

  public float intensityAtQ( Vector3D q_vec )
  {
    float rc_tof[] = QtoRowColTOF( q_vec );
    if ( rc_tof == null )
      return -1;

    float f_row = rc_tof[0];
    float f_col = rc_tof[1];

    float tof = rc_tof[2];

    if ( debug )
      System.out.println( "q = " + q_vec + 
                          " r,c,tof = " + f_row +
                          ", " + f_col +
                          ", " + tof );

    int row = Math.round( f_row );
    int col = Math.round( f_col ); 

    int first_row,                     // find adjacent rows and cols
        last_row;
    int first_col, 
        last_col;
    if ( f_row > row )
    {
      first_row = row;
      last_row = row + 1;
    }
    else
    {
      first_row = row-1;
      last_row = row;
    }

    if ( f_col > col )
    {
      first_col = col;
      last_col = col + 1;
    }
    else
    {
      first_col = col-1;
      last_col = col;
    }
                                       // ignore points without four neighbors 
    if ( first_row < 1 || last_row > n_rows )
      return -1;                  
                                
    if ( first_col < 1 || last_col > n_cols )
      return -1; 

//    float tof = rc_tof[2];
    float mag_q = q_vec.length();

                                             // interpolate intensity at eight 
    float val[][] = new float[2][2];         // neighboring pixels    
    float first_mid,
          last_mid;
    int   first_index,
          last_index;
    float tof_frac;
    
    for ( int i = 0; i < 2; i++ )
      for ( int j = 0; j < 2; j++ )
    {
      row = first_row + i;
      col = first_col + j;
      Data d = grid.getData_entry( row, col );
      if ( d == null )
      {
        System.out.println("ERROR: No Data block at row, col = " + row + 
                                                            ", " + col );
        return -1;
      }
   
      Vector3D pix_position = grid.position(row,col);
      float    final_path = pix_position.length();
      pix_position.normalize();
      float    angle = (float)( Math.acos( pix_position.dot( unit_k ) ));
 
                                              // before finding tof in x-scale
                                              // shift to account for
                                              // calibrated t0
      tof = tof_calc.TOFofDiffractometerQ( angle, 
                                           initial_path+final_path, 
                                           mag_q )  - t0;
      if ( !same_xscale )
        x_vals = d.getX_scale().getXs();

      int index = arrayUtil.get_index_of( tof, x_vals );

      float y_vals[] = d.getY_values();
      if ( index <= 0 || index >= y_vals.length - 1 )  // ignore first and last
        val[i][j] = 0.0f;                              // bin since we can't 
      else                                             // interpolate
      {
        if ( d.isHistogram() )
        {
          float bin_mid = (x_vals[index] + x_vals[index+1]) / 2;
          if ( tof > bin_mid )
          {
            first_index = index;
            last_index = index+1;
            first_mid = bin_mid;
            last_mid = (x_vals[last_index] + x_vals[last_index+1]) / 2;
          }
          else
          {
            first_index = index-1;
            last_index = index;
            first_mid = (x_vals[first_index] + x_vals[first_index+1]) / 2;
            last_mid = bin_mid;
          }
        }
        else   // must be function, so x-values are already at bin centers
        {
          first_index = index;
          last_index  = index+1;
          first_mid = x_vals[index];
          last_mid  = x_vals[last_index];
        }

        tof_frac = (tof - first_mid)/(last_mid - first_mid);
        val[i][j] = (1-tof_frac) * y_vals[first_index] + 
                        tof_frac * y_vals[last_index];
      }
    }

    float row_frac = f_row - first_row;
    float col_frac = f_col - first_col;
    float intensity_0 = (1 - row_frac) * val[0][0] + row_frac * val[1][0] ;
    float intensity_1 = (1 - row_frac) * val[0][1] + row_frac * val[1][1] ;
    float intensity = (1 - col_frac) * intensity_0 + col_frac * intensity_1;

//    float y[] = d.getY_values();
//    float x[] = d.getX_scale().getXs();

//    for ( int i = 0; i < y.length; i++ )
//      System.out.println("i, x, y = " + i + ", " + x[i] + ", " + y[i] );

//  System.out.println("mag_q = " + mag_q );
//  System.out.println("tof   = " + tof   );
//  System.out.println("intensity = " + intensity );

    return intensity;
  }


  /* ------------------------- getDetectorCenter ------------------------- */
  /**
   * Gets an accurate position of the detector's center
   * @param grid  The grid object describing the detector
   * @return  The position in 3D for the center of the grid
   */
  private static Vector3D getDetectorCenter( IDataGrid grid )
  {
    Vector3D corner1 = grid.position( 1, 1 );    
    Vector3D corner2 = grid.position( grid.num_rows(), grid.num_cols() );
    Vector3D temp = new Vector3D( corner1 );
    temp.subtract( corner2 );
    Vector3D det_center = new Vector3D( corner1 );   // find "center" as average
    det_center.multiply( 0.5f );                     // of two opposite corners
    temp.set( corner2 );
    temp.multiply( 0.5f );
    det_center.add( temp );
    return det_center;
 }
 
 
  /* -----------------------------  RCofQVec ----------------------------- */
  /**
   * Determines the row and column of the pixel on grid corresponding to the
   * Q vector( the Q with 2PI in lab coordinates). 
   * @param Qlab_w2pi  The Q vector with 2PI in lab coordinates
   * @param grid       The grid describing the detector
   * @return  two floats: the first is the row(index) and the second is 
   *          the column
   */
  public static float[] RCofQVec( Vector3D Qlab_w2pi, IDataGrid grid )
  {
    if( Qlab_w2pi == null)
      return null;
    if( grid == null)
      return null;  
    float[] q = Qlab_w2pi.get();
    if ( q[0] >= 0 )          // no solution, since q is in the wrong direction
      return null;
    float mag_q = Qlab_w2pi.length();
    float mag_k = -(mag_q * mag_q) / (2*q[0]);
    Vector3D n =grid.z_vec();
   
    Vector3D k_prime = new Vector3D( q[0] + mag_k, q[1], q[2] );
    Vector3D det_center = getDetectorCenter( grid );
    float t = det_center.dot(n)/k_prime.dot(n);
    if ( t <= 0 )                // no solution, since the beam missed the
      return null;               // detector plane
    
    Vector3D det_point = new Vector3D( k_prime );
    det_point.multiply( t );
    //Vector3D pix_position = new Vector3D( det_point );

    det_point.subtract( det_center );
    float u_comp = det_point.dot( grid.x_vec() );
    float v_comp = det_point.dot( grid.y_vec() );
                                                 // get "fractional" and integer
                                                 // row and col values for this
                                                 // pixel.
    float f_row = grid.row( u_comp, v_comp );

    int row = Math.round( f_row );
    if ( row < 1 || row > grid.num_rows() )
      return null;

    float f_col = grid.col( u_comp, v_comp );

    int col = Math.round( f_col ); 
    if ( col < 1 || col > grid.num_cols() )
      return null;

    float[] Res = new float[2];
    Res[0]= f_row;
    Res[1] =f_col;
    return Res;
  }
  

  /* -------------------------- XcmYcmofQVec ----------------------------- */
  /**
   * Determines the distance from the center(cm) of the pixel on grid 
   * corresponding to the Q vector( the Q with 2PI in lab coordinates). 
   *
   * @param Qlab_w2pi  The Q vector with 2PI in lab coordinates
   * @param grid       The grid describing the detector
   *
   * @return  two floats: the first is the distance in the grid.x_vec 
   *           direction and the second is the distance(cm) in the
   *           grid.y_vec direction. 
   */
  public static float[] XcmYcmofQVec( Vector3D Qlab_w2pi, IDataGrid grid)
  {
    float[]RC = RCofQVec( Qlab_w2pi, grid);
    if( RC == null)
      return null;
    float[] Res = new float[2];
    Res[0] = grid.x(RC[0],RC[1])*100;
    Res[1] = grid.y(RC[0],RC[1])*100;
    return Res;
 }

 
 /* --------------------------- TofofQVec ------------------------------ */
 /**
  * Determines the TOF corresponding to the Q vector( the Q with 2PI in lab
  *      coordinates).  
  *
  * @param Qlab_w2pi            The Q vector with 2PI in lab coordinates
  * @param grid                 The grid describing the detector
  * @param initial_path_length  The length(m) from source to sample
  *
  * @return The time of flight for this Q vector.
  */
  public static float TofofQVec( Vector3D   Qlab_w2pi, 
                                 IDataGrid  grid, 
                                 float      initial_path_length )
  {
    float[] RC = RCofQVec(Qlab_w2pi, grid);
    if( RC == null)
     return Float.NaN;
    Vector3D pix_position = grid.position(RC[0],RC[1]);
    float final_path = pix_position.length();
    pix_position.normalize();
    float angle = (float)( Math.acos( pix_position.dot( unit_k ) ));

    float tof = tof_calc.TOFofDiffractometerQ( angle,
                                               initial_path_length+final_path,
                                               Qlab_w2pi.length() );
    return tof;                            
  }
  

  /* ---------------------------- WlofQVec ------------------------------ */
  /**
   * Determines the wavelength corresponding to the Q vector( the Q with 2PI 
   * in lab coordinates).  
   *
   * @param Qlab_w2pi  The Q vector with 2PI in lab coordinates
   * @param grid       The grid describing the detector
   * @param initial_path_length  The length(m) from source to sample
   *
   * @return The wavelength for this Q vector.
   */
  public static float WlofQVec( Vector3D  Qlab_w2pi, 
                                IDataGrid grid, 
                                float     initial_path_length ) 
  {
    float t = TofofQVec( Qlab_w2pi, grid, initial_path_length );
    return tof_calc.Wavelength( initial_path_length+Qlab_w2pi.length() , t );
  }
  
                                               
 /* ------------------------------ main ------------------------------ */
 /*
  *  main program for basic testing
  */

 public static void main( String args[] )
 {
   RunfileRetriever rr = new RunfileRetriever( 
                       "/usr/local/ARGONNE_DATA/SCD_QUARTZ/scd06496.run" );
   DataSet ds = rr.getFirstDataSet( Retriever.HISTOGRAM_DATA_SET );
   VecQToTOF transformer = new VecQToTOF( ds );
   System.out.println("Got Grid: " + transformer.getDataGrid() );

//   Vector3D q_vec = new Vector3D( 0.8701965f, 6.3653164f, 0.8469445f );
   Vector3D q_vec = new Vector3D( -0.31071144f, 6.3506145f, 0.8738657f );
//   Vector3D q_vec = new Vector3D( -0.48379692f, 10.031614f, 4.6503353f );
   float rc_tof[] = transformer.QtoRowColTOF( q_vec );

 
   System.out.println("Peak at: --------------------------------------- " );  
   System.out.println("Q is : " + q_vec );
   System.out.println("row = " + rc_tof[0] );  
   System.out.println("col = " + rc_tof[1] );  
   System.out.println("tof = " + rc_tof[2] );  

   float rc_chan[] = transformer.QtoRowColChan( q_vec );
   System.out.println("Peak at: --------------------------------------- " );  
   System.out.println("Q is : " + q_vec );
   System.out.println("row  = " + rc_chan[0] );  
   System.out.println("col  = " + rc_chan[1] );  
   System.out.println("chan = " + rc_chan[2] );  

   float xy_wl[]   = transformer.QtoXcmYcmWl( q_vec );
   System.out.println("Peak at: --------------------------------------- " );  
   System.out.println("Q is : " + q_vec );
   System.out.println("x  = " + xy_wl[0] );  
   System.out.println("y  = " + xy_wl[1] );  
   System.out.println("wl = " + xy_wl[2] );  

 }

}
