/* 
 * File: RemoveDetector.java
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
@see Operators.TOF_SCD.PeaksFileUtils#RemoveDetector(java.lang.String,int,java.lang.String)
 */
public class RemoveDetector extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public RemoveDetector(){
     super("RemoveDetector");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "RemoveDetector";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Peaks File Name",""));
      addParameter( new IntegerPG("ID of Detector to Remove",0));
      addParameter( new SaveFilePG("Output File ( Blank to Write Back to Input File )",""));
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
      S.append("Remove all peaks with the specified detector number from the specified");
      S.append(" file of peaks, and write the modified peaks list to the output file.");
      S.append(" The output file can be left blank, in which case the modified list will");
      S.append(" be written back to input file!");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.PeaksFileUtils#RemoveDetector(java.lang.String,int,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("The specified peaks file is loaded into a list of peaks, and all peaks");
      S.append(" from the specified detector are removed from the list.  If a non-blank");
      S.append(" output file is specified the modified list is written to the output file.");
      S.append(" If the output file name is blank, the modified list will be written back");
      S.append(" to the original input file.");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("Name of the peaks file from which the specified detector will be removed.");
      S.append("@param   ");
      S.append("The number of the detector that should be removed.");
      S.append("@param   ");
      S.append("Name of the new file to write.  If this is blank or");
      S.append(" a zero length string, the new list of peaks will be");
      S.append(" written back to the input file.");
      S.append("@error ");
      S.append("Exceptions will be thrown if the input peaks file can't be read,");
      S.append(" of if the output peaks file can't be written.");
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

         java.lang.String peaks_file = getParameter(0).getValue().toString();
         int det_num = ((IntegerPG)(getParameter(1))).getintValue();
         java.lang.String out_file = getParameter(2).getValue().toString();
         Operators.TOF_SCD.PeaksFileUtils.RemoveDetector(peaks_file,det_num,out_file );

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



