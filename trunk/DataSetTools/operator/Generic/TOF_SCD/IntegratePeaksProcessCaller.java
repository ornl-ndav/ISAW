/* 
 * File: IntegratePeaksProcessCaller.java
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

import java.util.*;
import java.io.*;

import gov.anl.ipns.Operator.*;
import gov.anl.ipns.Util.Sys.SimpleExec;


/**
 *  The getResult() method of this class will execute the IntegratePeaksProcess
 *  main method, to integrate the peaks in a particular detector.  The
 *  integrated peaks are returned in a Vector of Peak_new objects.
 *  
 */
public class IntegratePeaksProcessCaller implements IOperator
{
  String   cmd_name = null;
  String   fin_name;
  String   fout_base;
  int      ds_num;

  boolean  use_calib_file;
  String   calib_file;
  int      calib_file_line;

  String   orientation_file;

  int      centering;

  int      minus_time_offset;
  int      plus_time_offset;
  int      incr_time_amount;

  float    d_min;
  int      log_Nth_peak;
  String   peak_algorithm;

  int      minus_col_offset;
  int      plus_col_offset;

  int      minus_row_offset;
  int      plus_row_offset;

  float    max_shoebox;

  /**
   *  Construct an object to integrate the peaks in the specified detector,
   *  using the specified parameters.
   */
  public IntegratePeaksProcessCaller( String   cmd_name,
                                      String   fin_name, 
                                      String   fout_base,
                                      int      ds_num,
  
                                      boolean  use_calib_file,
                                      String   calib_file,
                                      int      calib_file_line,

                                      String   orientation_file,

                                      int      centering,
                                      int      minus_time_offset,
                                      int      plus_time_offset,
                                      int      incr_time_amount,

                                      float    d_min, 
                                      int      log_Nth_peak,
                                      String   peak_algorithm,

                                      int      minus_col_offset,
                                      int      plus_col_offset,

                                      int      minus_row_offset,
                                      int      plus_row_offset,
                                      float    max_shoebox
                                    )
  {
    this.cmd_name           = cmd_name;
    this.fin_name           = fin_name;
    this.fout_base          = fout_base;
    this.ds_num             = ds_num;

    this.use_calib_file     = use_calib_file;
    this.calib_file         = calib_file;
    this.calib_file_line    = calib_file_line;

    this.orientation_file   = orientation_file;

    this.centering          = centering;
    this.minus_time_offset  = minus_time_offset;
    this.plus_time_offset   = plus_time_offset;;
    this.incr_time_amount   = incr_time_amount;

    this.d_min              = d_min;
    this.log_Nth_peak       = log_Nth_peak;
    this.peak_algorithm     = peak_algorithm;

    this.minus_col_offset   = minus_col_offset;
    this.plus_col_offset    = plus_col_offset;

    this.minus_row_offset   = minus_row_offset;
    this.plus_row_offset    = plus_row_offset;
    this.max_shoebox        = max_shoebox;
  }


  /**
   *  Execute the IntegratePeaksProcess main program as a separate process,
   *  then read the file that is produced and return the peaks that were
   *  integrated in a Vector of Peak_new objects.  The temporary integrate file
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

      if ( peak_algorithm.equalsIgnoreCase( Integrate_new.SHOE_BOX ) )
        peak_algorithm = Integrate_new.SHOE_BOX.substring(0,4);

      String exe_path = "DataSetTools.operator.Generic.TOF_SCD.";

      command = cmd_name               + " " +
                exe_path               +
               "IntegratePeaksProcess" + " " + 
         "\""+  fin_name  +"\""        + " " +        // Need quotes in case
         "\""+  fout_base +"\""        + " " +        // spaces in file names
                ds_num                 + " " +

                use_calib_file         + " " + 
         "\""+  calib_file +"\""       + " " + 
                calib_file_line        + " " + 

                orientation_file       + " " + 

                centering              + " " + 

                minus_time_offset      + " " + 
                plus_time_offset       + " " +
                incr_time_amount       + " " +

                d_min                  + " " +
                log_Nth_peak           + " " + 
                peak_algorithm         + " " + 

                minus_col_offset       + " " + 
                plus_col_offset        + " " + 

                minus_row_offset       + " " + 
                plus_row_offset       + " " + 
                max_shoebox;

      System.out.println("EXECUTING: " + command );
                                                     // Execute the command
                                                     // possibly on remote node
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


    System.out.println( "Integrate file written for run " + fin_name +
                        " ds_num " + ds_num );

                                                     // NOW read in the results
    Vector peaks = new Vector();                     // from the file and
    try                                              // return vector of peaks
    {
      File file = new File(fout_base + ".integrate");
      if ( file.exists() )
      {
        peaks = Peak_new_IO.ReadPeaks_new( fout_base + ".integrate" );
        File delete_file = new File( fout_base + ".integrate" );
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

    System.out.println( "Returning " + peaks.size() + 
                        " peaks for run " + fin_name +
                        " ds_num " + ds_num );
    return peaks;
  }

}
