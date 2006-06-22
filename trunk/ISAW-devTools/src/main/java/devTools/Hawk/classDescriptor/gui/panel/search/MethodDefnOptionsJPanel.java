/*
 * File:  MethodDefnOptionsJPanel.java
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
 * Revision 1.3  2004/03/12 19:46:18  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:15:51  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.panel.search;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Makes a panel which allows the user to enter search parameters that a method has to have.
 * @author Dominic Kramer
 */
public class MethodDefnOptionsJPanel extends ConstructorDefnOptionsJPanel
{
	/**
	 * Create a new MethodDefnOptionsJPanel.
	 */
	public MethodDefnOptionsJPanel()
	{
		super();
		remove(panelsPanel);
		panelsPanel = new JPanel();
		panelsPanel.setLayout(new GridLayout(6,0));
		panelsPanel.add(charPanel);
		panelsPanel.add(typePanel);
		panelsPanel.add(namePanel);
		panelsPanel.add(parametersPanel);
		panelsPanel.add(new JLabel());
		panelsPanel.add(new JLabel());
		add(panelsPanel, BorderLayout.CENTER);
	}
}
