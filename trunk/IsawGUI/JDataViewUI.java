/*
 * @(#)JDataViewGUI.java     1.0  99/09/02    Alok Chatterjee
 *                           1.1  2000/02/23  Dennis Mikkelson
 *
 * 1.0  99/09/02    Added the comments and made this a part of package IsawGUI
 * 1.1  2000/09/02  Changed to use ViewManagers to hold internal and external
 *                  viewers, instead of placing DataSetViewers in frames.
 *                  Also removed the JInternalFrame listener, since it was
 *                  no longer needed.  Finally, replaced drawImage() with
 *                  a more flexible ShowDataSet() method that will create and
 *                  initialize an internal or external ViewManager with
 *                  any of several types of viewers. 
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
import OverplotView.*;

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
  InternalViewManager i_view_manager = null;

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
    *  @see DataSetTools.viewer.InternalViewManager
    */
  public void ShowDataSet(DataSet ds, String frame, String view_type )
  {
    if (frame == "Internal Frame")
    {
      i_view_manager = new InternalViewManager( ds, view_type );
	i_view_manager.setResizable(true);
	i_view_manager.setIconifiable(true);
    	i_view_manager.setMaximizable(true);    
	i_view_manager.setClosable(true);

      add( i_view_manager );
      i_view_manager.toFront();
    }		
    else if (frame == "External Frame")
     view_manager = new ViewManager( ds, view_type );
  }

  public JFrame ShowSelectedGraphView(DataSet ds )
  {
    JFrame jf = new JFrame(ds.getTitle());
    OverplotView.GraphableDataManager sgv = new OverplotView.GraphableDataManager(ds);
    jf.getContentPane().add(sgv);
     sgv.setVisible(true);
    return jf;
    
    
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
