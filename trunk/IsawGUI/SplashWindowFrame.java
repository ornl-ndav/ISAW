package IsawGUI;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import DataSetTools.util.*;


public class SplashWindowFrame extends JFrame {
    SplashWindow sw;
    Image splashIm;

    SplashWindowFrame() {
       super();

       /* Add the window listener */
       addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent evt) {
              dispose(); 
	      //System.exit(0);
	  }});

       /* Size the frame */
       setSize(475,360);

       /* Center the frame */
       Dimension screenDim = 
            Toolkit.getDefaultToolkit().getScreenSize();
       Rectangle frameDim = getBounds();
       setLocation((screenDim.width - frameDim.width) / 2,
		(screenDim.height - frameDim.height) / 2);

       MediaTracker mt = new MediaTracker(this);
        //System.out.println("Isaw_Home is = "+System.getProperty("ISAW_HOME"));
       String ipath = System.getProperty("ISAW_HOME");
       ipath = StringUtil.fixSeparator(ipath);
       ipath = ipath.replace('\\','/');


       splashIm = Toolkit.getDefaultToolkit().getImage(ipath+"/images/Isaw.gif");
       mt.addImage(splashIm,0);
       try {
          mt.waitForID(0);
       } catch(InterruptedException ie){}

       sw = new SplashWindow(this,splashIm);

       try {
	  Thread.sleep(5000);
      
       } catch(InterruptedException ie){}

       //sw.dispose();


       /* Show the frame */
       setVisible(true);
       }
}

class SplashWindow extends Window {
    Image splashIm;

    SplashWindow(Frame parent, Image splashIm) {
        super(parent);
        this.splashIm = splashIm;
        setSize(475,360);

        /* Center the window */
        Dimension screenDim = 
             Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle winDim = getBounds();
        setLocation((screenDim.width - winDim.width) / 2,
		(screenDim.height - winDim.height) / 2);
        setVisible(true);
    }

    public void paint(Graphics g) {
       if (splashIm != null) {
           g.drawImage(splashIm,0,0,this);
       }
    }
}
