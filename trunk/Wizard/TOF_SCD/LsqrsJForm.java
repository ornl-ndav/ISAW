/*
 * File:  LsqrsJForm.java   
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
 * Revision 1.3  2003/06/06 15:12:03  bouzekc
 * Added log message header to file.
 *
 */

package Wizard.TOF_SCD;

import  java.util.Vector;
import  DataSetTools.util.*;
import  DataSetTools.parameter.*;
import  DataSetTools.wizard.*;
import  Operators.TOF_SCD.*;
import  DataSetTools.operator.Operator;

/**
 * 
 *  This is a Form to add extra functionality to LsqrsJ.  It outputs 
 *  multiple ls#.mat files, where # corresponds to a run number.
 *  Other than that, it functions in a similar manner to LsqrsJ.
 */
public class LsqrsJForm extends Form
{

  protected static int RUN_NUMBER_WIDTH = 5;
  private static final String  identmat = "[[1,0,0][0,1,0][0,0,1]]";

  /* This one is used to determine whether or not to use the identity matrix.
     It can be externally set using setFirstTime(boolean) in this class. */
  private boolean firstTime = true; 

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
  /**
   * Construct a Form with a default parameter list.
   */
  public LsqrsJForm()
  {
    super( "LsqrsJForm" );
    this.setDefaultParameters();
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Full constructor for LsqrsJForm.
   *
   *  @param runnums        The run numbers to use for naming the
   *                        matrix files.
   *  @param peaksPath      The path where the peaks file is.
   *  @param expName        The experiment name.
   *  @param restrictSeq    The sequence numbers to restrict.
   *  @param transform      The transformation matrix to apply.
   */
  
  public LsqrsJForm(String runNums, String peaksPath, String expName,
                    String restrictSeq, String transform)
  {
    this();
    getParameter(0).setValue(runNums);
    getParameter(1).setValue(peaksPath);
    getParameter(2).setValue(expName);
    getParameter(3).setValue(restrictSeq);
    getParameter(4).setValue(transform);
  }

  /**
   *
   *  Attempts to set reasonable default parameters for this form.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();

    //0
    addParameter(new IntArrayPG("Run Numbers",null));
    //1
    addParameter(new DataDirPG( "Peaks File Path", null));
    //2
    addParameter(new StringPG( "Experiment Name", null));
    //3
    addParameter(new IntArrayPG("Restrict Sequence Numbers (blank for all)",null));
    //4
    addParameter(new StringPG("Transform Matrix",identmat));
    //5
    addParameter(new ArrayPG("Matrix Files", new Vector()));

    /*a little convoluted, but necessary to set it up for the identity
     matrix only version of this Form.*/
    if(HAS_CONSTANTS)
      if(firstTime)
        setParamTypes(new int[]{0,1,2},new int[]{3,4}, new int[]{5});
      else
        setParamTypes(new int[]{0,1,2,4},new int[]{3}, new int[]{5});
        
    else  //standalone or first time form
      if(firstTime)
        setParamTypes(null, new int[]{0,1,2,3,4}, new int[]{5});
      else
        setParamTypes(new int[]{4}, new int[]{0,1,2,3}, new int[]{5});

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
    s.append("LsqrsJ.  It use the given *.peaks file and the transformation ");
    s.append("matrix for calculation.  In addition, it ");
    s.append("\"knows\" which runs to restrict for each matrix file.  ");
    s.append("It outputs a ls#.mat file for each run.");
    s.append("Other than that, it functions in a similar manner to ");
    s.append("LsqrsJ.\n");
    s.append("@assumptions ");
    s.append("@algorithm Using the given run numbers, transformation matrix, ");
    s.append("and peaks file, this Form calls LsqrsJ, creating the appropriate ");
    s.append("ls#.mat file for each run number in the peaks file.\n");
    s.append("The user is given a choice as to what transformation matrix to ");
    s.append("use for each run number (NOT IMPLEMENTED YET).\n");
    s.append("@param runnums The run numbers to use for naming the matrix ");
    s.append("files.\n");
    s.append("@param peaksPath The path where the peaks file is.\n");
    s.append("@param expName The experiment name.\n");
    s.append("@param restrictSeq The sequence numbers to restrict.\n");
    s.append("@param transform The transformation matrix to apply.\n");
    s.append("@return A Boolean indicating success or failure of the Form's ");
    s.append("execution.\n");
    s.append("@error Invalid peaks path.\n");
    s.append("@error Invalid experiment name.\n");
    s.append("@error Invalid transformation matrix.\n");
    return s.toString();
  }

