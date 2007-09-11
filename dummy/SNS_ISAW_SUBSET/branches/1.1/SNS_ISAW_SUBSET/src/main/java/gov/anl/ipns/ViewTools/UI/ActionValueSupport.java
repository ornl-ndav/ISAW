/*
 * File: ActionValueSupport.java
 *
 * Copyright (C) 2005, Mike Miller
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
 * Primary   Mike Miller <millermi@uwstout.edu>
 * Contact:  Student Developer, University of Wisconsin-Stout
 *           
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log: ActionValueSupport.java,v $
 *  Revision 1.1  2005/03/28 05:35:42  millermi
 *  - Initial Version - This new event, listener, and utility classes
 *    are used by ComponentSwappers, ComponentLayoutManagers, and
 *    ComponentViewManagers to pass messages and values associated
 *    with the messages.
 *
 */
 package gov.anl.ipns.ViewTools.UI;
 
 import java.util.Vector;
 
/**
 *
 */
 public class ActionValueSupport implements java.io.Serializable,
                                            ActionValueListener
 {
   private Vector listeners;
   private ActionValueEvent last_sent_event;
   
   public ActionValueSupport()
   {
     listeners = new Vector();
     last_sent_event = null;
   }
   
  /**
   * Add the specified listener so it will listen to ActionValueEvents
   * generated by a class. If listener already exists, it will not be
   * added a second time.
   *
   *  @param  avl The ActionValueListener being added.
   */
   public void addActionValueListener( ActionValueListener avl )
   {
     // If listener is null or already part of list, do not add it.
     if( !(avl == null || listeners.contains(avl)) )
       listeners.add(avl);
   }
   
  /**
   * Remove the specified listener so it will no longer listen to
   * ActionValueEvents.
   *
   *  @param  avl The ActionValueListener being removed.
   */
   public void removeActionValueListener( ActionValueListener avl )
   {
     listeners.remove(avl);
   }
   
  /**
   * Remove all ActionValueListeners that are listening to the class.
   */
   public void removeAllActionValueListeners()
   {
     // Remove all listeners registered by this class.
     listeners.clear();
   }
   
  /**
   * Use this method to notify listeners that a value has changed.
   *
   *  @param  event The ActionValueEvent used to notify liseners that a change
   *                has occurred.
   */
   public void sendActionValue( ActionValueEvent event )
   {
     // Make sure event is valid.
     if( event == null )
       return;
     // Remember the event that is being sent out.
     last_sent_event = event;
     // Tell all listeners that a value has changed.
     int num_listeners = listeners.size();
     for( int i = 0; i < num_listeners; i++ )
       ((ActionValueListener)listeners.elementAt(i)).valueChanged(event);
   }
   
  /**
   * This method will notify all listeners of a value that has changed.
   * In order to prevent an infinite loop, this method will filter out
   * any messages that it has already sent.
   *
   *  @param  event ActionValueEvent that contains a changed value.
   */
   public void valueChanged( ActionValueEvent event )
   {
     // If this message was just sent out, ignore it.
     if( event == last_sent_event )
       return;
     sendActionValue(event);
   }
   
  /**
   * For test purposes only...
   *
   *  @param  args Input parameters are ignored.
   */
   public static void main( String args[] )
   {
     // Create artificial hierarchy:
     //            avs3a        // Listen to both elements one level down.
     //           |    |
     //      avs2a     avs2b    // These must listen to element above and below.
     //      |        |    |
     //   avs1a    avs1b  avs1c // Listen to element one level up.
     ActionValueSupport avs1a = new ActionValueSupport();
     ActionValueSupport avs2a = new ActionValueSupport();
     ActionValueSupport avs3a = new ActionValueSupport();
     ActionValueSupport avs2b = new ActionValueSupport();
     ActionValueSupport avs1b = new ActionValueSupport();
     ActionValueSupport avs1c = new ActionValueSupport();
     
     // Left side lower level.
     avs1a.addActionValueListener( new ActionValueListener(){
         public void valueChanged(ActionValueEvent ave)
	 {
	   System.out.println("ActionValueSupport1a: "+ave.getActionCommand());
	 }
       } );
     avs1a.addActionValueListener(avs2a); // Listen to element in level above
     
     // Left side middle level.
     avs2a.addActionValueListener( new ActionValueListener(){
         public void valueChanged(ActionValueEvent ave)
	 {
	   System.out.println("ActionValueSupport2a: "+ave.getActionCommand());
	 }
       } );
     avs2a.addActionValueListener(avs1a); // Listen to element in level below
     avs2a.addActionValueListener(avs3a);  // Listen to element in level above
     
     // Top-most element.
     avs3a.addActionValueListener( new ActionValueListener(){
         public void valueChanged(ActionValueEvent ave)
	 {
	   System.out.println("ActionValueSupport3a: "+ave.getActionCommand());
	 }
       } );
     avs3a.addActionValueListener(avs2a); // Listen to elements in level below
     avs3a.addActionValueListener(avs2b);
     
     // Right side, middle level.
     avs2b.addActionValueListener( new ActionValueListener(){
         public void valueChanged(ActionValueEvent ave)
	 {
	   System.out.println("ActionValueSupport2b: "+ave.getActionCommand());
	 }
       } );
     avs2b.addActionValueListener(avs1b); // Listen to element in level below
     avs2b.addActionValueListener(avs1c); // Listen to element in level below
     avs2b.addActionValueListener(avs3a);  // Listen to element in level above
     
     // Mid-right side, lower level.
     avs1b.addActionValueListener( new ActionValueListener(){
         public void valueChanged(ActionValueEvent ave)
	 {
	   System.out.println("ActionValueSupport1b: "+ave.getActionCommand());
	 }
       } );
     avs1b.addActionValueListener(avs2b); // Listen to element in level above
     
     // Right side, lower level.
     avs1c.addActionValueListener( new ActionValueListener(){
         public void valueChanged(ActionValueEvent ave)
	 {
	   System.out.println("ActionValueSupport1c: "+ave.getActionCommand());
	 }
       } );
     avs1c.addActionValueListener(avs2b); // Listen to element in level above
     
     // Send message in at left side of lower level. Message should
     // propegate throughout all levels by traveling up to the top level and
     // then back down.
     avs1a.sendActionValue( new ActionValueEvent(avs1a,"TestProperty",
                                                       new Integer(1),
						       new Integer(5)) );
     System.out.println();
     avs3a.sendActionValue( new ActionValueEvent( avs3a,"TestProperty2",
                                                       new Integer(1),
						       new Integer(5)) );
   }
 }
