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
 * Revision 1.13  2004/07/26 22:15:21  dennis
 * Converted to just use single precision, since PeakData_d.java is
 * now the double precision version.
 *
 * Revision 1.12  2004/03/19 17:19:50  dennis
 * Removed unused variable(s)
 *
 * Revision 1.11  2004/03/15 06:10:53  dennis
 * Removed unused import statements.
 *
 * Revision 1.10  2004/03/15 03:28:44  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.9  2003/08/07 20:51:25  dennis
 * Removed un-needed debug prints.
 *
 * Revision 1.8  2003/08/05 21:37:14  dennis
 * Decrement time channel by one when reading from SCD Peaks file,
 * since Peak.java increments the time channel by one when
 * translating to external form.
 *
 * Revision 1.7  2003/08/04 15:49:09  dennis
 * Temporarily made 'l1' and the grid public, for use by the SCDcalib
 * operator, that is in a different package.
 *
 * Revision 1.6  2003/08/01 13:30:51  dennis
 * Now uses files from both new detectors in test code in main.
 *
 * Revision 1.5  2003/07/31 22:46:49  dennis
 * Added method  ReadPeaks( peaks_file_name, run_file_name ) to build
 * a vector of PeakData objects from an "ordinary" SCD peaks file
 * and a run file that was used to make the peaks file.  The run file
 * is used to get needed information on the instrument and detectors
 * that is not included in the "ordinary" peaks file.
 * Changed names of methods that read & write lists of PeakData objects
 * to ReadPeakData() and WritePeakData(), to avoid confusion with methods
 * that deal with "ordinary" peaks files.
 *
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

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.Util.Numeric.*;

