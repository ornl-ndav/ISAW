/* 
 * File: MergeRuns.java
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
@see Operators.TOF_SCD.PeaksFileUtils#MergeRuns(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 */
public class MergeRuns extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public MergeRuns(){
     super("MergeRuns");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "MergeRuns";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataDirPG("Directory With Peaks Files",""));
      addParameter( new StringPG("Base Name of Peaks Files","TOPAZ_"));
      addParameter( new StringPG("Peaks File Suffix",".peaks"));
      addParameter( new IntArrayPG("List of Run Numbers","5637:5644"));
      addParameter( new SaveFilePG("Fully Qualified Output File Name",""));
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
      S.append("This method will merge peaks files from several runs into one new");
      S.append(" peaks file containing all of the peaks from all of the runs.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.PeaksFileUtils#MergeRuns(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("The individual peaks files are loaded into lists of peaks, which are");
      S.append(" then merged into one list with all peaks.  The resulting list is then");
      S.append(" written to the specified output file.");
      S.append("@assumptions    "); 
      S.append("All of the runs to be merged must be in the same directory, and");
      S.append(" must have names including the run number in one plase in the file");
      S.append(" name and must differ only in the run number.");
      S.append(" The run number, goniometer angles and monitor counts should have");
      S.append(" been previously set to correct values for each run, in the");
      S.append(" individual peaks files.");
      S.append("@param   ");
      S.append("The directory containing the peaks files to");
      S.append(" be merged.");
      S.append("@param   ");
      S.append("The peak file name, up to, but not including");
      S.append(" the run number.");
      S.append("@param   ");
      S.append("The portion of the peaks file names that");
      S.append(" follows the run number, including the file");
      S.append(" extension.");
      S.append("@param   ");
      S.append("List of run numbers for the peaks files to");
      S.append(" be merged, specified as a comma separated list.");
      S.append(" Ranges of integers can be specified with a colon separator.");
      S.append(" A range of consecutive run numbers can be");
      S.append(" specified with a colon separator.");
      S.append("@param   ");
      S.append("The fully qualified name of the merged file");
      S.append(" to be written.");
      S.append("@error ");
      S.append("If any of the peaks files cannot be read, or if the output");
      S.append(" file cannot be written, an exception will be thrown.");
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

         java.lang.String directory = getParameter(0).getValue().toString();
         java.lang.String base_name = getParameter(1).getValue().toString();
         java.lang.String suffix = getParameter(2).getValue().toString();
         java.lang.String run_list = (java.lang.String)(getParameter(3).getValue());
         java.lang.String out_file = getParameter(4).getValue().toString();
         Operators.TOF_SCD.PeaksFileUtils.MergeRuns(directory,base_name,suffix,run_list,out_file );

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



