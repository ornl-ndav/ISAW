/*
 * File:  ActivateWizard.java
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.6  2003/10/18 21:33:23  bouzekc
 * Removed call to setAboutMessage().
 *
 * Revision 1.5  2003/04/02 15:02:46  pfpeterson
 * Changed to reflect new heritage (Forms are Operators). (Chris Bouzek)
 *
 * Revision 1.4  2003/02/26 17:21:37  rmikk
 * Now writes to DataSetTools.util.SharedData.status_pane
 *
 * Revision 1.3  2002/11/27 23:31:16  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/06/06 16:21:34  pfpeterson
 * Now use new parameters.
 *
 * Revision 1.1  2002/05/28 20:35:09  pfpeterson
 * Moved files
 *
 *
 */

package Wizard;

import DataSetTools.wizard.*;
import DataSetTools.parameter.*;

/**
 *  This wizard calculates the contact dose, prompt activation, 
 *  and storage time for the specified sample.
 */
public class ActivateWizard extends Wizard{
  /**
   *
   *  Default constructor.  Sets standalone in Wizard to true.
   */
  public ActivateWizard()
  {
    this(true);
    super.setHelpMessage(
    "This wizard calculates the contact dose, prompt activation, "
    + "and storage time for a sample"); 
  }

  /**
   *  Constructor for setting the standalone variable in Wizard.
   *
   *  @param standalone          Boolean indicating whether the
   *                             Wizard stands alone (true) or
   *                             is contained in something else
   *                             (false).
   */
  public ActivateWizard(boolean standalone)
  {
    super("Activate Wizard", standalone);
    super.addForm(new ActivateForm());
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
    ActivateWizard w = new ActivateWizard(true);
    w.showForm(0);
  }
}
