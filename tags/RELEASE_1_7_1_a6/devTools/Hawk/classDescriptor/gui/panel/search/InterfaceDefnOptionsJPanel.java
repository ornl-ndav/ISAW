/*
 * File:  InterfaceDefnOptionsJPanel.java
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
 * Revision 1.1  2004/02/07 05:15:50  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.panel.search;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Makes a panel which allows the user to enter search parameters that an interface's general information 
 * has to meet.
 * @author Dominic Kramer
 */
public class InterfaceDefnOptionsJPanel extends JPanel
{
	/**
	 * Integer specifying that the Interface object has to represents a class.
	 */
	public static final int CLASS = 1;
	/**
	 * Integer specitying that the Interface object has to represent an interface.
	 */
	public static final int INTERFACE = 2;
	/**
	 * Integer specitying that the Interface object has to represent either a class or interface.
	 */
	public static final int ANY = 0;
	
	/** Combo box specifying that the Interface is either a class, interface, or either. */
	protected JComboBox typeBox;
	/** The panel containing fields to specify the Interface's name. */
	protected BasicOptionsJPanel namePanel;
	/** The panel containing fields to specify the class or interface the Interface extends. */
	protected BasicOptionsJPanel extendsPanel;
	/** The panel containing fields to specify the Interface's package's name. */
	protected BasicOptionsJPanel packageNamePanel;
	/** The panel containing fields to specify what the Interface implements. */
	protected VectorOptionsJPanel implementsPanel;
	/** The panel containing fields to specify the Interface's characteristics (public, static, final, etc.). */
	protected VectorOptionsJPanel characteristicsPanel;
	/** The panel containing all of the other panel's. */
	protected JPanel panelsPanel;
	
	/**
	 * Create a InterfaceDefnOptionsJPanel.
	 */	
	public InterfaceDefnOptionsJPanel()
	{
		String[] stringArr = new String[3];
			stringArr[0]="any";
			stringArr[1]="class";
			stringArr[2]="interface";
		typeBox = new JComboBox(stringArr);
		JPanel typePanel = new JPanel();
			typePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			typePanel.add(new JLabel("Type:                 "));
			typePanel.add(typeBox);
		
		namePanel = new BasicOptionsJPanel("Name:                ");
		extendsPanel = new BasicOptionsJPanel("Extends:            ");
		packageNamePanel = new BasicOptionsJPanel("Package Name:  ");
		implementsPanel = new VectorOptionsJPanel("Implements:      ","Implements:  ","interfaces");
		characteristicsPanel = new VectorOptionsJPanel("Characteristics:  ","Characteristic:  ","characteristics");
		
		setLayout(new BorderLayout());
		panelsPanel = new JPanel();
		panelsPanel.setLayout(new GridLayout(6,0));
		panelsPanel.add(characteristicsPanel);
		panelsPanel.add(typePanel);
		panelsPanel.add(namePanel);
		panelsPanel.add(extendsPanel);
		panelsPanel.add(implementsPanel);
		panelsPanel.add(packageNamePanel);
		
		add(panelsPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Get the type that the Interface has to be.
	 * @return Either CLASS, INTERFACE, or ANY.
	 */	
	public int getType()
	{
		return typeBox.getSelectedIndex();
	}
	
	/**
	 * Get the panel describing what the Interface's name must be.
	 */
	public BasicOptionsJPanel getNamePanel()
	{
		return namePanel;
	}
	
	/**
	 * Get the panel describing what the Interface must extend.
	 */
	public BasicOptionsJPanel getExtendsPanel()
	{
		return extendsPanel;
	}
	
	/**
	 * Get the panel describing what the Interface's package's name must be.
	 */
	public BasicOptionsJPanel getPackageNamePanel()
	{
		return packageNamePanel;
	}
	
	/**
	 * Get the panel describing what the Interface implements.
	 */
	public VectorOptionsJPanel getImplementsPanel()
	{
		return implementsPanel;
	}
	
	/**
	 * Get the panel describing what the Interface's characteristics must be (public, static, final, etc.).
	 */	
	public VectorOptionsJPanel getCharPanel()
	{
		return characteristicsPanel;
	}
}