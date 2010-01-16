/* 
 * File: QViewHandler.java
 *
 * Copyright (C) 2009, Paul Fischer
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0800276 and by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author: fischerp $
 *  $Date: 2009-08-25 11:02:13 -0500 (Tue, 25 Aug 2009) $            
 *  $Revision: 19931 $
 */

package EventTools.ShowEventsApp.ViewHandlers;

import EventTools.ShowEventsApp.Command.Commands;
import MessageTools.*;

/**
 * Builds and Displays a graph of q values. Updates
 * automatically when data is loaded and displayed on the screen.
 */
public class QViewHandler extends GraphViewHandler 
{
   
   /**
    * Sets the message center for the DViewHandler but does
    * not display or create anything else.  The class relies mainly
    * on the message center.  It will display the jframe when it
    * receives SHOW_Q_GRAPH message.
    * 
    * @param messageCenter The MessageCenter that is used for handling this
    *                      Q value viewer.
    */
   public QViewHandler(MessageCenter messageCenter)
   {
      super( messageCenter );
      this.messageCenter.addReceiver(this, Commands.SHOW_Q_GRAPH);
      this.messageCenter.addReceiver(this, Commands.HIDE_Q_GRAPH);
      this.messageCenter.addReceiver(this, Commands.SET_Q_VALUES);

      frame_title = "Q-values View";
      title       = "Magnitude Q";
      x_units     = "Inv(" + '\u00c5' + ")";
      y_units     = "weighted";
      x_label     = "Q";
      y_label     = "Intensity";
   }
   
   public  void WindowClose( String ID)
   {
      super.messageCenter.send(  new Message( Commands.HIDE_Q_GRAPH,null,true));
      super.HideGraph( );

   }
 
   /**
    * Receive messages to display the frame, hide the frame,
    * get the xy values, and set the values/create the graph.
    */
   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.SHOW_Q_GRAPH))
      {
         super.ShowGraph();
         
         return false;
      }
      
      if (message.getName().equals(Commands.HIDE_Q_GRAPH))
      {
         super.HideGraph();
         
         return false;
      }
      
      if (message.getName().equals(Commands.SET_Q_VALUES))
      {
         super.setInfo( (float[][])(message.getValue()) );

         return false;
      }
      return false;
   }
}
