/*
 * File:  LoadMultiSCDHistForm.java
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

package Wizard.TOF_SCD;

import java.io.Serializable;
import DataSetTools.parameter.*;
import java.util.Vector;
import Wizard.LoadMultiHistogramsForm;

/**
 *  This class defines a form for loading histograms from 
 *  multiple SCD runfiles.  In addition to loading the user
 *  specified histogram, the corresponding monitor DataSet
 *  is also loaded.  This Form was created to meet the 
 *  needs of SCD users, and remove some error-prone
 *  areas.
 */
public class LoadMultiSCDHistForm extends LoadMultiHistogramsForm
                              implements Serializable
{

  private static int RUN_NUMBER_WIDTH = 5;
  /**
   *  Construct a LoadMultiHistogramsForm.
   *  
   */
  public LoadMultiSCDHistForm()
  {
    super("Open multiple SCD histograms");
    this.setDefaultParameters();
  } 

  /**
   *
   *  Full constructor.  Uses the input parameters to create
   *  a LoadMultiSCDHistForm without the need to externally
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
   *  @param hist_num         The histogram number you wish
   *                          to load.
   *
   *  @param g_mask           The group mask to apply.  If
   *                          left blank, none will be applied.
   *
   *  @param histograms       The array which you wish to store
   *                          the loaded histograms in.
   */
  public LoadMultiSCDHistForm(String run_nums,     // 0
                              String data_dir,     // 1
                              int hist_num,        // 3
                              int g_mask,          // 4
                              Vector histograms)   // 5
  {
    this();
    getParameter(0).setValue(run_nums);
    getParameter(1).setValue(data_dir);
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
    addParameter(new IntArrayPG("Run Numbers", "06496", false));
    addParameter(new DataDirPG("Location of SCD runfiles", "", false));
    addParameter(new InstNamePG("Instrument Name", "SCD", true));
    addParameter(new IntegerPG( "Histogram number", 1, false));
    addParameter(new IntArrayPG( "Group IDs to omit", "", false));
    addParameter(new ArrayPG( "Histogram List", new Vector(), false));
    addParameter(new ArrayPG( "Monitor Run List", new Vector(), false));
    setParamTypes(new int[]{2},new int[]{0,1,3,4},new int[]{5,6});
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
    s.append("(e.g. the first one)  from one or more SCD runfiles, under ");
    s.append("the control of a Wizard.  It also loads the monitor ");
    s.append("DataSets corresponding to each runfile.\n");
    s.append("@assumptions It is assumed that the specified runfiles ");
    s.append("exist.  In addition, it is assumed that the specifed ");
    s.append("histogram exists.\n");
    s.append("@algorithm This Form opens each SCD runfile and retrieves the ");
    s.append("specifed histogram from it.  The histograms are then stored ");
    s.append("in an ArrayPG.  The corresponding monitor DataSets are stored ");
    s.append("in a parallel ArrayPG.\n");
    s.append("@param run_nums Array of integers representing the SCD runfile ");
    s.append("numbers which you wish to load histograms from.\n");
    s.append("@param data_dir The directory from which to load the SCD ");
    s.append("runfiles from.\n");
    s.append("@param hist_num The histogram number you wish to load.\n");
    s.append("@param g_mask The group mask to apply.  If left blank, none ");
    s.append("will be applied.\n");
    s.append("@param histograms The array which you wish to store the loaded ");
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
    return "LOADMULTISCDHISTFORM";
  }

}//class
