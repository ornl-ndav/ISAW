/*
 * File: DataSetMutableTreeNode.java
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
 * Revision 1.7  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 */

package IsawGUI;

import DataSetTools.dataset.DataSet;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.tree.TreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * encapsulates a collection of Data objects.
 */
public class DataSetMutableTreeNode
  implements TreeNode, MutableTreeNode
{

  private DataSet         ds          = null;
  private MutableTreeNode parent      = null;
  private Vector          data_nodes  = null;
  private String          name        = null;
  private boolean         selected    = false;

  public DataSetMutableTreeNode( DataSet ds )
  {
    this.ds = ds;
    name = ds.toString();
    setUserObject( ds );
  }

 
  /**
   * attempts to make all references null so we can relaim the
   * memory that this obj is using.
   */ 
  public void extinguish()
  {
    ds = null;
    name = null;
    setParent( null );

    for( int i=0;  i<data_nodes.size();  i++ )
      ( (DataMutableTreeNode)data_nodes.get(i) ).extinguish();
  }


  public String toString()
  {
    return name;
  }


  /**
   * test to see if this item is selected.
   */ 
  public boolean isSelected()
  {
    return selected;
  }


  /**
   * set or clear this object's selection status.
   */ 
  public void setSelected( boolean selected )
  {
    this.selected = selected;
  }
    

/*------------------------------=[ TreeNode ]=--------------------------------*/

  public Enumeration children()
  {
    return data_nodes.elements();
  }


  /**
   * always returns true.
   */
  public boolean getAllowsChildren()
  {
    return true;
  }


  /**
   * returns the child TreeNode at index childIndex.
   */
  public TreeNode getChildAt( int childIndex )
  {
    return (TreeNode)data_nodes.get( childIndex );
  }


  public int getChildCount()
  {
    return data_nodes.size();
  }


  public int getIndex( TreeNode node )
  {
    return data_nodes.indexOf( node );
  }


  public TreeNode getParent()
  {
    return parent;
  }


  /**
   * the object is never a leaf.  although it may be empty at times, it is
   * intended as a container for Data objects, which are always children
   * of this container.
   */
  public boolean isLeaf()
  {
    return false;
  }



/*---------------------------=[ MutableTreeNode ]=----------------------------*/


  /**
   * insert a node 'child' at index.
   */
  public void insert( MutableTreeNode child, int index )
  {
    data_nodes.insertElementAt( child, index );
  }


  /**
   * remove a Data object from this node
   */
  public void remove( int index )
  {
    ds.removeData_entry( index );
    data_nodes.remove( index );
  }


  /**
   * remove a Data object from this node.
   */
  public void remove( MutableTreeNode node )
  {
    DataMutableTreeNode d_node = (DataMutableTreeNode)node;
    int group_id = d_node.getUserObject().getGroup_ID();
    ds.removeData_entry_with_id( group_id );

    data_nodes.remove( node );
  }


  public void removeFromParent()
  {
    parent.remove( this );
  }


  public void setParent( MutableTreeNode node )
  {
    parent = node;
  }


  /**
   * sets this object to contain nodes of the Data objects
   * contained by in the DataSet parameter of this function.  also, this
   * method resets all previous selections.  use this method to 
   * make the tree reflect changes in the ordering of DataSet objects
   * (after sorting operations, etc).
   */
  public void setUserObject( Object obj )
  {
    DataSet ds = (DataSet)obj;
    data_nodes = new Vector();
    
    for( int i=0;  i<ds.getNum_entries();  i++ )
    {
      DataMutableTreeNode node = new DataMutableTreeNode( ds.getData_entry(i) );
      node.setParent( this );

      data_nodes.addElement( node );
    }
  }


  /**
   * returns a reference to the DataSet objects contained in this
   * object.
   */
  public DataSet getUserObject()
  {
    return ds;
  }
}



