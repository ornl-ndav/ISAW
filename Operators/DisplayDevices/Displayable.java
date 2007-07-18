/* 
 * File: Displayable.java
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
 * Revision 1.1  2007/07/18 19:55:23  dennis
 * Operator wrapper around method to get an instance
 * of a Displayable object.
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
public class Displayable extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public Displayable(){
     super("Displayable");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "Displayable";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("DataSet or VirtualArray",null));
      addParameter( new StringPG("View Type","Graph"));
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
      S.append("Get an instance of an IDisplayable object that");
      S.append(" contains the specified data_object using the");
      S.append(" specified view_name. This IDisplayable object");
      S.append(" can then be passed to a DisplayDevice to actually");
      S.append(" print, display or save to a file.");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("Currently, the data object to be displayed must be a");
      S.append(" DataSet, IVirtualArrayList1D, or IVirtualArray2D");
      S.append(" object.");
      S.append("@param   ");
      S.append("The DataSet, IVirtualArrayList1D or IVirtualArray2D");
      S.append(" object to be displayed.");
      S.append("@param   ");
      S.append("The name of the viewer to be used when displaying");
      S.append(" the data, such as \"Image\"  for DataSet or IVirtualArray2D");
      S.append(" data, or \"Graph\" for DataSet or IVirtualArrayList1D data.");
      S.append("@error ");
      S.append("If the data object is not one of the supported types,");
      S.append(" or if the requested view is not available for the data");
      S.append(" object, an error is generated.");
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

         java.lang.Object data_object = 
                              (java.lang.Object)(getParameter(0).getValue());
         java.lang.String view_name = getParameter(1).getValue().toString();
         gov.anl.ipns.DisplayDevices.IDisplayable Xres = 
   Operators.DisplayDevices.DisplayableUtil.getInstance(data_object,view_name );

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
