/**
 * File: SCD_Multiple_Scattering_Util.java
 */

package Operators.TOF_SCD;

import java.util.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.dataset.*;
import DataSetTools.instruments.*;
import gov.anl.ipns.MathTools.Geometry.*;


public class SCD_Multiple_Scattering_Util
{
  static int min_found = 100000;
  static int max_found = 0;
  static int total_found = 0;
  static int num_with_zero = 0;

  /**
   *  Get a list of the possible secondary reflections that could be generated
   *  by neutrons travelling in the specified direction with the specified
   *  wavelength.  This method returns a Vector containing sets of three 
   *  objects containint the following information.  
   *    First entry:  Vector3D holding h,k,l of first secondary scattering peak
   *    Second entry: Direction vector in real space for first secondary
   *                  scattering peak. 
   *    Third entry:  The wavelength for the first secondary scattering peak.
   *
   *  @param incident_dir   Vector3D in direction of travel for some 
   *                        incident neutron beam. 
   *  @param wl             The wavelength of the neutrons.
   *  @param bandwidth      Tolerance band around the specified wavelength
   *                        expressed as a total percent variation.  That is,
   *                        the min and max wavelengths are within 
   *                        +- bandwidth/2 percent of the specified wavelength.
   *  @param or_mat         The orientation transform, rotated by the 
   *                        goniometer angles, so that the predicted Q vectors
   *                        are in a reciprocal space coordinate system 
   *                        aligned with the lab coordinate system.
   *  @param max_h          Peaks with h values between -max_h and + max_h
   *                        will be tested.
   *  @param max_k          Peaks with k values between -max_k and + max_k
   *                        will be tested.
   *  @param max_l          Peaks with l values between -max_l and + max_l
   *                        will be tested.
   * @return a Vector containing hkl, real-space direction and wavelength 
   *         for peaks that could scatter neutrons travelling along the 
   *         specified direction with the specified wavelength.
   */
  public static Vector FindSecondaryReflections( Vector3D  incident_dir, 
                                                 float     wl,
                                                 float     bandwidth,
                                                 Tran3D    or_mat,
                                                 int       max_h,
                                                 int       max_k,
                                                 int       max_l )
  {
    System.out.println("\nStarting new FindSecondaryReflections\n");
    Vector result = new Vector();
    float max_wl = wl * ( 1 + bandwidth/200 );
    float min_wl = wl * ( 1 - bandwidth/200 );
    float new_wl;

    incident_dir.normalize();

    int found_count = 0;
    Vector3D q_vec = new Vector3D();
                                            // try "all" h,k,l values
    for ( int h = -max_h; h <= max_h; h++ )
      for ( int k = -max_k; k <= max_k; k++ )
        for ( int l = -max_l; l <= max_l; l++ )
        {
           Vector3D hkl = new Vector3D( h, k, l );

           or_mat.apply_to( hkl, q_vec );   // make the q_vector in lab coords

           if ( q_vec.dot( incident_dir ) < 0 )
           {                                // find the wavelength at which the
                                            // q_vec will scatter.  That is,
                                            // solve: |Q+t*V|=t where t = 1/wl
                                            // V is unit vector in direction of
                                            // incident beam and Q = q_vec.
             new_wl = -(2*q_vec.dot(incident_dir) )/q_vec.dot(q_vec);
             if ( min_wl < new_wl && new_wl < max_wl )
             {
               found_count++;

               Vector3D real_vec  = new Vector3D( q_vec );
               Vector3D shift_vec = new Vector3D( incident_dir );
               shift_vec.multiply( 1/new_wl );
               real_vec.add( shift_vec );
               real_vec.normalize();

               result.add ( new Vector3D( h, k, l ) );
               result.add ( new Vector3D( real_vec ) );
               result.add ( new_wl );
             }
           }
        }
    System.out.println("\nEnding new FindSecondaryReflections\n");
    return result;
  }
 

  public static Vector FindSecondaryReflections( Vector<Peak_new> peaks,
                                                 Tran3D           or_mat,
                                                 float            bandwidth,
                                                 int              max_h,
                                                 int              max_k,
                                                 int              max_l )
  {
    for (int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);
      int seq_num = peak.seqnum();
      FindSecondaryReflections( peaks, or_mat, bandwidth, seq_num,
                                max_h, max_k, max_l );      
    }
  
