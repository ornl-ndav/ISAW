/*
 * File:  SDDSFileFilter.java
 *
 * Copyright (C) 2002, Alok Chatterjee
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
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
 * $Log$
 * Revision 1.3  2003/06/13 21:59:41  bouzekc
 * Now extends RobustFileFilter to take care of common
 * functionality.
 *
 * Revision 1.2  2002/11/27 23:23:16  pfpeterson
 * standardized header
 *
 */
package DataSetTools.retriever ;

import DataSetTools.util.RobustFileFilter;

/**
 * FileFilter for SDDS files.
 */
public class SDDSFileFilter extends RobustFileFilter{
  /**
   *  Default constructor.  Calls the super constructor,
   *  sets the description, and sets the file extensions.
   */
  public SDDSFileFilter()
  {
    super();
    super.setDescription("SDDS files (*.sdds)");
    super.addExtension(".sdds");
  }
}
