/*
 * File:  SearchGUI.java
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
 * Revision 1.4  2004/05/26 19:41:21  kramer
 * Made the class implement ExternallyControlledFrame and added any inherited
 * methods.
 *
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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import devTools.Hawk.classDescriptor.gui.ExternallyControlledFrame;
import devTools.Hawk.classDescriptor.gui.panel.AlphabeticalListJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.AttributeDefnOptionsJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.ConstructorDefnOptionsJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.InterfaceDefnOptionsJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.MethodDefnOptionsJPanel;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.SearchUtilities;

/**
 * This is the window which allows the user to search for an interface meeting specific conditions.
 * @author Dominic Kramer
 */
public class SearchGUI extends JFrame implements ActionListener, ListSelectionListener, ExternallyControlledFrame
{
	/** The panel used to specify the conditions one of the interface's attributes has to meet. */
	protected AttributeDefnOptionsJPanel attributePanel;
	/** The button that closes the window. */
	protected JButton closeButton;
	/** The panel used to specify the conditions one of the interface's constructors has to meet. */
	protected ConstructorDefnOptionsJPanel constructorPanel;
	/** The Interfaces that are to be searched. */
	protected Vector interfacesToSearchVec;
	/** The panel used to specify the conditions that the interface's general properties have to meet. */
	protected InterfaceDefnOptionsJPanel intFPanel;
	/** The panel used to specify the conditions one of the interface's methods has to meet. */
	protected MethodDefnOptionsJPanel methodPanel;
	/** The tabbed pane onto which the panels are placed. */
	protected JTabbedPane tabbedPane;
	
	/** The panel containing the interfaces found from the search. */
	protected AlphabeticalListJPanel listPanel;
	/** The button which initiates the search. */
	protected JButton searchButton;
	/** The project containing all of the interfaces that meet the condition of the search. */
	protected Project projectMadeFromSearch;
	/** The HawkDesktop onto which windows opened from this search window are to be placed. */
	protected HawkDesktop desktop;
	
	/**
	 * Make a new SearchGUI.
	 * @param VEC The Vector of Interface objects to search.
	 * @param desk The HawkDesktop onto which windows which are opened from this window are placed.
	 */
	public SearchGUI(Vector VEC, HawkDesktop desk)
	{
		interfacesToSearchVec = VEC;
		desktop = desk;
		
		JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			searchButton = new JButton("Search");
				searchButton.addActionListener(this);
			closeButton = new JButton("Close");
				closeButton.addActionListener(this);
			buttonPanel.add(searchButton);
			buttonPanel.add(closeButton);
			
		tabbedPane = new JTabbedPane();
		JPanel panel1 = new JPanel();
			panel1.setLayout(new BorderLayout());
			intFPanel = new InterfaceDefnOptionsJPanel();
			panel1.add(intFPanel, BorderLayout.CENTER);
		
		JPanel panel2 = new JPanel();
			panel2.setLayout(new BorderLayout());
			attributePanel = new AttributeDefnOptionsJPanel();
			panel2.add(attributePanel, BorderLayout.CENTER);
		
		JPanel panel3 = new JPanel();
			panel3.setLayout(new BorderLayout());
			constructorPanel = new ConstructorDefnOptionsJPanel();
			panel3.add(constructorPanel, BorderLayout.CENTER);
		
		JPanel panel4 = new JPanel();
			panel4.setLayout(new BorderLayout());
			methodPanel = new MethodDefnOptionsJPanel();
			panel4.add(methodPanel, BorderLayout.CENTER);
		
		JPanel panel5 = new JPanel();
			panel5.setLayout(new BorderLayout());
			projectMadeFromSearch = new Project();
			listPanel = new AlphabeticalListJPanel(projectMadeFromSearch,false,false,desktop,this);
			listPanel.setLayout(new GridLayout(1,1));
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());
				topPanel.add(new JLabel("Search Results"),BorderLayout.NORTH);
				topPanel.add(new JSeparator(),BorderLayout.SOUTH);
			panel5.add(topPanel,BorderLayout.NORTH);
			panel5.add(listPanel,BorderLayout.CENTER);
			
		tabbedPane.addTab("General Properties",null,panel1,"Specify general properties of the class you're searching for");
		tabbedPane.addTab("Field Properties",null,panel2,"Specify the fields' properties of the class you're searching for");
		tabbedPane.addTab("Constructor Properties",null,panel3,"Specify the constructors' properties of the class you're searching for");
		tabbedPane.addTab("Method Properties",null,panel4,"Specify the methods' properties of the class you're searching for");
		tabbedPane.addTab("Search Results",null,panel5,"The search results");
		
		mainPanel.add(tabbedPane, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		getContentPane().add(mainPanel);
		
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
			JMenuItem closeItem = listPanel.getCloseMenuItem();
			closeItem.addActionListener(this);
			closeItem.setActionCommand("Close");
			fileMenu.add(closeItem);
		menubar.add(fileMenu);
		menubar.add(listPanel.getEditMenu());
		menubar.add(listPanel.getViewMenu());
		menubar.add(listPanel.getPropertiesMenu(listPanel.getShortenJavaCheckBox().isSelected(),listPanel.getShortenOtherCheckBox().isSelected()));
		setJMenuBar(menubar);
		
		pack();
	}
	
	/**
	 * Handles action events.
	 */
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Close"))
			dispose();
		else if (event.getActionCommand().equals("Search"))
		{
			Vector foundVec = SearchUtilities.findMatches(interfacesToSearchVec,  attributePanel, constructorPanel, methodPanel, intFPanel);
			if (foundVec.size() == 0)
			{
				Interface tempInterface = new Interface();
				tempInterface.getPgmDefn().setInterface_name("No class or interface found");
				foundVec.addElement(tempInterface);
			}
			tabbedPane.setSelectedIndex(4);
			projectMadeFromSearch.setInterfaceVec(foundVec);
			listPanel.getShortenJavaCheckBox().setSelected(false);
			listPanel.getShortenOtherCheckBox().setSelected(false);
			listPanel.fillList();
		}
	}
	
	/**
	 * Handles listening for the list to change.
	 */
	public void valueChanged(ListSelectionEvent e)
	{
	}
	
	//----The methods that are to be inherited from ExternallyControlledFrame are handled by JFrame exept this one
	/** 
	 * Get the Component that is being controlled.
	 * @return this.
	 */
	public Component getControlledComponent()
	{
		return this;
	}
}
