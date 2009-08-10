
package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;
import EventTools.Viewers.SlicedEventsViewer;
import EventTools.Histogram.Histogram3D;
import EventTools.EventList.IEventList3D;
import EventTools.ShowEventsApp.Command.Commands;


public class EventViewHandler implements IReceiveMessage
{
  private MessageCenter      message_center;
  private SlicedEventsViewer my_viewer; 


  public EventViewHandler( MessageCenter message_center,
                           Histogram3D   histogram )
  {
    System.out.println("THREAD = " + Thread.currentThread() );
    System.out.println("0 Constructing EventViewHandler ++++++++++++++");
    this.message_center = message_center;
    System.out.println("1 Constructing EventViewHandler ++++++++++++++");
    message_center.addReceiver( this, Commands.ADD_EVENTS_TO_VIEW );
    System.out.println("2 Constructing EventViewHandler ++++++++++++++");
    my_viewer = new SlicedEventsViewer( histogram, "Events in Q" );
    System.out.println("3 Constructing EventViewHandler ++++++++++++++");
    System.out.println("4 my_viewer = " + my_viewer );
  }


  public boolean receive( Message message )
  {
    if ( message.getName().equals(Commands.ADD_EVENTS_TO_VIEW) )
    {
      IEventList3D events = (IEventList3D)message.getValue();
      System.out.println("ASKED TO ADD EVENTS " + events.numEntries() );
      System.out.println("my_viewer = " + my_viewer );
      my_viewer.add_events( events, true, 0, 0, 0 );
    }
    return false;
  }

}
