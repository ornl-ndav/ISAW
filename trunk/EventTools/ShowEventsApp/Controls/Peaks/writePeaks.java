package EventTools.ShowEventsApp.Controls.Peaks;

import javax.swing.*;

import java.awt.event.*;
import java.io.File;

import MessageTools.*;
import EventTools.ShowEventsApp.Command.*;

public class writePeaks extends JPanel
{
   private static final long serialVersionUID = 1L;
   private MessageCenter     message_center;
   private JButton           savePeaks;
   
   public writePeaks(MessageCenter message_center)
   {
      this.message_center = message_center;

      this.setBorder(BorderFactory.createEmptyBorder());
      savePeaks = new JButton("Write Peaks File...");
      savePeaks.addActionListener(new buttonListener());
      
      this.add(savePeaks);
      this.validate();
   }
   
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command,
                                    value,
                                    true);
      
      message_center.receive(message);
   }
   
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
               JOptionPane.showMessageDialog(null, "Error saving file", "Error Saving!", JOptionPane.ERROR_MESSAGE);
               return;
            }
            else if (returnVal == JFileChooser.APPROVE_OPTION)
            {
               File file = fc.getSelectedFile();
               sendMessage(Commands.WRITE_PEAK_FILE, file);
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
