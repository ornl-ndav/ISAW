/*
 * File: JDataTree.java
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
 * Revision 1.35  2007/10/26 22:36:42  amoe
 * Added getSelectedDataSets() .
 *
 * Revision 1.34  2006/07/13 14:19:00  rmikk
 * Fixed deleteNode() method to properly notify observers when experiments
 * and full DataSets are deleted
 *
 * Revision 1.33  2005/08/24 16:25:59  dennis
 * Minor format and documenation changes.
 *
 * Revision 1.32  2005/08/18 16:33:40  dennis
 *   Added a boolean parameter "notify" to selectNode() method so that
 * notification of each individual selection can be turned off when
 * selecting multiple Data blocks.  selectNode() now returns the DataSet
 * set to be notified, rather than actually notifying it, if "notify" is
 * false.
 *   The selectNodesWithPaths() method now accumulates a "list" of all
 * DataSets to be notified, in a hashtable, and then notifies each DataSet
 * only one time.  This fixes the problem with slow selection of nodes in
 * the tree.   Deleting Data blocks from DataSets, from the tree, still
 * needs to be made more efficient.
 *   Made some minor additions to the documentation.
 *
 * Revision 1.31  2005/08/17 02:07:45  dennis
 * Now trap some null pointer exceptions that occur when deleting
 * ranges of nodes that cross DataSet and/or Experiment boundaries.
 *
 * Revision 1.30  2005/08/15 03:58:31  dennis
 * Modified deleteNodesWithPaths() so that it maintains a hashtable
 * of DataSets that had Data blocks deleted, then just notifies those
 * DataSets one time at the end, rather than notifying that Data
 * was deleted for every individual block that was deleted.  This
 * significantly improved the efficincy of deleting nodes via the
 * tree, for "moderate" size DataSets.
 * Modified deleteNode() to return the DataSet that was modified,
 * IF a Data block was deleted, so that deleteNodesWithPath() can
 * save it and notify it once at the end.
 * NOTE: Deleting thousands of Data blocks from a DataSet
 * with tens of thousands of Data blocks is still VERY INEFFICIENT
 * when done from the tree.  It takes a couple of seconds at most to
 * do this with an operator, and MANY MINUTES to do this from the tree.
 * Most of the time is lost in the method model.removeNodeFromParent().
 * This method takes about 1/30 second to execute on a fast machine
 * (2Ghz pentium M) with a large DataSet (65000 entries).
 * While the current modifications improved the speed a more than an
 * order of magnitude, more needs to be done.
 *
 * Revision 1.29  2004/07/30 16:09:20  kramer
 *
 * Now when the user has a node representing a DataSet closed, opens a viewer
 * for that DataSet, and selects a spectrum, the DataSet's node in the tree
 * doesn't get unselected.
 * Note:  Here, selected refers to the JTree's definition of selection, not the
 * concept of selecting a Data block in a DataSet.
 *
 * Revision 1.28  2004/07/14 21:57:53  kramer
 *
 * Now if the user has the tree collapsed and points at a spectrum in a viewer,
 * the tree doesn't expand.  However, if it is already expanded it doesn't get
 * collapsed either.  Also, if the user opens a viewer for a histogram and its
 * corresponding node is expanded, the tree will scroll to display the node
 * corresponding to a spectrum pointed at in the viewer.  If the histogram's
 * node is collapsed, the spectrum's node is selected but the tree doesn't
 * expand or scroll to display it.
 *
 * Revision 1.27  2004/07/14 20:03:16  kramer
 *
 * Fixed a NullPointerException that would occasionaly originate from
 * getSelectedNode().  I simplified the method to use more specific methods
 * from the JTree class, but the method's functionality is still the same.
 *
 * Revision 1.26  2004/07/14 18:51:25  kramer
 *
 * Now the tree QUICKLY handles selection changed messages and makes the
 * selected nodes' text blue (and makes unselected nodes' text black).
 * If a spectrum is selected in a viewer the corresponding node in the tree
 * is selected.  However, now the tree does not scroll to make the node visible.
 *
 * Revision 1.25  2004/07/07 16:38:54  kramer
 *
 * Now the tree responds to "selection changes" from viewers.  If a Data object
 * in a DataSet is selected, its corresponding node in the tree has its text
 * color set to blue.  Also, now the tree will allow the user to use shift
 * and control to select multiple nodes.
 *
 * Revision 1.24  2004/06/30 14:21:11  kramer
 *
 * The tree now handles "pointed at" messages.  Now when the user selects a
 * spectrum in a viewer, the corresponding node on the tree is selected.
 *
 * Revision 1.23  2004/06/29 16:48:33  kramer
 *
 * Modified the update method.  Now when spectra are removed from a DataSet (for
 * example through a viewer) the corresponding nodes are removed from the tree.
 *
 * Revision 1.22  2004/03/15 03:31:25  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.21  2004/02/18 22:33:41  dennis
 * Changed the behavior of the addExperiment() methods.
 * Previously, if an experiment name was already in the tree,
 * addExperiment() would return false and not add the new
 * experiment with a duplicate name to the tree.
 * Now, the experiment name will be modified by prepending
 * the smallest integer that makes the name unique, and
 * the experiment will always be added to the tree.
 *
 * Revision 1.20  2004/01/08 15:06:19  bouzekc
 * Removed unused imports and unused local variables.
 *
 * Revision 1.19  2003/08/08 16:31:43  chatterjee
 * The 'Modified' folder in the Tree view now opens up and the
 * scrollbar moves to it when a modified dataset is put into it.
 * Modified datasets can come through GUI driven operations
 * or from scripts.
 *
 * Revision 1.18  2003/03/27 15:57:36  pfpeterson
 * Notifies IObservers of DESTROY when multiple DataSets or an Experiment
 * is DESTROYed.
 *
 * Revision 1.17  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 * Revision 1.16  2002/11/18 22:30:29  pfpeterson
 * Added a "DEBUG" variable which is checked before untrapped updates
 * are printed to the command line.
 *
 * Revision 1.15  2002/11/07 16:32:25  pfpeterson
 * Properly ignores message from DataSets telling viewers to close.
 *
 */
 
