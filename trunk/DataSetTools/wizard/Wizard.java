/*
 * File:  Wizard.java
 *
 * Copyright (C) 2002, Dennis Mikkelson, 2003 Chris Bouzek
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA and by
 * the National Science Foundation under grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.47  2003/06/30 17:11:40  bouzekc
 * Moved enabling/disabling of navigation buttons into a
 * private method.  This allows the inner SwingWorker class to
 * correctly enable/disable the buttons.
 *
 * Revision 1.46  2003/06/30 15:16:23  bouzekc
 * Added executeNoGUI() method to allow execution of the
 * Wizard without creating the GUI.
 *
 * Revision 1.45  2003/06/30 14:26:10  bouzekc
 * Now uses the LookAndFeelManager class to set the Pluggable
 * Look and Feel (PLAF) of the Wizard.  Sets the PLAF to Motif
 * if running on a Linux machine due to space problems with
 * low resolution screens.
 *
 * Revision 1.44  2003/06/27 22:05:47  bouzekc
 * Fixed log spelling error.  Added missing javadocs.
 *
 * Revision 1.43  2003/06/27 21:32:50  bouzekc
 * Now uses Form's addPropertyChangeListener() method to add
 * itself as a listener on parameters.  Reformatted javadocs
 * at top of class so the Form linking tutorial shows up
 * correctly.
 *
 * Revision 1.42  2003/06/27 18:40:34  bouzekc
 * Fixed bug where changing parameters in a Form did not
 * always reset the Wizard progress bar.  Modifed boolean
 * check in the propertyChange method.
 *
 * Revision 1.41  2003/06/26 22:19:42  bouzekc
 * Changed Wizard height to 75% of full screen to
 * accomodate taller Forms.
 *
 * Revision 1.40  2003/06/26 19:35:40  bouzekc
 * Shrunk Wizard opening size to 75% full screen width and
 * 60% full screen height.
 *
 * Revision 1.39  2003/06/26 16:58:52  bouzekc
 * Viewing of generic file result parameters, such as those
 * returned from OperatorForms, now enabled.
 *
 * Revision 1.38  2003/06/26 16:24:34  bouzekc
 * Removed unused FRAME_WIDTH and FRAME_HEIGHT instance
 * variables.
 *
 * Revision 1.37  2003/06/26 16:22:24  bouzekc
 * Reformatted for consistency.
 *
 * Revision 1.36  2003/06/26 16:20:12  bouzekc
 * Changed button names to be more expressive.  Now starts
 * full screen.
 *
 * Revision 1.35  2003/06/24 22:33:54  bouzekc
 * Removed unused variables.  Removed unused closeFile
 * method.
 *
 * Revision 1.34  2003/06/20 16:28:18  bouzekc
 * Now allows ignoring of parameter property changes, which is
 * needed for loading saved files.  Added methods to set and
 * get the ignore property change value.  Set the debug print
 * statements within a DEBUG block.
 *
 * Revision 1.33  2003/06/19 16:19:02  bouzekc
 * Added javadoc tutorial on how to create the parameter table.
 * Modified convertXMLToParameters to more accurately deal with
 * parameter validation.  Added a method to automatically link the
 * Wizard's Forms' parameters using a given parameter table.
 *
 * Revision 1.32  2003/06/18 22:47:43  bouzekc
 * Fixed potential bug with validating File parameters from
 * a loaded .wsf file.
 *
 * Revision 1.31  2003/06/17 20:28:33  bouzekc
 * Added method to get last valid Form.  Fixed progress bar
 * updating code to more accurately reflect the status of the
 * Wizard and the Forms.
 *
 * Revision 1.30  2003/06/17 16:43:49  bouzekc
 * Added dual progress bars, one for the overall progress and
 * one for the current Form progress.
 *
 * Revision 1.29  2003/06/16 23:06:26  bouzekc
 * Added internal class subclassed from SwingWorker to enable
 * multithreading of the Wizard.
 *
 * Revision 1.28  2003/06/13 22:07:04  bouzekc
 * Now uses the appendExtension() in RobustFileFilter.
 *
 * Revision 1.27  2003/06/12 22:01:42  bouzekc
 * Fixed bug where it tried to set parameter values to
 * "emptyString"
 *
 * Revision 1.26  2003/06/12 21:20:17  bouzekc
 * Fixed bug where save() would crash if a null value was
 * used.  Updated convertXMLtoParameters to be smarter -
 * it now knows about tags and can set validity of
 * parameters based on several types and factors.
 *
 * Revision 1.25  2003/06/10 21:53:43  bouzekc
 * Modified populateViewMenu to show LoadFilePGs and
 * SaveFilePGs rather than BrowseFilePGs (no sense showing
 * a directory).
 *
 * Revision 1.24  2003/06/10 14:20:25  bouzekc
 * Fixed bug where the View menu was not updated when the
 * First or Last button was clicked.
 *
 * Revision 1.23  2003/06/09 14:51:23  bouzekc
 * Added code to handle ErrorStrings as well as a Boolean
 * "false" for Form invalidation.
 *
 * Revision 1.22  2003/06/05 22:17:52  bouzekc
 * Incremental improvement to more carefully define what
 * should be listed in the Wizard's View menu.
 *
 * Revision 1.21  2003/06/04 14:13:21  bouzekc
 * Improved the parameter checking for the <View> menu.
 *
 * Revision 1.20  2003/06/02 21:57:35  bouzekc
 *
 * Revision 1.19  2003/05/08 15:10:30  pfpeterson
 * Added a FileFilter to the save and load dialogs. (Chris Bouzek)
 *
 * Revision 1.18  2003/04/29 14:08:37  pfpeterson
 * Generate help page for Form from HTMLizer. (Chris Bouzek)
 *
 * Revision 1.17  2003/04/28 16:17:58  pfpeterson
 * Now recalls save/load filename. (Chris Bouzek)
 *
 * Revision 1.16  2003/04/24 18:56:06  pfpeterson
 * Added functionality to save Wizards. (Chris Bouzek)
 *
 * Revision 1.15  2003/04/02 14:56:51  pfpeterson
 * Changed to work with new Forms. (Chris Bouzek)
 *
 * Revision 1.14  2003/03/19 15:03:24  pfpeterson
 * Better implementation of the view menu. It is dynamically created
 * to contain only valid parameters. (Chris Bouzek)
 *
 * Revision 1.13  2003/03/13 15:31:20  pfpeterson
 * The next generation of parameter viewing. This allows any parameter
 * to be viewed. (Chris Bouzek).
 *
 * Revision 1.12  2003/03/11 19:51:20  pfpeterson
 * First implementation of the 'View DataSet' button (Chris Bouzek).
 *
 * Revision 1.11  2003/03/06 15:50:18  pfpeterson
 * Changed to work with SharedData's private StatusPane.
 *
 * Revision 1.10  2003/02/27 18:01:46  pfpeterson
 * Fixed bug when values are changed on forms, enlarged the status_pane,
 * and added some more debug statements.
 *
 * Revision 1.9  2003/02/26 21:43:47  pfpeterson
 * Changed reference to Form.setCompleted(false) to From.invalidate().
 *
 * Revision 1.8  2003/02/26 17:20:43  rmikk
 * Now uses DataSetTools.util.SharedData.status_pane
 *
 * Revision 1.7  2003/02/24 20:47:29  pfpeterson
 * Added a debug flag and made exec_forms smarter. Now properly invalidates
 * and sets progress bar on failure.
 *
 * Revision 1.6  2003/02/11 15:09:38  dennis
 * Bugfix...CommandHandler constructor can't be private.
 * (Chris Bouzek)
 *
 * Revision 1.5  2002/11/27 23:26:33  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/06/12 13:36:54  pfpeterson
 * invalidate method now sets the value of the progress bar.
 *
 * Revision 1.3  2002/06/11 14:56:18  pfpeterson
 * Small updates to documentation.
 *
 * Revision 1.2  2002/06/06 16:15:45  pfpeterson
 * Now use new parameters.
 *
 * Revision 1.1  2002/05/28 20:36:00  pfpeterson
 * Moved files
 *
 * Revision 1.4  2002/04/12 20:53:06  pfpeterson
 * More updates to the GUI.
 *
 * Revision 1.3  2002/04/11 22:35:32  pfpeterson
 * Big changes including:
 *   - new GUI (layout works better)
 *   - execute button runs previous forms if not already done.
 *   - invalidates forms after the one being executed.
 *
 * Revision 1.2  2002/03/12 16:09:45  pfpeterson
 * Now automatically disable constant and result parameters.
 *
 * Revision 1.1  2002/02/27 17:27:52  dennis
 * Wizard class for controlling a sequence of "Forms" that
 * determine a calculation
 */
