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
 * Revision 1.7  2003/04/02 15:02:46  pfpeterson
 * Changed to reflect new heritage (Forms are Operators). (Chris Bouzek)
 *
 * Revision 1.5  2003/03/19 15:08:33  pfpeterson
 * Uses the TimeFocusGroupForm rather than TimeFocusForm and GroupingForm.
 * (Chris Bouzek)
 *
 * Revision 1.4  2003/03/13 19:00:52  dennis
 * Added $Log$
 * Added Revision 1.7  2003/04/02 15:02:46  pfpeterson
 * Added Changed to reflect new heritage (Forms are Operators). (Chris Bouzek)
 * Added
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
public class TimeFocusGroupWizard extends Wizard
{
  /**
   *
   *  Default constructor.  Sets standalone in Wizard to true.
   */
  public TimeFocusGroupWizard()
  {
    this(true);
  }

  /**
   *  Constructor for setting the standalone variable in Wizard.
   *
   *  @param standalone          Boolean indicating whether the
   *                             Wizard stands alone (true) or
   *                             is contained in something else
   *                             (false).
   */
  public TimeFocusGroupWizard(boolean standalone)
  {
    super("Time Focus and Group Wizard", standalone);
    this.createAllForms();
  }

  /**
   *  Adds and coordinates the necessary Forms for this Wizard.
   *  Here is the breakdown of the referential links.
   *
   *  Loaded histograms:
   *  LoadMultiHistogramsForm[5] = TimeFocusGroupForm[0]
   *  
   *  Time focused results:
   *  TimeFocusGroupForm[number of banks * 3 + 1]
   *  = SaveAsGSASForm[0]
   *
   *  Monitor DataSets:
   *  LoadMultiHistogramsForm[8] = SaveAsGSASForm[1]
   *
   *  Run numbers:
   *  LoadMultiHistogramsForm[0] = SaveAsGSASForm[2]
   * 
   *  Instrument name:
   *  LoadMultiHistogramsForm[2] = SaveAsGSASForm[3]
   */
  private void createAllForms()
  {
    LoadMultiHistogramsForm lmhf = new LoadMultiHistogramsForm();
    TimeFocusGroupForm tfgf = new TimeFocusGroupForm();
    SaveAsGSASForm sagf = new SaveAsGSASForm();

    //pass the histograms from the load form to the 
    //time focusing and grouping.
    tfgf.setParameter(lmhf.getParameter(5) ,0);
    //pass the time focus results to SaveAsGSAS
    sagf.setParameter(tfgf.getParameter(61), 0);
    //pass the monitor DataSets to SaveAsGSAS
    sagf.setParameter(lmhf.getParameter(6), 1);
    //pass the run numbers to SaveAsGSAS
    sagf.setParameter(lmhf.getParameter(0), 2);
    //pass the instrument name to SaveAsGSAS
    sagf.setParameter(lmhf.getParameter(2), 3);
    this.addForm(lmhf);
    this.addForm(tfgf);
    this.addForm(sagf);
  }

  /*
   *
   *  Overridden methods.
   */
  public boolean  load()
  {
    return false;
  }
  public void save()
  {
  }
  public void close()
  {
    this.save();
    System.exit(0);
  }

  /**
   *  Method for running the Time Focus Group wizard 
   *   as standalone.
   */
  public static void main(String args[])
  {
    TimeFocusGroupWizard w = new TimeFocusGroupWizard(true);
    w.showForm(0);
  }
}
