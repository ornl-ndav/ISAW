/* 
 * File: MonitorPeakArea.java
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
public class MonitorPeakArea extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public MonitorPeakArea(){
     super("MonitorPeakArea");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "MonitorPeakArea";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("Monitor DataSet",null));
      addParameter( new IntegerPG("ID of Monitor to Integrate",2));
      addParameter( new FloatPG("Incident Energy Estimate (meV)",50));
      addParameter( new FloatPG("Search Window half-width (micro-seconds)",500));
      addParameter( new FloatPG("Peak Area Extent Factor (multiple of FWHM)",8.5));
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
      S.append("Calculate the area under the specified peak in the");
      S.append(" specified monitor.  The peak to integrate is");
      S.append(" determined by finding the largest histogram bin");
      S.append(" within the specified time-of-flight interval around");
      S.append(" the specified incident energy.  An estimate of");
      S.append(" a linear background is subtracted from the");
      S.append(" peak area.");
      S.append("@algorithm    "); 
      S.append("The specified incident energy estimate is mapped");
      S.append(" to a time-of-flight based on the initial path length.");
      S.append(" The interval of length 2*tof_half_interval is scanned");
      S.append(" to find the largest value, and the peak is assumed");
      S.append(" to be at that point.  The FWHM of the peak is then");
      S.append(" determined and an interval of length");
      S.append(" extent_factor*FWHM is considered.  A linear");
      S.append(" background is estimated samples around each");
      S.append(" end point of that interval.  The peak area is");
      S.append(" then obtained by summing the counts over");
      S.append(" that interval, minus the area under the linear");
      S.append(" backgroun.");
      S.append("@assumptions    "); 
      S.append("The specified DataSet must have a group with");
      S.append(" the specified ID and that Data block must have");
      S.append(" a peak within the specified time-of-flight");
      S.append(" interval around the specified incident energy.");
      S.append("@param   ");
      S.append("A monitor DataSet containing the specified");
      S.append(" monitor data.");
      S.append("@param   ");
      S.append("The id of the monitor to integrate.");
      S.append("@param   ");
      S.append("Estimate of the incident energy(meV), used");
      S.append(" to find the time-of-flight that will be at the");
      S.append(" center of a window containing the incident");
      S.append(" neutron pulse in the monitor spectrum.");
      S.append("@param   ");
      S.append("Half-width in microseconds of the");
      S.append(" time-of-flight windows containing the");
      S.append(" incident neutron pulse in monitor spectra.");
      S.append(" The search for the peak is restricted to");
      S.append(" the time-of-flight corresponding to the");
      S.append(" estimated Ein, plus or minus the");
      S.append(" tof_half_interval.");
      S.append("@param   ");
      S.append("The extent factor to use for the peak.  The");
      S.append(" peak will be considered to have zero counts");
      S.append(" outside of the interval extent_factor*FWHM.");
      S.append(" The integration and background estimate is");
      S.append(" done on the interval of length");
      S.append(" extent_factor*FWHM.");
      S.append("@return The integrated monitor counts over the interval ");
      S.append("");
      S.append(" of length extent_factor*FWHM, centered on the");
      S.append(" peak maximum, minus an estimated linear");
      S.append(" background.");
      S.append("@error ");
      S.append("");
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

         DataSetTools.dataset.DataSet mon_ds = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         int mon_id = ((IntegerPG)(getParameter(1))).getintValue();
         float Ein_estimate = ((FloatPG)(getParameter(2))).getfloatValue();
         float tof_half_interval = ((FloatPG)(getParameter(3))).getfloatValue();
         float extent_factor = ((FloatPG)(getParameter(4))).getfloatValue();
         float Xres=Operators.TOF_DG_Spectrometer.TOF_NDGS_Calc.MonitorPeakArea(mon_ds,mon_id,Ein_estimate,tof_half_interval,extent_factor );

         return new Float(Xres);
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



