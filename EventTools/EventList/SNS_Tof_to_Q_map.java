/* 
 * File: SNS_Tof_to_Q_map.java
 *
 * Copyright (C) 2009, Dennis Mikkelson
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

package EventTools.EventList;

import java.util.*;
import java.io.*;

import gov.anl.ipns.MathTools.Geometry.*;

import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.dataset.*;
import DataSetTools.math.*;
import DataSetTools.trial.*;
import DataSetTools.instruments.*;

/** 
 *  This class constructs the time-of-flight to vector Q mapping information
 *  needed to efficiently map raw SNS event data into reciprocal space.
 *  The information can be constructed from an ISAW .DetCal file, or from
 *  the detector position information at the start of a peaks file for the 
 *  instrument.  NOTE: The order and size of the detectors listed in the 
 *  .DetCal or .Peaks file MUST be the same as in the *neutron_event.dat
 *  file and ids must be stored by column in order of increasing row number,
 *  so that the pixel ids match.
 *    The MapEventToQ() method does the actual mapping from (tof,id) to vector
 *  Q. The result is a list of events in Q along with a corresponding list
 *  of weight factors.
 *
 *  Following A.J.Schultz's anvred, the weight factors should be:
 * 
 *    sin^2(theta) / (lamda^4 * spec * eff * trans)
 *
 *  where theta = scattering_angle/2
 *        lamda = wavelength (in angstroms?)
 *        spec  = incident spectrum correction
 *        eff   = pixel efficiency
 *        trans = absorption correction
 *  
 *  The quantity:
 *
 *    sin^2(theta) / eff 
 *
 *  depends only on the pixel and can be pre-calculated 
 *  for each pixel.  It could be saved in array pix_weight[].
 *  For now, pix_weight[] is calculated by the method:
 *  BuildPixWeights() and just holds the sin^2(theta) values.
 *
 *  The wavelength dependent portion of the correction is saved in
 *  the array lamda_weight[].
 *  The time-of-flight is converted to wave length by multiplying
 *  by tof_to_lamda[id], then (int)STEPS_PER_ANGSTROM * lamda
 *  gives an index into the table lamda_weight[].
 *
 *  The lamda_weight[] array contains values like:
 *
 *      1/(lamda^power * spec(lamda))
 *   
 *  which are pre-calculated for each lamda.  These values are
 *  saved in the array lamda_weight[].  The optimal value to use
 *  for the power should be determined when a good incident spectrum
 *  has been determined.
 *
 *  The pixel efficiency and absorption correction are NOT CURRENTLY USED.
 *  The absorption correction, trans, depends on both lamda and the pixel,
 *  Which is a fairly expensive calulation when done for each event.
 */

public class SNS_Tof_to_Q_map 
{
  public static final float  ANGST_PER_US_PER_M = 
                                        (float)tof_calc.ANGST_PER_US_PER_M;

  public static final String SNAP  = "SNAP";
  public static final String ARCS  = "ARCS";
  public static final String SEQ   = "SEQ";
  public static final String TOPAZ = "TOPAZ";

  private final float MAX_WAVELENGTH = 50.0f;    // max in lamda_weight table
  private final float STEPS_PER_ANGSTROM = 100;  // resolution of lamda table 
  private final int   NUM_WAVELENGTHS = 
                             Math.round( MAX_WAVELENGTH * STEPS_PER_ANGSTROM );

  private boolean     debug = true;

  private IDataGrid[]  grid_arr; 
  private VecQMapper[] inverse_mapper;

  private float        L1;               // L1 in meters.
  private float        t0;               // t0 shift in 100ns units

  private float[]      QUxyz;            // unit vector in Q direction for pixel
  private float[]      tof_to_MagQ;      // magQ is tof_to_MagQ[id] / tof
  private float[]      tof_to_lamda;     // lamda is tof_to_lamda[id] * tof
  private int          first_offset = 1; // ids start at 1, so we waste one
                                         // position (#0) in our arrays to try
                                         // to avoid some off by one errors

  private float[]      lamda_weight;     // 1/(lamda^power*spec(lamda)) indexed
                                         // by STEPS_PER_ANGSTROM * lamda
  private float[]      pix_weight;       // sin^2(theta(pix_id)) / eff(pix_id)

  private int[]        counts_per_det = new int[6];

  private String            instrument_name = "NO_NAME";
  private int               run_num         = 0;
  private float             monitor_count   = 100000;
  private SampleOrientation orientation     = new SNS_SampleOrientation(0,0,0);

