/* 
 * File: SharedMessageHandler.java
 *
 * Copyright (C) 2009, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$:
 *  $Date$:            
 *  $Rev$:
 */

package EventTools.ShowEventsApp.ViewHandlers;

import java.awt.Container;
import java.awt.Dimension;

import gov.anl.ipns.Util.Sys.StatusPane;
import gov.anl.ipns.Util.Sys.WindowShower;
import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import javax.swing.*;
import EventTools.ShowEventsApp.Command.Commands;


/**
 * Handler of messages for the StatusPane( with the save and clear buttons)
 * 
 * @author Ruth
 * 
 */
public class StatusMessageHandler implements IReceiveMessage
{



   StatusPane    statPane;

   String        filename;

   MessageCenter message_center;


   /**
    * Constructor
    * 
    * @param message_center
    *           The message center
    * @param container
    *           The container to add the status pane. If null a JFrame is
    *           created for the status pane.
    */
   public StatusMessageHandler( MessageCenter message_center,
            Container container )
   {

      statPane = gov.anl.ipns.Util.Sys.SharedMessages.getStatusPane();
      this.message_center = message_center;

      message_center.addReceiver( this , Commands.DISPLAY_INFO );
      message_center.addReceiver( this , Commands.DISPLAY_ERROR );
      message_center.addReceiver( this , Commands.DISPLAY_WARNING );
      message_center.addReceiver( this , Commands.DISPLAY_CLEAR );

      if( container == null )
      {
         JFrame jf = new JFrame( " Messages" );
         jf.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
         Dimension D = jf.getToolkit().getScreenSize();
         jf.setBounds( 0 , (int) ( D.getHeight() * .7f ) ,
                  (int) ( D.getWidth() * .3 ) , (int) ( D.getHeight() * .3 ) );
         container = jf.getContentPane();
         WindowShower.show( jf );
      }

      container.add( statPane );
      filename = System.getProperty( "user.home" );
   }


   /**
    * Received messages from the message center
    */
   @Override
   public boolean receive( Message message )
   {

      if( message == null )
         return true;
      
      Object name = message.getName();
      
      if( name == null )
         return false;

      if( Commands.DISPLAY_INFO.equals( name ) )
      {
         statPane.add( message.getValue() );
      }
      else if( Commands.DISPLAY_WARNING.equals( name ) )
      {
         statPane.add( message.getValue() );
      }
      else if( Commands.DISPLAY_ERROR.equals( name ) )
      {
         statPane.add( message.getValue() );
      }
      else if( Commands.DISPLAY_CLEAR.equals( name ) )
      {
         statPane.Clearr();
      }
      else
         return false;
      
      return true;


   }


   public static void main( String[] args )
   {

      MessageCenter msgC = new MessageCenter( "Test" );
      StatusMessageHandler statPane = new StatusMessageHandler( msgC , null );

      Message msg = new Message( Commands.DISPLAY_INFO , "Hi There" , false );
      SwingUtilities.invokeLater( new MyThread( msgC , msg ) );
      
      msg = new Message( Commands.DISPLAY_INFO , "I am fine" , false );
      SwingUtilities.invokeLater( new MyThread( msgC , msg ) )
      ;
      msg = new Message( Commands.DISPLAY_INFO , "How are you" , false );
      SwingUtilities.invokeLater( new MyThread( msgC , msg ) );
      
      msg = new Message( Commands.DISPLAY_INFO , "Too bad" , false );
      SwingUtilities.invokeLater( new MyThread( msgC , msg ) );
      
      SwingUtilities.invokeLater( new MyThread( msgC ,
               MessageCenter.PROCESS_MESSAGES ) );

   }
}


class MyThread extends Thread
{

   MessageCenter msgC;

   Message       msg;


   public MyThread( MessageCenter msgC, Message msg )
   {

      this.msgC = msgC;
      this.msg = msg;

   }


   public void run()
   {

      msgC.receive( msg );
   }
}
