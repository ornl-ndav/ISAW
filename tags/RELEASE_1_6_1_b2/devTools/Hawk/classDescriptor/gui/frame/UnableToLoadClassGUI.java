/*
 * File:  UnableToLoadClassGUI.java
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class UnableToLoadClassGUI implements ActionListener
{
	private JFrame frame;
	private JTable table;
	private Vector errorVec;
	private JScrollPane scrollPane;
	private boolean showErrorBox;
	
	public UnableToLoadClassGUI()
	{
		errorVec = new Vector();
		showErrorBox = false;
	}
	
	public boolean showErrorBox()
	{
		return showErrorBox;
	}
	
	public void showGUI()
	{
		frame = new JFrame();
		frame.setTitle("Error");
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		Vector headerVec = new Vector();
			headerVec.add("Unable To Load Class");
			headerVec.add("Class Location");
			headerVec.add("Cause");
			headerVec.add("Error Thrown");
			
		table = new JTable(errorVec, headerVec);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setPreferredScrollableViewportSize(new Dimension(700, 200));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton button = new JButton("Ok");
		button.addActionListener(this);
		buttonPanel.add(button);
		
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new GridLayout(2,0));
			labelPanel.add(new JLabel("The following errors occured while loading the classes and interfaces."));
			labelPanel.add(new JLabel("If any class could not be found modify your classpath to allow them to be found."));
		
		mainPanel.add(labelPanel, BorderLayout.NORTH);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		frame.getContentPane().add(mainPanel);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void processThrowable(String filename, String classname, Throwable e)
	{
		showErrorBox = true;
		Vector rowVec = new Vector();
		rowVec.add(classname);
		rowVec.add(filename);
		if (e.getClass().getName().equals("java.lang.ClassNotFoundException"))
		{
			rowVec.add("Could not find class "+e.getMessage());
		}
		else if (e.getClass().getName().equals("java.lang.NoClassDefFoundError"))
		{
			String message = "";
			StringTokenizer tokenizer = new StringTokenizer(e.getMessage(), System.getProperty("file.separator"));
			int count = tokenizer.countTokens();
			for (int i=1; i<count; i++)
				message += tokenizer.nextToken() + ".";
			message += tokenizer.nextToken();
			rowVec.add("Could not find class "+message);
		}
		else if (e.getClass().getName().equals("java.lang.SecurityException"))
		{
			rowVec.add(e.getMessage());
		}
		else
		{
			rowVec.add("Unknown");
			SystemsManager.printStackTrace(e);
		}
		rowVec.add(e.toString());
		errorVec.add(rowVec);
	}
	
	public void actionPerformed(ActionEvent event)
	{
			frame.dispose();
	}

}
