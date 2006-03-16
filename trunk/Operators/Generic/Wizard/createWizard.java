/* 
 * File: createWizard.java
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
 * Revision 1.1  2006/03/16 03:59:10  hammonds
 * Add the capability to create operators from a script.
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
public class createWizard extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public createWizard(){
     super("createWizard");
     }


   /**
    * Gives the user the command for the operator.
    *
	* @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "createWizard";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new ChoiceListPG("Wizard Name",null));
      addParameter( new BooleanPG("Standalone",false));
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
      S.append("This Operator creates an instance of a Wizard.  Combined with other wizard methods this allows a new Wizard to be created from a script.");
      S.append("@algorithm    "); 
      S.append("Calls the Wizard constructor and returns an instance of a wizard.");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The name of the wizard to be created.");
      S.append("@param   ");
      S.append("Will this Wizard be created standalone.  Normally this will be false.");
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

         java.lang.String wizName = getParameter(0).getValue().toString();
         boolean standalone = ((BooleanPG)(getParameter(1))).getbooleanValue();
         DataSetTools.wizard.Wizard Xres=Operators.Generic.Wizard.WizardMethods.createWizard(wizName,standalone);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


