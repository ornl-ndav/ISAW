/* 
 * File: SelfShielding.java
 *  
 * Copyright (C) 2007     Dennis Mikkelson
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
 *            Menomonie, Wisconsin
 *            54751
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2007/06/28 20:56:18  dennis
 * Operator to do self shielding calculation for direct geometry
 * spectrometer.  This operator "wraps" the SelfShielding
 * static method from class SelfShieldingCalc.
 *
 *
 */

package Operators.TOF_DG_Spectrometer;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class SelfShielding extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public SelfShielding(){
     super("SelfShielding");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "SelfShielding";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet to correct for self-shielding",DataSetTools.dataset.DataSet.EMPTY_DATA_SET));
      addParameter( new FloatPG("Inverse scattering length * sample thickness",0));
      addParameter( new FloatPG("Inverse absorption length * sample thickness",0));
      addParameter( new FloatPG("Sample Angle (degrees)",45));
      addParameter( new BooleanPG("Make New DataSet?",true));
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
      S.append("This operator will correct the specified spectrometer DataSet for");
      S.append(" self-shielding by the sample.");
      S.append("@algorithm    "); 
      S.append("The incident energy, and scattering angle are obtained from");
      S.append(" the Data attributes for each Data block.  For each Data block,");
      S.append(" the self-shielding factor is calculated at each sample point.");
      S.append(" If the data is in terms of time-of-flight, the energy is first");
      S.append(" calculated at each sample point to use in the self-shielding");
      S.append(" calculation.");
      S.append(" If the data is in terms of energy loss, the corresponding");
      S.append(" final energy is first calculated to use in the self-shielding");
      S.append(" calculation.");
      S.append(" The calculated self-shielding values are multiplied times");
      S.append(" the corresponding y-values and errors to obtain the");
      S.append(" corrected data.  The corrected data is returned in the");
      S.append(" first positon of the returned vector.  The original DataSet");
      S.append(" is also altered to be the corrected data, if the make new");
      S.append(" DataSet parameter is false.");
      S.append(" The self shielding factors are stored in a DataSet and");
      S.append(" returned as the second entry in the return vector.  The");
      S.append(" x-axis is the same (time-of-flight or energy loss)");
      S.append(" as the original DataSet.");
      S.append("@assumptions    "); 
      S.append("The DataSet must be in terms of EnergyLoss or time-of-flight.");
      S.append("@param   ");
      S.append("The DataSet to correct for self-shielding.");
      S.append("@param   ");
      S.append("Inverse scattering length * sample thickness.");
      S.append("@param   ");
      S.append("Inverse absorption length * sample thickness.");
      S.append("@param   ");
      S.append("Acute angle between a vector perpendicular to");
      S.append(" the sample surface and the beam direction.");
      S.append("@param   ");
      S.append("Flag indicating whether the corrected data should");
      S.append(" be placed in a new DataSet or if the current");
      S.append(" DataSet should just be altered to have the");
      S.append(" corrected data.");
      S.append("@return A vector with two DataSets is returned.  ");
      S.append(" The first DataSet ");
      S.append(" contains the corrected data from the original DataSet.");
      S.append(" The second DataSet contains the values of the self");
      S.append(" shielding factor, self, that was calculated and");
      S.append(" applied to each y-value in the spectum, and");
      S.append(" to the estimated error at each y-value.");
      S.append("@error ");
      S.append("An illegal argument exception is thrown if the DataSet");
      S.append(" argument is null, or if it has no Data blocks.");
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
         float mutS = ((FloatPG)(getParameter(1))).getfloatValue();
         float mutA = ((FloatPG)(getParameter(2))).getfloatValue();
         float gamma = ((FloatPG)(getParameter(3))).getfloatValue();
         boolean make_new_ds = ((BooleanPG)(getParameter(4))).getbooleanValue();

         java.util.Vector Xres=Operators.TOF_DG_Spectrometer.SelfShieldingCalc.SelfShielding(ds,mutS,mutA,gamma,make_new_ds );

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



