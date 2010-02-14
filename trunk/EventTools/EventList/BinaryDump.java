
package EventTools.EventList;

import java.io.*;

public class BinaryDump
{

  public static void main( String args[] ) throws IOException
  {
    if ( args.length < 1 )
      System.out.println("Please specify file name as first argument");
    String filename = args[0];
    int    first_byte = 0;

    RandomAccessFile r_file = new RandomAccessFile( filename, "r" );
    int num_bytes = (int)r_file.length();

    r_file.seek( first_byte );

    byte[] buffer = new byte[num_bytes];
    r_file.read( buffer );

    int val    = 0;
    int hits   = 0;
    int misses = 0;
    int MAX_TO_SHOW = 4000;

    for ( int i = 0; i < num_bytes; i++ )
    {
      if ( i % 4 == 0 && i < MAX_TO_SHOW )
        System.out.printf( " i/4 = %5d   ", i/4 );

      int byte_1 = buffer[i];
      if ( byte_1 < 0 )
        byte_1 += 256;

      if ( i < MAX_TO_SHOW )
        System.out.printf( " %4d ", byte_1 );

      if ( (i+1) % 4 == 0 )
      {
        val = SNS_TofEventList.getValue_32( buffer, i-3 );
        if ( i < MAX_TO_SHOW )
          System.out.printf("   : %9d   ", val);
      }

      if ( (i+1) % 4 == 0 && i < MAX_TO_SHOW )
        System.out.println();

      if ( (i+1) % 4 == 0 )
        if ( val == i / 4 )
          hits++;
        else
          misses++;
    }
    System.out.println();
    System.out.println("Hits : " + hits + "   Misses :" + misses );

  }


}
