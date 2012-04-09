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
    System.out.println("Peak " + seq_num );
    System.out.println( Peak_new_IO.PeakString(fixed_peak) );

    float new_wl;
    float     wl   = fixed_peak.wl();
    float     row  = fixed_peak.y();
    float     col  = fixed_peak.x();
    IDataGrid grid = fixed_peak.getGrid();
    Vector3D sec_beam_dir = grid.position( row, col );
    System.out.println("Secondary beam direction = " + sec_beam_dir );

    float max_wl = wl * ( 1 + bandwidth/200 );
    float min_wl = wl * ( 1 - bandwidth/200 );
    System.out.println("min, max wl = " + min_wl + ", " + max_wl );

                                         // put orientation matrix into lab
                                         // coordinates, so peaks are predicted
                                         // in lab coordinates.
    or_mat = ApplyGoniometerRotationToUB( fixed_peak, or_mat ); 

 // sec_beam_dir.set( 1, 0 , 0 );        // quick hack to test code for 
                                         // finding h,k,l with wl and that dir
    Vector3D shift_vec = new Vector3D( sec_beam_dir );
    shift_vec.multiply( 1/wl );
    Tran3D toQ = new Tran3D( or_mat );
    System.out.println( toQ );    
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
               System.out.print  ( "wl match at " + h + ", " + k + ", " + l );
               System.out.println( "  wl = " + new_wl );
               found_count++;
             }
           }
           else
             discard_count++;
        }
  
    System.out.println("Discarded " + discard_count );
    System.out.println("found " + found_count );
  
    if ( found_count > max_found )
      max_found = found_count;

    if ( found_count < min_found )
      min_found = found_count;

    total_found += found_count;
   
    if ( found_count == 0 )
      num_with_zero++;

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
    String peaks_filename  = "/home/dennis/TOPAZ_3680.peaks";
    String or_mat_filename = "/home/dennis/TOPAZ_3680.mat";
//  int    seq_num = 1;
    int    seq_num = 2;

  /*
    String peaks_filename  = "/home/dennis/TOPAZ_3680_EV.peaks";
    String or_mat_filename = "/home/dennis/TOPAZ_3680_EV.mat";
//  int    seq_num = 2;
    int    seq_num = 4;
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
