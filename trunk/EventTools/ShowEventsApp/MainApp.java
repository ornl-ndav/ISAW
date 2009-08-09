
package EventTools.ShowEventsApp;

import MessageTools.*;


public class MainApp
{

  public static void main(String[] args)
  {
    MessageCenter message_center = new MessageCenter("Test");
    new UpdateManager(message_center, null, 100);

    multiPanel mp = new multiPanel( message_center );
  }

}
