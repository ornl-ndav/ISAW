/* 
 * File: SetFinalTOF.java
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
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
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
public class SetFinalTOF extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public SetFinalTOF(){
     super("SetFinalTOF");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "SetFinalTOF";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new SampleDataSetPG("SNS Total TOF DataSet",null));
      addParameter( new FloatPG("Incident Energy (meV)",50.0));
      addParameter( new FloatPG("Fit coefficient 'a'",92.0));
      addParameter( new FloatPG("Fit coefficient 'b'",81.3));
      addParameter( new FloatPG("Fit coefficient 'c'",0.00130));
      addParameter( new FloatPG("Fit coefficient 'd'",-751.0));
      addParameter( new FloatPG("Fit coefficient 'r'",1.65));
      addParameter( new FloatPG("Fit coefficient 'g'",-14.5));
      addParameter( new IntegerPG("min tof channel (after pulse hits sample)",
                                   200));
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
      S.append("This operator switchs the specified TOF_NDGS");
      S.append(" DataSet so the Data block's time-of-flight");
      S.append(" axes all specify the time-of-flight from the");
      S.append(" sample to the detector instead of from the");
      S.append(" moderator to the detector.  The time-of-flight");
      S.append(" axes are assumed to be the same on all Data");
      S.append(" blocks in the DataSet.");
      S.append(" Three operations are performed.");
      S.append(" First, a corrected total time-of-flight is");
      S.append(" calculated, based on the the experimentally");
      S.append(" measured time of flight as:");
      S.append(" ");
      S.append(" t_total = t_exp -");
      S.append(" (1+tanh((Ei-a)/b))/(2*c*Ei)+");
      S.append(" (1-tanh((Ei-a)/b))/(2*d*Ei)+");
      S.append(" f+g*tanh((Ei-a)/b)");
      S.append(" ");
      S.append(" This funtion models the delay time of");
      S.append(" neutron emission from the moderator");
      S.append(" (Alexander Kolesnikov).");
      S.append(" ");
      S.append(" Second, the time-of-flight from the");
      S.append(" moderator to the sample is calculated");
      S.append(" based on Ein and the initial flight path.");
      S.append(" This \"initial time-of-flight\" is subtracted");
      S.append(" from the corrected total time-of-flight,");
      S.append(" to obtain the sample to detector");
      S.append(" time-of-flight.");
      S.append(" ");
      S.append(" Third, the initial portion of the data,");
      S.append(" corresponding to zero or negative");
      S.append(" sample to detector times-of-flight");
      S.append(" is removed.  In fact, some additional");
      S.append(" channels should be omitted, to keep the");
      S.append(" energies bounded.");
      S.append("@algorithm    "); 
      S.append("As described under overview, the total");
      S.append(" time-of-flight is adjusted for the delay");
      S.append(" of neutrons leaving the moderator, and");
      S.append(" the time-of-flight from the moderator to");
      S.append(" the sample is then subtracted to obtain");
      S.append(" the sample to detector time-of-flight.");
      S.append(" In order to keep the final energy calculation");
      S.append(" bounded the time-of-flight from sample");
      S.append(" to detector must be strictly greater than zero.");
      S.append(" This is done by omitting some additional");
      S.append(" time channels after the neutrons leave the");
      S.append(" sample.  The number of additional time");
      S.append(" channels to omit is specified as a paramater.");
      S.append("@assumptions    "); 
      S.append("The DataSets must not be null or empty.");
      S.append(" The Data blocks for individual spectra");
      S.append(" must have at least two data points and have");
      S.append(" basic attributes such as the initial path.");
      S.append("@param   ");
      S.append("The sample histogram DataSet to be");
      S.append(" adjusted to give sample to detector");
      S.append(" times-of-flight.");
      S.append("@param   ");
      S.append("The incident energy");
      S.append("@param   ");
      S.append("The 'a' coefficient in the correction equation");
      S.append("@param   ");
      S.append("The 'b' coefficient in the correction equation");
      S.append("@param   ");
      S.append("The 'c' coefficient in the correction equation");
      S.append("@param   ");
      S.append("The 'd' coefficient in the correction equation");
      S.append("@param   ");
      S.append("The 'f' coefficient in the correction equation");
      S.append("@param   ");
      S.append("The 'g' coefficient in the correction equation");
      S.append("@param   ");
      S.append("The number of time channels to skip");
      S.append(" beyond the channel where the");
      S.append(" time-of-flight from the sample");
      S.append(" to detector is 0.  This must be at");
      S.append(" least 10 and more typically should");
      S.append(" be several hundred.");
      S.append("@return The specified DataSet is modified by ");
      S.append("");
      S.append(" adjusting it's time scale and discarding");
      S.append(" all data before the neutron pulse hits");
      S.append(" the sample.");
      S.append("@error ");
      S.append("An IllegalArgumentException is thrown if the");
      S.append(" DataSet is null, empty or has Data blocks");
      S.append(" with only one value.  Other exceptions will");
      S.append(" be thrown if the Data blocks don't have");
      S.append(" basic attributes such as the initial path.");
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

         DataSetTools.dataset.DataSet ds = 
                 (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         float Ein = ((FloatPG)(getParameter(1))).getfloatValue();
         float a = ((FloatPG)(getParameter(2))).getfloatValue();
         float b = ((FloatPG)(getParameter(3))).getfloatValue();
         float c = ((FloatPG)(getParameter(4))).getfloatValue();
         float d = ((FloatPG)(getParameter(5))).getfloatValue();
         float f = ((FloatPG)(getParameter(6))).getfloatValue();
         float g = ((FloatPG)(getParameter(7))).getfloatValue();
         int n_channels_delay = ((IntegerPG)(getParameter(8))).getintValue();
         Operators.TOF_DG_Spectrometer.TOF_NDGS_Calc.
                   SetFinalTOF(ds,Ein,a,b,c,d,f,g,n_channels_delay );

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