package DataSetTools.wizard;

import DataSetTools.components.ParametersGUI.PropChangeProgressBar;

import DataSetTools.dataset.DataSet;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import DataSetTools.wizard.util.*;

import ExtTools.SwingWorker;

import IsawHelp.HelpSystem.HTMLizer;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;


/**
 *  The Wizard class provides the top level control for a sequence of
 *  operations to be carried out with user interaction.  The
 *  quantities the the user interacts with are stored in a master list
 *  of all quantities used in the Wizard.  The quantites are stored as
 *  IParameterGUI objects.  The operations that are to be carried out
 *  are described by Form objects.  The Wizard also controls a
 *  sequence of forms and allows the user to step back and forth
 *  between forms. Each form defines an execute() method to carry out
 *  the action determined by the form. The Wizard also manages a
 *  simple help system. Help messages should be set for each form
 *  that is used in a particular Wizard application, as well as for
 *  the Wizard itself.
 *
 *  <br><br>
 *  <u>HOW TO CREATE THE PARAMETER TABLE</u><br><br>
 *  By Chris Bouzek<br><br>
 *
 *  The parameter table is used to coordinate the Forms for this
 *  Wizard.<br><br>
 *
 *  The referential links are arranged in a tabular format.<br><br>
 *  Suppose that we have a Wizard with three Forms:<br><br>
 *
 *  LoadMultiHistogramsForm, named lmhf<br>
 *  TimeFocusGroupForm, named tfgf<br>
 *  SaveAsGSASForm, named sagf<br><br>
 *
 *  Suppose that the parameters on these forms are linked as follows:<br>
 *  lmhf parameter 5 = tfgf parameter 0<br>
 *  tfgf parameter 61 = sagf parameter 0<br>
 *  lmhf parameter 6 = sagf parameter 1<br>
 *  lmhf parameter 0 = sagf parameter 2<br>
 *  lmhf parameter 2 = sagf parameter 3<br><br>
 *
 *  Now, if we consider a table with the rows set as the Form indices, the
 *  columns set as the parameter number indices, and the cells of the table as
 *  the actual parameter numbers to link, it would look like this:<br><br>
 *
 *  <center>
 *
 *    lmhf    tfgf    sagf<br>
 *  |----------------------|<br>
 *  |   5   |   0   | -1   |<br>
 *  |----------------------|<br>
 *  |  -1   |  61   |  0   |<br>
 *  |----------------------|<br>
 *  |   6   |  -1   |  1   |<br>
 *  |----------------------|<br>
 *  |   0   |  -1   |  2   |<br>
 *  |----------------------|<br>
 *  |   2   |  -1   |  3   |<br>
 *  |----------------------|<br><br></center>
 *
 *  The indices are accessed in the following manner: [rowIndex][columnIndex]:
 *  You must place the actual parameter number within the integer array.
 *  For example, to set the link between LoadMultiHistogramForm's 5th
 *  parameter into TimeFocusGroupForm's 0th parameter, use the following:
 *  (assuming fpi has already been declared as a two-dimensional integer
 *  array of sufficient size):<br><br>
 *
 *  fpi[0][0] = 5;<br>
 *  fpi[0][1] = 0;<br><br>
 *
 *  Alternately, you may create the entire table  using Java's array
 *  initialization scheme, as shown:<br><br>
 *
 *   int fpi[][] = { {5,0,-1}, {-1,61,0}, {6,-1,1},{0,-1,2},{2,-1,3} };<br><br>
 *
 *   DON'T put a row of -1's in the parameter table.  There is no point to it,
 *   since you are supposed to be linking parameters, so linking no parameters
 *   at all makes no sense.<br><br>
 *
 *  @see Form
 */
public abstract class Wizard implements PropertyChangeListener {
  // size of the window
  private static final int FORM_PROGRESS = 100;
  private static final int STRUT_HEIGHT  = 5;

  // string constants for the menus and buttons
  private static final String EXIT_COMMAND        = "Exit";
  private static final String FIRST_COMMAND       = "Go to First Form";
  private static final String BACK_COMMAND        = "Go Back One Form";
  private static final String NEXT_COMMAND        = "Go to Next Form";
  private static final String LAST_COMMAND        = "Go to Last Form";
  private static final String CLEAR_COMMAND       = "Reset All Forms";
  private static final String EXEC_ALL_COMMAND    = "Execute All Forms";
  private static final String EXEC_COMMAND        = "Execute Current Form";
  private static final String HELP_ABOUT_COMMAND  = "About";
  private static final String WIZARD_HELP_COMMAND = "on Wizard";
  private static final String FORM_HELP_COMMAND   = "on Current Form";
  private static final String SAVE_WIZARD_COMMAND = "Save Wizard Data";
  private static final String LOAD_WIZARD_COMMAND = "Load Wizard Data";
  private static final String VIEW_MENU           = "View Parameters";

