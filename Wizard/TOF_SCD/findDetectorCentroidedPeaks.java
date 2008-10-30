/* 
 * File: findDetectorCentroidedPeaks.java
 *  
 * Copyright (C) 2008     Ruth Mikkelson
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
 *            Menomonie, WI 54751
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 *
 * $Log:$
 *
 */

package Wizard.TOF_SCD;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;
import java.util.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class findDetectorCentroidedPeaks extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public findDetectorCentroidedPeaks(){
     super("find Detector Centroided Peaks");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "findDetectorCentroidedPeaks";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet",null));
      addParameter( new IntegerPG("DetectorID",-1));
      addParameter( new IntegerPG("num_peaks",30));
      addParameter( new IntegerPG("min Peak intensity",0));
      addParameter( new IntegerPG("min_time_chan",0));
      addParameter( new IntegerPG("max_time_chan",50000));
      addParameter( new ChoiceListPG("Rows to keep",""));
      addParameter( new ChoiceListPG("Cols to keep",""));
      addParameter( new IntegerPG("monitor count",10000));
      addParameter( new FloatPG("Max d-spacing",12f));
      addParameter( new BooleanEnablePG("Use new FindPeaks",
               addTo(addTo(addTo(new Vector(), true),2),0)));
      addParameter( new BooleanPG("Use Smoothed Data", true));
      addParameter( new BooleanPG("Use validity test", true));
      addParameter( new BooleanPG("Use old Centroid", true));
      addParameter( new BooleanEnablePG("Show Peak Images",
               addTo(addTo(addTo(new Vector(), true),1),0)));
      addParameter( new IntegerPG("Num slices in peaks image",2));
      addParameter( new PlaceHolderPG("log stuff", null));
   }
   private Vector addTo(Vector V1, Object value){
      if( V1 == null)
         return addTo( new Vector(), value);
      V1.addElement( value);
      return V1;
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
      S.append("Finds all Peaks on one detector");
      S.append("@algorithm    "); 
      S.append("See Documentation for FindPeaks and DataSetTools.operator.Generic.TOF_SCD.Util.centroid.");
      S.append(" Also the Peak object is a Peak_new object with the grid's elements cleared.");
      S.append("@assumptions    "); 
      S.append("All inputs are relevent.");
      S.append(" If the grid is a RowColGrid it can be replaced by a UniformGrid");
      S.append("@param   ");
      S.append("The data set with the detector");
      S.append("@param   ");
      S.append("The detectorID");
      S.append("@param   ");
      S.append("The maximum number of peaks to return.");
      S.append("@param   ");
      S.append("The minimum peak intensity to look for.");
      S.append("@param   ");
      S.append("The minimum time channel to use.");
      S.append("@param   ");
      S.append("The maximum time channel to use.");
      S.append("@param   ");
      S.append("min row to keep");
      S.append("@param   ");
      S.append("max row to keep");
      S.append("@param   ");
      S.append("min col to keep");
      S.append("@param   ");
      S.append("max col to keep");
      S.append("@param   ");
      S.append("Monitor count");
      S.append( "@param MaxDSpacing   maximum d-spacing for crystal" );

      S.append( "@param NewPeakAlg Use new FindPeaks");
      S.append( "@param Use Smoothed Data" );
      S.append( "@param Use validity test" );
      S.append( "@param Use old Centroid" );
      S.append( "@param Show Peak Images" );
     
      S.append("@param   ");
      S.append("log buffer If this is a non-null StringBuffer, log info will"+
                         "be appended to it ");
      S.append("@return A Vector of Peaks  ");
      S.append("");
      S.append(" Also a Peaks file and an Experiment file are created.");
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
                     "HiddenOperator"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         DataSetTools.dataset.DataSet DS = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         int DetectorID = ((IntegerPG)(getParameter(1))).getintValue();
         int num_peaks = ((IntegerPG)(getParameter(2))).getintValue();
         int min_int = ((IntegerPG)(getParameter(3))).getintValue();
         int min_time_chan = ((IntegerPG)(getParameter(4))).getintValue();
         int max_time_chan = ((IntegerPG)(getParameter(5))).getintValue();
         java.lang.String PixelRow = getParameter(6).getValue().toString();
         java.lang.String PixelCol = getParameter(7).getValue().toString();
         int monCount = ((IntegerPG)(getParameter(8))).getintValue();
         float MaxDspacing =((FloatPG)(getParameter(9))).getfloatValue();

         boolean NewFindPeaks= ((BooleanPG)getParameter(10)).getbooleanValue();
         boolean  SmoothData= ((BooleanPG)getParameter(11)).getbooleanValue();
         boolean  ValidityTest= ((BooleanPG)getParameter(12)).getbooleanValue();
         boolean  Centroid= ((BooleanPG)getParameter(13)).getbooleanValue();
         boolean ShowPeaksView= ((BooleanPG)getParameter(14)).getbooleanValue();
         int numSlices= ((IntegerPG)(getParameter(15))).getintValue();
         Object logBuffer = getParameter(16).getValue();
         StringBuffer LogBuffer = null;
         if( logBuffer instanceof StringBuffer)
            LogBuffer =(StringBuffer)logBuffer;
         java.util.Vector Xres=Wizard.TOF_SCD.Util.findDetectorCentroidedPeaks(DS,DetectorID,num_peaks,min_int,
                     min_time_chan,max_time_chan,PixelRow, PixelCol,monCount,MaxDspacing, 

                    NewFindPeaks,
                     SmoothData,
                     ValidityTest,
                     Centroid,
                     ShowPeaksView,
                     numSlices,LogBuffer );

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



