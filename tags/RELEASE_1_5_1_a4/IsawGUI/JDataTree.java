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
    tree = new JTree(   new JDataTreeModel(  new DefaultMutableTreeNode( "Session" )  )   );
    tree.setShowsRootHandles( true );
    tree.putClientProperty("JTree.lineStyle", "Angled");
    tree.addMouseListener( ml );
    tree.addKeyListener( kl );

    getMyModel().insertNodeInto(  new Experiment( MODIFIED_NODE_TITLE ),
                                  (DefaultMutableTreeNode)getMyModel().getRoot(),
                                  0  );

    setLayout(  new GridLayout(1,1)  );
    setBorder(  new CompoundBorder( new EmptyBorder(4,4,4,4), 
                                    new EtchedBorder(EtchedBorder.RAISED) )  );

    JScrollPane pane = new JScrollPane( tree );
    add( pane );

  }

  /**
   * gets the first selected node.
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
   * get the number of selections.
   */
  public int getSelectionCount()
  {
    return tree.getSelectionCount();
  }


  /**
   * create and add a new Experiment object to the tree.
   */
  public boolean addExperiment( DataSet[] dss, String name )
  {
    Experiment e = new Experiment( dss, name );
    return addExperiment( e );
  }


  /**
   * add a new Experiment container to the tree.  the new Experiment object
   * is added to the bottom of the tree.
   */
  public boolean addExperiment( Experiment e )
  {
                              //we don't want to be creating duplicate
                              //names, so enforce their uniqueness
    Experiment[] exps = getExperiments();
    for( int i=0;  i<exps.length;  i++ )
      if(   exps[i].toString().equals(  e.toString()  )   )
        return false;

    for( int i=0;  i<e.getChildCount();  i++ )
      e.getUserObject(i).addIObserver( this );

    int child_count = (  (DefaultMutableTreeNode)getMyModel().getRoot()  ).getChildCount();
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
      System.out.println(  exps[i].toString() + " " + exp_name  );

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
        System.out.println( "found it" );
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
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
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
    for( int i=0;  i<tps.length;  i++ )   //this call takes care of
      deleteNode( tps[i], true );         //observer notification

  }


  /*
   * remove an arbitrary node from the tree.  Any Experiment container
   * object, DataSet object, or Data object may be removed using this
   * function.  this method does NOT automatically notify observers 
   * of IObserver implementers because it could potentially be quite 
   * "expensive" to do so if many items are deleted.  the responsibility
   * is left to the caller.
   *

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


    DataSet ds = null;
    if( node instanceof Experiment )
    {
      Experiment exp = (Experiment)node;
      if(  exp.toString().equals( MODIFIED_NODE_TITLE )  )
        return;
      else
      {
        if(notify){ // notify DataSet IObservers that they are going away
          Enumeration kids=exp.children();
          while(kids.hasMoreElements()){
            ds=((DataSetMutableTreeNode)kids.nextElement()).getUserObject();
            ds.deleteIObserver(this);
            ds.notifyIObservers(IObserver.DESTROY);
            ds.addIObserver(this);
          }
        }
        getMyModel().removeNodeFromParent( node );
        getMyModel().extinguishNode( node );
      }
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

      if( notify )
      {
        ds = ( (DataSetMutableTreeNode)node ).getUserObject();
        ds.deleteIObserver( this );
        ds.notifyIObservers( IObserver.DESTROY );
        ds.addIObserver( this );
      }

      getMyModel().removeNodeFromParent( node );
      getMyModel().extinguishNode( node );

      return;
    }

    else if( node instanceof DataMutableTreeNode )
    {

      DataMutableTreeNode data_node = (DataMutableTreeNode)node;
      int group_id = data_node.getUserObject().getGroup_ID();

      DataSetMutableTreeNode dataset_node = (DataSetMutableTreeNode)data_node.getParent();
      ds = (DataSet)dataset_node.getUserObject();
//      ds.removeData_entry_with_id( group_id );

                 //remove node from the tree 
                 //and free up the memory
      getMyModel().removeNodeFromParent( node );
      getMyModel().extinguishNode( node );

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
    for( int i=0;  i<tps.length;  i++ )
      selectNode(  (MutableTreeNode)tps[i].getLastPathComponent()  );
  }


  /**
   * selects a specific node.  the node can be of any data type
   * allowed in the tree (Experiment, DataSetMutableTreeNode, and
   * DataMutableTreeNode).  this method does NOT clear all
   * other selections; all previous selections are persistent.
   */ 
  public void selectNode( MutableTreeNode node )
  {
    if( node instanceof Experiment )
    {
      Experiment exp = (Experiment)node;
      exp.setSelected( true );
    }

    if( node instanceof DataSetMutableTreeNode )
    {
      DataSetMutableTreeNode ds_node = (DataSetMutableTreeNode)node;
      ds_node.setSelected( true );
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
 
                                          
    if( reason instanceof DataSet )
      System.out.println( "ERROR: new DataSet object (JDataTree)" ); 


    if( observed instanceof DataSet  &&  reason instanceof String )
    {
      String reason_str = (String)reason;

      DataSet ds = (DataSet)observed;

                       //if update has been called from this object
                       //after a deletion, doing it again will be a problem. 
      DataSetMutableTreeNode ds_node = (DataSetMutableTreeNode)getNodeOfObject( observed );
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
          Experiment parent = (Experiment)ds_node.getParent();
          parent.setUserObject( ds );
        }
      }

      else if ( reason_str.equals(SELECTION_CHANGED) )
      {
        //TODO: figure out what to do for this IObserver message
      }

      else if ( reason_str.equals(POINTED_AT_CHANGED) )
      {
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


  /**
   * get a list of all of the Experiments in the tree.
   */ 
  public Experiment[] getExperiments()
  {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
    int total_exp_count = root.getChildCount();

    int e_count = 0;
    Experiment[] exps = new Experiment[ total_exp_count ];
    for( int i=0;  i<root.getChildCount();  i++ ) 
      exps[ e_count++ ] = (Experiment)root.getChildAt(i);  //this should be safe as
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

    DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
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
    }
    else if( root instanceof Experiment )
    {
      Experiment exp = (Experiment)root;

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

    return ds_nodes;
  }


}