  /**
   *  Construct the mapping from (tof,id) to Qxyz from the information at
   *  the start of a .peaks file or .DetCal file.  NOTE: There MUST be an
   *  entry for each detector in the instrument, so a .peaks file may not 
   *  work, if some detectors are missing.
   *
   *  @param  filename  The name of the .DetCal or .peaks file with 
   *                    position information about EVERY detector and
   *                    L1 and t0 values.
   *
   *  @param  spectrum_filename Name of file containing incident spectrum
   *                            for this instrument.  The spectrum file
   *                            must be an ASCII file giving values for the
   *                            incident spectrum.  The file must be the same
   *                            form as InstrumentInfo/SNS/SNAP_Spectrum.dat
   *                            If no spectrum file is available, pass in null.
   *                            The SNAP spectrum file will be used by default
   *                            for SNAP.
   *
   *  @param  instrument_name  Name of the instrument, used to determine
   *                           pixel orderings for event file.
   */
  public SNS_Tof_to_Q_map( String filename, 
                           String spectrum_filename,
                           String instrument_name )  
         throws IOException
  {
     this.instrument_name = instrument_name;
                                                  // Bring in the grids
     FileReader     f_in        = new FileReader( filename );
     BufferedReader buff_reader = new BufferedReader( f_in );
     Scanner        sc          = new Scanner( buff_reader );

     String version_title = sc.next();
     while ( version_title.startsWith("#") )      // Skip any comment lines
     {                                            // and the version info
       sc.nextLine();
       version_title = sc.next();
     }

     float[]   L1_t0 = Peak_new_IO.Read_L1_T0( sc );
     Hashtable grids = Peak_new_IO.Read_Grids( sc );
     sc.close();

     L1 = L1_t0[0];
     t0 = L1_t0[1] * 10;                          // Need factor of 10, since
                                                  // SNS data is in terms of
                                                  // 100ns clock ticks.

                                                  // Sort the grids on ID
     Object[] obj_arr = (grids.values()).toArray();
     Arrays.sort( obj_arr, new GridID_Comparator() );
                        
     if ( debug )
     {
       System.out.println("Instrument name " + instrument_name );
       System.out.println("Loaded detectors from " + filename );
       System.out.println("Working with " + obj_arr.length + " GRIDS " );
       System.out.println("Spectrum file name " + spectrum_filename );
     }

                                                  // and record them in our
                                                  // local list.
     grid_arr = new IDataGrid[ obj_arr.length ];
     for ( int i = 0; i < grid_arr.length; i++ )
       grid_arr[i] = (IDataGrid)obj_arr[i]; 

     if ( instrument_name.equalsIgnoreCase(TOPAZ) )
       grid_arr = ReorderTOPAZ_grids( grid_arr );

     if ( instrument_name.equalsIgnoreCase(SNAP) )
       grid_arr = ReorderSNAP_grids( grid_arr );

     if ( instrument_name.equalsIgnoreCase(ARCS) )
       grid_arr = ReorderARCS_grids( grid_arr );

     if ( instrument_name.equalsIgnoreCase(SEQ) )
       grid_arr = ReorderSEQ_grids( grid_arr );

     SampleOrientation orient = new SNS_SampleOrientation( 0, 0, 0 );
     inverse_mapper = new VecQMapper[ grid_arr.length ];
     for ( int i = 0; i < grid_arr.length; i++ )
       inverse_mapper[i] = new VecQMapper( grid_arr[i], L1, t0/10, orient );

     System.out.println("In constructor, spectrum file = " + spectrum_filename);

     BuildMaps();
     if ( spectrum_filename == null || spectrum_filename.trim().length() == 0 ) 
       spectrum_filename = null;

     BuildLamdaWeights( spectrum_filename );
     BuildPixWeights();
/*
     System.out.println( "L1 = " + L1 );
     System.out.println( "t0 = " + t0 );
*/
  }


  /**
   *  Map the specified time-of-flight events to a packed array of events
   *  in reciprocal space, listing Qx,Qy,Qz for each event, interleaved
   *  in one array.  NOTE: due to array size limitations in Java,
   *  at most (2^31-1)/3 = 715.8 million events can be processed in one
   *  batch by this method. 
   *
   *  @param tofs  List of integer time-of-flight values, giving the number
   *               of 100ns clock ticks since t0 for this event.
   *
   *  @param ids   List of detector pixel ids corresponding to the listed
   *               tofs.
   *
   *  @return an array of floats containing values (Qx,Qy,Qz) for each 
   *          event, interleaved in the array. 
   */
  public FloatArrayEventList3D_2 MapEventsToQ( int[] tofs, 
                                               int[] ids  )
  {
    if ( tofs == null )
      throw new IllegalArgumentException( "Time-of-flight array is null" );

    int first = 0;
    int num_to_map = tofs.length;
    return MapEventsToQ( tofs, ids, first, num_to_map );
  }


