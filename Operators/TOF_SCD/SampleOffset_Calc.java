/* File: SampleOffset_Calc.java 
 *
 * Copyright (C) 2012, Dennis Mikkelson
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
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */
package Operators.TOF_SCD;

import java.util.*;

import gov.anl.ipns.MathTools.Geometry.Tran3D;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import DataSetTools.math.tof_calc;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new_IO;

/**
 *  This class contaings static methods to help find a sample offset, based
 *  on indexed peak positions.
 */
public class SampleOffset_Calc
{

  public static double IndexingError( Vector<Peak_new> peaks, 
                                      Vector3D         sample_shift,
                                      float            tolerance )
  {
    Vector<Vector3D>  q_vectors   = new Vector<Vector3D>();
    Vector<Vector3D>  hkl_vectors = new Vector<Vector3D>();
    Vector<Float>     tofs        = new Vector<Float>();

    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);
      Vector3D hkl = new Vector3D( Math.round(peak.h()), 
                                   Math.round(peak.k()), 
                                   Math.round(peak.l()) );
      if ( IndexingUtils.ValidIndex( hkl, tolerance ) )
      {
        hkl_vectors.add( hkl );

        Vector3D q = new Vector3D( peak.getQ() );
        q_vectors.add( q );

        tofs.add( peak.time() );
      }
    }
 
    Tran3D UB = new Tran3D();
    double sqr_err = IndexingUtils.Optimize_UB_3D( UB, hkl_vectors, q_vectors );
/*
    System.out.println("Original sum squared error = " + sqr_err );
    System.out.println("UB = " + UB );
    float[] l_par = IndexingUtils.getLatticeParameters( UB );
    System.out.printf("%8.4f %8.4f %8.4f  %8.4f %8.4f %8.4f  %8.4f\n",
        l_par[0], l_par[1], l_par[2], l_par[3], l_par[4], l_par[5], l_par[6] );

    double sum_sq_error = SumSqError( UB, hkl_vectors, q_vectors );
    System.out.println("Calculated sum squared error = " + sum_sq_error );
*/
    float L1 = peaks.elementAt(0).L1();
    ShiftQVectors( q_vectors, tofs, L1, sample_shift );

    double sum_sq_error = SumSqError( UB, hkl_vectors, q_vectors );
//  System.out.println("New sum squared error        = " + sum_sq_error );

    return sum_sq_error;
  } 


  public static void ShiftQVectors( Vector<Vector3D>  q_vectors,
                                    Vector<Float>     tofs,
                                    float             L1,
                                    Vector3D          sample_shift )
  {
     Vector3D beam_dir = new Vector3D( 1, 0, 0 );
     for ( int i = 0; i < q_vectors.size(); i++ )
     {
       Vector3D q   = q_vectors.elementAt( i );
       float    wl  = -2*(beam_dir.dot(q))/q.dot(q);
       float    tof = tofs.elementAt(i);
       Vector3D det_pos = DetPositionOfQ( q, beam_dir, tof, L1 );

       det_pos.subtract( sample_shift );
       float new_L1 = L1 + sample_shift.getX();

       Vector3D new_q = tof_calc.DiffractometerVecQ( det_pos, new_L1, tof ); 
       new_q.multiply ( (float)( 0.5/Math.PI) );

       q_vectors.elementAt(i).set( new_q );
     }     
  }


  public static Vector3D DetPositionOfQ( Vector3D q,
                                         Vector3D beam_dir,
                                         float    tof,
                                         float    L1 )
  {
    float wl = -2*(beam_dir.dot(q))/q.dot(q);
    Vector3D det_pos = new Vector3D( beam_dir );
    det_pos.multiply( 1/wl );
    det_pos.add( q );
    det_pos.normalize();
    float total_path = tof_calc.PathLength( wl, tof );
    float L2 = total_path-L1;
    det_pos.multiply( L2 );
    return det_pos;
  }
                               


  public static double SumSqError( Tran3D            UB,
                                   Vector<Vector3D>  hkl_vectors, 
                                   Vector<Vector3D>  q_vectors )
  {
    double sum_sq_error = 0;
    double err;
    Vector3D pred_q = new Vector3D();
    for ( int i = 0; i < q_vectors.size(); i++ )
    {
      UB.apply_to( hkl_vectors.elementAt(i), pred_q );
      pred_q.subtract( q_vectors.elementAt(i) );
      err = pred_q.length();
      sum_sq_error += err * err;
    }
    return sum_sq_error;
  }


  public static double SearchForMin( Vector<Peak_new> peaks,
                                     float            tolerance,
                                     Vector3D         direction,
                                     double           step,
                                     Vector3D         offset )
  {
    Vector3D best_offset = new Vector3D();
    Vector3D new_offset = new Vector3D();
    double   error;
    double   min_error = 1.0e200;
    for ( int i = -5; i <= 5; i++ )
    {
      new_offset.set( direction );
      new_offset.multiply( (float)(step * i) );
      new_offset.add( offset );
      error = IndexingError( peaks, new_offset, tolerance );
      if ( error < min_error )
      {
        min_error = error;
        best_offset.set( new_offset );
      }
    }

    offset.set( best_offset );
    return min_error;
  }
 

  public static double EstimateOffset( Vector<Peak_new> peaks,
                                       float            tolerance,
                                       Vector3D         offset )
  {
    offset.set( 0, 0, 0 );
    Vector3D direction = new Vector3D();
 
    float  step  = 0.005f;
    double error = 0;
    for (int i = 0; i < 17; i++ )
    {
//      System.out.println( "\nStep = " + step );
      direction.set( 0, 0, 1 );
      SearchForMin( peaks, tolerance, direction, step, offset ); 

      direction.set( 0, 1, 0 );
      SearchForMin( peaks, tolerance, direction, step, offset ); 

      direction.set( 1, 0, 0 );
      error = SearchForMin( peaks, tolerance, direction, step, offset ); 
      step = step/2;

//    System.out.printf( " offset = %12.7f  %12.7f  %12.7f   Error = %12.9f\n",
//                     offset.getX(), offset.getY(), offset.getZ(), error );
    }

    return error;
  }


  public static void main( String args[] ) throws Exception
  {
    String peaks_file_name = "/home/dennis/natrolite_very_noisy_indexed.peaks"; 
//    String peaks_file_name = "/home/dennis/TOPAZ_3131.peaks"; 
//    String peaks_file_name = "/home/dennis/TOPAZ_3131_EV.peaks"; 
//    String peaks_file_name = "/home/dennis/TOPAZ_3680_EV.peaks"; 
//    String peaks_file_name = "/home/dennis/snap_natrolite_7413.peaks";
    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( peaks_file_name );

    float tolerance = 0.12f;

    Vector3D offset = new Vector3D();
    double err = EstimateOffset( peaks, tolerance, offset ); 
    System.out.printf( " offset = %12.7f  %12.7f  %12.7f   Error = %12.9f\n",
                         offset.getX(), offset.getY(), offset.getZ(), err );
    
  }
}
