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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class InterfaceDefnOptionsJPanel extends JPanel
{
	public static final int CLASS = 1;
	public static final int INTERFACE = 2;
	public static final int ANY = 0;
	
	protected JComboBox typeBox;
	protected BasicOptionsJPanel namePanel;
	protected BasicOptionsJPanel extendsPanel;
	protected BasicOptionsJPanel packageNamePanel;
	protected VectorOptionsJPanel implementsPanel;
	protected VectorOptionsJPanel characteristicsPanel;
	protected JPanel panelsPanel;
	protected JLabel label;
	
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
	
	public int getType()
	{
		return typeBox.getSelectedIndex();
	}
	public BasicOptionsJPanel getNamePanel()
	{
		return namePanel;
	}
	
	public BasicOptionsJPanel getExtendsPanel()
	{
		return extendsPanel;
	}
	
	public BasicOptionsJPanel getPackageNamePanel()
	{
		return packageNamePanel;
	}

	public VectorOptionsJPanel getImplementsPanel()
	{
		return implementsPanel;
	}
	
	public VectorOptionsJPanel getCharPanel()
	{
		return characteristicsPanel;
	}

	public static void main(String args[])
	{
		JFrame frame = new JFrame();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		InterfaceDefnOptionsJPanel panel = new InterfaceDefnOptionsJPanel();
		mainPanel.add(panel, BorderLayout.CENTER);
		frame.getContentPane().add(mainPanel);
		frame.setVisible(true);
		frame.pack();
	}
}