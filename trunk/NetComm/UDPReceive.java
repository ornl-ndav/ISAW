/*
 * @(#)UDPReceive.java
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2001/04/23 19:44:27  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.1  2001/01/30 23:27:46  dennis
 *  Initial version, network communications for ISAW.
 *
 */

package NetComm;

import java.net.*;
import java.lang.*;
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
      sock=new DatagramSocket(port);
    }
    catch( SocketException se )
    { 
      se.printStackTrace();
      System.exit( 1 );
    }
    this.user = user;
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
	 
    while ( true )
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
