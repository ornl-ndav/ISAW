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
 * Revision 1.10  2003/06/18 19:58:23  bouzekc
 * Uses super.getResult() for initializing PropertyChanger
 * variables.  Now fires off property change events in a
 * semi-intelligent manner.
 *
 * Revision 1.9  2003/06/17 20:37:56  bouzekc
 * Fixed setDefaultParameters so all parameters have a
 * visible checkbox.  Added more robust error checking on
 * the raw and output directory parameters.  Fixed matrix
 * files parameter setting bug.
 *
 * Revision 1.8  2003/06/11 23:04:08  bouzekc
 * No longer uses StringUtil.setFileSeparator as DataDirPG
 * now takes care of this.
 *
 * Revision 1.7  2003/06/11 22:48:42  bouzekc
 * Added parameters so that the identity matrix is used for the
 * iteration step.  Added parameters so that the user can view
 * the scalar log file.  Updated documentation.  Moved calls
 * to setFileSeparator out of the loop.
 *
 * Revision 1.6  2003/06/10 21:56:08  bouzekc
 * Fixed problem where the matrix file name Vector was not
 * being set as a parameter.
 *
 * Revision 1.5  2003/06/10 20:31:41  bouzekc
 * Moved creation of lsqrsJ out of the for loop to avoid
 * excessive Object creation.  Now also outputs an overall
 * orientation matrix for the entire set of runs.
 *
 * Revision 1.4  2003/06/09 21:56:05  bouzekc
 * Updated documentation.
 * Added constructor to set HAS_CONSTANTS to reduce
 * the number of calls to setDefaultParameters().
 * Updated parameter names.
 * Removed code for the "firstTime" variable from
 * setDefaultParameters - no longer needed.
 * Overrode makeGUI() to set the transformation matrix
 * back to identity matrix if LsqrsJ is run more than once.
 *
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
import  java.io.File;

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

  private boolean useIdentity = false;
  private BooleanPG useIdentCheckBox;

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
  /**
   * Construct a Form with a default parameter list.
   */
  public LsqrsJForm()
  {
    super( "LsqrsJForm" );
    this.setDefaultParameters();
  }

  /**
   *  Construct a Form using the default parameter list.
   *
   *  @param hasConstParams         boolean indicating whether
   *                                this Form should have constant
   *                                parameters.
   */
  public LsqrsJForm(boolean hasConstParams)
  {
    super( "LsqrsJForm", hasConstParams );
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
    addParameter(new IntArrayPG("Run Numbers",null, false));
    //1
    addParameter(new DataDirPG( "Peaks File Path", null, false));
    //2
    addParameter(new StringPG( "Experiment Name", null, false));
    //3
    addParameter(new IntArrayPG(
                   "Restrict Peaks Sequence Numbers (blank for all)", 
                    null, false));
    //4
    addParameter(new BooleanPG("Use Identity Matrix for Iteration?", false, 
                               false));
    //5
    addParameter(new StringPG("Transform Matrix",identmat, false));
    //6 - parameter added solely so user can view scalar file and is optional.
    //As such, it is ALWAYS valid
    addParameter(new LoadFilePG("Scalar Log", null, true));
    //7
    addParameter(new ArrayPG("Matrix Files", new Vector(), false));

    if(HAS_CONSTANTS)
      setParamTypes(new int[]{0,1,2},new int[]{3,4,5,6}, new int[]{7});
    else  //standalone or first time form
      setParamTypes(null, new int[]{0,1,2,3,4,5,6}, new int[]{7});

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
    s.append("It outputs a ls#expName.mat file for each run, and an ");
    s.append("lsexpName.mat file which is produced for ALL runs.");
    s.append("Other than that, it functions in a similar manner to ");
    s.append("LsqrsJ.\n");
    s.append("NOTE: although it resets the transformation matrix to the ");
    s.append("identity after the first iteration, the matrix can be reset ");
    s.append("at any time if you want to change it again when executing.\n");
    s.append("NOTE: the transformation matrix is initially set to the ");
    s.append("identity matrix.\n");
    s.append("@assumptions The peaks file exists and the transformation ");
    s.append("matrix is valid.");
    s.append("@algorithm Using the given run numbers, transformation matrix, ");
    s.append("and peaks file, this Form calls LsqrsJ, creating the appropriate ");
    s.append("lsexpName#.mat file for each run number in the peaks file, as well ");
    s.append("as an overall lsexpName.mat file.\n");
    s.append("The user is given a choice as to what transformation matrix to ");
    s.append("use for each run number (NOT IMPLEMENTED YET).\n");
    s.append("@param runnums The run numbers to use for naming the matrix ");
    s.append("files.\n");
    s.append("@param peaksPath The path where the peaks file is.\n");
    s.append("@param expName The experiment name.\n");
    s.append("@param restrictSeq The sequence numbers to restrict.\n");
    s.append("@param useIdentity Whether or not LsqrsJ should use the  ");
    s.append("identity matrix (i.e. if this is set to true, then the ");
    s.append("transformation matrix is always the identity matrix.\n");
    s.append("@param transform The transformation matrix to apply.\n");
    s.append("@param scalarLog The scalar log file.  Unused by LsqrsJ, it ");
    s.append("is included for convenience.\n");
    s.append("@param matrixFiles The Vector of LsqrsJ output matrix files.\n");
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
    peaksDir = param.getValue().toString();
    if(new File(peaksDir).exists())
      param.setValid(true);
    else
      param.setValid(false);

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

    //get "use identity" value - this is a class value, and is actually
    //retrieved using setIdentityParameter below
    setIdentityParameter(); 

    /*get transformation matrix - only set to identity if
      not first time through.  Otherwise, use user input
      value.*/

    if(useIdentity)
      xFormMat = identmat;
    else
    {
      param = (IParameterGUI)getParameter(5);
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

    //the scalar log file - ALWAYS valid
    param = (IParameterGUI)getParameter(6);
    param.setValid(true);


    //peaks file
    peaksName = peaksDir + expName + ".peaks";

   //call LsqrsJ - this is the same every time, so keep it out of the loop
   leastSquares = new LsqrsJ();
   leastSquares.getParameter(0).setValue(peaksName);
   leastSquares.getParameter(2).setValue(restrictSeq);
   leastSquares.getParameter(3).setValue(xFormMat);

    //validate the parameters and set the progress bar variables
    super.getResult();

    //set the increment amount
    increment = (1.0f / runsArray.length) * 100.0f;

    for(int i = 0; i < runsArray.length; i++)
    {
      runNum = DataSetTools
               .util
               .Format
               .integerPadWithZero(runsArray[i], RUN_NUMBER_WIDTH);

      matFileName = peaksDir + "ls" + expName + runNum + ".mat";
      matNamesVec.add(matFileName);

      SharedData.addmsg("LsqrsJ is creating " + matFileName + " for " + peaksName);

      leastSquares.getParameter(1).setValue(runNum);
      leastSquares.getParameter(4).setValue(matFileName);

      obj = leastSquares.getResult();

      if(obj instanceof ErrorString)
        return errorOut("LsqrsJ failed: " + obj.toString());

      //fire a property change event off to any listeners
      oldPercent = newPercent;
      newPercent += increment;
      super.fireValueChangeEvent((int)oldPercent, (int)newPercent);
    }

    //now put out an orientation matrix for all of the runs.
    matFileName = peaksDir + "ls" + expName + ".mat";
    matNamesVec.add(matFileName);
    leastSquares.getParameter(1).setValue("");
    leastSquares.getParameter(4).setValue(matFileName);
    obj = leastSquares.getResult();

    if(obj instanceof ErrorString)
      return errorOut("LsqrsJ failed: " + obj.toString());

    //set the matrix file name vector parameter
    param = (IParameterGUI)getParameter(7);
    param.setValue(matNamesVec);
    param.setValid(true);
  
    SharedData.addmsg("--- LsqrsJForm finished. ---");

    return new Boolean(true);
  }

  /**
   *  Overridden so that the identity matrix can be used on any iterations 
   *  of Lsqrs.  
   */
  protected void makeGUI()
  {
    IParameterGUI param;
    //the scalar log file - ALWAYS valid
    param = (IParameterGUI)getParameter(6);
      param.setValid(true);
    if(useIdentCheckBox == null)
      setIdentityParameter();
    //after the first time through, we don't want to change the
    //identity matrix
    if(!useIdentity)
    {
      useIdentCheckBox.setValue(Boolean.FALSE);
      //alternate so that the user won't accidentally apply the matrix twice.
      //This will not interfere with getResult(), since it gets the checkbox
      //value anyways
      useIdentity = true;
    }
    else
    {
      useIdentCheckBox.setValue(Boolean.TRUE);
      //reset the matrix to identity
      ((IParameterGUI)getParameter(5)).setValue(identmat);
    }

    super.makeGUI();  //now make the GUI
  }

  /**
   *  Gets the true/false value from the identity check box on the GUI
   *  and sets the useIdentity variable to that value.
   */
  private void setIdentityParameter()
  {
    if(useIdentCheckBox == null)
      useIdentCheckBox = (BooleanPG)super.getParameter( 4 );
    //this one doesn't need to be checked for validity
    useIdentCheckBox.setValid(true);
    useIdentity = useIdentCheckBox.getbooleanValue();
  }
}
