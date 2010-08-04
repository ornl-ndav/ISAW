
package EventTools.ShowEventsApp.ViewHandlers;

import java.util.Vector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.ViewTools.Components.ViewControls.ColorScaleControl.*;
import gov.anl.ipns.Util.Sys.IhasWindowClosed;
import gov.anl.ipns.Util.Sys.IndirectWindowCloseListener;

import DataSetTools.operator.Generic.TOF_SCD.PeakQ;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.Viewers.SlicedEventsPanel;
import EventTools.EventList.IEventList3D;
import EventTools.EventList.FloatArrayEventList3D;
import EventTools.Histogram.Histogram3D;
import EventTools.Histogram.ProjectionBinner3D;
import EventTools.Histogram.UniformEventBinner;
import EventTools.Histogram.IEventBinner;
import EventTools.ShowEventsApp.multiPanel;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.DrawingOptionsCmd;
import EventTools.ShowEventsApp.Command.SelectPointCmd;
import EventTools.ShowEventsApp.Command.LoadEventsCmd;

import SSG_Tools.Viewers.JoglPanel;
import SSG_Tools.Cameras.Camera;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;


/**
 *  This class handles the messaging interface for the 3D event viewer
 *  panel.
 */ 
public class LocalEventViewHandler implements IReceiveMessage, IhasWindowClosed
{
  private MessageCenter      message_center;
  private SlicedEventsPanel  events_panel; 
  private FinishJFrame       frame3D;
  private long               num_to_show;
  private long               num_shown;
  private Object             eventPanelMonitor = new Object();

  private boolean            just_local_view;
  private Vector3D           my_center_point = null;
  private float              my_size = .5f;
  private int                my_num_bins = 64;
  private Histogram3D        histogram = null;
  private Vector             event_buffer = new Vector();
  private boolean            points_added = false;

  Component component;
  Rectangle PanelOrig;

  public LocalEventViewHandler( MessageCenter message_center,
                                MessageCenter view_message_center )
  {
    this.message_center = message_center;
    this.num_shown      = 0;
    this.num_to_show    = 1000000;
 
    message_center.addReceiver( this, Commands.LOAD_FILE_DATA );

    message_center.addReceiver( this, Commands.SELECT_POINT );

    message_center.addReceiver( this, Commands.INIT_EVENTS_VIEW );
    message_center.addReceiver( this, Commands.SET_DRAWING_OPTIONS );
    message_center.addReceiver( this, Commands.SET_COLOR_SCALE );
    message_center.addReceiver( this, Commands.MARK_PEAKS );
    view_message_center.addReceiver( this, Commands.ADD_EVENTS_TO_VIEW );
    view_message_center.addReceiver( this, Commands.UPDATE );

    message_center.addReceiver( this, Commands.ADD_EVENTS_TO_HISTOGRAMS );
    message_center.addReceiver( this, Commands.INIT_HISTOGRAM );

    view_message_center.addReceiver( this, Commands.SHOW_DISPLAY_PANE );

    Set_Histogram( my_num_bins, new Vector3D( -10, -5, 3 ), 4 );

    events_panel = new SlicedEventsPanel();
                                                // Is there a better way to do
                                                // this?  It would be nice to
                                                // keep the jogl_panel 
                                                // encapsulated

    JoglPanel jogl_panel = events_panel.getJoglPanel();
//    jogl_panel.getDisplayComponent().addMouseListener(
//                                    new MouseClickListener( jogl_panel ));
    frame3D = new FinishJFrame( "Local View" );
    //------------------------
    Rectangle R1 = multiPanel.PANEL_BOUNDS;
    Rectangle R = new Rectangle( R1.x,R1.y, R1.width, R1.height);
    R.x +=R.width;
    R.width = R.height = 600;
    this.PanelOrig = new Rectangle( R1.x,R1.y, R1.width, R1.height);
   
    //-----------------------------
    frame3D.setBounds(R);
    
    frame3D.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    frame3D.addWindowListener( new IndirectWindowCloseListener(this,"3D") );
    component = events_panel.getJoglPanel().getDisplayComponent();
    frame3D.getContentPane().add( component );
    frame3D.setVisible( true );
  }


  @Override
  public void WindowClose(String ID)
  {
    frame3D = null;
  }


