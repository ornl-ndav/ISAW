/*
 * File:  SwingWizardFrontEnd.java
 *
 * Copyright (C) 2003 Chris Bouzek
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
 * Revision 1.7  2004/01/09 22:27:15  bouzekc
 * Implements the IGUIWizardFrontEnd.
 *
 * Revision 1.6  2004/01/09 15:35:17  bouzekc
 * Now correctly disposes of the window when it is not standalone and exits the
 * system when it is.
 *
 * Revision 1.5  2004/01/08 15:01:33  bouzekc
 * Added code for arbitrary file (text or runfile) viewing.
 *
 * Revision 1.4  2004/01/06 23:18:28  bouzekc
 * Changed access method for the internal Wizard variable so that it
 * was more direct.
 *
 * Revision 1.3  2003/12/16 00:51:27  bouzekc
 * Fixed bug that prevented Form progress indicator from advancing
 * incrementally.
 *
 * Revision 1.2  2003/12/15 02:44:07  bouzekc
 * Removed unused imports.
 *
 * Revision 1.1  2003/11/29 21:50:58  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.wizard;

import DataSetTools.components.ParametersGUI.PropChangeProgressBar;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import DataSetTools.wizard.util.WizardFileFilter;

import ExtTools.SwingWorker;

import IsawHelp.HelpSystem.HTMLizer;

import java.awt.*;
import java.awt.event.*;

import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.*;


/**
 * This class is designed as the base class for a Wizard GUI front end.   It is
 * a refactoring of GUI code within Wizard.java, v. 1.94 and as such, it has
 * all the Swing niceties, including dual progress bars.  It is meant to have
 * package level access only.
 */
class SwingWizardFrontEnd implements IGUIWizardFrontEnd {
  //~ Static fields/initializers ***********************************************

  public static final String VIEW_DS    = "View DataSet";
  public static final String VIEW_ASCII = "View Text File";

  //~ Instance fields **********************************************************

  private JFrame frame;
  private JPanel form_panel;
  private JLabel form_label;
  private JMenu view_menu;
  private CommandHandler command_handler;
  private JFileChooser fileChooser;
  private AbstractButton[] wizButtons;
  private PropChangeProgressBar formProgress;
  private JProgressBar wizProgress;
  private String help_message = "Help not available for Wizard";
  private Wizard wiz          = null;

  //~ Constructors *************************************************************

  /**
   * Constructor to take a Wizard for building a front end.
   *
   * @param wizard The Wizard to use for the back end.
   */
  public SwingWizardFrontEnd( Wizard wizard ) {
    wiz               = wizard;
    wizButtons        = new AbstractButton[8];  //number of buttons
    frame             = new JFrame( wiz.getTitle(  ) );

    if( wiz.getStandalone(  ) ) {
      frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    } else {
      frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    }
    form_panel        = new JPanel(  );
    form_label        = new JLabel( " ", SwingConstants.CENTER );
    formProgress      = new PropChangeProgressBar(  );
    wizProgress       = new JProgressBar(  );
    command_handler   = new CommandHandler( wiz );

    try {
      UIManager.setLookAndFeel( 
        new com.incors.plaf.kunststoff.KunststoffLookAndFeel(  ) );
    } catch( Exception e ) {}
  }

  //~ Methods ******************************************************************

