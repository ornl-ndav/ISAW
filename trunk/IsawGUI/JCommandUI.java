/*
 * File: JCommandUI.java
 *
 * Copyright (C) 1999, Alok Chatterjee
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
 * Revision 1.18  2003/03/07 21:17:11  dennis
 * Now really destroys the LiveDataMonitor when the Exit button
 * is pressed, rather than just setting the reference to it
 * to null.
 *
 * Revision 1.17  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 * Revision 1.16  2002/03/08 23:02:15  dennis
 * System Properties display now uses JTextArea instead of
 * JTextField.  Needed for jdk 1.4.0
 *
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
import java.awt.event.*;

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
     private JTextArea textArea;
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
     textArea = new JTextArea(
       "Build Date: " + SharedData.BUILD_DATE + "\n" +

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
        jtp.addTab("Session Log", new JPanelwithToolBar( "Save", new MySaveHandler(),njsp));
        jtp.addTab("System Props", ta);
        //jtp.addTab("Det Info", sp);
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
    //jtp.insertTab( tab_title, null, obj, null, 0 );
    jtp.insertTab( tab_title, null, 
                  new JPanelwithToolBar( "EXIT",
                   new MyHandler( obj )                      
                   ,obj)
                 , null, 0 );

  }
 private class MySaveHandler implements ActionListener
   {
     public void actionPerformed( ActionEvent e )
       { JFileChooser Jf = new JFileChooser();
         int res =Jf.showSaveDialog( null );
         if( res != JFileChooser.APPROVE_OPTION)
           return;
         File f = Jf.getSelectedFile();
         Util util = new Util();
         util.saveDoc( sessionLog, f.toString());
        }
    }

  /**
   * adds a tab and pops it up to the front.
   */ 
  public void setTab( String tab_title, JScrollPane obj )
  {
//    jtp.addTab(tab_title, obj);
    jtp.insertTab( tab_title, null, 
                  new JPanelwithToolBar( "EXIT",
                   new MyHandler( obj )                      
                   ,obj)
                 , null, 0 );
  }

class MyHandler implements ActionListener
   {JComponent obj;
    public MyHandler( JComponent obj )
       {this.obj = obj;
       }
    public void actionPerformed(ActionEvent e)
       { 
         if ( obj instanceof LiveDataMonitor )
           ((LiveDataMonitor)obj).destroy();
         obj = null;
         System.gc();
         jtp.remove( jtp.getSelectedIndex()); 
      }
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
                       
                       Object[][] detParamList = new Object[ds.getNum_entries()][3];
                    
        
         for (int i = 0; i < ds.getNum_entries(); i++) 
           {
            AttributeList  attr_list = ds.getData_entry(i).getAttributeList();
                           
            detParamList[i][0] = new Integer(((Integer)(attr_list.getAttributeValue(Attribute.GROUP_ID))).
                                 intValue());
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
