/* 
 * File: OmitSpectraWithPeaks.java
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
public class OmitSpectraWithPeaks extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public OmitSpectraWithPeaks(){
     super("OmitSpectraWithPeaks");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "OmitSpectraWithPeaks";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet with data from one detector",DataSetTools.dataset.DataSet.EMPTY_DATA_SET));
      addParameter( new ArrayPG("Vector of BasicPeakInfo objects",null));
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
      S.append("Remove all spectra in the specified DataSet that are at row,col positions");
      S.append(" affected by the specified peaks.  The DataSet must only contain data");
      S.append(" from one area detector, and that must be the same as the detector in");
      S.append(" which the specified peaks were found.");
      S.append("@algorithm    "); 
      S.append("The selection flags are cleared and then any pixel within");
      S.append(" +-2 sigma of any peak is marked as selected.  When all");
      S.append(" such pixels have been selected, all selected Data blocks");
      S.append(" are removed from the DataSet.  The selection flags of the");
      S.append(" remaining Data blocks will all be cleared.");
      S.append("@assumptions    "); 
      S.append("The DataSet must contain data from ONE area detector.");
      S.append(" The vector of BasicPeakInfo objects must contain");
      S.append(" information about the peaks on that area detector.");
      S.append("@param   ");
      S.append("DataSet with data from one detector");
      S.append("@param   ");
      S.append("Vector of BasicPeakInfo objects containing list of peaks");
      S.append(" found in the DataSet");
      S.append("@return The DataSet is modified in place by removing Data");
      S.append("");
      S.append(" blocks that are near peaks.");
      S.append("@error ");
      S.append("An exception is thrown if the DataSet does not have");
      S.append(" data from an area grid.");
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
         java.util.Vector peaks = (java.util.Vector)(getParameter(1).getValue());
         Operators.TOF_Diffractometer.Omit_SCD_Peaks_Calc.OmitSpectraWithPeaks(ds,peaks );
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