  public boolean receive( Message message )
  {
    if ( message.getName().equals(Commands.INIT_HISTOGRAM) )
    {
      System.out.println("GOT INIT_HISTOGRAM ***** " );
      System.out.println( "my_center_point = " + my_center_point );
      if ( my_center_point != null )  
        just_local_view = true; 
      else
        return false;

      Set_Histogram( my_num_bins, my_center_point, my_size );
      event_buffer.clear();
      points_added = false;

      System.out.println("Set Histogram center to " + my_center_point );
    }

    else if ( message.getName().equals(Commands.ADD_EVENTS_TO_HISTOGRAMS) )
    {
      if ( ! just_local_view )
        return false;

      FloatArrayEventList3D events = (FloatArrayEventList3D)message.getValue();

      FloatArrayEventList3D sublist = 
                      events.getLocalEvents( my_center_point.get(), my_size );

      if ( sublist == null )
      {
        System.out.println("NO NEARBY POINTS");
        return false;
      }

      System.out.println("******* Sublist size: " + sublist.numEntries() );

      synchronized( histogram )  
      {
        histogram.addEvents( sublist, true );
      }

      String max_message = String.format(
                        "Max Histogram Value : %4.2f,  Total Events: %d",
                         histogram.maxVal(), histogram.numAdded()  );
      System.out.println( max_message );

      synchronized( event_buffer )
      {
        event_buffer.add( sublist );
      }
    }

    else if ( message.getName().equals( Commands.UPDATE ) )
    {
       System.out.println("Got UPDATE command, points_added = " + points_added);
       System.out.println("just_local_view = " + just_local_view );
       if ( points_added )
         return false;

       if ( event_buffer.size() > 0 )
       {
         for ( int i = 0; i < event_buffer.size(); i++ )
         {
           IEventList3D events = (IEventList3D)event_buffer.elementAt(i);
           SetWeightsFromHistogram( events, histogram );
           events_panel.addEvents( events );
         }

         System.out.println("Done ADDING events to Display");
         points_added = true;
         events_panel.updateDisplay();
       }       
    }


    else if ( message.getName().equals(Commands.ADD_EVENTS_TO_VIEW) )
    {
      if ( just_local_view )  
        return false;   

      IEventList3D events = (IEventList3D)message.getValue();

      long num_new = events.numEntries();

      if ( num_new + num_shown > num_to_show )      // limit the number shown
        return false;                               // TODO: use a queue.
      else
        num_shown += num_new;

      synchronized ( eventPanelMonitor )
      {
        events_panel.addEvents( events );
        events_panel.updateDisplay();
      }
    }
    else if ( message.getName().equals(Commands.SET_DRAWING_OPTIONS) )
    {
      DrawingOptionsCmd draw_options = (DrawingOptionsCmd)message.getValue();
      synchronized ( eventPanelMonitor )
      {
        events_panel.setDrawingOptions( draw_options.getFilterMax(),
                                        draw_options.getFilterMin(),
                                        draw_options.getShowAxes(),
                                        draw_options.getPointSize(),
                                        draw_options.getAlpha(),
                                        draw_options.getAlphaValue(),
                                        draw_options.getOrthographic() );
        events_panel.updateDisplay();
      }
    }
    else if ( message.getName().equals(Commands.SET_COLOR_SCALE ) )
    {
      ColorScaleInfo color_info = (ColorScaleInfo)message.getValue();
      synchronized ( eventPanelMonitor )
      {
        events_panel.setColors( color_info );
        events_panel.updateDisplay();
      }
    }
    else if ( message.getName().equals(Commands.INIT_EVENTS_VIEW ) )
    {
      synchronized ( eventPanelMonitor )
      {
        num_shown = 0;
        events_panel.clear();
        events_panel.updateDisplay();
      }
    }
    else if( message.getName().equals( Commands.LOAD_FILE_DATA ) )
    {
      LoadEventsCmd info = (LoadEventsCmd)message.getValue();

      frame3D.setTitle( "Local View: "+info.getEventFile() );

      num_to_show = info.getEventsToShow();
    }
    else if ( message.getName().equals( Commands.MARK_PEAKS ) )
    {
       int MARK_SIZE = 10;
       Object val = message.getValue();
       if ( val == null )
         return(false);

       synchronized ( eventPanelMonitor )
       {
         if ( val instanceof Vector )
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
           events_panel.addMarkers( verts, MARK_SIZE, 
                                    Polymarker.BOX, Color.WHITE );
           events_panel.updateDisplay();
         }
         else if ( val instanceof Boolean )
         {
           boolean on_off = (Boolean)val;
           events_panel.SetMarkersOnOff( on_off );
           events_panel.updateDisplay();
         }
       }
    }
    else if( message.getName().equals(Commands.SHOW_DISPLAY_PANE) )
    {
       if( frame3D != null)
          return false;
       frame3D = new FinishJFrame( "Reciprocal Space Events" );
       //------------------------
       Rectangle R = new Rectangle(PanelOrig.x,PanelOrig.y,PanelOrig.width,
                      PanelOrig.height);
       R.x +=R.width;
       R.width = R.height = 765;
       
       //-----------------------------
       frame3D.setBounds(R);
       frame3D.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
       frame3D.addWindowListener( new IndirectWindowCloseListener(this,"3D") );
       component = events_panel.getJoglPanel().getDisplayComponent();
       frame3D.getContentPane().add( component );
       frame3D.setVisible( true );
    }
    else if ( message.getName().equals( Commands.SELECT_POINT ) )
    {
      System.out.println("Selected Point = " + message.getValue() );

      SelectPointCmd cmd = (SelectPointCmd)message.getValue();

      JoglPanel jp = events_panel.getJoglPanel();
      Camera camera = jp.getCamera();
      Vector3D vrp = camera.getVRP();
      Vector3D cop = camera.getCOP();
      Vector3D vuv = camera.getVUV();
      Vector3D new_vrp = new Vector3D(cmd.getQx(), cmd.getQy(), cmd.getQz()); 
      Vector3D diff_vec = new Vector3D( new_vrp );
      diff_vec.subtract( vrp );
      cop.add( diff_vec );
      camera.setVRP( new_vrp );
      camera.setCOP( cop ); 
      jp.setCamera( camera );
      jp.Draw();

      my_center_point = new_vrp;
      System.out.println( "Set my_center_point to " + my_center_point );
    }

