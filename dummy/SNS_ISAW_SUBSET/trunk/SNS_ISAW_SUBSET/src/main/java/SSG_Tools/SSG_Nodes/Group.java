/*
 * File:  Group.java
 *
 * Copyright (C) 2004, Dennis Mikkelson
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
 * Last Modified:
 *
 *  $Author: eu7 $
 *  $Date: 2008-08-21 14:06:02 -0500 (Thu, 21 Aug 2008) $            
 *  $Revision: 301 $
 *
 *  $Log: Group.java,v $
 *  2008/08/21  Updated to latest version from UW-Stout repository.
 *
 *  Revision 1.7  2007/11/30 05:32:57  dennis
 *  Now checks whether the node being added as a child is this node.
 *  If so, the node is not added.  This prevents introducing a cycle
 *  in the scene graph.
 *
 *  Revision 1.6  2007/08/25 04:21:55  dennis
 *  Parameterized raw type.
 *
 *  Revision 1.5  2006/08/04 02:16:21  dennis
 *  Updated to work with JSR-231, 1.0 beta 5,
 *  instead of jogl 1.1.1.
 *
 *  Revision 1.4  2004/12/13 05:02:27  dennis
 *  Minor fix to documentation
 *
 *  Revision 1.3  2004/11/22 18:44:21  dennis
 *  Removed redundant else clause.
 *
 *  Revision 1.2  2004/11/03 19:28:57  dennis
 *  Added calls to preRender() and postRender(), to Render() method,
 *  to take care of pushing and popping the name stack, for derived
 *  classes that use this Render() method.
 *
 *  Revision 1.1  2004/10/25 21:59:47  dennis
 *  Added to SSG_Tools CVS repository
 *
 *           
 *  10/16/04  Moved to SSG_Nodes package so that it can use the Node.setParent
 *            Method.  Added the javadocs.
 */

package SSG_Tools.SSG_Nodes;

import java.util.*;
import javax.media.opengl.*;

public class Group extends Node
{
  private Vector<Node> children; 

  /* -------------------------- constructor ------------------------ */
  /**
   *  Construct a new empty group node.
   */
  public Group()
  {
    children = new Vector<Node>();
  }

  /* -------------------------- addChild ---------------------------- */
  /**
   *  Add the specified child to the end of the list of children.
   *
   *  @param  child  The child that should be added.
   */
  public void addChild( Node child )
  {
    if ( child != null && child != this )
    {
      children.addElement( child );
      child.setParent( this );
    }      
  }

  /* -------------------------- addChild ---------------------------- */
  /**
   *  Add the specified child at the specified index, provided index is
   *  between 0 and the size of the list.
   *
   *  @param  child  The child that should be added.
   *  @param  index  The index at which the child should be added.
   */
  public void addChild( Node child, int index )
  {
    if ( child != null && 
         child != this && 
         index >= 0    && 
         index <= children.size() )
    {
      children.add( index, child );
      child.setParent( this );
    }   
  }

  
  /* -------------------------- removeChild ---------------------------- */
  /**
   *  Remove the specified child.
   *
   *  @param  child  Reference to the child that should be removed.
   */
  public void removeChild( Node child )
  {
    if ( child != null )
    {
      children.remove( child );
      child.setParent( null );
    }
  }

  
  /* -------------------------- removeChild ---------------------------- */
  /**
   *  Remove the child at the specified index.
   *
   *  @param  index  The index of the child that should be removed.
   */
  public void removeChild( int index )
  {
    if ( index >= 0 && index < children.size() )
    {
      Node child = children.elementAt( index );
      children.remove( index );
      child.setParent( null );
    }
  }

  /* ------------------------------ clear ------------------------------- */
  /**
   *  Remove all children from this group node. 
   */
  public void Clear()
  {
    for ( int i = 0; i < children.size(); i++ )
       children.elementAt(i).setParent( null ); 
    children.clear();
  }

  /* ---------------------------- numChildren --------------------------- */
  /**
   *  Get the number of child nodes for this groupl
   *
   *  @return the current number of children for this group node.
   */
  public int numChildren()
  {
    return children.size();
  }  

  /* ---------------------------- getChild ------------------------------ */
  /**
   *  Get a reference to the child node at the specified index, or null
   *  if the child does not exist.  
   *
   *  @param  index  The index of the child that is requested.
   *
   *  @return the child at the specified index, or null if the index is 
   *          not valid.
   */
  public Node getChild( int index )
  {
    if ( index < 0 || index >= children.size() )
      return null;
    
    return children.elementAt( index );
  }

  /* ---------------------------- Render ------------------------------ */
  /**
   *  Render all children of this node.  Note: since this render method
   *  uses preRender() and postRender(), if a derived class uses this 
   *  method to actually render it's children, that derived class should
   *  NOT call preRender() and postRender() before rendering it's children.
   *
   *  @param  drawable  The drawable on which the object is to be rendered.
   */
  public void Render( GLAutoDrawable drawable )
  {
    preRender( drawable );             // take care of name stack

    for ( int i = 0; i < children.size(); i++ )
      children.elementAt(i).Render( drawable );

    postRender( drawable );            // clean up when done
  }

}
