/* 
 * File: QMapperHandler.java
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

package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;

import gov.anl.ipns.Operator.IOperator;
import gov.anl.ipns.Operator.Threads.ParallelExecutor;
import gov.anl.ipns.Operator.Threads.ExecFailException;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.EventList.IEventList3D;
import EventTools.EventList.SNS_Tof_to_Q_map;
import EventTools.EventList.SNS_TofEventList;
import EventTools.EventList.ITofEventList;
import EventTools.EventList.MapEventsToQ_Op;
import EventTools.EventList.EventSegmentLoadOp;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.LoadEventsCmd;
import EventTools.ShowEventsApp.Command.SetNewInstrumentCmd;
import EventTools.ShowEventsApp.Command.SelectPointCmd;
import EventTools.ShowEventsApp.Command.SelectionInfoCmd;
import EventTools.ShowEventsApp.Command.Util;

import DataSetTools.math.tof_calc;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
import DataSetTools.operator.Generic.TOF_SCD.IPeakQ;
import DataSetTools.operator.Generic.TOF_SCD.PeakQ;

public class QMapperHandler implements IReceiveMessage
{
  private MessageCenter    message_center;
  private String           instrument_name = null;
  private SNS_Tof_to_Q_map mapper = null;

  public QMapperHandler( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.SET_NEW_INSTRUMENT );
    message_center.addReceiver( this, Commands.MAP_EVENTS_TO_Q );
    message_center.addReceiver( this, Commands.SELECT_POINT );
    message_center.addReceiver( this, Commands.GET_PEAK_NEW_LIST );
  }


  public boolean receive( Message message )
  {
    long   start;
    double run_time;

    if ( message.getName().equals(Commands.SET_NEW_INSTRUMENT) )
    {
      Object obj = message.getValue();
      if ( obj == null || !(obj instanceof SetNewInstrumentCmd) )
      {
        Util.sendError( "Wrong type value in SENT_NEW_INSTRUMENT command" );
      }

      SetNewInstrumentCmd cmd = (SetNewInstrumentCmd)obj;
      String new_instrument = cmd.getInstrumentName();

                                         // if this is a new instrument, get
                                         // a new mapper.
      if ( instrument_name == null ||
           !new_instrument.equals( instrument_name ) )
      {
        String det_file = cmd.getDetectorFileName();

        if ( det_file == null )
        {
          String isaw_home = System.getProperty( "ISAW_HOME" ) + "/";
          det_file = isaw_home + "InstrumentInfo/SNS/" +
                     new_instrument + ".DetCal";
        }
                                               // TODO add the incident 
        try                                    // spectrum as a parameter to
        {                                      // SNS_Tof_to_Q_map
          start    = System.nanoTime();
          mapper   = new SNS_Tof_to_Q_map( det_file, new_instrument );
          run_time = (System.nanoTime() - start)/1.0e6;
          System.out.printf("Made Q mapper in %5.1f ms\n", run_time  );
        }
        catch ( Exception ex )
        {
          Util.sendError( "ERROR: Could not make Q mapper for "+ det_file );
          return false;
        }
      }
    }

    else if ( message.getName().equals(Commands.SELECT_POINT) )
    {
      if ( mapper == null )
      {
         Util.sendError( "ERROR: NO TOF TO Qxyz MAPPING AVAILABLE" );
         return false;
      }
      SelectPointCmd   cmd = (SelectPointCmd)message.getValue();
      SelectionInfoCmd info;
                              // PeakQ, Peak_new and SelectionInfCom
                              // use Q = 1/d  but the event display
                              // and select point message use Q = 2PI/d                             
      float eventx =  (float)( cmd.getQx() / (2 * Math.PI) );
      float eventy =  (float)( cmd.getQy() / (2 * Math.PI) );
      float eventz =  (float)( cmd.getQz() / (2 * Math.PI) );
      Peak_new peak = mapper.GetPeak( eventx, eventy, eventz );

      if ( peak == null )
      {
        info = new SelectionInfoCmd( 0, 0, 0, 0, 0, 
                   new Vector3D(),
                   new Vector3D(),
                   0, 0, 0, 0, 0 );
      }
      else
      {
        float[]  Q        = peak.getUnrotQ();
        Vector3D hkl      = new Vector3D( peak.h(), peak.k(), peak.l() );
        Vector3D Qxyz     = new Vector3D( Q[0], Q[1], Q[2] );
        float magnitude_Q = Qxyz.length();
        float Energy      = Calc_energy( peak);

        info = new SelectionInfoCmd(
                   peak.ipkobs(),
                   peak.detnum(),
                   (int)(.5f+peak.x()),
                   (int)(.5f+peak.y()),
                   0, 
                   hkl,
                   Qxyz,
                   magnitude_Q,
                   peak.d(),
                   peak.time(),
                   Energy, 
                   peak.wl()  );
      }
                                           // ask histogram object to get
                                           // correct histogram page and
                                           // correct ipkobs!
        Message add_hist_info_message =
                     new Message( Commands.ADD_HISTOGRAM_INFO, info, true );
        message_center.receive( add_hist_info_message );
    }

    else if ( message.getName().equals(Commands.MAP_EVENTS_TO_Q) )
    {
       Object obj = message.getValue();

       if ( obj != null && obj instanceof ITofEventList )
       {
         ITofEventList ev_list = (ITofEventList)obj;
         IEventList3D[] event_lists = MapToQ( ev_list );

         if ( event_lists != null )
         {
           for ( int i = 0; i < event_lists.length; i++ )
             message_center.receive( new Message( Commands.ADD_EVENTS,
                                                  event_lists[i],
                                                  false ));
         }
       }
       
       return false;
    }

    else if ( message.getName().equals(Commands.GET_PEAK_NEW_LIST) )
    {
      Object obj = message.getValue();
      if ( obj == null )
        return false;

      if ( obj instanceof Vector )
      {
        Vector<Peak_new> peak_new_list =
                                  ConvertPeakQToPeakNew( mapper, (Vector)obj );
        Message peak_new_message =
               new Message( Commands.SET_PEAK_NEW_LIST, peak_new_list, true );

        message_center.receive( peak_new_message );
      }
    }

    return false;
  }

  
  private float Calc_energy( Peak_new Peak)
  {
     float time = Peak.time();
     float row = Peak.y();
     float col = Peak.x();
     float path_length = Peak.getGrid().position(row,col).length()+Peak.L1();
     return tof_calc.Energy( path_length , time );
  }
  

  public static Vector<Peak_new>ConvertPeakQToPeakNew(SNS_Tof_to_Q_map mapper,
                                                      Vector<PeakQ>  q_peaks )
  {
    Vector<Peak_new> new_peaks = new Vector<Peak_new>();

    for  ( int k = 0; k < q_peaks.size(); k++ )
    {
      IPeakQ q_peak = q_peaks.elementAt(k);
      float[] qxyz = q_peak.getUnrotQ();
      Peak_new peak = mapper.GetPeak( qxyz[0], qxyz[1], qxyz[2] );
      if ( peak != null )
      {
        peak.setFacility( "SNS" );
        peak.sethkl( q_peak.h(), q_peak.k(), q_peak.l() );
        peak.seqnum( k );
        peak.ipkobs( q_peak.ipkobs() );
        new_peaks.add( peak );
      }
    }

    return new_peaks;
  }  


  private IEventList3D[] MapToQ( ITofEventList ev_list )
  {
    int N_THREADS = 4;

    if ( ev_list == null )
      return null;

    int num_ev = (int)(ev_list.numEntries());
    if ( num_ev <= 0 )
      return null;

    long start = System.nanoTime();

    int[] tofs = ev_list.eventTof( 0, num_ev );
    int[] ids  = ev_list.eventPixelID( 0, num_ev );

    IEventList3D[] event_lists = null;

    if ( tofs.length > 400000 )          // split into separate threads
    {
      Object results    = null;
      int    first      = 0;
      int    num_to_map = tofs.length/N_THREADS;

      Vector<IOperator> toQ_ops = new Vector<IOperator>();
      for ( int i = 0; i < N_THREADS; i++ )
      {
        if ( i == N_THREADS - 1 )
          num_to_map = tofs.length - first;       // bring in the rest

        toQ_ops.add( new MapEventsToQ_Op( tofs, 
                                          ids, 
                                          first, 
                                          num_to_map,  
                                          mapper ) );
        first += num_to_map;
      }
      try
      {
        ParallelExecutor exec =
                           new ParallelExecutor( toQ_ops, N_THREADS, 600000 );
        results = exec.runOperators();
      }
      catch ( ExecFailException fail_exception )
      {
        results = fail_exception.getPartialResults();
        System.out.println("ExecFailException while converting to Q: " +
                            fail_exception.getFailureStatus() );
      }

      event_lists = new IEventList3D[N_THREADS];

      for ( int i = 0; i < N_THREADS; i++ )
        event_lists[i] = (IEventList3D)(((Vector)results).elementAt(i));
    }
    else
    {
      event_lists = new IEventList3D[1];
      event_lists[0] = mapper.MapEventsToQ( tofs, ids );
    }

    Util.sendInfo("Converted to Q in " + ((System.nanoTime()-start)/1e6) +
                  " ms" );
    return event_lists;
  }

}
