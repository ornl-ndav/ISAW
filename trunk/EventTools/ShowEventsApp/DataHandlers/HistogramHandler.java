
package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;

import gov.anl.ipns.Operator.IOperator;
import gov.anl.ipns.Operator.Threads.ParallelExecutor;
import gov.anl.ipns.Operator.Threads.ExecFailException;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;
import EventTools.Histogram.Histogram3D;
import EventTools.EventList.IEventList3D;
import EventTools.EventList.SNS_Tof_to_Q_map;
import EventTools.ShowEventsApp.Command.Commands;


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
  }


  public boolean receive( Message message )
  {
    if ( message.getName().equals(Commands.ADD_EVENTS) )
    {
      IEventList3D events = (IEventList3D)message.getValue();
      System.out.println("ASKED TO ADD EVENTS " + events.numEntries() );
      histogram.addEvents( events );
      System.out.println("MAX HISTOGRAM BIN " + histogram.maxVal() );
    }
    return false;
  }

}
