/*
 * File:  WizardFileFilter.java
 *
 * Copyright (C) 2003, Christopher M. Bouzek
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
 * Contact : Chris Bouzek <coldfusion78@yahoo.com>
 *           Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.3  2003/06/13 22:00:20  bouzekc
 * Now extends RobustFileFilter to take care of common
 * functionality.
 *
 * Revision 1.2  2003/06/13 16:27:51  bouzekc
 * Removed embedded tabs.  Added log message.
 *
 */
package DataSetTools.wizard.util;

import DataSetTools.util.RobustFileFilter;

/**
 * Filters out .wsf (Wizard Save File) files.
 */
public class WizardFileFilter extends RobustFileFilter
{
  /**
   *  Default constructor.  Calls the super constructor,
   *  sets the description, and sets the file extensions.
   */
  public WizardFileFilter()
  {
    super();
    super.setDescription("Wizard Save File (*.wsf)");
    super.addExtension(".wsf");
  }
}
