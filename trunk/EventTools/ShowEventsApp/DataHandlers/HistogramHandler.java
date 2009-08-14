
package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;

import gov.anl.ipns.Operator.IOperator;
import gov.anl.ipns.Operator.Threads.ParallelExecutor;
import gov.anl.ipns.Operator.Threads.ExecFailException;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;
import EventTools.Histogram.Histogram3D;
import EventTools.EventList.IEventList3D;
import EventTools.EventList.SNS_Tof_to_Q_map;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.SelectionInfoCmd;


public class HistogramHandler implements IReceiveMessage
{
  private static int    NUM_THREADS = 6;
  private MessageCenter message_center;
  private Histogram3D   histogram;

  public HistogramHandler( MessageCenter message_center,
                           Histogram3D   histogram      )
  {
    this.message_center = message_center;
    this.histogram      = histogram;
    message_center.addReceiver( this, Commands.ADD_EVENTS );
    message_center.addReceiver( this, Commands.CLEAR_HISTOGRAM );
    message_center.addReceiver( this, Commands.SET_WEIGHTS_FROM_HISTOGRAM );
    message_center.addReceiver( this, Commands.ADD_HISTOGRAM_INFO );
  }


  public boolean receive( Message message )
  {
    System.out.println("***HistogramHandler in thread " 
                       + Thread.currentThread());

    if ( message.getName().equals(Commands.ADD_EVENTS) )
    {
      IEventList3D events = (IEventList3D)message.getValue();
      System.out.println("ASKED TO ADD EVENTS " + events.numEntries() );
      histogram.addEvents( events );
      System.out.println("MIN HISTOGRAM BIN " + histogram.minVal() );
      System.out.println("MAX HISTOGRAM BIN " + histogram.maxVal() );
    }
    else if (  message.getName().equals(Commands.CLEAR_HISTOGRAM) )
    {
      histogram.clear();
      System.out.println("CLEARED HISTOGRAM");
    }
    else if ( message.getName().equals(Commands.SET_WEIGHTS_FROM_HISTOGRAM))
    {
      IEventList3D events = (IEventList3D)message.getValue();

      int n_events = events.numEntries();

      float[] weights = events.eventWeights();
      if ( weights == null || weights.length != n_events )
        weights = new float[ n_events ];

      float[] xyz = events.eventVals();

      float eventX,
            eventY,
            eventZ;

      int index = 0;
      for ( int i = 0; i < n_events; i++ )
      {
        eventX     = xyz[ index++ ];
        eventY     = xyz[ index++ ];
        eventZ     = xyz[ index++ ];
        weights[i] = histogram.valueAt( eventX, eventY, eventZ );
      }

      System.out.println("SET WEIGHTS FROM HISTOGRAM");
    }
    else if ( message.getName().equals(Commands.ADD_HISTOGRAM_INFO))
    {
      Object val = message.getValue();
      if ( val instanceof SelectionInfoCmd )         // fill in counts field
      {
        SelectionInfoCmd select_info_cmd = (SelectionInfoCmd)val;
        Vector3D Qxyz = select_info_cmd.getQxyz();

        float counts = histogram.valueAt( Qxyz.getX(), 
                                          Qxyz.getY(), 
                                          Qxyz.getZ() );
        select_info_cmd.setCounts( counts );

        Message info_message =
                   new Message( Commands.ADD_HISTOGRAM_INFO_ACK, 
                                select_info_cmd, 
                                true );
        System.out.println("SET COUNT FROM HISTOGRAM");
        message_center.receive( info_message );
      }
    }
    return false;
  }


  public static void SetWeightsFromHistgram( IEventList3D events, 
                                             Histogram3D histogram )
  {
    int n_events = events.numEntries();

    float[] weights = events.eventWeights();
    if ( weights == null || weights.length != n_events )
      weights = new float[ n_events ];

    float[] xyz = events.eventVals();

    float eventX,
          eventY,
          eventZ;

    int index = 0;
    for ( int i = 0; i < n_events; i++ )
    {
      eventX     = xyz[ index++ ];
      eventY     = xyz[ index++ ];
      eventZ     = xyz[ index++ ];
      weights[i] = histogram.valueAt( eventX, eventY, eventZ );
    }
  }

}