  /**
   *  Map the specified sub-list of time-of-flight events to a packed
   *  array of events in reciprocal space, listing Qx,Qy,Qz for each event,
   *  interleaved in one array.  The weight field will be set to 0 for
   *  events with invalid ids.  For events with valid ids, the weight
   *  field is set to the product of a weight based on the pixel id, and
   *  a weight based on the neutron wavelength corresponding that event.
   *  NOTE: Since the resulting Qxyz values are packed in a one-dimensional
   *        array, at most (2^31-1)/12 = 178.9 million events can be processed
   *        in one batch by this method.
   *
   *  @param event_list  List of (tof,id) specifying detected neutrons.
   *
   *  @param first       The index of the first event to map to Q
   *
   *  @param num_to_map  The number of events to map to Q
   *
   *  @return an array of floats containing values (Qx,Qy,Qz) for each
   *          event, interleaved in the array.
   */
  public FloatArrayEventList3D_2 MapEventsToQ( ITofEventList event_list,
                                               int   first,
                                               int   num_to_map )
  {
     if ( event_list == null )
       throw new IllegalArgumentException( "event_list is null" );

     int num_events = (int)event_list.numEntries();

     if ( first < 0 || first >= num_events )
       throw new IllegalArgumentException("First index: " + first +
                 " < 0 or >= number of events in list: " + num_events );

     int     id;
     int     id_offset;
     int     index;
     float   tof_chan;
     float   magQ;
     float   qx,qy,qz;
     int     minus_id_count = 0;
     int     large_id_count = 0;
     int     skip_det_count = 0;
     int     grid_id = 0;
     float   lamda;
     int     lamda_index;
     int     last;
     int     num_mapped;

     last = first + num_to_map - 1;
     if ( last >= num_events )
       last = num_events - 1;

     num_mapped = last - first + 1;

     int[]   events  = event_list.rawEvents( first, num_to_map );
     float[] Qxyz    = new float[ 3 * num_mapped ];
     float[] weights = new float[ num_mapped ];

     for ( int i = 0; i < num_mapped; i++ )
     {
       weights[i] = 0;
       id = events[2*(i + first) + 1];
       grid_id = id / 65536;

//     if ( grid_id >= 0 && grid_id < counts_per_det.length )
//       counts_per_det[grid_id]++;

       if ( id <= 0 )
         minus_id_count++;

       else if ( id >= tof_to_MagQ.length )
         large_id_count++;

       else
       {
         tof_chan = t0 + events[2*(i + first)];
         magQ = tof_to_MagQ[id]/tof_chan;

         id_offset = 3*id;
         qx = magQ * QUxyz[id_offset++];
         qy = magQ * QUxyz[id_offset++];
         qz = magQ * QUxyz[id_offset  ];

         index = i * 3;
         Qxyz[index++] = qx;
         Qxyz[index++] = qy;
         Qxyz[index  ] = qz;

         lamda = tof_chan/10.0f * tof_to_lamda[id];
         lamda_index = (int)( STEPS_PER_ANGSTROM * lamda );

         if ( lamda_index < 0 )
           lamda_index = 0;
         if ( lamda_index >= lamda_weight.length )
           lamda_index = lamda_weight.length - 1;

         weights[i] = pix_weight[id] * lamda_weight[ lamda_index ];
       }
     }

     return new FloatArrayEventList3D_2( weights, Qxyz );
  }


  /**
   *  Map the specified sub-list of time-of-flight events to a packed 
   *  array of events in reciprocal space, listing Qx,Qy,Qz for each event, 
   *  interleaved in one array.  NOTE: due to array size limitations in Java,
   *  at most (2^31-1)/3 = 715.8 million events can be processed in one
   *  batch by this method. 
   *
   *  @param tofs        List of integer time-of-flight values, giving the 
   *                     number of 100ns clock ticks since t0 for this event.
   *
   *  @param ids         List of detector pixel ids corresponding to the listed
   *                     tofs.
   *
   *  @param first       The index of the first event to map to Q
   *
   *  @param num_to_map  The number of events to map to Q
   *
   *  @return an array of floats containing values (Qx,Qy,Qz) for each 
   *          event, interleaved in the array. 
   */
  public FloatArrayEventList3D_2 MapEventsToQ( int[] tofs, 
                                               int[] ids,
                                               int   first,
                                               int   num_to_map )
  {
     if ( tofs == null )
       throw new IllegalArgumentException( "Time-of-flight array is null" );

     if ( ids == null )
       throw new IllegalArgumentException( "Pixel id array is null" );

     if ( tofs.length != ids.length )
       throw new IllegalArgumentException("TOF array length " + tofs.length +
                                         " != id array length " + ids.length);

     if ( tofs.length > Integer.MAX_VALUE/12 )
       throw new IllegalArgumentException("TOF array length " + tofs.length +
                                         " exceeds " + Integer.MAX_VALUE/12 );

     if ( first < 0 || first >= tofs.length )
       throw new IllegalArgumentException("First index: " + first +
                                         " < 0 or >= " + tofs.length );

     if ( tofs.length == 0 )
     {
       float[] empty_Qxyz = new float[0];
       return new FloatArrayEventList3D_2( null, empty_Qxyz );
     }

     if ( first == 0 )
     {
       ITofEventList raw_events = new TofEventList( tofs, ids );
       return MapEventsToQ( raw_events, 0, num_to_map );
     }
     else                        // construct TofEventList from part of tofs[] and ids[] 
     {
       int[] new_tofs = new int[ num_to_map ];
       int[] new_ids  = new int[ num_to_map ];

       System.arraycopy( tofs, first, new_tofs, 0, num_to_map );
       System.arraycopy( ids,  first, new_ids,  0, num_to_map );

       ITofEventList raw_events = new TofEventList( new_tofs, new_ids );
       return MapEventsToQ( raw_events, 0, num_to_map );
     }

  }


