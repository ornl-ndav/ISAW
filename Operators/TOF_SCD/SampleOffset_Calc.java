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
import java.io.*;

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

/**
 *  Calculate the indexing error that would occur for the specified peaks, 
 *  if the sample was shifted by the specified amount.  That is, assuming 
 *  the sample was actually at a shifted position, then several instrument 
 *  parameters would change, including the detector positions (relative 
 *  to the sample), L1 and the incident beam direction.  The indexing error 
 *  is obtained as follows.  First the Q-vectors are changed, based on the 
 *  specified sample shift, then a UB matrix corresponding to the current 
 *  peak indexing with the new Q-vectors is calculated and the total 
 *  sumsquared error is obtained.
 *  
 *  @param peaks         List of indexed peak objects
 *  @param sample_shift  Vector3D specifying the displacement of the sample 
 *                       from the origin in meters.
 *  @param tolerance     Required tolerance for a peak to be considered to 
 *                       be indexed
 *  @param UB            The new UB matrix, corresponding to the shifted 
 *                       sample, is returned in this matrix.
 *  @return This method returns Math.sqrt(sum_sq_error)/hkl_vectors.size()),
 *          where sum_sq_error is the sum of (UB*hkl - Q)^2 across all 
 *          indexed peaks, or 10^10 if there were not at least 4 indexed peaks.
 */
  public static double IndexingError( Vector<Peak_new> peaks, 
                                      Vector3D         sample_shift,
                                      float            tolerance,
                                      Tran3D           UB  )
  {
    Vector<Vector3D>  q_vectors     = new Vector<Vector3D>();
    Vector<Vector3D>  det_positions = new Vector<Vector3D>();
    Vector<Vector3D>  hkl_vectors   = new Vector<Vector3D>();
    Vector<Float>     tofs          = new Vector<Float>();

    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);
      Vector3D hkl = new Vector3D( peak.h(), peak.k(), peak.l() );
      if ( IndexingUtils.ValidIndex( hkl, tolerance ) )
      {
        Vector3D q = new Vector3D( peak.getQ() );
        q_vectors.add( q );

        Vector3D position = peak.getGrid().position( peak.y(), peak.x() );
        det_positions.add( position );

        tofs.add( peak.time() );

        hkl.set( Math.round(peak.h()), 
                 Math.round(peak.k()), 
                 Math.round(peak.l()) );
        hkl_vectors.add( hkl );
      }
    }

    if ( hkl_vectors.size() < 4 )           // not enough indexed peaks
    {
      return 1.0e10f;
    } 

    float L1 = peaks.elementAt(0).L1();

//  ShiftQVectors( q_vectors, tofs, L1, sample_shift );
    ShiftQVectors( q_vectors, det_positions, tofs, L1, sample_shift );

    double sqr_err = IndexingUtils.Optimize_UB_3D( UB, hkl_vectors, q_vectors );

    double sum_sq_error = SumSqError( UB, hkl_vectors, q_vectors );

    return (float)(Math.sqrt(sum_sq_error)/hkl_vectors.size());  
  } 


