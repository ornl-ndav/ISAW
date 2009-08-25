/* 
 * File: markPeaks.java
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

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

/**
 * Creates a panel to hold the mark peaks checkbox
 * and sends messages of MARK_PEAKS when checked or unchecked.
 */
public class markPeaks extends JPanel
{
   private static final long serialVersionUID = 1L;
   private MessageCenter     message_center;
   private JCheckBox         markPeaksCbx;
   
   /**
    * Creates the mark peaks checkbox and adds it to itself.
    * 
    * @param message_center
    */
   public markPeaks(MessageCenter message_center)
   {
      this.message_center = message_center;

      this.setLayout(new GridLayout(1,1));
      markPeaksCbx = new JCheckBox("Mark Peaks");
      markPeaksCbx.setSelected(false);
      markPeaksCbx.addActionListener(new peaksListener());
      markPeaksCbx.setHorizontalAlignment(JCheckBox.CENTER);
      
      this.add(markPeaksCbx);
   }

   /**
    * ActionListener that listens to markPeaksCheckbox and
    * sends out a message of MARK_PEAKS.
    */
   private class peaksListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         sendMessage(Commands.MARK_PEAKS, markPeaksCbx.isSelected());
      }
   }
   
   /**
    * Sends a message to the messagecenter
    * 
    * @param command
    * @param value
    */
   private void sendMessage(String command, Object value)
   {
      Message message = new Message( command,
                                     value,
                                     true );
      
      message_center.receive( message );
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Test Mark Peaks");
      TestReceiver tr = new TestReceiver("Testing Mark Peaks");
      
      mc.addReceiver(tr, Commands.MARK_PEAKS);
      
      markPeaks mPeaks = new markPeaks(mc);
      
      JFrame View = new JFrame("Test Mark Peaks");
      View.setBounds(10, 10, 120, 75);
      View.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      View.setVisible(true);
      
      View.add(mPeaks);
      
      new UpdateManager(mc, null, 100);
   }
}