  /**
   *  Map a specified qx,qy,qz back to a detectors row, col, tof and ID, if
   *  possible.  NOTE: Currently this does not use the sample orientation
   *  information, but assumes that the sample orientation angles are all 
   *  zero.  
   * 
   *  @param   qx  The x component of the q vector
   *  @param   qy  The y component of the q vector
   *  @param   qz  The z component of the q vector
   *
   *  @return an array of four floats containing the fractional row, column, 
   *          time-of-flight and detector ID correspondingg the specified
   *          Q vector.  If the Q vector does not match any pixel on any
   *          of the detectors handled by this object, NULL is returned.
   */
  public float[] QtoRowColTOF_ID( float qx, float qy, float qz )
  {
    Vector3D q_vec = new Vector3D( qx, qy, qz );

    q_vec.multiply( (float)(2*Math.PI)  );

    for ( int k = 0; k < inverse_mapper.length; k++ )
    {
      float[] recalc = inverse_mapper[k].QtoRowColTOF( q_vec );
      if ( recalc != null )
      {
//      recalc[2] = recalc[2] - t0/10;      // switch from "theoretical" tof to
                                            // measured tof, in microseconds?
        float[] result = { recalc[0], recalc[1], recalc[2], grid_arr[k].ID() };
        return result;
      }
    }
    return null;
  }


  /**
   *  Construct a Peak_new object from specified qx,qy,qz components if
   *  possible.  If the specified qx,qy,qz values do not map to one of
   *  the detectors held by this object, then null is returned.
   *
   *  NOTES:
   *
   *  1. Currently this does not use the sample orientation
   *     information, but assumes that the sample orientation 
   *     angles are all zero.  
   *  2. The h,k,l, seqnum, ipkobs and Facility fields are NOT set
   *     by this method.
   *  3. Code using this should check that the result is not null     
   * 
   *  @param   qx  The x component of the q vector
   *  @param   qy  The y component of the q vector
   *  @param   qz  The z component of the q vector
   *
   *  @return A Peak_new object corresponding to the specified qx,qy,qz,
   *          or null if the specified qx,qy,qz don't map back to any
   *          detector.
   */
  public Peak_new GetPeak( float qx, float qy, float qz )
  {
    float[] row_col_tof_ID = QtoRowColTOF_ID( qx, qy, qz );
    
    if ( row_col_tof_ID == null )
      return null;

    int det_id = (int)row_col_tof_ID[3];
     
    boolean   found = false;                    // find the IDataGrid with
    IDataGrid grid  = null;                     // the correct ID
    int i = 0;
    while ( !found && i < grid_arr.length )
    {
      grid = grid_arr[i];
      if ( grid.ID() == det_id )
        found = true;
      else
        i++;
    }

    Peak_new peak = new Peak_new( run_num,
                                  monitor_count,
                                  row_col_tof_ID[1],
                                  row_col_tof_ID[0],
                                  row_col_tof_ID[2]/10,   // use tof since not
                                                          // histogrammed
                                  grid,
                                  orientation,
                                  row_col_tof_ID[2],
                                  L1,
                                  t0  );
    
    peak.setInstrument(  instrument_name );
    
    return peak;
  }


