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
 * Revision 1.3  2004/03/12 19:46:14  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:08:50  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.plaf.metal.MetalBorders;

import devTools.Hawk.classDescriptor.gui.panel.ProjectSelectorJPanel;

/**
 * This is Hawk's main window from which the user does everything.
 * @author Dominic Kramer
 */
public class HawkDesktop extends JFrame implements ActionListener
{
	/**
	 * This is basically a modified JPanel which contains a JList of the currently opened Projects.  It 
	 * also has support for a popup menu and has a method to obain a JMenuBar.  The ProjectSelectorJPanel 
	 * handles ActionEvents from selecting an item from a meu or popup menu.
	 */
	protected ProjectSelectorJPanel proPanel;
	/**
	 * This is the JTabbedPane.  Each tab in the JTabbedPane 
	 * holds a JDesktop each of which hold JInternalFrames.
	 */
	protected JTabbedPane tabbedPane;
	
	/**
	 * This is set to true if this HawkDesktop is the first HawkDesktop opened.
	 */
	protected boolean firstWindowOpened;
	
	/**
	 * Creates a HawkDesktop.
	 * @param vec The Vector of Projects to add to the ProjectSelectorJPanel in the HawkDesktop.
	 * @param firstOpened True if this is the first HawkDesktop window opened.
	 */
	public HawkDesktop(Vector vec, boolean firstOpened)
	{
		firstWindowOpened = firstOpened;
		
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
				proPanel = new ProjectSelectorJPanel(vec,this);
					westPanel.add(new JLabel("Current Projects"), BorderLayout.NORTH);
					westPanel.add(proPanel, BorderLayout.CENTER);
						westSuperPanel.add(westPanel,BorderLayout.CENTER);
						westSuperPanel.add(new JLabel(" "), BorderLayout.EAST);
						westSuperPanel.add(new JLabel(" "), BorderLayout.WEST);
						westSuperPanel.add(new JLabel(" "), BorderLayout.SOUTH);

		JPanel topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());
			//JPanel buttonPanel = new JPanel();
				//buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			JToolBar buttonPanel = new JToolBar();
//			buttonPanel.setRollover(true);
			buttonPanel.setFloatable(false);
				
				JButton newButton = getIconedJButton("devTools/Hawk/classDescriptor/pixmaps/new.png","New");
				newButton.setToolTipText("Create a new project");
				newButton.setActionCommand("project.new");
				newButton.addActionListener(this);
				buttonPanel.add(newButton);
				
				JButton openButton = getIconedJButton("devTools/Hawk/classDescriptor/pixmaps/stock-open.png","Open");
				openButton.setToolTipText("Open a previously saved project");
				openButton.setActionCommand("project.open");
				openButton.addActionListener(this);
				buttonPanel.add(openButton);
				
				JButton saveButton = getIconedJButton("devTools/Hawk/classDescriptor/pixmaps/filesave.png","Save");
				saveButton.setToolTipText("Save a project");
				saveButton.setActionCommand("project.save");
				saveButton.addActionListener(this);
				buttonPanel.add(saveButton);
				
				buttonPanel.addSeparator();
				
				JButton viewAlphaButton = getIconedJButton("devTools/Hawk/classDescriptor/pixmaps/view_alpha.png","View Alphabetically");
				viewAlphaButton.setToolTipText("View the project's classes and interfaces in alphabetical order");
				viewAlphaButton.setActionCommand("alphaWindow.popup");
				viewAlphaButton.addActionListener(this);
				buttonPanel.add(viewAlphaButton);
				
				JButton viewPackagesButton = getIconedJButton("devTools/Hawk/classDescriptor/pixmaps/view_packages.png","View By Package");
				viewPackagesButton.setToolTipText("View the project's classes and interfaces categorized by the package they're in");
				viewPackagesButton.setActionCommand("packageWindow.popup");
				viewPackagesButton.addActionListener(this);
				buttonPanel.add(viewPackagesButton);
				
				buttonPanel.addSeparator();
				
