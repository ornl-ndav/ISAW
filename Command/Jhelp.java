/*
 * File:  Jhelp.java
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
 * Revision 1.1  2002/12/08 22:07:31  dennis
 * Utility class to obtain jhelp component for new help system.
 * Allows fallback to old help system if jhall.jar not present. (Ruth)
 *
 */

/**  This class gets the JHelp component that is added to a
 *   JPanel.  if the HelpSet system's jar file is absent, a
 *   ClassNotFound exception which can be caught will occur when 
 *   the class associated with the Help System is absent
 */
package Command;

import javax.swing.*;
import javax.help.*;
import IsawHelp.HelpSystem.*;

public  class Jhelp //throws ClassNotFoundException if help system class
                    //files are absent
    {
      public JComponent getHelpComponent()
       {
         return new JHelp( new IsawOpHelpSet(false) );
       }
    }
