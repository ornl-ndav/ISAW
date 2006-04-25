/* 
 * File: Load_GLAD_LPSD_Info.java
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
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2005/07/11 21:05:54  dennis
 * Minor reformatting of getDocumentation() method.
 * Now uses System.getProperty() instead of
 * SharedData.getProperty().
 * Removed unused imports.
 *
 * Revision 1.1  2005/07/06 13:30:54  dennis
 * Operator to call LoadUtil.Load_GLAD_LPSD_Info() method.
 * This operator was "largely" produced by the operator generator,
 * but was hand edited so that the ISAW_HOME path could be prepended
 * to the filename "/Databases/gladdets.par".
 *
 */

package Operators.Generic.Load;

import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;
import Command.*;

public class Load_GLAD_LPSD_Info extends GenericOperator{
   public Load_GLAD_LPSD_Info(){
     super("Load_GLAD_LPSD_Info");
     }

   public String getCommand(){
      return "Load_GLAD_LPSD_Info";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new SampleDataSetPG("GLAD sample DataSet",
                                 DataSetTools.dataset.DataSet.EMPTY_DATA_SET));

      String path      = System.getProperty("ISAW_HOME");
      String file_name = path + "/Databases/gladdets6.par";
      addParameter( new LoadFilePG("File name for GLAD detector parameters",
                                   file_name ));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("This operator will load LPSD information for the IPNS GLAD");
      S.append("instrument from the configuration file, gladdets.par, as");
      S.append("used on the old VAX systems.  The information from the");
      S.append("file is used to make \"DataGrids\" corresponding to each");
      S.append("of the LPSDs in the glad instrument and associate the");
      S.append("correct Data block (spectrum) with each of the pixels");
      S.append("of the Data grid.");
      S.append("@algorithm    "); 
      S.append("The gladdets6.par file lists the detectors in each bank");
      S.append("and the first segment ID that is associated with each of");
      S.append("the detectors.  For each such detector, a \"DataGrid\" is");
      S.append("constructed that has the height, width and depth of");
      S.append("the detector and is divided into 64 segments.  The");
      S.append("Data blocks of the DataSet are then checked to find");
      S.append("the Data blocks that correspond to that detector.  For");
      S.append("each Data block corresponding to the detector, the");
      S.append("list of segments that are grouped in the block is replaced");
      S.append("with a new list of segments with the same segment ID's,");
      S.append("but with valid row and column numbers and referring");
      S.append("to the DataGrid for the detector.  The column numbers");
      S.append("are all 1 and the row numbers are");
      S.append("1 + seg_id - first_seg_id.");
      S.append("@assumptions    "); 
      S.append("The DataSet must be the sample histogram for the GLAD");
      S.append("instrument at IPNS, as currently (7/5/05) read by the ISAW");
      S.append("runfile retriever.  There must be segments corresponding");
      S.append("to the detectors listed in gladdets6.par with consistent");
      S.append("segment IDs and pixel info list attributes set for each");
      S.append("group.");
      S.append("@param   ");
      S.append("The GLAD DataSet to which the information is to be");
      S.append("added.");
      S.append("@param   ");
      S.append("The fully qualified name of the gladdets6.par");
      S.append("file.");
      S.append("@error ");
      S.append("");
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

         DataSetTools.dataset.DataSet ds = 
                    (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         java.lang.String file_name = getParameter(1).getValue().toString();
         Operators.Generic.Load.LoadUtil.Load_GLAD_LPSD_Info(ds,file_name);
         return "Success";
      }
      catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}
