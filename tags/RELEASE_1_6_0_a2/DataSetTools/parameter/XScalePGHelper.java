/*
 * File:  XScalePGHelper.java
 *
 * Copyright (C) 2003 Chris Bouzek
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
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/09/09 00:31:56  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.parameter;

import DataSetTools.dataset.XScale;

import java.util.Vector;


/**
 * This is a singleton class designed to help out with the "split" hierarchy of
 * the IXScalePGs.  It should be left as default access level unless there is
 * a good reason to use it outside the package.
 */
class XScalePGHelper {
  //~ Methods ******************************************************************

  /**
   * Converts an XScale into a Vector.
   *
   * @param scale The XScale to convert.
   *
   * @return The Vector values from the internal XScale.
   */
  public static Vector convertXScaleToVector( XScale scale ) {
    if( scale == null ) {
      return new Vector(  );
    }

    Vector temp = new Vector( 50, 10 );

    float[] elems = scale.getXs(  );

    for( int i = 0; i < elems.length; i++ ) {
      temp.add( new Float( elems[i] ) );
    }

    elems = null;

    return temp;
  }
}
