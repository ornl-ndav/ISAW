/*
 * File:  ColorfulTextGUI.java
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
 * Revision 1.2  2004/03/11 18:30:31  bouzekc
 * Documented file using javadoc statements.
 * Added support for colored line numbers.
 *
 * Revision 1.1  2004/02/07 05:09:14  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.internalFrame;

import java.awt.Color;
import java.text.DecimalFormat;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.tools.SourceCodeToken;
import devTools.Hawk.classDescriptor.tools.SourceCodeTokenizer;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * This is the superclass to the classes SourceCodeGUI and ShortenedSourceGUI because 
 * it handles some general methods for writing colored text to a window.
 * @author Dominic Kramer
 */
public abstract class ColorfulTextGUI extends DesktopInternalFrame
{
	/**
	 * Create a new ColorfulTextGUI object.
	 * @param desk The HawkDesktop associated with this window.
	 */
	public ColorfulTextGUI(HawkDesktop desk)
	{
		super(desk);
	}
	
	/**
	 * Gets a copy of this window.
	 */
	public abstract DesktopInternalFrame getCopy();
	
	/**
	 * This method takes the string supplied and uses a SourceCodeTokenizer 
	 * to break the string into source code, comment, and javadocs sections.  Each 
	 * section is added to the StyledDocument with the correct color depending if the 
	 * section is source code, javadocs statement, or comment.  This method calls the 
	 * addProcessedText(String, StyledDocument) to handle coloring source code sections.
	 * @param str A String representing part or all of a source code file.
	 * @param doc The document to add the colorized version of the string to.
	 */
	public void styleDocument(String str, StyledDocument doc)
	{
		SourceCodeTokenizer tokenizer = new SourceCodeTokenizer(str);
		int maxLineNum = tokenizer.getNumberOfLinesInString();
		int numOfDigits = 0;
		while (maxLineNum%(Math.pow(10,numOfDigits)) != maxLineNum)
			numOfDigits++;
		
		String formatStr = "";
		for (int i=0; i<numOfDigits; i++)
			formatStr += "0";
		DecimalFormat numberModifier = new DecimalFormat(formatStr);
		SourceCodeToken token = tokenizer.nextToken();
		while (token != null)
		{
			try
			{
				if (token.isSourceCode())
				{
					for (int i=0; i<token.getStringVec().size(); i++)
					{
						doc.insertString(doc.getLength(),""+numberModifier.format(token.getLineNumberAt(i))+"  ",doc.getStyle("lineNumber"));
						addProcessedText(token.getStringAt(i)+"\n", doc);
					}
				}
				else if (token.isComment())
				{
					for (int i=0; i<token.getStringVec().size(); i++)
					{
						doc.insertString(doc.getLength(),""+numberModifier.format(token.getLineNumberAt(i))+"  ",doc.getStyle("lineNumber"));
						doc.insertString(doc.getLength(),token.getStringAt(i)+"\n",doc.getStyle("comment"));
					}
				}
				else if (token.isJavadocs())
				{
					for (int i=0; i<token.getStringVec().size(); i++)
					{
						doc.insertString(doc.getLength(),""+numberModifier.format(token.getLineNumberAt(i))+"  ",doc.getStyle("lineNumber"));
						doc.insertString(doc.getLength(),token.getStringAt(i)+"\n",doc.getStyle("javadocs"));
					}
				}
			}
			catch (BadLocationException e)
			{
				SystemsManager.printStackTrace(e);
			}
			token = tokenizer.nextToken();
		}
	}
	
