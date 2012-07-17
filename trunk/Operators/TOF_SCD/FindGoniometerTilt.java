/* 
 * File: FindGoniometerTilt.java
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
@see Operators.TOF_SCD.PeaksFileUtils#FindGoniometerError(java.lang.String,float,float)
 */
public class FindGoniometerTilt extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public FindGoniometerTilt(){
     super("FindGoniometerTilt");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "FindGoniometerTilt";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Indexed Peaks File",""));
      addParameter( new FloatPG("Maximum Tilt Angle (in degrees)",5));
      addParameter( new FloatPG("Tolerance on Indexing",0.12));
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
      S.append("This operator does a brute force search for the goniometer tilt angles");
      S.append(" that maximize the number of peaks indexed by ONE UB.  The list of");
      S.append(" peaks should include peaks from runs with several different");
      S.append(" goniometer settings.  The peaks must be indexed consistently,");
      S.append(" which can be done with IndexMultipleRuns() method.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.PeaksFileUtils#FindGoniometerError(java.lang.String,float,float)");
      S.append("@algorithm    "); 
      S.append("The 3-dimensional space of possible tilt angles is sampled");
      S.append(" at several different resolutions covering a range of");
      S.append(" [-max_angle,max_angle] for rotations of the goniometer assembly about");
      S.append(" the x, y and z axes.  If there is reasonable agreement between the");
      S.append(" estimated tilt angles for the different resolutions, the estimate should");
      S.append(" be useful.  If there is no agreement for the different resolutions, it");
      S.append(" is likely that different local minima were found, which may mean the");
      S.append(" goniometer is already quite well oriented, so there is no general trend");
      S.append(" in the data that can be used to estimate the tilt angles.");
      S.append(" NOTE: Information about the results from each resolution sampling");
      S.append(" is printed to the command window from which ISAW was started.");
      S.append("@assumptions    "); 
      S.append("The peaks are from multiple runs with different");
      S.append(" goniometer settings, and are indexed consistently.");
      S.append("@param   ");
      S.append("File of indexed peaks from mulitple runs with different");
      S.append(" goniometer settings.");
      S.append("@param   ");
      S.append("The maximum tilt angle to try for any of the");
      S.append(" goniometer \"tilts\" Rx, Ry, Rz, in degrees.");
      S.append("@param   ");
      S.append("The tolerance on Miller indices for a peak to be");
      S.append(" considered indexed");
      S.append("@return A Vector with four entries, the maximum number of peaks");
      S.append("");
      S.append(" indexed, and the estimated rotation angles about the x, y and");
      S.append(" z axes, respectively.  The positive x-axis is in the direction of");
      S.append(" neutron beam travel, the positive z-axis is vertically upward and");
      S.append(" the positive y-axis is in the horizontal plane, chosen so that the");
      S.append(" x,y,z directions form a right-handed orthonormal coordinate");
      S.append(" system.");
      S.append("@error ");
      S.append("If the peaks are not consistently indexed, the number that can");
      S.append(" be indexed simultaneously with one UB will be very small and");
      S.append(" the estimated rotation angles will be meaningless.  Check the");
      S.append(" output on the command window.");
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
         float max_angle = ((FloatPG)(getParameter(1))).getfloatValue();
         float tolerance = ((FloatPG)(getParameter(2))).getfloatValue();
         java.util.Vector Xres=Operators.TOF_SCD.PeaksFileUtils.FindGoniometerTilt(peaks_file,max_angle,tolerance );

         return Xres;
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



