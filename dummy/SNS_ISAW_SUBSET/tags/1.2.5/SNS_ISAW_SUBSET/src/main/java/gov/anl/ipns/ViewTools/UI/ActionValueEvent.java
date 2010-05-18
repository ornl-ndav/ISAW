/*
 * File: ActionValueListener.java
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
 *  $Log$
 *  Revision 1.1  2005/03/28 05:35:41  millermi
 *  - Initial Version - This new event, listener, and utility classes
 *    are used by ComponentSwappers, ComponentLayoutManagers, and
 *    ComponentViewManagers to pass messages and values associated
 *    with the messages.
 *
 */
 package gov.anl.ipns.ViewTools.UI;

 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
/**
 * This class extends the capabilities of the ActionEvent by providing an
 * old and new value, similar to the PropertyChangeEvent. This event was
 * developed to allow messages and associated values be passed through the
 * viewer structure without passing PropertyChangeEvents generated by
 * the javax.swing components that the viewers are built on.
 */
 public class ActionValueEvent extends ActionEvent
 {
   private Object old_value = null;
   private Object new_value = null;
  /**
   * Constructor - Produces an event with a source, action command, old value,
   * and new value. Use this when modifiers and time stamps are not important.
   *
   *  @param  source Source that generated the action.
   *  @param  command The String associated with the event.
   *  @param  old_value The value previously held before the action was fired.
   *  @param  new_value The current value of the action.
   */
   public ActionValueEvent( Object source, String command,
                            Object old_value, Object new_value ) 
   {
     super(source,ActionEvent.ACTION_PERFORMED,command);
     this.old_value = old_value;
     this.new_value = new_value;
   }
   
  /**
   * Constructor - Produces an event with a source, action command, old value,
   * and new value. Use this when modifiers and time stamps are not important.
   *
   *  @param  source Source that generated the action.
   *  @param  command The String associated with the event.
   *  @param  old_value The value previously held before the action was fired.
   *  @param  new_value The current value of the action.
   *  @param  modifiers Modifiers applied to the action.
   */
   public ActionValueEvent( Object source, String command,
                            Object old_value, Object new_value, int modifiers ) 
   {
     super(source,ActionEvent.ACTION_PERFORMED,command,modifiers);
     this.old_value = old_value;
     this.new_value = new_value;
   }
   
  /**
   * Constructor - Produces an event with a source, action command, old value,
   * and new value. Use this when modifiers and time stamps are not important.
   *
   *  @param  source Source that generated the action.
   *  @param  command The String associated with the event.
   *  @param  old_value The value previously held before the action was fired.
   *  @param  new_value The current value of the action.
   *  @param  time_stamp The time the event occurred.
   *  @param  modifiers Modifiers applied to the action.
   */
   public ActionValueEvent( Object source, String command,
                            Object old_value, Object new_value,
			    long time_stamp, int modifiers ) 
   {
     super(source,ActionEvent.ACTION_PERFORMED,command,time_stamp,modifiers);
     this.old_value = old_value;
     this.new_value = new_value;
   }
   
  /**
   * Constructor - Converts a PropertyChangeEvent into an ActionValueEvent.
   *
   *  @param  pce PropertyChangeEvent to be converted to an ActionValueEvent.
   */
   public ActionValueEvent( PropertyChangeEvent pce ) 
   {
     super(pce.getSource(),ActionEvent.ACTION_PERFORMED,pce.getPropertyName());
     this.old_value = pce.getOldValue();
     this.new_value = pce.getNewValue();
   }
   
  /**
   * Get the value held prior to the ActionValueEvent being sent.
   *
   *  @return The old value held prior to the event being sent.
   */
   public Object getOldValue() { return old_value; }
   
  /**
   * Get the value currently held by the action.
   *
   *  @return The value currently held by the action.
   */
   public Object getNewValue() { return new_value; }
   
  /**
   * Get an equivalent PropertyChangeEvent. Since ActionValueEvents are
   * very similar to PropertyChangeEvents, this is a convenience method
   * for converting to PropertyChangeEvents.
   */
   public PropertyChangeEvent toPropertyChange()
   {
     return new PropertyChangeEvent( getSource(), getActionCommand(),
                                     getOldValue(), getNewValue() );
   }
 }
