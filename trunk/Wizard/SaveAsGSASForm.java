/*
 * File:  SaveAsGSASForm.java
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2003/03/13 19:04:14  dennis
 *  Added $Log:$ comment to include revision information.
 *
 *
 */

package Wizard;

import java.io.*;
import DataSetTools.wizard.*;
import DataSetTools.parameter.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.operator.Generic.Save.WriteGSAS;
import DataSetTools.operator.Operator;
import java.util.Vector;
import DataSetTools.operator.Generic.Load.LoadMonitorDS;

/**
 *  This class defines a form for saving DataSets in
 *  GSAS format under the control of a Wizard.
 */
public class SaveAsGSASForm extends    Form
                              implements Serializable
{
  /**
   *  Construct an SaveAsGSASForm to open the DataSets listed in
   *  operands[] and save them using the GsasWriter operator..  This constructor basically
   *  just calls the super class constructor and builds an appropriate
   *  help message for the form.
   *
   *  @param  constants The list of names of parameters to be saved.
   *  @param  operands  The list of names of parameters to use for the
   *                    GsasWriter.
   *  @param  w         The wizard controlling this form.
   */
  public SaveAsGSASForm( String constant[], String operands[], Wizard w )
  {
    super("Save as GSAS", constant, operands, null, w );

    String help = "This form lets you save DataSets in GSAS format. \n";
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
    ArrayPG gr_res, mds_pg;
    Vector grouped, monitors;
    Operator op;
    Object obj, result;
    int num_datasets;
    boolean export_mon, seq_num;
    IParameterGUI param;
    DataSet mds, group_ds;
    String gsas_dir, gsas_name, save_name;
    boolean DEBUG = true;

    //get the ArrayPG grouping result
    gr_res = (ArrayPG)wizard.getParameter("GroupingResults");
    grouped = (Vector)gr_res.getValue();

    //get the user input parameters
    //get directory
    //should be no need to check this for validity
    param = wizard.getParameter( "GSASDir" );
    gsas_dir = param.getValue().toString() + "/";
    param.setValid(true);

    param = wizard.getParameter("GSASName");

    obj = param.getValue();
    if( obj != null && obj instanceof String )
    {
      gsas_name = (String)obj;
      //empty name; give it a default
      if( gsas_name.equals("") )
        gsas_name = "GSAS";
      param.setValid(true);
    }
    else
    {
      param.setValid(false);
      SharedData.addmsg(
        "Please enter a valid name for the file.\n");
      return false;
    }

    param = wizard.getParameter("ExportMon");
    //this one doesn't need to be checked for validity
    param.setValid(true);
    export_mon = ((BooleanPG)param).getbooleanValue();

    param = wizard.getParameter("SeqNum");
    //this one doesn't need to be checked for validity
    param.setValid(true);
    seq_num = ((BooleanPG)param).getbooleanValue();

    //get the ArrayPG load monitor DS result
    mds_pg = (ArrayPG)wizard.getParameter("MonitorRunList");
    monitors = (Vector)mds_pg.getValue();

    //go through the vector
    for( int i = 0; i < grouped.size(); i++ )
    {
      //get the DataSet in each Vector "slot"
      group_ds = (DataSet)grouped.elementAt(i);
      mds = (DataSet)monitors.elementAt(i);

      //save the GSAS file
      save_name = gsas_dir + gsas_name + i + ".GSAS";
      //the monitor DataSet parameter still needs to be loaded
      //somehow - null should work for the time being
      op = new WriteGSAS(mds, group_ds,
                         save_name, new Boolean(export_mon),
                         new Boolean(seq_num));
      if( DEBUG )
        SharedData.addmsg(mds.toString());
      result = op.getResult();

      if( result instanceof String && result.equals("Success") )
        SharedData.addmsg("File " + save_name + "saved.\n");
      else  //something went wrong
      {
        SharedData.addmsg(
          "File " + save_name + "could not be saved.\n");
        return false;
      }
    }

    SharedData.addmsg("Finished saving GSAS files.\n\n");

    return true;

  }


}//class
