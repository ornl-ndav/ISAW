/* 
 * File: GoniometerAnglesFromPhiRotation.java
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
@see Operators.TOF_SCD.FindGonAnglesFromPHI_Rotation#GoniometerAnglesFromPhiRotation(java.lang.String,java.lang.String,float,float,float,float)
 */
public class GoniometerAnglesFromPhiRotation extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public GoniometerAnglesFromPhiRotation(){
     super("GoniometerAnglesFromPhiRotation");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "GoniometerAnglesFromPhiRotation";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("First Peaks File",""));
      addParameter( new LoadFilePG("Second Peaks File (new PHI angle)",""));
      addParameter( new FloatPG("Change in PHI angle",15));
      addParameter( new FloatPG("Minimum Unit Cell Edge",3));
      addParameter( new FloatPG("Maximum Unit Cell Edge",12));
      addParameter( new FloatPG("Tolerance (for indexing)",0.12));
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
      S.append("Estimate the goniometer angles from two peaks files, that were");
      S.append(" measured at the same Chi and Omega angles, but different Phi angles.");
      S.append(" NOTE: This will occasionally fail by finding other rotations that index");
      S.append(" a large number of peaks that are symmetry equivalent to the correctly");
      S.append(" rotated peaks.  It should be possible to avoid this problem by choosing");
      S.append(" a different PHI rotation.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.FindGonAnglesFromPHI_Rotation#GoniometerAnglesFromPhiRotation(java.lang.String,java.lang.String,float,float,float,float)");
      S.append("@algorithm    "); 
      S.append("This method first finds a UB corresponding to the first set of");
      S.append(" peaks using the FFT based method.  It then tests a large number of");
      S.append(" rotations by the specified Phi angle, about different axes, applied to UB,");
      S.append(" to find the one that best indexes the second set of peaks.  That second");
      S.append(" UB is then optimized.  Finally, given the two UB matrices, the axis and");
      S.append(" and angle of rotation that maps the a*, b* and c* vectors for the first");
      S.append(" UB to the a*, b* and c* vectors for the second UB are calculated, and");
      S.append(" the corresponding Phi, Chi and Omega angles are calculated.");
      S.append("@assumptions    "); 
      S.append("The two peaks files must come from the same sample, with the only");
      S.append(" change between the measurements being that the PHI angle was");
      S.append(" changed.  There must be enough good peaks in the first file to");
      S.append(" properly index them using the FFT based indexing method.");
      S.append("@param   ");
      S.append("Fully qualified name of the first peaks file.");
      S.append("@param   ");
      S.append("Fully qualified name of the second peaks file.");
      S.append("@param   ");
      S.append("The amount the Phi angle was changed between measuring");
      S.append(" the first set of peaks and the second set of peaks.");
      S.append("@param   ");
      S.append("Lower bound on real space primitive unit cell edges.");
      S.append("@param   ");
      S.append("Upper bound on real space primitive unit cell edges.");
      S.append("@param   ");
      S.append("Tolerance on h,k,l for determining which peaks are");
      S.append(" indexed.");
      S.append("@return A Vector with three entries, the estimated PHI, CHI and OMEGA angles,");
      S.append("");
      S.append(" in that order.");
      S.append("@error ");
      S.append("An exception will be thrown if the files don't exist, or if an");
      S.append(" inconsistent rotation is found.");
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

         java.lang.String file_1 = getParameter(0).getValue().toString();
         java.lang.String file_2 = getParameter(1).getValue().toString();
         float phi = ((FloatPG)(getParameter(2))).getfloatValue();
         float min_d = ((FloatPG)(getParameter(3))).getfloatValue();
         float max_d = ((FloatPG)(getParameter(4))).getfloatValue();
         float tolerance = ((FloatPG)(getParameter(5))).getfloatValue();
         java.util.Vector Xres=Operators.TOF_SCD.FindGonAnglesFromPHI_Rotation.GoniometerAnglesFromPhiRotation(file_1,file_2,phi,min_d,max_d,tolerance );

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



