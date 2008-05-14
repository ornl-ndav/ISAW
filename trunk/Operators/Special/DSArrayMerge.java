/* 
 * File: DSArrayMerge.java
 *  
 * Copyright (C) 2008     Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, Tennessee, USA.
 *
 *
 * Modified:
 *
 *  $Author:$
 *  $Date:$            
 *  $Revision:$
 *
 */

package Operators.Special;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This Operator merges all Data entries from all DataSets
 * in a Vector of DataSets, to form a new DataSet.  This
 * is useful if a single DataSet is to be formed from all of
 * the NxDatas in a NeXus file.  It takes one parameter, a
 * Vector of DataSets that are to be merged.  The attributes
 * of the new DataSet that is created, are copied from the
 * first DataSet in the Vector.
 */
public class DSArrayMerge extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public DSArrayMerge(){
     super("DSArrayMerge");
     }


   /**
    * Specifies the command to use for this operator in scripts.
    *
    * @return  The command name for this operator: DSArrayMerge.
    */
   public String getCommand(){
      return "DSArrayMerge";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Vector of DataSets to Merge",null));
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
      S.append("This Operator merges all Data entries from all DataSets");
      S.append(" in an \"array\" of DataSets, to form a new DataSet.  This");
      S.append(" is useful if a single DataSet is to be formed from all of");
      S.append(" the NxDatas in a NeXus file.");
      S.append("@algorithm    "); 
      S.append("A new DataSet is created by copying the attributes of the");
      S.append(" first DataSet in the array of DataSets.  Next, references to");
      S.append(" all of the Data entries in all of the DataSet in the array are");
      S.append(" copied to the new DataSet.  NOTE: The new DataSet contains");
      S.append(" \"shallow\" copies of the Data blocks from all DataSets in the");
      S.append(" array.");
      S.append("@assumptions    "); 
      S.append("The specified \"array\" (actually a Vector) must only");
      S.append(" contain DataSet objects.");
      S.append("@param   ");
      S.append("\"Array\" (actually a Vector) of DataSets to be merged.");
      S.append("@return A DataSet with attributes taken from the first DataSet and");
      S.append("");
      S.append(" containing all of the Data blocks from all of the DataSets");
      S.append(" in the array.  If the array is empty, the EMPTY_DATA_SET");
      S.append(" is returned.");
      S.append("@error ");
      S.append("An error occurs if the array is null, contains a null, or if");
      S.append(" it contains an Object that is not a DataSet.");
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
                     "Edit List"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){

      try{
         java.util.Vector ds_list = (java.util.Vector)(getParameter(0).getValue());
         DataSetTools.dataset.DataSet Xres=Operators.Special.DataSetArrayMerge_calc.merge(ds_list );

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
