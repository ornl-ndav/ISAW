/*
 * File:  TimeFocusForm.java
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * Modified: 
 *
 * $Log$
 * Revision 1.5  2003/03/13 19:00:52  dennis
 * Added $Log:$ comment to include revision information.
 *
 *
 */

package Wizard;

import java.io.*;
import DataSetTools.wizard.*;
import DataSetTools.parameter.*;
import DataSetTools.dataset.*;
import java.util.Vector;
import DataSetTools.operator.Operator;
import Operators.TOF_Diffractometer.TimeFocus;
import DataSetTools.util.*;
import javax.swing.*;
import java.awt.*;

/**
 *  This class defines a form for time focusing spectra in
 *  a DataSet under the control of a Wizard.
 */
public class TimeFocusForm extends    Form
                              implements Serializable
{
  private JPanel godlike, demonic;
  private JButton maestro;

  /**
   *  Construct an TimeFocusForm to time focus the spectra in a DataSet
   *  using the arguments in operands[].  This constructor basically
   *  just calls the super class constructor and builds an appropriate
   *  help message for the form.
   *
   *  @param  constants The list of names of parameters to be time focused
   *  @param  operands  The list of names of parameters to use for the
   *                    time focusing.
   *  @param  result    The list of names of parameters which have been
   *                    time focused.
   *  @param  w         The wizard controlling this form.
   */
  public TimeFocusForm( String constants[], String operands[], String result[], Wizard w )
  {
    super("Time focus DataSets", constants, operands, result, w );

    StringBuffer help = new StringBuffer();
    help.append("This form lets you time focus the spectra in DataSets. \n");
    help.append("Note that whatever you put in for parameters will affect ");
    help.append("ALL DataSets in the list.\n");
    setHelpMessage( help.toString() );


  }

  /**
   *  Time focuses the Vector of DataSet arrays and loads them
   *  into a new Vector of DataSet arrays (in an ArrayPG).
   *
   *  @return true if all of the parameters are valid and all hist_ds
   *  can be time focused; false if any significant error occurs
   */
  public boolean execute()
  {
    SharedData.addmsg("Executing...\n");
    ArrayPG histograms, tfr;
    Vector hist_ds_vec;
    Operator tf;
    Object obj, result;
    DataSet hist_ds, time_focused;
    int num_ds;
    float angle, path;
    boolean make_new_ds;
    String focusing_IDs;
    IParameterGUI param;
    DataSet ds;

    //get the DataSet array
    histograms = (ArrayPG)wizard.getParameter("RunList");
    hist_ds_vec = (Vector)histograms.getValue();

    //get the user input parameters
    param = wizard.getParameter("FocusIDs");
    focusing_IDs = ((StringPG)param).getStringValue();
    //not sure how to validate this
    param.setValid(true);

    param = wizard.getParameter("NewAngle");

    obj = param.getValue();
    if( obj != null && obj instanceof Float)
    {
      angle = ((Float)obj).floatValue();
      param.setValid(true);
    }
    else
    {
      param.setValid(false);
      SharedData.addmsg(
        "The new angle must be a float value.\n");
      return false;
    }

    param = wizard.getParameter("NewFPath");
    //needs to be an error check here-if the
    //parameter is entered as "" it will
    //throw a java.lang.NumberFormatException
    obj = param.getValue();
    if( obj != null && obj instanceof Float)
    {
      path = ((Float)obj).floatValue();
      param.setValid(true);
    }
    else
    {
      param.setValid(false);
      SharedData.addmsg(
        "The new path must be a float value.\n");
      return false;
    }

    param = wizard.getParameter("MakeNewds");
    //this one doesn't need to be checked for validity
    param.setValid(true);
    make_new_ds = ((BooleanPG)param).getbooleanValue();

    //get the time focus result parameter
    tfr = (ArrayPG)wizard.getParameter("TimeFocusResults");
    //clear it out when the form is re-run
    tfr.clearValue();

    //make sure list exists
    if( hist_ds_vec != null )
    {
      //get the hist_ds_vec array size
      num_ds = hist_ds_vec.size();
      //go through the array, getting each runfile's hist_ds
      for( int i = 0; i < num_ds; i++ )
      {
        obj = hist_ds_vec.elementAt(i);

        if( obj instanceof DataSet )
        {
          hist_ds = (DataSet)obj;

          //time_focus the DataSet

          if( hist_ds != DataSet.EMPTY_DATA_SET )
          {
            tf = new TimeFocus(hist_ds, focusing_IDs,
                               angle, path, make_new_ds);
            obj = tf.getResult();
          }
          else
          {
            SharedData.addmsg("Encountered empty DataSet: " + hist_ds);
            return false;
          }

          //make sure we are working with hist_ds
          //TimeFocus will always return a DataSet unless it
          //hits an error (as of 02/17/2003)
          if( obj instanceof DataSet )
          {
            time_focused = (DataSet)obj;
            SharedData.addmsg(hist_ds + " time focused.\n");
          }
          else
          {
            if( obj instanceof ErrorString )
              SharedData.addmsg(obj.toString() + "\n");
            else
              SharedData.addmsg("Could not focus DataSet: "
                                + hist_ds);
            return false;
          }
          //add the time focused DataSet to time focused results
          tfr.addItem(time_focused);
        }
        else //something went wrong in previous form
        {
          SharedData.addmsg("Encountered non-DataSet.\n");
          return false;
        }
      }
      tfr.setValid(true);
      SharedData.addmsg("Finished time focusing DataSets.\n\n");
      return true;
    }
    //broke, need to return false to let the wizard know
    else
    {
      SharedData.addmsg("No histograms selected.\n");
      return false;
    }

  }

}
