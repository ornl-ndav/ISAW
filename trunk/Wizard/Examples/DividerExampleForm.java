/*
 * File:  DividerExampleForm.java
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
 * Revision 1.1  2002/02/27 17:31:39  dennis
 * Example Form to allow dividing some parameters.
 * (Used by MathWizard.java)
 *
 *
 */

package Wizard.Examples;

import java.io.*;
import Wizard.*;

/**
 *  This class defines a form for dividing two numbers under the control
 *  of a Wizard.
 */
public class DividerExampleForm extends    Form
                                implements Serializable
{
  /**
   *  Construct a DividerExampleForm to divide the parameters named in
   *  the list operands[] and place the result in the parameter named by
   *  result[0].  This constructor basically just calls the super class
   *  constructor and builds an appropriate help message for the form.
   *
   *  @param  operands  The list of names of parameters to be divided, in
   *                    this case only operands[0] and operands[1] are used.
   *  @param  result    The list of names of parameters to be calculated,
   *                    in this case only result[0] is used.
   *  @param  w         The wizard controlling this form.
   */
  public DividerExampleForm( String operands[], String result[], Wizard w )
  {
    super("Divide two Numbers", null, operands, result, w );

    String help = "This form let's you divide the numbers \n";
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
    float quotient = Float.NaN;
    WizardParameter param;

    param = wizard.getParameter( editable_params[1] );
    Float denom = (Float)param.getNewValue();
    if ( denom.floatValue() == 0 )
      quotient = Float.NaN;
    else
    { 
      param = wizard.getParameter( editable_params[0] );
      Float num = (Float)param.getNewValue();
      quotient = num.floatValue() / denom.floatValue();
    }

    param = wizard.getParameter( result_params[0] );
    param.setValue( new Float(quotient) );
    return true;
  } 

}
