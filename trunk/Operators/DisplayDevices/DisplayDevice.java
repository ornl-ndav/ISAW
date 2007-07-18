/* 
 * File: DisplayDevice.java
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
 * Revision 1.1  2007/07/18 14:46:00  dennis
 * Operator to get an instance of a DisplayDevice.
 *
 *
 */

package Operators.DisplayDevices;


import java.util.*;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.DisplayDevices.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class DisplayDevice extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public DisplayDevice(){
     super("DisplayDevice");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "DisplayDevice";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      Vector choices = new Vector();
      choices.add( GraphicsDevice.PRINTER );
      choices.add( GraphicsDevice.FILE );
      choices.add( GraphicsDevice.SCREEN );
      choices.add( GraphicsDevice.PREVIEW );
      choices.add( GraphicsDevice.SCREEN );
      clearParametersVector();
      addParameter( new ChoiceListPG("Device Type",choices) );
      addParameter( new StringPG("File or Printer Name:","") );
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
      S.append("Open a DisplayDevice for sending graphs to a");
      S.append(" File, Printer, Screen or a Preview window.");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("The requested device must exist and be accessible");
      S.append(" from your system.");
      S.append("@param   ");
      S.append("The type of GraphicsDevice to construct:");
      S.append(" File, Printer, Screen or Preview.");
      S.append("@param   ");
      S.append("The file or printer name for file or printer devices.");
      S.append(" This parameter is ignored for other device types.");
      S.append("");
      S.append("@return A reference to a File, Printer, Screen or ");
      S.append("Preview Device for displaying graphs and images.");
      S.append("");
      S.append("@error ");
      S.append("An Error will be returned if the requested device");
      S.append(" can not be opened/");
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

         java.lang.String type = getParameter(0).getValue().toString();
         java.lang.String name = getParameter(1).getValue().toString();
         gov.anl.ipns.DisplayDevices.GraphicsDevice Xres =
           gov.anl.ipns.DisplayDevices.GraphicsDevice.getInstance(type,name );
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
