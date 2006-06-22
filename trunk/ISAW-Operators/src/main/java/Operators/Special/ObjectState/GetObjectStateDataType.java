/* 
 * File: GetObjectStateDataType.java
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
 * Revision 1.2  2005/08/25 16:13:15  dennis
 * Added/moved to logical menu OBJECT_STATE_UTILS
 *
 * Revision 1.1  2005/08/17 21:27:56  kramer
 *
 * Initial checkin.  This is an Operator that is used to get the data type
 * of the data stored at a particular location in an ObjectState, given the
 * full path to the data.
 */

package Operators.Special.ObjectState;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class GetObjectStateDataType extends GenericOperator{
   public GetObjectStateDataType(){
     super("Get the Data Type of a Value in an ObjectState");
     }

   public String getCommand(){
      return "GetObjectStateDataType";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new StringPG("Enter the path to the data",""));
      addParameter( new PlaceHolderPG("Enter the ObjectState to use",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("An ObjectState is a data structure used to ");
      S.append("store state information.  An ObjectState ");
      S.append("can contain any data type.  In addition, ");
      S.append("like files on a filesystem, the data in an ");
      S.append("ObjectState is hierarchial.  Thus, the ");
      S.append("data can be referenced by a full path.  ");
      S.append("<p>");
      S.append("Given a path in the ObjectState, this ");
      S.append("operator determines the data type of ");
      S.append("the data currently stored at the path.  ");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The full path to the data in the ");
      S.append("ObjectState that should be ");
      S.append("read.  This path should be ");
      S.append("in the form ");
      S.append("/{item1}/{item2}/.... ");
      S.append("@param   ");
      S.append("The data structure whose data is to be ");
      S.append("analyzed.  ");
      S.append("@return This operator returns the data type ");
      S.append("");
      S.append("of the data at the specified location ");
      S.append("in the given ObjectState.  ");
      S.append("@error ");
      S.append("If the path given to this operator is ");
      S.append("invalid, an error will be thrown.  ");
      return S.toString();
   }


   public String[] getCategoryList(){
     return Operator.OBJECT_STATE_UTILS;
   }


   public Object getResult(){
      try{

         java.lang.String path = getParameter(0).getValue().toString();
         gov.anl.ipns.ViewTools.Components.ObjectState state = (gov.anl.ipns.ViewTools.Components.ObjectState)(getParameter(1).getValue());
         java.lang.Object Xres=Operators.Special.ObjectState.ObjectStateUtilities.GetObjectStateDataType(path,state);
         return Xres;
       }catch(java.lang.Exception S0){
         return new ErrorString(S0.getMessage());
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


