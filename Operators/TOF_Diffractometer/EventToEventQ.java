/* 
 * File: EventToEventQ.java
 *  
 * Copyright (C) 2011     Dennis Mikkelson
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

import java.util.Vector;

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
@see Operators.TOF_Diffractometer.EventToEventQ_calc#EventToEventQ(java.lang.String,java.lang.String,java.lang.String,java.lang.String,float,float,java.lang.String,java.lang.String)
 */
public class EventToEventQ extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public EventToEventQ(){
     super("EventToEventQ");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "EventToEventQ";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();

      Vector choices = new Vector();
      choices.add( "Binary_Little_Endian(PC)" );
      choices.add( "Binary_Big_Endian(Java)" );
      choices.add( "ASCII" );

      addParameter( new LoadFilePG("Event File Name",""));
      addParameter( new LoadFilePG("DetCal File Name",""));
      addParameter( new LoadFilePG("Bank File Name",""));
      addParameter( new LoadFilePG("Mapping File Name",""));
      addParameter( new FloatPG("First Event to Load",0.0));
      addParameter( new FloatPG("Number of Events To Load",1.0E9));
      addParameter( new SaveFilePG("Ouput File Name",""));
      addParameter( new ChoiceListPG("Type of File to Write", choices));
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
      S.append("This Operator will load an SNS raw event file, map those events to");
      S.append(" reciprocal space, and then write the corresponding Q vector values");
      S.append(" to a file.  The file can be in one of three formats, binary little");
      S.append(" endian, binary big endian, or 3-column ASCII.  The values are written");
      S.append(" in groups of 3 floats.  The first float is the component of Q in the");
      S.append(" direction of the beam.  (This will always be negative.)  The second");
      S.append(" float is the component of Q in the horizontal plane, perpendicular");
      S.append(" to the beam.  The third float is the component of Q in the vertically");
      S.append(" upward direction.  In all cases the magnitude of Q is taken to be");
      S.append(" 2*PI/d.");
       S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_Diffractometer.EventToEventQ_calc#EventToEventQ(java.lang.String,java.lang.String,java.lang.String,java.lang.String,float,float,java.lang.String,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The name of the file with the raw time-of-flight events");
      S.append("@param   ");
      S.append("The name of the file with the detector calibrations.");
      S.append(" (Blank for default.)");
      S.append("@param   ");
      S.append("The name of the file with bank and pixelID (NeXus ID)");
      S.append(" info");
      S.append("@param   ");
      S.append("The name of the file that maps DAS pixel IDs");
      S.append(" to NeXus pixel IDs");
      S.append("@param   ");
      S.append("The first Event to load");
      S.append("@param   ");
      S.append("The number of events to load");
      S.append("@param   ");
      S.append("The name of the file of event Q's that will be written");
      S.append("@param   ");
      S.append("String specifying which type of file to write");
      S.append(" ASCII, Binary_Little_Endian(PC), or");
      S.append(" Binary_Big_Endian(Java)");
      S.append("@error ");
      S.append("Exceptions will be thrown if there is an error");
      S.append(" while reading or writing the files.");
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

         java.lang.String event_filename = getParameter(0).getValue().toString();
         java.lang.String DetCal_filename = getParameter(1).getValue().toString();
         java.lang.String bank_filename = getParameter(2).getValue().toString();
         java.lang.String mapping_filename = getParameter(3).getValue().toString();
         float first_event = ((FloatPG)(getParameter(4))).getfloatValue();
         float num_events = ((FloatPG)(getParameter(5))).getfloatValue();
         java.lang.String output_filename = getParameter(6).getValue().toString();
         java.lang.String file_type = getParameter(7).getValue().toString();
         Operators.TOF_Diffractometer.EventToEventQ_calc.EventToEventQ(event_filename,DetCal_filename,bank_filename,mapping_filename,first_event,num_events,output_filename,file_type );

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



