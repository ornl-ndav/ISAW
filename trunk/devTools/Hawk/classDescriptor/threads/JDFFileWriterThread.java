/*
 * File:  JDFFileWriterThread.java
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
 * Revision 1.2  2004/03/11 18:49:50  bouzekc
 * Documented file using javadoc statements.
 *
 * Revision 1.1  2004/02/07 05:10:27  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.threads;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import devTools.Hawk.classDescriptor.gui.frame.ProgressGUI;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.StatisticsManager;
import devTools.Hawk.classDescriptor.tools.SystemsManager;
import devTools.Hawk.classDescriptor.tools.dataFileUtilities;

/**
 * This class is used to write data about a Project to a file in a separate thread.  Because the 
 * data is written in another thread, the gui does not seem to freeze.
 * @author Dominic Kramer
 */
public class JDFFileWriterThread extends Thread
{
	/**
	 * This is a Vector of Interface objects.  The data from each Interface object 
	 * is written to a file.
	 */
	protected Vector vec;
	/**
	 * The window displaying the progress of this thread.
	 */
	protected ProgressGUI progress;
	/**
	 * The project whose data is to be written to a file.
	 */
	protected Project pro;
	/**
	 * This object is used to write the data.
	 */
	protected dataFileUtilities data;
	
	/**
	 * This creates a new thread in which the data about the Project supplied 
	 * will be writen to the file.
	 * @param PRO The project whose data is to be written.
	 * @param DATA The dataFileUtilities object used to write the data.
	 */
	public JDFFileWriterThread(Project PRO, dataFileUtilities DATA)
	{
		pro = PRO;
		vec = pro.getInterfaceVec();
		data = DATA;
		StatisticsManager manager = new StatisticsManager(pro);
		progress = new ProgressGUI(0,manager.getTotalNumberOfClassesAndInterfaces(),"Saving Project "+pro.getProjectName());
		progress.setVisible(true);
	}
	
	/**
	 * This method actually writes the data.  Call the start() method to start the thread.  The start() method will in turn call this 
	 * method to write the data.
	 */
	public void run()
	{
		try
		{
			DataOutputStream output = new DataOutputStream(new FileOutputStream(data.getFileName(), data.append()));
			
			output.writeUTF(pro.getProjectName()+"\n");
			
			int i = 0;
			while (i < vec.size() && !progress.isCancelled())
			{
				((Interface) (vec.elementAt(i))).println(output);
				i++;
				progress.setText("Wrote data for class "+i+"\n");
				progress.setValue(i);
			}
			
			if (!progress.isCancelled())
			{
				progress.isCompleted();
				progress.appendMessage("Done");
				progress.dispose();
			}
			
			output.close();
		}
		catch (IOException e)
		{
			SystemsManager.printStackTrace(e);
		}
		catch (ClassCastException e)
		{
			System.out.println("A ClassCastException was thrown in printlnAndWriteClass(Vector) probably because you didn't pass a vector of Interface objects to the function.");
			SystemsManager.printStackTrace(e);
		}
	}
}