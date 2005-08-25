/* 
 * File: mkUniformXScale.java
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
 * Revision 1.1  2005/08/25 16:46:00  rmikk
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
public class mkUniformXScale extends GenericOperator{
   public mkUniformXScale(){
     super("make UniformXScale");
     }

   public String getCommand(){
      return "UniformXScale";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new FloatPG("start x",0));
      addParameter( new FloatPG("end x",1));
      addParameter( new IntegerPG("# of x values",10));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Creates a UniformXScale");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("The number of x values is one more than the number of bins.  The bins have equal lengths in");
      S.append("a Uniform XScale.");
      S.append("@param   ");
      S.append("Starting x vlaue");
      S.append("@param   ");
      S.append("ending x value");
      S.append("@param   ");
      S.append("Number of x values");
      S.append("@error ");
      S.append("");
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

         float startx = ((FloatPG)(getParameter(0))).getfloatValue();
         float endx = ((FloatPG)(getParameter(1))).getfloatValue();
         int numx = ((IntegerPG)(getParameter(2))).getintValue();
         DataSetTools.dataset.UniformXScale Xres=DataSetTools.dataset.UniformXScale.createUniformXScale(startx,endx,numx);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


