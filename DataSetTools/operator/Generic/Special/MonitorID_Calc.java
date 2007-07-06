/*
 * File:  MonitorID_Calc.java
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2007/07/06 19:32:17  dennis
 *  This class contains methods to identify the up and down stream
 *  monitor IDs from a DataSet containing monitor Data.  This extracts
 *  and re-engineers the basic calculations of the UpstreamMonitorID
 *  and DownstreamMonitorID, so that they can be used directly from
 *  Java code, as static methods, without having to instantiate
 *  operators.
 *
 */

package DataSetTools.operator.Generic.Special;

import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.AttrUtil;
import gov.anl.ipns.MathTools.Geometry.DetectorPosition;

/**
 * This file contains static methods to identify the up and downstream
 * monitor IDs for a specified DataSet.  
 */


public class MonitorID_Calc
{

  /* ------------------------- UpstreamMonitorID -------------------------- */
  /**
   * This method searches through the specified DataSet for the upstream 
   * monitor data entry with the highest total count.  The DataSet should
   * contain only monitor Data, and the corresponding detector positions must
   * be along the beam.  Specifically, any group that is more than about
   * 2.5 degrees away from the beam, when viewed from the sample position
   * will NOT be considered to be a monitor.  An upstream monitor is assumed
   * to be before the sample, so its position is essentially 180 degrees
   * from the beam direction.
   *
   * @param  mon    The monitor DataSet.  This DataSet should only 
   *                contain beam monitor Data.
   *
   * @return an int with the Group ID of the histogram of the 
   *         upstream monitor with the largest TOTAL_COUNT attribute. 
   *         If no upstream monitor is found it will return -1.
   */
  public static int UpstreamMonitorID( DataSet mon )
  {
    int   mon_id   = -1;
    float monCount = -1f;

    for( int i = 0; i < mon.getNum_entries(); i++ )
    {
      Data monD = mon.getData_entry(i);
                                            // try to use RAW ANGLE in radians
                                            // and if not present try Detector 
                                            // Position
      float ang = AttrUtil.getRawAngle( monD );
      ang = (float)(ang * Math.PI/180.0);
      if ( Float.isNaN( ang ) )
      {
        DetectorPosition det_pos = AttrUtil.getDetectorPosition( monD );
        if ( det_pos == null )
          return -1;
        else
          ang = det_pos.getScatteringAngle();
      }
                                         // NOTE: cos(ang) < -0.999 iff
                                         // ang is within 2.562 degrees of 180 
      if( Math.cos(ang) < -0.999f )
      {
        float count = AttrUtil.getTotalCount( monD );
        if( count > monCount )
        {
          monCount = count;
          mon_id   = monD.getGroup_ID();
        }
      }
    }
    return mon_id;
  }


  /* ------------------------ DownstreamMonitorID ------------------------ */
  /**
   * This method searches through the specified DataSet for the downstream 
   * This operator sarches through the specified DataSet for the downstream 
   * monitor data entry with the highest total count.  The DataSet should
   * contain only monitor Data, and the corresponding detector positions must
   * be along the beam.  Specifically, any group that is more than about
   * 2.5 degrees away from the beam, when viewed from the sample position
   * will NOT be considered to be a monitor.  A downstream monitor is assumed
   * to be after the sample, so its position is essentially at 0 degrees
   * along the beam direction.
   *
   * @param  mon    The monitor DataSet.  This DataSet should only 
   *                contain beam monitor Data.
   *
   * @return an int with the Group ID of the histogram of the 
   *         downstream monitor with the largest TOTAL_COUNT attribute. 
   *         If no downstream monitor is found it will return -1.
   */
  public static int DownstreamMonitorID( DataSet mon )
  {
    int   mon_id   = -1;
    float monCount = -1f;

    for( int i = 0; i < mon.getNum_entries(); i++ )
    {
      Data monD = mon.getData_entry(i);
                                            // try to use RAW ANGLE in radians
                                            // and if not present try Detector 
                                            // Position
      float ang = AttrUtil.getRawAngle( monD );
      ang = (float)(ang * Math.PI/180.0);

      if ( Float.isNaN( ang ) )
      {
        DetectorPosition det_pos = AttrUtil.getDetectorPosition( monD );
        if ( det_pos == null )
          return -1;
        else
          ang = det_pos.getScatteringAngle(); 
      }
                                           // NOTE: cos(ang) > 0.999 iff
                                           // ang is within 2.562 degrees of 0
      if ( Math.cos(ang) > 0.999f )
      {
        float count = AttrUtil.getTotalCount( monD ); 
        if( count > monCount )
        {
          monCount = count;
          mon_id = monD.getGroup_ID();
        }
      }
    }
    return mon_id;
  }  

}
