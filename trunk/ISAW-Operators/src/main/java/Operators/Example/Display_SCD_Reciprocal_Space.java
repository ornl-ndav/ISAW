/* 
 * File: Display_SCD_Reciprocal_Space.java
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
 *            Menomonie, WI. 54751
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2005/06/20 16:33:29  dennis
 * Initial Version of Operator to display a list of SCD
 * DataSets in the reciprocal space viewer.  The DataSets
 * must contain all necessary attributes and be stored
 * in a Vector.  (Previous versions of this concept
 * loaded DataSets and calibrations from files.)
 *
 *
 */

package Operators.Example;

import java.util.*;

import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class Display_SCD_Reciprocal_Space extends GenericOperator{
   public Display_SCD_Reciprocal_Space(){
     super("Display_SCD_Reciprocal_Space");
     }

   public String getCommand(){
      return "Display_SCD_Reciprocal_Space";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new ArrayPG("Vector of DataSets", null));
      addParameter( new FloatPG("Peak threshold", 50));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("This method will take a list of SCD DataSets, ");
      S.append("which include sample orientation and detector ");
      S.append("position information, and display a 3D view of ");
      S.append("reciprocal space.");
      S.append("@algorithm    "); 
      S.append("Each bin of the histogram that is above the ");
      S.append("specified threshold is mapped to Qxyz, based ");
      S.append("on the detector position, sample orientation ");
      S.append("and time-of-flight.");
      S.append("@assumptions    "); 
      S.append("All attributes needed to do the conversion from");
      S.append("time-of-flight to vector Qxyz must be included");
      S.append("in all of the DataSets.  This includes initial_path,");
      S.append("detector data grids, sample orientation and ");
      S.append("PixelInfo lists.");
      S.append("@param   ");
      S.append("A vector of SCD DataSets with needed attributes");
      S.append("@param   ");
      S.append("Threshold above which histgram bins will ");
      S.append("be included as peaks.");
      S.append("@error ");
      S.append("null Vector of DataSets in Show_SCD_Reciprocal_Space");
      S.append("@error ");
      S.append("no DataSets in Vector in Show_SCD_Reciprocal_Space");
      return S.toString();
   }


   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "Utils",
                     "Examples"
                     };
   }


   public Object getResult(){
      try{
         Vector vec    = (Vector)(getParameter(0).getValue());
         float  thresh = ((FloatPG)(getParameter(1))).getfloatValue();
         Operators.Example.DisplayUtil.Display_SCD_Reciprocal_Space( vec, thresh );
         return "Display Shown";
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }
}
