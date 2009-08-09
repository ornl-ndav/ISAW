package EventTools.ShowEventsApp.Controls.Peaks;

import javax.swing.*;

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
   private JTextField         maxPeaksTxt;
   private JTextField         minPeakTxt;
   private JTextField         logFileTxt;
   
   public findPeaksPanel(MessageCenter message_center)
   {
      this.message_center = message_center;
      
      //this.setLayout(new GridLayout(2,3));
      //this.setBounds(0, 0, 150, 175);
      this.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      
      smoothCbx = new JCheckBox("Smooth Data");
      smoothCbx.setSelected(false);
      
      JLabel maxPeaksLbl = new JLabel("Max # of Peaks");
      maxPeaksTxt = new JTextField();
      maxPeaksTxt.setSize(100, 20);
      
      JLabel minPeakLbl = new JLabel("Min Peak Intens.");
      minPeakTxt = new JTextField();
      minPeakTxt.setSize(100, 20);
      
      JLabel logFileLbl = new JLabel("Log File Name");
      logFileTxt = new JTextField();
      logFileTxt.setSize(100, 20);
      
      findPeaksButton = new JButton("Find Peaks");
      findPeaksButton.addActionListener(new buttonListener());

      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.gridwidth = 2;
      gbc.weighty = 0.5;
      this.add(smoothCbx, gbc);
      
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 0.0;
      gbc.weighty = 0.5;
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.gridwidth = 1;
      this.add(maxPeaksLbl, gbc);
      
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 0.5;
      gbc.weighty = 0.5;
      gbc.anchor = GridBagConstraints.FIRST_LINE_END;
      gbc.gridx = 1;
      gbc.gridy = 1;
      gbc.gridwidth = 1;
      gbc.ipadx = 25;
      this.add(maxPeaksTxt, gbc);
      
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 0.0;
      gbc.weighty = 0.5;
      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.gridwidth = 1;
      gbc.ipadx = 0;
      this.add(minPeakLbl, gbc);
      
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 0.5;
      gbc.weighty = 0.5;
      gbc.gridx = 1;
      gbc.gridy = 2;
      gbc.gridwidth = 1;
      gbc.ipadx = 25;
      this.add(minPeakTxt, gbc);
      
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 0.0;
      gbc.weighty = 0.5;
      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.gridwidth = 1;
      gbc.ipadx = 0;
      this.add(logFileLbl, gbc);
      
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 0.5;
      gbc.weighty = 0.5;
      gbc.gridx = 1;
      gbc.gridy = 3;
      gbc.gridwidth = 1;
      gbc.ipadx = 25;
      this.add(logFileTxt, gbc);
      
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 4;
      gbc.gridwidth = 2;
      gbc.ipadx = 0;
      this.add(findPeaksButton, gbc);
      
      this.validate();
   }
   
   private class buttonListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         FindPeaksCmd findPeaksCmd = new FindPeaksCmd(smoothCbx.isSelected(),
               Integer.parseInt(maxPeaksTxt.getText()), Integer.parseInt(minPeakTxt.getText()),
               logFileTxt.getText());
         
         sendMessage(Commands.FIND_PEAKS, findPeaksCmd);
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
