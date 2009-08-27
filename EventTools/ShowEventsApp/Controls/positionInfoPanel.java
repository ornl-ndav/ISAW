package EventTools.ShowEventsApp.Controls;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

import MessageTools.*;
import EventTools.ShowEventsApp.Command.*;
import SSG_Tools.SSG_Nodes.Group;

public class positionInfoPanel extends JPanel
                               implements IReceiveMessage
{
   public static final long    serialVersionUID = 1L;
   private MessageCenter       messageCenter;
   private JTextField          countsTxt;
   private JTextField          detectorNumTxt;
   private JTextField          ColRowTxt;
   private JTextField          rowTxt;
   private JTextField          histogramPageTxt;
   private JTextField          hklTxt;
   private JTextField          qxyzTxt;
   private JTextField          qTxt;
   private JTextField          dSpacingTxt;
   private JTextField          timeTxt;
   private JTextField          eTxt;
   private JTextField          wavelengthTxt;
   private char                ANG = '\u00c5';
   private char                MU = '\u03bc';
   
   public positionInfoPanel(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      this.setLayout(new GridLayout(1,1));
      this.add(buildPanel());
      //this.add(buildZoomedPanel());
      messageCenter.addReceiver(this, Commands.SHOW_SELECTED_POINT_INFO);
   }

   private JPanel buildZoomedPanel()
   {
      JPanel panel = new JPanel();
      this.setLayout(new GridLayout(1,1));
      
      return panel;
   }
   
   
   private JPanel buildPanel()
   {
      JPanel panel = new JPanel();
      
      panel.setLayout(new GridLayout(11, 2));
      panel.setBorder(new TitledBorder("Position Info."));
      
      JTextField countLbl = new JTextField("Counts");
      countLbl.setEditable(false);
      countLbl.setBackground(Color.WHITE);
      
      JTextField detectorLbl = new JTextField("Detector Number");
      detectorLbl.setEditable(false);
      detectorLbl.setBackground(Color.WHITE);
      
      JTextField ColRowLbl = new JTextField("Column,Row");
      ColRowLbl.setEditable(false);
      ColRowLbl.setBackground(Color.WHITE);
      
    
      
      JTextField histogramLbl = new JTextField("Histogram Page");
      histogramLbl.setEditable(false);
      histogramLbl.setBackground(Color.WHITE);
      
      JTextField hklLbl = new JTextField("h,k,l");
      hklLbl.setEditable(false);
      hklLbl.setBackground(Color.WHITE);
      
      JTextField qxyzLbl = new JTextField("Qx,Qy,Qz");
      qxyzLbl.setEditable(false);
      qxyzLbl.setBackground(Color.WHITE);
      
      JTextField qLbl = new JTextField("Q(Inv(" + ANG + "))");
      qLbl.setEditable(false);
      qLbl.setBackground(Color.WHITE);
      
      JTextField dSpacingLbl = new JTextField("d-Spacing(" + ANG + ")");
      dSpacingLbl.setEditable(false);
      dSpacingLbl.setBackground(Color.WHITE);
      
      JTextField timeLbl = new JTextField("Time(" + MU + "s)");
      timeLbl.setEditable(false);
      timeLbl.setBackground(Color.WHITE);
      
      JTextField eLbl = new JTextField("E(meV)");
      eLbl.setEditable(false);
      eLbl.setBackground(Color.WHITE);
      
      JTextField wavelengthLbl = new JTextField("Wavelength(" + ANG + ")");
      wavelengthLbl.setEditable(false);
      wavelengthLbl.setBackground(Color.WHITE);
      
      countsTxt = new JTextField("0");
      countsTxt.setEditable(false);
      countsTxt.setBackground(Color.WHITE);
      countsTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      detectorNumTxt = new JTextField("0");
      detectorNumTxt.setEditable(false);
      detectorNumTxt.setBackground(Color.WHITE);
      detectorNumTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      ColRowTxt = new JTextField("(  0,  0)");
      ColRowTxt.setEditable(false);
      ColRowTxt.setBackground(Color.WHITE);
      ColRowTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      detectorNumTxt = new JTextField("0");
      detectorNumTxt.setEditable(false);
      detectorNumTxt.setBackground(Color.WHITE);
      detectorNumTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      histogramPageTxt = new JTextField("0");
      histogramPageTxt.setEditable(false);
      histogramPageTxt.setBackground(Color.WHITE);
      histogramPageTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      hklTxt = new JTextField("(  0.00,   0.00,   0.00)");
      hklTxt.setEditable(false);
      hklTxt.setBackground(Color.WHITE);
      hklTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      qxyzTxt = new JTextField("(  0.00,   0.00,   0.00)");
      qxyzTxt.setEditable(false);
      qxyzTxt.setBackground(Color.WHITE);
      qxyzTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      qTxt = new JTextField(" 0.0000");
      qTxt.setEditable(false);
      qTxt.setBackground(Color.WHITE);
      qTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      dSpacingTxt = new JTextField(" 0.0000000");
      dSpacingTxt.setEditable(false);
      dSpacingTxt.setBackground(Color.WHITE);
      dSpacingTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      timeTxt = new JTextField("   0.0");
      timeTxt.setEditable(false);
      timeTxt.setBackground(Color.WHITE);
      timeTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      eTxt = new JTextField(" 0.0000");
      eTxt.setEditable(false);
      eTxt.setBackground(Color.WHITE);
      eTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      wavelengthTxt = new JTextField("0.000000");
      wavelengthTxt.setEditable(false);
      wavelengthTxt.setBackground(Color.WHITE);
      wavelengthTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      panel.add(countLbl);
      panel.add(countsTxt);
      panel.add(detectorLbl);
      panel.add(detectorNumTxt);
      panel.add(ColRowLbl);
      panel.add(ColRowTxt);
      panel.add(histogramLbl);
      panel.add(histogramPageTxt);
      panel.add(hklLbl);
      panel.add(hklTxt);
      panel.add(qxyzLbl);
      panel.add(qxyzTxt);
      panel.add(qLbl);
      panel.add(qTxt);
      panel.add(dSpacingLbl);
      panel.add(dSpacingTxt);
      panel.add(timeLbl);
      panel.add(timeTxt);
      panel.add(eLbl);
      panel.add(eTxt);
      panel.add(wavelengthLbl);
      panel.add(wavelengthTxt);
      
      return panel;
   }
   
   private void setFields(SelectionInfoCmd selection)
   {
      countsTxt.setText( "" + String.format( "%4.2f" , selection.getCounts() ));
      detectorNumTxt.setText( "" + selection.getDetNum() );
      ColRowTxt.setText( "("+selection.getCol()+", "+selection.getRow()+")" );
      histogramPageTxt.setText( "" + selection.getHistPage() );
      hklTxt.setText( String.format("(%6.2f, %6.2f, %6.2f)",
            selection.getHKL().getX(), selection.getHKL().getY(),
            selection.getHKL().getZ()) );
      qxyzTxt.setText( String.format("(%6.2f, %6.2f, %6.2f)",
            selection.getQxyz().getX(), selection.getQxyz().getY(),
            selection.getQxyz().getZ()) );
      qTxt.setText( String.format("%7.4f", selection.getRaw_Q()) );
      dSpacingTxt.setText( String.format("%8.6f", selection.getD_spacing()) );
      timeTxt.setText( String.format("%6.1f", selection.getTof()) );
      eTxt.setText( String.format("%7.4f", selection.getE_mev()) );
      wavelengthTxt.setText( String.format("%8.6f", selection.getWavelength()) );
   }
   
   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.SHOW_SELECTED_POINT_INFO))
      {
         SelectionInfoCmd selection = (SelectionInfoCmd)message.getValue();
         setFields(selection);
         
         System.out.println(selection);
         
         return true;
      }
      
      return false;
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Testing MessageCenter");
      TestReceiver tc = new TestReceiver("Position Info TestingMessages");
      mc.addReceiver(tc, Commands.SHOW_SELECTED_POINT_INFO);
      
      positionInfoPanel pip = new positionInfoPanel(mc);
      
      JFrame View = new JFrame( "Test Position Info Panel" );
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setBounds(10,10, 350, 450);
      View.setVisible(true);
      
      View.add(pip);
      new UpdateManager(mc, null, 100);
   }
}
