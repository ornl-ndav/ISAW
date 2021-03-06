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
   *  objects with the following information.  
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
    return result;
  }


  /**
   *  Get a list of the possible pairs of reflections that could lead to 
   *  an increased number of counts in a peak.  Specifically, all possible
   *  reflections are first obtained for the wavelength of the given peak.
   *  For each of these reflections, all possible reflections are checked
   *  to see if any of them reflect back into the original peak.
   *  This method returns a Vector containing sets of six 
   *  objects with the following information.  
   *    First entry:  Vector3D holding h,k,l of first secondary scattering peak
   *    Second entry: Direction vector in real space for first secondary
   *                  scattering peak. 
   *    Third entry:  The wavelength for the first secondary scattering peak.
   *    Fourth entry: Vector3D holding h,k,l of secondary scattering peak that
   *                  reflected back to the original peak
   *    Fifth entry:  Direction vector in real space for the secondary  
   *                  scattering peak.           
   *    Sixth entry:  The wavelength for the secondary scattering peak.
   *
   *  @param incident_dir   Vector3D in direction of travel for some 
   *                        incident neutron beam. 
   *  @param wl             The wavelength of the neutrons.
   *  @param bandwidth      Tolerance band around the specified wavelength
   *                        expressed as a total percent variation.  That is,
   *                        the min and max wavelengths are within 
   *                        +- bandwidth/2 percent of the specified wavelength.
   *  @param angle_tol      Tolerance on how closely the last reflection in
   *                        a pair of reflections should match the primary
   *                        reflection.  The tolerance must be specified in
   *                        degrees.
   *  @param or_mat         The orientation transform, which was already 
   *                        rotated by the goniometer angles, so that the 
   *                        predicted Q vectors are in a reciprocal space 
   *                        coordinate system aligned with the lab coordinate
   *                        system.
   *  @param h_target       Miller index for peak to check for additive
   *                        two stage scattering.
   *  @param k_target       Miller index for peak to check for additive
   *                        two stage scattering.
   *  @param l_target       Miller index for peak to check for additive
   *                        two stage scattering.
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
  public static Vector FindAdditiveReflections( Vector3D  incident_dir,
                                                float     wl,
                                                float     bandwidth,
                                                float     angle_tol,
                                                Tran3D    or_mat,
                                                int       h_target,
                                                int       k_target,
                                                int       l_target,
                                                int       max_h,
                                                int       max_k,
                                                int       max_l )
  {
    Vector3D target_hkl = new Vector3D( h_target, k_target, l_target );
    Vector3D target_dir = ReflectionDirection( incident_dir, wl, 
                                               or_mat, target_hkl );
    if ( target_dir == null )
      return new Vector();
                                         
    Vector reflections = new Vector();
    double dot_product_tol = Math.cos( angle_tol * Math.PI/180.0 );
    
                                         // first find all peaks that scatter
                                         // at the required wavelength and
                                         // incident beam
    Vector first_reflections = 
                 FindSecondaryReflections( incident_dir, wl, bandwidth,
                                           or_mat,
                                           max_h, max_k, max_l );

                                         // for each of these directions, find
                                         // peaks that will scatter back to 
                                         // the target h,k,l
    for ( int i = 0; i < first_reflections.size()/3; i+=3 )
    {
      Vector3D secondary_dir = (Vector3D)first_reflections.elementAt( i + 1 );
      Vector second_reflections = 
                  FindSecondaryReflections( secondary_dir, wl, bandwidth,
                                            or_mat,
                                            max_h, max_k, max_l );
      for ( int j = 0; j < second_reflections.size()/3; j+=3 )
      {
/*
        hkl = (Vector3D)second_reflections.elementAt( j );
        if ( Math.round(hkl.getX()) == h_target &&
             Math.round(hkl.getY()) == k_target &&
             Math.round(hkl.getZ()) == l_target  )
*/      
        Vector3D last_ref_dir = (Vector3D)second_reflections.elementAt( j+1 );
        if ( last_ref_dir.dot( target_dir ) > dot_product_tol )
        {
          reflections.add( first_reflections.elementAt(i) );
          reflections.add( first_reflections.elementAt(i+1) );
          reflections.add( first_reflections.elementAt(i+2) );
          reflections.add( second_reflections.elementAt(j) );
          reflections.add( second_reflections.elementAt(j+1) );
          reflections.add( second_reflections.elementAt(j+2) );
        }
      }
      
    }

