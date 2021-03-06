/*
 * File:  DailyPeaksWizard.java
 *
 * Copyright (C) 2003, Chris M. Bouzek
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
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.14  2005/03/06 00:38:30  dennis
 * Adjusted the table of shared parameters to account for the new
 * d_min parameter added to the integrate form.
 *
 * Revision 1.13  2004/04/21 19:30:14  dennis
 * Now uses peaksform.APPEND_PARAM instead of '8' to refer back
 * to the FindMultiplePeaksForm parameter for appending to a
 * file.
 *
 * Revision 1.12  2004/04/21 19:15:00  dennis
 * Changed to work with new FindMultiplePeaksForm that has the
 * min and max time channels as parameters.
 *
 * Revision 1.11  2003/12/15 01:45:31  bouzekc
 * Removed unused imports.
 *
 * Revision 1.10  2003/10/18 21:22:02  bouzekc
 * Now uses HTML file for help message.
 *
 * Revision 1.9  2003/07/16 16:29:00  bouzekc
 * Now uses the base class's wizardLoader().
 *
 * Revision 1.8  2003/07/14 15:34:29  bouzekc
 * Removed link to IntegrateMultiRunsForm's run numbers
 * parameter.
 *
 * Revision 1.7  2003/07/03 14:16:30  bouzekc
 * Added comments and ordered methods according to access
 * privilege.
 *
 * Revision 1.6  2003/06/30 16:05:52  bouzekc
 * Now takes --nogui command line arguments.
 *
 * Revision 1.5  2003/06/26 16:27:27  bouzekc
 * "Update Peaks File" now defaults to true.
 *
 * Revision 1.4  2003/06/25 20:25:33  bouzekc
 * Unused private variables removed, reformatted for
 * consistency.
 *
 * Revision 1.3  2003/06/19 16:20:17  bouzekc
 * Now uses Wizard's linkFormParameters() to link the
 * parameters in the parameter table.
 *
 * Revision 1.2  2003/06/11 22:37:23  bouzekc
 * Added Wizard help message.  Fixed parameter linking to work
 * with updated Forms.
 *
 * Revision 1.1  2003/06/10 21:06:49  bouzekc
 * Added to CVS.
 *
 *
 */
package Wizard.TOF_SCD;

import DataSetTools.wizard.*;


/**
 * This class constructs a Wizard used for initially finding peaks.  In this
 * Wizard, an iteration over LsqrsJ and IndexJ is used for creating a matrix
 * file.
 */
public class DailyPeaksWizard extends Wizard {
  //~ Constructors *************************************************************

  /**
   * Default constructor.  Sets standalone in Wizard to true.
   */
  public DailyPeaksWizard(  ) {
    this( true );
  }

  /**
   * Constructor for setting the standalone variable in Wizard.
   *
   * @param standalone Boolean indicating whether the Wizard stands alone
   *        (true) or is contained in something else (false).
   */
  public DailyPeaksWizard( boolean standalone ) {
    super( "Daily SCD Peaks Wizard", standalone );
    this.createAllForms(  );
    this.setHelpURL( "DPW.html" );
  }

  //~ Methods ******************************************************************

  /**
   * Method for running the Daily Peaks wizard as standalone.
   */
  public static void main( String[] args ) {
    DailyPeaksWizard w = new DailyPeaksWizard( true );
    w.wizardLoader( args );
  }

  /**
   * Adds and coordinates the necessary Forms for this Wizard.  Each row
   * of the array corresponds to one of the parameters that is shared across
   * the different forms of this wizard.  Each column corresponds to one
   * of the forms of the wizard.  The entry in row k, column j is the 
   * parameter number used for the kth shared parameter on form j.  A "-1"
   * in row k, column j indicates that the kth shared parameter does not
   * appear on form j.
   */
  private void createAllForms(  ) {
    int[][] fpi                     = {
      {  0, -1, -1,  0 },  //raw data path 
      {  1,  1,  1,  1 },  //peaks file path
      {  2,  0,  0, -1 },  //run numbers
      {  3,  2,  2,  3 },  //experiment name
      { 10, -1, -1,  5 },  //SCD calibration file
      {  9, -1, -1,  9 }   //SCD calibration file line
    };
    FindMultiplePeaksForm peaksform = new FindMultiplePeaksForm(  );

    //A.J. Schultz wants the "append" parameter to default to true
    peaksform.getParameter( peaksform.APPEND_PARAM )
             .setValue( Boolean.TRUE );

    //these Forms rely on previously calculated values, so set them
    //up as having constants with "true"
    IndexJForm indexjform                 = new IndexJForm( true );
    LsqrsJForm lsqrsjform                 = new LsqrsJForm( true );
    IntegrateMultiRunsForm integmultiform = new IntegrateMultiRunsForm( true );
    this.addForm( peaksform );
    this.addForm( indexjform );
    this.addForm( lsqrsjform );
    this.addForm( integmultiform );
    super.linkFormParameters( fpi );
  }
}
