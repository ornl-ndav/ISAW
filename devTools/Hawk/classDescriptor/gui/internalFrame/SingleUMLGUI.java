/*
 * File:  SingleUMLGUI.java
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
 * Modified:
 *
 * $Log$
 * Revision 1.4  2004/05/26 19:56:43  kramer
 * Made the gui use a SingleUMLJPanel to show the actual information.
 *
 * Revision 1.3  2004/03/12 19:46:16  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:09:16  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.internalFrame;

import java.awt.Component;

import javax.swing.JDesktopPane;
import javax.swing.JMenuBar;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.gui.panel.SingleUMLJPanel;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;

/**
 * This is a special type of JInternalFrame that displays a UML diagram of an Interface object 
 * in an ASCII format.
 * @author Dominic Kramer
 */
public class SingleUMLGUI extends DesktopInternalFrame
{
	protected SingleUMLJPanel panel;
	
	/**
	 * Create a new SingleUMLGUI.
	 * @param INTF The Interface object whose data is written.
	 * @param title The title of the window.
	 * @param shortJava True if you want a name to be shortened if it is a java name.  For 
	 * example, java.lang.String would be shortened to String.
	 * @param shortOther True if you want a name to be shortened if it is a non-java name.
	 * @param desk The HawkDesktop that this window is on.
	 */
	public SingleUMLGUI(Interface INTF, String title, boolean shortJava, boolean shortOther, HawkDesktop desk)
	{
		super(desk,desk.getSelectedDesktop(),INTF,false,true,true,true);
					
		//now to set some of the characteristics of the window
			setTitle(title);
			setLocation(0,0);
			setSize(175,400);
			setClosable(true);
			setIconifiable(true);
			setMaximizable(true);
			setResizable(true);
		
		panel = new SingleUMLJPanel(INTF,shortJava,shortOther,this);
		JMenuBar menuBar = panel.createJMenuBar();
		menuBar.add(viewMenu);
		menuBar.add(windowMenu);
		setJMenuBar(menuBar);
		getContentPane().add(panel);
		resizeAndRelocate();
	}
	
	public void resizeAndRelocate()
	{
		pack();
		int count = desktop.getTabbedPane().getTabCount();
		if (count > 0)
		{
			int maxWidth = ((JDesktopPane)desktop.getTabbedPane().getComponentAt(0)).getWidth();
			int maxHeight = ((JDesktopPane)desktop.getTabbedPane().getComponentAt(0)).getHeight();
			
			int height = getHeight();
			int width = getWidth();

			int newX = getX();
			int newY = getY();
			int newWidth = getWidth();
			int newHeight = getHeight();
			
			if ((maxHeight-height)<=0)
			{
				newHeight = (int)(0.8*maxHeight);
				newY = (int)(0.1*maxHeight);
			}
			else
				newY = (maxHeight-height)/2;
			
			if ((maxWidth-width)<=0)
			{
				newWidth = (int)(0.8*maxWidth);
				newX = (int)(0.1*maxWidth);
			}
			else
				newX = (maxWidth-width)/2;
						
			setLocation(newX,newY);
			setSize(newWidth,newHeight);
		}
	}
	
	/**
	 * Gets a copy of this window.
	 * @return A copy of this window.
	 */
	public AttachableDetachableFrame getCopy()
	{
		return new SingleUMLGUI(selectedInterface,getTitle(),panel.areJavaNamesShortened(),panel.areNonJavaNamesShortened(),desktop);
	}
	
	public Component[] determineWaitingComponents()
	{
		return panel.determineWaitingComponents();
	}
}
