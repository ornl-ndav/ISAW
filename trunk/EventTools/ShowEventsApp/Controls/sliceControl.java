package EventTools.ShowEventsApp.Controls;

import java.awt.event.*;
import java.awt.GridLayout;
import javax.swing.*;

import gov.anl.ipns.MathTools.Geometry.SlicePlane3D;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.ViewTools.UI.*;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

public class sliceControl extends JPanel //implements IReceiveMessage
{
   private static final long  serialVersionUID  = 1L;
   private SliceSelectorSplitPaneUI scUI;
   private MessageCenter   message_center;
   private JFrame          sliceDisplay;
   
   public sliceControl(MessageCenter mc, int wasinModel )
   {
      message_center = mc;
      //message_center.addReceiver(this, Commands.SET_ORIENTATION_MATRIX);
/*      
      scUI = new SliceSelectorSplitPaneUI(ISlicePlaneSelector.QXYZ_MODE,
            inModel);
      scUI.addActionListener(new Action());
      //createFrame();
      setSliceInformation(inModel);
      this.add(scUI);
*/
   }
   
   public sliceControl(MessageCenter mc)
   {
      message_center = mc;
      scUI = new SliceSelectorSplitPaneUI(ISlicePlaneSelector.QXYZ_MODE);
      scUI.addActionListener(new Action());
      this.setLayout(new GridLayout(1,1));
      this.add(scUI);
   }
   
   public void setSliceInformation()   // modelData inModel)
   {
      SlicePlane3D plane = new SlicePlane3D();
/*      
      Vector3D center = inModel.getHistogram().xBinner().centerVec(scUI.getSliceNumber());
      center.add(inModel.getHistogram().yBinner().centerVec(scUI.getSliceNumber()));
      center.add(inModel.getHistogram().zBinner().centerVec(scUI.getSliceNumber()));

      plane.setOrigin(center);
      plane.setU_and_V(inModel.getBase(), inModel.getUp());
      scUI.setPlane(plane);
*/
   }
   
   public void setPlane(SlicePlane3D plane)
   {
      scUI.setPlane(plane);
   }
   
   public void setFrameInformation()   // modelData inModel)
   {
      //setSliceInformation(inModel);
//      scUI.setFrameData(inModel);
      //scUI.setImageData(inModel);
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
   
   /*public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.PLANE_CHANGED))
      {
         
         return true;
      }
      return false;
   }*/
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("sliceControl MessageCenter");
      TestReceiver tr = new TestReceiver("Test");

      mc.addReceiver(tr, Commands.PLANE_CHANGED);
      mc.addReceiver(tr, Commands.SLICE_MODE_CHANGED);
      mc.addReceiver(tr, Commands.SET_SLICE_1);
      
      sliceControl sc = new sliceControl(mc);//, new modelData(args[0]));

      JFrame View = new JFrame("Slice Control");
      View.setBounds(625,210,225,425);
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setVisible(true);
      
      //View.setLayout( new GridLayout(1,1) );
      View.add(sc);
    
      new UpdateManager( mc, null, 1000 );
   }
}
