/*
 * File:  PeakData.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/07/30 16:30:10  dennis
 * Initial form of convenience class that groups information about a peak.
 *
 *
 */

package DataSetTools.trial;

import java.io.*;
import java.util.*;
import DataSetTools.math.*;
import DataSetTools.util.*;

  /*
   *  This is a convenience class for recording complete peak info with methods
   *  to read and write lists of PeakData.  It differs from the class
   *  DataSetTools.operator.Generic.TOF_SCD.Peak in that it includes the
   *  detector size and orientation information and the recorded 
   *  time-of-flight.  Also, it is currently "passive" and just records the
   *  data and takes care of I/O... it does not do conversions between 
   *  measured position, Q, and Miller indices.  This version is "experimental"
   *  and is intended for use by the SCD calibration, and RecipPlaneView 
   *  classes.
   *
   *  NOTE: The current "flat" structure should be changed to use
   *         SampleOrientation to record chi, phi, omega, and UniformGrid
   *         to record the detector information.
   */
public class PeakData
{
    int    run_num = 0;                // Run info .....
    double moncnt = 0;

    double chi   = 0,                  // Instrument info .....
           phi   = 0,
           omega = 0;
    double l1    = 9.378;

    int    det_id = 0;                 // Detector info ......

    double det_a  = 90,                // Detector position
           det_a2 = 0,
           det_d  = .23;
                                       // Detector orientation
    Vector3D_d up_vec   = new Vector3D_d( 0, 1, 0 );
    Vector3D_d base_vec = new Vector3D_d( 1, 0, 0 );

    int    n_rows = 100,               // Detector size
           n_cols = 100;
    double width  = .15,
           height = .15;

                                       // Peak info .....
    int    seqn   = 0;
    double counts = 0;
   
    double row = 0,                    // Measured position
           col = 0,
           tof = 0;

    double qx  = 0,                    // Q position
           qy  = 0,
           qz  = 0;

    double h   = 0,                    // Miller indices
           k   = 0,
           l   = 0;


  public static boolean WritePeaks( Vector peaks, String file_name ) 
  {
    System.out.println("Starting to write peaks " + peaks.size() );
    try
    {
      FileWriter out_file = new FileWriter( file_name );
      PrintWriter writer  = new PrintWriter( out_file );
      writer.println( "" + peaks.size() );

      int last_run = -1;
      int last_id  = -1;
      for ( int i = 0; i < peaks.size(); i++ )
      {
        PeakData pd = (PeakData)peaks.elementAt(i);
        if ( pd.run_num != last_run || pd.det_id != last_id )
        {
          writer.print("0   NRUN DETNUM DETA   DETA2    DETD");
          writer.print("     CHI     PHI   OMEGA  MONCNT      L1");
          writer.print(" ROW COL  HEIGHT   WIDTH");
          writer.print(" BASE_VX BASE_VY BASE_VZ");
          writer.print("   UP_VX   UP_VY   UP_VZ");
          writer.println();
          writer.print("1 ");
          writer.print( Format.integer( pd.run_num, 6 ) );
          writer.print( Format.integer( pd.det_id, 4 ) );
          writer.print( Format.real(pd.det_a, 8, 2 ));
          writer.print( Format.real(pd.det_a2, 8, 2 ));
          writer.print( Format.real(pd.det_d, 8, 4 ));
          writer.print( Format.real(pd.chi, 8, 2 ));
          writer.print( Format.real(pd.phi, 8, 2 ));
          writer.print( Format.real(pd.omega, 8, 2 ));
          writer.print( Format.real(pd.moncnt, 8, 2 ));
          writer.print( Format.real(pd.l1, 8, 4) );
          writer.print( Format.integer( pd.n_rows, 4 ) );
          writer.print( Format.integer( pd.n_cols, 4 ) );
          writer.print( Format.real(pd.height, 8, 2 ));
          writer.print( Format.real(pd.width, 8, 2 ));
          writer.print( Format.real(pd.base_vec.get()[0], 8, 4 ));
          writer.print( Format.real(pd.base_vec.get()[1], 8, 4 ));
          writer.print( Format.real(pd.base_vec.get()[2], 8, 4 ));
          writer.print( Format.real(pd.up_vec.get()[0], 8, 4 ));
          writer.print( Format.real(pd.up_vec.get()[1], 8, 4 ));
          writer.print( Format.real(pd.up_vec.get()[2], 8, 4 ));
          writer.println();
          writer.print("2   SEQN      H      K      L    COL    ROW");
          writer.println("       TOF     IPK     QX     QY     QZ");
          last_run = pd.run_num;
          last_id  = pd.det_id;
        }
        writer.print("3 ");                               // line type 3, data
        writer.print( Format.integer( i, 6 ) );
        writer.print( Format.real(pd.h, 7, 2 ));
        writer.print( Format.real(pd.k, 7, 2 ));
        writer.print( Format.real(pd.l, 7, 2 ));
        writer.print( Format.real( pd.col, 7, 2 ) );
        writer.print( Format.real( pd.row, 7, 2 ) );
        writer.print( Format.real( pd.tof, 10, 2 ) );
        writer.print( Format.real( pd.counts, 8, 1 ) );
        writer.print( Format.real( pd.qx, 7, 2 ) );
        writer.print( Format.real( pd.qy, 7, 2 ) );
        writer.print( Format.real( pd.qz, 7, 2 ) );
        writer.println();
      }
      out_file.close();
    }
    catch ( Exception exception )
    {
      System.out.println("Exception in WriteFileListener");
      System.out.println(exception);
      exception.printStackTrace();
      return false;
    }
    return true;
  }


