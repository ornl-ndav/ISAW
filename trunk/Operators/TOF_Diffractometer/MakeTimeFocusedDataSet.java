/* 
 * File: MakeTimeFocusedDataSet.java
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
 */
public class MakeTimeFocusedDataSet extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public MakeTimeFocusedDataSet(){
     super("MakeTimeFocusedDataSet");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "MakeTimeFocusedDataSet";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new StringPG("Instrument","SNAP"));
      addParameter( new LoadFilePG("Event File Name",System.getProperty("Data_Directory","")));
      addParameter( new LoadFilePG("DetCal File Name",System.getProperty("ISAW_HOME","")+"/InstrumentInfo/SNS"));
      addParameter( new LoadFilePG("Bank FileName",System.getProperty("ISAW_HOME","")+"/InstrumentInfo/SNS"));
      addParameter( new LoadFilePG("Mapping File Name",System.getProperty("ISAW_HOME","")+"/InstrumentInfo/SNS"));
      addParameter( new LongPG("firs Event",1));
      addParameter( new LongPG("Num Events To Load",800000));
      addParameter( new FloatPG("focusing angle(degrees0",90));
      addParameter( new FloatPG("Focusing flight path(m)",.5f));
      addParameter( new FloatPG("min time to focus",1000));
      addParameter( new FloatPG("max time to focus",30000));
      addParameter( new BooleanEnablePG("Logarithmic binning?","[false,1,1]"));
      addParameter( new FloatPG("Length first interval(log binning)",.7));
      addParameter( new IntegerPG("# buns(uniform)",10000));
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
      S.append("Makes a DataSet from Event Data where each detector is time focused");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The name of the instrument");
      S.append("@param   ");
      S.append("The name of the file with events");
      S.append("@param   ");
      S.append("The name of the file with the detector  calibrations");
      S.append("@param   ");
      S.append("The name of the file with bank and pixelID(nex)    info");
      S.append("@param   ");
      S.append("The name of the file that maps DAS pixel_id's   to NeXus pixel_id's");
      S.append("@param   ");
      S.append("The first Event to load");
      S.append("@param   ");
      S.append("The number of events to load");
      S.append("@param   ");
      S.append("The \"virtual\" scattering angle, two theta,     (in degrees) to which the data should be focused");
      S.append("@param   ");
      S.append("The final flight path length (in meters) to which   the data should be focused");
      S.append("@param   ");
      S.append("The minimum time to consider");
      S.append("@param   ");
      S.append("The maximum time to consider");
      S.append("@param   ");
      S.append("If true use log binning, otherwise use uniform  binnings");
      S.append("@param   ");
      S.append("The length of first interval( isLog = true");
      S.append("@param   ");
      S.append(")The number of uniform bins( isLog=false )");
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

         java.lang.String Instrument = getParameter(0).getValue().toString();
         java.lang.String EventFileName = getParameter(1).getValue().toString();
         java.lang.String DetCalFileName = getParameter(2).getValue().toString();
         java.lang.String bankInfoFileName = getParameter(3).getValue().toString();
         java.lang.String MappingFileName = getParameter(4).getValue().toString();
         long firstEvent = ((LongPG)(getParameter(5))).getlongValue();
         long NumEventsToLoad = ((LongPG)(getParameter(6))).getlongValue();
         float angle_deg = ((FloatPG)(getParameter(7))).getfloatValue();
         float final_L_m = ((FloatPG)(getParameter(8))).getfloatValue();
         float min = ((FloatPG)(getParameter(9))).getfloatValue();
         float max = ((FloatPG)(getParameter(10))).getfloatValue();
         boolean isLog = ((BooleanEnablePG)(getParameter(11))).getbooleanValue();
         float first_logStep = ((FloatPG)(getParameter(12))).getfloatValue();
         int nUniformbins = ((IntegerPG)(getParameter(13))).getintValue();
         DataSetTools.dataset.DataSet Xres=Operators.TOF_Diffractometer.Util.MakeTimeFocusedDataSet(Instrument,EventFileName,DetCalFileName,bankInfoFileName,MappingFileName,firstEvent,NumEventsToLoad,angle_deg,final_L_m,min,max,isLog,first_logStep,nUniformbins );

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