  /**
   *  Build the list of weights corresponding to different wavelengths.
   *  Although the spectrum file need not have a fixed number of
   *  points, it MUST have the spectrum recorded as a histogram with one
   *  more bin boundary than the number of bins.
   *    The entries in the table produced are:
   *
   *     1/( lamda^power * spec(lamda) )
   *
   *  Where power was chosen to give a relatively uniform intensity display
   *  in 3D.  The power is currently 2.4.
   *  The spectrum values are first normalized so that the MAXIMUM value is
   *  1 and the minimum value is 0.1.
   */
  private void BuildLamdaWeights( String spectrum_file_name )
  {
    float   MIN_SPECTRUM_VALUE = 0.1f;
    float   power = 2.4f;

    float   lamda; 
    float[] spec_val   = null;
    float[] spec_lamda = null;

                                              // build in dependence on lamda
    System.out.println("Building approximate weighting (no spectrum)");

    lamda_weight = new float[ NUM_WAVELENGTHS ];
    for ( int i = 0; i < lamda_weight.length; i++ )
    {
      lamda = i / STEPS_PER_ANGSTROM;
      lamda_weight[i] = (float)(1/Math.pow(lamda,power));
    }

    System.out.println("Spectrum file specified as: " + spectrum_file_name );
    if ( spectrum_file_name == null  ||
         spectrum_file_name.trim().length() == 0 )    // no incident spectrum 
      return;

                                                      // check if file exists
    File spec_file = new File( spectrum_file_name );
    if ( !spec_file.exists() )
    {
      System.out.println("File Doesn't Exist " + spectrum_file_name);
      return;
    }
                                                      // now load the file
    int num_bins = 0;
    try
    { 
      FileReader     f_in        = new FileReader( spectrum_file_name );
      BufferedReader buff_reader = new BufferedReader( f_in );
      Scanner        sc          = new Scanner( buff_reader );

      for ( int i = 0; i < 6; i++ )                   // skip info lines
        sc.nextLine();

      String num_y_line = sc.nextLine().trim();

      int blank_index = num_y_line.lastIndexOf(" ");

      num_y_line = num_y_line.substring(blank_index);
      num_y_line = num_y_line.trim();

      Integer NUM_BINS = new Integer( num_y_line );
      num_bins = NUM_BINS;
      if ( num_bins <= 0 )
      {
        System.out.println("NEGATIVE NUMBER OF BINS IN " + spectrum_file_name);
        return;
      }

      spec_lamda = new float[ num_bins+1 ];
      spec_val   = new float[ num_bins ];
      for ( int i = 0; i < num_bins; i++ )
      {
        spec_lamda[i] = sc.nextFloat();
        spec_val[i]   = (float)sc.nextFloat();
      }
    }
    catch ( Exception ex )
    {
      System.out.println("FAILED TO LOAD SPECTRUM: " + spectrum_file_name );
      System.out.println("EXCEPTION = " + ex + "\n" + 
                         "NOT WEIGHTING BY INCIDENT SPECTRUM!" );
      return;
    }

    System.out.println("Setting lamda weights from spectrum file: " +
                        spectrum_file_name );
                                                      // normalize spectrum
    float max = spec_val[0];
    for ( int i = 0; i < num_bins; i++ )
      if ( spec_val[i] > max )
        max = spec_val[i];

    if ( max <= 0 )
    {
      System.out.println("ERROR: Spectrum file had no positive entries");
      return;
    }
                                                  // set so max value == 1
    for ( int i = 0; i < num_bins; i++ )
      spec_val[i] /= max;

    for ( int i = 0; i < num_bins; i++ )          // clamp to be > 0
      if ( spec_val[i] < MIN_SPECTRUM_VALUE )
        spec_val[i] = MIN_SPECTRUM_VALUE;
                                                  // now adjust weights based
                                                  // on incident spectru,
    int   index;
    float val;
    float lamda_min  = spec_lamda[0];
    float lamda_max  = spec_lamda[num_bins - 2]; 
    float spec_first = spec_val[0];
    float spec_last  = spec_val[num_bins - 1]; 
    for ( int i = 0; i < lamda_weight.length; i++ )
    {
      lamda = i / STEPS_PER_ANGSTROM;
      if ( lamda <= lamda_min )
        val = spec_first; 
      else if ( lamda >= lamda_max )
        val = spec_last;
      else
      {
        index = Arrays.binarySearch( spec_lamda, lamda ); 
        if ( index < 0 )
          index = -index - 1; 
        if ( index > num_bins - 1 )
          index = num_bins - 1;
        val = spec_val[index];
      }
      lamda_weight[i] = lamda_weight[i] / val;
    }
  }


