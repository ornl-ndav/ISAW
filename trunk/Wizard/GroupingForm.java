/*
 * File:  GroupingForm.java
 *
 * Copyright (C) 2003, Christopher Bouzek
 *
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
 */

package Wizard;

import java.io.*;
import DataSetTools.wizard.*;
import DataSetTools.parameter.*;
import DataSetTools.dataset.*;
import java.util.Vector;
import DataSetTools.operator.Operator;
import Operators.TOF_Diffractometer.Grouping;
import DataSetTools.util.*;

/**
 *  This class defines a form for summing a list of data blocks in
 *  a DataSet under the control of a Wizard.
 */
public class GroupingForm extends    Form
                              implements Serializable
{
  /**
   *  Construct an GroupingForm to sum the data blocks in the DataSet
   *  using the arguments in operands[].  This constructor basically
   *  just calls the super class constructor and builds an
   *  appropriate help message for the form.
   *
   *  @param  constants The list of names of parameters to be grouped.
   *  @param  operands  The list of names of parameters to use for the
   *                    grouping.
   *  @param  result    The list of names of parameters which have been
   *                    grouped.
   *  @param  w         The wizard controlling this form.
   */
  public GroupingForm( String constants[], String operands[], String results[], Wizard w )
  {
    super("Group DataSets", constants, operands, results, w );

    String help =
      "This form lets you group DataSets after they have been time focused.\n";
    setHelpMessage( help );
  }

  /**
   *  Groups the Vector of DataSet arrays and loads them
   *  into a new Vector of DataSet arrays (in an ArrayPG).
   *
   *  @return true if all of the parameters are valid and all Datasets
   *  can be grouped; false if any significant error occurs
   */
  public boolean execute()
  {
    SharedData.addmsg("Executing...\n");
    ArrayPG tfa, ga;
    Vector tf_ds_vec;
    Operator grp;
    Object obj, result;
    DataSet time_focused, grouped;
    int num_ds;
    String group_string;
    int new_id;
    IParameterGUI param;
    DataSet ds;

    //get the Time Focused results
    tfa = (ArrayPG)wizard.getParameter("TimeFocusResults");
    tf_ds_vec = (Vector)tfa.getValue();

    //get the user input parameters
    param = wizard.getParameter("GroupStr");
    //not sure how to check this one for validity
    param.setValid(true);
    group_string = ((StringPG)param).getStringValue();

    param = wizard.getParameter("NewGroupID");
    obj = ((IntegerPG)param).getValue();
    if( obj != null )
    {
      new_id = ((Integer)obj).intValue();
      param.setValid(true);
    }
    else
    {
       param.setValid(false);
       SharedData.addmsg("The new path must be a float value.\n");
       return false;
    }

    //get the grouping result parameter
    ga = (ArrayPG)wizard.getParameter("GroupingResults");
    //clear it out when the form is re-run
    ga.clearValue();

    //make sure list exists
    if( tf_ds_vec != null )
    {
      //get the tf_ds_vec array size
      num_ds = tf_ds_vec.size();
      //go through the array, getting each runfile's DataSets
      for( int i = 0; i < num_ds; i++ )
      {
        obj = tf_ds_vec.elementAt(i);
        if( obj instanceof DataSet )
        {
          time_focused = (DataSet)obj;

          //group the DataSets

          if( time_focused != DataSet.EMPTY_DATA_SET )
          {
            grp = new Grouping(time_focused, group_string, new_id);
            obj = grp.getResult();
          }
          else
          {
            SharedData.addmsg(
              "Encountered empty DataSet: " + time_focused);
            return false;
          }

          //make sure we are working with DataSets
          //Grouping will always return a DataSet unless it
          //hits an error (as of 02/17/2003)
          if( obj instanceof DataSet )
          {
            grouped = (DataSet)obj;
            SharedData.addmsg(time_focused + " grouped.\n");
          }
          else
          {
            if( obj instanceof ErrorString )
              SharedData.addmsg(obj.toString() + "\n");
            else
              SharedData.addmsg("Could not focus DataSet: "
                                + time_focused + "\n");
            return false;
          }
          //add the time focused DataSet array to time focused results
          ga.addItem(time_focused);
        }
        else //something went wrong in previous form
        {
          SharedData.addmsg("Encountered non-DataSet.\n");
          return false;
        }
      }
      ga.setValid(true);
      SharedData.addmsg("Finished grouping DataSets.\n\n");
      return true;
    }
    //broke, need to return false to let the wizard know
    else
    {
      SharedData.addmsg("No DataSets available to group.\n");
      return false;
    }
  }

}
