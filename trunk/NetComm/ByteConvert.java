/*
 *  @(#)ByteConvert.java
 *
 *  Class with static methods for packing and unpacking data into a byte array.
 *
 *  Programmer: Dennis Mikkelson
 *
 *  $Log$
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
    int j     = 0;

    while ( j < 4 )
    {
      if ( buffer[ start + j ] >= 0 )
        value = value * 256 + (int)buffer[ start + j ];
      else
        value = value * 256 + (256 + (int)buffer[ start + j ]);
      j++;
    } 

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
      buffer[ start + j ] = (byte)(value % 256);
      value = value / 256;
    }

    return start+4;                       // can be used to step along buffer
  }


  public static void main( String args[] )
  {
    byte buffer[] = new byte[16];

    ByteConvert.toBytes( buffer,  0, 123456789 );
    ByteConvert.toBytes( buffer,  4, 234567891 );
    ByteConvert.toBytes( buffer,  8, 345678912 );
    ByteConvert.toBytes( buffer, 12, 456789123 );
        
    for ( int i = 0; i < 4; i++ )
      System.out.println( " "+toInt( buffer, 4*i ));
  }

} 
