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
 *  Revision 1.86  2002/03/07 22:24:57  pfpeterson
 *  Now using global version information.
 *
 *  Revision 1.85  2002/03/04 20:31:03  pfpeterson
 *  Default properties file comments out more lines if ISAW is not found
 *  in the classpath.
 *
 *  Revision 1.84  2002/02/27 17:31:58  pfpeterson
 *  Made the StatusPane larger (look for JSplitPane sp1).
 *
 *  Revision 1.83  2002/02/26 21:15:57  pfpeterson
 *  Corrected the condition for the no data highlighted error to appear
 *  in the status pane.
 *
 *  Revision 1.82  2002/02/25 23:32:27  pfpeterson
 *  Extracted the writing of default properties file from Isaw.java and
 *  set new values to be more reasonable.
 *  Added some more error checking into the routines that call various
 *  viewers.
 *
 *  Revision 1.81  2002/02/22 20:39:12  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.80  2002/02/18 21:57:52  pfpeterson
 *  Fixed nexus and windows problem.
 *  Made a "Save and Exit" option on the "Edit Properties" text editor.
 *
 *  Revision 1.79  2002/02/15 20:30:37  pfpeterson
 *  Fixed the file filter in the "Save As" JFileChooser.
 *
 *  Revision 1.78  2002/02/14 22:43:48  pfpeterson
 *  Added "Glossary" and "API Documentation" to the help menus. This requires the
 *  docs.jar to be unpacked and Glossary.html to work properly.
 *
 *  Revision 1.77  2002/01/25 19:37:17  pfpeterson
 *  scriptFilter is no longer embedded in Isaw.java. It is now in the IsawGUI directory.
 *
 *  Revision 1.76  2002/01/25 19:26:57  pfpeterson
 *  scriptFilter is now a public class. This allows for the filter to be used in
 *  the CommandPane.
 *
 *  Revision 1.75  2002/01/14 20:30:28  pfpeterson
 *  Modified to use writer interface for GSAS files.
 *  Removed menu item for explicit GSAS export. Done through 'Save As' now.
 *
 *  Revision 1.74  2002/01/10 17:07:24  rmikk
 *  Now most SaveAs filechoosers remember the last Save
 *  directory
 *
 *  Revision 1.73  2002/01/10 15:46:01  rmikk
 *  Now uses the Global Status Pane
 *     DataSetTools.util.SharedData.status_pane
 *  A few file dialog boxes remember their last files
 *
 *  Revision 1.72  2002/01/09 20:12:11  rmikk
 *  -Fixed a compiler error.
 *
 *  Revision 1.71  2002/01/09 19:33:43  rmikk
 *  -Incorporated a Status Pane.  It is static and has the name
 *    IsawStatusPane.  It can be used as follows:
 *          SharedData.status_pane.add( Message)
 *   ONLY IF ISAW is RUNNING with a non null StatusPane
 *
 *  Revision 1.70  2002/01/09 15:45:11  rmikk
 *  Changed the message given when an exception occurs.
 *  Also return from this procedure after this exception
 *     occurred
 *
 *  Revision 1.69  2002/01/08 21:26:52  rmikk
 *  Fixed the File filter on a Save dialog box to exclude
 *  Run files
 *
 *  Revision 1.68  2001/12/20 20:32:36  chatterjee
 *  Added a view menuitem Log View that will show the ancillary log SDDS file
 *  in a table. This is using the package from APS.
 *
 *  Revision 1.67  2001/12/11 17:56:47  pfpeterson
 *  Modified the help menu system. Now includes, About, Operations (old help), Command Pane, and links to online resources.
 *
 *  Revision 1.66  2001/12/05 20:53:05  pfpeterson
 *  extended dual monitor support to cover more windows
 *
 *  Revision 1.65  2001/11/30 23:14:58  pfpeterson
 *  Changed the size and position of the main window to assume a 4:3 aspect ratio from the screen height. This should fix the dual monitor problem.
 *
 *  Revision 1.64  2001/11/20 21:38:07  pfpeterson
 *  Revised GSAS Export menu to be more precise.
 *
 *  Revision 1.63  2001/11/13 22:38:10  pfpeterson
 *  added PDF name to GSAS export menu item
 *
 *  Revision 1.62  2001/11/08 22:27:52  chatterjee
 *  Added ability to write out PDF (Pair distribution function) format as a part of the GSAS file output.
 *
 *  Revision 1.61  2001/09/06 22:20:26  dennis
 *  Replaced keyPressed() method with keyReleased() method in the
 *  KeyListener class, so that the information from the newly
 *  pointed at node is shown, rather than the information from
 *  the previous node.
 *
 *  Revision 1.60  2001/09/04 17:16:11  chatterjee
 *  Added a key listener to allow for updates in the viewers as the
 *  nodes on the tree are pointed at using the up/down arrow keys.
 *  The JDataTree constructor changed to accept a keylistener along witha mouse listener.
 *
 *  Revision 1.59  2001/08/23 15:22:37  chatterjee
 *  Increased the JFileChooser size to 550,300 to better accomodate the filter options.
 *  Reordered the appearance of the filters.
 *
 *  Revision 1.57  2001/08/21 21:31:41  chatterjee
 *  Added separate file filters for .run and .nxs/hdf files
 *
 *  Revision 1.56  2001/08/16 18:53:02  chatterjee
 *  Checking in merged version
 *
 *  Revision 1.55  2001/08/16 00:40:18  rmikk
 *  NDS  server was set to notify Isaw and not the JTree
 *
 *  Revision 1.54  2001/08/15 22:28:49  chatterjee
 *  Rearranged File menu and incorporated the Remote File Server & added a Table View
 *  Used new JFileChooser in some cases
 *
 *  Revision 1.53  2001/08/14 19:25:29  chatterjee
 *  Fixed the LiveData for HRMECS.Does not throw up the FileDialog when Live Data for HRMECS is chosen
 *
 *  Revision 1.52  2001/08/08 20:20:51  dennis
 *  Now reloads the properties file after editing/saving.
 *
 *  Revision 1.51  2001/08/08 18:08:46  chatter
 *  DB search URl added
 *
 *  Revision 1.50  2001/08/07 22:11:39  rmikk
 *  Fixed session log report when a file is loaded in.
 *
 *  Revision 1.49  2001/08/02 19:06:27  neffk
 *  added DB_IMPORT_MI, a menu item to pops up a browser to search
 *  through ANL's database of past experiments.
 *
 *  Revision 1.48  2001/07/31 19:47:40  neffk
 *  adds Isaw.java as a listener when new live data clients are created.
 *  also, changed some formatting, finished removing the 'Option' menu.
 *
 *  Revision 1.45  2001/07/26 20:41:51  neffk
 *  fixed TreeListener to keep the 'Operator' menu current.  previously,
 *  whenever deletions (especially Experiment objects) were made, the
 *  'Operator' menu became unusable because these deletions qualified
 *  as unselect events, which were trapped and delt with by a block of
 *  code that hadn't been updated to preserve the 'oMenu' reference.
 *
 *  Revision 1.44  2001/07/26 16:19:48  neffk
 *  changed '==' to .equals() where appropriate for String objects.
 *  this fixes the problem of live data servers not working after a
 *  viewer was popped up (as a direct result of the viewer reloading
 *  the peoperties file, which changed the location in memory of the
 *  strings in question, which caused the '==' operator to fail)
 *
 *  Revision 1.43  2001/07/25 20:49:54  neffk
 *  fixed right-click menus.  the problem was generated by the
 *  restructuring of ISAW that was prompted by the need for ISAW to
 *  be notified of newly generated DataSet objects.  the solution
 *  to that problem was to instantiate the tree's listener (which
 *  is responsible for instantiating JOperationsMenu) in a scole
 *  where Isaw.this was visable.  so, MouseListener (inner class of
 *  Isaw.java) was instantiated in the constructor if Isaw and passed
 *  into the constructor of JDataTree.  previous to this bugfix, that
 *  MouseListener was passed to JDataTree through a public method other
 *  than the the JDataTree constructor.  there are a number of problems
 *  with doing things before an object is completely constructed, but
 *  the reverse seems to be the case here--the problem only is apparent
 *  when the mouse listener is added *after* construction.  odd.
 *
 *  Revision 1.42  2001/07/25 19:18:52  neffk
 *  updated to reflect changes in JDataTree and Experiment.  these
 *  changes fix the problem of only showing the first new DataSet object
 *  in the 'Modified' node of the tree.
 *
 *  Revision 1.41  2001/07/25 16:12:22  neffk
 *  added the mouse listener (for the tree) as an inner class so that
 *  it has access to Isaw.this.  the change was made so that Isaw can
 *  be updated when operators generate new DataSet objects and update
 *  the tree, the command pane, and the properties panel.
 *
 *  Revision 1.40  2001/07/24 18:58:16  neffk
 *  operations menu contains only "[empty]" when the current selections
 *  is not a DataSet object.
 *
 *  Revision 1.39  2001/07/24 14:34:43  neffk
 *  adding DataSet objects to the tree is now cleaner.  there is
 *  the distinction between new and modified objects, and the
 *  'add...' functions take care of IObserver notificatoin.  also changed
 *  the path for IsawHelp and added the capability to grab multiple Data
 *  objects, put them in a DataSet object automatically when selected
 *  and viewed.  ISAW's title is fixed w/ a space, too.
 *
 *  Revision 1.38  2001/07/23 19:23:48  neffk
 *  added isForced(String) and removeForec(String) so that people
 *  can force a file to be loaded, regardless of the file extension.
 *  currently IPNS will not load a files that have changed names.
 *
 *  Revision 1.37  2001/07/23 18:33:50  neffk
 *  made the -F option more robust by checking filename estensions.
 *  prints out success and failure messages on the console.  note that the
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
import DataSetTools.components.containers.SplitPaneWithState; 
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.components.ui.*;
import DataSetTools.dataset.*;
import DataSetTools.components.containers.*;
import DataSetTools.gsastools.*;
import DataSetTools.instruments.*;
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
import NexIO.*;
//import SDDS.java.SDDSedit.*;
 

 /**
  * The main class for ISAW. It is the GUI that ties together the 
  * DataSetTools, IPNS, ChopTools and graph packages.
  */
