/*
 * File:  AdderExampleForm.java
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2002/11/27 23:31:16  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/06/06 16:21:35  pfpeterson
 * Now use new parameters.
 *
 * Revision 1.1  2002/05/28 20:35:10  pfpeterson
 * Moved files
 *
 * Revision 1.1  2002/02/27 17:31:22  dennis
 * Example Form to allow adding some parameters.
 * (Used by MathWizard.java)
 *
 *
 */

package Wizard;

import java.io.*;
import DataSetTools.wizard.*;
import DataSetTools.parameter.*;

/**
 *  This class defines a form for adding a list of numbers under the control
 *  of a Wizard.
 */
public class AdderExampleForm extends    Form
                              implements Serializable
{
  /**
   *  Construct an AdderExampleForm to add the parameters named in 
   *  the list operands[] and place the result in the parameter named by
   *  result[0].  This constructor basically just calls the super class
   *  constructor and builds an appropriate help message for the form.
   *
   *  @param  operands  The list of names of parameters to be added.
   *  @param  result    The list of names of parameters to be calculated,
   *                    in this case only result[0] is used.
   *  @param  w         The wizard controlling this form. 
   */
  public AdderExampleForm( String operands[], String result[], Wizard w )
  {
    super("Add List of Numbers", null, operands, result, w );

    String help = "This form let's you add the numbers \n";
    for ( int i = 0; i < operands.length; i++ )
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
    FloatPG param;

    float sum = 0.0f;
    for ( int i = 0; i < editable_params.length; i++ )
    {
      param = (FloatPG)wizard.getParameter( editable_params[i] );
      sum += param.getfloatValue();
      param.setValid(true);
    } 

    param = (FloatPG)wizard.getParameter( result_params[0] );
    param.setfloatValue( sum );
    return true;
  } 

}
