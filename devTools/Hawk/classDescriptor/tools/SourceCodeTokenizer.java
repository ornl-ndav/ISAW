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
 * Revision 1.1  2004/02/07 05:10:47  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SourceCodeTokenizer
{
	protected String str;
	protected int offset;
	
	public SourceCodeTokenizer(String s)
	{
		str = s;
		offset = 0;
	}
	
	/**
	 * This returns a SourceCodeToken object which contains the information for the String representing the next token as well as
	 * its type (i.e. Source Code, Comment, or Javadocs).  A section in the code is either a comment section, a javadocs section, or
	 * a source code section.
	 * @return The next section in the code
	 */
	public SourceCodeToken nextToken()
	{
		System.out.println("offset="+offset);
		
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
			token = new SourceCodeToken(newString,SourceCodeToken.SOURCE_CODE);
		}
		else if (commentDoubleSlash)
		{
			newString = processAsDoubleSlashComment();
			token = new SourceCodeToken(newString,SourceCodeToken.COMMENT);
		}
		else if (commentSlashStar)
		{
			newString = processAsSlashStarComment();
			token = new SourceCodeToken(newString,SourceCodeToken.COMMENT);
		}
		else if (javadocs)
		{
			newString = processAsSlashStarComment();
			token = new SourceCodeToken(newString,SourceCodeToken.JAVADOCS);
		}
		
		return token;
	}
	
	/**
	 * This method assumes that offset starts from the start of a source code section.  It returns
	 * the String representing all of the code until it reaches a /* or a // (Note /** is just a special version of /*).
	 * @return The source code section
	 */
	protected String processAsSource()
	{
		System.out.println("Entering processAsSource()");
		
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
		
		System.out.println("Leaving processAsSource()");
		
		return result;
	}
	
	
	/**
	 * This method assumes that offset starts from the start of a comment that uses a // for commenting.
	 * It returns the String starting from offset until a \n is reached
	 * @return The comment section
	 */
	protected String processAsDoubleSlashComment()
	{
		System.out.println("Entering processAsDoubleSlashComment()");
		
		String result = "";
		String next = getNextChar();
		while (!result.endsWith("\n") && result != null)
		{
			System.out.println("    result="+result);
			System.out.println("    next="+next);
			result += next;
			next = getNextChar();
		}
		if (next != null)
			offset--;

		System.out.println("Leaving processAsDoubleSlashComment()");
		
		return result;
	}
	
	/**
	 * This method assumes that offset starts from the start of a /* or /** used for commenting.
	 * It returns the String starting from offset util a *\/ is reached
	 * @return
	 */
	protected String processAsSlashStarComment()
	{
		System.out.println("Entering processAsSlashStarComment()");
		
		String result = "";
		String next = getNextChar();
		while (!result.endsWith("*/") && next != null)
		{
			result += next;
			next = getNextChar();			
		}
		if (next != null)
			offset--;

		System.out.println("Leaving processAsSlashStarComment()");
			
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
		return s;
	}
}
