/*
 * File:  HelloOperatorWizard.java
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
 *           Chris M. Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 */

package Wizard;

import DataSetTools.wizard.OperatorForm;
import DataSetTools.wizard.Wizard;
import Operators.HelloOperator;

/**
 *  This class constructs a Wizard for loading the HelloOperator.
 *  It is meant mainly as a demonstration for using the 
 *  adaptor class OperatorForm.
 */
public class HelloOperatorWizard extends Wizard
{
  /**
   *
   *  Default constructor.  Sets standalone in Wizard to true.
   */
  public HelloOperatorWizard()
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
  public HelloOperatorWizard(boolean standalone)
  {
    super("Hello Operator Wizard", standalone);
    this.createAllForms();
  }

  /**
   *  Adds and coordinates the necessary Forms for this Wizard.
   */
  private void createAllForms()
  {
    HelloOperator hop = new HelloOperator("Chris Bouzek");
    OperatorForm of = new OperatorForm(hop);
    this.addForm(of);
  }

  /**
   *  Method for running the HelloOperator wizard 
   *   as standalone.
   */
  public static void main(String args[])
  {
    HelloOperatorWizard w = new HelloOperatorWizard(true);
    w.wizardLoader( args );
  }
}
