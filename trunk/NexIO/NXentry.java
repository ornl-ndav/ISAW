/*
 * File:  NXentry.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.4  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/11/20 16:14:40  pfpeterson
 * reformating
 *
 * Revision 1.2  2002/04/01 20:18:47  rmikk
 * Changed an argument to the ProcessDS method from the DataSet index to the NxNode of the first NXdata
 *
 */
package NexIO;

import DataSetTools.dataset.*;

/** Interface that all processors of NXentry info should implement<P>
 *NOTE: There is only ONE implementer
 */
public interface NXentry{

  public String getErrorMessage();
 
  /**
   * index parameter is there in case that there is more than one
   * NXdata per NXentry
   */
  public boolean processDS(DataSet DS, NxNode Mon_DB);
                            
  //public void setNxData(NxData nd);
}
