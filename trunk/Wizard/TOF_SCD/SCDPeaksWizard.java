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
    int fpi[][] = { {0,-1,-1,-1,-1,-1,-1,0}, //raw data path 
                    {1,2,1,1,1,1,1,1},  //peaks file path
                    {2,0,0,0,0,0,0,2},  //run numbers
                    {3,1,2,4,4,2,2,3},  //experiment name
                    {7,-1,-1,-1,-1,-1,-1,5}};  //SCD calibration file 

    FindMultiplePeaksForm peaksform = new FindMultiplePeaksForm();

    BlindJForm blindjform = new BlindJForm();
    IndexJForm blindindexjform = new IndexJForm();
    IndexJForm lsqrsindexjform = new IndexJForm();
    ScalarJForm scalarjform = new ScalarJForm();
    LsqrsJForm lsqrsjform1 = new LsqrsJForm();
    LsqrsJForm lsqrsjform2 = new LsqrsJForm();

    IntegrateMultiRunsForm integmultiform = new IntegrateMultiRunsForm();

    //constant params
    integmultiform.setHasConstants(true);
    integmultiform.setDefaultParameters();

    blindjform.setHasConstants(true);
    blindjform.setDefaultParameters();

    scalarjform.setHasConstants(true);
    scalarjform.setDefaultParameters();

    blindindexjform.setHasConstants(true);
    blindindexjform.setDefaultParameters();
    
    lsqrsindexjform.setHasConstants(true);
    lsqrsindexjform.setDefaultParameters();

    /*The first time we run lsqrsjform, we want to allow user inputs
      for the transformation matrix.  Otherwise, we want to set it 
      to the identity matrix.*/
    lsqrsjform1.setFirstTime(true);
    lsqrsjform2.setFirstTime(false);

    lsqrsjform1.setHasConstants(true);
    lsqrsjform1.setDefaultParameters();

    lsqrsjform2.setHasConstants(true);
    lsqrsjform2.setDefaultParameters();

    /*The first time through, indexjform gets input from BlindJ.
      On successive runs, it gets it from LsqrsJ.*/
    blindindexjform.getParameter(5).setValue(IndexJForm.JBLIND);
    lsqrsindexjform.getParameter(5).setValue(IndexJForm.JLSQRS);


    //link the raw data path
    integmultiform.setParameter(peaksform.getParameter(fpi[0][0]), fpi[0][7]);

    //link the peaks path
    blindjform.setParameter(peaksform.getParameter(fpi[1][0]), fpi[1][1]);
    scalarjform.setParameter(peaksform.getParameter(fpi[1][0]), fpi[1][2]);
    blindindexjform.setParameter(peaksform.getParameter(fpi[1][0]), fpi[1][3]);
    lsqrsindexjform.setParameter(peaksform.getParameter(fpi[1][0]), fpi[1][4]);
    lsqrsjform1.setParameter(peaksform.getParameter(fpi[1][0]), fpi[1][5]);
    lsqrsjform2.setParameter(peaksform.getParameter(fpi[1][0]), fpi[1][6]);
    integmultiform.setParameter(peaksform.getParameter(fpi[1][0]), fpi[1][7]);

    //link the run numbers
    blindjform.setParameter(peaksform.getParameter(fpi[2][0]), fpi[2][1]);
    scalarjform.setParameter(peaksform.getParameter(fpi[2][0]), fpi[2][2]);
    blindindexjform.setParameter(peaksform.getParameter(fpi[2][0]), fpi[2][3]);
    lsqrsindexjform.setParameter(peaksform.getParameter(fpi[2][0]), fpi[2][4]);
    lsqrsjform1.setParameter(peaksform.getParameter(fpi[2][0]), fpi[2][5]);
    lsqrsjform2.setParameter(peaksform.getParameter(fpi[2][0]), fpi[2][6]);
    integmultiform.setParameter(peaksform.getParameter(fpi[2][0]), fpi[2][7]);

    //link the experiment name
    blindjform.setParameter(peaksform.getParameter(fpi[3][0]), fpi[3][1]);
    scalarjform.setParameter(peaksform.getParameter(fpi[3][0]), fpi[3][2]);
    blindindexjform.setParameter(peaksform.getParameter(fpi[3][0]), fpi[3][3]);
    lsqrsindexjform.setParameter(peaksform.getParameter(fpi[3][0]), fpi[3][4]);
    lsqrsjform1.setParameter(peaksform.getParameter(fpi[3][0]), fpi[3][5]);
    lsqrsjform2.setParameter(peaksform.getParameter(fpi[3][0]), fpi[3][6]);
    integmultiform.setParameter(peaksform.getParameter(fpi[3][0]), fpi[3][7]);

    //link the SCD calibration file
    integmultiform.setParameter(peaksform.getParameter(fpi[4][0]), fpi[4][7]);

    this.addForm(peaksform);
    this.addForm(blindjform );
    this.addForm(blindindexjform );
    this.addForm(scalarjform );
    this.addForm(lsqrsjform1 );  //first lsqrsjform
    this.addForm(lsqrsindexjform );
    this.addForm(lsqrsjform2 );  //second lsqrsjform
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
