/*
 * File:  IntegrateMultiRunsForm.java   
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
 * Revision 1.6  2003/06/09 21:53:23  bouzekc
 * Updated documentation.
 * Added constructor to set HAS_CONSTANTS to reduce
 * the number of calls to setDefaultParameters().
 * Removed unused matrix name parameter and associate code.
 *
 * Revision 1.5  2003/06/06 15:12:02  bouzekc
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

/**
 * 
 *  This Form is a "port" of the script used to integrate 
 *  multiple SCD runs.  It "knows" to apply the 
 *  lsxxxx.expName.mat file to the SCDxxxx.run in the peaks
 *  file.
 */
public class IntegrateMultiRunsForm extends Form
{

  private Vector choices;
  protected static int RUN_NUMBER_WIDTH = 5;
  protected final String SCDName = "SCD";

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
  /**
   * Construct a Form with a default parameter list.
   */
  public IntegrateMultiRunsForm()
  {
    super( "IntegrateMultiRunsForm" );
    this.setDefaultParameters();
  }

  /**
   *  Construct a Form using the default parameter list.
   *
   *  @param hasConstParams         boolean indicating whether
   *                                this Form should have constant
   *                                parameters.
   */
  public IntegrateMultiRunsForm(boolean hasConstParams)
  {
    super( "IntegrateMultiRunsForm", hasConstParams );
    this.setDefaultParameters();
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Full constructor for IntegrateMultiRunsForm.
   *
   *  @param rawpath            The raw data path.
   *  @param outpath            The output data path for the *.integrate file.
   *  @param runnums            The run numbers to load.
   *  @param expname            The experiment name (i.e. "quartz").
   *  @param ctype              Number for the centering type.
   *  @param calibfile          SCD calibration file.
   *  @param time_slice_range   The time-slice range
   *  @param increase_amt       Amount to increase slice size by.
   */
  
  public IntegrateMultiRunsForm(String rawpath, String outpath, String runnums,
                                String expname, int ctype, String calibfile,
                                String time_slice_range, 
                                int increase_amt)
  {
    this();
    getParameter(0).setValue(rawpath);
    getParameter(1).setValue(outpath);
    getParameter(2).setValue(runnums);
    getParameter(3).setValue(expname);
    getParameter(4).setValue(choices.elementAt(ctype));
    getParameter(5).setValue(calibfile);
    getParameter(6).setValue(time_slice_range);
    getParameter(7).setValue(new Integer(increase_amt));
  }

  /**
   *
   *  Attempts to set reasonable default parameters for this form.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();

    if( choices==null || choices.size()==0 ) init_choices();
    
    //0
    addParameter(new DataDirPG( "Raw Data Path", "", false));
    //1
    addParameter(new DataDirPG("Peaks File Output Path", "", false));
    //2
    addParameter(new IntArrayPG("Run Numbers", "", false));
    //3
    addParameter(new StringPG("Experiment name", "quartz", false));
    //4
    ChoiceListPG clpg=new ChoiceListPG("Centering Type", choices.elementAt(0));
    clpg.addItems(choices);
    addParameter(clpg);
    //5
    addParameter(new LoadFilePG("SCD Calibration File", "/IPNShome/scd/instprm.dat", 
                                false));
    //6
    addParameter(new IntArrayPG("The Time-Slice Range", "-1:3", false));
    //7
    addParameter(new IntegerPG("Amount to Increase Slice Size By", 
                               new Integer(1), false));
    //8
    addParameter(new LoadFilePG( "Integrated Peaks File ", "", false));

    //don't monkey around with the run numbers and such if this form
    //relies on previously calculated values
    if(HAS_CONSTANTS)
      setParamTypes(new int[]{0,1,2,3,5}, new int[]{4,6,7}, new int[]{8});
    else
      setParamTypes(null, new int[]{0,1,2,3,4,5,6,7}, new int[]{8});
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
    s.append("@overview This Form is designed to find integrate peaks from ");
    s.append("multiple SCD RunFiles. ");
    s.append("It \"knows\" to apply the lsxxxx.expName.mat file to the ");
    s.append("SCDxxxx.run in the peaks file.\n");
    s.append("@assumptions It is assumed that:\n");
    s.append("1. Data of interest is in histogram 2.\n");
    s.append("2. There is a matrix file for each run, in the format \"ls");
    s.append("<experiment name><run number>.mat\" in the same directory as ");
    s.append("the peaks file\n.");
    s.append("@algorithm This Form first gets all the user input parameters, ");
    s.append("then for each runfile, it loads the first histogram, the SCD ");
    s.append("calibration data, and calls Integrate.\n");
    s.append("@param rawpath The raw data path.\n");
    s.append("@param outpath The output data path for the *.integrate file.\n");
    s.append("@param runnums The run numbers to load.\n");
    s.append("@param expname The experiment name (i.e. \"quartz\").\n");
    s.append("@param ctype Number for the centering type.\n");
    s.append("@param calibfile SCD calibration file.\n");
    s.append("@param time_slice_range The time-slice range.\n");
    s.append("@param increase_amt Amount to increase slice size by.\n");
    s.append("@return A Boolean indicating success or failure of the Form's ");
    s.append("execution.\n");
    s.append("@error Invalid raw data path.\n");
    s.append("@error Invalid peaks file path.\n");
    s.append("@error Invalid run numbers.\n");
    s.append("@error Invalid experiment name.\n");
    s.append("@error Invalid calibration file name.\n");
    return s.toString();
  }

  /**
   *  Returns the String command used for invoking this
   *  Form in a Script.
   */
  public String getCommand()
  {
    return "INTEGRATEMULTIRUNSFORM";
  }

  /**
   *  This Form first gets all the user input parameters, then for each runfile, 
   *  it loads the first histogram, the SCD calibration data, and calls Integrate.
   *
   *  @return A Boolean indicating success or failure.
   */
  public Object getResult()
  {
    SharedData.addmsg("Executing...\n");
    IParameterGUI param;
    Object obj;
    String outputDir, centerType, matrixName, calibFile, expName, rawDir;
    String integName, sliceRange, loadName, runNum;
    boolean append, first;
    int timeSliceDelta, histNum;
    DataSet histDS;
    int[] runsArray;

    //get raw data directory
    //should be no need to check this for validity
    param = (IParameterGUI)super.getParameter( 0 );
    rawDir = param.getValue().toString() + "/";
    param.setValid(true);

    //get output directory
    //should be no need to check this for validity
    param = (IParameterGUI)getParameter( 1 );
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
         "ERROR: you must enter one or more valid run numbers.");

    //get experiment name
    param = (IParameterGUI)getParameter( 3 );
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        expName = obj.toString();
        param.setValid(true);
    }
    else
      return errorOut(param,
         "ERROR: you must enter a valid experiment name.");

