
package EventTools.ShowEventsApp.ViewHandlers;

import java.util.Vector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.PeakDisplayInfo;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.PeaksDisplayPanel;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.PeakArrayPanels;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.PeakImagesCmd;
import EventTools.Histogram.Histogram3D;

/**
 *  This class handles the display of images of regions around peaks.
 */ 
public class PeakViewHandler implements IReceiveMessage
{
  private MessageCenter      message_center;
  private PeakImagesCmd      images_cmd = null;

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
      {
        images_cmd = (PeakImagesCmd)obj;
        ShowImages( images_cmd );        
      }      
    }
    return false;
  }


  private void ShowImages( PeakImagesCmd images_cmd )
  {
    System.out.println( images_cmd ); 

    Vector<Peak_new>    peaks      = images_cmd.getPeaks();
    Vector<Histogram3D> histograms = images_cmd.getRegions();

    PeakDisplayInfo[] peak_infos = new PeakDisplayInfo[ peaks.size() ];
    for ( int i = 0; i < peaks.size(); i++ )
    {
      Peak_new peak      = peaks.elementAt(i);
      float[][][] region = histograms.elementAt(i).getHistogramArray();
      PeakDisplayInfo peak_info = new PeakDisplayInfo( "Peak " + i,
                                                        region,
                                                        50, 50, 50, true );
      peak_infos[i] = peak_info;
    }

    PeaksDisplayPanel ppanel = new PeaksDisplayPanel( peak_infos );

    FinishJFrame frame = new FinishJFrame( "Peak Images" );
    frame.getContentPane().setLayout( new GridLayout(1,1) );
    frame.getContentPane().add( ppanel );
    frame.setSize(500,500);
    frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    frame.setVisible( true );
  }

}
