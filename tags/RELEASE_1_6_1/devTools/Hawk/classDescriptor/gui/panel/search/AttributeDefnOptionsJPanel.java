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
 * Revision 1.1  2004/02/07 05:15:49  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.panel.search;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import devTools.Hawk.classDescriptor.modeledObjects.AttributeDefn;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.SearchUtilities;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AttributeDefnOptionsJPanel extends JPanel
{
	protected VectorOptionsJPanel charPanel;
	protected BasicOptionsJPanel typePanel;
	protected BasicOptionsJPanel namePanel;
	protected JPanel panelsPanel;
	
	public AttributeDefnOptionsJPanel()
	{
		charPanel = new VectorOptionsJPanel( "Characteristics:  ","Characteristic:  ","characteristics");
		typePanel = new BasicOptionsJPanel(  "Type:                 ");
		namePanel = new BasicOptionsJPanel("Name:                ");
		
		setLayout(new BorderLayout());
		panelsPanel = new JPanel();
		panelsPanel.setLayout(new GridLayout(3,0));
		panelsPanel.add(charPanel);
		panelsPanel.add(typePanel);
		panelsPanel.add(namePanel);
		
		add(panelsPanel, BorderLayout.CENTER);
	}
	
	public BasicOptionsJPanel getNamePanel()
	{
		return namePanel;
	}
	
	public BasicOptionsJPanel getTypePanel()
	{
		return typePanel;
	}
	
	public VectorOptionsJPanel getCharPanel()
	{
		return charPanel;
	}
	
	public static void main(String args[])
	{
		JFrame frame = new JFrame();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		AttributeDefnOptionsJPanel panel = new AttributeDefnOptionsJPanel();
		mainPanel.add(panel, BorderLayout.CENTER);
		frame.getContentPane().add(mainPanel);
		frame.setVisible(true);
		frame.pack();
	}
	
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
	
	public boolean matches(Interface intf)
	{
		return true;
	}
}