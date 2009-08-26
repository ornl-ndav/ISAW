/* 
 * File: IsawEV.java
 *
 * Copyright (C) 2009, Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.ShowEventsApp;

import javax.swing.*;
import MessageTools.*;

import EventTools.ShowEventsApp.DataHandlers.*;
import EventTools.ShowEventsApp.ViewHandlers.*;

/**
 *  This is the main class for the IsawEV neutron events viewer application.
 *  It sets up the MessageCenter and instantiates the controls,
 *  Data and View handlers that communicate via messages to carry out
 *  the work of the application.
 */
public class IsawEV 
{
  public  static final int NUM_BINS = 512;

  /**
   *  Construct an instance of the IsawEV application.
   */
  public IsawEV()
  {
    MessageCenter message_center = new MessageCenter("Test");
    message_center.setDebugReceive( true );
    message_center.setDebugSend( true );

    new UpdateManager(message_center, null, 100);

    new multiPanel( message_center );

    new EventLoader( message_center );

    new HistogramHandler( message_center, NUM_BINS );

    new EventTools.ShowEventsApp.ViewHandlers.EventViewHandler( message_center );

    new PeakListHandler( message_center );
    
    new OrientationMatrixHandler( message_center );

     
    new DQDataHandler( message_center );
    
    new DViewHandler( message_center );

    new QViewHandler( message_center );
  }


  /**
   *  Runnable to do the construction of the GUI in the AWT Event thread to
   *  guarantee that the calls to Swing function occur in the correct 
   *  thread.
   */
  public static class Builder implements Runnable
  {
    public void run()
    {
      new IsawEV();
    }  
  }


  /**
   *  Main program to launch the IsawEV application.
   */
  public static void main(String[] args)
  {
     SwingUtilities.invokeLater( new Builder() );
  }

}
