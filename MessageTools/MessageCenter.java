/*
 * File:  MessageCenter.java
 *
 * Copyright (C) 2005-2009 Dennis Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * Modified:
 *
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package MessageTools;

import java.util.*;

/**
 *    A MessageCenter object keeps lists of incoming Messages and lists 
 *  of IReceiveMesssage objects that have "subscribed" to receive messages
 *  from a particular named message queue.
 *
 *    The incoming Messages are directed to "named" queues of messages, 
 *  using the MessageCenter.receive() method.  The name of the queue is
 *  built into the Message when the Message is constructed.  Although the 
 *  name is declared as an Object, it is expected that the names will 
 *  normally be Strings.  
 *
 *    IReceiveMessage objects can register to receive messages from a
 *  particular named queue, using the MessageCenter.addReceiver() method.
 *
 *    When the dispatchMessages() method is called, all received messages 
 *  currently in the named message queues will be routed to the registered
 *  receivers for those message queues.  Messages are processed based on 
 *  their time stamp and on the order in which they have been received.  
 *  All messages are discarded by the MessageCenter, after they have been 
 *  processed, whether or not their was a registered receiver for the message.  
 *
 *    The class TimedTrigger is a utility class that will call the  
 *  dispatchMessages() method at regular intervals, to trigger processing.  
 *  Alternatively, for processing messages AND updating displays the utility
 *  class, SSG_Tools.Utils.UpdateManager should be used. 
 *
 *    A message center also has one special queue that is used to notify
 *  any interested IReceiveMessage objects, when a batch of messages have
 *  just been processed.  The name of the special queue is the String 
 *  "MC_Queue:SequenceOfMessagesProcessed".  This queue name should NOT be 
 *  used for other purposes.  The name can be obtained by calling the 
 *  getProcessCompleteQueueName() method.  If an IReceiveMessage object
 *  adds itself as a receiver for such messages, then when a sequence of
 *  messages is processed, it will be sent a MessageCenter.MESSAGES_PROCESSED 
 *  message, provided some messages were processed and returned a value true.
 */
public class MessageCenter
{
  private static final String DONE = "MC_Queue:SequenceOfMessagesProcessed";

  private boolean debug_send    = false;
  private boolean debug_receive = false;

  private String    center_name;

  private Hashtable<Object,Vector<IReceiveMessage>> receiver_table;
  private Hashtable<Object,Vector<Message>> message_table;

  private Hashtable<Object,Vector<IReceiveMessage>> sender_receiver_table;
  private Hashtable<Object,Vector<Message>> sender_message_table;
   
  private static long  tag_count   = 0;
  private boolean      sender_busy = false;
  private Object       lists_lock = new Object();  // lock for message_table
                                                   // and receiver_table

  public final static Message MESSAGES_PROCESSED =
                                             new Message( DONE, null, true );


  /* ------------------------- constructor ------------------------------- */
  /**
   *  Construct a new message center with the specified name and empty
   *  message and receiver lists.
   *
   *  @param  name  The String name assigned to this message center.
   */
  public MessageCenter( String name )
  {
    receiver_table        = new Hashtable<Object,Vector<IReceiveMessage>>();
    message_table         = new Hashtable<Object,Vector<Message>>();
    sender_receiver_table = null;
    sender_message_table  = null;
    center_name = name;

    MessageSenderThread sender = new MessageSenderThread();
    sender.start();
  }


  /* --------------------------- send ---------------------------------- */
  /**
   *  Accept the specified message, and add it to the queue determined by
   *  the message name.  When the message center processes messages, the
   *  message will be sent to any receivers that were previously added
   *  to the list of objects that receive messages from that queue,
   *  using the addReceiver() method.
   *
   *  @param message  The message that is to be added to the queue.
   *
   *  @return  Returns true if the message was a valid "normal" message that
   *           was added to the queue.  Returns false if the message was
   *           invalid, or if the message was the "special" PROCESS_MESSAGES.
   */
  public synchronized boolean send( Message message )
  {
    if ( debug_receive )
    {
      System.out.println( "\n**** RECEIVED MESSAGE **** : ");
      System.out.println( "QUEUE: " + message.getName() );
      System.out.println( "VALUE: " + message.getValue() );
      System.out.println();
    }

    if ( message == null )
    {
      System.out.println("Warning: null message in " 
                        + center_name + " MessageCenter.receive()");
      return false;
    }

    Object name = message.getName();
    if ( name == null )
    {
      System.out.println("Warning: null message name in "
                        + center_name + " MessageCenter.receive()");
      return false;
    }

    synchronized(lists_lock)
    {
      Vector<Message> list = message_table.get( name );
      if ( list == null )
      {
        list = new Vector<Message>();
        message_table.put( name, list );
      }

      if ( message.replace() )
        list.clear();

      message.setTag( tag_count++ );     // record the tag count, to serve as
                                         // tie breaker when sorting
      list.add( message );
    }

    return true;
  }


