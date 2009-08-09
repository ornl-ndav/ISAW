package EventTools.ShowEventsApp.Controls.Peaks;


import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

public class peakOptionsPanel extends JPanel
{
   private static final long serialVersionUID = 1L;
   
   public peakOptionsPanel(MessageCenter message_center)
   {
      findPeaksPanel findPeaks = new findPeaksPanel(message_center);
      markPeaks markpeaks = new markPeaks(message_center);
      writePeaks writepeaks = new writePeaks(message_center);
      
      this.setBounds(0, 0, 300, 300);
      this.setBorder(new TitledBorder("Peak Options"));
      
      this.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      
      gbc.fill = GridBagConstraints.BOTH;
      gbc.ipadx = 50;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = .75;
      this.add(findPeaks, gbc);
      
      gbc.ipadx = 50;
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.weighty = .125;
      this.add(markpeaks, gbc);
      
      gbc.ipadx = 50;
      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.weighty = .125;
      this.add(writepeaks, gbc);
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Test Peak Options");
      TestReceiver tr = new TestReceiver("Testing Peak Options");
      
      mc.addReceiver(tr, Commands.WRITE_PEAK_FILE);
      mc.addReceiver(tr, Commands.FIND_PEAKS);
      mc.addReceiver(tr, Commands.MARK_PEAKS);
      
      peakOptionsPanel peakOptions = new peakOptionsPanel(mc);
      
      JFrame View = new JFrame("Test Peak Options");
      View.setBounds(10, 10, 300, 300);
      View.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      View.setVisible(true);
      
      View.add(peakOptions);
      
      new UpdateManager(mc, null, 100);
   }
}
