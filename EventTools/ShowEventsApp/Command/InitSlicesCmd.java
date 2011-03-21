/* 
 * File: InitSlicesCmd.java
 *
 * Copyright (C) 2011, Dennis Mikkelson
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
 *  $Author: $
 *  $Date: $            
 *  $Revision: $
 */

package EventTools.ShowEventsApp.Command;

import gov.anl.ipns.MathTools.Geometry.Vector3D;

/**
 * This class contains the command information about a region to be
 * histogrammed that is provided by the Select Slices panel in IsawEV.
 */
public class InitSlicesCmd
{
  boolean       use_weights;
  boolean       use_HKL;
  Vector3D      center_point;
  HistogramEdge edge_1;
  HistogramEdge edge_2;
  HistogramEdge edge_3;
  String        region_shape;

  public InitSlicesCmd( boolean        use_weights,
                        boolean        use_HKL,
                        Vector3D       center_point,
                        HistogramEdge  edge_1,
                        HistogramEdge  edge_2,
                        HistogramEdge  edge_3,
                        String         region_shape )
  {
    this.use_weights  = use_weights;
    this.use_HKL      = use_HKL;
    this.center_point = center_point;
    this.edge_1       = edge_1;
    this.edge_2       = edge_2;
    this.edge_3       = edge_3;
    this.region_shape = region_shape;
  }
                      

  public boolean useWeights()
  {
    return use_weights;
  }

  public boolean useHKL()
  {
    return use_HKL;
  }

  public Vector3D getCenterPoint()
  {
    return center_point;
  }

  public HistogramEdge getDirection_1()
  {
    return edge_1;
  }

  public HistogramEdge getDirection_2()
  {
    return edge_2;
  }

  public HistogramEdge getDirection_3()
  {
    return edge_3;
  }

  public String getShape()
  {
    return region_shape;
  }

  public String toString()
  {
    return "\nUse Weights:   "  + useWeights()  +
           "\nUse HKL:       "  + useHKL() +
           "\nCenter Point:  "  + getCenterPoint() +
           "\nDirection 1:   "  + getDirection_1() +
           "\nDirection 2:   "  + getDirection_2() +
           "\nDirection 3:   "  + getDirection_3() +
           "\nShape:         "  + getShape();
  }
             
}

