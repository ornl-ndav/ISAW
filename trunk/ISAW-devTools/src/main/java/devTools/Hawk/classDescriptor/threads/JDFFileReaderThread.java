/*
 * File:  JDFFileReaderThread.java
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
 * Revision 1.1  2004/02/07 05:10:27  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.threads;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import devTools.Hawk.classDescriptor.gui.frame.ProgressGUI;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * This file reads data from a native Hawk file and creates a project.  Because the file is read 
 * in a separate thread, the gui does not seem to freeze.
 * @author Dominic Kramer
 */
public class JDFFileReaderThread extends Thread
{
	/**
	 * The name of the native Hawk file from which the data is read.
	 */
	protected String filename;
	/**
	 * This is the window that shows the progress of the thread.
	 */
	protected ProgressGUI progress;
	/**
	 * The Vector of Interface objects created.
	 */
	protected Vector vec;
	
	/**
	 * Creates a new JDFFileReaderThread.
	 * @param VEC The Vector to add the Interfaces created to.
	 */
	public JDFFileReaderThread(String STR, Vector VEC)
	{
		filename = STR;
		vec = VEC;

		progress = new ProgressGUI(0,getNumberOfLines(filename),"Opening File");
		progress.setVisible(true);
	}
	
	/**
	 * This method reads the data and creates Interfaces.  Do not call this method to start the thread.  Call 
	 * start() which in turn will call this method.
	 */
	public void run()
	{
		try
		{
			int num = 0;
			DataInputStream reader = new DataInputStream(new FileInputStream(filename));
			
			
			String line = readFileLine(reader); //this is the project's name
			num++;
			progress.setValue(num);
			
			line = readFileLine(reader);

				while (line != null && !line.trim().equals("") && !progress.isCancelled())
				{
					Interface intF = (new Interface()).makeInterfaceObject(line);
					progress.setText("Data for class "+num+" read\n");
						
					num++;
					vec.add(intF.getClone());
					
					line = readFileLine(reader);
					progress.setValue(num);
				}
			
			if (!progress.isCancelled())
			{
				progress.isCompleted();
				progress.appendMessage("Done");
				progress.dispose();
			}
			System.out.println("num="+num);
			reader.close();
		}
		catch(IOException e)
		{
			progress.appendMessage("An error occured while trying to read the file.");
		}
	}
	
	/**
	 * This reads a line of data from the file.  Note:  the file is a binary file so you have to 
	 * use this method to actually read a line.
	 * @param dat The DataInputStream to read the file from.
	 * @return The next line from the file.
	 */
	public String readFileLine(DataInputStream dat) throws UTFDataFormatException, FileNotFoundException, IOException
	{
		String answer = null;
		
			try
			{
				answer = dat.readUTF();
				while (!answer.endsWith("\n"))
					answer += dat.readUTF();
				
				StringTokenizer tokenizer = new StringTokenizer(answer,"\n");
				answer = tokenizer.nextToken();

			}
/*
			catch (UTFDataFormatException e)
			{
				JOptionPane opPane = new JOptionPane();
				JOptionPane.showMessageDialog(opPane,"The file "+filename+" could not be read because its data has been corrupted.\n" +
					"Make sure it is a "+SystemsManager.getHawkFileExtensionWithoutPeriod()+" file written by Hawk and not just " +
						"a file that ends in "+SystemsManager.getHawkFileExtensionWithoutPeriod()+"."
					,"File Not Found"
					,JOptionPane.ERROR_MESSAGE);
			}
			catch (FileNotFoundException e)
			{
				JOptionPane opPane = new JOptionPane();
				JOptionPane.showMessageDialog(opPane,"The file "+filename+" does not exist."
					,"File Not Found"
					,JOptionPane.ERROR_MESSAGE);
			}
*/
			catch (EOFException e)
			{
				try
				{
					int num = dat.available();
					//if num == 0, then the exception was thrown because the end of the 
					//file was reached.  However, this exception is used to stop the reading of the 
					//by catching the exception.  So if num == 0, the exception occured for the 
					//correct reason.
					if (num != 0)
						SystemsManager.printStackTrace(e);
				}
				catch(Exception error)
				{
					SystemsManager.printStackTrace(e);
					SystemsManager.printStackTrace(error);
				}
			}
			
		
		return answer;
	}
	
	/**
	 * This gets the number of lines in the String str.  More specifically, it finds the number of times 
	 * \n appears in the string.
	 * @param str The String to check.
	 * @return The number of lines.
	 */
	public int getNumberOfLines(String str)
	{
		int num = 0;
		String word = "";
		try
		{
			DataInputStream reader = new DataInputStream(new FileInputStream(str));
			while (num != -1)
			{
				if (reader.readUTF().equals("\n"))
					num++;
			}
			reader.close();			

		}
		catch (UTFDataFormatException e)
		{
			JOptionPane opPane = new JOptionPane();
			JOptionPane.showMessageDialog(opPane,"The file "+filename+" could not be read because its data has been corrupted.\n" +
				"Make sure it is a "+SystemsManager.getHawkFileExtensionWithoutPeriod()+" file written by Hawk and not just " +
					"a file that ends in "+SystemsManager.getHawkFileExtensionWithoutPeriod()+"."
				,"File Not Found"
				,JOptionPane.ERROR_MESSAGE);
		}
		catch (EOFException e)
		{  /*if this exception is caught it means that the end of the file is reached which is ok.*/}
		catch (IOException e)
		{
			SystemsManager.printStackTrace(e);
		}
		
		return num;
	}
}