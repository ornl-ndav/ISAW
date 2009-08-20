package EventTools.ShowEventsApp.DataHandlers;

import EventTools.ShowEventsApp.Command.Commands;
import MessageTools.*;

public class DQDataHandler implements IReceiveMessage
{
   private MessageCenter messageCenter;
   
   public DQDataHandler(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      this.messageCenter.addReceiver(this, Commands.ADD_EVENTS);
      this.messageCenter.addReceiver(this, Commands.CLEAR_DQ);
      this.messageCenter.addReceiver(this, Commands.GET_D_VALUES);
      this.messageCenter.addReceiver(this, Commands.GET_Q_VALUES);
   }
   
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);
      
      messageCenter.receive(message);
   }
   
   private float[][] buildGraphData()
   {
      // Sample data for now. replace!
      float[][] xy = new float[2][500];
      
      for(int i = 0; i < 2; i++)
         for(int j = 0; j < 500; j++)
            xy[i][j] = j;
            
      return xy;
   }
   
   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.ADD_EVENTS))
      {
         return true;
      }
      
      if (message.getName().equals(Commands.CLEAR_DQ))
      {
         return true;
      }
      
      if (message.getName().equals(Commands.GET_D_VALUES))
      {
         sendMessage(Commands.SET_D_VALUES, buildGraphData());
         
         return true;
      }
      
      if (message.getName().equals(Commands.GET_Q_VALUES))
      {
         sendMessage(Commands.SET_Q_VALUES, buildGraphData());
         return true;
      }
      
      return false;
   }
}
