/*
 * File:  LinkedFrame.java
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JMenu;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;

/**
 * Special type of JInternalFrame that has a JMenu associated with it, which allows the user to select to view 
 * an Interface's UML diagram, shortened source code, source code, and javadoc file.  Any subclass of this class 
 * simply has to add "viewMenu" to the frame to allow the user to select what they want to view.  The viewMenu is 
 * made by this class.  Also, this class does all of the work to display the correct information.
 * @author Dominic Kramer
 */
public abstract class LinkedFrame extends AttachableDetachableFrame implements ActionListener
{
	/** The menu that contains options to view the various forms of data about a class or 
	 * interface (such as a UML diagram, shortened source code, source code, or javadocs).
	 */
	protected JMenu viewMenu;
	/**
	 * The Interface whose data is to be displayed.
	 */
	protected Interface selectedInterface;
	
	/**
	 * Creates a new LinkedFrame object.  Also, a view menu is created that allows the user to choose to view 
	 * a class or interface's source code, javadoc, UML diagram, or shortened source code.  Thus any class 
	 * subclassing this class already has a view menu created for it after calling this constructor.
	 * @param parentDesktop The HawkDesktop from which this frame originated.
	 * @param parentPane The JDesktopPane in the HawkDesktop from which this frame originated.
	 * @param intf The Interface whose data is to be displayed.
	 * @param showUMLOption True if the view menu should contain an option to view the class or interface's UML diagram.
	 * @param showShortenedSourceOption True if the view menu should contain an option to view the class or interface's shortened source code.
	 * @param showJavadocsOption True if the view menu should contain an option to view the class or interface's javadocs.
	 * @param showSourceOption True if the view menu should contain an option to view the class or interface's source code.
	 */
	public LinkedFrame(HawkDesktop parentDesktop, JDesktopPane parentPane, Interface intf, boolean showUMLOption, boolean showShortenedSourceOption, boolean showJavadocsOption, boolean showSourceOption)
	{
		super(parentDesktop, parentPane);
		selectedInterface = intf;
		viewMenu = InternalFrameUtilities.constructViewMenu(this, showUMLOption, showShortenedSourceOption, showJavadocsOption, showSourceOption);
	}
	
	/**
	 * Handles the action events to open up the correct windows.
	 */
	public void actionPerformed(ActionEvent event)
	{
		InternalFrameUtilities.processActionEventFromViewMenu(event,selectedInterface,parentHawkDesktop,this);
		super.actionPerformed(event);
	}
}
