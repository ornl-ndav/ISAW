/*
 * File:  ScriptExampleWizard.java
 *
 * Copyright (C) 2003, Chris M. Bouzek
 *
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
 *           Chris M. Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * $Log$
 * Revision 1.6  2003/12/15 02:44:08  bouzekc
 * Removed unused imports.
 *
 * Revision 1.5  2003/11/05 02:20:29  bouzekc
 * Changed to work with new Wizard and Form design.
 *
 * Revision 1.4  2003/09/27 00:53:32  bouzekc
 * Main method now uses wizardLoader().  This is in keeping with the other
 * wizards.
 *
 * Revision 1.3  2003/07/09 23:16:52  bouzekc
 * Works with the new versions of ScriptForm and JyScriptForm.
 *
 * Revision 1.2  2003/07/08 18:10:30  bouzekc
 * Now uses SharedData.getProperty to get the Script_Path
 * directory.  Added comments.
 *
 * Revision 1.1  2003/07/02 17:11:27  bouzekc
 * Added to CVS.
 *
 */
package Wizard.Examples;

import DataSetTools.parameter.LoadFilePG;
import DataSetTools.wizard.ScriptForm;
import DataSetTools.wizard.Wizard;


/**
 * This class has a main program that constructs a Wizard to show how
 * ScriptForms can be created and have their result parameters linked
 * together.
 */
public class ScriptExampleWizard extends Wizard {
  //~ Constructors *************************************************************

  /**
   * Default constructor.  Sets standalone in Wizard to true.
   */
  public ScriptExampleWizard(  ) {
    this( true );
  }

  /**
   * Constructor for setting the standalone variable in Wizard.
   *
   * @param standalone Boolean indicating whether the Wizard stands alone
   *        (true) or is contained in something else (false).
   */
  public ScriptExampleWizard( boolean standalone ) {
    super( "Example Wizard for Script Forms", standalone );
    this.createAllForms(  );

    StringBuffer s = new StringBuffer(  );
    s.append( 
      "This class has a main program that constructs a Wizard to show\n" );
    s.append( "how ScriptForms can be created and have their parameters\n" );
    s.append( "linked together.\n" );
    this.setHelpMessage( s.toString(  ) );
  }

  //~ Methods ******************************************************************

  /**
   * Method for running the Script Example wizard as standalone.
   */
  public static void main( String[] args ) {
    ScriptExampleWizard w = new ScriptExampleWizard( true );
    w.wizardLoader( args );
  }

  /**
   * Adds and coordinates the necessary Forms for this Wizard.
   */
  private void createAllForms(  ) {
    //here is where we link all of our parameters.
    int[][] fpi          = {
      { 0, 0 },  //raw data path
      { 1, 1 },  //peaks file path
      { 2, 2 },  //run numbers
      { 3, 3 }
    };  //experiment name
    ScriptForm peaks     = new ScriptForm( 
        "/find_multiple_peaks.iss", new LoadFilePG( "Peaks File", null, false ) );
    ScriptForm integrate = new ScriptForm( 
        "/integrate_multiple_runs.iss",
        new LoadFilePG( "Integrated Peaks File", null, false ),
        new int[]{ 0, 1, 2, 3 } );
    this.addForm( peaks );
    this.addForm( integrate );

    //use Form's method to actually link the parameters
    super.linkFormParameters( fpi );
  }
}
