/*
 * File:  SystemsManager.java
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
import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SystemsManager
{
	/**
	 *  This returns the class descriptor's home directory.  This will return on UNIX for example
	 *  /home/bob/.hawk
	 * @return The directory that is the program's home directory
	 */
	public static String getClassDescriptorHomeDirectory()
	{
		return (System.getProperty("user.home")+System.getProperty("file.separator")+".hawk");
	}
	
	public static String getClassDescriptorTemporaryDirectory()
	{
		return (getClassDescriptorHomeDirectory()+System.getProperty("file.separator")+"temporary");
	}
		
		/**
		 * 
		 */
		public static String getVersion()
		{
			return "0.8.02.2_1";
		}
		
		public static String getBuildDate()
		{
			return "Friday February 6, 2004 at 03:22 PM CST";
		}
		
		public static String getAuthor()
		{
			return "Dominic Kramer";
		}
		
		public static void printStackTrace(Throwable e)
		{
			System.out.println("Exception Caught");
			System.err.println(e);
			StackTraceElement[] traceArray = e.getStackTrace();
			for (int i = 0; i < traceArray.length; i++)
				System.out.println("  "+traceArray[i]);
		}
		
		public static boolean startClassDescriptor()
		{
			boolean answer = true;
			
			File classDescHome = new File(SystemsManager.getClassDescriptorHomeDirectory()+System.getProperty("file.separator"));
		
			if (classDescHome.exists())
			{
				//check if classDescHome is a directory
				if (classDescHome.isDirectory())
				{
					//procede into the directory to analyze its conents
					
					File tempDir = new File(getClassDescriptorTemporaryDirectory());
					
					if (!tempDir.exists() && answer)
					{
						boolean madeTemp = tempDir.mkdir();
						
						if (!madeTemp)
						{
							JOptionPane oPane = new JOptionPane();
							JOptionPane.showMessageDialog(oPane,
								"Hawk could not start because the directory "+tempDir.getAbsolutePath()+" could not be created.",
								"Error",
								JOptionPane.ERROR_MESSAGE);
		
							answer = false;
						}
					}
					
				}
				else
				{
					//then the Hawk's home directory is actually a file
					//in this case pop up a window that informs the user that a file
					//exists instead of a directory
					JOptionPane oPane = new JOptionPane();
					JOptionPane.showMessageDialog(oPane,
						"The location "+classDescHome.getAbsolutePath()+" exists as a file.\nThis location is used as a directory which " +
							"contains all\nof Hawk's configuration and default database files.\nEither rename the file or delete it.  Then, when " +
							"Hawk\nis restarted, the appropriate directories will be made.",
						"File Error",
						JOptionPane.ERROR_MESSAGE);
						
						//answer = false because the user needs to change the file's name.
						answer = false;
				}
			}
			else
			{
				JOptionPane opPane = new JOptionPane();
				JOptionPane.showMessageDialog(opPane,
					"The directory "+classDescHome.getAbsolutePath()+" could not be found.\nThis directory contains Hawk's database " +
						"and configuration files.\nThe directory will now be created.",
					"Directory Not Found",
					JOptionPane.ERROR_MESSAGE);
				
				//now procede to make the directories and files
				File homeDir =  new File(getClassDescriptorHomeDirectory());
				boolean homeMade = homeDir.mkdir();
				
				if (!homeMade)
				{
					JOptionPane oPane = new JOptionPane();
					JOptionPane.showMessageDialog(oPane,
						"Hawk could not start because the directory "+homeDir.getAbsolutePath()+" could not be created.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
		
					answer = false;
				}
								
				if (answer)
				{
					try
					{
						(new File(SystemsManager.getClassDescriptorHomeDirectory()+System.getProperty("file.separator")+"default.jdf")).createNewFile();
					}
					catch(IOException e)
					{
						System.out.println("An IOException was thrown in startClassDescriptor() in SystemsManager.java");
						System.out.println("  when trying to create the file "+(new File(SystemsManager.getClassDescriptorHomeDirectory()+System.getProperty("file.separator")+"default.inf")));
						answer = false;
						JOptionPane oPane = new JOptionPane();
						JOptionPane.showMessageDialog(oPane,
							"Hawk could not start because the file "+getClassDescriptorHomeDirectory()+System.getProperty("file.separator")+"default.jdf\n" +
									"could not be created.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					}
				}
				
				File tempDir = new File(getClassDescriptorTemporaryDirectory());
				boolean tempDirMade = tempDir.mkdir();
				
				if (!tempDirMade && answer)
				{
					JOptionPane oPane = new JOptionPane();
					JOptionPane.showMessageDialog(oPane,
						"Hawk could not start because the directory "+tempDir.getAbsolutePath()+" could not be created.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
						
						answer = false;	
				}
			}
			return answer;
		}
		
		public static void clearClassDescriptorTemporaryDirectory()
		{
			File tempDir = new File(getClassDescriptorTemporaryDirectory());
			File[] fileArr = tempDir.listFiles();
			
			if (fileArr != null)
			{
				for (int i=0; i<fileArr.length; i++)
					fileArr[i].delete();
			}
		}
}