/*
 * File:  FindClassThread.java
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
 * Revision 1.1  2004/02/07 05:10:26  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.threads;

import java.util.Vector;

import devTools.Hawk.classDescriptor.gui.frame.CreateNewProjectGUI;
import devTools.Hawk.classDescriptor.gui.frame.ProgressGUI;
import devTools.Hawk.classDescriptor.tools.FileReflector;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FindClassThread extends Thread
{
	protected CreateNewProjectGUI gui;
	
	protected ProgressGUI progress;
	
	public FindClassThread(CreateNewProjectGUI GUI)
	{
		gui = GUI;
		
		progress = new ProgressGUI(0,100,"Finding .jar and .class Files");
		progress.setText("Locating .class and .jar files . . . .\n");
		progress.setIndeterminante(true);
	}
	
	public void run()
	{
		Vector fileVec = new Vector();
		
		fileVec = FileReflector.getVectorOfInterfacesGUI(fileVec,progress);
		
		progress.setIndeterminante(false);
		progress.setValue(0);
		progress.setMaximum(fileVec.size());
		progress.setString(null);
		progress.appendMessage("Adding names to list . . . .\n");
		
		//gui.getModel().removeAllElements();
		int i=0;
		while (i<fileVec.size() && !progress.isCancelled())
		{
			try
			{
				gui.getModel().addElement((String)fileVec.elementAt(i));
				progress.setValue(i+1);
			}
			catch(NullPointerException e)
			{
				System.out.println("A NullPointerException was thrown in actionPerformed(ActionEvent) in CreateNewProjectGUI");
				System.err.println(e);
			}
			
			i++;
		}
		
		if (!progress.isCancelled())
		{
			progress.appendMessage("Done");
			progress.isCompleted();
			progress.dispose();
		}
	}
}
