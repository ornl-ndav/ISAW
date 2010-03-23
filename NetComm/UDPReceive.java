/*
 * File: UDPReceive.java
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
 *  Revision 1.4  2004/03/15 19:39:19  dennis
 *  Removed unused imports after factoring out view components,
 *  math and utilities.
 *
 *  Revision 1.3  2002/11/27 23:27:59  pfpeterson
 *  standardized header
 *
 */

package NetComm;

import java.net.*;
import java.io.*;

/**
 *  Thread that receives a whole UDP packet and calls the ProcessData
 *  routine of its IUDPUser object.
 */
public class  UDPReceive extends Thread
{
  public static final int BUFFER_SIZE = 65536;

  private DatagramSocket sock;
  private IUDPUser       user;  
  private boolean        keep_running;

  /**
   *  Construct a UDPReceive object to listen for UDP packets on the specified
   *  port and IP address, and call the ProcessData routine of the given 
   *  IUDPUser object. 
   *
   *  @param  port   The port on which to listen for UDP packets.
   *  @param  laddr  The local ip address on which to listen for UDP packets.
   *  @param  user   The object whose ProcessData method is called when
   *                 this object receives a UDP packet.
   */
  public UDPReceive( int port, InetAddress laddr, IUDPUser user )
  {
    try
    {
      sock = new DatagramSocket( port, laddr );
    }
    catch( Exception ex )
    {
      ex.printStackTrace();
      System.exit( 1 );
    }
    this.user = user;
    keep_running = true;
  }


  /**
   *  Close the underlying DatagramSocket and exit the running thread
   *  that is receiving packets from that socket.
   */
  public void close()
  {
    keep_running = false;
    sock.close();
  }


  /**
   *  Construct a UDPReceive object to listen for UDP packets on the specified
   *  port and call the ProcessData routine of the given IUDPUser object. 
   *
   *  @param  port   The port on which to listen for UDP packets.
   *  @param  user   The object whose ProcessData method is called when
   *                 this object receives a UDP packet.
   */  
  public UDPReceive( int port, IUDPUser user )
  { 
    try
    {
      sock = new DatagramSocket( port );
    }
    catch( Exception ex )
    { 
//    ex.printStackTrace();
      throw new IllegalArgumentException( "UDP Socket " + port + " failed " + ex );
    }
    this.user = user;
    keep_running = true;
  }


  /**
   * Specify the size of the UDP buffer for sending packets.  NOTE: The
   * underlying OS may not actually allocate a buffer of the specified 
   * size.
   *
   * @param size  The requested size of the send buffer, in bytes.
   */
  public void setReceiveBufferSize( int size ) throws SocketException
  {
    sock.setReceiveBufferSize( size );
  }


  /** 
   *  The "run" method for this thread.  This is called by the java system
   *  when the thread is started.  When a UDP packet is recieved, the 
   *  data from the packet is passed to the ProcessData() method of the 
   *  IUDPUser object that was passed to the constructor.
   */
  public void run()
  { 
    byte           b[];
    DatagramPacket pack=null;
     
    b = new byte[BUFFER_SIZE];
	 
    while ( keep_running )
    { 
      try
      {
        pack= new DatagramPacket( b, BUFFER_SIZE );
        sock.receive( pack );
      }
      catch( IOException e )
      {
        System.out.println("ERROR receiving UDP packet in UDPReceive.run() "+e);
      } 

      user.ProcessData( pack.getData(), pack.getLength() );
    }
  }
}
