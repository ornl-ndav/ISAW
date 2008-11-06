/* 
 * File: Peak_new_IO.java
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

import gov.anl.ipns.MathTools.Geometry.Vector3D;

import DataSetTools.dataset.IDataGrid;
import DataSetTools.dataset.IDataGridComparator;
import DataSetTools.dataset.UniformGrid;
import DataSetTools.instruments.IPNS_SCD_SampleOrientation;
import DataSetTools.instruments.LANSCE_SCD_SampleOrientation;
import DataSetTools.instruments.SNS_SampleOrientation;
import DataSetTools.instruments.SampleOrientation;
import DataSetTools.instruments.FacilityInstrumentNames;
import DataSetTools.math.tof_calc;


/**
 *  This class has static methods for reading and writing part or all
 *  of a new(as of May 2008) format Peaks file.  Since length values are
 *  listed in CENTIMETERS in the file, but most are recorded in METERS
 *  in internal data structures, these I/O routines do the required
 *  conversions.
 */
public class Peak_new_IO
{
  public static final String L1_T0_TITLES = "6         L1    T0_SHIFT";

  public static final String GRID_TITLES =
                       "4 DETNUM  NROWS  NCOLS   WIDTH   HEIGHT   DEPTH " +
                       "  DETD   CenterX   CenterY   CenterZ    BaseX  " +
                       "  BaseY    BaseZ      UpX      UpY      UpZ";

  public static final String PEAK_GROUP_TITLES =
                       "0 NRUN DETNUM    CHI    PHI  OMEGA MONCNT";

  public static final String PEAK_TITLES =
                       "2   SEQN    H    K    L     COL     ROW    CHAN  " +
                       "     L2  2_THETA       AZ        WL        D " +
                       "  IPK      INTI   SIGI RFLG";

  public static final String VERSION_TITLE    = "Version:";
  public static final String FACILITY_TITLE   = "Facility:";
  public static final String INSTRUMENT_TITLE = "Instrument:";


  private Peak_new_IO()
  {
    // This class has only static methods, so don't let anyone 
    // instantiate it.
  }


  /**
   *  Write the specified Vector of peaks to the specified file in the 
   *  new SNS peaks file format, with calibration information and
   *  a table of detector position and orientation information at
   *  the start of the file.
   *
   *  @param  file_name  The name of the peaks file to be created.
   *  @param  peaks      A Vector of Peak_new objects.  NOTE:
   *                     The Vector must contain only Peak_new objects.
   *  @param  append     Flag indicating whether or not to append to
   *                     an existing peaks file (CURRENTLY NOT USED).
   *
   *  @throws an IOException if there is a problem writing the file.
   */
  public static void WritePeaks_new( String           file_name, 
                                     Vector<Peak_new> peaks, 
                                     boolean          append      )
                     throws IOException
  {
     PrintStream out = new PrintStream( file_name );  
     if ( peaks == null || peaks.size() <= 0 )
       throw new IllegalArgumentException(
                               "Null or empty peaks Vector in Write_new()");

                                                  // Find all the grids

     Hashtable<Integer,IDataGrid> grids = new Hashtable<Integer,IDataGrid>();
     int num_peaks = peaks.size();
     IDataGrid grid;
     for ( int i = 0; i < num_peaks; i++ ) 
     {
       grid = peaks.elementAt(i).getGrid();
       grids.put( grid.ID(), grid );  
     }
                                                   // Sort the grids     
     Enumeration<IDataGrid > grid_enum = grids.elements();
     IDataGrid[] grid_array = new IDataGrid[ grids.size() ];
     int index = 0;
     while ( grid_enum.hasMoreElements() )
       grid_array[ index++ ] = grid_enum.nextElement();

     Arrays.sort( grid_array, new IDataGridComparator() );

     Object[]   temp_array = peaks.toArray();
     Peak_new[] peak_array = new Peak_new[temp_array.length];
     for ( int i = 0; i < temp_array.length; i++ )
        peak_array[i] = (Peak_new)(temp_array[i]);

     Arrays.sort( peak_array, new Peak_newComparator() );

     out.println( VERSION_TITLE    + " " + "2.0" + "  " +
                  FACILITY_TITLE   + " " + peak_array[0].getFacility() + "  " +
                  INSTRUMENT_TITLE + " " + peak_array[0].getInstrument() );
                         
     out.println( L1_T0_TITLES ); 
     out.println( L1_T0_String( peak_array[0]) ); 

     out.println( GRID_TITLES );
     for ( int i = 0; i < grid_array.length; i++ )
       out.println( GridString( grid_array[i] ) );

     int previous_id  = -1; 
     int previous_run = -1;
     int id;
     int run;
     Peak_new peak;
     for ( int i = 0; i < peak_array.length; i++ )
     {
       peak = peak_array[i];
       id   = peak.getGrid().ID(); 
       run  = peak.nrun();
       if ( id != previous_id || run != previous_run )
       {
         out.println( PEAK_GROUP_TITLES );
         out.println( PeakGroupString( peak ) );
         out.println( PEAK_TITLES );
         previous_id  = id;
         previous_run = run;
       }

       peak.seqnum(i+1);
       out.println( PeakString( peak ) );
     }
     out.close();
  }


