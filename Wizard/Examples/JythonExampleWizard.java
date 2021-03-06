/*
 * File:  JythonExampleWizard.java
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
 * Revision 1.8  2006/07/10 16:26:13  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.7  2003/12/15 02:17:30  bouzekc
 * Removed unused imports.
 *
 * Revision 1.6  2003/11/05 02:20:29  bouzekc
 * Changed to work with new Wizard and Form design.
 *
 * Revision 1.5  2003/08/07 14:22:58  bouzekc
 * Now uses wizardLoader() rather than show().
 *
 * Revision 1.4  2003/07/09 23:16:51  bouzekc
 * Works with the new versions of ScriptForm and JyScriptForm.
 *
 * Revision 1.3  2003/07/09 21:51:02  rmikk
 * Fixed parameter linking error
 *
 * Revision 1.2  2003/07/08 20:57:01  bouzekc
 * Added missing slash to file name.
 *
 * Revision 1.1  2003/07/08 18:11:14  bouzekc
 * Added to CVS.
 *
 *
 */
package Wizard.Examples;

import gov.anl.ipns.Parameters.LoadFilePG;
import DataSetTools.wizard.JyScriptForm;
import DataSetTools.wizard.Wizard;


/**
 * This class has a main program that constructs a Wizard to show how
 * JyScriptForms (Script Forms created using Jython scripts)  can be  created
 * and have their result parameters linked together.
 */
public class JythonExampleWizard extends Wizard {
  //~ Constructors *************************************************************

  /**
   * Default constructor.  Sets standalone in Wizard to true.
   */
  public JythonExampleWizard(  ) {
    this( true );
  }

  /**
   * Constructor for setting the standalone variable in Wizard.
   *
   * @param standalone Boolean indicating whether the Wizard stands alone
   *        (true) or is contained in something else (false).
   */
  public JythonExampleWizard( boolean standalone ) {
    super( "Example Wizard for Jython Script Forms", standalone );
    this.createAllForms(  );

    StringBuffer s = new StringBuffer(  );
    s.append( "This class has a main program that constructs a Wizard to \n" );
    s.append( "show how JyScriptForms can be created and have their \n" );
    s.append( "parameters linked together.\n" );
    this.setHelpMessage( s.toString(  ) );
  }

  //~ Methods ******************************************************************

  /**
   * Method for running the Jython Example wizard as standalone.
   */
  public static void main( String[] args ) {
    JythonExampleWizard w = new JythonExampleWizard( true );
    w.wizardLoader( args );
  }

  /**
   * Adds and coordinates the necessary Forms for this Wizard.
   */
  private void createAllForms(  ) {
    //here is where we link all of our parameters.
    int[][] fpi            = {
      { 0, 0 },  //raw data path
      { 1, 1 },  //peaks file path
      { 2, 2 },  //run numbers
      { 3, 3 },  //experiment name
      { 7, 5 }
    };  //SCD calibration file
    JyScriptForm peaks     = new JyScriptForm( 
        "find_multiple_peaks2.py", new LoadFilePG( "Peaks File", null ) );
    JyScriptForm integrate = new JyScriptForm( 
        "integrate_multiple_runs2.py",
        new LoadFilePG( "Integrated Peaks File", null ),
        new int[]{ 0, 1, 2, 3, 5 } );
    this.addForm( peaks );
    this.addForm( integrate );

    //use Form's method to actually link the parameters
    super.linkFormParameters( fpi );
  }
}
