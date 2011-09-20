/* 
 * File: WritePixelSensitivity_2.java
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
@see Operators.TOF_SCD.WritePixelSensitivity_calc#WritePixelSensitivity_2(java.lang.String,java.lang.String,java.lang.String,float,float,int,int)
 */
public class WritePixelSensitivity_2 extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public WritePixelSensitivity_2(){
     super("WritePixelSensitivity_2");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "WritePixelSensitivity_2";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Vanadium NeXus File Name",""));
      addParameter( new LoadFilePG("Background NeXus File Name",""));
      addParameter( new SaveFilePG("Pixel Sensitivity Map File",""));
      addParameter( new FloatPG("Min Time-of-Flight to Sum",0.0));
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
      S.append(" counts in a vanadium run file minus the summed pixel counts from a");
      S.append(" background run.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.WritePixelSensitivity_calc#WritePixelSensitivity_2(java.lang.String,java.lang.String,java.lang.String,float,float,int,int)");
      S.append("@algorithm    "); 
      S.append("The average sensitivity of pixels in a square");
      S.append(" neighborhood of each interior pixel is calculated.  For all interior");
      S.append(" pixels, the average total count over a square neighborhood of size");
      S.append(" (2*half-width+1)^2 is first calculated for both the vanadium run and");
      S.append(" the background run.  Based on the difference of these averages, the");
      S.append(" sensitivity scale factor for each pixel with a non-zero average is taken");
      S.append(" to be MAX_COUNT/ave_count, where MAX_COUNT is the maximum average");
      S.append(" count for any interior pixel in the detector, and ave_count is");
      S.append(" the average count for that pixel.  If the ave_count is zero for");
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
      S.append("The name of the NeXus file containing the");
      S.append(" vanadium data.");
      S.append("@param   ");
      S.append("The name of the NeXus file containing the");
      S.append(" background data.");
      S.append("@param   ");
      S.append("The name of the sensitivity file to write.");
      S.append("@param   ");
      S.append("The start of the interval of times-of-flight");
      S.append(" that will be summed.");
      S.append("@param   ");
      S.append("The end of the interval of times-of-flight");
      S.append(" that will be summed.");
      S.append("@param   ");
      S.append("The number of rows and columns around the edge");
      S.append(" of the detector that do not have any data, but");
      S.append(" are just zero.");
      S.append("@param   ");
      S.append("The distance from the center of a square");
      S.append(" neighborhood to the edge of the neighborhood.");
      S.append("@error ");
      S.append("Exceptions will be thrown if either of the NeXuf files can't be read,");
      S.append(" or if the detector configurations in the vanadium and bacground");
      S.append(" runs don't match.");
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

         java.lang.String van_file = getParameter(0).getValue().toString();
         java.lang.String back_file = getParameter(1).getValue().toString();
         java.lang.String sens_file = getParameter(2).getValue().toString();
         float min_tof = ((FloatPG)(getParameter(3))).getfloatValue();
         float max_tof = ((FloatPG)(getParameter(4))).getfloatValue();
         int zero_border = ((IntegerPG)(getParameter(5))).getintValue();
         int half_width = ((IntegerPG)(getParameter(6))).getintValue();
         Operators.TOF_SCD.WritePixelSensitivity_calc.WritePixelSensitivity_2(van_file,back_file,sens_file,min_tof,max_tof,zero_border,half_width );

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



