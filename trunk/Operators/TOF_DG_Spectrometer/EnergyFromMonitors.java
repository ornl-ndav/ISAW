/* 
 * File: EnergyFromMonitors.java
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
 *
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
public class EnergyFromMonitors extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public EnergyFromMonitors(){
     super("EnergyFromMonitors");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "EnergyFromMonitors";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new MonitorDataSetPG("Monitor DataSet",null));
      addParameter( new FloatPG("Incident Energy Estimate (meV)",50));
      addParameter( new FloatPG("Window half-width (micro-seconds)",500));
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
      S.append("This operator uses the first two Data blocks");
      S.append(" from the monitor DataSet to calculate");
      S.append(" the incident energy, given an estimate of");
      S.append(" the incident energy and information about");
      S.append(" the width of a time-of-flight window");
      S.append(" centered on that incident energy, that");
      S.append(" should be used to locate the incident");
      S.append(" pulse in a monitor spectrum.");
      S.append("@algorithm    "); 
      S.append("The specified incident energy is converted");
      S.append(" to a corresponding time-of-flight for each");
      S.append(" beam monitor spectrum.  The spectrum is");
      S.append(" restricted to a window with the specified");
      S.append(" time-of-flight half width.  This window is");
      S.append(" assumed to contain the incident neutron");
      S.append(" pulse.  The centroid of the pulse in each");
      S.append(" spectrum is found and used to calculate");
      S.append(" the incident energy.  NOTE: Currently a");
      S.append(" very crude centroid algorithm is used.");
      S.append(" It should be possible to get a more accurate");
      S.append(" calculation of Ein by using a better peak");
      S.append(" fitting algorithm.");
      S.append("@assumptions    "); 
      S.append("The DataSet must contain data from the");
      S.append(" instruments beam monitors.  The first two");
      S.append(" entries in the DataSet must be from two");
      S.append(" distinct beam monitors at different postions");
      S.append(" along the beam.");
      S.append("@param   ");
      S.append("A monitor DataSet containing two distinct");
      S.append(" beam monitors as the first two entries.");
      S.append("@param   ");
      S.append("Estimate of the incident energy(meV),");
      S.append(" used to find the time-of-flight that will");
      S.append(" be at the center of windows containing");
      S.append(" the incident neutron pulse in the monitor");
      S.append(" spectra.");
      S.append("@param   ");
      S.append("Half-width in microseconds of the");
      S.append(" time-of-flight windows containing the");
      S.append(" incident neutron pulse in monitor spectra.");
      S.append("@error ");
      S.append("This operator will throw an illegal argument");
      S.append(" exception if the DataSet is null or has less");
      S.append(" than two entries.  It will also thow exceptions");
      S.append(" if the Data does not have the needed basic");
      S.append(" attributes such as initial path length and");
      S.append(" detector position.");
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

         DataSetTools.dataset.DataSet mon_ds = 
                 (DataSetTools.dataset.DataSet)(getParameter(0).getValue());

         float Ein_estimate = ((FloatPG)(getParameter(1))).getfloatValue();

         float tof_half_interval = ((FloatPG)(getParameter(2))).getfloatValue();

         float Xres=Operators.TOF_DG_Spectrometer.TOF_NDGS_Calc.
                    EnergyFromMonitors(mon_ds,Ein_estimate,tof_half_interval );

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
