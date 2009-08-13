package EventTools.ShowEventsApp.Controls.HistogramControls;

import java.awt.event.*;
import java.awt.GridLayout;
import javax.swing.*;

import gov.anl.ipns.MathTools.Geometry.SlicePlane3D;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.ViewTools.UI.*;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

public class sliceControl extends JPanel
                          implements IReceiveMessage
{
   private static final long  serialVersionUID  = 1L;
   private SliceSelectorSplitPaneUI scUI;
   private MessageCenter   message_center;
   private JFrame          sliceDisplay;
   
   public sliceControl(MessageCenter mc)
   {
      message_center = mc;
      message_center.addReceiver(this, Commands.SET_ORIENTATION_MATRIX);
      
      scUI = new SliceSelectorSplitPaneUI(ISlicePlaneSelector.QXYZ_MODE);
      scUI.addActionListener(new Action());
      this.setLayout(new GridLayout(1,1));
      this.add(scUI);
   }
   
   public void setSliceInformation()
   {

   }
   
   public void setPlane(SlicePlane3D plane)
   {
      scUI.setPlane(plane);
   }
   
   public void setFrameInformation()
   {

   }
   
   public void showSliceControl()
   {
      sliceDisplay.setVisible(true);
   }
   
   private void sendMessage(String command, Object value)
   {
      Message message = new Message( command,
                                     value,
                                     true );
      
      message_center.receive( message );
   }
   
   private class Action implements ActionListener
   {
      public void actionPerformed(ActionEvent arg0)
      {
         String command = arg0.getActionCommand();
         
         SlicePlaneInformationCmd spi = 
            new SlicePlaneInformationCmd(scUI.getSliceMode(),
                                      scUI.getPlane(),
                                      scUI.getPlane().getNormal(),
                                      scUI.getStepSize(),
                                      scUI.getSliceWidth(),
                                      scUI.getSliceHeight(),
                                      scUI.getSliceThickness(),
                                      scUI.getSliceNumber());

         sendMessage(command, spi);
      }
   }
   
   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.SET_ORIENTATION_MATRIX))
      {
         
         return true;
      }
      return false;
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("sliceControl MessageCenter");
      TestReceiver tr = new TestReceiver("Test");

      mc.addReceiver(tr, Commands.SET_ORIENTATION_MATRIX);
      
      sliceControl sc = new sliceControl(mc);

      JFrame View = new JFrame("Slice Control");
      View.setBounds(625,210,225,425);
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setVisible(true);
      
      View.add(sc);
    
      new UpdateManager( mc, null, 1000 );
   }
}
