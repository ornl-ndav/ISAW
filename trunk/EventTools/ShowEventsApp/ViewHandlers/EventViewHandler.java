
package EventTools.ShowEventsApp.ViewHandlers;

import java.util.Vector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.ViewTools.Components.ViewControls.ColorScaleControl.*;
import gov.anl.ipns.Util.Sys.IhasWindowClosed;
import gov.anl.ipns.Util.Sys.IndirectWindowCloseListener;

import DataSetTools.operator.Generic.TOF_SCD.PeakQ;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.Viewers.SlicedEventsPanel;
import EventTools.EventList.IEventList3D;
import EventTools.ShowEventsApp.multiPanel;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.DrawingOptionsCmd;
import EventTools.ShowEventsApp.Command.SelectPointCmd;
import EventTools.ShowEventsApp.Command.LoadEventsCmd;
import EventTools.ShowEventsApp.Command.FindPeaksCmd;

import SSG_Tools.Viewers.JoglPanel;
import SSG_Tools.SSG_Nodes.SimpleShapes.*;


/**
 *  This class handles the messaging interface for the 3D event viewer
 *  panel.
 */ 
public class EventViewHandler implements IReceiveMessage, IhasWindowClosed
{
  private MessageCenter      message_center;
//                             view_message_center;
  private SlicedEventsPanel  events_panel; 
  private FinishJFrame       frame3D;
  private long               num_to_show;
  private long               num_shown;
  private Object             eventPanelMonitor = new Object();
  private Component          component;
  private Rectangle          PanelOrig;
  private float[][]          orientation_matrix = null;

  private float              MARK_SIZE = .2f;

