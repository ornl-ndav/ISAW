/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2001/07/15 05:27:12  neffk
 * replaces JTreeUI.
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
import java.awt.datatransfer.*;

/**
 * wraps a JTree object.  the result is a simpler interface that's
 * more appropriate for ISAW's tree.  for example, there's no need
 * for arbitrary branches--ISAW only allows 3 levels.
 *
 * JDataTree replaces JTreeUI.  although this code was extracted
 * from JTreeUI, much effor has gone into simplifying and de-coupling
 * this code from the rest of ISAW.
 */
public class JDataTree
  extends JPanel 
  implements IObserver, 
             Serializable 
{
  private JDataViewUI jdvui;
  private JTree tree;

  boolean set_selection = false ;
  private JTabbedPane jtp;
  DataSet current_ds = null;

  private Vector experiments = null;

  public static final String MODIFIED_NODE_TITLE = "Modified DataSet(s)";


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
    experiments = new Vector();

    setLayout(new GridLayout(1,1));

                                      //set up the JTree object that 
                                      //Experiments objects, DataSets
                                      //objects, and Data objects 
                                      //will be shown in.
    tree = new JTree(   DefaultTreeModel(  new DefaultMutableTreeNode( "Session" )  )   );
    tree.setShowsRootHandles( true );
    tree.putClientProperty("JTree.lineStyle", "Angled");
    tree.addMouseListener(  new MouseListener( this )  );
    JScrollPane pane = new JScrollPane( tree );
    add( pane );

    setBorder(new CompoundBorder(new EmptyBorder(4,4,4,4), new EtchedBorder (EtchedBorder.RAISED)));

    jdvui = new JDataViewUI();

/*
    jtp = new JTabbedPane();
    jtp.addTab("Runfiles", pane);
    add( jtp );
*/
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
      return null;
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
    tree.getModel().insertNodeInto(  e, root, 0  );
    tree.expandRow( 0 );      //make sure root node is expanded
  }


  /**
   * returns node that contains 'obj'.  if 'obj' is not found,
   * null is returned.  null is also returned if 'obj' is of an
   * unsupported type. 
   */
  public MutableTreeNode getNodeOfObject( Object obj )
  {
    Enumeration e = root.preorderEnumeration();
    while(  e.hasMoreElements()  )
    {
      MutableTreeNode node = (MutableTreeNode)e.nextElement();
  
      if( node instanceof Experiment )
      {
        Experiment e = (Experiment)node;
        if(  e.getUserObject() == obj  )
          return node;
      }

      else if( node instanceof DataSetMutableTreeNode )
      {
        DataSetMutableTreeNode dsmtn = (DataSetMutableTreeNode)node;
        if(  dsmtn.getUserObject() == obj  )
          return node;
      }
      else
        return null;
    }
  }


  /*
   * remove an arbitrary node from the tree.  Any Experiment container
   * object, DataSet object, or Data object may be removed using this
   * function.  this method does NOT automatically notify observers 
   * of IObserver implementers because it could potentially be quite 
   * "expensive" to do so if many items are deleted.
   */
  public void deleteNode( Object obj, boolean notify )
  {
    MutableTreeNode node = getNodeOfObject( obj );
    tree.getModel().removeNodeFromParent( node );

    if( notify  && node instanceof DataSetMutableTreeNode )
    {
      DataSet ds = ( (DataSetMutableTreeNode)node ).getUserObject();
      ds.notifyIObservers( IObserver.DESTROY );
    }

    else if( notify && node instanceof DataMutableTreeNode )
    {
    
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
      DefaultMutableTreeNode node = getNodeOfObject(reason);

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

      if( (String)reason == DESTROY )
      {
        deleteNode( observed );
      }
      else if( (String)reason == DATA_REORDERED)
      {
        DefaultMutableTreeNode node = getNodeOfObject(observed);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
        int index = parent.getIndex(node) ;

        if(node == null)   
        {
          //System.out.println("ERROR: Node not found in treeUpdate:");
          return;
        }
        DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(ds);
        tree.getModel().removeNodeFromParent(node);
        tree.getModel().insertNodeInto(level2,parent,index);
    
        for (int k = 0; k < ds.getNum_entries() ; k++)
        {
          Data entry = ds.getData_entry(k);
          DefaultMutableTreeNode level3 = new DefaultMutableTreeNode(entry); 
          index = level2.getChildCount();
          tree.getModel().insertNodeInto(level3,level2,index);
        }  
    
        tree.expandRow(index);
      }


      else if ( (String)reason == DATA_DELETED )
      {
        DefaultMutableTreeNode ds_node = getNodeOfObject( observed );
        if(  ds_node != null  )  //karma++
        {
 
          //remove old JTree representation of the DataSet object
          DefaultMutableTreeNode parent = (DefaultMutableTreeNode)ds_node.getParent();
          int index_of_ds_node = parent.getIndex( ds_node );
          tree.getModel().removeNodeFromParent( ds_node );

          //create a new JTree representation of the DataSet object
          DefaultMutableTreeNode new_ds_node = new DefaultMutableTreeNode( ds );
          for( int i=0;  i<ds.getNum_entries();  i++ )
          {
            Data d = ds.getData_entry(i);
            DefaultMutableTreeNode data_block_node = new DefaultMutableTreeNode( d ); 
            tree.getModel().insertNodeInto( data_block_node,
                                  new_ds_node, 
                                  new_ds_node.getChildCount()  );  //add to end of branch
          }  
          tree.getModel().insertNodeInto( new_ds_node, parent, index_of_ds_node );
          tree.expandRow( index_of_ds_node );
        }
      }

      else if ( (String)reason == SELECTION_CHANGED )
      {
        DefaultMutableTreeNode node = getNodeOfObject(observed);
        if(node == null)
        {
//          System.out.println("ERROR: Node not found in treeUpdate:");
          return;
        }

        DefaultTreeModel tree.getModel() = (DefaultTreeModel)tree.getModel();

        for(int i=node.getChildCount()-1; i>=0; i--)
        {
          DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode)node.getChildAt(i);
          Data data = (Data)dataNode.getUserObject();
          if(data.isSelected())
//          tree.setSelectionRow(i);
//          System.out.println("Selected block" +i);
          System.out.print("");
        }
      }

      else if ( (String)reason == POINTED_AT_CHANGED )
      {
//        int index = ds.getMostRecentlySelectedIndex();
        int index = ds.getPointedAtIndex() ;
        if(index>=0)
        {
          Data d = ds.getData_entry(index);
//          if(d.isMostRecentlySelected()) 
          JPropertiesUI pui = new JPropertiesUI();
          pui.showAttributes(d.getAttributeList());
      }
    }


      else if( (String)reason == GROUPS_CHANGED )
      {
       DefaultMutableTreeNode node = getNodeOfObject(observed);
       DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
       int index = parent.getIndex(node) ;
       if(node == null)
       {
       // System.out.println("ERROR: Node not found in treeUpdate:");
         return;
       } 
       DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(ds);

       tree.getModel().removeNodeFromParent(node);
       tree.getModel().insertNodeInto(level2,parent,index);
       tree.expandRow(index);
       for (int k = 0; k < ds.getNum_entries() ; k++)
       {
         Data entry = ds.getData_entry(k);
         DefaultMutableTreeNode level3 = new DefaultMutableTreeNode(entry); 
         index = level2.getChildCount();
         tree.getModel().insertNodeInto(level3,level2,index);
       }  
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
        ringmaster = new JDataTreeRingmaster( JDataTree.tree );
      }


      public void mousePressed( MouseEvent e ) 
      {
        if(  tree.getSelectionCount() > 0  ) 
        {
          TreePath[] selectedPath = null;
          TreePath[] tp           = tree.getSelectionPaths();  

          int button1 =  e.getModifiers() & InputEvent.BUTTON1_MASK;
          int button3 =  e.getModifiers() & InputEvent.BUTTON3_MASK;
           
                                                          
          if(  button3 == InputEvent.BUTTON3_MASK  )
            ringmaster.generatePopupMenu( tp, e );

          else if(  button1 == InputEvent.BUTTON1_MASK  )
          {
            if(e.getClickCount() == 1) 
              ringmaster.pointAtNode( tp );

            else if(e.getClickCount() == 2) 
              ringmaster.selectNode( tp );
          }
        }
      }
    }

/*---------------------------=[ end MouseAdapter ]=---------------------------*/


 }
