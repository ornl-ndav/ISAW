/*
 * File: showDocumentTest.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.3  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 */

package IsawGUI;
import java.applet.*;
import java.net.*;
import java.awt.*;

 public class showDocumentTest extends Applet {

   URL jamsa = null;

   public void init()
     {
       try {
           jamsa = new URL("http://www.jamsa.com");
         } 
       catch (MalformedURLException e)
         {
           System.out.println("Error:" + e.getMessage());
         }
     }

   public boolean mouseDown(Event evt, int x, int y)
     {
       // when user clicks, go to Jamsa Press Home page
       getAppletContext().showDocument(jamsa, "_blank");
       return(true);
     }
  }
