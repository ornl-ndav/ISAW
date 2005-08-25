/* 
 * File: LoadDetInfo.java
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
 * Revision 1.1  2005/08/25 16:42:13  rmikk
 * Initial Checkin for operator Generated operators
 *
 *
 */

package Operators.Generic.Load;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class LoadDetInfo extends GenericOperator{
   public LoadDetInfo(){
     super("LoadDetInfo");
     }

   public String getCommand(){
      return "LoadDetInfo";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet",null));
      addParameter( new LoadFilePG("Det Inf file",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("");
      S.append("Load detector information (and initial path, and instrument type,");
      S.append("if present) for the specified DataSet from the specified file.");
      S.append("The file must list the values on separate lines, with a single");
      S.append("identifier at the start of the line.  Lines starting with \"#\"");
      S.append("are ignored.  The accepted identifiers are:<ul>");
      S.append("<li>Instrument_Type");
      S.append("<li>Initial_Path");
      S.append("<li>Num_Grids");
      S.append("<li>Grid_ID");
      S.append("<li> Num_Rows");
      S.append("<li> Num_Cols");
      S.append("<li> Width");
      S.append("<li>Height");
      S.append("<li> Depth");
      S.append("<li> Center");
      S.append("<li> X_Vector");
      S.append("<li> Y_Vector");
      S.append("<li>First_Index");
      S.append("</ul>");
      S.append("The identifier MUST appear at the start of the line, followed by spaces");
      S.append("and  a single number, except for the Center, X_Vector and Y_Vector, which");
      S.append("require three numbers separated by spaces.");
      S.append("NOTE: Values can be omitted, and the previous or default values will");
      S.append("be used.  This allows, for example, specifying the width, height");
      S.append("and depth once for the first detector and omitting them for");
      S.append("later detectors that have the same dimensions.");
      S.append("");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("DataSet to be fixed");
      S.append("@param   ");
      S.append("The name of the specially formatted file containing detector information");
      S.append("@error ");
      S.append("");
      return S.toString();
   }


   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "DataSet",
                     "Tweak",
                     "LANSCE"
                     };
   }


   public Object getResult(){
      try{

         DataSetTools.dataset.DataSet DS = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         java.lang.String filename = getParameter(1).getValue().toString();
         Operators.Generic.Load.LoadUtil.LoadDetectorInfo(DS,filename);
         return "Success";
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