/**
 *  Get estimates of the peak position errors in local detector coordinates for 
 *  each indexed peak, assuming the beam direction is (1,0,0).
 *
 *  @param peaks       List of indexed peaks objects
 *  @param tolerance   Required tolerance for a peak to be considered to be
 *                     indexed
 *  @param det_errors  Vector that will be filled with the detector position 
 *                     errors for each indexed peak.  The position errors 
 *                     are returned as x,y,z components in the local 
 *                     coordinate system on the face of the detector.
 *  @param indexes     Vector that records the index of the peak in the 
 *                     list of peaks, for each error stored in det_errors.
 */
  public static void GetPathAndDetErrors( Vector<Peak_new> peaks,
                                          float            tolerance,
                                          Vector<Vector3D> det_errors,
                                          Vector<Integer>  indexes )
  {
    Tran3D UB = new Tran3D();
    FindUB_FromIndexedPeaks( peaks, tolerance, UB );

    det_errors.clear();
    indexes.clear();

    Vector3D beam_dir = new Vector3D( 1, 0, 0 );
                                               // pointed down .25 degrees 
//  Vector3D beam_dir = new Vector3D( 0.9999905f, 0, -0.00436335f ); 

    float    L1       = peaks.elementAt(0).L1();

    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);
      Vector3D hkl = new Vector3D( peak.h(), peak.k(), peak.l() );
      if ( IndexingUtils.ValidIndex( hkl, tolerance ) )
      {
        hkl.set( Math.round(peak.h()), 
                 Math.round(peak.k()), 
                 Math.round(peak.l()) );

        Vector3D pred_q = new Vector3D();
        UB.apply_to( hkl, pred_q );

        Vector3D position = peak.getGrid().position( peak.y(), peak.x() );

        Vector3D pos_error = DetPositionOfQ(pred_q, beam_dir, peak.time(), L1);
        pos_error.subtract( position );

        float d_col    = pos_error.dot( peak.getGrid().x_vec() );
        float d_row    = pos_error.dot( peak.getGrid().y_vec() );
        float d_normal = pos_error.dot( peak.getGrid().z_vec() );

        det_errors.add( new Vector3D( d_col, d_row, d_normal ) );
        indexes.add( i );
      }
    }
  }


/**
 *  Shift the measured q_vectors by adjusting calculated detector positions,
 *  L1, and beam direction, corresponding to a shifted sample position.
 *  The initial beam direction is assumed to be (1,0,0), IPNS coordinates.
 *
 *  @param q_vectors     The new q_vectors will overwrite the current
 *                       q_vectors in this array.
 *  @param tofs          List of measured times-of-flight for the peaks.
 *  @param L1            The initial path length.
 *  @param sample_shift  The displacement of the sample from the origin
 */
  public static void ShiftQVectors( Vector<Vector3D>  q_vectors,
                                    Vector<Float>     tofs,
                                    float             L1,
                                    Vector3D          sample_shift )
  {
     Vector3D new_beam_dir = new Vector3D( L1, 0, 0 );
     new_beam_dir.add( sample_shift );
     
     float new_L1 = new_beam_dir.length();
     new_beam_dir.normalize();

     for ( int i = 0; i < q_vectors.size(); i++ )
     {
       Vector3D q   = q_vectors.elementAt( i );
       float    tof = tofs.elementAt(i);

       Vector3D det_pos = DetPositionOfQ( q, new_beam_dir, tof, new_L1 );
       det_pos.subtract( sample_shift );

       Vector3D new_q = tof_calc.DiffractometerVecQ( det_pos, new_L1, tof ); 
       new_q.multiply ( (float)( 0.5/Math.PI) );

       q_vectors.elementAt(i).set( new_q );
     }     
  }


