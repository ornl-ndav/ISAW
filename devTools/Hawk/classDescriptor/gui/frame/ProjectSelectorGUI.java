/*
 * File:  ProjectSelector.java
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
 * Revision 1.1  2004/02/07 05:08:52  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import devTools.Hawk.classDescriptor.gui.panel.ProjectSelectorJPanel;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ProjectSelectorGUI extends JFrame implements ActionListener
{
	private ProjectSelectorJPanel panel;
	
	public ProjectSelectorGUI(Vector provec)
	{
		addWindowListener(new WindowDestroyer());
		panel = new ProjectSelectorJPanel(provec, null);
		JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
			JPanel innerPanel = new JPanel();
			innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			innerPanel.add(panel);
			
		mainPanel.add(innerPanel, BorderLayout.CENTER);
		setJMenuBar(panel.getProjectJMenuBar());
		getContentPane().add(mainPanel);
		pack();
	}

	public void actionPerformed(ActionEvent e)
	{
	}
	
	class WindowDestroyer extends WindowAdapter
	{
		public void windowClosing(WindowEvent event)
		{
			dispose();
			System.exit(0);
		}
	}
}
