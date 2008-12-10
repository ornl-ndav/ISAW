/* 
 * File: FindPeaksProcess.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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
import gov.anl.ipns.Operator.*;
import Wizard.TOF_SCD.*;
import java.util.*;

/**
 *  The main program of this class will take a list of parameters from the
 *  command line, including and input and output file name.  The parameters
 *  specify the information needed to read the data from one detector and
 *  find and store the peaks in another file.  Since this runs in a separate
 *  process, several of these can run simultaneously on a multi-core machine
 *  or using slurm.  
 */
public class FindPeaksProcess 
{
  /**
   *  Read the specified detector data from the specified file, find the
   *  peaks in that detector and write the resulting information to a 
   *  peaks file.  The parameters are taken from the command line as follows:
   *
   *  args[ 0] - fully qualified NeXus file name
   *  args[ 1] - fully qualified base name for peaks file, logfile and 
   *             viewer file.
   *  args[ 2] - data set number to read from the file
   *  args[ 3] - number of peaks to toe be returned, if possible
   *  args[ 4] - minimum intensity to use with the peaks search

   *  args[ 5] - minimum time channel to check
   *  args[ 6] - maximum time cahnnel to check

   *  args[ 7] - flag indicating whether or not to use a calibration file
   *  args[ 8] - fully qualified calibration file name 
   *  args[ 9] - the line number of the (ipns style) calibration file 

   *  args[10] - string specifying the row numbers to use, indexed
   *             starting with index 1.
   *  args[11] - string specifying the column numbers to use, indexed
   *             starting with index 1.
   *  args[12] - the monitor count for this run
   *  args[13] - the maximum D-spacing to consider
   *  args[14] - boolean indicating whether or not to use the new find peaks
   *             method
   *  args[15] - flag indicating whether or not the data should be smoothed as
   *             part of the find peaks process.
   *  args[16] - flag indicating whether or not to eliminate peaks that do
   *             not appear to be valid based on heuristics in the new
   *             find peaks model
   *  args[17] - flag indicating whether the old centrod algorithm should be
   *             applied to set the reflag code, and adjust the peak position
   *  args[18] - flag indicating whether the new peaks view should be 
   *             displayed for the peaks.
   *  args[19] - the number of time-of-flight slices to include in the peaks
   *             view, before and after the central peak position.
   */
  public static void main( String args[] )
  {
    String  fin_name           = args[0];
    String  fout_base          = args[1];
    int     ds_num             = Integer.parseInt( args[2] );
    int     num_peaks          = Integer.parseInt( args[3] );
    int     min_intensity      = Integer.parseInt( args[4] );
    int     min_time_chan      = Integer.parseInt( args[5] );
    int     max_time_chan      = Integer.parseInt( args[6] );

    boolean use_calib_file     = Boolean.parseBoolean( args[7] );
    String  calib_file         = args[8];
    int     calib_file_line    = Integer.parseInt( args[9] );

    String  pixel_row          = args[10];
    String  pixel_col          = args[11];
    int     mon_count          = Integer.parseInt( args[12] );
    float   max_Dspacing       = Float.parseFloat( args[13] );
    boolean use_new_find_peaks = Boolean.parseBoolean( args[14] );
    boolean do_smoothing       = Boolean.parseBoolean( args[15] );
    boolean do_validity_test   = Boolean.parseBoolean( args[16] );
    boolean do_centroid        = Boolean.parseBoolean( args[17] );
    boolean show_peaks_view    = Boolean.parseBoolean( args[18] );
    int     num_slices         = Integer.parseInt( args[19] );

    /*
    System.out.println( "LOADING " + fin_name + " #" + ds_num );
    System.out.println( "WRITING " + fout_base );
    System.out.println( "num_peaks          = " + num_peaks );
    System.out.println( "min_intensity      = " + min_intensity );
    System.out.println( "min_time_chan      = " + min_time_chan );
    System.out.println( "max_time_chan      = " + max_time_chan );

    System.out.println( "use_calib_file     = " + use_calib_file );
    System.out.println( "calib_file         = " + calib_file );
    System.out.println( "calib_file_line    = " + calib_file_line );

    System.out.println( "pixel_row          = " + pixel_row );
    System.out.println( "pixel_col          = " + pixel_col );
    System.out.println( "mon_count          = " + mon_count );
    System.out.println( "max_Dspacing       = " + max_Dspacing );
    System.out.println( "use_new_find_peaks = " + use_new_find_peaks );
    System.out.println( "do_smoothing       = " + do_smoothing  );
    System.out.println( "do_validity_test   = " + do_validity_test );
    System.out.println( "do_centroid        = " + do_centroid );
    System.out.println( "show_peaks_view    = " + show_peaks_view );
    System.out.println( "num_slices         = " + num_slices );

    */

    Retriever retriever = null;

    boolean is_IPNS_file = fin_name.toUpperCase().endsWith( "RUN" );
    if ( is_IPNS_file )
      retriever = new RunfileRetriever( fin_name );
    else
      retriever = new NexusRetriever( fin_name );

// TODO We need a parameter to determine if cache info is used!!!
//    retriever.RetrieveSetUpInfo(null);

    DataSet   ds = retriever.getDataSet( ds_num );
    
    if ( !is_IPNS_file )
      ((NexusRetriever)retriever).close();

    System.out.println( "+++++++++FINISHED READING " + fin_name +
                        " closed for DS ###" + ds_num );

    if ( use_calib_file )
      Wizard.TOF_SCD.Util.Calibrate( ds, calib_file, calib_file_line );

                                             // NOTE THIS WILL NOT WORK FOR
                                             // IPNS DATA WITH SEVERAL GRIDS
                                             // IN ONE DataSet
    int[]        det_ids = Grid_util.getAreaGridIDs( ds ); 
    int          det_id = det_ids[0];

    StringBuffer log_buffer = new StringBuffer();
    
    Vector peaks = Wizard.TOF_SCD.Util.findDetectorCentroidedPeaks ( 
       ds,
       det_id,
       num_peaks,
       min_intensity,
       min_time_chan,
       max_time_chan,
       pixel_row,
       pixel_col,
       mon_count,
       max_Dspacing,
       use_new_find_peaks,
       do_smoothing,
       do_validity_test,
       do_centroid,
       show_peaks_view,
       num_slices,
       log_buffer );

    System.out.println( "+++++++++FINISHED FINDING PEAKS " + fin_name +
                        " for DS ###" + ds_num + " FOUND " + peaks.size()  );

    if ( peaks.size() > 0 )
    {
      String file_name = fout_base + ".peaks";
      try                                            // Write temp peaks file
      {
        Peak_new_IO.WritePeaks_new( file_name, peaks, false );
        System.out.println( "+++++++++FINISHED WRITING PEAKS " + file_name +
                            " for DS ###" + ds_num );
      }
      catch ( Exception ex )
      {
        System.out.println("Exception writing peaks file " + file_name );
        ex.printStackTrace();
      }
      
    }

    return; 
  }

}
