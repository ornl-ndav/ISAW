/*
 * File:  Wrappable.java
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
 * $Log$
 * Revision 1.2  2004/02/24 22:26:37  bouzekc
 * Now extends Serializable.
 *
 * Revision 1.1  2003/10/29 01:14:23  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.operator;

/**
 * This is an interface used to denote that a class is "wrappable" i.e. can be
 * sent in as an argument to JavaWrapperOperator.
 */
public interface Wrappable extends java.io.Serializable{
  //~ Methods ******************************************************************

  /**
   * @return The String command for this Wrappable Object.  Used mainly in ISS
   *         scripts.
   */
  public String getCommand(  );

  /**
   * @return The javadoc style documentation for this Wrappable.
   */
  public String getDocumentation(  );

  /**
   * Method used to perform calculations.  It is assumed that the public
   * instance variables have been set before calling this.
   */
  public Object calculate(  );
}
