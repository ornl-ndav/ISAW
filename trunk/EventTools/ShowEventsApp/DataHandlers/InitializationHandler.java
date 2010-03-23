/* 
 * File: InitializationHandler.java
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
 *           Departmet of Mathematics, Statistics and Computer Science
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

package EventTools.ShowEventsApp.DataHandlers;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.EventList.SNS_Tof_to_Q_map;
import EventTools.EventList.FileUtil;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.LoadEventsCmd;
import EventTools.ShowEventsApp.Command.LoadUDPEventsCmd;
import EventTools.ShowEventsApp.Command.SetNewInstrumentCmd;
import EventTools.ShowEventsApp.Command.*;
import gov.anl.ipns.Util.File.FileIO;

/**
 *  This class processes messages to load a file, and first initializes the
 *  histogram and instrument type.  It will send out commands to 
 *  set up the Q-mapper and clear the histograms.  When it receives
 *  messages indicating that these data structures are ready to receive
 *  data, then it will send out a message to actually load the file data or
 *  to begin listening for live events.  If a file is currently being 
 *  loaded, this will IGNORE FURTHER INITIALIZATION ATTEMPTS until the
 *  file has finished loading.
 */
public class InitializationHandler implements IReceiveMessage
{
  public static final String UNKNOWN_INSTRUMENT = "UNKNOWN_INSTRUMENT";

  private MessageCenter    message_center;
                                                 // flags indicating state of
  private boolean          histogram_ok;         // initialization and loading
  private boolean          dq_ok;
  private boolean          instrument_ok;
  private boolean          loading_file;
  private boolean          udpLoadStarted; 
                                         
  private SocketEventLoader socket_evl;
  private int               current_port;
                                                 // references to current
  private LoadEventsCmd       load_file_cmd;     // command objects
  private LoadUDPEventsCmd    UDPcmd;
  private SetNewInstrumentCmd new_instrument_cmd;

  private String   currentUDPInstrument = null;
  private String[] supported_inst       = FileUtil.SupportedSNS_Instruments();

