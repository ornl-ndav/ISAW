/*
 * File:  MultiplierExampleForm.java
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
 * Revision 1.1  2002/05/28 20:35:14  pfpeterson
 * Moved files
 *
 * Revision 1.2  2002/03/12 16:10:34  pfpeterson
 * Updated to work better with disabling wizard feature.
 *
 * Revision 1.1  2002/02/27 17:32:00  dennis
 * Example Form to allow multiplying some parameters.
 * (Used by MathWizard.java)
 *
 *
 */

package Wizard;

import java.io.*;
import DataSetTools.wizard.*;

/**
 *  This class defines a form for multiplying a list of numbers under the 
 *  control of a Wizard.
 */
public class MultiplierExampleForm extends    Form
                                   implements Serializable
{

  /**
   *  Construct a MultiplierExampleForm to multiply the parameters named in
   *  the list operands[] and place the result in the parameter named by
   *  result[0].  This constructor basically just calls the super class
   *  constructor and builds an appropriate help message for the form.
   *
   *  @param  operands  The list of names of parameters to be multiplied.
   *  @param  result    The list of names of parameters to be calculated,
   *                    in this case only result[0] is used.
   *  @param  w         The wizard controlling this form.
   */
  public MultiplierExampleForm( String constants[], String operands[], String result[], Wizard w )
  {
    super("Multiply the Numbers", constants, operands, result, w );

    String help = "This form let's you multiply the numbers \n";
    for ( int i = 0 ; i < operands.length  ; i++ )
        help = help + "  " + operands[i]  + "\n";
    for ( int i = 0 ; i < constants.length ; i++ )
        help = help + "  " + constants[i] + "\n";
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
    float product = 1.0f;
    WizardParameter param;

    for ( int i = 0; i < editable_params.length; i++ ){
        param = wizard.getParameter( editable_params[i] );
        Float val = (Float)param.getNewValue();
        product *= val.floatValue();
    }
    for( int i = 0 ; i < const_params.length ; i++ ){
        param=wizard.getParameter( const_params[i] );
        Float val=(Float)param.getNewValue();
        product*=val.floatValue();
    }

    param = wizard.getParameter( result_params[0] );
    param.setValue( new Float(product) );
    return true;
  } 

}
