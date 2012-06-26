package Operators.TOF_SCD;

import java.util.*;

import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.MathTools.Geometry.*;

import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.instruments.*;



public class PeaksFileUtils
{

/**
 *  Extract the peaks from the first run and return them in a new Vector.
 *  The peaks in peaks list are assumed to be grouped according to run
 *  number.
 *
 *  @param peaks  The list of peaks from which to extract the peaks with the
 *                first run number.
 *  @return A list of peaks containing the first group of peaks with the 
 *          same run number.
 */
  public static Vector<Peak_new> GetFirstRunPeaks( Vector<Peak_new> peaks )
  {
    Vector<Peak_new> first_peaks = new Vector<Peak_new>();
    int index    = 0;
    boolean done = false;

    int first_run = peaks.elementAt(0).nrun();
    done = false;
    while ( index < peaks.size() && !done )
    {
      if ( peaks.elementAt(index).nrun() != first_run )
        done = true;
      else
      {
        first_peaks.add( peaks.elementAt(index) );
        index++;
      }
    }
    return first_peaks;
  }

/**
 *  Extract peaks that follow the last peak with the specified current_run
 *  number, but have a different run number and that are in a contiguous 
 *  group all with the same run number.  The peaks in peaks list are assumed 
 *  to be grouped according to run number.
 *
 *  @param current_run  The last run number from the list of peaks that has
 *                      been already extracted.
 *  @param peaks        The list of peaks from which peaks with the next run 
 *                      number will be extracted.
 *  @return A Vector of peaks with the next run number.  If the current_run
 *          number is the last in the list, the vector will be returned as
 *          as an empty Vector.
 */
  public static Vector<Peak_new> GetNextRunPeaks( int current_run,
                                                  Vector<Peak_new> peaks )
  {
    Vector<Peak_new> next_peaks = new Vector<Peak_new>();
    int index    = 0;
    boolean done = false;
                                   // scan for peaks with specified run 
    while ( index < peaks.size() && !done )
    {
      if ( peaks.elementAt(index).nrun() == current_run )
        done = true;
      else
        index++;
    }

    if ( !done )                   // current run not valid
      return next_peaks;
                                   // skip peaks with specified run
    done = false;
    while ( index < peaks.size() && !done )
    {
      if ( peaks.elementAt(index).nrun() != current_run )
        done = true;
      else
        index++;
    }

    if ( !done )                   // current run was last in list of peaks
      return next_peaks;

    int new_run = peaks.elementAt(index).nrun();
    done = false;
    while ( index < peaks.size() && !done )
    {
      if ( peaks.elementAt(index).nrun() != new_run )
        done = true;
      else
      {
        next_peaks.add( peaks.elementAt(index) );
        index++;
      }
    }
    return next_peaks;
  }


/**
 *  Find a Niggli reduced cell that will best index the specified peaks,
 *  using the FFT indexing routine from IndexingUtils.
 *
 *  @param peaks      Vector of peaks to be indexed.
 *  @param min_d      A number strictly less than the shortest edge of the
 *                    real space Niggli reduced cell.
 *  @param max_d      A number strictly more than the longest edge of the
 *                    real space Niggli reduced cell.
 *  @param tolerance  Tolerance on fractional Miller indexes for a peak to
 *                    be considered indexed.
 *  @param angle_step Approximate angle between successive directions used
 *                    in the FFT indexing routine.
 *  @return A Tran3D holding the UB matrix found by the FFT indexing routine.
 */
  public static Tran3D FindUB( Vector<Peak_new> peaks,
                               float            min_d,
                               float            max_d,
                               float            tolerance,
                               float            angle_step )
  {
    if ( peaks.size() < 4 )
      return null;

    Vector<Vector3D> q_vectors = new Vector<Vector3D>();
    for ( int i = 0; i < peaks.size(); i++ )
      q_vectors.add( new Vector3D( peaks.elementAt(i).getUnrotQ() ) );

    Tran3D UB_tran = new Tran3D();
    IndexingUtils.FindUB_UsingFFT( UB_tran,
                                   q_vectors,
                                   min_d, max_d,
                                   tolerance,
                                   angle_step );
    return UB_tran;
  }


/**
 *  Get the UB matrix that corresponds to the indices already set on 
 *  the list of peaks.
 *
 *  @param peaks      The list of indexed peaks.
 *  @param tolerance  Tolerance on the fractional Miller indices for a 
 *                    peak to count as indexed.
 *  @return A Tran3D object with the UB matrix.
 */
  public static Tran3D FindUB_FromIndexing( Vector<Peak_new> peaks,
                                            float            tolerance )
  {
    Vector<Vector3D> q_vectors = new Vector<Vector3D>();
    Vector<Vector3D> hkls      = new Vector<Vector3D>();
    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak = peaks.elementAt(i);

      Vector3D hkl_vec = new Vector3D( Math.round(peak.h()), 
                                       Math.round(peak.k()), 
                                       Math.round(peak.l()) );

      if ( IndexingUtils.ValidIndex( hkl_vec, tolerance ) )
      {
        q_vectors.add( new Vector3D( peak.getUnrotQ() ) );
        hkls.add( hkl_vec ); 
      }
    }