  /**
   * Gets a file for input or output.
   *
   * @param saving A boolean indicating whether you want to open the file for
   *        saving (true) or loading (false)
   *
   * @return the File that has been retrieved.
   */
  public final File getFile( boolean saving ) {
    int result;
    String save_file_abs_path;
    File save_file;

    if( fileChooser == null ) {
      initFileChooser(  );
    }

    if( saving ) {
      result = fileChooser.showSaveDialog( frame );
    } else {
      result = fileChooser.showOpenDialog( frame );
    }

    if( result == JFileChooser.CANCEL_OPTION ) {
      return null;
    }
    save_file = fileChooser.getSelectedFile(  );

    if( saving ) {
      save_file_abs_path   = save_file.toString(  );

      //make sure the extension is on there
      save_file_abs_path   = new WizardFileFilter(  ).appendExtension( 
          save_file_abs_path );
      save_file            = new File( save_file_abs_path );
    }

    if( saving && save_file.exists(  ) ) {
      int choice;
      StringBuffer s = new StringBuffer(  );
      s.append( "You are about to overwrite " );
      s.append( save_file.toString(  ) );
      s.append( ".\n  If this is OK, press " );
      s.append( "<Enter> or click the <Yes> button.\n  Otherwise, please " );
      s.append( "click <No> to re-select the file." );
      choice = JOptionPane.showConfirmDialog( 
          frame, s.toString(  ), "Overwrite File?", JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE );

      //if this occurred, the user clicked <No>, so we'll do a recursive call
      if( choice == JOptionPane.NO_OPTION ) {
        return this.getFile( saving );
      }
    }

    //somehow we got a bad file
    if( ( save_file == null ) || save_file.getName(  ).equals( "" ) ) {
      JOptionPane.showMessageDialog( 
        frame, "Please enter a valid file name", "ERROR",
        JOptionPane.ERROR_MESSAGE );

      return null;
    } else {
      //successfully retrieved a File
      return save_file;
    }
  }

  /**
   * Utility method to set the Form progress bar value and label.
   *
   * @param value The new value to set.
   * @param label The new label to set
   */
  public final void setFormProgressParameters( int value, String label ) {
    formProgress.setValue( value );
    formProgress.setString( label );
  }

  /**
   * Exits the wizard application.  If the Wizard has been changed and not been
   * saved, this will ask the user if they want to save.
   */
  public final void close(  ) {
    int save_me = JOptionPane.NO_OPTION;

    if( wiz.getModified(  ) ) {
      save_me = JOptionPane.showConfirmDialog( 
          frame, "Would you like to save your changes?",
          "Would you like to save your changes?", JOptionPane.YES_NO_OPTION );

      if( save_me == JOptionPane.YES_OPTION ) {
        wiz.save(  );
      }
    }

    //save the projects directory
    if( wiz.getProjectsDirectory(  ) != null ) {
      TextWriter.writeASCII( Wizard.CONFIG_FILE, wiz.getProjectsDirectory(  ) );
    }

    if( wiz.getStandalone(  ) ) {
      System.exit( 0 );
    } else {
      frame.dispose(  );
    }
  }

  /**
   * Utility to display a JOptionPane with an error message when a file is not
   * loaded successfully.  Also writes the error to loadwizard.err in the
   * error directory (given by getErrorDirectory()).  The naming convention
   * for the file is errFileMonthDay.err.
   *
   * @param e The Throwable generated when the attempt was made to load the WSF
   *        file.
   * @param errFile The error file name to write to, without extension.
   * @param errMess The message to display.
   */
  public final void displayAndSaveErrorMessage( 
    Throwable e, String errFile, StringBuffer errMess ) {
    String errDir         = wiz.getErrorDirectory(  );
    StringBuffer message  = errMess;
    GregorianCalendar cal = new GregorianCalendar(  );
    int month             = cal.get( Calendar.MONTH );
    int day               = cal.get( Calendar.DAY_OF_MONTH );
    String time           = String.valueOf( month ) + "_" +
      String.valueOf( day );
    errFile               = errFile + time + ".err";
    message.append( "\nPlease see the " );
    message.append( errFile );
    message.append( " file in\n" );
    message.append( errDir );
    message.append( " for more information.\n" );
    JOptionPane.showMessageDialog( 
      frame, message.toString(  ), "ERROR", JOptionPane.ERROR_MESSAGE );

    String saveFile = StringUtil.setFileSeparator( errDir + "/" + errFile );
    TextWriter.writeStackTrace( saveFile, e );
  }

