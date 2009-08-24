package EventTools.ShowEventsApp.ViewHandlers;

import java.awt.GridLayout;

import javax.swing.*;

import EventTools.ShowEventsApp.Command.Commands;
import gov.anl.ipns.ViewTools.Components.OneD.FunctionViewComponent;
import MessageTools.*;

public class DViewHandler implements IReceiveMessage
{
   private MessageCenter messageCenter;
   private JFrame        dDisplayFrame;
   private JPanel        graphPanel;
   private String        Title = "d-spacing";
   private String        x_units = "" + '\u00c5';
   private String        y_units = "weighted";
   private String        x_label = "d-spacing";
   private String        y_label = "Intensity";
   
   public DViewHandler(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      this.messageCenter.addReceiver(this, Commands.SHOW_D_GRAPH);
      this.messageCenter.addReceiver(this, Commands.HIDE_D_GRAPH);
      this.messageCenter.addReceiver(this, Commands.SET_D_VALUES);
      this.messageCenter.addReceiver(this, Commands.ADD_EVENTS_TO_VIEW);
   }
   
   private void displayDFrame()
   {
      dDisplayFrame = new JFrame("d-spacing View");
      dDisplayFrame.setLayout(new GridLayout(1,1));
      dDisplayFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      dDisplayFrame.setBounds(0, 0, 500, 300);
      dDisplayFrame.setVisible(true);
      
      if (graphPanel != null)
         dDisplayFrame.add(graphPanel);
      else
         dDisplayFrame.add(placeholderPanel());
      
      dDisplayFrame.repaint();
   }
   
   private JPanel placeholderPanel()
   {
      JPanel placeholderpanel = new JPanel();
      placeholderpanel.setLayout(new GridLayout(1,1));
      
      JLabel label = new JLabel("No Data Loaded!");
      label.setHorizontalAlignment(JLabel.CENTER);
      
      placeholderpanel.add(label);
      
      return placeholderpanel;
   }
   
   private void setPanelInformation(float[][] xyValues)
   {
      float[] x_values = xyValues[0];
      float[] y_values = xyValues[1];
      float[] errors = null;

      if(dDisplayFrame != null)
         dDisplayFrame.getContentPane().removeAll();

      graphPanel = FunctionViewComponent.ShowGraphWithAxes(x_values, y_values, errors, 
               Title, x_units, y_units, x_label, y_label);
      
      if (dDisplayFrame != null)
         dDisplayFrame.add(graphPanel);
   }
   
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);
      
      messageCenter.receive(message);
   }

   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.SHOW_D_GRAPH))
      {
         displayDFrame();
         
         return true;
      }
      
      if (message.getName().equals(Commands.ADD_EVENTS_TO_VIEW))
      {
         sendMessage(Commands.GET_D_VALUES, null);
      }
      
      if (message.getName().equals(Commands.HIDE_D_GRAPH))
      {
         dDisplayFrame.dispose();
         
         return true;
      }
      
      if (message.getName().equals(Commands.SET_D_VALUES))
      {
         setPanelInformation(((float[][])message.getValue()));
         
         if (dDisplayFrame != null)
            dDisplayFrame.validate();
         
         return true;
      }
      
      return false;
   }
}
