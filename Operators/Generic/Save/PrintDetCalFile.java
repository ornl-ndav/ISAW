/* 
 * File: PrintDetCalFile.java
 *  
 * Copyright (C) 2010     Ruth Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *
 * Last Modified:
 *
 * $ Author: $
 * $Date: 2009-06-01 10:26:25 -0500 (Mon, 01 Jun 2009) $$
 * $Revision: 19721 $
 */

package Operators.Generic.Save;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class PrintDetCalFile extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public PrintDetCalFile(){
     super("PrintDetCalFile");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "PrintDetCalFile";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("NeXus File Name",System.getProperty("Data_Directory","")));
      addParameter( new SaveFilePG("DetCal filename",System.getProperty("Data_Directory","")));
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
      S.append("This operator reads in a NeXus file containing area detector type data, then produces a DETCAL file");
      S.append(" describing these area detectors.  This is a shell around the method");
      S.append(" EventTools.EventList.DumpGrids.PrintDetCalFile");
      S.append("@algorithm    "); 
      S.append("");
      S.append(" Read through the file to find the NXinstrument and NXsource nodes( for L0)");
      S.append(" Then creates an area detector corresponding to each NXdetector subnode of NXinstrument");
      S.append("@assumptions    "); 
      S.append("The Nexus file contains only ONE NXinstrument node. If there is an NXentry node, this NXinstrument");
      S.append(" node must be a child of the NXentry Node.");
      S.append(" ");
      S.append(" There need not be any NXdata nodes");
      S.append(" ");
      S.append(" T0 shift is 0.");
      S.append("@param   ");
      S.append("The Name of the NeXus file");
      S.append("@param   ");
      S.append("The Name of the file where the detector information is to be written in the DETCAL format");
      S.append("@return null or an error string.");
      S.append("");
      S.append(" The DETCAL file should be printed to the filename specified by the DetCalFile argument");
      S.append("@error ");
      S.append("Improper filenames, formats , etc.");
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

         java.lang.String NexFile = getParameter(0).getValue().toString();
         java.lang.String DetCalFile = getParameter(1).getValue().toString();
         java.lang.Object Xres=EventTools.EventList.DumpGrids.PrintDetCalFile(NexFile,DetCalFile );

         return Xres;
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



