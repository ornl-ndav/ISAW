/* 
 * File: MonitorEventsToDS.java
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

package EventTools.EventList;

import java.io.*;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;

/**
 * This class has a static method to load an SNS monitor histogram
 * into a DataSet.
 */
public class  MonitorEventsToDS
{
  public static final int BUFFER_SIZE = 80004;

  public static int[] getMonitorHistogram( String filename )
                      throws IOException
  {
    File ev_file = new File( filename );
    if ( !ev_file.exists() )
      throw new IllegalArgumentException( filename + " does not exist.");

    long file_size = ev_file.length();
    if ( file_size != 80004 )
      throw new IllegalArgumentException( filename + " is not a monitor file.");

    byte[] buffer = new byte[BUFFER_SIZE];
    RandomAccessFile r_file = new RandomAccessFile( filename, "r" );

    r_file.seek( 0 );
    long bytes_read = r_file.read( buffer );
    System.out.println("READ " + bytes_read + " bytes");
    r_file.close();

    int[] histogram = new int[ BUFFER_SIZE/4 ];

    int index = 0;
    for ( int i = 0; i < bytes_read; i += 4 )
      histogram[index++] = SNS_TofEventList.getValue_32( buffer, i );

    return histogram;
  }


  public static DataSet getMonitorDataSet( String filename )
                        throws IOException
  {
    int[] hist = getMonitorHistogram( filename );

    float[] ys = new float[1000];
    for ( int i = 0; i < 1000; i++ )
    {
       int sum = 0;
       for ( int k = 20*i; k < 20*i+20; k++ )
         sum += hist[k];
       ys[i] = sum;
    }    

    XScale x_scale = new UniformXScale( 0, 20000, 1000 );
    Data data = new HistogramTable( x_scale, ys, 1 );

    DataSet ds = new DataSet("Beam Monitor (20 bins summed)",
                             new OperationLog(),
                             "Microseconds",
                             "Time-of-flight",
                             "counts",
                             "Beam Intensity");
    ds.addData_entry( data );
    return ds;
  }


  /**
   *  main program providing basic test for this class
   */
  public static void main(String[] args) throws IOException
  {
    String file_name = "/usr2/SNAP_4/EVENTS/SNAP_732_bmon_histo.dat";
//  String file_name = "/usr2/SNAP_5/SNAP_875_bmon_histo.dat";
    int[]  histogram = getMonitorHistogram( file_name );

    for ( int i = 0; i < 10; i++ )
      System.out.println( "" + i + " " + histogram[i] );

    for ( int i = 5000; i < 5010; i++ )
      System.out.println( "" + i + " " + histogram[i] );

    for ( int i = 10000; i < 10010; i++ )
      System.out.println( "" + i + " " + histogram[i] );

    for ( int i = 15000; i < 15010; i++ )
      System.out.println( "" + i + " " + histogram[i] );

    for ( int i = 19990; i < 20000; i++ )
      System.out.println( "" + i + " " + histogram[i] );

    DataSet ds = getMonitorDataSet( file_name );
    new ViewManager( ds, IViewManager.SELECTED_GRAPHS );
  }

}
