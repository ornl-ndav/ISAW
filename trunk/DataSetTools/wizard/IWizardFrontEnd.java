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
 * Revision 1.5  2004/01/12 20:03:52  bouzekc
 * Moved constants into ConsoleWizardFrontEnd.
 *
 * Revision 1.4  2004/01/09 22:26:13  bouzekc
 * Separated out some methods into IGUIWizardFrontEnd.
 *
 * Revision 1.3  2004/01/08 14:52:35  bouzekc
 * Replaced arrows on navigational buttons with words.  Changed
 * "View Parameters" to "View."
 *
 * Revision 1.2  2003/12/16 00:51:27  bouzekc
 * Fixed bug that prevented Form progress indicator from advancing
 * incrementally.
 *
 * Revision 1.1  2003/11/29 21:50:58  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.wizard;

import java.io.*;


/**
 * This is a package level interface defining the front end for a Wizard.
 */
interface IWizardFrontEnd {
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
}
