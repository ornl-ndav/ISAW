/*
 * File:  ResultLinkExampleWizard.java
 *
 * Copyright (C) 2003, Chris M. Bouzek
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
 *           Chris M. Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/06/26 18:06:02  bouzekc
 * Added to CVS.
 *
 *
 */
package Wizard.Examples;

import DataSetTools.parameter.IParameterGUI;

import DataSetTools.wizard.*;

import java.util.Vector;

import Operators.TOF_SCD.*;


/**
 *  This class has a main program that constructs a Wizard to show how
 *  OperatorForms can have their result parameters linked together.  As
 *  of 06/25/2003, it is not especially useful, although if you just wanted
 *  to run BlindJ and LsqrsJ, you could.  It does, however, need a peaks file
 *  to work with.
 */
public class ResultLinkExampleWizard extends Wizard {
  /**
   *
   *  Default constructor.  Sets standalone in Wizard to true.
   */
  public ResultLinkExampleWizard(  ) {
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
  public ResultLinkExampleWizard( boolean standalone ) {
    super( "Example Wizard for linking OperatorForm Parameters", standalone );
    this.createAllForms(  );
    StringBuffer s = new StringBuffer();
    s.append("This class has a main program that constructs a Wizard to show\n");
    s.append("how OperatorForms can have their result parameters linked\n");
    s.append("together.  As of 06/25/2003, it is not especially useful\n");
    s.append("for actual data reduction work, although if you just wanted\n");
    s.append("to run BlindJ and LsqrsJ, you could.  It does, however,\n");
    s.append("need a peaks file to work with.\n");
    this.setHelpMessage(s.toString());
  }

  /**
   *  Adds and coordinates the necessary Forms for this Wizard.
   *
   */
  private void createAllForms(  ) {
    //here is where we link all of our parameters.  Parameter 3 of BlindJ is
    //the matrix file, as is parameter 4 of LsqrsJ.  In addition, the peaks
    //file that they both need is parameter 0 for both Operators.
    int[][] fpi = {{ 0,0 },
                   { 3, 4}};

    //BlindJ puts out a matrix file.  We will link it to LsqrsJ's matrix file
    //parameter
    OperatorForm blind = new OperatorForm( new BlindJ()  );
    OperatorForm lsqrs = new OperatorForm( new LsqrsJ()  );

    this.addForm( blind );
    this.addForm( lsqrs );

    //linking the matrix file parameters together is problematic, as the name
    //of the result parameter from BlindJ is "Result".  This is obviously not
    //very useful to us, so we will use the name that is given to the matrix
    //file parameter in LsqrsJ.  That way, we have a meaningful name for both
    //parameters.  Note that anytime we link parameters they share:
    //1. Names
    //2. Types
    blind.getParameter(3).setName(lsqrs.getParameter(4).getName());

    //use Form's method to actually link the parameters
    super.linkFormParameters( fpi );
  }

  /**
   *  Method for running the Time Focus Group wizard
   *   as standalone.
   */
  public static void main( String[] args ) {
    ResultLinkExampleWizard w = new ResultLinkExampleWizard( true );

    w.showForm( 0 );
  }
}
