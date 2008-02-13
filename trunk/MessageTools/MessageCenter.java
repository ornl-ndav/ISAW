/*
 * File:  MessageCenter.java
 *
 * Copyright (C) 2005 Dennis Mikkelson
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
 *  $Log: MessageCenter.java,v $
 *  Revision 1.10  2007/10/15 04:22:27  dennis
 *    Improved efficiency in two ways.
 *    First, the private method sendMessage() now will return false if the
 *  list of receivers was empty.
 *    Second, sendMessage() now checks the return value of the receive method
 *  for the listeners, and only returns true if at least one receiver returned
 *  true.  As a result, if no messages were actually processed by receivers,
 *  it may not be necessary to redraw the display.
 *
 *  Revision 1.9  2007/10/14 17:19:22  dennis
 *  A few small fixes to javadoc comments.
 *
 *  Revision 1.8  2007/08/26 20:21:46  dennis
 *  Removed unneeded type casts.
 *  Commented empty blocks.
 *
 *  Revision 1.7  2007/08/25 05:02:52  dennis
 *  Parameterized raw types.
 *
 *  Revision 1.6  2006/11/09 02:39:43  dennis
 *  Removed one unused variable.
 *
 *  Revision 1.5  2006/11/02 22:48:43  dennis
 *  Added queue for notifying receivers when a batch of messages
 *  have been processed.
 *
 *  Revision 1.4  2006/10/30 00:56:41  dennis
 *  Modified main test program to use a TimedTrigger to trigger
 *  the processing of messages.
 *
 *  Revision 1.3  2005/11/30 17:59:01  dennis
 *  Separated the code to send one message into it's own private
 *  method.  This may be used in the future to notify objects that
 *  the processing of messages has been completed.
 *
 *  Revision 1.2  2005/11/29 01:54:11  dennis
 *  Finished implementing methods, added javadoc comments and some
 *  test code in the main program.
 *
 */

package MessageTools;

import java.util.*;

/**
 *    A MessageCenter object keeps lists of incoming Messages and lists 
 *  of IReceiveMesssage objects that have "subscribed" to receive messages.
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
 *    When a special message, MessageCenter.PROCESS_MESSAGES is passed to
 *  the MessageCenter.receive() method, all received messages currently in
 *  the named message queues will be routed to the registered receivers for
 *  those message queues.  Messages are processed based on their time stamp
 *  and on the order in which they have been received.  All messages are
 *  discarded by the MessageCenter, after they have been processed, whether
 *  or not their was a registered receiver for the message.  
 *
 *    The class TimedTrigger is a utility class that will send the
 *  MessageCenter.PROCESS_MESSAGES message to a MessageCenter at regular
 *  intervals, to process all messages in it's queue at regular intervals.
 *  Alternatively, for processing messages AND updating displays the utility
 *  class, SSG_Tools.Utils.UpdateManager should be used. 
 *
 *    A message center also has one special queue that is used to notify
 *  any interested IReceiveMessage objects, when a batch of messages have
 *  just been processed, in response to a MessageCenter.PROCESS_MESSAGES
 *  message.  The name of the special queue is the String 
 *  "MC_Queue:SequenceOfMessagesProcessed".  This queue name should NOT be 
 *  used for other purposes.  The name can be obtained by calling the 
 *  getProcessCompleteQueueName() method.  If an IReceiveMessage object
 *  adds itself as a receiver for such messages, then when a sequence of
 *  messages is processed, it will be sent a MessageCenter.MESSAGES_PROCESSED 
 *  message.
 */
public class MessageCenter implements IReceiveMessage
{
  private static final String DONE = "MC_Queue:SequenceOfMessagesProcessed";

  private String    center_name;
  private Hashtable<Object,Vector<Message>> message_table;
  private Hashtable<Object,Vector<IReceiveMessage>> receiver_table;
   