    Tran3D newUB = new Tran3D();
    IndexingUtils.Optimize_UB_3D( newUB, hkls, q_vectors );
    return newUB;
  }


/**
 *  Get a new UB matrix to index the specified list of peaks, by trying to
 *  index the peaks with the specified UB, and then repeatedly optimizing the
 *  new UB obtained from that indexing.
 *
 *  @param peaks      List of peaks to index
 *  @param UB         Initial UB to optimize
 *  @param tolerance  Tolerance on the Miller indices to consider a peak
 *                    indexed
 *
 *  @return A Tran3D with the UB found by optimizing the fit of the specified
 *          UB.
 */
  public static Tran3D OptimizeUB_AndIndexPeaks( Vector<Peak_new> peaks,
                                                 Tran3D           UB,
                                                 float            tolerance )
  {
    Vector<Vector3D> q_vectors = new Vector<Vector3D>();
    for ( int i = 0; i < peaks.size(); i++ )
      q_vectors.add( new Vector3D( peaks.elementAt(i).getUnrotQ() ) );

    float std_dev = IndexingUtils.IndexingStdDev( UB, q_vectors, tolerance );

    Vector miller_ind = new Vector();
    Vector indexed_qs = new Vector();
    float[] fit_error = new float[1];

    int n_indexed = IndexingUtils.GetIndexedPeaks( UB, q_vectors, tolerance,
                                           miller_ind, indexed_qs, fit_error );

    System.out.println("FIRST UB:");
    System.out.println("Indexed " + n_indexed );
    System.out.println("Std Dev " + std_dev );
    System.out.println("Fit Err " + fit_error[0] );

    Tran3D newUB = new Tran3D();

    for ( int count = 0; count < 4; count++ )
    {
      fit_error[0] = IndexingUtils.Optimize_UB_3D(newUB,miller_ind,indexed_qs);

      std_dev = IndexingUtils.IndexingStdDev( newUB, q_vectors, tolerance );
      n_indexed = IndexingUtils.GetIndexedPeaks( newUB, q_vectors, tolerance,
                                          miller_ind, indexed_qs, fit_error );
      System.out.println("NEXT UB:");
      System.out.println("Indexed " + n_indexed );
      System.out.println("Std Dev " + std_dev );
      System.out.println("Fit Err " + fit_error[0] );
   }

    Tran3D newUB_inverse = new Tran3D( newUB );
    newUB_inverse.invert();

    for ( int i = 0; i < peaks.size(); i++ )
    {
      peaks.elementAt(i).sethkl( 0, 0, 0 );
      Vector3D q   = new Vector3D( peaks.elementAt(i).getUnrotQ() );
      Vector3D hkl = new Vector3D();
      newUB_inverse.apply_to( q, hkl );
      if ( IndexingUtils.ValidIndex( hkl, tolerance ) )
      {

        peaks.elementAt(i).sethkl( Math.round(hkl.getX()), 
                                   Math.round(hkl.getY()), 
                                   Math.round(hkl.getZ()) );
      }
    }

    return newUB;
  }


