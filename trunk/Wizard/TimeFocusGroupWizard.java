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
                    new IntArrayPG( "Enter run numbers",
                    new String("12358"), false));
    w.setParameter( "DataDir",
                    new DataDirPG( "Enter directory",new String(""), false));
    w.setParameter( "InstName",
                    new InstNamePG( "Enter instrument name",
                    new InstrumentNameString("GPPD"), false));

    //for TimeFocusForm
    w.setParameter( "RunList",
                    new ArrayPG( "Run List", new Vector(), false));
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
                                                    // Specify the parameters
                                                    // used by the forms and
                                                    // add the forms to the
                                                    // Wizard

    //open files form
    String edit_parms[] = {"RunNumbers", "DataDir", "InstName"};
    String result_parms[] = {"RunList"};
    Form form0 = new CreateMultiFilesForm(  edit_parms, result_parms, w );
    w.add( form0 );

    //time focus form
    String const_parms1[] = {"RunList"};
    String edit_parms1[] = {"FocusIDs", "NewAngle", "NewFPath", "MakeNewds"};
    String result_parms1[] = {"TimeFocusResults"};
    Form form1 = new TimeFocusForm(  const_parms1, edit_parms1, result_parms1, w );
    w.add( form1 );

    //grouping form
    String const_parms2[] = {"TimeFocusResults"};
    String edit_parms2[] = {"GroupStr", "NewGroupID"};
    String result_parms2[] = {"GroupingResults"};
    Form form2 = new GroupingForm(  const_parms2, edit_parms2, result_parms2, w );
    w.add( form2 );

    w.show(0);
  }

}
