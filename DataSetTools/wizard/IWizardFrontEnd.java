/*
 * File:  IWizardFrontEnd.java
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
 * Revision 1.2  2003/12/16 00:51:27  bouzekc
 * Fixed bug that prevented Form progress indicator from advancing
 * incrementally.
 *
 * Revision 1.1  2003/11/29 21:50:58  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.wizard;

import java.beans.PropertyChangeListener;

import java.io.*;


/**
 * This is a package level interface defining the front end GUI for a Wizard.
 */
interface IWizardFrontEnd {
  //~ Static fields/initializers ***********************************************

  public static final String EXIT_COMMAND        = "Exit";
  public static final String FIRST_COMMAND       = " << ";
  public static final String BACK_COMMAND        = " < ";
  public static final String NEXT_COMMAND        = " > ";
  public static final String LAST_COMMAND        = " >> ";
  public static final String CLEAR_COMMAND       = "Reset";
  public static final String CLEAR_ALL_COMMAND   = "Reset All";
  public static final String EXEC_ALL_COMMAND    = "Do All";
  public static final String EXEC_COMMAND        = "Do";
  public static final String WIZARD_HELP_COMMAND = "on Wizard";
  public static final String FORM_HELP_COMMAND   = "on Current Form";
  public static final String SAVE_WIZARD_COMMAND = "Save Wizard State";
  public static final String LOAD_WIZARD_COMMAND = "Load Wizard State";
  public static final String VIEW_MENU           = "View Parameters";
  public static final String SET_PROJECT_DIR     = "Set Project Directory";
  public static final int FORM_PROGRESS          = 100;
  public static final int EXEC_ALL_IND           = 0;
  public static final int EXEC_IND               = 1;
  public static final int CLEAR_IND              = 2;
  public static final int FIRST_IND              = 3;
  public static final int BACK_IND               = 4;
  public static final int NEXT_IND               = 5;
  public static final int LAST_IND               = 6;
  public static final int CLEAR_ALL_IND          = 7;

  //~ Methods ******************************************************************

  /**
   * Gets a file for input or output.
   *
   * @param saving A boolean indicating whether you want to open the file for
   *        saving (true) or loading (false)
   *
   * @return the File that has been retrieved.
   */
  public File getFile( boolean saving );

  /**
   * Accessor method for the progress indicator.  For example, invoking this on
   * SwingWizardFrontEnd will get the internal progress bar.
   *
   * @return The PropertyChanger progress indicator.
   */
  public PropertyChangeListener getFormProgressIndicator(  );

  /**
   * Utility method to set the progress widget value and label.
   *
   * @param value The new value to set.
   * @param label The new label to set
   */
  public void setFormProgressParameters( int value, String label );

  /**
   * Exits the wizard application.  If the Wizard has been changed and not been
   * saved, this should ask the user if they want to save.
   */
  public void close(  );

  /**
   * Utility to display an error message and write an error file when a .wsf
   * file is not loaded successfully.
   *
   * @param e The Throwable generated when the attempt was made to load the WSF
   *        file.
   * @param errFile The error file name to write to, without extension.
   * @param errMess The message to display.
   */
  public void displayAndSaveErrorMessage( 
    Throwable e, String errFile, StringBuffer errMess );

  /**
   * Show the form at the specified position in the list of forms. If the index
   * is invalid, an error message will be displayed in the status pane. To
   * avoid strange events that can occur due to all of the
   * PropertyChangeListeners, this sets ignorePropChanges to true upon entry,
   * and to the previous state upon exit.
   *
   * @param index The index of the form to show.
   */
  public void showForm( int index );

  /**
   * Shows the last valid Form.
   */
  public void showLastValidForm(  );

  /**
   * Method to update the formProgress progress widget based on whether or not
   * the Form is done().  This sets the value and the String label.  The basic
   * use of this method is when you want to set the Form progress to
   * completely done or completely "not done."
   */
  public void updateFormProgress(  );

  /**
   * Updates the wizProgress widget showing the overall progress of the Wizard
   * based on the last form completed at the time of the method call.
   */
  public void updateWizardProgress(  );
}
