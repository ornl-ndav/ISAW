/*
 * File:  InterfaceTableGUI.java
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
 * Revision 1.3  2004/03/12 19:46:15  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:08:51  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import devTools.Hawk.classDescriptor.modeledObjects.Interface;

/**
 * This class is still in the developmental stage.  It could possibly be a means 
 * of displaying information about a class or interface in a more compact way.
 * @author Dominic Kramer
 */
public class InterfaceTableGUI extends JFrame implements ActionListener
{
	/**
	 * The Interface whose data is being displayed.
	 */
	private Interface currentInterface;
	/**
	 * The JTable table's model to handle modifying the table.
	 */
	private DefaultTableModel model;
	/**
	 * The table which holds the Interface object's data.
	 */
	private JTable table;
	
	/**
	 * Create a new InterfaceTableGUI.
	 * @param INTF The interface whose data is to be used.
	 * @param title The title of the window.
	 * @param shortJava Set this to true if java names are to be shortened.  For example, 
	 * java.lang.String will be shortened to String.
	 * @param shortOther Set this to true if non-java names are to be shortened.
	 */
	public InterfaceTableGUI(Interface INTF, String title, boolean shortJava, boolean shortOther)
	{
		setTitle(title);
		
		//now to order the 
		
		model = new DefaultTableModel();
	}
	
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent e)
	{

	}

}
