/*
 * File:  AttributeDefnOptionsJPanel.java
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
 * Revision 1.2  2004/03/11 18:27:23  bouzekc
 * Documented file using javadoc statements.
 * Removed the main method.
 *
 * Revision 1.1  2004/02/07 05:15:49  bouzekc
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

import devTools.Hawk.classDescriptor.modeledObjects.AttributeDefn;
import devTools.Hawk.classDescriptor.tools.SearchUtilities;

/**
 * Makes a panel which allows the user to enter search parameters that an attribute has to have.
 * @author Dominic Kramer
 */
public class AttributeDefnOptionsJPanel extends JPanel
{
	/** The panel holding the fields to specify the attribute's characteristics (public, static, final, etc.). */
	protected VectorOptionsJPanel charPanel;
	/** The panel holding the fields to specify the attribute's type. */
	protected BasicOptionsJPanel typePanel;
	/** The panel holding the fields to specify the attribute's name. */
	protected BasicOptionsJPanel namePanel;
	/** The panel which holds all of the other panels. */
	protected JPanel panelsPanel;
	
	/**
	 * Create a new AttributeDefnOptionsJPanel.
	 */
	public AttributeDefnOptionsJPanel()
	{
		charPanel = new VectorOptionsJPanel( "Characteristics:  ","Characteristic:  ","characteristics");
		typePanel = new BasicOptionsJPanel(  "Type:                 ");
		namePanel = new BasicOptionsJPanel("Name:                ");
		
		setLayout(new BorderLayout());
		panelsPanel = new JPanel();
		panelsPanel.setLayout(new GridLayout(6,0));
		panelsPanel.add(charPanel);
		panelsPanel.add(typePanel);
		panelsPanel.add(namePanel);
		panelsPanel.add(new JLabel());
		panelsPanel.add(new JLabel());
		panelsPanel.add(new JLabel());
		add(panelsPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Get the panel containing the fields to enter the attribute's name.
	 */
	public BasicOptionsJPanel getNamePanel()
	{
		return namePanel;
	}
	
	/**
	 * Get the panel containing the fields to specify the attribute's type.
	 */
	public BasicOptionsJPanel getTypePanel()
	{
		return typePanel;
	}
	
	/**
	 * Get the panel containing the fields to specify the attribute's characteristics (public, static, final, etc.).
	 */
	public VectorOptionsJPanel getCharPanel()
	{
		return charPanel;
	}
	
	/**
	 * Determines if the the AttributeDefn object matches the search parameters from this panel.
	 * @param atD The AttributeDefn object to compare against.
	 * @return True if the AttributeDefn object is a match.
	 */
	public boolean matchesWith(AttributeDefn atD)
	{
		boolean answer = SearchUtilities.stringMatches(namePanel.getTextFieldText(), atD.getAttribute_name(),namePanel.mustMatchContainWord(),namePanel.matchEntireWord(),namePanel.matchCaseSensitive());
		if (answer)
		{
			answer = answer && SearchUtilities.stringMatches(typePanel.getTextFieldText(), atD.getAttribute_type(),typePanel.mustMatchContainWord(),typePanel.matchEntireWord(),typePanel.matchCaseSensitive());
			if (answer)
			{
				
			}
		}
		return answer;
	}
}