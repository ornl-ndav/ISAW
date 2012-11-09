/* 
 * File: EventInfo.java
 *
 * Copyright (C) 2012, Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author: dennis $
 *  $Date: 2012/11/09 15:40:55 $            
 *  $Revision: 1.2 $
 */

package EventTools.Integrate;

import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This is an immutable class that holds basic data about a single event.
 */
public class EventInfo 
{
  public static float Q_scale = (float)( 1.0 / (2.0 * Math.PI ) );
  float qx, 
        qy, 
        qz;
  int   det_id, 
        row, 
        col;
  float tof,
        lamda;

/**
 * Construct an EventInfo object from a sequence of eight values in a 
 * list of events as returned by SNS_Tof_to_Q_map.MapEventsTo_Q_ID_Row_Col().
 *
 * @param list        array containing the event information in groups of 
 *                    eight successive floats
 * @param start_index the index in the list[] where the information for the
 *                    desired event begins.
 */
  public EventInfo( float[] list, int start_index )
  {
    if ( start_index < 0 || start_index > list.length-8 )
      throw new IllegalArgumentException("List to short for start index, " +
                                         "list.length = " + list.length +
                                         " start_index = " + start_index );
    qx     = list[start_index++];
    qy     = list[start_index++];
    qz     = list[start_index++];

    det_id = Math.round( list[start_index++] );
    row    = Math.round( list[start_index++] );
    col    = Math.round( list[start_index++] );

    tof    = list[start_index++];
    lamda  = list[start_index];
  }


/**
 *  Get the 3D Q vector for this event, in lab coordinates, following the
 *  physics convention that |Q| = 2PI/d.
 */
  public Vector3D VecQ()
  {
    return new Vector3D( qx, qy, qz );
  }


/**
 *  Get the 3D Q vector for this event, in lab coordinates, following the
 *  crystallographic convention that |Q| = 1/d.
 */
  public Vector3D VecQ_over_2PI()
  {
    return new Vector3D( qx * Q_scale, qy * Q_scale, qz * Q_scale );
  }


/**
 *  Get the magnitude of Q for this event, following the
 *  physics convention that |Q| = 2PI/d.
 */
  public float MagQ()
  {
    return (float)( Math.sqrt( qx*qx + qy*qy + qz*qz ) );
  }


/**
 *  Get the magnitude of Q for this event, following the
 *  crystallographic convention that |Q| = 1/d.
 */
  public float MagQ_over_2PI()
  {
    return (float)( Math.sqrt( qx*qx + qy*qy + qz*qz ) ) * Q_scale;
  }


/**
 *  Get the x component of the Q vector for this event, in lab coordinates,
 *  following the physics convention that |Q| = 2PI/d.
 */
  public float Qx()
  {
    return qx;
  }


/**
 *  Get the y component of the Q vector for this event, in lab coordinates,
 *  following the physics convention that |Q| = 2PI/d.
 */
  public float Qy()
  {
    return qy;
  }


/**
 *  Get the z component of the Q vector for this event, in lab coordinates,
 *  following the physics convention that |Q| = 2PI/d.
 */
  public float Qz()
  {
    return qz;
  }


/**
 *  Get the module ID for the detector where this event was measured.
 */
  public int ID()
  {
    return det_id;
  }
  

/**
 *  Get the row number of the pixel where this event was measured.
 */
  public int Row()
  {
    return row;
  }


/**
 *  Get the column number of the pixel where this event was measured.
 */
  public int Col()
  {
    return col;
  }


/**
 *  Get the time-of-flight at which this event was measured.
 */
  public float Tof()
  {
    return tof;
  }

  
/**
 *  Get the wavelength at which this event was measured.
 */
  public float Lamda()
  {
    return lamda;
  }


/**
 *  Get a string listing the Q vector, ID, row, col, tof and lambda for this
 *  event.
 */
  public String toString()
  {
    return String.format( "%6.3f  %6.3f  %6.3f, %3d  %3d  %3d  %8.1f  %6.4f",
                         Qx(), Qy(), Qz(), ID(), Row(), Col(), Tof(), Lamda());
  }
}