//  System.out.println("Additive reflections found: " + reflections.size()/6);
    return reflections;
  }

 
  /**
   *  Get a list of the possible secondary reflections that could be generated
   *  by the specified peak in the list of peaks.  This method returns a 
   *  Vector containing sets of three objects with the following information.  
   *    First entry:  Vector3D holding h,k,l of first secondary scattering peak
   *    Second entry: Direction vector in real space for first secondary
   *                  scattering peak. 
   *    Third entry:  The wavelength for the first secondary scattering peak.
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
    float     new_wl;
    float     wl     = fixed_peak.wl();
    float     max_wl = wl * ( 1 + bandwidth/200 );
    float     min_wl = wl * ( 1 - bandwidth/200 );
    float     row    = fixed_peak.y();
    float     col    = fixed_peak.x();
    IDataGrid grid   = fixed_peak.getGrid();

    Vector3D sec_beam_dir = grid.position( row, col );
    sec_beam_dir.normalize();

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

    System.out.println("UN-Rotated UB:");
    System.out.println( or_mat );
    Tran3D calcUB = new Tran3D();
    SampleOffset_Calc.FindUB_FromIndexedPeaks( peaks, .12f, calcUB );
    System.out.println("Calculated UB:");
    System.out.println( calcUB );

    Vector result = FindSecondaryReflections( sec_beam_dir, wl, bandwidth,
                                              or_mat,
                                              max_h, max_k, max_l );
    System.out.println("Possible secondary reflections found = " + 
                       result.size()/3 );
    ShowReflections( result );

    int found_count = result.size()/3;
    if ( found_count > max_found )
      max_found = found_count;

    if ( found_count < min_found )
      min_found = found_count;

    total_found += found_count;

    if ( found_count == 0 )
      num_with_zero++;

    return result;
  }

  /**
   *  Get a list of the possible pairs of reflections that could lead to 
   *  an increased number of counts in a peak.  Specifically, all possible
   *  reflections are first obtained for the wavelength of the given peak.
   *  For each of these reflections, all possible reflections are checked
   *  to see if any of them reflect back into the original peak.
   *  This method returns a Vector containing sets of six 
   *  objects with the following information.  
   *
   *    First entry:  Vector3D holding h,k,l of first secondary scattering peak
   *    Second entry: Direction vector in real space for first secondary
   *                  scattering peak. 
   *    Third entry:  The wavelength for the first secondary scattering peak.
   *    Fourth entry: Vector3D holding h,k,l of secondary scattering peak that
   *                  reflected back to the original peak
   *    Fifth entry:  Direction vector in real space for the secondary  
   *                  scattering peak.           
   *    Sixth entry:  The wavelength for the secondary scattering peak.
   *
   *  The incident direction is assumed to be (1,0,0).  The wavelength is
   *  obtained from the peak object.
   *
   *  @param peaks          List of peaks from which one is selected to 
   *                        process.
   *  @param or_mat         The orientation transform, that will be rotated 
   *                        by the goniometer angles from the peak, so that 
   *                        the predicted Q vectors are in a reciprocal space
   *                        coordinate system aligned with the lab coordinate 
   *                        system.
   *  @param bandwidth      Tolerance band around the specified wavelength
   *                        expressed as a total percent variation.  That is,
   *                        the min and max wavelengths are within 
   *                        +- bandwidth/2 percent of the specified wavelength.
   *  @param angle_tol      Tolerance on how closely the last reflection in
   *                        a pair of reflections should match the primary
   *                        reflection.  The tolerance must be specified in
   *                        degrees.
   *  @param seq_num        The peak sequence number for the peak to process.
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
  public static Vector FindAdditiveReflections( Vector<Peak_new> peaks,
                                                Tran3D           or_mat,
                                                float            bandwidth,
                                                float            angle_tol,
                                                int              seq_num,
                                                int              max_h,
                                                int              max_k,
                                                int              max_l )
  {
    Peak_new  fixed_peak = peaks.elementAt(seq_num - 1);
    float     new_wl;
    float     wl     = fixed_peak.wl();
    float     max_wl = wl * ( 1 + bandwidth/200 );
    float     min_wl = wl * ( 1 - bandwidth/200 );
    float     row    = fixed_peak.y();
    float     col    = fixed_peak.x();
    IDataGrid grid   = fixed_peak.getGrid();

    Vector3D sec_beam_dir = grid.position( row, col );
    sec_beam_dir.normalize();
                                         // put orientation matrix into lab
                                         // coordinates, so peaks are predicted
                                         // in lab coordinates.
    or_mat = ApplyGoniometerRotationToUB( fixed_peak, or_mat );
    Vector3D incident_dir = new Vector3D( 1, 0, 0 );

    Vector result = FindAdditiveReflections( 
                             incident_dir, wl, bandwidth, angle_tol,
                             or_mat,
                             Math.round(fixed_peak.h()), 
                             Math.round(fixed_peak.k()), 
                             Math.round(fixed_peak.l()), 
                             max_h, max_k, max_l );

    return result;
  }


/**
 *  Get a Vector3D pointing in the direction of the position of the detector
 *  pixel for the peak in real space.  Note: this gives the actual direction
 *  of the reflected beam, in real space.
 *
 *  @param peak   The peak object containing information about the peak
 *
 *  @return A unit vector pointing toward the detector pixel for the
 *          peak in real space.
 */
  public static Vector3D ReflectionDirection( Peak_new peak )
  {
    float     row    = peak.y();
    float     col    = peak.x();
    IDataGrid grid   = peak.getGrid();

    Vector3D sec_beam_dir = grid.position( row, col );
    sec_beam_dir.normalize();
    return sec_beam_dir;
  }


