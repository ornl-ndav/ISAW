/* 
 * File: setValues.java
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
 * Revision 1.2  2006/07/19 18:07:15  dennis
 * Removed unused imports.
 *
 * Revision 1.1  2006/07/17 03:23:19  dennis
 * Operator to setValues into a Data block in a DataSet.
 * Only the Y-values and Errors can be set and only Data
 * blocks that are of type Tabulated Data can be altered.
 * This is also quite inefficient, and should be done
 * directly from Java code, if many values in many Data
 * blocks are to be altered.
 *
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
public class setValues extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public setValues(){
     super("setValues");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "setValues";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet to change",null));
      addParameter( new IntegerPG("Data block index",0));
      addParameter( new FloatPG("min X value",0));
      addParameter( new ArrayPG("List of new values",null));
      Vector choices = new Vector(2);
      choices.add( "Y" );
      choices.add( "Error" );
      addParameter( new ChoiceListPG("Values to Set", choices));
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
      S.append("Set a list of Y-values or Errors from the specified");
      S.append(" Data block into the specified DataSet");
      S.append("@algorithm    "); 
      S.append("The specified min_x value will be mapped to the");
      S.append(" last X in the XScale that is less than or equal to");
      S.append(" the specified X-value.  If the specified min_x is");
      S.append(" less than the smallest value in the XScale, then it");
      S.append(" will be mapped to the smallest value in the XScale.");
      S.append(" When the Y-values or errors are set, they will");
      S.append(" correspond to the X-values as follows.  If the Data");
      S.append(" block is a histogram, the Kth Y-value or error will");
      S.append(" correspond to the histogram value or error for the");
      S.append(" bin that is K bins to the right of the specified X-value.");
      S.append(" If the Data block is a function, the Kth Y-value will");
      S.append(" correspond to the X-value that is K steps to the");
      S.append(" right of the specified X-value.  In either case,");
      S.append(" a contiguous block of values will be assigned, starting");
      S.append(" with the value corresponding to the specified X-value.");
      S.append("@assumptions    "); 
      S.append("The DataBlock index must be valid and the min_x");
      S.append(" value should be in the range of values covered by");
      S.append(" the XScale.");
      S.append("@param   ");
      S.append("The DataSet into which the values will be set.");
      S.append("@param   ");
      S.append("The index of the Data block into which the values");
      S.append(" will be set.");
      S.append("@param   ");
      S.append("The requested minimum X value.");
      S.append("@param   ");
      S.append("Vector of float specifying a Contiguous list of values");
      S.append(" to be assigned to this Data block, starting with the");
      S.append(" value corresponding to the specified min_x.");
      S.append("@param   ");
      S.append("String \"Y\" or \"Error\" specifying which values");
      S.append(" are to be set.");
      S.append("@return A message indicating that the values were set");
      S.append("");
      S.append(" successfully will be returned.");
      S.append("@error ");
      S.append("An exception will be thrown if the specified");
      S.append(" Data block index does not exist.");
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
         java.util.Vector vals = (java.util.Vector)(getParameter(3).getValue());
         java.lang.String which_vals = getParameter(4).getValue().toString();
         java.lang.String Xres=Operators.Generic.DataValueAccessMethods.setValues(ds,index,min_x,vals,which_vals );

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