/**
 * Remove all peaks with the specified detector number from the specified
 * vector of peaks.
 *
 * @param  peaks      Vector of Peak_new objects from which the specified 
 *                    detector will be removed.
 * @param  det_num    The ID of the detector that should be removed.
 *
 * @return A new Vector of Peak_new objects that does not contain any peaks
 *         with the specified detector.
 */
  public static Vector<Peak_new> RemoveDetector( Vector<Peak_new> peaks, 
                                                 int              det_num )
  {
    Vector<Peak_new> new_peaks = new Vector<Peak_new>();
    for ( int i = 0; i < peaks.size(); i++ )
      if ( det_num != peaks.elementAt(i).detnum() )
        new_peaks.add( peaks.elementAt(i) );
    return new_peaks;
  }


/**
 * Remove all peaks with the specified detector number from the specified
 * file of peaks, and write the modified peaks list to the output file.
 * The output file can be left blank, in which case the modified list will
 * be written back to input file!
 *
 * @param  peaks_file Name of the peaks file from which the specified 
 *                    detector will be removed.
 * @param  det_num    The ID of the detector that should be removed.
 * @param  out_file   Name of the new file to write.  If this is blank or
 *                    a zero length string, the new list of peaks will be 
 *                    written back to the input file.
 */
  public static void RemoveDetector( String peaks_file, 
                                     int    det_num,
                                     String out_file )
                     throws Exception
  {
    Vector<Peak_new> peaks     = Peak_new_IO.ReadPeaks_new( peaks_file );
    Vector<Peak_new> new_peaks = RemoveDetector( peaks, det_num );

    out_file = out_file.trim();
    if ( out_file.length() == 0 )
      Peak_new_IO.WritePeaks_new( peaks_file, new_peaks, false );
    else
      Peak_new_IO.WritePeaks_new( out_file, new_peaks, false );
  }

/**
 * Set the run number, goniometer angles and/or monitor count for peaks
 * in the specified list of peaks.  NOTE: The list must contain peaks from only 
 * one run.  
 *
 * @param  peaks            Vector of peaks to which the specified 
 *                          information will be added.
 * @param  set_run_num      Flag indicating whether or not a new run number
 *                          should be set.
 * @param  run_num          New run number
 * @param  set_gonio_angles Flag indicating whether or not new goniometer 
 *                          angles should be set.
 * @param  chi              New chi angle
 * @param  phi              New phi angle
 * @param  omega            New omega angle
 * @param  set_mon_count    Flag indicating whether or not a new monitor 
 *                          count should be set.
 * @param  mon_count        New monitor count
 * @return a new list of new peak objects with the specified information added.
 */
  public static Vector<Peak_new> AddRunInfo( Vector<Peak_new> peaks,
                                             boolean          set_run_num,
                                             int              run_num,
                                             boolean          set_gonio_angles,
                                             float            chi,
                                             float            phi,
                                             float            omega,
                                             boolean          set_mon_count,
                                             float            mon_count )
  {
    Vector<Peak_new> new_peaks = new Vector<Peak_new>();

                                          // since all peaks from one run,
                                          // the same run number, sample 
                                          // orienation and monitor count
                                          // are used for all peaks 
    Peak_new peak = peaks.elementAt(0);

    int n_run = peak.nrun();
    if ( set_run_num )
      n_run = run_num;

    SampleOrientation orientation = peak.getSampleOrientation();
    if ( set_gonio_angles )
    {
      String facility = peak.getFacility();
      if ( facility.trim().startsWith("SNS") )
        orientation = new SNS_SampleOrientation( phi, chi, omega );
      else    // assume the IPNS SCD at Lansce
        orientation = new IPNS_SCD_SampleOrientation( phi, chi, omega );
    }

    float monct = peak.monct();
    if ( set_mon_count )
      monct = mon_count;

    for ( int i = 0; i < peaks.size(); i++ )
    {
      peak = peaks.elementAt(i);
                                               // make new peak with possibly
                                               // updated info
      Peak_new new_peak = new Peak_new( n_run,
                                        monct,
                                        peak.x(),
                                        peak.y(),
                                        peak.z(),
                                        peak.getGrid(),
                                        orientation,
                                        peak.time(),
                                        peak.L1(),
                                        peak.T0() );

                                                // copy over the other fields
      new_peak.setFacility( peak.getFacility() );
      new_peak.setInstrument( peak.getInstrument() );
      new_peak.sethkl( peak.h(), peak.k(), peak.l() ); 
      new_peak.seqnum( peak.seqnum() );
      new_peak.ipkobs( peak.ipkobs() );
      new_peak.inti( peak.inti() );
      new_peak.sigi( peak.sigi() );
      new_peak.reflag( peak.reflag() );

      new_peaks.add( new_peak );
    } 

    return new_peaks;
  }


