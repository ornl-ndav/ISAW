/*
 * File:  MathWizard.java
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2002/02/27 17:33:02  dennis
 * Example Wizard that controls four forms for doing
 * +,-,*,/ operations.
 *
 *
 */

package Wizard.Examples;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import Wizard.*;
import DataSetTools.util.*;
import DataSetTools.operator.*;


/**
 *  This class has a main program that constructs a Wizard for doing add,
 *  subtract, multiply and divide operations on a specified list of parameters.
 */
public class MathWizard
{

  /**
   *  The main program constructs a new Wizard, defines the parameters to
   *  be stored in the master parameter list, and constructs instances of
   *  of the forms that define the operations available.
   */
  public static void main( String args[] )
  {
                                                      // build the wizard and
                                                      // specify the help 
                                                      // messages.
    Wizard w = new Wizard( "Math Wizard" ); 
    Wizard.status_display.append("MathWizard Main\n");
    w.setHelpMessage("This wizard will let you do arithetic operations");
    w.setAboutMessage("This is a simple Demonstation Wizard, 2/26/2002, D.M.");

                                                      // define the entries in
                                                      // in the master list
    w.setParameter( "Value 1", 
                     new WizardParameter( "Enter Value 1",new Float(1), false));
    w.setParameter( "Value 2", 
                     new WizardParameter( "Enter Value 2",new Float(2), false));
    w.setParameter( "Value 3", 
                     new WizardParameter( "Enter Value 3",new Float(3), false));
    w.setParameter( "Result 1", 
                     new WizardParameter( "Result 1",new Float(0), false ));
    w.setParameter( "Result 2", 
                     new WizardParameter( "Result 2",new Float(0), false ));
    w.setParameter( "Result 3", 
                     new WizardParameter( "Result 3",new Float(0), false ));
    w.setParameter( "Result 4", 
                     new WizardParameter( "Result 4",new Float(0), false ));

                                                    // Specifiy the parameters
                                                    // used by the forms and
                                                    // add the forms to the
                                                    // Wizard
    String edit_parms[] = { "Value 1", "Value 2", "Value 3" };
    String out_parms[]  = {"Result 1"};
    Form form0 = new AdderExampleForm(  edit_parms, out_parms, w );
    w.add( form0 );

    String edit_parms_1[] = { "Value 1", "Value 2" };
    String out_parms_1[]  = {"Result 2"}; 
    Form form1 = new SubtracterExampleForm(  edit_parms_1, out_parms_1, w );
    w.add( form1 );

    String edit_parms_2[] = { "Value 1", "Value 2", "Value 3", 
                              "Result 1", "Result 2" };
    String out_parms_2[]  = {"Result 3"};
    Form form2 = new MultiplierExampleForm(  edit_parms_2, out_parms_2, w );
    w.add( form2 );

    String edit_parms_3[] = { "Result 2", "Value 2" };
    String out_parms_3[]  = {"Result 4"};
    Form form3 = new DividerExampleForm(  edit_parms_3, out_parms_3, w );
    w.add( form3 );

    w.show(0);
  }
}
