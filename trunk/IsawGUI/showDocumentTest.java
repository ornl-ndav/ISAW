
package IsawGUI;
import java.applet.*;
import java.net.*;
import java.awt.*;

 public class showDocumentTest extends Applet {

   URL jamsa = null;

   public void init()
     {
       try {
           jamsa = new URL("http://www.jamsa.com");
         } 
       catch (MalformedURLException e)
         {
           System.out.println("Error:" + e.getMessage());
         }
     }

   public boolean mouseDown(Event evt, int x, int y)
     {
       // when user clicks, go to Jamsa Press Home page
       getAppletContext().showDocument(jamsa, "_blank");
       return(true);
     }
  }
