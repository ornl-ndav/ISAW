
/*
 * $Id$
 *
 * $Log$
 * Revision 1.6  2001/07/31 16:02:32  neffk
 * added the extinguish() method, which helps reclaim memory that this
 * objects uses after it's severed from the tree.
 *
 * Revision 1.5  2001/07/25 17:34:15  neffk
 * changed 'name' to represent the tag and title of the DataSet object.
 * the getTitle() method of DataSet returns the only the title, however
 * the toString() method returns the title prepended w/ a tag number,
 * making the DataSet object's apparent title unique within ISAW.
 *
 * Revision 1.4  2001/07/23 13:59:31  neffk
 * added a selected flag.
 *
 * Revision 1.3  2001/07/20 16:33:08  neffk
 * fixed a synch problem, fixed removeFromParent().  removed a redundant
 * index check; karma--;
 *
 * Revision 1.2  2001/07/18 16:38:02  neffk
 * uses DataMutableTreeNodes instead of DefaultMutableTreeNodes.
 * also, fixed setParent(...) to set the parent of this node, not
 * its children.  (oops)
 *
 * Revision 1.1  2001/07/15 05:26:23  neffk
 * contains Data objects in JDataTree.
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



