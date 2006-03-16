/* 
 * File: VectorToIntListString.java
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
 * Revision 1.1  2005/10/04 23:01:33  dennis
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
public class VectorToIntListString extends    GenericOperator
                                   implements HiddenOperator
{
   public VectorToIntListString(){
     super("VectorToIntListString");
     }

   public String getCommand(){
      return "VectorToIntListString";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Vector of numbers",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Convert a list of numeric values in a Vector to a String, specifying");
      S.append("an increasing sequence of integers.  NOTE: Floats or Doubles will");
      S.append("be rounded to the nearest integer.  Also, the resulting list of");
      S.append("integers will be sorted, duplicates removed and converted to a String");
      S.append("representation like  3:6,18,20:25");
      S.append("@algorithm    "); 
      S.append("The Float, Integer or Double objects in the vector are rounded and");
      S.append("copied into an array of ints.  The array of ints is then sorted to be");
      S.append("in increasing order and duplicates are removed.  Finally, the");
      S.append("increasing sequence of ints is converted to a String form.");
      S.append("@assumptions    "); 
      S.append("All of the entries in the Vector must be numeric.  Specifically, they");
      S.append("must be Integer, Float or Double");
      S.append("@param   ");
      S.append("A Vector containing numeric values.");
      S.append("@return A String specifying an increasing sequence of integers, or an");
      S.append("");
      S.append("error string if the input parameter is not a Vector containing");
      S.append("numeric values");
      S.append("@error ");
      S.append("If there are non-numeric values in the Vector, an ErrorString");
      S.append("will be returned.");
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
         java.lang.Object Xres=Operators.Generic.TypeConversion.Convert.VectorToIntListString(obj);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


