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
 *  needed to efficiently process raw SNS event data into reciprocal space.
 *  The information can be constructed from an ISAW .DetCal file, or from
 *  the detector position information at the start of a peaks file for the 
 *  instrument.  NOTE: The order and size of the detectors listed in the 
 *  .DetCal or .Peaks file MUST be the same as in the *neutron_event.dat
 *  file and ids must be stored by column in order of increasing row number,
 *  so that the pixel ids match.
 */
public class SNS_Tof_to_Q_map 
{
  public static final float  ANGST_PER_US_PER_M = 
                                        (float)tof_calc.ANGST_PER_US_PER_M;

  public static final String SNAP = "SNAP";
  public static final String ARCS = "ARCS";
  public static final String SEQ  = "SEQ";
  public static final int    NUM_TOF_WEIGHTS = 166666/10;  // 1us tof bins

  private IDataGrid[]  grid_arr; 
  private VecQMapper[] inverse_mapper;

  private float        L1;             // L1 in meters.
  private float        t0;             // t0 shift in 100ns units
  private float[]      QUxyz;          // unit vector in Q direction for pixel
  private float[]      tof_to_MagQ;    // magQ is tof_to_MagQ[id] / tof
  private float[]      tof_to_lamda;   // lamda is tof_to_lamda[id] * tof
  private float[]      lamda_weight;   // 1/(lamda^4 * spec(lamda)) indexed by
                                       // 100 * lamda
  private float[]      pix_weight;     // sin^2(theta(pix_id)) / eff(pix_id)

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
   *  @param  instrument_name  Name of the instrument, used to determine
   *                           pixel orderings for event file.
   */
  public SNS_Tof_to_Q_map( String filename, String instrument_name )  
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

     float[]   L1_t0          = Peak_new_IO.Read_L1_T0( sc );
     Hashtable grids          = Peak_new_IO.Read_Grids( sc );
     sc.close();

     L1 = L1_t0[0];
     t0 = L1_t0[1] * 10;                          // Need factor of 10, since
                                                  // SNS data is in terms of
                                                  // 100ns clock ticks.

                                                  // Sort the grids on ID
     Object[] obj_arr = (grids.values()).toArray();
     Arrays.sort( obj_arr, new GridID_Comparator() );
/*                        
     System.out.println("Working with " + obj_arr.length + " GRIDS " );
*/
                                                  // and record them in our
                                                  // local list.
     grid_arr = new IDataGrid[ obj_arr.length ];
     for ( int i = 0; i < grid_arr.length; i++ )
       grid_arr[i] = (IDataGrid)obj_arr[i]; 

     if ( instrument_name.equalsIgnoreCase(SNAP) )
       ReorderSNAP_grids( grid_arr );

     if ( instrument_name.equalsIgnoreCase(ARCS) )
       ReorderARCS_grids( grid_arr );

     if ( instrument_name.equalsIgnoreCase(SEQ) )
       grid_arr = ReorderSEQ_grids( grid_arr );

