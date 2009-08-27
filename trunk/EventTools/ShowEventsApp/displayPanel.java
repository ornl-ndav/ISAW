/* 
 * File: displayPanel.java
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
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.ShowEventsApp;

import javax.swing.*;

import java.awt.*;

import MessageTools.*;
import EventTools.ShowEventsApp.Command.*;

/**
 * Creates a simple JPanel and listens for messages.
 * On startup, displays the welcome screen for the project IsawEV.
 * The messages will have along with it the panel to be displayed
 * on this panel.
 */
public class displayPanel extends JPanel
                          implements IReceiveMessage
{
   public static final long serialVersionUID = 1L;
   private MessageCenter    messageCenter;
   private JPanel           previousPanel = null;
   
   /**
    * Creates the welcome screen and displays it on the panel.
    * 
    * @param messageCenter
    */
   public displayPanel(MessageCenter messageCenter)
   {
      this.setLayout(new GridLayout(1,1));
      this.messageCenter = messageCenter;
      this.messageCenter.addReceiver(this, Commands.CHANGE_PANEL);

      String filename = System.getProperty("ISAW_HOME");
      filename += "/images/splashscreen.png";
      //filename = filename.replaceAll(" ", "%20");
      
      Image image = new ImageIcon(filename).getImage();
      previousPanel = new titleScreen(image);
      this.add(previousPanel);
      this.validate();
      this.repaint();
   }
   
   /**
    * Takes the panel received from the message center
    * and displays it on the panel removing the previous panel.
    * @param panel
    */
   private void setPanel(JPanel panel)
   {
      if (panel == null)
      {
         this.remove(previousPanel);
         return;
      }
      
      if (previousPanel != null)
         this.remove(previousPanel);

      this.add(panel);
      previousPanel = panel;
      this.validate();
      this.repaint();
   }
   
   /**
    * Listens for CHANGE_PANEL and then calls setPanel().
    */
   public boolean receive(Message message)
   {      
      if (message.getName().equals(Commands.CHANGE_PANEL))
      {
         setPanel((JPanel)message.getValue());
         return true;
      }
      
      return false;
   }
}