 /**
  *  Get a String listing the calibrated L1 and T0_shift values
  *  for a group of peaks, in a form required for writing a new 
  *  format peaks file.
  *
  *  NOTE: The L1 value from the peak is converted from METERS
  *        to CM, for writing to the peaks file.
  *
  *  @param peak  The peak from which the information is obtained.
  *
  *  @return a String listing the L1 and T0_shift values.
  */
  public static String L1_T0_String( Peak_new peak )
  {
     float l1 = peak.L1() * 100f;    // l1 in cm
     float T0 = peak.T0();
     return String.format("7 %10.4f  %10.3f", l1, T0 );
  } 


 /**
  *  Get a String listing the run number, detector number, chi, phi
  *  and omega, etc. for a group of peaks, in a form required for 
  *  writing a new format peaks file.
  *
  *  NOTE: The L1 value from the peak is converted from METERS
  *        to CM, for writing to the peaks file.
  *
  *  @param peak  The peak from which the information is obtained.
  *
  *  @return a String listing the run number, detector number, etc.
  *          for the group of peaks.
  */
  public static String PeakGroupString( Peak_new peak )
  {
    int id      = peak.getGrid().ID();
    int run_num = peak.nrun();
    float chi   = peak.chi();
    float phi   = peak.phi();
    float omega = peak.omega();
    float monct = peak.monct();
    return String.format( "1 %4d %6d %6.2f %6.2f %6.2f %6.0f",
                           run_num, id, chi, phi, omega, monct );
  }


 /**
  *  Get a String listing the information from a peak in the form
  *  required for writing a new format peaks file.
  *
  *  NOTE: The L2 value from the peak pixel is converted from METERS
  *        to CM, for writing to the peaks file.
  *
  *  @param peak  The peak from which the information is obtained.
  *
  *  @return a String listing the sequence number, h, k, l, col,
  *          row, channel, etc. for the specified peak.
  */
  public static String PeakString( Peak_new peak )
  {
    int seqn   = peak.seqnum();
    float h    = peak.h();
    float k    = peak.k();
    float l    = peak.l();
    float col  = peak.x();
    float row  = peak.y();
    float chan = peak.z() + 1;  // external form has channels starting at 1

    float[] spherical_coords = ((Peak_new)peak).NeXus_coordinates();
    float l2        = spherical_coords[0] * 100;
    float two_theta = spherical_coords[2];
    float azimuth   = spherical_coords[1];
    float wl        = peak.wl();
    float dspacing  = peak.d();
    float ipk       = peak.ipkobs();
    float inti      = peak.inti();
    float sigi      = peak.sigi();
    int   reflag    = peak.reflag();
    return String.format( "3 %6d %4.0f %4.0f %4.0f %7.2f %7.2f %7.2f " +
                          "%8.3f %8.5f %8.5f %9.6f %8.4f " +
                          "%5.0f %9.2f %6.2f %4d",
                           seqn, h, k, l, col, row, chan,
                           l2, two_theta, azimuth, wl, dspacing,
                           ipk, inti, sigi, reflag );
  }


