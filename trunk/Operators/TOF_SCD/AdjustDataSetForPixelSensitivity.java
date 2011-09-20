/* 
 * File: AdjustDataSetForPixelSensitivity.java
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
@see Operators.TOF_SCD.WritePixelSensitivity_calc#AdjustDataSetForPixelSensitivity(DataSetTools.dataset.DataSet,java.lang.String)
 */
public class AdjustDataSetForPixelSensitivity extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public AdjustDataSetForPixelSensitivity(){
     super("AdjustDataSetForPixelSensitivity");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "AdjustDataSetForPixelSensitivity";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet to be Adjusted",null));
      addParameter( new LoadFilePG("Pixel Sensitivity Map File",""));
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
      S.append("Adjust the measured intensities in the specified DataSet using");
      S.append(" the pixel sensitivity factors stored in the specified pixel sensitivity");
      S.append(" map file.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.WritePixelSensitivity_calc#AdjustDataSetForPixelSensitivity(DataSetTools.dataset.DataSet,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("Each value in a spectrum is multiplied by the scale factor for the");
      S.append(" corresponding detector pixel.");
      S.append("@assumptions    "); 
      S.append("The pixel sensitivity map file must be of the format written");
      S.append(" by WritePixelSensitivity or WritePixelSensitivity_2.");
      S.append("@param   ");
      S.append("The DataSet whose values are to be adjusted for");
      S.append(" the calculated pixel sensitivities.");
      S.append("@param   ");
      S.append("The name of the file containing the pixel sensitivity map.");
      S.append("@error ");
      S.append("Exceptions will be thrown if the sensitivity map file can't be read, or if");
      S.append(" there are detectors or spectra in the DataSet which do not have entries in the");
      S.append(" sensitivity map file.");
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

         DataSetTools.dataset.DataSet ds = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         java.lang.String filename = getParameter(1).getValue().toString();
         Operators.TOF_SCD.WritePixelSensitivity_calc.AdjustDataSetForPixelSensitivity(ds,filename );

         return "Success";
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



