/* 
 * File:  Formatter.java
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
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package Operators.TOF_Diffractometer;

import EventTools.EventList.*;


public class Formatter extends Thread
  {
    private int       first;
    private int       num;
    private String[]  string_list;
    private FloatArrayEventList3D q_list;

    public Formatter( int                   first,
                      int                   num,
                      String[]              string_list,
                      FloatArrayEventList3D q_list )
    {
      this.first       = first;
      this.num         = num;
      this.string_list = string_list;
      this.q_list      = q_list;
    }
    public void run()
    {
      for ( int i = first; i < first + num; i++ )
        string_list[i] = String.format( "%13.6e %13.6e %13.6e\n",
                      q_list.eventX(i), q_list.eventY(i), q_list.eventZ(i) );
    }
  }

