/*
 * File:  PGActionListener.java
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
 *
 * $Log$
 * Revision 1.2  2003/06/23 16:27:02  bouzekc
 * Added javadoc class description.
 *
 * Revision 1.1  2003/06/23 14:45:04  bouzekc
 * Added to CVS.
 *
 */

package DataSetTools.util;

import DataSetTools.parameter.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

/**
 *
 *  Class designed for ParameterGUIs which need internal ActionListeners.  
 */
public class PGActionListener implements ActionListener{
  VectorPG vpf;
  public PGActionListener( VectorPG vpg){
    vpf = vpg;
  }

  public void actionPerformed( ActionEvent evt ){ 
    (new JOptionPane()).showMessageDialog(null,"Result="+
      StringUtil.toString(vpf.getValue()));
  }
}    
