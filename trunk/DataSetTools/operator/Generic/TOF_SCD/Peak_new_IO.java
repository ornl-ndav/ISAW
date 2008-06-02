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
import DataSetTools.instruments.SampleOrientation;


public class Peak_new_IO
{
  public static final String L1_T0_TITLES = "6         L1    T0_SHIFT";

  public static final String GRID_TITLES =
                       "4 DETNUM  NROWS  NCOLS   WIDTH  HEIGHT   DEPTH " +
                       "  DETD  CenterX  CenterY  CenterZ    BaseX  " +
                       "  BaseY    BaseZ      UpX      UpY      UpZ";

  public static final String PEAK_GROUP_TITLES =
                       "0 NRUN DETNUM    CHI    PHI  OMEGA MONCNT     L1";

  public static final String PEAK_TITLES =
                       "2   SEQN    H    K    L     COL     ROW    CHAN  " +
                       "     L2  2_THETA       AZ        WL        D " +
                       "  IPK      INTI   SIGI RFLG";


  private Peak_new_IO()
  {
    // This class has only static methods, so don't let anyone 
    // instantiate it.
  }


  /**
   *  Write the specified peaks to the specified file in the 
   *  new SNS peaks file format, with calibration information and
   *  a table of detector position and orientation information at
   *  the start of the file.
   *
   *  @param  file_name  The name of the peaks file to be created.
   *  @param  peaks      A Vector of Peak_new objects.  NOTE:
   *                     The Vector must contain only Peak_new objects.
   *  @param  append     Flag indicating whether or not to append to
   *                     an existing peaks file (CURRENTLY NOT USED).
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

     out.println( "Version: 2.0" + 
                         "  Facility: "    + peak_array[0].getFacility() +
                         "  Instrument: "  + peak_array[0].getInstrument() );
                         
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
  }


 /**
  *  Get a String listing the calibrate L1 and T0_shift values
  *  for a group of peaks, in a form required for writing a new 
  *  format peaks file.
  *
  *  @param peak  The peak from which the information is obtained.
  *
  *  @return a String listing the L1 and T0_shift values.
  */
  public static String L1_T0_String( Peak_new peak )
  {
     float l1 = peak.L1() * 100;    // l1 in cm
     float T0 = peak.T0();
     return String.format("7 %10.4f  %10.3f", l1, T0 );
  } 


 /**
  *  Get a String listing the run number, detector number, chi, phi
  *  and omega, etc. for a group of peaks, in a form required for 
  *  writing a new format peaks file.
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
    SampleOrientation orientation = peak.getSampleOrientation();
    float chi   = peak.chi();
    float phi   = peak.phi();
    float omega = peak.omega();
    float monct = peak.monct();
    float l1    = peak.L1() * 100;
    return String.format( "1 %4d %6d %6.2f %6.2f %6.2f %6.0f %6.1f",
                           run_num, id, chi, phi, omega, monct, l1 );
  }


 /**
  *  Get a String listing the information from a peak in the form
  *  required for writing a new format peaks file.
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
   *  NOTE: This writes the grid info in SNS/NeXus coordinates!!!
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
                         "%8.4f %8.4f %8.4f " +
                         "%8.5f %8.5f %8.5f " +
                         "%8.5f %8.5f %8.5f",
                      id, n_rows, n_cols, width, height, depth, det_d,
                      center.getY()*100, center.getZ()*100, center.getX()*100,
                        base.getY(),       base.getZ(),       base.getX(),
                          up.getY(),         up.getZ(),         up.getX() );
  }

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
    float t0 = 0;

    Peak_new peak = new Peak_new( run_num,
                                  mon_ct,
                                  col, row, chan,
                                  grid,
                                  orientation,
                                  tof,
                                  initial_path,
                                  t0             );
    peak.seqnum( 46 );
    peak.sethkl( -2, 9, 3 );
    peak.ipkobs(21);
    peak.inti( 104.99f );
    peak.sigi( 20.35f );
    peak.reflag( 10 );

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
  }

}
