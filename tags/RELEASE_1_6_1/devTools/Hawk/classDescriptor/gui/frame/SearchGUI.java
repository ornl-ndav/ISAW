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
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import devTools.Hawk.classDescriptor.gui.panel.search.AttributeDefnOptionsJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.ConstructorDefnOptionsJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.InterfaceDefnOptionsJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.MethodDefnOptionsJPanel;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.SearchUtilities;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SearchGUI extends JFrame implements ActionListener, ListSelectionListener
{
	protected InterfaceDefnOptionsJPanel intFPanel;
	protected AttributeDefnOptionsJPanel attributePanel;
	protected ConstructorDefnOptionsJPanel constructorPanel;
	protected MethodDefnOptionsJPanel methodPanel;
	protected JButton searchButton;
	protected JButton closeButton;
	protected Vector interfaceVec;
	
	public SearchGUI(Vector VEC)
	{
		interfaceVec = VEC;
		
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
			
		JTabbedPane tabbedPane = new JTabbedPane();
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
		
		tabbedPane.addTab("General Properties",null,panel1,"Specify general properties of the class you're searching for");
		tabbedPane.addTab("Field Properties",null,panel2,"Specify the fields' properties of the class you're searching for");
		tabbedPane.addTab("Constructor Properties",null,panel3,"Specify the constructors' properties of the class you're searching for");
		tabbedPane.addTab("Method Properties",null,panel4,"Specify the methods' properties of the class you're searching for");
		tabbedPane.addTab("Search Results",null,panel5,"The search results");
		
		mainPanel.add(tabbedPane, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		getContentPane().add(mainPanel);
		pack();
	}
	
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Close"))
			dispose();
		else if (event.getActionCommand().equals("Search"))
		{
			Vector foundVec = SearchUtilities.findMatches(interfaceVec,  attributePanel, constructorPanel, methodPanel, intFPanel);
			
			System.out.println("Search Results:");
			System.out.println("-----------------------");
			for (int i=0; i<foundVec.size(); i++)
				System.out.println(""+((Interface)foundVec.elementAt(i)).getPgmDefn().getInterface_name());
				
			if (foundVec.size() == 0)
				System.out.println("No results found");
		}
	}

	public void valueChanged(ListSelectionEvent e)
	{
	}
	
	public static void main(String args[])
	{
		try {
			UIManager.setLookAndFeel(
				new com.sun.java.swing.plaf.gtk.GTKLookAndFeel());
			SearchGUI gui = new SearchGUI(new Vector());
			gui.setVisible(true);
			System.out.println("(false || !false) =" + (false || !false));
			System.out.println("(true || !false) =" + (true || !false));
			System.out.println("(false || !true) =" + (false || !true));
			System.out.println("(true || !true) =" + (true || !true));
		} catch (Exception e) {
		}
	}
}
