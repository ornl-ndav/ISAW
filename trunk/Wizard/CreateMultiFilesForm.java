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
import DataSetTools.operator.Generic.Load.LoadOneRunfile;
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
   *  @param  operands  The list of names of parameters to be added.
   *  @param  w         The wizard controlling this form. 
   */
  public CreateMultiFilesForm( String operands[], String result[], Wizard w )
  {
    super("Open multiple files", null, operands, result, w );

    String help = "This form lets you open multiple run files \n";
    setHelpMessage( help );

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
	   IParameterGUI param;
	   ArrayPG apg;
	   int run_numbers[];
	   String run_dir, inst_name, file_name;
	   Operator op;
	   Object result;
	   DataSet[] result_sets;
	  
	   //gets the run numbers
    param = (IntArrayPG)wizard.getParameter( "RunNumbers" );
    run_numbers = IntList.ToArray(param.getValue().toString());
    param.setValid(true);
    
    //get directory
    param = wizard.getParameter( "DataDir" );
    run_dir = param.getValue().toString() + "/";
    param.setValid(true);
    
    //get instrument name
    param = wizard.getParameter( "InstName" );
    inst_name = param.getValue().toString(); 
    param.setValid(true);
    
    //create full runfile array
    apg = (ArrayPG)wizard.getParameter( "RunList" );
    //clear it out when the form is re-run
    apg.clearValue();
    
    for( int i = 0; i < run_numbers.length; i++ )
    {
      file_name = run_dir + inst_name + run_numbers[i] + ".RUN";
      op = new LoadOneRunfile(file_name, "0");
      result = op.getResult();
      
      if( result instanceof DataSet[] )
      {
        //add the runfile
        result_sets = (DataSet[])result;
        apg.addItem(result_sets);
        //let the user know the runfile was added successfully
        SharedData.addmsg(file_name + " added successfully.\n");
        apg.setValid(true);
      }
      else // something went wrong
      {
        if( result instanceof ErrorString )
          SharedData.addmsg(result.toString() + "\n");
        else
          SharedData.addmsg("Could not load " + file_name + ".\n");
        return false;
      }

    }//for
    
    SharedData.addmsg("Finished loading DataSets from runfiles.\n\n");
	   
    return true;
  } 
  
}//class
