/*
 * File:  LookAndFeelManager.java
 *
 * Copyright (C) 2003 Chris M. Bouzek
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
 *           Chris M. Bouzek <coldfusion78@yahoo.com>
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
 * Revision 1.1  2003/06/30 14:21:08  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.util;

import javax.swing.*;


/**
 *  This class is designed as a utility to allow quick setting of Java's
 *  Pluggable Look and Feel for an application.  It calls UIManager's
 *  setLookAndFeel() method to do this.
 */
public class LookAndFeelManager {
  /**
   *  Sets the look and feel based on the various platforms.  Tries to
   *  determine the platform it is running on beforehand.  Note that if it is
   *  running on a Linux platform, the Motif Look and Feel is selected.
   */
  public static void setLookAndFeel(  ) {
    String strPLAF = LookAndFeelManager.getLookAndFeelClassName(  );

    //try to set the Look and Feel.  If it is not found, quietly drop the
    //exceptions on the floor.  We will always have a default look and feel -
    //the Metal Look and Feel
    try {
      UIManager.setLookAndFeel( strPLAF );
    } catch( ClassNotFoundException cnfe ) {}
     catch( InstantiationException ie ) {}
     catch( IllegalAccessException iae ) {}
     catch( UnsupportedLookAndFeelException ulafe ) {}
  }

  /**
   *  Determine the operating system.  This information is used to build the
   *  GUI with the appropriate Look and Feel.
   *
   *  @return      A String displaying the Look and Feel class name for the
   *               system.
   */
  private static String getLookAndFeelClassName(  ) {
    final String WIN_ID = "windows";
    final String LIN_ID = "linux";
    final String SUN_ID = "sunos";
    final String MAC_ID = "mac";

    String strPLAF;
    String opSystem = System.getProperty( "os.name" );

    opSystem = opSystem.trim(  );

    if( opSystem != null ) {
      //clear any spaces
      int index = opSystem.indexOf( " " );

      if( index > 0 ) {
        opSystem = opSystem.substring( 0, index );
      }

      opSystem = opSystem.toLowerCase(  );

      if( opSystem.startsWith( WIN_ID ) ) {
        strPLAF = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
      } else if( opSystem.startsWith( LIN_ID ) ) {
        strPLAF = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
      } else if( opSystem.startsWith( SUN_ID ) ) {
        strPLAF = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
      } else if( opSystem.startsWith( MAC_ID ) ) {
        strPLAF = "it.unitn.ing.swing.plaf.macos.MacOSLookAndFeel";
      } else {
        strPLAF = "javax.swing.plaf.metal.MetalLookAndFeel";
      }
    } else {
      strPLAF = "javax.swing.plaf.metal.MetalLookAndFeel";
    }

    return strPLAF;
  }
}
