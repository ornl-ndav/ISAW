/*
 * File: JDataViewUI.java
 *
 * Copyright (C) 1999, Alok Chatterjee
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
 * $Log$
 * Revision 1.13  2003/10/30 20:40:30  dennis
 * Removed @see tag referring to InternalViewManager.
 * (InternalViewManager is no longer used and has been
 *  removed from the source tree.)
 *
 * Revision 1.12  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 */
 
package IsawGUI;

import java.awt.*;
import java.awt.print.*;
import java.awt.event.*;
import java.awt.Frame.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.awt.Color.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.util.zip.*;

/**
 * The display area for the images and graphs for ISAW. It defines different methods 
 * to control the layout of the different image and graph windows.
 * 
 *
 * @version 1.1  
 */

public class JDataViewUI extends JDesktopPane implements Serializable
{
  private int xoffset = 0, yoffset = 0;
  private int w = 500, h = 450;
  Toolkit toolkit;
  JInternalFrame sel_frame;
  ViewManager         view_manager   = null;

  public static final String INTERNAL_FRAME = "Create view in Internal Frame";
  public static final String EXTERNAL_FRAME = "Create view in External Frame";

  /**
    * Create a new JDataViewUI and sets the border.
    *   
    */
  public JDataViewUI()
  {
    toolkit = getToolkit();
    MatteBorder mb = 	new MatteBorder(4,4,4,4, Color.lightGray);
    setBorder(new CompoundBorder( mb, new EtchedBorder (EtchedBorder.LOWERED,Color.gray, 
                                                                Color.lightGray)));
  }
    
  /**
    *  Create a ViewManager to hold the specified DataSet.  The ViewManager
    *  can be in an internal or external frame.  It can also be initialized
    *  with any of the available types of viewers.
    * 
    *  @param   ds        Reference to the DataSet to be viewed
    *  @param   frame     String specifying whether to use an internal or
    *                     external frame for the ViewManager
    *  @param   view_type Specifies the initial viewer type for to be used
    *                     by the ViewManager.  Currently, this can be one
    *                     of IViewManager.IMAGE  or
    *                        IViewManager.SCROLLED_GRAPHS
    *
    *  @see DataSetTools.viewer.IViewManager
    *  @see DataSetTools.viewer.ViewManager
    */
  public void ShowDataSet(DataSet ds, String frame, String view_type )
  {
    if( frame == INTERNAL_FRAME )
    {
      System.out.println("ERROR: Internal Frames no longer supported");
      System.out.println("   ... using External JFrame" );
    }
    view_manager = new ViewManager( ds, view_type );
  }


  public void closeAll() 
  {
    JInternalFrame[] frames = getAllFrames();
    for(int i=0; i < frames.length; ++i) 
    {
      if(!frames[i].isIcon()) 
      {
        try {
		frames[i].setIcon(true);
		}
	  catch(java.beans.PropertyVetoException ex) {
	        System.out.println("iconification vetoed!");
	       }
	}
    }
  }
	
    
	
    public void MaxAll() {
	JInternalFrame[] frames = getAllFrames();

	for(int i=0; i < frames.length; ++i) {
          if(!frames[i].isIcon()) {
            try 
            {
              frames[i].setMaximum(true);	
            }
            catch(java.beans.PropertyVetoException ex) 
            {
              System.out.println("Maximization vetoed!");
            }
          }
        }
   }
	
	public void openAll() {
		JInternalFrame[] frames = getAllFrames();

		for(int i=0; i < frames.length; ++i) {
			if(frames[i].isIcon()) {
				try {
					frames[i].setIcon(false);
				}
				catch(java.beans.PropertyVetoException ex) {
					System.out.println("restoration vetoed!");
				}
			}
		}
	}
	public void cascade() {
		JInternalFrame[] frames = getAllFrames();
		int x =0, y = 0;
		for(int i=0; i < frames.length; ++i) {
			if( ! frames[i].isIcon()) 
			{
				frames[i].setBounds(x,y,w,h);
				frames[i].toFront();
				x += 30;
				y += 30;
			}
		}
	}
	
	public void tile_Vertically() {
		JInternalFrame[] frames = getAllFrames();
		int x =0, y = 0 ;
		for(int i=0; i < frames.length; ++i) {
			if( ! frames[i].isIcon()) 
			{
				frames[i].setBounds(x,y,w,h);
				frames[i].toFront();
				x += xoffset;
				y += 115;
			}
		}
	}
		public void closeViews() 
		{
            JInternalFrame[] frames = getAllFrames();
		    for(int i=0; i < frames.length; ++i) 
		    frames[i].dispose();
		    		System.gc();
		            System.runFinalization();
	    }


	   public JInternalFrame getSelectedFrame() 
		{
            JInternalFrame[] frames = getAllFrames();
		    for(int i=0; i < frames.length; ++i) 
		    {
                      if( frames[i].isSelected() )
                         return frames[i];
		    }
		    return null;
	    }


}   