public class Isaw 
  extends JFrame 
  implements Serializable, IObserver
{
     
  private static final String TITLE              = "ISAW";
    //private static final String VERSION            = "Release 1.2";

  private static final String FILE_M             = "File";
  private static final String LOAD_DATA_M        = "Load Data";
  private static final String LOAD_LIVE_DATA_M   = "Live";
  private static final String LOAD_LOCAL_DATA_MI = "Local";
  private static final String LOAD_REMOTE_DATA_M = "Remote";


  private static final String LOAD_SCRIPT_MI     = "Load Script";
  

  private static final String SAVE_ISAW_DATA_MI  = "Save As";
  //private static final String GSAS_EXPORT_MI     = "Export As GSAS Powder File";
  private static final String DB_IMPORT_MI       = "Search Database";
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
  private static final String TABLE_VIEW_MI      = "Table View";
  private static final String LOG_VIEW_MI        = "Log View";

  private static final String INSTR_VIEW_M       = "Instrument Info";

  private static final String MACRO_M            = "Macros";

  private static final String OPTION_M           = "Options";
  private static final String METAL_MI           = "Metal Look";
  private static final String MOTIF_MI           = "Motif Look";
  private static final String WINDOZE_MI         = "Windows Look";

  private static final String OPERATOR_M         = "Operations";

  private static final String HELP_M          = "Help";
  private static final String ABOUT_MI        = "About ISAW";
  private static final String OPERATIONS_MI   = "Operations";
  private static final String COMMAND_PANE_MI = "Command Pane";

  private static final String GLOSSARY_MI     = "Glossary";
  private static final String API_DOCS_MI     = "API Documentation";

  private static final String HOME_LINK_MI    = "ISAW Homepage";
  private static final String FTP_LINK_MI     = "ISAW FTP Site";
  private static final String USERMAN_LINK_MI = "Online Documentation";
  private static final String HOME_LINK       = "http://www.pns.anl.gov/ISAW/";
  private static final String FTP_LINK        = "ftp://zuul.pns.anl.gov/isaw/";
  private static final String USERMAN_LINK    = "ftp://zuul.pns.anl.gov/isaw/Documents/";

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
  private final float LEFT_WEIGHT  = 0.90f;

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
  private static final String DB_URL     = "http://www.pns.anl.gov/ISAW/";
  private static final String WIN_ID     = "Windows";

  JDataTree jdt;  

  JPropertiesUI jpui;
  JCommandUI jcui;
   //StatusPane IsawStatusPane;
  JMenu oMenu = new JMenu( OPERATOR_M );
  CommandPane cp;
  Util util;
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
   * @param args  an array of String objects that correspond
   *              to the command line arguments, not including the
   *              name of the program.
   */
  public Isaw( String[] args ) 
  {
    super( TITLE );
                      //used for loading runfiles
    util = new Util(); 
    Vector mm = util.listProperties();
    JScrollPane tt = util.viewProperties();
    cp = new Command.CommandPane();
    cp.addIObserver( this );
    cp.setLogDoc(sessionLog);

    jpui = new JPropertiesUI();
    jpui.setPreferredSize( new Dimension(200, 200) );
    jpui.setMinimumSize(new Dimension(20, 50));


    jcui = new JCommandUI( cp, sessionLog, jpui );
    jcui.setPreferredSize( new Dimension( 700, 50 ) );
    jcui.setMinimumSize(new Dimension(20, 50));  

    MouseListener ml = new MouseListener();
    KeyListener kl = new KeyListener(); 
    jdt = new JDataTree( ml, kl);
    ml.init();
    kl.init();
   
    jdt.setPreferredSize(new Dimension(200, 500));
    jdt.setMinimumSize(new Dimension(20, 50));
    jdt.addTreeSelectionListener(  new TreeSelectionHandler( this )  );
        
              //checks for various command line options.
              //some options are acted upon immediatly,
              //others are returned in a Hashtable 
              //object of name value pairs for 
              //later use.  data files specified at
              //the command line are loaded 
              //immediatly.
    parse_args( args );

    //IsawStatusPane = new StatusPane(30, 80, 
          SharedData.status_pane.setBorder(new javax.swing.border.TitledBorder("Status"));
          SharedData.status_pane.setEditable(true);
          SharedData.status_pane.setLineWrap(false);

    setupMenuBar();        

    
    JPanel StatusPanel= new JPanelwithToolBar(
                 "Save","Clear",
                  new SaveDocToFileListener( 
                         SharedData.status_pane.getDocument(),null),
                  new ClearDocListener( SharedData.status_pane.getDocument()),
                  new JScrollPane( SharedData.status_pane),
                  BorderLayout.EAST);
                         
    cp.addPropertyChangeListener(SharedData.status_pane);
    JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    leftPane.setOneTouchExpandable(false);
    rightPane.setOneTouchExpandable(false);
 
    rightPane.setBottomComponent(jcui);
    rightPane.setResizeWeight( RIGHT_WEIGHT );
    //leftPane.setBottomComponent(jpui);
    leftPane.setTopComponent(jdt);
    leftPane.setResizeWeight( LEFT_WEIGHT );
 
/*
    JSplitPane sp= new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                                           leftPane, 
                                           rightPane,
                                           0.25f);
*/
    JSplitPane sp= new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                                           leftPane, 
                                           rightPane, 
                                           0.20f);

    JSplitPane sp1= new SplitPaneWithState( JSplitPane.VERTICAL_SPLIT,
                                            sp,
                                      StatusPanel,
                                            0.80f );
   
       
    sp.setOneTouchExpandable(true);
    Container con = getContentPane();
    con.add(sp1);
  }
    
 
     
  /**
   * Sets up the menubar that is used for all operations on DataSets
   */
  private void setupMenuBar() 
  {
        
    JMenuBar menuBar = new JMenuBar();

    JMenu fMenu = new JMenu( FILE_M );
       JMenu fileLoadDataset = new JMenu( LOAD_DATA_M );
        JMenuItem Runfile = new JMenuItem( LOAD_LOCAL_DATA_MI ); 
        JMenu LiveData = new JMenu( LOAD_LIVE_DATA_M );
        JMenu RemoteData = new JMenu( LOAD_REMOTE_DATA_M );

     JMenuItem script_loader = new JMenuItem( LOAD_SCRIPT_MI );       
    
    JMenuItem fileSaveData = new JMenuItem( SAVE_ISAW_DATA_MI );
    //JMenuItem fileSaveDataAs = new JMenuItem( GSAS_EXPORT_MI );
    JMenuItem dbload = new JMenuItem( DB_IMPORT_MI );
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
    JMenuItem tableView = new JMenuItem( TABLE_VIEW_MI );
    JMenuItem logView = new JMenuItem( LOG_VIEW_MI );
    JMenu instrumentInfoView = new JMenu( INSTR_VIEW_M );


    Script_Class_List_Handler SP = new Script_Class_List_Handler();      
    opMenu macrosMenu = new opMenu(SP, jdt, sessionLog , Isaw.this);
    macrosMenu.setOpMenuLabel( MACRO_M );
    macrosMenu.addStatusPane( SharedData.status_pane );



/*
    JMenu optionMenu = new JMenu( OPTION_M );
    JMenuItem optionwindowsLook =  new JMenuItem( WINDOZE_MI );
    JMenuItem optionmetalLook   =  new JMenuItem( METAL_MI );
    JMenuItem optionmotifLook   =  new JMenuItem( MOTIF_MI );
*/


    JMenu hMenu               = new JMenu( HELP_M );
    JMenuItem helpAbout       = new JMenuItem( ABOUT_MI );
    JMenuItem helpOperations  = new JMenuItem( OPERATIONS_MI );
    JMenuItem helpCommandPane = new JMenuItem( COMMAND_PANE_MI );
    JMenuItem glossary        = new JMenuItem( GLOSSARY_MI );
    JMenuItem apiDocs         = new JMenuItem( API_DOCS_MI );
    JMenuItem homeLink        = new JMenuItem( HOME_LINK_MI );
    JMenuItem ftpLink         = new JMenuItem( FTP_LINK_MI );
    JMenuItem docLink         = new JMenuItem( USERMAN_LINK_MI );

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
 
    fMenu.add(dbload);
    fMenu.add(fileLoadDataset);
    fileLoadDataset.add(Runfile);
    fileLoadDataset.add(LiveData);
    fileLoadDataset.add(RemoteData);
      SetUpRemoteData( RemoteData, jdt); 


    fMenu.add(script_loader);
    
    //fMenu.addSeparator();
    fMenu.add(fileSaveData);
    //fMenu.add(fileSaveDataAs);
    fMenu.addSeparator();
    
   // fMenu.addSeparator();
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
    for( int ii=1;  ii<14 && found;  ii++ )
    {
      String SS = System.getProperty("Inst"+new Integer(ii).toString().trim()+"_Name");
      if( SS == null) 
        found=false;
      else
      {
        JMenuItem dummy = new JMenuItem(SS);
        dummy.setToolTipText( System.getProperty("Inst"+new Integer(ii).toString().trim()+"_Path"));
        dummy.addActionListener( new MenuItemHandler());
        LiveData.add(dummy);
      }
    }
        
         
    vMenu.add(imageView);
    vMenu.add(s_graphView);
    vMenu.add(graphView);
    vMenu.add(threeDView);
    vMenu.add( tableView );
    vMenu.add( logView );
    vMenu.add(instrumentInfoView);         
      
    hMenu.add(helpAbout);
    hMenu.add(helpOperations);
    hMenu.add(helpCommandPane);
    hMenu.add(glossary);
    hMenu.add(apiDocs);
    hMenu.addSeparator();
    hMenu.add(homeLink);
    hMenu.add(ftpLink);
    hMenu.add(docLink);

    fileExit.addActionListener(       new MenuItemHandler()        );
    Runfile.addActionListener(        new LoadMenuItemHandler()    );
    LiveData.addActionListener(       new LoadMenuItemHandler()    );

    script_loader.addActionListener(  new ScriptLoadHandler(this)  );

    fileSaveData.addActionListener(   new MenuItemHandler()        );
    //fileSaveDataAs.addActionListener( new MenuItemHandler()        );
    dbload.addActionListener(         new MenuItemHandler()        );
    
    
    graphView.addActionListener(new MenuItemHandler()); 
         
    s_graphView.addActionListener(new MenuItemHandler()); 

    threeDView.addActionListener(new MenuItemHandler()); 
    imageView.addActionListener(new MenuItemHandler());  
    tableView.addActionListener(new MenuItemHandler());  
    logView.addActionListener(new MenuItemHandler());      
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


    fileLoadDataset.addActionListener(new MenuItemHandler());
    removeSelectedNode.addActionListener(new MenuItemHandler());
    editProps.addActionListener(new AttributeMenuItemHandler());
    editAttributes.addActionListener(new AttributeMenuItemHandler());
    editSetAttribute.addActionListener(new AttributeMenuItemHandler());
    setGroupAttributes.addActionListener(new AttributeMenuItemHandler());
    clearSelection.addActionListener(new AttributeMenuItemHandler());
    
    helpAbout.addActionListener(new MenuItemHandler());
    helpOperations.addActionListener(new MenuItemHandler());
    helpCommandPane.addActionListener(new MenuItemHandler());
    glossary.addActionListener(new MenuItemHandler());
    apiDocs.addActionListener(new MenuItemHandler());
    homeLink.addActionListener(new MenuItemHandler());
    ftpLink.addActionListener(new MenuItemHandler());
    docLink.addActionListener(new MenuItemHandler());
   
    menuBar.add(fMenu);
    menuBar.add(eMenu);
    menuBar.add(vMenu);
    menuBar.add(oMenu);
    menuBar.add(macrosMenu);
    menuBar.add(hMenu);
    setJMenuBar(menuBar);
  }

 
  /**
   * Adds new DataSet objectss to the JTree.  this method should only
   * be used to add the initial DataSet objects.  this method also
   * makes the tree, properties and command ui observers 
   * of the datasets.  Modified DataSet objects should be added by 
   * calling 'addModifiedDataSet()'.  
   *
   * @param ds   Array of DataSets
   * @param name String identifying the Runfile
   */
  protected void addNewDataSets( DataSet[] dss, String name )
  {
    jdt.addExperiment( dss, name );
    for(int i =0; i<dss.length; i++)
    {
      cp.addDataSet( dss[i] );
      dss[i].addIObserver( this );
      dss[i].addIObserver( jpui );
//      dss[i].addIObserver( jcui );
    }
  }

   private void SetUpRemoteData( JMenu RemoteData, IObserver jdt)
       {
          int i=1;
          boolean done =false;
          while (!done )
          {  String S= System.getProperty("IsawFileServer"+i+"_Name");
             
             if( S == null) done = true;
             else
               {JMenuItem Server= new JMenuItem( S );
                RemoteData.add( Server);
                Server.addActionListener( new RemoteMenuHandler( Isaw.this, 
                      sessionLog ));
                i++;
               }            
           }

          i=1;
          done =false;
          while (!done )
          {  String S= System.getProperty("NDSFileServer"+i+"_Name");
             if( S == null) done = true;
             else
               {JMenuItem Server= new JMenuItem( S );
                RemoteData.add( Server);
                Server.addActionListener( new RemoteMenuHandler( Isaw.this, 
                          sessionLog ));
                i++;
               }            
           }

        }//SetUpRemote 

  /**
   * Adds a modified DataSet to the JTree.
   */
  public void addModifiedDataSet( DataSet ds )
  {
//    SharedData.status_pane.add( "Isaw: addModifiedDataSet(...)" );

    jdt.addToModifiedExperiment( ds );

    cp.addDataSet( ds );
    ds.addIObserver( this );
    ds.addIObserver( jpui );
//    ds.addIObserver( jcui );
  }
 

  /**
   * the EDIT_ATTR_MI menu item's actions.
   */        
  private class AttributeMenuItemHandler implements ActionListener 
  {
    public void actionPerformed(ActionEvent ev) 
    { 
      String s = ev.getActionCommand();

      if( s.equals(EDIT_ATTR_MI) )
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


      if( s.equals(EDIT_PROPS_MI) )
        propsDisplay();
                 
                 
      if( s.equals(SET_ATTR_MI) )
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


       if( s.equals(CLEAR_SELECTION_MI) )
         jdt.clearSelections();
 



      if( s.equals(SET_GLOBAL_ATTR_MI) )
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
    String filename = null; 
 
    public ScriptLoadHandler( Isaw IS)
    {  
      this.IS = IS;
//      IS.setState(IS.ICONIFIED);
    }


    public void actionPerformed(ActionEvent ev) 
    {
      String s=ev.getActionCommand();
             
      if( s.equals(LOAD_SCRIPT_MI) )
      {
	//String SS;
        if( filename == null)
              filename = System.getProperty( "Script_Path" );
        if( filename == null )
          filename = System.getProperty( "user.home" );

        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(filename));
	Dimension d = new Dimension(650,300);
	fc.setPreferredSize(d);
 
        String str = (String)Script_Path ;
        fc.setFileFilter(new scriptFilter());
        String fname;
        try
        {
          int state = fc.showOpenDialog(null);
          if(  state == 0  &&  fc.getSelectedFile() != null  )
          {
            File f = fc.getSelectedFile();
            filename =f.toString();
            //fname = f.getName();
                        
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                          
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
          else return;
        } 
        catch( Exception e )
        {  SharedData.status_pane.add("Choose an input file");
          //System.out.println( "Choose a input file" );
          return;
        }

        JDataTree jjt=IS.jdt;
        cp.getExecScript( filename, IS, jdt, sessionLog, SharedData.status_pane);
      }
    }
 
 
    }
 

  /**
   * what does this do?  your guess is a good as mine.
   */
  /* public class scriptFilter extends javax.swing.filechooser.FileFilter
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
     } */

    /**
     * gets the description of what files this filter shows
     */ 
    /* public String getDescription()
       {
       return "Script Files(*.iss)";
       } */
 
    /**
     * returns a file extension
     */
    /* public String getSuffix(File f) 
       {
       String s = f.getPath(), suffix = null;
       int i = s.lastIndexOf('.');
       if (i>0 && i<s.length() -1)
       suffix = s.substring(i+1).toLowerCase();
       return suffix;
    }
    }*/
   

  /*
   *
   */ 
  //$$$$ eliminate
  private class LoadMenuItemHandler implements ActionListener 
  {
    public void actionPerformed( ActionEvent e ) 
    {
      if(  e.getActionCommand().equals( LOAD_LOCAL_DATA_MI )  )
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
    String filename=null;
    
    public void actionPerformed( ActionEvent ev ) 
    { 
      String s = ev.getActionCommand();
      if( s.equals(EXIT_MI) )
        System.exit(0);
                    
      if( s.equals( SAVE_ISAW_DATA_MI ))
      {
        MutableTreeNode node = jdt.getSelectedNode();
        if( node instanceof DataSetMutableTreeNode )
        {
          try
          { 
            String title = new String( "Please choose the File to save" );
            if(filename ==  null)
               filename =System.getProperty("user.home");
            fc.setCurrentDirectory(  new File( filename )  );
            fc.setMultiSelectionEnabled( false );
	    fc.resetChoosableFileFilters();
            fc.addChoosableFileFilter(  new NeutronDataFileFilter( true )  ); 
            fc.addChoosableFileFilter(  new NexIO.NexusfileFilter()  );
           // fc.addChoosableFileFilter(  new IPNS.Runfile.RunfileFilter()  );
	    Dimension d = new Dimension(650,300);
	    fc.setPreferredSize(d);
            
            if(  (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION ) ) 
                return;
             
            setCursor(  Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR )  );                        
                        
            File f =  fc.getSelectedFile();
            filename = f.toString();
            DataSet ds = ( (DataSetMutableTreeNode)node ).getUserObject();
           // if(   !DataSet_IO.SaveDataSet(  ds, f.toString()  )   )   
           // System.out.println("Could not save File");
             util.Save( f.toString(), ds,  jdt );
           setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//String filename, DataSet ds, IDataSetListHandler lh)

          }
          catch( Exception e ) 
          {
            SharedData.status_pane.add( "Error "+e );
            return;
          } 
         
        }
        else
          return;
        return;
      }
                       


                                  //browse ANL database for data files.
                                  //in the future, this menu item probably
                                  //should actually import the files
      if( s.equals(DB_IMPORT_MI) )
      {
        BrowserControl bc = new BrowserControl();
        bc.displayURL( DB_URL );
        return;
      }

                                  //loads a file that was stored using
                                  //ISAW's proprietary file structure (?)
                                  //instead of other neutron data formats.
                                  //hence, there is only one DataSet object
                                  //to be loaded.
      if( s.equals(LOAD_LOCAL_DATA_MI) )
      {
        try
        { if( filename == null) filename= System.getProperty("user.home");
                                             //create a file dialog box and get
                                             //which file to open
          String msg = new String( "Please choose the File to open" );
          FileDialog fc = new FileDialog(  new Frame(), 
                                           msg, 
                                           FileDialog.LOAD  );
	  fc.setDirectory(  filename  );
          fc.show();
          File f = new File( fc.getDirectory(), fc.getFile() );
          filename = f.toString();
          DataSet ds = DataSet_IO.LoadDataSet( filename );
                          
                                //add it to the tree and other 
                                //dependants
          DataSet[] dss = new DataSet[1];  dss[0] = ds;
          addNewDataSets(  dss, dss[0].toString()  );
        }
        catch( Exception e )
        {
          e.printStackTrace();
          return;
        }
      }
                       
                          //the next 13 or so menu options 
                          //open links to the various 
                          //instrument web sites
      if( s.equals(HRMECS_LINK_MI) )
        bc.displayURL( HRMECS_URL );

      if( s.equals(LRMECS_LINK_MI) )
        bc.displayURL( LRMECS_URL );
                       
      if( s.equals(HIPD_LINK_MI) )
        bc.displayURL( HIPD_URL ); 
        
      if( s.equals(QENS_LINK_MI) )
        bc.displayURL( QENS_URL );
                       
      if( s.equals(POSY1_LINK_MI) )
        bc.displayURL( POSY_URL );
                       
      if( s.equals(POSY2_LINK_MI) )
        bc.displayURL( POSY2_URL );
                       
      if( s.equals(SCD_LINK_MI) )
        bc.displayURL( SCD_URL );
                       
      if( s.equals(SAND_LINK_MI) )
        bc.displayURL( SAND_URL );
                        
      if( s.equals(SAD_LINK_MI) )
        bc.displayURL( SAD_URL );
                       
      if( s.equals(SEPD_LINK_MI) )
        bc.displayURL( SEPD_URL );
                       
      if( s.equals(GPPD_LINK_MI) )
        bc.displayURL( GPPD_URL );
                       
      if( s.equals(GLAD_LINK_MI) )
        bc.displayURL( GLAD_URL );
                       
      if( s.equals(CHEXS_LINK_MI) )
        bc.displayURL( CHEX_URL );
                       
                                       //the next 13 menu options
                                       //execute instrument-specific
                                       //macros on various forms of data


      if( s.equals(CHEXS_MACRO_MI) ) 
        SharedData.status_pane.add( "Instrument-specific macros/scripts are not implemented" );

      if( s.equals(GLAD_MACRO_MI) ) 
        SharedData.status_pane.add( "Instrument-specific macros/scripts are not implemented" );

      if( s.equals(GPPD_MACRO_MI) ) 
        SharedData.status_pane.add( "Instrument-specific macros/scripts are not implemented" );

      if( s.equals(HRMECS_MACRO_MI) )
      { 
     /* fd.show();
        File f = new File(  fd.getDirectory(), fd.getFile()  );
        String dir = fd.getDirectory();
        FileSeparator fs = new FileSeparator(dir);
        fs.setSize(700,700);
        fs.setVisible(true);
      */
      }

      if( s.equals(HIPD_MACRO_MI) )  
        SharedData.status_pane.add( "Instrument-specific macros/scripts are not implemented" );

      if( s.equals(LRMECS_MACRO_MI) ) 
        SharedData.status_pane.add( "Instrument-specific macros/scripts are not implemented" );

      if( s.equals(POSY1_MACRO_MI) ) 
        SharedData.status_pane.add( "Instrument-specific macros/scripts are not implemented" );

      if( s.equals(POSY2_MACRO_MI) ) 
        SharedData.status_pane.add( "Instrument-specific macros/scripts are not implemented" );

      if( s.equals(QENS_MACRO_MI) ) 
        SharedData.status_pane.add( "Instrument-specific macros/scripts are not implemented" );

      if( s.equals(SAD_MACRO_MI) ) 
        SharedData.status_pane.add( "Instrument-specific macros/scripts are not implemented" );

      if( s.equals(SAND_MACRO_MI) ) 
        SharedData.status_pane.add( "Instrument-specific macros/scripts are not implemented" );

      if( s.equals(SCD_MACRO_MI) ) 
        SharedData.status_pane.add( "Instrument-specific macros/scripts are not implemented" );

      if( s.equals(SEPD_MACRO_MI) ) 
        SharedData.status_pane.add( "Instrument-specific macros/scripts are not implemented" );

 

 
    //menuitem for macro loader below
 
      if( s.equals( System.getProperty("Inst1_Name") )  )
        setupLiveDataServer( "Inst1_Path" );
 
      if( s.equals( System.getProperty("Inst2_Name") )  )
        setupLiveDataServer( "Inst2_Path" );

      if( s.equals( System.getProperty("Inst3_Name") )  )
        setupLiveDataServer( "Inst3_Path" );
        
      if( s.equals( System.getProperty("Inst4_Name") )  )
        setupLiveDataServer( "Inst4_Path" );

      if( s.equals( System.getProperty("Inst5_Name") )  )
        setupLiveDataServer( "Inst5_Path" );

      if( s.equals( System.getProperty("Inst6_Name") )  )
        setupLiveDataServer( "Inst6_Path" );

      if( s.equals( System.getProperty("Inst7_Name") )  )
        setupLiveDataServer( "Inst7_Path" );

      if( s.equals( System.getProperty("Inst8_Name") )  )
        setupLiveDataServer( "Inst8_Path" );

      if( s.equals( System.getProperty("Inst9_Name") ) ) 
        setupLiveDataServer( "Inst9_Path" ); 

      if( s.equals( System.getProperty("Inst10_Name") )  )
        setupLiveDataServer( "Inst10_Path" );

      if( s.equals( System.getProperty("Inst11_Name") )  )
        setupLiveDataServer( "Inst11_Path" );

      if( s.equals( System.getProperty("Inst12_Name") )  )
        setupLiveDataServer( "Inst12_Path" );

      if( s.equals( System.getProperty("Inst13_Name") )  )
        setupLiveDataServer( "Inst13_Path" );
 
      /* if( s.equals(GSAS_EXPORT_MI) )
	 {
	 //fc = new JFileChooser(new File(System.getProperty("user.dir")) );
	 if( filename == null)
	 filename = System.getProperty("user.dir");
	 fc.setCurrentDirectory( new File(filename));          
	 int state = fc.showSaveDialog(null);
	 if (state ==0 && fc.getSelectedFile() != null)
	 {
	 File f = fc.getSelectedFile();
	 filename =f.toString();
	 
	 MutableTreeNode node = jdt.getSelectedNode();
	 if( node instanceof DataSetMutableTreeNode )
	 {
	 DataSet ds = ( (DataSetMutableTreeNode)node ).getUserObject();
	 DataSet mon_ds = 
	 ( (DataSetMutableTreeNode)(node.getParent().getChildAt(0)) ).getUserObject();
	 
	 DataSetTools.writer.GsasWriter gw=
	 new DataSetTools.writer.GsasWriter(filename);
	 gw.writeDataSets(new DataSet[] {mon_ds,ds});
	 }
	 }
	 } */
     

      if( s.equals(IMAGE_VIEW_MI) )
      {
        DataSet ds = getViewableData(  jdt.getSelectedNodePaths()  );
        if(  ds != DataSet.EMPTY_DATA_SET  && ds != null){
          new ViewManager( ds, IViewManager.IMAGE );
          ds.setPointedAtIndex( 0 );
          ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
        }else{
          SharedData.status_pane.add( "nothing is currently highlighted in the tree" );
        }
      }
                 
                 
      if( s.equals(SELECTED_VIEW_MI) )  
      { //SharedData.status_pane.add("Hi There");
        
        DataSet ds = getViewableData(  jdt.getSelectedNodePaths()  );
        if(  ds != DataSet.EMPTY_DATA_SET  && ds != null){
          new ViewManager( ds, IViewManager.SELECTED_GRAPHS );
          ds.setPointedAtIndex( 0 );
          ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
        }else{
            SharedData.status_pane.add( "nothing is currently highlighted in the tree" );
        }
      }
                         
                 
      if( s.equals(SCROLL_VIEW_MI) )
      {   
        DataSet ds = getViewableData(  jdt.getSelectedNodePaths()  );
        if(  ds != DataSet.EMPTY_DATA_SET  && ds != null){
          new ViewManager( ds, IViewManager.SCROLLED_GRAPHS );
          ds.setPointedAtIndex( 0 );
          ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
        }else{
            SharedData.status_pane.add( "nothing is currently highlighted in the tree" );
        }
      }
                 

      if( s.equals(THREED_VIEW_MI) )
      {   
        DataSet ds = getViewableData(  jdt.getSelectedNodePaths()  );
        if(  ds != DataSet.EMPTY_DATA_SET  && ds != null){
            new ViewManager( ds, IViewManager.THREE_D );
            ds.setPointedAtIndex( 0 );
            ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
        }else{
            SharedData.status_pane.add( "nothing is currently highlighted in the tree" );
        }
        return;
      }

      if( s.equals(TABLE_VIEW_MI) )
      {   
        DataSet ds = getViewableData(  jdt.getSelectedNodePaths()  );
        if(  ds != DataSet.EMPTY_DATA_SET  && ds != null){
            new ViewManager( ds, IViewManager.TABLE );
            ds.setPointedAtIndex( 0 );
            ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
        }else{
            SharedData.status_pane.add( "nothing is currently highlighted in the tree" );
        }
        return;
      }

      if( s.equals(LOG_VIEW_MI) )
      {   
        SDDS.java.SDDSedit.sddsEdit frame = new SDDS.java.SDDSedit.sddsEdit();
	  frame.pack();
        frame.setVisible(true); 
        frame.setSize(400,500);
        frame.setTitle("SDDS Editor");
      }
           
      if( s.equals(ABOUT_MI) )
      {
        String S=DataSetTools.util.FilenameUtil.helpDir("About.html");
	HTMLPage H = new HTMLPage( S ) ;
	Dimension D = getToolkit().getScreenSize();
	// make the help window pop up centered and 60% of screen size
	H.setSize((int)(.6*4*D.height/3) , (int)(.6*D.height) ); 
	H.setLocation((int)(.2*4*D.height/3), (int)(.2*D.height) );
	try{
	    H.show();
	}
	catch(Exception e){
	    SharedData.status_pane.add("CANNOT FIND HELP FILE");
	}
        
	H.show();
      } 
                
      if( s.equals(OPERATIONS_MI) )
      {
        String S=DataSetTools.util.FilenameUtil.helpDir("Help.html");
	HTMLPage H = new HTMLPage( S ) ;
	Dimension D = getToolkit().getScreenSize();
	// make the help window pop up centered and 60% of screen size
	H.setSize((int)(.6*4*D.height/3) , (int)(.6*D.height) ); 
	H.setLocation((int)(.2*4*D.height/3), (int)(.2*D.height) );
	try{
	    H.show();
	}
	catch(Exception e){
	    SharedData.status_pane.add("CANNOT FIND HELP FILE");
	}
        
	H.show();
      } 
                
      if( s.equals(COMMAND_PANE_MI) )
      {
	String S=DataSetTools.util.FilenameUtil.helpDir("Command/CommandPane.html");
	//S="http://www.pns.anl.gov/ISAW/ISAW%20Tutorial_files/v3_document.htm";
	HTMLPage H = new HTMLPage( S ) ;
	Dimension D = getToolkit().getScreenSize();
	// make the help window pop up centered and 60% of screen size
	H.setSize((int)(.6*4*D.height/3) , (int)(.6*D.height) ); 
	H.setLocation((int)(.2*4*D.height/3), (int)(.2*D.height) );
	try{
	    H.show();
	}
	catch(Exception e){
	    SharedData.status_pane.add("CANNOT FIND HELP FILE");
	}
        
	H.show();
      } 
                
      if( s.equals(GLOSSARY_MI) ){
	  String S=DataSetTools.util.FilenameUtil.docDir("Glossary.html");
	  SharedData.status_pane.add("Displaying glossary in web browser");
	  if( S != null) bc.displayURL(S);
      }

      if( s.equals(API_DOCS_MI) ){
	  String S=DataSetTools.util.FilenameUtil.docDir("index.html");
          if( S != null){
              bc.displayURL(S);
              SharedData.status_pane.add("Displaying API documentation"
                                         +" in web browser");
          }
      }

      if( s.equals(HOME_LINK_MI) ){
	  SharedData.status_pane.add("Displaying ISAW homepage in"
				     +" web browser");
	  bc.displayURL( HOME_LINK );
      }

      if( s.equals(FTP_LINK_MI) ){
	  SharedData.status_pane.add("Displaying ftp site in web browser");
	  bc.displayURL( FTP_LINK );
      }

      if( s.equals(USERMAN_LINK_MI) ){
	  SharedData.status_pane.add("Displaying user manual location"
				     +" in web browser");
	  bc.displayURL( USERMAN_LINK );
      }


                                    //remove some node from the tree.  since
                                    //the tree could change drastically, we'll
                                    //let the tree handle it's own business
                                    //and the messy details of deleting nodes.
      if( s.equals(REMOVE_NODE_MI) )
          jdt.deleteSelectedNodes(); 
    }


    /**
     *
     */ 
    public void setupLiveDataServer( String instr )
    {
      String instrument_computer = System.getProperty( instr );
     // System.out.println( "loading: live data from " + instrument_computer );

      LiveDataMonitor monitor = new LiveDataMonitor( instrument_computer );

      monitor.addIObserver( Isaw.this );

      String name = instrument_computer + " Live Data" ;
      jcui.setTab( name, monitor );
    }
  }
 
 
 
  /**
   * Creates a frame which can display a string array.
   * 
   * @param info   Array of Strings for display.
   */
  public void IsawViewHelp( String[] info )
  {
    JFrame mm = new JFrame();
    JDialog hh = new JDialog();
    hh.setSize(188,70);

    //Center the opdialog frame 
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension size = hh.getSize();
    screenSize.width = (int)(screenSize.height*2/3);
    screenSize.height = screenSize.height/2;
    size.width = (int)(size.height*2/3);
    size.height = size.height/2;
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
                    //keep a reference to the frame that ISAW resides
                    //within so we have access to the menu within this
                    //listener
    JFrame isaw_frame = null;

    /* 
     *
     */
    public TreeSelectionHandler( JFrame frame )
    {
      isaw_frame = frame;
    }
    

    /*
     * creates menu of operators if appropriate for the current selection
     */
    public void valueChanged( TreeSelectionEvent e )
    {
                                                  //deal w/ unselection events
      if( e.getNewLeadSelectionPath() == null )
      {
        oMenu.removeAll();
        oMenu.add(  new JMenuItem( "[empty]" )  );
        return;
      }

      if(  e.getPaths().length < 1  ) 
      {
        oMenu.removeAll();
        oMenu.add(  new JMenuItem( "[empty]" )  );
        return;
      }
                                                 //deal w/ all other
                                                 //selection events
      MutableTreeNode node = jdt.getSelectedNode();
      if(  node instanceof Experiment  )
      {
        oMenu.removeAll();
        oMenu.add(  new JMenuItem( "[empty]" )  );
    
        return; 
      }

      else if(  node instanceof DataSetMutableTreeNode  )
      {
        DataSetMutableTreeNode dsmtn = (DataSetMutableTreeNode)node;
        DataSet ds = (DataSet)dsmtn.getUserObject();
        
        jcui.showLog(ds);

        JTable table = jcui.showDetectorInfo(ds);
//        table.hasFocus();  
        jpui.showAttributes( ds.getAttributeList() );

                              //since the Operations menu is sensitive
                              //to tree selections, we have to look 
                              //after keeping the menu up to date.  so,
                              //here we build a menu according to the
                              //class of the selected node.
        int num_ops = ds.getNum_operators(); 

//        System.out.println( "number of operators: " + num_ops );

        Operator ds_ops[] = new Operator[num_ops];
        for ( int i = 0; i < num_ops; i++ )
          ds_ops[i] = ds.getOperator(i);
 
        ActionListener listener = new JOperationsMenuHandler( ds, 
                                                              jdt, 
                                                              Isaw.this,
                                                              sessionLog );
        oMenu.removeAll();
        OperatorMenu.build( oMenu, ds_ops, listener );
      }

      else if(  node instanceof DataMutableTreeNode  )
      {
        oMenu.removeAll();
        oMenu.add(  new JMenuItem( "[empty]" )  );
        return; 
      }

      else
      {
        SharedData.status_pane.add( "type not appropriate for operators" );

        oMenu.removeAll();
        oMenu.add(  new JMenuItem( "[empty]" )  );
        return; 
      }
    }
  }
   /**
     * Try to determine whether this application is running under Windows
     * or some other platform by examing the "os.name" property.
     *
     * @return true if this application is running under a Windows OS
     */
    public static boolean isWindowsPlatform()
    {
        String os = System.getProperty("os.name");

        if ( os != null && os.startsWith(WIN_ID))
            return true;
        else
            return false;
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
    int window_width = (int)(screenSize.height*.95*4/3);
    size.height = window_height/2+200;
    size.width = window_width/2;
    int y = window_height - size.height;
    int x = window_width - size.width;
    kp.setLocation(x, y);
    kp.setVisible(true);

    JMenuBar mb = new JMenuBar();
    JMenu fi = new JMenu( FILE_M );
    JMenuItem op1 = new JMenuItem("Save");
    fi.add(op1);
    mb.add(fi);
    JMenuItem op2 = new JMenuItem("Save and Exit");
    fi.add(op2);
    mb.add(fi);
    JMenuItem op3 = new JMenuItem("Exit");
    fi.add(op3);
    mb.add(fi);

    kp.setJMenuBar(mb);  
    op1.addActionListener(new propsHandler());
    op2.addActionListener(new propsHandler());
    op3.addActionListener(new propsHandler());
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
      SharedData.status_pane.add("Document is null");   
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
      if( s.equals("Save") ){ 
        (new Util()).saveDoc( doc , filename );        
        SharedData.isaw_props.reload();
        SharedData.status_pane.add( "IsawProps saved successfully") ;     
      }else if( s.equals("Save and Exit") ){
        (new Util()).saveDoc( doc , filename );        
        SharedData.isaw_props.reload();
        SharedData.status_pane.add( "IsawProps saved successfully") ;     
	kp.dispose();
      }else if( s.equals("Exit") ){ 
        kp.dispose();
      }
      else
        SharedData.status_pane.add( "Unable to quit" );
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
    boolean windows = isWindowsPlatform();
    
    try{
      FileInputStream input = new FileInputStream(path + "IsawProps.dat" );
      isawProp.load( input );
      System.setProperties(isawProp);  
      input.close();
    }catch( IOException ex ){
      DefaultProperties prop=new DefaultProperties();
      prop.write();
    }


    SplashWindowFrame sp = new SplashWindowFrame();
    Thread splash_thread = new Thread(sp);
    splash_thread.start();
    splash_thread = null;
    sp = null;

    /* assume a 4:3 aspect ratio for the monitor */
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int window_height = (int)(screenSize.height*0.4);
    int window_width = (int)(screenSize.height*4*0.8/3);

    /* set the top left corner such that the window is centered
     * horizontally and 3/4 of the way down vertically */
    int y = (int)(screenSize.height - window_height)*3/4;
    int x = (int)(screenSize.height*4/3 - window_width)/2;

    System.out.print("Loading " + TITLE + " Release ");
    if(SharedData.VERSION.equals("Unknown_Version")){
        System.out.println("1.2.1 alpha");
    }else{
        System.out.println(SharedData.VERSION );
    }
    JFrame Isaw = new Isaw( args );
    Isaw.pack();
    Isaw.setBounds(x,y,window_width,window_height);
    Isaw.show();
    Isaw.validate();
    //SharedData.status_pane.add("Hi There");
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
 
                                  //this means that a new DataSet has
                                  //been created. 
    if ( reason instanceof DataSet )
    {
      DataSet ds = (DataSet)reason;
      MutableTreeNode node = jdt.getNodeOfObject( reason );
 
      if( node == null ) 
      {

                    //this must be a new DataSet object...
                    //put it in the modified folder on 
                    //the tree and send to command pane
        addModifiedDataSet( ds );
        return;
      }
      else
        return;
    }
    else if( reason instanceof String )
    {
//      System.out.println( "reason (Isaw.java): " + (String)reason );
    }
    else
      SharedData.status_pane.add( "unsupported type in Isaw.update()" );
  }
 
 
 String data_dir = null;
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
       if( data_dir==null) data_dir = System.getProperty( DATA_DIR_ENV );
      if( data_dir == null )
        data_dir = System.getProperty( "user.home" );
        
                                       //create and display the 
                                       //file chooser, load files
      JFrame frame = new JFrame();
      fc.setCurrentDirectory(  new File( data_dir )  );
      fc.setMultiSelectionEnabled( true );
      fc.setFileFilter(  new NeutronDataFileFilter()  ); 
      fc.addChoosableFileFilter(  new NexIO.NexusfileFilter()  );
      fc.addChoosableFileFilter(  new IPNS.Runfile.RunfileFilter()  );
      Dimension d = new Dimension(650,300);
      fc.setPreferredSize(d);
      if(  fc.showDialog(frame,null) == JFileChooser.APPROVE_OPTION  ) 
      {
        setCursor(  Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR )  );

        load_files(  fc.getSelectedFiles()  );
      
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        data_dir = fc.getSelectedFile().toString();
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
          SharedData.status_pane.add(  "loading (forced): " + removeForce( filenames[i] )  );
          files[i] = new File(  removeForce( filenames[i] )  );
        }
        else if(  filter.accept_filename( filenames[i] )  )
        {
        //  System.out.println( "loading: " + filenames[i] );
          files[i] = new File( filenames[i] );
        }
        else
          SharedData.status_pane.add(  "failed: " + filenames[i]  );

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
        {
         DataSet DSS[];
         DSS = util.loadRunfile(  files[i].getPath()  );
         if( DSS != null)
           if( DSS.length > 0)
            {
              addNewDataSets( DSS , 
                         ""+DSS[0].getTag()+":"+files[i].getName()  );
   
               util.appendDoc(  sessionLog, "Load " + files[i].toString()  );
              
            }
	}
    return;
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
    DataSet ds     = DataSet.EMPTY_DATA_SET;
    DataSet new_ds = DataSet.EMPTY_DATA_SET;

    try{  // pick up on the lack of TreePath
        MutableTreeNode node 
            = (MutableTreeNode)( tps[0].getLastPathComponent() );
        ds = jdt.getDataSet( node );  //if it's just one (1) DataSet object
                                      //nothing need be done... just return it
        node = (MutableTreeNode)tps[0].getLastPathComponent();
        if(  node instanceof DataSetMutableTreeNode  &&  tps.length == 1  )
            return ( (DataSetMutableTreeNode)node ).getUserObject(); 

                                //if there are multiple selections of
                                //Data objects, create an empty clone
                                //of the containing DataSet and add
                                //the selections to the clone.
        new_ds = ds.empty_clone();
        new_ds.addLog_entry( "clones w/ selected subset of spectra" );
        for( int i=0;  i<tps.length;  i++ ){
            node = (MutableTreeNode)(  tps[i].getLastPathComponent()  );
            if(  node instanceof DataMutableTreeNode && 
                 jdt.getDataSet( node ).equals( ds )  )
                new_ds.addData_entry( ( 
                            (DataMutableTreeNode)node ).getUserObject() );
        }
        
    }catch(ArrayIndexOutOfBoundsException e){
        /* SharedData.status_pane.add("ERROR: Must choose at least "
           +"one DataSet or DataBlock"); */
        return DataSet.EMPTY_DATA_SET;
    }

    return new_ds;
  }


  /**
   * listens to the events generated by the JDataTree's JTree
   */ 
  class MouseListener extends MouseAdapter
  {
    private JDataTreeRingmaster ringmaster = null;

    public void init()
    {
      ringmaster = new JDataTreeRingmaster( jdt, Isaw.this );
    }


    public void mousePressed( MouseEvent e )
    {
      if(  jdt.getSelectionCount() > 0  )
      {
        TreePath[] selectedPath = null;
        TreePath[] tps          = jdt.getSelectedNodePaths();

        int button1 =  e.getModifiers() & InputEvent.BUTTON1_MASK;
        int button3 =  e.getModifiers() & InputEvent.BUTTON3_MASK;


                                            //respond to right-click events
        if(  button3 == InputEvent.BUTTON3_MASK  )
          ringmaster.generatePopupMenu( tps, e );

                                            //respond to left-click events
        else if(  button1 == InputEvent.BUTTON1_MASK  )
        {
          if( e.getClickCount() == 1 )
             ringmaster.pointAtNode( tps[0] );

          else if( e.getClickCount() == 2 )
            jdt.selectNodesWithPaths( tps );
        }
      }
    }
  }

/**
   * listens to the events generated by the JDataTree's JTree
   */ 
  class KeyListener extends KeyAdapter
  {
    private JDataTreeRingmaster ringmaster = null;

    public void init()
    {
      ringmaster = new JDataTreeRingmaster( jdt, Isaw.this );
    }


    public void keyReleased( KeyEvent e )
    {

      if(  jdt.getSelectionCount() > 0  )
      {
        TreePath[] selectedPath = null;
        TreePath[] tps          = jdt.getSelectedNodePaths();

        int downKey =  KeyEvent.VK_DOWN;
        int upKey =    KeyEvent.VK_UP;
                                            //respond to up-down key events
        if(  downKey == KeyEvent.VK_DOWN || upKey == KeyEvent.VK_UP )
        {
             ringmaster.pointAtNode( tps[0] );
        }
      }
    }
  }


}
