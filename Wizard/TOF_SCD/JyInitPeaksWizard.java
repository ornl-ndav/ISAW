/*
 * File:  JyInitPeaksWizard.java
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
 * Revision 1.4  2003/12/15 02:17:29  bouzekc
 * Removed unused imports.
 *
 * Revision 1.3  2003/11/30 02:33:35  bouzekc
 * Calls wizardLoader() instead of showForm().
 *
 * Revision 1.2  2003/11/05 02:20:30  bouzekc
 * Changed to work with new Wizard and Form design.
 *
 * Revision 1.1  2003/07/14 20:36:46  bouzekc
 * Added to CVS.
 *
 *
 */
package Wizard.TOF_SCD;

import DataSetTools.parameter.LoadFilePG;
import DataSetTools.wizard.JyScriptForm;
import DataSetTools.wizard.OperatorForm;
import DataSetTools.wizard.Wizard;
import Operators.TOF_SCD.BlindJ;
import Operators.TOF_SCD.IndexJ;
import Operators.TOF_SCD.LsqrsJ;
import Operators.TOF_SCD.ScalarJ;


/**
 * This class constructs a Wizard used for initially finding peaks.  In this
 * Wizard, BlindJ is used for creating a matrix file.  This Wizard uses Jython
 * Script Forms and Operator Forms, as opposed to the InitialPeaksWizard,
 * which uses OperatorForms and custom crafted Forms.
 */
public class JyInitPeaksWizard extends Wizard {
  //~ Constructors *************************************************************

  /**
   * Default constructor.  Sets standalone in Wizard to true.
   */
  public JyInitPeaksWizard(  ) {
    this( true );
  }

  /**
   * Constructor for setting the standalone variable in Wizard.
   *
   * @param standalone Boolean indicating whether the Wizard stands alone
   *        (true) or is contained in something else (false).
   */
  public JyInitPeaksWizard( boolean standalone ) {
    super( "Initial SCD Peaks Wizard", standalone );
    this.createAllForms(  );

    StringBuffer s = new StringBuffer(  );
    s.append( "This Wizard is designed to be used as an initial\n" );
    s.append( "tool for finding peaks from SCD run files.  It\n" );
    s.append( "applies BlindJ, IndexJ, ScalarJ, and LsqrsJ\n" );
    s.append( "to the output .peaks file from the first Form.\n" );
    this.setHelpMessage( s.toString(  ) );
  }

  //~ Methods ******************************************************************

  /**
   * Method for running the Initial Peaks wizard as standalone.
   */
  public static void main( String[] args ) {
    JyInitPeaksWizard w = new JyInitPeaksWizard( true );
    w.wizardLoader( args );
  }

  /**
   * Adds and coordinates the necessary Forms for this Wizard.
   */
  private void createAllForms(  ) {
    int[][] fpi            = {
      { 10, 0, 0, -1, 0, 0 },  //peaks file 
      { -1, 2, 1, 0, -1, -1 }
    };  //matrix
    JyScriptForm peaksform = new JyScriptForm( 
        "find_multiple_peaks2.py", new LoadFilePG( "Peaks file", null, false ) );

    //the return types of all of these Operator Forms is LoadFilePG,
    //hence the "LoadFile"
    OperatorForm blindjform  = new OperatorForm( 
        new BlindJ(  ), new LoadFilePG( "BlindJ log file", null, false ),
        new int[]{ 0 } );
    OperatorForm indexjform  = new OperatorForm( 
        new IndexJ(  ), new LoadFilePG( "IndexJ log file", null, false ),
        new int[]{ 0, 1 } );
    OperatorForm scalarjform = new OperatorForm( 
        new ScalarJ(  ), new LoadFilePG( "ScalarJ log file", null, false ),
        new int[]{ 0 } );
    OperatorForm lsqrsjform  = new OperatorForm( 
        new LsqrsJ(  ), new LoadFilePG( "LsqrsJ log file", null, false ),
        new int[]{ 0 } );
    OperatorForm indexjform2 = new OperatorForm( 
        new IndexJ(  ), new LoadFilePG( "IndexJ log file", null, false ),
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
