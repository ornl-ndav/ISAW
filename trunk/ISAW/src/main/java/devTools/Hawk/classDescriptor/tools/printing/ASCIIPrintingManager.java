/*
 * File:  ASCIIPrintingManager.java
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import devTools.Hawk.classDescriptor.gui.frame.ProgressGUI;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;
import devTools.Hawk.classDescriptor.tools.preferences.ASCIIPrintingPreferencesManager;

/**
 * @author Dominic Kramer
 */
public class ASCIIPrintingManager extends AbstractPrintManager
{
	
	public ASCIIPrintingManager(String filename, ProgressGUI gui) throws FileNotFoundException
	{
		super(filename,gui);
		System.out.println("In devTools.Hawk.classDescriptor.tools.printing.ASCIIPrintingManager");
		prefsManager = new ASCIIPrintingPreferencesManager();
	}
	
	public ASCIIPrintingPreferencesManager getPreferencesManager()
	{
		return (ASCIIPrintingPreferencesManager)prefsManager;
	}

	public void printIntroduction(int spaces) throws IOException
	{
		String tab = getIndent(spaces);
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(tab);
		buffer.append("Title:  ");
		buffer.append(prefsManager.getTitle());
		buffer.append(NEW_LINE);
		buffer.append(tab);
		buffer.append("Date:  ");
		buffer.append(prefsManager.getDate());
		buffer.append(NEW_LINE);
		buffer.append(tab);
		buffer.append("Author:  ");
		buffer.append(prefsManager.getAuthor());
		buffer.append(NEW_LINE);
		buffer.append(tab);
		buffer.append("Description:  ");
		StringTokenizer tokenizer = new StringTokenizer(prefsManager.getDescription(),NEW_LINE);
		while (tokenizer.hasMoreTokens())
		{
			buffer.append(tab);
			buffer.append(tokenizer.nextToken());
		}
		writeBytes(buffer.toString());
	}

	public void printSectionHeader(String text, int spaces) throws IOException
	{
		String tab = getIndent(spaces);
		String line = getLine("#",getPreferencesManager().getPageWidth()-spaces);
		String spacer = getLine(" ",getPreferencesManager().getPageWidth()-2-text.length()-spaces);
		StringBuffer buffer = new StringBuffer(tab);
		buffer.append(line);
		buffer.append(NEW_LINE);
		buffer.append(tab);
		buffer.append("#");
		buffer.append(text);
		buffer.append(spacer);
		buffer.append("#");
		buffer.append(NEW_LINE);
		buffer.append(tab);
		buffer.append(line);
		
		writeBytes(buffer.toString());
	}

	public void printSubSectionHeader(String text, int spaces) throws IOException
	{
		String tab = getIndent(spaces);
		String line = getLine("=",getPreferencesManager().getPageWidth()-2-spaces);
		String spacer = getLine(" ",getPreferencesManager().getPageWidth()-2-text.length()-spaces);
		StringBuffer buffer = new StringBuffer(tab);
		buffer.append("+");
		buffer.append(line);
		buffer.append("+");
		buffer.append(NEW_LINE);
		buffer.append(tab);
		buffer.append("|");
		buffer.append(text);
		buffer.append(spacer);
		buffer.append("|");
		buffer.append(NEW_LINE);
		buffer.append(tab);
		buffer.append("+");
		buffer.append(line);
		buffer.append("+");
		
		writeBytes(buffer.toString());
	}

	public void printSubSubSectionHeader(String text, int spaces) throws IOException
	{
		String tab = getIndent(spaces);
		String line = getLine("-",getPreferencesManager().getPageWidth()-2-spaces);
		String spacer = getLine(" ",getPreferencesManager().getPageWidth()-2-text.length()-spaces);
		StringBuffer buffer = new StringBuffer(tab);
		buffer.append("+");
		buffer.append(line);
		buffer.append("+");
		buffer.append(NEW_LINE);
		buffer.append(tab);
		buffer.append("|");
		buffer.append(text);
		buffer.append(spacer);
		buffer.append("|");
		buffer.append(NEW_LINE);
		buffer.append(tab);
		buffer.append("+");
		buffer.append(line);
		buffer.append("+");
		
		writeBytes(buffer.toString());
	}

	public void printEndHeader(int spaces) throws IOException
	{
		String tab = getIndent(spaces);
		StringBuffer buffer = new StringBuffer(tab);
		writeBytes(getLine("*",getPreferencesManager().getPageWidth()-spaces));
	}

	public void printPackageList(Vector intfVec, int spaces) throws IOException
	{
		String tab = getIndent(spaces);
		Vector vec = InterfaceUtilities.getPackageListVector(intfVec);
		StringBuffer buffer = new StringBuffer();
		for (int i=0; i<vec.size(); i++)
		{
			buffer.append(tab);
			buffer.append((String)vec.elementAt(i));
			buffer.append(NEW_LINE);
		}
		
		writeBytes(buffer.toString());
	}

	public void finalizePrinting() throws IOException
	{
		close();
	}

	protected void printUML(Interface intf, int spaces, boolean overridePreferences, boolean shortJava, boolean shortOther) throws IOException
	{
		String tab = getIndent(spaces);
		boolean newShortJava = getShortenJavaValue(getPreferencesManager().getUMLPrefsManager(),overridePreferences,shortJava);
		boolean newShortOther = getShortenNonJavaValue(getPreferencesManager().getUMLPrefsManager(),overridePreferences,shortOther);
		writeBytes(intf.getSingleUMLAsString(tab,newShortJava,newShortOther));
	}

	protected void printShortenedSource(Interface intf, int spaces, boolean overridePreferences, boolean shortJava, boolean shortOther) throws IOException
	{
		String tab = getIndent(spaces);
		boolean newShortJava = getShortenJavaValue(getPreferencesManager().getShortenedSourcePrefsManager(),overridePreferences,shortJava);
		boolean newShortOther = getShortenNonJavaValue(getPreferencesManager().getShortenedSourcePrefsManager(),overridePreferences,shortOther);
		writeBytes(intf.getShortenedSourceCode(tab,newShortJava,newShortOther));
	}
	
	//these are helper methods
	private String getIndent(int size)
	{
		StringBuffer tabBuffer = new StringBuffer();
		for (int i=1; i<=size; i++)
			tabBuffer.append(" ");
		return tabBuffer.toString();
	}
	
	private int getTabSize(boolean overridePreferences, int size)
	{
		if (overridePreferences)
			return size;
		else
			return getPreferencesManager().getTabSize();
	}
	
	private String getLine(String character)
	{
		System.out.println(getLine(character,false,0));
		return getLine(character,false,0);
	}
	
	private String getLine(String character, int newTabSize)
	{
		return getLine(character,true,newTabSize);
	}
	
	private String getLine(String character, boolean override, int newWidth)
	{
		int pageWidth = getPreferencesManager().getPageWidth();
		if (override)
			pageWidth = newWidth;
			
		StringBuffer separator = new StringBuffer();
		for (int i=1; i<=pageWidth; i++)
			separator.append(character);

		return separator.toString();
	}
}
