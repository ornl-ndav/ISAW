/* 
 * File: IntegratePeaksProcess.java
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

package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new_IO;
import DataSetTools.operator.DataSet.Attribute.LoadOrientation;

import gov.anl.ipns.Util.Sys.StringUtil;

import java.util.*;
import java.io.*;

/**
 *  The main program of this class will take a list of parameters from the
 *  command line, including and input and output file name.  The parameters
 *  specify the information needed to read the data from one detector 
 *  integrate and store the integrated intensities in another file.  
 *  Since this runs in a separate process, several of these can run 
 *  simultaneously on a multi-core machine or using slurm.  
 */
public class IntegratePeaksProcess 
{
  public static final String LOG_SUFFIX       = "integrate.log";
  public static final String INTEGRATE_SUFFIX = ".integrate";

  public static final float DEFAULT_MONITOR_COUNT = 10000;

  public static boolean debug = false;

  /**
   *  Read the specified detector data from the specified file, integrate the
   *  peaks in that detector and write the resulting information to an 
   *  "integrate" file.  The parameters are taken from the command line as 
   *  follows:
   *
   *  args[ 0] - fully qualified NeXus file name
   *  args[ 1] - fully qualified base name for peaks file, logfile and 
   *             viewer file.
   *
   *  args[ 2] - data set number to read from the file
   *
   *  args[ 3] - flag indicating whether or not to use a calibration file
   *  args[ 4] - fully qualified calibration file name 
   *  args[ 5] - the line number of the (ipns style) calibration file 
   *
   *  args[ 6] - integer code for centering type:
   *             0 = primitive
   *             1 = a centered 
   *             2 = b centered 
   *             3 = c centered 
   *             4 = face centered 
   *             5 = body centered 
   *             6 = rhombohedral centered 
   *
   *  args[ 7] - time slice offset in minus direction 
   *  args[ 8] - time slice offset in plus direction 
   *  args[ 9] - time slice increment amount 
   *
   *  args[10] - the minimum d spacing to use 
   *
   *  args[11] - step between peaks that are logged
   *
   *  args[12] - name of the integration algorithm.  Must be one of
   *             "MaxItoSigI", "Shoe_Box", "MaxIToSigI-old", "TOFINT"
   *
   *  args[13] - col slice offset in minus direction 
   *  args[14] - col slice offset in plus direction 
   *
   *  args[15] - col slice offset in minus direction 
   *  args[16] - col slice offset in plus direction 
   */
  public static void main( String args[] )
  {
    if ( debug )
    {
      System.out.println("------------------------------------------------");
      System.out.println("Raw command line args to Integrate Peaks Process");
      System.out.println("------------------------------------------------");
      for ( int i = 0; i < args.length; i++ )
        System.out.println( args[i] );
      System.out.println("------------------------------------------------");
    }

    String  fin_name           = args[0];
    String  fout_base          = args[1];
    int     ds_num             = Integer.parseInt( args[2] );

    boolean use_calib_file     = Boolean.parseBoolean( args[3] );
    String  calib_file         = args[4];
    int     calib_file_line    = Integer.parseInt( args[5] );

    String  orientation_file   = args[6];

    int     centering          = Integer.parseInt( args[7] );

    int     minus_time_offset  = Integer.parseInt( args[8] );
    int     plus_time_offset   = Integer.parseInt( args[9] );
    int     incr_time_amount   = Integer.parseInt( args[10] );

    float   d_min              = Float.parseFloat( args[11] );
    int     log_Nth_peak       = Integer.parseInt( args[12] );
    String  peak_algorithm     = args[13];
    if (peak_algorithm.equalsIgnoreCase(Integrate_new.SHOE_BOX.substring(0,4)))
      peak_algorithm = Integrate_new.SHOE_BOX;

    int     minus_col_offset   = Integer.parseInt( args[14] );
    int     plus_col_offset    = Integer.parseInt( args[15] );

    int     minus_row_offset   = Integer.parseInt( args[16] );
    int     plus_row_offset    = Integer.parseInt( args[17] );
    float   max_shoebox        = Float.parseFloat( args[18] );

                               // get rid of any quotes that were placed around
                               // file names to allow names with spaces to be
                               // passed as command line parameters.  
    fin_name   = StringUtil.replace( fin_name, "\"", "" );
    fout_base  = StringUtil.replace( fout_base, "\"", "" );
    calib_file = StringUtil.replace( calib_file, "\"", "" );

    System.out.println( "LOADING " + fin_name + " #" + ds_num );
    System.out.println( "WRITING " + fout_base );

    System.out.println( "use_calib_file     = " + use_calib_file );
    System.out.println( "calib_file         = " + calib_file );
    System.out.println( "calib_file_line    = " + calib_file_line );

    System.out.println( "orientation_file   = " + orientation_file );

    System.out.println( "centering          = " + centering );

    System.out.println( "minus_time_offset  = " + minus_time_offset );
    System.out.println( "plus_time_offset   = " + plus_time_offset );
    System.out.println( "incr_time_amount   = " + incr_time_amount );

    System.out.println( "d_min              = " + d_min );

    System.out.println( "log_Nth_peak       = " + log_Nth_peak );

    System.out.println( "peak_algorithm     = " + peak_algorithm );

    System.out.println( "minus_col_offset  = " + minus_col_offset );
    System.out.println( "plus_col_offset   = " + plus_col_offset );

    System.out.println( "minus_row_offset  = " + minus_row_offset );
    System.out.println( "plus_row_offset   = " + plus_row_offset );
    System.out.println( "max_shoebox   = " + max_shoebox );


    boolean is_IPNS_file = fin_name.toUpperCase().endsWith( "RUN" );
    Retriever retriever = null;
    DataSet   ds     = null;

    try
    {
      if ( is_IPNS_file )
      {
        retriever = new RunfileRetriever( fin_name );
        ds = retriever.getDataSet( ds_num );
                                             // NOTE THIS WILL NOT WORK FOR
                                             // IPNS DATA WITH SEVERAL GRIDS
                                             // IN ONE DataSet
      }
      else
      {
             // NOTE: We may need some way to control if cache info is used.
             //       The statement retriever.RetrieveSetUpInfo(null);
             //       will use the cache if available, so that's OK.  The big
             //       problem is that the cache will be out of date if the
             //       instrument configuration changes !!!
        retriever = new NexusRetriever( fin_name );
        ((NexusRetriever)retriever).RetrieveSetUpInfo(null);
        ds = retriever.getDataSet( ds_num );
      }
    }
    catch ( Throwable ex )
    {
      System.err.println("Could not get DataSet #" + ds_num +
                         " from file " + fin_name );
      System.err.println( ex.getStackTrace() );
      System.exit(5);
    }

    if ( ds == null || ds.getNum_entries() <= 0 )
    {
       System.err.println("NULL DataSet number " + ds_num +
                          " from file " + fin_name );
       System.exit(1);
    }

    float   monct     = DEFAULT_MONITOR_COUNT;
    int     mon_index = 0;
    boolean found     = false;
    int     n_ds      = retriever.numDataSets();
    while ( mon_index < n_ds && !found )
    {
      if ( retriever.getType( mon_index ) == Retriever.MONITOR_DATA_SET )
        found = true;
      else
        mon_index++;
    }

    if ( found )
    {
      DataSet mon_ds = retriever.getDataSet( mon_index );
      if ( mon_ds != null && mon_ds.getNum_entries() > 0 )
      {
        monct = 0;
        float[] ys = mon_ds.getData_entry(0).getY_values();
        if ( ys != null && ys.length > 0 )
          for ( int i = 0; i < ys.length; i++ )
            monct += ys[i];
       }
    }

    if ( retriever instanceof NexusRetriever ) // must close the NeXus file
      ((NexusRetriever)retriever).close();

    System.out.println( "+++++++++FINISHED READING " + fin_name +
                        " closed for DS ###" + ds_num );

    if ( use_calib_file )
      Wizard.TOF_SCD.Util.Calibrate( ds, calib_file, calib_file_line );


    Object load_res = ( new LoadOrientation(ds, orientation_file) ).getResult();
    if( load_res != null && 
        load_res instanceof gov.anl.ipns.Util.  SpecialStrings.ErrorString )
    {
      System.err.println( "ERROR loading orientation matrix for DataSet " 
                          + ds_num + " for " + fin_name );
      System.err.println( load_res );
      System.exit(4);
    }

    int[] det_ids = Grid_util.getAreaGridIDs( ds ); 
    if ( det_ids == null )
    {
       System.err.println("No area detectors in DataSet number " + ds_num + 
                            " from file " + fin_name );
       System.exit(2);
    }
    int det_id = det_ids[0];

    StringBuffer log_buffer = new StringBuffer();
    log_buffer.append( "\n INTEGRATE LOG INFORMATION FOR RUN " + fin_name +
                       " DS " + ds_num + " #########################\n");

    int[] time_range = { minus_time_offset, plus_time_offset };
    int[] col_range  = { minus_col_offset, plus_col_offset };
    int[] row_range  = { minus_row_offset, plus_row_offset };

    Object result = Integrate_new.integrate( ds,
                                  centering,
                                  time_range,
                                  incr_time_amount,
                                  d_min,
                                  log_Nth_peak,
                                  peak_algorithm,
                                  col_range,
                                  row_range,
                                  max_shoebox,
                                  monct,
                                  log_buffer );

    System.out.println( "+++++++++FINISHED INTEGRATING PEAKS " + fin_name +
                        ", for DS number = " + ds_num + 
                        ", Det ID = " + det_id );

    Vector peaks = null;
    if ( result != null && result instanceof Vector )
    {
      peaks = (Vector)result;
      System.out.println("INTEGRATED " + peaks.size() + " PEAKS" );
    }
    else
    {
      System.out.println("INTEGRATION FAILD " + result );
    }

    if ( peaks != null && peaks.size() > 0 )
    {
      String file_name = fout_base + INTEGRATE_SUFFIX;
      try                                            // Write temp peaks file
      {
        Peak_new_IO.WritePeaks_new( file_name, peaks, false );
        System.out.println( "+++++++++FINISHED WRITING INTEGRATE FILE " +
                            file_name +
                            " for DS ###" + 
                            ds_num );
      }
      catch ( Exception ex )
      {
        System.err.println("Exception writing integrate file " + file_name );
        System.err.println( ex.getStackTrace() );
        System.exit(3); 
      }
    }
    else
      System.err.println("NO PEAKS INTEGRATED FOR " + fin_name + 
                         " DS number " + ds_num +
                         " Det ID " + det_id );

    String file_name = fout_base + LOG_SUFFIX;
    try
    {
      FileOutputStream fos = new FileOutputStream( file_name );
      fos.write( log_buffer.toString().getBytes() );
      fos.close();
      System.out.println( "+++++++++FINISHED WRITING INTEGRATE LOG " + 
                          file_name +
                          " for DS ###" +
                          ds_num );
    }
    catch ( Exception ex )
    {
      System.err.println("Exception writing integrate log file "+file_name);
      System.err.println( ex.getStackTrace() );
      System.exit(4);
    }

    System.exit(0); 
  }

}
