/* 
 * File: mkVariableXScale.java
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
 * Revision 1.4  2006/07/26 19:09:10  dennis
 * Moved method to convert an Object to a VariableXScale, out
 * of the VariableXScale class, into this class.
 *
 * Revision 1.3  2006/07/10 21:48:01  dennis
 * Removed unused imports after refactoring to use New Parameter
 * GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.2  2006/07/10 16:26:06  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.1  2005/08/25 16:45:59  rmikk
 * Initial Checkin
 *
 *
 */

package Operators;

import DataSetTools.operator.Generic.*;
import DataSetTools.dataset.VariableXScale;
import DataSetTools.operator.JavaWrapperOperator;

import gov.anl.ipns.Parameters.PlaceHolderPG;
import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

public class mkVariableXScale extends GenericOperator{

   public mkVariableXScale(){
     super("make VariableXScale");
     }

   public String getCommand(){
      return "VariableXScale";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("X values",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Creates a VariableXScale if possible.");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("The input object can be converted to an increasing float[].");
      S.append("@param   ");
      S.append("An Object containing a some type of list of values that can  be converted to a float[] then to");
      S.append("a VariableXScale.");
      S.append("@error ");
      S.append("\"Cannot convert to float[]\", \"Values are not increasing\",\"no Values\", etc.");
      return S.toString();
   }


   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "Utils",
                     "Convert"
                     };
   }


  /**
   *   Static method to create a VariableXScale.  This static method is 
   * used by the Operator that creates a VariableXScale.
   *
   * @param vals  An Object containing a some type of list of values that can
   *              be converted to a float[] then to a VariableXScale.
   *
   * @return  A VariableXScale or an ErrorString
   */
  public static Object createVariableXScale( Object vals )
  {
    if( vals == null)
       return new ErrorString("There are no values");

    float[] values=null;
    try
    {
      values=(float[])JavaWrapperOperator.cvrt((new float[0]).getClass(), vals);
    }
    catch( Exception s )
    {
       return new ErrorString("Cannot convert data to float[]");
    }

    if( values == null )
     return new ErrorString("Cannot convert data to float[]");

    for( int i = 1; i < values.length; i++ )
      if( values[i-1] >= values[i] )
        return new ErrorString("Values are not increasing");

    return new VariableXScale( values );
  }


   public Object getResult(){
      try{

         java.lang.Object xvals = (java.lang.Object)(getParameter(0).getValue());
         java.lang.Object Xres=createVariableXScale(xvals);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


