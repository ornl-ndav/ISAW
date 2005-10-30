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
 * Revision 1.3  2004/03/12 19:46:15  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:08:52  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Vector;

//import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
//import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;

import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.StatisticsManager;

/**
 * This class makes a window displaying statistics about the supplied Project.  These 
 * statistics include the number of classes, interfaces, inner-classes, abstract classes, 
 * etc. in the project.
 * @author kramer
 */
public class StatisticsGUI extends JFrame implements ActionListener
{
	/**
	 * The Project that is being analyzed.
	 */
	protected Project project;
	/**
	 * The manager used to obtain information about the Project.
	 */
	protected StatisticsManager manager;
	
	/**
	 * Create a new StatisticsGUI.
	 * @param pro The Project to analyze.
	 */
	public StatisticsGUI(Project pro)
	{
		project = pro;
		manager = new StatisticsManager(pro);
		
		setTitle(""+project.getProjectName()+"'s Statistics");
		Container pane = getContentPane();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		/*
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
		numberPanel.add(new JLabel(""+manager.getTotalNumberOfInterfaces()));

		textPanel.add(new JLabel("     Total number of classes:  "));
		numberPanel.add(new JLabel(""+manager.getTotalNumberOfClasses()));
		
		textPanel.add(new JLabel("          Number of Inner Classes:  "));
		numberPanel.add(new JLabel(""+manager.getTotalNumberOfInnerClasses()));

		textPanel.add(new JLabel("          Number of Non-Inner Classes:  "));
		numberPanel.add(new JLabel(""+manager.getTotalNumberOfOuterClasses()));

		textPanel.add(new JLabel("          Number of Abstract Classes:  "));
		numberPanel.add(new JLabel(""+manager.getTotalNumberOfAbstractClasses()));
		
		textPanel.add(new JLabel("               Number of Abstract Inner Classes:  "));
		numberPanel.add(new JLabel(""+manager.getNumberOfAbstractInnerClasses()));

		textPanel.add(new JLabel("               Number of Abstract Non-Inner Classes:  "));
		numberPanel.add(new JLabel(""+manager.getNumberOfAbstractOuterClasses()));
		
		textPanel.add(new JLabel("          Number of Concrete Classes:  "));
		numberPanel.add(new JLabel(""+manager.getTotalNumberOfConcreteClasses()));

		textPanel.add(new JLabel("               Number of Concrete Inner Classes:  "));
		numberPanel.add(new JLabel(""+manager.getNumberOfConcreteInnerClasses()));

		textPanel.add(new JLabel("               Number of Concrete Non-Inner Classes:  "));
		numberPanel.add(new JLabel(""+manager.getNumberOfConcreteOuterClasses()));

			subMainPanel.add(textPanel, BorderLayout.WEST);
			subMainPanel.add(numberPanel, BorderLayout.EAST);
			subMainPanel.add(new JSeparator(), BorderLayout.SOUTH);
			
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		
		mainPanel.add(subMainPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		*/
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		JPanel classPanel = new JPanel();
		classPanel.setLayout(new BorderLayout());
			JPanel topClassPanel = new JPanel();
			topClassPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			topClassPanel.add(new JLabel("Class Statistics"));
				classPanel.add(new JLabel(" "), BorderLayout.NORTH);
				JTable classTable = getClassTable();
				classTable.setPreferredScrollableViewportSize(new Dimension(300, 50));
				JScrollPane classPane = new JScrollPane(classTable);
				classPanel.add(classPane,BorderLayout.CENTER);
		
		JPanel interfacePanel = new JPanel();
		interfacePanel.setLayout(new BorderLayout());
			JPanel topInterfacePanel = new JPanel();
			topInterfacePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			topInterfacePanel.add(new JLabel("Interface Statistics"));
				interfacePanel.add(new JLabel(" "), BorderLayout.NORTH);
					JTable interfaceTable = getInterfaceTable();
					interfaceTable.setPreferredScrollableViewportSize(new Dimension(300, 50));
					JScrollPane interfacePane = new JScrollPane(interfaceTable);
				interfacePanel.add(interfacePane,BorderLayout.CENTER);

		JPanel interfaceAndClassPanel = new JPanel();
		interfaceAndClassPanel.setLayout(new BorderLayout());
			JPanel topInterfaceAndClassPanel = new JPanel();
				topInterfaceAndClassPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
//				topInterfaceAndClassPanel.add(new JLabel("Totals"));
					interfaceAndClassPanel.add(topInterfaceAndClassPanel, BorderLayout.NORTH);
						JTable totalTable = getClassAndInterfaceTable();
						totalTable.setPreferredScrollableViewportSize(new Dimension(300, 50));
						JScrollPane totalPane = new JScrollPane(totalTable);
					interfaceAndClassPanel.add(totalPane,BorderLayout.CENTER);
		
		tabbedPane.addTab("Class Information",classPanel);			
		tabbedPane.addTab("Interface Information",interfacePanel);
		tabbedPane.addTab("Totals",interfaceAndClassPanel);
		mainPanel.add(tabbedPane,BorderLayout.CENTER);
		pane.add(mainPanel);
		setSize(420,170);
	}
	
