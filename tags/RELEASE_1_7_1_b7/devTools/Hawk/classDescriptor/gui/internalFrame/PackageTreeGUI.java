/*
 * File:  PackageTreeGUI.java
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
 * Revision 1.4  2004/05/26 19:51:49  kramer
 * Added the method determineWaitingComponents().
 *
 * Revision 1.3  2004/03/12 19:46:16  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:09:15  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.internalFrame;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.gui.panel.PackageTreeJPanel;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;

/**
 * This is a special type of JInternalFrame that displays classes in a JTree.  Packages are 
 * displayed in the nodes under the root node.  Under each package node are the nodes that 
 * contain the classes and interfaces in the package.
 * @author Dominic Kramer
 */
public class PackageTreeGUI extends DesktopInternalFrame implements ActionListener
{
	/**
	 * The panel which displays a JTree and all of the packages and classes and interfaces.
	 */
	protected PackageTreeJPanel treeJPanel;
	
	/**
	 * Creates a new PackageTreeGUI.
	 * @param pro The Project whose data is to be written.
	 * @param packageShortJava Set this to true if you want package names to be shortened if it is 
	 * a java name.
	 * @param packageShortOther Set this to true if you want packages names to be shortened if it is 
	 * a non-java name.
	 * @param title The window's title.
	 * @param classShortJava Set this to true if you want class names to be shortened if they are 
	 * java names.
	 * @param classShortOther Set this to true if you want class names to be shortened if they are 
	 * non-java names.
	 * @param desk The HawkDesktop that this window this is on.
	 */
	public PackageTreeGUI(Project pro, String title, boolean packageShortJava, boolean packageShortOther, boolean classShortJava, boolean classShortOther, HawkDesktop desk)
	{
		super(desk,desk.getSelectedDesktop(),new Interface(),false,false,false,false);
		
		setTitle(title);
		setLocation(0,0);
		setSize(200,200);
		setClosable(true);
		setIconifiable(true);
		setMaximizable(true);
		setResizable(true);
		
		Container pane = getContentPane();
		//JPanel mainPanel = new JPanel();
		//mainPanel.setLayout(new BorderLayout());
		treeJPanel = new PackageTreeJPanel(pro, this, packageShortJava, packageShortOther, classShortJava, classShortOther,desktop);
		treeJPanel.setLayout(new GridLayout(1,1));
		//mainPanel.add(treeJPanel, BorderLayout.CENTER);
		pane.add(treeJPanel);
		
		menuBar = treeJPanel.getJMenuBar();
//		refreshMoveAndCopyMenu();
//		windowMenu.addMenuListener(new WindowMenuListener(this,menuBar,windowMenu));
		menuBar.add(windowMenu);
			
		setJMenuBar(menuBar);
		pack();
	}
	
	/**
	 * Gets a copy of this window.
	 * @return A copy of this window.
	 */
	public AttachableDetachableFrame getCopy()
	{
		return new PackageTreeGUI(treeJPanel.getProject(),getTitle(),treeJPanel.getShortenPackageJavaBox().isSelected(),treeJPanel.getShortenPackageOtherBox().isSelected(),treeJPanel.getShortenClassJavaBox().isSelected(), treeJPanel.getShortenClassOtherBox().isSelected(),desktop);
	}
	
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
//		PackageTreeGUI copy = (PackageTreeGUI)getCopy();
//		copy.setVisible(true);
//		processWindowChange(event,copy,this);
		super.actionPerformed(event);
	}
	
	/**
	 * The Components in the array returned from this method are the Components that should have the 
	 * mouse use the waiting animation when an operation is in progress.
	 */
	public Component[] determineWaitingComponents()
	{
		return treeJPanel.determineWaitingComponents();
	}

/*
	public void menuCanceled(MenuEvent e)
	{}
	public void menuDeselected(MenuEvent e)
	{}
	public void menuSelected(MenuEvent e)
	{
		refreshMoveAndCopyMenu(menuBar,windowMenu);
	}
*/
}
