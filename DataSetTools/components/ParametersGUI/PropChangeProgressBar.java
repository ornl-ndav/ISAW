/*
 * File:  PropChangeProgressBar.java
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
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA and by 
 * the National Science Foundation under grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/06/16 23:07:23  bouzekc
 * Added to CVS.
 *
 */

package DataSetTools.components.ParametersGUI;

import java.beans.*;
import javax.swing.JProgressBar;
import java.lang.IllegalArgumentException;

/**
 *  This class is basically just a JProgressBar with PropertyChangeListener
 *  functionality added.  It is meant to be used with classes that need to fire
 *  property change events off to an external progress bar.  This class works
 *  with incremental values, and as such does not currently use old values.
 */
public class PropChangeProgressBar extends JProgressBar implements
                                                          PropertyChangeListener
{
  private int increment;

  //the name of the property associated with this component
  public static final String VALUE = "Percentage Done";

  public void propertyChange(PropertyChangeEvent pce) 
    throws IllegalArgumentException
  {
    if(pce.getPropertyName() != this.VALUE)
      return;

    Object obj = pce.getNewValue();


    //progress bars need numbers to deal with
    if(obj instanceof Integer)
    {
      increment = ((Integer)obj).intValue();
    System.out.println("The current value is: " + this.getValue());
    System.out.println("The current increment is: " + increment);

      this.setValue( this.getValue() + increment);
    }
    else
      throw new IllegalArgumentException(
        "PropChangeProgressBar cannot handle non-integer values.");
  }
}
