/*
 * File: VecQMapper.java
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
 *  Revision 1.2  2004/05/03 16:26:14  dennis
 *  Removed unused local variable.
 *
 *  Revision 1.1  2004/04/02 15:06:11  dennis
 *  Class for mapping from Q vectors to row,col,time, etc. for a single
 *  detector, accounting for goniometer rotations.  This was
 *  extracted from class VecQToTOF, keeping only the parts of the
 *  calculation that did not depend on a time scale and did not
 *  require interpolation in the data.  The original VecQToTOF class
 *  should be modified to extend this class. (TODO)
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

public class VecQMapper
{
  static final Vector3D unit_k = new Vector3D( 1, 0, 0 );
 
  boolean debug = false;  

  IDataGrid grid;

  Tran3D   goniometerR,
           goniometerRinv;

  Vector3D det_center;  // center position of detector.
  Vector3D u, v, n;     // vectors defining coordinate axes on face of
                        // detector and perpendicular to the detector.
  int      n_rows,
           n_cols;
  float    det_width,
           det_height;
  float    initial_path,
           t0;

  Vector3D q_center;    // unit q_vector in direction of q from center of
                        // detector
  float    min_q_dot;   // dot product of unit q vector in direction of
                        // corner pixel. (Used to quickly discard most
                        // cases where q would not have come from this 
                        // detector.

  /* --------------------------- constructor ------------------------- */
  /** 
   */
  public VecQMapper( IDataGrid  grid, 
                    float      initial_path, 
                    float      t0, 
                    SampleOrientation orientation  )
  {
    this.grid         = grid;
    this.initial_path = initial_path;
    this.t0           = t0;

    n_rows = grid.num_rows();
    n_cols = grid.num_cols();

    u = grid.x_vec();
    v = grid.y_vec();
    n = grid.z_vec();
    det_width  = grid.width();
    det_height = grid.height();

    Vector3D corner1 = grid.position( 1, 1 );    
    Vector3D corner2 = grid.position( grid.num_rows(), grid.num_cols() );
    Vector3D temp = new Vector3D( corner1 );
    temp.subtract( corner2 );

    det_center = new Vector3D( corner1 );   // find "center" as average of
    det_center.multiply( 0.5f );            // two opposite corners
    temp.set( corner2 );
    temp.multiply( 0.5f );
    det_center.add( temp );

    goniometerR    = orientation.getGoniometerRotation();
    goniometerRinv = orientation.getGoniometerRotationInverse();    

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

  /* --------------------- getGoniometerRotationInverse -------------------- */
  
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
    // Also, cos(theta) = (q dot (-k))/(||q|| ||k||), and since k is a multiple
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

 /* ------------------------------ main ------------------------------ */
 /*
  *  main program for basic testing
  */

 public static void main( String args[] )
 {
   RunfileRetriever rr = new RunfileRetriever( 
                       "/usr/local/ARGONNE_DATA/SCD_QUARTZ/scd06496.run" );
   DataSet ds = rr.getFirstDataSet( Retriever.HISTOGRAM_DATA_SET );

   IDataGrid grid = Grid_util.getAreaGrid( ds, 5 );

   Attribute attr;

   attr = grid.getData_entry( 1, 1 ).getAttribute( Attribute.INITIAL_PATH );
   float initial_path = (float)(attr.getNumericValue());

   float t0 = 0;
   attr = grid.getData_entry( 1, 1 ).getAttribute(Attribute.T0_SHIFT);
   if ( attr != null )
     t0 = (float)attr.getNumericValue();

   SampleOrientation orientation =
      (SampleOrientation)ds.getAttributeValue(Attribute.SAMPLE_ORIENTATION);

   VecQMapper transformer = new VecQMapper( grid, 
                                            initial_path, 
                                            t0,
                                            orientation );

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

   float xy_wl[]   = transformer.QtoXcmYcmWl( q_vec );
   System.out.println("Peak at: --------------------------------------- " );  
   System.out.println("Q is : " + q_vec );
   System.out.println("x  = " + xy_wl[0] );  
   System.out.println("y  = " + xy_wl[1] );  
   System.out.println("wl = " + xy_wl[2] );  
 }

}
