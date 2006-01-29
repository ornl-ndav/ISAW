/* 
 * File: VectorToSphericalDetectorPosition.java
 *  
 * Copyright (C) 2005     Kurtiss Olson
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
 * Revision 1.2  2006/01/29 20:27:16  dennis
 * Used dos2unix to convert to proper UNIX text files.
 *
 * Revision 1.1  2005/11/21 17:54:42  dennis
 * Hidden operators to pack and unpack DetectorPosition objects.
 * (Kurtiss Olson)
 *
 *
 */

package Operators.Generic.TypeConversion;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class VectorToSphericalDetectorPosition extends GenericOperator
                                               implements HiddenOperator
{
   public VectorToSphericalDetectorPosition(){
     super("VectorToSphericalDetectorPosition");
     }

   public String getCommand(){
      return "VectorToSphericalDetectorPosition";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Vector with DetectorPosition parameters",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Convert the values from a Vector containing numeric values into a");
      S.append("DetectorPosition object.");
      S.append("@algorithm    "); 
      S.append("Numerical values from the Vector are used for the spherical radius,");
      S.append("azimuth angle, and polar angle, and a DetectorPosition object is created");
      S.append("from those values.");
      S.append("@assumptions    "); 
      S.append("The Vector must have three numeric values.");
      S.append("@param   ");
      S.append("A Vector containing spherical radius, azimuth angle, and polar");
      S.append("angle for the spherical coordinates.");
      S.append("@return A DetectorPosition object, with spherical coordinates from the");
      S.append("values from the Vector, or an ErrorString.");
      S.append("@error ");
      S.append("If the Vector passed in does not have three numeric values,");
      S.append("then an ErrorString will be returned");
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

         java.lang.Object det_vector = (java.lang.Object)(getParameter(0).getValue());
         java.lang.Object Xres=Operators.Generic.TypeConversion.Convert.VectorToSphericalDetectorPostion(det_vector);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


