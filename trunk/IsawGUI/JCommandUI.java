/*
 * @(#)JCommandUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 *
 */

package IsawGUI;

import java.awt.*;
import java.awt.Color.*;
import DataSetTools.dataset.*;
import DataSetTools.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.JTree.*;
import java.util.zip.*;

public class JCommandUI  extends JPanel  implements Serializable
{
     private JTree logTree;
     private DefaultMutableTreeNode root;
     DefaultTreeModel model;
     
	 public JCommandUI()
	 {
	    setLayout(new GridLayout(1,1));
        root = new DefaultMutableTreeNode("TreeLog");
         
         
        model = new DefaultTreeModel(root);
       
        logTree = new JTree(model);
        JScrollPane pane = new JScrollPane(logTree);
        //pane.setPreferredSize(new Dimension(300,100));
        //pane.setMinimumSize(new Dimension(50,50));
        add(pane);
	 }
	 
	 public JTree showLog(DataSet ds)
	 {
            DefaultMutableTreeNode level1 = new DefaultMutableTreeNode(ds); 
            if(root.getChildCount()>0)
             {
                DefaultMutableTreeNode dsnode =  (DefaultMutableTreeNode)root.getChildAt(0);
                model.removeNodeFromParent(dsnode);
             }
            model.insertNodeInto(level1, root, 0);
            int num = ds.getOp_log().numEntries();
            System.out.println("Num of log entries" +num);
            for (int i = 0; i<num; i++)
            {
                System.out.println(" log entries  :" +ds.getOp_log().getEntryAt(i));
                 DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(ds.getOp_log().getEntryAt(i));
                 model.insertNodeInto(level2,level1,i);
           logTree.expandRow(i);
           logTree.expandRow(1);
            }
            
            return (JTree)logTree;    
	 }	 
}
