/*
 * File:  HawkDesktop.java
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
 * Revision 1.1  2004/02/07 05:08:50  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import devTools.Hawk.classDescriptor.gui.panel.ProjectSelectorJPanel;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HawkDesktop extends JFrame implements ActionListener
{
	protected Vector proVec;
	protected ProjectSelectorJPanel proPanel;
	protected JTabbedPane tabbedPane;
	
	public HawkDesktop(Vector vec)
	{
		proVec = vec;
		
		setTitle("Hawk");
		Dimension dim = getToolkit().getScreenSize();
		setSize((int)(dim.getWidth()*0.8),(int)(dim.getHeight()*0.8));
		setLocation((int)(dim.getWidth()*0.1),(int)(dim.getHeight()*0.1));
		addWindowListener(new WindowDestroyer());
		
		JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
		JPanel westSuperPanel = new JPanel();
			westSuperPanel.setLayout(new BorderLayout());
			JPanel westPanel = new JPanel();
				westPanel.setLayout(new BorderLayout());
				proPanel = new ProjectSelectorJPanel(proVec,this);
					westPanel.add(new JLabel("Current Projects"), BorderLayout.NORTH);
					westPanel.add(proPanel, BorderLayout.CENTER);
						westSuperPanel.add(westPanel,BorderLayout.CENTER);
						westSuperPanel.add(new JLabel(" "), BorderLayout.EAST);
						westSuperPanel.add(new JLabel(" "), BorderLayout.WEST);
						westSuperPanel.add(new JLabel(" "), BorderLayout.SOUTH);

		JPanel topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());
			JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//				JButton newButton = new JButton(new ImageIcon("jar:///pixmaps/gtk-new.png"));
				JButton newButton = new JButton("New");
				newButton.setToolTipText("Create a new project");
				newButton.setActionCommand("project.new");
				newButton.addActionListener(this);
				buttonPanel.add(newButton);
				
//				JButton openButton = new JButton(new ImageIcon("jar://pixmaps/stock-open.png"));
				JButton openButton = new JButton("Open");
				openButton.setToolTipText("Open a previously saved project");
				openButton.setActionCommand("project.open");
				openButton.addActionListener(this);
				buttonPanel.add(openButton);
				
//				JButton saveButton = new JButton(new ImageIcon("jar:pixmaps/filesave.png"));
				JButton saveButton = new JButton("Save");
				saveButton.setToolTipText("Save a project");
				saveButton.setActionCommand("project.saveAs");
				saveButton.addActionListener(this);
				buttonPanel.add(saveButton);
				
//				JButton printButton = new JButton(new ImageIcon("devTools.Hawk.classDescriptor/pixmaps/fileprint.png"));
				JButton printButton = new JButton("Print");
				printButton.setToolTipText("Print class and interface information");
				printButton.setActionCommand("project.print");
				printButton.addActionListener(this);
				buttonPanel.add(printButton);
				
				topPanel.add(buttonPanel, BorderLayout.CENTER);
				topPanel.add(new JSeparator(), BorderLayout.SOUTH);				
						
		tabbedPane = new JTabbedPane();
			JDesktopPane dTop = new JDesktopPane();
			tabbedPane.add(dTop);
			tabbedPane.setTitleAt(0,"Tab 1");

		mainPanel.add(topPanel, BorderLayout.NORTH);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,westSuperPanel,tabbedPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);

		mainPanel.add(splitPane, BorderLayout.CENTER);
		
		getContentPane().add(mainPanel);
		setJMenuBar(proPanel.getProjectJMenuBar());		
	}
	
	public String[] getTabNames()
	{
		int num = tabbedPane.getTabCount();
		String[] arr = new String[num];
		
		for (int i=0; i<num; i++)
			arr[i] = tabbedPane.getTitleAt(i);
		
		return arr;
	}
	
	public JTabbedPane getTabbedPane()
	{
		return tabbedPane;
	}
	
	public JDesktopPane getSelectedDesktop()
	{
		int num = tabbedPane.getSelectedIndex();
		if (num != -1)
			return (JDesktopPane)tabbedPane.getComponentAt(num);
		else
			return null;
	}
	
	public void setSelectedDesktop(int index)
	{
		tabbedPane.setSelectedIndex(index);
	}
	
	public void actionPerformed(ActionEvent event)
	{
		proPanel.processSentEvent(event);
	}
	
	class WindowDestroyer extends WindowAdapter
	{
		public void windowClosing(WindowEvent event)
		{
			dispose();
			System.exit(0);
		}
	}
}