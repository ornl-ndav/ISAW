/*
 * File:  BlindJForm.java   
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
import  DataSetTools.parameter.*;
import  DataSetTools.wizard.*;
import  Operators.TOF_SCD.BlindJ;

/**
 * 
 *  This is a Form to add extra functionality to BlindJ.  It outputs an
 *  expname#.mat matrix file for each run number (#) that it is given.
 *  Other than that, it functions in a similar manner to BlindJ.
 */
public class BlindJForm extends Form
{

  protected static int RUN_NUMBER_WIDTH = 5;

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
  /**
   * Construct a Form with a default parameter list.
   */
  public BlindJForm()
  {
    super( "BlindJForm" );
    this.setDefaultParameters();
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Full constructor for BlindJForm.
   *
   *  @param runnums        The run numbers to use for naming the
   *                        matrix files.
   *  @param expname        The experiment name (i.e. "quartz").
   *  @param peakspath      The path where the peaks file is.
   *  @param seqnum         List of sequence numbers to use.
   */
  
  public BlindJForm(String runnums, String expname, String peakspath,
                    String seqnum)
  {
    this();
    getParameter(0).setValue(runnums);
    getParameter(1).setValue(expname);
    getParameter(2).setValue(peakspath);
    getParameter(3).setValue(seqnum);
  }

  /**
   *
   *  Attempts to set reasonable default parameters for this form.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter(new IntArrayPG("Run Numbers", "", false));
    addParameter(new StringPG( "Experiment name", "", false));
    addParameter(new DataDirPG("Peaks File Path", "", false));
    addParameter(new IntArrayPG( "Sequence Numbers", "20:32,40,42", false));
    addParameter(new ArrayPG( "Matrix files", new Vector(), false));

    if(HAS_CONSTANTS)
      setParamTypes(new int[]{0,1,2},new int[]{3}, new int[]{4});
    else
      setParamTypes(null, new int[]{0,1,2,3}, new int[]{4});
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
    s.append("@overview This is a Form to add extra functionality to ");
    s.append("BlindJ.  It outputs an expname#.mat matrix file for each ");
    s.append("run number (#) that it is given.  Other than that, it ");
    s.append("functions in a similar manner to BlindJ.\n");
    s.append("@assumptions It is assumed that:\n");
    s.append("@algorithm Takes user input parameters and runs BlindJ, ");
    s.append("outputting a expName#.mat file for each run number.");
    s.append("@param runnums The run numbers to use for naming the matrix ");
    s.append("@files.\n");
    s.append("@param expname The experiment name (i.e. \"quartz\").\n");
    s.append("@param peakspath The path where the peaks file is.\n");
    s.append("@param seqnum List of sequence numbers to use.\n");
    s.append("@return A Boolean indicating success or failure of the Form's ");
    s.append("execution.\n");
    s.append("@error Invalid run numbers.\n");
    s.append("@error Invalid sequence numbers.\n");
    s.append("@error Invalid experiment name.\n");
    return s.toString();
  }

  /**
   *  Returns the String command used for invoking this
   *  Form in a Script.
   */
  public String getCommand()
  {
    return "JBLINDFORM";
  }


 /**
   * getResult() takes the user input parameters and runs BlindJ, 
   * outputting a expName#.mat file for each run number.  In 
   * addition, it sends its output to a blind.log file.
   *
   *  @return A Boolean indicating success or failure.
   */
  public Object getResult()
  {
    SharedData.addmsg("Executing...\n");
    IParameterGUI param;
    String expName, outputDir, runNum, seqNums, matName, peaksName;
    Vector matNameArray;
    Object obj;
    
    int[] runsArray;

    matName = "";

    //gets the run numbers
    param = (IParameterGUI)super.getParameter(0);
    obj = param.getValue();

    if( (obj != null) && (obj.toString().length() > 0) )
    {
      runsArray = IntList.ToArray(obj.toString());
      param.setValid(true);
    }
    else
      return errorOut(param, 
        "ERROR: you must enter one or more valid run numbers.\n");

    //get experiment name
    param = (IParameterGUI)super.getParameter( 1 );
    obj = param.getValue();
    if( (obj != null) && (obj.toString().length() > 0) )
    {
      expName = obj.toString();
      param.setValid(true);
    }
    else
    return errorOut(param, 
        "ERROR: you must enter a valid experiment name.\n");
      
    //get output directory
    //should be no need to check this for validity
    param = (IParameterGUI)super.getParameter( 2 );
    outputDir = param.getValue().toString() + "/";
    param.setValid(true);

    //get the sequence numbers
    param = (IParameterGUI)super.getParameter( 3 );
    //String form for the BlindJ constructor.  
    obj = param.getValue();
    
    if( (obj != null) && (obj.toString().length() > 0) )
    {
      seqNums = obj.toString();
      param.setValid(true);
    }
    else
      return errorOut(param, 
        "ERROR: you must enter one or more valid sequence numbers.\n");

    //the peaks file
    peaksName = outputDir + "/" + expName + ".peaks"; 
    peaksName = StringUtil.setFileSeparator(peaksName);

    matNameArray = new Vector();
      
    for(int i = 0; i < runsArray.length; i++)
    {
      /*Get the run numbers. We don't want to remove the leading zeroes!*/
      runNum = DataSetTools
               .util
               .Format
               .integerPadWithZero(runsArray[i], RUN_NUMBER_WIDTH);

      //one of many saved matrix files...
      matName = outputDir + "/" + expName + runNum + ".mat";
      matName = StringUtil.setFileSeparator(matName);
      matNameArray.add(matName);

      SharedData.addmsg("BlindJ is creating " + matName + ".");
      
      //call BlindJ
      obj = new BlindJ(peaksName, seqNums, matName).getResult();

      if(obj instanceof ErrorString)
        return errorOut("BlindJ failed: " + obj.toString());
    }
  
    SharedData.addmsg("--- BlindJForm finished. ---");

    SharedData.addmsg("Matrix Files listed in: ");
    SharedData.addmsg(matNameArray.toString());

    //set the matrix file array 
    param = (IParameterGUI)super.getParameter(4);
    param.setValue(matNameArray);
    param.setValid(true);

    return new Boolean(true);
  }
}
