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
 * This work was supported by the National Science Foundation.
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

/**
 *  This class defines a form for time focusing spectra in 
 *  a DataSet under the control of a Wizard.
 */
public class TimeFocusForm extends    Form
                              implements Serializable
{
  /**
   *  Construct an TimeFocusForm to time focus the spectra in a DataSet 
   *  using the arguments in operands[].  This constructor basically 
   *  just calls the super class constructor and builds an appropriate 
   *  help message for the form.
   *
   *  @param  constants The vector of arrays of DataSets to be time focused
   *  @param  operands  The list of names of parameters to be added.
   *  @param  result    The 
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
   *  This overrides the execute() method of the super class and provides
   *  the code that actually does the calculation.
   *
   *  @return 
   */
  public boolean execute()
  {
    SharedData.addmsg("Executing...\n");
    ArrayPG apg, tfr;
    Vector runfiles;
    Operator tf;
    Object obj, result;
    DataSet datasets[], time_focused[];
    int num_runs, num_datasets;
    float angle, path;
    boolean make_new_ds;
    String focusing_IDs;
    IParameterGUI param;
    DataSet ds;
    
    //get the ArrayPG run list result
    apg = (ArrayPG)wizard.getParameter("RunList");
    runfiles = (Vector)apg.getValue(); 
    
    //get the user input parameters
    param = wizard.getParameter("FocusIDs");
    param.setValid(true);
    focusing_IDs = ((StringPG)param).getStringValue();
    param = wizard.getParameter("NewAngle");
    param.setValid(true);
    angle = ((FloatPG)param).getfloatValue();
    param = wizard.getParameter("NewFPath");
    param.setValid(true);
    path = ((FloatPG)param).getfloatValue();
    param = wizard.getParameter("MakeNewds");
    param.setValid(true);
    make_new_ds = ((BooleanPG)param).getbooleanValue();
    
    //get the time focus result parameter
    tfr = (ArrayPG)wizard.getParameter("TimeFocusResults");
    //clear it out when the form is re-run
    tfr.clearValue();
    
    //make sure list exists
    if( runfiles != null )
    {   
      //get the runfiles array size
      num_runs = runfiles.size();
      //go through the array, getting each runfile's DataSets
      for( int i = 0; i < num_runs; i++ )
      {
        obj = runfiles.elementAt(i);

        if( obj instanceof DataSet[] )
        {
          datasets = (DataSet[])obj;
          num_datasets = datasets.length;
		        
          //the time focused DataSets will be the same in number
          //so create the time_focused_array at that size
          time_focused = new DataSet[num_datasets];
		       
          //time_focus the DataSets
          for( int j = 0; j < num_datasets; j++ )
          {
	           ds = datasets[j];
	           
	           if( ds != DataSet.EMPTY_DATA_SET )
	           {
              tf = new TimeFocus(ds, focusing_IDs, 
                                 angle, path, make_new_ds);
              obj = tf.getResult();
            }
            
            //make sure we are working with DataSets
            //TimeFocus will always return a DataSet unless it 
            //hits an error (as of 02/17/2003)
            if( obj instanceof DataSet )
            { 
              time_focused[j] = (DataSet)obj;
              SharedData.addmsg(datasets[j] + " time focused.\n"); 
            }
            else
            {
	             if( obj instanceof ErrorString )
                       SharedData.addmsg(obj.toString() + "\n");
	             else
                       SharedData.addmsg("Could not focus DataSet: "
                                         +datasets[j]);
            }
          }
          //add the time focused DataSet array to time focused results
          tfr.addItem(time_focused);
        }
      }
      tfr.setValid(true);
      SharedData.addmsg("Finished time focusing DataSets.\n\n");
      return true;
    }
    //broke, need to return false to let the wizard know
    else
    {
      SharedData.addmsg("No runfiles selected.\n");
      return false;
    }
    
  } 
		    
}