  /**
   *  Construct object to handle load and initialization messages for Q-Mapper, 
   *  Histogram,  UDP port, 3D viewer and D,Q graphs.
   *
   *  @param message_center  The MessageCenter through which load and 
   *                         initialization messages will be sent.
   */
  public InitializationHandler( MessageCenter message_center )
  {
    this.message_center = message_center;

    histogram_ok   = false;
    dq_ok          = false;
    instrument_ok  = false;
    loading_file   = false;
    udpLoadStarted = false;

    socket_evl   = null;
    current_port = -1;
                                                            // load messages 
    message_center.addReceiver( this, Commands.LOAD_FILE );
    message_center.addReceiver( this, Commands.LOAD_UDP_EVENTS);

                                                            // control messages
    message_center.addReceiver( this, Commands.PAUSE_UDP);
    message_center.addReceiver( this, Commands.CLEAR_UDP);
    message_center.addReceiver( this, Commands.CONTINUE_UDP);

                                                            // status messages
    message_center.addReceiver( this, Commands.LOAD_FAILED );
    message_center.addReceiver( this, Commands.LOAD_FILE_DONE );
    message_center.addReceiver( this, Commands.INIT_HISTOGRAM_DONE );
    message_center.addReceiver( this, Commands.INIT_DQ_DONE );
    message_center.addReceiver( this, Commands.INIT_NEW_INSTRUMENT_DONE);
  }

  
  /**
   *  Send messages to initialize the 3D view, Q-Mapper, 3D histogram
   *  and D,Q graphs, based on information in the SetNewInstrumentCmd.
   *
   *  @param new_inst_cmd  The command containing the information about
   *                       the instrument the various components should 
   *                       be set up for. 
   */
  private void InitData( SetNewInstrumentCmd new_inst_cmd )
   {
      Message init_view = new Message( Commands.INIT_EVENTS_VIEW, 
                                       null, true, true );
      message_center.send( init_view );

      Message new_inst = new Message( Commands.INIT_NEW_INSTRUMENT,
                                      new_inst_cmd, true, true );
      message_center.send( new_inst );

      Message clear_hist = new Message( Commands.INIT_HISTOGRAM , 
                                        new_inst_cmd, true, true );
      message_center.send( clear_hist );

      Message clear_dq = new Message( Commands.INIT_DQ, 
                                      new_inst_cmd, true, true );
      message_center.send( clear_dq );
   }
  
  
  public boolean receive( Message message )
  {
    if ( message.getName().equals(Commands.LOAD_FILE) )
    {
      if ( loading_file )
      {
        Util.sendInfo( "Still Loading File " + load_file_cmd.getEventFile() );
        Util.sendInfo( "IGNORING EXTRA LOAD REQUEST" );       
        return false; 
      }
    
      loading_file = true;
      if( udpLoadStarted )
         pauseAndResetUDP();

      histogram_ok  = false;
      dq_ok         = false;
      instrument_ok = false;
     
      load_file_cmd = (LoadEventsCmd)message.getValue();

      String inst_name = getInstrumentName( load_file_cmd.getEventFile() );
      if ( inst_name.equals( UNKNOWN_INSTRUMENT ) )
      {
        loading_file = false;
        Util.sendInfo("UNSUPPORTED INSTRUMENT " + inst_name + 
                      " FROM FILE " + load_file_cmd.getEventFile() );
        return false;
      }  
      new_instrument_cmd = 
         new SetNewInstrumentCmd( inst_name,
                                  load_file_cmd.getDetFile(),
                                  load_file_cmd.getIncSpectrumFile(),
                                  load_file_cmd.getBankFile( ),
                                  load_file_cmd.getIDMapFile( ),
                                  load_file_cmd.getScaleFactor(),
                                  load_file_cmd.getMaxQValue(),
                                  load_file_cmd.getAbsorptionRadius(),
                                  load_file_cmd.getAbsorptionSMU(),
                                  load_file_cmd.getAbsorptionAMU()  );
      InitData( new_instrument_cmd );

      int MAX_WAIT = 20;
      int wait_count = 0;
      while ( wait_count < MAX_WAIT && !InitDone() )
      {
        try 
        {
          Thread.sleep( 500 );
        }
        catch ( Exception ex )
        {} 
        wait_count++;
      }

      if ( InitDone() )
      {
        Message load = new Message( Commands.LOAD_FILE_DATA,
                                    load_file_cmd, true, true );
        message_center.send( load );
      }
      else
      {
        Util.sendInfo( "ERROR: FAILED INITIALIZE FOR FILE LOAD" );
        loading_file = false;
      }

      return false;
    }
    
    else if ( message.getName().equals(Commands.INIT_HISTOGRAM_DONE) )
      histogram_ok = true;

    else if ( message.getName().equals(Commands.INIT_DQ_DONE) )
      dq_ok = true;

    else if ( message.getName().equals(Commands.INIT_NEW_INSTRUMENT_DONE) )
      instrument_ok = true;

    else if ( message.getName().equals(Commands.LOAD_FAILED ) )
    {
      histogram_ok  = true;
      dq_ok         = true;
      instrument_ok = true;
      loading_file  = false;
    }

    else if ( message.getName().equals(Commands.LOAD_FILE_DONE ) )
      loading_file  = false;

    else if( message.getName().equals(Commands.LOAD_UDP_EVENTS) )
    {
      if ( loading_file )
      {
        Util.sendInfo( "Still Loading File " + load_file_cmd.getEventFile() );
        Util.sendInfo( "IGNORING EXTRA LOAD REQUEST" );       
        return false; 
      }

      pauseAndResetUDP();
      
      histogram_ok  = false;
      dq_ok         = false;
      instrument_ok = false;
      udpLoadStarted = true;
      
      UDPcmd =(LoadUDPEventsCmd) message.getValue();
      currentUDPInstrument= UDPcmd.getInstrument();
      new_instrument_cmd = 
         new SetNewInstrumentCmd( UDPcmd.getInstrument(),
                  UDPcmd.getDetFile(),
                  UDPcmd.getIncSpectrumFile(),
                  UDPcmd.getBankFile(),
                  UDPcmd.getIDMapFile(),
                  -1,
                  UDPcmd.getMaxQValue(),
                  UDPcmd.getAbsorptionRadius(),
                  UDPcmd.getAbsorptionSMU(),
                  UDPcmd.getAbsorptionAMU()  );

      (new InitDataThread( new_instrument_cmd )).start();
       
      SetUpUDP_Port( false );

      return false;
    }

    else if( message.getName().equals( Commands.PAUSE_UDP ) )
    {
      if ( socket_evl != null )
        socket_evl.setPause( true );
    }

    else if( message.getName().equals(Commands.CLEAR_UDP) )
    {
      if ( socket_evl != null )
      {
        socket_evl.setPause( true );
        socket_evl.resetAccumulator();
      }
       
      if ( !udpLoadStarted )
        return false;

      System.out.println("Sending message " + new_instrument_cmd );
      (new InitDataThread( new_instrument_cmd )).start();
      SetUpUDP_Port( true );

      return false;
    }

    else if( message.getName().equals( Commands.CONTINUE_UDP ) )
    {
      if ( socket_evl != null ) 
        socket_evl.setPause( false );
    }

    return false;
  }


