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
 * Revision 1.3  2004/03/12 19:46:20  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:10:48  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

/**
 * This class contains static methods to get general information about Hawk such as 
 * the location of needed files.  It also contains methods that may be used by any class, 
 * such as the printStackTrace(Throwable e) that handles printing error messages to the 
 * screen.  This allows no need for common code to be repeated multiple times in the code.
 * @author kramer
 */
public class SystemsManager
{
	private SystemsManager()
	{}
	
	/**
	 *  This returns the class descriptor's home directory.  This will return on UNIX for example
	 *  /home/bob/.hawk
	 * @return The directory that is the program's home directory
	 */
	public static String getClassDescriptorHomeDirectory()
	{
		return (System.getProperty("user.home")+System.getProperty("file.separator")+".hawk");
	}
	
	/**
	 * Get Hawk's directory for storing temporary files.
	 * @return Hawk's directory for temporary files.
	 */
	public static String getClassDescriptorTemporaryDirectory()
	{
		return (getClassDescriptorHomeDirectory()+System.getProperty("file.separator")+"temporary");
	}
	
	/**
	 * This gets the file extension for native Hawk files with a period in from of it.
	 * @return .hjp
	 */
	public static String getHawkFileExtension()
	{
		return "."+getHawkFileExtensionWithoutPeriod();
	}
	
	/**
	 * This gets the file extension for native Hawk files.
	 * @return hjp
	 */
	public static String getHawkFileExtensionWithoutPeriod()
	{
		return "hjp";
	}
	
		/**
		 * Get the current version.
		 * @return The version number.
		 */
		public static String getVersion()
		{
			return "0.8.02.3-3";
		}
		
		/**
		 * Get the time that the release you are running was built.
		 * @return The build date.
		 */
		public static String getBuildDate()
		{
			return "Wednesday March 10, 2004 at 4:30 PM CST";
		}
		
		/**
		 * Get the author.
		 * @return The author's name.
		 */
		public static String getAuthor()
		{
			return "Dominic Kramer";
		}
		
		/**
		 * Get the author's email address.
		 * @return The author's email address.
		 */
		public static String getAuthorsEmailAddress()
		{
			return "kramerd@uwstout.edu";
		}
		
		/**
		 * Prints a stack trace describing where the Throwable e originated.  The 
		 * stack trace is printed to the standard output (most likely the console).
		 * @param e The Throwable to process.
		 */
		public static void printStackTraceToStandardOutput(Throwable e)
		{
			System.err.println(e);
			StackTraceElement[] traceArray = e.getStackTrace();
			for (int i = 0; i < traceArray.length; i++)
				System.out.println("  "+traceArray[i]);
		}
		
		/**
		 * Displays a window telling the user that an error has occured, who to contact about the error, and 
		 * describes that an error log was written to a file (whose name is given).  This method also prints a 
		 * stack trace for the Throwable e to the file.  If there is an error displaying the window or printing the 
		 * error log to the file an error message along with the error log is printed to the standard output.
		 * @param e The exception or error to use.
		 */
		public static void printStackTrace(Throwable e)
		{
			String filename = System.getProperty("user.home")+System.getProperty("file.separator")+System.currentTimeMillis()+"HawkErrorLog.txt";
			while ((new File(filename)).exists())
				filename = System.getProperty("user.home")+System.getProperty("file.separator")+System.currentTimeMillis()+"HawkErrorLog.txt";
			
			try
			{
				PrintWriter writer = new PrintWriter(new FileOutputStream(filename));
				writer.println("A "+e.getClass().getName()+" Exception Caught");
				writer.println("Operating System:  "+System.getProperty("os.name"));
				writer.println("Operating System Architecture:  "+System.getProperty("os.arch"));
				writer.println("Operating System Version:  "+System.getProperty("os.version"));
				writer.println("Classpath:  "+System.getProperty("java.class.path"));
				writer.println("Java Version:  "+System.getProperty("java.version"));
				writer.println("Java VM Specification Version:  "+System.getProperty("java.vm.specification.version"));
				writer.println("Java VM Implementation Version:  "+System.getProperty("java.vm.version"));
				System.err.println(e);
				writer.println();
				writer.println(e.getClass().getName());
				StackTraceElement[] traceArray = e.getStackTrace();
				for (int i = 0; i < traceArray.length; i++)
					writer.println("  "+traceArray[i]);
				
				writer.close();
				
				JOptionPane opPane = new JOptionPane();
				JOptionPane.showMessageDialog(opPane,
					"An error has occured in Hawk possibly causing it to function incorrectly." +
					"\nAn error report has been printed to the file "+filename+
					"\nPlease send the error report along with the actual results, expected" +
					"\nresults, and what you were doing when the error occured to "+
					"\n"+getAuthorsEmailAddress()+" to have the error processed.  Your " +
					"\nhelp in developing Hawk by sending error reports and comments is " +
					"\ngreatly appreciated.",
					"Error Caught",
					JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception error)
			{
				System.out.println("An error occured in Hawk possibly causing it to function incorrectly." +
				"\nAn error log was being written to "+filename+" when an error occured." +
				"\nThe error log is given below.");
				System.out.println();
				System.out.println("A "+e.getClass().getName()+" Exception Caught");
				System.out.println("Operating System:  "+System.getProperty("os.name"));
				System.out.println("Operating System Architecture:  "+System.getProperty("os.arch"));
				System.out.println("Operating System Version:  "+System.getProperty("os.version"));
				System.out.println("Classpath:  "+System.getProperty("java.class.path"));
				System.out.println("Java Version:  "+System.getProperty("java.version"));
				System.out.println("Java VM Specification Version:  "+System.getProperty("java.vm.specification.version"));
				System.out.println("Java VM Implementation Version:  "+System.getProperty("java.vm.version"));
				System.out.println();
				System.err.println(e);
				StackTraceElement[] traceArray = e.getStackTrace();
				for (int i = 0; i < traceArray.length; i++)
					System.out.println("  "+traceArray[i]);
				System.out.println("\nPlease send the error report along with the actual results, expected" +
				"\nresults, and what you were doing when the error occured to "+
				"\n"+getAuthorsEmailAddress()+" to have the error processed.  Your " +
				"\nhelp in developing Hawk by sending error reports and comments is " +
				"\ngreatly appreciated.");
			}
		}
		
		/**
		 * This method verifies that the Hawk home directory as well as Hawk's temporary 
		 * directory exist.  If they do not, this method tries to make them.  If Hawk is safe to 
		 * start (in other words the correct files and directories exist) this method returns true.
		 * @return True if it is safe to start Hawk and false otherwise.
		 */
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
						(new File(SystemsManager.getClassDescriptorHomeDirectory()+System.getProperty("file.separator")+"default"+getHawkFileExtension())).createNewFile();
					}
					catch(IOException e)
					{
						System.out.println("An IOException was thrown in startClassDescriptor() in SystemsManager.java");
						System.out.println("  when trying to create the file "+(new File(SystemsManager.getClassDescriptorHomeDirectory()+System.getProperty("file.separator")+"default.inf")));
						answer = false;
						JOptionPane oPane = new JOptionPane();
						JOptionPane.showMessageDialog(oPane,
							"Hawk could not start because the file "+getClassDescriptorHomeDirectory()+System.getProperty("file.separator")+"default"+getHawkFileExtension()+"\n" +
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
		
		/**
		 * Clears Hawk's directory which holds temporary files.
		 */
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
