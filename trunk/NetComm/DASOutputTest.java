/*
 * File: DASOutputTest.java
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
 *  Revision 1.14  2002/11/29 23:04:57  dennis
 *  Now forms file name from instrument name and run number using
 *  InstrumentType.formIPNSFileName().  Reduced delay between sending
 *  spectra to 10ms every 10 spectra, to send scd data more quickly.
 *
 *  Revision 1.13  2002/11/27 23:27:59  pfpeterson
 *  standardized header
 *
 *  Revision 1.12  2002/07/24 23:16:20  dennis
 *  Default INSTRUMENT_COMPUTER is now loopback: 127.0.0.1
 *
 *  Revision 1.11  2001/12/10 22:05:06  dennis
 *  Now will pad run numbers with leading zeros, to length 4 when
 *  forming the file name string.
 *
 */
package NetComm;

import java.io.*;
import java.net.*;
import NetComm.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.instruments.*;

/**
 *  This class simulates the output of the DAS at IPNS to the LiveDataServer
 *  for IPNS instruments.  It first loads a DataSet and then repeatedly sends
 *  portions of the data packed into byte arrays.  The amplitudes of the 
 *  spectra are gradually increased as the process continues.  The 
 *  LiveDataServer must be initialized from the same IPNS runfile as this
 *  simulator.  
 *
 *  @see UDPSend
 *  @see ByteConvert 
 */  
public class DASOutputTest
{
  public static final int    DEFAULT_DAS_UDP_PORT = 6080;
  public static final String INSTRUMENT_COMPUTER  = "127.0.0.1";
//public static final String INSTRUMENT_COMPUTER  = "dmikk.mscs.uwstout.edu";
//public static final String INSTRUMENT_COMPUTER  = "mscs138.mscs.uwstout.edu";
//public static final String INSTRUMENT_COMPUTER  = "mandrake.pns.anl.gov";

  byte    buffer[]    = new byte[ 65536 ];
  String  file_name   = null;
  byte    inst_name[] = null;
  int     run_number  = -1;

  /**
   *   Send one array of y_values, scaled by a scale factor.  The array
   *   of y_values is converted to a sequence of bytes and sent in the sequence:
   *   MAGIC_NUMBER ( 4 bytes )
   *   LENGTH OF INSTRUMENT NAME (1 byte)
   *   INSTRUMENT NAME ( typically 3-4 bytes )
   *   RUN NUMBER ( 4 bytes )
   *   GROUP ID ( 4 bytes )
   *   STARTING CHANNEL INDEX ( 4 bytes )
   *   NUMBER OF CHANNELS ( 4 bytes )
   *   LIST OF CHANNEL Y_VALUES ( N * 4 bytes ) 
   */
  public void SendSpectrum( UDPSend sender, 
                            int     id, 
                            int     scale_factor, 
                            float   y[] )
  {
    int  size     = 0;
    int  value    = 0;

    size = ByteConvert.toBytes( buffer, size, LiveDataServer.MAGIC_NUMBER );

                                                    // pack the instrument name
                                                    // into buffer, starting
                                                    // with one byte giving
                                                    // the inst name length.
    buffer[ size ] = (byte)inst_name.length;
    size++;
    for ( int i = 0; i < inst_name.length; i++ )
      buffer[size + i] = inst_name[i];
    size += inst_name.length;
                                                    // pack the run number into
                                                    // buffer.
    size = ByteConvert.toBytes( buffer, size, run_number );

                                                     // pack the id, starting
                                                     // channel and number of
                                                     // channels into buffer. 
    size = ByteConvert.toBytes( buffer, size, id );
    size = ByteConvert.toBytes( buffer, size, 0 );
    size = ByteConvert.toBytes( buffer, size, y.length );
                                              
                                                     // pack the spectrum 
                                                     // into buffer
    for ( int i = 0; i < y.length; i++ )
    {
      value = (int)(scale_factor * y[i]);
      size = ByteConvert.toBytes( buffer, size, value );
    }
    sender.send( buffer, size );  
  }

  /**
   *   Send the Data blocks from the DataSet as complete histograms, sleeping
   *   between sending each Data block.
   */ 
  public void SendDataBlocks( UDPSend sender, DataSet ds, int counter )
  {
    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      Data d  = ds.getData_entry(i);
      int  id = d.getGroup_ID();

      SendSpectrum( sender, id, (counter+1), d.getCopyOfY_values() );
      if ( i % 10 == 0 )                           // pause every 10th spectrum
      {
        try  { Thread.sleep(10); }
        catch(Exception e){ System.out.println("sleep Exception"); } 
      }
    }  
    System.gc();
  }

  /**
   *  The test's main program loads the histogram and monitor DataSets for 
   *  a specified runfile, then repeatedly sends the spectra from the 
   *  DataSets.
   */
  public static void main( String args[] )
  { 
    System.out.println("=================================================");
    System.out.println("Starting DAS simulator compiled to send to "
                        + INSTRUMENT_COMPUTER );
    System.out.println("=================================================");
 
    if ( args.length <= 0 )
    { 
      System.out.println("ERROR: you must specifiy a runfile to use for");
      System.out.println("       testing.  The runfile will be sent to "); 
      System.out.println("       the LiveDataServer in the form of a ");
      System.out.println("       abbreviated instrument name and an integer ");
      System.out.println("       run number." );
      System.out.println("       Try 'java NetComm.DASOutputTest  HRCS  2447'");
      System.out.println("NOTE:  You can also specify a third parameter");
      System.out.println("       giving the UDP port number to use");

      System.exit(1);
    } 
                                                // make an output channel for
                                                // for UDP packets on the port
    UDPSend sender = null;
    try
    {
      if ( args.length >= 3 )
      {
        int port_num = Integer.parseInt( args[2] );
        sender = new UDPSend( INSTRUMENT_COMPUTER, port_num );
      }
      else
        sender = new UDPSend( INSTRUMENT_COMPUTER, DEFAULT_DAS_UDP_PORT );
    }
    catch(Exception u)
    {
      System.out.println("ERROR starting DASOutputTest: " + u);
      System.exit(1);
    }


    DASOutputTest test = new DASOutputTest();

    test.inst_name  = args[0].getBytes();
    test.run_number = Integer.parseInt( args[1] );

    if ( test.run_number <= 0 )
    {
      System.out.println("Sending message with run number 0");
      test.SendSpectrum( sender, 0, 1, new float[0] );
      System.exit( 0 );
    }                                                    // pad run_number with

   test.file_name = InstrumentType.formIPNSFileName( args[0],
                                                     test.run_number );

    System.out.println( "Sending data for run " + test.file_name );

    RunfileRetriever rr = new RunfileRetriever( test.file_name );
    if ( rr == null )
    {
      System.out.println("ERROR: runfile not found: " + test.file_name );
      System.exit(1);
    }

    DataSet data_sets[] = new DataSet[ rr.numDataSets() ];
    for ( int i = 0; i < data_sets.length; i++ )
      data_sets[i] = rr.getDataSet(i);

    rr = null;

    System.out.println("USING FILE: " + test.file_name );
    System.out.println("NUM DataSets = " + data_sets.length );

    int count = 0;
    while ( true )
    {
      System.out.println("Sent block " + count );
      count++;
 
      for ( int i = 0; i < data_sets.length; i++ )         // send each DataSet
        test.SendDataBlocks( sender, data_sets[i], count );

      try  { Thread.sleep(500); }
      catch(Exception e){ System.out.println("sleep Exception"); } 
    }
    
  }

}