  /**
   * Show the form at the specified position in the list of forms. If the index
   * is invalid, an error message will be displayed in the status pane. To
   * avoid strange events that can occur due to all of the
   * PropertyChangeListeners, this sets ignorePropChanges to true upon entry,
   * and to the previous state upon exit.
   *
   * @param index The index of the form to show.
   */
  public final void showForm( int index ) {
    boolean ignore = wiz.getIgnorePropertyChanges(  );
    wiz.setIgnorePropertyChanges( true );

    if( !frame.isShowing(  ) ) {
      makeGUI(  );
      showGUI(  );
    }

    if( ( index < 0 ) || ( index > ( wiz.getNumForms(  ) - 1 ) ) ) {  // invalid index
      DataSetTools.util.SharedData.addmsg( 
        "Error: invalid form number in show(" + index + ")\n" );

      return;
    }

    Form f = wiz.getCurrentForm(  );  // get rid of any current form

    if( f != null ) {
      f.setVisible( false );
    }
    form_panel.removeAll(  );
    f = wiz.getForm( index );  // show the specified form
    form_panel.add( f.getPanel(  ) );
    f.setVisible( true );
    form_panel.validate(  );
    wiz.setCurrentFormNumber( index );
    enableNavButtons( true, index );

    if( wiz.getNumForms(  ) == 1 ) {
      form_label.setText( f.getTitle(  ) );
    } else {
      form_label.setText( "Form " + ( index + 1 ) + ": " + f.getTitle(  ) );
    }
    updateFormProgress(  );
    updateWizardProgress(  );
    populateViewMenu(  );
    wiz.setIgnorePropertyChanges( ignore );
  }

  /**
   * Shows the last valid Form.
   */
  public final void showLastValidForm(  ) {
    int lastValidNum = wiz.getLastValidFormNum(  );

    if( lastValidNum < 0 ) {
      showForm( 0 );
    } else {
      showForm( lastValidNum );
    }
  }

  /**
   * Method to update the formProgress progress bar based on whether or not the
   * Form is done().  This sets the value and the String label.  The basic use
   * of this method is when you want to set the Form progress bar to
   * completely done or completely "not done."
   */
  public final void updateFormProgress(  ) {
    Form f = wiz.getCurrentForm(  );

    if( f != null ) {
      if( f.done(  ) ) {
        formProgress.setString( f.getTitle(  ) + " Done" );
        formProgress.setValue( FORM_PROGRESS );
      } else {
        formProgress.setString( f.getTitle(  ) + " Progress" );
        formProgress.setValue( 0 );
      }
    } else {
      formProgress.setString( "Form Progress" );
      formProgress.setValue( 0 );
    }
  }

  /**
   * Updates the wizProgress bar showing the overall progress of the Wizard
   * based on the last form completed at the time of the method call.
   */
  public final void updateWizardProgress(  ) {
    int lastDone = wiz.getLastValidFormNum(  ) + 1;
    wizProgress.setValue( lastDone );
    wizProgress.setString( 
      "Wizard Progress: " + ( lastDone ) + " of " + wiz.getNumForms(  ) +
      " Forms done" );
  }

  /**
   * Accessor method for the progress indicator.  This particular version
   * allows access to the internal form progress bar.
   *
   * @return The PropertyChanger form progress indicator.
   */
  public PropertyChangeListener getFormProgressIndicator(  ) {
    return formProgress;
  }

  /**
   * Sets the WizardFrontEnd's opening size.  This will attempt to find the
   * width and height in IsawProps.dat (WIZARD_WIDTH and WIZARD_HEIGHT).  If
   * it cannot find them, it sets the size to 75% of screen height and 45% of
   * screen width.
   */
  private void setInitialSize(  ) {
    int height;
    int width;

    //set height
    String num = SharedData.getProperty( "WIZARD_HEIGHT" );

    if( num != null ) {
      height = Integer.parseInt( num );
    } else {
      height = ( int )( Toolkit.getDefaultToolkit(  ).getScreenSize(  )
                               .getHeight(  ) * 0.75f );
    }

    //set width
    num = SharedData.getProperty( "WIZARD_WIDTH" );

    if( num != null ) {
      width = Integer.parseInt( num );
    } else {
      width = ( int )( Toolkit.getDefaultToolkit(  ).getScreenSize(  ).getWidth(  ) * 0.45f );
    }
    frame.setBounds( 0, 0, width, height );
  }

