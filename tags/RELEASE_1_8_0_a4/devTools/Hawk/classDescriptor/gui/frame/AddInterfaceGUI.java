/*
 * File:  AddInterface.java
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
 * Revision 1.1  2004/02/07 05:08:49  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.event.ActionListener;

import javax.swing.event.ListSelectionListener;

import devTools.Hawk.classDescriptor.gui.panel.ProjectSelectorJPanel;
import devTools.Hawk.classDescriptor.modeledObjects.Project;

/**
 * This is basically a modified version of a CreateNewProjectGUI.  This class sets the inheireted boolean value "createNewProject" to false.  Thus, instead of 
 * making a new project from a Vector of Interface objects, the Vector of Interface objects are added to the Vector of Interface objects of the 
 * original Project "pro" (which is supplied in the constructor).  Also, the title is changed to "Add Interfaces to "Project name" " and the text field holding the 
 * project's name, has pro's name set as the text.
 * @author Dominic Kramer
 */
public class AddInterfaceGUI extends CreateNewProjectGUI implements ActionListener, ListSelectionListener
{
	/**
	 * Constructor used to create a new AddInterfaceGUI.
	 * @param psjp The ProjectSelectorJPanel from which the Project was selected from.
	 * @param GUI This gui is used to display any problems that are encountered while trying to make the project.
	 * @param pro The Project object to which the Interface objects should be added to.
	 */
	public AddInterfaceGUI(ProjectSelectorJPanel psjp, UnableToLoadClassGUI GUI, Project pro)
	{
		super(psjp,GUI);
		createdProject = pro;
		nameField.setText(pro.getProjectName());
		setTitle("Add Interfaces to "+pro.getProjectName());
	}
	
	/**
	 * Returns the flag that describes if the class should make a new project.  This method 
	 * always returns false.
	 * @return True if a new project should be created and false otherwise.
	 */
	public boolean createNewProject()
	{
		return false;
	}
}