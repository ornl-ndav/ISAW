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
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JDFFileWriterThread extends Thread
{
	protected Vector vec;
	protected ProgressGUI progress;
	protected Project pro;
	protected dataFileUtilities data;
	
	public JDFFileWriterThread(Vector VEC, Project PRO, dataFileUtilities DATA)
	{
		vec = VEC;
		pro = PRO;
		data = DATA;
		StatisticsManager manager = new StatisticsManager(pro);
		progress = new ProgressGUI(0,manager.getTotalNumberOfClassesAndInterfaces(),"Saving Project "+pro.getProjectName());
		progress.setVisible(true);
	}
	
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
		}
	}
}