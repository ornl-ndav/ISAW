/* 
 * File: getSysProps.java
 *  
 * Copyright (C) 2005     John Hammonds

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
 *            9700 S. Cass Ave., Argonne, IL 60439
 *
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2005/06/14 13:24:47  hammonds
 * New operator to get value from a system property.
 *

 */

package Operators.Generic.System;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class getSysProps extends GenericOperator{
   public getSysProps(){
     super("getSysProp");
     }

   public String getCommand(){
      return "getSysProp";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new StringPG("Enter System Property","0"));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("This method returns the value (as a string) of a system property.");
      S.append("@algorithm    "); 
      S.append("This method simply passed the input string to System.getProperty.");
      S.append("@assumptions    "); 
      S.append("None");
      S.append("@param   ");
      S.append("The name of the property to be retrieved.");
      S.append("@return Returns the value of the specified property.");
      return S.toString();
   }


   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "Utils",
                     "System"
                     };
   }


   public Object getResult(){
      try{

         java.lang.String prop = getParameter(0).getValue().toString();
         java.lang.String Xres=Operators.Generic.System.getSystemProperty.getSysProp(prop);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