/**
 * Set the run number, goniometer angles and/or monitor count for peaks
 * in the specified file.  NOTE: The file must contain peaks from only 
 * one run.  The output file can be left blank, in which case the modified
 * list will be written back to input file!
 *
 * @param  peaks_file       Name of file with list of peaks to which the 
 *                          specified information will be added.
 * @param  set_run_num      Flag indicating whether or not a new run number
 *                          should be set.
 * @param  run_num          New run number
 * @param  set_gonio_angles Flag indicating whether or not new goniometer 
 *                          angles should be set.
 * @param  chi              New chi angle
 * @param  phi              New phi angle
 * @param  omega            New omega angle
 * @param  set_mon_count    Flag indicating whether or not a new monitor 
 *                          count should be set.
 * @param  mon_count        New monitor count
 * @param  out_file         Name of the new file to write.  If this is blank or
 *                          a zero length string, the new list of peaks will be 
 *                          written back to the input file.
 */
  public static void AddRunInfo( String   peaks_file,
                                 boolean  set_run_num,
                                 int      run_num,
                                 boolean  set_gonio_angles,
                                 float    chi,
                                 float    phi,
                                 float    omega,
                                 boolean  set_mon_count,
                                 float    mon_count,
                                 String   out_file )
                     throws Exception
  {
    Vector<Peak_new> peaks     = Peak_new_IO.ReadPeaks_new( peaks_file );
    Vector<Peak_new> new_peaks = AddRunInfo( peaks, 
                                             set_run_num, run_num,
                                             set_gonio_angles, chi, phi, omega,
                                             set_mon_count, mon_count );

    out_file = out_file.trim();
    if ( out_file.length() == 0 )
      Peak_new_IO.WritePeaks_new( peaks_file, new_peaks, false );
    else
      Peak_new_IO.WritePeaks_new( out_file, new_peaks, false );
  }


/**
 *  This method will merge peaks files from several runs into one new
 *  peaks file containing all of the peaks from all of the runs.
 *  All of the runs to be merged must be in the same directory, and
 *  must have names including the run number in one plase in the file 
 *  name and must differ only in the run number.
 *  The run number, goniometer angles and monitor counts should have
 *  been previously set to correct values for each run, in the 
 *  individual peaks files.
 *
 *  @param directory    The directory containing the peaks files to
 *                      be merged.
 *  @param base_name    The peak file name, up to, but not including
 *                      the run number.
 *  @param suffix       The portion of the peaks file names that 
 *                      follows the run number, including the file
 *                      extension.
 *  @param run_list     List of run numbers for the peaks files to
 *                      be merged, specified as a comma separated list.
 *                      A range of consecutive run numbers can be
 *                      specified with a colon separator.
 *  @param out_file     The fully qualified name of the merged file 
 *                      to be written.
 */
  public static void MergeRuns( String directory,
                                String base_name,
                                String suffix,
                                String run_list,
                                String out_file )
                     throws Exception
  {
    Vector<Peak_new> all_peaks  = new Vector<Peak_new>();
    Vector<Peak_new> some_peaks = new Vector<Peak_new>();
    int[] run_number = IntList.ToArray( run_list );
    for ( int i = 0; i < run_number.length; i++ )
    {
      String peaks_file = directory + "/" + base_name + run_number[i] + suffix;
      some_peaks = Peak_new_IO.ReadPeaks_new( peaks_file );
      for ( int k = 0; k < some_peaks.size(); k++ )
        all_peaks.add( some_peaks.elementAt(k) );
    }
     
    out_file = out_file.trim();
    Peak_new_IO.WritePeaks_new( out_file, all_peaks, false );
  }


