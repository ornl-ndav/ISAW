/*
 * File:  ByteConvert.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.4  2002/11/27 23:27:59  pfpeterson
 *  standardized header
 *
 */

package NetComm;

/**
 *  This class provides static methods to pack values into a byte
 *  array and to extract values from a byte array, as needed for sending
 *  and receiving UDP packets.  Currently only integer values are packed
 *  and unpacked. 
 *   
 *
 *  @see DASOutputTest
 *  @see LiveDataServer
 */

public final class ByteConvert
{
  /**
   * Don't let anyone instantiate this class.
   */
  private ByteConvert() {}

  /** 
   *  Form an integer value from four bytes starting at a specified location 
   *  in a buffer that is an array of bytes.
   *
   *  @param  buffer  The array of bytes containing an integer starting at
   *                  the specified location.  The buffer should have length
   *                  at least start + 4;
   *
   *  @param  start   The index giving the position of the first byte to 
   *                  use from the buffer.
   *
   *  @return  The integer value stored in 4 bytes in buffer[] beginning with
   *           position "start".  If there are not four bytes in the buffer
   *           at and beyond position "start", this will print an error 
   *           message and return 0.
   */
  public static int toInt( byte buffer[], int start )
  {
    if ( start+3 >= buffer.length )
    {
      System.out.println("ERROR: buffer overflow in ByteConvert.toInt()" );
      return 0;
    }

    int value = 0;

    for ( int j = 0; j < 4; j++ ) 
      value = ( value << 8 ) | ( (int)buffer[ start + j ] & 255 );

      // In Java, bytes are "signed bytes" so they are sign extended when they
      // are converted to type int.  We must mask off the leading 1's that 
      // the jvm quitely adds.

    return value; 
  } 

  /**
   *  Pack an integer value into four bytes starting at a specified location
   *  in a buffer that is an array of bytes.
   *
   *  @param  buffer  The array of bytes into which the integer is to be 
   *                  packed.  The buffer should have length at least equal
   *                  to start + 4;
   *
   *  @param  start   The position in the buffer in which to place the first
   *                  byte of the integer.
   *
   *  @param  value   The integer value to store in the buffer.
   *
   *  @return  The position where the next integer could be added to the buffer.
   *           That is, return start+4.  If there was not room to store the
   *           specified value, just return start.
   */

  public static int toBytes( byte buffer[], int start, int value )
  {
    if ( start+3 >= buffer.length )       // not enough room for the four bytes
    {
      System.out.println("ERROR: buffer overflow in ByteConvert.toBytes()" );
      return start;
    }

    for ( int j = 3; j >= 0; j-- )
    {
      buffer[ start + j ] = (byte)(value & 255);
      value = value >> 8;
    }

    return start+4;                       // can be used to step along buffer
  }


  public static void main( String args[] )
  {
    int  test_ints[] = {  123456789,  234567891,  345678912,  456789123,
                         -123456789, -234567891, -345678912, -456789123,
                                  1,        256,      65536,   16777216,
                                255,      65280,   16711680,  -16777216,  
                                 -1 };

    byte buffer[] = new byte[ 4 * test_ints.length ];

    for ( int i = 0; i < test_ints.length; i++ )           // pack test ints
      ByteConvert.toBytes( buffer,  4*i,  test_ints[i] );  // into buffer

    boolean error = false;
    for ( int i = 0; i < test_ints.length; i++ )
      if ( toInt( buffer, 4*i ) != test_ints[i] )          // unpack buffer and
      {                                                    // compare to 
                                                           // test ints
        error = true;
        System.out.print("Conversion failed: " );
        System.out.println( " "+toInt( buffer, 4*i ) + " != " + test_ints[i] );
      }
 
    if ( !error )
      System.out.println("Conversion worked OK for all test cases!" );
  }

} 
