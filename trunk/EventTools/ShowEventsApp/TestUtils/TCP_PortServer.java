/* 
 * File: TCP_PortServer.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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
 *           Departmet of Mathematics, Statistics and Computer Science
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


package EventTools.ShowEventsApp.TestUtils;

import java.net.*;
import java.io.*;

/**
 *  This class will listen for a TCP connection request on a specified
 *  port, and respond with an unsigned short int specifying which UDP port
 *  IsawEV should receive raw UDP event packets.  This is useful for testing
 *  the interface with Jim Kohls event catcher (live data server) for 
 *  getting the port on which events will be relayed to IsawEV.
 */
public class TCP_PortServer
{

  /**
   *  This program runs a TCP socket server to listen for IsawEV.  When 
   *  IsawEV connects to the socket server, a TCP connection on a specified
   *  port is made.  The server then immediately sends out a short int
   *  specifying which port IsawEV should use to get live events.
   *
   *  @param args  An array of Strings specifying which port should be 
   *               used for the TCP connection in args[0] and which 
   *               port should be used for the UDP connection args[1].
   *               These values will default to 9000 for TCP and 9022
   *               for UDP, if they are not listed.
   */
  public static void main( String args[] ) throws Exception
  {
    if ( args.length >= 1 &&
         args[0].endsWith("help") )
    {
      System.out.println("Enter TCP port and UDP port numbers on command line");
      System.exit(1);
    }

    int tcp_port = 9000;
    if ( args.length > 0 )
      tcp_port = Integer.parseInt(args[0].trim());
    System.out.println("Making TCP SocketServer on port " + tcp_port );

    int udp_port = 9022;
    if ( args.length > 1 )
      udp_port = Integer.parseInt(args[1].trim());
 
    ServerSocket servsock = new ServerSocket( tcp_port );
    servsock.setSoTimeout( 300000 );

    Socket sock = null;
    boolean listening = true;
    while ( listening )
    {
      System.out.println("Listening for Client on " + tcp_port );
      try
      {
        sock = servsock.accept();
        sock.setSoLinger( false, 0 );
        sock.setKeepAlive( false );
      }
      catch ( IOException ex )
      {
        System.out.println( ex );
      }

      OutputStream os = sock.getOutputStream();
      os.write( udp_port & 0xFF );
      os.write( (udp_port / 256) & 0xFF );

      System.out.println("Sent UDP port " + udp_port + " to IsawEV");

/*                                                         // for some reason
                                                           // this did not work
      System.out.println("Made TCP connection on port " + port );
      while ( !sock.isClosed()         &&
               sock.isConnected()      &&
               sock.isBound()          &&
              !sock.isOutputShutdown() )
      {
        Thread.sleep(10);
        System.out.println("Socket Alive");
      }
      System.out.println("Socket DISCONNECTED");
*/
    }

    sock.close();
    servsock.close();
  }
}

