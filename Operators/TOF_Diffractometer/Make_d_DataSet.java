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
      addParameter( new LoadFilePG("Event File Name",
                    System.getProperty("Data_Directory","")));
      addParameter( new LoadFilePG("DetCal FileName",""));
      addParameter( new LoadFilePG("Bank File Name",""));
      addParameter( new LoadFilePG("Mapping File Name",""));
      addParameter( new FloatPG("First Event to Load",0));
      addParameter( new FloatPG("Number of Events To Load",1E7));
      addParameter( new FloatPG("Min d-spacing",.2f));
      addParameter( new FloatPG("Max d-spacing",10));
      addParameter( new BooleanEnablePG("Logarithmic Binning?","[true,1,1]"));
      addParameter( new FloatPG("Length of First Interval",.0002));
      addParameter( new IntegerPG("Number of Bins",10000));

      addParameter( new BooleanEnablePG("Use d Map File?","[false,1,0]"));
      addParameter( new LoadFilePG("d-space Mapping File",""));
      addParameter( new BooleanEnablePG("Get Ghost Histogram?","[false,3,0]"));
      addParameter( new LoadFilePG("Ghost Information File Name",""));
      addParameter( new IntegerPG("Number of Ghost IDs",300000));
      addParameter( new IntegerPG("Number of Ghosts per ID",16));
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
      S.append("This operator makes a DataSet in d-spacing directly ");
      S.append("from event data.  One spectrum is constructed for each ");
      S.append("detector bank in the instrument.  If the option ");
      S.append("Get Ghost Histogram is checked, then the Data returned ");
      S.append("will be an estimated ghost histogram based on the ");
      S.append("*GhostPks.dat file provided by Jason Hodge. ");
      S.append("@algorithm    "); 
      S.append("If the Use d Map File option is checked, then the ");
      S.append("histograms are constructed by mulitplying each ");
      S.append("time-of-flight times the diffractometer constant for ");
      S.append("that pixel.  The diffractometer constants are loaded ");
      S.append("from the *dspacemap.dat.");
      S.append("If this option is NOT checked, the d-values will be ");
      S.append("calculated based on the instrument geometry. ");
      S.append("If the Get Ghost Histogram is checked, then the ghost ");
      S.append("data from the GhostPks.dat file is used to generated ");
      S.append("fractional events in near by bins, given a actual event.");
      S.append("");
      S.append("@assumptions    "); 
      S.append("The neutron event file must be in the form written at the ");
      S.append("SNS.  Geometry information must either be included for ");
      S.append("the instrument in the distribution directory under ");
      S.append("InstrumentInfo/SNS/inst_name" );
      S.append("Or must be in files explicitly specified in the ");
      S.append("parameters.");
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
      S.append("The minimum d-spacing to consider");
      S.append("@param   ");
      S.append("The maximum d-spacing to consider");
      S.append("@param   ");
      S.append("If true use log binning, otherwise use uniform   binnings");
      S.append("@param   ");
      S.append("The length of first interval( isLog = true )");
      S.append("@param   ");
      S.append("The number of uniform bins( isLog=false )");
      S.append("@param   ");
      S.append("Use D space Map file vs geometry?");
      S.append("@param   ");
      S.append("The dspace mapping file listing the diffractometer ");
      S.append("constant for each DAS id. ");
      S.append("@param   ");
      S.append("Use Ghosting?");
      S.append("@param   ");
      S.append("Ghost File Name");
      S.append("@param   ");
      S.append("Number of Ghost IDs");
      S.append("@param   ");
      S.append("Number of Ghosts per ID");
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
         float min = ((FloatPG)(getParameter(6))).getfloatValue();
         float max = ((FloatPG)(getParameter(7))).getfloatValue();
         boolean isLog = ((BooleanEnablePG)(getParameter(8))).getbooleanValue();
         float first_logStep = ((FloatPG)(getParameter(9))).getfloatValue();
         int nUniformbins = ((IntegerPG)(getParameter(10))).getintValue();

         boolean useDMap = ((BooleanEnablePG)(getParameter(11))).getbooleanValue(); 
         String DMapfileName = getParameter(12).getValue().toString();
         boolean useGhosting = ((BooleanEnablePG)(getParameter(13))).getbooleanValue();
         String GhostFileName = getParameter(14).getValue().toString();
         int nIds = ((IntegerPG)(getParameter(15))).getintValue();
         int nGhosts = ((IntegerPG)(getParameter(16))).getintValue();
         DataSetTools.dataset.DataSet Xres=
            Operators.TOF_Diffractometer.Util.Make_d_DataSet(
                  EventFileName,
                  DetCalFileName,
                  bankInfoFileName,
                  MappingFileName,
                  firstEvent,
                  NumEventsToLoad,
                  min,
                  max,
                  isLog,
                  first_logStep,
                  nUniformbins,
                  useDMap,
                  DMapfileName,
                  useGhosting,
                  GhostFileName,
                  nIds,
                  nGhosts);

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



