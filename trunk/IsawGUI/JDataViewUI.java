/*
 * @(#)JDataViewGUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawTools
 * 
 */
 
package IsawGUI;

import java.awt.*;
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
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JDataViewUI extends JDesktopPane implements Serializable
{
    private int xoffset = 0, yoffset = 0;
    private int w = 500, h = 450;
    Toolkit toolkit;
    JInternalFrame jif, sel_frame;
    JFrame jef;
    public JDataViewUI()
    {
        toolkit = getToolkit();
       
    }
    
    public void drawImage(DataSet ds, String frame)
	{
        
	     ImageView iv = new ImageView(ds);
	    if (frame == "Internal Frame")
	    {
            JInternalFrame jif = new JInternalFrame(ds.toString());            
            jif.setBounds(0,0,w,h);
	        jif.setResizable(true);
	        jif.setIconifiable(true);
    	    jif.setMaximizable(true);    
	        jif.setClosable(true);
            jif.setVisible(true);
            jif.getContentPane().add(iv);
           // jif.addInternalFrameListener(new Listener());
            add(jif);  
			jif.toFront();
		
		}	
		
		else if (frame == "External Frame")
		{
		    jef = new JFrame(ds.toString());
		    jef.setBounds(0,0,w,h);
		    jef.setResizable(true);
		    jef.setVisible(true);
		    
		    jef.addWindowListener(new WindowAdapter()
		    {
		       public void windowClosing(WindowEvent ev)
		        {
		            jef.dispose();
		            System.gc();
		            System.runFinalization();
		        }
		    });
		    
		    jef.getContentPane().add(iv);
		    jef.validate();  
		}
     }
     
     public void closeAll() {
		JInternalFrame[] frames = getAllFrames();

		for(int i=0; i < frames.length; ++i) {
			if(!frames[i].isIcon()) {
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
	    
	    
	       public void printImage() 
		{
            JInternalFrame[] frames = getAllFrames();
            
             for(int i=0; i < frames.length; ++i) 
		    {
		        
		        
                      if (frames[i].isSelected())
                         sel_frame =  frames[i];
		    }
            Properties properties =new Properties();
            JFrame frame  = new JFrame();
                 frame.setContentPane(sel_frame) ;  
            //frame.setContentPane(getSelectedFrame());
            PrintJob pj = toolkit.getPrintJob(frame,"Print Image", properties);

				    try 
				    {
				        //getSelectedFrame().printAll(pj.getGraphics());
					    sel_frame.printAll(pj.getGraphics());
					    pj.end();
				    }
				    catch(Exception ex) 
				    {
					    System.out.println("closeViews vetoed!" +ex);
				    }
		   // }
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
	    
	public class Listener implements InternalFrameListener 
	 
	{
	    public Listener(){ }
	    
	    public void internalFrameClosing(InternalFrameEvent e)
	    {
	       System.out.println("Now closing frame ......"); 
	       try
	       {
	            jif.dispose();
	       }
	       catch(Exception ee){ System.out.println("Now closing frame ......" +ee);}
	    }
	     public void internalFrameClosed(InternalFrameEvent e)
	    {}
	    
	    public void internalFrameActivated(InternalFrameEvent e)
	    {}
	    
	    public void internalFrameDeactivated(InternalFrameEvent e)
	    {}
	    
	    public void internalFrameDeiconified(InternalFrameEvent e)
	    {}
	    
	    public void internalFrameIconified(InternalFrameEvent e)
	    {}
	    
	    public void internalFrameOpened(InternalFrameEvent e)
	    {}
	}
	    
	    
}   
