
/*
 * File: SiteHelp .java 
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
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2003/02/19 17:17:38  rmikk
 * Changed file:/// to file:// so applet is found under linux
 *
 * Revision 1.1  2003/01/27 15:03:21  rmikk
 * Initial Checkin -The JMenuItem added to the IsawGUI
 *
 */
package IsawHelp;
import javax.swing.*;
import java.awt.event.*;
import IsawGUI.*;

/** This class creates a JMenuItem that "completely" takes care of displaying
*   the introductory help for ISAW
*/
public class SiteHelp  extends JMenuItem
  {
   /** Create the JMenuItem for displaying the introductory help on ISAW
   */
   public SiteHelp()
     {
      super( "Intro to ISAW");
      addActionListener( new MyActionListener( ));
    
     }

   /** ActionListener that is invoked when the above JMenuItem is selected
   */
   static class MyActionListener implements ActionListener
     { 
   
      /** This method is invoked when a SiteHelp JMenuItem is selected.  It
      *   searches for the IsawMain.htm file, then sends the correct file to
      *   the BrowserControl, which pops up a navigator
      */
      public void actionPerformed( ActionEvent evt)
        { 
         String S = DataSetTools.util.FilenameUtil.helpDir( "IsawMain.htm");
         if( S == null)
           return;
         if( S.startsWith("file:///"))
            S = "file://"+S.substring(8);
         
         BrowserControl bc = new BrowserControl();
         bc.displayURL( S);
        }
     }

  }