  /**
   *  Build the pix_weight[] factors, corresponding to different pixels.  
   *  The pix_weight[] factor is just sin^2(theta).
   */
  private void BuildPixWeights()
  {
    int pix_count = 0;                             // first count the pixels
    for ( int  i = 0; i < grid_arr.length; i++ )
    {
      IDataGrid grid = grid_arr[i];
      pix_count += grid.num_rows() * grid.num_cols();
    }

    pix_weight   =  new float[ (pix_count + first_offset) ];

    Vector3D  pix_pos;                             // using IPNS coords 
    Vector3D  beam_vec = new Vector3D( 1, 0, 0 );  // internally
    double    cos_2_theta;
    double    theta;
    double    sine_theta;
    IDataGrid grid;
    int       n_rows;
    int       n_cols;

    int index = first_offset;                       // pixel index starts at 1
    for ( int  i = 0; i < grid_arr.length; i++ )
    {
      grid = grid_arr[i];
      n_rows = grid.num_rows();
      n_cols = grid.num_cols();

      for ( int col = 1; col <= n_cols; col++ )
        for ( int row = 1; row <= n_rows; row++ )
        {
           pix_pos = grid.position( row, col );
           pix_pos.normalize();

           cos_2_theta = pix_pos.dot( beam_vec );
           theta = Math.acos( cos_2_theta ) / 2;
           sine_theta = Math.sin( theta );

           pix_weight[ index++ ] = (float)(sine_theta * sine_theta);
        }
    }
  }


  /**
   *  Build the tables giving the unit vector in the direction of Q,
   *  the conversion constant from time-of-flight to magnitude of Q and
   *  the conversion constant for time-of-flight to lamda. 
   *  each pixel in the detector.
   */
  private void BuildMaps()
  {
    int pix_count = 0;                             // first count the pixels
    for ( int  i = 0; i < grid_arr.length; i++ )
    {
      IDataGrid grid = grid_arr[i];
      pix_count += grid.num_rows() * grid.num_cols(); 
    }                                          

    tof_to_lamda =  new float[ (pix_count + first_offset) ];
                                                   // NOTE: the pixel IDs 
                                                   // start at 1 in the file
                                                   // so to avoid shifting we
                                                   // will also start at 1
    tof_to_MagQ = new float[  (pix_count + first_offset)]; 
                                                   // Scale factor to convert
                                                   // tof to Magnitude of Q
    QUxyz       = new float[3*(pix_count + first_offset)];
                                                   // Interleaved components
                                                   // of unit vector in the
                                                   // direction of Q for this
                                                   // pixel.

                                                   // Since SNS tofs are in
                                                   // units of 100ns, we need
                                                   // the factor of 10 in this
                                                   // partial constant
    float     part = (float)(10 * 4 * Math.PI / tof_calc.ANGST_PER_US_PER_M);
                                                   
    float     two_theta;
    float     L2;
    Vector3D  pix_pos;
    Vector3D  unit_qvec;
    int       n_rows,
              n_cols;
    float[]   coords;
    IDataGrid grid;
    int       index;
    pix_count = first_offset;                       // pixel index starts at 1
    for ( int  i = 0; i < grid_arr.length; i++ )
    {
      grid = grid_arr[i];
      n_rows = grid.num_rows();
      n_cols = grid.num_cols();

      for ( int col = 1; col <= n_cols; col++ )
        for ( int row = 1; row <= n_rows; row++ )
        {
           pix_pos    = grid.position( row, col );
           L2         = pix_pos.length();
/*
           if ( row == 1 && col == 1 )
             System.out.println("L2 = " + L2);
*/
           coords     = pix_pos.get();
           coords[0] -= L2;                        // internally using IPNS
                                                   // coordinates
           unit_qvec = new Vector3D(coords);
           unit_qvec.normalize();
           index = pix_count * 3;
           QUxyz[ index     ] = unit_qvec.getX();
           QUxyz[ index + 1 ] = unit_qvec.getY();
           QUxyz[ index + 2 ] = unit_qvec.getZ();

           two_theta   = (float)Math.acos( pix_pos.getX() / L2 );

           tof_to_MagQ[pix_count] = 
                          (float)(part * (L1 + L2) * Math.sin(two_theta/2));

           tof_to_lamda[pix_count] = ANGST_PER_US_PER_M /(L1 + L2);

           pix_count++; 
        }
    }
                                                   // As a check, dump out
                                                   // QUxyz for the first pixel
                                                   // in each detector
    /*
    index = 0; 
    for ( int i = 0; i < grid_arr.length; i++ )
    {
      System.out.printf( "For first pixel in detector #%2d  " +
                         "MagQ conv factor = %7.4f  " + 
                         "Qx = %6.3f  Qy = %6.3f  Qz = %6.3f\n",
                          i,
                          tof_to_MagQ[i],
                          QUxyz[ 3*index     ],  
                          QUxyz[ 3*index + 1 ],  
                          QUxyz[ 3*index + 2 ]  );
      grid = grid_arr[i];
      index += grid.num_rows() * grid.num_cols();
    }
    */
  }


