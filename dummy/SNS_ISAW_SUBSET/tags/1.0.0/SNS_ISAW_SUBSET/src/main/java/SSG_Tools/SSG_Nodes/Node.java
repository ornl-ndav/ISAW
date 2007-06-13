/*
 * File:  Node.java
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
 * Modified:
 *
 *  $Log: Node.java,v $
 *  Revision 1.7  2005/08/03 16:55:20  dennis
 *  Minor fix to javadocs.
 *
 *  Revision 1.6  2005/07/26 13:00:33  dennis
 *  Added static method to remove a specified node from the static list
 *  of nodes.  Added method to clear the static list of nodes.
 *  Fixed problem with infinite loop of calls to Node.setParent()
 *  and Group.removeChild() by simplifying setParent() method to NOT
 *  also call removeChild().  NOTE: IF setParent() is called by methods
 *  other than Group.add/removeChild(), the scene graph will likely be
 *  in an inconsistent state.
 *
 *  Revision 1.5  2005/07/25 13:42:09  dennis
 *  Improved javadocs.
 *  setParent() method now removes node as child of previous parent.
 *  Added method to remove a particular Pick ID from the hashtable of
 *  (pick_id, node) pairs.
 *
 *  Revision 1.4  2004/11/22 18:43:48  dennis
 *  Removed redundant "else" clause.
 *
 *  Revision 1.3  2004/11/03 19:23:57  dennis
 *  Added methods  preRender()  and  postRender()  that can be
 *  called by derived classes to take care of the name stack,
 *  before and after rendering.
 *  Made the pick_id private, since it no longer needs to be
 *  accessed directly from derived classes.
 *
 *  Revision 1.2  2004/10/26 17:08:38  dennis
 *  Added a static hashtable to keep list of nodes with valid pick_ids.
 *  The hashtable is now updated when a pick ID is set on any node.
 *  Added static method getNodeWithID() to get a reference to the node with
 *  a specific id.
 *
 *  Revision 1.1  2004/10/25 21:59:47  dennis
 *  Added to SSG_Tools CVS repository
 *
 *   10/25/04  Added an integer pick_id field and methods to get and set
 *             the pick id, for use when rendering in selection mode.
 *
 *   10/16/04  Restricted parent node to be a group node.
 */

package SSG_Tools.SSG_Nodes;

import java.util.*;

import net.java.games.jogl.*;

import SSG_Tools.*;

/** 
 *  This is the abstract base class for all classes that can be nodes in a
 *  simple scene graph.  It provides methods dealing with selection and 
 *  "parents" that are useful for all nodes.  Specifically, this base
 *  class maintains an integer ID to be used when picking objects.  This
 *  ID is entered in a hash table, so that the corresponding node can be
 *  easily found using the getNodeWithID(id) method.  This only works 
 *  properly if the IDs given to nodes are unique.  NOTE: ids only need
 *  to be assigned to nodes that need to be pickable.
 *    To make these ids useful, preRender() and postRender() methods 
 *  have been defined to push and pop the name stack with the node's id
 *  provided the id is valid.  Ids are initially set to INVALID_PICK_ID=-1
 *  so the id must first to set to a unique valid value > 0, before it
 *  can be picked.  Also, to actually be picked, the node needs to either
 *  have an actual visible representation (such as Shape or SimpleShape)
 *  or it must be a group node that has child nodes that are visible
 *  representations.
 *    In addition to methods dealing with ids, this class also has
 *  methods for getting and setting the parent of a node. 
 */

abstract public class Node implements IGL_Renderable
{
  public  static final int INVALID_PICK_ID = -1;

  private static Hashtable id_table = new Hashtable();
  
  private Group  my_parent = null;
  private int    pick_id   = INVALID_PICK_ID;


  /* ---------------------------- setParent -------------------------- */
  /**
   *  Set the parent of this node in the scene graph.   This method has 
   *  package visiblilty, to prevent classes from other packages from 
   *  improperly setting the parent.  This method should ONLY be called
   *  by the addChild and removeChild methods of a Group node.
   *
   *  @param  parent  The new parent for this node.
   */
  void setParent( Group parent )
  {
     my_parent = parent;
  }


  /* ---------------------------- getParent -------------------------- */
  /**
   *  Get a reference to the parent of this node in the scene graph.
   *
   *  @return  the current parent of this node.
   */
  public Group getParent()
  {
    return my_parent;
  }


