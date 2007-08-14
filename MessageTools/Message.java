/*
 * File:  Message.java
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
 *  $Log$
 *  Revision 1.1  2007/08/14 00:09:00  dennis
 *  Added MessageTools files from UW-Stout repository.
 *
 *  Revision 1.4  2006/10/31 00:36:51  dennis
 *  Fixed calculation of time stamp to be in milliseconds, after
 *  switch to using System.nanoTime().
 *
 *  Revision 1.3  2006/10/23 00:16:43  dennis
 *  Switched time stamp to a double, obtained System.nanoTime()/1.0e6
 *
 *  Revision 1.2  2005/11/29 01:55:53  dennis
 *  Added methods to get/set a tag value.
 *  Added javadoc comments.
 *
 */

package MessageTools;

/**
 *    A Message object is a bundle of information for one message handled by 
 *  a MessageCenter.  The information set by the application is immutable
 *  and is established at construction time.   The typical use of this
 *  object is that some control will construct a message object, and give
 *  that message object to a MessageCenter object.  The MessageCenter will
 *  place the message in the named queue and process the message along with
 *  its other messages.
 *    When the message is constructed, the following information is required: 
 *  the name of the message queue it is to be placed in; an object containing
 *  the value; and a boolean indicating whether or not previous messages
 *  in the queue should be replaced by the new message.  The message queue
 *  name will usually be a String.  
 *    When the message is constructed it is given a time stamp.  This time
 *  stamp is used to guarantee that messages are delivered in increasing time
 *  order.  If the time stamps of two messages are the same, the order that
 *  they arrived at the MessageCenter will be used to break the tie. 
 */
public class Message
{
  private Object  name;
  private Object  value;
  private boolean replace_flag;
  private double  time_stamp;
  private long    tag;


  /* ---------------------------- constructor ---------------------------- */
  /** 
   *  Construct a new message to be placed in the specified message queue, 
   *  with the specified value.
   *
   *  @param name          The "name" of the message queue in which the 
   *                       message is to be placed.  This will usually be a 
   *                       String.
   *  @param value         Object containing the "value" sent by this message.
   *                       Value objects MUST be of the type expected by 
   *                       receivers for this message.
   *  @param replace_flag  Set this flag to true if this message is to 
   *                       replace all previous messages in the named 
   *                       queue.
   */
  public Message( Object name, Object value, boolean replace_flag )
  {
    this.name         = name;
    this.value        = value;
    this.replace_flag = replace_flag;
    this.time_stamp   = System.nanoTime()/1.0E6;
    this.tag          = 0;
  }


  /* ---------------------------- getName ---------------------------- */
  /**
   *  Get the Object used as the name of the message queue, in which this 
   *  message should be place.  
   *
   *  @return the "name" of the queue for this message.
   */
  public Object getName()
  {
    return name;
  }


  /* ---------------------------- getValue ---------------------------- */
  /**
   *  Get the Object used as the value of this message.
   *
   *  @return the "value" object of this message.
   */
  public Object getValue()
  {
    return value;
  }


  /* ---------------------------- replace ----------------------------- */
  /**
   *  Get the value of the "replace" flag for this message.
   *
   *  @return  true if this message should replace all previously received
   *          messages for the message queue.
   */
  public boolean replace()
  {
    return replace_flag;
  }


  /* -------------------------- getTime_stamp -------------------------- */
  /**
   *  Get the system time in milliseconds that was recorded when this
   *  message was created.
   *
   *  @return the time stampe of this message.
   */
  public double getTime_stamp()
  {
    return time_stamp;
  }


  /* ------------------------------ getTag ---------------------------- */
  /**
   *  Get the tag value for this object.  The tag value is set by the
   *  MessageCenter when it is given a message, and is used as a tie
   *  breaker, if the time stamps are equal, when the messages are sorted
   *  for delivery to the receivers. 
   *
   *  @return the tag value for this messsage.
   */
  public long getTag()
  {
    return tag;
  }


  /* ------------------------------ setTag ---------------------------- */
  /**
   *  Set the tag value for this object.  The tag value is set by the
   *  MessageCenter when it is given a message, and is used as a tie
   *  breaker, if the time stamps are equal, when the messages are sorted
   *  for delivery to the receivers.  NOTE: This method has package 
   *  visibility, and is not meant to be used directly in applications.
   *
   *  return the tag value for this messsage.
   */
  void setTag( long new_tag )
  {
    tag = new_tag;
  }

  
  /* ----------------------------- toString ---------------------------- */
  /**
   *  Pack the name, value, replace_flag, time_stamp and tag for this
   *  message into one string.  
   */
  public String toString()
  {
    return name +         ", " +
           value +        ", " + 
           replace_flag + ", " + 
           time_stamp   + ", " + 
           tag;
  }

}
