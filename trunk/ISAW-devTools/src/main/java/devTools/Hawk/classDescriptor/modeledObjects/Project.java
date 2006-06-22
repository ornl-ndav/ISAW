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
 * Revision 1.4  2004/06/04 23:44:16  kramer
 * Fixed some Javadoc errors.
 *
 * Revision 1.3  2004/03/12 19:46:18  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:10:06  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.modeledObjects;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.util.Vector;

import javax.swing.JOptionPane;

import devTools.Hawk.classDescriptor.gui.frame.FileAlreadyExistsGUI;
import devTools.Hawk.classDescriptor.threads.JDFFileReaderThread;
import devTools.Hawk.classDescriptor.threads.JDFFileWriterThread;
import devTools.Hawk.classDescriptor.tools.SystemsManager;
import devTools.Hawk.classDescriptor.tools.dataFileUtilities;

/**
 * This class is used to describe a project.  A project is basically a collection of classes and/or interfaces.
 * @author Dominic Kramer
 *
 */
public class Project
{
	/** This is the object used to handle saving the project or opening the project from a file. */
	private dataFileUtilities data;
	/** The Vector of Interface objects each of which represents a class or interface in this project. */
	private Vector interfaceVec; //this is a vector of Interface objects that currently have
				                              //been selected by the user.  The one at the end of the 
				                              //vector is the most recent one selected.
	/** The project's name. */
	private String projectName; //the project's name

//	private int currentInterface;  //this int represents the element in the Vector interfaceVec
				     //that the user is currently looking at
	
	/** Default constructor. */
	public Project()
	{
			data = new dataFileUtilities();
			data.setAlreadySaved(false);
			//interfaceVec = data.getVectorOfInterfaceObjects();
			interfaceVec = new Vector();
			projectName = "project";
	}
	
	/**
	 * Create a Project object given the paramters.  The constructor considers the project as not being 
	 * already saved.
	 * @param Data The dataFileUtilities object which is used for opening and saving the project to a file.
	 * @param Str The projec'ts name.
	 */
	public Project(dataFileUtilities Data, String Str)
	{
		data = Data;
		interfaceVec = Project.getVectorOfInterfaceObjectsFromFile(data.getFileName());
		data.setAlreadySaved(true);
		projectName = Str;
//		currentInterface = 0;
	}
	
	/**
	 * Creates a project from a native Hawk file.
	 * @param Str1 The full filename of the native Hawk file.
	 * @param append True if data is to be appened to this file or not if data is written to the file.
	 * @throws FileNotFoundException
	 */
	public Project(String Str1, boolean append)
	{
		data = new dataFileUtilities(Str1, append);
		data.setAlreadySaved(true);
		projectName = "";
		try
		{
			projectName = Project.getProjectNameFromFile(Str1);
			interfaceVec = Project.getVectorOfInterfaceObjectsFromFile(Str1);
		}
		catch (UTFDataFormatException e)
		{
			JOptionPane opPane = new JOptionPane();
			JOptionPane.showMessageDialog(opPane,"The file "+Str1+" could not be read because its data has been corrupted.\n" +
				"Make sure it is a "+SystemsManager.getHawkFileExtensionWithoutPeriod()+" file written by Hawk and not just " +
					"a file that ends in "+SystemsManager.getHawkFileExtensionWithoutPeriod()+"."
				,"File Not Found"
				,JOptionPane.ERROR_MESSAGE);
				if (projectName.equals(""))
					projectName = "Name could not be resolved";
		}
		catch (EOFException e)
		{
			JOptionPane opPane = new JOptionPane();
			JOptionPane.showMessageDialog(opPane,"The file "+Str1+" could not be read because its data has been corrupted.\n" +
				"Make sure it is a "+SystemsManager.getHawkFileExtensionWithoutPeriod()+" file written by Hawk and not just " +
					"a file that ends in "+SystemsManager.getHawkFileExtensionWithoutPeriod()+"."
				,"File Not Found"
				,JOptionPane.ERROR_MESSAGE);
			if (projectName.equals(""))
				projectName = "Name could not be resolved";
		}
		catch(IOException e)
		{
			System.out.println("IOException thrown in getProjectNameFromFile(String) in Project.java");
			SystemsManager.printStackTrace(e);
		}
	}
	
	/**
	 * Returns the project's name.  If a project is added to a JTree, the JTree will use this method to determine 
	 * the string to place on the node in the tree.
	 * @return The project's name.
	 */
	public String toString()
	{
		return projectName;
	}
	