  /**
   *  Rearrange the TOPAZ grids so that there is a "dummy" grid zero,
   *  since the TOPAZ grids are numbered starting at 1 instead of at zero.
   *
   *  @param grid_arr  The original list of actual grids.
   *
   *  @return an new array of grids with a dummy grid zero at the start
   *          of the array, followed by the actual grids, in order.
   */
  private IDataGrid[] ReorderTOPAZ_grids( IDataGrid[] grid_arr )
  {
    if ( debug )
      System.out.println("Reorder TOPAZ grids");

    IDataGrid[] temp = new IDataGrid[ grid_arr.length + 1 ];

    temp[0] = MakeDummyTOPAZDetector();
    for ( int i = 0; i < grid_arr.length; i++ )
      temp[i+1] = grid_arr[i];

    return temp;
  }


  /**
   *  Make a "dummy" detector to fill in position zero of the array of
   *  detectors for TOPAZ.  This is needed since the events coming to 
   *  TOPAZ from the DAS will start with ID = 1+65,536, not with 1 :-(
   *  The detector is 1/10 the size of an actual detector and is placed
   *  above the sample, so if events come in with ID's starting at 1, 
   *  the counts should show up.  No actual detector will be at this
   *  position.
   */
  private IDataGrid  MakeDummyTOPAZDetector()
  {
    float DET_WIDTH  = 0.015f;        // make this 1/10 the size of a real
    float DET_HEIGHT = 0.015f;        // detector.  If it gets counts we
    float DET_DEPTH  = 0.002f;        // should see them!
    int   N_ROWS     = 256;
    int   N_COLS     = 256;
    int   ID         = 0;

    Vector3D center    = new Vector3D(0, 0, 0.5f);    // IPNS coords
    Vector3D base_vec  = new Vector3D(1, 0, 0);
    Vector3D up_vec    = new Vector3D(0, 1, 0);

    UniformGrid grid = new UniformGrid( ID, "m",
                                        center, base_vec, up_vec,
                                        DET_WIDTH, DET_HEIGHT, DET_DEPTH,
                                        N_ROWS, N_COLS );
    return grid;
  }


  /**
   *  Reorder the SNAP grids, so that they appear in the array of grids in the
   *  order that corresponds to the DAS pixel numbering.
   *  Looking at the face of the detector array from the sample position,
   *  the DAS numbers the 3x3 array of detectors as:
   *  
   *    2   5   8
   *    1   4   7
   *    0   3   6
   *
   *  The detectors as read into ISAW from the NeXus file are arranged
   *  and given detector numbers as:
   *
   *    4   3   2
   *    7   6   5
   *   10   9   8 
   *
   *  These initially occupy grid array positions numbered starting at 
   *  zero, as shown:
   *
   *    2   1   0
   *    5   4   3
   *    8   7   6
   *
   *  This method re-forms the grid array, so that the correct IDataGrid
   *  occupies the position indexed by the id information from the DAS.
   *  For example, the DAS expects that the pixels on its detector 0,
   *  are positioned and orgranized like ISAW/NeXus detector number 10
   *  in position 8 of the original array.
   *
   *  @param grid_arr  List of IDataGrids in order of increasing
   *                   detector number (according to ISAW/NeXus).
   */
  private IDataGrid[] ReorderSNAP_grids( IDataGrid[] grid_arr )
  {
    if ( debug )
      System.out.println("Reorder SNAP grids");

    if ( grid_arr.length != 9 )
      System.out.println("WARNING: SNAP configuration changed. \n" +
                         "Update SNS_Tof_to_Q_map.ReorderSNAP_grids!" );

    IDataGrid[] temp = new IDataGrid[ grid_arr.length ];
 
    for ( int i = 0; i < grid_arr.length; i++ )
      temp[i] = grid_arr[i];

    grid_arr[0] = temp[8];
    grid_arr[1] = temp[5];
    grid_arr[2] = temp[2];
    grid_arr[3] = temp[7];
    grid_arr[4] = temp[4];
    grid_arr[5] = temp[1];
    grid_arr[6] = temp[6];
    grid_arr[7] = temp[3];
    grid_arr[8] = temp[0];

    return grid_arr;
  }