  /**
   *  Returns the String command used for invoking this
   *  Form in a Script.
   */
  public String getCommand()
  {
    return "JLSQRSFORM";
  }


 /**
   * getResult() takes the user input parameters and runs LsqrsJ, 
   * using the given *.peaks file.  It pops up a dialog box so that the 
   * user can select transformation matrices for each run number.
   * (DIALOG BOX NOT IMPLEMENTED YET).
   *
   *  @return A Boolean indicating success or failure.
   */
  public Object getResult()
  {
    SharedData.addmsg("Executing...");
    IParameterGUI param;
    String runNum, peaksDir, xFormMat, restrictSeq, matFileName, expName,
           peaksName;
    Vector matNamesVec = new Vector(20,4);
    Object obj;
    int[] runsArray;
    LsqrsJ leastSquares;

    //gets the run numbers
    param = (IParameterGUI)getParameter(0);
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        runsArray = IntList.ToArray(obj.toString());
        param.setValid(true);
    }
   else
     return errorOut(param,
       "ERROR: you must enter one or more valid run numbers.\n");

    //get input file directory
    param = (IParameterGUI)super.getParameter(1);
    obj = param.getValue();
    if( (obj != null) && (obj.toString().length() > 0) )
    {
        peaksDir = obj.toString();
        param.setValid(true);
    }
    else
      return errorOut(param, 
        "ERROR: you must enter a valid input directory.");

    //gets the experiment name
    param = (IParameterGUI)super.getParameter(2);
    obj = param.getValue();

    if( (obj != null) && (obj.toString().length() > 0) )
    {
        expName = obj.toString();
        param.setValid(true);
    }
    else
      return errorOut(param, 
        "ERROR: you must enter a valid experiment name.");

    /*get restricted sequence numbers - leave in String form
      for LsqrsJ*/
    param = (IParameterGUI)getParameter(3);
    obj = param.getValue();
    if( obj != null )
    {
        restrictSeq = obj.toString();
        param.setValid(true);
    }
    else
    return errorOut(param, 
        "ERROR: you must enter valid sequence numbers to restrict.");

    /*get transformation matrix - only set to identity if
      not first time through.  Otherwise, use user input
      value.*/

    if(firstTime)
    {
      param = (IParameterGUI)getParameter(4);
      obj = param.getValue();
      if( (obj != null) && (obj.toString().length() > 0) )
      {
        xFormMat = obj.toString();
        param.setValid(true);
      }
      else
      return errorOut(param, 
        "ERROR: you must enter a valid transformation matrix.");
    }
    else
      xFormMat = identmat;

    //peaks file
    peaksName = peaksDir + "/" + expName + ".peaks";
    peaksName = StringUtil.setFileSeparator(peaksName);

    for(int i = 0; i < runsArray.length; i++)
    {
      /*Get the run number. We don't want to remove the leading zeroes!*/
      runNum = DataSetTools
               .util
               .Format
               .integerPadWithZero(runsArray[i], RUN_NUMBER_WIDTH);

      matFileName = peaksDir + "/ls" + expName + runNum + ".mat";
      matFileName = StringUtil.setFileSeparator(matFileName);
      matNamesVec.add(matFileName);

      SharedData.addmsg("LsqrsJ is creating " + matFileName + " for " + peaksName);

      //call LsqrsJ
      leastSquares = new LsqrsJ();
      leastSquares.getParameter(0).setValue(peaksName);
      leastSquares.getParameter(1).setValue(runNum);
      leastSquares.getParameter(2).setValue(restrictSeq);
      leastSquares.getParameter(3).setValue(xFormMat);
      leastSquares.getParameter(4).setValue(matFileName);

      obj = leastSquares.getResult();

      if(obj instanceof ErrorString)
        return errorOut("LsqrsJ failed: " + obj.toString());
    }
  
    SharedData.addmsg("--- LsqrsJForm finished. ---");

    return new Boolean(true);
  }

  /**
   *  Used to set the firstTime class variable so that the 
   *  LsqrsJForm does not try to apply the transformation matrix 
   *  twice.
   */
  public void setFirstTime(boolean first)
  {
    this.firstTime = first;
  }
}
