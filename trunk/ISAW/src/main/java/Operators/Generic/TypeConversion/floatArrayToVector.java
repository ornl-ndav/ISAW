/* 
 * File: floatArrayToVector.java
 *  
 * Copyright (C) 2005     Dennis Mikkelson
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
 * Contact :  Dennis Mikkelson<mikkelsond@uwstout.edu>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2005/10/05 02:49:34  dennis
 * Converted from dos to unix text.
 *
 * Revision 1.1  2005/10/04 23:01:34  dennis
 * Operator generated around method in
 * Operators/Generic/TypeConversions/Convert class.
 *
 *
 */

package Operators.Generic.TypeConversion;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class floatArrayToVector extends    GenericOperator
                                implements HiddenOperator
{
   public floatArrayToVector(){
     super("floatArrayToVector");
     }

   public String getCommand(){
      return "floatArrayToVector";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("array of floats",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Convert the floats in an array of floats to a vector of");
      S.append("Float objects");
      S.append("@algorithm    "); 
      S.append("The floats in the array of floats are used to construct Float");
      S.append("objects, which are then added to the Vector in the same");
      S.append("order they appeared in the array.");
      S.append("@assumptions    "); 
      S.append("The array of floats is not null.");
      S.append("@param   ");
      S.append("An array of floats");
      S.append("@return A Vector of Integer objects, created from the");
      S.append("");
      S.append("array of floats, in the SAME order.");
      S.append("@error ");
      S.append("If the input object is NOT an array of floats,");
      S.append("an ErrorString is returned.");
      return S.toString();
   }


   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "Utils",
                     "Convert"
                     };
   }


   public Object getResult(){
      try{

         java.lang.Object obj = (java.lang.Object)(getParameter(0).getValue());
         java.lang.Object Xres=Operators.Generic.TypeConversion.Convert.floatArrayToVector(obj);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


