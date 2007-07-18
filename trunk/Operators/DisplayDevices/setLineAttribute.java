/* 
 * File: setLineAttribute.java
 *  
 * Copyright (C) 2007     Dennis Mikkelson
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
 * Revision 1.1  2007/07/18 19:56:38  dennis
 * Initial version of operator wrapper around static
 * method to set line attributes.
 *
 *
 */

package Operators.DisplayDevices;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class setLineAttribute extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public setLineAttribute(){
     super("setLineAttribute");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "setLineAttribute";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Displayable to modify",null));
      addParameter( new IntegerPG("Line Index",0));
      addParameter( new StringPG("Attribute Name","Line Color"));
      addParameter( new StringPG("New Value","Red"));
   }


   /**
    * Writes a string for the documentation of the operator provided by
    * the user.
    *
    * @return  The documentation for the operator.
    */
   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("This method sets an attribute of the displayable that pertains");
      S.append(" to a particular portion of the display, such as one particular");
      S.append(" line.");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("The displayable must be initialized");
      S.append("@param   ");
      S.append("The IDisplayable object for which the");
      S.append(" attribute is to be set.");
      S.append("@param   ");
      S.append("An index identifying the part of the display");
      S.append(" that the attribute applies to, such as a");
      S.append(" specific line number.");
      S.append("@param   ");
      S.append("The name of the attribute being set.");
      S.append("@param   ");
      S.append("The value to use for the attribute.");
      S.append("@error ");
      S.append("An error will be generated if the type is not");
      S.append(" valid, or the new value is not valid or does not");
      S.append(" matcth the type.");
      return S.toString();
   }


   /**
    * Returns a string array with the category the operator is in.
    *
    * @return  An array containing the category the operator is in.
    */
   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "DisplayDevices"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         gov.anl.ipns.DisplayDevices.IDisplayable displayable = 
         (gov.anl.ipns.DisplayDevices.IDisplayable)(getParameter(0).getValue());
         int index = ((IntegerPG)(getParameter(1))).getintValue();
         java.lang.String name = getParameter(2).getValue().toString();
         java.lang.String value = getParameter(3).getValue().toString();
         gov.anl.ipns.DisplayDevices.Displayable.setLineAttribute(displayable,index,name,value );
;
         return "Success";
      }catch(java.lang.Exception S0){
         return new ErrorString(S0.getMessage());
      }catch( Throwable XXX){
        String[]Except = ScriptUtil.
            GetExceptionStackInfo(XXX,true,1);
        String mess="";
        if(Except == null) Except = new String[0];
        for( int i =0; i< Except.length; i++)
           mess += Except[i]+"\r\n            "; 
        return new ErrorString( XXX.toString()+":"
             +mess);
                }
   }
}



