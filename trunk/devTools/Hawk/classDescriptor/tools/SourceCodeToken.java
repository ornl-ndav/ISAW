/*
 * File:  SourceCodeToken.java
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
 * Revision 1.5  2004/05/26 21:00:06  kramer
 * Modified how information is internally stored in a token.
 *
 * Revision 1.4  2004/03/12 19:46:20  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:10:47  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

import java.util.Vector;

/**
 * This class is used with the class SourceCodeTokenizer.  This token 
 * contains a String, which is the data the token holds, as well as an integer 
 * which describes what the token represents (either source code, a javadocs 
 * statement, a slash star comment (a comment starting with /* ), or a slash 
 * slash comment (a comment starting with //)).
 * @author Dominic Kramer
 */
public class SourceCodeToken
{
	/**
	 * An integer used to signify that the token's type is unknown.
	 */
	public static final int UNKNOWN = -1;
	/**
	 * An integer used to signify that the token represents source code.
	 */
	public static final int SOURCE_CODE = 0;
	/**
	 * An integer used to signify that the token represents a javadocs statement.
	 */
	public static final int JAVADOCS = 1;
	/**
	 * An integer used to signify that the token represents a slash star comment 
	 * (a comment starting with /*).
	 */
	public static final int SLASH_STAR_COMMENT = 2;
	/**
	 * An integer used to signify that the token represents a slash star comment 
	 * (a comment starting with //).
	 */
	public static final int SLASH_SLASH_COMMENT = 3;
	
	/**
	 * A Vector of Strings, each of which is one line from the string that the token is 
	 * supposed to represent.  Thus it is easy to get an arbitrary line from the string.
	 */
//	protected Vector strVec;
	/**
	 * The buffer which holds the String's contents.
	 */
	protected StringBuffer buffer;
	/**
	 * This token probably came from a much larger StringBuffer (holding the contents of a string).  
	 * This field is the line number of the first line from the buffer where this token starts.
	 */
	protected int firstLineNumber;
	/**
	 * This token probably came from a much larger StringBuffer (holding the contents of a string).  
	 * This field contains the index of the character from the larger buffer from which this token starts.
	 */
	protected long firstIndex;
	/**
	 * Used to describe the tokens type (either SOURCE_CODE, JAVADOCS, 
	 * SLASH_STAR_COMMENT, or SLASH_SLASH_COMMENT).
	 */
	protected int type;
	/**
	 * This Vector contains the indices where a new line character occurs.
	 */
	private Vector lineNumberVec;

	/**
	 * Create a new SourceCodeToken
	 * @param str The String that this token encapsulates.
	 * @param initialLineNumber The line number for the first String in the vector s.
	 * @param typeNumber This token's type (either SOURCE_CODE, JAVADOCS, 
	 * SLASH_STAR_COMMENT, or SLASH_SLASH_COMMENT).
	 */
	public SourceCodeToken(String str, long initialIndex, int initialLineNumber, int typeNumber)
	{
		firstIndex = initialIndex;
		firstLineNumber = initialLineNumber;
		type = typeNumber;
		buffer = new StringBuffer(str);
		int lastOccurance = 0;
		int previousOccurance = 0;
		lineNumberVec = new Vector();
			while (lastOccurance<buffer.length() && lastOccurance>=previousOccurance)
			{
				previousOccurance = lastOccurance;
				lastOccurance = buffer.indexOf(System.getProperty("line.separator"),lastOccurance+1);
				lineNumberVec.add(new Integer(lastOccurance));
			}
	}
	
	/**
	 * This method gets the line number for the line that contains the character at index 'index'.
	 * @param index The character in the StringBuffer 'buffer' used.
	 * @return The corresponding line number.
	 */
	public int getLineNumberAtIndex(int index)
	{
		int lineNum = 0;
		int i = 0;
		while (index>((Integer)lineNumberVec.elementAt(i)).intValue())
		{
			lineNum++;
			i++;
		}
		return lineNum;
	}
	
	/**
	 * Get the index of the first character in this token from the larger 
	 * StringBuffer that this token is a subset of.
	 */
	public long getFirstIndex()
	{
		return firstIndex;
	}
	
	/**
	 * Get the string that this token represents without line numbers in the original form from 
	 * the source code.
	 * @return The string that this token represents.
	 */
	public String toString()
	{
		return buffer.toString();
	}
	
	/**
	 * Get the type associated with this token.
	 * @return Either SOURCE_CODE, JAVADOCS, SLASH_STAR_COMMENT, 
	 * or SLASH_STAR_COMMENT.
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * True if the token represents source code.
	 * @return True or false.
	 */
	public boolean isSourceCode()
	{
		return (type == SOURCE_CODE);
	}
	
	/**
	 * True if the token represents a comment (either a slash star comment or a slash slash comment).
	 * @return True or false.
	 */
	public boolean isComment()
	{
		return ((type == SLASH_SLASH_COMMENT) || (type == SLASH_STAR_COMMENT));
	}
	
	/**
	 * True if the token represents a slash slash comment.
	 * @return True or false.
	 */
	public boolean isSlashSlashComment()
	{
		return (type == SLASH_SLASH_COMMENT);
	}
	
	/**
	 * True if the token represents a slash star comment.
	 * @return True or false.
	 */
	public boolean isSlashStarComment()
	{
		return (type == SLASH_STAR_COMMENT);
	}
	
	/**
	 * True if this token represents a javadocs statement.
	 * @return True or false.
	 */
	public boolean isJavadocs()
	{
		return (type == JAVADOCS);
	}
}
