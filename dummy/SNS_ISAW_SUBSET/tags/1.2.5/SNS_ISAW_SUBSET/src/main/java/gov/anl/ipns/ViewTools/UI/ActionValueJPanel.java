/*
 * File: ActionValueJPanel.java
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
 *  Revision 1.1  2005/03/28 05:35:42  millermi
 *  - Initial Version - This new event, listener, and utility classes
 *    are used by ComponentSwappers, ComponentLayoutManagers, and
 *    ComponentViewManagers to pass messages and values associated
 *    with the messages.
 *
 */
 package gov.anl.ipns.ViewTools.UI;
 
 import javax.swing.JPanel;
 
/**
 *
 */
 public class ActionValueJPanel extends JPanel
                                implements ActionValueListener
 {
   private ActionValueSupport support;
   public ActionValueJPanel()
   {
     super();
     setLayout(new java.awt.GridLayout(1,1));
     support = new ActionValueSupport();
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
     support.addActionValueListener(avl);
   }
   
  /**
   * Remove the specified listener so it will no longer listen to
   * ActionValueEvents.
   *
   *  @param  avl The ActionValueListener being removed.
   */
   public void removeActionValueListener( ActionValueListener avl )
   {
     support.removeActionValueListener(avl);
   }
   
  /**
   * Remove all ActionValueListeners that are listening to the class.
   */
   public void removeAllActionValueListeners()
   {
     // Remove all listeners registered by this class.
     support.removeAllActionValueListeners();
   }
   
  /**
   * Use this method to notify listeners that a value has changed.
   *
   *  @param  event The ActionValueEvent used to notify liseners that a change
   *                has occurred.
   */
   public void sendActionValue( ActionValueEvent event )
   {
     support.sendActionValue(event);
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
     support.valueChanged(event);
   }
 }
