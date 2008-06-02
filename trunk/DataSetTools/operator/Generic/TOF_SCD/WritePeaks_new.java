/* 
 * File: WritePeaks_new.java
 *  
 * Copyright (C) 2008     Dennis Mikkelson
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
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, Tennessee, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package DataSetTools.operator.Generic.TOF_SCD;

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
public class WritePeaks_new extends GenericOperator implements HiddenOperator
{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public WritePeaks_new(){
     super("WritePeaks_new");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "WritePeaks_new";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new SaveFilePG("Peaks File Name","default.peaks"));
      addParameter( new PlaceHolderPG("Vector of Peaks",null));
      addParameter( new BooleanPG("Append to Existing File?",false));
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
      S.append("Write the specified peaks to the specified file in the");
      S.append(" new SNS peaks file format, with calibration information and");
      S.append(" a table of detector position and orientation information at");
      S.append(" the start of the file.");
      S.append("@algorithm    "); 
      S.append("The Vector of peaks is scanned to find all detector grids");
      S.append(" used.  The list of detector grids is sorted by ID");
      S.append(" and written");
      S.append(" at the start of the peaks file.  References to the peaks");
      S.append(" objects are copied to an array and sorted based on the");
      S.append(" run number, detector ID and h,k,l values.  The list of");
      S.append(" sorted peaks is then written to the specified file.");
      S.append("@assumptions    "); 
      S.append("The Vector of peaks must contain only Peak_new objects.");
      S.append("@param   ");
      S.append("The name of the peaks file to be created.");
      S.append("@param   ");
      S.append("A Vector of Peak_new objects.");
      S.append(" NOTE: The Vector must contain only Peak_new objects.");
      S.append("@param   ");
      S.append("Flag indicating whether or not to append to");
      S.append(" an existing peaks file (CURRENTLY NOT USED).");
      S.append("@return An error message if the peaks Vector is empty, or ");
      S.append("");
      S.append("the file can't be written.  Null if the file was written ");
      S.append("successfully.");
      S.append("@error ");
      S.append("Errors will be returned if the peaks Vector is empty,");
      S.append(" contains anything but Peak_new objects or");
      S.append(" if the file can't be written.");
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
                     "Instrument Type",
                     "TOF_NSCD"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

        java.lang.String file_name = getParameter(0).getValue().toString();
        java.util.Vector peaks = (java.util.Vector)(getParameter(1).getValue());
        boolean append = ((BooleanPG)(getParameter(2))).getbooleanValue();
        Peak_new_IO.WritePeaks_new(file_name,peaks,append );
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



