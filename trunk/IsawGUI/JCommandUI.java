/*
 * $Id$
 *
 * $Log$
 * Revision 1.12  2001/08/08 20:22:15  dennis
 * Now gets build date from DataSetTools/util/SharedData
 *
 * Revision 1.11  2001/07/31 19:38:16  neffk
 * the setTab() method now inserts a tab at index zero (0) instead
 * of adding it on the end of the list.  this has the added effect
 * of popping the new tab to the top.  however, this is a poor
 * solution... if the tabs are arranged in some other order, this won't
 * actually bring the new tab up the surface. also, if the menu item
 * that invlokes new live data servers should check if this one exists
 * and if it does, just pop the tab to the front instead of tring to
 * make a second.  there are some additional formatting changes, also.
 *
 * -----------
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 */
 
package IsawGUI;

import java.awt.*;
import java.awt.Color.*;
import DataSetTools.dataset.*;
import DataSetTools.*;
import DataSetTools.components.ui.*;
import DataSetTools.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.JTree.*;
import java.util.zip.*;
import javax.swing.table.*;
import java.util.*;
import IPNS.Runfile.*;
import javax.swing.border.*;
import Command.*;


/**
 * The class that displays various JTabbed panes containing 
 * different information. Presently contains logtree, detector info, 
 * System info and a command pane to run macro scripts.
 */
public class JCommandUI  extends JPanel  implements IObserver, Serializable
{
     private JTree logTree;
     private DefaultMutableTreeNode root;
     private JTable table;
     public JTabbedPane jtp;
     private JTextField textArea;
     private JPanel livePanel;
     Document sessionLog=null;
     DefaultTreeModel model;
     DefaultTableModel dtm ;
     DataSet current_ds = null;
     Vector tab_titles;
     JPropertiesUI jpui;
     
         /** @associates <{IsawGUI.JCommandUI}> */
        // private com.sun.java.util.collections.TreeSet lnkJCommandUI;
     
  public JCommandUI(CommandPane cp, Document sessionLog, JPropertiesUI jpui)
  {

          this.sessionLog = sessionLog;
         JTextArea sessiontext = new JTextArea(sessionLog);
         sessiontext.setEditable(false);
         JScrollPane njsp = new JScrollPane(sessiontext);
     setLayout(new GridLayout(1,1));
        root = new DefaultMutableTreeNode("TreeLog");
        model = new DefaultTreeModel(root);
        String server_name = System.getProperty("Inst1_Path");
        dtm = new DefaultTableModel();
        table = new JTable(dtm);
        logTree = new JTree(model);
        logTree.setShowsRootHandles(true);

        JScrollPane sp = new JScrollPane(table);
        JScrollPane pp = new JScrollPane(jpui.getPropsTable());
        JScrollPane pane = new JScrollPane(logTree);
        //setBorder(new BevelBorder (BevelBorder.RAISED));
   setBorder(new CompoundBorder( new EmptyBorder(4,4,4,4), 
                                                      new EtchedBorder (EtchedBorder.RAISED)));


        Runtime rt = Runtime.getRuntime();         
     textArea = new JTextField(
       "Build Date: "+ SharedData.BUILD_DATE + "\n" +
       "Total JVM Memory in bytes = "+ rt.totalMemory() + "\n" +
       "Free JVM Memory in bytes = "+ rt.freeMemory() + "\n" +
       "\n" +
       "Java Version = "+ System.getProperty("java.version") + "\n" +
       "Java Vendor = "+ System.getProperty("java.vendor") + "\n" +
       "Java Class Version = "+ System.getProperty("java.class.version") + "\n" +
       "Java ClassPath = "+ System.getProperty("java.class.path") + "\n" +
       "Java Home = "+ System.getProperty("java.home") + "\n" +
       "\n" +
       "User Home = "+ System.getProperty("user.home") + "\n" +
       "User Directory = "+ System.getProperty("user.dir") + "\n" +
       "User Name = "+ System.getProperty("user.name") + "\n" +
       "\n" +
       "File Separator= "+ System.getProperty("file.separator") + "\n" +
       "Path Separator= "+ System.getProperty("path.separator") + "\n" +
       "\n" +
       "Operating System Name= "+ System.getProperty("os.name") + "\n" +
       "Operating System Architecture= "+ System.getProperty("os.arch") + "\n" +
       "Operating System Version= "+ System.getProperty("os.version")
     );
      
        JScrollPane ta = new JScrollPane(textArea);
       
     //   String tab_names [] = {"DataSet Log", "Session Log", "Detector Info", "System Properties", "CommandPane"};
        
       jtp = new JTabbedPane();
        jtp.addTab("Attributes", pp);
        jtp.addTab("DataSet Log", pane);
        jtp.addTab("Session Log", njsp);
        jtp.addTab("System Props", ta);
        jtp.addTab("Det Info", sp);
   jtp.addTab("Scripts", cp);

   //  for (int i=0; i<tab_names.length; i++)
  //   {
   //      String attr = tab_names[i];
        
   //       tab_titles.addElement(attr);
   //  }

//System.out.println("Tab names   " +tab_titles);
        add(jtp);
  }


