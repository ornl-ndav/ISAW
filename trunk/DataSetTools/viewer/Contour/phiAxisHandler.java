/*
 * File:  phiAxisHandler.java 
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
 *  Revision 1.2  2002/11/27 23:24:30  pfpeterson
 *  standardized header
 *
 *  Revision 1.1  2002/08/30 15:18:26  rmikk
 *  Initial Checkin
 *
 *  Revision 1.2  2002/08/02 19:35:13  rmikk
 *  Fix to replace XScale features
 *
*/


package DataSetTools.viewer.Contour;
import DataSetTools.dataset.*;
import DataSetTools.math.*;

/** A phi axis Handler returns a phi value given a Group index and "time" index.
*   The time index can also be determined given a phi value and Group index. Note:
*   phi should probably be contained in a continuous interval say -pi/2 to pi/2  
*/
public class phiAxisHandler  implements IAxisHandler
 {
  DataSet ds;
   
  public phiAxisHandler( DataSet DS)
    {ds = DS;
    }
  public String getAxisName()
    {return "phi";
    }

  public String getAxisUnits()
    {return "radians";
    }

  /** Gets the axis value for this Group and xvalue<P>
  * NOTE: The y value can be gotten with getX( xIndex )
  */
  public float  getValue( int GroupIndex, int xIndex)
    {// if( xIndex < 0)
      //  return 0;
     // if(xIndex >= ds.getNum_entries())
     //   return 0;
       return getMaxAxisValue( GroupIndex);   
     }
  /** Returns the xIndex that has the given axis value = Value for  
  *  this Group. Interpolated
  */
  public float  getXindex( int GroupIndex, float Value)
    { return -1; // all have same phi value
    }

  /**Returns the Maximum possible value for this axis and GroupIndex
  * 
  */
  public float getMaxAxisValue(int GroupIndex)
   { Data D = ds.getData_entry( GroupIndex);
      DetPosAttribute dp =( DetPosAttribute)(D.getAttribute( Attribute.DETECTOR_POS));
      if( dp == null)
        return Float.NaN;
       DetectorPosition p = dp.getDetectorPosition();
       float[] r = p.getSphericalCoords();
       return r[2];
   }

  /**Returns the Minimum possible value for this axis and GroupIndex
  * 
  */
  public float getMinAxisValue(int GroupIndex)
   {return getMaxAxisValue( GroupIndex );
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

