/* 
 * File: CloseLog.java
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
 * Revision 1.2  2006/07/10 22:10:18  dennis
 * Removed unused imports after refactoring to use new Parameter GUIs
 * in gov.anl.ipns.Parameters.
 *
 * Revision 1.1  2005/08/26 15:30:58  rmikk
 * Initial Checkin's
 *
 *
 */

package Operators.Generic.System;

import DataSetTools.operator.Generic.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 * This operator is a wrapper around 
@see gov.anl.ipns.Util.Sys.SharedMessages#closeLog()
 */
public class CloseLog extends GenericOperator{
   public CloseLog(){
     super("Close LogFile");
     }

   public String getCommand(){
      return "CloseLog";
   }

   public void setDefaultParameters(){
      clearParametersVector();
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Closes the log file. Subsequent LogAdd's will go to the status pane");
      S.append("\r\n");
      S.append(" This operator wraps the method gov.anl.ipns.Util.Sys.SharedMessages.closeLog()\n");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
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

         gov.anl.ipns.Util.Sys.SharedMessages.closeLog();
         return "Success";
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


