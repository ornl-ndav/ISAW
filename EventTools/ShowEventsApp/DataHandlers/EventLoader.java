
package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;

import gov.anl.ipns.Operator.IOperator;
import gov.anl.ipns.Operator.Threads.ParallelExecutor;
import gov.anl.ipns.Operator.Threads.ExecFailException;
import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.EventList.IEventList3D;
import EventTools.EventList.SNS_Tof_to_Q_map;
import EventTools.EventList.SNS_TofEventList;
import EventTools.EventList.MapEventsToQ_Op;
import EventTools.EventList.EventSegmentLoadOp;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.LoadEventsCmd;
import EventTools.ShowEventsApp.Command.SelectPointCmd;
import EventTools.ShowEventsApp.Command.SelectionInfoCmd;
import EventTools.ShowEventsApp.Command.Util;

import DataSetTools.math.tof_calc;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
import DataSetTools.operator.Generic.TOF_SCD.IPeakQ;
import DataSetTools.operator.Generic.TOF_SCD.PeakQ;

public class EventLoader implements IReceiveMessage
{
  private MessageCenter    message_center;
  private String           instrument_name = null;
  private SNS_Tof_to_Q_map mapper;
  private float[][]        OrientationMatrix = null;
  private float[][]        OrientationMatrixInv = null;

