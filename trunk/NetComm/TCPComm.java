/*
 * @(#)TCPComm.java
 *
 *  Handles sending and receiving Java Objects via TCP. 
 *
 *  Programmers: Ruth Mikkelson,
 *               Alok Chatterjee,
 *               Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.1  2001/01/30 23:27:28  dennis
 *  Initial version, network communications for ISAW.
 *
 *
 */
package NetComm;

import java.net.*;
import java.lang.*;
import java.io.*;

/**
 *  Creates Object I/O streams, given a TCP socket, and handles sending
 *  and receiving Java Objects via TCP.  To use this for a client program,
 *  the client would first construct a new Socket to connect with a server
 *  on a particular port.  Using the new Socket, the client would the
 *  construct a new TCPComm object.  Subsequently, the client can use
 *  the Send() method to send an object to the server and it can use the
 *  Receive() method to get an object from the server.
 *
 *  @see  ThreadedTCPComm
 *  @see  DataSetTools.retriever.LiveDataRetriever 
 */
public class TCPComm
{
  protected Socket             sock;
  private   InputStream        in_stream;
  protected ObjectInputStream  obj_in_stream;

  private   OutputStream       out_stream;
  protected ObjectOutputStream obj_out_stream;

  /**
   *  Create a TCPComm object to handle sending and receiving objects via
   *  TCP.  This creates Object input/output streams associated with the 
   *  specified socket.  Set the timeout period to the specified number 
   *  of milliseconds.
   *
   *  @param  sock         The socket to use 
   *  
   *  @param  time_out_ms  The time out period for communications on this
   *                       socket.
   */
  
  public TCPComm( Socket sock, int time_out_ms ) throws Exception
  { 
    this.sock       = sock;
    if(time_out_ms >= 10)
       this.sock.setSoTimeout( time_out_ms );
    in_stream       = sock.getInputStream();
    out_stream      = sock.getOutputStream();

    obj_out_stream  = new ObjectOutputStream( out_stream );
    obj_in_stream   = new ObjectInputStream( in_stream );
  }
 
  /**
   *  Send an object to a remote machine via a TCP connection.
   *
   *  @param  data_obj  The object to send.  The object (and any member 
   *                    objects) must be serializable.  The object will be
   *                    written to the ObjectOutputStream associated with
   *                    the TCP connection.  
   */
  public void Send( Object data_obj ) throws IOException
  {
    try
    {
      obj_out_stream.writeObject( data_obj );
      if ( data_obj instanceof TCPCommExitClass )       // Exit request, so 
      {                                                 // shutdown connection
        System.out.println("'Exit' sent, shutting down TCP connection");
        FreeResources();
      }
    }
    catch( Exception e )                      // if we can't send, shut it down
    {
      System.out.println("Exception in TCPComm.Send is : " + e );
      FreeResources();
      throw( new IOException() );
    }
  }
 
  /**
   *  Receive an object from a remote machine via a TCP connection.
   *
   *  @return  The object read from the ObjectInputStream associated with
   *           the TCP connection.
   */        
  public Object Receive() throws IOException
  { 
    Object data_obj;
    try
    {
      data_obj = obj_in_stream.readObject( );
    }
    catch( Exception e )                   // if we can't receive, shut it down
    { 
      System.out.println("Exception in TCPComm.Receive is : " + e );
      FreeResources();
      throw( new IOException() );
    };

    if ( data_obj instanceof TCPCommExitClass )       // Exit request received
    {                                                 // so shutdown connection
      System.out.println("'Exit' received, shutting down TCP connection");
      FreeResources();
    }

    return data_obj; 
  }


  /**
   *  Close down the input and output streams, and the socket, associated 
   *  with this TCP connection.  This method is called from finalize, when
   *  the TCPComm object is garbage collected.
   */
  protected void FreeResources()
  {
    if( obj_in_stream != null )                     // close the obj_in_stream
    try
    {
      obj_in_stream.close();
    }
    catch(Exception s)
    {
      System.out.println("Exception in TCPComm.FreeResources(), " +
                         " can't close obj_in_stream:" + s ); 
    }
    obj_in_stream = null;
	
    if( obj_out_stream != null )                   // close the obj_out_stream
    try
    {
      obj_out_stream.close();
    }
    catch(Exception s)
    {
      System.out.println("Exception in TCPComm.FreeResources(), " +
                         " can't close obj_out_stream:" + s );
    }
    obj_out_stream = null;
       
    if( in_stream != null )                        // close the raw in_stream
    try
    {
      in_stream.close();
    }
    catch(Exception s)
    {
      System.out.println("Exception in TCPComm.FreeResources(), " +
                         " can't close in_stream:" + s );
    }
    in_stream = null;

    if( out_stream != null )                       // close the raw out_stream
    try
    {
      out_stream.close();       
    }
    catch(Exception s)
    {
      System.out.println("Exception in TCPComm.FreeResources(), " +
                         " can't close out_stream:" + s );
     }
    out_stream = null;
      
    if( sock != null )                             // close the socket
    try
    {
      sock.close();
    }
    catch(Exception s)
    {
      System.out.println("Exception in TCPComm.FreeResources(), " +
                         " can't close socket:" + s );
    }
    sock = null;
  }

  /**
   *  This finalize routine first calls FreeResources() to free up the streams
   *  and sockets used by the TCPComm object, then calls the super classes 
   *  finalize. 
   */
  public void finalize() throws Throwable 
  {
    FreeResources();
    super.finalize();
  }
}