  private static long  tag_count = 0;
  public final static Message PROCESS_MESSAGES = 
                                             new Message( null, null, false );

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
    message_table  = new Hashtable<Object,Vector<Message>>();
    receiver_table = new Hashtable<Object,Vector<IReceiveMessage>>();
    center_name = name;
  }


  /* --------------------------- receive ---------------------------------- */
  /**
   *  Accept the specified message, and add it to the queue determined by
   *  the message name.  If the message is the special static message:
   *  MessageCenter.PROCESS_MESSAGES, then the current queues of messages
   *  will be dispatched to all objects that were specified to receive
   *  the named messages, using the addReceiver() method.
   *
   *  @param message  The message that is to be added to the queue.
   *
   *  @return  Returns true if the message was a valid "normal" message that
   *           was added to the queue, OR if the message was the "special"
   *           PROCESS_MESSAGES object, and a positive number of messages
   *           were actually processed.  Returns false if the message was
   *           invalid, or if the message was the "special" PROCESS_MESSAGES
   *           object, and there were no messages to process. 
   */
  public synchronized boolean receive( Message message )
  {
    if ( message == PROCESS_MESSAGES )
    {
       int num_processed = sendAll();

       if ( num_processed > 0 )
         return true;
       
       return false;
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

    Vector<Message> list = message_table.get( name );
    if ( list == null )
    {
      list = new Vector<Message>();
      message_table.put( name, list );
    }

    if ( message.replace() )
      list.clear();

    message.setTag( tag_count++ );       // record the tag count, to serve as
                                         // tie breaker when sorting
    list.add( message );

    return true;
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

    Vector<IReceiveMessage> list = receiver_table.get( name );
    if ( list == null )
      return true;

    list.removeElement( receiver );

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


  /* ----------------------------------------------------------------------
   *
   *  PRIVATE METHODS
   *
   */


  /* --------------------------- sendAll --------------------------------- */
  /**
   *  Dispatch all messages currently in the message queues to the registered
   *  receiver objects.
   *
   *  @return  The number of messages that were successfully processed.  
   *           NOTE: if no messages were actually processed, then the system 
   *           state did not change, so further processing may not be needed 
   *           at this time.  A message is considered to be successfully 
   *           processed only if there was actually at least one receiver to 
   *           receive that message.
   */
  private int sendAll()
  {                                  // get all messages from the message table
                                     // in an array, and sort based on time
    int num_messages = 0;
    Enumeration<Vector<Message>> lists = message_table.elements();
    Vector<Message> list;
    while ( lists.hasMoreElements() )
    {
      list = lists.nextElement();
      num_messages += list.size();
    }

    Message messages[] = new Message[ num_messages ];
    lists = message_table.elements();
    int index = 0;
    while ( lists.hasMoreElements() )
    {
      list = lists.nextElement();
      for ( int i = 0; i < list.size(); i++ )
      {
        messages[index] = list.elementAt(i);
        index++;
      }
      list.clear();
    }
    message_table.clear();

    Arrays.sort( messages, new MessageComparator() );

                                             // now route the ordered messages
                                             // to the receivers, and increment
                                             // the number of messages sent, if
                                             // there were any receivers.
    int num_sent = 0;
    for ( int i = 0; i < messages.length; i++ )
      if ( sendMessage( messages[i] ) )
        num_sent++;
                                             // Send the MESSAGES_PROCESSED 
                                             // message, and increment the
                                             // number of messages sent, if
                                             // there were any receivers
    if ( sendMessage( MESSAGES_PROCESSED ) )
      num_sent++;

    return num_sent;
  }


  /* -------------------------- sendMessage ------------------------------ */
  /**
   *  Dispatch the specified message to the registered receiver objects.
   *
   *  @param message  The message to send to receivers registered to get
   *                  messages with that name.
   *
   *  @return True if some messages were successfully processed by some
   *          receivers of this message.
   */
  private boolean sendMessage( Message message )
  {
    Vector<IReceiveMessage> listeners = receiver_table.get( message.getName() );
 
    boolean some_processed = false;
    if ( listeners != null && listeners.size() > 0 )
    {
      for ( int j = 0; j < listeners.size(); j++ )
        if ( listeners.elementAt(j).receive( message ) )
          some_processed = true;
    }
  
    return some_processed;
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
    
    TestCenter.receive( new Message( "Queue 1", new Integer(1), false ) );    
    TestCenter.receive( new Message( "Queue 1", new Integer(2), false ) );    
    TestCenter.receive( new Message( "Queue 1", new Integer(3), false ) );    
    TestCenter.receive( new Message( "Queue 1", new Integer(4), false ) );    
    TestCenter.receive( new Message( "Queue 2", new Integer(5), false ) );    
    TestCenter.receive( new Message( "Queue 2", new Integer(6), false ) );    
    TestCenter.receive( new Message( "Queue 2", new Integer(7), false ) );    
    TestCenter.receive( new Message( "Queue 1", new Integer(8), false ) );    
    TestCenter.receive( new Message( "Queue 1", new Integer(9), false ) ); 

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

    TestCenter.receive( new Message( "Queue 1", new Integer(11), false ) );
    TestCenter.receive( new Message( "Queue 1", new Integer(12), false ) );
    TestCenter.receive( new Message( "Queue 1", new Integer(13), false ) );
    TestCenter.receive( new Message( "Queue 1", new Integer(14), false ) );
    TestCenter.receive( new Message( "Queue 2", new Integer(15), false ) );
    TestCenter.receive( new Message( "Queue 2", new Integer(16), false ) );
    TestCenter.receive( new Message( "Queue 2", new Integer(17), false ) );
    TestCenter.receive( new Message( "Queue 1", new Integer(18), true ) );
    TestCenter.receive( new Message( "Queue 1", new Integer(19), false ) );
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
