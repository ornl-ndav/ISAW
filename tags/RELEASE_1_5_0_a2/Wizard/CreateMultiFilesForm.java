/*
 * File:  CreateMultiFilesForm.java
 *
 * Copyright (C) 2003, Christopher Bouzek
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
 * This work was supported by the National Science Foundation.
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

/**
 *  This class defines a form for time focusing spectra in
 *  a DataSet under the control of a Wizard.
 */
public class CreateMultiFilesForm extends    Form
                              implements Serializable
{
  /**
   *  Construct an CreateMultiFilesForm to open the files listed in
   *  operands[] and store it in result[].  This constructor basically
   *  just calls the super class constructor and builds an appropriate
   *  help message for the form.
   *
   *  @param  operands  The list of names of parameters to be loaded.
   *  @param  result    The list of names of parameters for the loaded
   *                    DataSets.
   *  @param  w         The wizard controlling this form.
   */
  public CreateMultiFilesForm( String operands[], String result[], Wizard w )
  {
    super("Open multiple files", null, operands, result, w );

    String help = "This form lets you open multiple run files \n";
    setHelpMessage( help );

  }

  /**
   *  Loads the specifed runfile's DataSets into an ArrayPG.  Each
   *  runfile's DataSet array occupies a space in the ArrayPG's
   *  Vector.
   *
   *  @return true if all of the parameters are valid and all Datasets
   *  can be loaded; false if any significant error occurs
   */
  public boolean execute()
  {
    SharedData.addmsg("Executing...\n");
    IParameterGUI param;
    ArrayPG histograms, monitors;
    int run_numbers[], h_num;
    String run_dir, inst_name, file_name, g_mask;
    Operator op, mon;
    Object result, obj, mon_res;
    DataSet result_ds;

    //gets the run numbers
    param = wizard.getParameter( "RunNumbers" );
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        run_numbers = IntList.ToArray(obj.toString());
        param.setValid(true);
    }
    else
    {
       param.setValid(false);
       SharedData.addmsg(
         "ERROR: you must enter one or more valid run numbers.\n");
       return false;
    }

    //get directory
    //should be no need to check this for validity
    param = wizard.getParameter( "DataDir" );
    run_dir = param.getValue().toString() + "/";
    param.setValid(true);

    //get instrument name
    param = wizard.getParameter( "InstName" );
    obj = param.getValue();
    if( obj != null && obj.toString().length() != 0 )
    {
        inst_name = obj.toString();
        //what instrument names should this check for?
        param.setValid(true);
    }
    else
    {
       param.setValid(false);
       SharedData.addmsg(
         "ERROR: you must enter a valid instrument name.\n");
       return false;
    }

    //get histogram number
    param = wizard.getParameter( "HNum" );
    obj = param.getValue();
    if( obj != null && obj instanceof Integer )
    {
        h_num = ((Integer)obj).intValue();
        param.setValid(true);
    }
    else
    {
       param.setValid(false);
       SharedData.addmsg(
         "ERROR: you must enter a valid histogram number.\n");
       return false;
    }

    //get group mask
    //how should I validate this?
    param = wizard.getParameter( "GMask" );
    obj = param.getValue();
    if( obj != null  )
    {
        g_mask = obj.toString();
        param.setValid(true);
    }
    else
    {
       param.setValid(false);
       SharedData.addmsg(
         "ERROR: you must enter a valid group mask.\n");
       return false;
    }

    //get the DataSet array
    histograms = (ArrayPG)wizard.getParameter( "RunList" );
    monitors = (ArrayPG)wizard.getParameter( "MonitorRunList" );
    //clear it out when the form is re-run
    histograms.clearValue();
    monitors.clearValue();

    for( int i = 0; i < run_numbers.length; i++ )
    {
      file_name = run_dir + inst_name + run_numbers[i] + ".RUN";
      op = new LoadOneHistogramDS(file_name, h_num, g_mask);
      mon = new LoadMonitorDS(file_name);
      result = op.getResult();
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
          result + " and " + mon_res + " added successfully.\n");
      }
      else // something went wrong
      {
        if( result instanceof ErrorString )
          SharedData.addmsg(result.toString() + "\n");
        else if (mon_res instanceof ErrorString )
          SharedData.addmsg(result.toString() + "\n");
        else
          SharedData.addmsg(
            "Could not load histogram and/or monitor from "
            + file_name + ".\n");
        return false;
      }

    }//for
    histograms.setValid(true);
    monitors.setValid(true);

    SharedData.addmsg("Finished loading DataSets from runfiles.\n\n");

    return true;
  }

}//class
