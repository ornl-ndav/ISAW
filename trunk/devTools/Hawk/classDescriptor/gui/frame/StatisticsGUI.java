/*
 * File:  StatisticsGUI.java
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
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.StatisticsManager;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class StatisticsGUI extends JFrame implements ActionListener
{
	protected Project project;
	protected StatisticsManager manager;
	protected JButton closeButton;
	
	public StatisticsGUI(Project pro)
	{
		project = pro;
		manager = new StatisticsManager(pro);
		
		setTitle(""+project.getProjectName()+"'s Statistics");
		Container pane = getContentPane();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(11,0));
		
		JPanel numberPanel = new JPanel();
		numberPanel.setLayout(new GridLayout(11,0));
		
		JPanel subMainPanel = new JPanel();
		subMainPanel.setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new GridLayout(2,0));
		
		namePanel.add(new JLabel("Project Name:  "+pro.getProjectName()));
		namePanel.add(new JSeparator());
			subMainPanel.add(namePanel, BorderLayout.NORTH);
		
		textPanel.add(new JLabel("Total number of classes and interfaces:"));
		numberPanel.add(new JLabel(""+manager.getTotalNumberOfClassesAndInterfaces()));

		textPanel.add(new JLabel("     Number of Interfaces:  "));
		numberPanel.add(new JLabel(""+manager.getNumberOfInterfaces()));

		textPanel.add(new JLabel("     Total number of classes:  "));
		numberPanel.add(new JLabel(""+manager.getNumberOfClasses()));
		
		textPanel.add(new JLabel("          Number of Inner Classes:  "));
		numberPanel.add(new JLabel(""+manager.getTotalNumberOfInnerClass()));

		textPanel.add(new JLabel("          Number of Non-Inner Classes:  "));
		numberPanel.add(new JLabel(""+manager.getTotalNumberOfNonInnerClass()));

		textPanel.add(new JLabel("          Number of Abstract Classes:  "));
		numberPanel.add(new JLabel(""+manager.getTotalNumberOfAbstractClasses()));
		
		textPanel.add(new JLabel("               Number of Abstract Inner Classes:  "));
		numberPanel.add(new JLabel(""+manager.getNumberOfAbstractInnerClasses()));

		textPanel.add(new JLabel("               Number of Abstract Non-Inner Classes:  "));
		numberPanel.add(new JLabel(""+manager.getNumberOfAbstractNonInnerClasses()));
		
		textPanel.add(new JLabel("          Number of Concrete Classes:  "));
		numberPanel.add(new JLabel(""+manager.getTotalNumberOfConcreteClasses()));

		textPanel.add(new JLabel("               Number of Concrete Inner Classes:  "));
		numberPanel.add(new JLabel(""+manager.getNumberOfConcreteInnerClasses()));

		textPanel.add(new JLabel("               Number of Concrete Non-Inner Classes:  "));
		numberPanel.add(new JLabel(""+manager.getNumberOfConcreteNonInnerClasses()));

			subMainPanel.add(textPanel, BorderLayout.WEST);
			subMainPanel.add(numberPanel, BorderLayout.EAST);
			subMainPanel.add(new JSeparator(), BorderLayout.SOUTH);
			
		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		
		mainPanel.add(subMainPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		pane.add(mainPanel);
		pack();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		dispose();
	}

}
