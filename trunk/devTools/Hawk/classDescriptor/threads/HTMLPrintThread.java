/*
 * File:  HTMLPrintThread.java
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
 * Revision 1.2  2004/03/11 18:42:55  bouzekc
 * Documented file using javadoc statements.
 *
 * Revision 1.1  2004/02/07 05:10:26  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.threads;

import java.util.Vector;

import devTools.Hawk.classDescriptor.gui.frame.PrintGUI;
import devTools.Hawk.classDescriptor.gui.frame.ProgressGUI;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.HTMLPrintFileManager;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * This class is used to write data about a project to a file in HTML format in 
 * a separate thread.  There is a PrintGUI associated with this class.  It uses this 
 * PrintGUI to decide what Interface objects it is supposed to write along with 
 * how it is supposed to write the data.  Because the information is written in a 
 * separate thread using this class, the GUI does not seem to freeze while the data is 
 * being written.
 * @author Dominic Kramer
 */
public class HTMLPrintThread extends Thread
{
	/**
	 * The PrintGUI associated with this thread.
	 */
	protected PrintGUI printGUI;
	/**
	 * The window displaying the progress of the thread.
	 */
	protected ProgressGUI progress;
	/**
	 * Used to determine the maximum value of the JProgressBar in the 
	 * ProgressGUI.
	 */
	protected int maxSize;
	
	/**
	 * Creates a new HTMLPrintThread object.
	 * @param PRINTGUI The PrintGUI associated with this thread.
	 */
	public HTMLPrintThread(PrintGUI PRINTGUI)
	{
		super();
		printGUI = PRINTGUI;
		maxSize = 0;
		if (printGUI.getUseIntroCheckBox().isSelected())
			maxSize++;
		if (printGUI.getUseContentsCheckBox().isSelected())
			maxSize++;
		if (printGUI.getUsePackageListCheckBox().isSelected())
			maxSize++;
		if (printGUI.getUseAlphaUMLCheckBox().isSelected())
			maxSize = maxSize + printGUI.getModel().size();
		if (printGUI.getUseShortenedSourceCheckBox().isSelected())
			maxSize = maxSize + printGUI.getModel().size();
			
		progress = new ProgressGUI(0, maxSize, "Printing In Progress");
		progress.setVisible(true);
	}
	
	/**
	 * This method actually does the work of writing the data.  However, do not directly 
	 * call this method.  Instead call the method start() which will in turn call this method.
	 */
	public void run()
	{
		try
		{
			HTMLPrintFileManager manager =
				new HTMLPrintFileManager(
					printGUI.getFileField().getText().trim(),
					"rw");

			Vector vec = printGUI.getVectorOfSelectedInterfaces();
			progress.setMaximum(vec.size());
			for (int i = 0; i < vec.size(); i++)
			{
				manager.printUML((Interface)vec.elementAt(i),true,true);
				manager.writeBytes("<br>");
				progress.setValue(i);
			}

			manager.close();
			progress.dispose();
		}
		catch (Exception e)
		{
			SystemsManager.printStackTrace(e);
		}
	}
}