  public EventLoader( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.LOAD_FILE );
    message_center.addReceiver( this, Commands.SELECT_POINT );
    message_center.addReceiver( this, Commands.ADD_HISTOGRAM_INFO_ACK );
    message_center.addReceiver( this, Commands.GET_PEAK_NEW_LIST );
    message_center.addReceiver( this, Commands.SET_HISTOGRAM_MAX );
    message_center.addReceiver(this, Commands.SET_ORIENTATION_MATRIX);
  }


  public boolean receive( Message message )
  {
    System.out.println("***EventLoader in thread " + Thread.currentThread());

    long   start;
    double run_time;

    if ( message.getName().equals(Commands.LOAD_FILE) )
    {
      LoadEventsCmd cmd = (LoadEventsCmd)message.getValue();
      
      String event_file_name = cmd.getEventFile();
      String new_instrument = null;
                                         // figure out the instrument name
      if ( event_file_name.indexOf( "ARCS") >= 0 )
        new_instrument = SNS_Tof_to_Q_map.ARCS;
     
      else if  ( event_file_name.indexOf( "SNAP") >= 0 )
        new_instrument = SNS_Tof_to_Q_map.SNAP;

      else if ( event_file_name.indexOf( "SEQ") >= 0 )
        new_instrument = SNS_Tof_to_Q_map.SEQ;
 
      else
      {
        Util.sendError( message_center, "ERROR: UNSUPPORTED INSTRUMENT " +
                                         event_file_name );
        return false;
      }
                                         // if this is a new instrument, get
                                         // a new mapper.
      if ( instrument_name == null ||
           !new_instrument.equals( instrument_name ) )
      {
        String det_file = cmd.getDetFile();

        if ( det_file == null || det_file.trim().length() == 0 )
        {
          String isaw_home = System.getProperty( "ISAW_HOME" ) + "/";
          det_file = isaw_home + "InstrumentInfo/SNS/" +
                     new_instrument + ".DetCal";
        }

        try
        {
          start    = System.nanoTime();
          mapper   = new SNS_Tof_to_Q_map( det_file, new_instrument );
          run_time = (System.nanoTime() - start)/1.0e6;
          System.out.printf("Made Q mapper in %5.1f ms\n", run_time  );
        }
        catch ( Exception ex )
        {
          Util.sendError( message_center,
                         "ERROR: Could not make Q mapper for "+ det_file );
          return false;
        }

        instrument_name = new_instrument; 

        Message new_inst_mess = new Message( Commands.SET_NEW_INSTRUMENT,
                                             new_instrument, true );
        message_center.receive( new_inst_mess );
      }

      start = System.nanoTime();
      try
      {
        LoadEvents( event_file_name, 
                    cmd.getFirstEvent(),
                    cmd.getEventsToLoad(),
                    cmd.getEventsToShow(),
                    cmd.getNumThreads()  ); 
      }
      catch ( Exception ex )
      {
        Util.sendError( message_center, "ERROR: Failed to Load File :" +
                        event_file_name + "\n" + ex );
        return false;
      }

      run_time = (System.nanoTime() - start)/1.0e6;
      String load_str = String.format( "Loaded file in %5.1f ms\n", run_time );
      Util.sendInfo( message_center, load_str );
      return false;

    }
    else if ( message.getName().equals(Commands.SELECT_POINT) )
    {
      if ( mapper == null )
      {
         Util.sendError( message_center, "NO DATA YET" );
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
        
        if( OrientationMatrix != null)
           hkl =Calc_hkl(Q, OrientationMatrixInv);
        float Energy = Calc_energy( peak);
        
        info = new SelectionInfoCmd(
                   peak.ipkobs(),
                   peak.detnum(),
                   (int)(.5f+peak.x()),
                   (int)(.5f+peak.y()),
                   0,                      // TODO  get correct histogram page
                   hkl,
                   Qxyz,
                   magnitude_Q,
                   peak.d(),
                   peak.time(),
                   Energy,              // TODO get correct energy
                   peak.wl()  );
      }
                                           // ask histogram object to get
                                           // correct histogram page and
                                           // correct ipkobs!
        Message add_hist_info_message =
                     new Message( Commands.ADD_HISTOGRAM_INFO, info, true );
        message_center.receive( add_hist_info_message );
    }
    else if  ( message.getName().equals(Commands.ADD_HISTOGRAM_INFO_ACK) )
    {
      SelectionInfoCmd new_info = (SelectionInfoCmd)message.getValue();
      Message info_message = 
                   new Message( Commands.SELECTED_POINT_INFO, new_info, true );
      message_center.receive( info_message );
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

    else if ( message.getName().equals(Commands.SET_HISTOGRAM_MAX) )
    {                                       // All we do  is display the
      Object obj = message.getValue();      // max value
      if ( obj == null )
        return false;

      if ( obj instanceof Float )
        Util.sendInfo( message_center, "Max Histogram Value : " + obj );
    }
    else if( message.getName().equals( Commands.SET_ORIENTATION_MATRIX ))
    {
       OrientationMatrix = (float[][])message.getValue();
       if(!isOrientationOK())
       {   OrientationMatrix = OrientationMatrixInv = null;
           return  true;
       }
       OrientationMatrix = LinearAlgebra.getTranspose( OrientationMatrix );
       OrientationMatrixInv = LinearAlgebra.getInverse( OrientationMatrix );
    }

    return false;
  }
  
  private Vector3D Calc_hkl(float[] Q, float[][]OrientationMatrixInv)
  {
     if( OrientationMatrixInv == null)
        return new Vector3D(0f,0f,0f);
     System.out.println("++++++++++++++Q+++++++++++++++++++");
     LinearAlgebra.print( Q );
     System.out.println("     - -- - - UBnv- - - - -- -");
     LinearAlgebra.print( OrientationMatrixInv );
     System.out.println("++++++++++++++++++++++++++++");
     float[]hkl = new float[3];
     java.util.Arrays.fill( hkl , 0f );
     for( int i=0;i<3;i++)
        for( int j=0; j< 3; j++)
            hkl[i] +=OrientationMatrixInv[i][j]*Q[j];
     
     return new Vector3D(hkl);
  }
  
  private float Calc_energy( Peak_new Peak)
  {
     float time = Peak.time();
     float row = Peak.y();
     float col = Peak.x();
     float path_length = Peak.getGrid().position(row,col).length()+Peak.L1();
     return tof_calc.Energy( path_length , time );
  }
  
  
  private boolean isOrientationOK()
  {
     if( OrientationMatrix == null)
        return false;
     if( OrientationMatrix.length !=3)
        return false;
     for( int i=0; i<3;i++)
        if(OrientationMatrix[i].length !=3)
           return false;
     return true;
  }


  private void LoadEvents( String event_file_name, 
                           long   first, 
                           long   num_to_load,
                           long   num_to_show,
                           int    num_threads  )
  {
    System.out.println("FIRST = " + first );
    System.out.println("NUM_TO_LOAD = " + num_to_load );
    System.out.println("NUM_TO_SHOW = " + num_to_show );
    System.out.println("NUM_THREADS = " + num_threads );

    SNS_TofEventList check_file = new SNS_TofEventList( event_file_name );
    long num_available = check_file.numEntries();
    check_file = null;

    if ( num_available > 0 )
    {
      message_center.receive(
                       new Message( Commands.CLEAR_HISTOGRAM, null, true) );
      message_center.receive( 
                       new Message( Commands.CLEAR_EVENTS_VIEW, null, true) );
      message_center.receive( new Message( Commands.CLEAR_DQ, null, true) );
    }
    else
    {
      System.out.println("ERROR: No events in, or can't open " + 
                          event_file_name );
      return;
    }
                                           // we'll save the events we'll view
                                           // in this vector
    Vector<IEventList3D> show_lists = new Vector<IEventList3D>();

    if ( num_to_load > num_available )     // can load more than exist
      num_to_load = num_available;

    if ( num_to_show > num_to_load )       // can't show more than are loaded
      num_to_show = num_to_load; 

    long MAX_SEG_SIZE = 10000000;
    long seg_size = num_to_load / num_threads;
    if ( seg_size > MAX_SEG_SIZE )
      seg_size = MAX_SEG_SIZE;

    boolean done       = false;
    long    num_loaded = 0;
    long    num_viewed = 0;
    Vector<IOperator> ops = new Vector<IOperator>();
    while ( !done )
    {
      ops.clear();
      int n_threads = 0;
      while ( n_threads < num_threads && num_loaded < num_to_load )
      {
        System.out.println("FIRST = " + first + " SEG_SIZE = " + seg_size );
        ops.add( new EventSegmentLoadOp( event_file_name, first, seg_size ) );
        first      += seg_size;
        num_loaded += seg_size;
        if ( num_to_load - num_loaded < seg_size )
          seg_size = num_to_load - num_loaded;
        n_threads++;     
      }

      long start_time = System.nanoTime();

      Object results;
      try
      {
        ParallelExecutor exec = new ParallelExecutor( ops, n_threads, 600000 );
        results = exec.runOperators();
      }
      catch ( ExecFailException fail_exception )
      {
        results = fail_exception.getPartialResults();
        System.out.println("ExecFailException while loading events: " +
                            fail_exception.getFailureStatus() );
        Util.sendError( message_center,
                   "Failed to load events from " + event_file_name );

        Util.sendError( message_center,
                        fail_exception.getFailureStatus().toString() );
        return;
      }
      long run_time = System.nanoTime() - start_time;
      System.out.printf("LOADED %d EVENTS IN %5.1f ms\n" ,
                         num_loaded, (run_time/1.0e6) );

      int num_segs = ((Vector)results).size();
      int[][] tofs = new int[num_segs][];
      int[][] ids  = new int[num_segs][];
      for ( int i = 0; i < num_segs; i++ )
      {
        Vector array_vec = (Vector)((Vector)results).elementAt(i);
        tofs[i] = (int[])array_vec.elementAt(0);
        ids[i]  = (int[])array_vec.elementAt(1);
      }

      ops.clear();
      for ( int i = 0; i < n_threads; i++ )
        ops.add( new MapEventsToQ_Op( tofs[i], ids[i], mapper ) );

      start_time = System.nanoTime();

      try
      {
        ParallelExecutor exec =
                           new ParallelExecutor( ops, n_threads, 600000 );
        results = exec.runOperators();
      }
      catch ( ExecFailException fail_exception )
      {
        results = fail_exception.getPartialResults();
        Util.sendError( message_center,
                   "Failed to convert events to Q " + event_file_name );

        Util.sendError( message_center, 
                        fail_exception.getFailureStatus().toString() );
        return;
      }
      run_time = System.nanoTime() - start_time;
      Util.sendInfo( message_center, 
            String.format("PARALLEL CONVERTED %d EVENTS TO Q IN %5.1f ms\n",
                           num_loaded, (run_time/1.0e6) ) );

      Vector<IEventList3D> event_lists = (Vector<IEventList3D>)results;

      for ( int i = 0; i < event_lists.size(); i++ )
      {
        message_center.receive( new Message( Commands.ADD_EVENTS, 
                                             event_lists.elementAt(i),
                                             false ));

        IEventList3D list = event_lists.elementAt(i);
        if ( list == null )
          System.out.println("ERROR: null list in EventLoader");
        else
        { 
/*
          int n_printed = Math.min( 10, list.numEntries());
          System.out.println("EventLoader, First " + n_printed + 
                             " events-------------------------");
          for ( int k = 0; k < n_printed; k++ )
            System.out.println( "xyz = " + list.eventX(k) + " " +
                                           list.eventY(k) + " " +
                                           list.eventZ(k) + " " +
                                "weight = " + list.eventWeight(k) );
*/
          if ( num_viewed < num_to_show )
          {
            show_lists.add( event_lists.elementAt(i) );
            num_viewed += event_lists.elementAt(i).numEntries();
          }
        }
      }

      if ( num_loaded >= num_to_load )
        done = true; 
    }

    Util.sendInfo( message_center, "Loaded " + num_loaded + 
                                   " from "  + event_file_name );

    for ( int i = 0; i < show_lists.size(); i++ )
      message_center.receive(new Message(Commands.SET_WEIGHTS_FROM_HISTOGRAM,
                                         show_lists.elementAt(i),
                                         false ));

    for ( int i = 0; i < show_lists.size(); i++ )
      message_center.receive( new Message( Commands.ADD_EVENTS_TO_VIEW,
                                           show_lists.elementAt(i),
                                           false ));

                                          // Now that we've added the events,
                                          // ask the histogram to announce
                                          // the max value it has.
    message_center.receive( new Message( Commands.GET_HISTOGRAM_MAX,
                                         null,
                                         true ));
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

}
