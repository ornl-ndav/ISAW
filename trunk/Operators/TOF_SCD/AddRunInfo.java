/* 
 * File: AddRunInfo.java
 *  
 * Copyright (C) 2012     Dennis Mikkelson
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
 * $Date: 2010-10-25 09:37:07 -0500 (Mon, 25 Oct 2010) $$
 * $Revision: 21066 $
 */

package Operators.TOF_SCD;
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
@see Operators.TOF_SCD.PeaksFileUtils#AddRunInfo(java.lang.String,boolean,int,boolean,float,float,float,boolean,float,java.lang.String)
 */
public class AddRunInfo extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public AddRunInfo(){
     super("AddRunInfo");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "AddRunInfo";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Name of Peaks File",""));
      addParameter( new BooleanEnablePG("Set Run Number",true));
      addParameter( new IntegerPG("Run Number",0));
      addParameter( new BooleanEnablePG("Set Goniometer Angles",true));
      addParameter( new FloatPG("Phi Angle",0));
      addParameter( new FloatPG("Chi Angle",135));
      addParameter( new FloatPG("Omega Angle",0));
      addParameter( new BooleanEnablePG("Set Monitor Counts",true));
      addParameter( new FloatPG("Monitor Counts",100000));
      addParameter( new SaveFilePG("Output File( Blank to Write to Input File )",""));
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
      S.append("Set the run number, goniometer angles and/or monitor count for peaks");
      S.append(" in the specified file.  NOTE: The file must contain peaks from only");
      S.append(" one run.  The output file can be left blank, in which case the modified");
      S.append(" list will be written back to input file!");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_SCD.PeaksFileUtils#AddRunInfo(java.lang.String,boolean,int,boolean,float,float,float,boolean,float,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("The specified peaks file is opened and loaded into a list.  A new peak");
      S.append(" object is created for each peak in the file, with data copied from the");
      S.append(" original peak object.  The new peak object will have the new run");
      S.append(" number, goniometer angles, and/or monitor counts depending on");
      S.append(" what fields were specified to change.  If the specified output file is");
      S.append(" not blank, the new list of peaks is then written to the output file.");
      S.append(" If the specified output file name is blank, the new list is written");
      S.append(" to the original file.");
      S.append(" written to the specified output file.");
      S.append("@assumptions    "); 
      S.append("The specified peaks file must be non-empty and must contain");
      S.append(" peaks from only one run.  The goniometer angles must either");
      S.append(" follow the angle convention at the SNS, and the file must identify");
      S.append(" the facility as the SNS, OR the angles must follow the angle");
      S.append(" convention of the IPNS SCD instrument currently at LANSCE.");
      S.append("@param   ");
      S.append("Name of file with list of peaks to which the");
      S.append(" specified information will be added.");
      S.append("@param   ");
      S.append("Flag indicating whether or not a new run number");
      S.append(" should be set.");
      S.append("@param   ");
      S.append("New run number");
      S.append("@param   ");
      S.append("Flag indicating whether or not new goniometer");
      S.append(" angles should be set.");
      S.append("@param   ");
      S.append("New phi angle");
      S.append("@param   ");
      S.append("New chi angle");
      S.append("@param   ");
      S.append("New omega angle");
      S.append("@param   ");
      S.append("Flag indicating whether or not a new monitor");
      S.append(" count should be set.");
      S.append("@param   ");
      S.append("New monitor count");
      S.append("@param   ");
      S.append("Name of the new file to write.  If this is blank or");
      S.append(" a zero length string, the new list of peaks will be");
      S.append(" written back to the input file.");
      S.append("@error ");
      S.append("If the input peaks file cannot be read properly, or the");
      S.append(" output file can't be written properly, then an exception");
      S.append(" will be thrown.");
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
                     "TOF_NSCD",
                     "PEAKS_FILE_UTILS"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String peaks_file = getParameter(0).getValue().toString();
         boolean set_run_num = ((BooleanEnablePG)(getParameter(1))).getbooleanValue();
         int run_num = ((IntegerPG)(getParameter(2))).getintValue();
         boolean set_gonio_angles = ((BooleanEnablePG)(getParameter(3))).getbooleanValue();
         float phi = ((FloatPG)(getParameter(4))).getfloatValue();
         float chi = ((FloatPG)(getParameter(5))).getfloatValue();
         float omega = ((FloatPG)(getParameter(6))).getfloatValue();
         boolean set_mon_count = ((BooleanEnablePG)(getParameter(7))).getbooleanValue();
         float mon_count = ((FloatPG)(getParameter(8))).getfloatValue();
         java.lang.String out_file = getParameter(9).getValue().toString();
         Operators.TOF_SCD.PeaksFileUtils.AddRunInfo(peaks_file,set_run_num,run_num,set_gonio_angles,phi,chi,omega,set_mon_count,mon_count,out_file );

         return "Success";
      }catch(java.lang.Exception S0){
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



