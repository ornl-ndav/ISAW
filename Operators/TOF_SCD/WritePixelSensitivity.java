/* 
 * File: WritePixelSensitivity.java
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
@see Operators.TOF_SCD.WritePixelSensitivity_calc#WritePixelSensitivity(java.lang.String,java.lang.String,float,float,int,int)
 */
public class WritePixelSensitivity extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public WritePixelSensitivity(){
     super("WritePixelSensitivity");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "WritePixelSensitivity";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Vanadium NeXus File Name",""));
      addParameter( new SaveFilePG("Pixel Sensitivity Map File",""));
      addParameter( new FloatPG("Min Time-of-Flight to Sum",0));
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
      S.append("Write a file with per-pixel scale factors based on the summed pixel");
      S.append(" counts in a vanadium run file.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.WritePixelSensitivity_calc#WritePixelSensitivity(java.lang.String,java.lang.String,float,float,int,int)");
      S.append("@algorithm    "); 
      S.append("The first file must contain data from");
      S.append(" a uniform scatterer, such as vanadium.  The average sensitivity of");
      S.append(" pixels in a square neighborhood of each interior pixel is calculated.");
      S.append(" For all interior pixels, the average total");
      S.append(" count over a square neighborhood of size (2*half-width+1)^2");
      S.append(" is first calculated.  Based on these averages, the sensitivity scale");
      S.append(" factor for each pixel with a non-zero average total count is taken to");
      S.append(" be MAX_COUNT/ave_count, where MAX_COUNT is the maximum average total");
      S.append(" count for any interior pixel in the detector, and ave_count is");
      S.append(" the average total count for that pixel.  If the ave_count is zero for");
      S.append(" a pixel (as it will be for border pixels) the sensitivity scale");
      S.append(" factor is set to 1.  Therefore, this sensitivity scale factor");
      S.append(" is >= 1 for all pixels.  The scale factor for each pixel of each");
      S.append(" detector is written to a simple binary file in PC (little-endian)");
      S.append(" format.  The first entry in the file is an integer giving the");
      S.append(" number of detector modules.  For each module the file contains");
      S.append(" (in sequence) three integers giving the bank number, the number of");
      S.append(" rows and the number of columns, followed by a list of 32-bit floats");
      S.append(" giving the sensitivity scale factor for each pixel, in row major order.");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The name of the NeXus file containing the vanadium data.");
      S.append("@param   ");
      S.append("The name of the sensitivity file to write.");
      S.append("@param   ");
      S.append("The start of the interval of times-of-flight that will be summed.");
      S.append("@param   ");
      S.append("The end of the interval of times-of-flight that will be summed.");
      S.append("@param   ");
      S.append("The number of rows and columns around the edge");
      S.append(" of the detector that do not have any data, but are just zero.");
      S.append("@param   ");
      S.append("The distance from the center of a square");
      S.append(" neighborhood to the edge of the neighborhood.");
      S.append(" Each square neighborhood has dimensions (2*zero_border + 1)^2.");
      S.append("@error ");
      S.append("An exception will be thrown if the input file can't be read or the");
      S.append(" output file can't be written.");
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

         java.lang.String nx_file = getParameter(0).getValue().toString();
         java.lang.String sens_file = getParameter(1).getValue().toString();
         float min_tof = ((FloatPG)(getParameter(2))).getfloatValue();
         float max_tof = ((FloatPG)(getParameter(3))).getfloatValue();
         int zero_border = ((IntegerPG)(getParameter(4))).getintValue();
         int half_width = ((IntegerPG)(getParameter(5))).getintValue();
         Operators.TOF_SCD.WritePixelSensitivity_calc.WritePixelSensitivity(nx_file,sens_file,min_tof,max_tof,zero_border,half_width );

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



