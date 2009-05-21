/* 
 * File: SetFinalTOF_2.java
 *  
 * Copyright (C) 2009     Dennis Mikkelson
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

package Operators.TOF_DG_Spectrometer;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class SetFinalTOF_2 extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public SetFinalTOF_2(){
     super("SetFinalTOF_2");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "SetFinalTOF_2";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new SampleDataSetPG("SNS Total TOF DataSet",null));
      addParameter( new FloatPG("Incident Energy (meV)",50));
      addParameter( new FloatPG("t0_Shift to subtract from total TOF",0));
      addParameter( new IntegerPG("min tof channel (after pulse hits sample)",200));
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
      S.append("Switch the specified TOF_NDGS DataSet so the Data block's");
      S.append(" time-of-flight axis specifies the time-of-flight from the sample");
      S.append(" to the detector instead of from the moderator to the detector.");
      S.append(" The time-of-flight axes are assumed to be the same on all");
      S.append(" Data blocks in the DataSet.  A t0 shift value may specified.");
      S.append(" The t0 shift value is subtracted from the total time-of-flight.");
      S.append("@algorithm    "); 
      S.append("Three operations are performed.");
      S.append(" First, the total time-of-flight is corrected using the");
      S.append(" specified t0_shift value as:  t_total = t_exp - t0_shift.");
      S.append(" Second, the time-of-flight from the moderator to the");
      S.append(" sample is calculated based on Ein and the initial flight");
      S.append(" path.  This \"initial time-of-flight\" is subtracted from");
      S.append(" the corrected total time-of-flight, to obtain the");
      S.append(" sample to detector time-of-flight for each bin.");
      S.append(" Third, the initial portion of the data, corresponding");
      S.append(" to zero or negative sample to detector");
      S.append(" times-of-flight is removed.  In fact, some additional");
      S.append(" channels should be omitted, to keep the energies");
      S.append(" bounded.");
      S.append("@assumptions    "); 
      S.append("The DataSets must not be null or empty and should be");
      S.append(" raw time-of-flight DataSets recording counts relative to");
      S.append(" the TOTAL time of flight from the source to the detector.");
      S.append(" The Data blocks for individual spectra must have at");
      S.append(" least two data points and have basic attributes such as");
      S.append(" the initial path.");
      S.append("@param   ");
      S.append("The sample histogram DataSet to be adjusted");
      S.append(" to give sample to detector times-of-flight.");
      S.append("@param   ");
      S.append("The incident energy.");
      S.append("@param   ");
      S.append("The value to subtract from the total time-of");
      S.append(" flight to get a corrected total time-of-flight.");
      S.append("@param   ");
      S.append("The number of time channels to skip beyond");
      S.append(" the channel where the time-of-flight from the");
      S.append(" sample to detector is 0.  This must be at");
      S.append(" least 10 and more typically should be several");
      S.append(" hundred.");
      S.append("@error ");
      S.append("An IllegalArgumentException is thrown if the DataSet");
      S.append(" is null, empty or has Data blocks with only one value.");
      S.append(" Other exceptions will be thrown if the Data blocks don't");
      S.append(" have basic attributes such as the initial path.");
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
                     "TOF_NDGS"
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
         float Ein = ((FloatPG)(getParameter(1))).getfloatValue();
         float t0_shift = ((FloatPG)(getParameter(2))).getfloatValue();
         int n_channels_delay = ((IntegerPG)(getParameter(3))).getintValue();
         Operators.TOF_DG_Spectrometer.TOF_NDGS_Calc.SetFinalTOF(ds,Ein,t0_shift,n_channels_delay );
;
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
