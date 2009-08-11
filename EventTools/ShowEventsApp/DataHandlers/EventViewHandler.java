
package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;
import java.awt.*;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;
import EventTools.Viewers.SlicedEventsPanel;
import EventTools.Histogram.Histogram3D;
import EventTools.EventList.IEventList3D;
import EventTools.ShowEventsApp.Command.Commands;


public class EventViewHandler implements IReceiveMessage
{
  private MessageCenter      message_center;
  private SlicedEventsPanel  events_panel; 

  public EventViewHandler( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.ADD_EVENTS_TO_VIEW );
    events_panel = new SlicedEventsPanel();
  }

  public Component getPanel()
  {
    return events_panel.getJoglPanel().getDisplayComponent();
  }

  public boolean receive( Message message )
  {
    if ( message.getName().equals(Commands.ADD_EVENTS_TO_VIEW) )
    {
      IEventList3D events = (IEventList3D)message.getValue();
      System.out.println("ASKED TO ADD EVENTS " + events.numEntries() );
      events_panel.addEvents( events );
      events_panel.updateDisplay();
    }
    return false;
  }

}
