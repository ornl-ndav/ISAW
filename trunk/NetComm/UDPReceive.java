/*
 * @(#)UDPReceive.java
 *
 *  Thread for receiving UDP packets.
 *
 *  Programmers: Ruth Mikkelson,
 *               Alok Chatterjee,
 *               Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.1  2001/01/30 23:27:46  dennis
 *  Initial version, network communications for ISAW.
 *
 *
 */

package NetComm;

import java.net.*;
import java.lang.*;
import java.io.*;

/**
 *  Thread that receives a whole UDP packet and calls the ProcessData
 *  routine of it IUDPUser object.
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