  /**
   * adds a tab and pops it up to the front.
   */ 
  public void setTab( String tab_title, JPanel obj )
  {
//    jtp.addTab(tab_title, obj);
    jtp.insertTab( tab_title, null, obj, null, 0 );
  }


  /**
   * adds a tab and pops it up to the front.
   */ 
  public void setTab( String tab_title, JScrollPane obj )
  {
//    jtp.addTab(tab_title, obj);
    jtp.insertTab( tab_title, null, obj, null, 0 );
  }



  public void showLog(DataSet ds)
  {
  if(ds == current_ds)    //Only draw the log for a different dataset.
   return;
  current_ds = ds; 
            DefaultMutableTreeNode level1 = new DefaultMutableTreeNode(ds); 
            if(root.getChildCount()>0)
             {
                DefaultMutableTreeNode dsnode =  (DefaultMutableTreeNode)root.getChildAt(0);
                model.removeNodeFromParent(dsnode);
             }
            model.insertNodeInto(level1, root, 0);
            int num = ds.getOp_log().numEntries();
            // System.out.println("Num of log entries" +num);
            for (int i = 0; i<num; i++)
            {
              //  System.out.println(" log entries  :" +ds.getOp_log().getEntryAt(i));
                 DefaultMutableTreeNode level2 = new DefaultMutableTreeNode(ds.getOp_log().getEntryAt(i));
                 model.insertNodeInto(level2,level1,i);
                 
                logTree.expandRow(i);
                logTree.expandRow(1);
            }
  }  
  
  public JTable showDetectorInfo(DataSet ds)
  {
               
            try{
                    // System.out.println("No. of entries = "+ds.getNum_entries());
                       
                       Object[][] detParamList = new Object[ds.getNum_entries()][7];
                    
        
         for (int i = 0; i < ds.getNum_entries(); i++) 
                        {
                            AttributeList  attr_list = ds.getData_entry(i).getAttributeList();
                           
        detParamList[i][0] = new         Integer(((Integer)(attr_list.getAttributeValue(Attribute.GROUP_ID))).intValue());
                            detParamList[i][1] = new         Float(((Float)(attr_list.getAttributeValue(Attribute.RAW_ANGLE))).floatValue());
                            detParamList[i][2] = new         Float(((Float)(attr_list.getAttributeValue(Attribute.INITIAL_PATH))).floatValue());
                            
                          
         //   detParamList[i][3] = new 
//Float(((Float)(attr_list.getAttributeValue(Attribute.NUM_CHANNELS))).floatValue());
      //                      detParamList[i][4] = new //Float(((Float)(attr_list.getAttributeValue(Attribute.TOTAL_COUNT))).floatValue());
                            
                          //  detParamList[i][4] = new //Float(((Float)(attr_list.getAttributeValue(Attribute.ENERGY_IN))).floatValue());
                          //  detParamList[i][5] = new //Float(((Float)(attr_list.getAttributeValue(Attribute.RAW_ANGLE))).floatValue());  
    
                       }

 String[] columnHeading = {"ID", "Raw Angle", "Flight Path",//"Start:Time(ms)", "End:Time(ms)"
                  //         , "Number of Channels", "Total Count"
       };
  DefaultTableModel dtm = new DefaultTableModel(detParamList, columnHeading);
  table.setModel(dtm);
  table.setSize( 200, 200 );
      
            }
            
             catch(Exception e){};
  ExcelAdapter myAd = new ExcelAdapter(table);


             return (JTable)table;  
  }


  public void update( Object observed, Object reason )
  {
   // System.out.println("Inside update in JCommandUI");
    if ( !( reason instanceof String) )   // currently we only allow Strings
    {
//      System.out.println("Error: Tree update called with wrong reason");
      return;
    }
 
    if( observed instanceof DataSet )             
    {
      DataSet ds = (DataSet)observed;
//      System.out.println("Update in CommandUI is called :"+ds.toString());

    if ( (String)reason == DESTROY )
      System.out.print("");

    else if( (String)reason == SELECTION_CHANGED )
      showLog(ds);

    else
//      System.out.println("ERROR: Unsupported Tree Update:" + reason );

      return; 
    }           
  }


}
