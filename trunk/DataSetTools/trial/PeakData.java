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
 * Revision 1.4  2003/07/31 14:18:18  dennis
 * Fixed reading of detector angle 2... it's now properly
 * interpreted as the angle above (or below) the "scattering plane"
 * where the detector center is located, as in Art's peak file
 * format.
 *
 * Revision 1.3  2003/07/30 22:03:35  dennis
 * Now uses a SampleOrientation object to store phi, chi and omega,
 * and uses a UniformGrid object to store the detector information.
 *
 * Revision 1.1  2003/07/30 16:30:10  dennis
 * Initial form of convenience class that groups information about a peak.
 *
 */

package DataSetTools.trial;

import java.io.*;
import java.util.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
import DataSetTools.dataset.*;
import DataSetTools.instruments.*;

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
   */
public class PeakData
{
    public static final double DEFAULT_DEPTH = 0.002; // default area detector
                                                      // thickness, 2mm
    int    run_num = 0;                // Run info .....
    double moncnt = 0;
                                       // Instrument info .....
    double l1     = 9.378;
    SampleOrientation_d orientation;

                                       // Detector info ......
    UniformGrid_d grid;
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


  /**
   *  Construct a default PeakData object.
   */
  public PeakData()
  {
    orientation = new IPNS_SCD_SampleOrientation_d(0,0,0);

    int    det_id = 0; 
    Vector3D_d center = new Vector3D_d( 0, -1, 0 );
    Vector3D_d up_vec   = new Vector3D_d( 0, 0, 1 );
    Vector3D_d base_vec = new Vector3D_d( -1, 0, 0 );

    int    n_rows = 100,
           n_cols = 100;

    double width  = .15,
           height = .15,
           depth  = DEFAULT_DEPTH;

    grid = new UniformGrid_d( det_id, "m", 
                              center, base_vec, up_vec, 
                              width, height, depth,
                              n_rows, n_cols );
  }

