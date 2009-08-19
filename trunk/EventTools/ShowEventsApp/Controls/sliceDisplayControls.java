package EventTools.ShowEventsApp.Controls;

import java.awt.GridLayout;
import java.awt.event.*;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.DisplaySliceCmd;
import EventTools.ShowEventsApp.Command.DisplaySliceCmd.moveSlice;
import EventTools.ShowEventsApp.Controls.HistogramControls.FrameController;
import MessageTools.*;

public class sliceDisplayControls extends JPanel
{
   public static final long  serialVersionUID = 1L;
   private MessageCenter     messageCenter;
   private FrameController   frame_control;
   private JCheckBox         showImageOne;
   private JCheckBox         showImageTwo;
   private JCheckBox         showImageThree;
   private JCheckBox         sliceOneCbx;
   private JCheckBox         sliceTwoCbx;
   private JCheckBox         sliceThreeCbx;
   private JRadioButton      moveSliceOne;
   private JRadioButton      moveSliceTwo;
   private JRadioButton      moveSliceThree;
   
   public sliceDisplayControls(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      this.setBorder(new TitledBorder("3D Slice Control"));
      this.setLayout(new GridLayout(1,1));
      
      Box box = new Box( BoxLayout.Y_AXIS );
      box.add(buildSliceDisplayOptions());
      box.add(buildSliceMoveOptions());
      
      this.add(box);
   }
   
   private JPanel buildSliceDisplayOptions()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(2,3));
      
      showImageOne = new JCheckBox("Show Image X");
      showImageOne.addActionListener(new actionListener());
      
      showImageTwo = new JCheckBox("Show Image Y");
      showImageTwo.addActionListener(new actionListener());
      
      showImageThree = new JCheckBox("Show Image Z");
      showImageThree.addActionListener(new actionListener());
      
      sliceOneCbx = new JCheckBox("Slice X");
      sliceOneCbx.addActionListener(new actionListener());
      
      sliceTwoCbx = new JCheckBox("Slice Y");
      sliceTwoCbx.addActionListener(new actionListener());
      
      sliceThreeCbx = new JCheckBox("Slice Z");
      sliceThreeCbx.addActionListener(new actionListener());

      panel.add(showImageOne);
      panel.add(showImageTwo);
      panel.add(showImageThree);
      
      panel.add(sliceOneCbx);
      panel.add(sliceTwoCbx);
      panel.add(sliceThreeCbx);
      
      return panel;
   }
   
   private JPanel buildSliceMoveOptions()
   {
      JPanel outerPanel = new JPanel();
      outerPanel.setLayout(new GridLayout(1,1));
      outerPanel.setBorder(new TitledBorder("Slice Shift"));
      
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,3));
      ButtonGroup group = new ButtonGroup();

      moveSliceOne = new JRadioButton("Move X");
      moveSliceTwo = new JRadioButton("Move Y");
      moveSliceThree = new JRadioButton("Move Z");
      
      group.add(moveSliceOne);
      group.add(moveSliceTwo);
      group.add(moveSliceThree);
      
      panel.add(moveSliceOne);
      panel.add(moveSliceTwo);
      panel.add(moveSliceThree);
      
      frame_control  = new FrameController();
      frame_control.addActionListener(new actionListener());
      
      Box box = new Box( BoxLayout.Y_AXIS );
      box.add(panel);
      box.add(frame_control);

      outerPanel.add(box);
      
      return outerPanel;
   }
   
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);
      
      messageCenter.receive(message);
   }
   
   private class actionListener implements ActionListener
   {
      public void actionPerformed(ActionEvent ae)
      {
         moveSlice move = null;
         
         if (moveSliceOne.isSelected())
            move = moveSlice.X;
         
         if (moveSliceTwo.isSelected())
            move = moveSlice.Y;
         
         if (moveSliceThree.isSelected())
            move = moveSlice.Z;
         
         DisplaySliceCmd displaySlice = 
            new DisplaySliceCmd(showImageOne.isSelected(),
                                showImageTwo.isSelected(),
                                showImageThree.isSelected(),
                                sliceOneCbx.isSelected(),
                                sliceTwoCbx.isSelected(),
                                sliceThreeCbx.isSelected(),
                                move, frame_control.getFrameNumber());
         
         sendMessage(Commands.SET_SLICE_1, displaySlice);
      }
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Testing MessageCenter");
      TestReceiver tc = new TestReceiver("Slice Panel TestingMessages");
      mc.addReceiver(tc, Commands.SET_SLICE_1);
      
      sliceDisplayControls sdc = new sliceDisplayControls(mc);
      
      JFrame View = new JFrame( "Test Slice Panel" );
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setBounds(10,10, 375, 375);
      View.setVisible(true);
      
      View.add(sdc);
      new UpdateManager(mc, null, 100);
   }
}
