/*
 * @(#)IUDPUser.java
 *
 *  Programmers: Dennis Mikkelson,
 *               Ruth Mikkelson,
 *               Alok Chatterjee
 *
 *  $Log$
 *  Revision 1.1  2001/01/30 23:27:22  dennis
 *  Initial version, network communications for ISAW.
 *
 *
 */

package NetComm;

/**
 *  The IUDPUser interface provides the interface that any user of the
 *  UDPReceive class must implement.  A class that is to receive UDP messages
 *  should implement this interface.  It must also create a new UDPReceive
 *  object, passing the port number it wants to listen to and itself 
 *  to the constructor for the UDPReceive object.  The UDPReceive object
 *  is a thread whose constructor opens the UPD socket.  After constructing
 *  the UDPReceive object, it must be started.  Subsequently, when the
 *  UDPReceive thread receives a UDP packet it will call the ProcessData
 *  method.
 *
 *  @see  UDPReceive
 *  @see  UDPSend
 *  @see  LiveDataServer
 */   
 
public interface IUDPUser
{
  /**
   *  The ProcessData method is called by the UDPReceive object when it 
   *  receives a UDP packet.
   *
   *  @param  data    Contains the data bytes from the UDP packet, stored
   *                  beginning with position 0.
   *
   *  @param  length  Contains the number of data bytes in the data array.
   *
   */
  void ProcessData( byte data[], int length );
}
