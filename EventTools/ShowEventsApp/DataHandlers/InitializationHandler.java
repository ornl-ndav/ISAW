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
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.LoadEventsCmd;
import EventTools.ShowEventsApp.Command.LoadUDPEventsCmd;
import EventTools.ShowEventsApp.Command.SetNewInstrumentCmd;
import EventTools.ShowEventsApp.Command.*;
import gov.anl.ipns.Util.File.FileIO;

/**
 *  This class processes messages to load a file, or initialize the
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
  private boolean          histogram_ok;
  private boolean          dq_ok;
  private boolean          instrument_ok;
  private boolean          loading_file;
  private boolean          load_failed;
  private LoadEventsCmd    load_file_cmd;
  private boolean          udpLoadStarted;  // no fail/no end unless 
                                            // loading_file is true
  private boolean          InitDone;
  private boolean          Clearing;
  private SocketEventLoader socket;
  private LoadUDPEventsCmd  UDPcmd;
  private String            currentUDPInstrument = null;

  public InitializationHandler( MessageCenter message_center )
  {
    this.message_center = message_center;

    histogram_ok  = false;
    dq_ok         = false;
    instrument_ok = false;
    loading_file  = false;
    load_failed   = false;
    socket = null;
    Clearing = false;
    udpLoadStarted = false;
    
    message_center.addReceiver( this, Commands.LOAD_FILE );
    message_center.addReceiver( this, Commands.LOAD_FAILED );
    message_center.addReceiver( this, Commands.LOAD_FILE_DONE );

    message_center.addReceiver( this, Commands.INIT_HISTOGRAM_DONE );
    message_center.addReceiver( this, Commands.INIT_DQ_DONE );
    message_center.addReceiver( this, Commands.INIT_NEW_INSTRUMENT_DONE);

    message_center.addReceiver( this, Commands.LOAD_UDP_EVENTS);
    message_center.addReceiver( this, Commands.PAUSE_UDP);
    message_center.addReceiver( this, Commands.CLEAR_UDP);
    message_center.addReceiver( this, Commands.CONTINUE_UDP);
  }

  
 // for a reload, all data must be reinitialized 
  private void InitData( SetNewInstrumentCmd new_inst_cmd )
   {
      InitDone = false;

      Message init_view = new Message( Commands.INIT_EVENTS_VIEW, 
                                       null, true, true );
      message_center.send( init_view );

      Message new_inst = new Message( Commands.INIT_NEW_INSTRUMENT ,
                                      new_inst_cmd , true , true );
      message_center.send( new_inst );

      Message clear_hist = new Message( Commands.INIT_HISTOGRAM , 
                                        new_inst_cmd , true , true );
      message_center.send( clear_hist );

      Message clear_dq = new Message( Commands.INIT_DQ, 
                                      new_inst_cmd, true, true );
      message_center.send( clear_dq );
   }
  
  
  public boolean receive( Message message )
  {
    if ( message.getName().equals(Commands.LOAD_FILE) )
    {
      if ( loading_file && !load_failed  )
      {
        Util.sendInfo( "Still Loading File " + load_file_cmd.getEventFile() );
        Util.sendInfo( "IGNORING EXTRA LOAD REQUEST" );       
        return false; 
      }
      else if( Clearing)
      {
         Util.sendInfo( "Still Clearing Data"  );
         Util.sendInfo( "IGNORING EXTRA LOAD REQUEST" );       
         return false; 
      }
    
      loading_file = true;
      if( udpLoadStarted )
         killUDP();
      load_failed   = false;
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
      SetNewInstrumentCmd new_inst_cmd = 
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

      InitData( new_inst_cmd );

      return false;
    }
    
    else if ( message.getName().equals(Commands.INIT_HISTOGRAM_DONE) )
    {
      histogram_ok = true;
      LoadIfPossible();
    }

    else if ( message.getName().equals(Commands.INIT_DQ_DONE) )
    {
      dq_ok = true;
      LoadIfPossible();
    }

    else if ( message.getName().equals(Commands.INIT_NEW_INSTRUMENT_DONE) )
    {
      instrument_ok = true;
      LoadIfPossible();
    }

    else if ( message.getName().equals(Commands.LOAD_FAILED ) )
    {
      load_failed  = true;
      loading_file = false;
    }

    else if ( message.getName().equals(Commands.LOAD_FILE_DONE ) )
    {
      load_failed   = false;
      loading_file  = false;
      histogram_ok  = false;
      dq_ok         = false;
      instrument_ok = false;
    }

    else if( message.getName().equals(Commands.LOAD_UDP_EVENTS))
    {
       if ( loading_file && !load_failed )
       {
         Util.sendInfo( "Still Loading File " + load_file_cmd.getEventFile() );
         Util.sendInfo( "IGNORING EXTRA LOAD REQUEST" );       
         return false; 
       }
       if( Clearing)
       { 
         Util.sendInfo( "Still Clearing Data "  );
         Util.sendInfo( "IGNORING EXTRA LOAD REQUEST" );       
         return false; 
       }
       killUDP();
      
       loading_file = false;
       load_failed   = false;//so will not go through LoadifPossible 
       histogram_ok  = false;
       dq_ok         = false;
       instrument_ok = false;
       udpLoadStarted = true;
      
       UDPcmd =(LoadUDPEventsCmd) message.getValue();
       currentUDPInstrument= UDPcmd.getInstrument();
       SetNewInstrumentCmd new_inst_cmd = 
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

       (new InitDataThread( new_inst_cmd )).start();
       
       return false;
    }

    else if( message.getName().equals( Commands.PAUSE_UDP))
    {
       if( !udpLoadStarted && socket == null && !InitDone )
          return false;

       if( Clearing )
          return false;

       socket.setPause( true);
    }

    else if( message.getName().equals(Commands.CLEAR_UDP))
    {
       if( !udpLoadStarted && socket == null&& !InitDone )
          return false;

       if( Clearing )
       {
          return false;
       }

       Clearing = true;
       if ( socket != null )
         {
          socket.setPause( true);
          socket.resetAccumulator( );
         }

      instrument_ok = false;
      load_failed = false;
            
      dq_ok =false;

      SetNewInstrumentCmd new_inst_cmd = 
          new SetNewInstrumentCmd( currentUDPInstrument,
                                   null, null, null,null, 
                                   -1, 1000000, 0, 0, 0 );
       
      (new InitDataThread( new_inst_cmd )).start();
    }

    else if( message.getName().equals( Commands.CONTINUE_UDP))
    {
       if( !udpLoadStarted && socket == null && !InitDone)
          return false;

       if( Clearing )
          return false;
       
       socket.setPause( false);
    }

    return false;
  }


  /**
   *  Get the name of a supported instrument from the specified file name.
   */
  public String getInstrumentName( String file_name )
  {
    String instrument_name = UNKNOWN_INSTRUMENT;

    String file_inst   = FileIO.getSNSInstrumentName( file_name );
    String[] supported = SNS_Tof_to_Q_map.supported_instruments;

    for ( int i = 0; i < supported.length; i++ )
      if ( file_inst.equalsIgnoreCase( supported[i] ) )
        instrument_name = supported[i];

    return instrument_name;
  }
  
  public void killUDP()
  {
     udpLoadStarted = false;
     if( socket != null)
     {
        socket.setPause( true );
        socket.resetAccumulator( );
        
     }
  }

  private void LoadIfPossible()
  {
     InitDone = false;
    if ( histogram_ok && 
                dq_ok && 
        instrument_ok &&
        !load_failed)
    {
      if( !udpLoadStarted)//was reading from a file
      {
        Message load = new Message( Commands.LOAD_FILE_DATA, 
                                    load_file_cmd, true, true );
        message_center.send( load );
        if( socket != null)
           socket.setPause( true );
        InitDone = true;
      }
      else
      {
        if( socket == null)
        { 
          socket = new SocketEventLoader( UDPcmd.getPort(), 
                                          message_center, 
                                          UDPcmd.getInstrument( ));
          socket.start();
        }
        if( Clearing)
          socket.setPause( true );
        else
          socket.setPause(false);
        InitDone = true;
        Clearing = false;
      }
    }
  }
  
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
