/* 
 * File: getEventLists.java
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
package EventTools.Histogram.Operators;

import java.util.*;

import EventTools.EventList.FloatArrayEventList3D;
import EventTools.EventList.IEventList3D;
import EventTools.Histogram.IEventBinner;

import gov.anl.ipns.Operator.*;

/**
 * This IOperator extracts information from the specified 3D array in the form
 * of lists of events at bin centers, for bins whose count values are in
 * specified intervals.  Several objects of this class, covering different 
 * ranges of "pages" in the 3D array are run in separate threads by a method
 * in the Histogram3D class, to get events from the entire Histogram3D object.  
 */

public class GetEventLists implements IOperator
{
  private float[][][]  histogram;
  private int          first_page;
  private int          last_page;
  private IEventBinner binner;

  private IEventBinner x_binner;
  private IEventBinner y_binner;
  private IEventBinner z_binner;


  /**
   * Construct an operator to extract events from the specified range of
   * pages of the specified histogram array.  All events are assumed to 
   * occur at the bin centers.  A bin with a count of 10 will give an 
   * event with count 10 at the x,y,z values corresponding to the center of
   * the bin's column, row and page.  IEventBinner objects determine the 
   * mapping between page,row and column numbers and x,y,z coordinates.
   *  
   * @param histogram  The 3D array from which the events are extracted.
   * @param first_page The first page of the portion of the 3D histogram 
   *                   that this operator will use.
   * @param last_page  The last page of the portion of the 3D histogram 
   *                   that this operator will use.
   * @param x_binner   The IEventBinner that determines which X-coordinate
   *                   corresponds to a particular column of the array.
   * @param y_binner   The IEventBinner that determines which Y-coordinate
   *                   corresponds to a particular row of the array.
   * @param z_binner   The IEventBinner that determines which Z-coordinate
   *                   corresponds to a particular page of the array.
   * @param binner     This binner specifies which count values will be
   *                   returned as events when the getResult() method is
   *                   called.
   */
  public GetEventLists( float[][][]  histogram, 
                        int          first_page, 
                        int          last_page,
                        IEventBinner x_binner,
                        IEventBinner y_binner,
                        IEventBinner z_binner,
                        IEventBinner binner )
  {
    this.first_page = first_page;
    this.last_page  = last_page;
    this.histogram  = histogram;
    this.binner     = binner;
    this.x_binner   = x_binner;
    this.y_binner   = y_binner;
    this.z_binner   = z_binner;
  }


  /**
   *  getResult() returns lists of events from the specified range of 
   *  pages of the histogram array.  For each bin of the specified 
   *  IEventBinner, a (possibly empty) list of events will be returned
   *  with each event corresponding to a bin in the histogram with count
   *  value in the corresponding interval.  For example, if the specified
   *  binner split the interval [10,100) into 9 uniform bins, then 9 
   *  lists of events will be returned.  The first list of events will 
   *  have x,y,z values at bin centers, for bins with counts in the 
   *  interval [10,20).  The second list of events will have x,y,z values
   *  at bin centers, for bins with counts in the interval [20,30), etc.
   *
   *  @return A vector containing one IEventList3D objects for each 
   *          interval [ai,bi) of the given binner.
   *          NOTE: If there were no events in an interval [ai,bi) then
   *          the corresponding entry in the Vector will be null. 
   *          CAUTION: IT IS NECESSARY TO CHECK IF EACH RETURNED VECTOR 
   *                   ENTRY IS NULL.
   */
  public Object getResult()
  {
    int       n_bins    = binner.getNumBins();
    int[]     bin_count = new int[ n_bins ];
    
    int       index;
    float[][] one_page;
    float[]   one_row;

    int n_cols = histogram[0][0].length;
    int n_rows = histogram[0].length;

    for ( int page = first_page; page <= last_page; page++ )
    {
      one_page = histogram[page];
      for ( int row = 0; row < n_rows; row++ )
      {
        one_row = one_page[row];
        for ( int col = 0; col < n_cols; col++ )
        {
          index = binner.getIndex( one_row[col] );
          if ( index >= 0 && index < n_bins )
            bin_count[index]++;
        }
      }
    }

    int[][]   codes  = new int[n_bins][] ;
    float[][] x_vals = new float[n_bins][];
    float[][] y_vals = new float[n_bins][];
    float[][] z_vals = new float[n_bins][];

    Vector result = new Vector( n_bins );
    int n_events;
    for ( int i = 0; i < n_bins; i++ )
    {
      n_events = bin_count[i];
      if ( n_events > 0 )
      { 
        codes[i]  = new int[ n_events ];
        x_vals[i] = new float[ n_events ]; 
        y_vals[i] = new float[ n_events ]; 
        z_vals[i] = new float[ n_events ]; 
      }
    }

    int[] ilist = new int[n_bins];
    for ( int page = first_page; page <= last_page; page++ )
    {
      one_page = histogram[page];
      for ( int row = 0; row < n_rows; row++ )
      {
        one_row = one_page[row];
        for ( int col = 0; col < n_cols; col++ )
        {
          index = binner.getIndex( one_row[col] );
          if ( index >= 0 && index < n_bins )
          {
            codes[index] [ ilist[index] ] = index;
            x_vals[index][ ilist[index] ] = (float)(x_binner.getCenter(col));
            y_vals[index][ ilist[index] ] = (float)(y_binner.getCenter(row));
            z_vals[index][ ilist[index] ] = (float)(z_binner.getCenter(page));
            ilist[index]++;
          }
        }
      }
    }

    for ( int i = 0; i < n_bins; i++ )
    {
      n_events = bin_count[i];
      if ( n_events > 0 )
      {
        IEventList3D events = new
           FloatArrayEventList3D( codes[i], x_vals[i], y_vals[i], z_vals[i] );

        result.add( events );
      }
      else
        result.add( null );
    }

    return result;
  }
}

