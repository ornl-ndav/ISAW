/*
 * File:  AbstractPrintManager.java
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
import java.io.RandomAccessFile;
import java.util.Vector;

import devTools.Hawk.classDescriptor.gui.frame.ProgressGUI;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;
import devTools.Hawk.classDescriptor.tools.preferences.AbstractPrintingPreferencesManager;
import devTools.Hawk.classDescriptor.tools.preferences.AbstractShortenedPreferencesManager;

/**
 * @author Dominic Kramer
 */
public abstract class AbstractPrintManager extends RandomAccessFile
{
	protected static final String NEW_LINE = "\n";//System.getProperty("line.separator","\n");
	
	//protected Vector intfVec;
	protected ProgressGUI progressGUI;
	protected AbstractPrintingPreferencesManager prefsManager;
	
	public AbstractPrintManager(String filename, ProgressGUI gui) throws FileNotFoundException
	{
		super(filename,"rw");
		//intfVec = interfaceVec;
		progressGUI = gui;
	}
	
	public AbstractPrintManager(String filename, String access, Vector interfaceVec, ProgressGUI gui) throws FileNotFoundException
	{
		super(filename,access);
		//intfVec = interfaceVec;
		progressGUI = gui;
	}
	
	public ProgressGUI getProgressGUI()
	{
		return progressGUI;
	}
	
	public void setProgressGUI(ProgressGUI gui)
	{
		progressGUI = gui;
	}

	public abstract void printIntroduction(int spaces) throws IOException;
	public abstract void printSectionHeader(String text, int spaces) throws IOException;
	public abstract void printSubSectionHeader(String text, int spaces) throws IOException;
	public abstract void printSubSubSectionHeader(String text, int spaces) throws IOException;
	public abstract void printEndHeader(int spaces) throws IOException;
	public abstract void printPackageList(Vector intfVec, int spaces) throws IOException;
	public abstract void finalizePrinting() throws IOException;
	
	//these methods are used to determine if the user's preferences should be overriden or not
	protected abstract void printUML(Interface intf, int spaces, boolean overridePreferences, boolean shortJava, boolean shortOther) throws IOException;
	protected abstract void printShortenedSource(Interface intf, int spaces, boolean overridePreferences, boolean shortJava, boolean shortOther) throws IOException;
	protected void printUMLShortenedSourceGrouped(Interface intf, int spaces, boolean overridePreferences, boolean shortJavaUML, boolean shortOtherUML, boolean shortJavaShortSource, boolean shortOtherShortSource) throws IOException
	{
		printSubSubSectionHeader(intf.getPgmDefn().getInterface_name(),spaces);
		printUML(intf,spaces,overridePreferences,shortJavaUML,shortOtherUML);
		printShortenedSource(intf,spaces,overridePreferences,shortJavaShortSource,shortOtherShortSource);
	}

	//these methods are used to print the information for only one Interface object
	public void printUML(Interface intf, int spaces) throws IOException
	{
		printUML(intf,spaces,false,false,false);
	}
		public void printUML(Interface intf,int spaces, boolean shortJava, boolean shortOther) throws IOException
		{
			printUML(intf,spaces,true,shortJava,shortOther);
		}

	public void printShortenedSource(Interface intf, int spaces) throws IOException
	{
		printShortenedSource(intf,spaces,false,false,false);
	}
		public void printShortenedSource(Interface intf, int spaces, boolean shortJava, boolean shortOther) throws IOException
		{
			printShortenedSource(intf,spaces,true,shortJava,shortOther);
		}

	public void printUMLShortenedSourceGrouped(Interface intf, int spaces) throws IOException
	{
		printUMLShortenedSourceGrouped(intf,spaces,false,false,false,false,false);
	}
		public void printUMLShortenedSourceGrouped(Interface intf, int spaces, boolean shortJavaUML, boolean shortOtherUML, boolean shortJavaShortSource, boolean shortOtherShortSource) throws IOException
		{
			printUMLShortenedSourceGrouped(intf,spaces,true,shortJavaUML,shortOtherUML,shortJavaShortSource,shortOtherShortSource);
		}
	
