/* 
 * File: ShowRotatedPeaks.java
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
@see Operators.TOF_SCD.GoniometerControlledPeaks#ShowRotatedPeaks(java.lang.String,java.lang.String)
 */
public class ShowRotatedPeaks extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public ShowRotatedPeaks(){
     super("ShowRotatedPeaks");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "ShowRotatedPeaks";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("First Peaks File",""));
      addParameter( new LoadFilePG("Second Peaks File",""));
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
      S.append("This operator plots the peak positions from two peaks files in 3D and");
      S.append(" provides controls, allowing the user to specify goniomater rotations");
      S.append(" phi, chi and omega.  Changing the phi, chi and omega values will rotate");
      S.append(" the peaks by the INVERSE of the rotation specified.  If the data and");
      S.append(" and goniometer values correspond properly, the peaks from both files");
      S.append(" will line up.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.GoniometerControlledPeaks#ShowRotatedPeaks(java.lang.String,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("The specified PHI, CHI and OMEGA angles are used to");
      S.append(" calculate the INVERSE of the goniometer rotation for");
      S.append(" the corresponding set of peaks.  The peaks are then");
      S.append(" rotated by that matrix.  If the peaks don't line up properly");
      S.append(" for both runs, then there is an error in the goniometer");
      S.append(" angles.");
      S.append("@assumptions    "); 
      S.append("The specified peaks files must exist and be in the");
      S.append(" form of peaks files currently written at the SNS.");
      S.append("@param   ");
      S.append("File with the first list of peaks.");
      S.append("@param   ");
      S.append("File with the second list of peaks");
      S.append("@error ");
      S.append("This will fail if the files do not exist, or if");
      S.append(" they can be read as valid peaks files.");
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

         java.lang.String file1 = getParameter(0).getValue().toString();
         java.lang.String file2 = getParameter(1).getValue().toString();
         Operators.TOF_SCD.GoniometerControlledPeaks.ShowRotatedPeaks(file1,file2 );

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