package IsawGUI;

import gov.anl.ipns.Util.Messaging.IObserver;

import java.awt.GridLayout;
import java.awt.event.*;
import java.io.Serializable;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import DataSetTools.components.ParametersGUI.IDataSetListHandler;
import DataSetTools.dataset.*;

/**
 * wraps a JTree object.  the result is a simpler interface that's
 * more appropriate for ISAW's tree.  for example, there's no need
 * for arbitrary branches--ISAW only allows 3 levels.
 *
 * these three levels are Experiment, DataSet, and Data.  each of
 * these types correspond node types Exerpiment, DataSetMutableTreeNode,
 * and DataMutableTreeNode.  each of these nodes is attached to the
 * root, which is a DefaultMutableTreeNode.  the structure of the
 * tree is as follows:
 *
 *   DefaultMutableTreeNode
 *     |
 *     +-- Experiment
 *           |
 *           +-- DataSetMutableTreeNode
 *           |
 *           ...
 *
 *           |
 *           +-- DataSetMutableTreeNode
 *                |
 *                +-- DataMutableTreeNode
 *                |
 *                ...
 *
 *                |
 *                +-- DataMutableTreeNode
 *
 *
 *
 * JDataTree replaces JTreeUI.  although this code was extracted
 * from JTreeUI, much effor has gone into simplifying and de-coupling
 * this code from the rest of ISAW.
 */