 /* --------------------------- dispatchMessages ------------------------ */
  /**
   * Send out all messages currently in the message table.
   * 
   * @return false if the mesage table is empty, or the messages
   *               can't currently be sent out because the message sending
   *               thread is busy.
   */
  public boolean dispatchMessages()
  {                                
    synchronized(lists_lock)
    {
       if ( sender_busy )                   // wait for later update triggers
         return false;                      // to process more messages

       if ( message_table.size() <= 0 )     // nothing to send
         return false;
                                            // grab all current messages
       sender_message_table = message_table;
       message_table = new Hashtable<Object,Vector<Message>>();

                                            // get copy of lists of receivers
       sender_receiver_table = new Hashtable<Object,Vector<IReceiveMessage>>();

       Vector<IReceiveMessage> list;
       Vector<IReceiveMessage> new_list;
       Enumeration keys = receiver_table.keys();
       while ( keys.hasMoreElements() )
       {
         Object key = keys.nextElement();
         list = receiver_table.get( key );
         new_list = new Vector<IReceiveMessage>();
         for ( int i = 0; i < list.size(); i++ )
           new_list.add( list.elementAt(i) );
         sender_receiver_table.put( key, new_list );
       }
       
       sender_busy = true;                  // trip flag so the sender thread
                                            // will process messages
       return false;
    }
  }
  
  
  /* --------------------------- addReceiver ------------------------------ */
  /**
   *  Add the specified receiver object to the specified message queue.  If
   *  the named queue does not already exist, it will be created.  The same
   *  receiver object cannot be added to one queue more than one time.
   *
   *  @param  receiver  The receiver object to be removed.
   *  @param  name      The message queue to which the receiver is to
   *                    be added.
   *
   *  @return  Return true if the receiver and queue name were valid, and
   *           false otherwise.
   */
  public synchronized boolean addReceiver( IReceiveMessage receiver, 
                                           Object          name )
  {
    if ( receiver == null )
    {
      System.out.println("Warning: null receiver in "
                        + center_name + " MessageCenter.addReceiver()");
      return false;
    }

    if ( name == null )
    {
      System.out.println("Warning: null message name in "
                        + center_name + " MessageCenter.addReceiver()");
      return false;
    }

    synchronized( lists_lock )
    {
      Vector<IReceiveMessage> list = receiver_table.get( name );
      if ( list == null )
      {
        list = new Vector<IReceiveMessage>();
        receiver_table.put( name, list );
      }

      boolean already_in_list = false;
      int i = 0;
      while ( i < list.size() && !already_in_list )
      {
        if ( list.elementAt(i) == receiver )
          already_in_list = true;
        i++;
      }

      if ( already_in_list )
      {
        System.out.println("Warning: receiver already in list in "
                          + center_name + " MessageCenter.addReceiver()");
      }
      else
        list.add( receiver );
    }

    return true;
  }


  /* ------------------------- removeReceiver ---------------------------- */
  /**
   *  Remove the specified receiver object from the specified message queue.
   *
   *  @param  receiver  The receiver object to be removed.
   *  @param  name      The message queue from which the receiver is to
   *                    be removed.
   *
   *  @return  Return true if the receiver and queue name were valid, and
   *           false otherwise.
   */
  public synchronized boolean removeReceiver( IReceiveMessage receiver, 
                                              Object          name )
  {
    if ( receiver == null )
    {
      System.out.println("Warning: null receiver in "
                        + center_name + " MessageCenter.removeReceiver()");
      return false;
    }

    if ( name == null )
    {
      System.out.println("Warning: null message name in "
                        + center_name + " MessageCenter.removeReceiver()");
      return false;
    }

    synchronized( lists_lock )
    {
      Vector<IReceiveMessage> list = receiver_table.get( name );
      if ( list == null )
        return true;

      list.removeElement( receiver );
    }

    return true;
  }


  /* --------------------- getProcessCompleteQueueName ------------------- */
  /**
   *  Get the name of the queue for the special MESSAGE_PROCESSING_COMPLETE 
   *  message.  Subscribers to this message queue will be sent a 
   *  MESSAGE_PROCESSING_COMPLETE message whenever the message center has
   *  processed all pending messages.  IReceiveMessage objects should 
   *  subscribe to this queue (using the addReceiver() method), if they
   *  need to be informed when a message processing cycle is complete.
   *
   *  @return the name of the queue for MESSAGE_PROCESSING_COMPLETE
   *          messages.
   */
  public String getProcessCompleteQueueName()
  {
    return DONE; 
  }


