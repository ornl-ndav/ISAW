package EventTools.ShowEventsApp.Command;

import javax.swing.SwingUtilities;

import EventTools.ShowEventsApp.Command.Commands;
import MessageTools.*;



/**
 * 
 * @author Ruth
 *
 */
public class Util
{

   
   /**
    * The message center will send messages to all listeners for the command
    * 
    * @param message_center   The message center
    * @param Command          The command 
    * @param value            The argument to the command
    * @param replace          If true, replaces all other messages from this command
    */
   public static void sendMessage( MessageCenter message_center,
                                   String Command,
                                   Object value,
                                   boolean replace)
   {
      if( message_center == null || Command == null )
         return;
      message_center.receive( new Message(Command,value,replace) );
   }
   
   /**
    * The message center will send messages to all listeners for the command.
    * This request will be run in the AWT event dispatching thread via
    * the SwingUtilities.invokeLater method
    * 
    * @param message_center   The message center
    * @param Command          The command 
    * @param value            The argument to the command
    * @param replace          If true, replaces all other messages from this command
    */
   public static void sendMessageOnAWTThread( MessageCenter message_center,
                                           String Command,
                                          Object value,
                                        boolean replace)
   {
      if( message_center == null || Command == null )
         return;
      javax.swing.SwingUtilities.invokeLater(  
               new AWTThreadRun(new Message(Command,value,replace), 
                        message_center));
   }
   
   /**
    * Sends an error message to the applicable status panel
    * @param message_center   The message center
    * @param message          The error message
    */
   public static void sendError( MessageCenter message_center,  String message)
   {
      sendMessage(message_center,  Commands.DISPLAY_ERROR, message, false);
   }
   
   
   /**
    * Sends an informational message to the applicable status panel
    * @param message_center   The message center
    * @param message          The error message
    */
   public static void sendInfo( MessageCenter message_center,  String message)
   {
      sendMessage(message_center,  Commands.DISPLAY_INFO, message, false);
   } 
   /**
    * Sends a warning message to the applicable status panel
    * @param message_center   The message center
    * @param message          The error message
    */
   public static void sendWarning( MessageCenter message_center,  String message)
   {
      sendMessage(message_center,  Commands.DISPLAY_WARNING, message, false);
   } 
   /**
    * Sends a clear message to the applicable status panel
    * @param message_center   The message center
    * @param message          The error message
    */
   public static void sendClearStatusPanel( MessageCenter message_center,  String message)
   {
      sendMessage(message_center,  Commands.DISPLAY_CLEAR, message, false);
   }
}
class AWTThreadRun extends Thread
{
   Message message;
   MessageCenter message_center;
   public AWTThreadRun( Message message, 
                        MessageCenter message_center)
   {

      this.message = message;
      this.message_center = message_center; 
   }
   /* (non-Javadoc)
    * @see java.lang.Thread#run()
    */
   @Override
   public void run()
   {
      if( message_center != null)
         message_center.receive( message );
     
   }
   
}
