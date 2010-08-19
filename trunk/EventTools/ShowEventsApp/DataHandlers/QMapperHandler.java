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
//import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.EventList.IEventList3D;
import EventTools.EventList.SNS_Tof_to_Q_map;
import EventTools.EventList.ITofEventList;
import EventTools.EventList.MapEventsToQ_Op;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.PeaksCmd;
import EventTools.ShowEventsApp.Command.IntegratePeaksCmd;
import EventTools.ShowEventsApp.Command.PeakImagesCmd;
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
  private String           instrument_name;
  private SNS_Tof_to_Q_map mapper;
  private Vector           omit_pixels_info  = null;
  private Vector           omit_q_range_info = null;

  public QMapperHandler( MessageCenter message_center )
  {
    instrument_name = "UNSPECIFIED";
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.INIT_NEW_INSTRUMENT );
    message_center.addReceiver( this, Commands.MAP_EVENTS_TO_Q );
    message_center.addReceiver( this, Commands.SELECT_POINT );
    message_center.addReceiver( this, Commands.SET_PEAK_Q_LIST );
    message_center.addReceiver( this, Commands.SET_INTEGRATED_PEAKS_LIST );
    message_center.addReceiver( this, Commands.REVERSE_WEIGHT_INTEGRALS );

    message_center.addReceiver( this, Commands.CLEAR_OMITTED_PIXELS );
    message_center.addReceiver( this, Commands.APPLY_OMITTED_PIXELS );
    message_center.addReceiver( this, Commands.CLEAR_OMITTED_DRANGE );
    message_center.addReceiver( this, Commands.APPLY_OMITTED_DRANGE );
  }


  public boolean receive( Message message )
  {
    long   start;
    double run_time;

    if ( message.getName().equals(Commands.INIT_NEW_INSTRUMENT) )
    {

      Object obj = message.getValue();
      if ( obj == null || !(obj instanceof SetNewInstrumentCmd) )
      {
        Util.sendError( "Wrong type value in INIT_NEW_INSTRUMENT command" );
        message_center.send( new Message( Commands.LOAD_FAILED,
                                          null, true, true ) );
      }

      SetNewInstrumentCmd cmd = (SetNewInstrumentCmd)obj;

      String new_instrument = cmd.getInstrumentName();
      String det_file  = cmd.getDetectorFileName();
      String bank_file = cmd.getBankFileName();
      String map_file  = cmd.getIDMapFileName();
      String spec_file = cmd.getIncidentSpectrumFileName();
      try 
      { 
        start  = System.nanoTime();
        mapper = new SNS_Tof_to_Q_map( new_instrument,
                                       det_file, 
                                       bank_file,
                                       map_file,
                                       spec_file,
                                       cmd.getAbsorptionPower(),
                                       cmd.getAbsorptionRadius(),
                                       cmd.getTotalAbsorption(),
                                       cmd.getAbsorptionTrue()   );
        ApplyPixelFilter( mapper );
        ApplyMagQ_Filter( mapper );
        run_time = (System.nanoTime() - start)/1.0e6;
        instrument_name = new_instrument;
        System.out.printf("Made Q mapper in %5.1f ms\n", run_time  );
      }
      catch ( Exception ex )
      {
        Util.sendError( "ERROR: Could not make Q mapper from: "+ det_file +
                        "\n with Bank File: " + bank_file +
                        "\n with ID Map File: " +map_file +
                        "\n with Spectrum File: " + spec_file +
                        "\n for instrument name: " + new_instrument );
        message_center.send( new Message( Commands.LOAD_FAILED,
                                          null, true, true ) );
        ex.printStackTrace();
        return false;
      }

      mapper.setMinQ( cmd.getMinQValue() );
      mapper.setMaxQ( cmd.getMaxQValue() );
      mapper.setAbsorptionParameters( cmd.getAbsorptionPower(),
                                      cmd.getAbsorptionRadius(),
                                      cmd.getTotalAbsorption(),
                                      cmd.getAbsorptionTrue()  );

      Util.sendInfo( "QMapper set up for " + instrument_name +
                     "\nUsing Detector File: " + det_file +
                     "\nUsing Spectrum File: " + spec_file );
      Message new_inst_done = new Message( Commands.INIT_NEW_INSTRUMENT_DONE,
                                           null,
                                           true,
                                           true );
      message_center.send( new_inst_done );
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
                              // PeakQ, Peak_new and SelectionInfoCmd
                              // use Q = 1/d  but the event display
                              // and SelectPointCmd  use Q = 2PI/d so 
                              // must switch the convention NOW!
      float qx = (float)( cmd.getQx() / (2 * Math.PI) );
      float qy = (float)( cmd.getQy() / (2 * Math.PI) );
      float qz = (float)( cmd.getQz() / (2 * Math.PI) );
      Vector3D Qxyz = new Vector3D( qx, qy, qz );

      float  magnitude_Q = Qxyz.length();
      float  d           = 1/magnitude_Q; 
      double off_axis    = Math.sqrt( qy * qy + qz * qz );
      double beam_comp   = qx;
      float  alpha       = (float)Math.atan2(off_axis,beam_comp);
      float  two_theta   = (float)(2*Math.abs(alpha) - Math.PI); 

      float  wl     = (float) (2 * d * Math.sin( two_theta/2 )); 
      float  weight = mapper.getEventWeight( wl, two_theta );
      float  energy = tof_calc.EnergyFromWavelength( wl );
      float  tof    = 0;         // fix this

      int    detnum      = 0;
      int    col         = 0;
      int    row         = 0;
                
      Peak_new peak = mapper.GetPeak( qx, qy, qz, null );
      if ( peak != null )
      {                                  // This info can ONLY be provided for
        tof    = peak.time();            // points in Q that correspond to
        detnum = peak.detnum();          // actual detector positions
        col    = (int)(.5f+peak.x());
        row    = (int)(.5f+peak.y());
      }

      float    ipkobs = 0;               // value will be provided by the
                                         // HistogramHandler
      Vector3D hkl    = new Vector3D();  // hkl value will be provided by the 
                                         // OrientationMatrixHandler

      info = new SelectionInfoCmd(
                   ipkobs,
                   0, 0, 
                   weight,
                   detnum,
                   col,
                   row,
                   0, 
                   0,
                   hkl,
                   Qxyz,
                   magnitude_Q,
                   d,
                   two_theta,
                   tof,
                   energy, 
                   wl  );
                                           // ask histogram object to get
                                           // correct histogram page and
                                           // correct ipkobs!
        Message add_hist_info_message =
                new Message( Commands.ADD_HISTOGRAM_INFO, info, true, true );
        message_center.send( add_hist_info_message );
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
             message_center.send(new Message(Commands.ADD_EVENTS_TO_HISTOGRAMS,
                                              event_lists[i],
                                              false,
                                              true ));
         }
       }
       
       return false;
    }

    else if ( message.getName().equals(Commands.SET_PEAK_Q_LIST) )
    {
      Object obj = message.getValue();
      if ( obj == null )
        return false;

      if ( obj instanceof PeaksCmd )
      {
        PeaksCmd peaks_cmd = (PeaksCmd)obj;

        Vector<PeakQ> peakQs = peaks_cmd.getPeaks(); 
        if ( peakQs == null )
          return false;

        Vector<Peak_new> peak_new_list = ConvertPeakQToPeakNew(mapper, peakQs);

        PeaksCmd new_cmd = new PeaksCmd( peak_new_list,
                                         peaks_cmd.getShowImages(),
                                         peaks_cmd.getImageSize(),
                                         peaks_cmd.getMaxOffset() );

        Message peak_new_message =
          new Message( Commands.SET_PEAK_NEW_LIST, new_cmd, true, true );

        message_center.send( peak_new_message );
      }
    }

    else if ( message.getName().equals(Commands.SET_INTEGRATED_PEAKS_LIST ) )
    {
      Object obj = message.getValue();
      if ( obj == null )
        return false;
                                      // In this case, the value should be a
      if ( obj instanceof Vector )    // Vector with two Vectors, one giving
      {                               // PeakQ's, the other integration info
        Vector info_vec = (Vector)obj;
        if ( info_vec.elementAt(0) instanceof Vector &&
             info_vec.elementAt(1) instanceof Vector )
        {
          Vector peakQs  = (Vector)info_vec.elementAt(0);
          Vector i_sigis = (Vector)info_vec.elementAt(1); 

          float[] run_info = new float[4];
          if (info_vec.size() > 2 && info_vec.elementAt(2) instanceof float[] )
          {
            run_info = (float[])(info_vec.elementAt(2));
            if ( run_info.length != 4 )
              run_info = new float[4];
          }

          Vector<Peak_new> peak_new_list =
          ConvertIntegratedPeakQToPeakNew( mapper, peakQs, i_sigis, run_info );

          Message peak_new_message =
            new Message(Commands.SET_PEAK_NEW_LIST, peak_new_list, true, true);

          message_center.send( peak_new_message );
        }
      }
    }

    else if ( message.getName().equals( Commands.REVERSE_WEIGHT_INTEGRALS ) )
    {
      Object obj = message.getValue();
      if ( obj instanceof IntegratePeaksCmd )
      {
        IntegratePeaksCmd cmd = (IntegratePeaksCmd)obj;
        Vector<IPeakQ> peakQs = cmd.getPeaks();
        Vector<float[]> i_isigis = cmd.getI_and_IsigI();
        if ( peakQs   == null || peakQs.size() == 0 ||
             i_isigis == null || i_isigis.size() != peakQs.size() )
         return false;

        Vector<Peak_new> peak_new_list = 
             ConvertIntegratedPeakQToPeakNew( mapper, peakQs, i_isigis, null );

        for ( int i = peak_new_list.size()-1; i >= 0; i-- )
        {
          Peak_new peak = peak_new_list.elementAt(i);
          float[] q  = peak.getUnrotQ();
          float   qx = q[0]; 
          float   qy = q[1];
          float   qz = q[2];
          Vector3D Qxyz = new Vector3D( qx, qy, qz );

          float  magnitude_Q = Qxyz.length();
          float  d           = 1/magnitude_Q;
          double off_axis    = Math.sqrt( qy * qy + qz * qz );
          double beam_comp   = qx;
          float  alpha       = (float)Math.atan2(off_axis,beam_comp);
          float  two_theta   = (float)(2*Math.abs(alpha) - Math.PI);

          float  wl     = (float) (2 * d * Math.sin( two_theta/2 ));
          float  weight = mapper.getEventWeight( wl, two_theta );

          if ( weight != 0 )
          {
            peak.inti( peak.inti() / weight );
            peak.sigi( peak.sigi() / (float)Math.sqrt(weight) );
          }
          else
            peak_new_list.remove(i);
        }
 
        float[] levels       = QuickIntegrateHandler.levels;
        int[]   level_counts = new int[ levels.length ];
        for ( int i = 0; i < peak_new_list.size(); i++ )
        {
          Peak_new peak = peak_new_list.elementAt(i);
          if ( peak.inti() > levels[0] )
          {
            level_counts[0]++;
            for ( int j = 1; j < levels.length; j++ )
              if ( peak.sigi() > levels[j] )
                level_counts[j]++;
          }
        }
        Message stats_mess 
                     = new Message( Commands.SET_INTEGRATED_INTENSITY_STATS,
                                    level_counts, true, true );
        message_center.send( stats_mess );

        if ( cmd.getRecord_as_peaks_list() )
        {
          Message peak_new_message =
            new Message(Commands.SET_PEAK_NEW_LIST, peak_new_list, true, true);

          message_center.send( peak_new_message );
        }
      }
    }

    else if ( message.getName().equals(Commands.APPLY_OMITTED_PIXELS) )
    {
      Object obj = message.getValue();
      if ( obj == null || !(obj instanceof Vector))
        return false;

      omit_pixels_info = (Vector)obj;
    }

    else if ( message.getName().equals(Commands.CLEAR_OMITTED_PIXELS) )
    {
       omit_pixels_info = null;
    }

    else if ( message.getName().equals(Commands.APPLY_OMITTED_DRANGE ) )
    {
      Object obj = message.getValue();
      if ( obj == null || !(obj instanceof Vector))
        return false;

      omit_q_range_info = (Vector)obj;
    }

    else if ( message.getName().equals(Commands.CLEAR_OMITTED_DRANGE) )
    {
       omit_q_range_info = null;
    }

    return false;
  }


  private void ApplyPixelFilter( SNS_Tof_to_Q_map mapper )
  {
    if ( omit_pixels_info != null )
    {
                                                  // filter using filter file
                                                  // will be implemented later
      String filename = (String)omit_pixels_info.elementAt(0);
      if ( filename != null )
        System.out.println("Would load omitted PixelInfo from " + filename );

      for ( int i = 1; i < omit_pixels_info.size(); i++ )
      {
        int[][] int_arrays = (int[][])omit_pixels_info.elementAt(i);
        int[]   det_ids = int_arrays[0];
        int[]   row_ids = int_arrays[1];
        int[]   col_ids = int_arrays[2];

        if ( det_ids == null )
          det_ids = mapper.getGridIDs();

        if ( row_ids == null && col_ids == null )      // skip whole detectors
          mapper.maskOffDetectors( det_ids );
        else
        {
          if ( row_ids != null )                              // skip rows
            mapper.maskOffDetectorRows( det_ids, row_ids );

          if ( col_ids != null )                              // skip columns
            mapper.maskOffDetectorColumns( det_ids, col_ids );
        }
      }  
    }
  }


  private void ApplyMagQ_Filter( SNS_Tof_to_Q_map mapper )
  {
    if ( omit_q_range_info != null )
    {
      try 
      {
        boolean omit_flag = (Boolean)omit_q_range_info.elementAt(0);
        float[] endpoints = (float[])omit_q_range_info.elementAt(1);

        mapper.setQ_Filter( endpoints, omit_flag ); 
      }
      catch ( Exception ex )
      {
        System.out.println("Received Invalid message for Q-Range to omit");
      }
    }
  }

 
  private float Calc_energy( Peak_new Peak)
  {
     float time = Peak.time();
     float row  = Peak.y();
     float col  = Peak.x();
     float path_length = Peak.getGrid().position(row,col).length()+Peak.L1();
     return tof_calc.Energy( path_length , time );
  }
  

  public static Vector<Peak_new>ConvertPeakQToPeakNew(SNS_Tof_to_Q_map mapper,
                                                      Vector<PeakQ>  q_peaks )
  {
    Vector<Peak_new> new_peaks = new Vector<Peak_new>();

    for  ( int k = 0; k < q_peaks.size(); k++ )
    {
      IPeakQ   q_peak = q_peaks.elementAt(k);
      float[]  qxyz   = q_peak.getUnrotQ();
      Peak_new peak   = mapper.GetPeak( qxyz[0], qxyz[1], qxyz[2], null );
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


  public static Vector<Peak_new>ConvertIntegratedPeakQToPeakNew
                                                 ( SNS_Tof_to_Q_map  mapper, 
                                                   Vector<IPeakQ>    q_peaks, 
                                                   Vector<float[]>   IsigIs,
                                                   float[]           run_info )
  {
    Vector<Peak_new> new_peaks = new Vector<Peak_new>();

    for  ( int k = 0; k < q_peaks.size(); k++ )
    {
      IPeakQ   q_peak = q_peaks.elementAt(k);
      float[]  i_sigi = IsigIs.elementAt(k);
      float[]  qxyz   = q_peak.getUnrotQ();
      Peak_new peak   = mapper.GetPeak( qxyz[0], qxyz[1], qxyz[2], run_info );
      if ( peak != null )
      {
        peak.setFacility( "SNS" );
        peak.sethkl( q_peak.h(), q_peak.k(), q_peak.l() );
        peak.seqnum( k );
        peak.ipkobs( q_peak.ipkobs() );
        peak.inti( i_sigi[0] );
        peak.sigi( i_sigi[1] );
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


    int num_events = (int)(ev_list.numEntries());
    if ( num_events <= 0 )
      return null;

    IEventList3D[] event_lists = null;

    if ( num_events > 400000 )          // split into separate threads
    {
      Object results    = null;
      int    first      = 0;
      int    num_to_map = num_events/N_THREADS;

      Vector<IOperator> toQ_ops = new Vector<IOperator>();
      for ( int i = 0; i < N_THREADS; i++ )
      {
        if ( i == N_THREADS - 1 )
          num_to_map = num_events - first;       // bring in the rest

        toQ_ops.add( new MapEventsToQ_Op( ev_list, 
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
      int num_to_map = (int)ev_list.numEntries();
      event_lists[0] = mapper.MapEventsToQ( ev_list, 0, num_to_map );
    }

//    Util.sendInfo("Converted to Q in " + ((System.nanoTime()-start)/1e6) +
//                  " ms" );
    return event_lists;
  }

}