  /**
   * Utility to display an HTML formatted help message.
   *
   * @param tempTitle The title to use.
   * @param html The help message to display.
   */
  private void displayHelpMessage( String tempTitle, String html ) {
    JFrame help_frame     = new JFrame( tempTitle );
    Dimension screen_size = Toolkit.getDefaultToolkit(  ).getScreenSize(  );
    help_frame.setSize( 
      new Dimension( 
        ( int )( screen_size.getWidth(  ) / 2 ),
        ( int )( screen_size.getHeight(  ) / 2 ) ) );

    JEditorPane htmlDisplay = new JEditorPane( "text/html", html );
    htmlDisplay.setEditable( false );
    help_frame.getContentPane(  ).add( new JScrollPane( htmlDisplay ) );
    help_frame.show(  );
  }

  /**
   * Method to call a ParameterViewer.  Since the only "oddball" events that
   * currently happen are for the view menu, the only commands to listen for
   * are the ones for the current form.
   *
   * @param com The command (IParameterGUI name) to attempt to display the
   *        parameter viewer for.
   */
  private void displayParameterViewer( String com ) {
    Form f;
    IParameterGUI iparam;
    boolean done;
    int index;
    int num_params;
    f            = wiz.getCurrentForm(  );
    done         = false;
    index        = 0;

    //get the parameters, remembering to get the result parameter
    num_params   = f.getNum_parameters(  ) + 1;

    while( !done && ( index < num_params ) ) {
      iparam   = ( IParameterGUI )f.getParameter( index );

      //does the command match up to a current form parameter name?
      done     = com.equals( iparam.getName(  ) );

      if( done ) {
        new ParameterViewer( iparam ).showParameterViewer(  );
      }
      index++;
    }
  }

  /**
   * Utility to display an HTML formatted help message.  If an error occurs
   * while reading the URL a blank JFrame is displayed, otherwise a JFrame
   * with the HTML content at the URL is displayed.  URLs were used because
   * JEditorPanes can't handle relative links without a document base.
   *
   * @param tempTitle The title to use.
   * @param url The URL that contains the HTML page.
   */
  private void displayURL( String tempTitle, URL url ) {
    JFrame help_frame     = new JFrame( tempTitle );
    Dimension screen_size = Toolkit.getDefaultToolkit(  ).getScreenSize(  );
    help_frame.setSize( 
      new Dimension( 
        ( int )( screen_size.getWidth(  ) / 2 ),
        ( int )( screen_size.getHeight(  ) / 2 ) ) );

    JEditorPane htmlDisplay = null;

    try {
      htmlDisplay = new JEditorPane( url );
    } catch( IOException ioe ) {
      htmlDisplay = new JEditorPane(  );
    }
    htmlDisplay.setEditable( false );
    help_frame.getContentPane(  ).add( new JScrollPane( htmlDisplay ) );
    help_frame.show(  );
  }

  /**
   * Utility to enable/disable the WizardFrontEnd navigation buttons.
   *
   * @param enable true to enable, false to disable.
   * @param index The index of the Form to enable/disable navigation buttons
   *        on.
   */
  private void enableNavButtons( boolean enable, int index ) {
    if( enable ) {
      for( int i = 0; i < wizButtons.length; i++ ) {
        wizButtons[i].setEnabled( true );
      }

      // enable/disable the navigation buttons
      if( index >= ( wiz.getNumForms(  ) - 1 ) ) {
        wizButtons[NEXT_IND].setEnabled( false );
        wizButtons[LAST_IND].setEnabled( false );
      }

      if( index <= 0 ) {
        wizButtons[BACK_IND].setEnabled( false );
        wizButtons[FIRST_IND].setEnabled( false );
      }
    } else {
      //disable all the buttons
      for( int i = 0; i < wizButtons.length; i++ ) {
        wizButtons[i].setEnabled( false );
      }
    }
  }

  /**
   * Utility for initializing the global fileChooser.
   */
  private void initFileChooser(  ) {
    fileChooser = new JFileChooser(  );
    fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
    fileChooser.setFileFilter( new WizardFileFilter(  ) );

    //start out in the projects directory
    if( wiz.getProjectsDirectory(  ) != null ) {
      fileChooser.setCurrentDirectory( 
        new File( wiz.getProjectsDirectory(  ) ) );
    }
  }

  /**
   * Initializes the progress bars.  Called from makeGUI().
   */
  private void initProgressBars(  ) {
    formProgress.setStringPainted( true );
    wizProgress.setStringPainted( true );
    wizProgress.setMaximum( wiz.getNumForms(  ) );
    updateFormProgress(  );
    updateWizardProgress(  );
  }

