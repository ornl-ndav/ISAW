/*
 * @(#)DASOutputTest.java
 *
 *  Simple Simulator to replace a DAS live data output.
 *
 *  Programmer:  Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.2  2001/02/02 21:00:51  dennis
 *  Now sends the instrument name and run number with each UPD packet.
 *  To run this test, specify the instrument name and run number
 *  as two separate arguments on the command line.
 *
 *  Revision 1.1  2001/01/30 23:27:17  dennis
 *  Initial version, network communications for ISAW.
 *
 *
 */
package NetComm;

import java.io.*;
import java.net.*;
import NetComm.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;

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
  public static final int    DAS_UDP_PORT        = 6087;
  public static final String INSTRUMENT_COMPUTER = "dmikk.mscs.uwstout.edu";
//public static final String INSTRUMENT_COMPUTER = "mscs138.mscs.uwstout.edu";
//public static final String INSTRUMENT_COMPUTER = "mandrake.pns.anl.gov";

  byte    buffer[]    = new byte[ 65536 ];
  String  file_name   = null;
  byte    inst_name[] = null;
  int     run_number  = -1;

  /**
   *   Send one array of y_values, scaled by a scale factor.  The array
   *   of y_values is converted to a sequence of bytes and sent in the sequence:
   *   MAGIC_NUMBER
   *   ID
   *   STARTING CHANNEL INDEX
   *   NUMBER OF CHANNELS
   *   LIST OF CHANNEL Y_VALUES
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

      try  { Thread.sleep(30); }
      catch(Exception e){ System.out.println("sleep Exception"); } 
    }  
  }

  /**
   *  The test's main program loads the histogram and monitor DataSets for 
   *  a specified runfile, then repeatedly sends the histograms from the two
   *  DataSets.
   */
  public static void main( String args[] )
  { 
    System.out.println("=================================================");
    System.out.println("Starting DAS simulator compiled to send to "
                        + INSTRUMENT_COMPUTER );
    System.out.println("=================================================");
 
                                                // make an output channel for
                                                // for UDP packets on the port
    UDPSend sender = null;
    try
    {
      sender = new UDPSend( INSTRUMENT_COMPUTER, DAS_UDP_PORT );
    }
    catch(Exception u)
    {
      System.out.println("ERROR starting DASOutputTest: " + u);
      System.exit(1);
    }

    if ( args.length <= 0 )
    { 
      System.out.println("ERROR: you must specifiy a runfile to use for");
      System.out.println("       testing.  The runfile will be sent to "); 
      System.out.println("       the LiveDataServer in the form of a ");
      System.out.println("       abbreviated instrument name and an integer ");
      System.out.println("       run number." );
      System.out.println("       Try 'java NetComm.DASOutputTest  HRCS  2447'");

      System.exit(1);
    } 

    DASOutputTest test = new DASOutputTest();

    test.inst_name  = args[0].getBytes();
    test.run_number = Integer.parseInt( args[1] );

    test.file_name = new String(test.inst_name, 0, test.inst_name.length);
    test.file_name = test.file_name.toUpperCase();
    test.file_name = test.file_name + test.run_number + ".RUN";

    System.out.println( "Sending data for run " + test.file_name );

    RunfileRetriever rr         = new RunfileRetriever( test.file_name );
    DataSet          monitor_ds = rr.getDataSet(0);
    DataSet          hist_ds    = rr.getDataSet(1);
    rr = null;
/*
    ViewManager monitor_vm   = new ViewManager( monitor_ds, IViewManager.IMAGE);
    ViewManager histogram_vm = new ViewManager( hist_ds, IViewManager.IMAGE );
*/
    for ( int i = 0; i < 1000; i++ )
    {
      test.SendDataBlocks( sender, monitor_ds, i );
      test.SendDataBlocks( sender, hist_ds, i );
    }
    
    int SIZE = 640+5;
    byte b[] = new byte[SIZE];
    for(int i=0; i<SIZE; i++) 
    b[i]=(byte)(i % 128 );
    sender.send( b, SIZE );  
  }

}
