/*
 * File:  FindMultiplePeaksForm.java   
 *
 * Copyright (C) 2003, Chris M. Bouzek
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 *           Chris M. Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 */

package Wizard.TOF_SCD;

import  java.util.Vector;
import  DataSetTools.util.*;
import  DataSetTools.operator.Generic.TOF_SCD.*;
import  DataSetTools.parameter.*;
import  DataSetTools.wizard.*;
import  DataSetTools.operator.DataSet.Attribute.*;
import  DataSetTools.dataset.DataSet;
import  DataSetTools.operator.DataSet.Math.Analyze.*;
import  DataSetTools.operator.Generic.Load.LoadOneHistogramDS;
import  DataSetTools.operator.Generic.Load.LoadMonitorDS;

/**
 * 
 *  This Form is a "port" of the script used to find peaks in 
 *  multiple SCD files.
 */
public class FindMultiplePeaksForm extends Form
{

  protected static int RUN_NUMBER_WIDTH = 5;
  protected final String SCDName = "SCD";

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
  /**
   * Construct a Form with a default parameter list.
   */
  public FindMultiplePeaksForm()
  {
    super( "FindMultiplePeaksForm" );
    this.setDefaultParameters();
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Full constructor for FindMultiplePeaksForm.
   *
   *  @param rawpath        The raw data path.
   *  @param outpath        The output data path for the *.peaks file.
   *  @param runnums        The run numbers to load.
   *  @param expname        The experiment name (i.e. "quartz").
   *  @param num_peaks      The maximum number of peaks to return.
   *  @param min_int        The minimum peak intensity to look for.
   *  @param append         Append to file (yes/no).
   *  @param calibfile      SCD calibration file.
   */
  
  public FindMultiplePeaksForm(String rawpath, String outpath, String runnums, 
                               String expname, int num_peaks, 
                               int min_int, boolean append, String calibfile)
  {
    this();
    getParameter(0).setValue(rawpath);
    getParameter(1).setValue(outpath);
    getParameter(2).setValue(runnums);
    getParameter(3).setValue(expname);
    getParameter(4).setValue(new Integer(num_peaks));
    getParameter(5).setValue(new Integer(min_int));
    getParameter(6).setValue(new Boolean(append));
    getParameter(7).setValue(calibfile);
  }

  /**
   *
   *  Attempts to set reasonable default parameters for this form.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter(new DataDirPG( "Raw Data Path", "", false));
    addParameter(new DataDirPG("Peaks File Output Path", "", false));
    addParameter(new IntArrayPG("Run Numbers", "06496:06498", false));
    addParameter(new StringPG( "Experiment name", "quartz", false));
    addParameter(new IntegerPG( "Maximum Number of Peaks", new Integer(50), false));
    addParameter(new IntegerPG( "Minimum Peak Intensity", new Integer(3), false));
    addParameter(new BooleanPG( "Append to File?", new Boolean(false), false));
    addParameter(new LoadFilePG( "SCD Calibration File", 
                                 "/IPNShome/scd/instprm.dat", false));
    addParameter(new LoadFilePG( "Peaks File ", "temp.peaks", false));
    setParamTypes(null,new int[]{0,1,2,3,4,5,6,7}, new int[]{8});
  }


  /**
   *
   *  Documentation for this OperatorForm.  Follows javadoc
   *  conventions.
   *
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer();
    s.append("@overview This Form is designed to find peaks from multiple");
    s.append("SCD RunFiles. ");
    s.append("@assumptions It is assumed that:\n");
    s.append("1. Data of interest is in the first histogram.\n");
    s.append("2. If the calibration file is not specified then the real ");
    s.append("space conversion is not performed.\n");
    s.append("@algorithm First the calibration data from the SCD file is ");
    s.append("loaded.\n");
    s.append("Then the FindPeaks Operator is used to find the peaks, based ");
    s.append("on user input.\n");
    s.append("Then the CentroidPeaks Operator is used to find the peak ");
    s.append("centers.\n");
    s.append("Next it writes the results to the specified *.peaks file.\n");
    s.append("Finally it writes the SCD experiment (*.x) file.\n");
    s.append("@param rawpath The raw data path.\n");
    s.append("@param outpath The output data path for the *.peaks file.\n");
    s.append("@param runnums The run numbers to load.\n");
    s.append("@param expname The experiment name (i.e. \"quartz\").\n");
    s.append("@param num_peaks The maximum number of peaks to return.\n");
    s.append("@param min_int The minimum peak intensity to look for.\n");
    s.append("@param append Append to file (yes/no).\n");
    s.append("@param calibfile SCD calibration file.\n");
    s.append("@return A Boolean indicating success or failure of the Form's ");
    s.append("execution.\n");
    s.append("@error An error is returned if a valid experiment name is not ");
    s.append("entered.\n");
    s.append("@error An error is returned if a valid number of peaks is not ");
    s.append("entered.\n");
    s.append("@error An error is returned if a valid minimum peak intensity ");
    s.append("is not entered.\n");
    s.append("@error An error is returned if a valid calibration file name ");
    s.append("is not entered.\n");
    return s.toString();
  }

  /**
   *  Returns the String command used for invoking this
   *  Form in a Script.
   */
  public String getCommand()
  {
    return "FINDMULTIPEAKSFORM";
  }


 /**
   * getResult() finds multiple peaks using the following algorithm:
   * First the calibration data from the SCD file is loaded.  Then the 
   * FindPeaks Operator is used to find the peaks, based on user input.
   * Then the CentroidPeaks Operator is used to find the peak centers.
   * Next it writes the results to the specified *.peaks file.  Finally it 
   * writes the SCD experiment (*.x) file.
   *
   *  @return A Boolean indicating success or failure.
   */
  public Object getResult()
  {
    SharedData.addmsg("Executing...\n");
    IParameterGUI param;
    int maxPeaks, minIntensity, histNum;
    Float monCount;
    String rawDir, outputDir, saveName, expName, calibFile, loadName;
    String runNum;
    boolean appendToFile, first; 
    Vector peaksVec;
    DataSet histDS, monDS;
    Object obj;
    
    int[] runsArray;

    //get raw data directory
    //should be no need to check this for validity
    param = (IParameterGUI)super.getParameter( 0 );
    rawDir = param.getValue().toString() + "/";
    param.setValid(true);

    //get output directory
    //should be no need to check this for validity
    param = (IParameterGUI)super.getParameter( 1 );
    outputDir = param.getValue().toString() + "/";
    param.setValid(true);

    //gets the run numbers
    param = (IParameterGUI)super.getParameter(2);
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        runsArray = IntList.ToArray(obj.toString());
        param.setValid(true);
    }
   else
     return errorOut(param,
       "ERROR: you must enter one or more valid run numbers.\n");

    //get experiment name
    param = (IParameterGUI)super.getParameter( 3 );
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        expName = obj.toString();
        param.setValid(true);
    }
    else
      return errorOut(param,
         "ERROR: you must enter a valid experiment name.\n");