/**
 *  Calculate the Vector3D pointing in the direction of the position of the
 *  detector pixel in real space for a specified h,k,l.  Note: this gives 
 *  the actual direction of the reflected beam, in real space.
 *
 *  @param  incident_dir  The incident beam direction.
 *  @param  wl            The wavelength of the reflection.
 *  @param  or_mat        The orientation matrix.
 *  @param  hkl           The hkl vector.
 *
 *  @return A unit vector pointing toward the detector pixel for the
 *          peak in real space.
 */
  public static Vector3D ReflectionDirection( Vector3D incident_dir, 
                                              float    wl, 
                                              Tran3D   or_mat, 
                                              Vector3D hkl )
  {
    Vector3D q_vec = new Vector3D();

    or_mat.apply_to( hkl, q_vec );          // make the q_vector in lab coords

    if ( q_vec.dot( incident_dir ) >= 0 )
      return null;

    Vector3D real_vec  = new Vector3D( q_vec );
    Vector3D shift_vec = new Vector3D( incident_dir );

    shift_vec.multiply( 1/wl );
    real_vec.add( shift_vec );
    real_vec.normalize();
    return real_vec;
  }


/**
 * Print out the reflections list from the specified result vector, in
 * a nicely formatted list.
 *
 * @param result  List of reflections stored as triples in a vector.
 *                The first entry in a triple is the hkl vector, the second
 *                is the real-space reflection direction for that hkl and
 *                the third is the wavelength for that reflection.
 */
  public static void ShowReflections( Vector result )
  {
    System.out.println( GetReflectionsString( result, 1 ) );
  }


