/* 
 * File: cnvrt2intArray1D.java
 *  
 * Copyright (C) 2005     Ruth Mikkelson
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
 * Contact :  Ruth Mikkelson<Mikkelsonr@uwstout.edu>
 *            Department of Mathematics, Statistics and Computer Science
 *            University of Wisconsin-Stout
 *            Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 * 
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2005/08/25 16:44:39  rmikk
 * Initial Checkin
 *
 *
 */

package Operators;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class cnvrt2intArray1D extends GenericOperator{
   public cnvrt2intArray1D(){
     super("convert to a 1D intArray");
     }

   public String getCommand(){
      return "cnvrt2intArray1D";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("int Array",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Converts a value to an array of int's.");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("The Argument represents a list of number as a Vector, String,float[],int[],etc.");
      S.append("@param   ");
      S.append("An Object that can be converted to an array of int's. It can be the string form");
      S.append("@error ");
      S.append("Conversion is not possible");
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

         java.lang.Object Arr = (java.lang.Object)(getParameter(0).getValue());
         int[] Xres=Command.ScriptUtil.cvrt2int1D(Arr);
         return Xres;
       }catch(java.lang.IllegalArgumentException S0){
         return new ErrorString(S0.getMessage());
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


