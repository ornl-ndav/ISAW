/*
 * @(#)JTreeUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * $Log$
 * Revision 1.7  2001/06/23 03:42:21  neffk
 * added a mouse selection of left click, call to menu on right click
 *
 * ---------- 
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
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
import java.awt.datatransfer.*;



/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JTreeUI extends JPanel implements IObserver, Serializable 
{
    protected JPopupMenu m_popup;
    protected Action m_action;
    private JDataViewUI jdvui;
    private JTree tree;
    private JPropertiesUI jpui;
    private JCommandUI jcui;
    private CommandPane cp;

    private DefaultMutableTreeNode root, root1;
    DefaultTreeModel model, model1;
    boolean set_selection = false ;
    private JTabbedPane jtp;
    private JTree logTree;
    DataSet current_ds = null;


  /**
   *
   */
  public JTreeUI( JPropertiesUI jpui, JCommandUI jcui, CommandPane cp ) 
  {
    this.jpui = jpui;
    this.jcui = jcui;  
    this.cp = cp;
    setLayout(new GridLayout(1,1));

    //
    // set up the JTree object that runfiles and
    // data sets will be shown in.
    //
    root = new DefaultMutableTreeNode("Session");
    DefaultMutableTreeNode level1 = new DefaultMutableTreeNode( "Modified DataSet(s)");
    model = new DefaultTreeModel(root);
    model.insertNodeInto(level1, root, 0);
    tree = new JTree(model);
    tree.setShowsRootHandles(true);
    //tree.putClientProperty("JTree.lineStyle", "Horizontal");
    tree.putClientProperty("JTree.lineStyle", "Angled");



/*--------------------------=[ start MouseAdapter ]=--------------------------*/

    MouseListener ml = new MouseAdapter() 
    {
      public void mousePressed(MouseEvent e) 
//      public void mouseClicked(MouseEvent e) 
      {
        if(  tree.getSelectionCount() > 0  ) 
        {
          TreePath[] selectedPath = null;
          TreePath[] tp           = tree.getSelectionPaths();  

          int button1 =  e.getModifiers() & InputEvent.BUTTON1_MASK;
          int button3 =  e.getModifiers() & InputEvent.BUTTON3_MASK;

          if(  button3 == InputEvent.BUTTON3_MASK  )
          {
            actionMenu( tp );
          }
          else if(  button1 == InputEvent.BUTTON1_MASK  )
          {
            if(e.getClickCount() == 1) 
            {
              pointAtNode( tp );
            }
            else if(e.getClickCount() == 2) 
            {
              selectNode( tp );
            }
          }
        }
      }

      public void pointAtNode( TreePath[] tp )
      {
        System.out.println( "data tree> " + IObserver.POINTED_AT_CHANGED  );
      }


      /**
       * sets newly selected Data as selected and notifies their containing
       * DataSet.
       */
      public void selectNode( TreePath[] tp )
      {
        System.out.println( "data tree> " + IObserver.SELECTION_CHANGED  );

        if(  tp.length > 0  )  //probably don't need this, but keep it for good karma
        {
          DefaultMutableTreeNode dmtn = null;

          //
          //we can only deal w/ selections from one DataSet at a time,
          //so we'll arbitrarily choose the DataSet that the first
          //selection corresponds to
          //
          DataSet ds = traverseUpToDataSet( tp[0] );

          for( int i=0;  i<tp.length;  i++ )
          {                                                   //only use first DataSet's Data
            dmtn = (DefaultMutableTreeNode)(  tp[i].getLastPathComponent()  ); 
            if(   dmtn != null  &&  ds.equals(  traverseUpToDataSet( tp[i] )  )   );
            {
              if(  dmtn.getUserObject() instanceof Data  )
              {
                Data d = (Data)dmtn.getUserObject();
                if(   ds.getData_entry(  d.getGroup_ID()  ).isSelected() == false   )
                  ds.getData_entry(  d.getGroup_ID()  ).setSelected( true );
                else
                  ds.getData_entry(  d.getGroup_ID()  ).setSelected( false );

                System.out.println(  "Data: " + d.toString()  );
              }
              else
              {
                if(  dmtn.getUserObject() != null  ) 
                  System.out.println(  "non-Data: " + dmtn.getUserObject().toString()  );
              }
            }
          }
          ds.notifyIObservers( IObserver.SELECTION_CHANGED );
        }
      }


      public void actionMenu( TreePath[] tp )
      {
        DataSet ds = traverseUpToDataSet( tp[0] );
        DefaultMutableTreeNode dmtn = null;
        if(  tp.length > 0  )
          dmtn = (DefaultMutableTreeNode)(  tp[0].getLastPathComponent()  ); 

        //
        // single Data object
        //
        if(   dmtn.getUserObject() instanceof Data  &&  tp.length == 1   )
        {
          SingleDataBlockPopupMenu( tp );
        }
 
        //
        // multiple Data objects
        //
        else if(  dmtn.getUserObject() instanceof Data  )
        {
          for( int i=0;  i<tp.length;  i++ )
          {
            dmtn = (DefaultMutableTreeNode)(  tp[i].getLastPathComponent()  ); 

            if(  !ds.equals( traverseUpToDataSet(tp[i]) )  ||        //only use first DataSet's Data.
                 !(dmtn.getUserObject() instanceof Data )  )         //make sure we're dealing with a group of
            {                                                        //consistant objects.
              System.out.println( "non-Data or illegal multiple DataSet selection" );
              return; 
            }
          }
          MultipleDataBlockPopupMenu( tp );
        }

        //
        // single DataSet object
        //
        else if(  dmtn.getUserObject() instanceof DataSet  &&  tp.length == 1  )
        {
          SingleDataSetPopupMenu( tp );
        }
 
        //
        // multiple DataSet objects
        //
        else if(  dmtn.getUserObject() instanceof DataSet  )
        {
          for( int i=0;  i<tp.length;  i++ )
          {
            dmtn = (DefaultMutableTreeNode)(  tp[i].getLastPathComponent()  ); 

            if(   ds.equals( traverseUpToDataSet( tp[i] ) )  &&    //only use first DataSet's Data.
                  dmtn.getUserObject() instanceof DataSet   )       //make sure we're dealing with a group of
            {                                                        //consistant objects.
              MultipleDataSetPopupMenu( tp );
            }
          }
        }
        else
        {
          System.out.println( "type not appropriate for actionMenu" );
        }
      }

      public void SingleDataBlockPopupMenu( TreePath[] tp )
      {
        System.out.println( "singleDataBlockPopupMenu(...)" );
      }


      public void MultipleDataBlockPopupMenu( TreePath[] tp )
      {
        System.out.println( "MultipleDataBlockPopupMenu(...)" );
      }


      public void SingleDataSetPopupMenu( TreePath[] tp )
      {
        System.out.println( "SingleDataSetPopupMenu(...)" );
      }


      public void MultipleDataSetPopupMenu( TreePath[] tp )
      {
        System.out.println( "MultipleDataSetPopupMenu(...)" );
      }


      /**
       * recursivly find the DataSet that this Data block
       * belongs to and return it the caller
       */
      protected DataSet traverseUpToDataSet( TreePath tp )
      {
        DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)(  tp.getLastPathComponent()  );
        
        //terminal case(s)
        if(  tp.getParentPath() == null  )
          return null;
        else if(  dmtn.getUserObject() instanceof DataSet  )
          return (DataSet)dmtn.getUserObject();  

        //reCURSE!
        else
          return traverseUpToDataSet(  tp.getParentPath()  );

//        Object o = new Object();  //jikes is having trouble with returns hidden in if's
//        return (DataSet)o;
      }
    };

