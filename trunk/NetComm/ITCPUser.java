/*
 * @(#)ITCPUser.java
 *
 *  Programmers: Dennis Mikkelson,
 *               Ruth Mikkelson,
 *               Alok Chatterjee
 *
 *  $Log$
 *  Revision 1.1  2001/01/30 23:27:19  dennis
 *  Initial version, network communications for ISAW.
 *
 *
 */

package NetComm;

/**
 *  The ITCPUser interface provides the interface that any user of the
 *  ThreadedTCPComm class must implement.  A class that is to receive TCP 
 *  messages asynchronously should implement this interface.  It must also
 *  create an instance of a ThreadedTCPComm object, passing in a TCP socket
 *  and itself to the constructor for the ThreadedTCPComm object.  For a
 *  "server" some of this can be carried out using a TCPServiceInit object. 
 *
 *  @see  TCPServiceInit 
 *  @see  ThreadedTCPComm 
 *  @see  TCPComm 
 *  @see  LiveDataServer
 */

public interface ITCPUser
{
  void ProcessData( Object data_obj, ThreadedTCPComm tcp_io );
}