  public EventViewHandler( MessageCenter message_center,
                           MessageCenter view_message_center )
  {
    this.message_center = message_center;
//    this.view_message_center = view_message_center;
    this.num_shown      = 0;
    this.num_to_show    = 1000000;
 
    message_center.addReceiver( this, Commands.LOAD_FILE_DATA );

    message_center.addReceiver( this, Commands.INIT_EVENTS_VIEW );
    message_center.addReceiver( this, Commands.SET_DRAWING_OPTIONS );
    message_center.addReceiver( this, Commands.SET_COLOR_SCALE );
    message_center.addReceiver( this, Commands.SET_ORIENTATION_MATRIX );
    message_center.addReceiver( this, Commands.SELECT_POINT );
    message_center.addReceiver( this, Commands.FIND_PEAKS );
    message_center.addReceiver( this, Commands.MARK_PEAKS );
    message_center.addReceiver( this, Commands.MARK_INDEXED_PEAKS );

    view_message_center.addReceiver( this, Commands.ADD_EVENTS_TO_VIEW );
    view_message_center.addReceiver( this , Commands.SHOW_DISPLAY_PANE );
    events_panel = new SlicedEventsPanel();
                                                // Is there a better way to do
                                                // this?  It would be nice to
                                                // keep the jogl_panel 
                                                // encapsulated

    JoglPanel jogl_panel = events_panel.getJoglPanel();
    jogl_panel.getDisplayComponent().addMouseListener(
                                    new MouseClickListener( jogl_panel ));
    frame3D = new FinishJFrame( "Reciprocal Space Events" );
    //------------------------
    Rectangle R1 = multiPanel.PANEL_BOUNDS;
    Rectangle R = new Rectangle( R1.x,R1.y, R1.width, R1.height);
    R.x +=R.width;
    R.width = R.height = 765;
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
    if ( message.getName().equals(Commands.ADD_EVENTS_TO_VIEW) )
    {
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

                                    // Check that the frame exists.  It won't
                                    // if the window has been closed!
      if ( frame3D != null )
        frame3D.setTitle( "Reciprocal Space Events for "+info.getEventFile() );

      num_to_show = info.getEventsToShow();
    }
    else if ( message.getName().equals( Commands.SELECT_POINT ) )
    {
       Object val = message.getValue();
       if ( val == null || !(val instanceof SelectPointCmd ) )
         return(false);

       synchronized ( eventPanelMonitor )
       {
         SelectPointCmd cmd = (SelectPointCmd)val;

         Vector3D position = cmd.getQ_vec();
         float point_size = 5 * MARK_SIZE; 

         events_panel.addSelectedPointMark( position, point_size,
                                            Polymarker.CROSS, Color.RED );
         events_panel.updateDisplay();
       }
    }
    else if ( message.getName().equals( Commands.FIND_PEAKS ) )
    {
                                        // just record new size choice if
                                        // requested.
      Object val = message.getValue();
      if ( val instanceof FindPeaksCmd )
      {
         FindPeaksCmd cmd = (FindPeaksCmd)val;
         if ( cmd.getMarkPeaks() )
           MARK_SIZE = cmd.getMarkSize();
      }
      return false;
    }
    else if ( message.getName().equals( Commands.MARK_PEAKS ) )
    {
       float mark_size = MARK_SIZE;
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
           events_panel.addMarkers( verts, mark_size, 
                                    Polymarker.BOX, Color.WHITE );
           events_panel.updateDisplay();
         }
         else if ( val instanceof Boolean )
         {
           boolean on_off = (Boolean)val;
           events_panel.SetMarkersOnOff( on_off );
           events_panel.SetIndexMarkersOnOff( on_off );
           events_panel.updateDisplay();
         }
       }
    }
    else if ( message.getName().equals( Commands.MARK_INDEXED_PEAKS ) )
    {
       float index_mark_size = 1.8f * MARK_SIZE;
       Object val = message.getValue();
       if ( val == null )
         return(false);

       synchronized ( eventPanelMonitor )
       {
         if ( val instanceof Vector )         // this should always be true
         {
           events_panel.ClearIndexMarkers();
           events_panel.updateDisplay();
           Vector<Peak_new> peaks = (Vector<Peak_new>)val;
           Vector indexed_peaks   = new Vector();
           for ( int i = 0; i < peaks.size(); i++ )
           {
             Peak_new peak = (Peak_new)peaks.elementAt(i);
             if ( Math.round( peak.h() ) != 0  ||
                  Math.round( peak.k() ) != 0  ||
                  Math.round( peak.l() ) != 0  )
               {
                 float[] q_arr = peaks.elementAt(i).getUnrotQ();
                 float qx = (float)(q_arr[0] * 2 * Math.PI);
                 float qy = (float)(q_arr[1] * 2 * Math.PI);
                 float qz = (float)(q_arr[2] * 2 * Math.PI);

                 float[] hkl = new float[3];
                 hkl[0] = Math.round( peak.h() );
                 hkl[1] = Math.round( peak.k() );
                 hkl[2] = Math.round( peak.l() );
                 float[] qxyz = LinearAlgebra.mult( orientation_matrix, hkl );

                 indexed_peaks.add( new Vector3D( qx, qy, qz ) );
                 indexed_peaks.add( new Vector3D( qxyz[0], qxyz[1], qxyz[2] ));
               }
           }
           if ( indexed_peaks.size() > 0 )
           {
             Vector3D[] verts = new Vector3D[ indexed_peaks.size() ];
             for ( int i = 0; i < verts.length; i++ )
               verts[i] = (Vector3D)indexed_peaks.elementAt(i);
             events_panel.addIndexMarkers( verts, index_mark_size,
                                         Polymarker.PLUS, Color.YELLOW );
             events_panel.updateDisplay();
           }
         }
       }
    }

    else if ( message.getName().equals(Commands.SET_ORIENTATION_MATRIX) ) 
    {
      Object val = message.getValue();
      if ( val == null || !( val instanceof Vector ) )
        return false;

      Vector vec = (Vector)val;
      if ( vec.size() < 1 || !( vec.elementAt(0) instanceof float[][] ) )
        return false; 

      float[][] UBT = (float[][]) vec.elementAt(0);
      orientation_matrix = LinearAlgebra.getTranspose( UBT );
      for ( int row = 0; row < 3; row++ )
        for ( int col = 0; col < 3; col++ )
           orientation_matrix[row][col] *= (float)(2*Math.PI);
    }

    else if ( message.getName().equals(Commands.SHOW_DISPLAY_PANE) )
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

    return false;
  }

  /**
   *  Listen for a mouse click on the jogl_panel, and send a SELECT_POINT
   *  message.  For now, the size of the mark is fixed.
   */
  public class  MouseClickListener extends MouseAdapter
  {
    JoglPanel my_panel;
    float     pmark_size = 2.5f * MARK_SIZE;

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

        Vector3D size = new Vector3D( pmark_size, pmark_size, pmark_size );
        SelectPointCmd value = new SelectPointCmd( point, size );
        Message message = new Message( Commands.SELECT_POINT,
                                       value, true, true );
        message_center.send( message );
      }
    }
  }


}
