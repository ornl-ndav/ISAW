/* 
 * File: getBounds.java
 *  
 * Copyright (C) 2007     Galina Pozharsky
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
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2007/11/29 20:55:58  rmikk
 * Initial checkin. display was not checked in because displayGraph is already there
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
public class getBounds extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public getBounds(){
     super("getBounds");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "getBounds";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("graphics device",null));
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
      S.append("This method returns a Vector with two floats: width and height of a specific device.");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The graphics device whose bounds will be returned");
      S.append("@error ");
      S.append("");
      return S.toString();
   }


   /**
    * Returns a string array with the category the operator is in.
    *
    * @return  An array containing the category the operator is in.
    */
   public String[] getCategoryList(){
            return new String[]{
                     "HIDDENOPERATOR"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         gov.anl.ipns.DisplayDevices.GraphicsDevice gd = (gov.anl.ipns.DisplayDevices.GraphicsDevice)(getParameter(0).getValue());
         java.util.Vector Xres=gov.anl.ipns.DisplayDevices.GraphicsDevice.getBounds(gd );

         return Xres;
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



