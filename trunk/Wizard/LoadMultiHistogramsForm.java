/*
 * File:  LoadMultiHistogramsForm.java
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
 *
 */

package Wizard;

import java.io.*;
import DataSetTools.wizard.*;
import DataSetTools.parameter.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.operator.Generic.Load.LoadOneHistogramDS;
import DataSetTools.operator.Generic.Load.LoadMonitorDS;
import DataSetTools.operator.Operator;
import java.util.Vector;
import  DataSetTools.instruments.InstrumentType;

/**
 *  This class defines a form for loading histograms from 
 *  multiple runfiles.  In addition to loading the user
 *  specified histogram, the corresponding monitor DataSet
 *  is also loaded.
 */
public class LoadMultiHistogramsForm extends Form
                              implements Serializable
{

  protected static int RUN_NUMBER_WIDTH = 5;

  /**
   *  Construct a LoadMultiHistogramsForm.
   *  
   */
  public LoadMultiHistogramsForm()
  {
    super("Open multiple histograms");
    this.setDefaultParameters();
  } 

  /**
   *  Subclass constructor.
   *  
   */
  protected LoadMultiHistogramsForm(String title)
  {
    super(title);
    this.setDefaultParameters();
  } 

  /**
   *
   *  Full constructor.  Uses the input parameters to create
   *  a LoadMultiHistogramsForm without the need to externally
   *  set the parameters.  It also sets the parameters needed 
   *  for the associated monitor DataSets.  getResult() may 
   *  be called immediately after using this constructor.
   *
   *  @param run_nums         List of integers representing
   *                          the runfile numbers which you
   *                          wish to load histograms from.
   *
   *  @param data_dir         The directory from which to load
   *                          the runfiles from.
   *
   *  @param inst_name        The instrument name (e.g. HRCS)
   *
   *  @param hist_num         The histogram number you wish
   *                          to load.
   *
   *  @param g_mask           The group mask to apply.  If
   *                          left blank, none will be applied.
   *
   *  @param histograms       The Vector which you wish to store
   *                          the loaded histograms in.
   */
  public LoadMultiHistogramsForm(String run_nums,     // 0
                                 String data_dir,     // 1
                                 String inst_name,    // 2
                                 int hist_num,        // 3
                                 int g_mask,          // 4
                                 Vector histograms)   // 5
  {
    this();
    getParameter(0).setValue(run_nums);
    getParameter(1).setValue(data_dir);
    getParameter(2).setValue(inst_name);
    getParameter(3).setValue(new Integer(hist_num));
    getParameter(4).setValue(new Integer(g_mask));
    getParameter(5).setValue(histograms);
    // monitor DataSets not setable <- <- <- <-
  }

  /**
   *
   *  Attempts to set reasonable default parameters for this form.
   *  Included in this is the setting of the monitor DataSet list
   *  corresponding to the respective runfiles, as well as the 
   *  corresponding type of the parameter (editable, result, or
   *  constant).
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter(new IntArrayPG("Run Numbers", "12358", false));
    addParameter(new DataDirPG("Location of runfiles", "", false));
    addParameter(new InstNamePG("Instrument Name", "GPPD", false));
    addParameter(new IntegerPG( "Histogram number", 1, false));
    addParameter(new IntArrayPG( "Group IDs to omit", "", false));
    addParameter(new ArrayPG( "Histogram List", new Vector(), false));
    addParameter(new ArrayPG( "Monitor Run List", new Vector(), false));
    setParamTypes(null,new int[]{0,1,2,3,4},new int[]{5,6});
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
    s.append("@overview This Form is designed for loading a histogram");
    s.append("(e.g. the first one)  from one or more runfiles, under ");
    s.append("the control of a Wizard.  It also loads the monitor ");
    s.append("DataSets corresponding to each runfile.\n");
    s.append("@assumptions It is assumed that the specified runfiles ");
    s.append("exist.  In addition, it is assumed that the specifed ");
    s.append("histogram exists.\n");
    s.append("@algorithm This Form opens each runfile and retrieves the ");
    s.append("specifed histogram from it.  The histograms are then stored ");
    s.append("in an ArrayPG.  The corresponding monitor DataSets are stored ");
    s.append("in a parallel ArrayPG.\n");
    s.append("@param run_nums Vector of integers representing the runfile ");
    s.append("numbers which you wish to load histograms from.\n");
    s.append("@param data_dir The directory from which to load the runfiles ");
    s.append("from.\n");
    s.append("@param inst_name The instrument name (e.g. HRCS).\n");
    s.append("@param hist_num The histogram number you wish to load.\n");
    s.append("@param g_mask The group mask to apply.  If left blank, none ");
    s.append("will be applied.\n");
    s.append("@param histograms The Vector which you wish to store the loaded ");
    s.append("histograms in.\n");
    s.append("@return Presently, returns a Boolean which indicates either ");
    s.append("success or failure.\n");
    s.append("@error Returns a Boolean false if any of the specified run ");
    s.append("numbers do not exist.\n");
    s.append("@error Returns a Boolean false if the instrument name is not ");
    s.append("valid.\n");
    s.append("@error Returns a Boolean false if the histogram does not ");
    s.append("in any one of the runfiles.\n");
    return s.toString();
  }

  /**
   *  Returns the String command used for invoking this
   *  Form in a Script.
   */
  public String getCommand()
  {
    return "LOADMULTIHISTFORM";
  }


  /**
   *  Loads the specifed runfile's DataSets into an ArrayPG.  Each
   *  runfile's DataSet array occupies a space in the ArrayPG's
   *  Vector. 
   *
   *  @return A Boolean indicating success or failure.
   */
  public Object getResult()
  {
    SharedData.addmsg("Executing... " + super.getTitle());
    IParameterGUI param;
    ArrayPG histograms, monitors;
    int run_numbers[], h_num;
    String run_dir, inst_name, file_name, g_mask, run_num;
    Operator load, mon;
    Object result, obj, mon_res;
    DataSet result_ds;

    //gets the run numbers
    param = (IParameterGUI)super.getParameter(0);
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        run_numbers = IntList.ToArray(obj.toString());
        param.setValid(true);
    }
    else
      return errorOut(param, 
         "You must enter one or more valid run numbers.");

    //get directory
    param = (IParameterGUI)super.getParameter( 1 );
    run_dir = StringUtil.setFileSeparator(
                param.getValue().toString() + "/");
    if(new File(run_dir).exists())
      param.setValid(true);
    else
      return errorOut(param,
          "You must enter a valid run number directory.");

    //get instrument name
    param = (IParameterGUI)super.getParameter( 2 );
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        inst_name = obj.toString();
        //what instrument names should this check for?
        param.setValid(true);
    }
    else
      return errorOut(param, 
         "You must enter a valid instrument name.");

    //get histogram number
    param = (IParameterGUI)super.getParameter( 3 );
    obj = param.getValue();
    if( obj != null && obj instanceof Integer )
    {
        h_num = ((Integer)obj).intValue();
        param.setValid(true);
    }
    else
      return errorOut(param, 
         "You must enter a valid histogram number.");

    //get group mask
    //how should I validate this?
    param = (IParameterGUI)super.getParameter( 4 );
    obj = param.getValue();
    if( obj != null  )
    {
        g_mask = obj.toString();
        param.setValid(true);
    }
    else
      return errorOut(param, 
         "You must enter a valid group mask.");

    //get the DataSet array
    histograms = (ArrayPG)super.getParameter( 5 );
    monitors = (ArrayPG)super.getParameter( 6 );
    //clear it out when the form is re-run
    histograms.clearValue();
    monitors.clearValue();

    super.getResult();

    //set the increment amount
    increment = (1.0f / run_numbers.length) * 100.0f;

    for( int i = 0; i < run_numbers.length; i++ )
    {
      file_name = run_dir + InstrumentType.formIPNSFileName(inst_name,
                    run_numbers[i]);
      load = new LoadOneHistogramDS(file_name, h_num, g_mask);
      mon = new LoadMonitorDS(file_name);
      result = load.getResult();
      mon_res = mon.getResult();

      if( result instanceof DataSet && mon_res instanceof DataSet)
      {
        //add the DataSet and its monitor
        result_ds = (DataSet)result;
        histograms.addItem(result_ds);
        result_ds = (DataSet)mon_res;
        monitors.addItem(result_ds);
        //let the user know the DataSet was added successfully
        SharedData.addmsg(
          result + " and " + mon_res + " added successfully.");
      }
      else // something went wrong
      {
        String errMessage = null;
        if( result instanceof ErrorString || mon_res instanceof ErrorString)
          errMessage = result.toString();
        else
            errMessage = 
              "Could not load histogram and/or monitor from " + file_name;
        return errorOut(errMessage); 
      }

      //fire a property change event off to any listeners
      oldPercent = newPercent;
      newPercent += increment;
      super.fireValueChangeEvent((int)oldPercent, (int)newPercent);
    }//for
    histograms.setValid(true);
    monitors.setValid(true);

    SharedData.addmsg("Finished loading DataSets from runfiles.\n");

    return Boolean.TRUE;
  }

}//class
