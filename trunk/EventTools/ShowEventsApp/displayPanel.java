package EventTools.ShowEventsApp;

import javax.swing.*;
import java.awt.*;

import MessageTools.*;
import EventTools.ShowEventsApp.Command.*;

public class displayPanel extends JPanel
                          implements IReceiveMessage
{
   public static final long serialVersionUID = 1L;
   private MessageCenter    messageCenter;
   private JPanel           previousPanel = null;
   
   /**
    * Controls what panel is currently displayed.  Listens for 
    * Commands.CHANGE_PANEL and receives the panel in the message and
    * displays that panel.
    * 
    * @param messageCenter
    */
   public displayPanel(MessageCenter messageCenter)
   {
      this.setLayout(new GridLayout(1,1));
      this.messageCenter = messageCenter;
      this.messageCenter.addReceiver(this, Commands.CHANGE_PANEL);
   }
   
   private void setPanel(JPanel panel)
   {
      if (panel == null)
      {
         this.remove(previousPanel);
         return;
      }
      
      if (previousPanel != null)
         //if (previousPanel != panel)
            this.remove(previousPanel);
         //else
            //return;

      //panel.setBorder(new LineBorder(Color.BLACK));
      this.add(panel);
      previousPanel = panel;
      this.validate();
      this.repaint();
   }
   
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command,
                                    value,
                                    true);
      
      messageCenter.receive(message);
   }
   
   public boolean receive(Message message)
   {
      //System.out.println(message.getName() + " " + message.getValue());
      
      if (message.getName().equals(Commands.CHANGE_PANEL))
      {
         setPanel((JPanel)message.getValue());
         return true;
      }
      
      return false;
   }
}