  /**
   *  Get a String listing the information from a grid in the form
   *  required for writing a new format peaks file.  
   *
   *  NOTE: The grid dimensions and position are converted from METERS
   *        to CM in the formatted String.
   *
   *  NOTE: This formats the grid info in SNS/NeXus coordinates!!!
   *
   *  @param grid  The grid from which the information is obtained.
   *
   *  @return A String listing the id, size, position and orientation 
   *          of the grid.
   */
  public static String GridString( IDataGrid grid )
  {
    int   id        = grid.ID();
    int   n_rows    = grid.num_rows();
    int   n_cols    = grid.num_cols();
    float width     = grid.width()  * 100;    // convert to cm
    float height    = grid.height() * 100;
    float depth     = grid.depth()  * 100;
    Vector3D center = grid.position();
    Vector3D base   = grid.x_vec();
    Vector3D up     = grid.y_vec();
    float det_d     = center.length() * 100;

    return String.format("5 %6d %6d %6d %7.4f %7.4f %7.4f %6.2f " +
                         "%9.4f %9.4f %9.4f " +
                         "%8.5f %8.5f %8.5f " +
                         "%8.5f %8.5f %8.5f",
                      id, n_rows, n_cols, width, height, depth, det_d,
                      center.getY()*100, center.getZ()*100, center.getX()*100,
                        base.getY(),       base.getZ(),       base.getX(),
                          up.getY(),         up.getZ(),         up.getX() );
  }


  /**
   *  Read a complete new format peaks file and return a Vector of 
   *  Peak_new objects.
   *
   *  @param filename  The name of the new peaks file to be read
   *
   *  @return a Vector containing the Peak_new objects that were constructed
   *          based on information from the specified file.
   *
   *  @throws IOException if there is a problem reading the specified file.
   */
  public static Vector<Peak_new> ReadPeaks_new( String filename ) 
                                 throws IOException
  {
     FileReader     f_in        = new FileReader( filename );
     BufferedReader buff_reader = new BufferedReader( f_in );
     Scanner        sc          = new Scanner( buff_reader );

     String[]  header_strings = ReadHeaderInfo( sc );
     float[]   l1_t0 = Read_L1_T0( sc );
     Hashtable grids = Read_Grids( sc );
    
     float  l1         = l1_t0[0];
     float  T0_shift   = l1_t0[1];
     String facility   = header_strings[1];
     String instrument = header_strings[2];
     Vector<Peak_new> peaks = Read_Peaks( sc, 
                                          grids, 
                                          l1, 
                                          T0_shift, 
                                          facility, 
                                          instrument );
     sc.close();
     return peaks;
  }


  /**
   *  Read in the header information, consisting of the version, facility
   *  and instrument.  Comment lines starting with a "#" can precede the
   *  header line.  The file (scanner) should already be opened and 
   *  positioned at the start of the file before calling this method.
   *  When this method returns, the file will be positioned at the start
   *  of the first line following the line with the version, facility and
   *  instrument.
   *  NOTE: This method is used by the ReadPeaks_new() method, which is the 
   *  method most users should call to read the peaks file.
   *
   *  @return an array of Strings, containing the version, facility and
   *          instrument names in that order.  If Strings with the proper
   *          titles are not found, this returns null.
   */
  public static String[] ReadHeaderInfo( Scanner sc ) throws IOException
  {
     String version_title = sc.next();
     while ( version_title.startsWith("#") )       // Skip any comment lines
     {
       sc.nextLine();
       version_title = sc.next();
     }

     if ( !version_title.equalsIgnoreCase( VERSION_TITLE ) )
       throw new IOException("ERROR: Bad version title in ReadHeaderInfo");

     String[] results = new String[3];
     results[0] = sc.next();                       // The version name

     String facility_title = sc.next();
     if ( !facility_title.equalsIgnoreCase( FACILITY_TITLE ) )
       throw new IOException("ERROR: Bad facility title in ReadHeaderInfo");

     results[1] = sc.next();                       // Facility name

     String instrument_title = sc.next();
     if ( !instrument_title.equalsIgnoreCase( INSTRUMENT_TITLE ) )
       throw new IOException("ERROR: Bad instrument title in ReadHeaderInfo");
     
     results[2] = sc.next();                       // Instrument name

     sc.nextLine();                          // Discard the rest of the line
                                             // so next method starts at the
                                             // beginning of a line.
     return results; 
  }


