/*
* File: readOrient.java
* 
 * Copyright (C) 2005     Ruth Mikkelson

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
 * Log:readOrient.java,v $
 */


package Operators.TOF_SCD;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class readOrient extends GenericOperator implements HiddenOperator{
   public readOrient(){
     super("Read Orientation");
     }

   public Object getResult(){
      try{

         java.lang.String filename = getParameter(0).getValue().toString();
         java.lang.Object Xres=Operators.TOF_SCD.IndexJ.readOrient(filename);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"+ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

   public String getCommand(){
      return "readOrient";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataDirPG("Enter File",""));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Read the orientation matrix out of the specified file.");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("matrixfile The file storing the UB Matrix. It can be a   *.x,or *.mat file");
      S.append("@return a float[][] rerpesentation of the orientation matrix or an ErrorString");
      return S.toString();
   }
}


