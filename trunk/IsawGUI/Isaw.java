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
  *  Revision 1.38  2001/07/23 19:23:48  neffk
  *  added isForced(String) and removeForec(String) so that people
  *  can force a file to be loaded, regardless of the file extension .
  *  currently IPNS will not load a files that have changed names.
  *
  *  Revision 1.37  2001/07/23 18:33:50  neffk
  *  make the -F option more robust by checking filename estensions.
  *  prints out success and failure messages on the console.  not that the
  *  interactive file chooser does not force users to use filenames w/
  *  correct extension.  see NuetronDataFileChooser to see how to force
  *  filenames when using -F to load files.
  *
  *  Revision 1.36  2001/07/23 13:55:47  neffk
  *  now uses ViewManager instead of JDataViewUI.  JDataViewUI has been
  *  removed from the source tree.  also, fixed some more indentation
  *  problems.
  *
  *  Revision 1.35  2001/07/18 16:45:36  neffk
  *  cleaned house.  removed *many* unused menu options, variables,
  *  and blocks of code.  found and made constants for many 'magic
  *  strings' (menu item names, url's, etc).  also, replaced JTreeUI
  *  with JDataTree.
  *
  *  [neffk@freya] cvs diff IsawGUI/Isaw.java > .23546crap
  *  [neffk@freya] ls -al
  *  total 832k
  *  drwxr-xr-x   19 neffk    users   4.0k Jul 18 11:39 .
  *  drwxr-xr-x    7 neffk    users   4.0k Jul 11 14:01 ..
  *  -rw-r--r--    1 neffk    users   108k Jul 18 11:40 .23546crap
  *  -rwxr-xr-x    1 neffk    users   642 Jun 27 13:59 build_isaw
  *
  *  Revision 1.34  2001/07/13 15:28:24  neffk
  *  cleaned up "View" menu.  previously, it was around 700 lines
  *  of cut'n'pasted code for figuring out what should be done w/
  *  (tree) selections before popping up a viewer.  this task is now
  *  in a function called getViewableData(), which deals w/ these
  *  idiosyncrasies of selection.  also, spent a few hours replacing
  *  "magic strings" w/ constant values, removing *many* old menu items
  *  and irrelevant options.  cleaned up parts of the code, but the
  *  indentation and the like are still in need of work.
  *
  *  Revision 1.31  2001/07/10 14:37:21  chatter
  *  Fixed the Selected Graph View menuitem. The window 
  *  still needs to be resized before the selected graphs 
  *  appear in the graph panel. Needs to be fixed.
  *
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
  *  Added code to create a new IsawProps.dat file in the user home 
  *  directory if one is not already present
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
  *   to make use of the loader in the Util class in IsawGUI package.
  *                 
  */
 
package IsawGUI;
 
import ChopTools.*;
import Command.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.components.ui.*;
import DataSetTools.dataset.*;
import DataSetTools.gsastools.*;
import DataSetTools.instruments.*;
import DataSetTools.operator.*;
import DataSetTools.operator.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import IPNS.Runfile.*;
import java.applet.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.io.IOException; 
import java.lang.*;
import java.net.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EventObject.*;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.JTree.*;
import javax.swing.plaf.metal.MetalLookAndFeel.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import NetComm.*;
 

 /**
  * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
  * ChopTools and graph packages.
  */
