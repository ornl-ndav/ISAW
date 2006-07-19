/* 
 * File: getValues.java
 *  
 * Copyright (C) 2006     Dennis Mikkelson
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
 * Revision 1.4  2006/07/19 18:07:15  dennis
 * Removed unused imports.
 *
 * Revision 1.3  2006/07/17 03:20:07  dennis
 * Clarified prompt string.
 *
 * Revision 1.2  2006/07/17 02:22:06  dennis
 * Converted from dos to unix text format.
 *
 * Revision 1.1  2006/07/17 02:14:50  dennis
 * Operator to get a sequence of X, Y or Error values from
 * a Data block and return them in a Vector.  NOTE: This
 * provides a basic (but inefficient) way to access
 * individual values in the Data block.  If many values
 * in many Data blocks are needed, the operation should
 * be done in Java, not in a script.
 *
 */

package Operators.Generic;

import java.util.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class getValues extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public getValues(){
     super("getValues");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "getValues";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet to get values from",null));
      addParameter( new IntegerPG("Data block index",0));
      addParameter( new FloatPG("min X value",0));
      addParameter( new FloatPG("max X value",10000));
      Vector choices = new Vector(4);
      choices.add("X");
      choices.add("Y");
      choices.add("Error");
      choices.add("Y");
      addParameter( new ChoiceListPG("Values to Get", choices));
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
      S.append("Get a list of X-values, Y-values or errors from the");
      S.append(" specified Data block in the specified DataSet.");
      S.append("@algorithm    "); 
      S.append("If X-values are obtained, they will start with the last");
      S.append(" X-value in the XScale that is less than or equal to the");
      S.append(" requested min_x and end with the first X-value in the");
      S.append(" XScale that is greater than or equal to the requested");
      S.append(" max_x.  If Y-values or errors are obtained, they will");
      S.append(" correspond to the X-values as follows.  If the Data");
      S.append(" block is a histogram, the Y-values or errors will");
      S.append(" correspond to the histogram values between the");
      S.append(" bin boundaries given by the X-values. If the Data");
      S.append(" block is a function, the Y-values or errors will");
      S.append(" correspond to the values at the X-values.");
      S.append("@assumptions    "); 
      S.append("The DataBlock index must be valid and the");
      S.append(" min and max XValues should be in the range");
      S.append(" covered by the XScale.");
      S.append("@param   ");
      S.append("The DataSet from which the values will be obtained.");
      S.append("@param   ");
      S.append("The index of the Data block from which the values");
      S.append(" will be obtained");
      S.append("@param   ");
      S.append("The requested minimum X value.");
      S.append("@param   ");
      S.append("The requested maximum X value.");
      S.append("@param   ");
      S.append("String \"X\", \"Y\" or \"Error\" specifying which values");
      S.append(" are to be obtained.");
      S.append("@error ");
      S.append("An exception will be thrown if the specified");
      S.append(" Data block index does not exist, or if the values");
      S.append(" requested do not exist.");
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
                     "DataSet",
                     "Tweak"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         DataSetTools.dataset.DataSet ds = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         int index = ((IntegerPG)(getParameter(1))).getintValue();
         float min_x = ((FloatPG)(getParameter(2))).getfloatValue();
         float max_x = ((FloatPG)(getParameter(3))).getfloatValue();
         java.lang.String which_vals = getParameter(4).getValue().toString();
         java.util.Vector Xres=Operators.Generic.DataValueAccessMethods.getValues(ds,index,min_x,max_x,which_vals );

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