  public static Vector ReadPeaks( String file_name )
  {
    Vector peaks = new Vector();
    int line_type;
    double last_det_a    = 90.0,
           last_det_a2   =  0.0, 
           last_det_d    =  1.0,
           last_chi      =  0.0,
           last_phi      =  0.0,
           last_omega    =  0.0,
           last_moncnt   =  1.0,
           last_l1       =  9.378,
           last_height   =  0.15,
           last_width    =  0.15,
           last_base_vx  = -1.0, 
           last_base_vy  =  0.0, 
           last_base_vz  =  0.0, 
           last_up_vx    =  0.0,
           last_up_vy    =  0.0,
           last_up_vz    =  1.0;

    int    n_peaks,
           last_run    = -1,
           last_det    = -1,
           last_n_rows = 100,
           last_n_cols = 100;
      try
      {
        TextFileReader tfr = new TextFileReader( file_name );
        n_peaks = tfr.read_int();

        for ( int i = 0; i < n_peaks; i++ )
        {
          line_type = tfr.read_int();
          if ( line_type == 0 )      // READ HEADER INFO
          {
            tfr.read_line();         // end of line 0

            line_type = tfr.read_int();
            last_run     = tfr.read_int();
            last_det     = tfr.read_int();
            last_det_a   = tfr.read_double();
            last_det_a2  = tfr.read_double();
            last_det_d   = tfr.read_double();
            last_chi     = tfr.read_double();
            last_phi     = tfr.read_double();
            last_omega   = tfr.read_double();
            last_moncnt  = tfr.read_double();
            last_l1      = tfr.read_double();
            last_n_rows  = tfr.read_int();
            last_n_cols  = tfr.read_int();
            last_height  = tfr.read_double(); 
            last_width   = tfr.read_double();
            last_base_vx = tfr.read_double();
            last_base_vy = tfr.read_double();
            last_base_vz = tfr.read_double();
            last_up_vx   = tfr.read_double();
            last_up_vy   = tfr.read_double();
            last_up_vz   = tfr.read_double();
            tfr.read_line();          // end of line type 1

            line_type = tfr.read_int();
            tfr.read_line();          // end of line type 2

            line_type = tfr.read_int();
          }

          PeakData peak = new PeakData();

          peak.run_num  = last_run;               // Run Info
          peak.moncnt   = last_moncnt;

          peak.chi      = last_chi;               // Instrument Info
          peak.phi      = last_phi;
          peak.omega    = last_omega;
          peak.l1       = last_l1;

          peak.det_id   = last_det;               // Det position & orientation
          peak.det_a    = last_det_a;
          peak.det_a2   = last_det_a2;
          peak.det_d    = last_det_d;
          peak.up_vec   = new Vector3D_d( last_up_vx, last_up_vy, last_up_vz );
          peak.base_vec = new Vector3D_d( last_base_vx, 
                                          last_base_vy, 
                                          last_base_vz );

          peak.n_rows   = last_n_rows;            // Detector size
          peak.n_cols   = last_n_cols;
          peak.width    = last_width;
          peak.height   = last_height;

          peak.seqn     = tfr.read_int();         // Peak Data
          peak.h        = tfr.read_double();
          peak.k        = tfr.read_double();
          peak.l        = tfr.read_double();
          peak.col      = tfr.read_double();
          peak.row      = tfr.read_double();
          peak.tof      = tfr.read_double();
          peak.counts   = tfr.read_double();
          peak.qx       = tfr.read_double();
          peak.qy       = tfr.read_double();
          peak.qz       = tfr.read_double();
          tfr.read_line();                         // end of line type 3
          peaks.addElement( peak );
        }
      }
      catch ( IOException e )
      {
        System.out.println( "IO exception in PeakData.ReadPeaks() " + e);
        e.printStackTrace();
        return null;
      }

     return peaks;
  }

 /**
  *  Main program for test purposes
  */
  public static void main( String args[] )
  {
    Vector peaks = ReadPeaks( "fft_peaks.dat" );
    System.out.println("Read peaks, # = " + peaks.size() );
    WritePeaks( peaks, "junk_peaks.dat" );
  }

}
