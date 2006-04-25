/*
 * File:  IUDPUser.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
 *                     Ruth Mikkelson,
 *                     Alok Chatterjee
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
 *  Revision 1.3  2002/11/27 23:27:59  pfpeterson
 *  standardized header
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
