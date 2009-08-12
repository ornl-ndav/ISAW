package EventTools.ShowEventsApp.Controls.Peaks;

import javax.swing.*;

import java.awt.event.*;
import java.awt.Dimension;
import java.awt.GridLayout;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

public class markPeaks extends JPanel
{
   private static final long serialVersionUID = 1L;
   private MessageCenter     message_center;
   private JCheckBox         markPeaksCbx;
   
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