	//these methods print information for a Vector of Interface objects
	public void printUMLListAlphabetically(Vector intfVec, int spaces) throws IOException
	{
		boolean shortJava = prefsManager.getUMLPrefsManager().getShortenJavaTermsForInterfaces();
		boolean shortOther = prefsManager.getUMLPrefsManager().getShortenNonJavaTermsForInterfaces();
		InterfaceUtilities.alphabatizeVector(intfVec,shortJava,shortOther);
		for (int i=0; i<intfVec.size(); i++)
		{
			printUML((Interface)intfVec.elementAt(i),spaces);
			println();
		}
	}
		public void printUMLListAlphabetically(Vector intfVec, int spaces, boolean shortJava, boolean shortOther) throws IOException
		{
			InterfaceUtilities.alphabatizeVector(intfVec,shortJava,shortOther);
			for (int i=0; i<intfVec.size(); i++)
			{
				printUML((Interface)intfVec.elementAt(i),spaces,shortJava,shortOther);
				println();
			}
		}
		
	public void printUMLListByPackage(Vector intfVec, int spaces) throws IOException
	{
		boolean shortJava = prefsManager.getUMLPrefsManager().getShortenJavaTermsForInterfaces();
		boolean shortOther = prefsManager.getUMLPrefsManager().getShortenNonJavaTermsForInterfaces();
		Vector vec = InterfaceUtilities.getVectorOfVectorOfInterfaces(intfVec,false,false,shortJava,shortOther);
		Vector tempVec = new Vector();
		for (int i=0; i<vec.size(); i++)
		{
			tempVec = (Vector)vec.elementAt(i);
			printSubSectionHeader(((Interface)(tempVec.elementAt(0))).getPgmDefn().getPackage_Name(),spaces);
			for (int j=0; j<tempVec.size(); j++)
			{
				printUML((Interface)tempVec.elementAt(j),spaces);
				println();
			}
		}
	}
		public void printUMLListByPackage(Vector intfVec, int spaces, boolean shortJava, boolean shortOther) throws IOException
		{
			Vector vec = InterfaceUtilities.getVectorOfVectorOfInterfaces(intfVec,false,false,shortJava,shortOther);
			Vector tempVec = new Vector();
			for (int i=0; i<vec.size(); i++)
			{
				tempVec = (Vector)vec.elementAt(i);
				printSubSectionHeader(((Interface)(tempVec.elementAt(0))).getPgmDefn().getPackage_Name(),spaces);
				for (int j=0; j<tempVec.size(); j++)
				{
					printUML((Interface)tempVec.elementAt(j),spaces,shortJava,shortOther);
					println();
				}
			}
		}
		
	public void printShortenedSourceListAlphabetically(Vector intfVec, int spaces) throws IOException
	{
		boolean shortJava = prefsManager.getShortenedSourcePrefsManager().getShortenJavaTermsForInterfaces();
		boolean shortOther = prefsManager.getShortenedSourcePrefsManager().getShortenNonJavaTermsForInterfaces();
		InterfaceUtilities.alphabatizeVector(intfVec,shortJava,shortOther);
		for (int i=0; i<intfVec.size(); i++)
		{
			printShortenedSource((Interface)intfVec.elementAt(i),spaces);
			println();
		}
	}
		public void printShortenedSourceListAlphabetically(Vector intfVec, int spaces, boolean shortJava, boolean shortOther) throws IOException
		{
			InterfaceUtilities.alphabatizeVector(intfVec,shortJava,shortOther);
			for (int i=0; i<intfVec.size(); i++)
			{
				printShortenedSource((Interface)intfVec.elementAt(i),spaces,shortJava,shortOther);
				println();
			}
		}

