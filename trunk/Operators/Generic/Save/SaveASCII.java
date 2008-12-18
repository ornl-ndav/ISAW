/* 
 * File: SaveASCII.java
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
 *            MSCS Deparatment
 *            HH237H
 *            Menomonie, WI. 54751
 *
 *
 *
 * Modified:
 *
 * $Log:$
 *
 */

package Operators.Generic.Save;
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
public class SaveASCII extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public SaveASCII(){
     super("SaveASCII");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "SaveASCII";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("Select DataSet",DataSetTools.dataset.DataSet.EMPTY_DATA_SET));
      addParameter( new BooleanPG("Include Error Estimates?",false));
      addParameter( new StringPG("Format (blank for default)",""));
      addParameter( new SaveFilePG("File to Write", "ASCII.txt"));
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
      S.append("This operator writes selected Data blocks from a DataSet");
      S.append(" to a two column (x,y) or three column (x,y,error) ASCII file.");
      S.append(" The Data blocks to be written MUST first be marked as");
      S.append(" \"selected\" in the DataSet, using one of the operators");
      S.append(" that set the selected flags, such as  listed in the");
      S.append(" Operations -> Attribute -> Select Groups by Index");
      S.append("@algorithm    "); 
      S.append("The x,y or x,y,err values from each selected Data block");
      S.append(" are written in columns to the specified file.  Header");
      S.append(" information specifies the DataSet, units, index that was");
      S.append(" printed, and the number of x and y values.  If the Data");
      S.append(" block is a histogram, the x value specifies the x-coordinate");
      S.append(" of the start of the bin, and the NEXT x values specifies the");
      S.append(" end of the bin.  If more than one Data block is saved in");
      S.append(" the same file, the Data blocks will be saved sequentially in");
      S.append(" the file.");
      S.append("@assumptions    "); 
      S.append("The Data blocks to save MUST be selected in the");
      S.append(" Data Set before using this operator.");
      S.append("@param   ");
      S.append("The DataSet from which Data blocks will be");
      S.append(" printed.");
      S.append("@param   ");
      S.append("Flag indicating whether to write three columns,");
      S.append(" x, y and error estimates for y, or just two");
      S.append(" columns, x, y.");
      S.append("@param   ");
      S.append("C style format string desribing how the two or");
      S.append(" three columns should be formatted, such as:");
      S.append(" \"%12.7E  %12.7E\n\".  If null, or blank, the");
      S.append(" values will be formatted using exponential");
      S.append(" notation.  NOTE: The number of format specifiers");
      S.append(" MUST correspond to the number of values printed,");
      S.append(" (2 or 3). ");
      S.append("@param   ");
      S.append("The fully qualified name of the file where the");
      S.append(" data should be written.");
      S.append("@return A string indicating that the file was successfully written,");
      S.append("");
      S.append(" and the name of the file that was written.");
      S.append("@error ");
      S.append("Exceptions will be thown if the DataSet is null or empty,");
      S.append(" if the file can't be written, or if the specified format is");
      S.append(" not valid, or the number of format specifiers doesn't");
      S.append(" match the number of columns being printed.");
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
                     "File",
                     "Save"
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
         boolean include_errors = ((BooleanPG)(getParameter(1))).getbooleanValue();
         java.lang.String format = getParameter(2).getValue().toString();
         java.lang.String out_file_name = getParameter(3).getValue().toString();
         java.lang.String Xres=Operators.Generic.Save.SaveASCII_calc.SaveASCII(ds,include_errors,format,out_file_name );

         return Xres;
       }catch(java.io.IOException S0){
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