 /**
  *  Find and read the values for L1 (in METERS) and T0 at the start of 
  *  the peaks file.  Lines that don't start with 7 will be skipped.  
  *  When the line starting with 7 is found, the L1 and T0_SHIFT values 
  *  will be read.
  *  When this method returns, the file will be positioned at the start
  *  of the line following the line with the l1 and t0 values.
  *
  *  NOTE: This method is used by the ReadPeaks_new() method, which is the 
  *  method most users should call to read the peaks file.
  *
  *  @param sc   The Scanner from which the values are to be read.
  *
  *  @return An array containing two floats, the L1 value and the T0 value
  *          in that order.  The value of L1 is CONVERTED from CM to METERS.
  *          and the value in meters is returned by this method.
  *          If the values are not read correctly from the file, this 
  *          method returns null.
  *
  *  @throws IOException if the values are not read successfully.
  */
  public static float[] Read_L1_T0( Scanner sc ) throws IOException
  {
    String next_val = sc.next();
    while ( !next_val.equals("7") )            // skip to line type 7 which
    {                                          // has l1 & t_zero values
      sc.nextLine();
      next_val = sc.next();
    }

     float l1 = Float.NaN;
     float t0 = Float.NaN;
     l1 = sc.nextFloat()/100f;
     t0 = sc.nextFloat();
     sc.nextLine();                            // Discard the rest of the line
                                               // so next method starts at the
                                               // beginning of a line.

     if ( Float.isNaN( l1 ) || Float.isNaN( t0 ) )    // something is wrong
       throw new IOException("l1 or T0_shift NaN in Read_L1_T0");

     float[] results = new float[2];
     results[0] = l1;
     results[1] = t0;

     return results;
  }


  /**
   *  Read in all of the grid information from the start of the file,
   *  converting from CENTIMETER values recorded in the file the values
   *  in METERS required by ISAW.  
   *  The file (scanner) must be positioned at the START of a line, before
   *  the first detector grid information line (line type 5).
   *  When this method returns, the file will be positioned at the start
   *  of the second non-blank line after the last detector grid information
   *  line.  
   *
   *  NOTE: This method is used by the ReadPeaks_new() method, which is the 
   *        method most users should call to read the peaks file.
   *
   *  NOTE: Grids are returned with sizes and center position in METERS.
   *
   *  NOTE: Currently, this permutes the coordinates, so that assuming
   *        the peaks file is written in SNS/NeXus coordinates, this 
   *        method will return grids in ISAW coordinates.
   *
   *  @param  sc   The Scanner from which the grid information is read.
   *  @return a Hashtable of Uniform grids with grid ID as key.
   *
   *  @throws IOException if no detector grids were successfully read. 
   */
  public static Hashtable<Integer,UniformGrid> Read_Grids( Scanner sc )
                                               throws IOException
  {
    String next_val = sc.next();
    while ( !next_val.equals("5") )            // skip to line type 5 which
    {                                          // has the data grid info
      sc.nextLine();
      next_val = sc.next();
    }
    
    Hashtable<Integer,UniformGrid> grids = new Hashtable<Integer,UniformGrid>();
    boolean more_grids = true;
    while ( more_grids )
    {
      int id = sc.nextInt();
      int nrows      = sc.nextInt();
      int ncols      = sc.nextInt();
      float width    = sc.nextFloat()/100; // file values in cm
      float height   = sc.nextFloat()/100;
      float depth    = sc.nextFloat()/100;
      sc.next(); // skip the redundant DETD
      float center_y = sc.nextFloat()/100;
      float center_z = sc.nextFloat()/100;
      float center_x = sc.nextFloat()/100;
      float base_y   = sc.nextFloat();
      float base_z   = sc.nextFloat();
      float base_x   = sc.nextFloat();
      float up_y     = sc.nextFloat();
      float up_z     = sc.nextFloat();
      float up_x     = sc.nextFloat();
      Vector3D center  = new Vector3D( center_x, center_y, center_z );
      Vector3D base    = new Vector3D( base_x, base_y, base_z );
      Vector3D up      = new Vector3D( up_x, up_y, up_z );
      UniformGrid grid = new UniformGrid( id, "m",
                                        center, base, up,
                                        width, height, depth,
                                        nrows, ncols );
      grids.put( new Integer(grid.ID()), grid );
      if ( sc.hasNext() )
      {
        if ( !sc.next().equals("5") )
        {
          more_grids = false;
          sc.nextLine();                // discard the line so next method
        }                               // can start at the beginning of a line
      }
      else
        more_grids = false;
    }

    if ( grids.size() <= 0 )
      throw new IOException("ERROR: No Detector grids found in peaks file");

     return grids;
  }


