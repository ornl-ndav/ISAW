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
 * Revision 1.6  2003/05/20 20:57:43  pfpeterson
 * Fixed imports.
 *
 * Revision 1.5  2003/05/20 20:49:58  dennis
 * Now constructs a new parameters vector in setDefaultParameters.
 * (Ruth Mikkelson)
 *
 * Revision 1.4  2003/04/02 15:02:46  pfpeterson
 * Changed to reflect new heritage (Forms are Operators). (Chris Bouzek)
 *
 * Revision 1.3  2002/11/27 23:31:16  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/06/06 16:21:36  pfpeterson
 * Now use new parameters.
 *
 * Revision 1.1  2002/05/28 20:35:11  pfpeterson
 * Moved files
 *
 * Revision 1.2  2002/03/12 16:10:32  pfpeterson
 * Updated to work better with disabling wizard feature.
 *
 * Revision 1.1  2002/02/27 17:31:39  dennis
 * Example Form to allow dividing some parameters.
 * (Used by MathWizard.java)
 *
 *
 */

package Wizard;

import java.io.*;
import java.util.Vector;
import DataSetTools.wizard.*;
import DataSetTools.parameter.*;

/**
 *  This class defines a form for dividing two numbers under the control
 *  of a Wizard.
 */
public class DividerExampleForm extends    Form
                                implements Serializable
{
  /**
   *  Default constructor.  Creates an AdderExampleForm and calls
   *  setDefaultParameters.
   */
  public DividerExampleForm( )
  {
    super("Divider example Form");
    this.setDefaultParameters();
  }

  /**
   *
   *  Attempts to set reasonable default parameters for this form.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();

    addParameter(new FloatPG("Value 2", new Float(2), false));
    addParameter(new FloatPG("Result 2", new Float(0), false));
    addParameter(new FloatPG("Result 4", new Float(0), false));
    setParamTypes(new int[]{0,1}, null, new int[]{2});
  }

  /**
   *  Returns the String command used for invoking this
   *  Form in a Script.
   */
  public String getCommand()
  {
    return "DIVIDEREXAMPLEFORM";
  }

  /**
   *  This overrides the execute() method of the super class and provides
   *  the code that actually does the calculation.
   *
   *  @return This always returns true, though a more robust version might
   *          check that the values were valid numbers and only set the
   *          result value and return true in that case.
   */
  public Object getResult()
  {
    float quotient = Float.NaN;
    FloatPG param;

    param = (FloatPG)super.getParameter(0);
    float denom = param.getfloatValue();
    if ( denom == 0 ){
      quotient = Float.NaN;
    }else{
      param = (FloatPG)super.getParameter(1);
      float num = param.getfloatValue();
      quotient = num / denom;
    }

    param = (FloatPG)super.getParameter(2);
    param.setfloatValue( quotient );
    return new Boolean(true);
  } 

}
