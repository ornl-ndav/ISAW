/*
 * File: NeutronDataFileFilter.java
 *
 * Copyright (C) 2001, Kevin Neff
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
 * Revision 1.12  2003/08/28 18:54:31  dennis
 * Added support for .csd files (concatenated files from the
 * Ideas MC simulation program)
 *
 * Revision 1.11  2003/06/13 22:01:15  bouzekc
 * Now extends RobustFileFilter to take care of common
 * functionality.  Removed setExtension() method and
 * SaveFilter variable.
 *
 * Revision 1.10  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 * Revision 1.9  2002/08/06 21:30:37  pfpeterson
 * Gsas files are no longer accepted by this filter.
 *
 * Revision 1.8  2002/06/18 19:48:19  rmikk
 * Added file types xmi, xmn and zip. xml replaced
 *
 * Revision 1.7  2002/06/18 18:57:18  rmikk
 * Added filters for xmi and xmn files. XML files, either
 *   ISAW or Nexus forms
 *
 * Revision 1.6  2002/01/08 21:26:21  rmikk
 * Fixed the display of the filter to only show xm.,gsas and
 * isd output and input.
 *
 */

package IsawGUI;

import DataSetTools.util.RobustFileFilter;
import java.util.Vector;

/**
 * filters neutron data file types.
 */
public class NeutronDataFileFilter extends RobustFileFilter
{
  /**
   *  Default constructor.  Calls the super constructor,
   *  sets the description, and sets the file extensions.
   */
  public NeutronDataFileFilter()
  {
    super();

    //trying to speed things up by creating a Vector of the exact size and
    //setting it directly rather than adding one by one 
    Vector v = new Vector(8,2);
    super.setDescription("*.isd (Temporary), *.xmi (Isaw XML), *.zip");
    v.add(".isd");
    v.add(".xmi");
    v.add(".zip");
    v.add(".hdf");
    v.add(".nxs");
    v.add(".run");
    v.add(".xmn");
    v.add(".gsa");
    v.add(".csd");
    super.setExtensionList(v);
  }

  /** 
   *  Legacy constructor.  Any calls to this in ISAW should be removed, as the
   *  SaveFilter class variable is no longer used.  I have left it in for
   *  compatibility reasons, although it merely tosses the sFilter parameter
   *  and calls the default constructor.
   */
  public NeutronDataFileFilter(boolean sFilter)
  {
    this();
  }
} 

