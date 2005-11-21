/* 
 * File: DetectorPositionToVector.java
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
 * Revision 1.1  2005/11/21 17:54:41  dennis
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
public class DetectorPositionToVector extends GenericOperator
                                      implements HiddenOperator
{
   public DetectorPositionToVector(){
     super("DetectorPositionToVector");
     }

   public String getCommand(){
      return "DetectorPositionToVector";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("DetectorPosition object",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Convert an object of type DetectorPosition, that contains values for");
      S.append("spherical coordinates, cartesian coordinates, cylinderical coordinates,");
      S.append("and the scattering angle, to a Vector, containing these ten values in");
      S.append("that order.");
      S.append("@algorithm    "); 
      S.append("The three values for the spherical coordinates, three values for cartesian");
      S.append("coordinates, three values for cylinderical coordinates, and the scattering");
      S.append("angle are extracted from the DetectorPosition object and placed as either");
      S.append("Float or Double objects depending on the precision in the vector, in that order.");
      S.append("@assumptions    "); 
      S.append("The DetectorPosition object must not be null.");
      S.append("@param   ");
      S.append("A DetectorPosition object.");
      S.append("@return A Vector containing sphere radius, azimuth angle, polar angle,");
      S.append("x, y, z, cylinder radius, cylinder azimuth angle, cylinder z,");
      S.append("and the scattering angle in that order.  If something other");
      S.append("than a DetectorPosition object is passed in, an ErrorString will");
      S.append("be returned.");
      S.append("@error ");
      S.append("If something other than a det_position object is passed in,");
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

         java.lang.Object det_position = (java.lang.Object)(getParameter(0).getValue());
         java.lang.Object Xres=Operators.Generic.TypeConversion.Convert.DetectorPositionToVector(det_position);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