/**
 * Get a multiline String showing the reflections stored in the specified 
 * result vector, in tabular form. The reflections are stored as triples in
 * the specified vector.  The first entry in a triple is the hkl vector, the 
 * second is the real-space reflection direction for that hkl and the third
 * is the wavelength for that reflection.  If these are sets of double 
 * reflections, a blank line will be included between pairs of reflections.
 *
 * @param result  Vector containing triples, hkl, beam_dir, and wavelength.
 * @param stride  The number of triples that should be grouped, and
 *                separated with a blank line.
 *
 * @return a multi-line string with the table of reflections.
 */
  public static String GetReflectionsString( Vector result, int stride )
  {
    StringBuffer buffer = new StringBuffer();
    if ( result.size() <= 0 )
      return new String(buffer);
/*
    buffer.append(
        "   H    K    L        refX       refY       refZ        wl\n");
*/
    for ( int i = 0; i < result.size(); i+=3 )
    {
      Vector3D hkl          = (Vector3D)result.elementAt(i);
      Vector3D new_beam_dir = (Vector3D)result.elementAt(i+1);
      float    new_wl       = (Float)result.elementAt(i+2);

      int   new_h  = Math.round( hkl.getX() );
      int   new_k  = Math.round( hkl.getY() );
      int   new_l  = Math.round( hkl.getZ() );

      buffer.append( String.format( "%4d %4d %4d ", new_h,new_k,new_l ));
      buffer.append( String.format( "  %9.6f  %9.6f  %9.6f",
                                     new_beam_dir.getX(),
                                     new_beam_dir.getY(),
                                     new_beam_dir.getZ() ));
      buffer.append( String.format( "  %8.6f\n", new_wl ));
      if ( i > 0 && i%2 == (stride-1) )
        buffer.append("\n");
    }
    return new String(buffer);
  }

 
  /**
   *  Get a new orientation matrix by applying the goniometer rotation 
   *  to the specified orientation matrix.  The new orientation
   *  matrix can be used directly to predicted peak positions in lab 
   *  coordinates.
   *
   *  @param  peak    The peak from which goniometer angles are 
   *                  obtained.
   *  @param  or_mat  The orientation matrix to which the goniometer
   *                  rotation is applied.
   */ 
  public static Tran3D ApplyGoniometerRotationToUB( Peak_new peak, 
                                                    Tran3D   or_mat )
  {
    SampleOrientation samp_or    = peak.getSampleOrientation();
    Tran3D            rotation   = samp_or.getGoniometerRotation();
    rotation.multiply_by( or_mat );
    return rotation; 
  }


/**
 *  Load an orientation matrix from the specified file.
 * 
 *  @param  filename  The fully qualified file name for the matrix file.
 *
 *  @return A Tran3D object with the orientation matrix, transposed, so
 *          it can be applied to column vectors.
 */
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


/**
 *  Simple demo method that calculates possible multiple scattering
 *  directions for a specified h,k,l in a particular sequence of runs.
 */
  public static void AnalyzeNickelRuns( int target_h, 
                                        int target_k, 
                                        int target_l,
                                        boolean show_reflections ) 
                     throws Exception
  {
    String directory  = "/usr2/TOPAZ_NICKEL_MULTIPLE_SCATT";
    String peaks_file = directory + "/Ni2ndScan.integrate";
    String mat_prefix = directory + "/lsNi2ndScan";
    int first_run = 4796;
    int last_run  = 4848;

    float bandwidth = 1.0f;
    float angle_tol = 0.5f;

    int   max_h    = 15;
    int   max_k    = 15;
    int   max_l    = 15;

    System.out.println("Additive reflections for peak " + target_h + 
                       ", " + target_k +
                       ", " + target_l );
    System.out.println("Wavelength tolerance = " + bandwidth + "%" );
    System.out.println("Angle tolerance      = " + angle_tol + "degrees" );
    System.out.printf(" RUN    N_ADDITIVE\n");

    Vector3D         incident_dir = new Vector3D( 1, 0, 0 );
    Vector           result;
    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( peaks_file );
    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);
//      System.out.println( peak );

      if ( Math.round(peak.h()) == target_h &&
           Math.round(peak.k()) == target_k &&
           Math.round(peak.l()) == target_l  )
      {
        int runnum = peak.nrun();
        Tran3D or_mat = LoadOrientationMatrix( mat_prefix + runnum + ".mat" );
        or_mat = ApplyGoniometerRotationToUB( peak, or_mat );

        float wl = peak.wl();

        result = FindAdditiveReflections( incident_dir, wl,
                                          bandwidth, angle_tol,
                                          or_mat,
                                          target_h, target_k, target_l,
                                          max_h, max_k, max_l );

        int n_additive_refl = result.size() / 6;
        if ( n_additive_refl > 0 )
        {
          System.out.printf("%4d     %2d\n", runnum, n_additive_refl );
          if ( show_reflections )
            System.out.println( GetReflectionsString( result, 2 ) ); 
        }
      }
    }


    System.out.println("Secondary reflections for peak " + target_h + 
                       ", " + target_k +
                       ", " + target_l );
    System.out.println("Wavelength tolerance = " + bandwidth + "%" );
    System.out.printf(" RUN    N_SUBTRACTIVE\n");

    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);
