/*
 * File:  TCPComm.java
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
 *  Revision 1.4  2001/04/23 19:44:17  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.3  2001/03/01 21:03:00  dennis
 *  Added detailed exception checking to the Recieve() method.
 *
 *  Revision 1.2  2001/02/15 22:03:11  dennis
 *  Now handles time outs down to 0 ms, and treats values <= 0 as
 *  requests for an infinite time out period.
 *  Also, now flushes the out streams when writing.
 *
 *  Revision 1.1  2001/01/30 23:27:28  dennis
 *  Initial version, network communications for ISAW.
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
 *  on a particular port.  Using the new Socket, the client would then
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
   *                       socket.  If time_out_ms <= 0, the time out period
   *                       will be infinite.
   */
  
  public TCPComm( Socket sock, int time_out_ms ) throws Exception
  { 
    this.sock       = sock;

    if(time_out_ms > 0)
       this.sock.setSoTimeout( time_out_ms );
    else
       this.sock.setSoTimeout( 0 );            // zero means timeout = infinite

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
      obj_out_stream.flush();
      out_stream.flush();
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
    Object data_obj = null;
    try
    {
      data_obj = obj_in_stream.readObject( );
    }
    catch( ClassNotFoundException e )
    {
      System.out.println("ClassNotFoundException reading Object " +
                         "in TCPComm.Receive() " + e );
      e.printStackTrace();
      FreeResources();                 // if we can't receive, shut it down
      throw( new IOException() );       
    }
    catch( InvalidClassException e )
    {
      System.out.println("InvalidClassException reading Object " +
                         "in TCPComm.Receive() " + e );
      e.printStackTrace();
      FreeResources();                 // if we can't receive, shut it down
      throw( new IOException() );       
    }
    catch( StreamCorruptedException e )
    {
      System.out.println("StreamCorruptedException reading Object " +
                         "in TCPComm.Receive() " + e );
      e.printStackTrace();
      FreeResources();                 // if we can't receive, shut it down
      throw( new IOException() );       
    }
    catch( OptionalDataException e )
    {
      System.out.println("OptionalDataException reading Object " +
                         "in TCPComm.Receive() " + e );
      e.printStackTrace();
      FreeResources();                 // if we can't receive, shut it down
      throw( new IOException() );       
    }
    catch( IOException e )
    {
      System.out.println("IOException reading Object " +
                         "in TCPComm.Receive() " + e );
      e.printStackTrace();
      FreeResources();                 // if we can't receive, shut it down
      throw( new IOException() );       
    }
    catch( Exception e )
    {
      System.out.println("Exception reading Object in TCPComm.Receive() " + e );
      e.printStackTrace();
      FreeResources();                 // if we can't receive, shut it down
      throw( new IOException() );       
    }


    if ( data_obj != null &&
         data_obj instanceof TCPCommExitClass )       // Exit request received
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