import java.io.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.instruments.*;
import DataSetTools.retriever.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.TOF_SCD.*;

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
    public static final float DEFAULT_DEPTH = 0.002f; // default area detector
                                                      // thickness, 2mm
    int   run_num = 0;                 // Run info .....
    float moncnt  = 0;
                                       // Instrument info .....
    public float l1  = 9.378f;
    SampleOrientation orientation;

                                       // Detector info ......
    public UniformGrid grid;
                                       // Peak info .....
    int   seqn   = 0;
    float counts = 0;
   
    float row = 0,                    // Measured position
          col = 0,
          tof = 0;

    float qx  = 0,                    // Q position
          qy  = 0,
          qz  = 0;

    float h   = 0,                    // Miller indices
          k   = 0,
          l   = 0;


  /**
   *  Construct a default PeakData object.
   */
  public PeakData()
  {
    orientation = new IPNS_SCD_SampleOrientation(0,0,0);

    int    det_id = 0; 
    Vector3D center = new Vector3D( 0, -1, 0 );
    Vector3D up_vec   = new Vector3D( 0, 0, 1 );
    Vector3D base_vec = new Vector3D( -1, 0, 0 );

    int    n_rows = 100,
           n_cols = 100;

    float width  = .15f,
          height = .15f,
          depth  = DEFAULT_DEPTH;

    grid = new UniformGrid( det_id, "m", 
                              center, base_vec, up_vec, 
                              width, height, depth,
                              n_rows, n_cols );
  }


  /**
   *  Construct a PeakData object using the specified orientation and grid
   *
   *  @param  orientation   The sample orientation for this peak
   *  @param  grid          The detector grid for this peak
   */
  public PeakData( SampleOrientation orientation, UniformGrid grid )
  {
    this.orientation = orientation;
    this.grid = grid;
  }


  /**
   *  Write a vector of PeakData objects to the specified file.
   *
   *  @param  peaks      The vector of peaks that will be written
   *  @param  file_name  The name of the file that will be written
   *
   *  @return True if the file was written correctly and false otherwise.
   */
  public static boolean WritePeakData( Vector peaks, String file_name ) 
  {
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
          writer.print("     CHI     PHI   OMEGA     MONCNT      L1");
          writer.print(" ROW COL   HEIGHT    WIDTH");
          writer.print(" BASE_VX BASE_VY BASE_VZ");
          writer.print("   UP_VX   UP_VY   UP_VZ");
          writer.println();
          writer.print("1 ");
          writer.print( Format.integer( pd.run_num, 6 ) );
          writer.print( Format.integer( pd.grid.ID(), 4 ) );
          DetectorPosition center = 
                   new DetectorPosition( pd.grid.position());
          float coords[] = center.getSphericalCoords();
          writer.print( Format.real(180*coords[1]/Math.PI, 8, 2 ));
          writer.print( Format.real(90-(180*coords[2]/Math.PI), 8, 2 ));
          writer.print( Format.real(coords[0], 8, 4 ));
          writer.print( Format.real(pd.orientation.getChi(), 8, 2 ));
          writer.print( Format.real(pd.orientation.getPhi(), 8, 2 ));
          writer.print( Format.real(pd.orientation.getOmega(), 8, 2 ));
          writer.print( Format.real(pd.moncnt, 11, 2 ));
          writer.print( Format.real(pd.l1, 8, 4) );
          writer.print( Format.integer( pd.grid.num_rows(), 4 ) );
          writer.print( Format.integer( pd.grid.num_cols(), 4 ) );
          writer.print( Format.real(pd.grid.height(), 9, 5 ));
          writer.print( Format.real(pd.grid.width(), 9, 5 ));
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
  *  Read a vector 
  */


  /**
   *  Read a vector of PeakData objects from a specified file.
   *
   *  @param  file_name   The PeakData file to read (NOTE: This must be
   *                      a file in the form handled by RecipPlaneView
   *                      and SCDcal, NOT the current peaks file used by
   *                      the SCD analysis codes.
   *
   *  @return  A vector filled with the PeakData objects read from the file.
   */
  public static Vector ReadPeakData( String file_name )
  {
    Vector              peaks            = new Vector();

    SampleOrientation last_orientation = null; 
    float chi,
          phi,
          omega;

    UniformGrid  last_grid = null;
    float det_a,
          det_a2, 
          det_d,
          height,
          width,
          x, y, z;

    float last_moncnt =  1.0f,
          last_l1     =  9.378f;

    int    det_id,
           n_rows,
           n_cols,
           n_peaks;

    int    last_run    = -1;
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
            det_a   = tfr.read_float();
            det_a2  = tfr.read_float();
            det_d   = tfr.read_float();
            DetectorPosition position = new DetectorPosition();
            position.setSphericalCoords( det_d, 
                                         (float)(Math.PI * det_a / 180), 
                                         (float)(Math.PI * (90-det_a2) / 180 ));
            Vector3D center = new Vector3D( position );

            chi     = tfr.read_float();
            phi     = tfr.read_float();
            omega   = tfr.read_float();
            last_orientation = new IPNS_SCD_SampleOrientation(phi,chi,omega);
            last_moncnt  = tfr.read_float();
            last_l1      = tfr.read_float();

            n_rows  = tfr.read_int();
            n_cols  = tfr.read_int();

            height  = tfr.read_float(); 
            width   = tfr.read_float();

            x = tfr.read_float();
            y = tfr.read_float();
            z = tfr.read_float();
            Vector3D base_vec = new Vector3D( x, y, z );
            x = tfr.read_float();
            y = tfr.read_float();
            z = tfr.read_float();
            Vector3D up_vec = new Vector3D( x, y, z );

            last_grid = new UniformGrid( det_id, "m",
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

          peak.seqn     = tfr.read_int();       // Peak Data
          peak.h        = tfr.read_float();
          peak.k        = tfr.read_float();
          peak.l        = tfr.read_float();
          peak.col      = tfr.read_float();
          peak.row      = tfr.read_float();
          peak.tof      = tfr.read_float();
          peak.counts   = tfr.read_float();
          peak.qx       = tfr.read_float();
          peak.qy       = tfr.read_float();
          peak.qz       = tfr.read_float();
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
   *  Read a vector of PeakData objects from an SCD peaks file AND
   *  one of the IPNS Runfiles used to form the peaks file.  The
   *  Runfile is needed to provide information about the time-of-flight
   *  and detector size. 
   *
   *  @param  peaks_file_name   The SCD peaks file to read (NOTE: This must be
   *                            a file in the form handled by the SCD analysis
   *                            software.
   *
   *  @param  run_file_name     The name of one of the Runfiles used to form
   *                            the peaks file; this provides the time scale
   *                            and detector size information.
   *
   *  @return  A vector filled with the PeakData objects read from the file.
   */
  public static Vector ReadPeaks( String peaks_file_name, String run_file_name )
  {
    Operator op = new ReadPeaks( peaks_file_name );

    Object obj = op.getResult();
    if ( !(obj instanceof Vector) )
    {
      System.out.println("ERROR: Couldn't read peaks file");
      System.out.println( obj.toString() );
      return null;
    }

    RunfileRetriever rr = new RunfileRetriever( run_file_name );
    DataSet ds = (DataSet)rr.getFirstDataSet( Retriever.HISTOGRAM_DATA_SET );
    if ( ds == null )
    {
      System.out.println("ERROR: Couldn't read Runfile " + run_file_name );
      return null;
    }

    Vector all_peaks = (Vector)obj;
    System.out.println("TOTAL NUMBER OF PEAKS = " + all_peaks.size() );
    
    Vector peaks = new Vector();                    // only keep those that
    for ( int i = 0; i < all_peaks.size(); i++ )    // are indexed
    {
      Peak p  = (Peak)all_peaks.elementAt(i);
      if ( p.h() != 0 || p.k() != 0 || p.l() != 0 )
        peaks.addElement( p );
    }
    System.out.println("NUMBER OF INDEXED PEAKS = " + peaks.size() );
    
    int grid_ids[]  = Grid_util.getAreaGridIDs( ds );
    if ( grid_ids.length <= 0 )
    {
      System.out.println("ERROR: no area detectors in " + run_file_name );
      return null;
    }

    UniformGrid grid; 
    UniformGrid sgrid;
    Hashtable grids = new Hashtable();
    for ( int i = 0; i < grid_ids.length; i++ )
    {
      sgrid = (UniformGrid)Grid_util.getAreaGrid( ds, grid_ids[i] );
      grid = new UniformGrid( sgrid, false );
      grids.put( new Integer(grid_ids[i]), grid );
    }

    sgrid = (UniformGrid)Grid_util.getAreaGrid( ds, grid_ids[0] );
    Data d = sgrid.getData_entry( 1, 1 );
    XScale xscale = d.getX_scale();

    Attribute attr = d.getAttribute( Attribute.INITIAL_PATH );
    if ( attr == null )
    {
      System.out.println("ERROR: no initial path in " + peaks_file_name );
      return null;
    }
      
    Vector pd_peaks = new Vector( peaks.size() );
    for ( int i = 0; i < peaks.size(); i++ )
    {
      PeakData pd = new PeakData();
      Peak       p  = (Peak)peaks.elementAt(i);
   
      pd.run_num = p.nrun();
      pd.moncnt  = p.monct();
      pd.l1      = (float)attr.getNumericValue();
      pd.seqn    = p.seqnum();
      pd.counts  = p.ipkobs();
      pd.row     = p.y();
      pd.col     = p.x();

      int    bin = (int)p.z() - 1;            // file stores time channel + 1
      if ( bin >= xscale.getNum_x() - 1 )
        pd.tof = xscale.getX( xscale.getNum_x() - 1 );
      else if ( bin >= 0 )
      {
        float x1 = xscale.getX( bin     );
        float x2 = xscale.getX( bin + 1 );
        pd.tof    = x1 + ( p.z() - bin ) * ( x2 - x1 ); 
      }
      else
      {
        pd.tof = 0;
        System.out.println("ERROR: invalid time channel 'z' in " + 
                            peaks_file_name );
      }

      pd.orientation = new IPNS_SCD_SampleOrientation( p.phi(), 
                                                       p.chi(), 
                                                       p.omega() );
      pd.grid = (UniformGrid)grids.get( new Integer(p.detnum()) );

      pd.qx  = 0;            // Q position, not set for now, since not needed
      pd.qy  = 0;
      pd.qz  = 0;

      pd.h   = p.h();
      pd.k   = p.k();
      pd.l   = p.l();
      pd_peaks.add( pd );
    }

    return pd_peaks;
  }


 /**
  *  Main program for test purposes
  */
  public static void main( String args[] )
  {
   
    String peakdata_name = "fft_peaks.dat";
    System.out.println("Test loading " + peakdata_name );
    Vector peaks = ReadPeakData( peakdata_name );
    System.out.println("Read peaks, # = " + peaks.size() );

    String new_peakdata_name = "junk_peaks.dat";
    System.out.println("Test writing to " + new_peakdata_name );
    WritePeakData( peaks, new_peakdata_name );

    String peaks_name = "/usr/local/ARGONNE_DATA/SCD_QUARTZ_2_DET/quartz.peaks";
    String run_name   = "/usr/local/ARGONNE_DATA/SCD_QUARTZ_2_DET/scd08336.run";
    System.out.println("Test loading " + peaks_name + " and " + run_name );
    peaks = ReadPeaks( peaks_name, run_name );
 
    String new_peakdata2_name = "junk_peaks2.dat";
    System.out.println("Test writing to " + new_peakdata2_name );
    WritePeakData( peaks, new_peakdata2_name );
  }

}