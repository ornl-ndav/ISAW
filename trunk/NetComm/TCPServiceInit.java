/*
 * @(#)TCPServiceInit.java
 *
 *  Thread used by server to listen for clients requesting a TCP connection. 
 *
 *  Programmers: Ruth Mikkelson,
 *               Alok Chatterjee,
 *               Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.1  2001/01/30 23:27:33  dennis
 *  Initial version, network communications for ISAW.
 *
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
  class TCPServiceInit extends Thread
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
      final int TIMEOUT_MS = 60000;
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
      }
    }
  }

