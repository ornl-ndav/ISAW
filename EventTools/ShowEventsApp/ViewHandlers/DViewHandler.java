package EventTools.ShowEventsApp.ViewHandlers;

import javax.swing.JFrame;
import javax.swing.JPanel;

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
      sendMessage(Commands.GET_D_VALUES, null);
   }
   
   private void setPanelInformation(float[][] xyValues)
   {
      float[] x_values = xyValues[0];
      float[] y_values = xyValues[1];
      float[] errors = null;

      
      graphPanel = 
         FunctionViewComponent.ShowGraphWithAxes(x_values, y_values, errors, 
               Title, x_units, y_units, x_label, y_label);
   }
   
   private void displayDFrame()
   {
      dDisplayFrame = new JFrame("Q View");
      dDisplayFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      dDisplayFrame.setBounds(0, 0, 500, 300);
      dDisplayFrame.setVisible(true);
      
      dDisplayFrame.add(graphPanel);
      dDisplayFrame.repaint();
   }
   
   private void hideDFrame()
   {
      dDisplayFrame.dispose();
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
         sendMessage(Commands.GET_D_VALUES, null);
         displayDFrame();
         
         return true;
      }
      
      if (message.getName().equals(Commands.HIDE_D_GRAPH))
      {
         hideDFrame();
         
         return true;
      }
      
      if (message.getName().equals(Commands.SET_D_VALUES))
      {
         setPanelInformation(((float[][])message.getValue()));
         
         return true;
      }
      return false;
   }
}
