/*
 * File:  InitialPeaksWizard.java
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
 * Revision 1.3  2003/06/19 16:20:32  bouzekc
 * Now uses Wizard's linkFormParameters() to link the
 * parameters in the parameter table.
 *
 * Revision 1.2  2003/06/11 22:44:31  bouzekc
 * Added Wizard help message.
 *
 * Revision 1.1  2003/06/10 21:06:15  bouzekc
 *
 * Added to CVS
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
 *  Wizard, BlindJ is used for creating a matrix file.
 */
public class InitialPeaksWizard extends Wizard
{

  private static final String LOADFILETYPE = "LoadFile";
  /**
   *
   *  Default constructor.  Sets standalone in Wizard to true.
   */
  public InitialPeaksWizard()
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
  public InitialPeaksWizard(boolean standalone)
  {
    super("Initial SCD Peaks Wizard", standalone);
    this.createAllForms();

    StringBuffer s = new StringBuffer();
    s.append("This Wizard is designed to be used as an initial\n");
    s.append("tool for finding peaks from SCD run files.  It\n");
    s.append("applies BlindJ, IndexJ, ScalarJ, and LsqrsJ\n");
    s.append("to the output .peaks file from the first Form.\n");
    this.setHelpMessage(s.toString());
  }

  /**
   *  Adds and coordinates the necessary Forms for this Wizard.
   */
  private void createAllForms()
  {
    int fpi[][] = { {9,0,0,-1,0}, //peaks file 
                    {-1,2,1,0,4}};  //matrix file

    FindMultiplePeaksForm peaksform = new FindMultiplePeaksForm();

    //the return types of all of these Operator Forms is LoadFilePG,
    //hence the "LoadFile"
    OperatorForm blindjform = new OperatorForm(new BlindJ(), LOADFILETYPE,
                                               "Matrix file");
    OperatorForm indexjform = new OperatorForm(new IndexJ(),LOADFILETYPE,
                                               "IndexJ log file");
    OperatorForm scalarjform = new OperatorForm(new ScalarJ(),LOADFILETYPE, 
                                                "ScalarJ log file");
    OperatorForm lsqrsjform = new OperatorForm(new LsqrsJ(), LOADFILETYPE,
                                               "LsqrsJ matrix file");

    this.addForm(peaksform);
    this.addForm(blindjform );
    this.addForm(indexjform );
    this.addForm(scalarjform );
    this.addForm(lsqrsjform );  

    super.linkFormParameters(fpi);
  }

  /**
   *  Method for running the Initial Peaks wizard 
   *   as standalone.
   */
  public static void main(String args[])
  {
    InitialPeaksWizard w = new InitialPeaksWizard(true);
    w.showForm(0);
  }
}
