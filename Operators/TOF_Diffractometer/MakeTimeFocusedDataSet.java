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
 * $Author:$
 * $Date:$
 * $Revision:$
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
      addParameter( new LoadFilePG("Event File Name",System.getProperty("Data_Directory","")));
      addParameter( new LoadFilePG("DetCal File Name",""));
      addParameter( new LoadFilePG("Bank FileName",""));
      addParameter( new LoadFilePG("Mapping File Name",""));
      addParameter( new FloatPG("First Event to Load",0));
      addParameter( new FloatPG("Number of Events To Load", 1E7));
      addParameter( new FloatPG("Focused Angle(degrees)",90));
      addParameter( new FloatPG("Focused Secondary Flight Path(m)",.5f));
      addParameter( new FloatPG("Min Time to Focus",1000));
      addParameter( new FloatPG("Max Time to Focus",30000));
      addParameter( new BooleanEnablePG("Logarithmic Binning?","[true,1,1]"));
      addParameter( new FloatPG("Length of First Interval",.2));
      addParameter( new IntegerPG("Number of Bins",10000));
      addParameter( new BooleanEnablePG("Get Ghost Histogram?", "[false,3,0]"));
      addParameter( new LoadFilePG("Ghost Information File Name", ""));
      addParameter( new IntegerPG(" Number of Ghost IDs", 300000));
      addParameter( new IntegerPG(" Number of Ghosts per ID", 16));
   }


   /**
    * Writes a string for the documentation of the operator provided by
    * the user.
    *
    * @return  The documentation for the operator.
    */
   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview     ");
      S.append("This operator makes a time-focused DataSet directly ");
      S.append("from event data.  One spectrum is constructed for each ");
      S.append("detector bank in the instrument.  If the option ");
      S.append("Get Ghost Histogram is checked, then the Data returned ");
      S.append("will be an estimated ghost histogram based on the ");
      S.append("*GhostPks.dat file provided by Jason Hodge. ");
      S.append("@algorithm    ");
      S.append("The instrument geometry is used to convert the measured ");
      S.append("time-of-flight reported by the DAS to a corresponding ");
      S.append("focused time-of-flight for the event, had that event ");
      S.append("been detected at the specified angle and secondary ");
      S.append("flight path distance.  ");
      S.append("If the Get Ghost Histogram is checked, then the ghost ");
      S.append("data from the GhostPks.dat file is used to generated ");
      S.append("fractional events in near by bins, given a actual event.");

      S.append("@algorithm     "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("");
      S.append("@param   ");
      S.append("The name of the SNS raw event file.");
      S.append("@param   ");
      S.append("The name of the .DetCal file with the detector calibrations");
      S.append("@param   ");
      S.append("The name of the _bank.xml file with bank and ");
      S.append("NeXus pixelID info.  ");
      S.append("@param   ");
      S.append("The name of the _TS.dat file that contains the mapping ");
      S.append("from DAS pixel_id's to NeXus pixel_id's");
      S.append("@param   ");
      S.append("The first Event to load");
      S.append("@param   ");
      S.append("The number of events to load");
      S.append("@param   ");
      S.append("The \"virtual\" scattering angle, two theta, ");
      S.append("(in degrees) to which the data should be focused");
      S.append("@param   ");
      S.append("The final flight path length (in meters) to which ");
      S.append("the data should be focused. ");
      S.append("@param   ");
      S.append("The minimum time-of-flight to map to.");
      S.append("@param   ");
      S.append("The maximum time-of-flight to map to.");
      S.append("@param   ");
      S.append("If true use log binning, otherwise use uniform  binnings");
      S.append("@param   ");
      S.append("The length of first interval( isLog = true");
      S.append("@param   ");
      S.append(")The number of uniform bins( isLog=false )");
      S.append("@param   ");
      S.append("Use ghosting corrections");
      S.append("@param   ");
      S.append("The Name of the file with the ghosting information");
      S.append("@param   ");
      S.append("The number of ghost ID's");
      S.append("@param   ");
      S.append(")The number ghosts per ID");
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

         java.lang.String EventFileName = getParameter(0).getValue().toString();
         java.lang.String DetCalFileName = getParameter(1).getValue().toString();
         java.lang.String bankInfoFileName = getParameter(2).getValue().toString();
         java.lang.String MappingFileName = getParameter(3).getValue().toString();
         float firstEvent = ((FloatPG)(getParameter(4))).getfloatValue();
         float NumEventsToLoad = ((FloatPG)(getParameter(5))).getfloatValue();
         float angle_deg = ((FloatPG)(getParameter(6))).getfloatValue();
         float final_L_m = ((FloatPG)(getParameter(7))).getfloatValue();
         float min = ((FloatPG)(getParameter(8))).getfloatValue();
         float max = ((FloatPG)(getParameter(9))).getfloatValue();
         boolean isLog = ((BooleanEnablePG)(getParameter(10))).getbooleanValue();
         float first_logStep = ((FloatPG)(getParameter(11))).getfloatValue();
         int nUniformbins = ((IntegerPG)(getParameter(12))).getintValue();
         boolean useGhosting = ((BooleanEnablePG)(getParameter(13))).getbooleanValue();
         String GhostFileName = getParameter(14).getValue( ).toString( );
         int nghostIDs =  ((IntegerPG)(getParameter(15))).getintValue();
         int nghosts   = ((IntegerPG)(getParameter(16))).getintValue();
         DataSetTools.dataset.DataSet Xres=
             Operators.TOF_Diffractometer.Util.MakeTimeFocusedDataSet(
                           EventFileName,
                           DetCalFileName,
                           bankInfoFileName,
                           MappingFileName,
                           firstEvent,
                           NumEventsToLoad,
                           angle_deg,
                           final_L_m,
                           min,
                           max,
                           isLog,
                           first_logStep,
                           nUniformbins,
                           useGhosting,
                           GhostFileName,
                           nghostIDs,
                           nghosts);

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