     SampleOrientation orient = new SNS_SampleOrientation( 0, 0, 0 );
     inverse_mapper = new VecQMapper[ grid_arr.length ];
     for ( int i = 0; i < grid_arr.length; i++ )
       inverse_mapper[i] = new VecQMapper( grid_arr[i], L1, t0/10, orient );

     
     BuildMaps();
     String spectrum_file_name = "/home/dennis/ISAW/SNAP_Spectrum.dat";
     BuildLamdaWeights( spectrum_file_name );
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
  public FloatArrayEventList3D_2 MapEventsToQ( int[] tofs, int[] ids )
  {
     if ( tofs == null )
       throw new IllegalArgumentException( "Time-of-flight array is null" );

     if ( ids == null )
       throw new IllegalArgumentException( "Pixel id array is null" );

     if ( tofs.length != ids.length )
       throw new IllegalArgumentException("TOF array length " + tofs.length +
                                         " != id array length " + ids.length);

     if ( tofs.length > Integer.MAX_VALUE/3 )
       throw new IllegalArgumentException("TOF array length " + tofs.length +
                                         " exceeds " + Integer.MAX_VALUE/3 );
     if ( tofs.length == 0 )
     {
       float[] empty_Qxyz = new float[0];
       return new FloatArrayEventList3D_2( null, empty_Qxyz );
     }

     float[] Qxyz    = new float[ 3 * tofs.length ];
     float[] weights = new float[ tofs.length ];
     int     id;
     int     id_offset;
     int     index;
     float   tof_chan;
     float   magQ;
     float   qx,qy,qz;
     int     minus_id_count = 0;
     int     large_id_count = 0;
//   int     test_count = 0;
//   float   inv_lamda_4;                     // 1/lamda^4
     float   lamda;
     int     lamda_index;
     for ( int i = 0; i < tofs.length; i++ )
     {
       id = ids[i];
       if ( id > 0 && id < tof_to_MagQ.length )
       {
         tof_chan = t0 + tofs[i];
         magQ = tof_to_MagQ[id]/tof_chan;

         id_offset = 3*id;
         qx = magQ * QUxyz[id_offset++];
         qy = magQ * QUxyz[id_offset++];
         qz = magQ * QUxyz[id_offset  ];

         index = i * 3;
         Qxyz[index++] = qx;
         Qxyz[index++] = qy;
         Qxyz[index  ] = qz;

         //
         // TODO
         //
         // following A.J.Schultz's anvred, weight factor should be:
         //
         //  sin^2(theta) / (lamda^4 * spec * eff * trans)
         //
         // where theta = scattering_angle/2
         //       lamda = wavelength (in angstroms?)
         //       spec  = incident spectrum correction
         //       eff   = pixel efficiency
         //       trans = absorption correction
         //
         // NOTE:
         //
         //   sin^2(theta) / eff  
         //
         // depends only on the pixel and can be pre-calculated 
         // for each pixel.  It is saved in array pix_weight[]
         //
         // The time-of-flight is converted to wave length by multiplying
         // by tof_to_lamda[id], then (int)100*lamda gives an index into
         // the table lamda_weight[] which contains 500 values for:
         //   
         //   1/(lamda^4 * spec(lamda))
         //
         // which are pre-calculated for each lamda.  These values are
         // saved in array lamda_weight[].
         //
         // trans depends on both lamda and the pixel.  This is a fairly
         // expensive calculation and is omitted for now.
/*
         inv_lamda_4 = 1.0f / ( tof_chan/10.0f * tof_to_lamda[id] );
         inv_lamda_4 = inv_lamda_4 * inv_lamda_4 * inv_lamda_4;
         weights[i] = pix_weight[id] * inv_lamda_4;
*/
         lamda = tof_chan/10.0f * tof_to_lamda[id];
         lamda_index = (int)(100*lamda);

/*
         if ( i < 100 )
           System.out.println( "i, lamda, lamda_index, lamda_weight = " +
                                i + ", " 
                                + lamda + ", " + 
                                + lamda_index + ", " + 
                                + lamda_weight[ lamda_index ] );
*/
         if ( lamda_index < 0 )
           lamda_index = 0;
         if ( lamda_index > lamda_weight.length )
           lamda_index = lamda_weight.length - 1;
  
         weights[i] = pix_weight[id] * lamda_weight[ lamda_index ];
//       weights[i] = pix_weight[id] * tof_weight[ (int)(tof_chan/10.0f) ];
//       weights[i] = tof_weight[ (int)(tof_chan/10.0f) ];

//       weights[i] = magQ * magQ * magQ/1000;   // TODO remove temporary hack

/*       // TEST map from qx,
         test_count++;
         if ( test_count % 10000 == 0 )
         {
           int   row = (id-1) % 256 + 1;
           int   col = ((id-1) / 256 ) % 256 + 1;
           float tof = (tofs[i] + t0)/10f;

           int det_id = id / (256 * 256);
           Vector3D q_vec = new Vector3D( qx, qy, qz );

           System.out.println("ID: " + det_id);
           System.out.printf( "Original  col = %5d  row = %5d  chan = %8.1f\n",
                               col, row, tof );

           float[] rctofid = QtoRowColTOF_ID( qx, qy, qz );
           if ( rctofid != null )
             System.out.printf( 
               "Re-mapped col = %5.0f  row = %5.0f  tof  = %8.0f  ID = %2.0f\n",
                      rctofid[1], rctofid[0], rctofid[2], rctofid[3] );
           else
             System.out.println("ERROR FAILED TO MAP Q TO ROW COL TOF ID");
         } 
*/
       }
       else if ( id < 0 )
         minus_id_count++;
       else if ( id >= tof_to_MagQ.length )
         large_id_count++;
     } 
/*
     System.out.println("tof_to_MagQ.length = " + tof_to_MagQ.length );
     System.out.println("NUMBER OF EVENTS WITH -ID      = " + minus_id_count );
     System.out.println("NUMBER OF EVENTS WITH LARGE ID = " + large_id_count );

     for ( int i = 0; i < 10; i++ )
      System.out.printf("tof: %8.1f  id: %7d " +
                        "  Qx: %6.2f  Qy: %6.2f  Qz: %6.2f\n",
                        (tofs[i]/10.0), ids[i], 
                        Qxyz[3*i], Qxyz[3*i+1], Qxyz[3*i+2]);
*/
     return new FloatArrayEventList3D_2( weights, Qxyz );
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
   *  Build the list of weights corresponding to different times-of-flight.
   *  NOT COMPLETE.  Currently this method just makes a rough approximation
   *  to the correct weight factor. (TODO)
   */
/*
  private void BuildTofWeights()
  {
    tof_weight = new float[ NUM_TOF_WEIGHTS ];
    for ( int i = 0; i < tof_weight.length; i++ )
      tof_weight[i] = 1.0e10f/((float)Math.pow(i,2.7));
  }
*/

  /**
   *  Build the list of weights corresponding to different wavelengths.
   *  NOTE: Although the spectrum file need not have a fixed numbe of
   *  points, it MUST have the spectrum recorded in steps of 0.01 angstrom
   *  starting at 0 out to the number of bins.  The resulting table will
   *  allow (int)100*lamda to be used as an index into the table to 
   *  find the weight to use for the specified lamda in Angstroms.
   *  The entries in the table are:
   *
   *     1/( lamda^4 * spec(lamda) )
   *
   *  The spectrum values will be normalized so that the MAXIMUM value is
   *  1 and the minimum value is .05.
   */
  private void BuildLamdaWeights( String spectrum_file_name )
  {
    int     DEFAULT_NUM_WAVELENGTHS = 500;
    float   MIN_SPECTRUM_VALUE      = 0.15f;
    float   lamda; 
                                              // first try to load the file
    boolean build_from_file = true;
    int     num_bins = DEFAULT_NUM_WAVELENGTHS;
    float[] spectrum = null;


    try
    { 
      FileReader     f_in        = new FileReader( spectrum_file_name );
      BufferedReader buff_reader = new BufferedReader( f_in );
      Scanner        sc          = new Scanner( buff_reader );

      for ( int i = 0; i < 6; i++ )                // skip info lines
        sc.nextLine();

      String num_y_line = sc.nextLine().trim();

      int blank_index = num_y_line.lastIndexOf(" ");

      num_y_line = num_y_line.substring(blank_index);
      num_y_line = num_y_line.trim();

      Integer NUM_BINS = new Integer( num_y_line );
      num_bins = NUM_BINS;

      spectrum = new float[ num_bins ];
      for ( int i = 0; i < num_bins; i++ )
      {
        sc.nextDouble();
        spectrum[i] = (float)sc.nextDouble();
      }
      build_from_file = true;
    }
    catch ( Exception ex )
    {
      System.out.println("EXCEPTION = " + ex );
      ex.printStackTrace();
      System.out.println("Failed to read spectrum file "+spectrum_file_name);
      System.out.println("Using default approximate correcton" );
      build_from_file = false;
    }

    if ( build_from_file )
    {
//    System.out.println("Building using spectrum file " );
                                                      // normalize spectrum
      float max = 0;
      for ( int i = 0; i < num_bins; i++ )
        if ( spectrum[i] > max )
          max = spectrum[i];

      if ( max <= 0 )
        build_from_file = false;
      else
      {      
        for ( int i = 0; i < num_bins; i++ )
          spectrum[i] /= max;

        for ( int i = 0; i < num_bins; i++ )          // clamp to be > 0
          if ( spectrum[i] < MIN_SPECTRUM_VALUE )
            spectrum[i] = MIN_SPECTRUM_VALUE;
                                                      // compute weights
        lamda_weight = new float[ num_bins ];
        for ( int i = 0; i < num_bins; i++ )
        {
          lamda = i/100f;
          lamda_weight[i] = 1f/(lamda*lamda*lamda*lamda * spectrum[i]);
        }
      }
    }

    if ( !build_from_file )                   // make rough approximation
    {
//    System.out.println("Building using weighting " );
      
      lamda_weight = new float[ DEFAULT_NUM_WAVELENGTHS ];
      for ( int i = 0; i < lamda_weight.length; i++ )
      {
        lamda = i/100f;
        lamda_weight[i] = (float)(1.0/Math.pow(lamda,2.4));
      }
    }

  }


  /**
   *  Build the list of weights corresponding to different pixels. 
   *  NOT COMPLETE.  Currently this method just makes a rough approximation
   *  to the correct weight factor. (TODO)
   */
  private void BuildPixWeights()
  {
    int first_offset = 1;
    int pix_count = 0;                             // first count the pixels
    for ( int  i = 0; i < grid_arr.length; i++ )
    {
      IDataGrid grid = grid_arr[i];
      pix_count += grid.num_rows() * grid.num_cols();
    }

    pix_weight   =  new float[ (pix_count + first_offset) ];
    tof_to_lamda =  new float[ (pix_count + first_offset) ];

    Vector3D  pix_pos;                             // using IPNS coords 
    Vector3D  beam_vec = new Vector3D( 1, 0, 0 );  // internally
    double    cos_2_theta;
    double    theta;
    double    sine_theta;
    float     L2;
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
           L2 = pix_pos.length();
           pix_pos.normalize();

           cos_2_theta = pix_pos.dot( beam_vec );
           theta = Math.acos( cos_2_theta ) / 2;
           sine_theta = Math.sin( theta );

           tof_to_lamda[ index ] = ANGST_PER_US_PER_M /(L1 + L2);
           pix_weight[ index++ ] = (float)(sine_theta * sine_theta);
/*
           if ( row == 1 && col == 1 )
           {
             System.out.println("TOTAL PATH   = " + (L1+L2) );
             System.out.println("tof_to_lamda = " + tof_to_lamda[index-1] );
           }
*/
        }
    }
  }



  /**
   *  Build the tables giving the unit vector in the direction of Q and
   *  the conversion constant from time of flight to magnitude of Q for
   *  each pixel in the detector.
   */
  private void BuildMaps()
  {
    int first_offset = 1;
    int pix_count = 0;                             // first count the pixels
    for ( int  i = 0; i < grid_arr.length; i++ )
    {
      IDataGrid grid = grid_arr[i];
      pix_count += grid.num_rows() * grid.num_cols(); 
    }                                          
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
  private void ReorderSNAP_grids( IDataGrid[] grid_arr )
  {
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
  private void ReorderARCS_grids( IDataGrid[] grid_arr )
  {
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
    *  @param  grid_1   The first grid 
    *  @param  grid_2   The second grid 
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

