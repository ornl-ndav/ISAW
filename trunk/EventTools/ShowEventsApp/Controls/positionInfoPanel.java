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
   //private MessageCenter     messageCenter;
   private JTextField          countsTxt;
   private JTextField          detectorNumTxt;
   private JTextField          histogramPageTxt;
   private JTextField          hklTxt;
   private JTextField          qxyzTxt;
   private JTextField          qTxt;
   private JTextField          dSpacingTxt;
   private JTextField          timeTxt;
   private JTextField          eTxt;
   private JTextField          wavelengthTxt;
   private char                ANG = '\u00c5';
   
   public positionInfoPanel()//MessageCenter messageCenter)
   {
      //this.messageCenter = messageCenter;
      this.setLayout(new GridLayout(2,1));
      this.add(buildPanel());
      this.add(buildZoomedPanel());
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
      
      panel.setLayout(new GridLayout(10, 2));
      panel.setBorder(new TitledBorder("Position Info."));
      
      JTextField countLbl = new JTextField("Counts");
      countLbl.setEditable(false);
      countLbl.setBackground(Color.WHITE);
      JTextField detectorLbl = new JTextField("Detector Number");
      detectorLbl.setEditable(false);
      detectorLbl.setBackground(Color.WHITE);
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
      JTextField timeLbl = new JTextField("Time(us)");
      timeLbl.setEditable(false);
      timeLbl.setBackground(Color.WHITE);
      JTextField eLbl = new JTextField("E(meV)");
      eLbl.setEditable(false);
      eLbl.setBackground(Color.WHITE);
      JTextField wavelengthLbl = new JTextField("Wavelength(" + ANG + ")");
      wavelengthLbl.setEditable(false);
      wavelengthLbl.setBackground(Color.WHITE);
      
      countsTxt = new JTextField();
      countsTxt.setEditable(false);
      countsTxt.setBackground(Color.WHITE);
      detectorNumTxt = new JTextField();
      detectorNumTxt.setEditable(false);
      detectorNumTxt.setBackground(Color.WHITE);
      histogramPageTxt = new JTextField();
      histogramPageTxt.setEditable(false);
      histogramPageTxt.setBackground(Color.WHITE);
      hklTxt = new JTextField();
      hklTxt.setEditable(false);
      hklTxt.setBackground(Color.WHITE);
      qxyzTxt = new JTextField();
      qxyzTxt.setEditable(false);
      qxyzTxt.setBackground(Color.WHITE);
      qTxt = new JTextField();
      qTxt.setEditable(false);
      qTxt.setBackground(Color.WHITE);
      dSpacingTxt = new JTextField();
      dSpacingTxt.setEditable(false);
      dSpacingTxt.setBackground(Color.WHITE);
      timeTxt = new JTextField();
      timeTxt.setEditable(false);
      timeTxt.setBackground(Color.WHITE);
      eTxt = new JTextField();
      eTxt.setEditable(false);
      eTxt.setBackground(Color.WHITE);
      wavelengthTxt = new JTextField();
      wavelengthTxt.setEditable(false);
      wavelengthTxt.setBackground(Color.WHITE);
      
      panel.add(countLbl);
      panel.add(countsTxt);
      panel.add(detectorLbl);
      panel.add(detectorNumTxt);
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
   
   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.SET_POSITION_INFO))
      {
         SelectionInfoCmd selection = (SelectionInfoCmd)message.getValue();
         System.out.println(selection);
         
         return true;
      }
      
      return false;
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Testing MessageCenter");
      TestReceiver tc = new TestReceiver("Position Info TestingMessages");
      mc.addReceiver(tc, Commands.SET_POSITION_INFO);
      
      positionInfoPanel pip = new positionInfoPanel();//mc);
      
      JFrame View = new JFrame( "Test Position Info Panel" );
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setBounds(10,10, 350, 450);
      View.setVisible(true);
      
      View.add(pip);
      new UpdateManager(mc, null, 100);
   }
}
