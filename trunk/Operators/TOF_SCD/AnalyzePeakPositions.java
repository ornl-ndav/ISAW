/* 
 * File: AnalyzePeakPositions.java
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
@see Operators.TOF_SCD.SampleOffset_Calc#AnalyzePeakPositions(java.lang.String,boolean,java.lang.String)
 */
public class AnalyzePeakPositions extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public AnalyzePeakPositions(){
     super("AnalyzePeakPositions");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "AnalyzePeakPositions";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Indexed Peaks File",null));
      addParameter( new BooleanEnablePG("Save to File",false));
      addParameter( new SaveFilePG("Output File",null));
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
      S.append("This Operator will take a list of indexed peaks,");
      S.append(" estimate the sample offset, and analyze the error");
      S.append(" in each peak's position in reciprocal space, in");
      S.append(" terms of errors in the column and row directions");
      S.append(" on the detector face, and total path length errors.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.SampleOffset_Calc#AnalyzePeakPositions(java.lang.String,boolean,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("The calculated UB matrix is used to predict peak");
      S.append(" positions in reciprocal space for each indexed peak.");
      S.append(" Those positions are mapped back to real space,");
      S.append(" assuming the time-of-flight is accurately measured,");
      S.append(" and the beam direction is along the coordinate axix.");
      S.append(" Specifically, the beam direction and Q-vector alone");
      S.append(" uniquely determine the direction and wavelength at");
      S.append(" which the peak would appear.  The wavelength and");
      S.append(" time of flight uniquely determine the total path length.");
      S.append(" From this information the error in the peak position on");
      S.append(" the detector is calculated in the local x and y coordinates");
      S.append(" on the detector face.  The error in the total path");
      S.append(" length is calculated relative to the L1 and L2 values for");
      S.append(" the measured peak.");
      S.append(" coordinate");
      S.append("@assumptions    "); 
      S.append("The peaks must be indexed, since the indexing is");
      S.append(" used to determine the UB matrix.  The time-of-flight");
      S.append(" is assumed to be exactly correct, and the incident");
      S.append(" beam direction is assumed to be exactly aligned with");
      S.append(" the coordinate axis.  NOTE: Errors in");
      S.append(" calculating the peak centroid will also affect the");
      S.append(" accuracy of peak positions, and will contribute to");
      S.append(" the overall error.");
      S.append("@param   ");
      S.append("The fully qualified name of an indexed peaks file.");
      S.append("@param   ");
      S.append("If true the results will be saved to a file, otherwise");
      S.append(" the results are just dumped out to the terminal window.");
      S.append("@param   ");
      S.append("The name of the file where the output should be saved.");
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
                     "TOF_NSCD"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String peaks_file_name = getParameter(0).getValue().toString();
         boolean save_to_file = ((BooleanEnablePG)(getParameter(1))).getbooleanValue();
         java.lang.String file_name = getParameter(2).getValue().toString();
         Operators.TOF_SCD.SampleOffset_Calc.AnalyzePeakPositions(peaks_file_name,save_to_file,file_name );

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



