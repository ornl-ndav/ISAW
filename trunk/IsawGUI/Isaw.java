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
 * Modified:
 *
 *  $Log$
 *  Revision 1.161  2003/10/14 22:12:37  dennis
 *  Fixed javadoc comment to build cleanly on jdk 1.4.2
 *
 *  Revision 1.160  2003/10/09 20:06:14  dennis
 *  Changed version to 1.6.0 alpha 1
 *
 *  Revision 1.159  2003/10/08 22:49:05  dennis
 *  Set version to 1.5.1
 *
 *  Revision 1.158  2003/10/03 19:04:26  dennis
 *  Set version to 1.5.1 beta 8
 *
 *  Revision 1.157  2003/09/23 21:11:06  dennis
 *  Set version number to 1.5.1 beta 7
 *
 *  Revision 1.156  2003/09/22 12:34:23  dennis
 *  Changed version to 1.5.1 beta 6
 *
 *  Revision 1.155  2003/09/15 22:29:11  dennis
 *  Set version number to 1.5.1 beta 5
 *
 *  Revision 1.154  2003/09/08 23:19:22  dennis
 *  Set version number to 1.5.1 beta 4
 *
 *  Revision 1.153  2003/08/28 23:12:46  dennis
 *  Set version number to 1.5.1 beta 3
 *
 *  Revision 1.152  2003/08/28 18:52:38  dennis
 *  Added support for loading .csd files (concatenated files from
 *  Ideas MC simulations).
 *
 *  Revision 1.151  2003/08/15 20:10:42  dennis
 *  Set version number to 1.5.1 beta 2
 *
 *  Revision 1.150  2003/08/11 22:56:50  dennis
 *  Set version number to 1.5.1 beta 1
 *
 *  Revision 1.149  2003/08/08 18:00:07  dennis
 *  Added new Selected Graph view to menus.  Cleaned up handling of
 *  viewer selection.
 *  Now when a new viewer is popped up, the "Pointed At" data block
 *  will only be set to the first Data block, if it was not previously
 *  set to a valid value.
 *
 *  Revision 1.148  2003/08/06 19:50:01  dennis
 *  Changed version to 1.5.1 alpha 11
 *
 *  Revision 1.147  2003/08/06 14:32:48  dennis
 *  Changed version to 1.5.1 alpha 10
 *
 *  Revision 1.146  2003/08/01 13:55:21  dennis
 *  Changed version to 1.5.1 alpha 9
 *
 *  Revision 1.145  2003/08/01 13:29:43  rmikk
 *  Placed a try-catch throwable structure around the main
 *    program to get a stack trace for any uncaught errors
 *
 *  Revision 1.144  2003/07/31 20:12:17  rmikk
 *  Added a print before a printStack trace for the error message
 *
 *  Revision 1.143  2003/07/31 19:26:47  rmikk
 *  Caught Throwable and printed the strack trace in this
 *  class
 *
 *  Revision 1.142  2003/07/16 22:43:58  dennis
 *  Changed version to 1.5.1 alpha 8.
 *
 *  Revision 1.141  2003/07/09 21:32:25  dennis
 *  Changed version to 1.5.1 alpha 7
 *
 *  Revision 1.140  2003/06/30 17:32:20  dennis
 *  Changed version to 1.5.1 alpha 6
 *
 *  Revision 1.139  2003/06/27 18:37:31  dennis
 *  changed version to 1.5.1 alpha 5
 *
 *  Revision 1.138  2003/06/23 21:58:24  dennis
 *  Changed version number to 1.5.1_a3
 *
 *  Revision 1.137  2003/06/17 21:41:20  pfpeterson
 *  Brought back the separate thread for initializing Script_Class_List_Handler.
 *
 *  Revision 1.136  2003/06/17 16:08:36  pfpeterson
 *  Commented out mulit-threading code which breaks the initialization
 *  of Script_Class_List_Handler.
 *
 *  Revision 1.135  2003/06/16 18:56:41  pfpeterson
 *  Script_Class_List_Handler is initialized in a second thread as soon
 *  as possible. Modified timing prints (slightly).
 *
 *  Revision 1.134  2003/06/16 16:48:51  bouzekc
 *  Changed accept_filename to acceptFileName to correspond
 *  with the new FileFilters.
 *
 *  Revision 1.133  2003/06/03 19:22:43  dennis
 *  Changed version number to 1.5.1 alpha 3
 *
 *  Revision 1.132  2003/05/20 20:59:33  pfpeterson
 *  Changed default version number.
 *
 *  Revision 1.131  2003/05/09 21:43:25  pfpeterson
 *  Added option '-t' to show timing information during load process.
 *
 *  Revision 1.130  2003/05/08 20:50:40  pfpeterson
 *  Changed the default version number.
 *
 *  Revision 1.129  2003/03/07 21:11:37  pfpeterson
 *  Now creates a single instance of various listeners for the menus
 *  rather than anonymous listers for each menu item. Speed improvement
 *  of ~30% during initial startup.
 *
 *  Revision 1.128  2003/03/06 23:00:53  pfpeterson
 *  No longer passes StatusPane to anyone. Changed default version,
 *  removed some dead code, and lined up some blocks.
 *
 *  Revision 1.127  2003/03/06 15:49:24  pfpeterson
 *  Changed to work with SharedData's private StatusPane.
 *
 *  Revision 1.126  2003/03/05 20:37:51  pfpeterson
 *  Changed SharedData.status_pane.add(String) to SharedData.addmsg(String)
 *
 *  Revision 1.125  2003/02/13 21:45:13  pfpeterson
 *  Removed calls to deprecated function fixSeparator.
 *
 *  Revision 1.124  2003/01/27 15:06:22  rmikk
 *  Added the New "Intro to Isaw" menu item under the
 *     Help Menu
 *
 *  Revision 1.123  2003/01/23 22:24:24  pfpeterson
 *  Removed a diagnostic print statement.
 *
 *  Revision 1.122  2003/01/20 17:26:16  pfpeterson
 *  Added method for having a default file filter. Currently allows 'nxs', 'nexus', 
 *  'sdds', 'ipns', 'run', 'gsas', 'gsa', and 'gda'.
 *
 *  Revision 1.121  2002/12/10 20:39:27  pfpeterson
 *  More gracefully errors when JavaHelp is not present.
 *
 *  Revision 1.120  2002/12/08 22:34:35  dennis
 *  The Help->Command option now brings up the new help system. (Ruth)
 *
 *  Revision 1.119  2002/12/03 17:47:44  pfpeterson
 *  Now uses InstrumentViewMenu for the instrument links. Deleted code which
 *  used to serve this purpose. Commented out code for the instrument macro
 *  menu items since the instrument macro menu is commented out.
 *
 *  Revision 1.118  2002/11/27 23:27:07  pfpeterson
 *  standardized header
 *
 *  Revision 1.117  2002/10/24 19:37:29  pfpeterson
 *  Uses new feature in HTMLPage to know not to display a help window
 *  because something went wrong with finding the file.
 *
 *  Revision 1.116  2002/10/24 18:45:45  pfpeterson
 *  No longer imports ChopTools.
 *
 *  Revision 1.115  2002/09/30 20:54:27  pfpeterson
 *  Changed default version number.
 *
 *  Revision 1.114  2002/08/15 21:09:41  pfpeterson
 *  Updated links due to changes in IPNS website.
 *
 *  Revision 1.113  2002/08/15 18:49:01  pfpeterson
 *  Added a verbose, '-v', switch and updated the default version
 *  number.
 *
 *  Revision 1.112  2002/08/12 19:13:37  pfpeterson
 *  Changed the default version if being run from cvs.
 *
 *  Revision 1.111  2002/08/06 21:32:42  pfpeterson
 *  Fixed a filefilter bug where it kept two copies of the selected
 *  filter and added gsas files to the load dialog.
 *
 *  Revision 1.110  2002/07/17 20:13:12  pfpeterson
 *  Commented out the 'Search Database search' and 'Log View' menu items.
 *
 *  Revision 1.109  2002/07/17 19:24:32  rmikk
 *  Updated the View file menu choices
 *
 *  Revision 1.108  2002/07/16 21:47:27  rmikk
 *  Add menu items and handlers for the quick table viewers
 *
 *  Revision 1.107  2002/07/12 18:24:12  pfpeterson
 *  Uses new methods of SharedData for getting properties.
 *
 *  Revision 1.106  2002/07/10 19:43:29  rmikk
 *  Added Code to connect the Contour view to Isaw
 *
 *  Revision 1.105  2002/07/08 20:49:25  pfpeterson
 *  Removed string constants from here that are now
 *  in DataSetTools.util.FontUtil.
 *
 *  Revision 1.104  2002/07/02 17:05:16  pfpeterson
 *  Added public string constants for lambda, angstrom, and inv(angstrom).
 *
 *  Revision 1.103  2002/06/28 13:38:42  rmikk
 *  -Uses the NEW status pane.  The new status pane includes not only the text area 
 *    but now the scroll bars and buttons.
 *    These features do not need to be added by the standalone ISAW application.
 *
 *  Revision 1.102  2002/06/14 15:58:30  pfpeterson
 *  Changed all System.getProperty to SharedData.getProperty. Also
 *  leave the creation of the default properties file the SharedData.
 *
 *  Revision 1.101  2002/06/11 19:19:13  pfpeterson
 *  Removed three lines to increase stability of the JSplitPanes.
 *
 *  Revision 1.100  2002/06/11 14:48:43  pfpeterson
 *  Changed default version to 1.4.0alpha.
 *
 *  Revision 1.99  2002/05/28 21:22:27  pfpeterson
 *  Changed mw SplitPaneWithState to JSPlitPane. Now resizing
 *  is smoother.
 *
 *  Revision 1.98  2002/04/29 15:14:19  pfpeterson
 *  Changed version.
 *
 *  Revision 1.97  2002/04/23 19:04:48  pfpeterson
 *  Fixed problem with percentage status_heights (missing a 'D').
 *
 *  Revision 1.96  2002/04/22 21:00:13  pfpeterson
 *  Changed title on 'Live Data' tabbed pane.
 *
 *  Revision 1.95  2002/04/22 20:08:50  pfpeterson
 *  Now File->Load->Local remembers the last FileFilter used.
 *
 *  Revision 1.94  2002/04/22 19:19:17  pfpeterson
 *  Improved the JSplitPane usage in the main window. Now the
 *  system properties for the divider locations are updated
 *  only when the divider itself is dragged.
 *
 *  Revision 1.93  2002/04/12 20:52:05  pfpeterson
 *  Removed the listeners that pay attention to the split_pane dividers.
 *
 *  Revision 1.92  2002/04/11 16:02:47  pfpeterson
 *  Put version information (w/o build date) in the title bar of
 *  the main ISAW window.
 *
 *  Revision 1.91  2002/04/10 20:47:00  pfpeterson
 *  Added command line switches -v and --version which print the version
 *  of ISAW currently running and exit.
 *
 *  Revision 1.90  2002/04/10 15:39:21  pfpeterson
 *  Added System properties to control MW width and height. Also improved
 *  changing the divider positions in the SplitPanes and removed SplitPanes
 *  that weren't used (get some screen space).
 *
 *  Revision 1.89  2002/04/08 18:22:15  pfpeterson
 *  Added properties to set JSplitPanel portions on startup.
 *
 *  Revision 1.88  2002/03/14 22:27:19  dennis
 *  Changed default version to 1.3.0 alpha.
 *
 *  Revision 1.87  2002/03/12 20:37:48  chatterjee
 *  Added SDDS functionality
 *
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
 *  Modified the help menu system. Now includes, About, Operations (old help), 
 *  Command Pane, and links to online resources.
 *
 *  Revision 1.66  2001/12/05 20:53:05  pfpeterson
 *  extended dual monitor support to cover more windows
 *
 *  Revision 1.65  2001/11/30 23:14:58  pfpeterson
 *  Changed the size and position of the main window to assume a 4:3 aspect ratio 
 *  from the screen height. This should fix the dual monitor problem.
 *
 */

 
