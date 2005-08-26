/* 
 * File: OpenLog.java
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
 * Revision 1.1  2005/08/26 15:30:57  rmikk
 * Initial Checkin's
 *
 *
 */

package Operators.Generic.System;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class OpenLog extends GenericOperator{
   public OpenLog(){
     super("Open Logging File");
     }

   public String getCommand(){
      return "OpenLog";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Log File Name",""));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("");
      S.append("Sets up the file that information will be logged to.");
      S.append("The previous log file will be updated and closed");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("If the file does not exist, the logging information will go to the status pane");
      S.append("@param   ");
      S.append("The name of the file that is to receive the logging information");
      S.append("@error ");
      S.append("");
      return S.toString();
   }


   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "Utils",
                     "System"
                     };
   }


   public Object getResult(){
      try{

         java.lang.String filename = getParameter(0).getValue().toString();
         gov.anl.ipns.Util.Sys.SharedMessages.openLog(filename);
         return "Success";
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


