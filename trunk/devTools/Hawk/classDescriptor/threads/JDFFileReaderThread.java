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
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import devTools.Hawk.classDescriptor.gui.frame.ProgressGUI;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.SystemsManager;
import devTools.Hawk.classDescriptor.tools.dataFileUtilities;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JDFFileReaderThread extends Thread
{
	protected dataFileUtilities data;
	protected Vector vec;
	protected ProgressGUI progress;
	protected int length;
	
	public JDFFileReaderThread(dataFileUtilities DATA, Vector VEC)
	{
		data = DATA;
		vec = VEC;
		
		progress = new ProgressGUI(0,getNumberOfLines(data.getFileName()),"Opening File");
		progress.setVisible(true);
	}
	
	public void run()
	{
		try
		{
			int num = 0;
			//BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(data.getFileName())));
			DataInputStream reader = new DataInputStream(new FileInputStream(data.getFileName()));
			
			
			String line = readFileLine(reader); //this is the project's name
			num++;
			progress.setValue(num);
			
			line = readFileLine(reader);

				while (line != null && !line.trim().equals("") && !progress.isCancelled())
				{
					Interface intF = (new Interface()).makeInterfaceObject(line);
					progress.setText("Data for class "+num+" read\n");
						
					num++;
					vec.add(intF.Clone());
					
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
	
	public String readFileLine(DataInputStream dat)
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
			catch (Exception e)
			{
				SystemsManager.printStackTrace(e);
			}
		
		return answer;
	}
	
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
		catch (EOFException e)
		{}
		catch (IOException e)
		{}
		
		return num;
	}
}