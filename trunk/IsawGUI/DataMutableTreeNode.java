/*
 * File: DataMutableTreeNode.java
 *
 * Copyright (C) 2001, Kevin Neff
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>

 *
 * $Log$
 * Revision 1.9  2003/12/16 00:00:49  bouzekc
 * Removed unused imports.
 *
 * Revision 1.8  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 * Revision 1.7  2002/09/11 23:22:09  dennis
 * Label for tree is now obtained using the Data.getLabel() method,
 * rather than using the attribute at index 1 of the attribute list.
 *
 * Revision 1.6  2002/05/30 16:53:17  chatterjee
 * Gets the label for the group and puts it in the tree
 *
 */

package IsawGUI;

import java.util.Enumeration;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import DataSetTools.dataset.Data;

/**
 * encapsulates a Data object.
 */
public class DataMutableTreeNode
  implements TreeNode, MutableTreeNode
{

  private Data            data   = null;
  private MutableTreeNode parent = null;
  private String          name   = null;

  /**
   * default constructor
   */ 
  public DataMutableTreeNode()
  {
  }


  public DataMutableTreeNode( Data d )
  {
    this.parent = parent;
    name=d.getLabel();
    setUserObject( d );
  }


  /**
   * attempts to make all references to this object null
   */ 
  public void extinguish()
  {
    data = null;
    name = null;
    setParent( null );
  }


  public String toString()
  {
    return name;
  }


/*------------------------------=[ TreeNode ]=--------------------------------*/

  public Enumeration children()
  {
    return new emptyEnumeration();
  }


  /**
   * never true.
   */
  public boolean getAllowsChildren()
  {
    return false;
  }


  /**
   * this node never has any children.
   */
  public TreeNode getChildAt( int childIndex )
  {
    return new DataMutableTreeNode();
  }


  public int getChildCount()
  {
    return 0;
  }


  public int getIndex( TreeNode node )
  {
    return -1;
  }


  public TreeNode getParent()
  {
    return parent;
  }


  /**
   * this node is always a leaf
   */
  public boolean isLeaf()
  {
    return true;
  }



/*---------------------------=[ MutableTreeNode ]=----------------------------*/


  /**
   * insert a node 'child' at index.
   */
  public void insert( MutableTreeNode child, int index )
  {
  }


  /**
   * remove a Data object from this node
   */
  public void remove( int index )
  {
  }


  /**
   * remove a Data object from this node.
   */
  public void remove( MutableTreeNode node )
  {
  }


  public void removeFromParent()
  {
    parent.remove( this );
  }


  /**
   * set the reference to the parent of this node.
   */ 
  public void setParent( MutableTreeNode node )
  {
    parent = node;
  }


  /**
   * set the object that this node contains.
   */
  public void setUserObject( Object obj )
  {
    data = (Data)obj;
  }


  /**
   * returns a reference to the Data objects contained in this node
   */
  public Data getUserObject()
  {
    return data;
  }


/*------------------------------=[ TreeNode ]=--------------------------------*/

  final class emptyEnumeration implements Enumeration
  {
    public emptyEnumeration()
    {
    }
  
    public boolean hasMoreElements()
    {
      return false; 
    }

    public Object nextElement()
    {
      return new Object();
    }
  }
}