/**
 *  Given a list of peaks from multiple runs, try to find a consistent 
 *  indexing.
 *
 *  @param peaks        List of peaks from multiple runs.  The goniometer
 *                      angles must be correctly set for all of the runs.
 *  @param use_fft      Flag indicating whether to obtain an initial indexing
 *                      of the first run's peaks using the FFT indexing routine
 *                      or to use initial index values already specified
 *                      for the peaks.
 *  @param min_d        A number strictly less than the shortest edge of the
 *                      real space Niggli reduced cell.
 *  @param max_d        A number strictly more than the longest edge of the
 *                      real space Niggli reduced cell.
 *  @param tolerance    Tolerance on fractional Miller indexes for a peak to
 *                      be considered indexed.
 *  @param reindexed_peaks  Vector that will be filled with the reindexed peaks
 *                          from the peaks vector.
 *  @param UB_matrices      Vector of UB_matrices that will be filled with
 *                          the optimized UB matrices for each run.
 */
  public static void IndexMultipleRuns( Vector<Peak_new> peaks,
                                        boolean          use_fft,
                                        float            min_d,
                                        float            max_d,
                                        float            tolerance,
                                        Vector<Peak_new> reindexed_peaks,
                                        Vector<Tran3D>   UB_matrices )
  {
    float angle_step = 1;

    Vector<Peak_new> first_peaks = GetFirstRunPeaks( peaks );
    int current_run = first_peaks.elementAt(0).nrun();

    Tran3D firstUB = FindUB(first_peaks, min_d, max_d, tolerance, angle_step);
    System.out.println("RUN NUMBER: " + current_run );
//    System.out.println("UB from FFT:\n" + firstUB );
    IndexingUtils.ShowLatticeParameters( firstUB );
    System.out.println();

    if ( !use_fft )                         // Find UB from initial indexing
    {
      firstUB = FindUB_FromIndexing( first_peaks, tolerance );
      System.out.println("UB from supplied indexes:\n" + firstUB );
      IndexingUtils.ShowLatticeParameters( firstUB );
      System.out.println();
    }

    reindexed_peaks.clear();
    UB_matrices.clear();
    firstUB = OptimizeUB_AndIndexPeaks( first_peaks, firstUB, tolerance );
    for ( int i = 0; i < first_peaks.size(); i++ )
      reindexed_peaks.add( first_peaks.elementAt(i) );
    UB_matrices.add( firstUB );

    Tran3D nextUB;
    boolean done = false;
    while ( !done )
    {
      Vector<Peak_new> next_peaks = GetNextRunPeaks( current_run, peaks );
      if ( next_peaks.size() == 0 )
        done = true;
      else
      {
        current_run = next_peaks.elementAt(0).nrun();
        System.out.println("RUN NUMBER: " + current_run );

        nextUB = OptimizeUB_AndIndexPeaks( next_peaks, firstUB, tolerance );
//      System.out.println("Optimized UB:\n" + nextUB );
        IndexingUtils.ShowLatticeParameters( nextUB );
        System.out.println();

        for ( int i = 0; i < next_peaks.size(); i++ )
          reindexed_peaks.add( next_peaks.elementAt(i) );
         UB_matrices.add( nextUB );
      }
    }
  }  


