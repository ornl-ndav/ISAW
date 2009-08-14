
package EventTools.ShowEventsApp;

import javax.swing.*;
import MessageTools.*;

import EventTools.ShowEventsApp.DataHandlers.*;

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

    EventLoader loader = new EventLoader( message_center );

    HistogramHandler hist_handler = 
                          new HistogramHandler( message_center, NUM_BINS );

    EventViewHandler view_handler = new EventViewHandler( message_center );
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