  /**
   *  Write a vector of PeakData objects to the specified file.
   *
   *  @param  peaks      The vector of peaks that will be written
   *  @param  file_name  The name of the file that will be written
   *
   *  @return True if the file was written correctly and false otherwise.
   */
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
        if ( pd.run_num != last_run || pd.grid.ID() != last_id )
        {
          writer.print("0   NRUN DETNUM DETA   DETA2    DETD");
          writer.print("     CHI     PHI   OMEGA  MONCNT      L1");
          writer.print(" ROW COL  HEIGHT   WIDTH");
          writer.print(" BASE_VX BASE_VY BASE_VZ");
          writer.print("   UP_VX   UP_VY   UP_VZ");
          writer.println();
          writer.print("1 ");
          writer.print( Format.integer( pd.run_num, 6 ) );
          writer.print( Format.integer( pd.grid.ID(), 4 ) );
          DetectorPosition_d center = 
                   new DetectorPosition_d( pd.grid.position());
          double coords[] = center.getSphericalCoords();
          writer.print( Format.real(180*coords[1]/Math.PI, 8, 2 ));
          writer.print( Format.real(90-(180*coords[2]/Math.PI), 8, 2 ));
          writer.print( Format.real(coords[0], 8, 4 ));
          writer.print( Format.real(pd.orientation.getChi(), 8, 2 ));
          writer.print( Format.real(pd.orientation.getPhi(), 8, 2 ));
          writer.print( Format.real(pd.orientation.getOmega(), 8, 2 ));
          writer.print( Format.real(pd.moncnt, 8, 2 ));
          writer.print( Format.real(pd.l1, 8, 4) );
          writer.print( Format.integer( pd.grid.num_rows(), 4 ) );
          writer.print( Format.integer( pd.grid.num_cols(), 4 ) );
          writer.print( Format.real(pd.grid.height(), 8, 2 ));
          writer.print( Format.real(pd.grid.width(), 8, 2 ));
          coords = pd.grid.x_vec().get();
          writer.print( Format.real(coords[0], 8, 4 ));
          writer.print( Format.real(coords[1], 8, 4 ));
          writer.print( Format.real(coords[2], 8, 4 ));
          coords = pd.grid.y_vec().get();
          writer.print( Format.real(coords[0], 8, 4 ));
          writer.print( Format.real(coords[1], 8, 4 ));
          writer.print( Format.real(coords[2], 8, 4 ));
          writer.println();
          writer.print("2   SEQN      H      K      L    COL    ROW");
          writer.println("       TOF     IPK     QX     QY     QZ");
          last_run = pd.run_num;
          last_id  = pd.grid.ID();
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


  /**
   *  Read a vector of peaks objects from a specified file.
   *
   *  @param  file_name   The peaks file to read (NOTE: This must be
   *                      a peaks file in the form handled by RecipPlaneView
   *                      and SCDcal, NOT the current peaks file used by
   *                      the SCD analysis codes.
   *
   *  @return  A vector filled with the PeakData objects read from the file.
   */
  public static Vector ReadPeaks( String file_name )
  {
    Vector              peaks            = new Vector();

    SampleOrientation_d last_orientation = null; 
    double chi,
           phi,
           omega;

    UniformGrid_d  last_grid = null;
    double det_a,
           det_a2, 
           det_d,
           height,
           width,
           x, y, z;

    double last_moncnt   =  1.0,
           last_l1       =  9.378;

    int    det_id,
           n_rows,
           n_cols,
           n_peaks;

    int    last_run    = -1,
           last_det    = -1;
      try
      {
        TextFileReader tfr = new TextFileReader( file_name );
        n_peaks = tfr.read_int();

        int line_type;
        for ( int i = 0; i < n_peaks; i++ )
        {
          line_type = tfr.read_int();
          if ( line_type == 0 )      // READ HEADER INFO
          {
            tfr.read_line();         // end of line 0

            line_type    = tfr.read_int();
            last_run     = tfr.read_int();

            det_id  = tfr.read_int();
            det_a   = tfr.read_double();
            det_a2  = tfr.read_double();
            det_d   = tfr.read_double();
            DetectorPosition_d position = new DetectorPosition_d();
            position.setSphericalCoords( det_d, 
                                         Math.PI * det_a / 180, 
                                         Math.PI * (90-det_a2) / 180 );
            Vector3D_d center = new Vector3D_d( position );

            chi     = tfr.read_double();
            phi     = tfr.read_double();
            omega   = tfr.read_double();
            last_orientation = new IPNS_SCD_SampleOrientation_d(phi,chi,omega);
            last_moncnt  = tfr.read_double();
            last_l1      = tfr.read_double();

            n_rows  = tfr.read_int();
            n_cols  = tfr.read_int();

            height  = tfr.read_double(); 
            width   = tfr.read_double();

            x = tfr.read_double();
            y = tfr.read_double();
            z = tfr.read_double();
            Vector3D_d base_vec = new Vector3D_d( x, y, z );
            x = tfr.read_double();
            y = tfr.read_double();
            z = tfr.read_double();
            Vector3D_d up_vec = new Vector3D_d( x, y, z );

            last_grid = new UniformGrid_d( det_id, "m",
                                           center, 
                                           base_vec, up_vec,
                                           width, height, DEFAULT_DEPTH,
                                           n_rows, n_cols );

            tfr.read_line();          // end of line type 1

            line_type = tfr.read_int();
            tfr.read_line();          // end of line type 2

            line_type = tfr.read_int();
          }

          PeakData peak = new PeakData();

          peak.run_num  = last_run;               // Run Info
          peak.moncnt   = last_moncnt;

                                                  // Instrument Info
          peak.orientation = last_orientation;               
          peak.l1       = last_l1;

          peak.grid  = last_grid;               // Det position & orientation

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