  /**
   *  Wait until all initializations are done or until 10 seconds have
   *  elapsed, then set up a udp event listener on the requested port.
   *  If the port requested in the last UDPcmd is new, close the existing 
   *  port and start listening on the newly requested port.
   *
   *  @param pause_flag  If true, immediately pause the udp event listener,
   */
  private void SetUpUDP_Port( boolean pause_flag )
  {
      int MAX_WAIT = 20;
      int wait_count = 0;
      while ( wait_count < MAX_WAIT && !InitDone() )
      {
        try
        {
          Thread.sleep( 500 );
        }
        catch ( Exception ex )
        {}
        wait_count++;
      }

      if ( InitDone() )
      {
        int requested_port = UDPcmd.getPort();
        if( socket_evl == null || requested_port != current_port )
        {
          if ( socket_evl != null )      
            socket_evl.close();

          socket_evl = new SocketEventLoader( requested_port,
                                              message_center,
                                              UDPcmd.getInstrument( ));
          current_port = requested_port;
          socket_evl.setPause( pause_flag );
          socket_evl.start();
        }

        socket_evl.setPause( pause_flag );
      }
      else
      {
        Util.sendInfo( "ERROR: FAILED INITIALIZE FOR UDP LOAD" );
        loading_file = false;
      }
  }


  /**
   *  Check whether or not the histogram, dq graphs, and q_mapper have
   *  all finished initializing.
   *
   *  @return true if all three flags are set, indicating initialization
   *          is complete.
   */
  private boolean InitDone()
  {
    return (  histogram_ok  &&  dq_ok  &&  instrument_ok );
  }


  /**
   *  Get the name of a supported instrument from the specified file name.
   */
  private String getInstrumentName( String file_name )
  {
    String instrument_name = UNKNOWN_INSTRUMENT;
    String file_inst       = FileIO.getSNSInstrumentName( file_name );

    for ( int i = 0; i < supported_inst.length; i++ )
      if ( file_inst.equalsIgnoreCase( supported_inst[i] ) )
        instrument_name = supported_inst[i];

    return instrument_name;
  }
  

  /**
   * If there is a socket event listener, pause it, reset its accumulator
   * and set a flag indicating that udp loading is not started.
   */
  private void pauseAndResetUDP()
  {
    udpLoadStarted = false;
    if( socket_evl != null)
    {
      socket_evl.setPause( true );
      socket_evl.resetAccumulator( );
    }
  }


  /**
   *  This class will send out the specified set new instrument command
   *  after a delay of about 1 second.  This will allow some time for any
   *  threads that are processing live events to finish, before clearing
   *  and resetting the histogram, 3D view, DQ graphs, etc.
   */
  class InitDataThread extends Thread
  {
     SetNewInstrumentCmd cmd;

     public InitDataThread( SetNewInstrumentCmd cmd )
     {
        this.cmd = cmd;
     }
     
     public void run()
     {
        try
        {
          Thread.sleep( 1000 );
        }
        catch(Exception s)
        {
        }
        InitData( cmd );
     }
  }
}