  /**
   *  Reorder the ARCS grids, so that they appear in the array of grids in the
   *  order that corresponds to the DAS pixel numbering.
   *
   *  This method re-forms the grid array, so that the correct IDataGrid
   *  occupies the position indexed by the id information from the DAS.
   *
   *  @param grid_arr  List of IDataGrids in order of increasing
   *                   detector number (according to ISAW/NeXus).
   */
  private IDataGrid[] ReorderARCS_grids( IDataGrid[] grid_arr )
  {
    if ( debug )
      System.out.println("Reorder ARCS grids");

    if ( grid_arr.length != 115 )
      System.out.println("WARNING: ARCS configuration changed. \n" +
                         "Update SNS_Tof_to_Q_map.ReorderARCS_grids!" );

    IDataGrid[] temp = new IDataGrid[ grid_arr.length ];

    for ( int i = 0; i < grid_arr.length; i++ )
      temp[i] = grid_arr[i];

    
    int index = 77;
    for ( int i = 0; i <= 37; i++ )
      grid_arr[i] = temp[index++];

    index = 38;
    for ( int i = 38; i <= 76; i++ )
      grid_arr[i] = temp[index++];

    index = 0;
    for ( int i = 77; i <= 114; i++ )
      grid_arr[i] = temp[index++];
/*
    for ( int i = 0; i < grid_arr.length; i++ )
      System.out.println( Peak_new_IO.GridString(grid_arr[i]) );
*/

    return grid_arr;
  }


  /**
   *  NOT WORKING YET !!!!!!!!!!!!!!!!!!
   *  Reorder the SEQIOIA grids, so that they appear in the array of grids
   *  in the order that corresponds to the DAS pixel numbering.
   *
   *  This method re-forms the grid array, so that the correct IDataGrid
   *  occupies the position indexed by the id information from the DAS.
   *  As of 6/29/09 the relationship between the grids as loaded by ISAW
   *  and the detectors/pixels written by the DAS is:
   *
   *    MY SEQ GRIDS        SEQ DAS INDEX
   *      MISSING          A1-A37     0 -  36
   *      0 -  36          B1-B37    37 -  73
   *     37 -  75          C1-C37    74 - 112 ( two split packs: C25T, C25B
   *                                                             C26T, C26B )
   *     76 - 112          D1-D37   113 - 149
   *      MISSING          E1-E37   150 - 186
   *
   *  see: https://flathead.ornl.gov/trac/TranslationService/browser/
   *                                 calibration/geometry/SEQ_geom.txt
   *
   *  @param grid_arr  List of IDataGrids in order of increasing
   *                   detector number (according to ISAW/NeXus).
   */
  private IDataGrid[]  ReorderSEQ_grids( IDataGrid[] grid_arr )
  {
    if ( debug )
      System.out.println("Reorder SEQ grids");

    int N_SEQUOIA_GRIDS = 187;      // total projected number of Data grids
                                    // eventually in SEQUOIA

    IDataGrid[] new_grids = new IDataGrid[ N_SEQUOIA_GRIDS ];

    if ( grid_arr.length != 113 )
      System.out.println("WARNING: SEQUOIA configuration changed. \n" +
                         "Update SNS_Tof_to_Q_map.ReorderSEQ_grids!" );

    IDataGrid dummy = makeDummyGrid();

    for ( int i = 0; i <= 36; i++ )
      new_grids[i] = dummy;

    int index = 0;
    for ( int i = 37; i <= 73; i++ )
      new_grids[i] = grid_arr[index++];

    index = 37;
    for ( int i = 74; i <= 112; i++ )
      new_grids[i] = grid_arr[index++];

    index = 76;
    for ( int i = 113; i <= 149; i++ )
      new_grids[i] = grid_arr[index++];

    for ( int i = 150; i <= 186; i++ )
      new_grids[i] = dummy;
/*
    for ( int i = 0; i < new_grids.length; i++ )
      System.out.println( Peak_new_IO.GridString(new_grids[i]) );
*/
    return new_grids;
  }


  private IDataGrid  makeDummyGrid()
  {
    int      id       =  1;
    String   units    = "meters";
    Vector3D center   = new Vector3D( 0, 10, 0 );
    Vector3D x_vector = new Vector3D( 1,  0, 0 );
    Vector3D y_vector = new Vector3D( 0,  1, 0 );
    float    width    = 0.1f;
    float    height   = 0.1f;
    float    depth    = 0.0001f;
    int      n_rows   = 128;
    int      n_cols   = 1;

    UniformGrid dummy = new UniformGrid( id, units,
                                         center, x_vector, y_vector,
                                         width, height, depth,
                                         n_rows, n_cols );
    return dummy;
  }


  public class  GridID_Comparator implements Comparator
  {
   /**
    *  Compare two IDataGrid objects based on their IDs.
    *
    *  @param  obj_1   The first grid 
    *  @param  obj_2   The second grid 
    *
    *  @return An integer indicating whether grid_1's ID is greater than,
    *          equal to or less than grid_2's ID.
    */
    public int compare( Object obj_1, Object obj_2 )
    {
      int id_1 = ((IDataGrid)obj_1).ID();
      int id_2 = ((IDataGrid)obj_2).ID();

      if ( id_1 > id_2 )
        return 1;
      else if ( id_1 == id_2 )
        return 0;
      else 
        return -1;
    }
  }

} 

