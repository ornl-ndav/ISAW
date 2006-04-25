/* 
 * File: Display_As_Image.java
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
 * Revision 1.1  2005/06/20 16:51:18  dennis
 * Operator to display any DataSet in the new
 * ImageViewComponent.
 *
 */

package Operators.Example;

import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;
import DataSetTools.dataset.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class Display_As_Image extends GenericOperator{
   public Display_As_Image(){
     super("Display_As_Image");
     }

   public String getCommand(){
      return "Display_As_Image";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet to display as Image", 
                    DataSetTools.dataset.DataSet.EMPTY_DATA_SET));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("This method generates a display of the Data ");
      S.append("blocks of a DataSet as one large image. ");
      S.append("@param   ");
      S.append("The DataSet to be displayed");
      S.append("@error ");
      S.append("DataSet is null in Display_As_Image");
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
         DataSet ds = (DataSet)(getParameter(0).getValue());
         Operators.Example.DisplayUtil.Display_As_Image( ds );
         return "Display Shown";
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }
}
