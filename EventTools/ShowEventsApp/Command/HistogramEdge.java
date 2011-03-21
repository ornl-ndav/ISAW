/* 
 * File: HistogramEdge.java
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
 * This class wraps the information about an edge of a 3D histogram that
 * is needed in an InitSlicesCmd object.
 */
public class HistogramEdge 
{
  private  Vector3D  edge_direction;
  private  double    step_size;
  private  int       num_bins;

  /**
   *  Construct a HistogramEdge object to hold the specified information
   *  @param  edge_direction  Vector3D giving the direction of the edge
   *  @param  step_size       double specifying the width of each histogram
   *                          bin in the spedified direction
   *  @param  num_binss       The number of bins to use in the specified
   *                          direction
   */ 
  public HistogramEdge( Vector3D edge_direction,
                        double   step_size,
                        int      num_bins )
  {
    this.edge_direction = new Vector3D( edge_direction );
    this.step_size      = step_size;
    this.num_bins       = num_bins;
  }

  /**
   *  @return a Vector3D object containing the direction vector
   */
  public Vector3D getDirection()
  {
    return new Vector3D( edge_direction );
  }

  /**
   *  @return a double with the step size
   */
  public double getStepSize()
  {
    return step_size;
  }

  /**
   *  @return an int with the number of bins to use 
   */
  public int getNumBins()
  {
    return num_bins;
  }

  public String toString()
  {
    return "\nDirection: " + getDirection()  +
           "\nStepSize:  " + getStepSize()   +
           "\nNum Bins:  " + getNumBins();
 }
             
}