	/**
	 * This method adds the string to the document  coloring words as needed.  
	 * Any keywords found in the string are colored blue and the rest of the words 
	 * are colored black.  Thus this method handles coloring only source code sections 
	 * of code (not javadocs statements or comments).
	 * @param str The string to add to the document.
	 * @param doc The document to add the colorized string to.
	 */
	public void addProcessedText(String str, StyledDocument doc)
	{
		String token = "";
		String subToken = "";
		try
		{
			for (int i=0; i<str.length(); i++)
			{
				token = String.valueOf(str.charAt(i));
								
				if (isAnInvisibleString(token))
				{
					/*
					if (token.equals("\t"))
						doc.insertString(doc.getLength(), "  ", null);
					else
					*/
							doc.insertString(doc.getLength(), token, null);
					
				}
				else
				{
					//grab the next char and convert it to a String
					i++;
					if (i<str.length())
						subToken = String.valueOf(str.charAt(i));
					while (!isAnInvisibleString(subToken) && i<str.length())
					{
						token += subToken;
						i++;
						if (i<str.length())
							subToken = String.valueOf(str.charAt(i));
					}
					//now token is the actual word that a person would see
					//now to add that word to the document
					if (isAKeyword(token))
						doc.insertString(doc.getLength(),token+subToken,doc.getStyle("keyword"));
					else
						doc.insertString(doc.getLength(),token+subToken,null);
				}
			}
		}
		catch (BadLocationException e)
		{
			SystemsManager.printStackTrace(e);
		}
	}
	
	/**
	 * Returns true if str is equal to a space, end of line character, tab character, or 
	 * return character.
	 * @param str The String to analyze.
	 * @return True for "invisible" characters (\n, \t, \r, or a space).
	 */
	private boolean isAnInvisibleString(String str)
	{
		if (str.equals(" ") || str.equals("\n") || str.equals("\t") || str.equals("\r"))
			return true;
		else
			return false;
	}
	
	/**
	 * This adds styles to the document.  The style name and color pairs are as follows:
	 * <br> Name            Color
	 * <br> keyword        Blue
	 * <br> comment       Green
	 * <br> javadocs        Red
	 * @param doc
	 */
	public void addStylesToDocument(StyledDocument doc)
	{
		//this is the default style which is the root of the style hierarchy
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style lineNumberStyle = doc.addStyle("lineNumber",def);
		StyleConstants.setForeground(lineNumberStyle,Color.GRAY);
		Style keyword = doc.addStyle("keyword",def);
		StyleConstants.setForeground(keyword,Color.BLUE);
		Style comment = doc.addStyle("comment",keyword);
		StyleConstants.setForeground(comment,Color.GREEN);
		Style javadocs = doc.addStyle("javadocs",comment);
		StyleConstants.setForeground(javadocs,Color.RED);
	}
	
	/**
	 * This returns true if the string supplied is a keyword.  Here are the following keywords:  
	 * abstract, boolean, break, byte, case, catch, char, class, const, continue, default, do, double, 
	 * else, extends, final, finally, float, for, future, generic, goto, if, implements, imports, inner, 
	 * instanceof, int, interface, long, native, new, null, operator, outer, package, private, protected, 
	 * public, rest, return, short, static, super, switch, synchronized, this, throw, throws, transient, 
	 * try, var, void, volatile, while
	 * @param str The string to analyze.
	 * @return True if str is a keyword and false otherwise.
	 */
	protected boolean isAKeyword(String str)
	{
		if (str.equals("abstract") || str.equals("boolean") || str.equals("break") || str.equals("byte") || str.equals("case") || str.equals("catch") || str.equals("char") || str.equals("class") || str.equals("const") ||
		str.equals("continue") || str.equals("default") || str.equals("do") || str.equals("double") || str.equals("else") || str.equals("extends") || str.equals("final") || str.equals("finally") || str.equals("float") || str.equals("for") || 
		str.equals("future") || str.equals("generic") || str.equals("goto") || str.equals("if") || str.equals("implements") || str.equals("import") || str.equals("inner") || str.equals("instanceof") || str.equals("int") || str.equals("interface") || 
		str.equals("long") || str.equals("native") || str.equals("new") || str.equals("null") || str.equals("operator") || str.equals("outer") || str.equals("package") || str.equals("private") || str.equals("protected") || str.equals("public") || 
		str.equals("rest") || str.equals("return") || str.equals("short") || str.equals("static") || str.equals("super") || str.equals("switch") || str.equals("synchronized") || str.equals("this") || str.equals("throw") || str.equals("throws") ||
		str.equals("transient") || str.equals("try") || str.equals("var") || str.equals("void") || str.equals("volatile") || str.equals("while"))
			return true;
		else
			return false;
	}
}