/*
 * File:  TimeFocusGroupWizard.java
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

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import DataSetTools.wizard.*;
import DataSetTools.util.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;


/**
 *  This class has a main program that constructs a Wizard for time
 *  focusing and grouping spectra in a DataSet.
 */
public class TimeFocusGroupWizard
{

  /**
   *  The main program constructs a new Wizard, defines the parameters to
   *  be stored in the master parameter list, and constructs instances of
   *  of the forms that define the operations available.
   */
  public static void main( String args[] )
  {
                                                      // build the wizard and
                                                      // specify the help
                                                      // messages.
    Wizard w = new Wizard( "Time Focus and Group Wizard" );
    SharedData.addmsg("TimeFocusGroupWizard Main\n");
    StringBuffer s = new StringBuffer();
    s.append("This wizard will let you do time focusing and grouping on ");
    s.append("the spectra in one or more DataSets.  The default values for ");
    s.append("FocusIDs focus all spectra in the DataSets.");
    w.setHelpMessage(s.toString());
    w.setAboutMessage("This is a work in progress.  02/03/2003, CMB");

                                                      // define the entries in
                                                      // in the master list
    //for CreateMultiFilesForm
    w.setParameter( "RunNumbers",
                    new IntArrayPG( "Run numbers",
                    new String("12358"), false));
    w.setParameter( "DataDir",
                    new DataDirPG( "Runfile Directory",new String(""), false));
    w.setParameter( "InstName",
                    new InstNamePG( "Instrument name",
                    new InstrumentNameString("GPPD"), false));
    w.setParameter( "HNum",
                    new IntegerPG( "Histogram number",
                    new Integer(1), false));
    w.setParameter( "GMask",
                    new IntArrayPG( "Group IDs to omit",
                    new String(""), false));
    w.setParameter( "MonitorRunNumbers",
                    new IntArrayPG( "Monitor Run numbers",
                    ((IntArrayPG)(w.getParameter( "RunNumbers" )))
                    .getStringValue(), false));
    w.setParameter( "RunList",
                    new ArrayPG( "Run List", new Vector(), false));
    w.setParameter( "MonitorRunList",
                    new ArrayPG( "Monitor Run List", new Vector(), false));

    //for TimeFocusForm
    w.setParameter( "TimeFocusResults",
                    new ArrayPG( "Time Focus Results", new Vector(), false));
    w.setParameter("FocusIDs",
                   new StringPG("List of IDs to focus", new String(""), false));
    w.setParameter("NewAngle",
                   new FloatPG("New angle in degrees", new Float(30.0f), false));
    w.setParameter("NewFPath",
                   new FloatPG("New final path (m)", new Float(4.0f), false));
    w.setParameter("MakeNewds",
                   new BooleanPG("Make new DataSet?", new Boolean(true), false));

    //for GroupingForm
    w.setParameter("GroupStr",
                   new StringPG("List of group IDs of the spectra",
                   new String("1:50"), false));
    w.setParameter("NewGroupID",
                   new IntegerPG("Group ID of the new datablock",
                   new Integer(1), false));
    w.setParameter( "GroupingResults",
                    new ArrayPG( "Grouping Results", new Vector(), false));

    //for SaveAsGSASForm
    w.setParameter("GSASDir",
                   new DataDirPG( "Directory to save to",
                   new String(""), false));
    w.setParameter("GSASName",
                   new StringPG("File name to save to",
                   new String("GSAS"), false));
    w.setParameter( "ExportMon",
                    new BooleanPG("Export monitor DataSet?",
                    new Boolean(false), false));
    w.setParameter( "SeqNum",
                    new BooleanPG("Use sequential bank numbering?",
                    new Boolean(false), false));
/*
    //for LoadMonitorDSForm
    w.setParameter( "MonitorRunNumbers",
                    new IntArrayPG( "Enter run numbers",
                    new String("12358"), false));
    w.setParameter( "MonitorDataDir",
                    new DataDirPG( "Enter directory",new String(""), false));
    w.setParameter( "MonitorInstName",
                    new InstNamePG( "Enter instrument name",
                    new InstrumentNameString("GPPD"), false));
    w.setParameter( "MonitorRunList",
                    new ArrayPG( "Monitor Run List", new Vector(), false));*/

                                                    // Specify the parameters
                                                    // used by the forms and
                                                    // add the forms to the
                                                    // Wizard

    //open files form
    String edit_parms0[] = {"RunNumbers", "DataDir", "InstName", "HNum", "GMask"};
    String result_parms0[] = {"RunList", "MonitorRunList"};
    Form form0 = new CreateMultiFilesForm(  edit_parms0, result_parms0, w );
/*
    //load monitor DS form
    String edit_parms1[] = {"MonitorRunNumbers", "MonitorDataDir", "MonitorInstName"};
    String result_parms1[] = {"MonitorRunList"};
    Form form1 = new LoadMonitorDSForm(  edit_parms1, result_parms1, w );*/

    //time focus form
    String const_parms2[] = {"RunList"};
    String edit_parms2[] = {"FocusIDs", "NewAngle", "NewFPath", "MakeNewds"};
    String result_parms2[] = {"TimeFocusResults"};
    Form form2 = new TimeFocusForm(  const_parms2, edit_parms2, result_parms2, w );

    //grouping form
    String const_parms3[] = {"TimeFocusResults"};
    String edit_parms3[] = {"GroupStr", "NewGroupID"};
    String result_parms3[] = {"GroupingResults"};
    Form form3 = new GroupingForm(  const_parms3, edit_parms3, result_parms3, w );

    //save as GSAS form
    String const_parms4[] = {"GroupingResults"};
    String edit_parms4[] = {"GSASDir", "GSASName", "ExportMon", "SeqNum"};
    Form form4 = new SaveAsGSASForm( const_parms4, edit_parms4, w );

    //add the forms
    w.add( form0 );   //CreateMultiFilesForm
    //w.add( form1 );   //LoadMonitorDSForm
    w.add( form2 );   //TimeFocusForm
    w.add( form3 );   //GroupingForm
    w.add( form4 );   //SaveAsGSASForm


    w.show(0);
  }

}
