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
 * Revision 1.1  2004/02/07 05:09:15  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.internalFrame;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.gui.panel.PackageTreeJPanel;
import devTools.Hawk.classDescriptor.modeledObjects.Project;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PackageTreeGUI extends DesktopInternalFrame implements ActionListener
{
	protected PackageTreeJPanel treeJPanel;
	
	public PackageTreeGUI(Project pro, boolean packageShortJava, boolean packageShortOther, boolean classShortJava, boolean classShortOther, HawkDesktop desk)
	{
		super(desk);
		
		setTitle(pro.getProjectName());
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
		//mainPanel.add(treeJPanel, BorderLayout.CENTER);
		pane.add(treeJPanel);
		
		menuBar = treeJPanel.getJMenuBar();
		refreshMoveAndCopyMenu();
		windowMenu.addMenuListener(new WindowMenuListener(this,menuBar,windowMenu));
		menuBar.add(windowMenu);
			
		setJMenuBar(menuBar);
		pack();
	}
	
	public DesktopInternalFrame getCopy()
	{
		return new PackageTreeGUI(treeJPanel.getProject(),treeJPanel.getShortenPackageJavaBox().isSelected(),treeJPanel.getShortenPackageOtherBox().isSelected(),treeJPanel.getShortenClassJavaBox().isSelected(), treeJPanel.getShortenClassOtherBox().isSelected(),desktop);
	}
	
	public void actionPerformed(ActionEvent event)
	{
		PackageTreeGUI copy = (PackageTreeGUI)getCopy();
		copy.setVisible(true);
		processWindowChange(event,copy,this);
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