  // debugging
  private static boolean DEBUG = false;

  // default help and about messages
  private String help_message  = "Help not available for Wizard";
  private String about_message = "Default Help About Message";
  private boolean standalone   = true;

  // instance variables
  private JFrame frame;
  private String title;
  protected Vector forms;
  private int form_num;
  private JPanel form_panel;
  private JButton exec_all_button;
  private JButton first_button;
  private JButton back_button;
  private JButton next_button;
  private JButton last_button;
  private JButton exec_button;
  private JButton clear_button;
  private JLabel form_label;
  private PropChangeProgressBar formProgress;
  private JProgressBar wizProgress;
  private JMenu view_menu;
  private CommandHandler command_handler;
  private boolean modified;
  private JFrame save_frame;
  private JFileChooser fileChooser;
  private File save_file;
  private JComponent[] wizComponents;
  private boolean ignorePropChanges;

  /**
   * The legacy constructor
   *
   * @param title Name displayed at top of window.
   */
  public Wizard( String title ) {
    this( title, true );
  }

  /**
   * The full constructor
   *
   * @param title Name displayed at top of window
   * @param standalone If is running by itself
   */
  public Wizard( String title, boolean standalone ) {
    modified          = false;
    this.title        = title;
    forms             = new Vector(  );
    wizComponents     = new JComponent[7];  //number of components to disable
    form_num          = -1;
    frame             = new JFrame( title );
    form_panel        = new JPanel(  );
    form_label        = new JLabel( " ", SwingConstants.CENTER );
    formProgress      = new PropChangeProgressBar(  );
    wizProgress       = new JProgressBar(  );
    command_handler   = new CommandHandler( this );
    save_frame        = new JFrame( "Save Form as..." );
  }

  /**
   *  Opens a file for input or output.
   *
   *  @param saving  A boolean indicating whether you want to open the
   *                 file for saving (true) or loading (false)
   */
  private File getFile( boolean saving ) {
    int result;
    WizardFileFilter wizFilter = new WizardFileFilter(  );
    String save_file_abs_path;

    if( fileChooser == null ) {
      fileChooser = new JFileChooser(  );
      fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
      fileChooser.setFileFilter( wizFilter );
    }

    //try to remember the previous value the user entered
    if( ( save_file != null ) && !save_file.toString(  ).equals( "" ) ) {
      fileChooser.setSelectedFile( save_file );
    }

    if( saving ) {
      result = fileChooser.showSaveDialog( save_frame );
    } else {
      result = fileChooser.showOpenDialog( save_frame );
    }

    if( result == JFileChooser.CANCEL_OPTION ) {
      return null;
    }

    save_file = fileChooser.getSelectedFile(  );

    if( saving ) {
      save_file_abs_path   = save_file.toString(  );

      //make sure the extension is on there
      save_file_abs_path   = wizFilter.appendExtension( save_file_abs_path );
      save_file            = new File( save_file_abs_path );
    }

    if( saving && save_file.exists(  ) ) {
      String temp;
      StringBuffer s = new StringBuffer(  );

      s.append( "You are about to overwrite " );
      s.append( save_file.toString(  ) );
      s.append( ".\n  If this is OK, press " );
      s.append( "<Enter> or click the <OK> button.\n  Otherwise, please " );
      s.append( "enter a new name or click <Cancel>." );
      temp = JOptionPane.showInputDialog( s.toString(  ) );

      if( ( temp != null ) && !temp.equals( "" ) ) {
        temp        = wizFilter.appendExtension( temp );
        save_file   = new File( 
            fileChooser.getCurrentDirectory(  ) + "/" + temp );
      }
    }

    if( ( save_file == null ) || save_file.getName(  ).equals( "" ) ) {
      JOptionPane.showMessageDialog( 
        save_frame, "Please enter a valid file name", "ERROR",
        JOptionPane.ERROR_MESSAGE );

      return null;
    } else {
      return save_file;
    }
  }

  /**
   *
   *  Write the Forms to a file, using the conc_forms Vector.
   *  The only things actually written are the Form's
   *  IParameterGUI types, name, and value in XML format along with
   *  XML tags for the Form index.
   *
   *  @param conc_forms The Vector of Forms to write to a file.
   *  @param file the File to write to.
   */
  private void writeForms( Vector conc_forms, File file ) {
    StringBuffer s = new StringBuffer(  );
    Form f;

    //String temp;
    Object obj;
    IParameterGUI ipg;
    FileWriter fw  = null;

    try {
      fw = new FileWriter( file );

      for( int i = 0; i < conc_forms.size(  ); i++ ) {
        s.append( "<Form number=" );
        s.append( i );
        s.append( ">\n" );

        f = ( Form )conc_forms.elementAt( i );

        for( int j = 0; j < f.getNum_parameters(  ); j++ ) {
          ipg = ( IParameterGUI )f.getParameter( j );
          s.append( "<" );
          s.append( ipg.getType(  ) );
          s.append( ">\n" );
          s.append( "<Name>" );
          s.append( ipg.getName(  ) );
          s.append( "</Name>\n" );
          s.append( "<Value>" );

          if( ipg != null ) {  //parameter is not null
            obj = ipg.getValue(  );
          } else {  //parameter is null, so set value to null
            obj = null;
          }

          if( ( obj == null ) || ( obj.toString(  ).length(  ) <= 0 ) ) {
            s.append( "emptyString" );
          } else {
            s.append( obj.toString(  ) );
            s.append( "" );
          }

          s.append( "</Value>\n" );
          s.append( "<Valid>" );
          s.append( ipg.getValid(  ) );
          s.append( "</Valid>\n" );
          s.append( "</" );
          s.append( ipg.getType(  ) );
          s.append( ">\n" );
        }

        s.append( "</Form>\n" );
      }

      fw.write( s.toString(  ) );
    } catch( IOException e ) {
      e.printStackTrace(  );
      JOptionPane.showMessageDialog( 
        save_frame, "Error saving file.  Please rerun the wizard and try again.",
        "ERROR", JOptionPane.ERROR_MESSAGE );
    } finally {
      if( fw != null ) {
        try {
          fw.close(  );
          modified = false;
        } catch( IOException e ) {
          //let it drop on the floor
        }
      }
    }
  }