    return false;
  }

  /**
   *  Listen for a mouse click on the jogl_panel, and send a SELECT_POINT
   *  message.  For now, the size of the "box", dx, dy, dz, are fixed.
   */
/*
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
       // System.out.println("3D point = " + point );

        Vector3D size = new Vector3D( 1, 1, 1 );
        SelectPointCmd value = new SelectPointCmd( point, size );
        Message message = new Message( Commands.SELECT_POINT,
                                       value, true, true );
        message_center.send( message );
      }
    }
  }
*/

  synchronized private void Set_Histogram( int      num_bins,
                                           Vector3D position,
                                           float    size )
  {
                                                 // make three orthonormal
                                                 // vectors for local histogram
    Vector3D axial_vec = new Vector3D(position);
    axial_vec.normalize();

    Vector3D tangential_vec = new Vector3D();
    Vector3D beam_vec = new Vector3D(-1,0,0);
    tangential_vec.cross( beam_vec, axial_vec );
    tangential_vec.normalize();
    
    Vector3D radial_vec = new Vector3D();
    radial_vec.cross( tangential_vec, axial_vec );
    radial_vec.normalize();

// KLUGE TO GET SOMETHING WORKING!!!
/*
    radial_vec     = new Vector3D( 1, 0, 0 );
    tangential_vec = new Vector3D( 0, 1, 0 );
    axial_vec      = new Vector3D( 0, 0, 1 );
*/
    float axial_component      = axial_vec.dot( position );
    float radial_component     = radial_vec.dot( position );
    float tangential_component = tangential_vec.dot( position );

    System.out.println("axial vector      = " + axial_vec );
    System.out.println("radial vector     = " + radial_vec );
    System.out.println("tangential vector = " + tangential_vec );

    System.out.println("axial component      = " + axial_component );
    System.out.println("radial component     = " + radial_component );
    System.out.println("tangential component = " + tangential_component );

    IEventBinner x_bin1D;
    IEventBinner y_bin1D;
    IEventBinner z_bin1D;

    x_bin1D = new UniformEventBinner( radial_component - size/2, 
                                      radial_component + size/2, num_bins );

    y_bin1D = new UniformEventBinner( tangential_component - size/2, 
                                      tangential_component + size/2, num_bins );

    z_bin1D = new UniformEventBinner( axial_component - size/2, 
                                      axial_component + size/2, num_bins );

    ProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, radial_vec);
    ProjectionBinner3D y_binner = 
                              new ProjectionBinner3D(y_bin1D, tangential_vec);
    ProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, axial_vec);

    if ( histogram == null )
      histogram = new Histogram3D( x_binner, y_binner, z_binner );
    else
    {
      histogram.setHistogramPosition( x_binner, y_binner, z_binner );
      histogram.clear();
    }
  }


  /**
   *  Set the weight value of the specified events to the value of
   *  the histogram bin that contains the event.  The histogram bin
   *  values are used to control the color map for the 3D event viewer.
   */
  private void SetWeightsFromHistogram( IEventList3D events,
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
