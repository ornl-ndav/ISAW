/*
 * File:  ColorfulTextUtilities.java
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
 package devTools.Hawk.classDescriptor.gui;

import java.text.DecimalFormat;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;
import devTools.Hawk.classDescriptor.tools.SourceCodeToken;
import devTools.Hawk.classDescriptor.tools.SourceCodeTokenizer;
import devTools.Hawk.classDescriptor.tools.SystemsManager;
import devTools.Hawk.classDescriptor.tools.preferences.AbstractColorfulPreferencesManager;

/**
 * @author kramer
 */
public class ColorfulTextUtilities
{
	private ColorfulTextUtilities(){}
	
	/**
	 * This method takes the filename supplied and uses a SourceCodeTokenizer 
	 * to break the file into source code, comment, and javadocs sections.  Each 
	 * section is added to the StyledDocument with the correct color depending if the 
	 * section is source code, javadocs statement, or comment.  This method calls the 
	 * addProcessedText(String, StyledDocument) to handle coloring source code sections.
	 * @param str The name of the file.
	 * @param doc The document to add the colorized version of the string to.
	 */
	public static long styleDocument(String str, StyledDocument doc)
	{
			SourceCodeTokenizer tokenizer = new SourceCodeTokenizer(str);
			SourceCodeToken token = tokenizer.nextToken();
			while (token != null)
			{
				try
				{
					if (token.isSourceCode())
							addProcessedText(token.toString(), doc);
					else if (token.isComment())
							doc.insertString(doc.getLength(),token.toString(),doc.getStyle("comment"));
					else if (token.isJavadocs())
							doc.insertString(doc.getLength(),token.toString(),doc.getStyle("javadocs"));
				}
				catch (BadLocationException e)
				{
					SystemsManager.printStackTrace(e);
				}
				token = tokenizer.nextToken();
			}
		
		return tokenizer.getNumberOfLines();
	}
	
	/**
	 * This method adds the string to the document  coloring words as needed.  
	 * Any keywords found in the string are colored blue and the rest of the words 
	 * are colored black.  Thus this method handles coloring only source code sections 
	 * of code (not javadocs statements or comments).
	 * @param str The string to add to the document.
	 * @param doc The document to add the colorized string to.
	 */
	public static void addProcessedText(String str, StyledDocument doc)
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
					if (InterfaceUtilities.isAJavaKeyword(token))
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
	private static boolean isAnInvisibleString(String str)
	{
		return ((str.equals(" ") || str.equals("\n") || str.equals("\t") || str.equals("\r")));
	}
	
	/**
	 * This adds styles to the document.  The style name and color pairs are as follows:
	 * <br> Name            Color
	 * <br> keyword        Blue
	 * <br> comment       Green
	 * <br> javadocs        Red
	 * @param doc
	 */
	public static void addStylesToDocument(StyledDocument doc, AbstractColorfulPreferencesManager manager)
	{
		//this is the default style which is the root of the style hierarchy
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style lineNumberStyle = doc.addStyle("lineNumber",def);
		StyleConstants.setForeground(lineNumberStyle,manager.getLineNumberColor());
		Style keyword = doc.addStyle("keyword",def);
		StyleConstants.setForeground(keyword,manager.getKeywordColor());
		Style comment = doc.addStyle("comment",keyword);
		StyleConstants.setForeground(comment,manager.getSlashSlashColor());
		Style javadocs = doc.addStyle("javadocs",comment);
		StyleConstants.setForeground(javadocs,manager.getJavadocsColor());
	}
	
	public static JTextPane getNumberedJPanel(long maxLineNum, AbstractColorfulPreferencesManager manager)
	{
		JTextPane pane = new JTextPane();
		StyledDocument doc = pane.getStyledDocument();
		addStylesToDocument(doc, manager);
		
		int numOfDigits = 0;
		while (maxLineNum%(Math.pow(10,numOfDigits)) != maxLineNum)
			numOfDigits++;
		String formatStr = "";
		for (int i=0; i<numOfDigits; i++)
			formatStr += "0";
		DecimalFormat numberModifier = new DecimalFormat(formatStr);
		
		try
		{
			for (int i=1; i<=maxLineNum; i++)
				doc.insertString(doc.getLength(),""+i+"\n",doc.getStyle("lineNumber"));
		}
		catch (Throwable t)
		{
			SystemsManager.printStackTrace(t);
		}
		
		return pane;
	}
}
