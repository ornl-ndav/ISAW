/*
 * File:  VerticalInterfacePanelGUI.java
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
 * Revision 1.2  2004/03/11 19:06:40  bouzekc
 * Documented file using javadoc statements.
 *
 * Revision 1.1  2004/02/07 05:08:52  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import devTools.Hawk.classDescriptor.modeledObjects.Interface;

/**
 * This class is a window displaying information about the supplied Interface by using JPanels 
 * (containing specific information about the Interface) placed vertically across the window.  
 * Currently, this class is incomplete and will probably be removed.
 * @author Dominic Kramer
 * @deprecated
 */
public class VerticalInterfacePanelGUI extends JFrame implements ActionListener
{
	protected Interface selectedInterface;
	
	public VerticalInterfacePanelGUI(Interface INTF, String title)
	{
		//now to define selectedInterface
			selectedInterface = INTF;
			
		//now to define the characteristics of the main window
			setTitle(title);
			setSize(200,800);
			addWindowListener(new WindowDestroyer());
			
		//now to get the main parts of the JFrame
			Container pane = getContentPane();
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			
		//now to make the button for buttonPanel
			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(this);
			buttonPanel.add(closeButton);
						
		mainPanel.add(INTF.getVerticalInterfaceJPanel() ,BorderLayout.CENTER);
		mainPanel.add(buttonPanel,BorderLayout.SOUTH);
		
		pane.add(mainPanel);
	}
	
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Close"))
		{
			dispose();
		}		
	}
	
	public class WindowDestroyer extends WindowAdapter
	{
		public WindowDestroyer() {}
		
		public void windowClosing(WindowEvent e)
		{
			dispose();
		}
	}
}
