
/*
 * $Id$
 *
 * $Log$
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


  public DataSetMutableTreeNode( DataSet ds )
  {
    this.ds = ds;
    name = ds.getTitle();
    setUserObject( ds );
  }


  public String toString()
  {
    return name;
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
    if(  index < data_nodes.size()  )
      ds.removeData_entry( index );
  }


  /**
   * remove a Data object from this node.
   */
  public void remove( MutableTreeNode node )
  {
    data_nodes.remove( node );
  }


  public void removeFromParent()
  {
    for( int i=0;  i<data_nodes.size();  i++ )
      ( (DataMutableTreeNode)data_nodes.get(i) ).removeFromParent();
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



