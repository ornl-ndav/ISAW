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
 * Revision 1.4  2003/06/25 20:25:33  bouzekc
 * Unused private variables removed, reformatted for
 * consistency.
 *
 * Revision 1.3  2003/06/19 16:20:17  bouzekc
 * Now uses Wizard's linkFormParameters() to link the
 * parameters in the parameter table.
 *
 * Revision 1.2  2003/06/11 22:37:23  bouzekc
 * Added Wizard help message.  Fixed parameter linking to work
 * with updated Forms.
 *
 * Revision 1.1  2003/06/10 21:06:49  bouzekc
 * Added to CVS.
 *
 *
 */
package Wizard.TOF_SCD;

import DataSetTools.operator.*;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import DataSetTools.wizard.*;

import Operators.TOF_SCD.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;


/**
 *  This class constructs a Wizard used for initially finding peaks.  In this
 *  Wizard, an iteration over LsqrsJ and IndexJ is used for creating a matrix file.
 */
public class DailyPeaksWizard extends Wizard {
  /**
   *
   *  Default constructor.  Sets standalone in Wizard to true.
   */
  public DailyPeaksWizard(  ) {
    this( true );
  }

  /**
   *  Constructor for setting the standalone variable in Wizard.
   *
   *  @param standalone          Boolean indicating whether the
   *                             Wizard stands alone (true) or
   *                             is contained in something else
   *                             (false).
   */
  public DailyPeaksWizard( boolean standalone ) {
    super( "Daily SCD Peaks Wizard", standalone );
    this.createAllForms(  );

    StringBuffer s = new StringBuffer(  );

    s.append( "This Wizard is designed to be used as an daily\n" );
    s.append( "tool for finding peaks from SCD run files.  It\n" );
    s.append( "applies IndexJ and LsqrsJ in an iterative\n" );
    s.append( "refinement to the output .peaks file from the \n" );
    s.append( "first Form.\n" );
    this.setHelpMessage( s.toString(  ) );
  }

  /**
   *  Adds and coordinates the necessary Forms for this Wizard.
   */
  private void createAllForms(  ) {
    int[][] fpi = {
      { 0, -1, -1, 0 },  //raw data path 
      { 1, 1, 1, 1 },  //peaks file path
      { 2, 0, 0, 2 },  //run numbers
      { 3, 2, 2, 3 },  //experiment name
      { 8, -1, -1, 5 },  //SCD calibration file
      { 7, -1, -1, 8 }
    };  //SCD calibration file line

    FindMultiplePeaksForm peaksform = new FindMultiplePeaksForm(  );

    //these Forms rely on previously calculated values, so set them
    //up as having constants with "true"
    IndexJForm indexjform                 = new IndexJForm( true );
    LsqrsJForm lsqrsjform                 = new LsqrsJForm( true );
    IntegrateMultiRunsForm integmultiform = new IntegrateMultiRunsForm( true );

    this.addForm( peaksform );
    this.addForm( indexjform );
    this.addForm( lsqrsjform );
    this.addForm( integmultiform );

    super.linkFormParameters( fpi );
  }

  /**
   *  Method for running the Daily Peaks wizard
   *   as standalone.
   */
  public static void main( String[] args ) {
    DailyPeaksWizard w = new DailyPeaksWizard( true );

    w.showForm( 0 );
  }
}
