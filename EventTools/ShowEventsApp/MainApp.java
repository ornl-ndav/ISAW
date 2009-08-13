
package EventTools.ShowEventsApp;

import javax.swing.*;
import MessageTools.*;

import EventTools.ShowEventsApp.DataHandlers.*;
import EventTools.Histogram.*;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

public class MainApp
{
  public  static final int NUM_BINS = 512;

  private Histogram3D histogram;

  public Histogram3D DefaultHistogram( int num_bins )
  {
    // Just make default histogram aligned with coord axes.
   
    long start_time = System.nanoTime();
    Vector3D xVec = new Vector3D(1,0,0);
    Vector3D yVec = new Vector3D(0,1,0);
    Vector3D zVec = new Vector3D(0,0,1);
 
    IEventBinner x_bin1D = new UniformEventBinner( -16.0f,  0,    num_bins );
    IEventBinner y_bin1D = new UniformEventBinner( -16.0f,  0,    num_bins );
    IEventBinner z_bin1D = new UniformEventBinner( - 8.0f, 8.0f, num_bins );

    ProjectionBinner3D x_binner = new ProjectionBinner3D(x_bin1D, xVec);
    ProjectionBinner3D y_binner = new ProjectionBinner3D(y_bin1D, yVec);
    ProjectionBinner3D z_binner = new ProjectionBinner3D(z_bin1D, zVec);

    Histogram3D histogram = new Histogram3D( x_binner,
                                             y_binner,
                                             z_binner );
    long run_time = System.nanoTime() - start_time;
    System.out.println("Time(ms) to allocate default histogram = " + 
                        run_time/1.e6);

    return histogram;
  }
 

  public MainApp()
  {
    MessageCenter message_center = new MessageCenter("Test");
    message_center.setDebugReceive( true );
    message_center.setDebugSend( true );

    new UpdateManager(message_center, null, 100);

    multiPanel mp = new multiPanel( message_center );

    EventLoader loader = new EventLoader( message_center );

    histogram = DefaultHistogram( NUM_BINS );

    HistogramHandler hist_handler = 
                          new HistogramHandler( message_center, histogram );

    EventViewHandler view_handler = new EventViewHandler( message_center );

    JFrame frame = new JFrame( "Reciprocal Space Events" );
    frame.setSize(750,750);
    frame.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
    frame.getContentPane().add( view_handler.getPanel() );
    frame.setVisible( true );
  }


  public static class Builder implements Runnable
  {
    public void run()
    {
      new MainApp();
    }  
  }

  
  public static void main(String[] args)
  {
//    MainApp app = new MainApp();
     SwingUtilities.invokeLater( new Builder() );
  }

}
