/* 
 * File: IEventList3D.java
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

package EventTools.EventList;

import EventTools.Histogram.IEventBinner;

/**
 * This interface specifies methods that must be implemented by objects that
 * represent a list of events in 3-dimensional real space.  Each event
 * is associated with a particular location (x,y,z) and has an associated 
 * weight.  The weight is a scale factor to be applied to the event and
 * reflects any scaling that must be applied to the event value.  
 */
public interface IEventList3D 
{
  /**
   * Get the min and max X-coordinates of the events in this event list.
   * 
   * @return  An IEventBinner object whose min is the minimum X-value and
   *          whose max is SLIGHTLY LARGER than the maximum X-value of the 
   *          events in this list.  Thus the min and max values determine a
   *          half open interval [x_min, x_max) that contains all the X-values.
   *          The number of bins used by the IEventBinner may be one, if
   *          the events have not been mapped to a discrete subset of possible
   *          positions.  If the events have been mapped to a discrete set
   *          of X-values, say by using byte or short values to code their 
   *          positions, then the number of bins will indicate this, and the
   *          IEventBinner will provide the correspondence between bin number
   *          and X-coordinate in the interval [x_min,x_max).  
   */
  IEventBinner xExtent();
  

  /**
   * Get the min and max Y-coordinates of the events in this event list.
   * 
   * @return  An IEventBinner object whose min is the minimum Y-value and
   *          whose max is SLIGHTLY LARGER than the maximum Y-value of the 
   *          events in this list.  Thus the min and max values determine a
   *          half open interval [y_min, y_max) that contains all the Y-values.
   *          The number of bins used by the IEventBinner may be one, if
   *          the events have not been mapped to a discrete subset of possible
   *          positions.  If the events have been mapped to a discrete set
   *          of Y-values, say by using byte or short values to code their 
   *          positions, then the number of bins will indicate this, and the
   *          IEventBinner will provide the correspondence between bin number
   *          and Y-coordinate in the interval [y_min,y_max).  
   */
  IEventBinner yExtent();


  /**
   * Get the min and max Z-coordinates of the events in this event list.
   * 
   * @return  An IEventBinner object whose min is the minimum Z-value and
   *          whose max is SLIGHTLY LARGER than the maximum Z-value of the 
   *          events in this list.  Thus the min and max values determine a
   *          half open interval [z_min, z_max) that contains all the Z-values.
   *          The number of bins used by the IEventBinner may be one, if
   *          the events have not been mapped to a discrete subset of possible
   *          positions.  If the events have been mapped to a discrete set
   *          of Z-values, say by using byte or short values to code their 
   *          positions, then the number of bins will indicate this, and the
   *          IEventBinner will provide the correspondence between bin number
   *          and Z-coordinate in the interval [z_min,z_max).  
   */
  IEventBinner zExtent();


  /**
   * Get the number of entries in this event list.  NOTE: If the weights 
   * represents multiple events occurring at the same locations, the value
   * returned will NOT account for this.  
   * 
   * @return  an integer given the number of entries in this event list.
   */
  int numEntries();

  
  /**
   * Get the weight for the specified entry in this event list.
   * 
   * @param index   The index of the event list entry
   * 
   * @return  A float giving the weight for the specified entry.
   */
  float eventWeight( int index ); 

  
  /**
   * Get a list of all of the event weights for this event list.  The ith
   * entry in the array of event weights is the weight for the ith event.
   * 
   * @return  A float array giving the event weights for all of the
   * events in this list, in order.
   */
  float[] eventWeights(); 
  

  /**
   *  Set the list of event weights for the events in this list.  
   *
   *  @param weights  the new list of event weights for the events.
   */
  public void setEventWeights( float[] weights );
  

  /**
   * Get the x,y,z values of the specified event list entry.
   * 
   * @param index  The index of the event list entry.
   * @param values Any array of at least three doubles, that will be filled
   *               out with the x,y and z values in positions 0,1 and 2 
   *               respectively.
   */
  void eventVals( int index, double[] values ); 

  
  /**
   * Get all of the events in this event list, in the form of a 
   * one-dimensional array containing interleaved events.  Specifically,
   * the first three entries will be the x, y and z coordinates of the
   * first event, the next three, the x, y and z coordinates of the second
   * event, etc.  In general the x,y,z coordinates of the ith event are
   * at positions 3*i, 3*i+1 and 3*i+2.
   * NOTE: For efficiency, this may return a reference to an internal array
   * of events, so the elements of this array should NOT be altered by 
   * the calling code.
   * 
   * @return a list of the x,y,z coordinates for all of the events.
   */
  float[] eventVals();
  
  
  /**
   * Get the X-coordinate for the specified entry in this event list.
   * 
   * @param index   The index of the event list entry
   * 
   * @return  An double giving the X-cooordinate for the specified entry.
   */
  double eventX( int index ); 
  
  
  /**
   * Get the Y-coordinate for the specified entry in this event list.
   * 
   * @param index   The index of the event list entry
   * 
   * @return  An double giving the Y-cooordinate for the specified entry.
   */
  double eventY( int index ); 
  
  
  /**
   * Get the Z-coordinate for the specified entry in this event list.
   * 
   * @param index   The index of the event list entry
   * 
   * @return  An double giving the Z-cooordinate for the specified entry.
   */
  double eventZ( int index ); 
  
}
