/*
 * File:  AttachableDetachableFrame.java
 *
 * Copyright (C) 2004 Dominic Kramer
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Dominic Kramer <kramerd@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA and by
 * the National Science Foundation under grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 */
package devTools.Hawk.classDescriptor.gui.internalFrame;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ToolTipManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import devTools.Hawk.classDescriptor.gui.ExternallyControlledFrame;
import devTools.Hawk.classDescriptor.gui.MouseNotifiable;
import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;

/**
 * @author Dominic Kramer
 */
public abstract class AttachableDetachableFrame extends JInternalFrame implements ActionListener, MenuListener, ExternallyControlledFrame, MouseNotifiable
{
	/** The HawkDesktop that the frame is attached to. */
	protected final HawkDesktop parentHawkDesktop;	
	/** The original JDesktopPane the frame was attached to. */
	protected final JDesktopPane originalDesktopPane;
	/** The menu that contains options for attaching/detaching the frame. */
	protected JMenu attachDetachMenu;
	  /** The menu that contains the options as where to attach the frame.  
	   *   The attachDetachMenu contains this menu.
	   */
	  protected JMenu attachMenu;
     /** The menu item allows the user to select to attach the frame to the 
      * original JDesktopPane that it was from when it was in its HawkDesktop.  
	  *   The attachDetachMenu contains this menu.
	  */
      protected JMenuItem attachToOriginal;
	/** The menu item allows the user to select to attach the frame to the 
	 * current JDesktopPane in the HawkDesktop specified by the field "parentHawkDesktop".  
	 *   The attachDetachMenu contains this menu.
	 */
      protected JMenuItem attachToCurrent;
	  /**
	   * The menu item that the user can select to detach the frame.  
	   * The attachDetachMenu contains this menu item.
	   */
	  protected JMenuItem detachMenuItem;
	  /** Set when the frame is acting like a JFrame. */
	  protected boolean isJFrame;
	  protected JFrame frame;
	  protected JDesktopPane pane;
	  
	/**
	 * Create a new AttachableDetachableFrame.  Also the JMenu "attachDetachMenu" is created.  
	 * Any class subclassing this class can therefore use the attachDetach menu in their 
	 * constructors after calling this constructor.
	 */
	public AttachableDetachableFrame(HawkDesktop parentDesktop, JDesktopPane parentPane)
	{
		parentHawkDesktop = parentDesktop;
		originalDesktopPane = parentPane;
		
		isJFrame = false;
		
		frame = new JFrame();
		pane = new JDesktopPane();
		
		attachDetachMenu = new JMenu("Reposition");
		attachDetachMenu.addMenuListener(this);
		  attachMenu = new JMenu("Attach to");
			attachToOriginal = new JMenuItem("Original tab");
			ToolTipManager.sharedInstance().registerComponent(attachToOriginal);
			attachToOriginal.addActionListener(this);
			attachToOriginal.setActionCommand("attach.original");
			attachMenu.add(attachToOriginal);
		    
			attachToCurrent = new JMenuItem("Current tab");
			attachToCurrent.addActionListener(this);
			attachToCurrent.setActionCommand("attach.current");
			attachMenu.add(attachToCurrent);
		attachDetachMenu.add(attachMenu);

		detachMenuItem = new JMenuItem("Detach");
		  detachMenuItem.addActionListener(this);
		  detachMenuItem.setActionCommand("detach");
		attachDetachMenu.add(detachMenuItem);
	}
	
	/**
	 * Gets a copy of this window.
	 * @return A copy of this window.
	 */
	public abstract AttachableDetachableFrame getCopy();
	
	/**
	 * If bol is true the method moves the selected internal window in front of the other windows 
	 * on the JDesktop.  If bol is false it moves the window behind the other windows on the JDesktopPane.
	 * @param bol True to move the window to the front and false to move the window to the back.
	 */
	public void setAsSelected(boolean bol)
	{
		if (bol)
			moveToFront();
		else
			moveToBack();
	}
	