/**
 *  Given a peaks file containing peaks from multiple runs, try to find a 
 *  consistent indexing.  If the goniometer angles are not correct, it may
 *  be necessary to increase the tolerance.
 *
 *  @param peaks_file   Name of peaks file with multiple runs.  The goniometer
 *                      angles must be correctly set for all of the runs.
 *  @param use_fft      Flag indicating whether to obtain an initial indexing
 *                      of the first run's peaks using the FFT indexing routine
 *                      or to use initial index values already specified
 *                      for the peaks.
 *  @param min_d        A number strictly less than the shortest edge of the
 *                      real space Niggli reduced cell.
 *  @param max_d        A number strictly more than the longest edge of the
 *                      real space Niggli reduced cell.
 *  @param tolerance    Tolerance on fractional Miller indexes for a peak to
 *                      be considered indexed.
 *  @param out_directory    Name of the directory where the output files should
 *                          be written.
 *  @param out_file_name    Name of the file of reindexed peaks that will be 
 *                          be written to the output directory.
 *  @param matrix_base_name Base name for the individual run UB matrices that
 *                          will be written to the output directory.  The names
 *                          will have the form BaseName_nnnn.mat where the 
 *                          BaseName is as specified and nnnn is the run 
 *                          number.
 */
  public static void IndexMultipleRuns( String  peaks_file,
                                        boolean use_fft,
                                        float   min_d,
                                        float   max_d,
                                        float   tolerance,
                                        String  out_directory,
                                        String  out_file_name,
                                        String  matrix_base_name )
                     throws Exception
  {
    Vector<Peak_new> reindexed_peaks = new Vector<Peak_new>();
    Vector<Tran3D>   UB_matrices     = new Vector<Tran3D>();

    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( peaks_file );

    IndexMultipleRuns( peaks, use_fft, min_d, max_d, tolerance,
                       reindexed_peaks, UB_matrices );

    String out_file = out_directory + "/" + out_file_name;
    Peak_new_IO.WritePeaks_new( out_file, reindexed_peaks, false );

    Vector<Peak_new> first_peaks = GetFirstRunPeaks( peaks );
    int current_run = first_peaks.elementAt(0).nrun();
    for ( int i = 0; i < UB_matrices.size(); i++ )
    {
      String mat_file = out_directory + "/" + 
                        matrix_base_name + "_" + current_run + ".mat";
      SaveUB( UB_matrices.elementAt(i), mat_file );

      Vector<Peak_new> next_peaks = GetNextRunPeaks( current_run, peaks );
      if ( next_peaks.size() > 0 )
        current_run = next_peaks.elementAt(0).nrun();
    }
  }


  /**
   *  Write the specified UB matrix to the specified file.
   *
   *  @param  UB         A Tran3D object containing a UB matrix.
   *  @param  filename   The name of the file to write.
   *  @throws An IOException will be thrown if the file cannot be written.
   */
  public static void SaveUB( Tran3D UB, String filename ) throws Exception
  {
    if ( UB == null )
      throw new Exception("UB matrix is NULL, can't write " + filename );

    float[][] UB_float = UB.get();
    double[][] UB_double = new double[3][3];
    for ( int i = 0; i < 3; i++ )
      for ( int j = 0; j < 3; j++ )
        UB_double[i][j] = UB_float[i][j];

    double[] abc = Util.abc( UB_double );
    float[]  abc_float = new float[ abc.length ];
    for ( int i = 0; i < abc.length; i++ )
      abc_float[i] = (float)abc[i];

    float[]  sig_abc = { 0, 0, 0, 0, 0, 0, 0 };

    Util.writeMatrix(filename, UB_float, abc_float, sig_abc);
  }


/**
 *  Get a new list of peaks containing all of those peaks from the input
 *  peaks list, that do NOT have index (0,0,0).
 *
 *  @param peaks      Vector of peaks, some of which are indexed.
 *
 *  @return A new list of peaks with the peaks from the original list that
 *          have been indexed.
 */
  public static  Vector<Peak_new> RemoveUnindexedPeaks(Vector<Peak_new> peaks)
  {
    Vector<Peak_new> indexed_peaks = new  Vector<Peak_new>( peaks.size() );
    Peak_new peak;
    for ( int i = 0; i < peaks.size(); i++ )
    {
      peak = peaks.elementAt(i);
      if ( peak.h() != 0 || peak.k() != 0 || peak.l() != 0 )
        indexed_peaks.add( peak );
    }
    return indexed_peaks;
  }