//      System.out.println( peak );

      if ( Math.round(peak.h()) == target_h &&
           Math.round(peak.k()) == target_k &&
           Math.round(peak.l()) == target_l  )
      {
        int runnum = peak.nrun();
        Tran3D or_mat = LoadOrientationMatrix( mat_prefix + runnum + ".mat" );
        or_mat = ApplyGoniometerRotationToUB( peak, or_mat );

        float wl = peak.wl();
        Vector3D hkl = new Vector3D( target_h, target_k, target_l );
        Vector3D reflection_dir = ReflectionDirection( incident_dir, wl,
                                                       or_mat, hkl );

        result = FindSecondaryReflections( reflection_dir, wl,
                                           bandwidth,
                                           or_mat,
                                           max_h, max_k, max_l );

        int n_subtractive_refl = result.size() / 3;
        System.out.printf("%4d     %2d\n", runnum, n_subtractive_refl );
        if ( show_reflections )
          ShowReflections(result);
      }
    }
 
  }


  public static void main(String args[]) throws Exception
  {
    AnalyzeNickelRuns( -1, -1, -1, true );
  
/*
    String peaks_filename  = "/home/dennis/TOPAZ_3134_EV.peaks";
    String or_mat_filename = "/home/dennis/TOPAZ_3134_EV.mat";
    int    seq_num = 474;
//  int    seq_num = 475;
    String peaks_filename  = "/home/dennis/TOPAZ_3680.peaks";
    String or_mat_filename = "/home/dennis/TOPAZ_3680.mat";
    int    seq_num = 1;
//  int    seq_num = 2;
*/
/*
    String peaks_filename  = "/home/dennis/TOPAZ_3680_EV.peaks";
    String or_mat_filename = "/home/dennis/TOPAZ_3680_EV.mat";
    int    seq_num = 2;
//  int    seq_num = 4;
*/
    String peaks_filename   = "/home/dennis/sapphire.peaks";
    String or_mat_filename  = "/home/dennis/lssapphire3680.mat";
    Vector<Peak_new> peaks  = Peak_new_IO.ReadPeaks_new( peaks_filename );
    Tran3D           or_mat = LoadOrientationMatrix( or_mat_filename );
    int             seq_num = 2;
    FindSecondaryReflections( peaks, or_mat, 1.0f, seq_num, 25, 25, 25 );

/*  
  FindSecondaryReflections( peaks, or_mat, 1.0f, 25, 25, 25 );
*/
/*
  Vector result = FindAdditiveReflections( peaks, or_mat, 1.0f, 0.5f,
                                           seq_num, 25, 25, 25 );
    ShowReflections( result );
*/
/*
    System.out.println("Min found = " + min_found );
    System.out.println("Max found = " + max_found );
    System.out.println("Total found = " + total_found );
    System.out.println("Num with zero = " + num_with_zero );

    System.out.println("Searching for additive reflections....");
    int max_additive    = 0;
    int count_additive = 0;
    for ( int s_num = 1; s_num <= peaks.size(); s_num++ )
    {
      Vector result = FindAdditiveReflections( peaks, or_mat, 
                                               1.0f, s_num, 25, 25, 25 );
      if ( result.size() > 0 )
      {
        Peak_new peak = (Peak_new)peaks.elementAt(s_num-1);
        System.out.println("Additive scattering found for peak # " + s_num ); 
        System.out.printf("h,k,l = %5.0f %5.0f %5.0f\n",
                           peak.h(), peak.k(), peak.l() );
        System.out.println("Original scattering direction " +
                            ReflectionDirection(peak) );
                           
        ShowReflections( result ); 
        count_additive++;
        if ( result.size() / 6  > max_additive )
          max_additive = result.size() / 6;
      }
    }
    if ( max_additive == 0 )
      System.out.println("NONE FOUND");
    else
      System.out.println("FOUND UP TO " + max_additive + 
                         " FOR " + count_additive + " REFLECTIONS" );
    System.out.println("OUT OF " + peaks.size() + " PEAKS IN PEAKS FILE");
*/   
  }

}
