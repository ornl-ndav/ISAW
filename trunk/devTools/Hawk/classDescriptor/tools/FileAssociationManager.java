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
 * Revision 1.4  2004/05/26 20:48:27  kramer
 * Gave the manager the ability to be able to "remember" how it associated the
 * files.
 *
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
	 * A Vector of LoggedInterface objects
	 */
	private Vector loggedIntFVector;
	/**
	 * The type of file that this manager is supposed to work with.  Either JAVASOURCE or 
	 * JAVADOCS.
	 */
	private int fileType;
	
	/**
	 * Create a FileAssociationManager to associate the files to the Vector of Interface objects 
	 * supplied.
	 * @param vec The Vector of Interface ojects to associate the files to.
	 * @param type The type of files to associate (either JAVASOURCE or JAVADOCS).
	 */
	public FileAssociationManager(Vector vec, int type)
	{
		loggedIntFVector = new Vector();
		for (int i=0; i<vec.size(); i++)
			loggedIntFVector.add(new LoggedInterface((Interface)vec.elementAt(i)));
		
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
		fileType = type;
		Vector vec = pro.getInterfaceVec();
		loggedIntFVector = new Vector(vec.size());
		for (int i=0; i<vec.size(); i++)
			loggedIntFVector.add(new LoggedInterface((Interface)vec.elementAt(i)));
	}
	
	/**
	 * Create a FileAssociationManager to associate the files to Interface object supplied.
	 * @param intF The interface to associate the files to.
	 * @param type The type of files to associate (either JAVASOURCE or JAVADOCS).
	 */
	public FileAssociationManager(Interface intF, int type)
	{
		fileType = type;
		loggedIntFVector = new Vector(1);
		loggedIntFVector.add(new LoggedInterface(intF));
	}
	
	/**
	 * Get the Vector of LoggedInterface objects used when associating files.
	 */
	public Vector getLoggedInterfaceVector()
	{
		return loggedIntFVector;
	}

	/**
	 * Set the Vector of LoggedInterface objects used when associating files.
	 */
	public void setLoggedInterfaceVector(Vector vec)
	{
		loggedIntFVector = vec;
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
	 * This takes a filename for a file and gets the shortened name for a class.  For instance if the filename 
	 * is /home/person/package1/package2/demoGUI.java or /home/person/package1/package2/demoGUI.html 
	 * this class will return demoGUI (Depending if the FileAssociationManager object used to call this method 
	 * is set to handle java source or HTML files).
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
	public void associateFiles(Vector dirsToSearch)
	{
		Vector filenameVec = new Vector();
		for (int i=0; i<dirsToSearch.size(); i++)
			processDirectoryOrFile((String)dirsToSearch.elementAt(i),filenameVec);
			
		for (int i=0; i<loggedIntFVector.size(); i++)
			associateFileWithInterface((LoggedInterface)loggedIntFVector.elementAt(i),filenameVec);
		
		/*
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
		*/
	}
	
	/**
	 * Determines if the object calling this method is supposed to deal with javadocs files 
	 * or java source files.  Then depending on which one the manager is dealing with, the 
	 * Interface's current stored value for the file is returned.
	 * @param intf The Interface in question.
	 * @return The Interface's javadocs or java source filename.
	 */
	public String getAppropriateFilename(Interface intf)
	{
		String name = "";
		if (fileType == JAVADOCS)
			name = intf.getJavadocsFileName();
		else if (fileType == JAVASOURCE)
			name = intf.getSourceFileName();
			
		return name;
	}
	
	/**
	 * This method searches through the Vector of filenames given for the correct file to associate with 
	 * the Interface object, intf.
	 * @param loggedIntf The LoggedInterface to use to keep track of how the file is associated.
	 * @param fileNameVec The Vector of Strings each of which is the filename.
	 */
	private void associateFileWithInterface(LoggedInterface loggedIntf, Vector fileNameVec)
	{
		boolean found = false;
		int i=0;
		String shortFileName = "";
		String shortenedClassName = loggedIntf.getInterface().getPgmDefn().getEnclosingClassName(true,true);
		while (i<fileNameVec.size() && !found)
		{
			shortFileName = getShortenedClassName((String)fileNameVec.elementAt(i));
			if (shortFileName.equals(shortenedClassName))
			{
				found = true;
				loggedIntf.setModified(true);
				if (fileType == FileAssociationManager.JAVADOCS)	
					loggedIntf.getInterface().setJavadocsFileName((String)fileNameVec.elementAt(i));
				else if (fileType == FileAssociationManager.JAVASOURCE)
					loggedIntf.getInterface().setSourceFileName((String)fileNameVec.elementAt(i));
					
				if (loggedIntf.getInterface().getPgmDefn().isInner())
				{
					String type = loggedIntf.getInterface().getPgmDefn().getInterface_type();
					loggedIntf.setLog("This is an inner "+type+".  The file for "+shortenedClassName+" (its enclosing "+type+") was therefore used.");
				}
				else
					loggedIntf.setLog("Used file "+fileNameVec.elementAt(i));
			}
			i++;
		}
	}
	
	/**
	 * Sets the modified flag for all of the LoggedInterface objects from the Vector loggedIntFVector to false.
	 */
	public void resetModificationFlags()
	{
		for (int i=0; i<loggedIntFVector.size(); i++)
			((LoggedInterface)loggedIntFVector.elementAt(i)).setModified(false);
	}
	
	private void reorganizeVector()
	{
		//a Vector of all of the LoggedInterface objects from loggedIntFVector that are outer classes or interfaces
		Vector outerVec = new Vector();
		//a Vector of all of the LoggedInterface objects from loggedIntFVector that are inner classes or interfaces
		Vector innerVec = new Vector();
		
		Interface currentIntf = new Interface();
		
		for (int i=0; i<loggedIntFVector.size(); i++)
		{
			currentIntf = ((LoggedInterface)loggedIntFVector.elementAt(i)).getInterface();
			if (currentIntf.getPgmDefn().isInner())
				innerVec.add(currentIntf);
			else
				outerVec.add(currentIntf);
		}
		
		InterfaceUtilities.alphabatizeVector(innerVec,false,false);
		InterfaceUtilities.alphabatizeVector(outerVec,false,false);
		
		Vector tempVec = new Vector(innerVec.size()+outerVec.size());
		for (int i=0; i<outerVec.size(); i++)
			tempVec.add(outerVec.elementAt(i));
		
		for (int i=0; i<innerVec.size(); i++)
			tempVec.add(innerVec.elementAt(i));
			
		loggedIntFVector = tempVec;
	}
	
	/*
	 * If the filename supplied to this method is a file, this method associates the file to the correct 
	 * interface by seeing if any Interface object has the same shortened name as the shortened name 
	 * from the file (see getShortenedClassName(String str)).  If the filename corresponds to a directory 
	 * this method recursively scans through the directory and associates the files.  This method uses 
	 * the method associateFile(String fileName, String shortClassName) to associate the files.
	 * @param Dir The filename of the file or directory to process.
	 */
	public void processDirectoryOrFile(String Dir, Vector filenameVec)
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
					 processDirectoryOrFile( F[i].getAbsolutePath(), filenameVec);
				else if ( F[i].isFile() && isFileType(F[i].getAbsolutePath()) )
					filenameVec.add(F[i].getAbsolutePath());
			}
		}
		
		public class LoggedInterface
		{
			private boolean modified;
			private String log;
			private Interface intf;
			
			public LoggedInterface()
			{
				intf = new Interface();
				log = "";
				modified = false;
			}
			
			public LoggedInterface(Interface intF)
			{
				intf = intF;
				log = "";
				modified = false;
			}
			
			public LoggedInterface(Interface intF, String message, boolean mod)
			{
				intf = intF;
				log = message;
				modified = mod;
			}
			
			public void setInterface(Interface intF)
			{
				intf = intF;
			}
			
			public Interface getInterface()
			{
				return intf;
			}
			
			public void setModified(boolean bol)
			{
				modified = bol;
			}
			
			public boolean isModified()
			{
				return modified;
			}
			
			public String getLog()
			{
				return log;
			}
			
			public void setLog(String message)
			{
				log = message;
			}
			
			public String toString()
			{
				return intf.getPgmDefn().getInterface_name();
			}
		}
}
