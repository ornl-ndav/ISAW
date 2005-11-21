/*
 * File:  PrintableDocumentSection.java
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

import java.util.StringTokenizer;

/**
 * @author Dominic Kramer
 */
public class PrintableDocumentSection extends PrintableDocument
{
	protected static final String DELIMITER = ":";
	
	protected StringBuffer textBuffer;
	protected String type;
	
	public PrintableDocumentSection(String text, String tp)
	{
		textBuffer = new StringBuffer(text);
		type = tp;
	}
	
	/**
	 * Creates a PrintableDocumentSection by parsing the String returned from 
	 * the toString() method called on another PrintableDocumentSection.  This is 
	 * used to support making a PrintableDocumentSection from a PrintableDocumentSection 
	 * saved to a file.
	 * @param code The String returned from calling toString() on another PrintableDocumentSection.
	 */
	public PrintableDocumentSection(String code)
	{
		this("","");
		StringTokenizer tokenizer = new StringTokenizer(code,DELIMITER);
		String token = "";
		if (tokenizer.hasMoreTokens())
		{
			token = tokenizer.nextToken();
			StringTokenizer typeTokenizer = new StringTokenizer(token,"=");
			if (typeTokenizer.hasMoreTokens())
				token = typeTokenizer.nextToken(); //token = "TYPE"
			if (typeTokenizer.hasMoreTokens())
			{
				token = typeTokenizer.nextToken(); //this should be the type
				type = token;
			}
		
			if (tokenizer.hasMoreTokens())
			{
				token = tokenizer.nextToken();
				textBuffer.append(token);
			}
		}
	}
	
	public String getText()
	{
		return textBuffer.toString();
	}
	
	public void setText(String text)
	{
		textBuffer = new StringBuffer(text);
	}
	
	public StringBuffer getTextBuffer()
	{
		return textBuffer;
	}
	
	public void setTextBuffer(StringBuffer buffer)
	{
		textBuffer = buffer;
	}
	
	public String getType()
	{
		return type;
	}
	
	public void setType(String str)
	{
		type = str;
	}
	
	public String toString()
	{
		//Below is a sample of how the String will look.
		/*
		 * TYPE=bold:this is some text that
		 * is bold and includes 	a tab and a
		 * new line character:
		 */
		
		String newLine = System.getProperty("line.separator","\n");
		StringBuffer buffer = new StringBuffer();
		buffer.append("TYPE=");
		buffer.append(type);
		buffer.append(DELIMITER);
		buffer.append(getText());
		buffer.append(DELIMITER);
		return buffer.toString();
	}
}
