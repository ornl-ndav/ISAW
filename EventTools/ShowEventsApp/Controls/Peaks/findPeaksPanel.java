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
import gov.anl.ipns.ViewTools.UI.FontUtil;
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

   private JTextField         maxPeaksTxt;
   private JTextField         minPeakTxt;

   private JCheckBox          smoothDataCbx;

   private JCheckBox          markPeaksCbx;

   private JCheckBox          showPeakImagesCbx;
   private JTextField         imageSizeTxt;
   private JTextField         maxSliceOffsetTxt;

   private JTextField         logFileTxt;

   private JButton            findPeaksButton;
   
   /**
    * Builds the panel as well as sets the messagecenter.
    * 
    * @param message_center
    */
   public findPeaksPanel(MessageCenter message_center)
   {
      this.message_center = message_center;
      
      this.setLayout(new GridLayout(9,1));

      this.add(buildMaxPeaks());
      this.add(buildMinPeaks());

      this.add(buildSmoothDataCheckBox());

      this.add(buildMarkPeaksCheckBox());

      this.add(buildShowPeakImageCheckBox());
      this.add(buildPeakSizePanel());
      this.add(buildNumSlicesPanel());

      this.add(buildLogPanel());

      this.add(buildButtonPanel());
   }


   /**
    * Builds the panel holding max # of peaks.
    * 
    * @return panel with label and textfield for max # peaks.
    */
   private JPanel buildMaxPeaks()
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
   private JPanel buildMinPeaks()
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
    * Builds the panel holding the smooth data checkbox.
    * 
    * @return panel with smooth data checkbox
    */
   private JPanel buildSmoothDataCheckBox()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));

      JLabel filler = new JLabel();

      smoothDataCbx = new JCheckBox("Smooth Data?");
      smoothDataCbx.setSelected(false);
      smoothDataCbx.addActionListener(new peaksListener());

      panel.add(filler);
      panel.add(smoothDataCbx);

      return panel;
   }


   /**
    * Builds the panel holding mark peaks checkbox.
    * 
    * @return panel with mark peaks checkbox
    */
   private JPanel buildMarkPeaksCheckBox()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));

      JLabel filler = new JLabel();

      markPeaksCbx = new JCheckBox("Mark Peaks");
      markPeaksCbx.setSelected(true);
      markPeaksCbx.addActionListener(new peaksListener());

      panel.add(filler);
      panel.add(markPeaksCbx);

      return panel;
   }


   /**
    * Builds the panel holding show peak images checkbox.
    * 
    * @return panel with show peaks checkbox
    */
   private JPanel buildShowPeakImageCheckBox()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));

      JLabel filler = new JLabel();

      showPeakImagesCbx = new JCheckBox("Show Peak Images");
      showPeakImagesCbx.setSelected(true);
      showPeakImagesCbx.addActionListener(new ShowImagesListener());

      panel.add(filler);
      panel.add(showPeakImagesCbx);

      return panel;
   }


   /**
    * Builds the panel holding peak image region size.
    * 
    * @return panel with peak image region size 
    */
   private JPanel buildPeakSizePanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));

      JLabel size_label = 
                       new JLabel("Image size ( "+FontUtil.INV_ANGSTROM+" )");

      imageSizeTxt = new JTextField("0.5");
      imageSizeTxt.setHorizontalAlignment(JTextField.RIGHT);

      panel.add(size_label);
      panel.add(imageSizeTxt);

      return panel;
   }


   /**
    * Builds the panel holding requested number of peak slices +/-.
    * 
    * @return panel with maxSliceOffsetTxt 
    */
   private JPanel buildNumSlicesPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));

      JLabel slices_label = new JLabel("Number of Image Sices (+/-)");

      maxSliceOffsetTxt = new JTextField("5");
      maxSliceOffsetTxt.setHorizontalAlignment(JTextField.RIGHT);

      panel.add(slices_label);
      panel.add(maxSliceOffsetTxt);

      return panel;
   }


   /**
    * Builds the panel holding log file name.
    * 
    * @return panel with label and textfield for log file.
    */
   private JPanel buildLogPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));
      
      JLabel logFileLbl = new JLabel("Find Peaks Log File Name");
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
   private JPanel buildButtonPanel()
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
         String error = "Max # of Peaks must be a valid integer!";
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
         String error = "Min Peak Intensity must be a valid integer!";
         JOptionPane.showMessageDialog( null, 
                                        error, 
                                       "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }

      try
      {
         Float.parseFloat(imageSizeTxt.getText());
      }
      catch (NumberFormatException nfe)
      {
         String error = "Image size in Q must be a valid float!";
         JOptionPane.showMessageDialog( null,
                                        error,
                                       "Invalid Input",
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }

      try
      {
         Integer.parseInt(maxSliceOffsetTxt.getText());
      }
      catch (NumberFormatException nfe)
      {
         String error = "Number of slices must be a  valid integer!";
         JOptionPane.showMessageDialog( null,
                                        error,
                                       "Invalid Input",
                                        JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
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
         if ( valid() )
         {
            FindPeaksCmd findPeaksCmd = new FindPeaksCmd(
                  Integer.parseInt(maxPeaksTxt.getText()), 
                  Integer.parseInt(minPeakTxt.getText()),
                  smoothDataCbx.isSelected(),
                  markPeaksCbx.isSelected(),
                  showPeakImagesCbx.isSelected(),
                  Float.parseFloat(imageSizeTxt.getText()),
                  Integer.parseInt(maxSliceOffsetTxt.getText()),
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
    * Listener for the Show Peak Images checkbox.
    */
   private class ShowImagesListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
        boolean show_peaks = showPeakImagesCbx.isSelected();
       
        if ( !show_peaks )
          sendMessage(Commands.CLOSE_PEAK_IMAGES, show_peaks );
 
        else if ( valid() ) 
        {
          PeaksCmd cmd = new PeaksCmd(
                                null, 
                                show_peaks,
                                Float.parseFloat(imageSizeTxt.getText()),
                                Integer.parseInt(maxSliceOffsetTxt.getText()));

          sendMessage(Commands.MAKE_PEAK_IMAGES, cmd );
        }
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
      message_center.send( message );
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
