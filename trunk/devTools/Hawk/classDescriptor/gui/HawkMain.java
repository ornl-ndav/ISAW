/*
 * File:  HawkMain.java
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
 * Revision 1.1  2004/02/07 05:08:08  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui;

import java.util.Vector;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.tools.CommandLineManager;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * This is the main class which contains the main method used to start Hawk.
 * @author Dominic Kramer
 *
 */
public class HawkMain
{
	/**
	 * This constructor is private to signify that HawkMain doesn't need to be instantiated.
	 */
	private HawkMain()
	{}
	
	/**
	 * The main method that is used to start all of Hawk.
	 * @param args The array of Strings passed as arguments to the main method.
	 */
	public static void main(String args[])
	{
		//now to check for any command line arguments
			CommandLineManager commandManager = new CommandLineManager(args);
			int result = commandManager.parseCommandLine();

			try
			{
				if (result == CommandLineManager.SHOW_GUI)
				{
					boolean show = SystemsManager.startClassDescriptor();
					
					if (show)
					{
						Vector proVec = commandManager.getCommandLineProjectVector();
						HawkDesktop gui = null;
						
						if (proVec.size() == 0)
							gui = new HawkDesktop(null,true);
						else
							gui = new HawkDesktop(proVec,true);
							
						gui.setVisible(true);
					}
					else
						System.exit(0);
				}
			}
			catch(InternalError e)
			{
				System.out.println("Hawk could not be started because the windowing system could not be contacted.");
			}
	}
}