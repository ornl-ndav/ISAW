/*
 * File:  TableViewModel.java 
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 * $Log$
 * Revision 1.2  2002/11/27 23:25:37  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/07/24 20:04:54  rmikk
 * Initial Checkin
 *
 */
package DataSetTools.viewer.Table;
import javax.swing.table.*;

/** Table Models that extend this class must have the routines below that translate
*   between a JTable's row and col and the Group and time
*/
public abstract class TableViewModel extends DefaultTableModel
                                     implements ITableViewModel
  {
    /** returns the group corresponding the the JTable entry at row, col
    */
    public abstract int getGroup( int row, int column);

    /** returns the time corresponding the the JTable entry at row, col
    */
    public abstract float getTime( int row, int column);

    /** returns the JTable row corresponding the the Given GroupINDEX and time
    */
    public abstract int getRow( int Group, float time);

    /** returns the JTable column corresponding the the Given GroupINDEX and time
    */
    public abstract int getCol( int Group, float time);
   }
