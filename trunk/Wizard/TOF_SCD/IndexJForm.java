/*
 * File:  IndexJForm.java   
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
 * Revision 1.3  2003/06/06 15:12:01  bouzekc
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
 *  This is a Form to add extra functionality to IndexJ.  It "knows" 
 *  which runs to restrict for each matrix file.
 *  Other than that, it functions in a similar manner to IndexJ.
 */
public class IndexJForm extends Form
{

  protected static int RUN_NUMBER_WIDTH = 5;
  private static Vector choices;

  //use these constants to set the choice list value
  public static final String JBLIND = "JBlind";
  public static final String JLSQRS = "JLeastSquares";

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
  /**
   * Construct a Form with a default parameter list.
   */
  public IndexJForm()
  {
    super( "IndexJForm" );
    this.setDefaultParameters();
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Full constructor for IndexJForm.
   *
   *  @param runnums        The run numbers to use for naming the
   *                        matrix files.
   *  @param peaksPath      The path where the peaks file is located.
   *  @param delta          Error parameter for indexing peaks.
   *  @param update         Whether to update the peaks file.
   *  @param matType        Whether the matrix files are from BlindJ
   *                        or LsqrsJ.
   *  @param expName        The experiment name.
   */
  
  public IndexJForm(String runnums, String peaksPath, 
                    float delta, boolean update, int matType,
                    String expName)
  {
    this();
    getParameter(0).setValue(runnums);
    getParameter(1).setValue(peaksPath);
    getParameter(2).setValue(new Float(delta));
    getParameter(3).setValue(new Boolean(update));
    getParameter(4).setValue(expName);
    getParameter(5).setValue(choices.elementAt(matType));
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
    addParameter(new IntArrayPG("Run Numbers",null));
    //1
    addParameter(new DataDirPG("Peaks File Path",null));
    //2
    addParameter(new FloatPG("Delta",0.05f));
    //3
    addParameter(new BooleanPG("Update Peaks File",true));
    //4
    addParameter(new StringPG("Experiment Name",null));
    //5
    ChoiceListPG clpg=new ChoiceListPG("Matrix File Input From", 
                                       choices.elementAt(0));
    clpg.addItems(choices);
    addParameter(clpg);

    /*I am not entirely sure that the choice list should be uneditable
      when the Form's constants are turned on.  Change it if you 
      need to.*/
    if(HAS_CONSTANTS)
      setParamTypes(new int[]{0,1,4},new int[]{2,3,5}, null);
    else
      setParamTypes(null, new int[]{0,1,2,3,4,5}, null);
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
    s.append("IndexJ.  It uses multiple expName#.matrix files when ");
    s.append("getResult() is called.  In addition, it \"knows\" which ");
    s.append("runs to restrict for each matrix file.  Other than that, ");
    s.append("it functions in a similar manner to IndexJ.\n");
    s.append("@assumptions It is assumed that:\n");
    s.append("The matrix files have the format \"<experiment name><run number>");
    s.append(".mat\" or \"ls<experiment name><run number>.mat\".");
    s.append("getResult() relies on this.\n");
    s.append("In addition, it is assumed that the peaks file and the matrix ");
    s.append("files are in the same directory.\n");
    s.append("@algorithm Using the given run numbers and matrix files, ");
    s.append("this Form calls IndexJ, giving it the appropriate matrix file ");
    s.append("for each run number in the peaks file.\n");
    s.append("@param runnums The run numbers to use for naming the matrix ");
    s.append("files.\n");
    s.append("@param peaksPath The path where the peaks file is located.\n");
    s.append("@param delta Error parameter for indexing peaks.\n");
    s.append("@param update Whether to update the peaks file.\n");
    s.append("@param matType Whether the matrix files are from BlindJ or ");
    s.append("LsqrsJ.\n");
    s.append("@param expName The experiment name.\n");
    s.append("@return A Boolean indicating success or failure of the Form's ");
    s.append("execution.\n");
    s.append("@error No valid run numbers are entered.\n");
    s.append("@error No valid experiment name is entered.\n");
    s.append("@error No valid input path is entered.\n");
    return s.toString();
  }

  /**
   *  Returns the String command used for invoking this
   *  Form in a Script.
   */
  public String getCommand()
  {
    return "JINDEXFORM";
  }


 /**
   * getResult() takes the user input parameters and runs IndexJ, 
   * using a expName#.mat file for each run number.  In 
   * addition, it sends its output to a index.log file.
   * Note that it "knows" which runs to restrict for each matrix file,
   * based on the matrix file names.
   *
   *  @return A Boolean indicating success or failure.
   */
  public Object getResult()
  {
    SharedData.addmsg("Calculating h, k, and l values for each run...");
    IParameterGUI param;
    String peaksDir, expName, peaksName, runNum, matInputPath, matrixFrom, 
           matName;
    Object obj;
    float delta;
    boolean update;
    int[] runsArray;

    //gets the run numbers
    param = (IParameterGUI)super.getParameter(0);
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        runsArray = IntList.ToArray(obj.toString());
        param.setValid(true);
    }
   else
     return errorOut(param,
       "ERROR: you must enter one or more valid run numbers.\n");

    //gets the input path
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

    //gets the delta
    param = (IParameterGUI)super.getParameter(2);
    obj = param.getValue();

    if( (obj != null) && (obj instanceof Float) )
    {
        delta = ((Float)obj).floatValue();
        param.setValid(true);
    }
    else
      return errorOut(param, 
        "ERROR: you must enter a valid delta value.");

    //gets the update value 
    param = (IParameterGUI)super.getParameter(3);
    obj = param.getValue();

    if( (obj != null) && (obj instanceof Boolean) )
    {
        update = ((Boolean)obj).booleanValue();
        param.setValid(true);
    }
    else
      return errorOut(param, 
        "ERROR: you must enter a valid update value.");

    //gets the experiment name
    param = (IParameterGUI)super.getParameter(4);
    obj = param.getValue();

    if( (obj != null) && (obj.toString().length() > 0) )
    {
        expName = obj.toString();
        param.setValid(true);
    }
    else
      return errorOut(param, 
        "ERROR: you must enter a valid experiment name.");

    //get the currently selected choice list value
    param = (IParameterGUI)super.getParameter(5);
    obj = param.getValue();

    if(obj == JBLIND)
      matrixFrom = expName;
    else
      matrixFrom = "ls" + expName;

    peaksName = peaksDir + "/" + expName + ".peaks";
    //make sure it is right for the system
    peaksName = StringUtil.setFileSeparator(peaksName);

    for(int i = 0; i < runsArray.length; i++)
    {
      /*load the run numbers.  We don't want to remove the leading zeroes!*/
      runNum = DataSetTools
               .util
               .Format
               .integerPadWithZero(runsArray[i], RUN_NUMBER_WIDTH);
               
      //the name of the matrix file
      matName = peaksDir + "/" + matrixFrom + runNum + ".mat";
      //make sure it is right for the system
      matName = StringUtil.setFileSeparator(matName);

      SharedData.addmsg("IndexJ is updating " + peaksName + " with " + matName);

      //call IndexJ
      obj = new IndexJ(peaksName, matName, delta, update);
      //synchronize the run number in the peaks and matrix file
      ((Operator)obj).getParameter(2).setValue(runNum);

      obj = ((Operator)obj).getResult();

      if(obj instanceof ErrorString)
        return errorOut("IndexJ failed: " + obj.toString());
    }
  
    SharedData.addmsg("--- IndexJForm finished. ---");

    return new Boolean(true);
  }

  /**
   * Set up a vector for use inside the ChoiceListPG.
   */
  private void init_choices(){
    choices=new Vector(2);
    choices.add(JBLIND);
    choices.add(JLSQRS);
  }
}
