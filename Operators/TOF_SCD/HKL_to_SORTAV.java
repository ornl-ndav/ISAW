/* 
 * File: HKL_to_SORTAV.java
 *  
 * Copyright (C) 2011     Dennis Mikkelson
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
 * This work was supported by the SNS division of Oak Ridge National Laboratory, Oak Ridge, Tennessee, USA.
 *
 *
 * Last Modified:
 *
 * $ Author: $
 * $Date$$
 * $Revision$
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
@see Operators.TOF_SCD.HKL_to_SORTAV_calc#HKL_to_SORTAV(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 */
public class HKL_to_SORTAV extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public HKL_to_SORTAV(){
     super("HKL_to_SORTAV");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "HKL_to_SORTAV";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("HKL file",""));
      addParameter( new LoadFilePG("Integrate file",""));
      addParameter( new LoadFilePG("Matrix file",""));
      addParameter( new SaveFilePG("SORTAV file(output)",""));
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
      S.append("This method will write a file of peak information with integrated");
      S.append(" intensities AND direction cosines for the incident and scattered");
      S.append(" beams, in the form needed by SORTAV.  This requires three files as");
      S.append(" input, a .integrate file and .mat file from ISAW and the .hkl file");
      S.append(" produced by ANVRED.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.HKL_to_SORTAV_calc#HKL_to_SORTAV(java.lang.String,java.lang.String,java.lang.String,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The fully qualified name of the hkl file produced");
      S.append(" by ANVRED");
      S.append("@param   ");
      S.append("The fully qualified name of the .integrate file");
      S.append(" produced by ISAW");
      S.append("@param   ");
      S.append("The fully qualified name of the matrix file,");
      S.append(" used to index the peaks, as produced by Isaw's");
      S.append(" InitialPeaksWizard, or IsawEV");
      S.append("@param   ");
      S.append("The fully qualified name of the .sortav file");
      S.append(" that will be written out, containing the");
      S.append(" beam and peak direction information");
      S.append("@return The output file is written as a DOS text file with lines terminated");
      S.append("");
      S.append(" by a carriage return and line feed pair.  This is needed so that the");
      S.append(" file can be read properly by WinGX on a windows machine.");
      S.append("@error ");
      S.append("An exception will be thrown if an error is");
      S.append(" encountered while reading or writing the");
      S.append(" files.");
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
                     "NEW_SNS"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String hkl_file = getParameter(0).getValue().toString();
         java.lang.String integrate_file = getParameter(1).getValue().toString();
         java.lang.String matrix_file = getParameter(2).getValue().toString();
         java.lang.String sortav_file = getParameter(3).getValue().toString();
         Operators.TOF_SCD.HKL_to_SORTAV_calc.HKL_to_SORTAV(hkl_file,integrate_file,matrix_file,sortav_file );

         return "Success";
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



