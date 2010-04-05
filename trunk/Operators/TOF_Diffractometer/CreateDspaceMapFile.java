/* 
 * File: CreateDspaceMapFile.java
 *  
 * Copyright (C) 2010     Dennis Mikkelson
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
 *            Department of Mathematics, Statistics and Computer Science
 *            University of Wisconsin-Stout
 *            Menomonie, WI 54751, USA
 *
 * This work was supported by the SNS division of Oakridge National Laboratory.
 *
 *
 * Last Modified:
 *
 * $ Author: $
 * $Date$$
 * $Revision$
 */

package Operators.TOF_Diffractometer;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 * This operator is a wrapper around 
@see EventTools.EventList.FileUtil#CreateDspaceMapFile(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 */
public class CreateDspaceMapFile extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public CreateDspaceMapFile(){
     super("CreateDspaceMapFile");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "CreateDspaceMapFile";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new InstNamePG("Instrument Name (required)", "PG3"));
      addParameter( new LoadFilePG(".DetCal file (blank for default)",""));
      addParameter( new LoadFilePG("Banking file (blank for default)",""));
      addParameter( new LoadFilePG("ID Map file (blank for default)",""));
      addParameter( new SaveFilePG("Output d-space map file name",""));
   }


   /**
    * Writes a string for the documentation of the operator provided by
    * the user.
    *
    * @return  The documentation for the operator.
    */
   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Create a d-space map file for the specified instrument.  The d-space");
      S.append(" map file is a simple file of doubles, written in little-endian (PC)");
      S.append(" format, with the kth double being the diffractometer constant for");
      S.append(" the pixel with DAS id k. The d-spacing corresponding to an event is");
      S.append(" just tof * d-space_map[k].");
      S.append("@algorithm    "); 
      S.append("Based on the geometry of the instrument");
      S.append(" the diffractometer constant mapping time-of-flight");
      S.append(" to d-space value is calculated for each DAS id.");
      S.append(" The resulting list of doubles is written in order");
      S.append(" to the specified dspace_map file.  The doubles are");
      S.append(" written in little-endian (PC) format, NOT in the");
      S.append(" format used natively by java.");
      S.append("@assumptions    "); 
      S.append("Geometry information for the instrument in");
      S.append(" the form of valid .DetCal, \"banking\" and id");
      S.append(" mapping files must be available.");
      S.append("@param   ");
      S.append("The name of the SNS instrument for which the");
      S.append(" d-space map will be generated. (required)");
      S.append("@param   ");
      S.append("The .DetCal file to use.");
      S.append(" If not specified the default .DetCal file from");
      S.append(" the InstrumentInfo/SNS directory will be used.");
      S.append("@param   ");
      S.append("The \"banking\" file to use.");
      S.append(" If not specified the default .DetCal file from");
      S.append(" the InstrumentInfo/SNS directory will be used.");
      S.append("@param   ");
      S.append("The DAS id \"mapping\" file to use.");
      S.append(" If not specified the default .DetCal file from");
      S.append(" the InstrumentInfo/SNS directory will be used.");
      S.append("@param   ");
      S.append("The name of the dspace_map file that will");
      S.append(" be written. (required)");
      S.append("@error ");
      S.append("An exception will be thrown if valid .DetCal,");
      S.append(" banking and id mapping are not specified");
      S.append(" correctly, and/or proper default files can't");
      S.append(" be found.");
      S.append(" An exception wil be thrown if the specified");
      S.append(" dspace_map file can not be written.");
      return S.toString();
   }


   /**
    * Returns a string array with the category the operator is in.
    *
    * @return  An array containing the category the operator is in.
    */
   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "File",
                     "Save"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String instrument_name = getParameter(0).getValue().toString();
         java.lang.String det_cal_filename = getParameter(1).getValue().toString();
         java.lang.String bank_filename = getParameter(2).getValue().toString();
         java.lang.String map_filename = getParameter(3).getValue().toString();
         java.lang.String dspace_map_filename = getParameter(4).getValue().toString();
         EventTools.EventList.FileUtil.CreateDspaceMapFile(instrument_name,det_cal_filename,bank_filename,map_filename,dspace_map_filename );

         return "Success";
      }catch(java.io.IOException S0){
         return new ErrorString(S0.getMessage());
      }catch( Throwable XXX){
        String[]Except = ScriptUtil.
            GetExceptionStackInfo(XXX,true,1);
        String mess="";
        if(Except == null) Except = new String[0];
        for( int i =0; i< Except.length; i++)
           mess += Except[i]+"\r\n            "; 
        return new ErrorString( XXX.toString()+":"
             +mess);
                }
   }
}



