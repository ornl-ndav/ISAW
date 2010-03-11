/* 
 * File: RemovePeaks.java
 *  
 * Copyright (C) 2010     Dennis Mikkelson
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
 *            MSCS Deparatment
 *            HH237H
 *            Menomonie, WI. 54751
 *
 * This work was supported by the SNS division of Oak Ridge National 
 * Laboratory, Oak Ridge, Tennessee, USA.
 *
 * Last Modified:
 *
 * $Author$
 * $Date$
 * $Revision$
 */

package Operators.TOF_Diffractometer;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.dataset.*;
import DataSetTools.util.SharedData;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class RemovePeaks extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public RemovePeaks(){
     super("RemovePeaks");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "RemovePeaks";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      String vanadium_file = SharedData.getProperty("ISAW_HOME","") +
                             "/Databases/VanadiumPeaks.dat";
      clearParametersVector();
      addParameter( new DataSetPG("DataSet With Peaks to Remove",
                                   DataSet.EMPTY_DATA_SET));
      addParameter( new LoadFilePG("File Listing d Values of Peaks to Remove", 
                                    vanadium_file ));
      addParameter( new FloatPG("Estimated Peak Width(delta_d/d)",0.005));
      addParameter( new FloatPG("Interval to Replace (Times Peak Width)",1.9));
      addParameter( new IntegerPG("Number of Channels to Average",10));
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
      S.append("Replace small intervals of spectra that have a");
      S.append(" peak that should not be included, with a linear");
      S.append(" interpolation of average values below and");
      S.append(" above the peak extent.  One use of this");
      S.append(" method is to remove peaks in a vanadium");
      S.append(" spectrum, before smoothing the spectrum.");
      S.append("@algorithm    "); 
      S.append("For each Data block in the DataSet, the position");
      S.append(" of each peak listed in the file will be converted to");
      S.append(" time-of-flight, using the flight path and detector");
      S.append(" information.  Two points left and right of the peak");
      S.append(" position are determined by the peak width");
      S.append(" (converted to time-of-flight) and the fractional");
      S.append(" number of peak widths to use left and right of");
      S.append(" the peaks.  These two points determine an");
      S.append(" interval over which the values will be replaced");
      S.append(" by a linear function.  The linear function interpolates");
      S.append(" N measured values immediately below the left");
      S.append(" end point and N measured values immediately above");
      S.append(" the right endpoint.");
      S.append("@assumptions    "); 
      S.append("The DataSet must be a time-of-flight DataSet");
      S.append(" with attributes specifying the initial flight path");
      S.append(" and effective detector position for each Data");
      S.append(" block.");
      S.append(" The specified file listing peaks to remove must");
      S.append(" contain lines with at least four columns specifying");
      S.append(" h,k,l,d for each peak.  The h,k and l values are");
      S.append(" currently ignored, but must be included.");
      S.append("@param   ");
      S.append("DataSet with spectra from which isolated peaks");
      S.append(" will be removed.  The spectra in this DataSet");
      S.append(" must be interms of time-of-flight and the");
      S.append(" Data blocks MUST have the effective position");
      S.append(" and initial path attributes.");
      S.append("@param   ");
      S.append("ASCII file listing the h,k,l and d-spacing in");
      S.append(" Angstroms, for the peaks that are to be removed");
      S.append(" from the spectra.  Each line of the file must");
      S.append(" have at least those four values.  Only the");
      S.append(" d-spacing value is currently used, but the");
      S.append(" first three numbers (representing h,k,l) must");
      S.append(" be present, since they are read and skipped");
      S.append(" to get to the d-space value.  Lines starting");
      S.append(" with a '#' symbol are ignored.");
      S.append("@param   ");
      S.append("Estimate of the width of the peaks specified");
      S.append(" by delta_d/d.  The actual width used will");
      S.append(" depend on the d_value as width = d*delta_d_over_d.");
      S.append(" This width is converted to be in terms of");
      S.append(" time-of-flight.");
      S.append("@param   ");
      S.append("Fractional number of peak widths, left and");
      S.append(" right of the nominal position, that defines the");
      S.append(" interval over which values will be replaced by");
      S.append(" interpolated values.  This is restricted to be");
      S.append(" between 1 and 20, so the values on the interval");
      S.append(" [peak-replace_dist*width,peak+replace_dist*width]");
      S.append(" will be replaced by linearly interpolated values.");
      S.append(" This interval is considered to be the peak");
      S.append(" extent.");
      S.append("@param   ");
      S.append("The number of channels below and above the");
      S.append(" peak extent that will be averaged to obtain");
      S.append(" values left and right of the peak postion.");
      S.append(" The actual values of the histogram will be");
      S.append(" replaced by values that are linearly");
      S.append(" interpolated between these average values.");
      S.append(" This is restricted to be between 1 and 100.");
      S.append("@return The y-values of each Data block in the DataSet");
      S.append("");
      S.append(" are altered in place.");
      S.append("@error ");
      S.append("This operator will fail under the following conditions:");
      S.append(" 1. If the DataSet is null or empty.");
      S.append(" 2. If the DataSet is not a time-of-flight DataSet.");
      S.append(" 3. If the DataSet is not a histogram.");
      S.append(" 4. If the peak file doesn't exist or can't be read.");
      S.append(" 5. If delta_d/d is less than or equal to zero.");
      S.append(" 6. If the replace_distance is less than 1 or more than 20.");
      S.append(" 7. If the num_to_average is less than 1 or more than 100.");
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
                     "TOF_NPD",
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
         DataSetTools.dataset.DataSet ds = 
                 (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         java.lang.String peak_file = getParameter(1).getValue().toString();
         float delta_d_over_d = ((FloatPG)(getParameter(2))).getfloatValue();
         float replace_dist = ((FloatPG)(getParameter(3))).getfloatValue();
         int num_to_average = ((IntegerPG)(getParameter(4))).getintValue();

         String units = ds.getX_units();
         if ( units.equals("Time(us)") )
           RemovePeaks_Calc.RemovePeaks_tof
                  ( ds,peak_file,delta_d_over_d,replace_dist,num_to_average );
         else if ( units.equals( "Angstroms" ) )
           RemovePeaks_Calc.RemovePeaks_d
                 ( ds,peak_file,delta_d_over_d,replace_dist,num_to_average );
         else
           throw new IllegalArgumentException( "DataSet must have x-units " +
                                               "Angstroms or Time(us)" );

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
