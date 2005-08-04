/* 
 * File: ViewNXdata.java
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
 *            Menomonie, WI 54751
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
 * Revision 1.1  2005/06/18 14:20:22  rmikk
 * Initial Checkin
 *
 *
 */

package Operators.Generic.Load;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class ViewNXdata extends GenericOperator{
   public ViewNXdata(){
     super("View a NXdata");
     }

   public String getCommand(){
      return "ViewNXdata";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Enter NeXus filename",null));
      addParameter( new StringPG("Entry NXentry Name",null));
      addParameter( new StringPG("Enter NXdata name",null));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("This class views an NXdata.  It uses the signal and axis attributes to");
      S.append("determine which field is data and which are  the \"time\" values");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The filename");
      S.append("@param   ");
      S.append("The name of the NXentry");
      S.append("@param   ");
      S.append("The name of the NXdata node to be viewed");
      S.append("@return The resultant DataSet. It may have detectors depending on the dimension");
      S.append("");
      S.append("of the data");
      return S.toString();
   }


   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "File",
                     "Load"
                     };
   }


   public Object getResult(){
      try{

         java.lang.String filename = getParameter(0).getValue().toString();
         java.lang.String EntryName = getParameter(1).getValue().toString();
         java.lang.String DataName = getParameter(2).getValue().toString();
         java.lang.Object Xres=NexIO.Util.NexViewUtils.getNxData(filename,EntryName,DataName);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


