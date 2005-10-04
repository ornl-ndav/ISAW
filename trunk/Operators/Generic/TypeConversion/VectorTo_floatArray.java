/* 
 * File: VectorTo_floatArray.java
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
public class VectorTo_floatArray extends    GenericOperator
                                 implements HiddenOperator
{
   public VectorTo_floatArray(){
     super("VectorTo_floatArray");
     }

   public String getCommand(){
      return "VectorTo_floatArray";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Vector of numbers",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Convert a list of numeric values in a Vector to an array");
      S.append("of floats, in the same order the values were stored in the vector.");
      S.append("@algorithm    "); 
      S.append("The elements of the Vector are converted to type float");
      S.append("and stored in the array of floats, in the same order as");
      S.append("they were in the Vector.");
      S.append("@assumptions    "); 
      S.append("The Vector contains only numeric objects of type Integer,");
      S.append("Float or Double");
      S.append("@param   ");
      S.append("A Vector containing numeric values.");
      S.append("@error ");
      S.append("If there are non-numeric values in the Vector, an");
      S.append("ErrorString will be returned.");
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
         java.lang.Object Xres=Operators.Generic.TypeConversion.Convert.VectorTo_floatArray(obj);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}