public class JDataTree
  extends JPanel 
  implements IObserver, Serializable, IDataSetListHandler, KeyListener
{
  private JTree tree;
  
  private boolean respondToPointedAt = true;
  private boolean isCtrlPressed = false;
  private boolean isShiftPressed = false;

  public static final String MODIFIED_NODE_TITLE = "Modified";
  public static boolean DEBUG=false;

  /**
   * the main container for various forms of data in the ISAW application.
   * the tree contains and visualizes the data on three levels--Experiments,
   * DataSets, and Data blocks.  
   *
   * Experiments are simply collections of DataSet objects.  they are 
   * roughly equivalent to runfiles.  DataSet objects store the actual
   * spectra, which are in the form of Data objects.
   */
  public JDataTree( MouseListener ml, KeyListener kl) 
  {
                                      //set up the JTree object that 
                                      //Experiments objects, DataSets
                                      //objects, and Data objects 
                                      //will be shown in.
//    tree = new JTree(   new DefaultTreeModel(  new DefaultMutableTreeNode( "Session" )  )   );
    tree = 
      new JTree( new JDataTreeModel( new DefaultMutableTreeNode( "Session" )) );
    tree.setShowsRootHandles( true );
    tree.putClientProperty("JTree.lineStyle", "Angled");
    tree.addMouseListener( ml );
    tree.addKeyListener( kl );
    tree.addKeyListener(this);
    tree.setCellRenderer(new JDataTreeCellRenderer());

    getMyModel().insertNodeInto( new Experiment( MODIFIED_NODE_TITLE ),
                                 (DefaultMutableTreeNode)getMyModel().getRoot(),
                                 0  );

    setLayout(  new GridLayout(1,1)  );
    setBorder(  new CompoundBorder( new EmptyBorder(4,4,4,4), 
                                    new EtchedBorder(EtchedBorder.RAISED) )  );

    JScrollPane pane = new JScrollPane( tree );
    add( pane );

  }

  /**
   * Gets the first selected node.
   * @return The first selected node or<br>
   * null if there aren't any nodes selected
   */ 
  public MutableTreeNode getSelectedNode()
  {
    TreePath path = tree.getSelectionPath();
    if (path != null)
       return (MutableTreeNode)path.getLastPathComponent();
    else
       return null;
    
    /*
    //Here is another implementation that checks for null
    //but is probably to complicated
    TreePath selectedPath = null;
    if(  tree.getSelectionCount() > 0  )
    {
      int[] selectedRows = tree.getSelectionRows();
      //selectedRows is either an empty array or null if 
      //nothing is selected
      if (selectedRows != null)
      {
         selectedPath =  tree.getPathForRow(selectedRows[0]); 
         //selectedPath is null if the selected row is not visible
         if (selectedPath != null)
            return (MutableTreeNode)selectedPath.getLastPathComponent();
      }
    }
    
    return null;
    */
  }
 
  
  /**
   * return a list of selected nodes.
   */ 
  public TreePath[] getSelectedNodePaths()
  {
    if(  tree.getSelectionCount() > 0  )
    {
      TreePath[] paths = new TreePath[ tree.getSelectionCount() ];
      paths = tree.getSelectionPaths();
      return paths;
    }
    else 
      return new TreePath[0];
  }


  /**
   * get the number of selections.
   */
  public int getSelectionCount()
  {
    return tree.getSelectionCount();
  }
  
  
  /**
   * This method returns an array of selected DataSets that are in the data
   * tree. 
   *
   * @return - This is the array of DataSets (DataSet[]).
   */
  public DataSet[] getSelectedDataSets()
  {
	  Vector<DataSet> vec = new Vector<DataSet>();
	  DataSet[] dss;
      TreePath[] tps = null;
      tps = getSelectedNodePaths();
 
      MutableTreeNode node;
      for( int i=0;  i<tps.length;  i++ )
      {
        node = (MutableTreeNode)tps[i].getLastPathComponent();
        if( node instanceof DataSetMutableTreeNode )
        {
          DataSetMutableTreeNode ds_node = (DataSetMutableTreeNode)node;
          DataSet ds = ds_node.getUserObject();
          vec.add(ds);
        }
      }
      
      dss = new DataSet[vec.size()];
      for(int i=0; i<dss.length; i++)
      {
    	dss[i]=vec.elementAt(i); 
      }
      
      return dss;      
  }


  /**
   * create and add a new Experiment object to the tree.
   *
   * @param  dss   Array of DataSets that make up the experiment
   * @param  name  Suggested name for the experiment.  If the 
   *               name is already in use, it will be modified by
   *               prepending an integer code to make it unique.
   *
   * @return  Currently, this will always return true, since the
   *          requested name will be modified if needed to make
   *          the name unique.
   */
  public boolean addExperiment( DataSet[] dss, String name )
  {
    Experiment e = new Experiment( dss, name );
    return addExperiment( e );
  }


  /**
   * Add a new Experiment container to the tree.  The new Experiment object
   * is added to the bottom of the tree.  If the name already existed
   * in the tree the name of the new experiment will have been changed
   * by prepending the smallest integer code that makes it unique.
   */
  public boolean addExperiment( Experiment e )
  {
    makeNameUnique( e );

    for( int i=0;  i<e.getChildCount();  i++ )
      e.getUserObject(i).addIObserver( this );

    int child_count = 
             ( (DefaultMutableTreeNode)getMyModel().getRoot() ).getChildCount();
    getMyModel().insertNodeInto(  (MutableTreeNode)e, 
                                  (MutableTreeNode)tree.getModel().getRoot(), 
                                  child_count  );
    tree.expandRow( 0 );      //make sure root node is expanded
    return true;
  }


  /**
   * add 'ds' to the experiment denoted by 'exp_name'
   */
  public void addToExperiment( DataSet ds, String exp_name )
  {
    Experiment[] exps = getExperiments();
    for( int i=0;  i< exps.length;  i++ )
    {
      // System.out.println(  exps[i].toString() + " " + exp_name  );

      if(  exps[i].toString().equals( exp_name )  )
        addToExperiment( ds, exps[i] );
    }
  }


  /**
   * add 'ds' to Experiment object 'exp'
   */ 
  public void addToExperiment( DataSet ds, Experiment exp )
  {
    DataSetMutableTreeNode ds_node = new DataSetMutableTreeNode( ds );
    int index = exp.getChildCount();

                            //make sure we're not adding
                            //a DataSet object to this
                            //Experiment that's already here
    DataSet[] dss = getDataSets( exp );
    for( int i=0;  i<dss.length;  i++ )
    {
      if( dss[i] == ds )
      {
        // System.out.println( "found it" );
        return;
      }
    }

    exp.insert( ds_node, index );
    getMyModel().reload( exp );
  }


  /**
   * adds a new Experiment object to the 'Modified' node of the tree.
   * use this whenever a new DataSet/Experiment is created and needs
   * to be added to the tree.  the Experiment is always added as the
   * last node within the 'Modified' sub-root node.
   */ 
  public void addToModifiedExperiment( DataSet ds )
  {
    ds.addIObserver( this );
    getModifiedExperiment().setUserObject( ds );

    getMyModel().reload(  getModifiedExperiment()  );
    tree.expandRow( 0 );
    tree.expandRow( 1 );
    tree.scrollRowToVisible(1);

  }


  /**
   * returns node that contains 'obj'.  if 'obj' is not found,
   * null is returned.  null is also returned if 'obj' is of an
   * unsupported type. 
   */
  public MutableTreeNode getNodeOfObject( Object obj )
  {
    DefaultMutableTreeNode root = 
                             (DefaultMutableTreeNode)tree.getModel().getRoot();
    Enumeration e = root.breadthFirstEnumeration();

    while(  e.hasMoreElements()  )
    {
      MutableTreeNode node = (MutableTreeNode)e.nextElement();
  
      if( node instanceof Experiment )
      {
        Experiment exp_node = (Experiment)node;
        if(  exp_node == obj  )  //check mem addr
          return exp_node;
      }

      else if( node instanceof DataSetMutableTreeNode )
      {
        DataSetMutableTreeNode ds_node = (DataSetMutableTreeNode)node;
        if(  ds_node.getUserObject() == obj  )  //check mem addr
          return ds_node;
      }

      else if( node instanceof DataMutableTreeNode )
      {
        DataMutableTreeNode data_node = (DataMutableTreeNode)node;
        if(  data_node.getUserObject() == obj  )  //check mem addr
          return data_node;
      }
    }
    return null;  //not found
  }


  /*
   * the JTree object that this class encapsulates uses a
   * DefaultTreeModel.  this function just saves us from
   * some nasty casting syntax.
   */ 
  protected JDataTreeModel getMyModel()
  {
    return (JDataTreeModel)tree.getModel();
  }


  /**
   * saves us the trouble of writing this mess every time we
   * need to do something to the Experiment that stores the modified
   * DataSet objects.  also allows us some flexibility in 
   * changing the structure of the tree...
   */ 
  protected Experiment getModifiedExperiment()
  {
    DefaultMutableTreeNode root = 
                             (DefaultMutableTreeNode)tree.getModel().getRoot();
    return (Experiment)root.getChildAt( 0 );
  }


  /**
   * delete all selected nodes
   */
  public void deleteSelectedNodes()
  {
    TreePath[] tps = getSelectedNodePaths();

    if( tps.length > 0 )
      deleteNodesWithPaths( tps );
  }


  /**
   * delete the nodes to which 'tps' leads.  this method automatically updates
   * IObservers of Data and DataSet objects.
   */
  public void deleteNodesWithPaths( TreePath[] tps )
  {
    Hashtable hash = new Hashtable();     // save DataSets to notify in 
                                          // hashtable
    DataSet   ds   = null;
    for( int i=0;  i<tps.length;  i++ )  
    {                                    
/*
      MutableTreeNode node = 
        (MutableTreeNode)tps[i].getLastPathComponent();
      if ( node instanceof Experiment )
        System.out.println("Deleteing experiment");
      else if ( node instanceof DataSetMutableTreeNode )
        System.out.println("Deleting DataSet ");
      else if ( node instanceof DataMutableTreeNode )
        System.out.println("Deleting Data BLOCK ");
      else 
        System.out.println("WHAT AM I DELETEING???? " + node);
*/       
                                          //this call takes care of
                                          //observer notification
      ds = deleteNode( tps[i], false );   //except when deleting Data blocks
      if ( ds != null )
        hash.put( ds, ds );
    }

    Object dss[] = hash.values().toArray();   // Now Notify any DataSets
    for ( int i = 0; i < dss.length; i++ )    // from which we deleted Data
    {
      ds = (DataSet)dss[i];
      ds.notifyIObservers( IObserver.DATA_DELETED );
    }
  }


  /*
   * Remove an arbitrary node from the tree.  Any Experiment container
   * object, DataSet object, or Data object may be removed using this
   * function.  This method will automatically notify observers if notify
   * is passed in true.  If notify is passed in false, then DataSets
   * that have individual Data blocks removed will NOT be notified.
   * If whole DataSets or experiments are deleted these will be 
   * sent a DESTROY message in any case.  IF notify is passed in 
   * false, the calling code is responsible for notifying DataSets
   * that were modified.  If any DataSet is modified by deleting 
   * a Data block, the DataSet to be notified is returned by this 
   * method.  Only one notification should be sent when many Data
   * blocks are deleted from on DataSet.
   *
   *   @param obj     the object to be removed from the tree
   *
   *   @param notify  If true, and the object is a DataMutableTreeNode,
   *                  then notify the DataSet's IObservers that a Data
   *                  block was deleted.
   *
   * @return A reference to a DataSet that had a Data block deleted,
   * or null, if only whole DataSets and experiments were deleted.
   */
  public DataSet deleteNode( Object obj, boolean notify )
  {
    MutableTreeNode node = null;
    if(  obj instanceof Data || 
         obj instanceof DataSet  )
    {
      node = getNodeOfObject( obj );
    }
    else if(  obj instanceof DataMutableTreeNode ||
              obj instanceof DataSetMutableTreeNode ||
              obj instanceof Experiment  )
    {
      node = (MutableTreeNode)obj;
    }
    else if( obj instanceof TreePath )
    {
      TreePath tp = (TreePath)obj;
      node = (MutableTreeNode)tp.getLastPathComponent();
    }
    else
      return null;

    DataSet ds = null;
    if( node instanceof Experiment )
    {
      Experiment exp = (Experiment)node;
      if( ! exp.toString().equals( MODIFIED_NODE_TITLE )  )       
      {                                  //Don't delete modified node
        Enumeration kids=exp.children();
        while(kids.hasMoreElements())
        {
          ds=((DataSetMutableTreeNode)kids.nextElement()).getUserObject();
          ds.deleteIObserver(this);
          ds.notifyIObservers(IObserver.DESTROY);
        }
       
        getMyModel().removeNodeFromParent( node );
        getMyModel().extinguishNode( node );       
      }
      return null;
    }

    /* for DataSet objects, care must be taken that the object
     * in question is not referenced elsewhere in the tree.  if so, only
     * node 'obj' is removed.  annoyingly, all viewers of that DataSet
     * will be destroyed, even those viewing the reference.  the purpose 
     * of references is to ease the load on the hardware... glad runs
     * are huge, scd files are right around 30M and growing, and the
     * other instruments will tax the system more and more as their
     * detector counts go up.  so, if people want deep copies of 
     * DataSet objects, they have to do it explicitly.   
     */ 
    else if( node instanceof DataSetMutableTreeNode )
    {
     // System.out.println( "destroyed a DataSet object" );
      ds = ( (DataSetMutableTreeNode)node ).getUserObject();           
      ds.deleteIObserver( this );
      ds.notifyIObservers( IObserver.DESTROY );
      
      getMyModel().removeNodeFromParent( node );
      getMyModel().extinguishNode( node );

      return null;
    }

    else if( node instanceof DataMutableTreeNode )
    {
      DataMutableTreeNode data_node = (DataMutableTreeNode)node;

      if ( data_node == null )           // node must have already been 
        return null;                     // deleted`

      DataSetMutableTreeNode dataset_node = 
                                  (DataSetMutableTreeNode)data_node.getParent();

      if ( dataset_node == null )        // Nothing there to delete, since
        return null;                     // we must have previously deleted
                                         // the DataSet 

      ds = (DataSet)dataset_node.getUserObject();

      if ( ds == null )                 // Nothing to delete
        return null;
 
                 // Remove node from the tree and free up the memory.  This
                 // calls DataSetMutableTreeNode.remove() which in turn removes
                 // the node from the DataSet.
                 // TODO: speed up removeNodeFromParent
      getMyModel().removeNodeFromParent( node );
      getMyModel().extinguishNode( node );

                 //if we deleted a Data object
                 //we might have to notify it's 
                 //IObservers...
      if( notify )
      {
        ds.notifyIObservers( IObserver.DATA_DELETED );
        return null;
      }

      return ds;
    }
    
    return null;
  }


  /**
   * adds a TreeSelectionListener.  replaces previous method, 
   * .getTree().addTreeSelectionListener(...), which broke
   * encapsulation of the JTree.
   */
  public void addTreeSelectionListener( TreeSelectionListener tsl )
  {
    tree.addTreeSelectionListener( tsl );
  }


  /**
   * clear all selected Experiment, DataSet, and Data objects.
   */
  public void clearSelections()
  {
                            //notify all DataSet objects,
                            //just in case.  this could
                            //be done more efficiently by
                            //only updating DataSet objects
                            //where there were selections.
    DataSet[] dss = getDataSets();
    for( int i=0;  i<dss.length;  i++ )
    {
      dss[i].clearSelections();
      dss[i].notifyIObservers( IObserver.SELECTION_CHANGED );
    }

    tree.getSelectionModel().clearSelection();
  }


  /**
   * selects the nodes that the elements of 'tps' lead to.
   */ 
  public void selectNodesWithPaths( TreePath[] tps )
  {
    Hashtable hash = new Hashtable();     // save DataSets to notify in 
                                          // hashtable
    DataSet ds = null;

    for( int i=0;  i<tps.length;  i++ )   // Select nodes, without doing notify
    {
      ds = selectNode( (MutableTreeNode)tps[i].getLastPathComponent(), false );
      if ( ds != null )
        hash.put( ds, ds ); 
    }

    Object dss[] = hash.values().toArray();   // Now Notify any DataSets
    for ( int i = 0; i < dss.length; i++ )    // from which we deleted Data
    {
      ds = (DataSet)dss[i];
      ds.notifyIObservers( IObserver.SELECTION_CHANGED );
    }
  }


  /**
   * Select a specific node from the tree.  The node can be of any data type
   * allowed in the tree (Experiment, DataSetMutableTreeNode, and
   * DataMutableTreeNode).  this method does NOT clear all
   * other selections; all previous selections are persistent.
   * If notify is passed in as true, and the node is a DataMutableTreeNode,
   * then the DataSet containing that node will be notified that a 
   * selection was changed.  IF notify is passed in false, and the node
   * being selected is a DataMutableTreeNode, then it is the responsibility
   * of the calling code to notify the corresponding DataSet.  In this case 
   * the corresponding DataSet is returned by this method.
   *
   *   @param node    the node to be selected.
   *
   *   @param notify  If true, and the node is a DataMutable tree node, 
   *                  then notify the parent DataSet that a selection was
   *                  changed.
   *
   * @return A reference to a DataSet that had a Data block selected, 
   * or null, if only whole DataSets and experiments were selected.
   * 
   */ 
  public DataSet selectNode( MutableTreeNode node, boolean notify )
  {
    if( node instanceof Experiment )
    {
      Experiment exp = (Experiment)node;
      exp.setSelected( true );
      return null;
    }

    if( node instanceof DataSetMutableTreeNode )
    {
      DataSetMutableTreeNode ds_node = (DataSetMutableTreeNode)node;
      ds_node.setSelected( true );
      return null;
    }

    if ( node instanceof DataMutableTreeNode )
    {
      DataMutableTreeNode d_node = (DataMutableTreeNode)node;
      Data d = d_node.getUserObject();
      d.setSelected( true );
                                  //find the DataSet that these Data objects
                                  //belong to and have it notify its IObservers
      DataSet ds =((DataSetMutableTreeNode)d_node.getParent() ).getUserObject();
      if ( notify )
        ds.notifyIObservers( IObserver.SELECTION_CHANGED );

      return ds;
    }

    return null;
  }
  

  /**
   * Get the complete TreePath corresponding to the node 
   * specified.
   */
  public static TreePath createTreePathForNode(MutableTreeNode node)
  {
     Stack stack = new Stack();
        stack.push(node);
     TreeNode parent = node.getParent();
     while (parent != null)
     {
        stack.push(parent);
        parent = parent.getParent();
     }
     Object[] obArr = new Object[stack.size()];
     for (int i=0; i<obArr.length; i++)
        obArr[i] = stack.pop();
     
     return new TreePath(obArr);
  }

  /**
   * examines the type of change made to 'observed' and
   * makes changes that are appropriate for this view
   */
  public void update( Object observed, Object reason )
  {
    if(  !( reason instanceof String)  &&  !( reason instanceof DataSet)  )
      return;

                                          
    if( reason instanceof DataSet )
      System.out.println( "ERROR: new DataSet object (JDataTree)" ); 


    if( observed instanceof DataSet  &&  reason instanceof String )
    {
      String reason_str = (String)reason;

      DataSet ds = (DataSet)observed;

 //   System.out.println("JDataTree.update called: " + reason_str + ", " + ds); 

                       //if update has been called from this object
                       //after a deletion, doing it again will be a problem. 
      DataSetMutableTreeNode ds_node = 
                          (DataSetMutableTreeNode)getNodeOfObject( observed );
      if( ds_node == null )
        return;

      if( reason_str.equals(DESTROY) )
      {
        deleteNode(  observed, true  );
      }
      else if( reason_str.equals(CLOSE_VIEWERS) )
      {
        // do nothing
      }
      else if( reason_str.equals(DATA_REORDERED) )
      {
                                       //force tree to update its leaves
                                       //to reflect the reordered Data 
                                       //objects                 
        ds_node.setUserObject(  ds_node.getUserObject()  );
      }


      else if ( reason_str.equals(DATA_DELETED) )
      {
        if(  ds_node != null  )  //karma++
        {
          ds_node.removeFromParent();
          getMyModel().reload();
          Experiment parent = (Experiment)ds_node.getParent();
          parent.setUserObject( ds );
        }
      }

      else if ( reason_str.equals(SELECTION_CHANGED) )
      {
         int[] selectedIndices = ds.getSelectedIndices();
         Enumeration en = ds_node.children();
         int i = 0;
         DataMutableTreeNode node;
         Object nextElement = null;
         while (en.hasMoreElements())
         {
            nextElement = en.nextElement();
            if (nextElement instanceof DataMutableTreeNode)
            {
               node = (DataMutableTreeNode)nextElement;
               node.setSelected(false);
               if (i < selectedIndices.length)
               {
                  if (node.getUserObject().equals(
                                        ds.getData_entry(selectedIndices[i])))
                  {
                     i++;
                     node.setSelected(true);
                  }
               }
            }
         }
         repaint();
      }

      else if ( reason_str.equals(POINTED_AT_CHANGED) )
      {
         if (respondToPointedAt)
         {
            Data data = ds.getData_entry(ds.getPointedAtIndex());
            if (data != null)
            {
               MutableTreeNode node = getNodeOfObject(data);
               if (node != null)
               {
                  TreePath createdPath = createTreePathForNode(node);
                  boolean dsIsExpanded = tree.isExpanded(
                                     createTreePathForNode(ds_node));
                  //this method will check if createdPath is null
                  addNodeToSelection(createdPath,dsIsExpanded);
                }
            }
         }
      }

                                       //TODO: should redraw the entire tree
      else if( (String)reason == GROUPS_CHANGED )
      {
        if(DEBUG)
          System.out.println( "unimplemented: GROUPS_CHANGED" );
      }

      else
      {
        if(DEBUG)
          System.out.println( "untrapped update(...) message in JDataTree: "
                              +(String)reason );
      }

      return; 
    }           
  }
  
  /**
   * Causes the node with the TreePath <code>
   * path</code> (and only the node with 
   * TreePath <code>path</code>) to be selected.
   * @param showNode If true, the tree will 
   * be expanded and scroll to make the 
   * node specified by <code>path</code> 
   * visible.  If false, nothing will be done 
   * to the tree (except to select the node).
   * @param path The TreePath corresponding 
   * to the node that is to selected.  Note:  
   * if <code>path</code> is null the tree 
   * remains unchanged.
   */
  private void addNodeToSelection(TreePath path, boolean showNode)
  {
     if (path != null && showNode)
     {
        tree.clearSelection();
        //this tells the tree to expand to 
        //make the node visible
        tree.setExpandsSelectedPaths(true);
        tree.scrollPathToVisible(path);
        tree.addSelectionPath(path);           
     }
  }

  /*
   * get the DataSet object that a DataMutableTreeNode belongs to.  this'll
   * save use some ugly casting syntax.
   */
  public DataSet getDataSet( MutableTreeNode node )
  {
    DataSet ds = DataSet.EMPTY_DATA_SET;

    if(  node instanceof DataMutableTreeNode  )
    {
      DataSetMutableTreeNode ds_node = (DataSetMutableTreeNode)node.getParent();
      ds = ds_node.getUserObject();
    }

    return ds;
  }


  /**
   * get a list of all of the Experiments in the tree.
   */ 
  public Experiment[] getExperiments()
  {
    DefaultMutableTreeNode root = 
                           (DefaultMutableTreeNode)tree.getModel().getRoot();
    int total_exp_count = root.getChildCount();

    int e_count = 0;
    Experiment[] exps = new Experiment[ total_exp_count ];
    for( int i=0;  i<root.getChildCount();  i++ ) 
      exps[ e_count++ ] = (Experiment)root.getChildAt(i);  
                                                 //this should be safe as
                                                 //long as no one changes
                                                 //the structure of the tree.
                                                 //see class documentation
                                                 //for details.
    return exps;
  }


/*-------------------------=[ IDataSetListHandler ]=--------------------------*/

  /**
   * searches through a JDataTree object and returns an
   * array of all of the DataSet objects contained within.
   */
  public DataSet[] getDataSets() 
  {
    Vector dss = new Vector();

    DefaultMutableTreeNode root = 
                              (DefaultMutableTreeNode)tree.getModel().getRoot();
    dss = searchFromNode( root );

                                       //pack them up in an array & return
    DataSet[] dss_array = new DataSet[ dss.size() ];
    for( int i=0;  i<dss.size();  i++ )
      dss_array[i] = (DataSet)dss.get(i);

    return dss_array;
  }


  /**
   * get all of the DataSet object that belong to an Experiment
   */ 
  public DataSet[] getDataSets( Experiment exp )
  {
    Vector dss = new Vector();
    dss = searchFromNode( exp );

                                       //pack them up in an array & return
    DataSet[] dss_array = new DataSet[ dss.size() ];
    for( int i=0;  i<dss.size();  i++ )
      dss_array[i] = (DataSet)dss.get(i);

    return dss_array;
  }



  private Vector searchFromNode( MutableTreeNode root )
  {

    Vector ds_nodes = new Vector();
    if( root instanceof DefaultMutableTreeNode )
    {
                                    //iterativly examine each Experiment object,
                                    //searching for DataSet objects.
      int exp_count = root.getChildCount();
      for(  int exp_index=0;  exp_index<exp_count;  exp_index++ )
      {
        if(  root.getChildAt( exp_index ) instanceof Experiment  )  //karma++
        {
          Experiment exp = (Experiment)root.getChildAt( exp_index );
 
          int ds_count = exp.getChildCount();
          for(  int ds_index=0;  ds_index<ds_count;  ds_index++ )  //karma++
            if( exp.getChildAt( ds_index ) instanceof DataSetMutableTreeNode  )
            {
              DataSetMutableTreeNode dsmtn = 
                            (DataSetMutableTreeNode)exp.getChildAt( ds_index );

              DataSet ds = (DataSet)dsmtn.getUserObject();
              ds_nodes.addElement( ds );
            }
            else
              System.out.println( "non-DataSet object found on second level" );
        }
        else
          System.out.println( "non-Experiment object found on first level" );
      }
    }
    else if( root instanceof Experiment )
    {
      Experiment exp = (Experiment)root;

      int ds_count = exp.getChildCount();
      for(  int ds_index=0;  ds_index<ds_count;  ds_index++ )  //karma++
        if( exp.getChildAt( ds_index ) instanceof DataSetMutableTreeNode  )
        {
          DataSetMutableTreeNode dsmtn = 
                            (DataSetMutableTreeNode)exp.getChildAt( ds_index );

          DataSet ds = (DataSet)dsmtn.getUserObject();
          ds_nodes.addElement( ds );
        }
        else
          System.out.println( "non-DataSet object found on second level" );
    }

    return ds_nodes;
  }


  /*
   *  Check to see if the experiment name was already used in the 
   *  tree.  If it was, then change the name of the experiment to
   *  be unique by prepending the smallest integer that makes the
   *  name unqiue.
   */
  private void makeNameUnique( Experiment e )
  {
    Experiment[] exps = getExperiments();
    boolean unique = false;
    String  exp_name = e.getName();
    int tag_count = 0;
    while ( !unique ) 
    {                      // try prepending an integer, until it is unique 
      if ( tag_count > 0 )
        exp_name = "" + tag_count + "_" + e.getName();

      int i = 0;
      unique = true;
      while ( i < exps.length && unique )
      {
        if( exps[i].toString().equals( exp_name ) )
        {
          unique = false;
          tag_count++;
        }
        else
          i++;
      }
    }
    e.setName( exp_name );    // Now set the experiment name to the unique one
  }

  
  public void keyPressed(KeyEvent e)
  {
     int code = e.getKeyCode();
     if ((code==KeyEvent.VK_SHIFT) || (code==KeyEvent.VK_CONTROL))
     {
        respondToPointedAt = false;
        if (code==KeyEvent.VK_SHIFT)
           isShiftPressed = true;
        if (code==KeyEvent.VK_CONTROL)
           isCtrlPressed = true;
     }
  }

   public void keyReleased(KeyEvent e)
   {
      int code = e.getKeyCode();
      if ((code==KeyEvent.VK_SHIFT) || (code==KeyEvent.VK_CONTROL))
      {
         respondToPointedAt = true;
         if (code==KeyEvent.VK_SHIFT)
            isShiftPressed = false;
         if (code==KeyEvent.VK_CONTROL)
            isCtrlPressed = false;
      }
   }

   public void keyTyped(KeyEvent e)
   {
   }
}
