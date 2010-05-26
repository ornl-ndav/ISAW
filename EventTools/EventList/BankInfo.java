/* 
 * File: BankInfo.java
 *
 * Copyright (C) 2010 Dennis Mikkelson
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

/**
 * This class is a simple "record" stucture for the size and range of NeXus
 * pixel ID information for one detector bank (i.e. module).  This object 
 * is immutable.
 */
public class BankInfo
{
  private int bank_id;
  private int n_cols;
  private int n_rows;
  private int first_id;
  private int last_id;

  public BankInfo( int[][] info, int index )
  {
    bank_id  = info[0][index];
    n_cols   = info[1][index];
    n_rows   = info[2][index];
    first_id = info[3][index];
    last_id  = info[4][index];
  }

  /**
   *  Get the bank ID (i.e. module ID) for this bank.
   *
   *  @return the bank ID.
   */
  public int ID()
  {
    return bank_id;
  }

  /**
   *  Get the number of columns in this detector bank.
   *
   *  @return the number of colums.
   */
  public int num_cols()
  {
    return n_cols;
  }

  /**
   *  Get the number of rows in this detector bank.
   *
   *  @return the number of rows.
   */
  public int num_rows()
  {
    return n_rows;
  }


  /**
   *  Get the first NeXus pixel ID used for this detector module.
   *  NOTE: The NeXus pixel ID may be different from the DAS pixel ID.
   *
   *  @return the first pixel ID in the bank.
   */
  public int first_NeXus_id()
  {
    return first_id;
  }


  /**
   *  Get the last NeXus pixel ID used for this detector module.
   *  NOTE: The NeXus pixel ID may be different from the DAS pixel ID.
   *
   *  @return the last pixel ID in the bank.
   */
  public int last_NeXus_id()
  {
    return last_id;
  }

}
