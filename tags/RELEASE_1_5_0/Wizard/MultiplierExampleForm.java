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
 * Revision 1.4  2003/04/02 15:02:46  pfpeterson
 * Changed to reflect new heritage (Forms are Operators). (Chris Bouzek)
 *
 * Revision 1.3  2002/11/27 23:31:16  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/06/06 16:21:38  pfpeterson
 * Now use new parameters.
 *
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
import DataSetTools.parameter.*;

/**
 *  This class defines a form for multiplying a list of numbers under the 
 *  control of a Wizard.
 */
public class MultiplierExampleForm extends    Form
                                   implements Serializable
{

  /**
   *  Default constructor.  Creates an AdderExampleForm and calls
   *  setDefaultParameters.
   */
  public MultiplierExampleForm( )
  {
    super("Multiplier example Form");
    this.setDefaultParameters();
  }

  /**
   *
   *  Attempts to set reasonable default parameters for this form.
   */
  public void setDefaultParameters()
  {
    addParameter(new FloatPG("Value 1", new Float(1), false));
    addParameter(new FloatPG("Value 2", new Float(2), false));
    addParameter(new FloatPG("Value 3", new Float(3), false));
    addParameter(new FloatPG("Result 1", new Float(1), false));
    addParameter(new FloatPG("Result 2", new Float(3), false));
    addParameter(new FloatPG("Result 3", new Float(3), false));
    setParamTypes(new int[]{0,1,2,3}, null, new int[]{4,5});
  }

  /**
   *  Returns the String command used for invoking this
   *  Form in a Script.
   */
  public String getCommand()
  {
    return "MULTIPLIEREXAMPLEFORM";
  }

  /**
   *
   *  Subtracts an array of floats.
   *  
   *  @return A Boolean indicating success or failure.  This
   *  Form is "dumb", i.e. it only returns true.
   */
  public Object getResult()
  {
    float product = 1.0f;
    FloatPG param;
    int[] const_indices = super.getParamType(CONST_PARAM);
    for( int i = 0 ; i < const_indices.length ; i++ ){
        param=(FloatPG)super.getParameter( i );
        product *= param.getfloatValue();
        param.setValid(true);
    }

    param = (FloatPG)super.getParameter( const_indices.length );
    param.setfloatValue( product );
    return new Boolean(true);
  } 

}