				JButton printButton = getIconedJButton("devTools/Hawk/classDescriptor/pixmaps/fileprint.png","Print");
				printButton.setToolTipText("Print class and interface information");
				printButton.setActionCommand("project.print");
				printButton.addActionListener(this);
				buttonPanel.add(printButton);
				
				JButton searchButton = getIconedJButton("devTools/Hawk/classDescriptor/pixmaps/stock-find.png","Search");
				searchButton.setToolTipText("Search for a class or interface");
				searchButton.setActionCommand("project.search");
				searchButton.addActionListener(this);
				buttonPanel.add(searchButton);
				
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
	
	/**
	 * This returns a JButton with the image from the location "location" on it.  If the image
	 * can not be found, a JButton with the text, "title" on it is returned instead.  This method 
	 * also gives the button a MetalBorders.ButtonBorder to make the buttons appear in a more 
	 * compact format.
	 * @param location The location to an image to place on the JButton
	 * @param title The text to put on the JButton if the image could not be found
	 * @return A JButton with an image or text on it
	 */
	public JButton getIconedJButton(String location, String title)
	{
		URL imageURL = ClassLoader.getSystemClassLoader().getResource(location);
		JButton button = new JButton();
		if (imageURL != null)
		{
			ImageIcon icon = new ImageIcon(imageURL);
			if (icon != null)
				button.setIcon(icon);//button = new JButton(icon);
			else
				button.setText(title);
		}
		else
			button.setText(title);
		
		button.setBorder(new MetalBorders.ButtonBorder());
					
		return button;
	}
	
	/**
	 * Returns true if this is the first HawkDesktop window open and false otherwise.
	 */
	public boolean isFirstWindowOpen()
	{
		return firstWindowOpened;
	}
	
	/**
	 * Sets or unsets this window as the first window open.
	 * @param bol True if the window is to be set as the first window open and false otherwise.
	 */
	public void setFirstWindowOpen(boolean bol)
	{
		firstWindowOpened = bol;
	}
	
	/**
	 * Sets the title.
	 * @param title The title.
	 */
	public void setHawkDesktopTitle(String title)
	{
		setTitle(title);
	}
	
	/**
	 * Get the names of the tabs.
	 * @return An array of Strings each of which is the name of a tab.  The element at index 
	 * "n" is the tab at index "n".
	 */
	public String[] getTabNames()
	{
		int num = tabbedPane.getTabCount();
		String[] arr = new String[num];
		
		for (int i=0; i<num; i++)
			arr[i] = tabbedPane.getTitleAt(i);
		
		return arr;
	}
	
	/**
	 * This gets the JTabbedPane in the HawkDesktop.
	 * @return The JTabbedPane.
	 */
	public JTabbedPane getTabbedPane()
	{
		return tabbedPane;
	}
	
	/**
	 * This gets the JDesktop pane from the tabbed pane that is currently visible.
	 * @return The currently visible JDesktop pane.
	 */
	public JDesktopPane getSelectedDesktop()
	{
		int num = tabbedPane.getSelectedIndex();
		if (num != -1)
			return (JDesktopPane)tabbedPane.getComponentAt(num);
		else
			return null;
	}
	
	/**
	 * Sets the selected JDesktopPane.  This basically sets selected tab 
	 * from the JTabbedPane.
	 * @param index The index of the selected tab in the JTabbedPane.
	 */
	public void setSelectedDesktop(int index)
	{
		tabbedPane.setSelectedIndex(index);
	}
	
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
		proPanel.actionPerformed(event);
	}
	
	/**
	 * Handles closing the window.  If the window is the first window opened and the user 
	 * selects to close the window, the program is shut down.  If the window is not the first 
	 * window opened, and the user selects to close the window, the window is just closed.
	 * @author Dominic Kramer
	 */
	class WindowDestroyer extends WindowAdapter
	{
		/**
		 * Handles closing the window.
		 */
		public void windowClosing(WindowEvent event)
		{
			dispose();
			if (firstWindowOpened)
				System.exit(0);
		}
	}
}