/*
 * @(#)ThreadedTCPComm.java
 *
 *  Contains a thread for receiving Java Objects via TCP.
 *
 *  Programmers: Ruth Mikkelson,
 *               Alok Chatterjee,
 *               Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.1  2001/01/30 23:27:40  dennis
 *  Initial version, network communications for ISAW.
 *
 *
 */
package NetComm;

import java.net.*;
import java.lang.*;
import java.io.*;
import NetComm.*;

/**
 *  Class that contains a thread that can receive an object via TCP and 
 *  call the ProcessData routine of it's TCPUser.
 *
 *  @see TCPComm
 *  @see TCPServiceInit
 *  @see LiveDataServer
 */

public class ThreadedTCPComm extends TCPComm
{
  private ITCPUser        user;  
  private ThreadedTCPComm this_object;
  private TCP_thread      thread;
  
  /**
   *  Construct a ThreadedTCPComm object to read objects from the specified
   *  socket and call the ProcessData routine of the given ITCPUser object.
   *  Set the timeout period to the specified number of milliseconds.
   *
   *  @param  socket      The socket to use 
   *  @param  timeout_ms  The time out period for communications on this
   *                      socket.
   *  @param  user        The object whose ProcessData() method is called
   *                      when an object is received on the specified socket. 
   */

  public ThreadedTCPComm( Socket sock, int timeout_ms, ITCPUser user ) 
                                                               throws Exception
  { 
    super( sock, timeout_ms );
    this.user   = user;
    this_object = this;

    thread = new TCP_thread();
    thread.start();
  }

  /**
   *  Private thread to read objects from the object input stream.
   */
  private class TCP_thread extends Thread
  { 
   /**
    * Keep reading input of objects from the socket and passing the objects
    * on to the ProcessData() method of the user, until an object
    * that is an instance of the TCPCommExitClass is received
    */ 
    public void run()
    { 
      Object data_obj = null;

      try
      {
        while ( true )
        {
          data_obj = obj_in_stream.readObject( );
       
          user.ProcessData( data_obj, this_object );

          if ( data_obj instanceof TCPCommExitClass ) 
          {
            System.out.println("'Exit' received in TCP_thread.run() ");
            break;
          }
        }
      }
      catch( Exception e )
      {
        System.out.println("ERROR reading Object in TCP_thread.run() " + e ); 
      }
                           // if we get here, either the attempt to read an
                           // object failed, or we received an "exit" object.  

      System.out.println("Shutting down TCP connection");
      thread = null;
      FreeResources();
    }
  }
}
