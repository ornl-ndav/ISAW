/* 
 * File: GetObjectState.java
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
 * Revision 1.1  2005/08/05 16:11:35  kramer
 * Initial checkin.  This is an operator that is used to get the state
 * (encapsulated in an ObjectState) of an IPreserveState object.
 *
 *
 */

package Operators.Special.ObjectState;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class GetObjectState extends GenericOperator{
   public GetObjectState(){
     super("Get an ObjectState");
     }

   public String getCommand(){
      return "GetObjectState";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Enter the object that encapsulates an ObjectState",null));
      addParameter( new BooleanPG("Load the default state",new Boolean(false)));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Various components in ISAW have the ability to remember ");
      S.append("their state.  This state is encapsulated in a data ");
      S.append("structure known as an ObjectState, and can include ");
      S.append("anything from the way data is displayed to the values of ");
      S.append("certain parameters.  This operator is used to get the ");
      S.append("state for a given component.  Either the current state ");
      S.append("of the component or the component's default state ");
      S.append("can be acquired.  ");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("This operator assumes that the object given has the ");
      S.append("ability to have its state encapsulated in an ObjectState.  ");
      S.append("@param   ");
      S.append("This is the object that is to have its state ");
      S.append("aquired after it is encapsulated in an ");
      S.append("ObjectState.  ");
      S.append("@param   ");
      S.append("Used to specify if the current state or the default ");
      S.append("state of the object given should be acquired.  ");
      S.append("If this parameter is \"true\" the default state is ");
      S.append("returned, and if it is \"false\" the current state is ");
      S.append("returned.  ");
      S.append("@return The ObjectState that encapsulates the state of the ");
      S.append("given object.  ");
      S.append("@error ");
      S.append("This operator will throw an error if the object ");
      S.append("given does not have the ability to ");
      S.append("encapsulate its state in an ObjectState.  ");
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

         gov.anl.ipns.ViewTools.Components.IPreserveState preserveOb = (gov.anl.ipns.ViewTools.Components.IPreserveState)(getParameter(0).getValue());
         boolean isDefault = ((BooleanPG)(getParameter(1))).getbooleanValue();
         gov.anl.ipns.ViewTools.Components.ObjectState Xres=Operators.Special.ObjectState.ObjectStateUtilities.GetObjectState(preserveOb,isDefault);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


