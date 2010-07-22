/* 
 * File: ClampValues.java
 *  
 * Copyright (C) 2010     Dennis Mikkelson
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
 *            MSCS Deparatment
 *            HH237H
 *            Menomonie, WI. 54751
 *
 * This work was supported by the SNS division of Oak Ridge National 
 * Laboratory, Oak Ridge, Tennessee, USA.
 *
 *
 * Last Modified:
 *
 * $ Author: $
 * $Date$$
 * $Revision$
 */

package Operators.Special;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;
import DataSetTools.dataset.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 * This operator is a wrapper around 
@see Operators.Special.ClampValues_calc#ClampValues(DataSetTools.dataset.DataSet,float,boolean)
 */
public class ClampValues extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public ClampValues(){
     super("ClampValues");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "ClampValues";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet to be clamped",DataSet.EMPTY_DATA_SET));
      addParameter( new FloatPG("Level to clamp at",0.1));
      addParameter( new BooleanPG("Is this a minimum value?",true));
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
      S.append("This operator will clamp all values in the specified");
      S.append(" DataSet to be above (or below) the specified level.");
      S.append(" If error values have been specified, the error");
      S.append(" values at these points will be set to 1.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.Special.ClampValues_calc#ClampValues(DataSetTools.dataset.DataSet,float,boolean)");
      S.append("@algorithm    "); 
      S.append("Each y-value is compared to the specified level.");
      S.append(" If the level is a minimum level, y-values that are");
      S.append(" below that level will be set to that level.");
      S.append(" If the level is a maximum level, y-values that are");
      S.append(" above that level will be set to that level.");
      S.append("@assumptions    "); 
      S.append("The DataSet must contain tabluated values, not");
      S.append(" a model function.");
      S.append("@param   ");
      S.append("The DataSet whose values are to be clamped.");
      S.append("@param   ");
      S.append("The level at which the values are to be clamped.");
      S.append("@param   ");
      S.append("Flag indicating whether the level is a minimum");
      S.append(" or a maximum level for values in the DataSet.");
      S.append("@return The values in the DataSet are modified in place.");
      S.append("");
      S.append(" There is no return value.");
      S.append("@error ");
      S.append("If the DataSet is null or empty, an IllegalArgumentException");
      S.append(" will be thrown.");
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
                     "Filters"
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
         float level = ((FloatPG)(getParameter(1))).getfloatValue();
         boolean is_min_val = ((BooleanPG)(getParameter(2))).getbooleanValue();
         Operators.Special.ClampValues_calc.ClampValues(ds,level,is_min_val );

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



