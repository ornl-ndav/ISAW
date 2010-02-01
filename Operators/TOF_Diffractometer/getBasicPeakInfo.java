/* 
 * File: getBasicPeakInfo.java
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
 * This work was supported by the SNS division of
 * Oak Ridge National Laboratory, Oak Ridge, Tennessee, USA.
 *
 *
 * Last Modified:
 *
 * $ Author: $
 * $Date: 2009-06-01 10:26:25 -0500 (Mon, 01 Jun 2009) $$
 * $Revision: 19721 $
 */

package Operators.TOF_Diffractometer;
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
public class getBasicPeakInfo extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public getBasicPeakInfo(){
     super("getBasicPeakInfo");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "getBasicPeakInfo";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet with data from ONE AREA DETECTOR",DataSetTools.dataset.DataSet.EMPTY_DATA_SET));
      addParameter( new IntegerPG("Maximum number of peaks to return",100));
      addParameter( new IntegerPG("Minimum peak intensity to consider",0));
      addParameter( new BooleanPG("Set true to smooth data",true));
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
      S.append("Get a Vector of BasicPeakInfo objects for specified DataSet with");
      S.append(" data from ONE AREA DETECTOR.  If the DataSet has data from more than");
      S.append(" one detector, peaks will only be found in the detector with the");
      S.append(" smallest ID.  Passing in a DataSet with data from more than one");
      S.append(" detector is inefficient.");
      S.append(" This method just gets a 3D histogram of values from the DataSet");
      S.append(" and then calls FindPeaksViaSort.getPeaks() to get the peaks.");
      S.append("@algorithm    "); 
      S.append("The FindPeaksViaSort method is used to construct");
      S.append(" a Vector of BasicPeakInfo objects.  Each of these objects");
      S.append(" contains information about the position and extent of");
      S.append(" a peak.  These objects are \"opaque\" from the scripting");
      S.append(" language.");
      S.append("@assumptions    "); 
      S.append("The DataSet should contain complete data from one area detector.");
      S.append("@param   ");
      S.append("DataSet with data from ONE AREA DETECTOR.");
      S.append("@param   ");
      S.append("Maximum number of peaks to return");
      S.append("@param   ");
      S.append("Minimum peak intensity to consider");
      S.append("@param   ");
      S.append("Pass in as true if data should be smoothed");
      S.append(" before searching for peaks.");
      S.append(" NOTE: If passed in as true, a copy of the");
      S.append(" data will be made so the DataSet will not");
      S.append(" be changed.");
      S.append("@return A Vector of BasicPeakInfo objects containing information about");
      S.append("");
      S.append(" the peaks that are found in the DataSet.");
      S.append("@error ");
      S.append("Exceptions are thrown if the DataSet is null or");
      S.append(" empty, or if it does not contain data from one");
      S.append(" area detector.");
      S.append(" An exception will also be thrown if there is not");
      S.append(" one Data block for each pixel of the detector.");
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
                     "TOF_NPD"
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
         int max_num_peaks = ((IntegerPG)(getParameter(1))).getintValue();
         int threshold = ((IntegerPG)(getParameter(2))).getintValue();
         boolean do_smoothing = ((BooleanPG)(getParameter(3))).getbooleanValue();
         java.util.Vector Xres=Operators.TOF_Diffractometer.Omit_SCD_Peaks_Calc.getBasicPeakInfo(ds,max_num_peaks,threshold,do_smoothing );

         return Xres;
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