	public void printShortenedSourceListByPackage(Vector intfVec,int spaces) throws IOException
	{
		boolean shortJava = prefsManager.getUMLPrefsManager().getShortenJavaTermsForInterfaces();
		boolean shortOther = prefsManager.getUMLPrefsManager().getShortenNonJavaTermsForInterfaces();
		Vector vec = InterfaceUtilities.getVectorOfVectorOfInterfaces(intfVec,false,false,shortJava,shortOther);
		Vector tempVec = new Vector();
		for (int i=0; i<vec.size(); i++)
		{
			tempVec = (Vector)vec.elementAt(i);
			printSubSectionHeader(((Interface)(tempVec.elementAt(0))).getPgmDefn().getPackage_Name(),spaces);
			for (int j=0; j<tempVec.size(); j++)
			{
				printShortenedSource((Interface)tempVec.elementAt(j),spaces);
				println();
			}
		}
	}
		public void printShortenedSourceListByPackage(Vector intfVec, int spaces, boolean shortJava, boolean shortOther) throws IOException
		{
			Vector vec = InterfaceUtilities.getVectorOfVectorOfInterfaces(intfVec,false,false,shortJava,shortOther);
			Vector tempVec = new Vector();
			for (int i=0; i<vec.size(); i++)
			{
				tempVec = (Vector)vec.elementAt(i);
				printSubSectionHeader(((Interface)(tempVec.elementAt(0))).getPgmDefn().getPackage_Name(),spaces);
				for (int j=0; j<tempVec.size(); j++)
				{
					printShortenedSource((Interface)tempVec.elementAt(j),spaces,shortJava,shortOther);
					println();
				}
			}
		}
	
	public void printUMLShortenedSourceGroupedAlphabetically(Vector intfVec, int spaces) throws IOException
	{
		InterfaceUtilities.alphabatizeVector(intfVec,false,false);
		for (int i=0; i<intfVec.size(); i++)
		{
			printUMLShortenedSourceGrouped((Interface)intfVec.elementAt(i),spaces);
			println();
		}
	}
		public void printUMLShortenedSourceGroupedAlphabetically(Vector intfVec, int spaces, boolean shortJavaUML, boolean shortOtherUML, boolean shortJavaShortSource, boolean shortOtherShortSource) throws IOException
		{
			InterfaceUtilities.alphabatizeVector(intfVec,false,false);
			for (int i=0; i<intfVec.size(); i++)
			{
				printUMLShortenedSourceGrouped((Interface)intfVec.elementAt(i),spaces,shortJavaUML,shortOtherUML,shortJavaShortSource,shortOtherShortSource);
				println();
			}
		}
	
	public void printUMLShortenedSourceGroupedByPackage(Vector intfVec,int spaces) throws IOException
	{
		Vector vec = InterfaceUtilities.getVectorOfVectorOfInterfaces(intfVec,false,false,false,false);
		Vector tempVec = new Vector();
		for (int i=0; i<vec.size(); i++)
		{
			tempVec = (Vector)vec.elementAt(i);
			printSubSectionHeader(((Interface)(tempVec.elementAt(0))).getPgmDefn().getPackage_Name(),spaces);
			for (int j=0; j<tempVec.size(); j++)
			{
				printUMLShortenedSourceGrouped((Interface)tempVec.elementAt(j),spaces);
				println();
			}
		}
	}
		public void printUMLShortenedSourceGroupedByPackage(Vector intfVec, int spaces, boolean shortJavaUML, boolean shortOtherUML, boolean shortJavaShortSource, boolean shortOtherShortSource) throws IOException
		{
			Vector vec = InterfaceUtilities.getVectorOfVectorOfInterfaces(intfVec,false,false,false,false);
			Vector tempVec = new Vector();
			for (int i=0; i<vec.size(); i++)
			{
				tempVec = (Vector)vec.elementAt(i);
				printSubSectionHeader(((Interface)(tempVec.elementAt(0))).getPgmDefn().getPackage_Name(),spaces);
				for (int j=0; j<tempVec.size(); j++)
				{
					printUMLShortenedSourceGrouped((Interface)tempVec.elementAt(j),spaces,shortJavaUML,shortOtherUML,shortJavaShortSource,shortOtherShortSource);
					println();
				}
			}
		}
		
	//these are helper methods for subclasses
	public void println() throws IOException
	{
		writeBytes(NEW_LINE);
	}
	
	protected boolean getShortenJavaValue(AbstractShortenedPreferencesManager manager, boolean overridePreferences, boolean value)
	{
		if (overridePreferences)
			return value;
		else
			return manager.getShortenJavaTermsForInterfaces();
	}
	
	protected boolean getShortenNonJavaValue(AbstractShortenedPreferencesManager manager, boolean overridePreferences, boolean value)
	{
		if (overridePreferences)
			return value;
		else
			return manager.getShortenNonJavaTermsForInterfaces();
	}
}
