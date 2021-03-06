/*
 * File:  InitialPeaksWizard.java
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
 * Revision 1.19  2006/07/10 21:48:02  dennis
 * Removed unused imports after refactoring to use New Parameter
 * GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.18  2006/07/10 16:26:14  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.17  2005/03/06 00:40:15  dennis
 * Added more documentation explaining the two dimensional array of
 * integers that coordinates the parameters that are shared between
 * wizard forms.
 *
 * Revision 1.16  2004/04/21 19:15:00  dennis
 * Changed to work with new FindMultiplePeaksForm that has the
 * min and max time channels as parameters.
 *
 * Revision 1.15  2003/12/15 02:47:33  bouzekc
 * Removed unused imports.
 *
 * Revision 1.14  2003/11/05 02:20:30  bouzekc
 * Changed to work with new Wizard and Form design.
 *
 * Revision 1.13  2003/10/27 01:31:39  bouzekc
 * Modified to work with updated Forms.
 *
 * Revision 1.12  2003/10/18 21:22:02  bouzekc
 * Now uses HTML file for help message.
 *
 * Revision 1.11  2003/07/16 16:29:13  bouzekc
 * Now uses the base class's wizardLoader().
 *
 * Revision 1.10  2003/07/08 23:04:47  bouzekc
 * Added second IndexJ OperatorForm to end of Wizard.
 *
 * Revision 1.9  2003/07/08 22:50:22  bouzekc
 * Changed result parameter names.
 *
 * Revision 1.8  2003/07/03 14:17:43  bouzekc
 * Added comments and ordered methods according to access
 * privilege.
 *
 * Revision 1.7  2003/06/30 16:05:53  bouzekc
 * Now takes --nogui command line arguments.
 *
 * Revision 1.6  2003/06/26 16:31:18  bouzekc
 * Unlinked the LsqrsJForm matrix file parameter from the
 * other Forms.
 *
 * Revision 1.5  2003/06/25 20:25:36  bouzekc
 * Unused private variables removed, reformatted for
 * consistency.
 *
 * Revision 1.4  2003/06/19 20:51:39  bouzekc
 * Now uses constant parameters for the OperatorForms.
 *
 * Revision 1.3  2003/06/19 16:20:32  bouzekc
 * Now uses Wizard's linkFormParameters() to link the
 * parameters in the parameter table.
 *
 * Revision 1.2  2003/06/11 22:44:31  bouzekc
 * Added Wizard help message.
 *
 * Revision 1.1  2003/06/10 21:06:15  bouzekc
 *
 * Added to CVS
 *
 *
 */
package Wizard.TOF_SCD;

import gov.anl.ipns.Parameters.LoadFilePG;

import DataSetTools.wizard.*;

import Operators.TOF_SCD.*;


/**
 * This class constructs a Wizard used for initially finding peaks.  In this
 * Wizard, BlindJ is used for creating a matrix file.
 */
public class InitialPeaksWizard extends Wizard {
  //~ Constructors *************************************************************

  /**
   * Default constructor.  Sets standalone in Wizard to true.
   */
  public InitialPeaksWizard(  ) {
    this( true );
  }

  /**
   * Constructor for setting the standalone variable in Wizard.
   *
   * @param standalone Boolean indicating whether the Wizard stands alone
   *        (true) or is contained in something else (false).
   */
  public InitialPeaksWizard( boolean standalone ) {
    super( "Initial SCD Peaks Wizard", standalone );
    this.createAllForms(  );
    this.setHelpURL( "IPW.html" );
  }

  //~ Methods ******************************************************************

  /**
   * Method for running the Initial Peaks wizard as standalone.
   */
  public static void main( String[] args ) {
    InitialPeaksWizard w = new InitialPeaksWizard( true );
    w.wizardLoader( args );
  }

  /**
   * Adds and coordinates the necessary Forms for this Wizard. Each row
   * of the array corresponds to one of the parameters that is shared across
   * the different forms of this wizard.  Each column corresponds to one
   * of the forms of the wizard.  The entry in row k, column j is the 
   * parameter number used for the kth shared parameter on form j.  A "-1"
   * in row k, column j indicates that the kth shared parameter does not
   * appear on form j.
   */
  private void createAllForms(  ) {
    int[][] fpi                     = {
      { 12,  0,  0, -1,  0,  0 },  //peaks file 
      { -1,  2,  1,  0, -1, -1 }   //matrix
    }; 
    FindMultiplePeaksForm peaksform = new FindMultiplePeaksForm(  );
    OperatorForm blindjform         = new OperatorForm( 
        new BlindJ(  ), new LoadFilePG( "BlindJ log file", null ),
        new int[]{ 0 } );
    OperatorForm indexjform         = new OperatorForm( 
        new IndexJ(  ), new LoadFilePG( "IndexJ log file", null ),
        new int[]{ 0, 1 } );
    OperatorForm scalarjform        = new OperatorForm( 
        new ScalarJ(  ), new LoadFilePG( "ScalarJ log file", null ),
        new int[]{ 0 } );
    OperatorForm lsqrsjform         = new OperatorForm( 
        new LsqrsJ(  ), new LoadFilePG( "LsqrsJ log file", null ),
        new int[]{ 0 } );
    OperatorForm indexjform2        = new OperatorForm( 
        new IndexJ(  ), new LoadFilePG( "IndexJ log file", null ),
        new int[]{ 0 } );
    this.addForm( peaksform );
    this.addForm( blindjform );
    this.addForm( indexjform );
    this.addForm( scalarjform );
    this.addForm( lsqrsjform );
    this.addForm( indexjform2 );
    super.linkFormParameters( fpi );
  }
}
