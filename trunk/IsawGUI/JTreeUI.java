/*
 * @(#)JTreeUI.java     1.0  99/09/02  Alok Chatterjee
 *
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

    public  JTreeUI(JPropertiesUI jpui, JCommandUI jcui, CommandPane cp) 
    {
		this.jpui = jpui;
		this.jcui = jcui;  
            this.cp = cp;
            setLayout(new GridLayout(1,1));
            root = new DefaultMutableTreeNode("Session");
            DefaultMutableTreeNode level1 = new DefaultMutableTreeNode( "Modified DataSet(s)");
            model = new DefaultTreeModel(root);
            model.insertNodeInto(level1, root, 0);
            tree = new JTree(model);
            tree.setShowsRootHandles(true);
           // tree.putClientProperty("JTree.lineStyle", "Horizontal");
	   	tree.putClientProperty("JTree.lineStyle", "Angled");
            JScrollPane pane = new JScrollPane(tree);

 		root1 = new DefaultMutableTreeNode("TreeLog");
        	model1 = new DefaultTreeModel(root1);
		logTree = new JTree(model1);
        	logTree.setShowsRootHandles(true);
	   	logTree.putClientProperty("JTree.lineStyle", "Angled");
		JScrollPane pane1 = new JScrollPane(logTree);
		setBorder(new CompoundBorder(new EmptyBorder(4,4,4,4), new EtchedBorder (EtchedBorder.RAISED)));

           /* DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();		
              renderer.setLeafIcon(new ImageIcon("C://new-orange.gif"));
              tree.setCellRenderer(renderer);
	    */
	     jdvui = new JDataViewUI();
 	     jtp = new JTabbedPane();
           jtp.addTab("Runfiles", pane);
	    // jtp.addTab("Operations Log", pane1);
		add(jtp);

     }// End of Constructor
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
        {int[] selectedRows = tree.getSelectionRows();
        //System.out.println("Tree is entered");
       // if(tree == null) 
	 // 	System.out.println("Tree is null");
       // else System.out.println("Tree is NOT null");
      
        selectedPath =  tree.getPathForRow(selectedRows[0]); 
        
        return (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
        }
        else 
        {
            return null;
        }
	  }//getSelectedNode
	  


     public JTree getTree()
     {
        return tree;  
     }
     
     public TreeModel getTreeModel()
     {
        return model;  
     }
     
     public void addDataSets(DataSet[] dss, String name)
     {
        DefaultMutableTreeNode level1 = new DefaultMutableTreeNode(name);
        int index = root.getChildCount();
        ( model).insertNodeInto(level1,root,index);
            for (int i = 0; i<dss.length; i++)
            { 

                cp.addDataSet(dss[i]);
                DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(dss[i]);
                index = level1.getChildCount();
                ( model ).insertNodeInto(level2,level1,index);
			

                for (int k = 0; k < dss[i].getNum_entries() ; k++)
                {
                     Data entry = dss[i].getData_entry(k);

                     DefaultMutableTreeNode level3 = new DefaultMutableTreeNode(entry); 
                     index = level2.getChildCount();
                     ( model ).insertNodeInto(level3,level2,index);
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
                              System.out.println("Selected block" +i);
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
