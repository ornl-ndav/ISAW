/*
 * File:  TimeFocusGroupForm.java
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
 * Revision 1.1  2003/03/19 15:07:51  pfpeterson
 * Added to CVS. (Chris Bouzek)
 *
 * Revision 1.5  2003/03/13 19:00:52  dennis
 * Added $Log$
 * Added Revision 1.1  2003/03/19 15:07:51  pfpeterson
 * Added Added to CVS. (Chris Bouzek)
 * Added comment to include revision information.
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
import Operators.TOF_Diffractometer.*;
import DataSetTools.util.*;
import javax.swing.*;
import java.awt.*;

/**
 *  This class defines a form for time focusing spectra in
 *  a DataSet under the control of a Wizard.
 */
public class TimeFocusGroupForm extends    Form
                              implements Serializable
{
  /**
   *  Construct a TimeFocusGroupForm to time focus and group the spectra in a 
   *  DataSet using the arguments in operands[].  This constructor basically
   *  just calls the super class constructor and builds an appropriate
   *  help message for the form.
   *
   *  @param  constants The list of names of parameters to be time focused
                        and grouped.
   *  @param  operands  The list of names of parameters to use for the
   *                    time focusing and grouping    .
   *  @param  result    The list of names of parameters which have been
   *                    time focused and grouped.
   *  @param  w         The wizard controlling this form.
   */
  public TimeFocusGroupForm( String constants[], String operands[], String result[], Wizard w )
  {
    super("Time focus DataSets", constants, operands, result, w );

    StringBuffer help = new StringBuffer();
    help.append("This form lets you time focus and group the spectra in one or more ");
    help.append("DataSets.\nNote that whatever you put in for parameters will affect ");
    help.append("ALL DataSets in the list.\n");
    setHelpMessage( help.toString() );


  }

  /**
   *  Time focuses and groups the Vector of DataSets and loads them
   *  into a new Vector of DataSets (in an ArrayPG).
   *
   *  @return true if all of the parameters are valid and all hist_ds
   *  can be time focused and grouped; false if any significant error occurs
   */
  public boolean execute()
  {
    SharedData.addmsg("Executing...\n");
    ArrayPG histograms, tfgr;
    Vector hist_ds_vec;
    Operator tf, gro;
    Object obj, result;
    DataSet hist_ds, time_focused, grouped;
    int num_ds, new_GID;
    float angle, path;
    boolean make_new_ds;
    String focusing_GIDs;
    IParameterGUI param;
    DataSet ds;

    //get the DataSet array
    histograms = (ArrayPG)wizard.getParameter("RunList");
    hist_ds_vec = (Vector)histograms.getValue();

    //get the user input parameters
    param = wizard.getParameter("FocusIDs");
    focusing_GIDs = ((StringPG)param).getStringValue();
    if( !focusing_GIDs.equals("") )
    {
      //set the new group ID
      new_GID = new Integer(focusing_GIDs.substring(0,1)).intValue();
      param.setValid(true);
    }
    else
    {
      param.setValid(false);
      SharedData.addmsg(
        "You must enter at least one valid group ID.\n");
      return false;  
    }    

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

    //get the time focus/group result parameter
    tfgr = (ArrayPG)wizard.getParameter("TimeFocusGroupResults");
    //clear it out when the form is re-run
    tfgr.clearValue();

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
            //do NOT make a new DataSet
            tf = new TimeFocusGID(hist_ds, focusing_GIDs,
                               angle, path, false);
            result = tf.getResult();
          }
          else
          {
            SharedData.addmsg("Encountered empty DataSet: " + hist_ds);
            return false;
          }

          //make sure we are working with DataSets
          //TimeFocus will always return a DataSet unless it
          //hits an error (as of 02/17/2003)
          if( result instanceof DataSet )
          {
            time_focused = (DataSet)result;
            SharedData.addmsg(hist_ds + " time focused.\n");
            //group it
            gro = new Grouping(time_focused, focusing_GIDs, new_GID);
            result = gro.getResult();
          }
          else
          {
            if( result instanceof ErrorString )
              SharedData.addmsg(result.toString() + "\n");
            else
              SharedData.addmsg("Could not time focus DataSet: "
                                + hist_ds);
            return false;
          }
          
          //check the grouped DataSet for correctness
          if( result instanceof DataSet )
          {
            grouped = (DataSet)result;
            SharedData.addmsg(time_focused + " grouped.\n");
          }
          else
          {
            if( result instanceof ErrorString )
              SharedData.addmsg(result.toString() + "\n");
            else
              SharedData.addmsg("Could not group DataSet: "
                                + time_focused);
            return false;
          }          
          
          //add the time focused DataSet to time focused results
          tfgr.addItem(grouped);
        }
        else //something went wrong in previous form
        {
          SharedData.addmsg("Encountered non-DataSet.\n");
          return false;
        }
      }
      tfgr.setValid(true);
      SharedData.addmsg("Finished time focusing and grouping DataSets.\n\n");
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
