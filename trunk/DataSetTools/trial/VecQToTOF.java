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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
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

import java.io.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
import DataSetTools.components.ThreeD.*;
import DataSetTools.dataset.*;
import DataSetTools.instruments.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import DataSetTools.operator.DataSet.Math.Analyze.*;

/** 
 *  This class is responsible for mapping a vector Q point to an interpolated
 *  intensity value in the original time-of-flight DataSet.  NOTE: This version
 *  is still in the proof of concept stage and currently only handles the first
 *  area detector in a run.
 */

public class VecQToTOF
{
  static final Vector3D unit_k = new Vector3D( 1, 0, 0 );

  IDataGrid grid;

  Tran3D   goniometerRinv;

  Vector3D det_center;  // center position of detector.
  Vector3D u, v, n;     // vectors defining coordinate axes on face of
                        // detector and perpendicular to the detector.
  int      n_rows,
           n_cols;
  float    det_width,
           det_height;
  float    initial_path;

  Vector3D q_center;    // unit q_vector in direction of q from center of
                        // detector
  float    min_q_dot;   // dot product of unit q vector in direction of
                        // corner pixel. (Used to quickly discard most
                        // cases where q would not have come from this 
                        // detector.

  /** 
   *  Construct a VecQToTOF conversion object, from the specified DataGrid.
   *  The DataGrid must have had it's Data block references set, and the
   *  Data blocks must be in terms of time-of-flight.  It must also have
   *  a SampleOrientation defined and the initial flight path defined.
   *  Currently this assumes that beyond a few monitors, the DataSet contains
   *  the data from ONE area detector.
   */
  VecQToTOF( DataSet ds )
  {
    int n_data = ds.getNum_entries();
    if ( n_data < 100 )
    {
      System.out.println("ERROR VecQToTOF constructor needs area detector");
      return;
    }

    Data d = null;
    PixelInfoList pil;
    Attribute attr;
    boolean area_detector_found = false;
    int data_index = 1;
    while ( !area_detector_found && data_index < n_data )
    {
      d = ds.getData_entry( data_index );
      attr = d.getAttribute( Attribute.PIXEL_INFO_LIST );
      if ( attr != null && attr instanceof PixelInfoListAttribute )
      {
        pil  = (PixelInfoList)attr.getValue();
        grid = pil.pixel(0).DataGrid(); 
        if ( grid.num_rows() > 1 && grid.num_cols() > 1 )
          area_detector_found = true;
      }
      else 
      {
        System.out.println(
             "ERROR: need PixelInfoList attribute in VecQToTOF constructor");
        return;
      }
      data_index++;
    }

    if ( !grid.isData_entered() )
      if ( !grid.setData_entries( ds ) )
      {
        System.out.println("ERROR: Can't set Data grid entries in VecQToTOF");  
        return;
      }

//    System.out.println("DataGrid is : " + grid );

    attr = d.getAttribute( Attribute.INITIAL_PATH );
    if ( attr != null )
      initial_path = (float)(attr.getNumericValue());
    else
      return;

    n_rows = grid.num_rows();
    n_cols = grid.num_cols();
    u = grid.x_vec();
    v = grid.y_vec();
    n = grid.z_vec();
    det_width  = grid.width();
    det_height = grid.height();

    Vector3D corner1 = grid.position( 0, 0 );    
    Vector3D corner2 = grid.position( grid.num_rows()-1, grid.num_cols()-1 );
    Vector3D temp = new Vector3D( corner1 );
    temp.subtract( corner2 );
    float diag = temp.length();
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

    Tran3D goniometerR = makeGoniometerRotationInverse( ds );
    goniometerRinv = new Tran3D();
    goniometerRinv.set( goniometerR );
    goniometerRinv.transpose();

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
    goniometerR.apply_to( q_center, q_center );
                                                     // then find q_corner at
                                                     // both corners.
    Vector3D q_corner1 = new Vector3D( corner1 );
    q_corner1.normalize();
    q_corner1.subtract( unit_k );
    q_corner1.normalize();
    goniometerR.apply_to( q_corner1, q_corner1 );

    Vector3D q_corner2 = new Vector3D( corner2 );
    q_corner2.normalize();
    q_corner2.subtract( unit_k );
    q_corner2.normalize();
    goniometerR.apply_to( q_corner2, q_corner2 );
                                                     // finally get min_q_dot
    min_q_dot = Math.min( q_center.dot(q_corner1), q_center.dot(q_corner2) );
  }   


