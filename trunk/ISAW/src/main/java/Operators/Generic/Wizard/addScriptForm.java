/* 
 * File: addScriptForm.java
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
public class addScriptForm extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public addScriptForm(){
     super("addScriptForm");
     }


   /**
    * Gives the user the command for the operator.
    *
	* @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "addScriptForm";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Wizard",null));
      addParameter( new StringPG("Script Name","myScript"));
      addParameter( new StringPG("Return Type","myParameterType"));
      addParameter( new StringPG("Return value name","myReturn"));
      addParameter( new PlaceHolderPG("Return Default Value",null));
      addParameter( new IntegerArrayPG("Parameters to keep constant","[0]"));
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
      S.append("Add a script as a form to a Wizard.");
      S.append("@algorithm    "); 
      S.append("Use Wizard's addForm method.");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The Wizard to add a form to.");
      S.append("@param   ");
      S.append("The name of the script to add as a form.");
      S.append("@param   ");
      S.append("The return type for this form.  This is a string specifying the type of parameter GUI to use.");
      S.append("@param   ");
      S.append("The name assosciated with the return value.");
      S.append("@param   ");
      S.append("The default value for the return.");
      S.append("@param   ");
      S.append("Which parameters of this form should be kept constant.");
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
         java.lang.String scriptName = getParameter(1).getValue().toString();
         java.lang.String retType = getParameter(2).getValue().toString();
         java.lang.String retName = getParameter(3).getValue().toString();
         java.lang.Object retDef = (java.lang.Object)(getParameter(4).getValue());
         java.util.Vector constParams = (java.util.Vector)(getParameter(5).getValue());
         Operators.Generic.Wizard.WizardMethods.addScriptForm(myWiz,scriptName,retType,retName,retDef,constParams);
         return "Success";
      }catch(java.lang.ClassNotFoundException S0){
    	  S0.printStackTrace();
    	  return new ErrorString(S0.getMessage());
      }catch(java.lang.InstantiationException S1){
         return new ErrorString(S1.getMessage());
      }catch(java.lang.IllegalAccessException S2){
         return new ErrorString(S2.getMessage());
      }catch(java.lang.NoSuchMethodException S3){
         return new ErrorString(S3.getMessage());
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


