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
 * Revision 1.1  2004/02/07 05:09:14  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.internalFrame;

import java.awt.Color;

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
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class ColorfulTextGUI extends DesktopInternalFrame
{
	public ColorfulTextGUI(HawkDesktop desk)
	{
		super(desk);
	}
	
	public abstract DesktopInternalFrame getCopy();
	
	public void styleDocument(String str, StyledDocument doc)
	{
		SourceCodeTokenizer tokenizer = new SourceCodeTokenizer(str);
		SourceCodeToken token = tokenizer.nextToken();
		while (token != null)
		{
			System.out.println("token.getString()="+token.getString());
			System.out.println("token.getType()="+token.getType());
			
			try
			{
				if (token.isSourceCode())
				{
					addProcessedText(token.getString(), doc);
				}
				else if (token.isComment())
				{
					doc.insertString(doc.getLength(),token.getString(),doc.getStyle("comment"));
				}
				else if (token.isJavadocs())
				{
					doc.insertString(doc.getLength(),token.getString(),doc.getStyle("javadocs"));
				}
			}
			catch (BadLocationException e)
			{
				SystemsManager.printStackTrace(e);
			}
			token = tokenizer.nextToken();
		}
	}

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
					if (token.equals("\t"))
						doc.insertString(doc.getLength(), "  ", null);
					else
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
	
	private boolean isAnInvisibleString(String str)
	{
		if (str.equals(" ") || str.equals("\n") || str.equals("\t") || str.equals("\r"))
			return true;
		else
			return false;
	}
	
	public void addStylesToDocument(StyledDocument doc)
	{
		//this is the default style which is the root of the style hierarchy
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style keyword = doc.addStyle("keyword",def);
		StyleConstants.setForeground(keyword,Color.BLUE);
		Style comment = doc.addStyle("comment",keyword);
		StyleConstants.setForeground(comment,Color.GREEN);
		Style javadocs = doc.addStyle("javadocs",comment);
		StyleConstants.setForeground(javadocs,Color.RED);
	}
	
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