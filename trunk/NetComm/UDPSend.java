/*
 * @(#)UDPSend.java
 *
 *  Programmers: Ruth Mikkelson,
 *               Alok Chatterjee,
 *               Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.1  2001/01/30 23:27:53  dennis
 *  Initial version, network communications for ISAW.
 *
 *
 */

package NetComm;

import java.net.*;
import java.lang.*;
import java.io.*;

/**
 *  A UDPSend class object handles the sending of UPD packets to a 
 *  specific host and socket.
 */
public class UDPSend
{
  private DatagramSocket sock;
  
  /**
   *  Construct a UPDSend object given the destination host and the port
   *  number to use when data is to be sent.
   *
   *  @param  host   IP address or fully qualified host name to which the
   *                 UDP packets are to be sent.
   *
   *  @param  port   The port number to use.
   */
  public UDPSend( String host, int port ) throws UnknownHostException,
                                                 SocketException
  { 
    InetAddress address = InetAddress.getByName( host );   
    sock = new DatagramSocket();
    sock.connect( address, port );
  }
 
  /**
   *   Make a UDP packet from the specified data[] and send it to the host
   *   and port that were specified when this UDPSend object was constructed.
   *
   *   @param  data   byte array containing the bytes to be sent.  The size
   *                  of this array should not exceed the amount of data that
   *                  can be fit into a UDP packet ( somewhat less than 64K ).
   *
   *   @param  length the number of bytes to use from the array data[], starting
   *                  with the first byte, data[0].
   */
  public void send( byte data[], int length )
  { 
    DatagramPacket pack = null;
    try
    {
      pack = new DatagramPacket( data, length );
      sock.send( pack );
    }
    catch ( Exception e )
    {
      System.out.println("ERROR: can't send UDP packet to " +pack.getAddress());
    }
  }
}
