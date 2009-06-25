/* 
 * File: Byte16FileEventList3D.java
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

package EventTools.EventList;

import java.io.*;

import EventTools.Histogram.IEventBinner;
import EventTools.Histogram.UniformEventBinner;


/**
 * This class accesses a list of events, coded using 16-bit unsigned
 * values packed in an array of bytes.  The array of bytes is loaded
 * from a file.  NOTE: A large array of bytes can be read from a disk
 * very quickly.  Typical disk read times are around 0.1 sec for a
 * list of 3 million events on PC/workstation hardware. 
 * This code is a prototype, so the file format WILL CHANGE.  The
 * current file format is:
 * 32-bit int giving the number of events.
 * float,float,32-bit int specifying the "X" binner.
 * float,float,32-bit int specifying the "Y" binner.
 * float,float,32-bit int specifying the "Z" binner.
 * list of 8*(number of events) bytes containing four 16-bit values
 * holding the x,y,z information and codes for each event.  The
 * first three 16-bit values get mapped to x,y,z coordinates using
 * the x,y,z binners, respectively.  The fourth 16-bit value holds 
 * the integer code for the event.  
 */
public class ByteFile16EventList3D implements IEventList3D 
{
  private IEventBinner x_binner = null;
  private IEventBinner y_binner = null;
  private IEventBinner z_binner = null;

  private int     num_events;
  private byte[]  buffer;
  private int[]   specified_codes = null;

/**
 * Construct an event list from the specified binary file.
 * 
 * @param  filename    Fully qualified file name for the file of events.
 *  
 * @throws IOException  If the file cannot be opened, or if an error is
 *                      encountered while reading the file.
 */
  public ByteFile16EventList3D( String filename ) throws IOException
  {
    FileInputStream fis = new FileInputStream( filename );
    BufferedInputStream bis = new BufferedInputStream( fis );
    DataInputStream dis = new DataInputStream( bis );

    num_events = dis.readInt();

    double min       = dis.readFloat();
    double max       = dis.readFloat();
    int    num_steps = dis.readInt();
    x_binner = new UniformEventBinner( min, max, num_steps );

    min       = dis.readFloat();
    max       = dis.readFloat();
    num_steps = dis.readInt();
    y_binner = new UniformEventBinner( min, max, num_steps );

    min       = dis.readFloat();
    max       = dis.readFloat();
    num_steps = dis.readInt();
    z_binner = new UniformEventBinner( min, max, num_steps );

    int num_bytes = 8*num_events;
    buffer = new byte[ num_bytes ];

    dis.read( buffer, 0, buffer.length );
    fis.close();
  }

  
  @Override
  public int numEntries()
  {
    return num_events;
  }

  
  @Override
  public int eventCode( int i )
  {
    if ( specified_codes == null )
      return getValue_16( i * 8 + 6 );
    else
      return specified_codes[i];
  }
 
  
  @Override
  public int[] eventCodes()
  {
    if ( specified_codes != null )
      return specified_codes;

    int[] codes = new int[ num_events ];
    for ( int i = 0; i < num_events; i++ )
      codes[i] = getValue_16( i * 8 + 6 );

    return codes;
  }


  @Override
  public void setEventCodes( int[] codes )
  {
    this.specified_codes = codes;
  }

  
  @Override
  public void eventVals( int i, double[] values )
  {
    int index = getValue_16( i * 8 );
    values[0] = x_binner.centerVal( index );

    index = getValue_16( i * 8 + 2 );
    values[1] = y_binner.centerVal( index );

    index = getValue_16( i * 8 + 4 );
    values[2] = z_binner.centerVal( index );
  }

  
  @Override
  public float[] eventVals()
  {
    float[] values = new float[3*num_events];
    int index;
    for ( int i = 0; i < num_events; i++ )
    {
      index = getValue_16( i * 8 );
      values[ index ] = (float)x_binner.centerVal( index );
      index++;
      values[ index ] = (float)y_binner.centerVal( index );
      index++;
      values[ index ] = (float)z_binner.centerVal( index );
      index++;
    }
    return values;
  }
  
  @Override
  public double eventX( int i )
  {
    int index = getValue_16( i * 8 );
    return x_binner.centerVal( index );
  }


  @Override
  public double eventY( int i )
  {
    int index = getValue_16( i * 8 + 2 );
    return y_binner.centerVal( index );
  }


  @Override
  public double eventZ( int i )
  {
    int index = getValue_16( i * 8 + 4 );
    return z_binner.centerVal( index );
  }


  public IEventBinner xExtent( )
  {
    return x_binner;
  }


  public IEventBinner yExtent( )
  {
    return y_binner;
  }


  public IEventBinner zExtent( )
  {
    return z_binner;
  }


  /**
   *  Return a string giving the number of entries, and the
   *  x,y,z extents of the events.
   */
  public String toString()
  {
    return String.format( "Num: %6d ", numEntries() ) +
           "XRange: " + xExtent() +
           "YRange: " + yExtent() +
           "ZRange: " + zExtent(); 
  }


  /**
   * Decode the integer value stored in a sequence of 
   * two bytes in the buffer.
   * 
   * @param byte_index  The index of the first byte in the
   *                    buffer
   *                    
   * @return The integer value 256*byte_2 + byte_1
   */
  private int getValue_16( int byte_index )
  {
    int byte_1 = buffer[byte_index++];

    if ( byte_1 < 0 )
      byte_1 += 256;

    int byte_2 = buffer[byte_index];

    if ( byte_2 < 0 )
      byte_2 += 256;

    return  byte_2 * 256 + byte_1;
  }