  /* ----------------------------- setPickID ------------------------- */
  /**
   *  Set the integer ID to used in selection mode.  To "un-set" the
   *  pick ID for this node AND remove that ID from the static list of
   *  node IDs, pass in id = INVALID_PICK_ID.  In this case, the OpenGL 
   *  name stack will no longer be pushed when the node is rendered, and
   *  the previous ID of the node can be reused.  NOTE: It is critical 
   *  that any node that is to be disposed of must either have it's ID
   *  reset to INVALID_PICK_ID by this method, or must have it's ID
   *  removed from the list of IDs.  IF this is NOT done, the Node class
   *  will hold a reference to the node in it's static list, and the
   *  memory for the node will not be freed.
   *
   *  @param  id  The new pick ID to use for this node.
   */
  public void setPickID( int id )
  {
    if ( id == INVALID_PICK_ID )               // just remove the node from the
    {                                          // id_table, record it and return
      id_table.remove( new Integer(pick_id) );  
      pick_id = id;
      return;
    }
                                               // if the id is valid, first
                                               // see if it's used and if so
                                               // reset id of the old node 
    Node old_node = getNodeWithID( id );
    if ( old_node != null )
      old_node.pick_id = INVALID_PICK_ID;

    pick_id = id;
    id_table.put( new Integer(pick_id), this );
  }


  /* ----------------------------- getPickID ------------------------- */
  /**
   *  Get the integer ID that is used for this node when it is rendered
   *  in selection mode.  
   *
   *  @return  The current pick ID to use for this node.
   */
  public int getPickID()
  {
    return pick_id;
  }


  /* ---------------------------- preRender -------------------------- */
  /**
   *  This method carries out basic operations that must be done BEFORE
   *  rendering a particular node, such as pushing the name stack if 
   *  the node has a valid pick_id.  IF derived classes override this 
   *  method, they should call super.preRender() first, before doing any
   *  preRender() operations needed at their level.
   *
   *  @param  drawable  The drawable on which the object is to be rendered.
   */
  public void preRender( GLDrawable drawable )
  {
    if ( pick_id != Node.INVALID_PICK_ID )                // put new name on
    {                                                     // name stack for
      GL gl = drawable.getGL();                           // selection mode
      gl.glPushName( pick_id );                           
    }
  }
  

  /* ---------------------------- postRender -------------------------- */
  /**
   *  This method carries out basic operations that must be done AFTER 
   *  rendering a particular node, such as poping the name stack if 
   *  the node has a valid pick_id.  IF derived classes override this 
   *  method, they should call super.postRender() after doing any
   *  postRender() operations needed at their level.
   *
   *  @param  drawable  The drawable on which the object is to be rendered.
   */
  public void postRender( GLDrawable drawable )
  {
    if ( pick_id != Node.INVALID_PICK_ID )         // pop the name stack if we
    {                                              // pushed a new name on it
      GL gl = drawable.getGL();
      gl.glPopName();              
    }
  }


  /* --------------------------- getNodeWithID ----------------------- */
  /**
   *  Get a reference to the node with the specified pick_id, if possible.
   *  If there is no such node, return null.
   */
  public static Node getNodeWithID( int id )
  {
    Object node = id_table.get( new Integer(id) );
    if ( node != null )
      return (Node)node;

    return null;
  }


  /* ---------------------------- removeNode ----------------------------- */
  /**
   *  Remove the node with this pick ID, from the static list of (ID,node)
   *  pairs.  Set the ID for the node to INVALID_PICK_ID and remove the
   *  node from the list.  NOTE: It is critical that any node that is to
   *  be disposed of must either have it's ID reset to INVALID_PICK_ID by 
   *  calling setPickID( INVALID_PICK_ID ), or must have it's ID
   *  removed from the list of IDs using the removeNode method.
   *  IF this is NOT done, the Node class will hold a reference to the 
   *  node in it's static list, and the memory for the node will not be freed.
   *
   *  @param  id  The ID to remove from the static list of (ID,node) pairs.
   */
  public static void removeNode( int id )
  {
    if ( id != INVALID_PICK_ID )
    {
      Integer key = new Integer(id);
      Node node = (Node)(id_table.get( key ));
      if ( node != null )
      {
        node.pick_id = INVALID_PICK_ID;
        id_table.remove( key );
        return;
      }
    }
  }


  /* ---------------------------- removeNode --------------------------- */
  /**
   *  Remove this node from the static list of (ID,node) pairs.  
   *  Set the ID for the node to INVALID_PICK_ID and remove the
   *  node from the list.  NOTE: It is critical that any node that is to
   *  be disposed of must either have it's ID reset to INVALID_PICK_ID by 
   *  calling setPickID( INVALID_PICK_ID ), or must have it's ID
   *  removed from the list of IDs using the removeNode method.
   *  IF this is NOT done, the Node class will hold a reference to the 
   *  node in it's static list, and the memory for the node will not be freed.
   *
   *  @param  node  The node to remove from the static list of (ID,node) pairs.
   */
  public static void removeNode( Node node )
  {
    if ( node == null )
      return;

    int id = node.pick_id;
    removeNode( id );
  }


  /* -------------------------- clearPickIDs -------------------------- */
  /**
   *  Clear out the list of all pickable nodes.  NOTE: Currently, the
   *  ids of the Nodes themselves are not reset, so this should be used
   *  with caution.  It is needed when rebuilding all scene graphs,
   *  since otherwise the list of pickable nodes will contain references to
   *  all of the old nodes, so the old nodes would not be garbage collected.
   */
  public static void clearPickIDs()
  {
    id_table.clear();
  }


}
