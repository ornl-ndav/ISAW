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
   *  @param  constants The vector of arrays of DataSets to be grouped
   *  @param  operands  The list of names of parameters to be added.
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
   *  This overrides the execute() method of the super class and provides
   *  the code that actually does the calculation.
   *
   *  @return 
   */
  public boolean execute()
  {try{
    SharedData.addmsg("Executing...\n");
    ArrayPG tfa, ga;
    Vector runfiles;
    Operator grp;
    Object obj, result;
    DataSet time_focused[], grouped[];
    int num_runs, num_datasets;
    float angle, path;
    boolean make_new_ds;
    String group_string;
    int new_id;
    IParameterGUI param;
    DataSet ds;
    
    //get the Time Focused results
    tfa = (ArrayPG)wizard.getParameter("TimeFocusResults");
    runfiles = (Vector)tfa.getValue(); 
    
    //get the user input parameters
    param = wizard.getParameter("GroupStr");
    param.setValid(true);
    group_string = ((StringPG)param).getStringValue();
    param = wizard.getParameter("NewGroupID");
    param.setValid(true);
    new_id = ((IntegerPG)param).getintValue();
    
    //get the grouping result parameter
    ga = (ArrayPG)wizard.getParameter("GroupingResults");
    //clear it out when the form is re-run
    ga.clearValue();
    
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
          time_focused = (DataSet[])obj;
          num_datasets = time_focused.length;
		        
          //the grouped DataSets will be the same in number
          //so create the time_focused_array at that size
          grouped = new DataSet[num_datasets];
		       
          //group the DataSets
          for( int j = 0; j < num_datasets; j++ )
          { 
	           ds = time_focused[j];
	          
	          	if( ds != DataSet.EMPTY_DATA_SET )
	           {
              grp = new Grouping(time_focused[j], group_string, new_id);
              obj = grp.getResult();
            }
            
            //make sure we are working with DataSets
            //Grouping will always return a DataSet unless it 
            //hits an error (as of 02/17/2003)
            if( obj instanceof DataSet )
            {
              grouped[j] = (DataSet)obj;
              SharedData.addmsg(time_focused[j] + " grouped.\n"); 
            }
            else
            {
	             if( obj instanceof ErrorString )
                       SharedData.addmsg(obj.toString() + "\n");
	             else
                       SharedData.addmsg("Could not focus DataSet: "
                                         + time_focused[j] + "\n");
            }
          }
          //add the time focused DataSet array to time focused results
          ga.addItem(time_focused);
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
    
      
    
  }catch(Exception e)
  {
	   printErrorFile(e);
	   return false;
  }
  
  } 
  

  
  public void printErrorFile(Exception exc)
  {
    try 
    {
      File err_file = new File("Errors.out");
      FileWriter fw = new FileWriter(err_file);
      PrintWriter pw = new PrintWriter(fw);
      exc.printStackTrace(pw);
      fw.close();
      pw.close();
    }
    catch( Exception e2 )
    {

    }
  }

}