  /**
   *  Set debugging state for messages that are sent out from the message
   *  center.
   *  
   *  @param debug_on_off  If true, every message that is sent from this
   *                       message center will print an informational
   *                       message on the console.  If false the message
   *                       center will operate silently.
   */
  public void setDebugSend( boolean debug_on_off )
  {
    debug_send = debug_on_off;
  }


  /**
   *  Set debugging state for messages that are received by the message
   *  center.
   *  
   *  @param debug_on_off  If true, every message that is received by this
   *                       message center will print an informational
   *                       message on the console.
   */
  public void setDebugReceive( boolean debug_on_off )
  {
    debug_receive = debug_on_off;
  }


  /* ----------------------------------------------------------------------
   *
   *  PRIVATE METHODS
   *
   */


  /* --------------------------- sendAll --------------------------------- */
  /**
   *  Dispatch all messages currently in the specified table of message 
   *  queues to the registered receiver objects in the specified table of
   *  receivers.
   *
   *  @param   my_message_table   The table of messages to be sent.
   *  @param   my_receiver_table  The table of receivers for various
   *                              message queues.
   *
   *  @return  The number of messages that were successfully processed and
   *           returned the value true. 
   *           NOTE: if no messages were actually processed, then the system 
   *           state did not change, so further processing may not be needed 
   *           at this time.  If a receiver returns the value true, then 
   *           this indicates that something was changed that will require
   *           IUpdatable objects to be redrawn.  Messages run in a separate
   *           thread cannot return any value so the do not return true.
   */
  private int sendAll( 
           Hashtable<Object,Vector<Message>> my_message_table,
           Hashtable<Object,Vector<IReceiveMessage>> my_receiver_table ) 
  {
                                     // get all messages from the message table
                                     // in an array, and sort based on time
    int num_messages = 0;
    Enumeration<Vector<Message>> lists = my_message_table.elements();
    Vector<Message> list;
    while ( lists.hasMoreElements() )
    {
      list = lists.nextElement();
      num_messages += list.size();
    }

    Message messages[] = new Message[ num_messages ];
    lists = my_message_table.elements();
    int index = 0;
    while ( lists.hasMoreElements() )
    {
      list = lists.nextElement();
      for ( int i = 0; i < list.size(); i++ )
      {
        messages[index] = list.elementAt(i);
        index++;
      }
    }

    Arrays.sort( messages, new MessageComparator() );

                                             // now route the ordered messages
                                             // to the receivers, and increment
                                             // the number of messages sent, if
                                             // there were any receivers.
    int num_true = 0;
    for ( int i = 0; i < messages.length; i++ )
      if ( sendMessage( messages[i], my_receiver_table ) )
        num_true++;
                                             // Send the MESSAGES_PROCESSED 
                                             // message, if sending some
                                             // messages returned true
    if ( num_true > 0 )
      sendMessage( MESSAGES_PROCESSED, my_receiver_table );

    return num_true;
  }


  /* -------------------------- sendMessage ------------------------------ */
  /**
   *  Dispatch the specified message to the registered receiver objects.
   *
   *  @param message            The message to send to receivers registered
   *                            to get messages with that name.
   *  @param my_receiver_table  The table of receivers for various
   *                            message queues.
   *
   *  @return True if some receiver objects returned true, indicating that
   *          the receiver altered something that will require any 
   *          IUpdateable objects to be redrawn.  The value returned by
   *          any messages that were delivered in a separate thread WILL 
   *          NOT be tracked or affect the value returned by this method.
   */
  private boolean sendMessage( 
               Message message, 
               Hashtable<Object,Vector<IReceiveMessage>> my_receiver_table )
  {
    Vector<IReceiveMessage> listeners = 
                                my_receiver_table.get( message.getName() );

    boolean some_changed = false;
    if ( listeners != null && listeners.size() > 0 )
    {
      if ( debug_send )
      {
        System.out.println( "\n**** SENDING MESSAGE **** : " );
        System.out.println( "QUEUE: " + message.getName() );
        System.out.println( "VALUE: " + message.getValue() );

        if ( listeners.size() <= 0 )
          System.out.println("+++++ WARNING +++++ : NO RECEIVERS!!");
      }

      for ( int j = 0; j < listeners.size(); j++ )
      {
        if ( debug_send )
          System.out.println("SENT TO -->" + listeners.elementAt(j));

        if ( message.useNewThread() )               // launch a new Thread
        {
          IReceiveMessage receiver = listeners.elementAt(j);
          Thread sender = new SendOneMessageThread( message, receiver );
          sender.start();
        }
                                                    // just call receive()
        else if ( listeners.elementAt(j).receive( message ) )
          some_changed = true;
      }
    }

    return some_changed;
  }