    //get maximum number of peaks to find
    param = (IParameterGUI)super.getParameter( 4 );
    obj = param.getValue();
    if( obj != null && obj instanceof Integer )
    {
        maxPeaks = ((Integer)obj).intValue();
        param.setValid(true);
    }
    else
      return errorOut(param,
         "ERROR: you must enter a valid number of peaks.\n");

    //get minimum intensity of peaks
    param = (IParameterGUI)super.getParameter( 5 );
    obj = param.getValue();
    if( obj != null && obj instanceof Integer )
    {
        minIntensity = ((Integer)obj).intValue();
        param.setValid(true);
    }
    else
      return errorOut(param,
         "ERROR: you must enter a valid minimum peak intensity.\n");

    //get append to file value
    param = (IParameterGUI)super.getParameter( 6 );
    //this one doesn't need to be checked for validity
    param.setValid(true);
    appendToFile = ((BooleanPG)param).getbooleanValue();

    //get calibration file name
    param = (IParameterGUI)super.getParameter( 7 );
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        calibFile = obj.toString();
        param.setValid(true);
    }
    else
      return errorOut(param,
         "ERROR: you must enter a valid calibration file name.\n");

    first = true;
    //the name for the saved file
    saveName = outputDir + expName + ".peaks";

    for(int i = 0; i < runsArray.length; i++)
    {
      /*load the histogram and monitor for the current run. 
        We don't want to remove the leading zeroes!*/
      runNum = DataSetTools
               .util
               .Format
               .integerPadWithZero(runsArray[i], RUN_NUMBER_WIDTH);

      loadName = rawDir + SCDName + runNum + ".RUN";

      /*get the histogram.  We want to retrieve the first one.*/
      histNum = 1;
      
      /*If you want to be able to use a group mask,
        change the "" below to a String variable.
        I've been told this is not used. -CMB*/
      obj = new LoadOneHistogramDS(loadName, histNum, "").getResult();

      //make sure it is a DataSet
      if(obj instanceof DataSet)
        histDS = (DataSet)obj;
      else
        return errorOut("LoadOneHistogramDS failed: " + obj.toString());

      obj = new LoadMonitorDS(loadName).getResult();

      //make sure it is a DataSet
      if(obj instanceof DataSet)
        monDS = (DataSet)obj;
      else
        return errorOut("LoadMonitorDS failed: " + obj.toString());

      SharedData.addmsg("Finding peaks for ");
      SharedData.addmsg(histDS.toString());
      
      //integrate
      obj = new IntegrateGroup(monDS, 1, 0, 50000).getResult();

      if(obj instanceof Float)
        monCount = (Float)obj;
      else
        return errorOut("IntegrateGroup failed: " + obj.toString());

      //load calibration data 
      obj = new LoadSCDCalib(histDS, calibFile , 1 ,"").getResult();
      if(obj instanceof ErrorString)
        return errorOut("LoadSCDCalib failed: " + obj.toString());

      // find peaks
      obj = new FindPeaks(histDS, monCount.floatValue(), maxPeaks, 
                          minIntensity).getResult();

      if(obj instanceof Vector)
        peaksVec = (Vector)obj;
      else
        return errorOut("FindPeaks failed: " + obj.toString());

      //"centroid" (find the center) the peaks
      obj = new CentroidPeaks(histDS, peaksVec).getResult();

      if(obj instanceof Vector)
        peaksVec = (Vector)obj;
      else
        return errorOut("CentroidPeaks failed: " + obj.toString());

      // write out the results to the .peaks file
      obj = new WritePeaks(saveName, peaksVec, 
                     new Boolean(appendToFile)).getResult();
      if(obj instanceof ErrorString)
        return errorOut("WritePeaks failed: " + obj.toString());


      //write the SCD experiment file
      obj = new WriteExp(histDS, monDS, outputDir + expName + ".x", 1, 
                   appendToFile).getResult();

      if(obj instanceof ErrorString)
        return errorOut("WriteExp failed: " + obj.toString());
                   
      if( first )
      {
        first = false;
        appendToFile = true;
      }
    }
  
    SharedData.addmsg("--- Done finding peaks. ---");

    SharedData.addmsg("Peaks are listed in ");
    SharedData.addmsg(saveName);

    //set the peaks file name
    param = (IParameterGUI)super.getParameter(8);
    param.setValue(saveName);
    param.setValid(true);

    //not really sure what to return
    return new Boolean(true);
  }
}
