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
 *  multiple SCD runs.
 */
public class IntegrateMultiRunsForm extends Form
{

  private Vector choices;
  //set it up as a standalone form
  private boolean HAS_CONSTANTS = false;
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


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Full constructor for IntegrateMultiRunsForm.
   *
   *  @param rawpath            The raw data path.
   *  @param outpath            The output data path for the *.integrate file.
   *  @param runnums            The run numbers to load.
   *  @param expname            The experiment name (i.e. "quartz").
   *  @param calibfile          SCD calibration file.
   *  @param time_slice_range   The time-slice range
   *  @param increase_amt       Amount to increase slice size by.
   *  @param matrix_name        The matrix file name to load.
   */
  
  public IntegrateMultiRunsForm(String rawpath, String outpath, String runnums,
                                String expname, String calibfile, 
                                String time_slice_range, 
                                int increase_amt, String matrix_name)
  {
    this();
    getParameter(0).setValue(rawpath);
    getParameter(1).setValue(outpath);
    getParameter(2).setValue(runnums);
    getParameter(3).setValue(expname);
    getParameter(4).setValue(calibfile);
    getParameter(5).setValue(time_slice_range);
    getParameter(6).setValue(new Integer(increase_amt));
    getParameter(7).setValue(matrix_name);
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
    addParameter(new DataDirPG("Output Path", "", false));
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
    LoadFilePG lfpg=new LoadFilePG("Matrix File Name",null, false);
    lfpg.setFilter(new MatrixFilter());
    addParameter( lfpg );
    //9
    addParameter(new LoadFilePG( "Integrated Peaks File ", "", false));

    //don't monkey around with the run numbers and such if this form
    //relies on previously calculated values
    if(HAS_CONSTANTS)
      setParamTypes(new int[]{0,1,2,3,5,8}, new int[]{4,6,7}, new int[]{9});
    else
      setParamTypes(null, new int[]{0,1,2,3,4,5,6,7,8}, new int[]{9});
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
    s.append("@assumptions It is assumed that:\n");
    s.append("1. Data of interest is in histogram 2.\n");
    s.append("2. There is a matrix file for each run.");
    s.append("@algorithm ");
    s.append("@return A Boolean indicating success or failure of the Form's ");
    s.append("execution.\n");
    s.append("@error ");
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
   *
   *  @return A Boolean indicating success or failure.
   */
  public Object getResult()
  {
    SharedData.addmsg("Executing...\n");
    IParameterGUI param;
    Object obj;
    String outputDir, centerType, matrixName, calibFile, expName, rawDir;
    String sliceRange, loadName, runNum;
    boolean append, first;
    StringBuffer integName;
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
    {
       param.setValid(false);
       SharedData.addmsg(
         "ERROR: you must enter one or more valid run numbers.\n");
       return new Boolean(false);
    }

    //get experiment name
    param = (IParameterGUI)getParameter( 3 );
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        expName = obj.toString();
        param.setValid(true);
    }
    else
    {
       param.setValid(false);
       SharedData.addmsg(
         "ERROR: you must enter a valid experiment name.\n");
       return new Boolean(false);
    }

    //get centering type
    param = (IParameterGUI)getParameter( 4 );
    obj = param.getValue();
    if( obj != null  )
    {
      centerType = obj.toString();
      param.setValid(true);
    }
    else
    {
      param.setValid(false);
      SharedData.addmsg(
        "ERROR: you must enter a valid centering type.");
      return new Boolean(false);
    }

    //get calibration file name
    param = (IParameterGUI)getParameter( 5 );
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        calibFile = obj.toString();
        param.setValid(true);
    }
    else
    {
       param.setValid(false);
       SharedData.addmsg(
         "ERROR: you must enter a valid calibration file name.");
       return new Boolean(false);
    }

    //get time slice range
    param = (IParameterGUI)getParameter( 6 );
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        sliceRange = obj.toString();
        param.setValid(true);
    }
    else
    {
       param.setValid(false);
       SharedData.addmsg(
         "ERROR: you must enter a valid time slice range.");
       return new Boolean(false);
    }

    //get time slice increase increment
    param = (IParameterGUI)getParameter( 7 );
    obj = param.getValue();
    if( obj != null && obj instanceof Integer )
    {
        timeSliceDelta = ((Integer)obj).intValue();
        param.setValid(true);
    }
    else
    {
       param.setValid(false);
       SharedData.addmsg(
         "ERROR: you must enter a valid integer " +
         "to increment the slice size by.");
       return new Boolean(false);
    }

    //get matrix file name
    param = (IParameterGUI)getParameter( 8 );
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        matrixName = obj.toString();
        param.setValid(true);
    }
    else
    {
       param.setValid(false);
       SharedData.addmsg(
         "ERROR: you must enter a matrix file name.");
       return new Boolean(false);
    }

    //the name for the saved *.integrate file
    integName = new StringBuffer();
    integName.append(outputDir);
    integName.append(expName);
    integName.append(".integrate");

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

      loadName = rawDir + SCDName + runNum + ".RUN";

      /*get the histogram from runfile retriever.
      histNum = some RunfileRetriever thing;*/
      histNum = 1;

      /*If you want to be able to use a group mask,
        change the "" below to a String variable.
        I've been told this is not used. -CMB*/
      obj = new LoadOneHistogramDS(loadName, histNum, "").getResult();

      //make sure it is a DataSet
      if(obj instanceof DataSet)
        histDS = (DataSet)obj;
      else
      {
        SharedData.addmsg(obj.toString());
        return new Boolean(false);
      }

      SharedData.addmsg("Integrating peaks for " + 
                         histDS.toString());

      //load the SCD calibration data
      obj = new LoadSCDCalib(histDS, calibFile,1,"").getResult();
      if(obj instanceof ErrorString)
      {
        SharedData.addmsg(obj.toString());
        return new Boolean(false);
      }

      /*Gets matrix file "lsxxxx.mat" for each run
        The "1" means that every peak will be written to the integrate.log file.
        At the moment, this will only load one matrix file, and will stay so
        until a LsqrsForm is written that can create these lsxxxx.mat files
        automagically.*/
      obj = new Integrate(histDS, integName.toString(), 
                         /*outputDir + "ls"  + runsArray[i] + ".mat",*/
                         matrixName,
                         sliceRange, timeSliceDelta, 1,
                         append).getResult();
      if(obj instanceof ErrorString)
      {
        SharedData.addmsg(obj.toString());
        return new Boolean(false);
      }

      if (first)
      {
        first = false;  //no longer our first time through
        append = true;  //start appending to the file
      }
    }
    
    SharedData.addmsg("--- integrate_multiple_runs is done. ---");
    SharedData.addmsg("Peaks are listed in " + integName.toString());

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


  /**
   *  Sets the HAS_CONSTANTS variable so that the Form can be
   *  run as a standalone or first Form, or as a Form that
   *  relies on previous Forms.
   *
   *  @param    constant    Set true if this is a Form that
   *                        relies on previous Forms, or
   *                        false if it is a standalone Form.
   */
  public void setHasConstants(boolean constant)
  {
    this.HAS_CONSTANTS = constant;
  }
}
