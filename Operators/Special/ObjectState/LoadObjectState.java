/* 
 * File: LoadObjectState.java
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
 * Modified:
 *
 * $Log$
 * Revision 1.2  2005/08/25 16:13:17  dennis
 * Added/moved to logical menu OBJECT_STATE_UTILS
 *
 * Revision 1.1  2005/08/05 16:12:16  kramer
 *
 * Initial checkin.  This is an operator that is used to load an ObjectState
 * from a file.
 */

package Operators.Special.ObjectState;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class LoadObjectState extends GenericOperator{
   public LoadObjectState(){
     super("Load an ObjectState");
     }

   public String getCommand(){
      return "LoadObjectState";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Enter the file where the state was saved",""));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Various components in ISAW have the ability to remember ");
      S.append("their state.  As a result, if someone is uses the ");
      S.append("component, changes the values of some parameters, ");
      S.append("and then stops using the component, when the component ");
      S.append("is reopened the parameters will retain their modified ");
      S.append("values.  This state information can be saved to a file.  ");
      S.append("Later it can be loaded and given to the component to ");
      S.append("have this component automatically conform to the ");
      S.append("state.  This operator is used to load a state that has ");
      S.append("already been saved to a file.  ");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("It is assumed that the file given to this operator both ");
      S.append("exists and corresponds to a stored ObjectState. ");
      S.append("@param   ");
      S.append("The full path to the file that is storing the ObjectState.  ");
      S.append("@error ");
      S.append("This operator will throw an error if the file given does not ");
      S.append("exist or if it doesn't correspond to a stored ObjectState.  ");
      return S.toString();
   }


   public String[] getCategoryList(){
     return Operator.OBJECT_STATE_UTILS;
   }


   public Object getResult(){
      try{

         java.lang.String filename = getParameter(0).getValue().toString();
         gov.anl.ipns.ViewTools.Components.ObjectState Xres=Operators.Special.ObjectState.ObjectStateUtilities.LoadObjectState(filename);
         return Xres;
       }catch(java.io.IOException S0){
         return new ErrorString(S0.getMessage());
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


