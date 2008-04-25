/* 
 * File: DisplayableUtil.java
 *  
 * Copyright (C) 2007     Dennis Mikkelson
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
 * Contact :  Dennis Mikkelson<mikkelsond@uwstout.edu>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *  Last Modified:
 *
 *  $Author$
 *  $Date$            
 *  $Revision$
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2007/07/18 15:30:16  dennis
 * Initial version of uitlity class for operators dealing
 * with Displayables.  For now this class has one static
 * method to get an instance of a Displayable, based on the
 * data type and requested view.
 *
 */

package Operators.DisplayDevices;

import DataSetTools.dataset.*;
import DataSetTools.viewer.DisplayDevices.*;

import gov.anl.ipns.DisplayDevices.*;
import gov.anl.ipns.ViewTools.Components.*;

/**
 * This class has a static method to get an instance of a
 * Displayable object to display DataSets or VirtualArrays
 */
public class DisplayableUtil
{

 /**
  * Get an instance of an IDisplayable object that shows the specified
  * data_object using the specified view_name.
  *
  * @param  data_object  The DataSet, IVirtualArrayList1D or IVirtualArray2D
  *                      object to be displayed.
  *
  * @param  view_name    The name of the viewer to be used when displaying
  *                      the data, such as "Image View" or
  *                      "Selected Graph View" for DataSets or
  *                      "Graph View" for IVirtualArrayList1D. 
  *
  * @return An IDisplayable object that can be sent to a DisplayDevice.
  *
  */
  public static IDisplayable getInstance( Object data_object, 
                                          String view_name  )
  {
    if ( data_object instanceof DataSet )
    {
      if ( view_name.equalsIgnoreCase( "Graph" ) )
        view_name = "Selected Graph View";
      else if ( view_name.equalsIgnoreCase( "Image" ) )
        view_name = "Image View";
      else if ( view_name.equalsIgnoreCase( "2D Viewer" ) )
        view_name = "2D Viewer";

      return new DataSetDisplayable( (DataSet)data_object, view_name );
    } 

    else if ( data_object instanceof IVirtualArrayList1D ) 
      return new VirtualArray1D_Displayable( (IVirtualArrayList1D)data_object,
                                              view_name );

    else if ( data_object instanceof IVirtualArray2D ) 
      return new VirtualArray2D_Displayable( (IVirtualArray2D)data_object,
                                              view_name );
    else
      throw new IllegalArgumentException("Need DataSet, IVirtualArrayList1D, "+
       " or IVirtualArray2D object to getInstance of Displayable");
  }

}
