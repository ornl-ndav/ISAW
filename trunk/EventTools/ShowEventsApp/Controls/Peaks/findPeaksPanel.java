/* 
 * File: findPeaksPanel.java
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

import java.awt.*;
import java.awt.event.*;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

/**
 * Creates a panel that displays the max number of peaks
 * to find, the minimum peak intensity as well as a
 * log file. Also sends a message of FindPeaksCmd.
 */
public class findPeaksPanel extends JPanel
{
   private static final long  serialVersionUID = 1L;
   private MessageCenter      message_center;
   private JButton            findPeaksButton;
   private JCheckBox          markPeaksCbx;
   private JTextField         maxPeaksTxt;
   private JTextField         minPeakTxt;
   private JTextField         logFileTxt;
   
   /**
    * Builds the panel as well as sets the messagecenter.
    * 
    * @param message_center
    */
   public findPeaksPanel(MessageCenter message_center)
   {
      this.message_center = message_center;
      
      this.setLayout(new GridLayout(5,1));

      this.add(buildCheckInfo());
      this.add(buildMaxPeaks());
      this.add(buildMinPeaks());
      this.add(buildLogPanel());
      this.add(buildButtonPanel());
   }
   
   /**
    * Builds the panel holding mark peaks checkbox.
    * 
    * @return panel with mark peaks checkbox
    */
   public JPanel buildCheckInfo()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));
      
      JLabel filler = new JLabel();
      
      markPeaksCbx = new JCheckBox("Mark Peaks");
      markPeaksCbx.setSelected(true);
      markPeaksCbx.addActionListener(new peaksListener());
      markPeaksCbx.setHorizontalAlignment(JCheckBox.CENTER);
      
      panel.add(filler);
      panel.add(markPeaksCbx);
      
      return panel;
   }
   
   /**
    * Builds the panel holding max # of peaks.
    * 
    * @return panel with label and textfield for max # peaks.
    */
   public JPanel buildMaxPeaks()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));
      
      JLabel maxPeaksLbl = new JLabel("Max # of Peaks");
      String defaultMaxPeaks = "50";
      maxPeaksTxt = new JTextField(defaultMaxPeaks);
      maxPeaksTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      panel.add(maxPeaksLbl);
      panel.add(maxPeaksTxt);
      
      return panel;
   }
   
   /**
    * Builds the panel holding min peak intensity.
    * 
    * @return panel with label and textfield for min peak intensity.
    */
   public JPanel buildMinPeaks()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));
      
      JLabel minPeakLbl = new JLabel("Min Peak Intensity");
      String defaultMinPeaks = "20";
      minPeakTxt = new JTextField(defaultMinPeaks);
      minPeakTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      panel.add(minPeakLbl);
      panel.add(minPeakTxt);
      
      return panel;
   }
   
   /**
    * Builds the panel holding log file name.
    * 
    * @return panel with label and textfield for log file.
    */
   public JPanel buildLogPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));
      
      JLabel logFileLbl = new JLabel("Log File Name");
      logFileTxt = new JTextField();
      logFileTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      panel.add(logFileLbl);
      panel.add(logFileTxt);
      
      return panel;
   }
   
   /**
    * Builds the panel holding find peaks button.
    * 
    * @return panel with button for find peaks.
    */
   public JPanel buildButtonPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));
      
      findPeaksButton = new JButton("Find Peaks");
      findPeaksButton.addActionListener(new buttonListener());
      
      panel.add(findPeaksButton);
      
      return panel;
   }
   
   /**
    * Called when find peaks button is pressed.  Checks to make
    * sure that the information entered for max # of peaks and
    * min peak intensity is of the right format.
    * 
    * @return false if there is information that is incorrect,
    *          true otherwise.
    */
   private boolean valid()
   {
      try
      {
         Integer.parseInt(maxPeaksTxt.getText());
      }
      catch (NumberFormatException nfe)
      {         
         String error = "Max # of Peaks must be of type Integer!";
         JOptionPane.showMessageDialog( null, 
                                        error, 
                                       "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Integer.parseInt(minPeakTxt.getText());
      }
      catch (NumberFormatException nfe)
      {
         String error = "Min Peak Intensity must be of type Integer!";
         JOptionPane.showMessageDialog( null, 
                                        error, 
                                       "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }
      
      /* if (logFileTxt.getText().equals(""))
      {
         String error = "You have not specified a log file!";
         JOptionPane.showMessageDialog( null, 
                                        error, 
                                       "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }
     else
      {
         File file = new File(logFileTxt.getText());
         if (!file.exists())
         {
            String error = logFileTxt.getText() + " does not exist!";
            JOptionPane.showMessageDialog( null, error, 
                                          "Invalid Input", 
                                           JOptionPane.ERROR_MESSAGE);
            return false;
         }
      }*/
      
      return true;
   }
   
   /**
    * ActionListener for the findpeaks button that calls
    * valid() to make sure the input is valid and then sends
    * a message of FIND_PEAKS and of type FindPeaksCmd.
    */
   private class buttonListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         if (valid())
         {
            FindPeaksCmd findPeaksCmd = new FindPeaksCmd(
                  false,
                  markPeaksCbx.isSelected(),
                  Integer.parseInt(maxPeaksTxt.getText()), 
                  Integer.parseInt(minPeakTxt.getText()),
                  logFileTxt.getText());
         
            sendMessage(Commands.FIND_PEAKS, findPeaksCmd);
         
            if(!markPeaksCbx.isEnabled())
               markPeaksCbx.setEnabled(true);
         }
      }
   }
   
   /**
    * ActionListener for markpeaks and sends either true
    * or false depending on if its checked or not.
    */
   private class peaksListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         sendMessage(Commands.MARK_PEAKS, markPeaksCbx.isSelected());
      }
   }
   
   /**
    * Sends a message to the message center.
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
      MessageCenter mc = new MessageCenter("Test Find Peaks");
      TestReceiver tr = new TestReceiver("Testing Find Peaks");
      
      mc.addReceiver(tr, Commands.FIND_PEAKS);
      
      findPeaksPanel findPeaks = new findPeaksPanel(mc);
      
      JFrame View = new JFrame("Test Find Peaks");
      View.setBounds(10, 10, 200, 175);
      View.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      View.setVisible(true);
      
      View.add(findPeaks);
      
      new UpdateManager(mc, null, 100);
   }
}