    return null;
  }


  /**
   *  Get a list of the possible secondary reflections that could be generated
   *  by the the specified peak in the list of peaks.  This method returns
   *  a Vector of arrays containing the following information.  
   *  1. h,k,l of peak
   *  2. wavelength for peak
   *  3. direction vector in real space for peak
   *  The first array has information about the peak with the specified
   *  sequence number.  The remaining entries have information about peaks
   *  that could occur as secondary reflections from neutrons traveling in
   *  the direction of the real space peak.
   */
  public static Vector FindSecondaryReflections( Vector<Peak_new> peaks,
                                                 Tran3D           or_mat,
                                                 float            bandwidth,
                                                 int              seq_num,
                                                 int              max_h,
                                                 int              max_k,
                                                 int              max_l )
  {
    Peak_new fixed_peak = peaks.elementAt(seq_num - 1);
    float new_wl;
    float     wl     = fixed_peak.wl();
    float     max_wl = wl * ( 1 + bandwidth/200 );
    float     min_wl = wl * ( 1 - bandwidth/200 );
    float     row    = fixed_peak.y();
    float     col    = fixed_peak.x();
    System.out.println("row, col = " + row + ", " + col );
    IDataGrid grid   = fixed_peak.getGrid();

    Vector3D sec_beam_dir = grid.position( row, col );
    sec_beam_dir.normalize();

    Vector3D x_dir = new Vector3D( 1, 0, 0 );
    float dot_prod = x_dir.dot( sec_beam_dir );
    System.out.println("Two theta = " + Math.acos( dot_prod ) );

//  System.out.println( Peak_new_IO.PeakString(fixed_peak) );
    System.out.println("Possible secondary scattering from peak #" + seq_num );
    System.out.printf( "h,k,l =  %3.0f %3.0f %3.0f  ", 
                       fixed_peak.h(), fixed_peak.k(), fixed_peak.l() );
    System.out.printf( "wl = %8.6f\n", wl );
    System.out.printf( "Secondary beam direction (IPNS lab x,y,z) " +
                       "%8.6f  %8.6f  %8.6f\n", 
                       sec_beam_dir.getX(),   
                       sec_beam_dir.getY(),   
                       sec_beam_dir.getZ() );

    System.out.printf(
          "wl tolerance(percent) = %4.1f, min wl = %8.6f max wl = %8.6f\n", 
           bandwidth, min_wl, max_wl );

                                         // put orientation matrix into lab
                                         // coordinates, so peaks are predicted
                                         // in lab coordinates.
    or_mat = ApplyGoniometerRotationToUB( fixed_peak, or_mat ); 


                                         // quick hack to test code for 
                                         // finding h,k,l with wl and that dir
    Vector3D initial_beam_dir = new Vector3D( 1, 0, 0 );
    Vector3D shift_vec = new Vector3D( initial_beam_dir );
    shift_vec.multiply( 1/wl );

    Vector3D alt_beam_dir = new Vector3D( fixed_peak.h(), 
                                          fixed_peak.k(), 
                                          fixed_peak.l() );
    or_mat.apply_to( alt_beam_dir, alt_beam_dir );
    System.out.println("alt_beam_dir length = " + alt_beam_dir.length() );
    alt_beam_dir.add( shift_vec );
    alt_beam_dir.normalize();
    System.out.printf( "Alternately calculated beam direction " +
                       "%8.6f  %8.6f  %8.6f\n",
                       alt_beam_dir.getX(),
                       alt_beam_dir.getY(),
                       alt_beam_dir.getZ() );
    dot_prod = x_dir.dot( alt_beam_dir );
    System.out.println("Two theta = " + Math.acos( dot_prod ) );

    shift_vec.set( sec_beam_dir );
    shift_vec.multiply( 1/wl );
    System.out.println("OR_MAT = " + or_mat );
    System.out.println("Incident Vector = " + sec_beam_dir );
    System.out.println("Shift Vector = " + shift_vec );
    System.out.println("Min, max wl = " + min_wl + ", " + max_wl );

    int discard_count = 0;
    int found_count = 0;
    Vector3D q_vec = new Vector3D();
    for ( int h = -max_h; h <= max_h; h++ )
      for ( int k = -max_k; k <= max_k; k++ )
        for ( int l = -max_l; l <= max_l; l++ )
        {
           Vector3D hkl = new Vector3D( h, k, l );
           or_mat.apply_to( hkl, q_vec );
           if ( q_vec.dot( sec_beam_dir ) < 0 )
           {
             q_vec.add( shift_vec );
             new_wl = 1/q_vec.length();
             if ( min_wl < new_wl && new_wl < max_wl )
             {
               System.out.printf( "wl match at hkl: %3d %3d %3d  ", h,k,l );
               System.out.printf( "  wl = %8.6f\n", new_wl );
               found_count++;
             }
             else
               discard_count++;
           }
        }
  
    System.out.println("Discarded " + discard_count + " possible hkls");
    System.out.println("Kept " + found_count + " within wavelength range" );
  
    if ( found_count > max_found )
      max_found = found_count;

    if ( found_count < min_found )
      min_found = found_count;

    total_found += found_count;
   
    if ( found_count == 0 )
      num_with_zero++;

//    sec_beam_dir.set( 1, 0, 0 );
    Vector result = FindSecondaryReflections( sec_beam_dir, wl, bandwidth,
                                              or_mat,
                                              max_h, max_k, max_l );
    System.out.println("Reflections returned in vector:");
    for ( int i = 0; i < result.size(); i+=3 )
    {
      Vector3D hkl          = (Vector3D)result.elementAt(i);
      Vector3D new_beam_dir = (Vector3D)result.elementAt(i+1);
      int new_h = Math.round( hkl.getX() );
      int new_k = Math.round( hkl.getY() );
      int new_l = Math.round( hkl.getZ() );
          new_wl = (Float)result.elementAt(i+2);
      System.out.printf( "wl match at hkl: %3d %3d %3d  ", new_h,new_k,new_l );
      System.out.printf( "  refl dir = %9.6f  %9.6f  %9.6f",
                            new_beam_dir.getX(),
                            new_beam_dir.getY(),
                            new_beam_dir.getZ() );
      System.out.printf( "  wl = %8.6f\n", new_wl );
    }
    System.out.println("New method found " + result.size()/3 );

    return null;
  }

 
  /**
   *  Apply rotation to the orientation matrix, so the predicted peaks are
   *  in lab coordinates.
   */ 
  public static Tran3D ApplyGoniometerRotationToUB( Peak_new peak, 
                                                    Tran3D   or_mat )
  {
    SampleOrientation samp_or    = peak.getSampleOrientation();
    Tran3D            rotation   = samp_or.getGoniometerRotation();
    rotation.multiply_by( or_mat );
    return rotation; 
  }


  public static Tran3D LoadOrientationMatrix( String filename )
                throws Exception
  {
                              // NOTE: readOrient already forms the transpose
    Object Res = Operators.TOF_SCD.IndexJ.readOrient( filename );

    if( Res == null || !(Res instanceof float[][]) )
      throw new IllegalArgumentException("Can't read orientation matrix file "+
                                          filename );

    Tran3D or_mat = new Tran3D( (float[][])Res );
    return or_mat; 
  }


  public static void main(String args[]) throws Exception
  {
  /*
    String peaks_filename  = "/home/dennis/TOPAZ_3134_EV.peaks";
    String or_mat_filename = "/home/dennis/TOPAZ_3134_EV.mat";
    int    seq_num = 474;
//  int    seq_num = 475;
*/
    String peaks_filename  = "/home/dennis/TOPAZ_3680.peaks";
    String or_mat_filename = "/home/dennis/TOPAZ_3680.mat";
    int    seq_num = 1;
//  int    seq_num = 2;
/*
    String peaks_filename  = "/home/dennis/TOPAZ_3680_EV.peaks";
    String or_mat_filename = "/home/dennis/TOPAZ_3680_EV.mat";
    int    seq_num = 2;
//  int    seq_num = 4;
*/

    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( peaks_filename );
    Tran3D           or_mat = LoadOrientationMatrix( or_mat_filename );
    
    FindSecondaryReflections( peaks, or_mat, 1.0f, seq_num, 25, 25, 25 );
//    FindSecondaryReflections( peaks, or_mat, 1.0f, 25, 25, 25 );

    System.out.println("Min found = " + min_found );
    System.out.println("Max found = " + max_found );
    System.out.println("Total found = " + total_found );
    System.out.println("Num with zero = " + num_with_zero );
  }

}
