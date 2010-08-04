
package EventTools.ShowEventsApp.ViewHandlers;

import java.util.Vector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.PeakDisplayInfo;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.PeaksDisplayPanel;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.PeakArrayPanels;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.PeakImagesCmd;
import EventTools.ShowEventsApp.Command.SelectPointCmd;
import EventTools.Histogram.Histogram3D;

/**
 *  This class handles the display of images of regions around peaks.
 */ 
public class PeakViewHandler implements IReceiveMessage,
                                        ActionListener
{
  private MessageCenter message_center;
  private Peak_new[]    peak_array = null;
  private Histogram3D[] histogram_array = null;


  public PeakViewHandler( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.SHOW_PEAK_IMAGES );
  }


  public boolean receive( Message message )
  {
    if ( message.getName().equals(Commands.SHOW_PEAK_IMAGES) )
    {
      System.out.println( "Got SHOW_PEAK_IMAGES command ");
      Object obj = message.getValue();
      if ( obj instanceof PeakImagesCmd )
        ShowImages( (PeakImagesCmd)obj );        
    }
    return false;
  }


  private void ShowImages( PeakImagesCmd images_cmd )
  {
    System.out.println( images_cmd ); 

    Vector<Peak_new>    peaks      = images_cmd.getPeaks();
    Vector<Histogram3D> histograms = images_cmd.getRegions();

    if ( peaks.size() != histograms.size() )
      return;

    peak_array     = new Peak_new[ peaks.size() ];
    histogram_array = new Histogram3D[ histograms.size() ];
    for ( int i = 0; i < histograms.size(); i++ )
    {
      peak_array[i] = peaks.elementAt(i);
      histogram_array[i] = histograms.elementAt(i);
    }

    PeakDisplayInfo[] peak_infos = new PeakDisplayInfo[ peak_array.length ];
    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak      = peak_array[i];
      float[][][] region = histogram_array[i].getHistogramArray();

      int seq_num = peak.seqnum();
      int det_num = peak.detnum();
      int row     = (int)peak.y();
      int col     = (int)peak.x();
      String title = "" + seq_num + ":  " + det_num + 
                     "  ( " + col + ", " + row + " )";
      PeakDisplayInfo peak_info = new PeakDisplayInfo(  title,
                                                        region,
                                                        0, 0, 0, true );
      peak_infos[i] = peak_info;
    }

    PeaksDisplayPanel ppanel = new PeaksDisplayPanel( peak_infos );
    ppanel.addActionListener( this );

    FinishJFrame frame = new FinishJFrame( "Peak Images" );
    frame.getContentPane().setLayout( new GridLayout(1,1) );
    frame.getContentPane().add( ppanel );
    frame.setSize(500,500);
    frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    frame.setVisible( true );
  }


  public void actionPerformed( ActionEvent event )
  {
    PeaksDisplayPanel ppanel = (PeaksDisplayPanel)event.getSource();
    int x_index = ppanel.getPointedAtCol();
    int y_index = ppanel.getPointedAtRow();
    int z_index = ppanel.getPointedAtPage();
    int p_index = ppanel.getPointedAtPeakIndex();
/*
    System.out.println("   IN PeakViewHandler... got " +
                              " PAGE = " + z_index +
                              " ROW  = " + y_index +
                              " COL  = " + x_index +
                              " PEAK# = " + p_index );
*/   
    if ( histogram_array != null &&
         p_index >= 0            && 
         p_index < histogram_array.length )
    {
      Histogram3D histogram = histogram_array[p_index];
      Vector3D bin_center = histogram.binLocation( x_index, y_index, z_index );

      Vector3D size = new Vector3D( 1, 1, 1 );
      SelectPointCmd value = new SelectPointCmd( bin_center, size );
      Message message = new Message( Commands.SELECT_POINT,
                                      value, true, true );
      message_center.send( message );
    }

  }

}
