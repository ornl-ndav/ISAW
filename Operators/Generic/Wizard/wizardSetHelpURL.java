/* 
 * File: wizardSetHelpURL.java
 *  
 * Copyright (C) 2006     John Hammonds
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
 * Contact :  John Hammonds<JPHammonds@anl.gov>
 *            Intense Pulsed Neutron Source
 *            Bldg 360 L170
 *            9700 S. Cass Ave.
 *            Argonne, IL 60439
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * 
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2006/03/17 05:08:09  hammonds
 * New functionality for Wizards.
 *
 *
 */

package Operators.Generic.Wizard;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class wizardSetHelpURL extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public wizardSetHelpURL(){
     super("wizardSetHelpURL");
     }


   /**
    * Gives the user the command for the operator.
    *
	* @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "wizardSetHelpURL";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Wizard",null));
      addParameter( new StringPG("Help URL",""));
   }


   /**
    * Writes a string for the documentation of the operator provided by
    * the user.
    *
	* @return  The documentation for the operator.
    */
   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("This operator sets the help for the specified Wizard to a URL");
      S.append("@algorithm    "); 
      S.append("Uses the wizard setHelpURL method");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The Wizard which this URL is to be added to.");
      S.append("@param   ");
      S.append("The URL to be used for help");
      S.append("@error ");
      S.append("");
      return S.toString();
   }


   /**
    * Returns a string array with the category the operator is in.
    *
    * @return  An array containing the category the operator is in.
    */
   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "Utils",
                     "System",
                     "Wizard"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         DataSetTools.wizard.Wizard myWiz = (DataSetTools.wizard.Wizard)(getParameter(0).getValue());
         java.lang.String myUrl = getParameter(1).getValue().toString();
         Operators.Generic.Wizard.WizardMethods.wizardSetHelpURL(myWiz,myUrl);
         return "Success";
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


