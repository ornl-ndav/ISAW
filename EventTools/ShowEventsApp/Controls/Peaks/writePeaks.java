/* 
 * File: writePeaks.java
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

package EventTools.ShowEventsApp.Controls.Peaks;

import javax.swing.*;

import java.awt.event.*;
import java.awt.GridLayout;

import MessageTools.*;
import EventTools.ShowEventsApp.Command.*;

/**
 * Creates a panel for the write peaks panel that also sends out
 * a message of WRITE_PEAK_FILE.
 */
public class writePeaks extends JPanel
{
   private static final long serialVersionUID = 1L;
   private MessageCenter     message_center;
   private JButton           savePeaks;
   
   /**
    * Builds button and adds to panel as well as stores
    * the message center passed in.
    * 
    * @param message_center
    */
   public writePeaks(MessageCenter message_center)
   {
      this.message_center = message_center;

      this.setLayout(new GridLayout(1,1));
      savePeaks = new JButton("Write Peaks File");
      savePeaks.addActionListener(new buttonListener());
      
      this.add(savePeaks);
   }
   
   /**
    * Sends a message to the messagecenter.
    * 
    * @param command
    * @param value
    */
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command,
                                    value,
                                    true);
      
      message_center.send(message);
   }
   
   /**
    * Listens to the button and when pressed
    * pops up the JFileChooser for the user to save their
    * peaks file anywhere they want. Sends a message of
    * WRITE_PEAKS_FILE along with the file name to the 
    * message center.
    */
   private class buttonListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         if (e.getSource() == savePeaks)
         {
            final JFileChooser fc = new JFileChooser();
            
            int returnVal = fc.showSaveDialog(null);
            
            if (returnVal == JFileChooser.CANCEL_OPTION)
            {
               System.out.println("User Canceled Save");
               return;
            }
            else if (returnVal == JFileChooser.ERROR_OPTION)
            {
               JOptionPane.showMessageDialog(null, "Error saving file", 
                              "Error Saving!", JOptionPane.ERROR_MESSAGE);
               return;
            }
            else if (returnVal == JFileChooser.APPROVE_OPTION)
            {
               String file_name = fc.getSelectedFile().toString();
               sendMessage(Commands.WRITE_PEAK_FILE, file_name);
               JOptionPane.showMessageDialog( null , 
                     "This option will disappear. Use Menu bar" );
            }
         }
      }
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Test Write Peaks");
      TestReceiver tr = new TestReceiver("Testing Write Peaks");
      
      mc.addReceiver(tr, Commands.WRITE_PEAK_FILE);
      
      writePeaks wPeaks = new writePeaks(mc);
      
      JFrame View = new JFrame("Test Write Peaks");
      View.setBounds(10, 10, 175, 75);
      View.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      View.setVisible(true);
      
      View.add(wPeaks);
      
      new UpdateManager(mc, null, 100);
   }
}
