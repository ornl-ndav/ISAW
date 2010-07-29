/* 
 * File: EventBinner.java
 *
 * Copyright (C) 2010, Dennis Mikkelson
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
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.Histogram;

/**
 * This is the abstract base class for classes that split a specified
 * interval [a,b) into a specified number contiguous bins [ai,bi). 
 * Methods are provided to map between a numeric value in [a,b) and the 
 * index, i, of the bin containing that value, and from an index i to the 
 * end points or center of the bin.  Derived classes implement this using
 * uniform or "log" bins.
 */
abstract public class EventBinner implements IEventBinner
{

  protected double min;
  protected double max;
  protected int    num_bins;


  @Override
  public double axisMin()
  {
    return min;
  }

  @Override
  public double axisMax()
  {
    return max;
  }

  @Override
  public int numBins()
  {
    return num_bins;
  }
  
  /**
   * Get a string specifying the min, max and number of steps used.
   */
  public String toString()
  {
    return String.format( "[ %7.2f, %7.2f ) : %4d ", min, max, num_bins );
  }

}
