/*
 * File:  LoadClassThread.java
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
 * Revision 1.2  2004/03/11 18:50:18  bouzekc
 * Documented file using javadoc statements.
 * Added field selectedProject which holds a reference to the Project object to
 * add the Interface objects created to.
 *
 * Revision 1.1  2004/02/07 05:10:27  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.threads;

import java.util.Vector;

import devTools.Hawk.classDescriptor.gui.frame.CreateNewProjectGUI;
import devTools.Hawk.classDescriptor.gui.frame.ProgressGUI;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;

/**
 * This class is used with the class CreateNewProjectGUI.  This class takes .class and .jar files 
 * from the CreateNewProjectGUI object's JList and loads them to get information about the class 
 * specified to make Interface objects.  This class loads these files in a separate thread so the gui 
 * doesn't seem to freeze.
 * @author Dominic Kramer
 */
public class LoadClassThread extends Thread
{
	/**
	 * The CreateNewProjectGUI object associated with this thread.
	 */
	protected CreateNewProjectGUI gui;
	/**
	 * The window showing the progress of this thread.
	 */
	protected ProgressGUI progress;
	protected Project selectedProject;
	
	/**
	 * This creates a new thread in which the .class and .jar files are loaded to make Interface 
	 * objects.
	 * @param GUI
	 */
	public LoadClassThread(Project pro, CreateNewProjectGUI GUI)
	{
		gui = GUI;
		selectedProject = pro;
		
		progress = new ProgressGUI(0,gui.numberOfClassesToLoad(),"Loading the Classes");
		progress.setVisible(true);
		
		System.out.println("#="+gui.numberOfClassesToLoad());
	}
		
	/**
	 * This takes a filename from fileVector from the CreateNewProjectGUI and convertes 
	 * it into an Interface and puts it in the Vector interfaceVec.  This method is called when 
	 * you call the start() method to start the thread.
	 */
	public void run()
	{		
		Vector fileVec = new Vector();
		for (int i=0; i<gui.getModel().size(); i++)
			fileVec.add((String)gui.getModel().get(i));
		
		Vector tempVec = gui.getFileReflector().getVectorOfInterfaces(fileVec,progress);
		for (int i=0; i<tempVec.size(); i++)
			gui.getVectorOfInterfacesCreated().add((Interface)tempVec.elementAt(i));
		
		gui.getCreatedProject().setInterfaceVec(gui.getVectorOfInterfacesCreated());
		gui.getCreatedProject().setProjectName(gui.getTextField().getText());

		if (gui.createNewProject())
		{
			if ( gui.getProjectSelectorJPanel().noProjectsListed())
			{
				gui.getProjectSelectorJPanel().getModel().remove(0);
			}

			gui.getProjectSelectorJPanel().getModel().addElement(gui.getCreatedProject());
			gui.getProjectSelectorJPanel().setNoProjectsListed(false);
		}
		else
			selectedProject.setProjectName(gui.getCreatedProject().getProjectName());
				
		gui.disposeAndShowErrorBox();
		if (!progress.isCancelled())
		{
			progress.appendMessage("Done");
			progress.isCompleted();
			progress.dispose();
		}
	}
}