  /**
   *  Read the peaks portion of a new Peaks file.  This method assumes that
   *  the header and grid information has already been read, and that the 
   *  next character to be read is the first character on a line, at or 
   *  before the first line of peak run/sample orientation(line type "1").
   *  This method will finish reading all peak information form the file.
   *  NOTE: This method is used by the ReadPeaks_new() method, which is the 
   *  method most users should call to read the peaks file.
   *  NOTE: The values for l2, 2_theta, az, and d-spacing are all ignored
   *  when reading the file, since that information is derived from the
   *  pixel position and wavelength.  Values for l2, 2_theta, az and d
   *  are included in the peaks file for information purposes only.
   *
   *  @param  sc          The scanner from which the peaks are to be read.
   *  @param  grids       A Hashtable of UniformGrid objects to use when
   *                      constructing the Peak_new objects.  There must be
   *                      an entry in this Hashtable for each detector listed 
   *                      in the peaks file.
   *  @param  l1_header   The l1 value (in METERS) to use for each peak.
   *  @param  T0_shift    The T0 value to use for each peak.
   *  @param  facility    The facility name to use for each peak.
   *  @param  instrument  The instrument name to use for each peak.
   *
   *  @throws IOException if the file cannot be read properly.
   */
  public static Vector<Peak_new> 
                Read_Peaks( Scanner   sc, 
                            Hashtable<Integer,UniformGrid> grids,
                            float     l1_header,
                            float     T0_shift,
                            String    facility,
                            String    instrument )
                throws IOException
  {
    Vector<Peak_new> peaks = new Vector<Peak_new>();

    int nrun,
        detnum,
        seqn,
        h, k, l,
        ipk,
        rflg;
    float chi, phi, omega, moncnt,
          l2,
          col, row, chan,
          wl, tof,
          inti, sigi;

    String next_val = sc.next();

    boolean has_more_lines = sc.hasNext();
    while ( has_more_lines )
    {
      while ( !next_val.equals("1") )            // skip to line type 1 which
      {                                          // has the RUN, DET, CHI, etc. 
        sc.nextLine();
        next_val = sc.next();
      }
      nrun   = sc.nextInt();
      detnum = sc.nextInt();
      chi    = sc.nextFloat();
      phi    = sc.nextFloat();
      omega  = sc.nextFloat();
      moncnt = sc.nextFloat();
      sc.nextLine();                              // advance to next full line

      SampleOrientation orientation;

      if ( facility.equalsIgnoreCase(FacilityInstrumentNames.SNS) )
      {
//      System.out.println("SNS orientation");
        orientation = new SNS_SampleOrientation( phi, chi, omega);
      }
      else if ( facility.equalsIgnoreCase(FacilityInstrumentNames.LANSCE) )
        orientation = new LANSCE_SCD_SampleOrientation( phi, chi, omega);

      else
      {
        System.out.println("IPNS orientation");
        orientation = new IPNS_SCD_SampleOrientation( phi, chi, omega);
      }
      
      if ( !sc.next().equals("2") )  
        throw new IOException("ERROR in peaks file, expected line type 2");
     
      sc.nextLine();                               // skip peak heading line
      
      next_val = sc.next();
      while (sc.hasNext() && next_val.equals("3")) // process all type 3 lines 
      {        
        seqn   = sc.nextInt();
        h      = sc.nextInt();
        k      = sc.nextInt();
        l      = sc.nextInt();
        col    = sc.nextFloat();
        row    = sc.nextFloat();
        chan   = sc.nextFloat() - 1;     // file has channel + 1
                 sc.nextFloat();         // skip the l2 value from the
                                         // file, since it is just a nominal
                                         // value and we later calculate it.
                                         // IF used, it would need to be /100
                 sc.nextFloat();         // skip 2_theta since it is derived
                 sc.nextFloat();         // skip az, since it is derived
        wl     = sc.nextFloat();
                 sc.nextFloat();         // skip d, since it is derived
        ipk    = sc.nextInt();
        inti   = sc.nextFloat();
        sigi   = sc.nextFloat(); 
        rflg   = sc.nextInt();
        IDataGrid grid = grids.get( detnum );
        if ( grid == null )
          throw new IOException("ERROR: missing detector #" + detnum);

        Vector3D position = grid.position(row,col);
        l2 = position.length();
        tof = tof_calc.TOFofWavelength( l1_header + l2, wl );

        Peak_new peak = new Peak_new( nrun,
                                      moncnt,
                                      col, row, chan,
                                      grid,
                                      orientation,
                                      tof,
                                      l1_header,
                                      T0_shift    );

        peak.setFacility( facility );
        peak.setInstrument( instrument );
        peak.seqnum( seqn );
        peak.sethkl( h, k, l );
        peak.ipkobs( ipk );
        peak.inti( inti );
        peak.sigi( sigi );
        peak.reflag( rflg );
        peaks.add( peak );
 
        if ( sc.hasNext() )
          sc.nextLine();
        if ( sc.hasNext() )
          next_val = sc.next();
      }

      if ( !sc.hasNext() )
        has_more_lines = false;
    }

    return peaks;
  }


