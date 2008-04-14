/*
 * File: ActiveJPanel.java
 *
 * Copyright (C) 2001 Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log: ActiveJPanel.java,v $
 * Revision 1.9  2007/08/09 14:33:25  rmikk
 * Added and implemented a new method send_out_messages that can "disable"
 *   one of these ActiveJPanels( though disable in terms of events to outside only)
 *
 * Revision 1.8  2005/01/27 19:52:03  millermi
 * - Removed send_message(STATE_CHANGED) from main() test program.
 *
 * Revision 1.7  2005/01/26 22:25:52  millermi
 * - Removed STATE_CHANGED public message String from class since
 *   it was unused.
 *
 * Revision 1.6  2004/03/15 23:53:56  dennis
 * Removed unused imports, after factoring out the View components,
 * Math and other utils.
 *
 * Revision 1.5  2004/03/11 22:33:22  serumb
 * Changed package.
 *
 * Revision 1.4  2004/02/04 18:16:50  dennis
 * Fixed spelling error in comment.
 *
 * Revision 1.3  2003/05/20 19:41:42  dennis
 * Added method removeAllActionListeners(). (Mike Miller)
 *
 * Revision 1.2  2002/11/27 23:13:34  pfpeterson
 * standardized header
 *
 */

package gov.anl.ipns.ViewTools.UI;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.io.*;
//import DataSetTools.dataset.*;
//import DataSetTools.util.*;

/**
 *  This class is a base class for panels that group one or more components 
 *  into a new reusable control that maintains a list of ActionListeners
 *  and sends actionEvents to the listeners when something is changed.  
 *  Derived classes should add components and event handlers for the 
 *  components to the panel, and use the send_message method to send 
 *  the actionEvents when the components are changed.   This class also
 *  defines one generic message STATE_CHANGED, derived classes should 
 *  define additional appropriate messages as needed.
 */

public class ActiveJPanel extends JPanel
                                  implements Serializable 
{
  protected Vector listeners = null;
  protected boolean send = true;
 
 /* ------------------------------ CONSTRUCTOR ---------------------------- */
 /** 
  */
  public ActiveJPanel( )
  { 
    listeners = new Vector();
  }

 /* ------------------------ addActionListener -------------------------- */
 /**
  *  Add an ActionListener for this ActiveJPanel.  
  *
  *  @param listener  An ActionListener whose ActionPerformed() method is
  *                   to be called when something changed in the panel.
  */
  public void addActionListener( ActionListener listener )
  {
    for ( int i = 0; i < listeners.size(); i++ )       // don't add it if it's
      if ( listeners.elementAt(i).equals( listener ) ) // already there
        return;
    listeners.add( listener );
  }


 /* ------------------------ removeActionListener ------------------------ */
 /**
  *  Remove the specified ActionListener from this ActiveJPanel.  If
  *  the specified ActionListener is not in the list of ActionListeners for
  *  for this panel this method has no effect.
  *
  *  @param listener  The ActionListener to be removed.
  */
  public void removeActionListener( ActionListener listener )
  {
    listeners.remove( listener );
  }

 /* ---------------------- removeAllActionListeners ----------------------- */
 /**
  * Method to remove all listeners from this ActiveJPanel.
  */ 
  public void removeAllActionListeners()
  {
    listeners.removeAllElements();
  }

/* -------------------------- send_message ------------------------------- */
/**
 *  Send a message to all of the action listeners for this panel
 */
 public void send_message( String message )
 {
   if( send )
     for ( int i = 0; i < listeners.size(); i++ )
      {
        ActionListener listener = (ActionListener)listeners.elementAt(i);
        listener.actionPerformed( new ActionEvent( this, 0, message ) );
      }
 }

 
 /**
  * Enable or disable the sending of messages to registered listeners
  * 
  * @param send  if true, all messages will be forwarded to listeners, 
  *              otherwise they will be ignored/dropped;
  *   
  *  NOTE: Will not be effective for checkedControl, FrameController and
  *        RangeControl until they are fixed.
  */
 public void send_out_messages( boolean send){
    
    this.send = send;
    
 }

/* -------------------------------------------------------------------------
 *
 * MAIN  ( Basic main program for testing purposes only. )
 *
 */
    public static void main(String[] args)
    {
      JFrame f = new JFrame("Test for XScaleChooserUI");
      f.setBounds(0,0,200,150);
      final ActiveJPanel test = new ActiveJPanel();

      f.getContentPane().setLayout( new GridLayout(1,1) );
      f.getContentPane().add( test );

      test.addActionListener( 
       new ActionListener()
       {
         public void actionPerformed(ActionEvent e)
         {
           System.out.println("Listener1,Message is :" + e.getActionCommand());
         }
       });

      test.addActionListener( 
       new ActionListener()
       {
         public void actionPerformed(ActionEvent e)
         {
           System.out.println("Listener2,Message is :" + e.getActionCommand());
         }
       });

      f.setVisible(true);
    }
}
