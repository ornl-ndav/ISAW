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
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FileAssociationManager
{
	public static final int JAVASOURCE = 1;
	public static final int JAVADOCS = 2;
	
	/**
	 * A Vector of Interface objects
	 */
	private Vector intFVector;
	private int fileType;
	
	public FileAssociationManager(Vector vec, int type)
	{
		intFVector = vec;
		fileType = type;
	}
	
	public FileAssociationManager(Project pro, int type)
	{
		intFVector = pro.getInterfaceVec();
		fileType = type;
	}
	
	public FileAssociationManager(Interface intF, int type)
	{
		Vector tempVector = new Vector();
		tempVector.add(intF);
		intFVector = tempVector;
		fileType = type;
	}
	
	public int getFileType()
	{
		return fileType;
	}
	
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