    //get centering type
    param = (IParameterGUI)getParameter( 4 );
    obj = param.getValue();
    if( obj != null  )
    {
      centerType = obj.toString();
      param.setValid(true);
    }
    else
      return errorOut(param,
        "ERROR: you must enter a valid centering type.");

    //get calibration file name
    param = (IParameterGUI)getParameter( 5 );
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        calibFile = obj.toString();
        param.setValid(true);
    }
    else
      return errorOut(param,
         "ERROR: you must enter a valid calibration file name.");

    //get time slice range
    param = (IParameterGUI)getParameter( 6 );
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        sliceRange = obj.toString();
        param.setValid(true);
    }
    else
      return errorOut(param,
         "ERROR: you must enter a valid time slice range.");

    //get time slice increase increment
    param = (IParameterGUI)getParameter( 7 );
    obj = param.getValue();
    if( obj != null && obj instanceof Integer )
    {
        timeSliceDelta = ((Integer)obj).intValue();
        param.setValid(true);
    }
    else
      return errorOut(param,
        "ERROR: you must enter a valid integer to increment the slice size by.");

    //the name for the saved *.integrate file
    integName = outputDir + "/" + expName + ".integrate";
    integName = StringUtil.setFileSeparator(integName);

    //first time through the file
    first = true;
    //appending to file?
    append = false;

    for(int i = 0; i < runsArray.length; i++)
    {
      /*load the histogram and monitor for the current run. 
        We don't want to remove the leading zeroes!*/
      runNum = DataSetTools
               .util
               .Format
               .integerPadWithZero(runsArray[i], RUN_NUMBER_WIDTH);

      loadName = rawDir + "/" + SCDName + runNum + ".RUN";
      loadName = StringUtil.setFileSeparator(loadName);

      SharedData.addmsg("Loading " + loadName + ".");

      //get the histogram from runfile retriever.
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

      SharedData.addmsg("Integrating peaks for " + loadName);

      //load the SCD calibration data
      obj = new LoadSCDCalib(histDS, calibFile,1,"").getResult();
      if(obj instanceof ErrorString)
        return errorOut("LoadSCDCalib failed: " + obj.toString());

      /*Gets matrix file "lsxxxx.mat" for each run
        The "1" means that every peak will be written to the integrate.log file.*/
      matrixName = StringUtil.setFileSeparator(
                     outputDir + "/ls" + expName + runNum + ".mat");

      SharedData.addmsg("Integrating run number " + runNum + ".");

      obj = new Integrate(histDS, integName, 
                         matrixName,
                         sliceRange, timeSliceDelta, 1,
                         append).getResult();
      if(obj instanceof ErrorString)
        return errorOut("Integrate failed: " + obj.toString());

      if (first)
      {
        first = false;  //no longer our first time through
        append = true;  //start appending to the file
      }
    }
    
    SharedData.addmsg("--- IntegrateMultiRunsForm is done. ---");
    SharedData.addmsg("Peaks are listed in " + integName);

    //set the integrate file name for the result
    param = (IParameterGUI)getParameter(9);
    param.setValue(integName.toString());
    param.setValid(true);

    //not really sure what to return
    return new Boolean(true);
  }

  /**
   * Create the vector of choices for the ChoiceListPG of centering.
   */
  private void init_choices(){
    choices=new Vector();
    choices.add("primitive");               // 0 
    choices.add("a centered");              // 1
    choices.add("b centered");              // 2
    choices.add("c centered");              // 3
    choices.add("[f]ace centered");         // 4
    choices.add("[i] body centered");       // 5
    choices.add("[r]hombohedral centered"); // 6
  }
}
