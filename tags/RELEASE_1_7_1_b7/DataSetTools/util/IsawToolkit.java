/*
 * File:  IsawToolkit.java
 *
 * Copyright (C) 2004, Chris M. Bouzek
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
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA and by
 * the National Science Foundation under grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2004/02/10 05:28:07  bouzekc
 *  Added to CVS.
 *
 */
package DataSetTools.util;

import java.awt.*;


/**
 * @author Chris Bouzek Replacement for Toolkit to help out with GUI stuff and
 *         dealing with those annoying beeps.
 */
public class IsawToolkit {
  //~ Constructors *************************************************************

  /**
   * Static method only class.  Do not instantiate.
   */
  private IsawToolkit(  ) {}

  //~ Methods ******************************************************************

  /**
   * Calls Toolkit.getDefaultToolkit().beep() if BEEP=true in IsawProps.dat.
   * Does nothing otherwise, although a "visual beep" would be cool.
   */
  public static void beep(  ) {
    Boolean beep = SharedData.getBooleanProperty( "BEEP" );

    //default to beeping
    if( ( beep == null ) || ( beep == Boolean.TRUE ) ) {
      Toolkit.getDefaultToolkit(  ).beep(  );
    }
  }
}