	public JTable getClassTable()
	{
		Vector rowData = new Vector();
		Vector name = new Vector();
		  name.add("");
		  name.add("Inner");
		  name.add("Outer");
		  name.add("Total");
		
		Vector abstractVec = new Vector();
		abstractVec.add("Abstract");
		abstractVec.add(""+manager.getNumberOfAbstractInnerClasses());
		abstractVec.add(""+manager.getNumberOfAbstractOuterClasses());
		abstractVec.add(""+manager.getTotalNumberOfAbstractClasses());
		
		Vector concreteVec = new Vector();
		concreteVec.add("Concrete");
		concreteVec.add(""+manager.getNumberOfConcreteInnerClasses());
		concreteVec.add(""+manager.getNumberOfConcreteOuterClasses());
		concreteVec.add(""+manager.getTotalNumberOfConcreteClasses());
		
		Vector totalVec = new Vector();
		totalVec.add("Total");
		totalVec.add(""+manager.getTotalNumberOfInnerClasses());
		totalVec.add(""+manager.getTotalNumberOfOuterClasses());
		totalVec.add(""+manager.getTotalNumberOfClasses());
		
		rowData.add(abstractVec);
		rowData.add(concreteVec);
		rowData.add(totalVec);
		
		return new JTable(rowData,name);
	}
	
	public JTable getInterfaceTable()
	{
		String[][] rowData = new String[3][4];
		String[] name = new String[4];
		  name[0]= "";
		  name[1]= "Inner";
		  name[2]= "Outer";
		  name[3]= "Total";
		  
		rowData[0][0] = "Abstract";
		rowData[0][1] = ""+manager.getNumberOfAbstractInnerInterfaces();
		rowData[0][2] = ""+manager.getNumberOfAbstractOuterInterfaces();
		rowData[0][3] = ""+manager.getTotalNumberOfAbstractInterfaces();
		
		rowData[1][0] = "Concrete";
		rowData[1][1] = ""+manager.getNumberOfConcreteInnerInterfaces();
		rowData[1][2] = ""+manager.getNumberOfConcreteOuterInterfaces();
		rowData[1][3] = ""+manager.getTotalNumberOfConcreteInterfaces();
		
		rowData[2][0] = "Total";
		rowData[2][1] = ""+manager.getTotalNumberOfInnerInterfaces();
		rowData[2][2] = ""+manager.getTotalNumberOfOuterInterfaces();
		rowData[2][3] = ""+manager.getTotalNumberOfInterfaces();
		
		return new JTable(rowData,name);
	}
	
	public JTable getClassAndInterfaceTable()
	{
		String[][] rowData = new String[5][4];
		String[] name = new String[4];
		  name[0]= "";
		  name[1]= "Classes";
		  name[2]= "Interfaces";
		  name[3]= "Total";
		  
		rowData[0][0] = "Outer";
		rowData[0][1] = ""+manager.getTotalNumberOfOuterClasses();
		rowData[0][2] = ""+manager.getTotalNumberOfOuterInterfaces();
		rowData[0][3] = ""+manager.getTotalNumberOfOuterClassesAndInterfaces();
		
		rowData[1][0] = "Inner";
		rowData[1][1] = ""+manager.getTotalNumberOfInnerClasses();
		rowData[1][2] = ""+manager.getTotalNumberOfInnerInterfaces();
		rowData[1][3] = ""+manager.getTotalNumberOfInnerClassesAndInterfaces();
		
		rowData[2][0] = "Abstract";
		rowData[2][1] = ""+manager.getTotalNumberOfAbstractClasses();
		rowData[2][2] = ""+manager.getTotalNumberOfAbstractInterfaces();
		rowData[2][3] = ""+manager.getTotalNumberOfAbstractClassesAndInterfaces();
		
		rowData[3][0] = "Concrete";
		rowData[3][1] = ""+manager.getTotalNumberOfConcreteClasses();
		rowData[3][2] = ""+manager.getTotalNumberOfConcreteInterfaces();
		rowData[3][3] = ""+manager.getTotalNumberOfConcreteClassesAndInterfaces();
		
		rowData[4][0] = "Total";
		rowData[4][1] = ""+manager.getTotalNumberOfClasses();
		rowData[4][2] = ""+manager.getTotalNumberOfInterfaces();
		rowData[4][3] = ""+manager.getTotalNumberOfClassesAndInterfaces();
		
		return new JTable(rowData,name);
	}

	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent e)
	{
		dispose();
	}

}
