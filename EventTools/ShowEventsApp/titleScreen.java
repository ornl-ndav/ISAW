/* 
 * File: titleScreen.java
 *
 * Copyright (C) 2009, Paul Fischer
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0800276 and by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author: fischerp $
 *  $Date: 2009-08-25 11:02:13 -0500 (Tue, 25 Aug 2009) $            
 *  $Revision: 19931 $
 */

package EventTools.ShowEventsApp;

import java.awt.*;
import javax.swing.*;

/**
 * Creates the welcome image to be displayed on the displayPanel
 * when the program is launched.
 */
public class titleScreen extends JPanel
{
   private static final long serialVersionUID = 1L;
   private Image image;
   
   /**
    * Constructor.
    *
    * @param image the Image to be displayed .
    * See the Java glossary under Image for ways to create an Image from a file.
    */
   public titleScreen( Image image )
   {
      setImage( image );
   }

   /**
    * Set or change the current Image to display.
    * setImage does a MediaTracker to ensure the Image is loaded.
    *
    * @param image the Image to be displayed.
    */
   public void setImage( Image image )
   {
      this.image = image;
      
      if ( image != null )
      {
         MediaTracker tracker;
         try
         {
            tracker = new MediaTracker( this );
            tracker.addImage( image, 0 );
            tracker.waitForID( 0 );
         }
         catch ( InterruptedException e )
         {
            System.out.println(e);
         }
      }
      repaint();
   }

   /**
    * Paints this component using the given graphics context.
    * Scales the image to the size of its parent component.
    *
    * @param g      Graphics context where to paint.
    */
   public void paintComponent( Graphics g )
   {
      super.paintComponent( g );
      Dimension dim = getSize();
      g.drawImage ( image, 0, 0, dim.width, dim.height, this);
   }

   public static void main(String[] args)
   {
      String filename = "D:/IsawSNS/ISAW/EventTools/ShowEventsApp/splashscreen.png";
      Image image = new ImageIcon(filename).getImage();
      titleScreen tt = new titleScreen(image);
      JFrame frame = new JFrame("Test Image");
      frame.setSize(600, 400);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      frame.add(tt);
      frame.setVisible(true);
   }
}
