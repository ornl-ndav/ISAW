/*
 * File:  NxData.java 
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
 * Revision 1.5  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/11/20 16:14:44  pfpeterson
 * reformating
 *
 * Revision 1.3  2002/02/26 15:42:11  rmikk
 * Added a set for the timeField field to put into the TimeField attribute.
 *    All NXdata are   merged.  To unmerge, extract with the TimefieldType
 *   attribute
 *
 */

package NexIO;
import DataSetTools.dataset.*;

/**
 * Interface for all processors of the NXdata part of the Nexus file
 * specification
 */

public interface NxData{
  
  /**
   * Fills out an existing DataSet with information from the NXdata
   * section of a Nexus datasource
   *
   * @param node the current node positioned to an NXdata part of a
   * datasource
   * @param DS the existing DataSet that is to be filled out
   *
   * @return error status: true if there is an error otherwise false
   */
  public boolean processDS( NxNode node , NxNode instrNode, DataSet DS );

  /**
   * Returns an error or warning message or "" if none
   */
  public String getErrorMessage();

  /**
   * When several NXdata's are in an NXentry, they can be tagged using
   * this method
   */
  public void setTimeFieldType( int t);
}
