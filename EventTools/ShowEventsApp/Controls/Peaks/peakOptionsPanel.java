package EventTools.ShowEventsApp.Controls.Peaks;


import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.GridLayout;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

public class peakOptionsPanel extends JPanel
{
   private static final long serialVersionUID = 1L;
   
   public peakOptionsPanel(MessageCenter message_center)
   {
      findPeaksPanel findPeaks = new findPeaksPanel(message_center);
      //markPeaks markpeaks = new markPeaks(message_center);
      writePeaks writepeaks = new writePeaks(message_center);
      
      this.setBorder(new TitledBorder("Peak Options"));
      
      this.setLayout(new GridLayout(6,1));
      //this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      this.add(findPeaks.buildCheckInfo());
      this.add(findPeaks.buildMaxPeaks());
      this.add(findPeaks.buildMinPeaks());
      this.add(findPeaks.buildLogPanel());
      this.add(findPeaks.buildButtonPanel());
      this.add(writepeaks);
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
