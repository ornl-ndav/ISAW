/*
 * File:  AboutGUI.java
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
 * Revision 1.3  2004/02/07 05:30:39  bouzekc
 * Made the path to the GPL relative to ISAW_HOME.
 *
 * Revision 1.2  2004/02/07 05:24:32  bouzekc
 * Changed all instances of "liscense" to "license".
 *
 * Revision 1.1  2004/02/07 05:08:48  bouzekc
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import devTools.Hawk.classDescriptor.tools.SystemsManager;

import DataSetTools.util.*;

/**
 * @author kramer
 *
 * 
 */ 
public class AboutGUI extends JFrame implements ActionListener
{
	private JFrame licenseFrame;
	
	public AboutGUI()
	{
		setTitle("About Hawk");
		setSize(200,200);
		addWindowListener(new WindowDestroyer("main"));
		Container pane = getContentPane();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new GridLayout(10,0));
		
			infoPanel.add(new JSeparator());
			infoPanel.add(new JLabel("Hawk "+SystemsManager.getVersion()));
			infoPanel.add(new JSeparator());
			infoPanel.add(new JLabel("    Hawk is brought to you by"));
			infoPanel.add(new JLabel("    Dominic Kramer who can "));
			infoPanel.add(new JLabel("    be contacted at"));
			infoPanel.add(new JLabel("    kramerd@uwstout.edu."));
			infoPanel.add(new JLabel("    Hawk is licensed under"));
			infoPanel.add(new JLabel("    the GNU General Public"));
			infoPanel.add(new JLabel("    Licensing Agreement"));
	
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			JButton viewButton = new JButton("View License");
			JButton closeButton = new JButton("Close"); 
			
			viewButton.addActionListener(this);
			closeButton.addActionListener(this);
			
			buttonPanel.add(viewButton);
			buttonPanel.add(closeButton);
		
		ImageIcon hawkIcon = new ImageIcon("devTools.Hawk.classDescriptor/pixmaps/about_hawk.png");
		
		mainPanel.add(new JLabel(hawkIcon), BorderLayout.NORTH);
		mainPanel.add(infoPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		pane.add(mainPanel);
		pack();
	}
	
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Close"))
		{
			dispose();
			if (licenseFrame != null)
				licenseFrame.dispose();
		}
		else if (event.getActionCommand().equals("View License"))
		{
			licenseFrame = new JFrame();
			
			licenseFrame.setTitle("GNU General Public Licensing Agreement");
			licenseFrame.setSize(550,550);
			licenseFrame.addWindowListener(new WindowDestroyer("license"));
			
			JTextArea license = new JTextArea();
			license.setEditable(false);
			String line = "";
			try
			{
				BufferedReader gnuReader = new BufferedReader(
          new FileReader( FilenameUtil.setForwardSlash( 
            SharedData.getProperty( "ISAW_HOME" ) +
            "/devTools/Hawk/classDescriptor/gui/license/LICENSE.txt") ) );
				while (line != null)
				{
					license.append(line+"\n");
					line = gnuReader.readLine();
				}
			}
			catch(IOException e)
			{
				System.out.println("An IOException was thrown in actionPerformed(ActionEvent) in AboutGUI.java:");
				System.err.println(e);
				StackTraceElement[] traceArray = e.getStackTrace();
				for (int i = 0; i < traceArray.length; i++)
						System.out.println("     "+traceArray[i]);
			}
			
			JScrollPane scrollpane = new JScrollPane(license);
			JPanel mainpanel = new JPanel();
			mainpanel.setLayout(new BorderLayout());
			
			JPanel buttonpanel = new JPanel();
			buttonpanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			
			JButton closebutton = new JButton("Close");
			closebutton.setActionCommand("licenseFrame.close");
			closebutton.addActionListener(this);
			buttonpanel.add(closebutton);
			
			mainpanel.add(scrollpane, BorderLayout.CENTER);
			mainpanel.add(buttonpanel, BorderLayout.SOUTH);
			
			licenseFrame.getContentPane().add(mainpanel);
			licenseFrame.setVisible(true);			
		}
		else if (event.getActionCommand().equals("licenseFrame.close"))
		{
			licenseFrame.dispose();
		}
	}
	
	private class WindowDestroyer extends WindowAdapter
	{
			private String name;
			
			public WindowDestroyer(String str)
			{
				name = str;
			}
			
			public void windowClosing(WindowEvent event)
			{
				if (name.equals("main"))	
					dispose();
				else if (name.equals("license"))
					licenseFrame.dispose();
			}
	}
}
