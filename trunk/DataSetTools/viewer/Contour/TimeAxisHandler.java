/*
 * File:  TimeAxisHandler.java 
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 *  $Log$
 *  Revision 1.4  2003/10/15 03:43:13  bouzekc
 *  Fixed javadoc errors.
 *
 *  Revision 1.3  2002/11/27 23:24:30  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/10/14 19:53:49  rmikk
 *  Fixed the getIndex method to interpolate indecies
 *
 *  Revision 1.1  2002/08/30 15:20:47  rmikk
 *  Initial Checkin
 *
 *  Revision 1.2  2002/08/02 19:35:13  rmikk
 *  Fix to replace XScale features
 *
*/


package DataSetTools.viewer.Contour;
import DataSetTools.dataset.*;
import DataSetTools.math.*;

/** A Time axis Handler returns a time value given a Group index and "time" index.
*   The time index can also be determined given a time value and Group index. Note:
*   time should be increasing for a given Data block  
*/
public class TimeAxisHandler implements IAxisHandler
 {
  DataSet ds;
   
  public TimeAxisHandler( DataSet DS)
    {ds = DS;
    }
  public String getAxisName()
    {return "time";
    }

  public String getAxisUnits()
    {return ds.getX_units();
    }

  /** 
   * Gets the axis value for this Group and xvalue.
   *
   * <br>
   * NOTE: The y value can be gotten with getX( xIndex )
   */
  public float  getValue( int GroupIndex, int xIndex)
    { if( xIndex < 0)
        return 0;
      if(xIndex >= ds.getNum_entries())
        return 0;
      return ds.getData_entry( GroupIndex).getX_scale().getX( xIndex); 
     }
  /** Returns the xIndex that has the given axis value = Value for  
  *  this Group. Interpolated
  */
  public float  getXindex( int GroupIndex, float Value)
    { XScale xscl=ds.getData_entry( GroupIndex).getX_scale();
      int i= xscl.getI(Value);
      float x1,x2 = xscl.getX(i);
      if( i > 0)
         x1 = xscl.getX(i-1);
      else
         return x2;
     
      return  (float)(i - 1+(Value - x1)/(x2-x1));
    }

  /**Returns the Maximum possible value for this axis and GroupIndex
  * 
  */
  public float getMaxAxisValue(int GroupIndex)
   { Data D = ds.getData_entry( GroupIndex);
     return D.getX_scale().getEnd_x();
   }

  /**Returns the Minimum possible value for this axis and GroupIndex
  * 
  */
  public float getMinAxisValue(int GroupIndex)
   { Data D = ds.getData_entry( GroupIndex);
     return D.getX_scale().getStart_x();
    }

  /** Sets the XScale
  */
  public void setXScale( int GroupIndex , XScale xscale)
    {
    }
  /** Determines the XScale being used to determine values for this axis
  */
  public XScale getXScale(int GroupIndex)
   { return ds.getData_entry( GroupIndex).getX_scale();
   }

  }