 /* -------------------------- intensityAtQ ------------------------------- */
 /**
  *
  */
  public float intensityAtQ( Vector3D q_vec )
  {
    float threshold = q_vec.dot( q_center );
    if ( threshold <= 0 )
      return 0;
  
    threshold = threshold/q_vec.length();
    if ( threshold < min_q_dot )
      return 0;

    Vector3D q1 = new Vector3D();
    goniometerRinv.apply_to( q_vec, q1 );
    q1.normalize();

//    System.out.println("q_vec = " + q_vec );
//    System.out.println("q1 = " + q1 );

    float q1_comp[] = q1.get();
    float alpha = -2*q1_comp[0];

    if ( alpha <= 0 )             // no solution, since the direction of q1 
      return 0;                   // would be reversed using alpha < 0 

    q1.multiply( alpha );
    Vector3D k_prime = new Vector3D( unit_k );
    k_prime.add( q1 );

//    System.out.println("k_prime = " + k_prime );

    float t = det_center.dot(n)/k_prime.dot(n);
    if ( t <= 0 )                // no solution, since the beam missed the
      return 0;                  // detector plane
    
//    System.out.println("t = " + t );

    Vector3D det_point = new Vector3D( k_prime );
    det_point.multiply( t );

    det_point.subtract( det_center );
    float u_comp = det_point.dot( u );
    float v_comp = det_point.dot( v );
//    System.out.println("u, v components = " + u_comp + ", " + v_comp );

                                  // NOTE: the rounding MUST be checked !!!!!
    int row = (int)grid.row( u_comp, v_comp );
    int col = (int)grid.col( u_comp, v_comp ); 
//    System.out.println("row, col = " + row + ", " + col );
 
    if ( row < 1 || row > n_rows )
      return 0; 

    if ( col < 1 || col > n_cols )
      return 0; 

    float mag_q = q_vec.length();

    Data d = grid.getData_entry( row, col );
    if ( d == null )
    {
      System.out.println("ERROR: No Data block at row, col = " + row + 
                                                          ", " + col );
      return 0;
    }
   
    Vector3D pix_position = grid.position(row,col);
    float final_path = pix_position.length();
    pix_position.normalize();
    float angle = (float)( Math.acos( pix_position.dot( unit_k ) ));
 
    float tof = tof_calc.TOFofDiffractometerQ( angle, 
                                               initial_path+final_path, 
                                               mag_q );

    float intensity = d.getY_value( tof, Data.SMOOTH_LINEAR ); 

//    float y[] = d.getY_values();
//    float x[] = d.getX_scale().getXs();

//    for ( int i = 0; i < y.length; i++ )
//      System.out.println("i, x, y = " + i + ", " + x[i] + ", " + y[i] );

//  System.out.println("mag_q = " + mag_q );
//  System.out.println("tof   = " + tof   );
//  System.out.println("intensity = " + intensity );

    return intensity;
  }

 /* ------------------- makeGoniometerRotationInverse --------------------- */
 /*
  *  Make the cumulative rotation matrix to "unwind" the rotations by chi,
  *  phi and omega, to put the data into one common reference frame for the
  *  crystal.
  */
  private Tran3D makeGoniometerRotationInverse( DataSet ds )
  {
    SampleOrientation orientation =
        (SampleOrientation)ds.getAttributeValue(Attribute.SAMPLE_ORIENTATION);

      return orientation.getGoniometerRotationInverse();
  }

}