public class Isaw 
  extends JFrame 
  implements Serializable, IObserver
 {
     
    private static final String TITLE              = "ISAW";
    private static final String VERSION            = "Release 1.1";

    private static final String FILE_M             = "File";
    private static final String LOAD_DATA_MI       = "Load Data File(s)";
    private static final String LOAD_LIVE_DATA_M   = "Load Live Data";
    private static final String LOAD_SCRIPT_MI     = "Load Script";
    private static final String LOAD_ISAW_DATA_MI  = "Load ISAW Data";
    private static final String SAVE_ISAW_DATA_MI  = "Save ISAW Data";
    private static final String GSAS_EXPORT_MI     = "Export GSAS File";
    private static final String EXIT_MI            = "Exit";

    private static final String EDIT_M             = "Edit";
    private static final String SET_GLOBAL_ATTR_MI = "Set Attribute for All Groups";
    private static final String SET_ATTR_MI        = "Set Attribute(s)";
    private static final String EDIT_ATTR_MI       = "Edit Attribute(s)";
    private static final String EDIT_PROPS_MI      = "Edit Properties File";
    private static final String CLEAR_SELECTION_MI = "Clear Selection";
    private static final String REMOVE_NODE_MI     = "Remove Selected Node(s)";

    private static final String VIEW_M             = "View";
    private static final String IMAGE_VIEW_MI      = "Image View";
    private static final String SCROLL_VIEW_MI     = "Scrolled Graph View";
    private static final String SELECTED_VIEW_MI   = "Selected Graph View";
    private static final String THREED_VIEW_MI     = "3D View";
    private static final String INSTR_VIEW_M       = "Instrument Info";

    private static final String MACRO_M            = "Macros";

    private static final String OPTION_M           = "Options";
    private static final String METAL_MI           = "Metal Look";
    private static final String MOTIF_MI           = "Motif Look";
    private static final String WINDOZE_MI         = "Windows Look";
 
    private static final String OPERATOR_M         = "Operations";
 
    private static final String HELP_M             = "Help";
    private static final String ABOUT_MI           = "About ISAW";

    private static final String CHEXS_LINK_MI  = "CHEXS Link";
    private static final String GLAD_LINK_MI   = "GLAD Link";
    private static final String GPPD_LINK_MI   = "GPPD Link";
    private static final String HIPD_LINK_MI   = "HIPD Link";
    private static final String HRMECS_LINK_MI = "HRMECS Link";
    private static final String LRMECS_LINK_MI = "LRMECS Link";
    private static final String POSY1_LINK_MI  = "POSY1 Link";
    private static final String POSY2_LINK_MI  = "POSY2 Link";
    private static final String QENS_LINK_MI   = "QENS Link";
    private static final String SAD_LINK_MI    = "SAD Link";
    private static final String SAND_LINK_MI   = "SAND Link";
    private static final String SCD_LINK_MI    = "SCD Link";
    private static final String SEPD_LINK_MI   = "SEPD Link";
         
    private static final String CHEXS_MACRO_MI  = "CHEXS";
    private static final String GLAD_MACRO_MI   = "GLAD";
    private static final String GPPD_MACRO_MI   = "GPPD";
    private static final String HIPD_MACRO_MI   = "HIPD";
    private static final String HRMECS_MACRO_MI = "HRMECS";
    private static final String LRMECS_MACRO_MI = "LRMECS";
    private static final String POSY1_MACRO_MI  = "POSY1";
    private static final String POSY2_MACRO_MI  = "POSY2";
    private static final String QENS_MACRO_MI   = "QENS";
    private static final String SAD_MACRO_MI    = "SAD";
    private static final String SAND_MACRO_MI   = "SAND";
    private static final String SCD_MACRO_MI    = "SCD";
    private static final String SEPD_MACRO_MI   = "SEPD";
 
    private final float RIGHT_WEIGHT = 0.6f;
    private final float LEFT_WEIGHT  = 0.85f;

    public static final String FILE_CMD = "-F";

    private static final String DATA_DIR_ENV = "Data_Directory";

    private static final String CHEX_URL   = "http://www.pns.anl.gov/CHEX/";
    private static final String GLAD_URL   = "http://www.pns.anl.gov/GLAD/";
    private static final String GPPD_URL   = "http://www.pns.anl.gov/GPPD/";
    private static final String HIPD_URL   = "http://www.pns.anl.gov/hipd/";
    private static final String HRMECS_URL = "http://www.pns.anl.gov/HRMECS/";
    private static final String LRMECS_URL = "http://www.pns.anl.gov/LRMECS/";
    private static final String POSY2_URL  = "http://www.pns.anl.gov/POSY2/";
    private static final String POSY_URL   = "http://www.pns.anl.gov/POSY/";
    private static final String QENS_URL   = "http://www.pns.anl.gov/qens/";
    private static final String SAD_URL    = "http://www.pns.anl.gov/SAD/";
    private static final String SAND_URL   = "http://www.pns.anl.gov/SAND/";
    private static final String SCD_URL    = "http://www.pns.anl.gov/SCD/";
    private static final String SEPD_URL   = "http://www.pns.anl.gov/SEPD/";

    JDataTree jdt;
    JPropertiesUI jpui;
    JCommandUI jcui;
    JMenu oMenu = new JMenu( OPERATOR_M );
    CommandPane cp;
    Util util;
    IObserver my_Isaw;
    Object Script_Path, 
           Data_Directory, 
           Help_Directory, 
           Default_Instrument, 
           Instrument_Macro_Path, 
           User_Macro_Path, 
           Image_Path;
    Document sessionLog = new PlainDocument();
    JTextArea propsText = new JTextArea(5,20);
    JFrame kp;

  /**
   * Creates a JFrame that displays different Isaw components.
   *
   */
  public Isaw( String[] args ) 
  {
    super( TITLE );
    my_Isaw = this;   //what the heck is this supposed to be?
                      //you can access 'this' from anywhere...
                      //is this really necessary?


                      //used for loading runfiles
    util = new Util(); 


    Vector mm = util.listProperties();
    JScrollPane tt = util.viewProperties();
    cp = new Command.CommandPane();
    cp.addIObserver(this);
    cp.setLogDoc(sessionLog);

    jpui = new JPropertiesUI();
    jpui.setPreferredSize( new Dimension(200, 200) );
    jpui.setMinimumSize(new Dimension(20, 50));


    jcui = new JCommandUI(cp, sessionLog,jpui);
    jcui.setPreferredSize( new Dimension( 700, 50 ) );
    jcui.setMinimumSize(new Dimension(20, 50));  

    jdt = new JDataTree();
    jdt.setPreferredSize(new Dimension(200, 500));
    jdt.setMinimumSize(new Dimension(20, 50));
    jdt.addTreeSelectionListener(  new TreeSelectionHandler()  );
        
              //checks for various command line options.
              //some options are acted upon immediatly,
              //others are returned in a Hashtable 
              //object of name value pairs for 
              //later use.  data files specified at
              //the command line are loaded 
              //immediatly.
    parse_args( args );

    setupMenuBar();        

    JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    leftPane.setOneTouchExpandable(false);
    rightPane.setOneTouchExpandable(false);
 
    rightPane.setBottomComponent(jcui);
    rightPane.setResizeWeight( RIGHT_WEIGHT );
    //leftPane.setBottomComponent(jpui);
    leftPane.setTopComponent(jdt);
    leftPane.setResizeWeight( LEFT_WEIGHT );
 
    JSplitPane sp= new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
                                   leftPane, rightPane);
        
    sp.setOneTouchExpandable(true);
    Container con = getContentPane();
    con.add(sp);
        
  }
    
 
     
  /**
   * Sets up the menubar that is used for all operations on DataSets
   */
  private void setupMenuBar() 
  {
        
    JMenuBar menuBar = new JMenuBar();

    JMenu fMenu = new JMenu( FILE_M );
    JMenuItem Runfile = new JMenuItem( LOAD_DATA_MI ); 
    JMenu LiveData = new JMenu( LOAD_LIVE_DATA_M ); 
    JMenuItem script_loader = new JMenuItem( LOAD_SCRIPT_MI );       
    JMenuItem fileLoadDataset = new JMenuItem( LOAD_ISAW_DATA_MI );
    JMenuItem fileSaveData = new JMenuItem( SAVE_ISAW_DATA_MI );
    JMenuItem fileSaveDataAs = new JMenuItem( GSAS_EXPORT_MI );
    JMenuItem fileExit = new JMenuItem( EXIT_MI );


    JMenu eMenu = new JMenu( EDIT_M );
    JMenuItem removeSelectedNode = new JMenuItem( REMOVE_NODE_MI );
    JMenuItem editAttributes = new JMenuItem( EDIT_ATTR_MI );
    JMenuItem editProps = new JMenuItem( EDIT_PROPS_MI );
    JMenuItem editSetAttribute = new JMenuItem( SET_ATTR_MI );
    JMenuItem setGroupAttributes = new JMenuItem( SET_GLOBAL_ATTR_MI );
    JMenuItem clearSelection = new JMenuItem( CLEAR_SELECTION_MI );


    JMenu vMenu = new JMenu( VIEW_M );
    JMenuItem imageView   = new JMenuItem( IMAGE_VIEW_MI );
    JMenuItem s_graphView = new JMenuItem( SCROLL_VIEW_MI );
    JMenuItem graphView   = new JMenuItem( SELECTED_VIEW_MI );
    JMenuItem threeDView = new JMenuItem( THREED_VIEW_MI );
    JMenu instrumentInfoView = new JMenu( INSTR_VIEW_M );


    Script_Class_List_Handler SP = new Script_Class_List_Handler();      
    opMenu macrosMenu = new opMenu(SP, jdt, sessionLog , this);
    macrosMenu.setOpMenuLabel( MACRO_M );


    JMenu optionMenu = new JMenu( OPTION_M );
    JMenuItem optionwindowsLook =  new JMenuItem( WINDOZE_MI );
    JMenuItem optionmetalLook   =  new JMenuItem( METAL_MI );
    JMenuItem optionmotifLook   =  new JMenuItem( MOTIF_MI );


    JMenu hMenu = new JMenu( HELP_M );
    JMenuItem helpISAW = new JMenuItem( ABOUT_MI );
 
         
    JMenuItem HRMECS = new JMenuItem( HRMECS_LINK_MI );
    JMenuItem LRMECS = new JMenuItem( LRMECS_LINK_MI );
    JMenuItem HIPD   = new JMenuItem( HIPD_LINK_MI );
    JMenuItem SAD    = new JMenuItem( SAD_LINK_MI );
    JMenuItem SCD    = new JMenuItem( SCD_LINK_MI );
    JMenuItem SAND   = new JMenuItem( SAND_LINK_MI );
    JMenuItem POSY1  = new JMenuItem( POSY1_LINK_MI );
    JMenuItem POSY2  = new JMenuItem( POSY2_LINK_MI );
    JMenuItem GLAD   = new JMenuItem( GLAD_LINK_MI );
    JMenuItem QENS   = new JMenuItem( QENS_LINK_MI );
    JMenuItem GPPD   = new JMenuItem( GPPD_LINK_MI );
    JMenuItem SEPD   = new JMenuItem( SEPD_LINK_MI );
    JMenuItem CHEXS  = new JMenuItem( CHEXS_LINK_MI );
         
    JMenuItem m_HRMECS = new JMenuItem( HRMECS_MACRO_MI );
    JMenuItem m_LRMECS = new JMenuItem( LRMECS_MACRO_MI );
    JMenuItem m_HIPD   = new JMenuItem( HIPD_MACRO_MI );
    JMenuItem m_SAD    = new JMenuItem( SAD_MACRO_MI );
    JMenuItem m_SCD    = new JMenuItem( SCD_MACRO_MI );
    JMenuItem m_SAND   = new JMenuItem( SAND_MACRO_MI );
    JMenuItem m_POSY1  = new JMenuItem( POSY1_MACRO_MI );
    JMenuItem m_POSY2  = new JMenuItem( POSY2_MACRO_MI );
    JMenuItem m_GLAD   = new JMenuItem( GLAD_MACRO_MI );
    JMenuItem m_QENS   = new JMenuItem( QENS_MACRO_MI );
    JMenuItem m_GPPD   = new JMenuItem( GPPD_MACRO_MI );
    JMenuItem m_SEPD   = new JMenuItem( SEPD_MACRO_MI );
    JMenuItem m_CHEXS  = new JMenuItem( CHEXS_MACRO_MI );
 
 
    fMenu.add(Runfile);
    fMenu.add(LiveData);
    fMenu.add(script_loader);
    fMenu.add(fileLoadDataset);
    fMenu.addSeparator();
    fMenu.add(fileSaveData);
    fMenu.add(fileSaveDataAs);
    fMenu.addSeparator();
    fMenu.add(fileExit);
 
    eMenu.add(removeSelectedNode);
    eMenu.add(editAttributes);
    eMenu.add(editProps);
    eMenu.add(editSetAttribute);
    eMenu.add(setGroupAttributes);
    eMenu.add(clearSelection);
      

         
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
    
    hMenu.add(helpISAW);
    fileExit.addActionListener(new MenuItemHandler());
    Runfile.addActionListener(new LoadMenuItemHandler());
    LiveData.addActionListener(new LoadMenuItemHandler());

    script_loader.addActionListener(new ScriptLoadHandler(this));

    fileSaveData.addActionListener(new MenuItemHandler());
    fileSaveDataAs.addActionListener(new MenuItemHandler());
    
    graphView.addActionListener(new MenuItemHandler()); 
         
    s_graphView.addActionListener(new MenuItemHandler()); 

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


    optionmetalLook.addActionListener(new MenuItemHandler());
    optionmotifLook.addActionListener(new MenuItemHandler());
    optionwindowsLook.addActionListener(new MenuItemHandler());
    fileLoadDataset.addActionListener(new MenuItemHandler());
    removeSelectedNode.addActionListener(new MenuItemHandler());
    editProps.addActionListener(new AttributeMenuItemHandler());
    editAttributes.addActionListener(new AttributeMenuItemHandler());
    editSetAttribute.addActionListener(new AttributeMenuItemHandler());
    setGroupAttributes.addActionListener(new AttributeMenuItemHandler());
    clearSelection.addActionListener(new AttributeMenuItemHandler());
    
    helpISAW.addActionListener(new MenuItemHandler());
   
    menuBar.add(fMenu);
    menuBar.add(eMenu);
    menuBar.add(vMenu);
    menuBar.add(optionMenu);
    menuBar.add(oMenu);
    menuBar.add(macrosMenu);
    menuBar.add(hMenu);
    setJMenuBar(menuBar);
  }

 
  /**
   * Adds DataSets to the JTree.  aslo makes the tree, properties and
   * command userinterfaces observers of the datasets.
   *
   * @param ds   Array of DataSets
   * @param name String identifying the Runfile
   */
  public void addDataSets( DataSet[] dss, String name )
  {
    jdt.addExperiment( dss, name );
    for(int i =0; i<dss.length; i++)
    {
      dss[i].addIObserver(jdt);
      dss[i].addIObserver(jpui);
      dss[i].addIObserver(jcui);
    }
  }


  /**
   * Adds a modified DataSet to the JTree.
   *
   * @param ds The modified DataSet to be added to the tree.
   * 
   */
  public void addDataSet( DataSet ds )
  {
    DataSet[] dss = new DataSet[ 1 ];
    dss[0] = ds;
 
    addDataSets( dss, "Modified" );
  }
 

  /**
   * Implementation of the EDIT_ATTR_MI menu item's actions.
   */        
  private class AttributeMenuItemHandler implements ActionListener 
  {
    public void actionPerformed(ActionEvent ev) 
    { 
      String s=ev.getActionCommand();

      if( s == EDIT_ATTR_MI )
      {   
        MutableTreeNode node = jdt.getSelectedNode();
        if(  node instanceof DataSetMutableTreeNode  )
        {
          Object obj = ( (DataSetMutableTreeNode)node ).getUserObject();
          JAttributesDialog  jad = new JAttributesDialog(  ( (IAttributeList)obj ).getAttributeList(), s  );
          ( (IAttributeList)obj ).setAttributeList(  jad.getAttributeList()  );
       }
        if(  node instanceof DataMutableTreeNode  )
        {
          Object obj = ( (DataMutableTreeNode)node ).getUserObject();
          JAttributesDialog  jad = new JAttributesDialog(  ( (IAttributeList)obj ).getAttributeList(), s  );
          ( (IAttributeList)obj ).setAttributeList(  jad.getAttributeList()  );
        } 
      }


      if( s == EDIT_PROPS_MI )
        propsDisplay();
                 
                 
      if( s == SET_ATTR_MI )
      {   
        MutableTreeNode node = jdt.getSelectedNode();
                  
        if(  node instanceof DataSetMutableTreeNode  )
        {
          DataSetMutableTreeNode ds_node = (DataSetMutableTreeNode)node;
          Object obj = ds_node.getUserObject();
          AttributeList new_list = makeNewAttributeList();
          JAttributesDialog  jad = new JAttributesDialog( new_list, s );
          AttributeList current_list = ( (IAttributeList)obj ).getAttributeList();
          new_list = jad.getAttributeList();
           
          for (int i = 0; i<new_list.getNum_attributes(); i++)
          {
            Attribute attr = new_list.getAttribute(i);
              
                                        //TODO: Add more instances 
            if(attr instanceof FloatAttribute)
            {
              float val = ((FloatAttribute)attr).getFloatValue();
              if(  !Float.isNaN(val)  )
                 current_list.setAttribute(attr);
             }
           }
           ( (IAttributeList)obj ).setAttributeList( current_list );
        }

        if(  node instanceof DataMutableTreeNode  )
        {
          DataMutableTreeNode d_node = (DataMutableTreeNode)node;
          Object obj = d_node.getUserObject();
          AttributeList new_list = makeNewAttributeList();
          JAttributesDialog  jad = new JAttributesDialog( new_list, s );
          AttributeList current_list = ( (IAttributeList)obj ).getAttributeList();
          new_list = jad.getAttributeList();
           
          for (int i = 0; i<new_list.getNum_attributes(); i++)
          {
            Attribute attr = new_list.getAttribute(i);
              
                                        //TODO: Add more instances 
            if(attr instanceof FloatAttribute)
            {
              float val = ((FloatAttribute)attr).getFloatValue();
              if(  !Float.isNaN(val)  )
                 current_list.setAttribute(attr);
             }
           }
           ( (IAttributeList)obj ).setAttributeList( current_list );
         }
       }


       if( s == CLEAR_SELECTION_MI )
         jdt.clearSelections();
 

      if( s == SET_GLOBAL_ATTR_MI )
      {   
        MutableTreeNode node = jdt.getSelectedNode();
        if(  node instanceof DataSetMutableTreeNode  ) 
        {
          DataSetMutableTreeNode ds_node = (DataSetMutableTreeNode)node;
          Object obj = ds_node.getUserObject();
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
              {   
                float val = ((FloatAttribute)attr).getFloatValue();
                if(  !Float.isNaN(val)  )
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
   * @param dss Array of DataSets
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
  {  
    Isaw IS;
       
 
       public ScriptLoadHandler( Isaw IS)
        { this.IS = IS;
       //  IS.setState(IS.ICONIFIED);
        }
         public void actionPerformed(ActionEvent ev) 
         
         {
             
             String s=ev.getActionCommand();
             
             if( s == LOAD_SCRIPT_MI )
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
                   JDataTree jjt=IS.jdt;
                   cp.getExecScript( filename, IS, jdt, sessionLog);
 
                }
         }
 
 
    }
 

  /**
   * what does this do?  your guess is a good as mine.
   */
  class scriptFilter extends javax.swing.filechooser.FileFilter
  {
    public boolean accept(File f) 
    {
      boolean accept = f.isDirectory();
      if(!accept)
      {
        String suffix = getSuffix(f);
        if (suffix != null) accept = suffix.equals("iss");
      }
      return accept;
    }

    /**
     * gets the description of what files this filter shows
     */ 
    public String getDescription()
    {
      return "Script Files(*.iss)";
    }
 
    /**
     * returns a file extension
     */
    public String getSuffix(File f) 
    {
      String s = f.getPath(), suffix = null;
      int i = s.lastIndexOf('.');
      if (i>0 && i<s.length() -1)
      suffix = s.substring(i+1).toLowerCase();
      return suffix;
    }
  }
   

  /*
   *
   */ 
  private class LoadMenuItemHandler implements ActionListener 
  {
    public void actionPerformed( ActionEvent e ) 
    {
      if(  e.getActionCommand().equals( LOAD_DATA_MI )  )
        load_runfiles( false, null );
    }
  }
 
 

  /*
   * trap menu events
   */
  private class MenuItemHandler implements ActionListener 
  {
    String msg = new String( "Choose the Folder/File to open" );
    FileDialog fd = new FileDialog( new Frame(), 
                                    msg,
                                    FileDialog.LOAD );
    final JFileChooser fc = new JFileChooser();
    BrowserControl bc =  new BrowserControl();

    
    public void actionPerformed( ActionEvent ev ) 
    {
      String s = ev.getActionCommand();
      if( s == EXIT_MI )
        System.exit(0);
                 
      if( s == WINDOZE_MI )
      {
        try
        {
          UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
          update();
        }
        catch( Exception e )
        { 
          System.out.println( "ERROR: setting look'n'feel" ); 
        }
      }

      if( s == METAL_MI )
      {
        try
        {
          UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
          update();
        }
        catch( Exception e )
        {
          System.out.println( "ERROR: setting metal look " ); 
        }
      }

      if( s == MOTIF_MI )
      {
        try
        {
          UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
          update();
        }
        catch( Exception e )
        {
          System.out.println( "ERROR: setting motif look" ); 
        }
      }
                    
      if( s == SAVE_ISAW_DATA_MI )
      {
        MutableTreeNode node = jdt.getSelectedNode();
        if( node instanceof DataSetMutableTreeNode )
        {
          try
          {
            String title = new String( "Please choose the File to save" );
            FileDialog fc = new FileDialog(  new Frame(), 
                                             title, 
                                             FileDialog.SAVE  );
            fc.setDirectory( "C:\\" );
            fc.show();
                        
            File f = new File(  fc.getDirectory(), fc.getFile()  );
            DataSet ds = ( (DataSetMutableTreeNode)node ).getUserObject();
            if(   !DataSet_IO.SaveDataSet(  ds, f.toString()  )   )   
            System.out.println("Could not save File");
          }
          catch( Exception e ) 
          {
            System.out.println( "Choose a DataSet to Save" );
          }   
        }
        else
          return;
      }
                       


                                  //loads a file that was stored using
                                  //ISAW's proprietary file structure (?)
                                  //instead of other neutron data formats.
                                  //hence, there is only one DataSet object
                                  //to be loaded.
      if( s == LOAD_ISAW_DATA_MI )
      {
        try
        {
                                             //create a file dialog box and get
                                             //which file to open
          String msg = new String( "Please choose the File to open" );
          FileDialog fc = new FileDialog(  new Frame(), 
                                           msg, 
                                           FileDialog.LOAD  );
          fc.setDirectory("C:\\");
          fc.show();
          File f = new File( fc.getDirectory(), fc.getFile() );
          String filename = f.toString();
          DataSet ds = DataSet_IO.LoadDataSet( filename );
                          
                                //add it to the tree and other 
                                //dependants
          addDataSet( ds );
          cp.addDataSet( ds );
        }
        catch( Exception e )
        {
          e.printStackTrace();
        }
      }
                       
                          //the next 13 or so menu options 
                          //open links to the various 
                          //instrument web sites
      if(s == "HRMECS Link")
        bc.displayURL( HRMECS_URL );

      if(s == "LRMECS Link")
        bc.displayURL( LRMECS_URL );
                       
      if(s == "HIPD Link")  
        bc.displayURL( HIPD_URL ); 
        
      if(s == "QENS Link")
        bc.displayURL( QENS_URL );
                       
      if(s == "POSY1 Link")
        bc.displayURL( POSY_URL );
                       
      if(s == "POSY2 Link")
        bc.displayURL( POSY2_URL );
                       
      if(s == "SCD Link")
        bc.displayURL( SCD_URL );
                       
      if(s == "SAND Link")
        bc.displayURL( SAND_URL );
                       
      if(s == "SAD Link")
        bc.displayURL( SAD_URL );
                       
      if(s == "SEPD Link")
        bc.displayURL( SEPD_URL );
                       
      if(s == "GPPD Link")
        bc.displayURL( GPPD_URL );
                       
      if(s == "GLAD Link")
        bc.displayURL( GLAD_URL );
                       
      if(s == "CHEXS Link")
        bc.displayURL( CHEX_URL );
                       
                                       //the next 13 menu options
                                       //execute instrument-specific
                                       //macros on various forms of data


      if(s == CHEXS_MACRO_MI ) 
        System.out.println( "Instrument-specific macros/scripts are not implemented" );

      if(s == GLAD_MACRO_MI ) 
        System.out.println( "Instrument-specific macros/scripts are not implemented" );

      if(s == GPPD_MACRO_MI ) 
        System.out.println( "Instrument-specific macros/scripts are not implemented" );

      if(s == HRMECS_MACRO_MI )
      { 
        fd.show();
        File f = new File(  fd.getDirectory(), fd.getFile()  );
        String dir = fd.getDirectory();
        FileSeparator fs = new FileSeparator(dir);
        fs.setSize(700,700);
        fs.setVisible(true);
      }

      if(s == HIPD_MACRO_MI )  
        System.out.println( "Instrument-specific macros/scripts are not implemented" );

      if(s == LRMECS_MACRO_MI ) 
        System.out.println( "Instrument-specific macros/scripts are not implemented" );

      if(s == POSY1_MACRO_MI ) 
        System.out.println( "Instrument-specific macros/scripts are not implemented" );

      if(s == POSY2_MACRO_MI ) 
        System.out.println( "Instrument-specific macros/scripts are not implemented" );

      if(s == QENS_MACRO_MI ) 
        System.out.println( "Instrument-specific macros/scripts are not implemented" );

      if(s == SAD_MACRO_MI ) 
        System.out.println( "Instrument-specific macros/scripts are not implemented" );

      if(s == SAND_MACRO_MI ) 
        System.out.println( "Instrument-specific macros/scripts are not implemented" );

      if(s == SCD_MACRO_MI ) 
        System.out.println( "Instrument-specific macros/scripts are not implemented" );

      if(s == SEPD_MACRO_MI ) 
        System.out.println( "Instrument-specific macros/scripts are not implemented" );

 
 
    // menuitem for macro loader below
 
      if(  s == System.getProperty( "Inst1_Name" )  )
        setupLiveDataServer( "Inst1_Path" );
 
      if(s == System.getProperty("Inst2_Name"))
        setupLiveDataServer( "Inst2_Path" );

      if(s == System.getProperty("Inst3_Name"))
        setupLiveDataServer( "Inst3_Path" );
        
      if(s == System.getProperty("Inst4_Name"))
        setupLiveDataServer( "Inst4_Path" );

      if(s == System.getProperty("Inst5_Name"))
        setupLiveDataServer( "Inst5_Path" );

      if(s == System.getProperty("Inst6_Name"))
        setupLiveDataServer( "Inst6_Path" );

      if(s == System.getProperty("Inst7_Name"))
        setupLiveDataServer( "Inst7_Path" );

      if(s == System.getProperty("Inst8_Name"))
        setupLiveDataServer( "Inst8_Path" );

      if(s == System.getProperty("Inst9_Name"))
        setupLiveDataServer( "Inst9_Path" );

      if(s == System.getProperty("Inst10_Name"))
        setupLiveDataServer( "Inst10_Path" );

      if(s == System.getProperty("Inst11_Name"))
        setupLiveDataServer( "Inst11_Path" );

      if(s == System.getProperty("Inst12_Name"))
        setupLiveDataServer( "Inst12_Path" );

      if(s == System.getProperty("Inst13_Name"))
        setupLiveDataServer( "Inst13_Path" );
 
      if( s == GSAS_EXPORT_MI )
      {
        //fc = new JFileChooser(new File(System.getProperty("user.dir")) );
                  
        int state = fc.showSaveDialog(null);
        if (state ==0 && fc.getSelectedFile() != null)
        {
          File f = fc.getSelectedFile();
          String filename =f.toString();
   
          MutableTreeNode node = jdt.getSelectedNode();
          if( node instanceof DataSetMutableTreeNode )
          {
            DataSet ds = ( (DataSetMutableTreeNode)node ).getUserObject();
            gsas_filemaker gsas_output = new gsas_filemaker( ds, filename );
          }
        }
      }
     

      if( s == IMAGE_VIEW_MI )
      {
        DataSet ds = getViewableData(  jdt.getSelectedNodePaths()  );
        if(  ds != null  )
        {
          new ViewManager( ds, IViewManager.IMAGE );
          ds.setPointedAtIndex( 0 );
          ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
        }
        else
          System.out.println( "nothing is currently highlighted in the tree" );
      }
                 
                 
      if( s == SELECTED_VIEW_MI )  
      {
        DataSet ds = getViewableData(  jdt.getSelectedNodePaths()  );
        if(  ds != DataSet.EMPTY_DATA_SET  )
        {
          new ViewManager( ds, IViewManager.SELECTED_GRAPHS );
          ds.setPointedAtIndex( 0 );
          ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
        }
        else
          System.out.println( "nothing is currently highlighted in the tree" );
      }
                         
                 
      if( s == SCROLL_VIEW_MI )
      {   
        DataSet ds = getViewableData(  jdt.getSelectedNodePaths()  );
        if(  ds != DataSet.EMPTY_DATA_SET  )
        {
          new ViewManager( ds, IViewManager.SCROLLED_GRAPHS );
          ds.setPointedAtIndex( 0 );
          ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
        }
        else
          System.out.println( "nothing is currently highlighted in the tree" );
      }
                 

      if( s == THREED_VIEW_MI )
      {   
        DataSet ds = getViewableData(  jdt.getSelectedNodePaths()  );
        if(  ds != DataSet.EMPTY_DATA_SET  )
        {
          new ViewManager( ds, IViewManager.THREE_D );
          ds.setPointedAtIndex( 0 );
          ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
        }
        else
          System.out.println( "nothing is currently highlighted in the tree" );
      }

                 
      if( s == ABOUT_MI )
      {
        String dir =  System.getProperty("user.dir")+ "/IsawHelp/Help.html";
        BrowserControl H = new BrowserControl() ; 
        H.displayURL( dir ) ;
      } 
                
 

                                    //remove some node from the tree.  since
                                    //the tree could change drastically, we'll
                                    //let the tree handle it's own business
                                    //and the messy details of deleting nodes.
      if( s == REMOVE_NODE_MI )
          jdt.deleteSelectedNodes(); 
    }


    /*
     * update look'n'feel
     */
    private void update()
    {
      SwingUtilities.updateComponentTreeUI(jdt);
      SwingUtilities.updateComponentTreeUI(jpui);
      SwingUtilities.updateComponentTreeUI(jcui);
    }

 
    public void setupLiveDataServer( String instr )
    {
      String instrument_computer = System.getProperty( instr );
      LiveDataMonitor objPanel = new LiveDataMonitor( instrument_computer );
      String live_name = instrument_computer + " Live Data" ;
      jcui.setTab( live_name, objPanel );
    }
  }
 
 
 
  /**
   * Creates a frame which can display a string array.
   * 
   * @param   info     Array of Strings for display.
   *
   */
  public void IsawViewHelp( String[] info )
  {
    JFrame mm = new JFrame();
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
 

   /*
    * listens to the tree, creating menus on-the-fly for context
    * sensative items (Operations menu, for example).
    */
  private class TreeSelectionHandler implements TreeSelectionListener
  {

    /*
     * creates menu of operators if appropriate for the current selection
     */
    public void valueChanged( TreeSelectionEvent e )
    {
                                                  //deal w/ unselection events
      if( e.getNewLeadSelectionPath() == null )
      {
        oMenu = new JMenu( OPERATOR_M );
        oMenu.add(  new JMenuItem( "[empty]" )  );
        return; 
      }

      if(  e.getPaths().length < 1  ) 
      {
        oMenu = new JMenu( OPERATOR_M );
        oMenu.add(  new JMenuItem( "[empty]" )  );
        return;
      }
                                                 //deal w/ all other
                                                 //selection events
      MutableTreeNode node = jdt.getSelectedNode();
      if(  node instanceof Experiment  )
      {
        oMenu = new JMenu( OPERATOR_M );
        oMenu.add(  new JMenuItem( "[empty]" )  );
        return; 
      }

      else if(  node instanceof DataSetMutableTreeNode  )
      {
        DataSetMutableTreeNode dsmtn = (DataSetMutableTreeNode)node;
        DataSet ds = (DataSet)dsmtn.getUserObject();  
        jcui.showLog(ds);

        JTable table = jcui.showDetectorInfo(ds);
        table.hasFocus();  
        jpui.showAttributes(ds.getAttributeList());

                              //since the Operations menu is sensitive
                              //to tree selections, we have to look 
                              //after keeping the menu up to date.  so,
                              //here we build a menu according to the
                              //class of the selected node.
        oMenu.removeAll();
        int num_ops = ds.getNum_operators(); 
        Operator ds_ops[] = new Operator[num_ops];
        for ( int i = 0; i < num_ops; i++ )
          ds_ops[i] = ds.getOperator(i);
 
        ActionListener listener = new JOperationsMenuHandler( ds, jdt, sessionLog );
        OperatorMenu.build( oMenu, ds_ops, listener );
      }

      else if(  node instanceof DataMutableTreeNode  )
      {
        oMenu = new JMenu( OPERATOR_M );
        oMenu.add(  new JMenuItem( "[empty]" )  );
        return; 
      }

      else
        System.out.println( "type not appropriate for operators" );
    }
  }
 

  /*
   * allows the user to edit the properties file in their home
   * directory (IsawProps.dat).
   */
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
    JMenu fi = new JMenu( FILE_M );
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
    {
      propsText.setDocument( doc ); 
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
      { 
        (new Util()).saveDoc( doc , filename );        
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
    * entry point for the ISAW application.
    */
  public static void main( String[] args ) 
  {
    Properties isawProp = new Properties(System.getProperties());
    String path = System.getProperty("user.home")+"\\";
    path = StringUtil.fixSeparator(path);


    try 
    {
      FileInputStream input = new FileInputStream(path + "IsawProps.dat" );
      isawProp.load( input );
      System.setProperties(isawProp);  
      input.close();
    }
    catch( IOException ex ) 
    {
      System.out.println(
        "Properties file could not be loaded due to error :" +ex );

      System.out.println(
        "Creating a new Properties file called IsawProps in the directory " +
        System.getProperty("user.home") );
           
      String npath = System.getProperty("user.home")+"\\";
      String ipath = System.getProperty("user.dir")+"\\";
      npath = StringUtil.fixSeparator(npath);
      npath = npath.replace('\\','/');
 
      ipath = StringUtil.fixSeparator(ipath);
      ipath = ipath.replace('\\','/');
 
      File f= new File( npath + "IsawProps.dat" );
 
      try
      {
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
      } 
      catch( Exception d )
      {
      }
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

    System.out.println("Loading " + TITLE + VERSION );
    JFrame Isaw = new Isaw( args );
    Isaw.pack();
    Isaw.setBounds(x,y,window_width,window_height);
    Isaw.show();
    Isaw.validate();
    Isaw.addWindowListener( 
      new WindowAdapter()
      {
        public void windowClosing( WindowEvent e ) 
        {
          System.exit(0);
        } 
      } 
    );  
  }
 




  
  /**
   * since this object is an IObserver, this method is called to
   * make changes as per notification.
   */ 
  public void update( Object observed, Object reason )
  {
                                  //currently we only allow 
                                  //String and DataSet objects
    if( !( reason instanceof String) && !( reason instanceof DataSet) )   
      return;
 
    if ( reason instanceof DataSet )
    {
      DataSet ds = (DataSet)reason;
      MutableTreeNode node = jdt.getNodeOfObject( reason );
 
      if( node == null ) 
      {
                    //this must be a new DataSet object...
                    //put it in the modified folder on 
                    //the tree.
        addDataSet( ds );
      }
      else
        return;
    }
    else
    {
      System.out.println("Error: Tree update called with wrong reason");
      return;
    }
  }
 
 

  /**
   * loads runfiles interactivly or in batch.  please use this function to
   * load runfiles, not one of the lower level functions.  the low-level
   * routines don't perform any sanity checks on filenames.  sanity checks 
   * are built into this method, and all future extensions involving 
   * filtering should be incorperated into this method.
   *
   *   @param batch      determines the mode that this function uses.  for
   *                     an interactive Swing based GUI prompt, set this
   *                     value to false.  to open files in a non-interactive,
   *                     or batch mode, set this value to true.
   *   @param filenames  the filenames that you want to load.  this list
   *                     is NOT used unless 'batch' is true.
   *
   */
  protected void load_runfiles( boolean batch, String[] filenames ) 
  {
    JFileChooser fc = new JFileChooser();



                //loads files in an interactive mode
    if( !batch )
    {
      String data_dir = System.getProperty( DATA_DIR_ENV );
      if( data_dir == null )
        data_dir = System.getProperty( "user.home" );
        
                                       //create and display the 
                                       //file chooser, load files
      JFrame frame = new JFrame();
      fc.setCurrentDirectory(  new File( data_dir )  );
      fc.setMultiSelectionEnabled( true );
      fc.setFileFilter(  new NeutronDataFileFilter()  ); 
      if(  fc.showDialog(frame,null) == JFileChooser.APPROVE_OPTION  ) 
      {
        setCursor(  Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR )  );

        load_files(  fc.getSelectedFiles()  );

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    }

       //loads files in batch mode
    else
    {
      NeutronDataFileFilter filter = new NeutronDataFileFilter();

                                       //count how many files are
                                       //of the correct formatt by
      int count = 0;                   //checking file extension
      for( int i=0;  i<filenames.length;  i++ )
        if(  filter.accept_filename( filenames[i] )  ||  isForced( filenames[i] )  )
          count++;

                                       //load the files w/ acceptable
                                       //names
      File[] files = new File[ count ];
      for( int i=0;  i<filenames.length;  i++ )
        if(  isForced( filenames[i] )  )
        {
          System.out.println(  "loading (forced): " + removeForce( filenames[i] )  );
          files[i] = new File(  removeForce( filenames[i] )  );
        }
        else if(  filter.accept_filename( filenames[i] )  )
        {
          System.out.println( "loading: " + filenames[i] );
          files[i] = new File( filenames[i] );
        }
        else
          System.out.println(  "failed: " + filenames[i]  );

      load_files( files );

    }
  }


  private boolean isForced( String filename )
  {
                                 //if the filename has a bang (!)
                                 //appended to it, then accept it
                                 //reguardless of its extension.
    int bang_index = filename.lastIndexOf( '!' );
    if(  bang_index == filename.length() - 1  )
      return true;
    else
      return false;
  }


  private String removeForce( String filename )
  {
    int bang_index = filename.lastIndexOf( '!' );

    if( bang_index > 0 )
      return new String( filename.substring( 0, bang_index )  );
    else
     return filename;
  }


  /*
   * loads runfiles.  this routine assumes that the files
   * have been checked for sanity (e.g. correct file extensions)
   */
  private void load_files( File[] files )
  {
    if(  files != null  &&  files.length > 0  )
      for( int i=0;  i<files.length;  i++ ) 
        addDataSets(  util.loadRunfile(  files[i].getPath()  ), 
                      files[i].getName()  );

    util.appendDoc(  sessionLog, "Load " + files.toString()  );
  }


  /**
   *
   */
  public Hashtable parse_args( String[] args )
  {
    Hashtable pairs = new Hashtable();

    int i=0;
    for( i=0;  i<args.length;  i++ )
      if(  args[i].equals( FILE_CMD )  )
      {
        i++;
        String[] filenames = new String[ args.length-i ];
        for( int j=i;  j<args.length;  j++ )
          filenames[j-i] = args[j];

        load_runfiles( true, filenames );
        break;
      }

    return pairs;
  }


  /**
   * organizes a number of selections on the tree into a
   * DataSet.  Data and DataSet objects and combinations thereof
   * are handled appropriatly.
   */
  private DataSet getViewableData( TreePath[] tps )
  {

                       //since it doesn't make sense to combine Data objects
                       //from different DataSet objects, we'll disallow that
                       //and arbitrarily choose the parent DataSet of the
                       //first Data object selected
    MutableTreeNode node = (MutableTreeNode)(  tps[0].getLastPathComponent()  );
    DataSet ds = jdt.getDataSet( node );


                                //if it's just one (1) DataSet object
                                //nothing need be done... just return it
    node = (MutableTreeNode)tps[0].getLastPathComponent();
    if(  node instanceof DataSetMutableTreeNode  )
     return ( (DataSetMutableTreeNode)node ).getUserObject(); 

    return DataSet.EMPTY_DATA_SET;
  }

}
