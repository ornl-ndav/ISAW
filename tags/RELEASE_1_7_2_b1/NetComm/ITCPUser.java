/*
 * File: ITCPUser.java
 *
 * Copyright (C) 2001, Dennis Mikkelson,
 *                     Ruth Mikkelson
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
