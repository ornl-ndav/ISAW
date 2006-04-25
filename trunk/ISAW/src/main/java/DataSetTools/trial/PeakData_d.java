/*
 * File:  PeakData_d.java
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
 * Revision 1.3  2006/01/16 03:10:11  dennis
 * Added method to centroid a file of PeakData_d objects.
 * Added instrument type parameter to ReadPeaks() method, to use
 * the same method for both IPNS and LANSCE.
 * Added copy constructor.
 *
 * Revision 1.2  2006/01/13 18:29:30  dennis
 * Modified ReadPeaks() method to take a peaks file name and a DataSet
 * rather than a peaks file name and run file name.
 *
 * Revision 1.1  2004/07/26 21:45:32  dennis
 * Renamed from PeakData.java to emphasize that this version uses double
 * precision values for all information.
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
public class PeakData_d
{
    public static final String IPNS_SCD   = "IPNS_SCD";
    public static final String LANSCE_SCD = "LANSCE_SCD";

    public static final double DEFAULT_DEPTH = 0.002; // default area detector
                                                      // thickness, 2mm
    int    run_num = 0;                // Run info .....
    double moncnt  = 0;
                                       // Instrument info .....
    public double l1 = 9.378;
    SampleOrientation_d orientation;

                                       // Detector info ......
    public UniformGrid_d grid;
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
  public PeakData_d()
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
   *  Construct a PeakData object using the specified orientation and grid
   *
   *  @param  orientation   The sample orientation for this peak
   *  @param  grid          The detector grid for this peak
   */
  public PeakData_d( SampleOrientation_d orientation, UniformGrid_d grid )
  {
    this.orientation = orientation;
    this.grid = grid;
  }


  /**
   *  Construct a PeakData_d object that is a shallow copy of the given
   *  PeakData_d objec.
   *
   *  @param  peak  The peak data object that will be copied.
   */
  public PeakData_d( PeakData_d peak )
  {
    this.run_num = peak.run_num;       // Run info .....
    this.moncnt  = peak.moncnt;
                                       // Instrument info .....
    this.l1          = peak.l1;
    this.orientation = peak.orientation;

                                       // Detector info ......
    this.grid = peak.grid;
                                       // Peak info .....
    this.seqn   = peak.seqn;
    this.counts = peak.counts;

    this.row = peak.row;               // Measured position
    this.col = peak.col;
    this.tof = peak.tof;

    this.qx = peak.qx;                 // Q position
    this.qy = peak.qy; 
    this.qz = peak.qz;

    this.h = peak.h;                   // Miller indices
    this.k = peak.k; 
    this.l = peak.l;
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
        PeakData_d pd = (PeakData_d)peaks.elementAt(i);
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
          DetectorPosition_d center = 
                   new DetectorPosition_d( pd.grid.position());
          double coords[] = center.getSphericalCoords();
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
   *  Read a vector of PeakData objects from a specified file.
   *
   *  @param  file_name   The PeakData file to read (NOTE: This must be
   *                      a file in the form handled by RecipPlaneView
   *                      and SCDcal, NOT the current peaks file used by
   *                      the SCD analysis codes.
   *
   *  @param  instrument  The particular SCD for which the peaks are being
   *                      read.  This is needed to determine how the sample
   *                      orientation angles chi, phi and omega are interpreted.
   *                      The default is LANSCE SCD, since the shifted 
   *                      goniometer angles follow a strict right hand rule.
   *                      This should be PeakData_d.IPNS_SCD or 
   *                      PeakData_d.LANSCE_SCD.
   *
   *  @return  A vector filled with the PeakData objects read from the file.
   */
  public static Vector ReadPeakData( String file_name, String instrument )
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
            if ( instrument.equalsIgnoreCase( IPNS_SCD ) )
              last_orientation = 
                   new IPNS_SCD_SampleOrientation_d(phi,chi,omega);
            else
              last_orientation = 
                   new LANSCE_SCD_SampleOrientation_d(phi,chi,omega);

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

          PeakData_d peak = new PeakData_d();

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
        System.out.println( "IO exception in PeakData_d.ReadPeaks() " + e);
        e.printStackTrace();
        return null;
      }

     return peaks;
  }


  /**
   *  Read a list of PeakData objects from a peaks file produced by the 
   *  GL_RecipPlaneViewer, find the centroid of the peaks for each particular
   *  hkl from a run, and then write the centroided peaks to the output file.
   *
   *  @param  in_file     The PeakData file to read containg all of the 
   *                      individual bins for each peak, as separate peak
   *                      entries. (NOTE: This must be
   *                      a file in the form handled by RecipPlaneView
   *                      and SCDcal, NOT the current peaks file used by
   *                      the SCD analysis codes.
   *
   *  @param  out_file    The file to which the resulting condensed 
   *                      PeakData file to read containg the centroided
   *                      peaks are written.
   *
   *  @param  instrument  The particular SCD for which the peaks are being
   *                      read.  This is needed to determine how the sample
   *                      orientation angles chi, phi and omega are interpreted.
   *                      The default is LANSCE SCD, since the shifted 
   *                      goniometer angles follow a strict right hand rule.
   *                      This should be PeakData_d.IPNS_SCD or 
   *                      PeakData_d.LANSCE_SCD.
   */
  public static void CentroidPeaksFile( String in_file,
                                        String out_file,
                                        String instrument )
  {
    Vector in_peaks = ReadPeakData( in_file, instrument );
    Vector out_peaks = new Vector();

 
    while ( in_peaks.size() > 0 )
    {
      PeakData_d cent_peak = 
                      new PeakData_d( (PeakData_d)(in_peaks.elementAt(0) ));

      cent_peak.counts = 0;
      double qx_tot  = 0;
      double qy_tot  = 0;
      double qz_tot  = 0;

      double row_tot = 0;
      double col_tot = 0;
      double tof_tot = 0;

      double h_tot = 0;
      double k_tot = 0;
      double l_tot = 0;

      int cur_h = (int)Math.round( cent_peak.h );
      int cur_k = (int)Math.round( cent_peak.k );
      int cur_l = (int)Math.round( cent_peak.l );

      int cur_run = cent_peak.run_num;

      double weight = 0;
      double total  = 0;
      for ( int i = in_peaks.size()-1; i >= 0; i-- )
      {
        PeakData_d peak = (PeakData_d)( in_peaks.elementAt(i) );
        if ( cur_h   == (int)Math.round( peak.h )  &&
             cur_k   == (int)Math.round( peak.k )  &&
             cur_l   == (int)Math.round( peak.l )  &&
             cur_run == peak.run_num               &&
             cent_peak.grid.ID() == peak.grid.ID() )
        {
          in_peaks.remove( i );
          weight = peak.counts;
          total += weight;
          cent_peak.counts += weight;

          qx_tot  += weight * peak.qx;
          qy_tot  += weight * peak.qy;
          qz_tot  += weight * peak.qz;

          row_tot += weight * peak.row;
          col_tot += weight * peak.col;
          tof_tot += weight * peak.tof;

          h_tot  += weight * peak.h;
          k_tot  += weight * peak.k;
          l_tot  += weight * peak.l;
        }
      }
      if ( weight > 0 )
      {
        cent_peak.qx  = qx_tot / total;
        cent_peak.qy  = qy_tot / total;
        cent_peak.qz  = qz_tot / total;

        cent_peak.row = row_tot / total;
        cent_peak.col = col_tot / total;
        cent_peak.tof = tof_tot / total;

        cent_peak.h  = h_tot / total;
        cent_peak.k  = k_tot / total;
        cent_peak.l  = l_tot / total;

        out_peaks.add( cent_peak );
      }
    }

    double TOL = 0.15;
    for ( int i = out_peaks.size() - 1; i >= 0; i-- )
    {
      PeakData_d peak = (PeakData_d)( out_peaks.elementAt(i) );
      if ( Math.abs( peak.h - Math.round(peak.h) ) > TOL   ||
           Math.abs( peak.k - Math.round(peak.k) ) > TOL   ||
           Math.abs( peak.l - Math.round(peak.l) ) > TOL    )
        out_peaks.remove( i );
    }

    PeakData_d list[] = new PeakData_d[ out_peaks.size() ]; 
    for ( int i = 0; i < out_peaks.size(); i++ )
      list[i] = (PeakData_d)( out_peaks.elementAt(i) );

    Arrays.sort( list, new PeakDataComparator() );

    for ( int i = 0; i < out_peaks.size(); i++ )
      list[i].seqn = i;

    out_peaks.clear();
    for ( int i = 0; i < list.length; i++ )
      out_peaks.add( list[i] );

    WritePeakData( out_peaks, out_file );
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
   *  @param  ds                The sample histogram DataSet for one of the
   *                            runs used to form the peaks file; this 
   *                            provides the time scale and detector size 
   *                            and position information.
   *
   *  @param  instrument  The particular SCD for which the peaks are being
   *                      read.  This is needed to determine how the sample
   *                      orientation angles chi, phi and omega are interpreted.
   *                      The default is LANSCE SCD, since the shifted 
   *                      goniometer angles follow a strict right hand rule.
   *                      This should be PeakData_d.IPNS_SCD or 
   *                      PeakData_d.LANSCE_SCD.
   *
   *  @return  A vector filled with the PeakData objects read from the file.
   */
  public static Vector ReadPeaks( String  peaks_file_name, 
                                  DataSet ds,
                                  String  instrument )
  {
    Operator op = new ReadPeaks( peaks_file_name );

    Object obj = op.getResult();
    if ( !(obj instanceof Vector) )
    {
      System.out.println("ERROR: Couldn't read peaks file");
      System.out.println( obj.toString() );
      return null;
    }

    if ( ds == null )
    {
      System.out.println("ERROR: null DataSet passed into ReadPeaks" );
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
      System.out.println("ERROR: no area detectors in DataSet " + ds );
      return null;
    }

    UniformGrid_d grid; 
    UniformGrid   sgrid;
    Hashtable grids = new Hashtable();
    for ( int i = 0; i < grid_ids.length; i++ )
    {
      sgrid = (UniformGrid)Grid_util.getAreaGrid( ds, grid_ids[i] );
      grid = new UniformGrid_d( sgrid, false );
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
      PeakData_d pd = new PeakData_d();
      Peak       p  = (Peak)peaks.elementAt(i);
   
      pd.run_num = p.nrun();
      pd.moncnt  = p.monct();
      pd.l1      = attr.getNumericValue();
      pd.seqn    = p.seqnum();
      pd.counts  = p.ipkobs();
      pd.row     = p.y();
      pd.col     = p.x();

      int    bin = (int)p.z() - 1;            // file stores time channel + 1
      if ( bin >= xscale.getNum_x() - 1 )
        pd.tof = xscale.getX( xscale.getNum_x() - 1 );
      else if ( bin >= 0 )
      {
        double x1 = xscale.getX( bin     );
        double x2 = xscale.getX( bin + 1 );
        pd.tof    = x1 + ( p.z() - bin ) * ( x2 - x1 ); 
      }
      else
      {
        pd.tof = 0;
        System.out.println("ERROR: invalid time channel 'z' in " + 
                            peaks_file_name );
      }

      if ( instrument.equalsIgnoreCase( IPNS_SCD ) )
        pd.orientation = new IPNS_SCD_SampleOrientation_d( p.phi(), 
                                                           p.chi(), 
                                                           p.omega() );
      else
        pd.orientation = new LANSCE_SCD_SampleOrientation_d( p.phi(), 
                                                             p.chi(), 
                                                             p.omega() );

      pd.grid = (UniformGrid_d)grids.get( new Integer(p.detnum()) );

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
    Vector peaks = ReadPeakData( peakdata_name, IPNS_SCD );
    System.out.println("Read peaks, # = " + peaks.size() );

    String new_peakdata_name = "junk_peaks.dat";
    System.out.println("Test writing to " + new_peakdata_name );
    WritePeakData( peaks, new_peakdata_name );

    String peaks_name = "/usr/local/ARGONNE_DATA/SCD_QUARTZ_2_DET/quartz.peaks";
    String run_name   = "/usr/local/ARGONNE_DATA/SCD_QUARTZ_2_DET/scd08336.run";
    System.out.println("Test loading " + peaks_name + " and " + run_name );

    RunfileRetriever rr = new RunfileRetriever( run_name );
    DataSet ds = (DataSet)rr.getFirstDataSet( Retriever.HISTOGRAM_DATA_SET );
    if ( ds == null )
      System.out.println("ERROR: Couldn't read Runfile " + run_name);

    peaks = ReadPeaks( peaks_name, ds, IPNS_SCD );
 
    String new_peakdata2_name = "junk_peaks2.dat";
    System.out.println("Test writing to " + new_peakdata2_name );
    WritePeakData( peaks, new_peakdata2_name );
  }

}