/**
 *  Shift the measured q_vectors by adjusting the recorded detector positions,
 *  L1, and beam direction, corresponding to a shifted sample position.
 *  The initial beam direction is assumed to be (1,0,0), IPNS coordinates.
 *
 *  @param q_vectors     The new q_vectors will overwrite the current
 *                       q_vectors in this array.
 *  @param det_positions List of the positions of the detected peaks 
 *                       on the detector.
 *  @param tofs          List of measured times-of-flight for the peaks.
 *  @param L1            The initial path length.
 *  @param sample_shift  The displacement of the sample from the origin
 */
  public static void ShiftQVectors( Vector<Vector3D>  q_vectors,
                                    Vector<Vector3D>  det_positions,
                                    Vector<Float>     tofs,
                                    float             L1,
                                    Vector3D          sample_shift )
  {
     Vector3D new_beam_dir = new Vector3D( L1, 0, 0 );
     new_beam_dir.add( sample_shift );

     float new_L1 = new_beam_dir.length();
     new_beam_dir.normalize();

     for ( int i = 0; i < q_vectors.size(); i++ )
     {
       float tof = tofs.elementAt(i);

       Vector3D new_det_pos = new Vector3D( det_positions.elementAt(i) );
       new_det_pos.subtract( sample_shift );

       Vector3D new_q = Q_Vector( new_det_pos, new_beam_dir, new_L1, tof ); 

       q_vectors.elementAt(i).set( new_q );
     }
  }


  /**
   *  Calculate the Q-vector corresponding to an ARBITRARY detector position
   *  beam direction, L1 and tof. 
   *
   *  @param det_pos    The detector position, relative to the sample.
   *  @param beam_dir   Arbitrary beam direction vector, a unit vector.
   *  @param L1         Initial path length in meters
   *  @param tof        The time-of-flight in microseconds.
   *
   *  @return The Q_vector.
   */
  public static Vector3D Q_Vector( Vector3D det_pos,
                                   Vector3D beam_dir,
                                   float    L1,
                                   float    tof )
  {
    float total_path = L1 + det_pos.length();
    float wl = tof_calc.Wavelength( total_path, tof );

                                       // get kf
    Vector3D q_vec = new Vector3D( det_pos );
    q_vec.normalize();
    q_vec.multiply( 1/wl );
                                       // get ki
    Vector3D incident_vec = new Vector3D( beam_dir );
    incident_vec.multiply( 1/wl );
                                       // get kf - ki
    q_vec.subtract( incident_vec );

    return q_vec;
  }

 
/**
 * Calculate the wavelength at which a particular Q-vector would
 * be measured, given a unit vector in the direction the neutron
 * beam is traveling.
 *
 * @param q         The Q-vector
 * @param beam_dir  Unit vector in the direction of the incident beam
 *
 * @return the wavelength at which the peak must be measured
 */
  public static float Wavelength( Vector3D q,
                                  Vector3D beam_dir )
  {
    return -2*(beam_dir.dot(q))/q.dot(q);
  }


/**
 * Calculate the position of the detector pixel relative to the sample,
 * that would measure a specified q-vector, assuming the beam_direction
 * vector, initial path length and the time-of-flight are known.
 *
 * @param q         The Q-vector
 * @param beam_dir  Unit vector in the direction of the incident beam
 * @param tof       The time-of-flight a which the peak was measured
 * @param L1        The initial path length
 *
 * @return The vector offset of the detector pixel from the sample position.
 */
  public static Vector3D DetPositionOfQ( Vector3D q,
                                         Vector3D beam_dir,
                                         float    tof,
                                         float    L1 )
  {
    float wl = Wavelength( q, beam_dir );
                                         // find vector pointing towards
                                         // the detector pixel from sample
    Vector3D det_pos = new Vector3D( beam_dir );
    det_pos.multiply( 1/wl );
    det_pos.add( q );
    det_pos.normalize();
                                         // find actual offset from sample 
    float total_path = tof_calc.PathLength( wl, tof );
    float L2 = total_path-L1;
    det_pos.multiply( L2 );
    return det_pos;
  }
                               

/**
 * Calculate the sum-squared errors of the measured Q-vectors,
 * compared to the Q-vectors predicted by the UB matrix, for
 * peaks with the listed hkl and Q-vectors.
 *
 * @param UB          The matrix that should map hkl to Q
 * @param hkl_vectors List of the integer HKLs of the peaks
 * @param q_vectors   List of the Q-vectors of the peaks,
 *                    using the Q = 1/d convention.
 */ 
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


