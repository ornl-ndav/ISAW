/*
 * File:  CommandLineManager.java
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
 * Revision 1.2  2004/03/11 18:31:09  bouzekc
 * Documented file using javadoc statements.
 * Modified to use the Project(String, boolean) constructor instead of the
 * Project(dataFileUtilities, boolean) when creating projects from native Hawk
 * filenames supplied at the command line.
 *
 * Revision 1.1  2004/02/07 05:10:45  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

import java.io.File;
import java.util.Vector;

import javax.swing.JOptionPane;

import devTools.Hawk.classDescriptor.modeledObjects.Project;

/**
 * This class handles the arguments passed to the program on the 
 * command line.
 * @author kramer
 */
public class CommandLineManager
{
	/**
	 * The array of Strings each of which is an argument from the 
	 * command line.
	 */
	private String[] arr;
	//vec is a Vector of Projects specified from the command line
	/**
	 * This is the Vector of Project objects created from .hjp files 
	 * specified from the command line.
	 */
	private Vector vec;
	/**
	 * Constant used to specify that the window should be displayed.
	 */
	public static final int SHOW_GUI = 1;	
	/**
	 * Constant used to specify that the window should not be displayed.
	 */
	public static final int DONT_SHOW_GUI = 2;
	
	/**
	 * Create a new CommandLineManager object.
	 * @param a The arguments from the command line.
	 */
	public CommandLineManager(String[] a)
	{
		arr = a;
		vec = new Vector();
	}
	
	/**
	 * Get the Vector of Project objects created by reading the .hjp files 
	 * supplied from the command line.
	 * @return A Vector of Project objects.
	 */
	public Vector getCommandLineProjectVector()
	{
		return vec;
	}
	
	/**
	 * This method reads through the command line arguments and decides what to do.  
	 * Information may be displayed to the screen or a Project object might be created from 
	 * a .hjp file if its full filename is specified.  Any Project objects are placed in the Vector 
	 * vec.
	 * @return Either SHOW_GUI or DONT_SHOW_GUI
	 */
	public int parseCommandLine()
	{
		int answer = DONT_SHOW_GUI;
		String unknown = "";
		
		if (arr.length == 0)
			answer = SHOW_GUI;
		
		for (int i = 0; i<arr.length; i++)
		{
			String str = arr[i];
			if (str.equals("--version") || str.equals("-V"))
			{
				if (!unknown.trim().equals(""))
					System.out.println("Unrecognized Option:  "+unknown);
				System.out.println("Hawk Version:  "+SystemsManager.getVersion());
				unknown = "";
			} 
			else if (str.equals("--build") || str.equals("-B"))
			{	
				if (!unknown.trim().equals(""))
					System.out.println("Unrecognized Option:  "+unknown);
				System.out.println("Hawk Build:  "+SystemsManager.getBuildDate());
				unknown = "";
			} 
			else if (str.equals("--author") || str.equals("-A"))
			{
				if (!unknown.trim().equals(""))
					System.out.println("Unrecognized Option:  "+unknown);
				System.out.println("Author:  "+SystemsManager.getAuthor());
				unknown = "";
			}
			else if (str.endsWith(SystemsManager.getHawkFileExtension()))
			{
				str = unknown + str;
				str = str.trim();
				unknown = "";
				
				File file = new File(str);
				String fileName = "";
				
				if (file.exists())
					fileName = str;
				else
				{
					file = new File(System.getProperty("user.dir")+System.getProperty("file.separator")+str);
					
					if (file.exists())
						fileName = System.getProperty("user.dir")+System.getProperty("file.separator")+str;
					else
					{
						JOptionPane opPane = new JOptionPane();
						JOptionPane.showMessageDialog(opPane,
							"The file "+file.getAbsolutePath()+" does not exist.",
							"File Error",
						JOptionPane.ERROR_MESSAGE);
					}
				}
				
				if (!fileName.equals(""))
				{
					Project newProject = new Project(fileName,false);									
					vec.add(newProject);
					answer = SHOW_GUI;
				}
				else
				{
					answer = SHOW_GUI;
				}
			}
			else if (str.equals("--help") || str.equals("-h"))
			{
				if (!unknown.trim().equals(""))
					System.out.println("Unrecognized Option:  "+unknown);
				System.out.println();
				System.out.println("Hawk is a java based developer's program used to analyze .class files");
				System.out.println("     derived from Java source code.  Given a .class file, Hawk can");
				System.out.println("     partially reconstruct the source code to create UML diagrams and");
				System.out.println("     other information to aid in understanding the code.  Therefore,");
				System.out.println("     you do not need a program's source code when using Hawk to");
				System.out.println("     start to understand how the program was created.");
				System.out.println();
				System.out.println("Usage:  java -jar Hawk.jar [OPTIONS] [FULL "+SystemsManager.getHawkFileExtension()+" FILENAMES]");
				System.out.println();
				System.out.println("Filenames:");
				System.out.println("     This is a list of "+SystemsManager.getHawkFileExtension()+" files that will be opened when Hawk starts");
				System.out.println();
				System.out.println("Options:");
				System.out.println("     -V, --version");
				System.out.println("          Print the version number");
				System.out.println("     -B, --build");
				System.out.println("          Print the build date");
				System.out.println("     -A, --author");
				System.out.println("          Print the author's name");
				System.out.println("     -h, --help");
				System.out.println("          Print this help");
				System.out.println();
				System.out.println("Report bugs to <kramerd@uwstout.edu>.");
			}
			else
			{
				unknown = unknown + " " + str + " ";
			}
		}
		
		if (!unknown.trim().equals(""))
			System.out.println("Unrecognized Option:  "+unknown);
		
		return answer;
	}	
}
