/*
 * File:  AllInOneFileFilter.java
 *
 * Copyright (C) 2004 Dominic Kramer
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
 *           Dominic Kramer <kramerd@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * $Log$
 * Revision 1.1  2004/07/12 22:24:31  kramer
 * Filter which displays all of the file types that ISAW can understand.
 *
 */
package DataSetTools.operator.Generic;

import gov.anl.ipns.Util.File.RobustFileFilter;

/**
 * File filter for the all of the commonly used file types, 
 * they are:<br>
 * *.csd<br>
 * *.dat<br>
 * *.gda<br>
 * *.gsa<br>
 * *.hdf<br>
 * *.isd<br>
 * *.nxs<br>
 * *.raw<br>
 * *.run<br>
 * *.sdds<br>
 * *.xmi<br>
 * *.zip
 */
public class AllInOneFileFilter extends RobustFileFilter
{
   /**
    *  Default constructor.  Calls the super constructor,
    *  sets the description, and sets the file extensions.
    */
   public AllInOneFileFilter()
   {
      super();
      setDescription("All supported files");
      addExtension(".isd");
      addExtension(".xmi");
      addExtension(".zip");
      addExtension(".gsa");
      addExtension(".gda");
      addExtension(".dat");
      addExtension(".hdf");
      addExtension(".nxs");
      addExtension(".csd");
      addExtension(".sdds");
      addExtension(".raw");
      addExtension(".run");      
   }
}
