/* 
 * File: ScanHistogram3D.java
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

import gov.anl.ipns.Operator.*;

/**
 * This IOperator accesses all entries in a range of "pages" and calculates
 * the min, max and total of the histogram values in those pages.
 * NOTE: Currently the Histogram3D updates the min, max and total count
 * whenever events are added to it, so this Operator should not be needed.
 * However, it can provide a useful check on the histogram state, and may
 * be needed in the future if some Histogram3D operations do not keep the
 * min, max and total up to date.
 */

public class ScanHistogram3D implements IOperator
{
  private int first_page;
  private int last_page;
  private float[][][] array;

  /**
   * Construct an operator to scan the specified range of pages in 
   * the specified histogram array.
   *  
   * @param array      The 3D array which is to be scanned.
   * @param first_page The first page of the portion of the 3D histogram 
   *                   that this operator will scan.
   * @param last_page  The last page of the portion of the 3D histogram 
   *                   that this operator will scan.
   */
  public ScanHistogram3D( float[][][] array, int first_page, int last_page )
  {
    this.first_page = first_page;
    this.last_page  = last_page;
    this.array      = array;
  }


  /**
   * Step through the specified portion of this histogram array and find
   * the min, max and total of the bins.
   * 
   * @return a Vector containing three Doubles, the sum of the events added 
   * and the updated min and max values in that order.
   */
  public Object getResult()
  {
    double sum = 0;
    float  min = array[0][0][0];
    float  max = array[0][0][0];

    float[][] one_page;
    float[]   one_row;

    float val;
    int   n_cols = array[0][0].length;
    int   n_rows = array[0].length;

    for ( int page = first_page; page <= last_page; page++ )
    {
      one_page = array[page];
      for ( int row = 0; row < n_rows; row++ )
      {
        one_row = one_page[row];
        for ( int col = 0; col < n_cols; col++ )
        {
           val = one_row[col];
           sum += val;
           if ( val > max )
             max = val;
           else if ( val < min )
             min = val;
        }
      }
    }

    Vector result = new Vector(2);
    result.add( new Double( sum ) );
    result.add( new Double( min ) );
    result.add( new Double( max ) );

    return result;
  }
}

