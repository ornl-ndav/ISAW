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

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class is used to break source code into tokens.  When, the next token from the source code is obtained from the method
 * nextToken(), the method looks at the next few characters in the String "str" and determines if the next section of the code is a 
 * slash star comment (a comment of the form /* . . . . *\/), slash slash comment (a comment of the form //.......\n), a javadocs 
 * comment (a comment of the form /** . . . . *\/), or actual code.  The next section is returned as an object that can be used to 
 * determine the section type.
 * @author Dominic Kramer
 */
public class SourceCodeTokenizer
{
	/**
	 * The String to tokenize.
	 */
	protected String str;
	/**
	 * The current offset in the string.
	 */
	protected int offset;
	/**
	 * The current line number.
	 */
	protected int lineNumber;
		
	/**
	 * Creates a SourceCodeTokenizer.
	 * @param s The String to tokenize.
	 */
	public SourceCodeTokenizer(String s)
	{
		str = s;
		offset = 0;
		lineNumber = 0;
	}
	
	/**
	 * This returns a SourceCodeToken object which contains the information for the String representing the next token as well as
	 * its type (i.e. Source Code, Slash Slash Comment, Slash Star Comment, or Javadocs).  A section in the code is either a comment 
	 * section, a javadocs section, or a source code section.
	 * @return The next section in the code
	 */
	public SourceCodeToken nextToken()
	{
		SourceCodeToken token = null;
		boolean source = false;
		boolean commentDoubleSlash = false;
		boolean commentSlashStar = false;
		boolean javadocs = false;
		int tempOffset = offset; //point to the next character
		if (tempOffset < str.length())
		{
			String next = String.valueOf(str.charAt(tempOffset));
			if (next.equals("/"))
			{
				tempOffset++;
				if (tempOffset < str.length())
				{
					next = String.valueOf(str.charAt(tempOffset));
					if (next.equals("/"))
						commentDoubleSlash = true;
					else if (next.equals("*"))
						commentSlashStar = true;
						
					tempOffset++;
					if (tempOffset < str.length())
					{
						next = String.valueOf(str.charAt(tempOffset));
						if (next.equals("*"))
						{
							commentDoubleSlash = false;
							commentSlashStar = false;
							javadocs = true;
						}
					}
				}
				else
					source = true;
			}
			else
				source = true;
		}
		
		String newString = "";
		if (source)
		{
			newString = processAsSource();
			lineNumber++;
			token = new SourceCodeToken(getVectorFromString(newString),lineNumber,SourceCodeToken.SOURCE_CODE);
			lineNumber = token.getLastLineNumber();
		}
		else if (commentDoubleSlash)
		{
			newString = processAsDoubleSlashComment();
			lineNumber++;
			token = new SourceCodeToken(getVectorFromString(newString),lineNumber,SourceCodeToken.SLASH_SLASH_COMMENT);
			lineNumber = token.getLastLineNumber();
		}
		else if (commentSlashStar)
		{
			newString = processAsSlashStarComment();
			lineNumber++;
			token = new SourceCodeToken(getVectorFromString(newString),lineNumber,SourceCodeToken.SLASH_STAR_COMMENT);
			lineNumber = token.getLastLineNumber();
		}
		else if (javadocs)
		{
			newString = processAsSlashStarComment();
			lineNumber++;
			token = new SourceCodeToken(getVectorFromString(newString),lineNumber,SourceCodeToken.JAVADOCS);
			lineNumber = token.getLastLineNumber();
		}
		
		return token;
	}
	
	/**
	 * Takes the String supplied and breaks it into lines.  Each line is an element from the Vector returned.
	 * @param str The String to use.
	 * @return A Vector of Strings.
	 */
	protected Vector getVectorFromString(String str)
	{
		Vector vec = new Vector();
		StringTokenizer tokenizer = new StringTokenizer(str,"\n");
		while(tokenizer.hasMoreTokens())
			vec.add(tokenizer.nextToken());
		
		return vec;
	}
	
	/**
	 * Get the number of lines that make up the string str.
	 * @return The number of lines that make up the string str.
	 */
	public int getNumberOfLinesInString()
	{
		StringTokenizer tokenizer = new StringTokenizer(str,"\n");
		return tokenizer.countTokens();
	}

	/**
	 * This method assumes that offset starts from the start of a source code section.  It returns
	 * the String representing all of the code until it reaches a /* or a // (Note /** is just a special version of /*).
	 * @return The source code section
	 */
	protected String processAsSource()
	{
		String result = "";
		String next = getNextChar();
		while ( !(result.endsWith("//") || result.endsWith("/*")) && next != null)
		{
			result += next;
			next = getNextChar();
		}
		if (next != null)
		{
			if (result.endsWith("//") || result.endsWith("/*"))
			{
				result = result.substring(0,result.length()-2);
				offset = offset - 2;
			}
			offset--;
		}
		
		return result;
	}
	
	
	/**
	 * This method assumes that offset starts from the start of a comment that uses a // for commenting.
	 * It returns the String starting from offset until a \n is reached
	 * @return The comment section
	 */
	protected String processAsDoubleSlashComment()
	{
		String result = "";
		String next = getNextChar();
		while (!result.endsWith("\n") && result != null)
		{
			result += next;
			next = getNextChar();
		}
		if (next != null)
			offset--;
		
		return result;
	}
	
	/**
	 * This method assumes that offset starts from the start of a /* or /** used for commenting.
	 * It returns the String starting from offset util a *\/ is reached
	 * @return
	 */
	protected String processAsSlashStarComment()
	{
		String result = "";
		String next = getNextChar();
		while (!result.endsWith("*/") && next != null)
		{
			result += next;
			next = getNextChar();			
		}
		if (next != null)
			offset--;
			
		return result;
	}
	
	/**
	 * This returns the next character in str as a String or null if the end of the String has been reached.
	 * @return The next character as a String.
	 */
	public String getNextChar()
	{
		String s = null;
		if (offset < str.length())
		{
			s = String.valueOf(str.charAt(offset));
			offset++;
		}
		//this was added to convert tabs into spaces
		if ((s != null) && (s.equals("\t")))
			s = "   ";
			
		return s;
	}
}