  /**
   *  Write a file containing a list of 3D events, with coordinates
   *  rounded to 16 bits, in the form read by the constructor of 
   *  this class.
   *
   *  @param events      The list of events to write to the file.
   *  @param file_name   The name of the file to write
   *
   */
  public static void MakeByteFile16_3D( IEventList3D events,
                                        String       file_name  )
                     throws IOException
  {
    FileOutputStream fos     = new FileOutputStream( file_name );
    BufferedOutputStream bos = new BufferedOutputStream( fos );
    DataOutputStream dos     = new DataOutputStream( bos );

    int num_events = events.numEntries();

    IEventBinner x_extent = events.xExtent();
    IEventBinner y_extent = events.yExtent();
    IEventBinner z_extent = events.zExtent();
    float x_min = (float)x_extent.axisMin();
    float x_max = (float)x_extent.axisMax();
    float y_min = (float)y_extent.axisMin();
    float y_max = (float)y_extent.axisMax();
    float z_min = (float)z_extent.axisMin();
    float z_max = (float)z_extent.axisMax();

    int n_x = 65536;
    int n_y = 65536;
    int n_z = 65536;

    UniformEventBinner x_binner = new UniformEventBinner( x_min, x_max, n_x );
    UniformEventBinner y_binner = new UniformEventBinner( y_min, y_max, n_y );
    UniformEventBinner z_binner = new UniformEventBinner( z_min, z_max, n_z );

    System.out.println("START FILE WRITE");
    long start = System.nanoTime();

    dos.writeInt( num_events );

    dos.writeFloat( x_min );
    dos.writeFloat( x_max );
    dos.writeInt( n_x );

    dos.writeFloat( y_min );
    dos.writeFloat( y_max );
    dos.writeInt( n_y );

    dos.writeFloat( z_min );
    dos.writeFloat( z_max );
    dos.writeInt( n_z );

    byte[] bytes = new byte[8*num_events];
    int index = 0;
    float val; 
    int   counts;
    for ( int i = 0; i < num_events; i++ )
    {
       val = (float)events.eventX(i);
       fill_bytes( x_binner.index(val), bytes, index );
       index += 2;

       val = (float)events.eventY(i);
       fill_bytes( y_binner.index(val), bytes, index );
       index += 2;

       val = (float)events.eventZ(i);
       fill_bytes( z_binner.index(val), bytes, index );
       index += 2;

       counts = events.eventCode(i);
       fill_bytes( counts, bytes, index );
       index += 2;
    }

    dos.write( bytes, 0, bytes.length );
    dos.close();

    System.out.println("FILE WRITTEN");
    System.out.println("WRITE TIME = " + (System.nanoTime()-start)/1000000 );
    System.out.println("TOTAL EVENTS = " + num_events );
  }
  

  /**
   * Pack the specified integer value into a sequence of two
   * bytes in the byte array buffer.  The integer value must
   * be between -32768 and 32767 for this to work properly.
   * 
   * @param val    The integer value to be encoded in the buffer
   * @param array  Array of bytes into which the value will be
   *               stored.
   * @param index  The position at which the low order byte will
   *               be stored.  The high order byte is stored at 
   *               position index+1.
   */
  static private void fill_bytes( int val, byte[] array, int index )
  {
    array[index] = (byte)(val & 255);
    index++;
    val = val/256;
    array[index] = (byte)(val & 255);
  }

  
  /**
   * Basic test of this class by reading in a file of events
   * and printing information about them.
   * 
   * @param args  The first command line argument must be the
   *              name of the file of events to read.
   * @throws IOException
   */
  public static void main( String args[] ) throws IOException
  {
    System.out.println("Opening " + args[0] );

    long start = System.nanoTime();

    IEventList3D events = new ByteFile16EventList3D( args[0] );

    long end = System.nanoTime();
    System.out.println("Time to load event list = " + (end-start)/1000000 );

    System.out.println("number of events = " + events.numEntries() );
    double[] vals = new double[3];
    int      code;
    for ( int i = 0; i < 10; i ++ )
    {
      events.eventVals( i, vals );
      code = events.eventCode( i );

      System.out.printf( "%12.7f %12.7f %12.7f %6d\n",
                          vals[0], vals[1], vals[2], code );
    }

    double sum = 0;
    start = System.nanoTime();
    int n_events = events.numEntries();
    for ( int i = 0; i < n_events; i ++ )
    {
      events.eventVals( i, vals );
      sum += vals[0] + vals[1] + vals[2];
      code = events.eventCode( i );
    }
    end = System.nanoTime();
    System.out.println("Time to get event array = " + (end-start)/1000000 );
    System.out.println("Sum = " + sum );

    double x, y, z;
    sum = 0;
    start = System.nanoTime();
    n_events = events.numEntries();
    for ( int i = 0; i < n_events; i ++ )
    {
      x = events.eventX( i );
      y = events.eventY( i );
      z = events.eventZ( i );
      sum += x+y+z;
      code = events.eventCode( i );
    }
    end = System.nanoTime();
    System.out.println("Time to get all events = " + (end-start)/1000000 );
    System.out.println("Sum = " + sum );
  }

}
