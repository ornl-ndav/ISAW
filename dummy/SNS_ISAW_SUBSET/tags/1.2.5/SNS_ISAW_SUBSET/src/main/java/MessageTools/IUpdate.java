/*
 * File:  IUpdate.java
 *
 * Copyright (C) 2006 Dennis Mikkelson
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
 *  Revision 1.1  2006/10/30 03:07:06  dennis
 *  Interface for objects that are to be updated AFTER processing a
 *  collection of methods.  For example, this is useful for processing
 *  multiple messages and then redrawing the new scene once.
 *
 *
 */

package MessageTools;

/**
 *  This interface should be implemented by classes that have an Update()
 *  method that should be called after a sequence of messages have been
 *  processed.
 */
public interface IUpdate
{

  /* ---------------------------- Update ---------------------------- */
  /**
   *  Update after processing a sequence of messages.
   * 
   *  @return  true if the update could be carried out ok, and false
   *                otherwise.
   */
  public boolean Update();

}