/**
 *  Search for the smallest indexing error for sample positions displaced 
 *  from the current offset by +-5 times the specified step size in the
 *  specified direction.
 *
 *  @param peaks      List of peak objects.  These MUST already have been 
 *                    indexed.
 *  @param tolerance  The required tolerance for a peak to count as indexed.
 *  @param direction  The direction in which the search for max should occur.
 *  @param step       The size of the steps to take in the specified direction.
 *  @param offset     The estimated sample offset is returned in this vector.
 *  @param UB         The new UB matrix, corresponding to the shifted sample, is
 *                    returned in this matrix.
 */
  public static double SearchForMin( Vector<Peak_new> peaks,
                                     float            tolerance,
                                     Vector3D         direction,
                                     double           step,
                                     Vector3D         offset,
                                     Tran3D           UB )
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
      error = IndexingError( peaks, new_offset, tolerance, UB );
      if ( error < min_error )
      {
        min_error = error;
        best_offset.set( new_offset );
      }
    }

    offset.set( best_offset );
    return min_error;
  }
 

/**
 *  Estimate the offset of the sample, by searching along lines parallel to
 *  the coordinate axes, gradually decreasing the length of the lines used
 *  and increasing the resolution of the search.  This method searches over a
 *  large range of sample positions, and is very fast.  Currently a region
 *  extending +-50mm in each direction is searched with an ending resolution 
 *  of about 1 micron.
 *
 *  @param peaks      List of peak objects.  These MUST already have been 
 *                    indexed.
 *  @param tolerance  The required tolerance for a peak to count as indexed.
 *  @param offset     The estimated sample offset is returned in this vector.
 *  @param UB         The new UB matrix, corresponding to the shifted sample, is
 *                    returned in this matrix.
 */
  public static double EstimateOffset( Vector<Peak_new> peaks,
                                       float            tolerance,
                                       Vector3D         offset,
                                       Tran3D           UB )
  {
    offset.set( 0, 0, 0 );
    Vector3D direction = new Vector3D();
 
    float  step  = 0.01f;
    double error = 0;
    for (int i = 0; i < 30; i++ )
    {
      direction.set( 0, 0, 1 );
      SearchForMin( peaks, tolerance, direction, step, offset, UB ); 

      direction.set( 0, 1, 0 );
      SearchForMin( peaks, tolerance, direction, step, offset, UB ); 

      direction.set( 1, 0, 0 );
      error = SearchForMin( peaks, tolerance, direction, step, offset, UB ); 

      step = (float)(step/Math.sqrt(2));
    }

    return error;
  }


/**
 *  Estimate the offset of the sample, by searching over a 3D volume and 
 *  gradually decreasing the size of the region use used and increasing the
 *  resolution of the search.  This method searches over a small range of 
 *  sample positions, and is quite slow.  Currently a region extending +-10mm
 *  in each direction is searched with an ending resolution of about 
 *  10 microns.
 *
 *  @param peaks      List of peak objects.  These MUST already have been 
 *                    indexed.
 *  @param tolerance  The required tolerance for a peak to count as indexed.
 *  @param offset     The estimated sample offset is returned in this vector.
 *  @param UB         The new UB matrix, corresponding to the shifted sample, is
 *                    returned in this matrix.
 */
  public static double EstimateOffset3D( Vector<Peak_new> peaks,
                                         float            tolerance,
                                         Vector3D         offset,
                                         Tran3D           UB )
  {
    offset.set( 0, 0, 0 );
    Vector3D direction = new Vector3D();
    Vector3D temp_offset = new Vector3D();

    double min_error = 1.0e100;
    Vector3D min_error_offset = new Vector3D();

    float  step  = 0.001f;    
    double error = 0;
    for ( int count = 0; count < 11; count++ )
    {
      for (int i = -10; i < 10; i++ )
        for (int j = -10; j < 10; j++ )
          for (int k = -10; k < 10; k++ )
      {
        temp_offset.set( i*step, j*step, k*step );
        temp_offset.add( offset );
        error = IndexingError( peaks, temp_offset, tolerance, UB );  
        if ( error < min_error )
        {
          min_error = error;
          min_error_offset.set( temp_offset ); // best offset at this level
        }                                      // search
      }
      offset.set( min_error_offset );          // center later search at the 
                                               // best offset from this level
      step = step/2;
    }

    return min_error;
  }


