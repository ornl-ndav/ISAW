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

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import DataSetTools.util.*;

public class SplashWindowFrame extends    JFrame
                               implements Runnable
{
  SplashWindow sw;
  Image splashIm;
  private static final int width  = 475;
  private static final int height = 360;

  SplashWindowFrame() 
  {
    super();

    String ipath = SharedData.getProperty("ISAW_HOME");
    if ( ipath == null )
    {
      System.out.println("WARNING: ISAW_HOME not defined in IsawProps.dat");
      System.out.println("         Some help files, scripts and operators");
      System.out.println("         may not be available.");
      return;
    }
    ipath = StringUtil.fixSeparator(ipath);
    ipath = ipath.replace('\\','/');
    splashIm = Toolkit.getDefaultToolkit().getImage(ipath+"/images/Isaw.gif");
    splashIm=splashIm.getScaledInstance(width,height,Image.SCALE_FAST);

    MediaTracker mt = new MediaTracker(this);
    mt.addImage(splashIm,0);
    try 
    {
      mt.waitForID(0);
    } 
    catch(InterruptedException ie){}

    sw = new SplashWindow( this, splashIm );
  }


  /**
   *  This run method just sleeps for a while and then disposes of the
   *  frame, so that the SplashImage goes away automatically after some
   *  time has passed.
   */
  public void run()             
  {
    try
    {
     Thread.sleep(7000);
    }
    catch(InterruptedException ie){}
    this.dispose();
  }


  private class SplashWindow extends Window 
  {
    Image splashIm;

    public SplashWindow( JFrame parent, Image splashIm ) 
    {
      super(parent);
      this.splashIm = splashIm;
      this.setSize(width,height);

      /* Center the window */
      Dimension screenDim = 
      Toolkit.getDefaultToolkit().getScreenSize();
      Rectangle winDim = this.getBounds();
      this.setLocation((screenDim.width - winDim.width) / 2,
                  (screenDim.height - winDim.height) / 2);
  
      this.setVisible(true);
    }

    public void paint(Graphics g) 
    {
      if (splashIm != null) 
        g.drawImage(splashIm,0,0,this);
    }
  }
}
