/* 
 * File: ClearPages.java
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

import gov.anl.ipns.Operator.*;

/**
 * This IOperator sets all entries to zero in a range of "pages" of the 
 * specified 3D array. Several objects of this class, covering different
 * ranges of "pages" in the 3D array are run in separate threads by a method 
 * in the Histogram3D class, to zero out all entries in a Histogram3D object.  
 */
public class ClearPages implements IOperator
{
  private int         first_page;
  private int         last_page;
  private float[][][] array;


  /**
   * Construct an operator to clear the specified range of pages in 
   * the specified histogram array.
   *  
   * @param array       The 3D array which is to be cleared.
   * @param first_page The first page of the portion of the 3D histogram 
   *                   that this operator will clear.
   * @param last_page  The last page of the portion of the 3D histogram 
   *                   that this operator will clear.
   */
  public ClearPages( float[][][] array, int first_page, int last_page )
  {
    this.first_page = first_page;
    this.last_page  = last_page;
    this.array      = array;
  }


  /**
   *  Set all entries in the specified range of pages to zero.
   *
   *  @return a Vector with one entry, the Double zero.
   */
  public Object getResult()
  {
    float[][] one_page;
    float[]   one_row;

    int n_cols = array[0][0].length;
    int n_rows = array[0].length;

    for ( int page = first_page; page <= last_page; page++ )
    {
      one_page = array[page];
      for ( int row = 0; row < n_rows; row++ )
      {
        one_row = one_page[row];
        for ( int col = 0; col < n_cols; col++ )
        {
          one_row[col] = 0;
        }
      }
    }
    return new Double( 0 );
  }
}
