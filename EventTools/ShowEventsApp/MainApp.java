
package EventTools.ShowEventsApp;

import javax.swing.*;
import MessageTools.*;

import EventTools.ShowEventsApp.DataHandlers.*;
import EventTools.ShowEventsApp.ViewHandlers.*;

public class MainApp 
{
  public  static final int NUM_BINS = 512;


  public MainApp()
  {
    MessageCenter message_center = new MessageCenter("Test");
    message_center.setDebugReceive( true );
    message_center.setDebugSend( true );

    new UpdateManager(message_center, null, 100);

    multiPanel mp = new multiPanel( message_center );

    new EventLoader( message_center );

    new HistogramHandler( message_center, NUM_BINS );

    new EventViewHandler( message_center );

    new PeakListHandler( message_center );
    
    new OrientationMatrixHandler( message_center );

    new StatusMessageHandler( message_center, null);
    
    new DQDataHandler( message_center );
    
    new DViewHandler( message_center );
    new QViewHandler( message_center );
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
//     MainApp app = new MainApp();
     SwingUtilities.invokeLater( new Builder() );
  }

}
