/*
 * File: Experiment.java
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
 * Revision 1.8  2003/12/16 00:00:49  bouzekc
 * Removed unused imports.
 *
 * Revision 1.7  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 */

package IsawGUI;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import DataSetTools.dataset.DataSet;

/**
 * encapsulates a related collection of DataSet objects.  this class is 
 * intended to be a high level container for DataSet orjects with the intent
 * of grouping for organization, as well as operations, like file i/o.
 */
public class Experiment
  implements TreeNode, MutableTreeNode
{

  private MutableTreeNode parent         = null;
  private Vector          dataset_nodes  = null;
  private String          name           = null;
  private boolean         selected       = false;


  public Experiment( String name )
  {
    this.name = name;
    dataset_nodes = new Vector();
  }


  public Experiment( DataSet[] dss, String name )
  {
    this.name = name;
    setUserObject( dss );
  }

 
  /**
   * get the selected status of this object.
   */
  public boolean isSelected( int index )
  {
    return selected;
  }


  /**
   * sets or clears this object's selected status.
   */ 
  public void setSelected( boolean selected )
  {
    this.selected = selected;
  }


  /**
   * removes a DataSet from this container.
   */
  public void remove( DataSet ds )
  {
    for( int i=0;  i<dataset_nodes.size();  i++ )
    {
      DataSetMutableTreeNode node = null;
      node = (DataSetMutableTreeNode)dataset_nodes.get(i);
      if(  node.getUserObject().equals( ds )  )
      {
        node = (DataSetMutableTreeNode)dataset_nodes.get(i);
        node.removeFromParent();
        dataset_nodes.remove( node );
      }
    }
  }


  /**
   * attempts to make all references null so we can reclaim the
   * memory that this obj is using.
   */ 
  public void extinguish()
  {
    name = null;
    setParent( null );

    for( int i=0;  i<dataset_nodes.size();  i++ )
      ( (DataSetMutableTreeNode)dataset_nodes.get(i) ).extinguish();
  }


  /**
   * returns a string representation of this object.  this method
   * is primarily used to get a title for this node.
   */ 
  public String toString()
  {
    return name;
  }


/*------------------------------=[ TreeNode ]=--------------------------------*/

  public Enumeration children()
  {
    return dataset_nodes.elements();
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
    return (TreeNode)dataset_nodes.get( childIndex );
  }


  /**
   * returns the number of DataSet objects contained in this class
   */
  public int getChildCount()
  {
    return dataset_nodes.size();
  }


  public int getIndex( TreeNode node )
  {
    return dataset_nodes.indexOf( node );
  }


  public TreeNode getParent()
  {
    return parent;
  }


  /**
   * the object is never a leaf.  although it may be empty at times, it is
   * intended as a container for DataSet objects, which are always children
   * of this container.
   */
  public boolean isLeaf()
  {
    return false;
  }



/*---------------------------=[ MutableTreeNode ]=----------------------------*/


  /**
   * insert a new DataSet into the Experiment container.
   */
  public void insert( MutableTreeNode child, int index )
  {
    child.setParent( this );
    dataset_nodes.insertElementAt( child, index );
  }


  /**
   * remove a DataSet object from the Experiment container. 
   */
  public void remove( int index )
  {
    dataset_nodes.remove( index );
  }


  /**
   * remove a DataSet object from the Experiment container.
   */
  public void remove( MutableTreeNode node )
  {
    dataset_nodes.remove( node );
  }


  /**
   * removes this node from its parent by setting the parent's
   * reference to this node equal to null. 
   */
  public void removeFromParent()
  {
    MutableTreeNode node = (MutableTreeNode)getParent();
    DefaultMutableTreeNode root = null;
    if( node instanceof DefaultMutableTreeNode )
      root = (DefaultMutableTreeNode)node;
    else
      return;  //karma--

    root.remove( this );
  }


  /**
   * sets the parent of this node
   */ 
  public void setParent( MutableTreeNode node )
  {
    parent = node;
  }


  /**
   * sets this object to contain an array of DataSet objects or update
   * a current entry.
   */
  public void setUserObject( Object obj )
  {
                        //add to this container
    if( obj instanceof DataSet )
    {
      DataSet ds = (DataSet)obj;
      DataSetMutableTreeNode node = new DataSetMutableTreeNode( ds );
      node.setParent( this );
      dataset_nodes.addElement( node );
      return;
    }
     

    DataSet[] dss = (DataSet[])obj;
    dataset_nodes = new Vector();
   
    for( int i=0;  i<dss.length;  i++ )
    {
      DataSetMutableTreeNode node = new DataSetMutableTreeNode( dss[i] );
      node.setParent( this );

      dataset_nodes.addElement( node );
    }
  }
  


  /**
   * returns a reference to the DataSet objects contained in this
   * object.
   */
  public DataSet getUserObject( int index )
  {
    if( dataset_nodes != null )
    {
      DataSetMutableTreeNode node = (DataSetMutableTreeNode)dataset_nodes.get( index );
      return node.getUserObject();
    }
    else
      return DataSet.EMPTY_DATA_SET;
  }
}
