/*
 * File:  NxWriteNode.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * $Log$
 * Revision 1.3  2002/11/20 16:15:42  pfpeterson
 * reformating
 *
 * Revision 1.2  2001/07/30 20:10:14  rmikk
 * added a show() routine
 *
 * Revision 1.1  2001/07/25 21:23:20  rmikk
 * Initial checkin
 *
 */

package NexIO.Write;

/**
 * Interface for all Write nodes that can be viewed as node based.
 * Implementations of this interface will include Nexus node format
 * for Nexus files and Nexus-based XML files
 */
public interface NxWriteNode{
  /**
   * returns error message or "" if none
   */
  public String getErrorMessage();

  /**
   * Gets the number of children with classname of this node Used to
   * find the number of NXentry's in a file that has Already been
   * opened ONLY
   */
  public int getNumClasses( String classname );


  /**
   * Creates a new child and connects it to the parent
   */
  public NxWriteNode newChildNode( String node_name , String class_name );   

  /**
   * Sets this node's value. If an array Value will be linear
   * This routine will convert to the correct type and dimensions
   */
  public void setNodeValue( Object Value ,int type , int ranks[]);

  /**
   * All attributes should be  a linear array
   */
  public void addAttribute( String AttrName, Object AttrValue,
                            int type, int ranks[]);

  //------------------ Links ----------------------------
  /**
   * Adds an already set up link as a child of this node
   *
   * @param  linkhandle   A name used to refer to a link
   */
  public void addLink( String linkhandle );

  /**
   * Sets up a link thus Avoiding saving the same information twice
   *
   * @param linkhandle the name used to refer to this link
   *
   * NOTE: Each link should have a separate name
   */
  public void  setLinkHandle(String linkhandle);


  //------------------ Saving --------------------------
  /**
   * Writes node to file if it can. The node cannot have attributes,
   * children, or links incorporated after this method is executed.
   *
   * NOTE: ONLY the Root node invoke this operation
   */
  public void write();
  public void show();
  public void close();
}
