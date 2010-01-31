/* 
 * File: FindPeaksProcessCaller.java
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

import java.util.*;
import java.io.*;

import gov.anl.ipns.Operator.*;
import gov.anl.ipns.Util.Sys.SimpleExec;

/**
 *  The getResult() method of this class will execute the FindPeaksProcess
 *  main method, to find the peaks in a particular detector.  The peaks
 *  are returned in a Vector of Peak_new objects.
 *  
 */
public class FindPeaksProcessCaller implements IOperator
{
  String   cmd_name = null;
  String   fin_name;
  String   fout_base;
  int      ds_num;
  int      num_peaks;
  int      min_intensity;
  int      min_time_chan;
  int      max_time_chan;

  boolean  use_calib_file;
  String   calib_file;
  int      calib_file_line;

  String   pixel_row;
  String   pixel_col;
  int      mon_count;
  float    max_Dspacing;
  boolean  use_new_find_peaks;
  boolean  do_smoothing;
  boolean  do_validity_test;
  boolean  do_centroid;
  boolean  show_peaks_view;
  int      num_slices;


  /**
   *  Construct an object to find the peaks in the specified detector, using
   *  the specified parameters.
   */
  public FindPeaksProcessCaller( String   cmd_name,
                                 String   fin_name, 
                                 String   fout_base,
                                 int      ds_num,
                                 int      num_peaks,
                                 int      min_intensity,
                                 int      min_time_chan,
                                 int      max_time_chan,

                                 boolean  use_calib_file,
                                 String   calib_file,
                                 int      calib_file_line,

                                 String   pixel_row,
                                 String   pixel_col,
                                 int      mon_count,
                                 float    max_Dspacing,
                                 boolean  use_new_find_peaks,
                                 boolean  do_smoothing,
                                 boolean  do_validity_test,
                                 boolean  do_centroid,
                                 boolean  show_peaks_view,
                                 int      num_slices
                               )
  {
    this.cmd_name           = cmd_name;
    this.fin_name           = fin_name;
    this.fout_base          = fout_base;
    this.ds_num             = ds_num;
    this.num_peaks          = num_peaks;
    this.min_intensity      = min_intensity;
    this.min_time_chan      = min_time_chan;
    this.max_time_chan      = max_time_chan;

    this.use_calib_file     = use_calib_file;
    this.calib_file         = calib_file;
    this.calib_file_line    = calib_file_line;

    this.pixel_row          = pixel_row;
    this.pixel_col          = pixel_col;
    this.mon_count          = mon_count;
    this.max_Dspacing       = max_Dspacing;
    this.use_new_find_peaks = use_new_find_peaks;
    this.do_smoothing       = do_smoothing;
    this.do_validity_test   = do_validity_test;
    this.do_centroid        = do_centroid;
    this.show_peaks_view    = show_peaks_view;
    this.num_slices         = num_slices;
  }


  /**
   *  Execute the FindPeaksProcess main program as a separate process,
   *  then read the file that is produced and return the peaks that were
   *  found in a Vector of Peak_new objects.  The temporary peaks file
   *  is deleted after it is read.
   *
   *  @return a Vector of Peak_new objects.
   */
  public Object getResult()
  {
    String command = null;
    try
    {                                                // Build the command 
                                                     // NOTE: The same command
                                                     // must work on all 
                                                     // systems.
      String exe_path = "DataSetTools.operator.Generic.TOF_SCD.";

      command = cmd_name           + " " +
                exe_path           +
               "FindPeaksProcess"  + " " + 
          "\""+ fin_name  +"\""    + " " +        // Need quotes in case there
          "\""+ fout_base +"\""    + " " +        // are spaces in file names
                ds_num             + " " +
                num_peaks          + " " +
                min_intensity      + " " +
                min_time_chan      + " " +
                max_time_chan      + " " + 
                                                   // NOTE: Append to file
                                                   //       is NOT passed down
                use_calib_file     + " " + 
          "\""+ calib_file +"\""   + " " + 
                calib_file_line    + " " + 

                pixel_row          + " " + 
                pixel_col          + " " + 
                mon_count          + " " +
                max_Dspacing       + " " +
                use_new_find_peaks + " " +
                do_smoothing       + " " + 
                do_validity_test   + " " + 
                do_centroid        + " " + 
                show_peaks_view    + " " + 
                num_slices         + " " ; 

      System.out.println("EXECUTING: " + command );
 
      SimpleExec.Exec( command );
    }

    catch( Exception ex )
    {
      System.out.println( "EXCEPTION executing command " + command +
                          " for " + fin_name +
                          " ds_num " + ds_num +
                          " on server " + cmd_name );
      ex.printStackTrace();
    }

    System.out.println( "Peaks file written for run " + fin_name +
                        " ds_num " + ds_num );

                                                     // NOW read in the results
    Vector peaks = new Vector();                     // from the file and
    try                                              // return vector of peaks
    {
      File file = new File(fout_base + ".peaks");
      if ( file.exists() )
      {
        peaks = Peak_new_IO.ReadPeaks_new( fout_base + ".peaks" );
        File delete_file = new File( fout_base + ".peaks" );
        delete_file.delete();
      }
    }
    catch ( Exception ex )
    {
      System.out.println( "EXCEPTION reading temporary peaks file "+fout_base+
                          " for " + fin_name +
                          " ds_num " + ds_num +
                          " on server " + cmd_name );
      ex.printStackTrace();
    }

    System.out.println( "Returning peaks for run " + fin_name +
                        " ds_num " + ds_num );
    return peaks;
  }
  
 

}
