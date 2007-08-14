/*
 * File:  IReceiveMessage.java
 *
 * Copyright (C) 2005 Dennis Mikkelson
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2007/08/14 00:09:00  dennis
 *  Added MessageTools files from UW-Stout repository.
 *
 *  Revision 1.2  2005/11/29 01:57:59  dennis
 *  Added javadoc comments.
 *
 */

package MessageTools;

/**
 *  This interface must be implemented by classes that can receive messages
 *  from this message handling system.  It defines one method, receive()
 *  that will be called to give a message to the object.
 */
public interface IReceiveMessage
{

  /* ---------------------------- receive ---------------------------- */
  /**
   *  Accept (and process) the specified message.
   *
   *  @param message  The message that is to be processed.
   *
   *  @return  true if the message was properly processed, and false
   *                otherwise.
   */
  public boolean receive( Message message );

}