  /**
   *  Basic functionality test of peak I/O
   */
  public static void main( String args[] )
  {
    int   id    = 17;
    Vector3D center = new Vector3D(  0.064748f, -.253230f,  -0.011417f );
    Vector3D x_vec  = new Vector3D( -0.954441f, -0.298400f, -0.000457f );
    Vector3D y_vec  = new Vector3D(  0.000054f, -0.001706f,  0.999999f );
    float    width  = 17.1228f / 100;
    float    height = 17.1218f / 100;
    float    depth  = 0.2f / 100;
    int      n_rows = 100;
    int      n_cols = 100;
    IDataGrid grid = new UniformGrid( id, "m",
                                      center, x_vec, y_vec,
                                      width, height, depth,
                                      n_rows, n_cols );
    int   run_num = 8336;
    float mon_ct  = 10101f;
    float col   = 14.64f;
    float row   = 42.12f;
    float chan  = 24.55f;
    float phi   = 0;
    float chi   = 167;
    float omega = 45;
    SampleOrientation orientation =
                         new IPNS_SCD_SampleOrientation( phi, chi, omega );
    float tof = 1263;
    float initial_path = 9.3777f;
    float t0 = 23.45f;

    Peak_new peak = new Peak_new( run_num,
                                  mon_ct,
                                  col, row, chan,
                                  grid,
                                  orientation,
                                  tof,
                                  initial_path,
                                  t0             );
    peak.setFacility( "IPNS" );
    peak.setInstrument( "SCD0" );
    peak.seqnum( 46 );
    peak.sethkl( -2, 9, 3 );
    peak.ipkobs(21);
    peak.inti( 104.99f );
    peak.sigi( 20.35f );
    peak.reflag( 10 );

    System.out.println("------------- OLD PEAK FORMAT --------------" );
    System.out.println( peak );
    System.out.println(); 
    System.out.println("------------- NEW PEAK FORMAT --------------" );

    System.out.println( L1_T0_TITLES );
    System.out.println( L1_T0_String( peak ) );

    System.out.println( GRID_TITLES );
    System.out.println( GridString( grid ) );
    System.out.println( GridString( grid ) );
    System.out.println( GridString( grid ) );

    System.out.println( PEAK_GROUP_TITLES );
    System.out.println( PeakGroupString( peak ) );

    System.out.println( PEAK_TITLES );
    System.out.println( PeakString( peak ) );
    System.out.println( PeakString( peak ) );
    System.out.println( PeakString( peak ) );
    System.out.println( PeakString( peak ) );
    System.out.println( PeakString( peak ) );

    Vector peaks = new Vector();
    for ( int i = 0; i < 10; i++ )
      peaks.add(peak);

    try 
    {
      WritePeaks_new( "Test_1.peaks", peaks, false );
      peaks = ReadPeaks_new( "Test_1.peaks" );
      WritePeaks_new( "Test_2.peaks", peaks, false );
/*
      peaks = ReadPeaks_new("/usr2/SNS_OXALIC_ACID_TEST/oxalic.integrate");
      WritePeaks_new( "Test_3.peaks", peaks, false );

      peaks = ReadPeaks_new( "Test_3.peaks" );
      WritePeaks_new( "Test_4.peaks", peaks, false );
*/
    }
    catch ( IOException ex )
    {
       System.out.println( ex );
       ex.printStackTrace();
    }
  }

}
