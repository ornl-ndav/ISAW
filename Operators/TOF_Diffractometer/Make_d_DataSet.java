/* 
 * File: Make_d_DataSet.java
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
public class Make_d_DataSet extends GenericOperator{


   /**
    * Constructor for the operator.  Calls the super class constructor.
    */
   public Make_d_DataSet(){
     super("Make_d_DataSet");
     }


   /**
    * Gives the user the command for the operator.
    *
    * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "Make_d_DataSet";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new StringPG("Instrument","SNAP"));
      addParameter( new LoadFilePG("Even tFile Name",System.getProperty("Data_Directory","")));
      addParameter( new LoadFilePG("DetCa lFile Name",System.getProperty("ISAW_HOME","")+"/InstrumentInfo/SNS"));
      addParameter( new LoadFilePG("bank File Name",System.getProperty("ISAW_HOME","")+"/InstrumentInfo/SNS"));
      addParameter( new LoadFilePG("Mapping File Name",System.getProperty("ISAW_HOME","")+"/InstrumentInfo/SNS"));
      addParameter( new IntegerPG("first Event to load",1));
      addParameter( new IntegerPG("Num Events to Load",null));
      addParameter( new FloatPG("Min d-spacing",.2f));
      addParameter( new FloatPG("Max d-spacing",10));
      addParameter( new BooleanPG("log d binning?",false));
      addParameter( new IntegerPG("# of uniform bins",10000));
      addParameter( new FloatPG("first log bin length",.0002));
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
      S.append("Makes a DataSet in d-spacing for each detector from Event Data");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The name of the instrument");
      S.append("@param   ");
      S.append("The name of the file with events");
      S.append("@param   ");
      S.append("he name of the file with the detector  calibrations");
      S.append("@param   ");
      S.append("The name of the file with bank and pixelID(nex)   info");
      S.append("@param   ");
      S.append("The name of the file that maps DAS pixel_id's  to NeXus pixel_id's");
      S.append("@param   ");
      S.append("The first Event to load");
      S.append("@param   ");
      S.append("The number of events to load");
      S.append("@param   ");
      S.append("The minimum d-spacing to consider");
      S.append("@param   ");
      S.append("The maximum d-spacing to consider");
      S.append("@param   ");
      S.append("If true use log binning, otherwise use uniform  binnings");
      S.append("@param   ");
      S.append("The number of uniform bins( isLog=false )");
      S.append("@param   ");
      S.append("The length of first interval( isLog = true )");
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
         int firstEvent = ((IntegerPG)(getParameter(5))).getintValue();
         int NumEventsToLoad = ((IntegerPG)(getParameter(6))).getintValue();
         float min = ((FloatPG)(getParameter(7))).getfloatValue();
         float max = ((FloatPG)(getParameter(8))).getfloatValue();
         boolean isLog = ((BooleanPG)(getParameter(9))).getbooleanValue();
         int nUniformbins = ((IntegerPG)(getParameter(10))).getintValue();
         float first_logStep = ((FloatPG)(getParameter(11))).getfloatValue();
         DataSetTools.dataset.DataSet Xres=Operators.TOF_Diffractometer.Util.Make_d_DataSet(Instrument,EventFileName,DetCalFileName,bankInfoFileName,MappingFileName,firstEvent,NumEventsToLoad,min,max,isLog,nUniformbins,first_logStep );

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