	public void actionPerformed(ActionEvent event)
	{
		String eventName = event.getActionCommand();
		if (eventName.startsWith("attach."))
		{
			isJFrame = false;
			
			if (eventName.endsWith(".original"))
			{
				originalDesktopPane.add(this);
			}
			else if (eventName.endsWith(".current"))
			{				
				if (parentHawkDesktop.getSelectedDesktop()==null)
				{
					int num = parentHawkDesktop.getTabbedPane().getTabCount() + 1;
					parentHawkDesktop.getTabbedPane().add(new JDesktopPane());
					parentHawkDesktop.getTabbedPane().setTitleAt(num-1,"Tab"+num);
				}
				
				parentHawkDesktop.getSelectedDesktop().add(this);
			}
			
			frame.dispose();
		}
		else if (eventName.equals("detach"))
		{
			isJFrame = true;
			frame = new JFrame();
			frame.setTitle(this.getTitle());
			pane = new JDesktopPane();
			frame.setContentPane(pane);
			AttachableDetachableFrame copy = this;//this.getCopy();
//			copy.setMaximizable(false);
//			copy.setIconifiable(false);
//			copy.setClosable(false);
//			copy.setResizable(false);
			pane.add(copy);
//			pane.getDesktopManager().maximizeFrame(copy);
			copy.setAsSelected(true);
			copy.setVisible(true);
			System.out.println("width="+frame.getWidth()+", height="+frame.getHeight());
			frame.setSize(copy.getSize());
			frame.setVisible(true);
			
			parentHawkDesktop.getSelectedDesktop().repaint();
		}
	}
	
	/** Inherited from the MenuListener interface. */
	public void menuCanceled(MenuEvent e) {}
	
	/** Inherited from the MenuListener interface. */
	public void menuDeselected(MenuEvent e) {}
	
	/** Inherited from the MenuListener interface.  Determines, 
	 *  what menus and menu items can/cannot be selected from the 
	 * attachDetachMenu.  It then uses setEnabled() method 
	 * to modify the items to allow the user to select them or not select
	 * them.
	 */
	public void menuSelected(MenuEvent event)
	{
		//first to allow the user to select any menu or menu item.
		attachMenu.setEnabled(true);
		attachToCurrent.setEnabled(true);
		attachToOriginal.setEnabled(true);
		attachToOriginal.setToolTipText(null);
		detachMenuItem.setEnabled(true);
		
		//now to be more specific and not allow the user to select some 
		//of the items.		
		if (isJFrame)
			detachMenuItem.setEnabled(false);
		else
			attachMenu.setEnabled(false);
		
		int i=0;
		boolean found = false;

		if (originalDesktopPane != null)
		{
			int count = parentHawkDesktop.getTabbedPane().getTabCount();
			while (!found && i<count)
			{
				found = parentHawkDesktop.getTabbedPane().getComponentAt(i).equals(originalDesktopPane);
				i++;
			}
		}
		
		if (!found)
		{
			attachToOriginal.setEnabled(false);
			attachToOriginal.setToolTipText("This window's original tab does not exist anymore.");
		}
	}
	
	//----this method is inherited from MouseNotifiable
	/**
	 * The Components in the array returned from this method are the Components that should have the 
	 * mouse use the waiting animation when an operation is in progress.  Inherited from interface 
	 * MouseNotifiable.
	 * @return  This method returns "this" as the first element in a one element Component array.
	 */
	public Component[] determineWaitingComponents()
	{
		Component[] compArr = new Component[1];
		compArr[0] = this;
		return compArr;
	}
	
	//----these methods are inheritd from AttachableDetachableFrame
	/* Handles disposing the frame. */
	    //public void dispose() is already defined by the superclass.
	/* Handles setting the frame's title. */
	    //public void setTitle(String title) is already defined by the superclass.
	/* Handles getting the frame's size. */
	    //public Dimension getSize() is already defined by the superclass.
	/** Get the Component this object is controlling or null 
	 * if it isn't controlling a Component.
	 */
	public Component getControlledComponent()
	{
		return this;
	}
}
