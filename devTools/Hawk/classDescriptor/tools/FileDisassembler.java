/*
 * File:  FileDisassembler.java
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
 * Revision 1.1  2004/03/12 19:48:55  bouzekc
 * Added to CVS.
 *
 */
 package devTools.Hawk.classDescriptor.tools;

import devTools.Hawk.classDescriptor.gui.frame.UnableToLoadClassGUI;

/**
 * This class uses the javap command gain information about .class files.  Javap is the 
 * java class file disassembler.  This class is still under construction.
 * @author Dominic Kramer
 */
public class FileDisassembler
{
	/**
	 * This is the window that contains any errors (that cannot be resolved) that may occur 
	 * when the classes are loaded.
	 */
	private UnableToLoadClassGUI gui;
	
	public FileDisassembler()
	{
		gui = new UnableToLoadClassGUI();	
	}
	
	public FileDisassembler(UnableToLoadClassGUI GUI)
	{
		gui = GUI;
	}
}
