 /*
  * File:  Isaw.java
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
  * Contact : Alok Chatterjee achatterjee@anl.gov>
  *           Intense Pulsed Neutron Source Division
  *           Argonne National Laboratory
  *           9700 S. Cass Avenue, Bldg 360
  *           Argonne, IL 60440
  *           USA
  *
  * 
  *
  * For further information, see http://www.pns.anl.gov/ISAW/>
  *
  * Modified:
  *
  *  $Log$
  *  Revision 1.30  2001/07/09 22:17:27  chatter
  *  Changed the User Interface
  *
  *  Revision 1.29  2001/06/27 20:10:43  chatter
  *  Macros Menu modified to use opMenu
  *
  *  Revision 1.28  2001/06/26 20:36:49  chatter
  *  Made Splash window run in its own thread (Dennis)
  *
  *  Revision 1.27  2001/06/25 23:14:57  chatter
  *  Added the 3D view menuitem
  *
  *  Revision 1.26  2001/06/25 22:13:51  chatter
  *  Changed the SelectedGraphView call to use the ViewManager
  *
  *  Revision 1.25  2001/06/25 20:09:26  chatter
  *  Splashpane and IsawProps.dat read in main
  *
  *  Revision 1.24  2001/06/20 21:43:47  chatter
  *  Loading of Nexus files added via the IsawGUI.util.loadRunfile method.
  *
  *  Revision 1.19  2001/06/14 14:54:01  chatter
  *  Removed Images_Directory and the jnexus.dll path from IsawProps.dat
  *
  *  Revision 1.18  2001/06/12 22:51:14  chatter
  *  Fixed code that sets up new IsawProps.dat file
  *
  *  Revision 1.17  2001/06/08 23:20:24  chatter
  *  Made Isaw an IObserver of LiveDataMonitor
  *
  *  Revision 1.16  2001/06/08 14:55:49  dennis
  *  Removed unused try{} catch{} block.
  *
  *  Revision 1.15  2001/06/08 14:08:49  chatter
  *  Added code to create a new IsawProps.dat file in the user home directory if one is not already present
  *
  *  Revision 1.14  2001/06/08 14:03:31  chatter
  *  Changed the name of the Properties file to IsawProps.dat
  *  Used arrays to fill up some of the menuitems
  *
  *  Revision 1.13  2001/06/06 14:32:39  chatter
  *  Added the gpl license
  *
  *
  * 0.7 2000/06/01 Added comments in Javadoc format. Also changed the loader
  *			to make use of the loader in the Util class in IsawGUI package.
  *                 
  */
 
 package IsawGUI;
 
 import DataSetTools.gsastools.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 import javax.swing.JTree.*;
 import javax.swing.plaf.metal.MetalLookAndFeel.*;
 import java.util.*;
 import java.util.EventObject.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.print.*;
 import java.io.*;
 import DataSetTools.retriever.*;
 import DataSetTools.dataset.*;
 import DataSetTools.operator.*;
 import DataSetTools.instruments.*;
 import java.util.zip.*;
 import DataSetTools.viewer.*;
 import DataSetTools.operator.*;
 import DataSetTools.util.*;
 import DataSetTools.components.ui.*;
 import ChopTools.*;
 import IPNS.Runfile.*;
 import java.applet.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.*;
 import java.lang.*;
 import java.io.IOException; 
 //import javax.help.*;
 import Command.*;
 import javax.swing.text.*;
 import java.applet.*;
 import NetComm.*;
 import DataSetTools.components.ParametersGUI.*;
 
 /**
  * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
  * ChopTools and graph packages.
  *
  * @version 0.7 
  */
 
 public class Isaw extends JFrame implements Serializable, IObserver
 {
     
     JTreeUI jtui;
     JPropertiesUI jpui;
     JDataViewUI jdvui;
     JCommandUI jcui;
     JMenu oMenu = new JMenu("Operations");
     JPopupMenu popup ;
     private PageFormat mPageFormat;
     JTree tree ;
     JMenuItem mi;
     String dirName = null;
     boolean set_selection = false ;
     TreeSelectionModel selectionModel;
     Util util;
     CommandPane cp;
     MyInternalFrame internalframe;
     IObserver my_Isaw;
     Object Script_Path, Data_Directory, Help_Directory, Default_Instrument, Instrument_Macro_Path, 
     User_Macro_Path, Image_Path;
     Document sessionLog = new PlainDocument();
     JTextArea propsText = new JTextArea(5,20);
     JFrame kp;

     /**
      * Creates a JFrame that displays different Isaw components.
      *
      */
     public Isaw() 
     {
        
        super("ISAW version 1.1");
        my_Isaw = this;

    /*    
 	  PrinterJob pj = PrinterJob.getPrinterJob();
        mPageFormat = pj.defaultPage();
    */
        
       // setupMenuBar();

 	  util = new Util(); 
        Vector mm = util.listProperties();
        JScrollPane tt = util.viewProperties();
        cp = new Command.CommandPane();
        cp.addIObserver(this);
        cp.setLogDoc(sessionLog);

        jpui = new JPropertiesUI();
        jpui.setPreferredSize( new Dimension(200, 200) );
        jpui.setMinimumSize(new Dimension(20, 50));

        jdvui = new JDataViewUI();
        jdvui.setPreferredSize(new Dimension(700, 500));
        SwingUtilities.updateComponentTreeUI(jdvui);

        jcui = new JCommandUI(cp, sessionLog,jpui);
        jcui.setPreferredSize( new Dimension( 700, 50 ) );
        jcui.setMinimumSize(new Dimension(20, 50));  

        jtui = new JTreeUI(jpui,jcui, cp);
        jtui.setPreferredSize(new Dimension(200, 500));
        jtui.setMinimumSize(new Dimension(20, 50));
        tree = jtui.getTree();
        tree.addTreeSelectionListener(new TreeSelectionHandler());
 	  selectionModel = tree.getSelectionModel();
        

        setupMenuBar();        

        JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftPane.setOneTouchExpandable(false);
 	  rightPane.setOneTouchExpandable(false);
 
       // rightPane.setTopComponent(jdvui);
        rightPane.setBottomComponent(jcui);
 	  rightPane.setResizeWeight( .6);
       // leftPane.setBottomComponent(jpui);
        leftPane.setTopComponent(jtui);
        leftPane.setResizeWeight( .7);
 
        JSplitPane sp= new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
                                     leftPane, rightPane);
        
        sp.setOneTouchExpandable(true);
        Container con = getContentPane();
        con.add(sp);
        
     } //Isaw
    
 
     
     /**
     * Sets up the menubar that is used for all operations on DataSets
     * 
     */
     private void setupMenuBar() 
      {
        
         JMenuBar menuBar = new JMenuBar();
         JMenu fMenu = new JMenu("File");
         JMenu eMenu = new JMenu("Edit");
         JMenu vMenu = new JMenu("View");
         
         JMenuItem imageView = new JMenuItem("Image View");
         JMenuItem s_graphView = new JMenuItem("Scrolled Graph View");
         JMenu instrumentInfoView = new JMenu("Instrument Info");
         JMenuItem threeDView = new JMenuItem("3D View");
 	   JMenu LiveData = new JMenu("Load Live Data"); 
         Script_Class_List_Handler SP= new Script_Class_List_Handler();      
         opMenu macrosMenu = new opMenu(SP, new DSgetArray(jtui), sessionLog , this);
         macrosMenu.setOpMenuLabel("Macros");

         JMenu optionMenu = new JMenu("Options");
         JMenu wMenu = new JMenu("Window");
         JMenu hMenu = new JMenu("Help");
 
         JMenuItem fileRunfile = new JMenuItem("Load Entire Runfile(s)");
         JMenuItem Runfile = new JMenuItem("Load Data File(s)"); 
         
         JMenuItem fileRunfiles = new JMenuItem("Load Selected Data");
         JMenuItem fileLoadDataset = new JMenuItem("Load ISAW Data");
         JMenuItem fileSaveData = new JMenuItem("Save ISAW Data");
         JMenuItem fileSaveDataAs = new JMenuItem("Export GSAS File");
         JMenuItem imagePrint = new JMenuItem("Print to File");
         JMenuItem fileExit = new JMenuItem("Exit");
         JMenuItem script_loader = new JMenuItem("Load Script");       
         JMenuItem removeSelectedNode = new JMenuItem("Remove Selected Node");
         removeSelectedNode.setAccelerator(KeyStroke.getKeyStroke('X', 
 							KeyEvent.CTRL_MASK, true));
 	   JMenuItem editUndo = new JMenuItem("Undo");
         editUndo.setAccelerator(KeyStroke.getKeyStroke('Z', KeyEvent.CTRL_MASK, true));
 
         JMenuItem editAttributes = new JMenuItem("Edit Attributes");
         JMenuItem editProps = new JMenuItem("Edit IsawProps");
         JMenuItem editSetAttribute = new JMenuItem("Set Attributes");
         JMenuItem setGroupAttributes = new JMenuItem("Set Attribute For All Groups");
 	   JMenuItem clearSelection = new JMenuItem("Clear Selection");
 
         JMenuItem viewFileSeparator =  new JMenuItem("File Separator");
         JMenuItem viewLogView =  new JMenuItem("Log View");

         JMenuItem optionwindowsLook =  new JMenuItem("Windows Look");
         JMenuItem optionmetalLook =  new JMenuItem("Metal Look");
         JMenuItem optionmotifLook =  new JMenuItem("Motif Look");
         
         JMenuItem windowRestoreView =  new JMenuItem("Restore Views");
         JMenuItem windowMinimizeView =  new JMenuItem("Minimize Views");
         JMenuItem windowMaximizeView =  new JMenuItem("Maximize Views");
         JMenuItem windowCloseView =  new JMenuItem("Close Views");
         JMenuItem windowtileView = new JMenuItem("Tile Views Vertically");
         JMenuItem windowCascadeView = new JMenuItem("Cascade Views");
         JMenuItem helpISAW = new JMenuItem("About ISAW");
         
         JMenuItem iFrame = new JMenuItem("Internal Frame");
         JMenuItem eFrame = new JMenuItem("External Frame");
 
         JMenuItem graphView = new JMenuItem("SelectedGraph View");
         
         JMenuItem eFrame_sg = new JMenuItem("Scrolled Graph External Frame");

         JMenuItem iFrame_sg = new JMenuItem("Scrolled Graph Internal Frame");

         JMenuItem iFrame_threeD = new JMenuItem("3D View Internal Frame");
         JMenuItem eFrame_threeD = new JMenuItem("3D View External Frame");

         
         JMenuItem HRMECS = new JMenuItem("HRMECS Link");
         JMenuItem LRMECS = new JMenuItem("LRMECS Link");
         JMenuItem HIPD = new JMenuItem("HIPD Link");
         JMenuItem SAD = new JMenuItem("SAD Link");
         JMenuItem SCD = new JMenuItem("SCD Link");
         JMenuItem SAND = new JMenuItem("SAND Link");
         JMenuItem POSY1 = new JMenuItem("POSY1 Link");
         JMenuItem POSY2 = new JMenuItem("POSY2 Link");
         JMenuItem GLAD = new JMenuItem("GLAD Link");
         JMenuItem QENS = new JMenuItem("QENS Link");
         JMenuItem GPPD = new JMenuItem("GPPD Link");
         JMenuItem SEPD = new JMenuItem("SEPD Link");
         JMenuItem CHEXS = new JMenuItem("CHEXS Link");
         
         JMenuItem m_HRMECS = new JMenuItem("HRMECS ");
         JMenuItem m_LRMECS = new JMenuItem("LRMECS ");
         JMenuItem m_HIPD = new JMenuItem("HIPD ");
         JMenuItem m_SAD = new JMenuItem("SAD ");
         JMenuItem m_SCD = new JMenuItem("SCD ");
         JMenuItem m_SAND = new JMenuItem("SAND ");
         JMenuItem m_POSY1 = new JMenuItem("POSY1 ");
         JMenuItem m_POSY2 = new JMenuItem("POSY2 ");
         JMenuItem m_GLAD = new JMenuItem("GLAD ");
         JMenuItem m_QENS = new JMenuItem("QENS ");
         JMenuItem m_GPPD = new JMenuItem("GPPD ");
         JMenuItem m_SEPD = new JMenuItem("SEPD ");
         JMenuItem m_CHEXS = new JMenuItem("CHEXS ");
 
 
         fMenu.add(Runfile);
         fMenu.add(LiveData);
         fMenu.add(script_loader);
       //  fMenu.add(fileRunfile);
       //  fMenu.add(fileRunfiles);
         fMenu.add(fileLoadDataset);
         fMenu.addSeparator();
         fMenu.add(fileSaveData);
         fMenu.add(fileSaveDataAs);
         fMenu.add(imagePrint);
         fMenu.addSeparator();
         fMenu.add(fileExit);
 
        // eMenu.add(editUndo);
 	   eMenu.add(removeSelectedNode);
         eMenu.add(editAttributes);
         eMenu.add(editProps);
         eMenu.add(editSetAttribute);
         eMenu.add(setGroupAttributes);
  	   eMenu.add(clearSelection);
           
         //imageView.add(iFrame);
         //imageView.add(eFrame);
         //threeDView.add(iFrame_threeD);
        // threeDView.add(eFrame_threeD);

        // s_graphView.add(iFrame_sg);
        // s_graphView.add(eFrame_sg);
         
         instrumentInfoView.add(HRMECS);
         instrumentInfoView.add(GPPD);
         instrumentInfoView.add(SEPD);
         instrumentInfoView.add(LRMECS);
         instrumentInfoView.add(SAD);
         instrumentInfoView.add(SAND);
         instrumentInfoView.add(SCD);
         instrumentInfoView.add(GLAD);
         instrumentInfoView.add(HIPD);
         instrumentInfoView.add(POSY1);
         instrumentInfoView.add(POSY2);
         instrumentInfoView.add(QENS);
         instrumentInfoView.add(CHEXS);
         
         boolean found =true;
         for( int ii =1; (ii<14)&&found;ii++)
           {String SS =System.getProperty("Inst"+new Integer(ii).toString().trim()+"_Name");
            if( SS == null) found=false;
            else
              {JMenuItem dummy = new JMenuItem(SS);
               dummy.setToolTipText( System.getProperty("Inst"+new Integer(ii).toString().trim()+"_Path"));
               dummy.addActionListener( new MenuItemHandler());
               LiveData.add(dummy);
               }
            }
        
         
         vMenu.add(imageView);
         vMenu.add(s_graphView);
         vMenu.add(graphView);
         vMenu.add(threeDView);
         vMenu.add(instrumentInfoView);         
           
         optionMenu.add(optionwindowsLook);
         optionMenu.add(optionmetalLook);
         optionMenu.add(optionmotifLook);
         
        /* wMenu.add(windowRestoreView);
         wMenu.add(windowMinimizeView);
         wMenu.add(windowMaximizeView);
         wMenu.add(windowCloseView);
         wMenu.add(windowCascadeView);
         wMenu.add(windowtileView);
          */     
         hMenu.add(helpISAW);
         fileExit.addActionListener(new MenuItemHandler());
         Runfile.addActionListener(new LoadMenuItemHandler());
 	   LiveData.addActionListener(new LoadMenuItemHandler());
 
         fileRunfile.addActionListener(new LoadMenuItemHandler());
         fileRunfiles.addActionListener(new LoadMenuItemHandler());
 
         script_loader.addActionListener(new ScriptLoadHandler(this));
 
         fileSaveData.addActionListener(new MenuItemHandler());
         fileSaveDataAs.addActionListener(new MenuItemHandler());
         imagePrint.addActionListener(new MenuItemHandler());
         
         graphView.addActionListener(new MenuItemHandler()); 
              
         s_graphView.addActionListener(new MenuItemHandler()); 
         //iFrame_sg.addActionListener(new MenuItemHandler()); 
         //eFrame_sg.addActionListener(new MenuItemHandler()); 

        // iFrame_threeD.addActionListener(new MenuItemHandler()); 
       //  eFrame_threeD.addActionListener(new MenuItemHandler()); 
         threeDView.addActionListener(new MenuItemHandler()); 
         imageView.addActionListener(new MenuItemHandler());  
         
         HRMECS.addActionListener(new MenuItemHandler());
         LRMECS.addActionListener(new MenuItemHandler());
         HIPD.addActionListener(new MenuItemHandler());
         
         GPPD.addActionListener(new MenuItemHandler());
         SEPD.addActionListener(new MenuItemHandler());
         SAND.addActionListener(new MenuItemHandler());
         
         SAD.addActionListener(new MenuItemHandler());
         SCD.addActionListener(new MenuItemHandler());
         POSY1.addActionListener(new MenuItemHandler());
         
         POSY2.addActionListener(new MenuItemHandler());
         QENS.addActionListener(new MenuItemHandler());
         GLAD.addActionListener(new MenuItemHandler());
         CHEXS.addActionListener(new MenuItemHandler());
         
         
         m_HRMECS.addActionListener(new MenuItemHandler());
         m_LRMECS.addActionListener(new MenuItemHandler());
         m_HIPD.addActionListener(new MenuItemHandler());
         
         m_GPPD.addActionListener(new MenuItemHandler());
         m_SEPD.addActionListener(new MenuItemHandler());
         m_SAND.addActionListener(new MenuItemHandler());
         
         m_SAD.addActionListener(new MenuItemHandler());
         m_SCD.addActionListener(new MenuItemHandler());
         m_POSY1.addActionListener(new MenuItemHandler());
         
         m_POSY2.addActionListener(new MenuItemHandler());
         m_QENS.addActionListener(new MenuItemHandler());
         m_GLAD.addActionListener(new MenuItemHandler());
         m_CHEXS.addActionListener(new MenuItemHandler());
 
 
         viewFileSeparator.addActionListener(new MenuItemHandler());
         viewLogView.addActionListener(new MenuItemHandler());
         optionmetalLook.addActionListener(new MenuItemHandler());
         optionmotifLook.addActionListener(new MenuItemHandler());
         optionwindowsLook.addActionListener(new MenuItemHandler());
         fileLoadDataset.addActionListener(new MenuItemHandler());
         removeSelectedNode.addActionListener(new MenuItemHandler());
 	   editUndo.addActionListener(new MenuItemHandler());
         editProps.addActionListener(new AttributeMenuItemHandler());
         editAttributes.addActionListener(new AttributeMenuItemHandler());
         editSetAttribute.addActionListener(new AttributeMenuItemHandler());
         setGroupAttributes.addActionListener(new AttributeMenuItemHandler());
 	   clearSelection.addActionListener(new AttributeMenuItemHandler());
         
         windowRestoreView.addActionListener(new MenuItemHandler());
         windowMinimizeView.addActionListener(new MenuItemHandler());
         windowMaximizeView.addActionListener(new MenuItemHandler());
         windowCloseView.addActionListener(new MenuItemHandler());
         windowCascadeView.addActionListener(new MenuItemHandler());
         windowtileView.addActionListener(new MenuItemHandler());
         helpISAW.addActionListener(new MenuItemHandler());
        
         menuBar.add(fMenu);
         menuBar.add(eMenu);
         menuBar.add(vMenu);
         menuBar.add(optionMenu);
         menuBar.add(oMenu);
         menuBar.add(macrosMenu);
         menuBar.add(wMenu);
         menuBar.add(hMenu);
         setJMenuBar(menuBar);
      }
 
     /**
     * Adds DataSets to the JTree and makes the tree, properties and
     * command userinterface observers of the datasets.
     *
     * @param dss	Array of DataSets
     * @param name String identifying the Runfile
     */
     public void addDataSets(DataSet[] dss, String name)
     {
       jtui.addDataSets(dss,name);
 	for(int i =0; i<dss.length; i++)
 	{
 	  dss[i].addIObserver(jtui);
 	  dss[i].addIObserver(jpui);
 	  dss[i].addIObserver(jcui);
 	}
     }
 
    /**
     * Adds a modified DataSet to the JTree.
     *
     * @param ds	The modified DataSet to be added to the tree.
     * 
     */
    public void addDataSet(DataSet ds)
    {
 	jtui.addDataSet(ds);	
    }
 
    /**
     * Implementation of the "Edit Attribute" menu item's actions.
     *
     */        
    private class AttributeMenuItemHandler implements ActionListener 
    {
      public void actionPerformed(ActionEvent ev) 
      { 
        String s=ev.getActionCommand();
             if(s=="Edit Attributes")
                 {   
                     DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                     JTree tree = jtui.getTree();
                     DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                      if(mtn.getUserObject() instanceof DataSet || mtn.getUserObject() instanceof Data)
                       {
                          Object obj = mtn.getUserObject();
                          JAttributesDialog  jad = new JAttributesDialog(((IAttributeList)obj).getAttributeList(), s);
                          ((IAttributeList)obj).setAttributeList(jad.getAttributeList());
                       }
                 }
                 if(s=="Edit IsawProps")
                 {   
                  propsDisplay();
                 }
                 

                 
                 if(s=="Set Attributes")
                 {   
                     DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                    
                     JTree tree = jtui.getTree();
                     DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                     
                      if(  mtn.getUserObject() instanceof DataSet || 
                           mtn.getUserObject() instanceof Data    )
                      
                       {
                         Object obj = mtn.getUserObject();
                         AttributeList new_list = makeNewAttributeList();
                         JAttributesDialog  jad = new JAttributesDialog(new_list,s);
                         AttributeList current_list = ((IAttributeList)obj).getAttributeList();
                         new_list = jad.getAttributeList();
                         
                         for (int i = 0; i<new_list.getNum_attributes(); i++)
                         {
                            Attribute attr = new_list.getAttribute(i);
                            
                    //To Add more instances add following code here later----
                            if(attr instanceof FloatAttribute)
                            {
                             float val = ((FloatAttribute)attr).getFloatValue();
                             if(!Float.isNaN(val))
                                current_list.setAttribute(attr);
                            }
                         }
                         ((IAttributeList)obj).setAttributeList(current_list);
                         
                       }
                 }
                 
 	if(s=="Clear Selection")
                 {   
                     selectionModel.clearSelection();
                 }
 
                 if(s=="Set Attribute For All Groups")
                 {   
                     DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                    
                     JTree tree = jtui.getTree();
                     DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                     
                      if(  mtn.getUserObject() instanceof DataSet  ) 
                       {
                         Object obj = mtn.getUserObject();
                         DataSet ds = (DataSet)obj;
                         AttributeList new_list = makeNewAttributeList();
                         JAttributesDialog  jad = new JAttributesDialog(new_list,s);
                         new_list = jad.getAttributeList();
                         
                         for (int k=0; k<ds.getNum_entries(); k++)
                         {
                             Data data = ds.getData_entry(k);
                             AttributeList current_list = data.getAttributeList();
                             for (int i = 0; i<new_list.getNum_attributes(); i++)
                                 {
                                 Attribute attr = new_list.getAttribute(i);
                                    
                         //Add more instances and following code here later----
                                 if(attr instanceof FloatAttribute)
                                 {   float val = ((FloatAttribute)attr).getFloatValue();
                                     if(!Float.isNaN(val))
                                     current_list.setAttribute(attr);
                                 }
                             }
                             data.setAttributeList(current_list);
                         } 
                       }
                 }     
        }
    }
 
 
    /**
     * Sets up the menubar that is used for all operations on DataSets.
     *
     * @param dss	Array of DataSets
     * @param name String identifying the Runfile
     */   
    private AttributeList makeNewAttributeList()
    {
         AttributeList new_list = new AttributeList();
         FloatAttribute attr = new FloatAttribute(Attribute.TEMPERATURE, Float.NaN);
         new_list.addAttribute(attr);
         attr = new FloatAttribute(Attribute.PRESSURE, Float.NaN);
         new_list.addAttribute(attr);
         attr = new FloatAttribute(Attribute.MAGNETIC_FIELD, Float.NaN);
         new_list.addAttribute(attr);
         return new_list;
    }
    
    private class ScriptLoadHandler implements ActionListener 
    {  Isaw IS;
       
 
       public ScriptLoadHandler( Isaw IS)
        { this.IS = IS;
       //  IS.setState(IS.ICONIFIED);
        }
         public void actionPerformed(ActionEvent ev) 
         
         {
             
             String s=ev.getActionCommand();
             
             if(s=="Load Script")
                {
 			String SS = System.getProperty("Script_Path");
                   if(SS == null)
                      SS =System.getProperty("user.home");
                         JFileChooser fc = new JFileChooser();
                   fc.setCurrentDirectory(new File(SS));
 
 			String str = (String)Script_Path ;
 			//System.out.println("Properties file loaded from " +str);
       		//JFileChooser fc = new JFileChooser(new File(SS));
 			fc.setFileFilter(new scriptFilter());
 			String fname, filename;
  			try
                     {
 				int state = fc.showOpenDialog(null);
                         if (state ==0 && fc.getSelectedFile() != null)
 			      {
                    	  File f = fc.getSelectedFile();
                           filename =f.toString();
                           fname = f.getName();
                         
                           setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                           
                           setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                              }
                         else return;
                     } 
                     catch (Exception e){System.out.println("Choose a input file");
                                         return;}
                   JTreeUI jjt=IS.jtui;
                   DSgetArray  DSA= new DSgetArray( jjt );
 			DataSet DS[] = DSA.getDataSets() ;
                         
 			cp.getExecScript( filename, IS , DS , sessionLog);
 
                }
         }
 
 
    }
 
 class scriptFilter extends javax.swing.filechooser.FileFilter
    {
      public boolean accept(File f) 
      {
         boolean accept = f.isDirectory();
         if(!accept){String suffix = getSuffix(f);
         if (suffix != null) accept = suffix.equals("iss");
      }
      return accept;
    }
 public String getDescription(){
     return "Script Files(*.iss)";
 
     }
 
    public String getSuffix(File f) 
    {
      String s = f.getPath(), suffix = null;
      int i = s.lastIndexOf('.');
      if (i>0 && i<s.length() -1)
      suffix = s.substring(i+1).toLowerCase();
      return suffix;
    }
   }
    
    private class LoadMenuItemHandler implements ActionListener 
    {
     
       JFileChooser fc = new JFileChooser( );
       
                         
         public void actionPerformed(ActionEvent ev) 
         
         {
             
             String s=ev.getActionCommand();
             
             if(s=="Load Data File(s)")
 
                  { 
                     try
                     {  



               /*         String SS = System.getProperty("Data_Directory");
                        if(SS == null)
                            SS =System.getProperty("user.home");
                         
                         fc.setCurrentDirectory(new File(SS));
 				int state = fc.showOpenDialog(null);
                         if (state ==0 && fc.getSelectedFile() != null)
 			      {
                    	  File f = fc.getSelectedFile();
                           String filename =f.toString();
                           String fname = f.getName();
                           System.out.println("The filename is "  + filename);
                           setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                           //int is  = this.getState();
                           //this.setState(this.ICONIFIED);
                           util.appendDoc(sessionLog, "Load " + '"' +filename + '"' );
                           DataSet[] dss = util.loadRunfile(filename);
                           addDataSets(dss, fname);
 				  if(dss[1]!=null)
 				   {
 				    // jdvui.ShowDataSet(dss[1],"Internal Frame",IViewManager.IMAGE);
 				     //dss[1].setPointedAtIndex(0);
 				    // dss[1].notifyIObservers(IObserver.POINTED_AT_CHANGED);
 			         }
                           setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                         //   Isaw.setState(Isaw.NORMAL);
                        }
                    */


                          String SS = System.getProperty("Data_Directory");
                        if(SS == null)
                            SS =System.getProperty("user.home");
                         
                         fc.setCurrentDirectory(new File(SS));
 
 
 				fc.setMultiSelectionEnabled(true);
         			JFrame frame = new JFrame();
         			int retval = fc.showDialog(frame, null);
 	  			if(retval == JFileChooser.APPROVE_OPTION) {
 	    			File theFile = fc.getSelectedFile();
 	    			if(theFile != null) {
 				  File [] files = fc.getSelectedFiles();
 				  if(fc.isMultiSelectionEnabled() && files != null && files.length >= 1) 
                           {
 		    		    int size = files.length;
                             String[] file_name = new String[size];
 		              
                             setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                             DataSet[] dss=null;
 		    		    for(int i = 0; i < size; i++) 
                             {
 		                   file_name[i] = files[i].getPath();
 
                                System.out.println("Print the files in listB  " +file_name[i]);
                                 util.appendDoc(sessionLog, "Load " + '"' +file_name[i] + '"' );


                                 dss = util.loadRunfile(file_name[i]);
                                 addDataSets(dss, file_name[i]);



              			/* RunfileRetriever r = new RunfileRetriever( file_name[i] );
              			 int numberOfDataSets = r.numDataSets();
                          
  					 dss = new DataSet[numberOfDataSets];
                                for (int j = 0; j < numberOfDataSets;j++)                                
                                  dss[j] = r.getDataSet(j); 
                                jtui.addDataSets(dss, files[i].getName());
                               */
 				    
                             }
                            if(dss.length > 1)
                            if(dss[1]!=null)
 				   { 		
                            // jdvui.ShowDataSet( dss[1], "Internal Frame", IViewManager.IMAGE ); 
                            // dss[1].notifyIObservers(IObserver.POINTED_AT_CHANGED);
 			         }
                             setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 
 		    		    }
 		              }
                        }

                     } 
                     catch (Exception e){System.out.println("Choose a input file");}
                    
                 }
 
 
               
                 if(s=="Load Entire Runfile(s)")
                 {
                     try
                     {
                         String SS = System.getProperty("Data_Directory");
                        if(SS == null)
                            SS =System.getProperty("user.home");
                         
                         fc.setCurrentDirectory(new File(SS));
 
                        // int state = fc.showOpenDialog(null);
                        // if (state == 0 && fc.getSelectedFile() != null)
 			     // {
                        	 // LoadFiles db = new 	LoadFiles(jtui,fc.getCurrentDirectory().toString(),jdvui);
                          // db.setSize(new Dimension(550,300));
                          // db.show(); 
 
 				fc.setMultiSelectionEnabled(true);
         			JFrame frame = new JFrame();
         			int retval = fc.showDialog(frame, null);
 	  			if(retval == JFileChooser.APPROVE_OPTION) {
 	    			File theFile = fc.getSelectedFile();
 	    			if(theFile != null) {
 				  File [] files = fc.getSelectedFiles();
 				  if(fc.isMultiSelectionEnabled() && files != null && files.length > 1) 
                           {
 		    		    int size = files.length;
                             String[] file_name = new String[size];
 		              
                             setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                             DataSet[] dss=null;
 		    		    for(int i = 0; i < size; i++) 
                             {
 		                   file_name[i] = files[i].getPath();
 
                                System.out.println("Print the files in listB  " +file_name[i]);
 
              			 RunfileRetriever r = new RunfileRetriever( file_name[i] );
              			 int numberOfDataSets = r.numDataSets();
                          
  					 dss = new DataSet[numberOfDataSets];
                                for (int j = 0; j < numberOfDataSets;j++)
                                
                                  dss[j] = r.getDataSet(j); 
                                  jtui.addDataSets(dss, files[i].getName());
 				    
                             }
 		
                            // jdvui.ShowDataSet( dss[1], "Internal Frame", IViewManager.IMAGE ); 
 
                             setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 
 		    		    }
 		              }
                        }
                       }
                     
 
 
                     catch (Exception e){System.out.println("Choose a input file: "+e);}
                 }
                 if(s=="Load Selected Data")
                 {
                     try
                     { 
                        String SS = System.getProperty("Data_Directory");
                        if(SS == null)
                            SS =System.getProperty("user.home");
                         
                         fc.setCurrentDirectory(new File(SS));
 
 				int state = fc.showOpenDialog(null);
 				if (state ==0 && fc.getSelectedFile() != null)
 			      {
 				  File f = fc.getSelectedFile();  
                        	  ListFiles db = new ListFiles(jtui,fc.getCurrentDirectory().toString());
                           db.setSize(new Dimension(550,300));
                           db.show(); 
 				}
                     }
                     catch (Exception e){System.out.println("Choose a input file: ");}
                 }
                     
         } 
    }
 
 
    private class MenuItemHandler implements ActionListener 
    {
         FileDialog fd = new FileDialog(new Frame(), "Choose the Folder/File to open", 				
                                         FileDialog.LOAD);
         final JFileChooser fc = new JFileChooser();
         BrowserControl bc =  new BrowserControl();
         public void actionPerformed(ActionEvent ev) 
         
         {
             String s=ev.getActionCommand();
             
                 if(s=="Exit")
                 {
                     System.exit(0);
                 }
 
 		if(s=="Undo")
             {
 	  		try
 			{
 				System.out.println("Inside Undo");
 			}
 	     		catch(Exception e){};
             }
                 if(s=="Print to File")
                 {
 		      //    PrintUtilities.printComponent(jdvui.getSelectedFrame());
 
 
   /*
 			Toolkit toolkit = ISI.getToolkit();
 			PrinterJob job = toolkit.getPrintJob(jdvui.getSelectedFrame(),"Image",null);
                 	mPageFormat = pj.pageDialog(mPageFormat);
                 	ComponentPrintable cp = new ComponentPrintable(jdvui.getSelectedFrame());
                 	pj.setPrintable(cp, mPageFormat);
                 	if (pj.printDialog()) 
 			{
 				Graphics page  = job.getGraphics();
 				ISI.print(page);
                   	try { pj.print(); }
                     	catch(Exception e) { System.out.println(e); }
 
 			 }
 
 
                  try{
                  internalframe = new MyInternalFrame(jdvui.getSelectedFrame());
                    }
               catch(IOException ioe){System.out.println("Printer error:" +ioe.getMessage());}
       		PrinterJob job = PrinterJob.getPrinterJob();
       		job.setPrintable(internalframe);
       		try{
            			job.print();
          		}
       		catch(PrinterException pe){System.out.println("Printer error:" +pe.getMessage());}
 
 */
    		  }
                 
               
          
                if(s == "Windows Look")
                 {
                     try
                     {
                         UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                         SwingUtilities.updateComponentTreeUI(jtui);
                         SwingUtilities.updateComponentTreeUI(jpui);
                         SwingUtilities.updateComponentTreeUI(jdvui);
                         SwingUtilities.updateComponentTreeUI(jcui);
                     
                     }
                     catch(Exception e){ System.out.println("ERROR: setting windows look"); }
                 }
                 if(s == "Metal Look")
                 {
                     try
                     {
                         UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                         SwingUtilities.updateComponentTreeUI(jtui);
                         SwingUtilities.updateComponentTreeUI(jpui);
                         SwingUtilities.updateComponentTreeUI(jdvui);
                         SwingUtilities.updateComponentTreeUI(jcui);
                     }
                     catch(Exception e){System.out.println("ERROR: setting metal look "); }
                 }
                     if(s == "Motif Look")
                     {
                         try
                         {
                             UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                             SwingUtilities.updateComponentTreeUI(jtui);
                             SwingUtilities.updateComponentTreeUI(jpui);
                             SwingUtilities.updateComponentTreeUI(jdvui);
                             SwingUtilities.updateComponentTreeUI(jcui);
                         }
                         catch(Exception e){System.out.println("ERROR: setting motif look" ); }
                     }
                    
                    if(s == "Save ISAW Data")
                     {
                        DefaultMutableTreeNode dn = jtui.getSelectedNode();
                         
                         try
                         {
                            FileDialog fc = new FileDialog(new Frame(), 
                                 "Please choose the File to save", FileDialog.SAVE);
                            fc.setDirectory("C:\\");
                             fc.show();
                             
                              
                             File f = new File(fc.getDirectory(), fc.getFile());
                             DataSet xxx = (DataSet)(dn.getUserObject());
                             System.out.print("filename="+f.toString());
                             if( !DataSet_IO.SaveDataSet( xxx, f.toString() ) )   
                                System.out.println("Could not save File");
                          
 /*                             System.out.println("cccc"+f.toString());
                             
                             FileOutputStream fos = new FileOutputStream(f);
                             GZIPOutputStream gout = new GZIPOutputStream(fos);
                             ObjectOutputStream oos = new ObjectOutputStream(gout);
                             oos.writeObject(dn);
                             oos.close();
 
 
 */
 
                         }
                         catch(Exception e){System.out.println("Choose a DataSet to Save");}   
                       }
                       
                       if(s=="Load ISAW Data")
                       {
                         
                         try
                         {
 
                            FileDialog fc = new FileDialog(new Frame(), 
                                 "Please choose the File to open", FileDialog.LOAD);
                            fc.setDirectory("C:\\");
                            fc.show();
 
                 
                             File f = new File(fc.getDirectory(), fc.getFile());
                             String filename = f.toString();
                             DataSet xxx = DataSet_IO.LoadDataSet( filename );
 /*
                             setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                             FileInputStream fis = new FileInputStream(f);
                             GZIPInputStream gin = new GZIPInputStream(fis);
                             ObjectInputStream ois = new ObjectInputStream(gin);
                             
                             DefaultMutableTreeNode dn = (DefaultMutableTreeNode)ois.readObject();
                             ois.close();
  */                          
                           //add dn to to the tree as a child of the root
                             jtui.openDataSet(xxx, filename);
 
 
 
                         }
                         catch(Exception e){System.out.println("Choose a input DataSet");} 
                       }
                       
                       if(s == "Log View")
                       {
                             DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                             if (mtn.getUserObject() instanceof DataSet)
                             { 
                                 DataSet ds = (DataSet)mtn.getUserObject();
                                 jcui.showLog(ds);
                             }
                             else {
                                 System.out.println("View is Selected");
                                 //IsawViewHelp("No DataSet selected");
                                 }
                      }
 
 
                      
                      if(s == "HRMECS Link")
                       {  
                         //String url = "http://www.pns.anl.gov/HRMECS/Layout_98.html";
 String url = "http://www.pns.anl.gov/HRMECS/";
                         //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                         bc.displayURL(url);
             
 
 
                         
                       }
                       if(s == "LRMECS Link")
                       { 
                         //String url = "http://www.pns.anl.gov/lrmecs/lrmecs.html";
 String url = "http://www.pns.anl.gov/LRMECS/";
                         //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                         bc.displayURL(url);
                       }
                       
                       if(s == "HIPD Link")  
                       { 
                         //String url = "http://www.pns.anl.gov/highpd.htm";
                  String url = "http://www.pns.anl.gov/hipd/";
                         //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                         bc.displayURL(url); 
                       }
        
                       if(s == "QENS Link")
                       {
                         //String url = "http://www.pns.anl.gov/qens/qens.html";
 String url = "http://www.pns.anl.gov/qens/";
                         bc.displayURL(url);
                       }
                       
                       if(s == "POSY1 Link")
                       {
                         //String url = "http://www.pns.anl.gov/posy/posy.html";
 String url = "http://www.pns.anl.gov/POSY/";
                        // setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                         bc.displayURL(url);
                       }
                       
                       if(s == "POSY2 Link")
                       {
                        // String url = "http://www.pns.anl.gov/posy2/posy2.htm";
 String url = "http://www.pns.anl.gov/POSY2/";
                         bc.displayURL(url);
                       }
                       
                       if(s == "SCD Link")
                       {
                        // String url = "http://www.pns.anl.gov/scd.html";
 String url = "http://www.pns.anl.gov/SCD/";
                         //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                         bc.displayURL(url);
                       }
                       
                       if(s == "SAND Link")
                       { 
                         //String url = "http://www.pns.anl.gov/sand.html";
 String url = "http://www.pns.anl.gov/SAND/";
                         //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                         bc.displayURL(url);
                       }
                       
                       if(s == "SAD Link")
                       {  
                         //String url = "http://www.pns.anl.gov/sad/sad_front.html";
 String url = "http://www.pns.anl.gov/SAD/";
                        // setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                         bc.displayURL(url);
                       }
                       
                       if(s == "SEPD Link")
                       { 
                         //String url = "http://www.pns.anl.gov/sepd_yel.htm";
 String url = "http://www.pns.anl.gov/SEPD/";
                         //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                         bc.displayURL(url);
                       }
                       
                       if(s == "GPPD Link")
                       {  
                         //String url = "http://www.pns.anl.gov/gppd/index.htm";
 String url = "http://www.pns.anl.gov/GPPD/";
                        // setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                         bc.displayURL(url);
                       }
                       
                       if(s == "GLAD Link")
                       {  
                        // String url = "http://www.pns.anl.gov/glad/glad.html";
 String url = "http://www.pns.anl.gov/GLAD/";
                         bc.displayURL(url);
                       }
                       
                       if(s == "CHEXS Link")
                       { 
                        // String url = "http://www.pns.anl.gov/chex.htm";
 String url = "http://www.pns.anl.gov/CHEX/";
                         //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                         bc.displayURL(url);
                       }
                       
                       
   //Menuitems for macro action below                    
                       
                       
                       
                       if(s == "HRMECS ")
                       { 
                         /////Dongfeng add the macro code in this section  
                        fd.show();
                         File f = new File(fd.getDirectory(), fd.getFile());
                         String dir = fd.getDirectory();
                            FileSeparator fs = new FileSeparator(dir);
                         fs.setSize(700,700);
                         fs.setVisible(true);
                        
 
 
 
                       }
                       if(s == "LRMECS ")
                       { 
              
                        HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/lrmecs/");
                        htm.setSize(600,400);
                        htm.show();
                         
                       }
                       if(s == "HIPD ")  
                       {  
                         HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/hipd/");
                         htm.setSize(600,400);
                         htm.show();
                       }
        
                       if(s == "QENS ")
                       {  
                         HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/qens/");
                         htm.setSize(600,400);
                         htm.show();
                       }
                       if(s == "POSY1 ")
                       {  
                         HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/posy/");
                         htm.setSize(600,400);
                         htm.show();
                       }
                       if(s == "POSY2 ")
                       {  
                         HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/posy2/");
                         htm.setSize(600,400);
                         htm.show();
                       }
                       if(s == "SCD ")
                       {  
                         HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/scd/");
                         htm.setSize(600,400);
                         htm.show();
                       }
                       if(s == "SAND ")
                       {  
                         HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/sand/");
                         htm.setSize(600,400);
                         htm.show();
                       }
                       if(s == "SAD ")
                       {  
                         HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/sad/");
                         htm.setSize(600,400);
                         htm.show();
                       }
                       if(s == "SEPD ")
                       {  
                         HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/sepd/");
                         htm.setSize(600,400);
                         htm.show();
                       }
                       if(s == "GPPD ")
                       {  
                         HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/gppd/");
                         htm.setSize(600,400);
                         htm.show();
                       }
                       if(s == "GLAD ")
                       {  
                         HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/glad/");
                         htm.setSize(600,400);
                         htm.show();
                       }
                       if(s == "CHEXS ")
                       {  
                         HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/chex/");
                         htm.setSize(600,400);
                         htm.show();
                       }
                       
 
 
    // menuitem for macro loader below
 
 			if(s == System.getProperty( "Inst1_Name" ))
                     { 
    				String instrument_computer = System.getProperty("Inst1_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer);
                         objPanel.addIObserver(my_Isaw);    
                         String live_name = System.getProperty("Inst1_Name") + " Live" ;
 				jcui.setTab(live_name, objPanel);
                     }
 
                   if(s == System.getProperty("Inst2_Name"))
                       { 
              
                         String instrument_computer = System.getProperty("Inst2_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer); 
                           objPanel.addIObserver(my_Isaw);     
                         String live_name = System.getProperty("Inst2_Name") + " Live Data" ;
 				jcui.setTab(live_name, objPanel);
                         
                       }
                    if(s == System.getProperty("Inst3_Name"))
                       {
                          
                         String instrument_computer = System.getProperty("Inst3_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer);    
                         objPanel.addIObserver(my_Isaw);  
                        String live_name = System.getProperty("Inst3_Name") + " Live Data" ;
 				jcui.setTab(live_name, objPanel);
 
                       }
        
                    if(s == System.getProperty("Inst4_Name"))
                       {  
                         String instrument_computer = System.getProperty("Inst4_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer);    
                         objPanel.addIObserver(my_Isaw);  
                    String live_name = System.getProperty("Inst4_Name") + " Live Data" ;
 				jcui.setTab(live_name, objPanel);
                       }
                    if(s == System.getProperty("Inst5_Name"))
                       {                          
                         String instrument_computer = System.getProperty("Inst5_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer);    
                         objPanel.addIObserver(my_Isaw);  
                            String live_name = System.getProperty("Inst5_Name") + " Live Data" ;
 				jcui.setTab(live_name, objPanel);
                       }
                    if(s == System.getProperty("Inst6_Name"))
                       {  
                         String instrument_computer = System.getProperty("Inst6_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer);    
                         objPanel.addIObserver(my_Isaw);  
                     String live_name = System.getProperty("Inst6_Name") + " Live Data" ;
 				jcui.setTab(live_name, objPanel);
                       }
                    if(s == System.getProperty("Inst7_Name"))
                       {  
                         String instrument_computer = System.getProperty("Inst7_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer);    
                         objPanel.addIObserver(my_Isaw);  
                        String live_name = System.getProperty("Inst7_Name") + " Live Data" ;
 				jcui.setTab(live_name, objPanel);
                       }
                    if(s == System.getProperty("Inst8_Name"))
                       {  
                         String instrument_computer = System.getProperty("Inst8_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer);    
                         objPanel.addIObserver(my_Isaw);  
                        String live_name = System.getProperty("Inst8_Name") + " Live Data" ;
 				jcui.setTab(live_name, objPanel);
                       }
                    if(s == System.getProperty("Inst9_Name"))
                       {  
                         String instrument_computer = System.getProperty("Inst9_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer);    
                         objPanel.addIObserver(my_Isaw);  
                        String live_name = System.getProperty("Inst9_Name") + " Live Data" ;
 				jcui.setTab(live_name, objPanel);
                       }
                    if(s == System.getProperty("Inst10_Name"))
                       {  
                         String instrument_computer = System.getProperty("Inst10_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer);    
                         objPanel.addIObserver(my_Isaw);  
                          String live_name = System.getProperty("Inst10_Name") + " Live Data" ;
 				jcui.setTab(live_name, objPanel);
                       }
                    if(s == System.getProperty("Inst11_Name"))
                       {                          
                          String instrument_computer = System.getProperty("Inst11_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer);    
                         objPanel.addIObserver(my_Isaw);  
                           String live_name = System.getProperty("Inst11_Name") + " Live Data" ;
 				jcui.setTab(live_name, objPanel);
                        }
                    if(s == System.getProperty("Inst12_Name"))
                       {  
                         String instrument_computer = System.getProperty("Inst12_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer);    
                         String live_name = System.getProperty("Inst12_Name") + " Live Data" ;
 				jcui.setTab(live_name, objPanel);
                       }
                    if(s == System.getProperty("Inst13_Name"))
                       {  
                          String instrument_computer = System.getProperty("Inst13_Path");
 				LiveDataMonitor objPanel = new LiveDataMonitor(instrument_computer);    
                         String live_name = System.getProperty("Inst13_Name") + " Live Data" ;
 				jcui.setTab(live_name, objPanel);                
                       }
 
                      if(s == "File Separator")
                       {
                       
                         fd.show();
                         File f = new File(fd.getDirectory(), fd.getFile());
                         String dir = fd.getDirectory();
                            FileSeparator fs = new FileSeparator(dir);
                            fs.setSize(700,700);
                            fs.setVisible(true);
                      }
                 
                 if(s == "Export GSAS File")
                 {
                   //fc = new JFileChooser(new File(System.getProperty("user.dir")) );
                  
                     	int state = fc.showSaveDialog(null);
                         if (state ==0 && fc.getSelectedFile() != null)
 			      {
                          
                     File f = fc.getSelectedFile();
                     String filename =f.toString();
 		
                     DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                     DataSet ds = (DataSet)mtn.getUserObject();
                     System.out.println("inside gsaag" +ds);
                     gsas_filemaker gg=new gsas_filemaker(ds, filename);
                        }
 
                  }
     
                                  
                  if(s=="Image View" )
                 {
 
 
 
    
                   /*  DefaultMutableTreeNode mtn = jtui.getSelectedNode();
 
 
                      if(  mtn.getLevel()==1)
                      {
                      System.out.println("Selected object is :"+mtn.getUserObject()); 
                        int num_child =  mtn.getChildCount();
                          for(int i=0; i<num_child; i++)
                       {DefaultMutableTreeNode child_dataset = (DefaultMutableTreeNode) mtn.getChildAt(i);
                        DataSet ds = (DataSet)child_dataset.getUserObject();
                        System.out.println("Child Dataset are" +ds);
                       }
                      }
                      */
                      DefaultMutableTreeNode mtn = jtui.getSelectedNode();
 			   if(mtn!=null)
 			   {
 
 
 
                       if(  mtn.getLevel()==1)
                      {  
                            int num_child =  mtn.getChildCount();
                        
                            DataSet mergedDS1 = null;
                            DataSet mergedDS2 = null;
                            DataSetOperator  op1, op2;
                    
                          
                            DefaultMutableTreeNode child_dataset0= (DefaultMutableTreeNode) mtn.getChildAt(0);
                            DefaultMutableTreeNode child_dataset1 = (DefaultMutableTreeNode) mtn.getChildAt(1);
            
                            DataSet ds0 = (DataSet)child_dataset0.getUserObject();
                            DataSet ds1 = (DataSet)child_dataset1.getUserObject();
                   
                        if(num_child == 2)
                         {   
                             op1 = new DataSetMerge( ds0, ds1 );
                             mergedDS1 = (DataSet)op1.getResult(); 
 
 					jdvui.ShowDataSet(mergedDS1,"External Frame",IViewManager.IMAGE);
 					mergedDS1.setPointedAtIndex(0);
 				      mergedDS1.notifyIObservers(IObserver.POINTED_AT_CHANGED);
                           //  addDataSet(mergedDS1);
                         }
                      if(num_child == 3)
                           
                         {  
                             DefaultMutableTreeNode child_dataset2 = (DefaultMutableTreeNode) mtn.getChildAt(2);
                             DataSet ds2 = (DataSet)child_dataset2.getUserObject();
                             op1 = new DataSetMerge( ds0, ds1 );
                             mergedDS1 = (DataSet)op1.getResult(); 
                             op2 = new DataSetMerge( mergedDS1, ds2 );
                             mergedDS2 = (DataSet)op2.getResult(); 
                             
 					jdvui.ShowDataSet(mergedDS2,"External Frame",IViewManager.IMAGE);
 					mergedDS2.setPointedAtIndex(0);
 				    mergedDS2.notifyIObservers(IObserver.POINTED_AT_CHANGED);
                           //  addDataSet(mergedDS2);
   
                         }
                      }
                     
                     if(mtn.getUserObject() instanceof DataSet)
                     {
                         DataSet ds = (DataSet)mtn.getUserObject();
                         
 				jdvui.ShowDataSet(ds,"External Frame",IViewManager.IMAGE);
                         ds.setPointedAtIndex(0);
 				ds.notifyIObservers(IObserver.POINTED_AT_CHANGED);
                         jpui.showAttributes(ds.getAttributeList());
                     }
                       
                     else if(mtn.getUserObject() instanceof Data)
                     {
                         Data data = (Data)mtn.getUserObject();
                         DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)mtn.getParent();
                         DataSet ds = (DataSet)parent.getUserObject();
                       //DataSet ds = (DataSet)mtn.getUserObject();
                         TreePath[] paths = null;
 	                    JTree tree = jtui.getTree();
 	                    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
 	                    TreePath[] tp = tree.getSelectionPaths();
                         Data ggg = (Data)mtn.getUserObject();
                         int start_id =  ggg.getGroup_ID();
                         DataSetOperator  op1;
                         String attr_name = new String("Group ID");
                         op1 = new ExtractByAttribute(ds, attr_name , true, start_id, start_id+tp.length-1);
                         DataSet new_ds = (DataSet)op1.getResult(); 
 
 				jdvui.ShowDataSet(new_ds,"External Frame",IViewManager.IMAGE);
                         jpui.showAttributes(data.getAttributeList());
                     }
                     else {
                                 System.out.println("View is Selected");
                                // IsawViewHelp("No DataSet selected");
                          }
 			}
 
 			else
 				System.out.println("No Tree Node Selected");
                   
                 }
                 
                 
                  if(s=="SelectedGraph View" )
                 {   
                     
                     DefaultMutableTreeNode mtn = jtui.getSelectedNode();
 			if(mtn!=null)
 			{
 
                       if(  mtn.getLevel()==1)
                      {  
                            int num_child =  mtn.getChildCount();
                        
                            DataSet mergedDS1 = null;
                            DataSet mergedDS2 = null;
                            DataSetOperator  op1, op2;
                    
                          
                            DefaultMutableTreeNode child_dataset0= (DefaultMutableTreeNode) mtn.getChildAt(0);
                            DefaultMutableTreeNode child_dataset1 = (DefaultMutableTreeNode) mtn.getChildAt(1);
            
                            DataSet ds0 = (DataSet)child_dataset0.getUserObject();
                            DataSet ds1 = (DataSet)child_dataset1.getUserObject();
                   
                        if(num_child == 2)
                         {   
                             op1 = new DataSetMerge( ds0, ds1 );
                             mergedDS1 = (DataSet)op1.getResult(); 
 
                              jdvui.ShowDataSet(mergedDS1, "ExternalFrame", IViewManager.SELECTED_GRAPHS);
                            
                         }
                      if(num_child == 3)
                           
                         {  
                             DefaultMutableTreeNode child_dataset2 = (DefaultMutableTreeNode) mtn.getChildAt(2);
                             DataSet ds2 = (DataSet)child_dataset2.getUserObject();
                             op1 = new DataSetMerge( ds0, ds1 );
                             mergedDS1 = (DataSet)op1.getResult(); 
                             op2 = new DataSetMerge( mergedDS1, ds2 );
                             mergedDS2 = (DataSet)op2.getResult(); 
 
 				
                           //  addDataSet(mergedDS2);
                           
                         //  chop_MacroTools fg = new chop_MacroTools();
                         // fg.drawAlldata (mergedDS2); 
   
                         }
                      }           
                     
                     if(  mtn.getUserObject() instanceof DataSet)
                     {
                         
                         DataSet ds = (DataSet)mtn.getUserObject();
                         
                         jdvui.ShowDataSet(ds, "ExternalFrame", IViewManager.SELECTED_GRAPHS);
                     }
                         
                     else if(  mtn.getUserObject() instanceof Data)
                     {
                         //DataSet ds  = (DataSet) mtn.getParent();
                         DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)mtn.getParent();
                         DataSet ds = (DataSet)parent.getUserObject();
                         
                         TreePath[] paths = null;
 	                    JTree tree = jtui.getTree();
 	                    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
 	                    TreePath[] tp = tree.getSelectionPaths();
                         Data ggg = (Data)mtn.getUserObject();
                         int start_id =  ggg.getGroup_ID();
                         DataSetOperator  op1;
                         String attr_name = new String("Group ID");
                         op1 = new ExtractByAttribute(ds, attr_name , true, start_id, start_id+tp.length-1);
                         DataSet new_ds = (DataSet)op1.getResult(); 
                        
                         
                          //chop_MacroTools fg = new chop_MacroTools();
                           // fg.drawAlldata (new_ds); 
 
                         jdvui.ShowDataSet(ds, "ExternalFrame", IViewManager.SELECTED_GRAPHS);
 
 
 
                         Data data = (Data)mtn.getUserObject();
                         jpui.showAttributes(data.getAttributeList());
                     }
                     else {
                                 System.out.println("View is Selected");
 //                                IsawViewHelp("No DataSet selected");
                          }
 			}
 			else 
 			System.out.println("No tree Node selected");
                   
                 }
                         
                 
                 if(s=="Scrolled Graph View" )
                 {   
                     DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                        if(mtn!=null)
 			{
 
                     System.out.println("The Selected Node in ISaw is "  +mtn.getUserObject());
                     if(  mtn.getLevel()==1)
                      {  
                            int num_child =  mtn.getChildCount();
                        
                            DataSet mergedDS1 = null;
                            DataSet mergedDS2 = null;
                            DataSetOperator  op1, op2;
                    
                          
                            DefaultMutableTreeNode child_dataset0= (DefaultMutableTreeNode) mtn.getChildAt(0);
                            DefaultMutableTreeNode child_dataset1 = (DefaultMutableTreeNode) mtn.getChildAt(1);
            
                            DataSet ds0 = (DataSet)child_dataset0.getUserObject();
                            DataSet ds1 = (DataSet)child_dataset1.getUserObject();
                   
                        if(num_child == 2)
                         {   
                             op1 = new DataSetMerge( ds0, ds1 );
                             mergedDS1 = (DataSet)op1.getResult(); 
 
 					jdvui.ShowDataSet(mergedDS1,"External Frame",IViewManager.SCROLLED_GRAPHS);
                            // addDataSet(mergedDS1);
                         }
                      if(num_child == 3)
                           
                         {  
                             DefaultMutableTreeNode child_dataset2 = (DefaultMutableTreeNode) mtn.getChildAt(2);
                             DataSet ds2 = (DataSet)child_dataset2.getUserObject();
                             op1 = new DataSetMerge( ds0, ds1 );
                             mergedDS1 = (DataSet)op1.getResult(); 
                             op2 = new DataSetMerge( mergedDS1, ds2 );
                             mergedDS2 = (DataSet)op2.getResult(); 
 
                            // addDataSet(mergedDS2);
 		       	   jdvui.ShowDataSet(mergedDS2,"External Frame",IViewManager.SCROLLED_GRAPHS);
 
                         }
                      }           
                     
                     if(  mtn.getUserObject() instanceof DataSet)
                     {
                         DataSet ds = (DataSet)mtn.getUserObject();
   				jdvui.ShowDataSet(ds,"External Frame",IViewManager.SCROLLED_GRAPHS);
 
                     }                   
                     else if(  mtn.getUserObject() instanceof Data)
                     {
                         DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)mtn.getParent();
                         DataSet ds = (DataSet)parent.getUserObject();
                        
                        
                     TreePath[] paths = null;
 	                JTree tree = jtui.getTree();
 	                DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
 	                TreePath[] tp = tree.getSelectionPaths();
 	                System.out.println("The number of selected files is  "+tp.length );
 	                
 	                Data ggg = (Data)mtn.getUserObject();
                     int start_id =  ggg.getGroup_ID();
                     DataSetOperator  op1;
                     String attr_name = new String("Group ID");
                     
 	                for (int i=0; i<tp.length; i++)
 	                {
                       // dmtn = (DefaultMutableTreeNode)tp[i].getLastPathComponent();
                       //  System.out.println("The selected files are in JTREEUI " +dmtn.toString());
                     }
 	                
                     
                     op1 = new ExtractByAttribute(ds, attr_name , true, start_id, start_id+tp.length-1);
                     
                     DataSet new_ds = (DataSet)op1.getResult(); 
 			  jdvui.ShowDataSet(new_ds,"External Frame",IViewManager.SCROLLED_GRAPHS);
 
 
 
                         Data data = (Data)mtn.getUserObject();
                         jpui.showAttributes(data.getAttributeList());
                     }
                     else {
                                 System.out.println("View is Selected");
                          }
 	             }
 			else 
                                 System.out.println("No Tree Node Selected");
 
                   
                 }
                 




                 if(s=="3D View" )
                 {   
                     DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                        if(mtn!=null)
 			{
 
                     System.out.println("The Selected Node in ISaw is "  +mtn.getUserObject());
                     if(  mtn.getLevel()==1)
                      {  
                            int num_child =  mtn.getChildCount();
                        
                            DataSet mergedDS1 = null;
                            DataSet mergedDS2 = null;
                            DataSetOperator  op1, op2;
                    
                          
                            DefaultMutableTreeNode child_dataset0= (DefaultMutableTreeNode) mtn.getChildAt(0);
                            DefaultMutableTreeNode child_dataset1 = (DefaultMutableTreeNode) mtn.getChildAt(1);
            
                            DataSet ds0 = (DataSet)child_dataset0.getUserObject();
                            DataSet ds1 = (DataSet)child_dataset1.getUserObject();
                   
                        if(num_child == 2)
                         {   
                             op1 = new DataSetMerge( ds0, ds1 );
                             mergedDS1 = (DataSet)op1.getResult(); 
 
 					jdvui.ShowDataSet(mergedDS1,"External Frame",IViewManager.THREE_D);
                            // addDataSet(mergedDS1);
                         }
                      if(num_child == 3)
                           
                         {  
                             DefaultMutableTreeNode child_dataset2 = (DefaultMutableTreeNode) mtn.getChildAt(2);
                             DataSet ds2 = (DataSet)child_dataset2.getUserObject();
                             op1 = new DataSetMerge( ds0, ds1 );
                             mergedDS1 = (DataSet)op1.getResult(); 
                             op2 = new DataSetMerge( mergedDS1, ds2 );
                             mergedDS2 = (DataSet)op2.getResult(); 
 
                            // addDataSet(mergedDS2);
 		       	   jdvui.ShowDataSet(mergedDS2,"External Frame",IViewManager.THREE_D);
 
                         }
                      }           
                     
                     if(  mtn.getUserObject() instanceof DataSet)
                     {
                         DataSet ds = (DataSet)mtn.getUserObject();
   				jdvui.ShowDataSet(ds,"External Frame",IViewManager.THREE_D);
 
                     }                   
                     else if(  mtn.getUserObject() instanceof Data)
                     {
                         DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)mtn.getParent();
                         DataSet ds = (DataSet)parent.getUserObject();
                        
                        
                     TreePath[] paths = null;
 	                JTree tree = jtui.getTree();
 	                DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
 	                TreePath[] tp = tree.getSelectionPaths();
 	                System.out.println("The number of selected files is  "+tp.length );
 	                
 	                Data ggg = (Data)mtn.getUserObject();
                     int start_id =  ggg.getGroup_ID();
                     DataSetOperator  op1;
                     String attr_name = new String("Group ID");
                     
 	                for (int i=0; i<tp.length; i++)
 	                {
                       // dmtn = (DefaultMutableTreeNode)tp[i].getLastPathComponent();
                       //  System.out.println("The selected files are in JTREEUI " +dmtn.toString());
                     }
 	                
                     
                     op1 = new ExtractByAttribute(ds, attr_name , true, start_id, start_id+tp.length-1);
                     
                     DataSet new_ds = (DataSet)op1.getResult(); 
 			  jdvui.ShowDataSet(new_ds,"External Frame",IViewManager.THREE_D);
 
 
 
                         Data data = (Data)mtn.getUserObject();
                         jpui.showAttributes(data.getAttributeList());
                     }
                     else {
                                 System.out.println("View is Selected");
                          }
 	             }
 			else 
                                 System.out.println("No Tree Node Selected");
 
                   
                 }

                 
                 if(s=="Restore Views")
                 {    
                     jdvui.openAll();
                 } 
                 
                 if(s=="Minimize Views")
                 {   
                     jdvui.closeAll();
                 }
                 
                  if(s=="Maximize Views")
                 {   
                     jdvui.MaxAll();
                 } 
                 
                 if(s=="Tile Views Vertically")
                 {   
                     jdvui.tile_Vertically();
                 }
                 
                  if(s=="Cascade Views")
                 {   
                     jdvui.cascade();
                 }
                 if(s=="Close Views")
                 {   
                     jdvui.closeViews();
                 }
                 
                 if(s=="About ISAW")
                 {
 			String dir =  System.getProperty("user.dir")+ "/IsawHelp/Help.html";
            		BrowserControl H = new BrowserControl() ; 
            		H.displayURL( dir ) ;
 
           } 
                 
                  if(s=="Remove Selected Node")
                 { 
 
                      DefaultMutableTreeNode dmtn = null;
 	                TreePath[] paths = null;
 	                JTree tree = jtui.getTree();
 	                DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
 			    TreeSelectionModel selectionModel = tree.getSelectionModel();
 	                TreePath[] tp = tree.getSelectionPaths();
 	                //System.out.println("The number of selected files is  "+tp.length );
 	                DataSet saved_parent = null;
 			    String message = IObserver.DATA_DELETED;
 			    for (int i=0; i<tp.length; i++)
 				{
 					 dmtn = (DefaultMutableTreeNode)tp[i].getLastPathComponent();
                         // System.out.println("The selected files are in JTREEUI " +dmtn.toString());
                         try
                         {	
  					Runtime r = Runtime.getRuntime();
 
                                
                                 if(  dmtn.getUserObject() instanceof DataSet)
                                 {
 
 
 						DataSet ds = (DataSet)dmtn.getUserObject();
                                    DefaultMutableTreeNode  parent =(DefaultMutableTreeNode)dmtn.getParent();
                                     setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                     model.removeNodeFromParent(dmtn); 
 						saved_parent = ds;
 						message = IObserver.DESTROY;
 					
                                     ds.notifyIObservers(IObserver.DESTROY);
 					//	System.out.println("Inside destroy request");
 
                                     
 						r.gc(); 
 						r.runFinalization();
 						System.gc();
 						System.runFinalization();
 		                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                 }
                                 
                                 else if(  dmtn.getUserObject() instanceof Data)
                                 {
 
                                     DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)dmtn.getParent();
                                     DataSet ds = (DataSet)parent.getUserObject();
                                 
                                     setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                     int child_position = parent.getIndex(dmtn);
                                  // System.out.println("The child index is   :" +child_position);
                                     ds.removeData_entry(child_position);
 
 					   //   ds.notifyIObservers(IObserver.DATA_DELETED);
             				saved_parent=ds;
                                  // System.out.println("Removed from DS  :" +child_position);
                                     model.removeNodeFromParent(dmtn);
                 
                                     ds.addLog_entry( "Removed " +dmtn.getUserObject().toString());
                                     System.gc();
 		                        System.runFinalization();
 		                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                 //  System.out.println("Removed from treemodel  :" +child_position);
                     
 					}
                                 
                                 else if (dmtn.getLevel() == 1)
                                 {
                                     setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                     model.removeNodeFromParent(dmtn);
                                   	r.gc(); 
 						r.runFinalization();
 						System.gc();
 						System.runFinalization();
 		                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                 }
                                 
                                 else 
                                 {
                                     System.out.println("Select a tree node to delete");
                                    	r.gc(); 
 						r.runFinalization();
 						System.gc();
 						System.runFinalization();
                                 }
                               
                             }
          
                         catch(Exception e){System.out.println("Select a tree node to delete"+e);
 			}
 
 			
 
 
 
                }
                   saved_parent.notifyIObservers(message);
                        repaint();         
 
                
 
 		}
           }
      }
 
 
 
   /**
    * Creates a frame which can display a string array.
    * 
    * @param   info     Array of Strings for display.
    *
    */
 
 
 
     public void IsawViewHelp(String [] info)
     {
         
         JFrame mm = new JFrame();
     //JDialog hh = new JDialog(mm, "ISAW View Help");
         JDialog hh = new JDialog();
         hh.setSize(188,70);
     //Center the opdialog frame 
 	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 	    Dimension size = hh.getSize();
 	    screenSize.height = screenSize.height/2;
 	    screenSize.width = screenSize.width/2;
 	    size.height = size.height/2;
 	    size.width = size.width/2;
 	    int y = screenSize.height - size.height;
 	    int x = screenSize.width - size.width;
 	    hh.setLocation(x-200, y-200);
 	    JTextArea textArea = new JTextArea();
 	    for (int i=0; i<info.length; i++)
         	textArea.setText(info[i]);
         	textArea.setLineWrap(true);
                 
         JScrollPane helpScroll = new JScrollPane(textArea);
         hh.getContentPane().add(helpScroll);   
         hh.setVisible(true);
     }
 
   /**
    * Creates a frame which can display a string array.
    * 
    * @param   info     Array of Strings for dispaly.
    *
    */
 
 
     private class TreeSelectionHandler implements TreeSelectionListener
     {
         public void valueChanged(TreeSelectionEvent e)
         {
 	      if (e.getNewLeadSelectionPath() == null) return; 
 
               JTree tree = jtui.getTree();  
               popup = new JPopupMenu();
               if(tree.getSelectionCount() < 1) return;
                  
                
               TreeNode achild = jtui.getSelectedNode();
               DefaultMutableTreeNode mtn = jtui.getSelectedNode();
 
               if(  mtn.getUserObject() instanceof DataSet)
               {
                 DataSet ds = (DataSet)mtn.getUserObject();  
                 jcui.showLog(ds);
 
 		    JTable table = jcui.showDetectorInfo(ds);
                 table.hasFocus();  
                 jpui.showAttributes(ds.getAttributeList());
 
 
 
   		// get list of DataSet operators....
   			int num_ops = ds.getNum_operators(); 
   			Operator ds_ops[] = new Operator[num_ops];
   			for ( int i = 0; i < num_ops; i++ )
     			ds_ops[i] = ds.getOperator(i);
 
   		// build list of Generic operators for testing purposes...
   			Operator generic_ops[] = new Operator[7];
  			generic_ops[0] = new SumRunfiles(); 
   			generic_ops[1] = new LoadMonitorDS(); 
   			generic_ops[2] = new LoadOneHistogramDS(); 
   			generic_ops[3] = new LoadOneRunfile(); 
   			generic_ops[4] = new EchoObject(); 
   			generic_ops[5] = new pause(); 
   			generic_ops[6] = new DataSetPrint(); 
 
   			oMenu.removeAll();
   
   		// add the menu items for the operators to the menus....
   			ActionListener listener = new JOperationsMenuHandler(ds,jtui, sessionLog);
   			OperatorMenu.build( oMenu,      ds_ops,      listener );
 		   }
                else if(  mtn.getUserObject() instanceof Data)
                {
                   oMenu.removeAll();
 			DefaultMutableTreeNode mtnn = (DefaultMutableTreeNode)jtui.getSelectedNode().getParent();
                   DataSet ds = (DataSet)mtnn.getUserObject();  
                   jcui.showLog(ds);
                   JTable table = jcui.showDetectorInfo(ds);
                   table.hasFocus();
                 
                    Data data = (Data)mtn.getUserObject();
   			jpui.showAttributes(data.getAttributeList());
 
 		  // get list of DataSet operators....
   			int num_ops = ds.getNum_operators(); 
   			Operator ds_ops[] = new Operator[num_ops];
   			for ( int i = 0; i < num_ops; i++ )
     			ds_ops[i] = ds.getOperator(i);
 			ActionListener listener = new JOperationsMenuHandler(ds,jtui, sessionLog);
                   //oMenu= new OpMenu((new DSOPHandler(ds) , new DSgetArray(tree), sessionlog , tree);
 
                   OperatorMenu.build( oMenu,      ds_ops,      listener );
                   int tt = oMenu.getItemCount();
                   for (int i =0; i<tt; i++)
                   {
                     JMenuItem mitem = (JMenuItem)oMenu.getItem(i);
                     mitem.setEnabled(false);
                   }
                }          
                    
             }
 }
 
      private void propsDisplay() 
      {
            kp = new JFrame();
            kp.setSize(400,600);

        //Center the properties frame 
	     Dimension size = this.getSize();
           Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
           int window_height = (int)(screenSize.height*.9);
           int window_width = (int)(screenSize.width*.95);  
	     size.height = window_height/2+200;
	     size.width = window_width/2;
	     int y = window_height - size.height;
	     int x = window_width - size.width;
	     kp.setLocation(x, y);
           kp.setVisible(true);

           JMenuBar mb = new JMenuBar();
           JMenu fi = new JMenu("File");
           JMenuItem op1 = new JMenuItem("Save IsawProps");
           fi.add(op1);
           mb.add(fi);
           JMenuItem op2 = new JMenuItem("Quit");
           fi.add(op2);
           mb.add(fi);

           kp.setJMenuBar(mb);  
           op1.addActionListener(new propsHandler());
           op2.addActionListener(new propsHandler());
           kp.setTitle("IsawProperties Panel (editable)");
           JScrollPane Z = new JScrollPane(propsText);
           kp.getContentPane().add(Z);
           kp.setVisible(true);
            String path = System.getProperty("user.home")+"\\";
            path = StringUtil.fixSeparator(path);
            String filename = path + "IsawProps.dat" ;
                      

               Document doc = (new Util()).openDoc( filename );
                 if( doc != null)
                  {propsText.setDocument( doc ); 
                   propsText.setCaretPosition(0);   
                  }
                else
                  System.out.println("Document is null");   
      }

    private class propsHandler implements ActionListener 
    {
          public void actionPerformed(ActionEvent ev) 
         
         {
             String s=ev.getActionCommand();

            String path = System.getProperty("user.home")+"\\";
            path = StringUtil.fixSeparator(path);
            String filename = path + "IsawProps.dat" ;
                      
	      Document doc = propsText.getDocument() ; 
	      if( s == "Save IsawProps" )
              { (new Util()).saveDoc( doc , filename );        
                System.out.println( "IsawProps saved successfully") ;     
              }
             else if( s == "Quit" )
               { 
                 kp.dispose();
               }
             else
                System.out.println("Unable to quit");    

                
         }

   }



   /**
    * Creates a frame which can display a string array.
    * 
    * @param   info     Array of Strings for dispaly.
    *
    */
  
         public static void main(String[] args) 
         {


        Properties isawProp = new Properties(System.getProperties());
        String path = System.getProperty("user.home")+"\\";
        path = StringUtil.fixSeparator(path);
        try {
 	        FileInputStream input = new FileInputStream(path + "IsawProps.dat" );
              isawProp.load( input );
              System.setProperties(isawProp);  
         //   System.getProperties().list(System.out);
              input.close();
            
        }
        catch (IOException ex) 
        {
           System.out.println("Properties file could not be loaded due to error :" +ex);
           System.out.println("Creating a new Properties file called IsawProps in the directory " +System.getProperty("user.home"));
           
           String npath = System.getProperty("user.home")+"\\";
           String ipath = System.getProperty("user.dir")+"\\";
           npath = StringUtil.fixSeparator(npath);
           npath = npath.replace('\\','/');
 
           ipath = StringUtil.fixSeparator(ipath);
           ipath = ipath.replace('\\','/');
 
           File f= new File( npath + "IsawProps.dat" );
 
           try{
                FileOutputStream op= new FileOutputStream(f);
                OutputStreamWriter opw = new OutputStreamWriter(op);
                opw.write("#This is a properties file");
                opw.write("\n");
                opw.write("Help_Directory="+ipath+"IsawHelp/");
                System.setProperty("Help_Directory",  ipath+"IsawHelp/");
                opw.write("\n");
                opw.write("Script_Path="+ipath+"Scripts/");
                System.setProperty("Script_Path",ipath+"Scripts/");
                opw.write("\n");
                opw.write("Data_Directory="+ipath+"SampleRuns/");
                 System.setProperty("Data_Directory",ipath+"SampleRuns/");
                opw.write("\n");
                opw.write("Default_Instrument=HRCS");
                System.setProperty("Default_Instrument","HRCS");
                opw.write("\n");
                opw.write("Instrument_Macro_Path="+ipath);
                System.setProperty("Instrument_Macro_Path",ipath);
                opw.write("\n");
                opw.write("User_Macro_Path="+ipath);
                System.setProperty("User_Macro_Path",ipath);
                opw.write("\n");
 
                opw.write("ISAW_HOME="+ipath);
                System.setProperty("ISAW_HOME",ipath);
 
                opw.write("\n");
                opw.write("Inst1_Name=HRMECS");
                opw.write("\n"); 
                opw.write("Inst1_Path=zeus.pns.anl.gov");
                System.setProperty("Inst1_Name", "HRMECS");
                System.setProperty("Inst1_Path", "zeus.pns.anl.gov");
                opw.write("\n");  
 
                opw.write("Inst2_Name=LRMECS");
                opw.write("\n");  
                opw.write("Inst2_Path=webproject-4.pns.anl.gov");
                System.setProperty("Inst2_Name", "LRMECS");
                System.setProperty("Inst2_Path", "webproject-4.pns.anl.gov");
                opw.write("\n");  
 
                opw.write("Inst3_Name=GPPD");
                opw.write("\n");  
                opw.write("Inst3_Path=gppd-pc.pns.anl.gov");
                System.setProperty("Inst3_Name", "GPPD");
                System.setProperty("Inst3_Path", "gppd-pc.pns.anl.gov");
                opw.write("\n");  
 
                opw.write("Inst4_Name=SEPD");
                opw.write("\n");  
                opw.write("Inst4_Path=dmikk.mscs.uwstout.edu");
                System.setProperty("Inst4_Name", "SEPD");
                System.setProperty("Inst4_Path", "dmikk.mscs.uwstout.edu");
                opw.write("\n");
 
                opw.write("Inst5_Name=SAD");
                opw.write("\n");
                opw.write("Inst5_Path=webproject-4.pns.anl.gov");
                System.setProperty("Inst5_Name", "SAD");
                System.setProperty("Inst5_Path", "webproject-4.pns.anl.gov");
                opw.write("\n");    
   
                opw.write("Inst6_Name=SAND");
                opw.write("\n");      
                opw.write("Inst6_Path=webproject-4.pns.anl.gov");
                System.setProperty("Inst6_Name", "SAND");
                System.setProperty("Inst6_Path", "webproject-4.pns.anl.gov");
                opw.write("\n");
       
                opw.write("Inst7_Name=SCD");
                opw.write("\n");      
                opw.write("Inst7_Path=webproject-4.pns.anl.gov");
                System.setProperty("Inst7_Name", "SCD");
                System.setProperty("Inst7_Path", "webproject-4.pns.anl.gov");
                opw.write("\n");  
     
                opw.write("Inst8_Name=GLAD");
                opw.write("\n");      
                opw.write("Inst8_Path=webproject-4.pns.anl.gov");
                System.setProperty("Inst8_Name", "GLAD");
                System.setProperty("Inst8_Path", "webproject-4.pns.anl.gov");
                opw.write("\n"); 
      
                opw.write("Inst9_Name=HIPD");
                opw.write("\n"); 
                opw.write("Inst9_Path=webproject-4.pns.anl.gov");
                System.setProperty("Inst9_Name", "HIPD");
                System.setProperty("Inst9_Path", "webproject-4.pns.anl.gov");
                opw.write("\n");
 
                opw.write("Inst10_Name=POSY1");
                opw.write("\n");      
                opw.write("Inst10_Path=webproject-4.pns.anl.gov");
                System.setProperty("Inst10_Name", "POSY1");
                System.setProperty("Inst10_Path", "webproject-4.pns.anl.gov");
                opw.write("\n");  
     
                opw.write("Inst11_Name=POSY2");
                opw.write("\n");  
                opw.write("Inst11_Path=webproject-4.pns.anl.gov");
                System.setProperty("Inst11_Name", "POSY2");
                System.setProperty("Inst11_Path", "webproject-4.pns.anl.gov");
                opw.write("\n");  
                
                opw.write("Inst12_Name=QENS");
                opw.write("\n");                 
                opw.write("Inst12_Path=webproject-4.pns.anl.gov");
                System.setProperty("Inst12_Name", "QENS");
                System.setProperty("Inst12_Path", "webproject-4.pns.anl.gov");
                opw.write("\n");  
                
                opw.write("Inst13_Name=CHEXS");
                opw.write("\n");                 
                opw.write("Inst13_Path=webproject-4.pns.anl.gov");
                System.setProperty("Inst13_Name", "CHEXS");
                System.setProperty("Inst13_Path", "webproject-4.pns.anl.gov");
                opw.write("\n"); 
                    
                opw.flush();
                opw.close(); 
              } catch(Exception d){}
         }


            SplashWindowFrame sp = new SplashWindowFrame();
            Thread splash_thread = new Thread(sp);
            splash_thread.start();
            splash_thread = null;
       	sp = null;
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      	int window_height = (int)(screenSize.height*.40);
            int window_width = (int)(screenSize.width*.80);        
	     
           int y = (screenSize.height - window_height - 50);
	     int x = (screenSize.width - window_width)/2;

        	System.out.println("Loading ISAW version 1.1");
         	JFrame Isaw = new Isaw();
         	Isaw.pack();
         	Isaw.setBounds(x,y,window_width,window_height);
            Isaw.show();
 		Isaw.validate();
         	Isaw.addWindowListener(new WindowAdapter(){
             public void windowClosing(WindowEvent e) {System.exit(0);} 
         	});  
         }//main
 
   /**
    * Creates a frame which can display a string array.
    * 
    * @param   info     Array of Strings for dispaly.
    *
    */
  
     	class PopupTrigger extends MouseAdapter 
      	{
       	public void mouseReleased(MouseEvent e) 
         	{
             	if (e.isPopupTrigger()) 
             	{
                 		int x = e.getX();
                 		int y = e.getY();
                 		TreePath path = tree.getPathForLocation(x, y);
                 		popup.show(tree, x, y);
             	}
         	}
     	}
 
 	public void update( Object observed, Object reason )
    	{
      		if ( !( reason instanceof String) && !( reason instanceof DataSet) )   
                    // currently we only allow Strings & DataSets
        	    {
        		  return;
                 }
 
      	//	if ( observed instanceof CommandPane )                   should always be true!!!
      		{
                         
  			if ( reason instanceof DataSet )
 			{
 			  	DataSet ds1 = (DataSet)reason;
 				DefaultMutableTreeNode node = jtui.getNodeOfObject(reason);
 
                                 if ( node == null )    // ds1 is a NEW DataSet, add it as a modified DataSet
 				  		{ addDataSet( ds1 );
 				  		  //jdvui.ShowDataSet(ds1,"External Frame",IViewManager.IMAGE);
 						}
                                 else
                                    System.out.println("ERROR: Currently we only insert a new DataSet");
 				return;
 			}       		
 
 
 
      			else
      			{
 			  System.out.println("Error: Tree update called with wrong reason");
 
      			}
 
       		return; 
      		}          	
    	}
 
 
 	
 }
