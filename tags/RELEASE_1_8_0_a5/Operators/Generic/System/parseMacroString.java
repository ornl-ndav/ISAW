/* 
 * File: parseMacroString.java
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
 * Revision 1.1  2006/02/09 06:07:11  hammonds
 * New operator to substute macros in strings.
 *
 *
 */

package Operators.Generic.System;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class parseMacroString extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public parseMacroString(){
     super("parseMacroString");
     }


   /**
    * Gives the user the command for the operator.
    *
	* @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "parseMacroString";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("String to Parse","hello ${user.name}"));
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
      S.append("This operator will parse a string for macros that need to be substututed for values stored in the");
      S.append(" system properties.  Each macro starts with the pair ${ and is terminated with a }.  The string");
      S.append(" contained in this macro must correspond to a key in the System Properties.");
      S.append("@algorithm    "); 
      S.append("First look for $.  Next character must be {.  Look for }.  Extract & trim string between these");
      S.append(" delimiters.  Use this string in System.getProperties(macroString).   Delete macro from the original");
      S.append(" String and replace this with the value stored in the property.");
      S.append("@assumptions    "); 
      S.append("Once a macro is started with a $ it must be completed or a Parse Exeption is thrown.");
      S.append(" The macro must correspond with a key in the system properties.");
      S.append(" No Space is allowed between $ & {.  i.e. should be ${ not $ {.");
      S.append(" Excess space surrounding the macro string is trimmed.  i.e. ${user.home} is equivalent to");
      S.append(" ${ user.home }");
      S.append("@param   ");
      S.append("String to be parsed");
      S.append("@error ");
      S.append("A ParseException is thrown if macro is started and not completed properly or if the macro does not");
      S.append(" correspond to a key in the SystemProperties.");
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
                     "System"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String inStr = getParameter(0).getValue().toString();
         java.lang.String Xres=Operators.Generic.System.ParseStringMacroBase.parseMacroString(inStr);
         return Xres;
       }catch(java.text.ParseException S0){
         return new ErrorString(S0.getMessage());
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


