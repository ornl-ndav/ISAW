 
/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2001/07/25 15:53:47  neffk
 * added a new constructor to create an empty Experiment.  also changed
 * the setUserObject function to only reset the Experiment when passed
 * a DataSet[] instead of cooking, baking a cake, and cleaning the
 * kitchen sink.
 *
 * Revision 1.3  2001/07/23 13:52:18  neffk
 * added a select flag, removed update().
 *
 * Revision 1.2  2001/07/20 16:39:23  neffk
 * fixed removeFromNode(), added some comments.
 *
 * Revision 1.1  2001/07/18 19:10:59  neffk
 * encapsulates DataSet objects.
 *
 */

package IsawGUI;

import DataSetTools.dataset.DataSet;
import DataSetTools.util.IObserver;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

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
      if(  ( (DataSetMutableTreeNode)dataset_nodes.get(i) ).getUserObject().equals( ds )  )
      {
        DataSetMutableTreeNode node = (DataSetMutableTreeNode)dataset_nodes.get(i);
        node.removeFromParent();
        dataset_nodes.remove( node );
      }
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
