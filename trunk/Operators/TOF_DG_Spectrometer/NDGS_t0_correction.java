/* 
 * File: NDGS_t0_correction.java
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
public class NDGS_t0_correction extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public NDGS_t0_correction(){
     super("NDGS_t0_correction");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "NDGS_t0_correction";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new FloatPG("Estimated Incident Energy",92));
      addParameter( new FloatPG("Neutron Delay Coefficient 'a'",92));
      addParameter( new FloatPG("Neutron Delay Coefficient 'b'",81.3));
      addParameter( new FloatPG("Neutron Delay Coefficient 'c'",0.0013));
      addParameter( new FloatPG("Neutron Delay Coefficient 'd'",-751.0));
      addParameter( new FloatPG("Neutron Delay Coefficient 'f'",1.65));
      addParameter( new FloatPG("Neutron Delay Coefficient 'g'",-14.5));
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
      S.append("Calculate an energy dependent t0 correction for");
      S.append(" the total time-of-flight.  The correction should be");
      S.append(" subtracted from the experimentally determined");
      S.append(" time of flight.  The correction is:");
      S.append("@algorithm    "); 
      S.append("The Neutron Delay correction, t0_shift, is calculated as:");
      S.append(" (1+tanh((Ei-a)/b))/(2*c*Ei)+(1-tanh((Ei-a)/b))/(2*d*Ei)+f+g*tanh((Ei-a)/b)");
      S.append(" This funtion models the delay time of neutron emission from the");
      S.append(" moderator. (Alexander Kolesnikov).");
      S.append("@assumptions    "); 
      S.append("");
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
      S.append("@return The t0_shift that should be subtracted from the ");
      S.append("");
      S.append(" experimentally measured time-of-flight.");
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

         float Ein = ((FloatPG)(getParameter(0))).getfloatValue();
         float a = ((FloatPG)(getParameter(1))).getfloatValue();
         float b = ((FloatPG)(getParameter(2))).getfloatValue();
         float c = ((FloatPG)(getParameter(3))).getfloatValue();
         float d = ((FloatPG)(getParameter(4))).getfloatValue();
         float f = ((FloatPG)(getParameter(5))).getfloatValue();
         float g = ((FloatPG)(getParameter(6))).getfloatValue();
         float Xres=Operators.TOF_DG_Spectrometer.TOF_NDGS_Calc.NDGS_t0_correction(Ein,a,b,c,d,f,g );

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



