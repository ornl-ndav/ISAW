/*
 * File:  TCPServiceInit.java
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
 *  Revision 1.5  2001/08/10 19:33:07  dennis
 *  Now exits if an exception occurs while setting up the socket.
 *  (This happens if the socket is already in use.)
 *
 *  Revision 1.4  2001/04/23 19:44:22  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.3  2001/02/15 21:25:28  dennis
 *  Changed the time out period to "0" which means an
 *  infinite timeout period
 *
 *  Revision 1.2  2001/01/31 14:19:47  dennis
 *  Made the class public.
 *
 *  Revision 1.1  2001/01/30 23:27:33  dennis
 *  Initial version, network communications for ISAW.
 *
 */
package NetComm;

import java.lang.*;
import java.net.*;

/**
 *  This class is a thread that is used by a server to listen for clients
 *  requesting a TCP connection.  When a request is received on the
 *  ServerSocket, a new Socket is created for the TCP communciations
 *  and a new ThreadedTCPComm object is created to handle I/O for the Socket.
 *  
 *  @see ThreadedTCPComm
 *  @see ITCPUser
 *  @see TCPComm
 *  @see LiveDataServer
 */
  public class TCPServiceInit extends Thread
  {
    ITCPUser     user;
    ServerSocket ssock;

    /**
     *  Construct a new TCPServiceInit object to listen for a TCP connection
     *  request on the specified port.
     *
     *  @param  user   The ITCPUser object whose ProcessData() method will
     *                 be called when 
     *  @param  port   The port on which to listen for TCP connection requests.
     */
    public TCPServiceInit( ITCPUser user, int port )
    {
      this.user = user;
      try
      {
        ssock = new ServerSocket( port );
      } 
      catch ( Exception e )
      {
        System.out.println("ERROR: Couldn't get sever socket"+e);
        ssock = null;
      } 
    }

   /**
    *  The "run" method for this thread.  This is called by the java system
    *  when the thread is started.  When a TCP connection request is received,
    *  it will create a new Socket for the connection and will create a new
    *  ThreadedTCPComm object to handle the communications over that new
    *  socket.  When Objects are received over the new socket, the 
    *  ThreadedTCPComm object will pass them to the ProcessData() method of
    *  ITCPUser object that was passed to the constructor.
    */
    public void run()
    {
      final int TIMEOUT_MS = 0;    // zero is interpreted as timeout = infinity
      while ( true )
      try
      {
        Socket sock = ssock.accept();
        ThreadedTCPComm tcp_io = new ThreadedTCPComm( sock,
                                                      TIMEOUT_MS,
                                                      user );
      }
      catch ( Exception e )
      {
        System.out.println( "Error setting up ThreadedTCPComm " + e );
        e.printStackTrace();
        System.exit(1);
      }
    }
  }

