/* 
 * File: DisplayGraph.java
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
 * Revision 1.1  2007/07/18 19:58:10  dennis
 * Initial version of operator wrapped around the
 * static method to display a displayable object
 * on a device.
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
public class DisplayGraph extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public DisplayGraph(){
     super("DisplayGraph");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "DisplayGraph";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("GraphicsDevice",null));
      addParameter( new PlaceHolderPG("Displayable",null));
      addParameter( new BooleanPG("Show Controls",true));
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
      S.append("Display the specified IDisplayable with the specified");
      S.append(" region, view type, line, and graph attributes.");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("The GraphicsDevice and Displayable must have");
      S.append(" been constructed and configured with the desired");
      S.append(" attributes before calling this operator.");
      S.append("@param   ");
      S.append("The graphics device where the display will be sent.");
      S.append("@param   ");
      S.append("IDisplayable object to be displayed.");
      S.append("@param   ");
      S.append("boolean indicating whether to include any");
      S.append(" associated controls, or just display the");
      S.append(" component showing the data.");
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

         gov.anl.ipns.DisplayDevices.GraphicsDevice gd = (gov.anl.ipns.DisplayDevices.GraphicsDevice)(getParameter(0).getValue());
         gov.anl.ipns.DisplayDevices.IDisplayable disp = (gov.anl.ipns.DisplayDevices.IDisplayable)(getParameter(1).getValue());
         boolean with_controls = ((BooleanPG)(getParameter(2))).getbooleanValue();
         gov.anl.ipns.DisplayDevices.GraphicsDevice.display(gd,disp,with_controls );
;
         return "Success";
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



