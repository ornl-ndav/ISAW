/* 
 * File: findCentroidedPeaks.java
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
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

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class findCentroidedPeaks extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public findCentroidedPeaks(){
     super("findCentroidedPeaks");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "findCentroidedPeaks";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataDirPG("raw data path",null));
      addParameter( new DataDirPG("Output data path for the .peaks file.",null));
      addParameter( new IntArrayPG("The run numbers to load",""));
      addParameter( new IntArrayPG("The data set numbers to load in each run",""));
      addParameter( new StringPG("The experiment name",""));
      addParameter( new IntegerPG("The maximum number of peaks to retur",30));
      addParameter( new IntegerPG("minimum peak intensity to look fo",0));
      addParameter( new IntegerPG("minimum time channel to use.",0));
      addParameter( new IntegerPG("maximum time channel to use",50000));
      addParameter( new BooleanPG("append to prev file output",false));
      
      java.util.Vector args = new java.util.Vector(3);
        args.addElement( false );
        args.addElement(2);
        args.addElement( 0 );
      addParameter( new BooleanEnablePG("Use calibration information",args));
      addParameter( new LoadFilePG("SCD calibration file.",""));
      addParameter( new IntegerPG("Calibration file line(mode)",-1));
      addParameter( new IntArrayPG("Row/Col values to keep. Blank for all",""));
      addParameter( new StringPG("Data filename extension",".nxs"));
      addParameter( new StringPG("The prefix for the filename","SCD"));
      addParameter( new IntegerPG("maxNumThreads",1));
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
      S.append("This operator finds the peaks for a series of runs and for a series of data set numbers in each");
      S.append(" run.  Threading is used in this operator. A Peaks file and an experiment file are created.");
      S.append("@algorithm    "); 
      S.append("See algorithms for FindPeaks and centroid.");
      S.append(" The operations on one detector represents one Thread that is executed.");
      S.append(" ");
      S.append("@assumptions    "); 
      S.append("The Inputs represent valid");
      S.append("@param   ");
      S.append("The raw data path");
      S.append("@param   ");
      S.append("The output data path for the .peaks file");
      S.append("@param   ");
      S.append("The run numbers to load.");
      S.append("@param   ");
      S.append("The data set numbers to load in each run");
      S.append("@param   ");
      S.append("The experiment name (i.e. \"quartz\")");
      S.append("@param   ");
      S.append("The maximum number of peaks to return per detector");
      S.append("@param   ");
      S.append("The minimum peak intensity to look for.");
      S.append("@param   ");
      S.append("The minimum time channel to use");
      S.append("@param   ");
      S.append("The maximum time channel to use. for all.");
      S.append("@param   ");
      S.append("Append to Peaks and experiment  file (yes/no).");
      S.append("@param   ");
      S.append("Use the calibration file");
      S.append("@param   ");
      S.append("SCD calibration file");
      S.append("@param   ");
      S.append("SCD calibration file line to use or mode. -1 means default");
      S.append("@param   ");
      S.append("The Row/Col values to keep. Blank for all");
      S.append("@param   ");
      S.append("The name of the extension on the data file");
      S.append("@param   ");
      S.append("The prefix for the filename. Does not include path.");
      S.append("@param   ");
      S.append("The maximum number of threads to execute at one time");
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

         java.lang.String rawpath = getParameter(0).getValue().toString();
         java.lang.String outpath = getParameter(1).getValue().toString();
         java.lang.String runnums = (java.lang.String)(getParameter(2).getValue());
         java.lang.String dataSetNums = (java.lang.String)(getParameter(3).getValue());
         java.lang.String expname = getParameter(4).getValue().toString();
         int num_peaks = ((IntegerPG)(getParameter(5))).getintValue();
         int min_int = ((IntegerPG)(getParameter(6))).getintValue();
         int min_time_cha = ((IntegerPG)(getParameter(7))).getintValue();
         int max_time_chan = ((IntegerPG)(getParameter(8))).getintValue();
         boolean append = ((BooleanPG)(getParameter(9))).getbooleanValue();
         boolean useCalib = ((BooleanPG)(getParameter(10))).getbooleanValue();
         java.lang.String calibfile = getParameter(11).getValue().toString();
         int line2use = ((IntegerPG)(getParameter(12))).getintValue();
         java.lang.String PixelRow = getParameter(13).getValue().toString();
         java.lang.String extension = getParameter(14).getValue().toString();
         java.lang.String fileNamePrefix = getParameter(15).getValue().toString();
         int maxNumThreads = ((IntegerPG)(getParameter(16))).getintValue();
         java.util.Vector Xres=Wizard.TOF_SCD.Util.findCentroidedPeaks(rawpath,outpath,runnums,dataSetNums,expname,num_peaks,min_int,min_time_cha,max_time_chan,append,useCalib,calibfile,line2use,PixelRow,extension,fileNamePrefix,maxNumThreads );

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



