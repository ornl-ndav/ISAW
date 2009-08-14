
package EventTools.ShowEventsApp.ViewHandlers;

import java.util.Vector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.ColorScaleControl.*;

import DataSetTools.operator.Generic.TOF_SCD.PeakQ;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.Viewers.SlicedEventsPanel;
import EventTools.Histogram.Histogram3D;
import EventTools.EventList.IEventList3D;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.DrawingOptionsCmd;
import EventTools.ShowEventsApp.Command.SelectPointCmd;
import EventTools.ShowEventsApp.Command.LoadEventsCmd;

import SSG_Tools.Viewers.JoglPanel;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;


/**
 *  This class handles the messaging interface for the 3D event viewer
 *  panel.
 */ 
public class EventViewHandler implements IReceiveMessage
{
  private MessageCenter      message_center;
  private SlicedEventsPanel  events_panel; 
  private JFrame             frame3D;

  public EventViewHandler( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.ADD_EVENTS_TO_VIEW );
    message_center.addReceiver( this, Commands.CLEAR_EVENTS_VIEW );
    message_center.addReceiver( this, Commands.SET_FILTER_OPTIONS );
    message_center.addReceiver( this, Commands.SET_COLOR_SCALE );
    message_center.addReceiver( this, Commands.LOAD_FILE );
    message_center.addReceiver( this, Commands.MARK_PEAKS );
    events_panel = new SlicedEventsPanel();
                                                // Is there a better way to do
                                                // this?  It would be nice to
                                                // keep the jogl_panel 
                                                // encapsulated

    JoglPanel jogl_panel = events_panel.getJoglPanel();
    jogl_panel.getDisplayComponent().addMouseListener(
                                    new MouseClickListener( jogl_panel ));
    frame3D = new JFrame( "Reciprocal Space Events" );
    frame3D.setSize(750,750);
    frame3D.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
    Component component = events_panel.getJoglPanel().getDisplayComponent();
    frame3D.getContentPane().add( component );
    frame3D.setVisible( true );
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
    else if ( message.getName().equals(Commands.SET_FILTER_OPTIONS) )
    {
       DrawingOptionsCmd filter_options = (DrawingOptionsCmd)message.getValue();
      events_panel.setDrawingOptions( filter_options.getFilterMax(),
                                      filter_options.getFilterMin(),
                                      filter_options.getPointSize(),
                                      filter_options.getAlpha(),
                                      filter_options.getAlphaValue(),
                                      filter_options.getOrthographic() );
      events_panel.updateDisplay();

    }
    else if ( message.getName().equals(Commands.SET_COLOR_SCALE ) )
    {
      ColorScaleInfo color_info = (ColorScaleInfo)message.getValue();
      events_panel.setColors( color_info );
      events_panel.updateDisplay();
    }
    else if ( message.getName().equals(Commands.CLEAR_EVENTS_VIEW ) )
    {
      events_panel.clear();
      events_panel.updateDisplay();
    }
    else if( message.getName().equals( Commands.LOAD_FILE ) )
    {
      LoadEventsCmd info = (LoadEventsCmd)message.getValue();
      frame3D.setTitle( "Reciprocal Space Events for "+info.getEventFile() );
    }
    else if ( message.getName().equals( Commands.MARK_PEAKS ) )
    {
       Object val = message.getValue();
       if ( val != null && val instanceof Vector )
       {
         events_panel.ClearMarkers();
         Vector<PeakQ> q_peaks = (Vector<PeakQ>)val;
         Vector3D[] verts = new Vector3D[ q_peaks.size() ];
         for ( int i = 0; i < verts.length; i++ )
         {
           float[] q_arr = q_peaks.elementAt(i).getUnrotQ();
           float qx = (float)(q_arr[0] * 2 * Math.PI);
           float qy = (float)(q_arr[1] * 2 * Math.PI);
           float qz = (float)(q_arr[2] * 2 * Math.PI);
           verts[i] = new Vector3D( qx, qy, qz );
         }
         events_panel.addMarkers( verts, 6, Polymarker.BOX, Color.WHITE );
         events_panel.updateDisplay();
       }
    }

    return false;
  }

  /**
   *  Listen for a mouse click on the jogl_panel, and send a SELECT_POINT
   *  message.  For now, the size of the "box", dx, dy, dz, are fixed.
   */
  public class  MouseClickListener extends MouseAdapter
  {
    JoglPanel my_panel;

    public MouseClickListener( JoglPanel panel )
    {
      my_panel = panel;
    }

    public void mouseClicked (MouseEvent e)
    {
      if ( e.getClickCount() == 1 )
      {
        int x = e.getX();
        int y = e.getY();

        Vector3D point = my_panel.pickedPoint( x, y );
        System.out.println("3D point = " + point );

        Vector3D size = new Vector3D( 1, 1, 1 );
        SelectPointCmd value = new SelectPointCmd( point, size );
        Message message = new Message( Commands.SELECT_POINT, value, true );
        message_center.receive( message );
      }
    }
  }


}
