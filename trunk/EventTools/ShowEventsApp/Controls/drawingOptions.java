package EventTools.ShowEventsApp.Controls;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

public class drawingOptions extends JPanel
{
   public static final long serialVersionUID = 1L;
   private MessageCenter    messageCenter;
   private JCheckBox        filterAbove;
   private JCheckBox        filterBelow;
   private JCheckBox        useAlpha;
   private JTextField       pointSize;
   private JTextField       alphaValue;
   private JCheckBox        orthographic;
   
   public drawingOptions(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      this.setBorder(new TitledBorder("Display Options"));
      this.setLayout(new GridLayout(6,1));
      
      this.add(buildOrtho());
      this.add(buildMin());
      this.add(buildMax());
      this.add(buildPointSize());
      this.add(builduseAlpha());
      
      JButton apply = new JButton("Apply");
      apply.addActionListener(new displayListener());
      
      this.add(apply);
   }
   
   private JPanel buildOrtho()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));
      
      orthographic = new JCheckBox("Orthographic View");
      
      panel.add(orthographic);

      return panel;
   }
   
   private JPanel buildMax()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));
      
      filterAbove = new JCheckBox("Filter Above Max");
      
      panel.add(filterAbove);
      
      return panel;
   }
   
   private JPanel buildMin()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));

      filterBelow = new JCheckBox("Filter Below Min");
      filterBelow.setSelected(true);
      
      panel.add(filterBelow);
      
      return panel;
   }
   
   private JPanel buildPointSize()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));

      JLabel label = new JLabel("Point Size");
      pointSize = new JTextField();
      pointSize.setHorizontalAlignment(JTextField.RIGHT);

      panel.add(label);
      panel.add(pointSize);
      
      return panel;
   }
   
   private JPanel builduseAlpha()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));

      useAlpha = new JCheckBox("Use Alpha");
      alphaValue = new JTextField();
      alphaValue.setHorizontalAlignment(JTextField.RIGHT);
      alphaValue.setText("1.0");
      
      panel.add(useAlpha);
      panel.add(alphaValue);
      
      return panel;
   }
   
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);
      
      messageCenter.receive(message);
   }
   
   private boolean valid()
   {
      if(pointSize.getText().equals(""))
      {
         String error = "You have not specified the Point size!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Float.parseFloat(pointSize.getText());
      }
      catch (NumberFormatException nfe)
      {
         String error = "Point size must be a float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      if(alphaValue.getText().equals(""))
      {
         String error = "You have not specified the alpha value!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      float num;
      
      try
      {
         num = Float.parseFloat(alphaValue.getText());
      }
      catch (NumberFormatException nfe)
      {
         String error = "Alpha value must be a float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      if (num > 1.0f || num < 0.0f)
      {
         String error = "Alpha value must be between 0.0 and 1.0!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      return true;
   }
   
   private class displayListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         if (valid())
         {
            FilterOptionsCmd filters = 
               new FilterOptionsCmd(orthographic.isSelected(),
                           filterBelow.isSelected(),
                           filterAbove.isSelected(),
                           Float.parseFloat(pointSize.getText()),
                           useAlpha.isSelected(),
                           Float.parseFloat(alphaValue.getText()));
            
            sendMessage(Commands.SET_FILTER_OPTIONS, filters);
         }
      }
   }

   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Testing MessageCenter");
      TestReceiver tc = new TestReceiver("FilePanel TestingMessages");
      mc.addReceiver(tc, Commands.SET_FILTER_OPTIONS);
      
      drawingOptions draw = new drawingOptions(mc);

      JFrame View = new JFrame( "Test File Panel" );
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setBounds(10,10, 300, 275);
      View.setVisible(true);
      
      View.add(draw);
     
      new UpdateManager(mc, null, 100);
   }
}