	/**
	 * Get the dataFileUtilities object associated with this project object.
	 * @return The dataFileUtilities object associated with this project object.
	 */
	public dataFileUtilities getData()
	{
		return data;
	}
	
	/**
	 * Set the dataFileUtilities object associated with this object.
	 * @param data2 The new dataFileUtilities object to associate with this 
	 * project object.
	 */
	public void setData(dataFileUtilities data2)
	{
		data = data2;
	}
	
	/**
	 * Get the Interface objects associated with this project object.
	 * @return A Vector of Interface objects.
	 */
	public Vector getInterfaceVec()
	{
		return interfaceVec;
	}	
	
	/**
	 * Set the Interface objects associated with this project object.
	 * @param vec A Vector of Interface objects.
	 */
	public void setInterfaceVec(Vector vec)
	{
		interfaceVec = vec;
	}
	
	/**
	 * Adds the supplied Interface object to the Vector of Interface objects this 
	 * project holds.
	 * @param intF The Interface object to add.
	 */
	public void addInterfaceToInterfaceVec(Interface intF)
	{
		interfaceVec.add(intF.getClone());
	}
/*	
	public int getCurrentInterface()
	{
		return currentInterface;
	}
	
	public void setCurrentInterface(int num)
	{
		currentInterface = num;
	}
*/
	/**
	 * Get the project's name.
	 * @return The project's name.
	 */
	public String getProjectName()
	{
		return projectName;
	}
	
	/**
	 * Set the projec'ts name.
	 * @param nm The project's new name.
	 */
	public void setProjectName(String nm)
	{
		projectName = nm;
	}
	
	/*
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
	
	/**
	 * This is used to save a project to a file and as the name implies, this
	 * method writes the native Hawk file.  This method prompts the user 
	 * if he/she wants to overwrite the file, append to the file, or cancel saving 
	 * the project it the file the user selects to save to already exists.
	 */
	public void writeNativeHawkFile()
	{
		String jdfFILE = getData().getFileName();
		if (!jdfFILE.endsWith(SystemsManager.getHawkFileExtension()))
			jdfFILE = jdfFILE + SystemsManager.getHawkFileExtension();
		
		String result = "";
		if ((new File(jdfFILE)).exists())
			result = (new FileAlreadyExistsGUI()).showQuestionDialog(jdfFILE);
		boolean append = true;
		if (result.equals("Append"))
			append = true;
		else if (result.equals("Overwrite"))
			append = false;
		
		if (!result.equals("Cancel"))
		{
				dataFileUtilities data = new dataFileUtilities(jdfFILE, append);
				setData(data);
				printInterfacesAndName();
				data.setAlreadySaved(true);
		}
	}
	
	/**
	 * This is used to save a project to a file and as the name implies, this
	 * method writes the native Hawk file.  This method automatically 
	 * overwrites the file the user selects with the new project data.  It 
	 * is intended to be used when the user selects "save" from a menu.
	 */
	public void writeNativeHawkFileWithoutPrompting()
	{
		printInterfacesAndName();
		getData().setAlreadySaved(true);
	}
	
	/**
	 * Invokes a new JDFFileWriterThread.  The writer is set not 
	 * to append data to the end of the file.
	 */
	private void printInterfacesAndName()
	{
		dataFileUtilities data = getData();
		data.setAppend(false);
		JDFFileWriterThread thread = new JDFFileWriterThread(this,data);
		thread.start();
	}
	
	/**
	* Returns the Project's name from a Hawk native file specified from the string fileName.
	* @return The project's name represented by the hawk native file.
	*/
	public static String getProjectNameFromFile(String fileName) throws UTFDataFormatException, IOException
	{
		String answer = "";
		try
		{
			DataInputStream reader = new DataInputStream(new FileInputStream(fileName));
			answer = reader.readUTF();
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			JOptionPane opPane = new JOptionPane();
			JOptionPane.showMessageDialog(opPane,"The file "+fileName+" does not exist."
				,"File Not Found"
				,JOptionPane.ERROR_MESSAGE);
		}
			
		return answer;
	}
	
	/**
	 * This returns a Vector of Interface objects made from the data from the 
	 * native Hawk file specified by the field fileName.
	 * @return A Vector of Interface objects.
	 */
	public static Vector getVectorOfInterfaceObjectsFromFile(String filename)
	{
		Vector vec = new Vector();
		
		JDFFileReaderThread thread = new JDFFileReaderThread(filename,vec);
		thread.start();
		
		return vec;
	}
}