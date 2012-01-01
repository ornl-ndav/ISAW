/* 
 * File: MakeIntialTopazDetCal.java
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
@see EventTools.EventList.MakeTopazDetectors#MakeIntialTopazDetCal(java.lang.String,java.lang.String)
 */
public class MakeIntialTopazDetCal extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public MakeIntialTopazDetCal(){
     super("MakeIntialTopazDetCal");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "MakeIntialTopazDetCal";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Detector Position Info File",null));
      addParameter( new SaveFilePG("Name of file to write (.DetCal)",null));
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
      S.append("This operator reads and ASCII file giving the TOPAZ detector size,");
      S.append(" position and orientation information for each detector and writes");
      S.append(" out a new .DetCal file, based on that information.  The information");
      S.append(" is listed in the ASCII file following the convention of Matt Frost's");
      S.append(" .pdf file describing the positions of the TOPAZ detectors.");
       S.append("\r\n");
     S.append(" This operator wraps the method EventTools.EventList.MakeTopazDetectors#MakeIntialTopazDetCal(java.lang.String,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("Each detector is first constructed along the beam line,");
      S.append(" using the size and distance values.  It is then");
      S.append(" rotated to it's correct position using the angles theta");
      S.append(" and omega specified in the file.");
      S.append("@assumptions    "); 
      S.append("The input file must have one line for each TOPAZ detector.");
      S.append(" The line must start with the characters Det, followed by a space");
      S.append(" and must contain the following information, in order, for");
      S.append(" each detector.");
      S.append(" id, width, height, depth, n_rows, n_cols, omega, chi, distance.");
      S.append(" The detector sizes are expressed in meters and the detector");
      S.append(" distances are all expressed in centimeters.");
      S.append(" NOTE: An example file is included in the ISAW distribution");
      S.append(" as ../ISAW/InstrumentInfo/SNS/TOPAZ/SAMPLE_TOPAZ_DETECTOR_DATA.txt");
      S.append("@param   ");
      S.append("The name of the ASCII file containing the detector position");
      S.append(" and orientation information.  See the sampe file:");
      S.append(" ../ISAW/InstrumentInfo/SNS/TOPAZ/SAMPLE_TOPAZ_DETECTOR_DATA.txt");
      S.append("@param   ");
      S.append("The name of the .DetCal file that should be written.");
      S.append("@return A string specifying what file was written.");
      S.append("@error ");
      S.append("An exception is thrown if the ASCII file can not be opened");
      S.append(" and read properl");
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

         java.lang.String det_data_file = getParameter(0).getValue().toString();
         java.lang.String det_cal_file = getParameter(1).getValue().toString();
         java.lang.String Xres=EventTools.EventList.MakeTopazDetectors.MakeIntialTopazDetCal(det_data_file,det_cal_file );

         return Xres;
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



