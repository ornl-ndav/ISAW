/*
 * File:  PrintManager.java
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
 * Revision 1.2  2004/03/11 18:56:04  bouzekc
 * Documented file using javadoc statements.
 *
 * Revision 1.1  2004/02/07 05:10:47  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

import java.awt.Container;
import java.awt.FlowLayout;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import devTools.Hawk.classDescriptor.modeledObjects.AttributeDefn;
import devTools.Hawk.classDescriptor.modeledObjects.ConstructorDefn;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.InterfaceDefn;
import devTools.Hawk.classDescriptor.modeledObjects.MethodDefn;

/**
 * This class has general methods used for printing class information to a file.  If you want to print
 * information about classes to a file you should use either ASCIIPrintFileManager or HTMLPrintFileManager.
 * Do not directly use this class.  It is here to make it easier to create sub-classes for printing class information.
 * @author Dominic Kramer
 */
public class PrintManager extends RandomAccessFile
{
	/**
	 * Creates a new PrintManager object.  This essentially makes a RandomAccessFile object.
	 * This constructor is used in subclasses of this class.
	 * @param file The name of the file you want to write to
	 * @param access The read-write access to the file as specified by a RandomAccessFile (i.e. "r", "w", or "rw")
	 * @throws FileNotFoundException
	 */
	protected PrintManager(String file, String access) throws FileNotFoundException
	{
		super(file,access);
	}
	
	/**
	 * Creates a new PrintManager object.  This essentially makes a RandomAccessFile object.
	 * When this constructor is used a file chooser is opened allowing the user to select a file to print to.
	 * This constructor is used in subclasses of this class.
	 * @param access The read-write access to the file as specified by a RandomAccessFile (i.e. "r", "w", or "rw")
	 * @throws FileNotFoundException
	 */
	protected PrintManager(String access) throws FileNotFoundException
	{
		super(getAbsoluteFileName(),access);
	}
	
	/**
	 * This opens a file chooser to allow the user to choose which file to print the information to.
	 * This method is used in the constructor PrintManager(String) to ask the user for a file.
	 * @return The full name of the file selected
	 */
	public static String getAbsoluteFileName()
	{
		String absoluteName = "";
		
		JFrame saveFrame = new JFrame();
		saveFrame.setSize(500,400);
		Container savePane = saveFrame.getContentPane();
			
		JPanel mainSavePanel = new JPanel();
		mainSavePanel.setLayout(new FlowLayout());
			
		JFileChooser saveChooser = new JFileChooser();
		int saveVal = saveChooser.showSaveDialog(saveFrame);
			
		savePane.add(mainSavePanel);
		savePane.setVisible(true);
			
		if (saveVal == JFileChooser.APPROVE_OPTION)
		{
			absoluteName = saveChooser.getSelectedFile().getAbsoluteFile().toString();
		}
		
		return absoluteName;
	}

	/**
	* This returns the number of characters in the longest String from the attributes, methods,
	* constructors, or general information
	*/
	public static int longestLine(Interface intF, boolean shortJava, boolean shortOther)
	{
		int longestString = 0;
			
		longestString = ASCIIPrintFileManager.longestLine(intF.getPgmDefn(), shortJava, shortOther);
			
		for (int i = 0; i < intF.getAttribute_vector().size(); i++)
		{
			int longestAttString = ASCIIPrintFileManager.longestLine(((AttributeDefn)(intF.getAttribute_vector().elementAt(i))), shortJava, shortOther);
			if ( longestAttString > longestString)
				longestString = longestAttString;
		}
			
		for (int i = 0; i < intF.getConst_vector().size(); i++)
		{
			int longestConstString = ASCIIPrintFileManager.longestLine(((ConstructorDefn)(intF.getConst_vector().elementAt(i))), shortJava, shortOther);
			if ( longestConstString > longestString)
				longestString = longestConstString;
		}
			
		for (int i = 0; i < intF.getMethod_vector().size(); i++)
		{
			int longestMethString = ASCIIPrintFileManager.longestLine(((MethodDefn)(intF.getMethod_vector().elementAt(i))), shortJava, shortOther);
			if ( longestMethString > longestString)
				longestString = longestMethString;
		}
			
		return longestString;
	}

	/**
	* This returns the number of characters in the longest String from the general information
	* about the class or interface
	*/
	public static int longestLine(InterfaceDefn intFDefn, boolean shortJava, boolean shortOther)
	{
		return intFDefn.getInterface_name(shortJava, shortOther).length();
	}

	/**
	* This returns the number of characters in the longest String from the attribute(s) in
	* the class or interface
	*/
	public static int longestLine(AttributeDefn attDefn, boolean shortJava, boolean shortOther)
	{
		return 1+attDefn.getAttribute_name(shortJava, shortOther).length()+1+attDefn.getAttribute_type(shortJava, shortOther).length();
		//ex.  +name:String -> for public String name
	}
	/**
	* This returns the number of characters in the longest String from the constructor(s)
	* in the class or interface
	*/
	public static int longestLine(ConstructorDefn constDefn, boolean shortJava, boolean shortOther)
	{
		int totalParameterLength = 0;
			
		for (int i = 0; i < constDefn.getConst_parameter_vector().size(); i++)
		{
			totalParameterLength = totalParameterLength + ((String)(constDefn.getConst_parameter_vector(shortJava, shortOther).elementAt(i))).length() + 2;
				//the extra 2 at the end is for a comma and space
		}
			
		totalParameterLength = totalParameterLength - 2;
			//this is to subtract 2 for the last parameters comma and space otherwise it would look like this
			//ex(int, double, float, String, long, )
			
		return 1+constDefn.getConst_name(shortJava, shortOther).length()+1+totalParameterLength+1;
		//ex.  +Person(int, double, int, float, String)
	}
	
