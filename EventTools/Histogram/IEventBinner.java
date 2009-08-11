/* 
 * File: IEventBinner.java
 *
 * Copyright (C) 2008, Dennis Mikkelson
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
 *  This interface describes methods for mapping between points where events
 *  occur in an interval, [a,b), and the index, i, of a specific  subinterval
 *  [ai,bi), where the event occurred.  The subintervals, [ai,bi), are 
 *  assumed to be non-overlapping and to exactly cover the interval [a,b).
 *  Methods are also included to get the end points of the half-open interval
 *  [a,b) where the events are assumed to lie, the number of half-open 
 *  subintervals [ai,bi) into which the full interval is divided, and the 
 *  center and end points of each subinterval.  NOTE: Since the interval [a,b)
 *  does not include the right hand end point, b MUST BE STRICTLY GREATER 
 *  THAN all points that are to be mapped to valid index. 
 */
public interface IEventBinner
{
  /**
   * Get the left hand end point, a, of the half open interval, [a,b), 
   * where an event may occur along this axis.
   * 
   * @return  The left hand end point, a.
   */
  double axisMin();
  
  
  /**
   * Get the right hand end point, b, of the half open interval, [a,b), 
   * where an event may occur along this axis
   * 
   * @return  The right hand end point, b.
   */
  double axisMax();
  
  
  /**
   * Get the number of disjoint subintervals [ai,bi) used to cover [a,b)
   * 
   * @return the number of subintervals. 
   */
  int numBins();

  
  /**
   * Get the index, i, of the subinterval [ai,bi) that contains the specified
   * value along this axis.  
   * NOTE: If the specified value is outside of the half-open interval, then
   * the returned index may not be valid.  That is, the returned value will
   * only be in the range 0,...,(num_bins-1) for values in [min,max).
   * 
   * @param val  The value in [a,b) to categorize.  
   * 
   * @return  The index of the subinterval containing the specified value. 
   *           If val is less than the minimum value, a, covered by this 
   *           IEventBinner then this method must return a negative index.
   *           If val is more than OR EQUAL to the maximum value, b, then
   *           this method must return a value greater than or equal to 
   *           the number of bins. 
   */
  int index( double val );
  
  
  /**
   * Get the value at the center of the subinterval with the specified index.
   * NOTE: The returned value will only be valid if the index is at least 
   * zero and less than the number of bins.
   * 
   * @param index The index, i, of the subinterval [ai,bi).
   * 
   * @return The center of the bin with the specified index, if the index
   *         is valid.
   */
  double centerVal( int index );
  
  
  /**
   * Get the value at the left side of the subinterval with the specified index.
   * NOTE: The returned value will only be valid if the index is at least 
   * zero and less than the number of bins.
   * 
   * @param index The index, i, of the subinterval [ai,bi).
   * 
   * @return The left endpoint, ai, of the bin with the specified index, if 
   *         the index is valid.
   */
  double minVal( int index );

  
  /**
   * Get the value at the left side of the subinterval with the specified index.
   * NOTE: The returned value will only be valid if the index is at least 
   * zero and less than the number of bins.
   * 
   * @param index The index, i, of the subinterval [ai,bi).
   * 
   * @return The left endpoint, ai, of the bin with the specified index, if 
   *         the index is valid.
   */
  double maxVal( int index );


  /**
   * Get the fractional value along the subinterval corresponding to the 
   * specified fractional index.
   * NOTE: The returned value will only be valid if the index is at least 
   * zero and less than the number of bins.
   * 
   * @param fractional_index The fractional index of a point part way 
   *                         through a bin.  If i is the floor and
   *                         x the fractional part above the floor, the
   *                         the point in [ai,bi) that that is "x" of the 
   *                         way from ai to bi will be returned.
   * 
   * @return The point part way along the bin specified by the given
   *         fractional index.
   */
  double Val( double fractional_index );

}
