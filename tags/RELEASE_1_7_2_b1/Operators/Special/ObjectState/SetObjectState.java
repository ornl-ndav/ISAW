/* 
 * File: SetObjectState.java
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
 * Revision 1.1  2005/08/05 16:20:54  kramer
 * Initial checkin.  This is an operator that is used to set an
 * ObjectState on a particular IPreserveState object.
 *
 *
 */

package Operators.Special.ObjectState;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class SetObjectState extends GenericOperator{
   public SetObjectState(){
     super("Set an ObjectState");
     }

   public String getCommand(){
      return "SetObjectState";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Enter the object that will accept the ObjectState",null));
      addParameter( new PlaceHolderPG("Enter the ObjectState",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Various components in ISAW have the ability to remember ");
      S.append("their state.  This state is encapsulated in a data ");
      S.append("structure known as an ObjectState, and can include ");
      S.append("anything from the way data is displayed to the values of ");
      S.append("certain parameters.  This operator is used to set the ");
      S.append("state on a given component.  ");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("This operator assumes that the object given to it has ");
      S.append("the ability to have its state set. ");
      S.append("@param   ");
      S.append("The object that is supposed to have its state set.  ");
      S.append("@param   ");
      S.append("The ObjectState that encapsulates the state ");
      S.append("that is to be set on the given object.  ");
      S.append("@return The string \"Success\" if the state was successfully set ");
      S.append("");
      S.append("and an error if the state cannot be saved.  ");
      S.append("@error ");
      S.append("This operator will throw an error if the object ");
      S.append("given to it does not have the ability to have ");
      S.append("its state set.  ");
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
         gov.anl.ipns.ViewTools.Components.ObjectState state = (gov.anl.ipns.ViewTools.Components.ObjectState)(getParameter(1).getValue());
         java.lang.String Xres=Operators.Special.ObjectState.ObjectStateUtilities.SetObjectState(preserveOb,state);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


