/*
 *  @(#)ByteConvert.java
 *
 *  Class with static methods for packing and unpacking data into a byte array.
 *
 *  Programmer: Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.2  2001/04/20 20:25:11  dennis
 *  Rewrote the conversions using low level logical operations
 *  "<<", "&" and "|".  It will now work correctly for negative
 *  integers as well as for positive integers.
 *
 *  Revision 1.1  2001/01/30 23:27:12  dennis
 *  Initial version, network communications for ISAW.
 *
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
