
/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2001/07/18 16:30:36  neffk
 * 0) removed dependancy on DefaultMutableTreeNode.
 * 1) removed DataMutableTreeNode( Data, MutableTreeNode )
 *
 * Revision 1.1  2001/07/15 05:38:34  neffk
 * contains Data object in JDataTree.
 *
 */

package IsawGUI;

import DataSetTools.dataset.Data;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.tree.TreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * encapsulates a Data object.
 */
public class DataMutableTreeNode
  implements TreeNode, MutableTreeNode
{

  private Data            data   = null;
  private MutableTreeNode parent = null;
  private String          name   = null;


  public DataMutableTreeNode()
  {
  }


  public DataMutableTreeNode( Data d )
  {
    this.parent = parent;
    name = new String(  "Group #" + d.getGroup_ID()  );
    setUserObject( d );
  }


  public String toString()
  {
    return name;
  }


/*------------------------------=[ TreeNode ]=--------------------------------*/

  public Enumeration children()
  {
    return null;
  }


  /**
   * never true.
   */
  public boolean getAllowsChildren()
  {
    return false;
  }


  /**
   * returns the child TreeNode at index childIndex.
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
  }


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
}



