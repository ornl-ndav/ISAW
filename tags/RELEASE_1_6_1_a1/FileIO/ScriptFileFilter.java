/*
 * File:  ScriptFileFilter.java
 *
 * Copyright (C)  2003 Thomas G. Worlton
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
 * Contact : Thomas G. Worlton <tworlton@anl.gov>
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
 * Revision 1.2  2003/12/15 23:53:56  bouzekc
 * Removed unused imports.
 *
 * Revision 1.1  2003/12/10 19:10:27  bouzekc
 * Added to CVS.
 *
 */
package FileIO;


/**
 * Class to filter out script files.  Filters out .bat, .com, .iss, .py,
 * .pyw files.
 */
public class ScriptFileFilter extends DataSetTools.util.RobustFileFilter{
  /**
   *  Default constructor.  Calls the super constructor,
   *  sets the description, and sets the file extensions.
   */
  public ScriptFileFilter() {
    super();
    super.setDescription("Script (*.iss)");
    super.addExtension(".iss");
    super.addExtension(".bat");
    super.addExtension(".com");
    super.addExtension(".py");
    super.addExtension(".pyw");
  }
}
