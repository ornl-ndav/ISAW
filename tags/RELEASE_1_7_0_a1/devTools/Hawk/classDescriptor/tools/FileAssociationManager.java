/*
 * File:  FileAssociationManager.java
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
 * Revision 1.3  2004/03/12 19:46:19  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:10:45  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;

/**
 * This class handles associating Interface objects with their appropriate javadocs and/or 
 * source code files.
 * @author Dominic Kramer
 */
public class FileAssociationManager
{
	/**
	 * Constant passed to the constructor to specify that the manager is to associate 
	 * javadocs files.
	 */
	public static final int JAVASOURCE = 1;
	/**
	 * Constant passed to hte constructor to specify that the manager is to assocaite 
	 * source code files.
	 */
	public static final int JAVADOCS = 2;
	
	/**
	 * A Vector of Interface objects
	 */
	private Vector intFVector;
	/**
	 * The type of file that this manager is supposed to work with.  Either JAVASOURCE or 
	 * JAVADOCS.
	 */
	private int fileType;
	
	/**
	 * Create a FileAssociationManager to associate the files to the Vector of Interface objects 
	 * supplied.
	 * @param vec The Vector of Interface bojects to associate the files to.
	 * @param type The type of files to associate (either JAVASOURCE or JAVADOCS).
	 */
	public FileAssociationManager(Vector vec, int type)
	{
		intFVector = vec;
		fileType = type;
	}
	
	/**
	 * Create a FileAssociationManager to associate the files to the Interface objects from the Project 
	 * object supplied.
	 * @param pro The project whose interfaces are going to have file associated with them.
	 * @param type The type of files to associate (either JAVASOURCE or JAVADOCS).
	 */
	public FileAssociationManager(Project pro, int type)
	{
		intFVector = pro.getInterfaceVec();
		fileType = type;
	}
	
	/**
	 * Create a FileAssociationManager to associate the files to Interface object supplied.
	 * @param intF The interface to associate the files to.
	 * @param type The type of files to associate (either JAVASOURCE or JAVADOCS).
	 */
	public FileAssociationManager(Interface intF, int type)
	{
		Vector tempVector = new Vector();
		tempVector.add(intF);
		intFVector = tempVector;
		fileType = type;
	}
	
	/**
	 * Get the filetype that this manager is associating with the interfaces.  It will either 
	 * correspond to javadocs files or source code files.
	 * @return Either JAVADOCS or JAVASOURCE.
	 */
	public int getFileType()
	{
		return fileType;
	}
	
	/**
	 * This method takes a filename and determines if it is the type of file that this manager is 
	 * supposed to handle.
	 * @param str The filename
	 * @return True if the file is the correct type.
	 */
	public boolean isFileType(String str)
	{
		boolean answer = false;
		
		if (str.endsWith(".java") && fileType==FileAssociationManager.JAVASOURCE)
			answer = true;
		else if (fileType == FileAssociationManager.JAVADOCS)
		{
			if (str.endsWith(".html") || str.endsWith(".htm"))
				answer = true;
		}
		
		return answer;
	}
	
	/**
	 * This takes a filename for a .class file and gets the shortened name for a class.  For instance if the filename 
	 * is /home/person/package1/package2/demoGUI.class this class will return demoGUI.
	 * @param fileName The filename to use.
	 * @return The shortened name for the class specified by the filename supplied.
	 */
	public String getShortenedClassName(String fileName)
	{
		String answer = "";
		
		StringTokenizer tokenizer = new StringTokenizer(fileName, System.getProperty("file.separator"));
		while (tokenizer.hasMoreTokens())
			answer = tokenizer.nextToken();
			
		//now answer is in the form <shortened class name>.java for java source files
		//     or <shortened class name>.html or <shortened class name>.htm for javadocs files
		
		if (fileType==FileAssociationManager.JAVASOURCE)
			answer = answer.substring(0, answer.length()-5);
		else if (fileType==FileAssociationManager.JAVADOCS)
		{
			if (answer.endsWith(".html"))
				answer = answer.substring(0, answer.length()-5);
			else if (answer.endsWith(".htm"))
				answer = answer.substring(0, answer.length()-4);
		}
		
		return answer;
	}
	
	/**
	 * This method associates the file "fileName" to the Interface object (if it exists) whose shortened name 
	 * is "shortClassName".  The method looks for a match from the Interface objects in 
	 * the Vector intFVector.
	 * @param fileName The filename to associate.
	 * @param shortClassName The shortened classname of the class or interface to associate the file 
	 * with.
	 */
	public void associateFile(String fileName, String shortClassName)
	{
		Interface currentInterface = new Interface();
		boolean found = false;
		int i=0;
		while (i<intFVector.size() && !found)
		{
			currentInterface = (Interface)(intFVector.elementAt(i));
			if (currentInterface.getPgmDefn().getInterface_name(true,true).equals(shortClassName))
			{
				found = true;
				if (fileType == FileAssociationManager.JAVADOCS)	
					currentInterface.setJavadocsFileName(fileName);
				else if (fileType == FileAssociationManager.JAVASOURCE)
					currentInterface.setSourceFileName(fileName);
			}
			i++;
		}
	}
	
	/**
	 * If the filename supplied to this method is a file, this method associates the file to the correct 
	 * interface by seeing if any Interface object has the same shortened name as the shortened name 
	 * from the file (see getShortenedClassName(String str)).  If the filename corresponds to a directory 
	 * this method recursively scans through the directory and associates the files.  This method uses 
	 * the method associateFile(String fileName, String shortClassName) to associate the files.
	 * @param Dir The filename of the file or directory to process.
	 */
	public void ProcessDirectoryOrFile(String Dir)
	{
			File dirFile = new File(Dir);
			 File[] F;
//			 F = new File[0];

			if (dirFile.isDirectory())	
				 F = dirFile.listFiles();
			else
			 {
				F = new File[1];
				F[0] = dirFile;
			 }
			 
			 for( int i  =  0;  i  <  F.length;  i++)
			 {
				 if( F[i].isDirectory())
				 {
					 ProcessDirectoryOrFile( F[i].getAbsolutePath());
				}
				else if ( F[i].isFile() && isFileType(F[i].getAbsolutePath()) )
				{
					String className = getShortenedClassName(F[i].getPath());
					associateFile(F[i].getAbsolutePath(), className);
				}
			}
		}
}
