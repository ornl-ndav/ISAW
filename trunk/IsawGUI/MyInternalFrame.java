/*
 * File: MyInternalFrame.java
 *
 * Copyright (C) 2001, Alok Chatterjee
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.2  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 */
package IsawGUI;
import javax.swing.*;
import java.net.*;
import java.awt.*;
import java.awt.print.*;
import java.io.*;

class MyInternalFrame extends JInternalFrame implements Printable
{

   public MyInternalFrame (JInternalFrame jif) throws IOException
   {
     super (jif.toString());
   }


  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
     int screenResolution = getToolkit().getScreenResolution();
     double pixelsPerPoint = (double)screenResolution/72d;
     
     int keepWidth = getSize().width;
     int keepHeight = getSize().height;
     int numPages = 0;
     int pageWidth = 0;
     int pageHeight = 0;
     if (pageIndex == 0) 
     {
        pageWidth = (int)(pageFormat.getImageableWidth()*pixelsPerPoint);
        pageHeight = (int)(pageFormat.getImageableHeight()*pixelsPerPoint);

        setSize(pageWidth, pageHeight);
        Graphics temp = graphics.create();
        printAll(temp);
        temp = null;
        

        int newHeight = getPreferredSize().height;
        if (newHeight%pageHeight ==0)
            numPages = newHeight / pageHeight;
        else
            numPages = newHeight / pageHeight + 1;

     }

     else if (pageIndex >= numPages)
     {
        return (Printable.NO_SUCH_PAGE);
     } 

    int newXOrigin = (int) (pageFormat.getImageableX()*pixelsPerPoint);
    int newYOrigin = (int) (pageFormat.getImageableY()*pixelsPerPoint);

    setSize(pageWidth, pageHeight);

    if(graphics instanceof Graphics2D)
    {
       Graphics2D g2D = (Graphics2D)graphics;
       g2D.scale(1/pixelsPerPoint, 1/pixelsPerPoint);
    } 
    graphics.translate(newXOrigin, newYOrigin - (pageIndex*pageHeight));
    graphics.setClip(0,(pageIndex*pageHeight), pageWidth,pageHeight);
    printAll(graphics);
    setSize(keepWidth, keepHeight);

    return(Printable.PAGE_EXISTS);

}
 

}
