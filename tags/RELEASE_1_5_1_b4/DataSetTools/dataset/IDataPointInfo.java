/*
 * File:  IDataPointInfo.java
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 * Revision 1.2  2002/11/27 23:14:06  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/07/31 15:57:26  dennis
 * Interface that can be implmenented by an operator
 * display information in a viewer's XAxisConversionsTable
 *
 *
 */

package DataSetTools.dataset;

/**
  * This interface describes methods of objects providing information about 
  * data at a particular x value in a particular Data block maintained by an 
  * object implementing this interface.
  *
  *  @see DataSetTools.operator.DataSet.Information.DS_Information 
  *  @see DataSetTools.components.ui.DataSetXConversionsTable
  *  @see DataSetTools.operator.DataSet.Conversion.XAxis.DiffractometerTofToD
  */

public interface IDataPointInfo
{

  /* ------------------------- PointInfoLabel --------------------------- */
  /**
   * Get String label for information about a point on the xaxis.
   *
   *  @param  x    the x-value for which the axis label is to be obtained.
   *  @param  i    the index of the Data block that will be used for obtaining
   *               the label.
   *
   *  @return  String describing the information provided by X_Info().
   */
  public String PointInfoLabel( float x, int i );


  /* -------------------------- PointInfo ---------------------------- */
  /**
   * Get the information for the specified point and Data block.
   *
   *  @param  x    the x-value for which the axis information is to be obtained.
   *  @param  i    the index of the Data block that will be used for obtaining
   *               the information about the x axis.
   *
   *  @return  information for the x axis at the specified x.
   */
  public String PointInfo( float x, int i );

}