  /**
   * Launches a JFileChooser so the user can set the project directory.
   */
  private void launchProjectChooser(  ) {
    JFileChooser projChooser = new JFileChooser(  );
    projChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

    if( wiz.getProjectsDirectory(  ) != null ) {
      projChooser.setCurrentDirectory( 
        new File( wiz.getProjectsDirectory(  ) ) );
    }

    int result = projChooser.showOpenDialog( 
        new JFrame( "Select Project Directory" ) );

    if( result != JFileChooser.CANCEL_OPTION ) {
      wiz.setProjectsDirectory( projChooser.getSelectedFile(  ).toString(  ) );
    }
  }

  /**
   * Makes the GUI for this WizardFrontEnd.
   */
  private void makeGUI(  ) {
    Box formControlsBox       = Box.createHorizontalBox(  );
    Box wizardControlsBox     = Box.createHorizontalBox(  );
    Box statusBox             = Box.createHorizontalBox(  );
    Box executionBox          = Box.createVerticalBox(  );
    Box commBox               = Box.createVerticalBox(  );
    JButton exec_all_button   = new JButton( EXEC_ALL_COMMAND );
    JButton first_button      = new JButton( FIRST_COMMAND );
    JButton back_button       = new JButton( BACK_COMMAND );
    JButton next_button       = new JButton( NEXT_COMMAND );
    JButton last_button       = new JButton( LAST_COMMAND );
    JButton exec_button       = new JButton( EXEC_COMMAND );
    JButton clear_button      = new JButton( CLEAR_COMMAND );
    JButton clear_all_button  = new JButton( CLEAR_ALL_COMMAND );
    wizButtons[EXEC_ALL_IND]  = exec_all_button;
    wizButtons[EXEC_IND]      = exec_button;
    wizButtons[CLEAR_IND]     = clear_button;
    wizButtons[FIRST_IND]     = first_button;
    wizButtons[BACK_IND]      = back_button;
    wizButtons[NEXT_IND]      = next_button;
    wizButtons[LAST_IND]      = last_button;
    wizButtons[CLEAR_ALL_IND] = clear_all_button;

    JPanel work_area          = new JPanel( new BorderLayout(  ) );
    JPanel controlsArea       = new JPanel( new BorderLayout(  ) );
    JPanel navControlsBox     = new JPanel( new GridLayout(  ) );
    JMenuBar menu_bar         = new JMenuBar(  );
    initProgressBars(  );
    frame.setJMenuBar( menu_bar );
    frame.addWindowListener( new CloseWizardWindow(  ) );
    frame.getContentPane(  ).add( work_area );
    setInitialSize(  );

    JMenu[] menuList = makeMenu(  );

    for( int j = 0; j < menuList.length; j++ ) {
      menu_bar.add( menuList[j] );
    }

    JScrollPane form_scrollpane = new JScrollPane( form_panel );

    // add the title to the work area
    work_area.add( form_label, BorderLayout.NORTH );

    //set up and add the "comm" box
    commBox.add( controlsArea );
    commBox.add( statusBox );

    //set up and add the execution box
    executionBox.add( formControlsBox );
    executionBox.add( wizardControlsBox );
    controlsArea.add( executionBox, BorderLayout.NORTH );

    //add the navigation controls box
    controlsArea.add( navControlsBox, BorderLayout.SOUTH );

    //set up and add the scrolled Form panel
    form_panel.setLayout( new GridLayout( 1, 1 ) );
    work_area.add( form_scrollpane, BorderLayout.CENTER );
    work_area.add( commBox, BorderLayout.SOUTH );

    //add the buttons and progress bars to each controls box
    formControlsBox.add( clear_button );
    formControlsBox.add( formProgress );
    formControlsBox.add( exec_button );
    wizardControlsBox.add( clear_all_button );
    wizardControlsBox.add( wizProgress );
    wizardControlsBox.add( exec_all_button );

    //add the navigation buttons to the navigation controls box
    navControlsBox.add( first_button );
    navControlsBox.add( back_button );
    navControlsBox.add( next_button );
    navControlsBox.add( last_button );

    // add the status to the panel
    if( wiz.getStandalone(  ) ) {
      JPanel statusPanel = SharedData.getStatusPane(  );
      statusBox.add( statusPanel );

      //status pane will grow very large if we let it
      statusPanel.setPreferredSize( new Dimension( 500, 100 ) );
    }

    //add the ActionListener to the buttons.
    for( int k = 0; k < wizButtons.length; k++ ) {
      wizButtons[k].addActionListener( command_handler );
    }
    enableNavButtons( true, 0 );
    wizProgress.setMaximum( wiz.getNumForms(  ) );
    wizProgress.setString( 
      "Wizard Progress: " + ( wiz.getCurrentFormNumber(  ) + 1 ) + " of " +
      wiz.getNumForms(  ) + " Forms done" );
  }

