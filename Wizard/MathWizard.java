/*
 * File:  MathWizard.java
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.7  2004/01/05 15:25:42  bouzekc
 * Removed unused imports.
 *
 * Revision 1.6  2003/11/30 02:32:14  bouzekc
 * Calls wizardLoader instead of showForm().
 *
 * Revision 1.5  2003/04/02 15:02:46  pfpeterson
 * Changed to reflect new heritage (Forms are Operators). (Chris Bouzek)
 *
 * Revision 1.4  2003/02/26 17:21:58  rmikk
 * Now writes to DataSetTools.util.SharedData.status_pane
 *
 * Revision 1.3  2002/11/27 23:31:16  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/06/06 16:21:37  pfpeterson
 * Now use new parameters.
 *
 * Revision 1.1  2002/05/28 20:35:13  pfpeterson
 * Moved files
 *
 * Revision 1.2  2002/03/12 16:10:33  pfpeterson
 * Updated to work better with disabling wizard feature.
 *
 * Revision 1.1  2002/02/27 17:33:02  dennis
 * Example Wizard that controls four forms for doing
 * +,-,*,/ operations.
 *
 *
 */

package Wizard;

import DataSetTools.wizard.*;

/**
 *  This class constructs a Wizard for doing add,
 *  subtract, multiply and divide operations on a specified list of parameters.
 */
public class MathWizard extends Wizard
{
  /**
   *
   *  Default constructor.  Sets standalone in Wizard to true.
   */
  public MathWizard()
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
  public MathWizard(boolean standalone)
  {
    super("Math Wizard", standalone);
    this.createAllForms();
  }

  /**
   *  Adds and coordinates the necessary Forms for this Wizard.
   *  Here is the breakdown of the referential links.
   *
   *  aef.Value1 = sef.Value1
   *  aef.Value2 = sef.Value2
   *  aef.Value1 = mef.Value1
   *  aef.Value2 = mef.Value2
   *  aef.Value3 = mef.Value3
   *  aef.Result1 = mef.Result1
   *  sef.Result2 = mef.Result2
   *  aef.Value2 = def.Value2
   *  sef.Result2 = def.Result2
   */
  private void createAllForms()
  {
    
    AdderExampleForm aef = new AdderExampleForm();
    SubtracterExampleForm sef = new SubtracterExampleForm();
    MultiplierExampleForm mef = new MultiplierExampleForm();
    DividerExampleForm def = new DividerExampleForm();

    sef.setParameter(aef.getParameter(0) ,0);
    sef.setParameter(aef.getParameter(1), 1);
    mef.setParameter(aef.getParameter(0), 0);
    mef.setParameter(aef.getParameter(1), 1);
    mef.setParameter(aef.getParameter(2), 2);
    mef.setParameter(aef.getParameter(3), 3);
    mef.setParameter(sef.getParameter(2), 4);
    def.setParameter(aef.getParameter(1), 0);
    def.setParameter(sef.getParameter(2), 1);
    this.addForm(aef);
    this.addForm(sef);
    this.addForm(mef);
    this.addForm(def);
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
    MathWizard w = new MathWizard(true);
    w.wizardLoader( args );
  }
}
