/* 
 * File: CreateEmptyState.java
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
 * Revision 1.1  2005/08/05 16:10:16  kramer
 * Initial checkin.  This is an operator that is used to create a new empty
 * ObjectState.  As a result, scripts can make empty ObjectStates to use.
 *
 *
 */

package Operators.Special.ObjectState;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class CreateEmptyState extends GenericOperator{
   public CreateEmptyState(){
     super("Create an Empty ObjectState");
     }

   public String getCommand(){
      return "CreateEmptyState";
   }

   public void setDefaultParameters(){
      clearParametersVector();
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Various components in ISAW have the ability to store ");
      S.append("their state in a data structure known as an ");
      S.append("ObjectState.  This state can contain anything from ");
      S.append("the way data is displayed to the values of ");
      S.append("certain parameters.  This operator is used to ");
      S.append("create a new empty ObjectState.  ");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@return A new empty ObjectState that can be used ");
      S.append("to encapsulate state information.  ");
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

         gov.anl.ipns.ViewTools.Components.ObjectState Xres=Operators.Special.ObjectState.ObjectStateUtilities.CreateEmptyState();
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


