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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * Modified: 
 *
 * $Log$
 * Revision 1.5  2003/03/19 15:08:33  pfpeterson
 * Uses the TimeFocusGroupForm rather than TimeFocusForm and GroupingForm.
 * (Chris Bouzek)
 *
 * Revision 1.4  2003/03/13 19:00:52  dennis
 * Added $Log$
 * Added Revision 1.5  2003/03/19 15:08:33  pfpeterson
 * Added Uses the TimeFocusGroupForm rather than TimeFocusForm and GroupingForm.
 * Added (Chris Bouzek)
 * Added comment to include revision information.
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
    s.append("focusing IDs focus all spectra in the DataSets.");
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
    w.setParameter( "RunList",
                    new ArrayPG( "Run List", new Vector(), false));
    w.setParameter( "MonitorRunList",
                    new ArrayPG( "Monitor Run List", new Vector(), false));

    //for TimeFocusGroupForm
    w.setParameter( "TimeFocusGroupResults",
                    new ArrayPG( "Time Focus Results", new Vector(), false));
    w.setParameter("FocusIDs",
                   new StringPG("List of group IDs to focus", new String("1:50"), false));
    w.setParameter("NewAngle",
                   new FloatPG("New angle in degrees", new Float(30.0f), false));
    w.setParameter("NewFPath",
                   new FloatPG("New final path (m)", new Float(4.0f), false));

    //for SaveAsGSASForm
    w.setParameter("GSASDir",
                   new DataDirPG( "Directory to save to",
                   new String(""), false));
    w.setParameter( "ExportMon",
                    new BooleanPG("Export monitor DataSet?",
                    new Boolean(false), false));
    w.setParameter( "SeqNum",
                    new BooleanPG("Use sequential bank numbering?",
                    new Boolean(false), false));

                                                    // Specify the parameters
                                                    // used by the forms and
                                                    // add the forms to the
                                                    // Wizard

    //open files form
    String edit_parms0[] = {"RunNumbers", "DataDir", "InstName", "HNum", "GMask"};
    String result_parms0[] = {"RunList", "MonitorRunList"};
    Form form0 = new CreateMultiFilesForm(  edit_parms0, result_parms0, w );

    //time focus and group form
    String const_parms1[] = {"RunList"};
    String edit_parms1[] = {"FocusIDs", "NewAngle", "NewFPath"};
    String result_parms1[] = {"TimeFocusGroupResults"};
    Form form1 = new TimeFocusGroupForm(  const_parms1, edit_parms1, result_parms1, w );

    //save as GSAS form
    String const_parms2[] = {"TimeFocusGroupResults", "MonitorRunList", "RunNumbers", "InstName"};
    String edit_parms2[] = {"GSASDir", "ExportMon", "SeqNum"};
    Form form2 = new SaveAsGSASForm( const_parms2, edit_parms2, w );

    //add the forms
    w.add( form0 );   //CreateMultiFilesForm
    w.add( form1 );   //TimeFocusGroupForm
    w.add( form2 );   //SaveAsGSASForm
    w.show(0);
  }

}
