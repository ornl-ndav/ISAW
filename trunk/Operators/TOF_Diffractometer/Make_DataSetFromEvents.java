/* 
 * File: Make_DataSetFromEvents.java
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
 * $Date: 2010-04-05 08:53:47 -0500 (Mon, 05 Apr 2010) $$
 * $Revision: 20588 $
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
@see Operators.TOF_Diffractometer.Util#Make_DataSetFromEvents(java.lang.String,java.lang.String,java.lang.String,java.lang.String,float,float,float,float,boolean,float,int,java.lang.String)
 */
public class Make_DataSetFromEvents extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public Make_DataSetFromEvents(){
     super("Make_DataSetFromEvents");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "Make_DataSetFromEvents";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new LoadFilePG("Event File",System.getProperty("Data_Directory")));
      addParameter( new LoadFilePG("DetCal File Name",""));
      addParameter( new LoadFilePG("Bank File Name",""));
      addParameter( new LoadFilePG("Mapping File Name",""));
      addParameter( new FloatPG("First Event to Load",0));
      addParameter( new FloatPG("Num Events to Load",1E8));
      addParameter( new FloatPG("Min x-axis value",.2));
      addParameter( new FloatPG("Max x-axis value",10));
      addParameter( new BooleanEnablePG("Logarithmic Binning","[true,1,1]"));
      addParameter( new FloatPG("Length of First Interval",.0002));
      addParameter( new IntegerPG("# of Uniform bins",1000));
      addParameter( new ChoiceListPG("Type of x-axis",gov.anl.ipns.Parameters.Conversions.ToVec("[d-Spacing,\"Magnitude Q\",Wavelength,Time-of-flight]")));
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
      S.append("Makes a DataSet containing spectra in Wavelength,|Q|, ");
      S.append("d-Spacing or time-focused to the center of each " );
      S.append("bank, with one spectrum for each detector bank.");
      S.append(" ");
      S.append("\r\n");
     S.append(" This operator wraps the method Operators.TOF_Diffractometer.Util#Make_DataSetFromEvents(java.lang.String,java.lang.String,java.lang.String,java.lang.String,float,float,float,float,boolean,float,int,java.lang.String)");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("The data information is event data.");
      S.append("@param   ");
      S.append("The name of the file with events");
      S.append("@param   ");
      S.append("The name of the .DetCal file with the detector calibrations");
      S.append("@param   ");
      S.append("The name of the _bank.xml file with bank and ");
      S.append("NeXus pixelID info.  ");
      S.append("@param   ");
      S.append("The name of the _TS.dat file that contains the mapping ");
      S.append("from DAS pixel_id's to NeXus pixel_id's");
      S.append("@param   ");
      S.append("The first event to load");
      S.append("@param   ");
      S.append("The number of events to load");
      S.append("@param   ");
      S.append("The minimum x-axis value to use");
      S.append("@param   ");
      S.append("The maximun x-axis value to  use");
      S.append("@param   ");
      S.append("If true use log binning, otherwise use uniform binning");
      S.append("@param   ");
      S.append("The length of the first interval (if islog = true)");
      S.append("@param   ");
      S.append("The number of uniform bins( if islog = false)");
      S.append("@param   ");
      S.append("One of the Strings d-Spacing, Magnitude Q, Wavelength,Time-of-flight");
      S.append("@return A DataSet in whose spectra are the histograms for a detector bank.");
      S.append("@error ");
      S.append("");
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
                     "Load"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         java.lang.String eventFile = getParameter(0).getValue().toString();
         java.lang.String DetFile = getParameter(1).getValue().toString();
         java.lang.String bankFile = getParameter(2).getValue().toString();
         java.lang.String mapFile = getParameter(3).getValue().toString();
         float firstEvent = ((FloatPG)(getParameter(4))).getfloatValue();
         float NEvents = ((FloatPG)(getParameter(5))).getfloatValue();
         float minx = ((FloatPG)(getParameter(6))).getfloatValue();
         float maxx = ((FloatPG)(getParameter(7))).getfloatValue();
         boolean isLog = ((BooleanEnablePG)(getParameter(8))).getbooleanValue();
         float step = ((FloatPG)(getParameter(9))).getfloatValue();
         int NBins = ((IntegerPG)(getParameter(10))).getintValue();
         java.lang.String xUnits = getParameter(11).getValue().toString();
         DataSetTools.dataset.DataSet Xres=Operators.TOF_Diffractometer.Util.Make_DataSetFromEvents(eventFile,DetFile,bankFile,mapFile,firstEvent,NEvents,minx,maxx,isLog,step,NBins,xUnits );

         return Xres;
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



