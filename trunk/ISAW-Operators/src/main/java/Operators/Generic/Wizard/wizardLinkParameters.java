/* 
 * File: wizardLinkParameters.java
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
public class wizardLinkParameters extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public wizardLinkParameters(){
     super("wizardLinkParameters");
     }


   /**
    * Gives the user the command for the operator.
    *
	* @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "wizardLinkParameters";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Wizard",null));
      addParameter( new ArrayPG("Link between forms",null));
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
      S.append("This operator creates links between parameters on Wizard forms");
      S.append("@algorithm    "); 
      S.append("Overall a 2D array is fed to the operator.  Each row of the array corresponds to one logical link chain.  The number of rows in the array is therefore equal to to the number of linked parameters.  Each row of the array should have the same number of elements as the number of forms that have been added to the Wizard.  Each element in the row specifies if a parameter on a given form should be linked.  If a parameter is linked with this chain then provide the parameter number for this element (numbering starts at 0).  If no parameters are linked in this sequence enter -1 into the element for that page.");
      S.append("@assumptions    "); 
      S.append("All forms to be linked have already been added to the wizard. <p>");
      S.append(" The first parameter on each form is parameter 0.<p>");
      S.append("@param   ");
      S.append("The wizard to add the links for.");
      S.append("@param   ");
      S.append("A 2D array holding the link map.");
      S.append("@error ");
      S.append("Throws Exceptions on checks for array dimension and checking for Integer data.");
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
         java.util.Vector myLinks = (java.util.Vector)(getParameter(1).getValue());
         Operators.Generic.Wizard.WizardMethods.wizardLinkParameters(myWiz,myLinks);
         return "Success";
      }catch(java.lang.Exception S0){
         return new ErrorString(S0.getMessage());
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


