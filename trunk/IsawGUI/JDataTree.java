/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2001/07/20 16:42:16  neffk
 * improved delete mechanism--now Data and DataSet objects can be
 * deleted from the tree.  added selectNodesWithPaths(), worked
 * on update()
 *
 * Revision 1.2  2001/07/18 16:52:46  neffk
 * removed many, many dependancies, unused variables, and code.
 * added extensive comments, detailing the structure of this object
 * so the poor sap that gets to maintain this isn't completely
 * in the dark.  other major changes include moving the tree from
 * DefaultMutableTreeNodes to Experiment, DataSetMutableTreeNode, and
 * DataMutableTreeNode objects.  although this container finds its roots
 * in JTReeUI, restrictions have been made, most notably the removal of
 * getTree() and getModel().  methods have been added to do low-level
 * operations that were formerly carried out by users of JTreeUI--things
 * like clear selections and removed nodes.  this diff was only 15k.
 *
 */
 
package IsawGUI;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.JTree.*;
import javax.swing.plaf.basic.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.math.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import Command.*;
import javax.swing.border.*;

import IPNS.Runfile.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;
import DataSetTools.components.ui.OperatorMenu;
import DataSetTools.components.ParametersGUI.IDataSetListHandler;
import java.awt.datatransfer.*;

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
  implements IObserver, Serializable, IDataSetListHandler
{
  private JTree tree;

  /**
   * the main container for various forms of data in the ISAW application.
   * the tree contains and visualizes the data on three levels--Experiments,
   * DataSets, and Data blocks.  
   *
   * Experiments are simply collections of DataSet objects.  they are 
   * roughly equivalent to runfiles.  DataSet objects store the actual
   * spectra, which are in the form of Data objects.
   */
  public JDataTree() 
  {
                                      //set up the JTree object that 
                                      //Experiments objects, DataSets
                                      //objects, and Data objects 
                                      //will be shown in.
    tree = new JTree(   new DefaultTreeModel(  new DefaultMutableTreeNode( "Session" )  )   );
    tree.setShowsRootHandles( true );
    tree.putClientProperty("JTree.lineStyle", "Angled");
    tree.addMouseListener(  new MouseListener()  );

    setLayout(new GridLayout(1,1));
    setBorder(new CompoundBorder(new EmptyBorder(4,4,4,4), new EtchedBorder (EtchedBorder.RAISED)));

    JScrollPane pane = new JScrollPane( tree );
    add( pane );

  }


  /**
   * 
   */ 
  public MutableTreeNode getSelectedNode()
  {
    TreePath selectedPath = null;
    if(  tree.getSelectionCount() > 0  )
    {
      int[] selectedRows = tree.getSelectionRows();
      selectedPath =  tree.getPathForRow(selectedRows[0]); 
      return (MutableTreeNode)selectedPath.getLastPathComponent();
    }
    else 
      return null;
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
   * create and add a new Experiment object to the tree.
   */
  public void addExperiment( DataSet[] dss, String name )
  {
    Experiment e = new Experiment( dss, name );
    addExperiment( e );
  }


  /**
   * add a new Experiment container to the tree.
   */
  public void addExperiment( Experiment e )
  {
    getMyModel().insertNodeInto(  (MutableTreeNode)e, 
                                  (MutableTreeNode)tree.getModel().getRoot(), 
                                  0  );
    tree.expandRow( 0 );      //make sure root node is expanded
  }


  /**
   * returns node that contains 'obj'.  if 'obj' is not found,
   * null is returned.  null is also returned if 'obj' is of an
   * unsupported type. 
   */
  public MutableTreeNode getNodeOfObject( Object obj )
  {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
    Enumeration e = root.breadthFirstEnumeration();

    if( e == null )
      System.out.println( "null enum" );

    while(  e.hasMoreElements()  )
    {
      MutableTreeNode node = (MutableTreeNode)e.nextElement();
  
      if( node instanceof Experiment )
      {
        Experiment exp_node = (Experiment)node;
        if(  exp_node.equals( obj )  )
          return exp_node;
      }

      else if( node instanceof DataSetMutableTreeNode )
      {
        DataSetMutableTreeNode ds_node = (DataSetMutableTreeNode)node;
        if(  ds_node.getUserObject().equals( obj )  )
          return ds_node;
      }

      else if( node instanceof DataMutableTreeNode )
      {
        DataMutableTreeNode data_node = (DataMutableTreeNode)node;
        if(  data_node.getUserObject().equals( obj )  )
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
  protected DefaultTreeModel getMyModel()
  {
    return (DefaultTreeModel)tree.getModel();
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
   * delete nodes
   */
  public void deleteNodesWithPaths( TreePath[] tps )
  {

    if( tps.length > 1 )
      for( int i=0;  i<tps.length-1;  i++ )
        deleteNode( tps[i], false );

                                        //this call takes care 
                                        //of notification if we
                                        //deleted Data objects
    deleteNode(  tps[ tps.length-1 ], true  );
  }


  /*
   * remove an arbitrary node from the tree.  Any Experiment container
   * object, DataSet object, or Data object may be removed using this
   * function.  this method does NOT automatically notify observers 
   * of IObserver implementers because it could potentially be quite 
   * "expensive" to do so if many items are deleted.  the responsibility
   * is left to the caller.
   *
   *   @param obj     the object to be removed from the tree, or the
   *                  MutableTreeNode to be removed from the tree, or
   *                  the TreePath to be removed from the tree.
   *   @param notify  notifies the DataSet's IObservers when true if 
   *                  the obj is a Data object.
   */
  public void deleteNode( Object obj, boolean notify )
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
      return;

                              //remove from the tree. 
    getMyModel().removeNodeFromParent( node );


    DataSet ds = null;
    if( node instanceof Experiment )
      System.out.println( "Exeriment deleted" );

    else if( node instanceof DataSetMutableTreeNode )
    {
      ds = ( (DataSetMutableTreeNode)node ).getUserObject();

                                       //we automatically notify 
                                       //IObservers of DataSet objects
                                       //because it's unlikely that
                                       //there will ever be very many
                                       //of them loaded at once, much
                                       //less deleted in one go.
      ds.notifyIObservers( IObserver.DESTROY );
      return;
    }

    else if( node instanceof DataMutableTreeNode )
    {
      DataMutableTreeNode data_node = (DataMutableTreeNode)node;
      int group_id = data_node.getUserObject().getGroup_ID();

      DataSetMutableTreeNode dataset_node = (DataSetMutableTreeNode)data_node.getParent();
      ds = (DataSet)dataset_node.getUserObject();
      ds.removeData_entry_with_id( group_id );

                 //if we deleted a Data object
                 //we might have to notify it's 
                 //IObservers...
      if( notify )
        ds.notifyIObservers( IObserver.DATA_DELETED );
    }
    
    return;
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
   * clear all selections
   */
  public void clearSelections()
  {
    tree.getSelectionModel().clearSelection();
    //TODO: notify DataSet objects
  }


  public void selectNodesWithPaths( TreePath[] tps )
  {
    for( int i=0;  i<tps.length;  i++ )
      selectNode(  (MutableTreeNode)tps[i].getLastPathComponent()  );
  }


  public void selectNode( MutableTreeNode node )
  {
    if( node instanceof Experiment )
    {
    }

    if( node instanceof DataSetMutableTreeNode )
    {
    }

    if( node instanceof DataMutableTreeNode )
    {
      DataMutableTreeNode d_node = (DataMutableTreeNode)node;
      Data d = d_node.getUserObject();
      d.setSelected( true );
                                  //find the DataSet that these Data objects
                                  //belong to and have it notify its IObservers
      DataSet ds = ( (DataSetMutableTreeNode)d_node.getParent() ).getUserObject();
      ds.notifyIObservers( IObserver.SELECTION_CHANGED );
    }
  }

  /**
   * examines the type of change made to 'observed' and
   * makes changes that are appropriate for this view
   */
  public void update( Object observed, Object reason )
  {
    if(  !( reason instanceof String)  &&  !( reason instanceof DataSet)  )
      return;
                                           
                                 //if we're sent a DataSet object, 
                                 //create a new Experiment and add
                                 //this DataSet object to it.
    if( reason instanceof DataSet )
    { 
      DataSet ds = (DataSet)reason;
      MutableTreeNode node = getNodeOfObject(reason);

      if( node == null )
      {
        DataSet[] dss = new DataSet[ 1 ];  dss[0] = ds;
        addExperiment( dss, "Modified" );
      }
      else
      {
        System.out.println( "DataSet found" );
        return;
      }
    }         


    if( observed instanceof DataSet )                                  
    {
      DataSet ds = (DataSet)observed;

      DataSetMutableTreeNode ds_node = (DataSetMutableTreeNode)getNodeOfObject( observed );

                       //if update has been called from this object
                       //there's no need to do it again.
      if( ds_node == null )
        return;

      if( (String)reason == DESTROY )
        deleteNode( observed, true );

      else if( (String)reason == DATA_REORDERED )
      {
                                       //force tree to update its leaves
                                       //to reflect the reordered Data 
                                       //objects                 
        ds_node.setUserObject(  ds_node.getUserObject()  );
      }


      else if ( (String)reason == DATA_DELETED )
      {
        if(  ds_node != null  )  //karma++
        {
          Experiment parent = (Experiment)ds_node.getParent();
          parent.setUserObject( ds );
        }
      }

      else if ( (String)reason == SELECTION_CHANGED )
      {
        //TODO: figure out what to do for this IObserver message
      }

      else if ( (String)reason == POINTED_AT_CHANGED )
      {
        int index = ds.getPointedAtIndex() ;
        if( index >= 0 )
        {
          Data d = ds.getData_entry( index );
          JPropertiesUI pui = new JPropertiesUI();
          pui.showAttributes(  d.getAttributeList()  );
        }
      }


                                       //TODO: should redraw the entire tree
      else if( (String)reason == GROUPS_CHANGED )
      {
        System.out.println( "unimplemented" );
      }

      else
      {
        System.out.println( "untrapped update(...) message in JDataTree" );
      }

      return; 
    }           
  }

  


/*--------------------------=[ start MouseAdapter ]=--------------------------*/

    class MouseListener extends MouseAdapter
    {
      private JDataTreeRingmaster ringmaster = null;


      public MouseListener()
      {
        ringmaster = new JDataTreeRingmaster( JDataTree.this );
      }


      public void mousePressed( MouseEvent e ) 
      {
        if(  tree.getSelectionCount() > 0  ) 
        {
          TreePath[] selectedPath = null;
          TreePath[] tps          = tree.getSelectionPaths();  

          int button1 =  e.getModifiers() & InputEvent.BUTTON1_MASK;
          int button3 =  e.getModifiers() & InputEvent.BUTTON3_MASK;
           
                                                          
          if(  button3 == InputEvent.BUTTON3_MASK  )
            ringmaster.generatePopupMenu( tps, e );

          else if(  button1 == InputEvent.BUTTON1_MASK  )
          {
            if( e.getClickCount() == 1 ) 
              ringmaster.pointAtNode( tps[0] );

            else if( e.getClickCount() == 2 ) 
              ringmaster.selectNode( tps );
          }
        }
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
      DataMutableTreeNode d_node = (DataMutableTreeNode)node;
      DataSetMutableTreeNode ds_node = (DataSetMutableTreeNode)node.getParent();
      ds = ds_node.getUserObject();
    }

    return ds;
  }



/*-------------------------=[ IDataSetListHandler ]=--------------------------*/

  /**
   * searches through a JDataTree object and returns an
   * array of all of the DataSet objects contained within.
   */
  public DataSet[] getDataSets() 
  {
    Vector ds_nodes = new Vector();

    DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();

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
            DataSetMutableTreeNode dsmtn = (DataSetMutableTreeNode)exp.getChildAt( ds_index );
            DataSet ds = (DataSet)dsmtn.getUserObject();
            ds_nodes.addElement( ds );
          }
          else
            System.out.println( "non-DataSet object found on second level" );
      }
      else
        System.out.println( "non-Experiment object found on first level" );
    }
  
    DataSet[] ds_nodes_array = new DataSet[ ds_nodes.size() ];
    for( int i=0;  i<ds_nodes.size();  i++ )
      ds_nodes_array[i] = (DataSet)ds_nodes.get(i);

    return ds_nodes_array;
  }




}