  /**
   * Utility method to make the actual menu items and return them to makeGUI()
   * so it can add them to the menu.  This also takes care of adding the
   * ActionListener to the menu items.
   *
   * @return Array of JMenus to add to a menu bar.
   */
  private JMenu[] makeMenu(  ) {
    JMenu[] menuList      = new JMenu[4];
    JMenu file_menu       = new JMenu( "File" );
    JMenu project_menu    = new JMenu( "Project Directory" );
    JMenu help_menu       = new JMenu( "Help" );
    view_menu             = new JMenu( VIEW_MENU );

    JMenuItem wizard_help = new JMenuItem( WIZARD_HELP_COMMAND );
    JMenuItem form_help   = new JMenuItem( FORM_HELP_COMMAND );
    JMenuItem setDir      = new JMenuItem( SET_PROJECT_DIR );
    JMenuItem save_wizard = new JMenuItem( SAVE_WIZARD_COMMAND );
    JMenuItem load_wizard = new JMenuItem( LOAD_WIZARD_COMMAND );
    JMenuItem exit_item   = new JMenuItem( EXIT_COMMAND );
    help_menu.add( wizard_help );
    help_menu.add( form_help );
    file_menu.addSeparator(  );
    file_menu.add( save_wizard );
    file_menu.add( load_wizard );
    file_menu.addSeparator(  );
    file_menu.add( exit_item );
    project_menu.add( setDir );
    save_wizard.setEnabled( true );
    load_wizard.setEnabled( true );
    menuList[0]   = file_menu;
    menuList[1]   = view_menu;
    menuList[2]   = project_menu;
    menuList[3]   = help_menu;
    wizard_help.addActionListener( command_handler );
    form_help.addActionListener( command_handler );
    exit_item.addActionListener( command_handler );
    view_menu.addActionListener( command_handler );
    save_wizard.addActionListener( command_handler );
    load_wizard.addActionListener( command_handler );
    setDir.addActionListener( command_handler );

    return menuList;
  }

  /**
   * Creates the view menu and listeners for certain types of parameters in the
   * current Form.
   */
  private void populateViewMenu(  ) {
    JMenuItem jmi;
    Form f;
    IParameterGUI iparam;
    Object val;
    f = wiz.getCurrentForm(  );

    if( f != null ) {
      view_menu.removeAll(  );

      //go through the parameter list.  We also want to look at the result
      //parameter
      for( int i = 0; i < ( f.getNum_parameters(  ) + 1 ); i++ ) {
        iparam   = ( IParameterGUI )f.getParameter( i );
        val      = iparam.getValue(  );

        /*semi-sophisticated attempt at being able to view
           DataSets, Vectors of items, and files.  Things like
           Strings and ints, which are easily viewable on the
           Form, should not be sent to the ParameterViewer. */
        if( 
          ( iparam instanceof DataSetPG ) || ( iparam instanceof ArrayPG ) ||
            ( iparam instanceof LoadFilePG ) || ( iparam instanceof SaveFilePG ) ||
            ( iparam instanceof StringPG &&
            ( val.toString(  ).indexOf( '.' ) > 0 ) ) ||
            iparam instanceof VectorPG ) {
          jmi = new JMenuItem( iparam.getName(  ) );
          view_menu.add( jmi );
          jmi.addActionListener( command_handler );
        }
      }
    }

    //arbitrary file viewing
    view_menu.addSeparator(  );
    jmi = new JMenuItem( VIEW_DS );
    view_menu.add( jmi );
    jmi.addActionListener( command_handler );
    jmi = new JMenuItem( VIEW_ASCII );
    view_menu.add( jmi );
    jmi.addActionListener( command_handler );
  }

