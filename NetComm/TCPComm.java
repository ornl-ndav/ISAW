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
 *  Revision 1.11  2003/02/21 18:37:15  dennis
 *  Now "automatically" sends and receives DataSets in compressed form.
 *
 *  Revision 1.10  2003/02/21 13:13:46  dennis
 *  Java keeps a hashtable of objects sent over the tcp/ip connection
 *  through the output stream.  This allows them to only send a "handle"
 *  for an object if the object has already been sent.  However, since
 *  our DataSets are mutable, this causes problems since only the
 *  initial form of the DataSet is sent.  Previously we worked around
 *  the problem by making and sending clones of the objects.  This
 *  modification adds a Reset() method to TCPComm that calls the
 *  underlying output stream's reset method.  Calling Reset() before
 *  sending the modified DataSet lets us send it without first
 *  cloning it.
 *
 *  Revision 1.9  2002/11/27 23:27:59  pfpeterson
 *  standardized header
 *
 */

package NetComm;

import java.net.*;
import java.lang.*;
import java.io.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;

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
  public static boolean        debug_tcp_comm = false;

  protected Socket             sock;
  private   String             last_address_string = "";
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
    this.sock           = sock;
    last_address_string = getInetAddressString();

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
   *  Get the address of the remote machine connected to the socket of this
   *  TCPComm object.
   *
   *  @return  The internet address of the remote machine, if there is one,
   *           or null if there is not.
   */
  public InetAddress getInetAddress()
  {
    if ( sock != null )
      return sock.getInetAddress();

    return null;
  }

  /**
   *  Get the String containing the address of the last remote machine that
   *  connected to the socket of this TCPComm object.
   *
   *  @return  String form of the last remote machine, if there was one,
   *           or an empty String if there was not.
   */
  public String getInetAddressString()
  {
    if ( sock != null )
      return sock.getInetAddress().toString();

    return last_address_string;
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
      if ( data_obj instanceof DataSet )    // use CompressedDataSet instead
      {
        CompressedDataSet comp_ds = new CompressedDataSet( (DataSet)data_obj );
        data_obj = comp_ds;
      }

      obj_out_stream.writeObject( data_obj );
      obj_out_stream.flush();
      out_stream.flush();
      if ( data_obj instanceof TCPCommExitClass )       // Exit request, so 
      {                                                 // shutdown connection
        if ( debug_tcp_comm )
          System.out.println("'Exit' sent, shutting down TCP connection");
        FreeResources();
      }
    }
    catch( Exception e )                      // if we can't send, shut it down
    {
      if ( debug_tcp_comm )
        System.out.println("Exception in TCPComm.Send is : " + e );
      FreeResources();
      throw( new IOException() );
    }
  }

  /** 
   *  Reset the object output stream, so that the hashtable of objects sent
   *  is cleared and subsequent requests to send objects will actually send
   *  the object, rather than just sending the object "handle".
   */
  public void Reset()
  {
    try
    {
      if ( obj_out_stream != null )
        obj_out_stream.reset();
    }
    catch ( Exception e )
    {
      System.out.println("reset of object out stream threw exception " + e );
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
      if ( data_obj instanceof CompressedDataSet )            // expand it 
        data_obj = ((CompressedDataSet)data_obj).getDataSet();    
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
      if ( debug_tcp_comm )
        System.out.println("BROKEN CONNECTION");
      data_obj = new TCPCommExitClass();
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
      if ( debug_tcp_comm )
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
