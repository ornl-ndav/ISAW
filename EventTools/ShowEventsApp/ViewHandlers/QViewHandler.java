package EventTools.ShowEventsApp.ViewHandlers;

import javax.swing.*;

import EventTools.ShowEventsApp.Command.Commands;
import gov.anl.ipns.ViewTools.Components.OneD.FunctionViewComponent;
import MessageTools.*;

public class QViewHandler implements IReceiveMessage
{
   private MessageCenter messageCenter;
   private JFrame        qDisplayFrame;
   private JPanel        graphPanel;
   private String        Title = "Magnitude Q";
   private String        x_units = "Inv(" + '\u00c5' + ")";
   private String        y_units = "weighted";
   private String        x_label = "Q";
   private String        y_label = "Intensity";
   
   public QViewHandler(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      this.messageCenter.addReceiver(this, Commands.SHOW_Q_GRAPH);
      this.messageCenter.addReceiver(this, Commands.HIDE_Q_GRAPH);
      this.messageCenter.addReceiver(this, Commands.SET_Q_VALUES);
      sendMessage(Commands.GET_Q_VALUES, null);
   }
   
   private void displayQFrame()
   {
      qDisplayFrame = new JFrame("Q View");
      qDisplayFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      qDisplayFrame.setBounds(0, 0, 500, 300);
      qDisplayFrame.setVisible(true);
      
      qDisplayFrame.add(graphPanel);
      qDisplayFrame.repaint();
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
   
   private void hideQFrame()
   {
      qDisplayFrame.dispose();
   }
   
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);
      
      messageCenter.receive(message);
   }

   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.SHOW_Q_GRAPH))
      {
         sendMessage(Commands.GET_Q_VALUES, null);
         displayQFrame();
         
         return true;
      }
      
      if (message.getName().equals(Commands.HIDE_Q_GRAPH))
      {
         hideQFrame();
         
         return true;
      }
      
      if (message.getName().equals(Commands.SET_Q_VALUES))
      {
         setPanelInformation(((float[][])message.getValue()));
         
         return true;
      }
      return false;
   }
}