  /**
   * Shows the JavaHelp HTML page for the current form.
   */
  private void showFormHelpMessage(  ) {
    HTMLizer form_htmlizer = new HTMLizer(  );
    Form f                 = wiz.getCurrentForm(  );

    if( f != null ) {
      String html = form_htmlizer.createHTML( f );
      displayHelpMessage( wiz.getTitle(  ), html );
    }
  }

  /**
   * Shows the GUI for this Wizard by calling the outer Frame's show() method.
   */
  private void showGUI(  ) {
    frame.show(  );
  }

  /**
   * Show the specified String in the help frame.  This is for the wizard help
   * message
   */
  private void showWizardHelpMessage(  ) {
    String wTitle = "Help: " + wiz.getTitle(  );

    if( wiz.getWizardHelpURL(  ) != null ) {
      displayURL( wTitle, wiz.getWizardHelpURL(  ) );
    } else {
      displayHelpMessage( wTitle, help_message );
    }
  }

  //~ Inner Classes ************************************************************

  /**
   * This class closes down the application when the user closes the frame.
   */
  private class CloseWizardWindow extends WindowAdapter {
    //~ Methods ****************************************************************

    /**
     * Triggered when a window is closed.
     *
     * @param event The window event which was triggered.
     */
    public void windowClosing( WindowEvent event ) {
      close(  );
    }
  }

  /* ---------------- Internal Event Handler Classes --------------------- */

  /**
   * This class handles all of the commands from buttons and menu items.
   */
  private class CommandHandler implements ActionListener {
    //~ Instance fields ********************************************************

    private Wizard wizard;
    private WizardWorker worker;

    //~ Constructors ***********************************************************

    /**
     * Creates a new CommandHandler object.
     *
     * @param wiz The Wizard to have this CommandHandler listen to.
     */
    public CommandHandler( Wizard wiz ) {
      this.wizard = wiz;
    }

    //~ Methods ****************************************************************

    /**
     * Required for ActionListener implementation.  Listens to all the Wizard
     * buttons.
     *
     * @param event The triggering ActionEvent
     */
    public void actionPerformed( ActionEvent event ) {
      String command = event.getActionCommand(  );
      int curFormNum = wizard.getCurrentFormNumber(  );

      if( command == FIRST_COMMAND ) {
        showForm( 0 );
        populateViewMenu(  );
      } else if( command == BACK_COMMAND ) {
        if( ( curFormNum - 1 ) >= 0 ) {
          showForm( --curFormNum );
          populateViewMenu(  );
        } else {
          DataSetTools.util.SharedData.addmsg( 
            "FORM 0 SHOWN, CAN'T STEP BACK\n" );
        }
      } else if( command == NEXT_COMMAND ) {
        if( ( curFormNum + 1 ) < wizard.getNumForms(  ) ) {
          showForm( ++curFormNum );
          populateViewMenu(  );
        } else {
          DataSetTools.util.SharedData.addmsg( 
            "NO MORE FORMS, CAN'T ADVANCE\n" );
        }
      } else if( command == LAST_COMMAND ) {
        showForm( wizard.getNumForms(  ) - 1 );
        populateViewMenu(  );
      } else if( command == CLEAR_ALL_COMMAND ) {
        wizard.invalidate( 0 );
      } else if( command == CLEAR_COMMAND ) {
        wizard.invalidate( curFormNum);
      } else if( command == EXEC_ALL_COMMAND ) {
        worker = new WizardWorker( wizard );
        worker.setFormNumber( wizard.getNumForms(  ) - 1 );
        worker.start(  );
      } else if( command == EXEC_COMMAND ) {
        //a new SwingWorker needs to be created for each click of the
        //execute button
        worker = new WizardWorker( wizard );
        worker.setFormNumber( curFormNum );
        worker.start(  );
      } else if( command == WIZARD_HELP_COMMAND ) {
        showWizardHelpMessage(  );
      } else if( command == FORM_HELP_COMMAND ) {
        showFormHelpMessage(  );
      } else if( command == SAVE_WIZARD_COMMAND ) {
        wizard.save(  );
      } else if( command == LOAD_WIZARD_COMMAND ) {
        wizard.load(  );
      } else if( command == VIEW_MENU ) {
        populateViewMenu(  );
      } else if( command == SET_PROJECT_DIR ) {
        launchProjectChooser(  );
      } else if( command == EXIT_COMMAND ) {
        close(  );
      } else if( ( command == VIEW_ASCII ) || ( command == VIEW_DS ) ) {
        displayFileBrowser( command );
      } else {
        //parameter selection command
        displayParameterViewer( command );
      }
    }

