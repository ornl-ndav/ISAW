package IsawGUI;
import javax.swing.*;
import java.net.*;
import java.awt.*;
import java.awt.print.*;
import java.io.*;

class MyInternalFrame extends JInternalFrame implements Printable
{

   public MyInternalFrame (JInternalFrame jif) throws IOException
   {
     super (jif.toString());
   }


  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
     int screenResolution = getToolkit().getScreenResolution();
     double pixelsPerPoint = (double)screenResolution/72d;
     
     int keepWidth = getSize().width;
     int keepHeight = getSize().height;
     int numPages = 0;
     int pageWidth = 0;
     int pageHeight = 0;
     if (pageIndex == 0) 
     {
        pageWidth = (int)(pageFormat.getImageableWidth()*pixelsPerPoint);
        pageHeight = (int)(pageFormat.getImageableHeight()*pixelsPerPoint);

        setSize(pageWidth, pageHeight);
        Graphics temp = graphics.create();
        printAll(temp);
        temp = null;
        

        int newHeight = getPreferredSize().height;
        if (newHeight%pageHeight ==0)
            numPages = newHeight / pageHeight;
        else
            numPages = newHeight / pageHeight + 1;

     }

     else if (pageIndex >= numPages)
     {
        return (Printable.NO_SUCH_PAGE);
     } 

    int newXOrigin = (int) (pageFormat.getImageableX()*pixelsPerPoint);
    int newYOrigin = (int) (pageFormat.getImageableY()*pixelsPerPoint);

    setSize(pageWidth, pageHeight);

    if(graphics instanceof Graphics2D)
    {
       Graphics2D g2D = (Graphics2D)graphics;
       g2D.scale(1/pixelsPerPoint, 1/pixelsPerPoint);
    } 
    graphics.translate(newXOrigin, newYOrigin - (pageIndex*pageHeight));
    graphics.setClip(0,(pageIndex*pageHeight), pageWidth,pageHeight);
    printAll(graphics);
    setSize(keepWidth, keepHeight);

    return(Printable.PAGE_EXISTS);

}
 

}
