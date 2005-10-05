/* 
 * File: LoadGSASlanl.java
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
 * Revision 1.1  2005/08/25 16:42:11  rmikk
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
public class LoadGSASlanl extends GenericOperator{
   public LoadGSASlanl(){
     super("LoadGSAS  lanl form");
     }

   public String getCommand(){
      return "LoadGSASlanl";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("Data Set",null));
      addParameter( new LoadFilePG("GSAS File",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("This Loads difc, difa and T0 from a GSAS parameter file where the information");
      S.append("is given on the special lanl format lines");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("DataSet to which difc, difa, and t0 are to be added");
      S.append("@param   ");
      S.append("The name of the GSAS file containing specially formatted lines(lanl) containing the appropriate difc,");
      S.append("difa, and t0 values");
      S.append("@return null or ErrorString describing the error condition.  The DataSet's groups will have the difc, difa and t0 ");
      S.append("");
      S.append("values added as a GSAS attribute");
      S.append("@error ");
      S.append("\"No such GSAS filename\", \"No DataSet\" and low level io problems are brought forward.");
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
         java.lang.Object Xres=Operators.Generic.Load.LoadUtil.LoadDifsGsas_lanl(DS,filename);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


