/*
 * File:  Project.java
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
 * Revision 1.1  2004/02/07 05:10:06  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.modeledObjects;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import devTools.Hawk.classDescriptor.tools.SystemsManager;
import devTools.Hawk.classDescriptor.tools.dataFileUtilities;

public class Project
{
	private dataFileUtilities data;
	private Vector interfaceVec; //this is a vector of Interface objects that currently have
				     //been selected by the user.  The one at the end of the 
				     //vector is the most recent one selected.
	private String projectName; //the project's name
	private int currentInterface;  //this int represents the element in the Vector interfaceVec
				     //that the user is currently looking at
	
	public Project()
	{
		try
		{
			data = new dataFileUtilities();
			//interfaceVec = data.getVectorOfInterfaceObjects();
			interfaceVec = new Vector();
			projectName = "project";
			currentInterface = 0;
		}
		catch(FileNotFoundException e)
		{
			System.out.println("A FileNotFoundException was thrown in Project() in Project.java");
		}
	}
	
	public Project(dataFileUtilities Data, String Str)
	{
		data = Data;
		interfaceVec = data.getVectorOfInterfaceObjects();
		projectName = Str;
		currentInterface = 0;
	}
	
	public Project(String Str1, String name, boolean append) throws FileNotFoundException
	{
		data = new dataFileUtilities(Str1, append);
		interfaceVec = data.getVectorOfInterfaceObjects();
		projectName = name;
		currentInterface = 0;
	}
	
	public String toString()
	{
		return projectName;
	}
	
	public dataFileUtilities getData()
	{
		return data;
	}
	
	public void setData(dataFileUtilities data2)
	{
		data = data2;
	}
	
	public Vector getInterfaceVec()
	{
		return interfaceVec;
	}	
	
	public void setInterfaceVec(Vector vec)
	{
		interfaceVec = vec;
	}
	
	public void addInterfaceToInterfaceVec(Interface intF)
	{
		interfaceVec.add(intF.Clone());
	}
	
	public int getCurrentInterface()
	{
		return currentInterface;
	}
	
	public void setCurrentInterface(int num)
	{
		currentInterface = num;
	}
	
	public String getProjectName()
	{
		return projectName;
	}

	/**
	* Returns the Project's name from a .jdf file
	* @param str The .jdf filename
	* @return The project's name represented by the .jdf file
	*/
	public static String getProjectName(String str)
	{
		String answer = "";
		try
		{
			DataInputStream reader = new DataInputStream(new FileInputStream(str));
			answer = reader.readUTF();
			reader.close();
		}
		catch(IOException e)
		{
			System.out.println("IOException thrown in getNames() in dataFileUtilities.java");
			SystemsManager.printStackTrace(e);
		}
			
		return answer;
	}
	
	public void setProjectName(String nm)
	{
		projectName = nm;
	}


	/**
	 * This creates a new Project object from a vector of full filenames.
	 * @param vec A vector of filenames either corresponding to a directory,
	 * .class file, or .jar file.  The filenames are full filenames.
	 * @param name The project's name
	 * @return The newly created Project
	 */
/*
	public Project createNewProjectFromFileNames(Vector vec, String name)
	{
		Project resultPro = new Project();
		projectName = name;
		
		//now to fill out interfaceVec
			Vector inVec = new Vector();
			
			for (int i=0; i<=vec.size(); i++)
			{
				Vector tempVec = new Vector();
				(new FileReflector()).ProcessDirectoryOrFile( (String)vec.elementAt(i), tempVec);
				
				for (int j=0; j<tempVec.size(); j++)
				{
					inVec.add((Interface)tempVec.elementAt(j));
				}
			}
			
			interfaceVec = inVec;
			currentInterface = 0;
		String tempFile = SystemsManager.getClassDescriptorHomeDirectory()+System.getProperty("file.separator")+"temporary"+System.getProperty("file.separator")+projectName;
		dataFileUtilities.writeJDFFile(resultPro, tempFile);
		return resultPro;
	}
*/
	/**
	 * This creates a new Project object from a vector of Interface objects.
	 * @param vec A vector of Interfaces.
	 * @param name The project's name
	 * @return The newly created Project
	 */
	public static Project createNewProjectFromInterfaces(Vector vec, String name)
	{
		//this creates a Project object with all of the default values
		Project resultPro = new Project();
		
		//this overrides some of the default values with specific values
			resultPro.setProjectName(name);
			resultPro.setInterfaceVec(vec);
				
		//The projects files are not written until the user saves the project
			//String tempFile = SystemsManager.getClassDescriptorHomeDirectory()+System.getProperty("file.separator")+"temporary"+System.getProperty("file.separator")+projectName;
			//dataFileUtilities.writeJDFAndCorrespondingFiles(resultPro, tempFile);
		return resultPro;
	}	
}