/*  
 * File:  IusesStatusPane.java   
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
 *           Menomonie, WI. 54751  
 *           USA  
 *  
 * This work was supported by the Intense Pulsed Neutron Source Division  
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.  
 *  
 * For further information, see <http://www.pns.anl.gov/ISAW/>  
 *  
 * Modified:  
 *  
 * $Log$
 * Revision 1.1  2002/01/10 16:02:30  rmikk
 * Initial Checkin
 * 
*/
package Command;

/** Implementers of this interface can accept a StatusPane to which
*  Values can be reported or can be cleared
*
*NOTE: DataSetTools.util.SharedData.status_pane is the "Global" StatusPane.
*      
*/
public interface IusesStatusPane
{
 /** Adds one Status Pane to the Object.
 * Currently the Object can only have one StatusPane. Setting it to
 * null should eliminate writing to this status pane
 */
 public void addStatusPane( StatusPane sp );
}
