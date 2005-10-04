/* 
 * File: IntListToVector.java
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
 * Revision 1.1  2005/10/04 23:01:32  dennis
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
public class IntListToVector extends GenericOperator implements HiddenOperator
{
   public IntListToVector(){
     super("IntListToVector");
     }

   public String getCommand(){
      return "IntListToVector";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("List of Integer","-1"));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Convert a specially formatted String, or an int array to a");
      S.append("Vector of integers, for use by the ISAW scripting system.");
      S.append("@algorithm    "); 
      S.append("If the input list is an array of integers, they will be sorted and duplicates");
      S.append("will be removed when the values are placed in the Vector.");
      S.append("If the input list is described by a string, the method IntList.ToArray(str)");
      S.append("is used to obtain the increasing sequence of integers.");
      S.append("@assumptions    "); 
      S.append("The input object must be an array of integers, or a String specifying an");
      S.append("increasing sequence of integers, in the form \"1:3,5,10:15\".");
      S.append("@param   ");
      S.append("A String describing an increasing sequence of");
      S.append("integers, in the form \"3:5,10,15,20:25\", or an array of integers");
      S.append("@return A Vector containing the increasing sequence of Integer objects,");
      S.append("");
      S.append("specified by in input list.  If the String description of the");
      S.append("list of integers is improperly formatted, the values may not");
      S.append("be copied to the Vector completely, or at all.");
      S.append("@error ");
      S.append("The only error that is detected at this time is passing in null, or an Object");
      S.append("that is not a String or an array of ints");
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

         java.lang.Object list = (java.lang.Object)(getParameter(0).getValue());
         java.lang.Object Xres=Operators.Generic.TypeConversion.Convert.IntListToVector(list);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


