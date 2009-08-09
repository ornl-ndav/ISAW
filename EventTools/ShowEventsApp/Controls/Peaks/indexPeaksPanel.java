package EventTools.ShowEventsApp.Controls.Peaks;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;

import MessageTools.*;
import EventTools.ShowEventsApp.Command.*;

public class indexPeaksPanel extends JPanel
{
   public static final long serialVersionUID = 1L;
   private MessageCenter    messageCenter;
   private JTextField       aTxt;
   private JTextField       bTxt;
   private JTextField       cTxt;
   private JTextField       alphaTxt;
   private JTextField       betaTxt;
   private JTextField       gammaTxt;
   private JTextField       toleranceTxt;
   private JTextField       fixedPeakTxt;
   private JTextField       requiredFractionTxt;
   private JButton          applyBtn;
   
   
   public indexPeaksPanel(MessageCenter messsageCenter)
   {
      this.messageCenter = messageCenter;
      
      this.setBorder(new TitledBorder("Index Peaks"));
      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      //this.setLayout(new GridBagLayout());
      //GridBagConstraints gbc = new GridBagConstraints();
      
      //gbc.fill = GridBagConstraints.BOTH;
      //gbc.gridx = 0;
      //gbc.gridy = 0;
      this.add(buildPanel());
      
      applyBtn = new JButton("Apply");
      applyBtn.addActionListener(new buttonListener());
      
      //gbc.gridx = 0;
      //gbc.gridy = 1;
      //gbc.weighty = .5;
      this.add(applyBtn);
      
      this.validate();
   }
   
   private JPanel buildPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(9, 2));
      
      JLabel aLbl = new JLabel("a:  ");
      aLbl.setHorizontalAlignment(JLabel.RIGHT);
      aTxt = new JTextField();
      aTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel bLbl = new JLabel("b:  ");
      bLbl.setHorizontalAlignment(JLabel.RIGHT);
      bTxt = new JTextField();
      bTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel cLbl = new JLabel("c:  ");
      cLbl.setHorizontalAlignment(JLabel.RIGHT);
      cTxt = new JTextField();
      cTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel alphaLbl = new JLabel("alpha:  ");
      alphaLbl.setHorizontalAlignment(JLabel.RIGHT);
      alphaTxt = new JTextField();
      alphaTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel betaLbl = new JLabel("beta:  ");
      betaLbl.setHorizontalAlignment(JLabel.RIGHT);
      betaTxt = new JTextField();
      betaTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel gammaLbl = new JLabel("gamma:  ");
      gammaLbl.setHorizontalAlignment(JLabel.RIGHT);
      gammaTxt = new JTextField();
      gammaTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel toleranceLbl = new JLabel("Tolerance:  ");
      toleranceLbl.setHorizontalAlignment(JLabel.RIGHT);
      toleranceTxt = new JTextField();
      toleranceTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel fixedPeakLbl = new JLabel("Fixed Peak Index:  ");
      fixedPeakLbl.setHorizontalAlignment(JLabel.RIGHT);
      fixedPeakTxt = new JTextField();
      fixedPeakTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel requiredFractionLbl = new JLabel("Required Fraction:  ");
      requiredFractionLbl.setHorizontalAlignment(JLabel.RIGHT);
      requiredFractionTxt = new JTextField();
      requiredFractionTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      panel.add(aLbl);
      panel.add(aTxt);
      panel.add(bLbl);
      panel.add(bTxt);
      panel.add(cLbl);
      panel.add(cTxt);
      panel.add(alphaLbl);
      panel.add(alphaTxt);
      panel.add(betaLbl);
      panel.add(betaTxt);
      panel.add(gammaLbl);
      panel.add(gammaTxt);
      panel.add(toleranceLbl);
      panel.add(toleranceTxt);
      panel.add(fixedPeakLbl);
      panel.add(fixedPeakTxt);
      panel.add(requiredFractionLbl);
      panel.add(requiredFractionTxt);
      
      return panel;
   }
   
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);
      
      messageCenter.receive(message);
   }

   private class buttonListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         IndexPeaksCmd indexCmd 
            = new IndexPeaksCmd(Float.parseFloat(aTxt.getText()), 
                           Float.parseFloat(bTxt.getText()), 
                           Float.parseFloat(cTxt.getText()),
                           Float.parseFloat(alphaTxt.getText()), 
                           Float.parseFloat(betaTxt.getText()), 
                           Float.parseFloat(gammaTxt.getText()), 
                           Float.parseFloat(toleranceTxt.getText()), 
                           Integer.parseInt(fixedPeakTxt.getText()), 
                           Float.parseFloat(requiredFractionTxt.getText()));
         
         sendMessage(Commands.INDEX_PEAKS, indexCmd);
      }
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Testing MessageCenter");
      TestReceiver tc = new TestReceiver("indexPeaks TestingMessages");
      mc.addReceiver(tc, Commands.INDEX_PEAKS);
      
      indexPeaksPanel ip = new indexPeaksPanel(mc);
      
      JFrame View = new JFrame( "Test Index Peaks Panel" );
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setBounds(10,10, 350, 350);
      View.setVisible(true);
      
      View.add(ip);
      new UpdateManager(mc, null, 100);
   }
}