/**
 *  Caluclate the UB matrix, given a list of indexed peaks, and return the 
 *  standard deviation of the q-vectors relative to the predicted q-vectors.
 *
 *  @param peaks      List of indexed peaks objects
 *  @param tolerance  The required tolerance to consider a peak indexed
 *  @param UB         The calculated UB matrix will be returned in this 
 *                    parameter
 *
 *  @return The standard deviation of the q-vectors, or 10^10 if there were 
 *          not at least 4 indexed peaks. 
 */
  public static float FindUB_FromIndexedPeaks( Vector<Peak_new> peaks, 
                                               float            tolerance,
                                               Tran3D           UB )
  {
    Vector<Vector3D>  q_vectors     = new Vector<Vector3D>();
    Vector<Vector3D>  hkl_vectors   = new Vector<Vector3D>();

    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);
      Vector3D hkl = new Vector3D( peak.h(), peak.k(), peak.l() );
      if ( IndexingUtils.ValidIndex( hkl, tolerance ) )
      {
        Vector3D q = new Vector3D( peak.getQ() );
        q_vectors.add( q );

        hkl.set( Math.round(peak.h()), 
                 Math.round(peak.k()), 
                 Math.round(peak.l()) );
        hkl_vectors.add( hkl );
      }
    }

    if ( hkl_vectors.size() < 4 )         // not enough properly indexed peaks
      return 1.0e10f;

    double sqr_err = IndexingUtils.Optimize_UB_3D( UB, hkl_vectors, q_vectors );

    return (float)Math.sqrt( sqr_err/hkl_vectors.size() );
  }


/**
 *  Print out a formatted string showing the average indexing error, the offset
 *  vector and the lattice parameters.
 *
 *  @param error    The standard deviation of the predicted q-vectors vs 
 *                  measured q-vectors.
 *  @param offset   The sample offset vector
 *  @param UB       The UB matrix from which the lattice parameters are obtained
 */
  public static String MakeInfoString( double error, Vector3D offset, Tran3D UB )
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append( String.format( 
               "Sample Offsets(mm):\n" +
               "Along Beam Direction %8.3f\n" +
               "In Horizontal Plane  %8.3f\n" +
               "Vertical Direction   %8.3f\n",  
                1000*offset.getX(), 1000*offset.getY(),1000*offset.getZ() ));
    buffer.append( String.format(
        "Standard Deviation of Measured Q-vectors(|Q|=1/d): %12.8f\n", error ) );
    float[] l_par = IndexingUtils.getLatticeParameters( UB );
    buffer.append( String.format(
        "Adjusted Lattice Params: %8.4f %8.4f %8.4f  %8.4f %8.4f %8.4f  %8.4f\n",
        l_par[0], l_par[1], l_par[2], l_par[3], l_par[4], l_par[5], l_par[6] ));
    return new String(buffer);
  }