  /**
   *  Loads Forms from a file.  It actually just loads the saved
   *  IParameterGUI values into the Wizard's Forms' parameters.
   */
  private void loadForms( File file ) {
    char ca;
    StringBuffer s = new StringBuffer(  );
    int good       = -1;
    FileReader fr  = null;

    try {
      fr   = new FileReader( file );

      good = fr.read(  );

      while( good >= 0 ) {
        ca = ( char )good;
        s.append( ca );
        good = fr.read(  );
      }

      convertXMLtoParameters( s );
    } catch( IOException e ) {
      e.printStackTrace(  );
      JOptionPane.showMessageDialog( 
        save_frame, "Error loading file.  Does the file match the Wizard?",
        "ERROR", JOptionPane.ERROR_MESSAGE );
    } finally {
      if( fr != null ) {
        try {
          fr.close(  );
          modified = false;
        } catch( IOException e ) {
          //let it drop on the floor
        }
      }
    }
  }

  /**
   *  Converts a StringBuffer which holds an XML String of
   *  IParameterGUI and Form information into data that
   *  the Wizard can understand.
   *
   *  @param s The StringBuffer that holds the parameter
   *           information.
   */
  private void convertXMLtoParameters( StringBuffer s )
    throws IOException {
    final String NAMESTART  = "<Name>";
    final String NAMEEND    = "</Name>";
    final String VALUESTART = "<Value>";
    final String VALUEEND   = "</Value>";
    final String VALIDSTART = "<Valid>";
    final String VALIDEND   = "</Valid>";

    String xml             = s.toString(  );
    String paramName;
    String paramValue;
    String paramValidity;
    String typeEnd;
    StringBuffer temp      = new StringBuffer(  );
    StringTokenizer st;
    int nameStartInd;
    int nameEndInd;
    int valueStartInd;
    int valueEndInd;
    int validStartInd;
    int validEndInd;
    int typeEndInd;
    Form cur_form;
    IParameterGUI curParam;
    boolean ignoreChanges  = false;

    //remove the newline characters
    st = new StringTokenizer( xml, "\n" );

    while( st.hasMoreTokens(  ) ) {
      temp.append( st.nextToken(  ) );
    }

    xml = temp.toString(  );

    //start going through the Forms
    for( int i = 0; i < forms.size(  ); i++ ) {
      cur_form = this.getForm( i );

      //start the parameter parsing for the Form
      for( int j = 0; j < cur_form.getNum_parameters(  ); j++ ) {
        //get the parameter
        curParam   = ( IParameterGUI )( cur_form.getParameter( j ) );

        //get the parameter name from the file
        nameStartInd   = xml.indexOf( NAMESTART );
        nameEndInd     = xml.indexOf( NAMEEND );
        paramName      = xml.substring( 
            nameStartInd + NAMESTART.length(  ), nameEndInd );

        //compare it to the Form parameter name
        if( !( curParam.getName(  ).equals( paramName ) ) ) {
          throw new IOException( 
            "Parameter " + paramName + " does not match Form parameter " +
            curParam.getName(  ) );
        }

        //get the parameter value from the file
        valueStartInd   = xml.indexOf( VALUESTART );
        valueEndInd     = xml.indexOf( VALUEEND );
        paramValue      = xml.substring( 
            valueStartInd + VALUESTART.length(  ), valueEndInd );

        if( paramValue.equals( "emptyString" ) ) {
          curParam.setValue( "" );
        } else {
          curParam.setValue( paramValue );
        }

        //set the parameter validity.  Read from the file, then check a few
        //types against certain criteria.
        //turn off property change checking for the parameter.
        ignoreChanges = ( ( ParameterGUI )curParam ).getIgnorePropertyChange(  );
        ( ( ParameterGUI )curParam ).setIgnorePropertyChange( true );

        validStartInd   = xml.indexOf( VALIDSTART );
        validEndInd     = xml.indexOf( VALIDEND );
        paramValidity   = xml.substring( 
            validStartInd + VALIDSTART.length(  ), validEndInd );
        curParam.setValid( new Boolean( paramValidity ).booleanValue(  ) );

        if( curParam instanceof DataSetPG ) {
          curParam.setValid( false );  //DataSets not valid
        } else if( ( curParam instanceof BrowsePG ) ) {
          if( !( new File( curParam.getValue(  ).toString(  ) ).exists(  ) ) ) {
            curParam.setValid( false );
          }
        } else if( curParam instanceof ArrayPG || curParam instanceof VectorPG ) {
          Vector v = ( Vector )( curParam.getValue(  ) );

          if( ( v == null ) || v.isEmpty(  ) ) {
            curParam.setValid( false );
          } else {  //Test the assumption

            for( int k = 0; k < v.size(  ); k++ ) {
              if( !( new File( v.elementAt( k ).toString(  ) ).exists(  ) ) ) {
                curParam.setValid( false );

                break;
              }
            }
          }
        }

        //find the index of the ending for the parameter, e.g. </DataDir>
        typeEnd      = "</" + ( ( IParameterGUI )curParam ).getType(  ) + ">";
        typeEndInd   = xml.indexOf( typeEnd );
        xml          = xml.substring( 
            typeEndInd + typeEnd.length(  ), xml.length(  ) );

        //set property change checking for the parameter back to the original.
        ( ( ParameterGUI )curParam ).setIgnorePropertyChange( ignoreChanges );
      }

      //end the Parameter parsing for the Form
    }
  }

  /**
   *  Load the state of the wizard from a file
   */
  public boolean load(  ) {
    File f;

    f = getFile( false );

    if( f == null ) {
      return false;
    }

    //set the property change checking for the Wizard to false, otherwise it
    //messes up our loading of valid result parameters
    this.setIgnorePropertyChanges( true );

    loadForms( f );
    showForm( 0 );

    //now we want to return to a state where the Wizard can listen to
    //property changes
    this.setIgnorePropertyChanges( false );

    return true;
  }

  /**
   *  Used to set the ignorePropChanges variable.  Useful because
   *  many things trigger property change events, with possible
   *  undesired effects.
   */
  public void setIgnorePropertyChanges( boolean ignore ) {
    ignorePropChanges = ignore;
  }

  /**
   *  Accessor method to get ignorePropChanges variable.
   */
  public boolean getIgnorePropertyChanges(  ) {
    return ignorePropChanges;
  }

  /**
   *  Save the state of the wizard to a file
   */
  public void save(  ) {
    if( forms == null ) {
      return;
    }

    File file;

    if( modified ) {
      file = getFile( true );

      if( file == null ) {
        return;
      }

      writeForms( forms, file );
    }
  }