  /**
   *  This class is the Thread that actually sends out the messages.  
   *  It will run as long as the application is running.  Whenever the
   *  dispatchMessages() method trips the "sender_busy" flag, this
   *  thread will send out all messages currently in the 
   *  "sender_message_table" to appropriate receivers, as listed in the
   *  "sender_receiver_table".
   */
  private class MessageSenderThread extends Thread
  {
    public void run()
    {
      while ( true )                       // keep looping forever
      {
        if ( sender_busy )
        {
          int num_sent = 0;
          try
          {
            sendAll( sender_message_table, sender_receiver_table );
          }
          catch ( Throwable ex )
          {
            System.out.println("Exception processing messages : " + ex );
            ex.printStackTrace();
          }
          finally
          {
            sender_busy = false;
          }
        }
        try
        {
          Thread.sleep(30);
        }
        catch ( Exception ex )
        {
          System.out.println("Exception sleeping in MessageCenter.sender");
        }
      }
    }
  }


  /**
   *  This class is used to send a message in a separate thread, if that
   *  option has been set for the message.
   */
  private class SendOneMessageThread extends Thread
  {
    private Message         message;
    private IReceiveMessage receiver;

    private SendOneMessageThread( Message message, IReceiveMessage receiver )
    {
      this.message  = message;
      this.receiver = receiver;
    }

    public void run()
    {
      receiver.receive( message );
    }
  }


  /* ------------------------------ main -------------------------------- */
  /**
   *  Main program for testing purposes.
   */  
  public static void main( String args[] )
  {
    System.out.println("Start test...");
    MessageCenter TestCenter = new MessageCenter("TestCenter");
    new TimedTrigger( TestCenter, 1500 );

    IReceiveMessage receiver_1 = new TestReceiver( "receiver_1" );
    IReceiveMessage receiver_2 = new TestReceiver( "receiver_2" );

    TestCenter.addReceiver( receiver_1, "Queue 1" );
    TestCenter.addReceiver( receiver_2, "Queue 2" );
    
    TestCenter.send( new Message( "Queue 1", new Integer(1), false ) );    
    TestCenter.send( new Message( "Queue 1", new Integer(2), false ) );    
    TestCenter.send( new Message( "Queue 1", new Integer(3), false ) );    
    TestCenter.send( new Message( "Queue 1", new Integer(4), false ) );    
    TestCenter.send( new Message( "Queue 2", new Integer(5), false ) );    
    TestCenter.send( new Message( "Queue 2", new Integer(6), false ) );    
    TestCenter.send( new Message( "Queue 2", new Integer(7), false ) );    
    TestCenter.send( new Message( "Queue 1", new Integer(8), false ) );    
    TestCenter.send( new Message( "Queue 1", new Integer(9), false ) ); 

    System.out.println("\nSent 9 messages to 2 queues...");
    
    try
    {
      System.out.println("Sleeping for 3 seconds...");
      Thread.sleep( 3000 );
    }
    catch ( Exception e )
    { /* Nothing should go wrong here */}

//    TestCenter.receive( PROCESS_MESSAGES );

    TestCenter.removeReceiver( receiver_2, "Queue 2" );

    TestCenter.send( new Message( "Queue 1", new Integer(11), false ) );
    TestCenter.send( new Message( "Queue 1", new Integer(12), false ) );
    TestCenter.send( new Message( "Queue 1", new Integer(13), false ) );
    TestCenter.send( new Message( "Queue 1", new Integer(14), false ) );
    TestCenter.send( new Message( "Queue 2", new Integer(15), false ) );
    TestCenter.send( new Message( "Queue 2", new Integer(16), false ) );
    TestCenter.send( new Message( "Queue 2", new Integer(17), false ) );
    TestCenter.send( new Message( "Queue 1", new Integer(18), true ) );
    TestCenter.send( new Message( "Queue 1", new Integer(19), false ) );
    System.out.println("\nSent 9 messages to 2 queues, but");
    System.out.println("removed queue 2's receiver, and collapsed all");
    System.out.println("but last two messages to queue 1");

//    TestCenter.receive( PROCESS_MESSAGES );

    try
    {
      System.out.println("Sleeping for 3 more seconds...");
      Thread.sleep( 3000 );
    }
    catch ( Exception e )
    { /* Nothing should go wrong here */ }

    System.out.println("End test...");
  }

}
