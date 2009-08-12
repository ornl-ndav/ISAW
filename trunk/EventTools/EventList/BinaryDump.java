package EventTools.EventList;

import java.io.*;

public class BinaryDump
{
  public static void main( String args[] ) throws IOException
  {
    String filename = "/usr2/ARCS_SCD_2/EVENTS/ARCS_1250_neutron_event.dat";
    int    first_byte = 0;
    int    seg_size   = 80;

    RandomAccessFile r_file = new RandomAccessFile( filename, "r" );

    r_file.seek( first_byte );

    byte[] buffer = new byte[seg_size];
    r_file.read( buffer );

    for ( int i = 0; i < buffer.length; i++ )
    {
      if ( i % 8 == 0 )
        System.out.println();

      if ( i % 4 == 0 )
        System.out.print("   : ");

      int byte_1 = buffer[i];
      if ( byte_1 < 0 )
        byte_1 += 256;
      System.out.printf( " %5d" , byte_1 );
    }
    System.out.println();

  }

}