  /**
   *  Save the state of the wizard then exit the wizard application.
   */
  public void close(  ) {
    int save_me = 1;

    if( modified ) {
      save_me = JOptionPane.showConfirmDialog( 
          null, "Would you like to save your changes?",
          "Would you like to save your changes?", JOptionPane.YES_NO_OPTION );

      if( save_me == 0 ) {
        save(  );
      }
    }

    System.exit( 0 );
  }

  /**
   *  Accessor method to get the number of Forms.
   *
   *  @return     Number of Forms in this Wizard.
   */
  public int getNumForms(  ) {
    return forms.size(  );
  }

  /**
   *  Makes the GUI for this Wizard.
   */
  protected void makeGUI(  ) {
    LookAndFeelManager.setLookAndFeel(  );

    GridBagConstraints gbc = new GridBagConstraints(  );

    gbc.fill        = GridBagConstraints.HORIZONTAL;
    gbc.weightx     = 1.0;
    gbc.anchor      = GridBagConstraints.WEST;
    gbc.gridwidth   = GridBagConstraints.REMAINDER;

    JPanel work_area = new JPanel( new GridBagLayout(  ) );
    JPanel button_panel = new JPanel( new GridBagLayout(  ) );
    JPanel progress_panel = new JPanel( new GridBagLayout(  ) );
    JMenuBar menu_bar = new JMenuBar(  );
    JMenu file_menu = new JMenu( "File" );
    JMenu help_menu = new JMenu( "Help" );

    view_menu       = new JMenu( VIEW_MENU );

    frame.setJMenuBar( menu_bar );
    frame.addWindowListener( new CloseWizardWindow(  ) );
    frame.getContentPane(  ).add( work_area );

    {
      int screenheight = ( int )( Toolkit.getDefaultToolkit(  ).getScreenSize(  )
                                         .getHeight(  ) * 0.75f );
      int screenwidth = ( int )( Toolkit.getDefaultToolkit(  ).getScreenSize(  )
                                        .getWidth(  ) * 0.75f );

      frame.setBounds( 0, 0, screenwidth, screenheight );
    }

    JMenuItem help_about  = new JMenuItem( HELP_ABOUT_COMMAND );
    JMenuItem wizard_help = new JMenuItem( WIZARD_HELP_COMMAND );
    JMenuItem form_help   = new JMenuItem( FORM_HELP_COMMAND );

    help_menu.add( help_about );
    help_menu.addSeparator(  );
    help_menu.add( wizard_help );
    help_menu.add( form_help );

    JScrollPane form_scrollpane = new JScrollPane( 
        form_panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

    JMenuItem save_wizard = new JMenuItem( SAVE_WIZARD_COMMAND );
    JMenuItem load_wizard = new JMenuItem( LOAD_WIZARD_COMMAND );
    JMenuItem exit_item   = new JMenuItem( EXIT_COMMAND );

    save_wizard.setEnabled( true );
    load_wizard.setEnabled( true );
    file_menu.addSeparator(  );
    file_menu.add( save_wizard );
    file_menu.add( load_wizard );
    file_menu.addSeparator(  );
    file_menu.add( exit_item );

    menu_bar.add( file_menu );
    menu_bar.add( view_menu );
    menu_bar.add( help_menu );

    // add the title to the panel
    work_area.add( form_label, gbc );

    // add the form to the panel
    gbc.weighty   = 50.0;
    gbc.fill      = GridBagConstraints.BOTH;
    form_panel.setLayout( new GridLayout( 1, 1 ) );
    work_area.add( form_scrollpane, gbc );

    // add the progress bars to the panel
    clear_button      = new JButton( CLEAR_COMMAND );
    exec_all_button   = new JButton( EXEC_ALL_COMMAND );

    if( forms.size(  ) > 1 ) {
      gbc.weighty   = 1.0;
      gbc.fill      = GridBagConstraints.HORIZONTAL;
      work_area.add( progress_panel, gbc );
      formProgress.setString( "Form Progress" );
      wizProgress.setString( 
        "Wizard Progress: " + ( getCurrentFormNumber(  ) + 1 ) + " of " +
        forms.size(  ) + " Forms done" );
      formProgress.setStringPainted( true );
      wizProgress.setStringPainted( true );

      wizProgress.setMaximum( forms.size(  ) );
      wizProgress.setValue( 0 );
      formProgress.setValue( 0 );
      gbc.fill        = GridBagConstraints.NONE;
      gbc.gridwidth   = 1;
      progress_panel.add( clear_button, gbc );
      gbc.weightx     = 20.0;
      gbc.fill        = GridBagConstraints.HORIZONTAL;
      gbc.gridwidth   = GridBagConstraints.RELATIVE;

      Box tempBox     = Box.createVerticalBox(  );

      tempBox.add( formProgress );
      tempBox.add( Box.createVerticalStrut( STRUT_HEIGHT ) );
      tempBox.add( wizProgress );
      progress_panel.add( tempBox, gbc );

      gbc.weightx     = 1.0;
      gbc.fill        = GridBagConstraints.NONE;
      gbc.gridwidth   = GridBagConstraints.REMAINDER;
      gbc.anchor      = GridBagConstraints.EAST;
      progress_panel.add( exec_all_button, gbc );
    }

    // add the navigation buttons to the panel
    gbc.weightx     = 1.0;
    gbc.anchor      = GridBagConstraints.WEST;
    gbc.gridwidth   = GridBagConstraints.REMAINDER;
    gbc.fill        = GridBagConstraints.HORIZONTAL;
    work_area.add( button_panel, gbc );
    first_button   = new JButton( FIRST_COMMAND );
    back_button    = new JButton( BACK_COMMAND );
    next_button    = new JButton( NEXT_COMMAND );
    last_button    = new JButton( LAST_COMMAND );
    exec_button    = new JButton( EXEC_COMMAND );

    back_button.setEnabled( false );
    next_button.setEnabled( false );

    if( forms.size(  ) > 1 ) {
      gbc.gridwidth   = 1;
      gbc.fill        = GridBagConstraints.NONE;

      if( forms.size(  ) > 2 ) {
        button_panel.add( first_button, gbc );
      }

      button_panel.add( back_button, gbc );
      gbc.weightx   = 20;
      gbc.anchor    = GridBagConstraints.CENTER;
      gbc.fill      = GridBagConstraints.HORIZONTAL;
      button_panel.add( exec_button, gbc );
      gbc.weightx   = 1;
      gbc.anchor    = GridBagConstraints.EAST;
      gbc.fill      = GridBagConstraints.NONE;

      if( forms.size(  ) > 2 ) {
        button_panel.add( next_button, gbc );
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        button_panel.add( last_button, gbc );
      } else {
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        button_panel.add( next_button, gbc );
      }
    } else {
      gbc.anchor      = GridBagConstraints.CENTER;
      gbc.gridwidth   = GridBagConstraints.REMAINDER;
      button_panel.add( exec_button, gbc );
    }

    // add the status to the panel
    gbc.fill      = GridBagConstraints.BOTH;
    gbc.anchor    = GridBagConstraints.WEST;
    gbc.weighty   = 5.0;

    if( standalone ) {
      work_area.add( SharedData.getStatusPane(  ), gbc );
    }

    save_wizard.addActionListener( command_handler );
    load_wizard.addActionListener( command_handler );
    first_button.addActionListener( command_handler );
    back_button.addActionListener( command_handler );
    next_button.addActionListener( command_handler );
    last_button.addActionListener( command_handler );
    clear_button.addActionListener( command_handler );
    exec_all_button.addActionListener( command_handler );
    exec_button.addActionListener( command_handler );
    help_about.addActionListener( command_handler );
    wizard_help.addActionListener( command_handler );
    form_help.addActionListener( command_handler );
    exit_item.addActionListener( command_handler );
    view_menu.addActionListener( command_handler );

    //add the components to the JComponents array so we can easily
    //enable/disable them
    wizComponents[0]   = exec_all_button;
    wizComponents[1]   = first_button;
    wizComponents[2]   = back_button;
    wizComponents[3]   = next_button;
    wizComponents[4]   = last_button;
    wizComponents[5]   = exec_button;
    wizComponents[6]   = clear_button;
  }

  /**
   *  Shows the GUI for this Wizard by calling the outer Frame's
   *  show() method.
   */
  protected void showGUI(  ) {
    frame.show(  );
  }

  /**
   *  Add another form to the list of forms maintained by this
   *  wizard.
   *
   *  @param f  The form to be added to the list.
   */
  public void addForm( Form f ) {
    forms.add( f );

    //each Form will send out a PropertyChange new value from 0 to 100,
    //so the Wizard needs 100 units for each Form
    wizProgress.setMaximum( forms.size(  ) );
    wizProgress.setString( 
      "Wizard Progress: " + ( getCurrentFormNumber(  ) + 1 ) + " of " +
      forms.size(  ) + " Forms done" );
  }

  /**
   *  Get the form that is currently displayed by the wizard.
   *
   *  @return  The currently displayed form.
   */
  public Form getCurrentForm(  ) {
    return getForm( getCurrentFormNumber(  ) );
  }

  /**
   * Get the number of the form currently being shown.
   */
  public int getCurrentFormNumber(  ) {
    return form_num;
  }

  /**
   * Get the form at specified index.
   *
   * @return The form at the specified index.
   */
  public Form getForm( int index ) {
    if( ( index >= 0 ) && ( index < forms.size(  ) ) ) {
      return ( Form )forms.elementAt( index );
    } else {
      return null;
    }
  }

  /**
   *  Show the form at the specified position in the list of forms.
   *  If the index is invalid, an error message will be displayed in
   *  the status pane.
   *
   *  @param  index  The index of the form to show.
   */
  public void showForm( int index ) {
    if( !frame.isShowing(  ) ) {
      this.makeGUI(  );
      this.showGUI(  );
    }

    if( ( index < 0 ) || ( index > ( forms.size(  ) - 1 ) ) ) {  // invalid index
      DataSetTools.util.SharedData.addmsg( 
        "Error: invalid form number in Wizard.show(" + index + ")\n" );

      return;
    }

    Form f = getCurrentForm(  );  // get rid of any current form

    if( f != null ) {
      f.setVisible( false );
    }

    form_panel.removeAll(  );

    f = getForm( index );  // show the specified form
    form_panel.add( f.getPanel(  ) );
    f.setVisible( true );

    int lastForm = this.getLastValidFormNum(  );

    //reset the progress bars - especially useful when loading up a Wizard
    //from a file
    if( f.done(  ) ) {
      formProgress.setString( f + " Done" );
      wizProgress.setString( 
        "Wizard Progress: " + ( lastForm + 1 ) + " of " + forms.size(  ) +
        " Forms done" );
      formProgress.setValue( FORM_PROGRESS );
      wizProgress.setValue( lastForm + 1 );
    } else {
      formProgress.setString( f + " Progress" );
      formProgress.setValue( 0 );
      wizProgress.setValue( lastForm + 1 );
    }

    //add the listener (this) to the Form's parameters and progress bar
    f.addPropertyChangeListener( this );
    f.addPropertyChangeListener( formProgress );

    form_panel.validate(  );
    form_num = index;

    this.enableNavButtons( true, index );

    if( forms.size(  ) == 1 ) {
      form_label.setText( f.getTitle(  ) );
    } else {
      form_label.setText( "Form " + ( index + 1 ) + ": " + f.getTitle(  ) );
    }
  }

  /**
   *  Utility to enable/disable the Wizard navigation buttons.
   *
   *  @param   enable            true to enable, false to disable.
   *
   *  @param  index              The index of the Form to enable/disable
   *                             navigation buttons on.
   */
  private void enableNavButtons( boolean enable, int index ) {
    if( enable ) {
      exec_button.setEnabled( true );
      exec_all_button.setEnabled( true );
      clear_button.setEnabled( true );

      // enable/disable the navigation buttons
      if( index >= ( forms.size(  ) - 1 ) ) {
        next_button.setEnabled( false );
      } else {
        next_button.setEnabled( true );
      }

      if( index >= ( forms.size(  ) - 2 ) ) {
        last_button.setEnabled( false );
      } else {
        last_button.setEnabled( true );
      }

      if( index <= 0 ) {
        back_button.setEnabled( false );
      } else {
        back_button.setEnabled( true );
      }

      if( index <= 1 ) {
        first_button.setEnabled( false );
      } else {
        first_button.setEnabled( true );
      }
    } else {
      for( int i = 0; i < wizComponents.length; i++ ) {
        wizComponents[i].setEnabled( false );
      }
    }
  }

  /**
   *  Used to get the number of the last valid Form (i.e the Form that has
   *  all of its parameters set to valid).
   *
   *  @return               The index of the last valid Form number.
   */
  public int getLastValidFormNum(  ) {
    for( int i = 0; i < this.getNumForms(  ); i++ ) {
      if( !getForm( i ).done(  ) ) {
        return i - 1;
      }
    }

    return forms.size(  ) - 1;
  }

  /**
   *  Set the help message that will be displayed when the user
   *  requests help with this wizard.
   *
   *  @param help_message  String giving the help message to use for this
   *                       wizard.
   */
  public void setHelpMessage( String help_message ) {
    this.help_message = help_message;
  }

  /**
   *  Get the help message for this wizard.
   *
   *  @return the String giving the help message for this wizard.
   */
  public String getHelpMessage(  ) {
    return help_message;
  }

  /**
   *  Set the message that will be displayed when the user chooses
   *  the help about option.
   *
   *  @param about_message  String giving the message to use for the
   *                        "Help About" option.
   */
  public void setAboutMessage( String about_message ) {
    this.about_message = about_message;
  }

  /**
   *  Get the help about message for this wizard.
   *
   *  @return the String giving the help about message for this wizard.
   */
  public String getAboutMessage(  ) {
    return about_message;
  }

  /**
   *  Show the specified String in the help frame.
   *
   *  @param str   The message to display in a dialog.
   *  @param title The title of the dialog.
   */
  private void ShowHelpMessage( String str, String title ) {
    JOptionPane.showMessageDialog( 
      this.frame, str, title, JOptionPane.INFORMATION_MESSAGE );
  }

  /**
   *  Shows the JavaHelp HTML page for the current form.
   *
   */
  private void ShowFormHelpMessage(  ) {
    HTMLizer form_htmlizer = new HTMLizer(  );
    String html            = form_htmlizer.createHTML( this.getCurrentForm(  ) );
    JFrame help_frame      = new JFrame( title );
    Dimension screen_size  = Toolkit.getDefaultToolkit(  ).getScreenSize(  );

    help_frame.setSize( 
      new Dimension( 
        ( int )( screen_size.getWidth(  ) / 2 ),
        ( int )( screen_size.getHeight(  ) / 2 ) ) );
    help_frame.getContentPane(  ).add( 
      new JScrollPane( new JEditorPane( "text/html", html ) ) );
    help_frame.show(  );
  }

  /**
   * Invalidate all forms starting with the number specified.
   */
  protected void invalidate( int start ) {
    for( int i = start; i < forms.size(  ); i++ ) {
      getForm( i ).invalidate(  );
    }

    //we are farther along then the invalidated Form, so reset the progress
    //bar and label
    if( wizProgress.getValue(  ) > start ) {
      wizProgress.setValue( start );
      wizProgress.setString( 
        "Wizard Progress: " + ( start + 1 ) + " of " + forms.size(  ) +
        " Forms done" );
      formProgress.setValue( 0 );
      formProgress.setString( getForm( start ) + " Progress" );
    }
  }

  /**
   * Execute all forms up to the number specified.
   */
  protected void exec_forms( int end ) {
    modified = true;

    boolean failed = false;
    Form f;

    // execute the previous forms
    for( int i = 0; i <= end; i++ ) {
      f = getForm( i );

      if( !f.done(  ) ) {
        formProgress.setValue( 0 );
        formProgress.setString( "Executing " + f );

        Object worked = f.getResult(  );

        if( 
          ( worked instanceof ErrorString ) ||
            ( worked instanceof Boolean &&
            ( !( ( Boolean )worked ).booleanValue(  ) ) ) ) {
          failed   = true;
          end      = i - 1;  //index to the last "good" Form

          break;
        }
      }

      if( !failed ) {
        wizProgress.setValue( ( i + 1 ) );
        wizProgress.setString( 
          "Wizard Progress: " + ( i + 1 ) + " of " + forms.size(  ) +
          " Forms done" );
        formProgress.setValue( FORM_PROGRESS );
        formProgress.setString( f + " Done" );
      }
    }

    invalidate( end + 1 );
  }

  /**
   *
   * Creates the view menu and listeners for all of the currently
   * validated parameters in the current Form.
   */
  private void populateViewMenu(  ) {
    JMenuItem jmi;
    Form f;
    IParameterGUI iparam;
    Object val;

    f = this.getCurrentForm(  );

    view_menu.removeAll(  );

    for( int i = 0; i < f.getNum_parameters(  ); i++ ) {
      iparam = ( IParameterGUI )f.getParameter( i );

      if( iparam.getValid(  ) ) {
        val = iparam.getValue(  );

        /*semi-sophisticated attempt at being able to view
           DataSets, Vectors of items, and files.  Things like
           Strings and ints, which are easily viewable on the
           Form, should not be sent to the ParameterViewer. */
        if( 
          ( iparam instanceof DataSetPG ) || ( iparam instanceof ArrayPG ) ||
            ( iparam instanceof LoadFilePG ) || ( iparam instanceof SaveFilePG ) ||
            ( iparam instanceof StringPG && val instanceof String &&
            ( ( ( String )val ).indexOf( '.' ) > 0 ) ) ) {
          jmi = new JMenuItem( iparam.getName(  ) );
          view_menu.add( jmi );
          jmi.addActionListener( command_handler );
        }
      }
    }
  }

  /**
   * Method to depopulate part of the view list if the
   * parameters change.  If ignorePropChanges is set
   * true, as it is when the Wizard is loading Forms,
   * the viewMenu is the only thing that changes.
   */
  public void propertyChange( PropertyChangeEvent ev ) {
    if( !ignorePropChanges ) {
      modified = true;
      this.invalidate( this.getCurrentFormNumber(  ) );
    }

    this.populateViewMenu(  );
  }

  /**
   * Method to call a ParameterViewer.  Since the only "oddball" events
   * that currently happen are for the view menu, the only commands to
   * listen for are the ones for the current form.
   */
  private void displayParameterViewer( String com ) {
    Form f;
    IParameterGUI iparam;
    boolean done;
    int index;
    int num_params;

    f            = this.getCurrentForm(  );
    done         = false;
    index        = 0;
    num_params   = f.getNum_parameters(  );

    while( !done && ( index < num_params ) ) {
      iparam   = ( IParameterGUI )f.getParameter( index );

      //does the command match up to a current form parameter name?
      done = com.equals( iparam.getName(  ) );

      if( done ) {
        new ParameterViewer( iparam ).showParameterViewer(  );
      }

      index++;
    }
  }

  /**
   *  Method to handle linking of parameters using the paramIndex table.
   *  Instructions for creating this table are at the top of the Wizard.
   *  Note that the Forms must be added before this method will work.
   *
   *  @param  paramTable                The table of parameters to link
   *                                    together.
   */
  public void linkFormParameters( int[][] paramTable ) {
    int numForms;
    int numParamsToLink;
    int nonNegFormIndex;
    int nonNegParamIndex;

    numForms          = paramTable[0].length;  //the columns in paramTable
    numParamsToLink   = paramTable.length;  //the rows in paramTable
    DEBUG             = false;

    //find the first paramTable[row][col] != 0 parameter index
    for( int rowIndex = 0; rowIndex < numParamsToLink; rowIndex++ ) {
      //find the first paramTable[row][col] >= 0 parameter index.  We have to 
      //do this before going through and linking parameters.  Otherwise, we 
      //might "lose" some.
      nonNegParamIndex = nonNegFormIndex = -1;  //init for each iteration

      for( int formIndex = 0; formIndex < numForms; formIndex++ ) {
        if( paramTable[rowIndex][formIndex] >= 0 ) {
          nonNegParamIndex   = paramTable[rowIndex][formIndex];
          nonNegFormIndex    = formIndex;

          break;
        }
      }

      //The next bit of code handles something which should not happen:
      //a row of negative numbers in the table.
      if( nonNegParamIndex < 0 ) {
        continue;  //kick to the top of the loop
      }

      for( int colIndex = 0; colIndex < numForms; colIndex++ ) {
        if( paramTable[rowIndex][colIndex] >= 0 ) {  //don't try to link a -1

          if( nonNegFormIndex != colIndex ) {
            if( DEBUG ) {
              System.out.print( "Linking " + getForm( nonNegFormIndex ) + ": " );
              System.out.print( nonNegParamIndex + ": " );
              System.out.print( 
                getForm( nonNegFormIndex ).getParameter( nonNegParamIndex )
                  .getName(  ) );
              System.out.print( " with " + getForm( colIndex ) + ": " );
              System.out.print( paramTable[rowIndex][colIndex] + ": " );
              System.out.println( 
                getForm( nonNegFormIndex ).getParameter( nonNegParamIndex )
                  .getName(  ) );
            }

            getForm( colIndex ).setParameter( 
              getForm( nonNegFormIndex ).getParameter( nonNegParamIndex ),
              paramTable[rowIndex][colIndex] );
          }
        }
      }
    }
  }

  /**
   *  Method to execute the Wizard without bringing up the GUI.
   *
   *  @param  saveFile             The Wizard Save File (*.wsf) to use.
   */
  public void executeNoGUI( String saveFile ) {
    this.setIgnorePropertyChanges( true );

    //load the data from the file
    loadForms( new File( saveFile ) );

    this.setIgnorePropertyChanges( false );
    exec_forms( forms.size(  ) - 1 );
  }

  /**
   *  This class closes down the application when the user closes
   *  the frame.
   */
  private class CloseWizardWindow extends WindowAdapter {
    //~ Methods ****************************************************************

    public void windowClosing( WindowEvent event ) {
      close(  );
    }
  }

  /* ---------------- Internal Event Handler Classes --------------------- */

  /**
   *  This class handles all of the commands from buttons and menu
   *  items.
   */
  private class CommandHandler implements ActionListener {
    //~ Instance fields ********************************************************

    private Wizard wizard;
    private WizardWorker worker;

    //~ Constructors ***********************************************************

    public CommandHandler( Wizard wiz ) {
      this.wizard = wiz;
    }

    //~ Methods ****************************************************************

    public void actionPerformed( ActionEvent event ) {
      String command = event.getActionCommand(  );

      if( command.equals( FIRST_COMMAND ) ) {
        form_num = 0;
        showForm( form_num );
        populateViewMenu(  );
      } else if( command.equals( BACK_COMMAND ) ) {
        if( ( form_num - 1 ) >= 0 ) {
          form_num--;
          showForm( form_num );
          populateViewMenu(  );
        } else {
          DataSetTools.util.SharedData.addmsg( 
            "FORM 0 SHOWN, CAN'T STEP BACK\n" );
        }
      } else if( command.equals( NEXT_COMMAND ) ) {
        if( ( form_num + 1 ) < forms.size(  ) ) {
          form_num++;
          showForm( form_num );
          populateViewMenu(  );
        } else {
          DataSetTools.util.SharedData.addmsg( 
            "NO MORE FORMS, CAN'T ADVANCE\n" );
        }
      } else if( command.equals( LAST_COMMAND ) ) {
        form_num = forms.size(  ) - 1;
        showForm( form_num );
        populateViewMenu(  );
      } else if( command.equals( CLEAR_COMMAND ) ) {
        invalidate( 0 );
      } else if( command.equals( EXEC_ALL_COMMAND ) ) {
        worker = new WizardWorker(  );
        worker.setFormNumber( forms.size(  ) - 1 );
        worker.start(  );
      } else if( command.equals( EXEC_COMMAND ) ) {
        //a new SwingWorker needs to be created for each click of the
        //execute button
        worker = new WizardWorker(  );
        worker.setFormNumber( form_num );
        worker.start(  );
      } else if( command.equals( HELP_ABOUT_COMMAND ) ) {
        ShowHelpMessage( about_message, "About: " + wizard.title );
      } else if( command.equals( WIZARD_HELP_COMMAND ) ) {
        ShowHelpMessage( help_message, "Help: " + wizard.title );
      } else if( command.equals( FORM_HELP_COMMAND ) ) {
        Form f = getCurrentForm(  );

        if( f != null ) {
          ShowFormHelpMessage(  );
        }
      } else if( command.equals( SAVE_WIZARD_COMMAND ) ) {
        save(  );
      } else if( command.equals( LOAD_WIZARD_COMMAND ) ) {
        load(  );
      } else if( command.equals( VIEW_MENU ) ) {
        populateViewMenu(  );
      } else if( command.equals( EXIT_COMMAND ) ) {
        close(  );
      } else {
        displayParameterViewer( command );
      }
    }
  }

  /**
   *
   *  This wizard is multithreaded so that the progress bar and Form update
   *  messages can be displayed for the user.  Since we only need two
   *  threads of execution, the choice of a SwingWorker was clear.
   */
  private class WizardWorker extends SwingWorker {
    //~ Instance fields ********************************************************

    private int formNum = 0;

    //~ Methods ****************************************************************

    /**
     *  Required for SwingWorker.
     */
    public Object construct(  ) {
      //can't have users mutating the values!
      enableNavButtons( false, getCurrentFormNumber(  ) );

      this.enableFormParams( false );

      //here is where the time intensive work is.
      exec_forms( formNum );
      populateViewMenu(  );

      enableNavButtons( true, getCurrentFormNumber(  ) );

      this.enableFormParams( true );

      return "Success";  //unused
    }

    /**
     *  Used to set the form number for calling exec_forms.
     */
    public void setFormNumber( int num ) {
      formNum = num;
    }

    /**
     *  Implicit calls to getCurrentForm in order to enable or disable the
     *  parameters.
     */
    private void enableFormParams( boolean enable ) {
      Form f = getCurrentForm(  );

      int[] var_indices = f.getVarParamIndices(  );

      for( int j = 0; j < var_indices.length; j++ ) {
        ( ( IParameterGUI )f.getParameter( var_indices[j] ) ).setEnabled( 
          enable );
      }
    }
  }
}
