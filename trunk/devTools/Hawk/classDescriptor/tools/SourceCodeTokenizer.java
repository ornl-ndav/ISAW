/*
 * File:  SourceCodeTokenizer.java
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
 * Revision 1.4  2004/05/26 21:04:39  kramer
 * Dramatically changed how this class works.
 *
 * Revision 1.3  2004/03/12 19:46:20  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:10:47  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

import java.io.BufferedReader;
import java.io.FileReader;

public class SourceCodeTokenizer
{
	private StringBuffer buffer;
	private long currentIndex;
	private int numOfLines;
	/**
	 * This field contains the line number for the start of the last token obtained.
	 */
	private int initialLineNumber;
	/**
	 * This field contains the index (from the StringBuffer 'buffer') from which 
	 * the last token obtained starts.
	 */
	private long initialIndex;

	public SourceCodeTokenizer(String fileName)
	{
		currentIndex  = 0;
		numOfLines = 0;
		initialIndex = 0;
		initialLineNumber = numOfLines;
		buffer = new StringBuffer();
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			int val = reader.read();
			while (val != -1)
			{
				buffer.append((char)val);
				val = reader.read();
			}
		}
		catch (Throwable t)
		{
			SystemsManager.printStackTrace(t);
		}
	}
	
	protected char getCharAt(long index)
	{
		char tempChar = buffer.charAt((int)index);
		if ((tempChar==10) || (tempChar==13))
		{
			if (currentIndex<buffer.length())
				numOfLines++;
		}
		return tempChar;
	}
	
	public SourceCodeToken nextToken()
	{
		SourceCodeToken token = null;
		if (currentIndex < buffer.length())
		{
			StringBuffer tempBuffer = new StringBuffer();
				if (currentIndex<buffer.length())
					tempBuffer.append(getCharAt(currentIndex));
				if ((currentIndex+1)<buffer.length())
					tempBuffer.append(getCharAt(currentIndex+1));
				
				if (tempBuffer.toString().equals("//"))
				{
					//process as a // comment
					token = new SourceCodeToken(processAsSlashSlashComment(),initialIndex,initialLineNumber,SourceCodeToken.SLASH_SLASH_COMMENT);
				}
				else if (tempBuffer.toString().equals("/*"))
				{
					if ((currentIndex+2)<buffer.length())
						tempBuffer.append(getCharAt(currentIndex+2));
					if (tempBuffer.toString().equals("/**"))
					{
						//process as a javadoc comment
						token = new SourceCodeToken(processAsSlashStarOrJavadocComment(),initialIndex,initialLineNumber,SourceCodeToken.JAVADOCS);
					}
					else
					{
						//process as a /* comment
						token = new SourceCodeToken(processAsSlashStarOrJavadocComment(),initialIndex,initialLineNumber,SourceCodeToken.SLASH_STAR_COMMENT);
					}
				}
				else if (tempBuffer.toString().equals(""))
				{
					//then the end of the String has been reached
					token = null;
				}
				else
				{
					//process as source
					token = new SourceCodeToken(processAsSource(),initialIndex,initialLineNumber,SourceCodeToken.SOURCE_CODE);
				}
		}
		return token;
	}
	
	/**
	 * Get the next two characters concantenated together in a String.  If there 
	 * is only one character left in the buffer, only that character (as a String) is returned.
	 */
	private String getNextTwoChars()
	{
		StringBuffer tempBuffer = new StringBuffer();
		if (currentIndex<buffer.length())
			tempBuffer.append(getCharAt(currentIndex));
		if ((currentIndex+1)<buffer.length())
			tempBuffer.append(getCharAt(currentIndex+1));
		return tempBuffer.toString();
	}
	
	private String processAsSource()
	{
		initialLineNumber = numOfLines;
		initialIndex = currentIndex;
		StringBuffer tempBuffer = new StringBuffer();
		String nextChars = getNextTwoChars();
		while (!nextChars.equals("//") && !nextChars.equals("/*") && (currentIndex<buffer.length()))
		{
			tempBuffer.append(getCharAt(currentIndex));
			currentIndex++;
			nextChars = getNextTwoChars();
		}
		return tempBuffer.toString();
	}
	
	private String processAsSlashSlashComment()
	{
		initialLineNumber = numOfLines;
		initialIndex = currentIndex;
		StringBuffer tempBuffer = new StringBuffer();
		char currentChar = getCharAt(currentIndex);
		while ((currentChar!='\n') && (currentChar!='\r') && (currentIndex<buffer.length()))
		{
			tempBuffer.append(currentChar);
			currentIndex++;
			currentChar = getCharAt(currentIndex);
		}
		return tempBuffer.toString();
	}
	
	private String processAsSlashStarOrJavadocComment()
	{
		initialLineNumber = numOfLines;
		initialIndex = currentIndex;
		StringBuffer tempBuffer = new StringBuffer();
		String nextTwoChars = getNextTwoChars();
		while (!nextTwoChars.equals("*/") && (currentIndex<buffer.length()))
		{
			tempBuffer.append(getCharAt(currentIndex));
			currentIndex++;
			nextTwoChars = getNextTwoChars();
		}
		if (currentIndex<buffer.length())
			tempBuffer.append(getCharAt(currentIndex));
		currentIndex++;
		if (currentIndex<buffer.length())
			tempBuffer.append(getCharAt(currentIndex)); //this will add the */ if possible
		currentIndex++;
		return tempBuffer.toString();
	}
	
	public long getNumberOfLines()
	{
		int num = 0;
		char currentChar = ' ';
		for (int i=0; i<buffer.length(); i++)
		{
			currentChar = getCharAt(i);
			if ((currentChar==10) || (currentChar==13))
				num++;
		}
		return num;
	}
}
