/*
 * File:  WindowMenuListener.java
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
 * Revision 1.2  2004/03/11 18:19:38  bouzekc
 * Documented file using javadoc statements.
 *
 * Revision 1.1  2004/02/07 05:09:16  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.internalFrame;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * This class listens for menu events and modifies the JMenu of a DesktopInternalFrame 
 * object as needed.
 * @author Dominic Kramer
 */
public class WindowMenuListener implements MenuListener
{
	/**
	 * The DesktopInternalFrame that this class will be listening to.
	 */
	protected DesktopInternalFrame frame;
	/**
	 * The JMenuBar which is more specifically going to be listened to.
	 */
	protected JMenuBar menuBar;
	/**
	 * The JMenu that will even more specifically be listened to.
	 */
	protected JMenu windowMenu;
	
	/**
	 * General constructor.
	 * @param Frame The DesktopInternalFrame to listen to.
	 * @param bar The JMenuBar to listen to.
	 * @param menu The JMenu to listen to.
	 */
	public WindowMenuListener(DesktopInternalFrame Frame, JMenuBar bar, JMenu menu)
	{
		frame = Frame;
		menuBar = bar;
		windowMenu = menu;
	}
	
	/**
	 * Responds to a menu being cancelled.
	 * @param e The MenuEvent that is caught.
	 */
	public void menuCanceled(MenuEvent e)
	{
	}
	
	/**
	 * Responds to a menu being deselected.
	 * @param e The MenuEvent that is caught.
	 */
	public void menuDeselected(MenuEvent e)
	{
	}
	
	/**
	 * Responds to a menu being selected.
	 * @param e The MenuEvent that is caught.
	 */
	public void menuSelected(MenuEvent e)
	{
/*
		int numOfTabs = frame.getHawkDesktop().getTabbedPane().getTabCount();		
		if (numOfTabs == 1)
			frame.setPreviousMenuItemEnabled(false);
		else
			frame.setPreviousMenuItemEnabled(true);
*/
		int num = frame.getHawkDesktop().getTabbedPane().getSelectedIndex();
		if (num > -1)
		{
			if (num == 0)
				frame.setPreviousMenuItemEnabled(false);
			else
				frame.setPreviousMenuItemEnabled(true);
		}
				
//		frame.refreshMoveAndCopyMenu();
	}
}
