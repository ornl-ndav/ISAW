
/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2001/07/31 16:00:51  neffk
 * added the extinguish() method to help reclaim this object's memory
 * when it's removed from the tree.
 *
 * Revision 1.4  2001/07/20 16:36:10  neffk
 * log error: fixed removeFromNode() in previous revision
 *
 * Revision 1.3  2001/07/20 16:30:31  neffk
 * fixed some fairly obvious errors, added a few comments, and changed
 * children() to return an implementor of Enumeration instead of null.
 * the change was intended to effect some other error messages... but
 * it pains my heart to delete coded that works, so i didn't delete it.
 *
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

  /**
   * default constructor
   */ 
  public DataMutableTreeNode()
  {
  }


  public DataMutableTreeNode( Data d )
  {
    this.parent = parent;
    name = new String(  "Group #" + d.getGroup_ID()  );
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


