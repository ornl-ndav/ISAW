/*
 * File:  IGUIWizardFrontEnd.java
 *
 * Copyright (C) 2004 Chris Bouzek
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
 * Revision 1.1  2004/01/09 22:25:45  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.wizard;

import java.beans.PropertyChangeListener;


/**
 * This is a package level interface defining a GUI front end for a Wizard.
 */
interface IGUIWizardFrontEnd extends IWizardFrontEnd {
  //~ Static fields/initializers ***********************************************

  public static final int FORM_PROGRESS          = 100;
  public static final String VIEW_MENU           = "View";
  public static final String SET_PROJECT_DIR     = "Set Project Directory";
  public static final String WIZARD_HELP_COMMAND = "on Wizard";
  public static final String FORM_HELP_COMMAND   = "on Current Form";
  public static final int EXEC_ALL_IND           = 0;
  public static final int EXEC_IND               = 1;
  public static final int CLEAR_IND              = 2;
  public static final int FIRST_IND              = 3;
  public static final int BACK_IND               = 4;
  public static final int NEXT_IND               = 5;
  public static final int LAST_IND               = 6;
  public static final int CLEAR_ALL_IND          = 7;
  public static final String EXIT_COMMAND        = "Exit";
  public static final String FIRST_COMMAND       = "First Form";
  public static final String BACK_COMMAND        = "Back One";
  public static final String NEXT_COMMAND        = "Forward One";
  public static final String LAST_COMMAND        = "Last Form";
  public static final String CLEAR_COMMAND       = "Reset";
  public static final String CLEAR_ALL_COMMAND   = "Reset All";
  public static final String EXEC_ALL_COMMAND    = "Do All";
  public static final String EXEC_COMMAND        = "Do";
  public static final String SAVE_WIZARD_COMMAND = "Save Wizard State";
  public static final String LOAD_WIZARD_COMMAND = "Load Wizard State";

  //~ Methods ******************************************************************

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
