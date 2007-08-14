/*
 * File:  TestReceiver.java
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
 *  Revision 1.1  2007/08/14 00:09:01  dennis
 *  Added MessageTools files from UW-Stout repository.
 *
 *  Revision 1.1  2005/11/29 01:59:36  dennis
 *  Initial version.
 *
 */

package MessageTools;

/**
 *  This class is a simple receiver for messages, for testing purposes.
 *  When a message is received, the name of the receiver, and the message
 *  are printed.
 */
public class TestReceiver implements IReceiveMessage
{

  String my_name;

  /* --------------------------- constructor --------------------------- */
  /**
   *  Construct a new TestReceiver with the specified name.
   *
   *  @param name  The name to be used to identify this TestReceiver 
   */
  public TestReceiver( String name )
  {
    my_name = name;
  }


  /* ----------------------------- receive ----------------------------- */
  /**
   *  Receive the specified message and process it by printing the name
   *  of this TestReceiver and the message.
   *
   *  @param message  The message that should be printed for testing purposes. 
   */
  public boolean receive( Message message )
  {
    System.out.println( my_name + " received " + message );
    return true;
  }

}
