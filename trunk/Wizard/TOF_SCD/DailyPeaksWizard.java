/*
 * File:  DailyPeaksWizard.java
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
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.1  2003/06/10 21:06:49  bouzekc
 * Added to CVS.
 *
 *
 */

package Wizard.TOF_SCD;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import DataSetTools.wizard.*;
import DataSetTools.util.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import Operators.TOF_SCD.*;

/**
 *  This class constructs a Wizard used for initially finding peaks.  In this
 *  Wizard, an iteration over LsqrsJ and IndexJ is used for creating a matrix file.
 */
public class DailyPeaksWizard extends Wizard
{
  /**
   *
   *  Default constructor.  Sets standalone in Wizard to true.
   */
  public DailyPeaksWizard()
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
  public DailyPeaksWizard(boolean standalone)
  {
    super("Daily SCD Peaks Wizard", standalone);
    this.createAllForms();
  }

  /**
   *  Adds and coordinates the necessary Forms for this Wizard.
   */
  private void createAllForms()
  {
    int fpi[][] = { {0,-1,-1,0}, //raw data path 
                    {1,1,1,1},  //peaks file path
                    {2,0,0,2},  //run numbers
                    {3,4,2,3},  //experiment name
                    {8,-1,-1,5},  //SCD calibration file
                    {7,-1,-1,8}};  //SCD calibration file line

    FindMultiplePeaksForm peaksform = new FindMultiplePeaksForm();

    //these Forms rely on previously calculated values, so set them
    //up as having constants with "true"
    IndexJForm indexjform = new IndexJForm(true);
    LsqrsJForm lsqrsjform = new LsqrsJForm(true);
    IntegrateMultiRunsForm integmultiform = new IntegrateMultiRunsForm(true);

    //link the raw data path
    integmultiform.setParameter(peaksform.getParameter(fpi[0][0]), fpi[0][3]);

    //link the peaks path
    indexjform.setParameter(peaksform.getParameter(fpi[1][0]), fpi[1][1]);
    lsqrsjform.setParameter(peaksform.getParameter(fpi[1][0]), fpi[1][2]);
    integmultiform.setParameter(peaksform.getParameter(fpi[1][0]), fpi[1][3]);

    //link the run numbers
    indexjform.setParameter(peaksform.getParameter(fpi[2][0]), fpi[2][1]);
    lsqrsjform.setParameter(peaksform.getParameter(fpi[2][0]), fpi[2][2]);
    integmultiform.setParameter(peaksform.getParameter(fpi[2][0]), fpi[2][3]);

    //link the experiment name
    indexjform.setParameter(peaksform.getParameter(fpi[3][0]), fpi[3][1]);
    lsqrsjform.setParameter(peaksform.getParameter(fpi[3][0]), fpi[3][2]);
    integmultiform.setParameter(peaksform.getParameter(fpi[3][0]), fpi[3][3]);

    //link the SCD calibration file
    integmultiform.setParameter(peaksform.getParameter(fpi[4][0]),fpi[4][3]);

    //link the SCD calibration file line
    integmultiform.setParameter(peaksform.getParameter(fpi[5][0]),fpi[5][3]);

    this.addForm(peaksform);
    this.addForm(indexjform );
    this.addForm(lsqrsjform );  
    this.addForm(integmultiform );
  }

  /**
   *  Method for running the Time Focus Group wizard 
   *   as standalone.
   */
  public static void main(String args[])
  {
    DailyPeaksWizard w = new DailyPeaksWizard(true);
    w.showForm(0);
  }
}
