/*
 * File: SharedData.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.4  2001/08/16 19:34:07  dennis
 *  Temporarily added an instance of Dongfeng's SpectrometerPlotter, so that
 *  DataSets from an old server could be received properly.
 *
 *  Revision 1.3  2001/07/27 22:18:26  dennis
 *  Added public final string BUILD_DATE.
 *
 *  Revision 1.2  2001/07/26 19:52:53  dennis
 *  Removed build date and shared version number.
 *
 *  Revision 1.1  2001/07/23 19:04:20  dennis
 *  Utility class to intialize and/or contain data of which there
 *  should be only one copy and that will be shared by several
 *  packages.
 *
 */

package DataSetTools.util;

import DataSetTools.operator.*;

/**
 *  Objects of this class have one instance of objects that are to be shared
 *  by several packages.  The shared objects are instantiated one time 
 *  as static members of the class.
 */

public class SharedData implements java.io.Serializable 
{
 /**
  *  To guarantee that the IsawProps.dat file is loaded, construct an
  *  object of type SharedData.  eg: SharedData sd = new SharedData();
  */
  public static final PropertiesLoader isaw_props 
                                        = new PropertiesLoader("IsawProps.dat");
  
  public static final String BUILD_DATE = "Unknown_Build_Date";

  // This is here to allow DataSets from an old version of the servers to
  // be received by this version.  SpectromterPlotter is obsolete and should
  // be removed.
  public static final Operator old_op = new SpectrometerPlotter();
}
