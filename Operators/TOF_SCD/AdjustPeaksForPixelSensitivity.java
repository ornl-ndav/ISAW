/* 
 * File: AdjustPeaksForPixelSensitivity.java
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
@see Operators.TOF_SCD.WritePixelSensitivity_calc#AdjustPeaksForPixelSensitivity(java.lang.String,java.lang.String,java.lang.String,float,float,int,int)
 */
public class AdjustPeaksForPixelSensitivity extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public AdjustPeaksForPixelSensitivity(){
     super("AdjustPeaksForPixelSensitivity");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "AdjustPeaksForPixelSensitivity";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Vanadium NeXus File Name",""));
      addParameter( new LoadFilePG("Integrated Peaks File",""));
      addParameter( new SaveFilePG("Adjusted Integrate File",""));
      addParameter( new FloatPG("Min Time-of_Flight to Sum",0));
      addParameter( new FloatPG("Max Time-of-Flight to Sum",16000));
      addParameter( new IntegerPG("Zero Pixel Border Size",8));
      addParameter( new IntegerPG("Region Half-Width",5));
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
      S.append("Adjust each peak intensity for the relative sensitivity of pixels");
      S.append(" in each detector, based on data from the specified NeXus file.  The");
      S.append(" NeXus file should contain data from a uniform scatterer, such as");
      S.append(" vanadium.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.WritePixelSensitivity_calc#AdjustPeaksForPixelSensitivity(java.lang.String,java.lang.String,java.lang.String,float,float,int,int)");
      S.append("@algorithm    "); 
      S.append("The average sensitivity of pixels in a square neighborhood");
      S.append(" of each interior pixel is calculated.  For all interior pixels, the");
      S.append(" average total count over a square neighborhood of size (2*half-width+1)^2");
      S.append(" is first calculated.  Based on these averages, the sensitivity scale");
      S.append(" factor for each pixel with a non-zero average total count is taken to");
      S.append(" be MAX_COUNT/ave_count, where MAX_COUNT is the maximum average total");
      S.append(" count for any interior pixel in the detector, and ave_count is");
      S.append(" the average total count for that pixel.  If the ave_count is zero for");
      S.append(" a pixel (as it will be for border pixels) the sensitivity scale");
      S.append(" factor is set to 1.  Therefore, this sensitivity scale factor");
      S.append(" is >= 1 for all pixels.  The peak intensity for each peak in the");
      S.append(" peaks file is multiplied by the sensitivity scale factor for the");
      S.append(" corresponding pixel in that detector.  The \"SIGI\" value is also");
      S.append(" multiplied by the same scale factor.");
      S.append("@assumptions    "); 
      S.append("The vanadium run file has data from all detectors listed in");
      S.append(" the integrated peaks file.");
      S.append("@param   ");
      S.append("The name of the NeXus file containing the");
      S.append(" vanadium data.");
      S.append("@param   ");
      S.append("The name of the peaks file whose intensities");
      S.append(" are to be adjusted.");
      S.append("@param   ");
      S.append("The name of the new peaks file that should be");
      S.append(" written containing the adjusted peak intensities.");
      S.append("@param   ");
      S.append("The start of the interval of times-of-flight");
      S.append(" that will be summed in the vanadium file.");
      S.append("@param   ");
      S.append("The end of the interval of times-of-flight");
      S.append(" that will be summed in the vanadium file.");
      S.append("@param   ");
      S.append("The number of rows and columns around the edge");
      S.append(" of the detector that do not have any data, but");
      S.append(" are just zero.");
      S.append("@param   ");
      S.append("The distance from the center of a square");
      S.append(" neighborhood to the edge of the neighborhood.");
      S.append(" Each square neighborhood has dimensions");
      S.append(" (2*zero_border + 1)^2.");
      S.append("@return No value is returned, but the the list of peaks with");
      S.append("");
      S.append(" adjusted intensities is written to the specified");
      S.append(" adjusted peaks file.");
      S.append("@error ");
      S.append("An exception will be thrown if there are integrated peaks");
      S.append(" from a detector that is not included in the vanadium file.");
      S.append(" An exception will be thrown if the vanadium file does not");
      S.append(" have data from area detectors.");
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

         java.lang.String nx_filename = getParameter(0).getValue().toString();
         java.lang.String peaks_file = getParameter(1).getValue().toString();
         java.lang.String adjusted_file = getParameter(2).getValue().toString();
         float min_tof = ((FloatPG)(getParameter(3))).getfloatValue();
         float max_tof = ((FloatPG)(getParameter(4))).getfloatValue();
         int zero_border = ((IntegerPG)(getParameter(5))).getintValue();
         int half_width = ((IntegerPG)(getParameter(6))).getintValue();
         Operators.TOF_SCD.WritePixelSensitivity_calc.AdjustPeaksForPixelSensitivity(nx_filename,peaks_file,adjusted_file,min_tof,max_tof,zero_border,half_width );

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



