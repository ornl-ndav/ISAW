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
import DataSetTools.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.math.*;
import DataSetTools.viewer.*;
import IPNS.Runfile.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;
import java.awt.datatransfer.*;
//import java.awt.dnd.*;
//import java.awt.datatransfer.*;


/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JTreeUI extends JPanel implements Serializable // ,DragGestureListener,
                                              //  DragSourceListener
{
    protected JPopupMenu m_popup;
    protected Action m_action;
    
    private JTree tree;
    private DefaultMutableTreeNode root;
    private int ds_tag;
    DefaultTreeModel model;
    public  JTreeUI() 
    { 
            /*DragSource dragSource = DragSource.getDefaultDragSource();

		    dragSource.createDefaultDragGestureRecognizer(
					this, // component where drag originates
					DnDConstants.ACTION_COPY_OR_MOVE, // actions
					this); // drag gesture recognizer
			*/		
	
           
		//	m_popup = new JPopupMenu();
         //   Action a1 = new AbstractAction("Delete") { 
          //  public void actionPerformed(ActionEvent e) {
          //      tree.repaint();}
          //      };
        //    m_popup.add(a1);
             
            ds_tag = 0;
            setLayout(new GridLayout(1,1));
            root = new DefaultMutableTreeNode("Session");
            
            DefaultMutableTreeNode level1 = new DefaultMutableTreeNode( "Modified DataSet(s)");
            //root.add(level1);
            
            model = new DefaultTreeModel(root);
            model.insertNodeInto(level1, root, 0);
            tree = new JTree(model);
            tree.setShowsRootHandles(true);
            
           // tree.add(m_popup);
           // tree.addMouseListener(new PopupTrigger());
            
            tree.putClientProperty("JTree.LineStyle", "Horizontal");
           // tree.setLayout(new BorderLayout());
           // tree.setBorder(
			//BorderFactory.createTitledBorder(""));
            JScrollPane pane = new JScrollPane(tree);
            add(pane);

           /* DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		
              renderer.setOpenIcon(new ImageIcon("C://Doggie.gif"));
              renderer.setClosedIcon(new ImageIcon("C://Devil.gif"));
              renderer.setLeafIcon(new ImageIcon("C://new-orange.gif"));
              tree.setCellRenderer(renderer);
	    */
            
        
 
    }// End of Constructor
    
 
  /*      public void dragGestureRecognized(DragGestureEvent e) {
		        // drag anything ...
		        e.startDrag(DragSource.DefaultCopyDrop, // cursor
			        new StringSelection(getFilename()), // transferable
			        this);  // drag source listener
	        }
	        public void dragDropEnd(DragSourceDropEvent e) {}
	        public void dragEnter(DragSourceDragEvent e) {}
	        public void dragExit(DragSourceEvent e) {}
	        public void dragOver(DragSourceDragEvent e) {}
	        public void dropActionChanged(DragSourceDragEvent e) {}

         */
 
 
        
      public DefaultMutableTreeNode getSelectedNode()
	  {
	    TreePath selectedPath = null;
	    if(tree.getSelectionCount()>0)
        {int[] selectedRows = tree.getSelectionRows();
        System.out.println("Tree is entered");
        if(tree == null) System.out.println("Tree is null");
        else System.out.println("Tree is NOT null");
      
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
                AttributeList attr_list = dss[i].getAttributeList();
                attr_list.setAttribute(new IntAttribute(Attribute.DS_TAG, get_ds_tag()));
                dss[i].setAttributeList(attr_list);
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
            
            }   
     }
     
     
     
     
     
     public void addDataSet(DataSet ds)
     {
        DefaultMutableTreeNode level1 = (DefaultMutableTreeNode)root.getChildAt(0);
        int index = root.getChildCount();
        
                AttributeList attr_list = ds.getAttributeList();
                attr_list.setAttribute(new IntAttribute(Attribute.DS_TAG, get_ds_tag()));
                ds.setAttributeList(attr_list);
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
     
     public void openDataSet(DataSet ds, String name)
     {
        
        DefaultMutableTreeNode level1 = new DefaultMutableTreeNode(name);
        int index = root.getChildCount();
        ( model ).insertNodeInto(level1,root,index);

            {
                AttributeList attr_list = ds.getAttributeList();
                attr_list.setAttribute(new IntAttribute(Attribute.DS_TAG, get_ds_tag()));
                ds.setAttributeList(attr_list);
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
     
      public int get_ds_tag()
       {
        ds_tag++;
        return ds_tag;
       }
       
       
   /*  class PopupTrigger extends MouseAdapter 
     {
        public void mouseReleased(MouseEvent e) 
        {
            if (e.isPopupTrigger()) 
            {
                int x = e.getX();
                int y = e.getY();
                TreePath path = tree.getPathForLocation(x, y);
                m_popup.show(tree, x, y);

            }
        }
    }
     
     */
     
 }
