/*
 * File:  SubtracterExampleForm.java
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
 * Revision 1.1  2002/05/28 20:35:15  pfpeterson
 * Moved files
 *
 * Revision 1.1  2002/02/27 17:32:15  dennis
 * Example Form to allow dividing some parameters.
 * (Used by MathWizard.java)
 *
 *
 */

package Wizard;

import java.io.*;
import DataSetTools.wizard.*;

/**
 *  This class defines a form for subtracting two numbers under the control
 *  of a Wizard.
 */
public class SubtracterExampleForm extends    Form
                                   implements Serializable
{

  /**
   *  Construct a SubtracterExampleForm to subtract the parameters named in
   *  the list operands[] and place the result in the parameter named by
   *  result[0].  This constructor basically just calls the super class
   *  constructor and builds an appropriate help message for the form.
   *
   *  @param  operands  The list of names of parameters to be subtracted, in
   *                    this case only operands[0] and operands[1] are used.
   *  @param  result    The list of names of parameters to be calculated,
   *                    in this case only result[0] is used.
   *  @param  w         The wizard controlling this form.
   */
  public SubtracterExampleForm( String operands[], String result[], Wizard w )
  {
    super("Subtract two Numbers", null, operands, result, w );

    String help = "This form let's you subtract the numbers \n";
    for ( int i = 0; i < operands.length && i < 2; i++ )
      help = help + "  " + operands[i] + "\n";
    setHelpMessage( help );
  }

  /**
   *  This overrides the execute() method of the super class and provides
   *  the code that actually does the calculation.
   *
   *  @return This always returns true, though a more robust version might
   *          check that the values were valid numbers and only set the
   *          result value and return true in that case.
   */
  public boolean execute()
  {
    float difference;
    WizardParameter param;

    param = wizard.getParameter( editable_params[0] );
    Float val = (Float)param.getNewValue();
    difference = val.floatValue();

    param = wizard.getParameter( editable_params[1] );
    val = (Float)param.getNewValue();
    difference = difference - val.floatValue();
   
    param = wizard.getParameter( result_params[0] );
    param.setValue( new Float(difference) );
    return true;
  } 

}
