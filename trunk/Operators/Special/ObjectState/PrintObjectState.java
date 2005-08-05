/* 
 * File: PrintObjectState.java
 *  
 * Copyright (C) 2005     Dominic Kramer
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
 * Contact :  Dominic Kramer<kramerd@uwstout.edu>
 *            Department of Mathematics, Statistics and Computer Science
 *            University of Wisconsin-Stout
 *            Menomonie, WI 54751, USA
 *
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2005/08/05 16:18:39  kramer
 * Initial checkin.  This is an operator that is used to get an ASCII
 * representation of an ObjectState.  It supports printing the keys in the
 * state or the keys and their values.  In addition, the way the values
 * are printed can be modified based on the parameters given to this
 * operator.
 *
 *
 */

package Operators.Special.ObjectState;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class PrintObjectState extends GenericOperator{
   public PrintObjectState(){
     super("Print an ObjectState");
     }

   public String getCommand(){
      return "PrintObjectState";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Enter the ObjectState to print",null));
      addParameter( new StringPG("Enter the value prefix","("));
      addParameter( new ChoiceListPG("Enter the value suffix",")"));
      addParameter( new BooleanPG("Print ObjectState values",new Boolean(true)));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("An ObjectState is a data structure that stores the state ");
      S.append("information for any object.  It can store anything from ");
      S.append("the way data is displayed to the values of certain ");
      S.append("parameters. ");
      S.append("<br><br>");
      S.append("ObjectStates can store any type of data, and ");
      S.append("particularly an ObjectState can store an ObjectState. ");
      S.append("This means an ObjectState has a hierarchial ");
      S.append("form.  This operator is used to print an ");
      S.append("ObjectState to a string to reflect this ");
      S.append("hierarchial form. ");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The ObjectState that is to be printed. ");
      S.append("@param   ");
      S.append("If the values in the ObjectState are specified ");
      S.append("to be printed, then for each value in the ");
      S.append("ObjectState, this is the string that is ");
      S.append("printed before printing the value. ");
      S.append("<br><br>");
      S.append("For example, suppose the ObjectState ");
      S.append("given to this operator contains a key ");
      S.append("\"key1\" that has a value \"value1\". ");
      S.append("Then suppose the value of this ");
      S.append("parameter is \"(\".  Also, suppose that ");
      S.append("the <i>suffix</i> is \")\".  Then, when ");
      S.append("the ObjectState is printed the value is ");
      S.append("printed as follows: ");
      S.append("<br><br>");
      S.append("key1 (value1) ");
      S.append("<br><br>");
      S.append("If the value of this parameter is \"\\n(\" then ");
      S.append("the ObjectState is printed as follows:  ");
      S.append("<br><br>");
      S.append("key1\n ");
      S.append("  (value1) ");
      S.append("<br><br>");
      S.append("Notice that the \"\\n\" causes the value to ");
      S.append("be printed on a new line.  Then, the ");
      S.append("value is automatically indented.  Then, ");
      S.append("the \"(\" is printed.  Finally, the value is ");
      S.append("printed. ");
      S.append("@param   ");
      S.append("If the values in the ObjectState are specified");
      S.append("to be printed, then for each value in the");
      S.append("ObjectState, this is the string that is");
      S.append("printed after printing the value.");
      S.append("@param   ");
      S.append("This is used to specify if the ObjectState's values ");
      S.append("should be printed or not.  If the values are not ");
      S.append("printed, then the printout just describes the ");
      S.append("layout of the ObjectState. ");
      S.append("@error ");
      S.append("");
      return S.toString();
   }


   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "MyMenu"
                     };
   }


   public Object getResult(){
      try{

         gov.anl.ipns.ViewTools.Components.ObjectState state = (gov.anl.ipns.ViewTools.Components.ObjectState)(getParameter(0).getValue());
         java.lang.String prefix = getParameter(1).getValue().toString();
         java.lang.String suffix = getParameter(2).getValue().toString();
         boolean printValues = ((BooleanPG)(getParameter(3))).getbooleanValue();
         java.lang.String Xres=Operators.Special.ObjectState.ObjectStateUtilities.PrintObjectState(state,prefix,suffix,printValues);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


