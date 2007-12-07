/*
 * File: SplashWindowFrame.java
 *
 * Copyright (C) 2001, Alok Chatterjee, Dennis Mikkelson
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.23  2007/12/07 00:08:37  amoe
 *  Added a feature that allows the Splash to turned on, then off.
 *    -Added showSplash()
 *    -Added disposeSplash()
 *    -Added showTimedSplash()
 *
 *  Revision 1.22  2005/06/08 21:19:48  dennis
 *  Removed one unused local variable.
 *  Uncommented the media tracker code, so the image appears as soon as
 *  the window does.
 *  Made the containing frame a Frame rather than a JFrame, since no
 *  other swing components were used here.
 *
 *  Revision 1.21  2005/05/25 20:24:53  dennis
 *  Now calls convenience method WindowShower.show() to show
 *  the window, instead of instantiating a WindowShower object
 *  and adding it to the event queue.
 *
 *  Revision 1.20  2004/03/15 03:31:26  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.19  2004/01/24 23:09:39  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.18  2003/12/12 18:15:18  dennis
 *  Temporarily commented out code that waited for an image to finish
 *  loading, using a MediaTracker.  This permitted the splash pane to
 *  appear with the image "simultaneously".  This code was commented
 *  out in a continuing attempt to fix intermittent problems with ISAW
 *  starting.
 *
 *  Revision 1.17  2003/12/11 18:24:52  dennis
 *    Now uses the WindowShower utility class to display the splash window
 *  from the Swing event handling thread, instead of showing it directly.
 *  This fixes an intermittent problem where Isaw would "hang" while
 *  loading on Linux, using j2sdk 1.4.2_02.
 *    Added code to start the thread that disposes of the splash screen
 *  after a period of time, so that the thread does not need to be started
 *  in the ISAW main program.
 *    Did some general code clean up and improved javadocs.
 *
 *  Revision 1.16  2003/02/13 21:45:13  pfpeterson
 *  Removed calls to deprecated function fixSeparator.
 *
 *  Revision 1.15  2003/02/13 17:28:33  pfpeterson
 *  Moved decision making for image directory to PropertiesLoader.
 *
 *  Revision 1.14  2003/02/12 21:20:02  pfpeterson
 *  Now checks properties for IMAGE_DIR before going to ISAW_HOME/images
 *  for the splashscreen image.
 *
 *  Revision 1.13  2002/12/11 17:25:20  pfpeterson
 *  Now scales image used in splash screen to the size of the splash screen.
 *
 *  Revision 1.12  2002/11/27 23:27:07  pfpeterson
 *  standardized header
 *
 *  Revision 1.11  2002/06/14 15:57:20  pfpeterson
 *  Use the ShareData.getProperty() method to get ISAW_HOME.
 *
 */

package IsawGUI;

import gov.anl.ipns.Util.Sys.*;

import java.awt.*;
import DataSetTools.util.*;

/**
 *   This class will display the ISAW splash screen on the center of the
 *   monitor for a period of time.
 */
public class SplashWindowFrame extends    Frame
                               implements Runnable
{
  private static final int    WIN_WIDTH  = 475;  // size of splash screen
  private static final int    WIN_HEIGHT = 360;
  private static final float  TIME       = 8;    // time to display the splash
                                                 // screen, in seconds
  
  private SplashWindow splash;
  
  /** 
   *  Construct the parent frame for the splash window
   */
  public SplashWindowFrame() 
  {
    super();
    splash = new SplashWindow( this );  // make a new splash window with 
                                        // this Frame as its parent.
  }

  /**
   * Show the splash screen.
   */
  public void showSplash()
  {
    // actually show the window in
    // the event thread, to avoid
    // deadlock or race conditions
    WindowShower.show( splash );
  }
  
  /**
   * Dispose of the splash screen.
   */
  public void disposeSplash()
  {
    this.dispose();
  }
  
  /**
   * Show the splash screen, wait several seconds, and then dispose it.
   */
  public void showTimedSplash()
  {
    showSplash();
    
    // start timer that kills the
    // the window after some time
    Thread splash_thread = new Thread( (Runnable)this );
    splash_thread.start();
  }
  
  /**
   *  This run method just allows the thread to sleep for a while.
   */
  public void run()             
  {
    try
    {
     Thread.sleep( (int)(1000 * TIME) );
    }
    catch(InterruptedException ie){}    
    disposeSplash();
  }

  /**
   *  This internal Window class actually gets the splash image and 
   *  displays it in itself.  
   */
  private class SplashWindow extends Window 
  {
    private Image splashIm;

    public SplashWindow( Frame parent ) 
    {
      super(parent);      
                                                          // find and load the
                                                          // image
      String ipath = SharedData.getProperty("IMAGE_DIR");
      if ( ipath == null ) 
         return;
      ipath    = StringUtil.setFileSeparator(ipath);
      splashIm = Toolkit.getDefaultToolkit().getImage( ipath + "Isaw.gif" );
      splashIm = splashIm.getScaledInstance( WIN_WIDTH, 
                                             WIN_HEIGHT, 
                                             Image.SCALE_FAST );

                                                  // use a media tracker to wait
      MediaTracker mt = new MediaTracker( this ); // for the image to load, so
      mt.addImage(splashIm,0);                    // when it appears, it pops 
      try                                         // pops up instantly. 
      {
        mt.waitForID(0);
      }
      catch(InterruptedException ie){}

                                                  // center the splash screen
                                                  // on the monitor
      this.setSize( WIN_WIDTH, WIN_HEIGHT );
      Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
      this.setLocation( ( screenDim.width  - WIN_WIDTH  ) / 2,
                        ( screenDim.height - WIN_HEIGHT ) / 2 );
    }
       
    public void paint(Graphics g) 
    {
      if (splashIm != null) 
        g.drawImage(splashIm,0,0,this);
    }
  }
}
