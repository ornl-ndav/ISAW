/* 
 * File: SliceSelectorPanel.java
 *
 * Copyright (C) 2011, Dennis Mikkelson
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
 *  $Author: $
 *  $Date: $            
 *  $Revision: $
 */

package EventTools.ShowEventsApp.Controls.SliceControls;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import gov.anl.ipns.ViewTools.UI.Vector3D_UI;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import MessageTools.*;

import EventTools.ShowEventsApp.Controls.IntegratePeaksPanel;
import EventTools.ShowEventsApp.Command.*;

public class SliceSelectorPanel extends JPanel
                                implements IReceiveMessage
{
  private MessageCenter    message_center;

  private final String[]   event_choices = { "RAW Events", "WEIGHTED Events" };
  private JComboBox        event_selector;

  private final String[]   coordinate_choices = { "HKL ( UB MUST be set )", 
                                                  "Lab Qxyz ( |Q| = 1/d )" };
  private JComboBox        coordinate_selector;

  private Vector3D_UI      origin_UI;
  private DirectionControl direction_1_UI;
  private DirectionControl direction_2_UI;
  private DirectionControl direction_3_UI;

  private JLabel           histogram_size;
  private JLabel           histogram_status;

  public static final String SHAPE_AS_SPECIFIED   = "As Specified";
  public static final String SHAPE_COLS_PERP_ROWS = "Force Cols Perp. Rows";
  public static final String SHAPE_ALL_PERP       = "Force All to be Perp.";

  private final String[] shape_choices = { SHAPE_AS_SPECIFIED, 
                                           SHAPE_COLS_PERP_ROWS,
                                           SHAPE_ALL_PERP };
  private JComboBox        shape_selector;

  public final String NO_HISTOGRAM    = IntegratePeaksPanel.NO_HISTOGRAM;
  public final String HISTOGRAM_READY = IntegratePeaksPanel.HISTOGRAM_READY;
  public final String EVENTS_ADDED    = IntegratePeaksPanel.EVENTS_ADDED;


  public SliceSelectorPanel( MessageCenter message_center )
  {
     this.message_center = message_center;
     message_center.addReceiver(this,Commands.SLICES_HISTOGRAM_READY);
     message_center.addReceiver(this,Commands.ADDED_EVENTS_TO_SLICES_HISTOGRAM);
     message_center.addReceiver(this, Commands.SLICES_HISTOGRAM_FREED);

     BoxLayout box_layout = new BoxLayout( this, BoxLayout.Y_AXIS );
     setLayout( box_layout );
     setBorder( new TitledBorder("Slice Selection Options") );

     JPanel event_panel = new JPanel();
     event_panel.setLayout( new GridLayout( 1, 2 ) );
     event_panel.add( new JLabel("Histogram Using:") );
     event_selector = new JComboBox( event_choices );
     event_panel.add( event_selector );

     JPanel coord_panel = new JPanel();
     coord_panel.setLayout( new GridLayout( 1, 2 ) );
     coord_panel.add( new JLabel("Specify Region in:") );
     coordinate_selector = new JComboBox( coordinate_choices );
     coord_panel.add( coordinate_selector );
     
     JPanel origin_panel = new JPanel();
     origin_panel.setLayout( new GridLayout( 1, 2 ) );
     origin_panel.add( new JLabel("Origin (center point)") );
     origin_UI = new Vector3D_UI("", new Vector3D(1,1,1) );
     origin_UI.setHorizontalAlignment( JTextField.RIGHT );
     origin_panel.add( origin_UI );

     Vector3D dir_1  = new Vector3D( 0, 0, 1 );
     Vector3D dir_2  = new Vector3D( 0, 1, 0 );
     Vector3D dir_3  = new Vector3D( 1, 0, 0 );
     direction_1_UI  = new DirectionControl( "Direction 1 (slice #)", 
                                              dir_1, "0.04", "25" );
     direction_2_UI  = new DirectionControl( "Direction 2 (row #)", 
                                              dir_2, "0.04", "251" );
     direction_3_UI  = new DirectionControl( "Direction 3 (col #)",
                                              dir_3, "0.04", "251" );

     JPanel shape_panel = new JPanel();
     shape_panel.setLayout( new GridLayout( 1, 2 ) );
     shape_panel.add( new JLabel("Region Shape:") );
     shape_selector = new JComboBox( shape_choices );
     shape_panel.add( shape_selector );

     JPanel size_panel = new JPanel();
     size_panel.setLayout( new GridLayout( 1, 2 ) );
     JButton size_button = new JButton("Histogram Size (MB)");
     size_panel.add( size_button );
     size_button.addActionListener( new SizeListener() );
     histogram_size = new JLabel( "NOT KNOWN " );
     histogram_size.setHorizontalAlignment( JLabel.RIGHT );
     size_panel.add( histogram_size );

     JPanel status_panel = new JPanel();
     status_panel.setLayout( new GridLayout( 1, 2 ) );
     status_panel.add( new JLabel("Histogram Status") );
     histogram_status = new JLabel( NO_HISTOGRAM );
     histogram_status.setHorizontalAlignment( JLabel.RIGHT );
     status_panel.add( histogram_status );

     JPanel init_panel = new JPanel();
     init_panel.setLayout( new GridLayout( 1, 2 ) );
     JButton init_histogram_btn = new JButton( "Initialize Histogram" );
     init_histogram_btn.addActionListener( new InitListener() );
     JButton free_histogram_btn = new JButton( "Free Histogram" );
     free_histogram_btn.addActionListener( new FreeListener() );
     init_panel.add( init_histogram_btn );
     init_panel.add( free_histogram_btn );

     JPanel show_panel = new JPanel();
     show_panel.setLayout( new GridLayout(1,1) );
     JButton show_btn = new JButton( "Show Slice Images" );
     show_btn.addActionListener( new ShowSlicesListener() );
     show_panel.add( show_btn );

     add( event_panel );
     add( coord_panel );

     add( origin_panel );
     add( direction_1_UI );
     add( direction_2_UI );
     add( direction_3_UI );
    
     add( shape_panel );

     add( size_panel );
     add( status_panel );
     add( init_panel );

     add ( show_panel );

     UpdateSize();
  }


  public boolean receive( Message message )
  {
    if ( message.getName().equals(Commands.SLICES_HISTOGRAM_READY) )
      histogram_status.setText( HISTOGRAM_READY );

    else if (
        message.getName().equals( Commands.ADDED_EVENTS_TO_SLICES_HISTOGRAM) )
      histogram_status.setText( EVENTS_ADDED );

    else if ( message.getName().equals(Commands.SLICES_HISTOGRAM_FREED) )
      histogram_status.setText( NO_HISTOGRAM );

    return false;
  }


  private boolean UpdateSize()
  {
    double size = 4.0 * (double)direction_1_UI.getNumSteps() *
                        (double)direction_2_UI.getNumSteps() *
                        (double)direction_3_UI.getNumSteps();
    if ( size == 0.0 )
    {
      histogram_size.setText("ILLEGAL SIZE");
      return false;
    }

    size /= 1e6;         // give the size in Megabytes
    histogram_size.setText( String.format("%10.1f" , size) ); 
    return true;
  }


  private class SizeListener implements ActionListener
  {
    public void actionPerformed( ActionEvent ev )
    {
      UpdateSize();
    }
  }

  private class ShowSlicesListener implements ActionListener
  {
    public void actionPerformed( ActionEvent ev )
    {
      boolean collapse_messages   = true;
      boolean use_separate_thread = true;

      Message mess = new Message( Commands.SHOW_SLICES_HISTOGRAM,
                                  null,
                                  collapse_messages,
                                  use_separate_thread );
      message_center.send(mess);
    }
  }


  private class FreeListener implements ActionListener
  {
    public void actionPerformed( ActionEvent ev )
    {
      boolean collapse_messages   = true;
      boolean use_separate_thread = true;

      Message mess = new Message( Commands.FREE_SLICES_HISTOGRAM,
                                  null,
                                  collapse_messages,
                                  use_separate_thread );
      message_center.send(mess);
    }
  }


  private class InitListener implements ActionListener
  {
    public void actionPerformed( ActionEvent ev )
    {
      if ( !UpdateSize() )
      {
        Util.sendError("ERROR: Illegal Number of Bins specified for SLICE");
        return;
      }

      boolean use_weights = true;
      if ( event_selector.getSelectedIndex() == 0 )
        use_weights = false;

      boolean use_HKL = true;
      if (coordinate_selector.getSelectedIndex() == 1 )
        use_HKL = false;

      Vector3D center = origin_UI.getVector();
      double   step_1 = direction_1_UI.getStepSize();
      double   step_2 = direction_2_UI.getStepSize();
      double   step_3 = direction_3_UI.getStepSize();

      if ( !use_HKL )                       // scale up by 2PI to get "real" Q
      {
        center.multiply( (float)(2*Math.PI) );
        step_1 *= 2*Math.PI;
        step_2 *= 2*Math.PI;
        step_3 *= 2*Math.PI;
      }
 
      HistogramEdge edge_1 = null;
      HistogramEdge edge_2 = null;
      HistogramEdge edge_3 = null;
      try
      {
        edge_1 = new HistogramEdge( direction_1_UI.getDirection(),
                                    step_1,
                                    direction_1_UI.getNumSteps() );
        edge_2 = new HistogramEdge( direction_2_UI.getDirection(),
                                    step_2,
                                    direction_2_UI.getNumSteps() );
        edge_3 = new HistogramEdge( direction_3_UI.getDirection(),
                                    step_3,
                                    direction_3_UI.getNumSteps() );
      }
      catch ( Exception ex )
      {
        Util.sendError( "Invalid direction or binning specified for SLICE\n" +
                         ex.getMessage() ); 
        return;
      }

      String shape = shape_selector.getSelectedItem().toString();

      InitSlicesCmd cmd = new InitSlicesCmd( use_weights,
                                             use_HKL,
                                             center,
                                             edge_1,
                                             edge_2,
                                             edge_3,
                                             shape );
      boolean collapse_messages   = true;
      boolean use_separate_thread = true;

      Message mess = new Message( Commands.INIT_SLICES_HISTOGRAM,
                                  cmd,
                                  collapse_messages,
                                  use_separate_thread );
      message_center.send(mess);
    }
  }


  public static void main( String args[] )
  {
    JFrame test_frame = new JFrame("SliceSelectorPanel");
    test_frame.setBounds( 0, 0, 390, 485 );
    test_frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

    MessageCenter test_mc = new MessageCenter("Test Message Center");
    SliceSelectorPanel selector = new SliceSelectorPanel( test_mc );

    test_frame.add( selector );
    test_frame.setVisible( true );
  }


}