	/**
	* This returns the number of characters in the longest String from the method(s)
	* in the class or interface
	*/
	public static int longestLine(MethodDefn methDefn, boolean shortJava, boolean shortOther)
	{
		int totalParameterLength = 0;
			
		for (int i = 0; i < methDefn.getMethod_parameter_vector().size(); i++)
		{
			totalParameterLength = totalParameterLength + ((String)(methDefn.getMethod_parameter_vector(shortJava, shortOther).elementAt(i))).length() + 2;
				//the extra 2 at the end is for a comma and space
		}
			
		totalParameterLength = totalParameterLength - 2;
			//this is to subtract 2 for the last parameters comma and space otherwise it would look like this
			//ex(int, double, float, String, long, )
			
		return 1+methDefn.getMethod_name(shortJava, shortOther).length()+1+totalParameterLength+1+1+methDefn.getMethod_return_type(shortJava, shortOther).length();
		//ex.  +Person(int, double, int, float, String):void
	}
	
	/**
	 * This returns the name line from a UML diagram
	 * @param intfd An InterfaceDefn object
	 * @param shortJava True if you want to shorten java names
	 * @param shortOther True if you want to shorten non-java names
	 * @return A UML representation of the InterfaceDefn object's name
	 */
	public static String getUMLNameLine(InterfaceDefn intfd, boolean shortJava, boolean shortOther)
	{
		return intfd.getInterface_name(shortJava,shortOther);
	}
	
	/**
	 * This returns the attribute line for the AttributeDefn object in UML format.
	 * @param attDefn The AttributeDefn object to describe
	 * @param shortJava True if you want to shorten java names
	 * @param shortOther True if you want to shorten non-java names
	 * @return A UML representation of the AttributeDefn object 
	 */
	public static String getUMLAttributeLine(AttributeDefn attDefn, boolean shortJava, boolean shortOther)
	{
		Vector charVector = attDefn.getAttribute_char_vector();
			//this is a vector of strings for example ["public" "static" "final"]
		String attLine = "";	
		for (int i = 0; i < charVector.size(); i++) 
			attLine = attLine + InterfaceUtilities.getUMLTermFromJavaTerm( (String)(charVector.elementAt(i)) );
		
		attLine = attLine + attDefn.getAttribute_name(shortJava, shortOther) + ":"+attDefn.getAttribute_type(shortJava, shortOther);
		
		return attLine;
	}
	
	/**
	 * This returns the constructor line for the ConstructorDefn object in UML format.
	 * @param constDefn The ConstructorDefn object to describe
	 * @param shortJava True if you want to shorten java names
	 * @param shortOther True if you want to shorten non-java names
	 * @return A UML representation of the ConstructorDefn object 
	 */
	public static String getUMLConstructorLine(ConstructorDefn constDefn, boolean shortJava, boolean shortOther)
	{
		Vector charVector = constDefn.getConst_char_vector();
			//this is a vector of strings for example ["public" "static" "final"]
		String constLine = "";	
		for (int i = 0; i < charVector.size(); i++) 
			constLine = constLine + InterfaceUtilities.getUMLTermFromJavaTerm( (String)(charVector.elementAt(i)) );
		
		constLine = constLine + constDefn.getConst_name(shortJava, shortOther) + "(";
				
		int size = constDefn.getConst_parameter_vector().size();
				
		for (int i = 0; i < size; i++)
		{
			constLine = constLine + ( (String)(constDefn.getConst_parameter_vector(shortJava, shortOther).elementAt(i)) );
					
			if (i != (size - 1) )
			{
				constLine = constLine + ", ";
			}
		}
				
		constLine = constLine + ")";
		
		return constLine;
	}
	
	/**
	 * This returns the method line for the MethodDefn object in UML format.
	 * @param methDefn The MethodDefn object to describe
	 * @param shortJava True if you want to shorten java names
	 * @param shortOther True if you want to shorten non-java names
	 * @return A UML representation of the MethodDefn object 
	 */
	public static String getUMLMethodLine(MethodDefn methDefn, boolean shortJava, boolean shortOther)
	{
		Vector charVector = methDefn.getMethod_char_vector();
			//this is a vector of strings for example ["public" "static" "final"]
		
		String methLine = "";
		
		for (int i = 0; i < charVector.size(); i++) 
			methLine = methLine + InterfaceUtilities.getUMLTermFromJavaTerm( (String)(charVector.elementAt(i)) );
	
		methLine = methLine + methDefn.getMethod_name(shortJava, shortOther) + "(";
			
		int size = methDefn.getMethod_parameter_vector().size();
			
		for (int i = 0; i < size; i++)
		{
			methLine = methLine + ( (String)(methDefn.getMethod_parameter_vector(shortJava, shortOther).elementAt(i)) );
				
			if (i != (size - 1) )
			{
				methLine = methLine + ", ";
			}
		}
			
		methLine = methLine + "):" + methDefn.getMethod_return_type(shortJava, shortOther);
			
		return methLine;
	}

}