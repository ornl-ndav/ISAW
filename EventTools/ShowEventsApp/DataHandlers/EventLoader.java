
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
import EventTools.EventList.MapEventsToQ_Op;
import EventTools.EventList.EventSegmentLoadOp;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.LoadEventsCmd;
import EventTools.ShowEventsApp.Command.SelectPointCmd;
import EventTools.ShowEventsApp.Command.SelectionInfoCmd;

import DataSetTools.operator.Generic.TOF_SCD.Peak_new;

public class EventLoader implements IReceiveMessage
{
  private static int       NUM_THREADS = 6;
  private MessageCenter    message_center;
  private String           instrument_name = null;
  private SNS_Tof_to_Q_map mapper;


  public EventLoader( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.LOAD_FILE );
    message_center.addReceiver( this, Commands.SELECT_POINT );
  }


  public boolean receive( Message message )
  {
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
        System.out.println("ERROR: UNSUPPORTED INSTRUMENT" +
                            event_file_name );
        return false;
      }
                                         // if this is a new instrument, get
                                         // a new mapper.
      if ( instrument_name == null ||
           !new_instrument.equals( instrument_name ) )
      {
        String det_file = cmd.getDetFile();
        if ( det_file == null )
        {
          System.out.println("ERROR: Detector File is null" );
          return false;
        }
        long start = System.nanoTime();
       
        try
        {
          mapper = new SNS_Tof_to_Q_map( cmd.getDetFile(), 
                                         new_instrument );
          System.out.println("Made Q mapper in " + 
                             (System.nanoTime() - start)/1.0e6 + " ms" );
        }
        catch ( Exception ex )
        {
          System.out.println("ERROR: Could not make Q mapper for " +
                              cmd.getDetFile() );
          return false;
        }
      }

      LoadEvents( event_file_name, 
                  cmd.getFirstEvent(),
                  cmd.getEventsToLoad(),
                  cmd.getEventsToShow() ); 
    }
    else if ( message.getName().equals(Commands.SELECT_POINT) )
    {
      SelectPointCmd   cmd = (SelectPointCmd)message.getValue();
      SelectionInfoCmd info;
      Peak_new peak = mapper.GetPeak( cmd.getQx(), cmd.getQy(), cmd.getQz() );

      if ( peak == null )
      {
        info = new SelectionInfoCmd( 0, 0, 0, 
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

        info = new SelectionInfoCmd(
                   peak.ipkobs(),
                   peak.detnum(),
                   0,                      // TODO  get correct histogram page
                   hkl,
                   Qxyz,
                   magnitude_Q,
                   peak.d(),
                   peak.time(),
                   Float.NaN,              // TODO get correct energy
                   peak.wl()  );
      }
      Message info_message = 
                     new Message( Commands.SELECTED_POINT_INFO, info, true);
      message_center.receive( info_message );
    }
    return false;
  }


  private void LoadEvents( String event_file_name, 
                           long   first, 
                           long   num_to_load,
                           long   num_to_show )
  {
    System.out.println("FIRST = " + first );
    System.out.println("NUM_TO_LOAD = " + num_to_load );
    System.out.println("NUM_TO_SHOW = " + num_to_show );

    long MAX_SEG_SIZE = 2000000;
                                           // we'll save the events we'll view
                                           // in this vector
    Vector<IEventList3D> show_lists = new Vector<IEventList3D>();

    if ( num_to_show > num_to_load )       // can't show more than are loaded
      num_to_show = num_to_load; 

    long seg_size = num_to_load / NUM_THREADS;
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
      while ( n_threads < NUM_THREADS && num_loaded < num_to_load )
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
        System.out.println("ExecFailException while converting to Q: " +
                            fail_exception.getFailureStatus() );
      }
      run_time = System.nanoTime() - start_time;
      System.out.printf("PARALLEL CONVERTED %d EVENTS TO Q IN %5.1f ms\n" ,
                        num_loaded, (run_time/1.0e6) );

      Vector<IEventList3D> event_lists = (Vector<IEventList3D>)results;
      for ( int i = 0; i < event_lists.size(); i++ )
      {
        message_center.receive( new Message( Commands.ADD_EVENTS, 
                                             event_lists.elementAt(i),
                                             false ));
        if ( num_viewed < num_to_show )
        {
          show_lists.add( event_lists.elementAt(i) );
          num_viewed += event_lists.elementAt(i).numEntries();
        }
      }

      if ( num_loaded >= num_to_load )
        done = true; 
    }

    for ( int i = 0; i < show_lists.size(); i++ )
      message_center.receive( new Message( Commands.ADD_EVENTS_TO_VIEW,
                                           show_lists.elementAt(i),
                                           false ));
  }


}
