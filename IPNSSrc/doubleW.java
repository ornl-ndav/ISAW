/*
 * File:  doubleW.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 * This class redefines the doubleW from the f2java compiler.
 *   See blind.java and subs.java for a description of the f2java compiler.

 * Modified:
 *
 * $Log$
 * Revision 1.2  2003/02/18 19:33:50  dennis
 * Removed ^M characters.
 *
 * Revision 1.1  2003/01/20 16:20:58  rmikk
 * Initial Checkin
 *
*/
package IPNSSrc;

public class doubleW
 { double val;
   public doubleW( double x)
    { val = x;
     }

   public doubleW( float x)
    { val = x;
     }

   public doubleW( int x)
    { val = x;
     }

   public doubleW( doubleW x)
    { val = x.val;
     }
  }
