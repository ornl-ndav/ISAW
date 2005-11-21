/*
 * File:  IXScalePG.java
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
 * This is a tagging interface to mark a ParameterGUI as an IXScale.  Derived
 * classes should be built upon the assumption that the internal value is
 * always an XScale, although it is certainly possible that the classes could
 * take a Vector or an XScale as a value for the constructor or setValue(). In
 * addition, take care to be sure that getValue() returns a Vector in the
 * implemented classes.
 */
public interface IXScalePG {
  //~ Methods ******************************************************************

  /**
   * Fast access method to return a Vector.
   *
   * @return The typecast Vector object.
   */
  public Vector getVectorValue(  );

  /**
   * Convenience method to access the inner XScale object.
   *
   * @return The inner XScale object.
   */
  public XScale getXScaleValue(  );
}
