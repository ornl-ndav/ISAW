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
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HTMLPrintThread extends Thread
{
	protected PrintGUI printGUI;
	protected ProgressGUI progress;
	protected int maxSize;
	
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