/**
 *  Change the Miller indices on the specified list of peaks, by multiplying
 *  them by the specified tranformation.
 *
 *  @param peaks     The list of peaks to re-index
 *  @param hkl_tran  The tranformation to apply to the HKL values.
 */
  public static void ChangeHKLs( Vector<Peak_new> peaks, Tran3D hkl_tran )
  {
    Vector3D hkl = new Vector3D();
    Peak_new peak;
    for ( int i = 0; i < peaks.size(); i++ )
    {
      peak = peaks.elementAt(i);
      hkl.set( Math.round( peak.h() ), 
               Math.round( peak.k() ), 
               Math.round( peak.l() ) );
      hkl_tran.apply_to( hkl, hkl );
      peak.sethkl( Math.round( hkl.getX() ), 
                   Math.round( hkl.getY() ), 
                   Math.round( hkl.getZ() ) );
    }
  }


/**
 *  Re-index the peaks in the specified list of peaks, so that the Miller
 *  indicies correspond to the specified conventional cell type and centering.
 *  The original indexing MUST correspond to a Niggli reduced cell.  The
 *  re-indexed peaks are returned in place in the original list.  This 
 *  just updates the indices by multiplying by the 3x3 transformation that
 *  maps to the conventional cell, so all peaks that were originally indexed
 *  will also be indexed according to the conventional cell.
 *
 *  @param peaks             The list of peaks to re-index
 *  @param cell_type         The requested cell_type for the new conventional
 *                           cell
 *  @param centering         The requested centering for the new conventional
 *                           cell
 *  @param remove_unindexed  Flag that selects whether or not to remove any
 *                           peaks from the list that were originally not
 *                           indexed.
 */
  public static void ConvertToConventionalCell( 
                                            Vector<Peak_new> peaks,
                                            String           cell_type,
                                            String           centering,
                                            boolean          remove_unindexed )
  {
    Vector<Peak_new> indexed_peaks = RemoveUnindexedPeaks( peaks );

    float tolerance = 1;               // accept ALL previously indexed peaks

    Vector miller_ind = new Vector();
    Vector indexed_qs = new Vector();
    int n_indexed = IndexingUtils.GetIndexedPeaks( indexed_peaks,
                                                   tolerance,
                                                   miller_ind,
                                                   indexed_qs );

    System.out.println("Number of peaks previously indexed: " + n_indexed );

    Tran3D newUB = new Tran3D();
    float fit_error;
    fit_error = IndexingUtils.Optimize_UB_3D( newUB, miller_ind, indexed_qs );

    System.out.println("Original average UB:");
    System.out.println( newUB );
    IndexingUtils.ShowLatticeParameters( newUB );

    Vector<ConventionalCellInfo> possible_list;
    possible_list = ScalarUtils.getCells( newUB, true, false );
    ScalarUtils.removeBadForms( possible_list, 50 );
    System.out.println();
    System.out.printf("%9s  %-13s %-12s %-11s\n","","Type","Centering","Error");
    for ( int i = 0; i < possible_list.size(); i++ )
      System.out.println( possible_list.elementAt(i) );
    System.out.println();

    Vector<ConventionalCellInfo> requested_list;
    requested_list = ScalarUtils.getCells( newUB, cell_type, centering );
    ConventionalCellInfo best_cell = requested_list.elementAt(0);
    for ( int i = 1; i < requested_list.size(); i++ )
      if ( requested_list.elementAt(i).getError() < best_cell.getError() )
        best_cell = requested_list.elementAt(i);

    Tran3D conventional_cell_UB = best_cell.getNewUB();
    System.out.println("Conventional cell average UB:");
    System.out.println( conventional_cell_UB );
    IndexingUtils.ShowLatticeParameters( conventional_cell_UB );

    System.out.println();
    Tran3D hkl_tran = best_cell.getHKL_Tran();
    System.out.println( "HKL Tranformation = " );
    System.out.println( hkl_tran );
    System.out.println();
 
    if ( remove_unindexed )           // clear the original list and copy only
    {                                 // the indexed peaks, back into it.
      peaks.clear();
      for ( int i = 0; i < indexed_peaks.size(); i++ )
        peaks.add( indexed_peaks.elementAt(i) );
    }

    ChangeHKLs( peaks, hkl_tran );
  }