    /**
     * Method to bring up a file browser for viewing arbitrary files.
     *
     * @param type The type of file to display, VIEW_DS (DataSet) or VIEW_ASCII
     *        (text).
     */
    private void displayFileBrowser( String type ) {
      JFileChooser fChooser = new JFileChooser( 
          wizard.getProjectsDirectory(  ) );
      int result            = fChooser.showOpenDialog( 
          new JFrame( type + "..." ) );

      if( result != JFileChooser.CANCEL_OPTION ) {
        String fileName = fChooser.getSelectedFile(  ).toString(  );

        if( type == VIEW_ASCII ) {
          ParameterViewer.tryToDisplayASCII( fileName );
        } else if( type == VIEW_DS ) {
          String sNum = JOptionPane.showInputDialog( "What DataSet Number?" );

          if( sNum != null ) {
            //get the DataSet out of the file and display it
            ParameterViewer.displayDataSet( fileName, Integer.parseInt( sNum ) );
          }
        }
      }
    }
  }

  /**
   * This wizard is multithreaded so that the progress bar and Form update
   * messages can be displayed for the user.  Since we only need two threads
   * of execution, the choice of a SwingWorker was clear.
   */
  private class WizardWorker extends SwingWorker {
    //~ Instance fields ********************************************************

    private int formNum   = 0;
    private Wizard wizard;

    //~ Constructors ***********************************************************

    /**
     * Constructor to allow direct linking of the enclosing class's Wizard
     * variable.
     *
     * @param wiz The enclosing class's Wizard.
     */
    public WizardWorker( Wizard wiz ) {
      wizard = wiz;
    }

    //~ Methods ****************************************************************

    /**
     * Used to set the Form number for calling exec_forms.
     *
     * @param num the Form number to use for exec_forms.
     */
    public void setFormNumber( int num ) {
      formNum = num;
    }

    /**
     * Required for SwingWorker.
     *
     * @return "Success" - unused.
     */
    public Object construct(  ) {
      //can't have users mutating the values!
      enableNavButtons( false, wizard.getCurrentFormNumber(  ) );
      this.enableFormParams( false );

      try {
        //here is where the time intensive work is.
        wizard.exec_forms( formNum );

        return "Success";
      } catch( Throwable e ) {
        //crashed hard when executing an outside Operator, Form, or Script
        //try to salvage what we can
        StringBuffer message = new StringBuffer(  );
        message.append( "An error has occurred during execution.  Please\n" );
        message.append( "save the Wizard and send both the Wizard Save\n" );
        message.append( "File and the wizard.err file to your developer.\n" );
        displayAndSaveErrorMessage( e, "wizard", message );

        //reset the progress bars by re-showing the Form
        showForm( wizard.getCurrentFormNumber(  ) );

        return "Failure";
      } finally {
        populateViewMenu(  );
        enableNavButtons( true, wizard.getCurrentFormNumber(  ) );
        this.enableFormParams( true );
      }
    }

    /**
     * Implicit calls to getCurrentForm in order to enable or disable the
     * parameters.
     *
     * @param enable Whether to enable parameters or not.
     */
    private void enableFormParams( boolean enable ) {
      Form f            = wizard.getCurrentForm(  );
      int[] var_indices = f.getVarParamIndices(  );

      for( int j = 0; j < var_indices.length; j++ ) {
        ( ( IParameterGUI )f.getParameter( var_indices[j] ) ).setEnabled( 
          enable );
      }
    }
  }
}