/*---------------------------=[ end MouseAdapter ]=---------------------------*/



    tree.addMouseListener(ml);
    JScrollPane pane = new JScrollPane(tree);

    root1 = new DefaultMutableTreeNode("TreeLog");
    model1 = new DefaultTreeModel(root1);
    logTree = new JTree(model1);
    logTree.setShowsRootHandles(true);
    logTree.putClientProperty("JTree.lineStyle", "Angled");
    JScrollPane pane1 = new JScrollPane(logTree);
    setBorder(new CompoundBorder(new EmptyBorder(4,4,4,4), new EtchedBorder (EtchedBorder.RAISED)));

    /* 
    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();		
    renderer.setLeafIcon(new ImageIcon("C://new-orange.gif"));
    tree.setCellRenderer(renderer);
    */

    jdvui = new JDataViewUI();
    jtp = new JTabbedPane();
    jtp.addTab("Runfiles", pane);
    //jtp.addTab("Operations Log", pane1);
    add(jtp);
  }


    /**
     *
     */
    public void showLog(DataSet ds)
	 {
		if(ds == current_ds)    //Only draw the log for a different dataset.
			return;
		current_ds = ds; 
            DefaultMutableTreeNode level1 = new DefaultMutableTreeNode(ds); 
            if(root1.getChildCount()>0)
             {
                DefaultMutableTreeNode dsnode =  (DefaultMutableTreeNode)root1.getChildAt(0);
                model1.removeNodeFromParent(dsnode);
             }
            model1.insertNodeInto(level1, root1, 0);
            int num = ds.getOp_log().numEntries();
            // System.out.println("Num of log entries" +num);
            for (int i = 0; i<num; i++)
            {
              //  System.out.println(" log entries  :" +ds.getOp_log().getEntryAt(i));
                 DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(ds.getOp_log().getEntryAt(i));
                 model1.insertNodeInto(level2,level1,i);
                 
                logTree.expandRow(i);
                logTree.expandRow(1);
            }
	 }	 

 
 
  public DefaultMutableTreeNode getSelectedNode()
  {
    TreePath selectedPath = null;
    if(tree.getSelectionCount()>0)
    {
      int[] selectedRows = tree.getSelectionRows();
      selectedPath =  tree.getPathForRow(selectedRows[0]); 
      return (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
    }
    else 
      return null;
  }
	  


     public JTree getTree()
     {
        return tree;  
     }

     
     public TreeModel getTreeModel()
     {
        return model;  
     }
     

  /**
   * adds data to the tree that displays runfiles
   * and data sets,
   */
  public void addDataSets(DataSet[] dss, String name)
  {
    DefaultMutableTreeNode level1 = new DefaultMutableTreeNode(name);
    int index = root.getChildCount();
    (model).insertNodeInto(level1,root,index);
    for (int i = 0; i<dss.length; i++)
    { 
      cp.addDataSet(dss[i]);  //update command pane

      //
      // add DataSet object to tree
      //
      DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(dss[i]);
      index = level1.getChildCount();
      (model).insertNodeInto(level2,level1,index);

      //
      // add Data objects to tree
      //
      for (int k = 0; k < dss[i].getNum_entries() ; k++)
      {
        Data entry = dss[i].getData_entry(k);
        DefaultMutableTreeNode level3 = new DefaultMutableTreeNode(entry); 
        index = level2.getChildCount();
        (model).insertNodeInto(level3,level2,index);
      }   

    tree.expandRow(2);
    tree.setSelectionRow(4);
    }   
  }

     public void addDataSet(DataSet ds)
     {
        cp.addDataSet(ds);
        DefaultMutableTreeNode level1 = (DefaultMutableTreeNode)root.getChildAt(0);
        int index = root.getChildCount();


           
                DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(ds);
                index = level1.getChildCount();
                ( model ).insertNodeInto(level2,level1,index);

                for (int k = 0; k < ds.getNum_entries() ; k++)
                {
                     Data entry = ds.getData_entry(k);
                     DefaultMutableTreeNode level3 = new DefaultMutableTreeNode(entry); 
                     index = level2.getChildCount();
                     ( model ).insertNodeInto(level3,level2,index);
		

                } 
		    ds.addIObserver(jpui);
		    ds.addIObserver(jcui);	
              
    }   

     public void openDataSet(DataSet ds, String name)
     {
        cp.addDataSet(ds);
        DefaultMutableTreeNode level1 = new DefaultMutableTreeNode(name);
        int index = root.getChildCount();
        ( model ).insertNodeInto(level1,root,index);

            {
                DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(ds);
                index = level1.getChildCount();
                ( model ).insertNodeInto(level2,level1,index);

                for (int k = 0; k < ds.getNum_entries() ; k++)
                {
                     Data entry = ds.getData_entry(k);
                     DefaultMutableTreeNode level3 = new DefaultMutableTreeNode(entry); 
                     index = level2.getChildCount();
                    ( model ).insertNodeInto(level3,level2,index);
                }  
            }           
     }
     
           

    	public DefaultMutableTreeNode getNodeOfObject(Object obj)
	{
		Enumeration e = root.breadthFirstEnumeration();
		while(e.hasMoreElements())
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
			if(obj==node.getUserObject())
			return node;
		}
		return null;
	}



	/**
	 * examines the type of change made to 'observed' and
	 * makes changes that are appropriate for this view
	 */
	public void update( Object observed, Object reason )
   	{
     		if ( !( reason instanceof String) && !( reason instanceof DataSet) )   
                   // currently we only allow Strings & DataSets
       	    {
       		  return;
                }

     		if ( observed instanceof DataSet )                           // this should always be true!!!
     		{
                        DataSet ds = (DataSet)observed;
 
			if ( reason instanceof DataSet )
			{
			  	DataSet ds1 = (DataSet)reason;
				DefaultMutableTreeNode node = getNodeOfObject(reason);

                                if ( node == null )    // ds1 is a NEW DataSet, add it as a modified DataSet
				  		{ addDataSet( ds1 );
				  		  jdvui.ShowDataSet(ds1,"External Frame",IViewManager.IMAGE);
						}
                                else
                                //  System.out.println("ERROR: Currently we only insert a new DataSet");
				return;
			}       		

			if ( (String)reason == DESTROY )
       			System.out.println("Reason: " + reason );

     			else if ( (String)reason == DATA_REORDERED)
			{
				DefaultMutableTreeNode node = getNodeOfObject(observed);
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
				int index = parent.getIndex(node) ;
				if(node == null)
				{
				//	System.out.println("ERROR: Node not found in treeUpdate:");
					return;
				}
				DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(ds);
				model.removeNodeFromParent(node);
                		( model ).insertNodeInto(level2,parent,index);
				
                		for (int k = 0; k < ds.getNum_entries() ; k++)
                		{
                     		Data entry = ds.getData_entry(k);
                     		DefaultMutableTreeNode level3 = new DefaultMutableTreeNode(entry); 
                     		index = level2.getChildCount();
                     		( model ).insertNodeInto(level3,level2,index);
					
                		}  
				
				tree.expandRow(index);
			}

			else if ( (String)reason == DATA_DELETED )
			{

			/*	DefaultMutableTreeNode node = getNodeOfObject(observed);
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
				if(node == null)
				{
				//	System.out.println("ERROR: Node not found in treeUpdate:");
					return;
				}

				int index = parent.getIndex(node)+1;
				DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
				model.reload(node);
	
				for(int i=node.getChildCount()-1; i>=0; i--)
				{
					DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode)node.getChildAt(i);
					Data data = (Data)dataNode.getUserObject();
					if(data.isSelected())
            			model.removeNodeFromParent(dataNode);
				}

				tree.expandRow(index);

			*/
			
				DefaultMutableTreeNode node = getNodeOfObject(observed);
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
				int index = parent.getIndex(node) ;
				if(node == null)
				{
				//	System.out.println("ERROR: Node not found in treeUpdate:");
					return;
				}
				DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(ds);

				model.removeNodeFromParent(node);
                		( model ).insertNodeInto(level2,parent,index);
				tree.expandRow(index);
                		for (int k = 0; k < ds.getNum_entries() ; k++)
                		{
                     		Data entry = ds.getData_entry(k);
                     		DefaultMutableTreeNode level3 = new DefaultMutableTreeNode(entry); 
                     		index = level2.getChildCount();
                     		( model ).insertNodeInto(level3,level2,index);
                		}  
			}

			else if ( (String)reason == SELECTION_CHANGED )
			{
				DefaultMutableTreeNode node = getNodeOfObject(observed);
				if(node == null)
				{
				//	System.out.println("ERROR: Node not found in treeUpdate:");
					return;
				}

				DefaultTreeModel model = (DefaultTreeModel)tree.getModel();

				

				for(int i=node.getChildCount()-1; i>=0; i--)
				{
					DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode)node.getChildAt(i);
					Data data = (Data)dataNode.getUserObject();
					if(data.isSelected())
				//	tree.setSelectionRow(i);
                              //System.out.println("Selected block" +i);
                              System.out.print("");
				}
		

			}

	else if ( (String)reason == POINTED_AT_CHANGED )
			{
				//	int index = ds.getMostRecentlySelectedIndex();
					int index = ds.getPointedAtIndex() ;
					if(index>=0)
					{
						Data d = ds.getData_entry(index);
					//	if(d.isMostRecentlySelected())	
						  JPropertiesUI pui = new JPropertiesUI();
                                      pui.showAttributes(d.getAttributeList());
					}
			}


			else if ( (String)reason == GROUPS_CHANGED )
			{
				DefaultMutableTreeNode node = getNodeOfObject(observed);
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
				int index = parent.getIndex(node) ;
				if(node == null)
				{
				//	System.out.println("ERROR: Node not found in treeUpdate:");
					return;
				}	
				DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(ds);

				model.removeNodeFromParent(node);
                		( model ).insertNodeInto(level2,parent,index);
				tree.expandRow(index);
                		for (int k = 0; k < ds.getNum_entries() ; k++)
                		{
                     		Data entry = ds.getData_entry(k);
                     		DefaultMutableTreeNode level3 = new DefaultMutableTreeNode(entry); 
                     		index = level2.getChildCount();
                     		( model ).insertNodeInto(level3,level2,index);
                		}  




			}


     			else
     			{
			//	System.out.println("Error: Tree update called with wrong reason");

     			}

      		return; 
     		}          	
   	}
 }
