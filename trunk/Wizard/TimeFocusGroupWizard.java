/*
 * File:  TimeFocusGroupWizard.java
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
 * Modified:
 *
 * $Log$
 * Revision 1.14  2003/07/03 14:43:40  bouzekc
 * Fixed odd CVS log entries due to double inclusion of the
 * log header tag.
 *
 * Revision 1.13  2003/06/30 16:17:10  bouzekc
 * Now takes --nogui command line arguments.
 *
 * Revision 1.12  2003/06/25 20:24:45  bouzekc
 * Unused private variables removed, reformatted for
 * consistency.
 *
 * Revision 1.11  2003/06/19 16:19:59  bouzekc
 * Now uses Wizard's linkFormParameters() to link the
 * parameters in the parameter table.
 *
 * Revision 1.10  2003/06/02 22:26:05  bouzekc
 * Fixed contact information.
 *
 * Revision 1.9   2003/04/29 15:45:37  pfpeterson
 * Updated code which links parameters between forms. (Chris Bouzek)
 *
 * Revision 1.8   2003/04/24 19:00:58  pfpeterson
 * Various small bug fixes. (Chris Bouzek)
 *
 * Revision 1.7   2003/04/02 15:02:46  pfpeterson
 * Changed to reflect new heritage (Forms are Operators). (Chris Bouzek)
 *
 * Revision 1.6   2003/03/19 23:07:37  pfpeterson
 * Expanded TimeFocusGroupForm to allow for up to 20 'banks' to be focused 
 * and grouped. (Chris Bouzek)
 *
 * Revision 1.5   2003/03/19 15:08:33  pfpeterson
 * Uses the TimeFocusGroupForm rather than TimeFocusForm and GroupingForm.
 * (Chris Bouzek)
 *
 * Revision 1.4   2003/03/13 19:00:52  dennis
 * Added log header to include revision information.
 *
 * Revision 1.3   2003/03/11 19:49:52  pfpeterson
 * Chris Bouzek's next version of the wizard.
 *
 * Revision 1.2   2003/03/03 13:38:17  dennis 
 * Added more error checking (Chris Bouzek).
 *
 * Revision 1.1   2003/02/26 20:22:42  pfpeterson
 * Added to CVS (Chris Bouzek).
 */
package Wizard;

import DataSetTools.parameter.IParameterGUI;

import DataSetTools.wizard.*;

import java.util.Vector;

import javax.swing.*;


/**
 *  This class has a main program that constructs a Wizard for time
 *  focusing and grouping spectra in a DataSet.
 */
public class TimeFocusGroupWizard extends Wizard {
  /**
   *
   *  Default constructor.  Sets standalone in Wizard to true.
   */
  public TimeFocusGroupWizard(  ) {
    this( true );
  }

  /**
   *  Constructor for setting the standalone variable in Wizard.
   *
   *  @param standalone          Boolean indicating whether the
   *                             Wizard stands alone (true) or
   *                             is contained in something else
   *                             (false).
   */
  public TimeFocusGroupWizard( boolean standalone ) {
    super( "Time Focus and Group Wizard", standalone );
    this.createAllForms(  );
  }

  /**
   *  Adds and coordinates the necessary Forms for this Wizard.
   *
   *  The referential links are arranged in a tabular format.
   *  At some point, the Wizard base class will be automating the
   *  links, so please follow this format.  This particular wizard follows
   *  this format.
   *
   *  Note that:
   *  LoadMultiHistogramsForm = lmhf
   *  TimeFocusGroupForm = tfgf
   *  SaveAsGSASForm = sagf
   *
   *    lmhf    tfgf    sagf
   *  |----------------------|
   *  |   5   |   0   | -1   |
   *  |----------------------|
   *  |  -1   |  61   |  0   |
   *  |----------------------|
   *  |   6   |  -1   |  1   |
   *  |----------------------|
   *  |   0   |  -1   |  2   |
   *  |----------------------|
   *  |   2   |  -1   |  3   |
   *  |----------------------|
   *
   *  The indices are accessed in the following manner, using [x][y]:
   *  x = row, y = col
   *  You must place the actual parameter number within the integer array.
   *  For example, to set the link between LoadMultiHistogramForm's 5th
   *  parameter into TimeFocusGroupForm's 0th parameter, use the following:
   *  (assuming fpi has already been declared as a two-dimensional integer
   *  array of sufficient size):
   *
   *  fpi[0][0] = 5;
   *  fpi[0][1] = 0;
   *
   *  Alternately, you may create the entire table  using Java's array
   *  initialization scheme, as shown:
   *
   *   int fpi[][] = { {5,0,-1}, {-1,61,0}, {6,-1,1},{0,-1,2},{2,-1,3} };
   */
  private void createAllForms(  ) {
    int[][] fpi = {
      { 5, 0, -1 },
      { -1, 61, 0 },
      { 6, -1, 1 },
      { 0, -1, 2 },
      { 2, -1, 3 }
    };

    LoadMultiHistogramsForm lmhf = new LoadMultiHistogramsForm(  );
    TimeFocusGroupForm tfgf      = new TimeFocusGroupForm(  );
    SaveAsGSASForm sagf          = new SaveAsGSASForm(  );

    this.addForm( lmhf );
    this.addForm( tfgf );
    this.addForm( sagf );

    super.linkFormParameters( fpi );
  }

  /**
   *  Method for running the Time Focus Group wizard
   *   as standalone.
   */
  public static void main( String[] args ) {
    TimeFocusGroupWizard w = new TimeFocusGroupWizard( true );

    //specified a --nogui switch but forgot to give a filename
    if( args.length == 1 ) {
      System.out.println( 
        "USAGE: java Wizard.TimeFocusGroupWizard " +
        "[--nogui] <Wizard Save File>" );
    } else if( args.length == 2 ) {
      w.executeNoGUI( args[1] );
    } else {
      w.showForm( 0 );
    }
  }
}
