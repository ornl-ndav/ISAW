/*
 * File:  SCDPeaksWizard.java
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
 *  This class constructs a Wizard used for finding peaks.
 */
public class SCDPeaksWizard extends Wizard
{
  /**
   *
   *  Default constructor.  Sets standalone in Wizard to true.
   */
  public SCDPeaksWizard()
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
  public SCDPeaksWizard(boolean standalone)
  {
    super("SCD Peaks Wizard", standalone);
    this.createAllForms();
  }

  /**
   *  Adds and coordinates the necessary Forms for this Wizard.
   */
  private void createAllForms()
  {
    int fpi[][] = { {0,-1,-1,-1,-1,0}, 
                    {1,-1,-1,-1,-1,1}, 
                    {2,-1,-1,-1,-1,2}, 
                    {3,-1,-1,-1,-1,3}, 
                    {7,-1,-1,-1,-1,5}, 
                    {8,0,0,-1,0,-1},
                    {-1,2,1,0,4,8},
                    {-1,-1,2,-1,1,-1}};

    FindMultiplePeaksForm peaksform = new FindMultiplePeaksForm();

    OperatorForm blindjform = new OperatorForm(new BlindJ());
    OperatorForm indexjform = new OperatorForm(new IndexJ());
    OperatorForm scalarjform = new OperatorForm(new ScalarJ());
    OperatorForm lsqrsjform = new OperatorForm(new LsqrsJ());

    IntegrateMultiRunsForm integmultiform = new IntegrateMultiRunsForm();
    //constant params
    integmultiform.setHasConstants(true);
    integmultiform.setDefaultParameters();

    //link the raw data path
    integmultiform.setParameter(peaksform.getParameter(fpi[0][0]), fpi[0][5]);

    //link the output path
    integmultiform.setParameter(peaksform.getParameter(fpi[1][0]), fpi[1][5]);

    //link the run numbers
    integmultiform.setParameter(peaksform.getParameter(fpi[2][0]), fpi[2][5]);

    //link the experiment name
    integmultiform.setParameter(peaksform.getParameter(fpi[3][0]), fpi[3][5]);

    //link the SCD calibration file
    integmultiform.setParameter(peaksform.getParameter(fpi[4][0]), fpi[4][5]);

    //link the peaks file
    blindjform.setParameter(peaksform.getParameter(fpi[5][0]), fpi[5][1]);
    indexjform.setParameter(peaksform.getParameter(fpi[5][0]), fpi[5][2]);
    lsqrsjform.setParameter(peaksform.getParameter(fpi[5][0]), fpi[5][4]);

    //link the matrix file name
    indexjform.setParameter(blindjform.getParameter(fpi[6][1]), fpi[6][2]);
    scalarjform.setParameter(blindjform.getParameter(fpi[6][1]), fpi[6][3]);
    lsqrsjform.setParameter(blindjform.getParameter(fpi[6][1]), fpi[6][4]);
    integmultiform.setParameter(blindjform.getParameter(fpi[6][1]), fpi[6][5]);

    //link the run restriction parameter
    lsqrsjform.setParameter(indexjform.getParameter(fpi[7][2]), fpi[7][4]);

    this.addForm(peaksform);
    this.addForm(blindjform );
    this.addForm(indexjform );
    this.addForm(scalarjform );
    this.addForm(lsqrsjform );
    this.addForm(integmultiform );
  }

  /**
   *  Method for running the Time Focus Group wizard 
   *   as standalone.
   */
  public static void main(String args[])
  {
    SCDPeaksWizard w = new SCDPeaksWizard(true);
    w.showForm(0);
  }
}
