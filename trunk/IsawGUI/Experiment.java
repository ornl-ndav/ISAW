 
/*
 * $Id$
 *
 * $Log$
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
  private Vector          dataset_select = null;
  private String          name           = null;


  public Experiment( DataSet[] dss, String name )
  {
    this.name = name;
    setUserObject( dss );
  }


  public String toString()
  {
    return name;
  }

 
  /**
   * returns whether or not DataSet 'index' is selected.
   */
  public boolean getSelected( int index )
  {
    return ( (Boolean)dataset_select.get(index) ).booleanValue();
  }


  public void setSelected( int index, boolean selected )
  {
    dataset_select.set(  index, new Boolean( selected )  );
  }


  public void clearSelections()
  {
    for( int i=0;  i<dataset_select.size();  i++ )
      dataset_select.set(  i, new Boolean( false )  );
  }


  public void update( String msg )
  {
    if( msg == IObserver.DATA_REORDERED )
    {
      System.out.println( "reordering" );
    }
  }

  /**
   * removes a DataSet from this container
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
   * insert a new DataSet into the Experiment container.  this operation
   * clears all previous selections.
   */
  public void insert( MutableTreeNode child, int index )
  {
    dataset_nodes.insertElementAt( child, index );
    dataset_select.insertElementAt(  new Boolean( false ), index  );

                  //clear previous selections
    clearSelections();
  }


  /**
   * remove a DataSet object from the Experiment container.  this
   * operation clears all previous selections.
   */
  public void remove( int index )
  {
    dataset_nodes.remove( index );
    dataset_select.remove( index );

                  //clear previous selections
    clearSelections();
  }


  /**
   * remove a DataSet object from the Experiment container.  this
   * operation clears all previous selections.
   */
  public void remove( MutableTreeNode node )
  {
    dataset_nodes.remove( node );
    dataset_select.remove( 0 );  //any node will do

                  //clear previous selections
    clearSelections();
  }


  public void removeFromParent()
  {
    for( int i=0;  i<dataset_nodes.size();  i++ )
      ( (DataSetMutableTreeNode)dataset_nodes.get(i) ).removeFromParent();
  }


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
                                       //if 'obj' is a DataSet[], clear
                                       //all objects in this container and
                                       //initialized w/ the elements of 'obj'
    if( obj instanceof DataSet[] )
    {
      DataSet[] dss = (DataSet[])obj;
      dataset_nodes = new Vector();
      dataset_select = new Vector();
    
      for( int i=0;  i<dss.length;  i++ )
      {
        DataSetMutableTreeNode node = new DataSetMutableTreeNode( dss[i] );
        node.setParent( this );

        dataset_nodes.addElement( node );
        dataset_select.addElement(  new Boolean( false )  );
      }
    }

                                       //if 'obj' is a DataSet object and
                                       //it's already stored here, just
                                       //update it.
    else if( obj instanceof DataSet )
    {
      DataSet ds = (DataSet)obj;

      for( int i=0;  i<dataset_nodes.size();  i++ )
        if(  ( (DataSetMutableTreeNode)dataset_nodes.get(i) ).getUserObject().equals( ds )  )
          ( (DataSetMutableTreeNode)dataset_nodes.get(i) ).setUserObject( ds );
    }
  }
  


  /**
   * returns a reference to the DataSet objects contained in this
   * object.
   */
  public DataSet getUserObject( int index )
  {
    DataSetMutableTreeNode node = (DataSetMutableTreeNode)dataset_nodes.get( index );
    return node.getUserObject();
  }
}
