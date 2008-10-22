/* 
 * File: BinaryPeakCode.java
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

/**
 * This class has static methods to encode and decode value, row, col and
 * channel information from one histogram bin into a single long integer.
 * The encoding is done so that the value is placed in the leading bits of
 * the long.  Consequently, if an array of such longs is sorted using 
 * Arrays.sort(), they are sorted in order of increasing value. 
 * The largest value, row, col and channel that can be encoded are specified
 * by public static final long fields in the class.  
 */
public class BinaryPeakCode 
{

/*
  public static final long MAX_CODEABLE_VALUE = Integer.MAX_VALUE;
  public static final long MAX_NUM_ROWS = 256;
  public static final long MAX_NUM_COLS = 256;
  public static final long MAX_NUM_CHAN = 65536;
                                            // code values in form
                                            //      value (31 bits)  2 billion
                                            //      row   ( 8 bits)  256
                                            //      col   ( 8 bits)  256
                                            //      chan  (16 bits)  65536
*/
  public static final long MAX_CODEABLE_VALUE = Integer.MAX_VALUE/2;
  public static final long MAX_NUM_ROWS = 256*2;
  public static final long MAX_NUM_COLS = 256*2;
  public static final long MAX_NUM_CHAN = 65536/2;
                                            // code values in form
                                            //      value (30 bits)  1 billion
                                            //      row   ( 9 bits)  512
                                            //      col   ( 9 bits)  512
                                            //      chan  (15 bits)  32768


  /**
   *  Encode value, row, col and channel information for one histogram bin
   *  into a single long.  The information is encoded in a form that will
   *  allow sorting on the value, using the Arrays.sort() method applied to
   *  a list of encoded values.
   *
   *  @param code  A long that encodes the value, row, col and channel
   *               for a histogram bin.
   * 
   *  @return an array with four ints containing the channel, column, row
   *          and value in that order. 
   */
  public static long Encode( int val, int row, int col, int chan )
  {
    long code;
    if ( val > MAX_CODEABLE_VALUE )
      code = MAX_CODEABLE_VALUE;
    else
      code = val;

    code = code * MAX_NUM_ROWS + row;
    code = code * MAX_NUM_COLS + col;
    code = code * MAX_NUM_CHAN + chan; 

    return code;
  }


  /**
   *  Decode a long to extract the value, row, col and channel information.
   *
   *  @param code  A long that encodes the value, row, col and channel
   *               for a histogram bin.
   *
   *  @return an array with four ints containing the channel, column, row
   *          and value in that order. 
   */
  public static int[] Decode( long code )
  {
    int info[] = new int[4];
    return Decode( code, info );
  }


  /**
   *  Decode a long to extract the value, row, col and channel information.
   *  This form accepts an array to hold the decoded information, so that
   *  small arrays don't have to be repeatedly allocated and deallocated
   *  if many values must be decoded.
   *
   *  @param code  A long that encodes the value, row, col and channel
   *               for a histogram bin.
   *  @param info  An array of ints of length >= four, that will be filled
   *               out with the information
   * 
   *  @return an array with four ints containing the channel, column, row
   *          and value in that order. 
   */
  public static int[]  Decode( long code, int[] info )
  {
    info[0] = (int)( code % MAX_NUM_CHAN );
    code /= MAX_NUM_CHAN;

    info[1] = (int)( code % MAX_NUM_COLS );
    code /= MAX_NUM_COLS;

    info[2] = (int)( code % MAX_NUM_ROWS );
    code /= MAX_NUM_ROWS;

    info[3] = (int)code;

    return info;
  }


  /**
   *  Get a string form of the value, row, col and channel information
   *  that is encoded by the specified long code word.
   *
   *  @param code  A long that encodes the value, row, col and channel
   *               for a histogram bin.
   */
  public static String codeToString( long code )
  {
    int[] info = Decode( code );
    
    String str = "  " + info[2];
    str += "  " + info[1];
    str += "  " + info[0];
    str += "  " + info[3];
  
    return str;
  }


  /**
   *  Basic test of encode and decode methods.
   */
  public static void main( String args[] )
  {
    int row   = 123;
    int col   = 234;
    int chan  = 345;
    int value = 111111;

    System.out.println("Values to encode: ");
    System.out.println("row   = " + row );
    System.out.println("col   = " + col );
    System.out.println("chan  = " + chan );
    System.out.println("value = " + value );
    System.out.println();
 
    long code = Encode( value, row, col, chan );
    System.out.println("CODED AS : " + code );
    System.out.println();

    int[] vals = Decode( code );
    System.out.println( "Decoded array values:" );
    for ( int i = 0; i < vals.length; i++ )
     System.out.println( vals[i] );
    System.out.println();

    System.out.println("Result of codeToString");
    System.out.println( codeToString( code ) );
  }

}
