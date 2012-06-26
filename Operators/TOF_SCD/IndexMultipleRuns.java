/* 
 * File: IndexMultipleRuns.java
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
@see Operators.TOF_SCD.PeaksFileUtils#IndexMultipleRuns(java.lang.String,boolean,float,float,float,java.lang.String,java.lang.String,java.lang.String)
 */
public class IndexMultipleRuns extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public IndexMultipleRuns(){
     super("IndexMultipleRuns");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "IndexMultipleRuns";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Peaks File to be Indexed",""));
      addParameter( new BooleanEnablePG("Use FFT for Initial Indexing",true));
      addParameter( new FloatPG("Minimum d (Less than Shortest Real Space Cell Edge)",3));
      addParameter( new FloatPG("Maximum d (More than Longest Real Space Cell Edge)",15));
      addParameter( new FloatPG("Tolerance",0.12));
      addParameter( new SaveFilePG("Output Directory",""));
      addParameter( new StringPG("Output Peaks File Name",""));
      addParameter( new StringPG("Matrix File Base Name",""));
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
      S.append("Given a peaks file containing peaks from multiple runs, try to find a");
      S.append(" consistent indexing.  If the goniometer angles are not correct, it may");
      S.append(" be necessary to increase the tolerance.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.PeaksFileUtils#IndexMultipleRuns(java.lang.String,boolean,float,float,float,java.lang.String,java.lang.String,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("A UB that indexes the peaks from the first run in the file is first");
      S.append(" obtained.  This can be obtained either by using the FFT indexing");
      S.append(" algorithm, or by finding the UB corresponding to already existing");
      S.append(" indexes.  This UB is then optimized and applied to the peaks from");
      S.append(" latter runs.  For each later run, the UB is re-optimized.  The");
      S.append(" optimzed UBs for each run are saved in the output directory.");
      S.append("@assumptions    "); 
      S.append("The peaks in the run file must be grouped according to run number");
      S.append(" and the goniometer angles must be reasonably accurate.");
      S.append("@param   ");
      S.append("Name of peaks file with multiple runs.  The goniometer");
      S.append(" angles must be correctly set for all of the runs.");
      S.append("@param   ");
      S.append("Flag indicating whether to obtain an initial indexing");
      S.append(" of the first run's peaks using the FFT indexing routine");
      S.append(" or to use initial index values already specified");
      S.append(" for the peaks");
      S.append("@param   ");
      S.append("A number strictly less than the shortest edge of the");
      S.append(" real space Niggli reduced cell.");
      S.append("@param   ");
      S.append("A number strictly more than the longest edge of the");
      S.append(" real space Niggli reduced cell.");
      S.append("@param   ");
      S.append("Tolerance on fractional Miller indexes for a peak to");
      S.append(" be considered indexed.");
      S.append("@param   ");
      S.append("Name of the directory where the output files should");
      S.append(" be written.");
      S.append("@param   ");
      S.append("Name of the file of reindexed peaks that will be");
      S.append(" be written to the output directory.");
      S.append("@param   ");
      S.append("Base name for the individual run UB matrices that");
      S.append(" will be written to the output directory.  The names");
      S.append(" will have the form BaseName_nnnn.mat where the");
      S.append(" BaseName is as specified and nnnn is the run");
      S.append(" number.");
      S.append("@error ");
      S.append("If the peaks file can't be read, or if the peaks cannot");
      S.append(" be indexed, the operator will fail.");
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
         boolean use_fft = ((BooleanEnablePG)(getParameter(1))).getbooleanValue();
         float min_d = ((FloatPG)(getParameter(2))).getfloatValue();
         float max_d = ((FloatPG)(getParameter(3))).getfloatValue();
         float tolerance = ((FloatPG)(getParameter(4))).getfloatValue();
         java.lang.String out_directory = getParameter(5).getValue().toString();
         java.lang.String out_file_name = getParameter(6).getValue().toString();
         java.lang.String matrix_base_name = getParameter(7).getValue().toString();
         Operators.TOF_SCD.PeaksFileUtils.IndexMultipleRuns(peaks_file,use_fft,min_d,max_d,tolerance,out_directory,out_file_name,matrix_base_name );

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



