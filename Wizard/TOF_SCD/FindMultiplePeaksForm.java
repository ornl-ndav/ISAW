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
 * $Log$
 * Revision 1.10  2003/06/16 23:04:30  bouzekc
 * Now set up to use the multithreaded progress bar in
 * DataSetTools.components.ParametersGUI.
 *
 * Revision 1.9  2003/06/11 23:04:05  bouzekc
 * No longer uses StringUtil.setFileSeparator as DataDirPG
 * now takes care of this.
 *
 * Revision 1.8  2003/06/11 22:39:20  bouzekc
 * Updated documentation.  Moved file separator "/" code out
 * of loop.
 *
 * Revision 1.7  2003/06/10 19:54:00  bouzekc
 * Fixed bug where the peaks file was not written with every
 * run.
 * Updated documentation.
 *
 * Revision 1.6  2003/06/10 16:45:48  bouzekc
 * Moved creation of Operators out of the for loop and
 * into a private method to avoid excessive Object re-creation.
 * Added parameter to specify line in SCD calibration file.
 *
 * Revision 1.5  2003/06/06 15:12:00  bouzekc
 * Added log message header to file.
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
  private LoadOneHistogramDS loadHist;
  private LoadMonitorDS loadMon;
  private IntegrateGroup integGrp;
  private LoadSCDCalib loadSCD;
  private FindPeaks fPeaks;
  private CentroidPeaks cenPeaks;
  private WriteExp wrExp;
  private WritePeaks wrPeaks;

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
   *  @param line2use       SCD calibration file line to use.
   *  @param calibfile      SCD calibration file.
   */
  
  public FindMultiplePeaksForm(String rawpath, String outpath, String runnums, 
                               String expname, int num_peaks, 
                               int min_int, boolean append, int line2use,
                               String calibfile)
  {
    this();
    getParameter(0).setValue(rawpath);
    getParameter(1).setValue(outpath);
    getParameter(2).setValue(runnums);
    getParameter(3).setValue(expname);
    getParameter(4).setValue(new Integer(num_peaks));
    getParameter(5).setValue(new Integer(min_int));
    getParameter(6).setValue(new Boolean(append));
    getParameter(7).setValue(new Integer(line2use));
    getParameter(8).setValue(calibfile);
  }

  /**
   *
   *  Attempts to set reasonable default parameters for this form.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    //0
    addParameter(new DataDirPG( "Raw Data Path", "", false));
    //1
    addParameter(new DataDirPG("Peaks File Output Path", "", false));
    //2
    addParameter(new IntArrayPG("Run Numbers", "06496:06498", false));
    //3
    addParameter(new StringPG( "Experiment name", "quartz", false));
    //4
    addParameter(new IntegerPG( "Maximum Number of Peaks", new Integer(50), false));
    //5
    addParameter(new IntegerPG( "Minimum Peak Intensity", new Integer(3), false));
    //6
    addParameter(new BooleanPG( "Append Data to File?", new Boolean(false), false));
    //7
    addParameter(new IntegerPG( "SCD Calibration File Line to Use", 
                                new Integer(-1), false));
    //8
    addParameter(new LoadFilePG( "SCD Calibration File", 
                                 "/IPNShome/scd/instprm.dat", false));
    //9
    addParameter(new LoadFilePG( "Peaks File ", "temp.peaks", false));
    setParamTypes(null,new int[]{0,1,2,3,4,5,6,7,8}, new int[]{9});
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
    s.append("@param append Whether to append data to the peaks file.\n");
    s.append("@param line2use SCD calibration file line to use.\n");
    s.append("@param calibfile SCD calibration file.\n");
    s.append("@param peaksFile Peaks filename that data is written to.\n");
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
    int maxPeaks, minIntensity, SCDline;
    float increment;
    Float monCount;
    String rawDir, outputDir, saveName, expName, calibFile, loadName;
    String runNum, expFile;
    boolean appendToFile, first; 
    Vector peaksVec;
    DataSet histDS, monDS;
    Object obj;
    
    int[] runsArray;

    //get raw data directory
    //should be no need to check this for validity
    param = (IParameterGUI)super.getParameter( 0 );
    rawDir = param.getValue().toString();
    param.setValid(true);

    //get output directory
    //should be no need to check this for validity
    param = (IParameterGUI)super.getParameter( 1 );
    outputDir = param.getValue().toString();
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

    //get line number for SCD calibration file
    param = (IParameterGUI)super.getParameter( 7 );
    obj = param.getValue();
    if( obj != null && obj instanceof Integer )
    {
        SCDline = ((Integer)obj).intValue();
        param.setValid(true);
    }
    else
      return errorOut(param,
         "ERROR: you must enter a valid line number to use.\n");

    //get calibration file name
    param = (IParameterGUI)super.getParameter( 8 );
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
    expFile = outputDir + expName + ".x";

    //to avoid excessive object creation, we'll create all of the 
    //Operators here, then just set their parameters in the loop
    createFindPeaksOperators(calibFile, maxPeaks, minIntensity,
                             saveName, expFile, SCDline);


    //set the increment amount
    increment = (1.0f / runsArray.length) * 100.0f;

    for(int i = 0; i < runsArray.length; i++)
    {
      /*load the histogram and monitor for the current run. 
        We don't want to remove the leading zeroes!*/
      runNum = DataSetTools
               .util
               .Format
               .integerPadWithZero(runsArray[i], RUN_NUMBER_WIDTH);

      loadName = rawDir + SCDName + runNum + ".RUN";

      SharedData.addmsg("Loading " + loadName + ".");

      //load the histogram
      
      loadHist.getParameter(0).setValue(loadName);
      obj = loadHist.getResult();
      //make sure it is a DataSet
      if(obj instanceof DataSet)
        histDS = (DataSet)obj;
      else
        return errorOut("LoadOneHistogramDS failed: " + obj.toString());

      //load the monitor
      
      loadMon.getParameter(0).setValue(loadName);
      obj = loadMon.getResult();
      //make sure it is a DataSet
      if(obj instanceof DataSet)
        monDS = (DataSet)obj;
      else
        return errorOut("LoadMonitorDS failed: " + obj.toString());

      SharedData.addmsg("Finding peaks for " + loadName + ".");
      
      //integrate
      
      integGrp.setDataSet(monDS);
      obj = integGrp.getResult();
      if(obj instanceof Float)
        monCount = (Float)obj;
      else
        return errorOut("IntegrateGroup failed: " + obj.toString());

      //load calibration data 

      loadSCD.setDataSet(histDS);
      obj = loadSCD.getResult();
      if(obj instanceof ErrorString)
        return errorOut("LoadSCDCalib failed: " + obj.toString());

      // find peaks

      fPeaks.getParameter(0).setValue(histDS);
      fPeaks.getParameter(1).setValue(monCount);
      obj = fPeaks.getResult();
      if(obj instanceof Vector)
        peaksVec = (Vector)obj;
      else
        return errorOut("FindPeaks failed: " + obj.toString());

      //"centroid" (find the center) the peaks

      cenPeaks.getParameter(0).setValue(histDS);
      cenPeaks.getParameter(1).setValue(peaksVec);
      obj = cenPeaks.getResult();
      if(obj instanceof Vector)
        peaksVec = (Vector)obj;
      else
        return errorOut("CentroidPeaks failed: " + obj.toString());

      SharedData.addmsg("Writing peaks for " + loadName + ".");

      // write out the results to the .peaks file

      wrPeaks.getParameter(1).setValue(peaksVec);
      wrPeaks.getParameter(2).setValue(new Boolean(appendToFile));
      obj = wrPeaks.getResult();
      if(obj instanceof ErrorString)
        return errorOut("WritePeaks failed: " + obj.toString());


      //write the SCD experiment file

      wrExp.getParameter(0).setValue(histDS);
      wrExp.getParameter(1).setValue(monDS);
      wrExp.getParameter(4).setValue(new Boolean(appendToFile));
      obj = wrExp.getResult();
      if(obj instanceof ErrorString)
        return errorOut("WriteExp failed: " + obj.toString());
                   
      if( first )
      {
        first = false;
        appendToFile = true;
      }

      //fire a property change event off to any listeners
      //again, these are incremental changes in order to fit in with the
      //overall Wizard progress bar
      super.fireValueChangeEvent(-1, (int)increment);
    }
  
    SharedData.addmsg("--- Done finding peaks. ---");

    SharedData.addmsg("Peaks are listed in ");
    SharedData.addmsg(saveName);

    //set the peaks file name
    param = (IParameterGUI)super.getParameter(9);
    param.setValue(saveName);
    param.setValid(true);

    return Boolean.TRUE;
  }

  /**
   *  Creates the Operators necessary for this Form and sets their
   *  constant values.
   *
   *  @param  calibFile              SCD calibration file.
   *
   *  @param  maxPeaks               Maximum number of peaks.
   *
   *  @param  minInten               Minimum peak intensity.
   *
   *  @param  peaksName              Fully qualified peaks file name.
   *
   *  @param  expFile                Fully qualified experiment file name.
   *
   *  @param  SCDline                The line to use from the SCD calib file.
   *
   */
  private void createFindPeaksOperators(String calibFile, int maxPeaks,
                                        int minInten, String peaksName, 
                                        String expFile, int SCDline)
  {
    loadHist = new LoadOneHistogramDS();
    loadMon = new LoadMonitorDS(); 
    integGrp = new IntegrateGroup(); 
    loadSCD = new LoadSCDCalib(); 
    fPeaks = new FindPeaks(); 
    cenPeaks = new CentroidPeaks(); 
    wrExp = new WriteExp(); 
    wrPeaks = new WritePeaks(); 

    //LoadOneHistogramDS

    //get the histogram.  We want to retrieve the first one.
    loadHist.getParameter(1).setValue(new Integer(1));
    /*If you want to be able to use a group mask,
      change the "" below to a String variable.
      I've been told this is not used. -CMB*/
    loadHist.getParameter(2).setValue("");

    //IntegrateGroup
    
    integGrp.getParameter(0).setValue(new Integer(1));
    integGrp.getParameter(1).setValue(new Float(0));
    integGrp.getParameter(2).setValue(new Float(50000));

    //LoadSCDCalib

    loadSCD.getParameter(0).setValue(calibFile);
    loadSCD.getParameter(1).setValue(new Integer(SCDline));
    loadSCD.getParameter(2).setValue("");

    //FindPeaks

    fPeaks.getParameter(2).setValue(new Integer(maxPeaks));
    fPeaks.getParameter(3).setValue(new Integer(minInten));

    //WritePeaks

    wrPeaks.getParameter(0).setValue(peaksName);

    //WriteExp
    wrExp.getParameter(2).setValue(expFile);
    wrExp.getParameter(3).setValue(new Integer(1));
  }
}
