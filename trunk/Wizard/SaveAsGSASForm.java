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
 *  Revision 1.5  2003/04/24 18:57:56  pfpeterson
 *  Various small bug fixes. (Chris Bouzek)
 *
 *  Revision 1.4  2003/04/02 15:02:46  pfpeterson
 *  Changed to reflect new heritage (Forms are Operators). (Chris Bouzek)
 *
 *  Revision 1.3  2003/03/19 15:07:04  pfpeterson
 *  Added the monitor DataSets as an explicit parameter and now creates
 *  filename from instrument name and run number. (Chris Bouzek)
 *
 *  Revision 1.2  2003/03/13 19:04:14  dennis
 *  Added $Log$
 *  Added Revision 1.5  2003/04/24 18:57:56  pfpeterson
 *  Added Various small bug fixes. (Chris Bouzek)
 *  Added
 *  Added Revision 1.3  2003/03/19 15:07:04  pfpeterson
 *  Added Added the monitor DataSets as an explicit parameter and now creates
 *  Added filename from instrument name and run number. (Chris Bouzek)
 *  Added comment to include revision information.
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
   *  Construct an SaveAsGSASForm to open a set of histograms 
   *  which have been time focused and grouped, and save them
   *  in GSAS format.
   */
  public SaveAsGSASForm()
  {
    super("Save as GSAS");
  }

  /**
   *
   *  Full constructor.  Uses the input parameters to create
   *  a SaveAsGSASForm without the need to externally
   *  set the parameters.  getResult() may 
   *  be called immediately after using this constructor.
   *
   *  @param tf_array         The array of time focused 
   *                          histograms that you wish to 
   *                          save in GSAS format.
   *
   *  @param mon_array        The array of monitor DataSets
   *                          that you wish to use for the
   *                          SaveAsGSAS operation.
   *
   *  @param run_nums         The run numbers from the
   *                          files that you loaded the
   *                          histograms and monitors from.
   *
   *  @param inst_name        The name of the instrument associated
   *                          with the histograms.
   *
   *  @param gsas_dir         The directory to store the GSAS files
   *                          in.
   * 
   *  @param export_mon       Boolean indicating whether you want to
   *                          export the monitor DataSets.
   *
   *  @param seq_num          Boolean indicating whether you want to 
   *                          sequentially number the banks.
   */
  public SaveAsGSASForm(ArrayPG tf_array,
                        ArrayPG mon_array,
                        IntArrayPG run_nums, 
                        InstNamePG inst_name,
                        DataDirPG gsas_dir,
                        BooleanPG export_mon,
                        BooleanPG seq_num)
  {
    this();
    setParameter(tf_array, 0);
    setParameter(mon_array, 1);
    setParameter(run_nums, 2);
    setParameter(inst_name, 3);
    setParameter(gsas_dir, 4);
    setParameter(export_mon, 5);
    setParameter(seq_num, 6);
  }  
                       
  /**
   *
   *  Attempts to set reasonable default parameters for this form.
   *  Included in this is a default setting of the DataSet array
   *  corresponding to the respective runfiles' loaded histograms, 
   *  as well as the corresponding type of the parameter (editable, 
   *  result, or constant).
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter(new ArrayPG("Time focused histograms", new Vector(), false));
    addParameter(new ArrayPG("Monitor DataSets", new Vector(), false));
    addParameter(new IntArrayPG( "Run Numbers", "12358", false));
    addParameter(new InstNamePG( "InstrumentName", null, false));
    addParameter(new DataDirPG( "Directory to save GSAS files to",null,false));
    addParameter(new BooleanPG("Export Monitor DataSet", false, false));
    addParameter(new BooleanPG("Sequential bank numbering", false, false));
    setParamTypes(new int[]{0,1,2,3},new int[]{4,5,6},null);
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
    s.append("@overview This Form is designed for saving an ArrayPG of time ");
    s.append("focused and grouped histograms into corresponding GSAS files, ");
    s.append("under the control of a Wizard.\n");
    s.append("@assumptions It is assumed that the specified histograms ");
    s.append("exist.  In addition, it is assumed that the a valid");
    s.append("monitor DataSet exists for each histogram.\n");
    s.append("@algorithm This Form converts each histogram to GSAS format ");
    s.append("using the monitor DataSet and the user specifed parameters.  ");
    s.append("Each histogram (which is a DataSet) is saved under a file ");
    s.append("named <InstName><InstNum>.gsa (e.g. hrcs2447.gsa).\n");
    s.append("@param run_nums Array of integers representing the runfile ");
    s.append("numbers which you wish to load histograms from.\n");
    s.append("@param data_dir The directory from which to load the runfiles ");
    s.append("from.\n");
    s.append("@param tf_array The array of time focused histograms that you ");
    s.append("wish to save in GSAS format.\n");
    s.append("@param mon_array The array of monitor DataSets that you wish ");
    s.append("to use for the SaveAsGSAS operation.\n");
    s.append("@param run_nums The run numbers from the files that you ");
    s.append("loaded the histograms and monitors from.\n");
    s.append("@param inst_name The name of the instrument associated with the ");
    s.append("histograms.\n");
    s.append("@param gsas_dir The directory to store the GSAS files in.\n");
    s.append("@param export_mon Boolean indicating whether you want to export ");
    s.append("the monitor DataSets into the GSAS file.\n");
    s.append("@param seq_num Boolean indicating whether you want to ");
    s.append("sequentially number the banks.\n");
    s.append("@return Presently, returns a Boolean which indicates either ");
    s.append("success or failure.\n");
    s.append("@error Returns a Boolean false if any of the specified ");
    s.append("histograms could not be saved in GSAS format.\n");
    return s.toString();
  }

  /**
   *  Returns the String command used for invoking this
   *  Form in a Script.
   */
  public String getCommand()
  {
    return "SAVEASGSASFORM";
  }



  /**
   *  Saves the loaded time focused and grouped histograms in GSAS 
   *  format.
   *
   *  @return true if all of the parameters are valid and all hist_ds
   *  can be time focused and grouped; false if any significant error occurs
   */
  public Object getResult()
  {
    SharedData.addmsg("Executing...\n");
    ArrayPG gr_res, mds_pg;
    Vector grouped, monitors;
    Operator op;
    Object obj, result;
    boolean export_mon, seq_num;
    IParameterGUI param;
    DataSet mds, group_ds;
    String gsas_dir, save_name, inst_name;
    boolean DEBUG = true;
    int[] run_numbers;

    //get the results
    gr_res = (ArrayPG)super.getParameter(0);
    grouped = (Vector)gr_res.getValue();
    mds_pg = (ArrayPG)super.getParameter(1);
    monitors = (Vector)mds_pg.getValue();
    obj = super.getParameter(2).getValue();
    run_numbers = IntList.ToArray(obj.toString());
    inst_name = ((IParameterGUI)super.getParameter(3))
                .getValue().toString().toLowerCase();

    //get the user input parameters
    //get directory
    //should be no need to check this for validity
    param = (IParameterGUI)super.getParameter(4);
    gsas_dir = param.getValue().toString() + "/";
    param.setValid(true);

    param = (IParameterGUI)super.getParameter(5);
    //this one doesn't need to be checked for validity
    param.setValid(true);
    export_mon = ((BooleanPG)param).getbooleanValue();

    param = (IParameterGUI)super.getParameter(6);
    //this one doesn't need to be checked for validity
    param.setValid(true);
    seq_num = ((BooleanPG)param).getbooleanValue();

    //go through the vector
    for( int i = 0; i < grouped.size(); i++ )
    {
      //get the DataSet in each Vector "slot"
      group_ds = (DataSet)grouped.elementAt(i);
      mds = (DataSet)monitors.elementAt(i);

      //save the GSAS file
      save_name = gsas_dir + inst_name + run_numbers[i] + ".gsa";
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
        return new Boolean(false);
      }
    }

    SharedData.addmsg("Finished saving GSAS files.\n\n");

    return new Boolean(true);

  }


}//class
