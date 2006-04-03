/* 
 * File: SubString.java
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
 * Revision 1.1  2006/04/03 18:53:56  hammonds
 * New methods to add to scripting language.
 *
 *
 */

package Operators.Generic.Text;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class SubString extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public SubString(){
     super("SubString");
     }


   /**
    * Gives the user the command for the operator.
    *
	* @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "SubString";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new StringPG("Input String","Search This"));
      addParameter( new IntegerPG("Beginning Index",0));
      addParameter( new IntegerPG("Ending Index",5));
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
      S.append("Return a substring of the input string.  The substring starts at the specified start index and extends to the character at just before the ending index.");
      S.append("@algorithm    "); 
      S.append("Uses Java's String.substring method.");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The original string.");
      S.append("@param   ");
      S.append("The starting index for the string.  The returned string includes the character at this index.");
      S.append("@param   ");
      S.append("The ending index for the substring.  Since the returned substring only goes up to endIndx -1, the character at this index is not included in the returned substring.");
      S.append("@error ");
      S.append("An IndexOutOfBoundsException is thrown by the underlying Java methods if the beginning index is negative, or the ending index is larger then the lenght of the string or if the beginning index is larger than the ending index.");
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
                     "Text"
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
         int begIndx = ((IntegerPG)(getParameter(1))).getintValue();
         int endIndx = ((IntegerPG)(getParameter(2))).getintValue();
         java.lang.String Xres=Operators.Generic.Text.StringMethods.SubString(inStr,begIndx,endIndx )
;
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


