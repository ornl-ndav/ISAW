/*
 * @(#)JCommandGUI.java     1.0  99/09/02  Alok Chatterjee
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
import javax.swing.table.*;
import java.util.*;
import IPNS.Runfile.*;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JCommandUI  extends JPanel  implements Serializable
{
     private JTree logTree;
     private DefaultMutableTreeNode root;
     private JTable table;
     private JTabbedPane jtp;
     private JTextField textArea;
     DefaultTreeModel model;
     DefaultTableModel dtm ;
     
	 public JCommandUI()
	 {
	    setLayout(new GridLayout(1,1));
        root = new DefaultMutableTreeNode("TreeLog");
        model = new DefaultTreeModel(root);
        
        dtm = new DefaultTableModel();
       
        table = new JTable(dtm);
        logTree = new JTree(model);
        logTree.setShowsRootHandles(true);
        
        JScrollPane sp = new JScrollPane(table);
        JScrollPane pane = new JScrollPane(logTree);
        
        Runtime rt = Runtime.getRuntime();         
	    textArea = new JTextField("Total JVM Memory in bytes = "+ rt.totalMemory()+"\n"
	                             +"Free JVM Memory in bytes = "+ rt.freeMemory()+"\n"
	                             +"\n"
	                             +"Java Version = "+ System.getProperty("java.version") +"\n"
	                             +"Java Vendor = "+ System.getProperty("java.vendor") +"\n"
	                           //  +"Java Vendor URL = "+ System.getProperty("java.version.url") +"\n"
	                           //  +"Java Home = "+ System.getProperty("java.version.home") +"\n"
	                             +"Java Class Version = "+ System.getProperty("java.class.version")+"\n"
	                             +"Java ClassPath = "+ System.getProperty("java.class.path") +"\n"
	                             +"Java Home = "+ System.getProperty("java.home") +"\n"
	                             +"\n"
	                             +"User Home = "+ System.getProperty("user.home") +"\n"
	                             +"User Directory = "+ System.getProperty("user.dir") +"\n"
	                             +"User Name = "+ System.getProperty("user.name") +"\n"
	                             +"\n"
	                             +"File Separator= "+ System.getProperty("file.separator")+"\n"
	                             +"Path Separator= "+ System.getProperty("path.separator")+"\n"
	                             +"\n"
	                             +"Operating System Name= "+ System.getProperty("os.name")+"\n"
	                             +"Operating System Architecture= "+ System.getProperty("os.arch")+"\n"
	                             +"Operating System Version= "+ System.getProperty("os.version")
	                             );
      
        JScrollPane ta = new JScrollPane(textArea);
        
        //pane.setPreferredSize(new Dimension(300,100));
        //pane.setMinimumSize(new Dimension(50,50));
       // add(pane);
       DetectorInfo di = new DetectorInfo();
       
        jtp = new JTabbedPane();
        jtp.addTab("Operations Log", pane);
        jtp.addTab("Detector Info", sp);
        jtp.addTab("System Properties", ta);
        add(jtp);
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
              //  System.out.println(" log entries  :" +ds.getOp_log().getEntryAt(i));
                 DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(ds.getOp_log().getEntryAt(i));
                 model.insertNodeInto(level2,level1,i);
                 
                logTree.expandRow(i);
                logTree.expandRow(1);
            }
          
            
            return (JTree)logTree;    
	 }	 
	 
	 public JTable showDetectorInfo(DataSet ds)
	 {
               
            try{
                    System.out.println("No. of entries = "+ds.getNum_entries());
                       
                       Object[][] detParamList = new Object[ds.getNum_entries()][7];
                    
        
      for (int i = 0; i < ds.getNum_entries(); i++) 
                        {
                            AttributeList  attr_list = ds.getData_entry(i).getAttributeList();
                           
                            detParamList[i][0] = new Integer(((Integer)(attr_list.getAttributeValue(Attribute.GROUP_ID))).intValue());
                            detParamList[i][1] = new Float(((Float)(attr_list.getAttributeValue(Attribute.RAW_ANGLE))).floatValue());
                            detParamList[i][2] = new Float(((Float)(attr_list.getAttributeValue(Attribute.INITIAL_PATH))).floatValue());
                            
                          
      //                      detParamList[i][3] = new Float(((Float)(attr_list.getAttributeValue(Attribute.NUM_CHANNELS))).floatValue());
      //                      detParamList[i][4] = new Float(((Float)(attr_list.getAttributeValue(Attribute.TOTAL_COUNT))).floatValue());
                            
                          //  detParamList[i][4] = new Float(((Float)(attr_list.getAttributeValue(Attribute.ENERGY_IN))).floatValue());
                          //  detParamList[i][5] = new Float(((Float)(attr_list.getAttributeValue(Attribute.RAW_ANGLE))).floatValue());      
                       }

				         String[] columnHeading = {"ID", "Raw Angle", "Flight Path",//"Start:Time(ms)", "End:Time(ms)"
				              //         , "Number of Channels", "Total Count"
							};

	                    DefaultTableModel dtm = new DefaultTableModel(detParamList, columnHeading);
	                    table.setModel(dtm);
                        table.setSize( 200, 200 );
      
            }
            
             catch(Exception e){};
           
             return (JTable)table;  
	 }

}
