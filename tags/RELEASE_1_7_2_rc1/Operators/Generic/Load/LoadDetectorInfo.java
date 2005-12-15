/* 
 * File: LoadDetectorInfo.java
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
 * Revision 1.1  2005/08/05 20:57:45  dennis
 * Initial version of operator to "fix" DataSets read from most
 * LANSCE NeXus files, by reading in detector position informat
 * from an auxilary file.  The instrument type and initial path
 * can also be specified in the file.
 * This operator is built on the static method
 * LoadUtil.LoadDetectorInfo(), using the Method2OperatorWizard.
 *
 *
 */

package Operators.Generic.Load;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class LoadDetectorInfo extends GenericOperator{
   public LoadDetectorInfo(){
     super("LoadDetectorInfo");
     }

   public String getCommand(){
      return "LoadDetectorInfo";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet to add detector info to",
                        DataSetTools.dataset.DataSet.EMPTY_DATA_SET));
      addParameter( new LoadFilePG("File with detector positions",
                        System.getProperty( "ISAW_HOME" ) + "/" +
                        "InstrumentInfo/LANSCE/smarts_detectors.dat"));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Load detector information (and initial path, if present) for the");
      S.append("specified DataSet from the specified file.  Internally, each");
      S.append("area detector, LPSD or panel of single tubes may be treated");
      S.append("as a \"DataGrid\", depending on the detector information");
      S.append("file.");
      S.append("@algorithm    "); 
      S.append("The detector information file is read, in sequence, and the");
      S.append("values are stored in a hashtable, based on the key word.");
      S.append("After loading the detector information, each \"DataGrid\"");
      S.append("is processed and the information on pixel positions is");
      S.append("calculated and stored with the corresponding Data block.");
      S.append("@assumptions    "); 
      S.append("There are two basic assumptions.");
      S.append("");
      S.append("First, the DataSet into which the detector information is to be");
      S.append("loaded must have a contiguous sequence of Data blocks, one");
      S.append("for each detector listed in the file.  The contiguous sequence");
      S.append("of Data blocks for each detector must be listed in row major");
      S.append("order, starting at the specified first index minus 1.  That is,");
      S.append("if the groups are numbered 1,2,3, etc. the detector info file");
      S.append("will use 1 as the starting index, and the Data block should");
      S.append("actually be at index 0 in the DataSet.");
      S.append("");
      S.append("Second, the detector info file must be in the right form.  The");
      S.append("expected form consists of a sequence of name value pairs");
      S.append("listed on separate lines.  The recognized names are:");
      S.append("Initial_Path");
      S.append("Num_Grids");
      S.append("Grid_ID");
      S.append("Num_Rows");
      S.append("Num_Cols");
      S.append("Width");
      S.append("Height");
      S.append("Depth");
      S.append("Center");
      S.append("X_Vector");
      S.append("Y_Vector");
      S.append("First_Index");
      S.append("");
      S.append("All lengths and vectors MUST be specified in meters.");
      S.append("The X_Vector and Y_Vector are specified in \"laboratory\"");
      S.append("coordinates and define a local coordinate system on the");
      S.append("face of the detector.  Spaces serve as separators, and");
      S.append("comment lines begin with a \"#\" symbol.");
      S.append("All of these quantities are needed to characterize each");
      S.append("detector, however, the values set for the previous detector");
      S.append("don't need to be specified for later detectors, if the value");
      S.append("hasn't changed.  For example, if all detectors have the same");
      S.append("height, it must be specified for the first detector in the file,");
      S.append("but need not be specified for each later detector.");
      S.append("The total number of detectors MUST be specified before");
      S.append("specifying any detector information.");
      S.append("@param   ");
      S.append("The DataSet to which the detector position information");
      S.append("should be added.");
      S.append("@param   ");
      S.append("The file containing the detector position information,");
      S.append("in the required format.");
      S.append("@return Nothing is returned, but the DataSet is altered by adding");
      S.append("");
      S.append("the detector position information.");
      S.append("@error ");
      S.append("The process will fail if the detector position file is not found,");
      S.append("has the wrong format, or has information for more detectors");
      S.append("than the corresponding DataSet.");
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

         DataSetTools.dataset.DataSet ds = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         java.lang.String det_file = getParameter(1).getValue().toString();
         Operators.Generic.Load.LoadUtil.LoadDetectorInfo(ds,det_file);
         return "Success";
      }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


