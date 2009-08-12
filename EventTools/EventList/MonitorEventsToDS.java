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
    for ( int i = 0; i < bytes_read-4; i += 4 )
      histogram[index++] = getValue_32( buffer, i+4 );

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
   * Decode the integer value stored in a sequence of 
   * two bytes in the buffer.  The four bytes determining
   * the Integer value are stored in the file and buffer in the 
   * sequence: b0, b1, b2, b3, with the lowest order byte, b0, first
   * and the the highest order byte, b3, last.
   * 
   * @param byte_index  The index of the first byte in the
   *                    buffer
   *                    
   * @return The integer value represented by four successive bytes from
   *         the file. 
   */
  private static int getValue_32( byte[] buffer, int byte_index )
  {
    byte_index += 3;                    // go to high order byte for
                                        // this integer, put it in a int
    int byte_v = buffer[byte_index--];  // variable, and make it positive
    if ( byte_v < 0 )                   // in case the "signed byte" was <0
      byte_v += 256;

    int val = byte_v;                   // store high order byte in val and
                                        // proceed to build up the total val
    byte_v = buffer[byte_index--];      // by combining with the lower order
    if ( byte_v < 0 )                   // three bytes.
      byte_v += 256;

    val = val * 256 + byte_v;

    byte_v = buffer[byte_index--];
    if ( byte_v < 0 )
      byte_v += 256;

    val = val * 256 + byte_v;

    byte_v = buffer[byte_index];
    if ( byte_v < 0 )
      byte_v += 256;

    val = val * 256 + byte_v;

    return val;
  }


  /**
   *  main program providing basic test for this class
   */
  public static void main(String[] args) throws IOException
  {
//  String file_name = "/usr2/SNAP_4/EVENTS/SNAP_732_bmon_histo.dat";
    String file_name = "/usr2/SNAP_5/SNAP_875_bmon_histo.dat";
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
