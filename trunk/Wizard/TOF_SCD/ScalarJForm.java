/*
 * File:  ScalarJForm.java   
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
 * Revision 1.3  2003/06/06 15:12:05  bouzekc
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
 *  This is a Form to add extra functionality to ScalarJ.  It outputs an
 *  sclrexpname#.mat matrix file for each run number (#) that it is given.
 *  Other than that, it functions in a similar manner to ScalarJ.
 */
public class ScalarJForm extends Form
{

  protected static int RUN_NUMBER_WIDTH = 5;
  private static Vector choices;

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
  /**
   * Construct a Form with a default parameter list.
   */
  public ScalarJForm()
  {
    super( "ScalarJForm" );
    this.setDefaultParameters();
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Full constructor for ScalarJForm.
   *
   *  @param runnums        The run numbers used for creating the matrix files.
   *  @param peaksPath      The path where the peaks file is.
   *  @param expName        The experiment name.
   *  @param delta          Error parameter for finding higher symmetry.
   *  @param ctype          Number for the type of search to do.
   */
  
  public ScalarJForm(String runnums, String peaksPath, String expName, 
                    float delta, int ctype)
  {
    this();
    getParameter(0).setValue(runnums);
    getParameter(1).setValue(peaksPath);
    getParameter(2).setValue(expName);
    getParameter(3).setValue(new Float(delta));
    getParameter(4).setValue(choices.elementAt(ctype));
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
    addParameter(new DataDirPG( "Peaks File Path", null));
    //2
    addParameter(new StringPG( "Experiment Name", null));
    //3
    addParameter(new FloatPG("Delta",0.05f));
    //4
    ChoiceListPG clpg=new ChoiceListPG("Search Method", choices.elementAt(0));
    clpg.addItems(choices);
    addParameter(clpg);
    //5
    addParameter(new ArrayPG("Scalar Log Files", new Vector()));

    if(HAS_CONSTANTS)
      setParamTypes(new int[]{0,1,2},new int[]{3,4}, new int[]{5});
    else
      setParamTypes(null, new int[]{0,1,2,3,4}, new int[]{5});
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
    s.append("ScalarJ.  It uses multiple *.mat files when ");
    s.append("getResult() is called.  In addition, it \"knows\" which ");
    s.append("runs to restrict for each matrix file.  Other than that, ");
    s.append("it functions in a similar manner to ScalarJ.\n");
    s.append("@assumptions It is assumed that:\n");
    s.append("The matrix files have the format \"<experiment name><run number>");
    s.append(".mat\" or \"ls<run number>.mat\".  getResult() relies on this.\n");
    s.append("@algorithm Using the given parameters, this Form calls IndexJ, ");
    s.append("giving it the appropriate matrix file for each run number in the ");
    s.append("peaks file.\n");
    s.append("@param runnums The run numbers used for creating the matrix files.\n");
    s.append("@param peaksPath The path where the peaks file is.\n");
    s.append("@param expName The experiment name.\n");
    s.append("@param delta Error parameter for finding higher symmetry.\n");
    s.append("@param ctype Number for the type of search to do.\n");
    s.append("@return A Boolean indicating success or failure of the Form's ");
    s.append("execution.\n");
    s.append("@error No valid run numbers are entered.\n");
    s.append("@error A valid path for the peaks file is not entered.\n");
    return s.toString();
  }

  /**
   *  Returns the String command used for invoking this
   *  Form in a Script.
   */
  public String getCommand()
  {
    return "JSCALARFORM";
  }


 /**
   * Using the given parameters, this Form calls IndexJ, giving it the 
   * appropriate matrix file for each run number in the peaks file.
   *
   *  @return A Boolean indicating success or failure.
   */
  public Object getResult()
  {
    SharedData.addmsg("Executing...");
    IParameterGUI param;
    String runNum, peaksDir, matName, expName;
    Vector scalLogVec = new Vector();
    Object obj;
    float delta;
    int[] runsArray;
    int searchType;

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

    //gets the delta
    param = (IParameterGUI)super.getParameter(3);
    obj = param.getValue();

    if( (obj != null) && (obj instanceof Float) )
    {
        delta = ((Float)obj).floatValue();
        param.setValid(true);
    }
    else
      return errorOut(param, 
        "ERROR: you must enter a valid delta value.");

    //gets the search type - put into integer form for ScalarJ
    param = (IParameterGUI)super.getParameter(4);
    obj = param.getValue();

    if( (obj != null) )
    {
        searchType = ((ChooserPG)param).getIndex(obj);
        param.setValid(true);
    }
    else
      return errorOut(param, 
        "ERROR: you must enter a valid symmetry search type.");

    for(int i = 0; i < runsArray.length; i++)
    {
      /*Load the run numbers.  We don't want to remove the leading zeroes!*/
      runNum = DataSetTools
               .util
               .Format
               .integerPadWithZero(runsArray[i], RUN_NUMBER_WIDTH);

      matName = peaksDir + "/" + expName + runNum + ".mat";
      matName = StringUtil.setFileSeparator(matName);

      SharedData.addmsg("ScalarJ is creating scalar.log for " + matName);
      
      //call ScalarJ
      obj = new ScalarJ(matName, delta, searchType).getResult();

      if(obj instanceof ErrorString)
        return errorOut("ScalarJ failed: " + obj.toString());
      else
        scalLogVec.add("scalar" + runNum + ".log");
    }
  
    SharedData.addmsg("--- ScalarJForm finished. ---");

    //set the ScalarJ log file Vector parameter
    param = (IParameterGUI)super.getParameter(5);
    param.setValue(scalLogVec);
    param.setValid(true);

    return new Boolean(true);
  }

  /**
   * Set up a vector for use inside the ChoiceListPG.
   */
  private void init_choices(){
    choices=new Vector(20);
    choices.add("No Restriction");
    choices.add("Highest Symmetry");
    choices.add("P - Cubic");
    choices.add("F - Cubic");
    choices.add("R - Hexagonal");
    choices.add("I - Cubic");
    choices.add("I - Tetragonal");
    choices.add("I - Orthorombic");
    choices.add("P - Tetragonal");
    choices.add("P - Hexagonal");
    choices.add("C - Orthorombic");
    choices.add("C - Monoclinic");
    choices.add("F - Orthorombic");
    choices.add("P - Orthorombic");
    choices.add("P - Monoclinic");
    choices.add("P - Triclinic");
    choices.add("R11 == R22 == R33");
    choices.add("R11 == R22 != R33");
    choices.add("R11 == R33 != R22");
    choices.add("R11 != R22 != R33");
  }
}
