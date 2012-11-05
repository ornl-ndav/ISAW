/* 
 * File: RemovePeaksInWLRange.java
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
@see Operators.TOF_SCD.PeaksFileUtils#RemovePeaksInWLRange(java.lang.String,float,float,java.lang.String)
 */
public class RemovePeaksInWLRange extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public RemovePeaksInWLRange(){
     super("RemovePeaksInWLRange");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "RemovePeaksInWLRange";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Peaks File",""));
      addParameter( new FloatPG("Min Wavelength of Range to Delete",1));
      addParameter( new FloatPG("Max Wevelength of Range to Delete",1));
      addParameter( new SaveFilePG("Output Peaks File",""));
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
      S.append("Get a new list of peaks containing only those peaks whose wavelength");
      S.append(" is NOT in the specified range of wavelengths.  If no output file is");
      S.append(" specified, the new list of peaks will be written back to the original");
      S.append(" peaks file, otherwise the new list of peaks will be written to the");
      S.append(" output file.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.PeaksFileUtils#RemovePeaksInWLRange(java.lang.String,float,float,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("Peaks in the specified interval of wavelengths will be removed");
      S.append(" from the list written to the output file.");
      S.append("@assumptions    "); 
      S.append("The input peaks file must exisit.");
      S.append(" The output peaks file must be writeable by the user.");
      S.append("@param   ");
      S.append("File of peaks.");
      S.append("@param   ");
      S.append("Lower bound on range of wavelengths that will be");
      S.append(" discarded.  Only peaks with wavelength less than");
      S.append(" this value or more than max_wl will be kept.");
      S.append("@param   ");
      S.append("Upper bound on range of wavelengths that will be");
      S.append(" discarded.  Only peaks with wavelength greater than");
      S.append(" this value or less than min_wl will be kept.");
      S.append("@param   ");
      S.append("File where only those peaks whose wavelength is");
      S.append(" outside of the specified range will be written.");
      S.append(" If blank, the peaks are written back to the input file.");
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

         java.lang.String peaks_file = getParameter(0).getValue().toString();
         float min_wl = ((FloatPG)(getParameter(1))).getfloatValue();
         float max_wl = ((FloatPG)(getParameter(2))).getfloatValue();
         java.lang.String out_file = getParameter(3).getValue().toString();
         Operators.TOF_SCD.PeaksFileUtils.RemovePeaksInWLRange(peaks_file,min_wl,max_wl,out_file );

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