/**
 * Make a multi-line string showing the estimated errors in
 * real space for a list of indexed peaks, as computed by
 * the GetPathAndDetErrors() method. 
 * @param peaks       The list of indexed peak objects
 * @param det_errors  The list of errors on the detector face
 * @param indexes     The list of indexes of peaks for which 
 *                    the errors were calculated
 * @return A multiline String showing the sequence number,
 *         wavelength, d, and computed erros and the average
 *         error for each detector module.
 */
  public static String MakePathAndDetErrorString( 
                                       Vector<Peak_new>  peaks,
                                       Vector<Vector3D>  det_errors,
                                       Vector<Integer>   indexes )
  {
    StringBuffer buffer = new StringBuffer();

    if ( peaks.size() == 0 )
      return new String(buffer);

    int   det_id  = -1;
    int   n_peaks = 0;
    float sum_x   = 0;
    float sum_y   = 0;
    float sum_z   = 0;

    Peak_new peak = peaks.elementAt( 0 );
    det_id = peak.getGrid().ID();
    buffer.append("DET_ID = " + det_id + "\n" );
    buffer.append("SEQ NUM        WL         D     dX(mm)    dY(mm)    dL(mm)\n");
    for ( int i = 0; i < indexes.size(); i++ )
    {
      int index = indexes.elementAt(i);
      peak = peaks.elementAt( index );
      if ( peak.getGrid().ID() != det_id )
      {
        if ( det_id != -1 && n_peaks > 0 )           // print summary
        {
          buffer.append("\n");
          buffer.append( String.format(
         "DET_ID: %2d  Ave_dx: %8.3f  Ave_dy: %8.3f  Ave_dL: %8.3f\n",
          det_id, 1000*sum_x/n_peaks, 1000*sum_y/n_peaks, 1000*sum_z/n_peaks));
          buffer.append("\n");
          buffer.append("DET_ID = " + peak.getGrid().ID() + "\n");
          buffer.append("SEQ NUM        WL         D     dX(mm)    dY(mm)    dL(mm)\n");
        }
        sum_x = 0;
        sum_y = 0;
        sum_z = 0;
        n_peaks = 0;
        det_id = peak.getGrid().ID();
      }
      Vector3D err = det_errors.elementAt( i );
      buffer.append( String.format( "%7d  %8.5f  %8.5f   %8.3f  %8.3f  %8.3f\n",
                    (index+1), peak.wl(), peak.d(), 
                    1000*err.getX(), 1000*err.getY(), 1000*err.getZ()));
      sum_x += err.getX();
      sum_y += err.getY();
      sum_z += err.getZ();
      n_peaks++;
    }
    if ( n_peaks > 0 )
    {
      buffer.append("\n");
      buffer.append( String.format(
      "DET_ID: %2d  Ave_dx: %8.3f  Ave_dy: %8.3f  Ave_dL: %8.3f\n",
        det_id, 1000*sum_x/n_peaks, 1000*sum_y/n_peaks, 1000*sum_z/n_peaks) );
    }
    return new String(buffer);
  }


  public static void AnalyzePeakPositions( String peaks_file_name,
                                           boolean save_to_file,
                                           String file_name )
                     throws Exception
  {
    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( peaks_file_name );

    float    tolerance = 0.12f;
    Tran3D   UB        = new Tran3D();
    Vector3D offset    = new Vector3D();

    double err = IndexingError( peaks, offset, tolerance, UB );
    String result = "With no sample offset:\n" +
                    MakeInfoString( err, offset, UB );

    err = EstimateOffset( peaks, tolerance, offset, UB );

    result += "\nWith Estimated sample offset:\n" +
              MakeInfoString( err, offset, UB );

    peaks = Peak_new_IO.ReadPeaks_new( peaks_file_name );

    Vector<Vector3D> det_errors = new Vector<Vector3D>();
    Vector<Integer>  indexes    = new Vector<Integer>();
    GetPathAndDetErrors( peaks, tolerance, det_errors, indexes );
    result += "\nAnalysis of Errors, per Peak and per Detector\n"
           + MakePathAndDetErrorString(peaks, det_errors, indexes);

    if ( save_to_file )
    {
      PrintStream out = new PrintStream( file_name );
      out.println( result );
      out.close();
    }
    else
      System.out.println( result );
  }


  public static void main( String args[] ) throws Exception
  {
//  String peaks_file_name = "/home/dennis/natrolite_very_noisy_indexed.peaks"; 
//  String peaks_file_name = "/home/dennis/TOPAZ_3131.peaks"; 
//  String peaks_file_name = "/home/dennis/TOPAZ_3131_EV.peaks"; 
//  String peaks_file_name = "/home/dennis/TOPAZ_3680_EV.peaks"; 
//  String peaks_file_name = "/home/dennis/TOPAZ_3680_calibrated.peaks"; 
//  String peaks_file_name = "/home/dennis/snap_natrolite_7413.peaks";
    String peaks_file_name = args[0];
    String out_file_name = "/home/dennis/AnalyzedPeaks.txt";

    AnalyzePeakPositions( peaks_file_name, true, out_file_name );
  }
}
