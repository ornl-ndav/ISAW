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
 *  Revision 1.8  2002/11/27 23:27:59  pfpeterson
 *  standardized header
 *
 *  Revision 1.7  2002/04/08 15:35:11  dennis
 *  Increased connect time out period from 20 sec to 60 sec.
 *
 */
package NetComm;

import java.net.*;
import java.lang.*;
import java.io.*;

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
      final int USE_TIMEOUT_MS     = 0;       // Allow 60 seconds to make the
      final int CONNECT_TIMEOUT_MS = 60000;   // connection, but once it is
                                              // made, let it be infinte.
                                              // ( zero is interpreted as 
                                              //   timeout = infinity )
      boolean         connection_made = false;
      Socket          sock = null;
      ThreadedTCPComm tcp_io;

      while ( true )
      {
        try
        {
          connection_made = false;
          sock   = ssock.accept();
          tcp_io = new ThreadedTCPComm( sock, CONNECT_TIMEOUT_MS, user );
          connection_made = true;
        }
        catch( StreamCorruptedException e )
        {
          System.out.println("StreamCorruptedException reading Object " +
                             "in TCPServiceInit.Run() " + e );
          e.printStackTrace();
        }
        catch( InterruptedIOException e )        // i.e. connection timed out
        {
          System.out.println("InterruptedIOException reading Object " +
                             "in TCPServiceInit.Run() " + e );
          e.printStackTrace();
        }
        catch ( Exception e )
        {
          System.out.println( "Error setting up ThreadedTCPComm " + e );
          e.printStackTrace();
          System.exit(1);
        }

        try
        {
          if ( connection_made )           
            sock.setSoTimeout( USE_TIMEOUT_MS );
        }
        catch ( SocketException e )
        {
           System.out.println("SocketException in TCPServiceInit.Run()");
           e.printStackTrace();
        }

        sock = null; 
        tcp_io = null;
      }
    }
  }

