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
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import DataSetTools.operator.DataSet.Math.Analyze.*;

/** This class is responsible for mapping a vector Q point to an interpolated
 *  intensity value in the original time-of-flight DataSet.  NOTE: This version
 *  is a crude proof of concept and only works for the old SCD detector.  Some
 *  detector data is hard-coded for the old SCD detector in this version, since
 *  the representation of the detector information will be changed in the near
 *  future.
 */

public class VecQToTOF
{
  static final Vector3D unit_k = new Vector3D( 1, 0, 0 );

  Data     data_blocks[][];
  Vector3D det_center;  // center position of detector.
  float    det_width,   // NOTE: det_center, width & height MUST be accurate.
           det_height;  // and what they represent must be coordinated with the
                        // rounding calculation. (Pixel centers, edges, etc.)
  int      n_rows,
           n_cols;
  Vector3D u,           // basis vectors for detector 
           v,
           n;

  Tran3D   goniometerRinv;

  Vector3D q_center;    // unit q_vector in direction of q from center of
                        // detector
  float    min_q_dot;   // dot product of unit q vector in direction of
                        // corner pixel. (Used to quickly discard most
                        // cases where q would not have come from this 
                        // detector.

  VecQToTOF( DataSet ds )
  {
                                                   // assume square detector
                                                   // with spectra in order
                                                   // from lower left to upper
                                                   // right;
    n_rows = (int)Math.round( Math.sqrt(ds.getNum_entries()) );
    n_cols = n_rows;       
    System.out.println("n_rows,cols = " + n_cols );

    Vector3D corner1 = getPosition( ds.getData_entry(0) );    
    Vector3D corner2 = getPosition( ds.getData_entry( ds.getNum_entries()-1) );
    Vector3D temp = new Vector3D( corner1 );
    temp.subtract( corner2 );
    float diag = temp.length();
    
    det_width  = (float)(diag/Math.sqrt( 2.0 ));
    det_height = det_width;
    System.out.println("det_width, height = " + det_width );

    det_center = new Vector3D( corner1 );   // find "center" as average of
    det_center.multiply( 0.5f );            // two opposite corners
    temp.set( corner2 );
    temp.multiply( 0.5f );
    det_center.add( temp );

    u = new Vector3D( -1, 0, 0 );
    v = new Vector3D(  0, 0, 1 );
    n = new Vector3D(  0, 1, 0 );

    Tran3D goniometerR = makeGoniometerRotation( ds );
    goniometerRinv = new Tran3D();
    goniometerRinv.set( goniometerR );
    goniometerRinv.transpose();
/*
    System.out.println("goniometerR = ");
    System.out.println("" + goniometerR );

    System.out.println("goniometerRinv = ");
    System.out.println("" + goniometerRinv );
*/

    Operator op = new DiffractometerTofToQ( ds,0,20,0 );
    ds = (DataSet)(op.getResult());

    op = new ConvertHistogramToFunction( ds, false, false );
    op.getResult();

    data_blocks = new Data[n_rows][n_cols];
    for ( int row = 0; row < n_rows; row++ )    // for simplicity, assume data
      for ( int col = 0; col < n_cols; col++ )  // blocks are in row major order
      {                                         // in the DataSet #########
        data_blocks[row][col] = ds.getData_entry( row * n_cols + col );
                                                // get rid of the attributes
        data_blocks[row][col].setAttributeList( new AttributeList() ); 
      }

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
                                                       // then find q_corner
    Vector3D q_corner = new Vector3D( det_center );
    Vector3D temp_u = new Vector3D( u );
    temp_u.multiply( det_width/2 );
    q_corner.add( temp_u );
    Vector3D temp_v = new Vector3D( v );
    temp_v.multiply( det_height/2 );
    q_corner.add( temp_v );

    q_corner.normalize();
    q_corner.subtract( unit_k );
    q_corner.normalize();
    goniometerR.apply_to( q_corner, q_corner );
                                                     // finally get min_q_dot
    min_q_dot = q_center.dot( q_corner );
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
/*
    System.out.println("q_vec = " + q_vec );
    System.out.println("q1 = " + q1 );
*/
    float q1_comp[] = q1.get();
    float alpha = -2*q1_comp[0];

    if ( alpha <= 0 )             // no solution, since the direction of q1 
      return 0;                   // would be reversed using alpha < 0 

    q1.multiply( alpha );
    Vector3D k_prime = new Vector3D( unit_k );
    k_prime.add( q1 );

//  System.out.println("k_prime = " + k_prime );

    float t = det_center.dot(n)/k_prime.dot(n);
    if ( t <= 0 )                // no solution, since the beam missed the
      return 0;                  // detector plane
    
//  System.out.println("t = " + t );

    Vector3D det_point = new Vector3D( k_prime );
    det_point.multiply( t );

    det_point.subtract( det_center );
    float u_comp = det_point.dot( u );
    float v_comp = det_point.dot( v );
//  System.out.println("u, v components = " + u_comp + ", " + v_comp );

                                  // NOTE: the rounding MUST be checked !!!!!
    int row = (int)Math.floor((n_cols-1)*(0.5 + v_comp/det_height));
    int col = (int)Math.floor((n_rows-1)*(0.5 + u_comp/det_width)); 
//  System.out.println("row, col = " + row + ", " + col );
 
    if ( row < 0 || row >= n_rows-1 )
      return 0; 

    if ( col < 0 || col >= n_cols-1 )
      return 0; 

    float mag_q = q_vec.length();
    float intensity = data_blocks[row][col].getY_value( mag_q, 
                                                        Data.SMOOTH_LINEAR ); 
    intensity += data_blocks[row+1][col].getY_value(mag_q, Data.SMOOTH_LINEAR); 
    intensity += data_blocks[row][col+1].getY_value(mag_q, Data.SMOOTH_LINEAR); 
    intensity += data_blocks[row+1][col+1].getY_value(mag_q,Data.SMOOTH_LINEAR);

//  System.out.println("mag_q = " + mag_q );
//  System.out.println("intensity = " + intensity );

    float y[] = data_blocks[row][col].getY_values();
    float x[] = data_blocks[row][col].getX_scale().getXs();

//    for ( int i = 0; i < y.length; i++ )
//      System.out.println("i, x, y = " + i + ", " + x[i] + ", " + y[i] );

    return intensity;
  }


 /* -------------------------- getPosition ------------------------------ */
 /*
  *  Get the segment position as a Vector3D object
  */
  private Vector3D getPosition( Data d )
  {
    DetectorPosition pos = (DetectorPosition)
                              d.getAttributeValue( Attribute.DETECTOR_POS );
    Vector3D vector = new Vector3D();
    float    coords[] = pos.getCartesianCoords();
    vector.set( coords[0], coords[1], coords[2] );
    return vector;
  }

 /* ------------------------ makeGoniometerRotation ------------------------ */
 /*
  *  Make the cumulative rotation matrix to "unwind" the rotations by chi,
  *  phi and omega, to put the data into one common reference frame for the
  *  crystal.
  */
  private Tran3D makeGoniometerRotation( DataSet ds )
  {
      float omega = ((Float)ds.getAttributeValue(Attribute.SAMPLE_OMEGA))
                         .floatValue();
      float phi   = ((Float)ds.getAttributeValue(Attribute.SAMPLE_PHI))
                         .floatValue();
      float chi   = ((Float)ds.getAttributeValue(Attribute.SAMPLE_CHI))
                         .floatValue();

      return tof_calc.makeEulerRotationInverse( phi, chi, -omega );
  }

}
