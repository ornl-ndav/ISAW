
/*
 * File:  DataSetData.java
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.18  2004/04/16 20:27:23  millermi
 * - No longer implements IVirtualArrayList1D. The method
 *   convertToVirtualArray() converts a DataSet to an array
 *   to be displayed by view components.
 *
 * Revision 1.17  2004/03/15 03:27:26  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.16  2004/03/12 22:34:32  serumb
 * Now uses IVirtualArrayList1D.
 *
 * Revision 1.15  2004/03/12 19:17:46  serumb
 * Added setAxisInfo method.
 *
 * Revision 1.14  2004/01/22 02:08:18  bouzekc
 * Removed unused imports.
 *
 * Revision 1.13  2004/01/06 23:30:28  serumb
 * Added documentation.
 *
 * Revision 1.12  2003/12/18 22:42:12  millermi
 * - This file was involved in generalizing AxisInfo2D to
 *   AxisInfo. This change was made so that the AxisInfo
 *   class can be used for more than just 2D axes.
 *
 * Revision 1.11  2003/12/16 23:19:01  dennis
 * Removed commented out code that returned controls.
 *
 * Revision 1.10  2003/12/16 23:17:07  dennis
 * Removed methods to get controls.  Combined methods to set x and y
 * values into one method.
 *
 * Revision 1.9  2003/11/21 15:33:33  rmikk
 * Notified all listeners when a new data set is set
 *
 * Revision 1.8  2003/11/21 14:52:15  rmikk
 * Add GPL
 * Added the setDataSet method
 * Implemented ActionListeners
 *
 */

package DataSetTools.components.View;
import DataSetTools.dataset.*;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.OneD.*;

import java.awt.event.*;
import java.util.*;

/**
 * This class is used to take information from a data set,
 * and allow the information to be stored as a virtual array1D
 * so that the information is able to be viewed through the 
 * function view component.
 */ 
public class DataSetData
{
  private static VirtualArrayList1D vlist;
 /*
  * Constructor that prevents users from making an instance of DataSetData
  */
  private DataSetData()
  {
    ;
  }
    
 /**
  * Constructor that takes in a data set and sets the selected 
  * indexes.
  */
  public static VirtualArrayList1D convertToVirtualArray( DataSet ds )
  {
    DataArray1D temp;
    Vector data_array = new Vector();
    Data tempdata;
    String title;
    boolean pointedAt = false;
    AxisInfo xInfo;
    AxisInfo yInfo;
    int pointed_at_index = 0;
    
    for( int i = 0; i < ds.getNum_entries(); i++ )
    {
      if( ds.getPointedAtIndex() == i )
      {
        pointedAt = true;
	pointed_at_index = i;
      }
      else
        pointedAt = false;
      tempdata = ds.getData_entry(i);
      title = Integer.toString(tempdata.getGroup_ID());
      temp = new DataArray1D( tempdata.getX_values(), tempdata.getY_values(),
                              tempdata.getErrors(), title,
			      tempdata.isSelected(), pointedAt );
      data_array.add( temp );
    }
    vlist = new VirtualArrayList1D(data_array);
    vlist.setTitle( ds.getTitle() );
    xInfo = vlist.getAxisInfo(AxisInfo.X_AXIS);
    yInfo = vlist.getAxisInfo(AxisInfo.Y_AXIS);
    vlist.setAxisInfo(AxisInfo.X_AXIS, xInfo.getMin(), xInfo.getMax(),
                      ds.getX_label(),ds.getX_units(),
		      AxisInfo.LINEAR ); 
    vlist.setAxisInfo(AxisInfo.Y_AXIS, yInfo.getMin(), yInfo.getMax(),
                      ds.getY_label(),ds.getY_units(),
		      AxisInfo.LINEAR ); 
    return vlist;
  }
}
