package EventTools.ShowEventsApp.Controls.Peaks;

import javax.swing.*;
import java.io.File;

import java.awt.*;
import java.awt.event.*;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

public class findPeaksPanel extends JPanel
{
   private static final long  serialVersionUID = 1L;
   private MessageCenter      message_center;
   private JButton            findPeaksButton;
   private JCheckBox          smoothCbx;
   private JCheckBox          markPeaksCbx;
   private JTextField         maxPeaksTxt;
   private JTextField         minPeakTxt;
   private JTextField         logFileTxt;
   
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
   
   public JPanel buildCheckInfo()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));
      
      smoothCbx = new JCheckBox("Smooth Data");
      smoothCbx.setSelected(false);
      smoothCbx.setHorizontalAlignment(JCheckBox.CENTER);
      
      markPeaksCbx = new JCheckBox("Mark Peaks");
      markPeaksCbx.setSelected(true);
      markPeaksCbx.addActionListener(new peaksListener());
      markPeaksCbx.setHorizontalAlignment(JCheckBox.CENTER);
      
      panel.add(smoothCbx);
      panel.add(markPeaksCbx);
      
      return panel;
   }
   
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
   
   public JPanel buildButtonPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));
      
      findPeaksButton = new JButton("Find Peaks");
      findPeaksButton.addActionListener(new buttonListener());
      
      panel.add(findPeaksButton);
      
      return panel;
   }
   
   private boolean valid()
   {
      try
      {
         Integer.parseInt(maxPeaksTxt.getText());
      }
      catch (NumberFormatException nfe)
      {         
         String error = "Max # of Peaks must be of type Integer!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Integer.parseInt(minPeakTxt.getText());
      }
      catch (NumberFormatException nfe)
      {
         String error = "Min Peak Intensity must be of type Integer!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      if (logFileTxt.getText().equals(""))
      {
         String error = "You have not specified a log file!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      /*else
      {
         File file = new File(logFileTxt.getText());
         if (!file.exists())
         {
            String error = logFileTxt.getText() + " does not exist!";
            JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return false;
         }
      }*/
      
      return true;
   }
   
   private class buttonListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         if (valid())
         {
            FindPeaksCmd findPeaksCmd = new FindPeaksCmd(smoothCbx.isSelected(),
                  Integer.parseInt(maxPeaksTxt.getText()), 
                  Integer.parseInt(minPeakTxt.getText()),
                  logFileTxt.getText());
         
            sendMessage(Commands.FIND_PEAKS, findPeaksCmd);
         
            if(!markPeaksCbx.isEnabled())
               markPeaksCbx.setEnabled(true);
         }
      }
   }
   
   private class peaksListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         sendMessage(Commands.MARK_PEAKS, markPeaksCbx.isSelected());
      }
   }
   
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
