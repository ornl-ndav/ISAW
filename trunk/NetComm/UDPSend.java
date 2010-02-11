/*
 * File:  UDPSend.java
 *
 * Copyright (C) 2001, Ruth Mikkelson
 *                     Alok Chatterjee,
 *                     Dennis Mikkelson
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
 *  Revision 1.5  2004/03/15 06:10:58  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.4  2002/11/27 23:27:59  pfpeterson
 *  standardized header
 *
 */

package NetComm;

import java.net.*;

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
    sock.connect( address, port );     // Not available in jdk 1.1.7
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
      pack = new DatagramPacket( data, length );                 // java 1.2&1.3
//    pack = new DatagramPacket( data, length, address, port );  // jdk 1.1.7
      sock.send( pack );
    }
    catch ( Exception e )
    {
      System.out.println("ERROR: can't send UDP packet to " +pack.getAddress());
    }
  }

  /**
   *  Make sure the socket gets closed when this object is no longer used.
   */
  protected void finalize() throws Throwable 
  {
    try 
    {
      sock.disconnect();
      if ( !sock.isClosed() )
        sock.close(); 
    } 
    finally 
    {
      super.finalize();
    }
  }

}
