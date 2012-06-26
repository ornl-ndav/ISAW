/* 
 * File: ChangeHKLs.java
 *  
 * Copyright (C) 2012     Dennis Mikkelson
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
 *            Department of Mathematics, Statistics and Computer Science
 *            University of Wisconsin-Stout
 *            Menomonie, WI 54751, USA
 *
 * This work was supported by the SNS division of Oakridge National Laboratory.
 *
 *
 * Last Modified:
 *
 * $ Author: $
 * $Date: 2010-10-25 09:37:07 -0500 (Mon, 25 Oct 2010) $$
 * $Revision: 21066 $
 */

package Operators.TOF_SCD;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 * This operator is a wrapper around 
@see Operators.TOF_SCD.PeaksFileUtils#ChangeHKLs(java.lang.String,java.util.Vector,boolean,java.lang.String)
 */
public class ChangeHKLs extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public ChangeHKLs(){
     super("ChangeHKLs");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "ChangeHKLs";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Peaks File to Alter",""));
      addParameter( new ArrayPG("Enter 3x3 Transform to Apply to HKLs","1,0,0,0,1,0,0,0,1"));
      addParameter( new BooleanPG("Remove Peaks That Are NOT Indexed",false));
      addParameter( new SaveFilePG("Output File (Blank to Write to Input File )",""));
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
      S.append("Change the indexes in a peaks file by applying a");
      S.append(" specified tranformation to the HKL values.");
      S.append(" NOTE: As long as the 9 values entered can be");
      S.append(" converted to a float array, the indices will be transformed");
      S.append(" by the corresponding 3x3 matrix.  NO ERROR CHECKING");
      S.append(" IS DONE!");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.PeaksFileUtils#ChangeHKLs(java.lang.String,java.util.Vector,boolean,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("The peaks file is loaded into a list and the");
      S.append(" the specified tranformation is multiplied times");
      S.append(" the HKL column vector from each peak, as:");
      S.append(" T*hkl");
      S.append("@assumptions    "); 
      S.append("The peaks file should already be indexed.");
      S.append("@param   ");
      S.append("The peaks file to re-index using the specified");
      S.append(" transformation");
      S.append("@param   ");
      S.append("Comma separated list of the nine entries for the");
      S.append(" 3x3 transformation that will be applied to the");
      S.append(" HKL values in the file");
      S.append("@param   ");
      S.append("Flag that selects whether or not to remove any");
      S.append(" peaks from the file that were originally not");
      S.append(" indexed.");
      S.append("@param   ");
      S.append("Name of the new file to write.  If this is");
      S.append(" blank or a zero length string, the updated list");
      S.append(" of peaks will be written back to the input file");
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
                     "Instrument Type",
                     "TOF_NSCD",
                     "PEAKS_FILE_UTILS"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String peaks_flie = getParameter(0).getValue().toString();
         java.util.Vector arr_vals = (java.util.Vector)(getParameter(1).getValue());
         boolean remove_unindexed = ((BooleanPG)(getParameter(2))).getbooleanValue();
         java.lang.String out_file = getParameter(3).getValue().toString();
         Operators.TOF_SCD.PeaksFileUtils.ChangeHKLs(peaks_flie,arr_vals,remove_unindexed,out_file );

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



