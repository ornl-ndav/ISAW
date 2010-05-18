/*
 * File:  TestFunction.java
 *
 * Copyright (C) 2004 Dennis Mikkelson
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2007/08/14 00:29:07  dennis
 *  Adding files from SSG_Tools at UW-Stout.
 *
 *  Revision 1.1  2005/12/07 03:41:16  dennis
 *  Moved this generally useful class from "BallWorld" demo.
 *
 *  Revision 1.2  2004/12/08 04:14:20  dennis
 *  Finished java doc comments.
 *
 *  Revision 1.1  2004/12/04 17:35:04  dennis
 *  Initial Version, added to CVS.
 *
 */
package SSG_Tools.MathTools;

/**
 *  This class represents a simple quadratic function used to test the
 *  bisection method in MathUtil.
 */
public class TestFunction implements IOneVariableFunction
{
  /**
   *  Calculate the value of this function at the specified x value.
   *
   *  @param  x    The point at which the function is evaluated.
   *
   *  @return  The value of the function at the specified point.
   */
  public double f( double x )
  {
     return x*x + x - 6;
  }

}
