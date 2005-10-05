/* 
 * File: GsasCalibToVector.java
 *  
 * Copyright (C) 2005     Dennis Mikkelson
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
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2005/10/05 02:49:33  dennis
 * Converted from dos to unix text.
 *
 * Revision 1.1  2005/10/04 23:01:32  dennis
 * Operator generated around method in
 * Operators/Generic/TypeConversions/Convert class.
 *
 *
 */

package Operators.Generic.TypeConversion;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class GsasCalibToVector extends GenericOperator
                               implements HiddenOperator
{
   public GsasCalibToVector(){
     super("GsasCalibToVector");
     }

   public String getCommand(){
      return "GsasCalibToVector";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("GsasCalib object",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Convert an object of type GsasCalib, that contains values for");
      S.append("dif_C, dif_A and t_zero, to a Vector, containing these three values");
      S.append("in that order.");
      S.append("@algorithm    "); 
      S.append("The values for dif_C, dif_A and t_zero are extracted from");
      S.append("the GsasCalib object and placed as Float objects in");
      S.append("the Vector, in that order.");
      S.append("@assumptions    "); 
      S.append("The GsasCalib object must not be null.");
      S.append("@param   ");
      S.append("A GsasCalib object");
      S.append("@error ");
      S.append("If something other than a gsas_calib object is passed in,");
      S.append("an ErrorString will be returned.");
      return S.toString();
   }


   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "Utils",
                     "Convert"
                     };
   }


   public Object getResult(){
      try{

         java.lang.Object gsas_calib = (java.lang.Object)(getParameter(0).getValue());
         java.lang.Object Xres=Operators.Generic.TypeConversion.Convert.GsasCalibToVector(gsas_calib);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


