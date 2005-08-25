/* 
 * File: SetObjectStateValue.java
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
 * Revision 1.2  2005/08/25 16:13:18  dennis
 * Added/moved to logical menu OBJECT_STATE_UTILS
 *
 * Revision 1.1  2005/08/17 21:39:03  kramer
 *
 * Initial checkin.  This is an Operator that is used to set the value
 * stored in an ObjectState given the full path to the data.  Currently
 * this Operator supports creating the specified path if it does not exist,
 * checking the data type of the currently stored data against the data type
 * of the new data, and allowing protection of the currently stored data by
 * specifying that data cannot be overwritten.
 *
 */

package Operators.Special.ObjectState;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class SetObjectStateValue extends GenericOperator{
   public SetObjectStateValue(){
     super("Set a Value in an ObjectState");
     }

   public String getCommand(){
      return "SetObjectStateValue";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new StringPG("Enter the path",""));
      addParameter( new PlaceHolderPG("Enter the ObjectState to use",null));
      addParameter( new PlaceHolderPG("Enter the value to use",null));
      addParameter( new BooleanPG("Should overwrites be allowed",new Boolean(true)));
      addParameter( new BooleanPG("Should type checking be enabled",new Boolean(true)));
      addParameter( new BooleanPG("Create the path if it doesn't exist?",new Boolean(true)));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("An ObjectState is a data structure used to ");
      S.append("store state information.  An ObjectState ");
      S.append("can contain any data type.  In addition, ");
      S.append("like files on a filesystem, the data in an ");
      S.append("ObjectState is hierarchial.  Thus, the ");
      S.append("data can be referenced by a full path. ");
      S.append("<p>");
      S.append("Given the path to an item in the ");
      S.append("ObjectState, this operator allows the ");
      S.append("item's value to be modified. ");
      S.append("@algorithm    ");
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The full path to the data in the ");
      S.append("ObjectState that should be ");
      S.append("modified.  This path should ");
      S.append("be in the form ");
      S.append("/{item1}/{item2}/.... ");
      S.append("@param   ");
      S.append("The ObjectState whose data is to ");
      S.append("be modified.  ");
      S.append("@param   ");
      S.append("The new value of the data that is ");
      S.append("to be modified.  ");
      S.append("@param   ");
      S.append("This is used to specify if values in ");
      S.append("the ObjectState should be ");
      S.append("overwritten or not.  That is, if ");
      S.append("there is data located at the ");
      S.append("specified path in the ObjecState ");
      S.append("and this parameter is set to ");
      S.append("false, the data will not be ");
      S.append("overwriten with the new data.  If ");
      S.append("this parameter is set to true, the ");
      S.append("old data in the ObjectState would ");
      S.append("be overwritten with the new data.  ");
      S.append("@param   ");
      S.append("This is used to specify type checking ");
      S.append("should be enabled.  In other words, ");
      S.append("if this parameter is set to true, the ");
      S.append("value given to this operator will not ");
      S.append("be placed at the specified path in ");
      S.append("the ObjectState unless the data type ");
      S.append("of the value is the same as the data ");
      S.append("type of the value already stored in the ");
      S.append("ObjectState.  If there is no value stored ");
      S.append("at the location, this operator will store ");
      S.append("the value given to this operator at the ");
      S.append("location.  ");
      S.append("@param  ");
      S.append("If true, this parameter specifies that the path given ");
      S.append("should be created if it does not already exist.  ");
      S.append("@return This operator returns \"Success\" if it ");
      S.append("");
      S.append("has successfully modified the ");
      S.append("specified data in the ObjectState.  ");
      S.append("@error ");
      S.append("This operator will throw an error if the ");
      S.append("path given to this operator is invalid.  ");
      S.append("If type checking is enabled, it will ");
      S.append("also throw an error if, by modifying ");
      S.append("the data at the specified location ");
      S.append("in the ObjectState, the data's type ");
      S.append("will be changed.  ");
      return S.toString();
   }


   public String[] getCategoryList(){
     return Operator.OBJECT_STATE_UTILS;
   }


   public Object getResult(){
      try{

         java.lang.String path = getParameter(0).getValue().toString();
         gov.anl.ipns.ViewTools.Components.ObjectState state = (gov.anl.ipns.ViewTools.Components.ObjectState)(getParameter(1).getValue());
         java.lang.Object value = (java.lang.Object)(getParameter(2).getValue());
         boolean allowOverwrites = ((BooleanPG)(getParameter(3))).getbooleanValue();
         boolean checkTypes = ((BooleanPG)(getParameter(4))).getbooleanValue();
         boolean createPath = ((BooleanPG)(getParameter(5))).getbooleanValue();
         java.lang.String Xres=Operators.Special.ObjectState.ObjectStateUtilities.SetObjectStateValue(path,state,value,allowOverwrites,checkTypes,createPath);
         return Xres;
       }catch(java.lang.Exception S0){
         return new ErrorString(S0.getMessage());
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


