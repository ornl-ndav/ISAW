/*
 * File: IArrayMaker_DataSet.java
 *
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Revision 1.3  2003/11/06 21:25:09  rmikk
 * Introduces a new method necessary for Selecting
 *    DataSets in a table
 *
 * Revision 1.2  2003/10/28 19:57:28  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.1  2003/10/27 15:05:08  rmikk
 * Initial Checkin
 *
 */


package DataSetTools.viewer;
import java.awt.event.*;
import javax.swing.*;
import DataSetTools.components.View.Menu.*;



/**
*    This interface contains methods and fields necessary for Generators.
*    DataSetViewerMaker to use a VirtualArray  where information for the
*     DataSetXConversionsTable is necessary
*/

public interface IArrayMaker_DataSet extends IArrayMaker{

  
  /**
  *    Get the DataSet Group corresponding to the given Selected Data
  *    @param  Info  Should be a SelectedData2D Object
  */
  public int getGroupIndex( ISelectedData Info);

  /**
  *    Returns the time corresponding to the given Selected Data
  *    @param  Info  Should be a SelectedData2D Object
  */
  public float getTime( ISelectedData Info);


  /**
  *    Returns the selected data corresponding to the give PointedAt
  *    condition
  *    @param PointedAtGroupIndex   The index in the DataSet of the group
  *             that is being pointed at
  *    @param PointedAtTime The time in question when the pointing takes
  *          place
  *    @return  a SelectedData2D containing the row and column corresponding
  *              to the selected condition
  */
  public ISelectedData getSelectedData( int PointedAtGroupIndex,
                                         float PointedAtTime);
  
  public void SelectRegion( ISelectedRegion region);
}
