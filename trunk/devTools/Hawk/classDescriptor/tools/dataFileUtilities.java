/*
 * File:  dataFileUtilities.java
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
 * Revision 1.1  2004/02/07 05:10:48  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import devTools.Hawk.classDescriptor.gui.frame.FileAlreadyExistsGUI;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.threads.JDFFileReaderThread;
import devTools.Hawk.classDescriptor.threads.JDFFileWriterThread;

public class dataFileUtilities
{
		protected String fileName;
		protected boolean append;
		
		/**
		* Create a new default dataFileUtilities object
		*/		
		public dataFileUtilities() throws FileNotFoundException
		{
			fileName = "";
			append = false;
		}
		
		/**
		* This makes a dataFileUtilities object from a .jdf file
		* @param file the .jdf file
		*/
		public dataFileUtilities(String file, boolean ap) throws FileNotFoundException
		{
			fileName = file;
			append = ap;
		}
		
		public String getFileName()
		{
			return fileName;
		}
		
		public boolean append()
		{
			return append;
		}

		public dataFileUtilities getJDFRAF()
		{
			return this;
		}

		public void printInterfacesAndName(Vector vec, Project pro)  //this takes a vector of Interface objects if you do not pass Interface objectes in the vector you'll get a ClassCastException
		{
			JDFFileWriterThread thread = new JDFFileWriterThread(vec,pro,this);
			thread.start();
		}

	//this returns a vector of Interface objects for ALL of the objects in the file specified by infoRAF
	public Vector getVectorOfInterfaceObjects()
	{
		Vector vec = new Vector();
		
		JDFFileReaderThread thread = new JDFFileReaderThread(this,vec);
		thread.start();
		
		return vec;
	}
	
	/**
	 * This is used to save a project to a file and as the name implies, this
	 * method writes the .jdf file and writes the .inf, .ext, and .lst files.
	 * @param pro The project to save
	 * @param fileName The name of the file to save the file to
	 */
	public static void writeJDFFile(Project pro, String fileName)
	{
		String jdfFILE = fileName;
		if (!jdfFILE.endsWith(".jdf"))
			jdfFILE = jdfFILE + ".jdf";
		
		String result = "";
		if ((new File(jdfFILE)).exists())
			result = (new FileAlreadyExistsGUI()).showQuestionDialog(fileName);
		boolean append = true;
		if (result.equals("Append"))
			append = true;
		else if (result.equals("Overwrite"))
			append = false;
		
		if (!result.equals("Cancel"))
		{
			try
			{
				pro.setData(new dataFileUtilities(jdfFILE, append));
				pro.getData().printInterfacesAndName(pro.getInterfaceVec(),pro);
			}
			catch(IOException e)
			{
				SystemsManager.printStackTrace(e);
			}
		}
	}
}