package IsawGUI;

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
import DataSetTools.viewer.Table.*;
import ExtTools.SwingWorker;
import IPNS.Runfile.*;
import java.applet.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.beans.*;
import java.io.*;
import java.io.IOException; 
import java.lang.*;
import java.lang.reflect.Array;
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
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicRootPaneUI;
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
  private static long   time=System.currentTimeMillis();

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
  private static final String IMAGE_VIEW_MI      = IViewManager.IMAGE;
  private static final String SCROLL_VIEW_MI     = IViewManager.SCROLLED_GRAPHS;
  private static final String SELECTED_VIEW_MI   = IViewManager.SELECTED_GRAPHS;
  private static final String SELECTED_VIEW_2_MI = IViewManager.SELECTED_GRAPH2;
  private static final String THREED_VIEW_MI     = IViewManager.THREE_D;
  private static final String TABLE_VIEW_MI      = IViewManager.TABLE;
  private static final String CONTOUR_VIEW_MI    = IViewManager.CONTOUR;
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

  private static final String IPNS_URL   = "http://www.pns.anl.gov/";
  private static final String DB_URL     = IPNS_URL+"computing/ISAW/";
  private static final String WIN_ID     = "Windows";

  private static boolean showTiming=false;
  JDataTree jdt;  

  JPropertiesUI jpui;
  JCommandUI jcui;
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

  static JSplitPane upper_sp;
  static JSplitPane main_sp;

  /**
   * Creates a JFrame that displays different Isaw components.
   *
   * @param args  an array of String objects that correspond
   *              to the command line arguments, not including the
   *              name of the program.
   */
  public Isaw( String[] args ) 
  { 
    super( TITLE+" "+getVersion(false) );
                      //used for loading runfiles
    if(showTiming)
      System.out.println("00:"+(System.currentTimeMillis()-time)/1000.);
    util = new Util(); 
    Vector mm = util.listProperties();
    JScrollPane tt = util.viewProperties();
    if(showTiming)
      System.out.println("01:"+(System.currentTimeMillis()-time)/1000.);
    cp = new Command.CommandPane();
    if(showTiming)
      System.out.println("02:"+(System.currentTimeMillis()-time)/1000.);
    cp.addIObserver( this );
    cp.setLogDoc(sessionLog);
    WindowResizeListener wrl=new WindowResizeListener(this);

    jpui = new JPropertiesUI();
    jpui.setPreferredSize( new Dimension(200, 200) );
    jpui.setMinimumSize(new Dimension(20, 50));

    jcui = new JCommandUI( cp, sessionLog, jpui );
    jcui.setPreferredSize( new Dimension( 700, 50 ) );
    jcui.setMinimumSize(new Dimension(20, 50));  
    MouseListener ml = new MouseListener();
    KeyListener kl = new KeyListener(); 
    this.addComponentListener(wrl);
    jdt = new JDataTree( ml, kl);
    ml.init();
    kl.init();
    jdt.setPreferredSize(new Dimension(200, 500));
    jdt.setMinimumSize(new Dimension(20, 50));
    jdt.addTreeSelectionListener(  new TreeSelectionHandler( this )  );
        
    /* Checks for various command line options. Some options are acted
       upon immediatly, others are returned in a Hashtable object of
       name value pairs for later use. Data files specified at the
       command line are loaded immediatly.*/
    parse_args( args );
    if(showTiming)
      System.out.println("03:"+(System.currentTimeMillis()-time)/1000.);
    setupMenuBar();        
    if(showTiming)
      System.out.println("04:"+(System.currentTimeMillis()-time)/1000.);

    cp.addPropertyChangeListener(SharedData.getStatusPane());

    upper_sp= new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
                              jdt, jcui); 
    Component sp_comp[]=upper_sp.getComponents();
    for( int i=0 ; i<sp_comp.length ; i++ ){
        if(sp_comp[i] instanceof BasicSplitPaneDivider)
            sp_comp[i].addMouseListener(wrl);
    }
    main_sp= new JSplitPane( JSplitPane.VERTICAL_SPLIT,
                             upper_sp,SharedData.getStatusPane());
    sp_comp=main_sp.getComponents();
    for( int i=0 ; i<sp_comp.length ; i++ ){
        if(sp_comp[i] instanceof BasicSplitPaneDivider)
            sp_comp[i].addMouseListener(wrl);
    }
    upper_sp.setOneTouchExpandable(true);
    Container con = getContentPane();
    con.add(main_sp);
  }
    
    /**
     * return version information
     *
     * @param Long  If true return full version information (including
     *              build date if possible) with TITLE in front. If
     *              false, return * just the version number.
     */
    public static String getVersion(boolean Long){
        String version;
        String val="";

        if(SharedData.VERSION.equals("Unknown_Version"))
          version="1.6.0 alpha 1";
        else
          version=SharedData.VERSION;

        if(Long){
            val=TITLE + " Release "+version;
            if(SharedData.BUILD_DATE.equals("Unknown_Build_Date")){
                // do nothing
            }else{ 
                val=val+" Built "+SharedData.BUILD_DATE;
            }
        }else{
            val=version;
        }

        return val;
    }
     
    /**
     * Set the size of the ISAW main window using values found in the
     * System properties.
     */
    public void setBounds(){
        int x,y,window_height,window_width;
        double sys_width,sys_height;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        /* read in values for default screen size from system properties */
        sys_width=SharedData.getdoubleProperty("Isaw_Width");
        sys_height=SharedData.getdoubleProperty("Isaw_Height");
        //System.out.println("("+sys_width+","+sys_height+")");

        /* assume a 4:3 aspect ratio for the monitor */
        if(sys_width>1.0){
            window_width = (int)sys_width;
        }else{
            window_width = (int)(screenSize.height*4*sys_width/3);
        }
        if(sys_height>1.0){
            window_height = (int)sys_height;
        }else{
            window_height = (int)(screenSize.height*sys_height);
        }
        //System.out.println("("+window_width+","+window_height+")");
        
        /* set the top left corner such that the window is centered
         * horizontally and 3/4 of the way down vertically */
        y = (int)(screenSize.height - window_height)*3/4;
        x = (int)(screenSize.height*4/3 - window_width)/2;

        this.setBounds(x,y,window_width,window_height);
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
    JMenuItem graphView2  = new JMenuItem( SELECTED_VIEW_2_MI );
    JMenuItem threeDView = new JMenuItem( THREED_VIEW_MI );
    JMenuItem tableView = new JMenuItem( TABLE_VIEW_MI );
    JMenu Tables= new JMenu("Selected Table View");

    // set up some listeners for later use
    MenuItemHandler menu_item_handler          =new MenuItemHandler();
    AttributeMenuItemHandler attr_menu_handler =new AttributeMenuItemHandler();
    LoadMenuItemHandler load_menu_handler      =new LoadMenuItemHandler();

    for( int k=0; k< TableViewMenuComponents.getNMenuItems(); k++)
    {
      JMenuItem jmi= new JMenuItem(TableViewMenuComponents.getNameMenuItem(k));
      jmi.addActionListener(menu_item_handler);
      Tables.add(jmi);
    }
    JMenuItem contourView = new JMenuItem( CONTOUR_VIEW_MI );
    JMenuItem logView = new JMenuItem( LOG_VIEW_MI );
    JMenu instrumentInfoView = null;
    try{
      instrumentInfoView = new InstrumentViewMenu( INSTR_VIEW_M );
    }catch(InstantiationException e){
      // leave instrumentInfoView as null so it is not added to the menus
    }

    Script_Class_List_Handler SP = new Script_Class_List_Handler();      
    opMenu macrosMenu = new opMenu(SP, jdt, sessionLog , Isaw.this);
    macrosMenu.setOpMenuLabel( MACRO_M );
    //macrosMenu.addStatusPane( SharedData.getStatusPane() ); //REMOVE

    JMenu hMenu               = new JMenu( HELP_M );
    JMenuItem helpAbout       = new JMenuItem( ABOUT_MI );
    JMenuItem helpOperations  = new JMenuItem( OPERATIONS_MI );
    JMenuItem helpCommandPane = new JMenuItem( COMMAND_PANE_MI );
    JMenuItem glossary        = new JMenuItem( GLOSSARY_MI );
    JMenuItem apiDocs         = new JMenuItem( API_DOCS_MI );
    JMenuItem homeLink        = new JMenuItem( HOME_LINK_MI );
    JMenuItem ftpLink         = new JMenuItem( FTP_LINK_MI );
    JMenuItem docLink         = new JMenuItem( USERMAN_LINK_MI );

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
      
    boolean found =true;
    for( int ii=1;  ii<14 && found;  ii++ )
    {
      String SS = SharedData.getProperty("Inst"
                                   +new Integer(ii).toString().trim()+"_Name");
      if( SS == null) 
        found=false;
      else
      {
        JMenuItem dummy = new JMenuItem(SS);
        dummy.setToolTipText( SharedData.getProperty("Inst"
                                  +new Integer(ii).toString().trim()+"_Path"));
        dummy.addActionListener(menu_item_handler);
        LiveData.add(dummy);
      }
    }
        
    vMenu.add(imageView);
    vMenu.add(threeDView);
    vMenu.add( contourView );
    vMenu.add(s_graphView);
    vMenu.add(graphView);
    vMenu.add(graphView2);
    
    
    vMenu.add( Tables );
    vMenu.add( tableView );
    //vMenu.add( logView );
    if(instrumentInfoView!=null)
      vMenu.add(instrumentInfoView);         
      
    hMenu.add(helpAbout);
    hMenu.add(helpOperations);
    hMenu.add(helpCommandPane);
    hMenu.add(glossary);
    hMenu.add(apiDocs);
    hMenu.add( new IsawHelp.SiteHelp() );
    hMenu.addSeparator();
    hMenu.add(homeLink);
    hMenu.add(ftpLink);
    hMenu.add(docLink);

    fileExit.addActionListener(menu_item_handler);
    Runfile.addActionListener(load_menu_handler);
    LiveData.addActionListener(load_menu_handler);

    script_loader.addActionListener(  new ScriptLoadHandler(this)  );

    fileSaveData.addActionListener( menu_item_handler );
    //fileSaveDataAs.addActionListener( menu_item_handler );
    dbload.addActionListener( menu_item_handler );
    
    graphView.addActionListener(menu_item_handler); 
    graphView2.addActionListener(menu_item_handler); 
    s_graphView.addActionListener(menu_item_handler); 

    threeDView.addActionListener(menu_item_handler); 
    imageView.addActionListener(menu_item_handler);  
    tableView.addActionListener(menu_item_handler);  
    contourView.addActionListener(menu_item_handler);  
    logView.addActionListener(menu_item_handler);      
    
    fileLoadDataset.addActionListener(menu_item_handler);
    removeSelectedNode.addActionListener(menu_item_handler);
    editProps.addActionListener(attr_menu_handler);
    editAttributes.addActionListener(attr_menu_handler);
    editSetAttribute.addActionListener(attr_menu_handler);
    setGroupAttributes.addActionListener(attr_menu_handler);
    clearSelection.addActionListener(attr_menu_handler);
    
    helpAbout.addActionListener(menu_item_handler);
    helpOperations.addActionListener(menu_item_handler);
    helpCommandPane.addActionListener(menu_item_handler);
    glossary.addActionListener(menu_item_handler);
    apiDocs.addActionListener(menu_item_handler);
    homeLink.addActionListener(menu_item_handler);
    ftpLink.addActionListener(menu_item_handler);
    docLink.addActionListener(menu_item_handler);
   
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
   * @param dss  Array of DataSets
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
          {  String S= SharedData.getProperty("IsawFileServer"+i+"_Name");
             
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
          {  String S= SharedData.getProperty("NDSFileServer"+i+"_Name");
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
              filename = SharedData.getProperty( "Script_Path" );
        if( filename == null )
          filename = SharedData.getProperty( "user.home" );

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
        {  SharedData.addmsg("Choose an input file");
          //System.out.println( "Choose a input file" );
          return;
        }

        JDataTree jjt=IS.jdt;
        cp.getExecScript( filename, IS, jdt, sessionLog );
      }
    }
  }
 
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
               filename =SharedData.getProperty("user.home");
            fc.setCurrentDirectory(  new File( filename )  );
            fc.setMultiSelectionEnabled( false );
	    fc.resetChoosableFileFilters();
            fc.addChoosableFileFilter( new DataSetTools.gsastools.GsasFileFilter() );
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
            SharedData.addmsg( "Error "+e );
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
        { if( filename == null) filename= SharedData.getProperty("user.home");
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
 
    //menuitem for macro loader below
 
      if( s.equals( SharedData.getProperty("Inst1_Name") )  )
        setupLiveDataServer( "Inst1_Path" );
 
      if( s.equals( SharedData.getProperty("Inst2_Name") )  )
        setupLiveDataServer( "Inst2_Path" );

      if( s.equals( SharedData.getProperty("Inst3_Name") )  )
        setupLiveDataServer( "Inst3_Path" );
        
      if( s.equals( SharedData.getProperty("Inst4_Name") )  )
        setupLiveDataServer( "Inst4_Path" );

      if( s.equals( SharedData.getProperty("Inst5_Name") )  )
        setupLiveDataServer( "Inst5_Path" );

      if( s.equals( SharedData.getProperty("Inst6_Name") )  )
        setupLiveDataServer( "Inst6_Path" );

      if( s.equals( SharedData.getProperty("Inst7_Name") )  )
        setupLiveDataServer( "Inst7_Path" );

      if( s.equals( SharedData.getProperty("Inst8_Name") )  )
        setupLiveDataServer( "Inst8_Path" );

      if( s.equals( SharedData.getProperty("Inst9_Name") ) ) 
        setupLiveDataServer( "Inst9_Path" ); 

      if( s.equals( SharedData.getProperty("Inst10_Name") )  )
        setupLiveDataServer( "Inst10_Path" );

      if( s.equals( SharedData.getProperty("Inst11_Name") )  )
        setupLiveDataServer( "Inst11_Path" );

      if( s.equals( SharedData.getProperty("Inst12_Name") )  )
        setupLiveDataServer( "Inst12_Path" );

      if( s.equals( SharedData.getProperty("Inst13_Name") )  )
        setupLiveDataServer( "Inst13_Path" );
 
      if( s.equals(IMAGE_VIEW_MI)      || 
          s.equals(SELECTED_VIEW_MI)   ||
          s.equals(SELECTED_VIEW_2_MI) ||
          s.equals(SCROLL_VIEW_MI)     || 
          s.equals(THREED_VIEW_MI)     ||
          s.equals(TABLE_VIEW_MI)      ||
          s.equals(CONTOUR_VIEW_MI)
          )
      {
        DataSet ds = getViewableData(  jdt.getSelectedNodePaths()  );
        if(  ds != DataSet.EMPTY_DATA_SET  && ds != null){
          new ViewManager( ds, s );
                                         // only set pointed at if not set
          if ( ds.getPointedAtIndex() == DataSet.INVALID_INDEX ){ 
            ds.setPointedAtIndex( 0 );
            ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
          }

        }else{
          SharedData.addmsg( "nothing is currently highlighted in the tree" );
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
        if(! H.isValid()) return;
	Dimension D = getToolkit().getScreenSize();
	// make the help window pop up centered and 60% of screen size
	H.setSize((int)(.6*4*D.height/3) , (int)(.6*D.height) ); 
	H.setLocation((int)(.2*4*D.height/3), (int)(.2*D.height) );
	try{
	    H.show();
	}
	catch(Exception e){
	    SharedData.addmsg("CANNOT FIND HELP FILE");
	}
        
	H.show();
      } 
                
      if( s.equals(OPERATIONS_MI) )
      {
        String S=DataSetTools.util.FilenameUtil.helpDir("Help.html");
	HTMLPage H = new HTMLPage( S ) ;
        if(! H.isValid()) return;
	Dimension D = getToolkit().getScreenSize();
	// make the help window pop up centered and 60% of screen size
	H.setSize((int)(.6*4*D.height/3) , (int)(.6*D.height) ); 
	H.setLocation((int)(.2*4*D.height/3), (int)(.2*D.height) );
	try{
	    H.show();
	}
	catch(Exception e){
	    SharedData.addmsg("CANNOT FIND HELP FILE");
	}
        
	H.show();
      } 
                
      if( s.equals(COMMAND_PANE_MI) )
      { try{
            Component jh = (new Jhelp()).getHelpComponent();
            JFrame jf = new JFrame("Command Pane-Scripting");
            jf.getContentPane().add(jh);
            Dimension D = getToolkit().getScreenSize();
            // make the help window pop up centered and 60% of screen size
            jf.setSize((int)(.6*4*D.height/3) , (int)(.6*D.height));
            jf.show();
            return;
        }catch( Exception sss){
          // let it drop on the floor
        }catch( NoClassDefFoundError e){
          // let it drop on the floor
        }
	String S=
          DataSetTools.util.FilenameUtil.helpDir("Command/CommandPane.html");

	HTMLPage H = new HTMLPage( S ) ;
        if(! H.isValid()) return;
	Dimension D = getToolkit().getScreenSize();

	// make the help window pop up centered and 60% of screen size
	H.setSize((int)(.6*4*D.height/3) , (int)(.6*D.height) ); 
	H.setLocation((int)(.2*4*D.height/3), (int)(.2*D.height) );
	try{
	    H.show();
	}
	catch(Exception e){
	    SharedData.addmsg("CANNOT FIND HELP FILE");
	}
        
	H.show();
      } 
                
      if( s.equals(GLOSSARY_MI) ){
	  String S=DataSetTools.util.FilenameUtil.docDir("Glossary.html");
	  SharedData.addmsg("Displaying glossary in web browser");
	  if( S != null) bc.displayURL(S);
      }

      if( s.equals(API_DOCS_MI) ){
	  String S=DataSetTools.util.FilenameUtil.docDir("index.html");
          if( S != null){
              bc.displayURL(S);
              SharedData.addmsg("Displaying API documentation"
                                         +" in web browser");
          }
      }

      if( s.equals(HOME_LINK_MI) ){
	  SharedData.addmsg("Displaying ISAW homepage in"
				     +" web browser");
	  bc.displayURL( HOME_LINK );
      }

      if( s.equals(FTP_LINK_MI) ){
	  SharedData.addmsg("Displaying ftp site in web browser");
	  bc.displayURL( FTP_LINK );
      }

      if( s.equals(USERMAN_LINK_MI) ){
	  SharedData.addmsg("Displaying user manual location"
				     +" in web browser");
	  bc.displayURL( USERMAN_LINK );
      }


                                    //remove some node from the tree.  since
                                    //the tree could change drastically, we'll
                                    //let the tree handle it's own business
                                    //and the messy details of deleting nodes.
      if( s.equals(REMOVE_NODE_MI) )
          jdt.deleteSelectedNodes(); 

     for( int k=0; k< TableViewMenuComponents.getNMenuItems(); k++)
       if( s.indexOf(TableViewMenuComponents.getNameMenuItem( k)) == 0)
         { DataSet ds = getViewableData(  jdt.getSelectedNodePaths()  );
           if(  ds != DataSet.EMPTY_DATA_SET  && ds != null){
             ds.setPointedAtIndex( 0 );
             new ViewManager( ds, s);
             ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
           }else{
            SharedData.addmsg("nothing is currently highlighted in the tree" );
           }
          return;
         } 
    }


    /**
     *
     */ 
    public void setupLiveDataServer( String instr )
    {
      String instrument_computer = SharedData.getProperty( instr );
     // System.out.println( "loading: live data from " + instrument_computer );

      LiveDataMonitor monitor = new LiveDataMonitor( instrument_computer );
      monitor.addIObserver( Isaw.this );

      String tab_name=instr;
      int index=-1;
      index=tab_name.indexOf("Path");
      if(index>0)
          tab_name=tab_name.substring(0,index)+"Name";
      tab_name=SharedData.getProperty(tab_name)+" Live Data";

      String name = instrument_computer+" Live Data";
      jcui.setTab( tab_name, monitor );
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
        SharedData.addmsg( "type not appropriate for operators" );

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
        String os = SharedData.getProperty("os.name");

        if ( os != null && os.startsWith(WIN_ID))
            return true;
        else
            return false;
    }


    /**
     * Handle resizing the internal parts of ISAW when the main window
     * resizes.
     */
    private static void mw_resized(int width, int height){
        // Set the tree width
        double tree_widthD=SharedData.getdoubleProperty("Tree_Width");
        int tree_width=(int)tree_widthD;
        if(tree_width<=1.0){
            tree_width=(int)(tree_widthD*(double)upper_sp.getWidth());
        }
        upper_sp.setDividerLocation(tree_width);
        
        // Set the status height. Remember that the divider location
        // is relative from the top of the pane.
        double status_heightD=SharedData.getdoubleProperty("Status_Height");
        int status_height=(int)status_heightD;
        if(status_height>1.0){
            status_height=main_sp.getHeight()-status_height;
        }else{
            status_height=(int)((1.0-status_heightD)
                                *(double)main_sp.getHeight());
        }
        main_sp.setDividerLocation(status_height);
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
    String path = SharedData.getProperty("user.home")+"\\";
    path = StringUtil.setFileSeparator(path);
    String filename = path + "IsawProps.dat" ;

    Document doc = (new Util()).openDoc( filename );
    if( doc != null)
    {
      propsText.setDocument( doc ); 
      propsText.setCaretPosition(0);   
    }
    else
      SharedData.addmsg("Document is null");   
  }


  private class propsHandler implements ActionListener 
  {
    public void actionPerformed(ActionEvent ev) 
    {
      String s=ev.getActionCommand();

      String path = SharedData.getProperty("user.home")+"\\";
      path = StringUtil.setFileSeparator(path);
      String filename = path + "IsawProps.dat" ;
                      
      Document doc = propsText.getDocument() ; 
      if( s.equals("Save") ){ 
        (new Util()).saveDoc( doc , filename );        
        SharedData.isaw_props.reload();
        SharedData.addmsg( "IsawProps saved successfully") ;     
      }else if( s.equals("Save and Exit") ){
        (new Util()).saveDoc( doc , filename );        
        SharedData.isaw_props.reload();
        SharedData.addmsg( "IsawProps saved successfully") ;     
	kp.dispose();
      }else if( s.equals("Exit") ){ 
        kp.dispose();
      }
      else
        SharedData.addmsg( "Unable to quit" );
    }
  }

  /**
   * Cuts a new thread to initialize Script_Class_List_Handler
   */
  private static void initScriptList(){
    ExtTools.SwingWorker worker=new ExtTools.SwingWorker(){
        public Object construct(){
          try{
            new Script_Class_List_Handler();
          }catch(Throwable e){
           System.out.println(" Error:"+e.toString());
            e.printStackTrace();
          }finally{
            return null;
          }
        }
      };
    worker.start();
  }

   /**
    * entry point for the ISAW application.
    */
  public static void main( String[] args ) 
  { try{
      System.out.println("Start of IsawMain");
      initScriptList(); // initialize Script_Class_List_Handler
    
      for( int i=0 ; i<Array.getLength(args) ; i++ ){
        if("--version".equals(args[i])){
            System.out.println(getVersion(true));
            System.exit(0);
        }else if("-v".equals(args[i])){
            Script_Class_List_Handler.LoadDebug=true;
            SharedData.DEBUG=true;
        }else if("-t".equals(args[i])){
          showTiming=true;
        }
      }
   
      // load IsawProps.dat into the system properties
      SharedData.isaw_props.reload();
      // show the splashscreen
      SplashWindowFrame sp = new SplashWindowFrame();
      Thread splash_thread = new Thread(sp);
      splash_thread.start();
      splash_thread = null;
      sp = null;
    
      System.out.println("Loading "+getVersion(true));
 
      JFrame Isaw = new Isaw( args );
      Isaw.pack();
      //Isaw.setBounds(x,y,window_width,window_height);
      ((Isaw)Isaw).setBounds();
      Isaw.show();
      Isaw.validate();
      //SharedData.status_pane.add("Hi There");
      Isaw.addWindowListener( new WindowAdapter(){
             public void windowClosing( WindowEvent ev ){
                  //System.out.println("windowClosing:"+ev.toString());
                 System.exit(0);
             } 
         } );
      mw_resized(Isaw.getWidth(),Isaw.getHeight());
    }catch( Throwable ss){
      System.out.println("Error :"+ss.toString() );
      ss.printStackTrace();
    }
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
      SharedData.addmsg( "unsupported type in Isaw.update()" );
  }
 
 
 String                             data_dir   = null;
 javax.swing.filechooser.FileFilter load_filter = null;
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
       if( data_dir==null) data_dir = SharedData.getProperty( DATA_DIR_ENV );
      if( data_dir == null )
        data_dir = SharedData.getProperty( "user.home" );
        
      // get the default FileFilter out of the properties file
      if(load_filter==null){
        String ext=SharedData.getProperty("Default_Ext");
        if(ext!=null){
          ext=ext.toLowerCase();
          if(ext.equals("nxs") || ext.equals("nexus"))
            load_filter=new NexIO.NexusfileFilter();
          else if(ext.equals("sdds"))
            load_filter=new DataSetTools.retriever.SDDSFileFilter();
          else if(ext.equals("ipns") || ext.equals("run"))
            load_filter=new IPNS.Runfile.RunfileFilter();
          else if(ext.equals("gsas") || ext.equals("gsa") || ext.equals("gda"))
            load_filter=new DataSetTools.gsastools.GsasFileFilter();
          else if(ext.equals("csd"))
            load_filter=new DataSetTools.retriever.IdeasFileFilter();
        }
      }

                                       //create and display the 
                                       //file chooser, load files
      JFrame frame = new JFrame();
      fc.setCurrentDirectory(  new File( data_dir )  );
      fc.setMultiSelectionEnabled( true );
      if(!(load_filter instanceof NeutronDataFileFilter))
          fc.setFileFilter(  new NeutronDataFileFilter()  ); 
      if(!(load_filter instanceof DataSetTools.retriever.SDDSFileFilter))
          fc.addChoosableFileFilter( new DataSetTools.retriever.SDDSFileFilter() );
      if(!(load_filter instanceof DataSetTools.gsastools.GsasFileFilter))
          fc.addChoosableFileFilter( new DataSetTools.gsastools.GsasFileFilter() );
      if(!(load_filter instanceof NexIO.NexusfileFilter))
          fc.addChoosableFileFilter( new NexIO.NexusfileFilter()  );
      if(!(load_filter instanceof IPNS.Runfile.RunfileFilter))
          fc.addChoosableFileFilter( new IPNS.Runfile.RunfileFilter()  );
      if(!(load_filter instanceof DataSetTools.retriever.IdeasFileFilter))
          fc.addChoosableFileFilter( new DataSetTools.retriever.IdeasFileFilter()  );
      Dimension d = new Dimension(650,300);
      fc.setPreferredSize(d);
      if (load_filter!=null) fc.setFileFilter(load_filter);
      if(  fc.showDialog(frame,null) == JFileChooser.APPROVE_OPTION  ) 
      {
        setCursor(  Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR )  );
         
        load_files(  fc.getSelectedFiles()  );
      
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        data_dir = fc.getSelectedFile().toString();
        load_filter = fc.getFileFilter();
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
        if(  filter.acceptFileName( filenames[i] )  ||  isForced( filenames[i] )  )
          count++;

                                       //load the files w/ acceptable
                                       //names
      File[] files = new File[ count ];
      for( int i=0;  i<filenames.length;  i++ )
        if(  isForced( filenames[i] )  )
        {
          SharedData.addmsg(  "loading (forced): " + removeForce( filenames[i] )  );
          files[i] = new File(  removeForce( filenames[i] )  );
        }
        else if(  filter.acceptFileName( filenames[i] )  )
        {
        //  System.out.println( "loading: " + filenames[i] );
          files[i] = new File( filenames[i] );
        }
        else
          SharedData.addmsg(  "failed: " + filenames[i]  );

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

    /**
     * Listens for property change events in the main window and
     * splitpanes
     */
    class WindowResizeListener implements MouseInputListener, 
                                          ComponentListener {
        Isaw isaw;

        public WindowResizeListener(Isaw mine){
            this.isaw=mine;
        }
        // ComponentListener interface
        public void componentHidden(ComponentEvent ev){
        }
        public void componentShown(ComponentEvent ev){
        }
        public void componentMoved(ComponentEvent ev){
        }
        public void componentResized(ComponentEvent ev){
            String param=ev.paramString();
            if(ev.getComponent().equals(isaw)){
                if(isaw.isVisible()){
                    param=this.getDimension(param);
                    mw_resized(this.getWidth(param),this.getHeight(param));
                }
            }
        }

        // MouseInputListener interface
        public void mouseClicked(MouseEvent ev){
        }
        public void mouseEntered(MouseEvent ev){
        }
        public void mouseExited(MouseEvent ev){
        }
        public void mousePressed(MouseEvent ev){
        }
        public void mouseReleased(MouseEvent ev){
            JSplitPane sp=(JSplitPane)
                ((BasicSplitPaneDivider)ev.getSource()).getParent();
            int orientation=sp.getOrientation();
            int position=sp.getDividerLocation();

            if(orientation==JSplitPane.VERTICAL_SPLIT)
                setNewVal("Status_Height",sp.getHeight(),position);
            else if(orientation==JSplitPane.HORIZONTAL_SPLIT)
                setNewVal("Tree_Width",   sp.getWidth(), position);
        }
        public void mouseDragged(MouseEvent ev){
        }
        public void mouseMoved(MouseEvent ev){
        }

        // internal methods to make things easier
        String setNewVal(String property, int pane_size, int div_pos){
            if(div_pos>pane_size) return "div>pane "+div_pos+">"+pane_size;

            float tol=.01f;
            float pane_sizeF=(float)pane_size;
            float div_posF=(float)div_pos;
            float prop=SharedData.getfloatProperty(property);
            float newPercent=div_posF/pane_sizeF;
            float oldPercent=prop;

            if(prop>1f){
                tol=.02f;
                oldPercent=prop/pane_sizeF;
            }

            if(oldPercent>1f) return "bad oldPercent "+oldPercent;
            if(newPercent>1f) return "bad newPercent "+newPercent;

            if(Math.abs(oldPercent-newPercent)>tol){
                if(property.equals("Status_Height")){
                    div_posF=pane_sizeF-div_posF;
                    newPercent=1f-newPercent;
                }
                if(tol>.01f){
                    prop=div_posF;
                }else{
                    prop=newPercent;
                    prop=Float.parseFloat(Format.real((double)prop,3,2));
                }
                System.setProperty(property,(Float.toString(prop)));
                return Float.toString(prop);
            }else{
                return "";
            }

        }

        String getDimension(String param){
            int index=param.indexOf(",");
            if(index>0)
                param=param.substring(index+1,param.length()-1);
            index=param.indexOf(" ");
            if(index>0)
                param=param.substring(index+1,param.length());

            return param;
        }
        int getWidth(String param){
            int index=param.indexOf("x");
            if(index>0)
                return Integer.parseInt(param.substring(0,index));
            else
                return -1;

        }
        int getHeight(String param){
            int index=param.indexOf("x");
            if(index>0){
                index++;
                return Integer.parseInt(param.substring(index,param.length()));
            }else
                return -1;

        }
    }
}