/**
 *  Re-index the peaks in the specified file of peaks, so that the Miller
 *  indicies correspond to the specified conventional cell type and centering.
 *  The original indexing MUST correspond to a Niggli reduced cell.  The
 *  re-indexed peaks can be written back to the original file or to a new
 *  peaks file.  This operator just updates the indices by multiplying by 
 *  the 3x3 transformation that maps to the conventional cell, so all peaks 
 *  that were originally indexed will also be indexed according to the 
 *  conventional cell.
 *
 *  @param peaks             The peaks file to re-index
 *  @param cell_type         The requested cell_type for the new conventional
 *                           cell
 *  @param centering         The requested centering for the new conventional
 *                           cell
 *  @param remove_unindexed  Flag that selects whether or not to remove any
 *                           peaks from the file that were originally not
 *                           indexed.
 * @param  out_file          Name of the new file to write.  If this is 
 *                           blank or a zero length string, the updated list 
 *                           of peaks will be written back to the input file.
 */
  public static void ConvertToConventionalCell( String  peaks_file,
                                                String  cell_type,
                                                String  centering,
                                                boolean remove_unindexed,
                                                String  out_file )
                     throws Exception
  {
    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( peaks_file );

    ConvertToConventionalCell( peaks, cell_type, centering, remove_unindexed );

    out_file = out_file.trim();
    if ( out_file.length() == 0 )
      Peak_new_IO.WritePeaks_new( peaks_file, peaks, false );
    else
      Peak_new_IO.WritePeaks_new( out_file, peaks, false );
  }


/**
 * Change the indexes in a peaks file by applying a specified tranformation
 * to the HKL values.
 *
 * @param peaks              The peaks file to re-index using the specified
 *                           transformation.
 * @param arr_vals           Comma separated list of the nine entries for the
 *                           3x3 transformation that will be applied to the
 *                           HKL values in the file.
 *  @param remove_unindexed  Flag that selects whether or not to remove any
 *                           peaks from the file that were originally not
 *                           indexed.
 * @param out_file           Name of the new file to write.  If this is 
 *                           blank or a zero length string, the updated list 
 *                           of peaks will be written back to the input file.
 */
  public static void ChangeHKLs( String  peaks_file, 
                                 Vector  arr_vals,
                                 boolean remove_unindexed,
                                 String  out_file )
                     throws Exception
  {
    if ( arr_vals.size() != 9 )
      throw new IllegalArgumentException("Nine floats required for matrix");

    for ( int i = 0; i < arr_vals.size(); i++ )
      System.out.println( "entry is " + arr_vals.elementAt(i) + 
                          " of type " + arr_vals.elementAt(i).getClass() );

    float[][] tran_arr = new float[3][3];
    int index = 0;
    try
    {
      for ( int row = 0; row < 3; row++ )
        for ( int col = 0; col < 3; col++ )
        {
          if ( arr_vals.elementAt(index) instanceof Float )
            tran_arr[row][col] = (Float)(arr_vals.elementAt(index));
          else if ( arr_vals.elementAt(index) instanceof Integer )
            tran_arr[row][col] = (Integer)(arr_vals.elementAt(index));
          else if ( arr_vals.elementAt(index) instanceof String )
            tran_arr[row][col] = 
                  Float.parseFloat((String)(arr_vals.elementAt(index)));
          else
            throw new IllegalArgumentException("Could not convert " 
                  + arr_vals.elementAt(index) + " to float");
          index++;
        }
    }
    catch ( Exception ex )
    {
      throw new IllegalArgumentException("Could not convert " 
                  + arr_vals.elementAt(index) + " to float");
    }

    Tran3D hkl_tran = new Tran3D( tran_arr );

    Vector<Peak_new> peaks = Peak_new_IO.ReadPeaks_new( peaks_file );

    ChangeHKLs( peaks, hkl_tran );

    if ( remove_unindexed )
      peaks = RemoveUnindexedPeaks( peaks );

    out_file = out_file.trim();
    if ( out_file.length() == 0 )
      Peak_new_IO.WritePeaks_new( peaks_file, peaks, false );
    else
      Peak_new_IO.WritePeaks_new( out_file, peaks, false );
  }
  

  public static void main( String args[] ) throws Exception
  {
    RemoveDetector( args[0], Integer.parseInt(args[1]), args[2] );
  }

}
