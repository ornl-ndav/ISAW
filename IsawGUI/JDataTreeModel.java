
/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2001/07/31 15:59:44  neffk
 * extends javax.swing.tree.DefaultTreeModel to help manage memory.
 * included in enxtensions is the extinguish() method, which looks after
 * setting everything in each node to null, thereby freeing up the
 * memory previously claimed by the node.	for large DataSet objects
 * (GLAD and SCD, expecially) delete is critical because the data is
 * soo large.  previously, DefaultTreeModel was used, which allowed
 * the developer to remove branches, but the branches were left intact
 * where we wish to destory every node below the detachment point.
 *
 */

package IsawGUI;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * extends DefaultTreeModel by including the 'extinguish' method
 * which completely disolves tree branches, instead of simply
 * removing the branch from its parent.
 */ 
public class JDataTreeModel extends DefaultTreeModel
{
 
  /**
   * initialize super class
   */ 
  public JDataTreeModel( TreeNode root )
  {
    super( root );
  }


  /**
   * removes 'node', setting that parent of each leaf to null
   * to ensure that System.gc can do its work.
   */ 
  public boolean extinguishNode( MutableTreeNode node )
  {
    if( node instanceof Experiment )
    {
      ( (Experiment)node ).extinguish();
      return true;
    }

    else if( node instanceof DataSetMutableTreeNode )
    {
      ( (DataSetMutableTreeNode)node ).extinguish();
      return true;
    }

    else if( node instanceof DataMutableTreeNode )
    {
      ( (DataMutableTreeNode)node ).extinguish();
      return true;
    }
 
    else
      return false;
  }
}

