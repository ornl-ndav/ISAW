/*
 * File:  PrintableDocument.java
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
 */
 package devTools.Hawk.classDescriptor.tools.printing;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Vector;

import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * @author Dominic Kramer
 */
public class PrintableDocument
{
	protected static final String START_SECTION = "<START_SECTION>";
	protected static final String END_SECTION = "<END_SECTION>";
	
	protected Vector sectionVec;
	
	public PrintableDocument()
	{
		sectionVec = new Vector();
	}
	
	public PrintableDocument(BufferedReader reader)
	{
		this();
		String newLine = System.getProperty("line.separator","\n");
		try
		{
			String line = "";
			while ((line!=null))
			{
				line = reader.readLine();
				if ((line != null) && line.equals(START_SECTION))
				{
					StringBuffer buffer = new StringBuffer();
					line = reader.readLine();
					//procede to analyze the section
					while (!line.equals(END_SECTION) && (line!=null))
					{
						buffer.append(line);
						buffer.append(newLine);
						line = reader.readLine();
					}
					sectionVec.add(new PrintableDocumentSection(buffer .toString()));
				}
				else
					line = reader.readLine();
			}
		}
		catch (Throwable t)
		{
			SystemsManager.printStackTrace(t);
		}
	}
	
	public PrintableDocumentSection getSectionAt(int index) throws ArrayIndexOutOfBoundsException
	{
		return (PrintableDocumentSection)(sectionVec.elementAt(index));
	}
	
	public void insertSectionAt(PrintableDocumentSection sect, int index)
	{
		sectionVec.insertElementAt(sect,index);
	}
	
	public void removeSectionAt(int index)
	{
		sectionVec.remove(index);
	}
	
	public void add(PrintableDocumentSection sect)
	{
		sectionVec.add(sect);
	}
	
	public int numberOfSections()
	{
		return sectionVec.size();
	}
	
	public void saveToFile(PrintWriter out)
	{
		String newLine = System.getProperty("line.separator","\n");
		try
		{
			StringBuffer buffer = new StringBuffer();
			for (int i=0; i<sectionVec.size(); i++)
			{
				buffer.append(START_SECTION);
				buffer.append(newLine);
				buffer.append(getSectionAt(i).toString());
				buffer.append(newLine);
				buffer.append(END_SECTION);
				buffer.append(newLine);
			}
			out.print(buffer.toString());
		}
		catch (Throwable t)
		{
			SystemsManager.printStackTrace(t);
		}
	}
}
