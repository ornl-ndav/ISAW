/*
 * @(#)UDPSend.java
 *
 *  Programmers: Ruth Mikkelson,
 *               Alok Chatterjee,
 *               Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.2  2001/03/01 21:01:34  dennis
 *  Modified to work with jdk1.1.7.  To do this, the remote machine
 *  address and port number are saved a placed in each UDP packet,
 *  rather than using the Java 2 connect() method on a socket.
 *
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
  private InetAddress    address;        // for jdk 1.1.7, we need to record
  private int            port;           // address and port and send them
                                         // with each packet.
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
    address = InetAddress.getByName( host );   
    this.port = port;

    sock = new DatagramSocket();
//    sock.connect( address, port );     // Not available in jdk 1.1.7
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
//    pack = new DatagramPacket( data, length );                 // java 1.2&1.3
      pack = new DatagramPacket( data, length, address, port );  // jdk 1.1.7
      sock.send( pack );
    }
    catch ( Exception e )
    {
      System.out.println("ERROR: can't send UDP packet to " +pack.getAddress());
    }
  }
}
