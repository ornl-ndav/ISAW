/*
 * File:  ASCIIPrintFileManager.java
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
 * Revision 1.3  2004/03/12 19:46:19  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:10:45  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import devTools.Hawk.classDescriptor.gui.frame.ProgressGUI;
import devTools.Hawk.classDescriptor.modeledObjects.AttributeDefn;
import devTools.Hawk.classDescriptor.modeledObjects.ConstructorDefn;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.MethodDefn;

/**
 * This class is used to write information about classes and interfaces to a file in ASCII format.
 * @author Dominic Kramer
 */
public class ASCIIPrintFileManager extends PrintManager
{
	/**
	 * Constructor which creates an ASCIIPrintFileManager object to print to the file "file" with access "access"
	 * @param file The file to print to
	 * @param access The file's read-write access (i.e. "r", "w", or "rw")
	 * @throws FileNotFoundException
	 */
	public ASCIIPrintFileManager(String file, String access) throws FileNotFoundException
	{
		super(file, access);
	}
	
	/**
	 * Constructor which creates an ASCIIPrintFileManager object.  This constructor opens a file chooser to allow the 
	 * user to choose where to print the information to.
	 * @param access The read-write access (i.e. "r", "w", or "rw")
	 * @throws FileNotFoundException
	 */
	public ASCIIPrintFileManager(String access) throws FileNotFoundException
	{
		super(access);
	}
	
	/**
	 * This adds a shortened source code representation of the Interface object intFd to the file.
	 * @param tabSize  The number of spaces you want the UML diagram to be moved in from the left of the page
	 * @param intF The Inteface object to print
	 * @param shortJava True if you want java names to be shortened
	 * @param shortOther True if you want non-java names to be shortened
	 */
	public void printShortenedSource(int tabSize, Interface intF, boolean shortJava, boolean shortOther)
	{
		try
		{
			StringBuffer buffer = new StringBuffer();
			String shortenedSource = "";
			
			StringBuffer tabBuffer = new StringBuffer();
			for (int j = 1; j<=tabSize; j++)
				tabBuffer.append(" ");
			
			String tab = tabBuffer.toString();
							
				shortenedSource = intF.getStringInJavadocFormat(shortJava, shortOther);
				StringTokenizer tokenizer = new StringTokenizer(shortenedSource,"\n");
				while (tokenizer.hasMoreTokens())
					buffer.append(tab + tokenizer.nextToken() + "\n");

				buffer.append("\n");
	
			writeBytes(buffer.toString());
		}
		catch(IOException e)
		{
			SystemsManager.printStackTrace(e);
		}
	}
	
		/**
		 * This prints the section heading.  Like the following:\n
		 * =====================================\n
		 * Section "num":  "title"\n
		 * =====================================\n
		 * If -1 is supplied as num or null is supplied as title then 
		 * the area of the section containing that information is not printed.
		 * @param num The section number
		 * @param len The number of characters in one line
		 * @param title The section title
		 * @return The string which is specially formatted so that
		 * when pasted into an editor it looks like the description
		 */
		public void printSectionHeading(int num, int len, String title)
		{
			try
			{
				StringBuffer buffer = new StringBuffer();
				for (int i = 1; i <= len; i++)
					buffer.append("=");
				
				buffer.append("\n");
				
				if (num >= 0)
				{
					buffer.append("Section " + num+":");
				}
				
				if (title != null)
				{
					buffer.append("  " + title);
				}
				
				buffer.append("\n");
				
				for (int i = 1; i <= len; i++)
					buffer.append("=");
					
				buffer.append("\n");
				
				writeBytes(buffer.toString());
			}
			catch(IOException e)
			{
				SystemsManager.printStackTrace(e);
			}
		}
		
		/**
		 * Creates the divider that signals the end of a project
		 * @param len The number of characters in one line
		 * @return  The divider 
		 * "\n===============\n"
		 * "\n===============\n"
		 * "\n===============\n"
		 */
		public void printEndDivider(int len)
		{
			try
			{
				for (int j=1; j<=2; j++)
				{
					for (int i=0; i<len; i++)
					{
						writeBytes("=");
					}
					writeBytes("\n");
				}
			}
			catch(IOException e)
			{
				SystemsManager.printStackTrace(e);
			}
		}
		
		/**
		 * Prints the introduction.
		 * @param title The title of the document printed.
		 * @param date The date the document was printed.
		 * @param author The author of the document printed
		 * @param description A description of the document printed.
		 */
		public void printIntroduction(String title, String date, String author, String description)
		{
			try
			{
				StringBuffer buffer = new StringBuffer();
				buffer.append("Title:  "+title+"\n");
				buffer.append("Date:  "+date+"\n");
				buffer.append("Author:  "+author+"\n");
				buffer.append("Description:  ");
				StringTokenizer tokenizer = new StringTokenizer(description, "\n");
				String tab = "              ";
				boolean writeTab = false;
				while (tokenizer.hasMoreTokens())
				{				
					if (writeTab)
						buffer.append(tab);
						
					buffer.append(tokenizer.nextToken()+"\n");
					writeTab = true;
				}
				buffer.append("\n");
				writeBytes(buffer.toString());
			}
			catch(IOException e)
			{
				SystemsManager.printStackTrace(e);
			}
		}
		
		/**
		 * This prints a sub-section heading in the format <br>
		 * "description"<br>"-----------"
		 * @param tabSize The number of spaces that the printout is indented from the left of the page
		 * @param len The number of characters in one line
		 * @param description The description to print
		 */
		public void printSubSectionHeading(int tabSize, int len, String description)
		{
			try
			{
				StringBuffer buffer = new StringBuffer();
				StringBuffer tabBuffer = new StringBuffer();
				
				for (int i = 1; i<=tabSize; i++)
					tabBuffer.append(" ");
					
				String tab = tabBuffer.toString();	
				
				buffer.append(tab);
				
				for (int i = 1; i<(len-tabSize); i++)
					buffer.append("-");
				
				buffer.append("\n"+tab+description+"\n");
				
				for (int i = 1; i<(len-tabSize); i++)
					buffer.append("-");
					
				buffer.append("\n");
				writeBytes(buffer.toString());
			}
			catch(IOException e)
			{
				SystemsManager.printStackTrace(e);
			}
		}
		
		/**
		 * Returns a list of package names formatted for one package
		 * per line
		 * @param vec A vector of Interfaces
		 * @return The formatted String
		 */
		public void printPackageList(int tabSize, Vector vec, ProgressGUI gui)
		{
			try
			{
				//this vector contains the unique package names
					Vector uniqueVec = InterfaceUtilities.getPackageListVector(vec);
								
				StringBuffer tabBuffer = new StringBuffer();
				for (int k = 1; k<=tabSize; k++)
					tabBuffer.append(" ");
				String tab = tabBuffer.toString();
				String oldText = gui.getText();
				int i=0;	
				while (i < uniqueVec.size() && !gui.isCancelled())
				{
					writeBytes(tab + (String)uniqueVec.elementAt(i) + "\n");
					gui.setValue(i+1);
					gui.setText(oldText + "Printing package "+(i+1)+" of "+uniqueVec.size()+"\n");
					i++;
				}
			}
			catch(IOException e)
			{
				SystemsManager.printStackTrace(e);
			}
		}
		
		/**
		 * This prints the shortened source code representations of each of the Interface objects in the Vector "vec" as they are read from the
		 * Vector.  This method takes a reference to a ProgressGUI used to display the progress of the printout.
		 * @param tabSize The number of space to indent the source code from the left side of the page
		 * @param vec A Vector of Interface objects
		 * @param shortJava True if you want to shorten java names
		 * @param shortOther True if you want to shorten non-java names
		 * @param gui The ProgressGUI to print the status of the printout as it is being printed.  Note:  you need to create the ProgressGUI in
		 * one thread and have this method working in a background thread to have the ProgressGUI print information correctly.
		 * @param increment Each time an Inteface object's info from the Vector vec is printed, increment is the amount of change to add to the
		 * progress gui's progress bar.
		 */
		public void printShortenedSource(int tabSize, Vector vec, boolean shortJava, boolean shortOther, ProgressGUI gui, int increment)
		{
			try
			{
				String shortenedSource = "";
				StringBuffer tabBuffer = new StringBuffer();
				for (int j = 1; j<=tabSize; j++)
					tabBuffer.append(" ");
				String tab = tabBuffer.toString();
				int i=0;
				int currentVal = 0;
				String oldProgressText = gui.getText();
				while (i<vec.size() && !gui.isCancelled())
				{
					StringBuffer buffer = new StringBuffer();
					shortenedSource = ((Interface)vec.elementAt(i)).getStringInJavadocFormat(shortJava, shortOther);
					StringTokenizer tokenizer = new StringTokenizer(shortenedSource,"\n");
					while (tokenizer.hasMoreTokens())
					{
						buffer.append(tab + tokenizer.nextToken() + "\n");
					}
					buffer.append("\n");
				
					writeBytes(buffer.toString());
					
					i++;
					currentVal = gui.getValue() + increment;
					gui.setValue(currentVal);
					gui.setText(oldProgressText+"Printing the shortened source for class "+i+" of "+vec.size()+"\n");
				}
			}
			catch(IOException e)
			{
				SystemsManager.printStackTrace(e);
			}
		}

	/**
	 * This prints the shortened source code representations of each of the Interface objects in the Vector "vec" alphabetically.  
	 * This method takes a reference to a ProgressGUI used to display the progress of the printout.
	 * @param tabSize The number of space to indent the source code from the left side of the page
	 * @param vec A Vector of Interface objects
	 * @param shortJava True if you want to shorten java names
	 * @param shortOther True if you want to shorten non-java names
	 * @param gui The ProgressGUI to print the status of the printout as it is being printed.  Note:  you need to create the ProgressGUI in
	 * one thread and have this method working in a background thread to have the ProgressGUI print information correctly.
	 * @param increment Each time an Inteface object's info from the Vector vec is printed, increment is the amount of change to add to the
	 * progress gui's progress bar.
	 */
		public void printShortenedSourceAlphabetically(int tabSize, Vector vec, boolean shortJava, boolean shortOther, ProgressGUI gui, int increment)
		{
			// now to alphabatize the vector
				InterfaceUtilities.alphabatizeVector(vec, true, true);
				
			printShortenedSource(tabSize, vec, shortJava, shortOther, gui, increment);
		}
		
		/**
		 *  Returns the formatted String which is the Table of Contents.  For a given 
		 *  index i, then numArr[i] is the section number and stringVec.elementAt(i) is
		 *  the section's description.  numArr and stringVec must have the same number
		 *  of elements.
		 * @param tabSize The number of characters in a tab
		*  @param numArr The array of section numbers
		 *  * @param stringVec A vector of Strings each of which is a section description
		 * @return The table of contents
		 */
		public void printTableOfContents(int tabSize, int[] numArr, Vector stringVec)
		{
			try
			{
				StringBuffer buffer = new StringBuffer();
				StringBuffer tabBuffer = new StringBuffer();
				for (int i = 1; i <= tabSize; i++)
					tabBuffer.append(" ");
				String tab = tabBuffer.toString();
			
				for (int i = 0; i < stringVec.size(); i++)
					buffer.append(tab + "Section " + numArr[i] + ":  " + (String)(stringVec.elementAt(i)) + "\n");
			
				writeBytes(buffer.toString());
			}
			catch(IOException e)
			{
				SystemsManager.printStackTrace(e);
			}
		}
	
	public static int[] getDimension(Interface intF, boolean shortJava, boolean shortOther)
	{
		int[] numberArr = {0,0};

			String uml = intF.getSingleUMLAsString(shortJava, shortOther);
			StringTokenizer tokenizer = new StringTokenizer(uml,"\n");
			int temp = 0;
			
			while (tokenizer.hasMoreTokens())
			{
				temp = numberArr[0];
				temp++;
				numberArr[0] = temp;
				tokenizer.nextToken();
			}
	
		numberArr[1] = longestLine(intF, shortJava, shortOther) + 4;
		//from manually counting the number of characters
		//I have found that longestLine(intF) returns the number of characters minus 4
	
		return numberArr;
	}	
	
	public static Vector getSingleUMLAsVector(Interface intF, boolean shortJava, boolean shortOther)
	{
		Vector lineVec = new Vector(getDimension(intF, shortJava, shortOther)[0]);
		
		String uml = intF.getSingleUMLAsString(shortJava, shortOther);
		StringTokenizer tokenizer = new StringTokenizer(uml, "\n");
		
		int i=0;
		while (tokenizer.hasMoreTokens())
		{
			lineVec.add(tokenizer.nextToken());
			i++;
		}
		
		return lineVec;
	}
	
	public Vector getFileAsAVector()
	{
		Vector vec = new Vector();
		
		try
		{
			String line = readLine();
			while (line != null)
			{
				vec.add(line);
				line = readLine();
			}
		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in getFileAsAnArray() in ASCIIPrintFileManager.java");
			System.err.println(e);			
		}
		
		return vec;
	}
	
	/**
	 * This inserts a String at the current file pointer
	 * @param str The String to insert
	 */
	public void insert(String str)
	{
		try
		{	
			long initialFilePointer = getFilePointer();
			byte[] byteArray = new byte[(int)(initialFilePointer)];
			seek(0);
			readFully(byteArray, 0, (int)(initialFilePointer));
			String preLines = "";
			for (int i=0; i<byteArray.length; i++)
				preLines = preLines + (char)byteArray[i];
			
			byte[] byteArray2 = new byte[(int)length()-(int)(initialFilePointer)];
			seek(initialFilePointer);
			readFully(byteArray2);
			String postLines = "";
			for (int i=0; i<byteArray2.length; i++)
				postLines = postLines + (char)byteArray2[i];
				
			seek(0);
			writeBytes(preLines+str+postLines);
		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in printUMLOnSameLevel(Interface, int, long) in ASCIIPrintFileManager.java");
			System.err.println(e);			
		}
		
	}
	
	/**
	 * This method takes Vector of Interface objects and alphabatizes them.  Then it prints a UML diagram for each class or 
	 * interface to the file in alphabetical order.
	 * @param vec A Vector of Interface objects.
	 * @param tabSize The number of spaces that a tab contains.
	 * @param shortJava Set this to true if java names are to be shortened.  For example, 
	 * java.lang.String will be shortened to String.
	 * @param shortOther Set this to true if non-java names are to be shortened.  
	 * @param gui The window that dispalys the progress of the method as it writes the data to the file.
	 */
	public void printUMLDiagramsAlphabetically(Vector vec, int tabSize, boolean shortJava, boolean shortOther, ProgressGUI gui)
	{
		try
		{
			//now to alphabatize the vector
				InterfaceUtilities.alphabatizeVector(vec, true, true);
				String tab = "";
				int currentValue = gui.getValue();
				for (int i=0; i<tabSize; i++)
					tab += " ";
				String oldText = gui.getText();
				for (int i=0; i<vec.size(); i++)
				{
					Interface intf = (Interface)vec.elementAt(i);
					intf.printSingleUMLAsString(this,tab,shortJava,shortOther);
					writeBytes("\n");
					currentValue++;
					gui.setValue(currentValue);
					gui.setText(oldText+"Printing the UML diagram for class "+(i+1)+" of "+vec.size()+"\n");					
				}
/*
			String tab = "";
			for (int k=0; k<tabSize; k++)
				tab = tab + " ";
			long firstLineOffset = getFilePointer();
			long lastLineOffset = getFilePointer();
			long firstLineOffsetAfterUML = tabSize;
			int tallest = 0;
			int[] sizeArr = {0,0};
			Vector umlVec = new Vector();
			int currentVal = 0;
			int i = 0;
			
			while (i<vec.size() && !gui.isCancelled())
			{
				sizeArr = getDimension((Interface)vec.elementAt(i), shortJava, shortOther);
				firstLineOffsetAfterUML = firstLineOffset+sizeArr[1];
				
				if (firstLineOffsetAfterUML <= len)
				{
					seek(firstLineOffset);
					umlVec = getSingleUMLAsVector((Interface)vec.elementAt(i), shortJava, shortOther);
					
					if (i == 0)
						insert(tab+(String)umlVec.elementAt(0)+"\n");
					else
						insert(" "+(String)umlVec.elementAt(0)+"\n");
					firstLineOffset = getFilePointer();
					skipBytes((int)firstLineOffset);
					
					for (int j=1; j<umlVec.size(); j++)
					{
						if (i == 0)
							insert(tab+(String)umlVec.elementAt(j)+"\n");
						else
							insert(" "+(String)umlVec.elementAt(j)+"\n");
						skipBytes((int)firstLineOffset);
					}
					
					if (umlVec.size() > tallest)
							tallest = umlVec.size();
					else
					{
						String filler = "";
						for (int k=0; k<sizeArr[1]+1; k++)
							filler = filler + " ";
						
						for (int I=0; I<tallest-umlVec.size(); I++)
							writeBytes(filler);
					}
					
					lastLineOffset = getFilePointer();
				}
				else
				{
					seek(lastLineOffset);
					umlVec = getSingleUMLAsVector((Interface)vec.elementAt(i), shortJava, shortOther);
					
					if (umlVec.size() > tallest)
						tallest = umlVec.size();
					writeBytes(tab+(String)umlVec.elementAt(0)+"\n");
					firstLineOffset = getFilePointer();
					for (int j=1; j<umlVec.size(); j++)
					{
						writeBytes(tab+(String)umlVec.elementAt(j)+"\n");
					}
					lastLineOffset = getFilePointer();
				}
				
				i++;
				currentVal = gui.getValue() + increment;
				gui.setValue(currentVal);
			}
*/

		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in printUMLDiagramsAlphabetically(Vector, int, int) in ASCIIPrintFileManager.java");
			System.err.println(e);			
		}
	}
	
	/**
	 * This method will print a sequence of dashes with | characters on the ends (|--------|).  This method is used in the method 
	 * printSingleUML(Interface intF, boolean shortJava, boolean shortOther) to help print the 
	 * UML diagram.
	 * @param intF The interface whose data is written.
	 * @param shortJava Set this to true if the java names in the data for the interface are to be shortened.  For example, 
	 * java.lang.String will be shortened to String.  If you do not set the correct parameter here the sequence of dashes will 
	 * not be the correct size.
	 * @param shortOther Set this to true if the java names in the data for the interface are to be shortened.  If you do not 
	 * set the correct parameter here the sequence of dashes will not be the correct size.
	 * @return A sequence of dashes.
	 */
	public static String getOuterDivider(Interface intF, boolean shortJava, boolean shortOther)
	{
		StringBuffer buffer = new StringBuffer(" ");
			
		//here you might think that i<=PrintFileManager.longestLine(intF)-2
		//however, I am adding 2 to that number to accomidate one space 
		//before and after a string is written to make the printout look nicer
		//and adding another 2 for the | before and | after each printed line
		//to make the text look like it is printed in a box
		//ex.  ----------
		//    | Name     |
		for (int i = 1; i <= ASCIIPrintFileManager.longestLine(intF, shortJava, shortOther)+2; i++)
			buffer.append("-");
				
		buffer.append(" ");
		return buffer.toString();
		//ex.  line1 = " --------- "
	}
	
	/**
	 * This method will print the line in the UML diagram that contains the interface's name.  This method is used in the method 
	 * printSingleUML(Interface intF, boolean shortJava, boolean shortOther) to help print the 
	 * UML diagram.
	 * @param intF The interface whose data is written.
	 * @param shortJava Set this to true if the java names in the data for the interface are to be shortened.  For example, 
	 * java.lang.String will be shortened to String.  If you do not set the correct parameter here the sequence of dashes will 
	 * not be the correct size.
	 * @param shortOther Set this to true if the java names in the data for the interface are to be shortened.  If you do not 
	 * set the correct parameter here the sequence of dashes will not be the correct size.
	 * @return A sequence of dashes.
	 */
	public static String getInterfaceNameLine(Interface intF, boolean shortJava, boolean shortOther)
	{
		StringBuffer buffer = new StringBuffer("| " + intF.getPgmDefn().getInterface_name(shortJava, shortOther));
		int num = buffer.length();
			
		for (int i = 1; i <= ASCIIPrintFileManager.longestLine(intF, shortJava, shortOther)-num+2; i++)
			buffer.append(" ");
				
		buffer.append(" |");
		return buffer.toString();
		
	}
	
	/**
	 * This method will print a sequence of dashes (--------).  This method is used in the method 
	 * printSingleUML(Interface intF, boolean shortJava, boolean shortOther) to help print the 
	 * UML diagram.
	 * @param intF The interface whose data is written.
	 * @param shortJava Set this to true if the java names in the data for the interface are to be shortened.  For example, 
	 * java.lang.String will be shortened to String.  If you do not set the correct parameter here the sequence of dashes will 
	 * not be the correct size.
	 * @param shortOther Set this to true if the java names in the data for the interface are to be shortened.  If you do not 
	 * set the correct parameter here the sequence of dashes will not be the correct size.
	 * @return A sequence of dashes.
	 */	public static String getInnerDivider(Interface intF, boolean shortJava, boolean shortOther)
	{
		StringBuffer buffer = new StringBuffer("|");
			
		for (int i = 1; i <= ASCIIPrintFileManager.longestLine(intF, shortJava, shortOther)+2; i++)
			buffer.append("-");
			
		buffer.append("|");
		return buffer.toString();
	}
	
	/**
	 * This method will print a sequence of spaces with | characters on the ends ( |         | ).  This method is used in the method 
	 * printSingleUML(Interface intF, boolean shortJava, boolean shortOther) to help print the 
	 * UML diagram.
	 * @param intF The interface whose data is written.
	 * @param shortJava Set this to true if the java names in the data for the interface are to be shortened.  For example, 
	 * java.lang.String will be shortened to String.  If you do not set the correct parameter here the sequence of dashes will 
	 * not be the correct size.
	 * @param shortOther Set this to true if the java names in the data for the interface are to be shortened.  If you do not 
	 * set the correct parameter here the sequence of dashes will not be the correct size.
	 * @return A sequence of dashes.
	 */
	public static String getBlankLine(Interface intF, boolean shortJava, boolean shortOther)
	{
		StringBuffer buffer = new StringBuffer("|");
			
		for (int i = 1; i <= ASCIIPrintFileManager.longestLine(intF, shortJava, shortOther)+2; i++)
			buffer.append(" ");
			
		buffer.append("|");
		return buffer.toString();
	}
	
	/**
	* This prints out the line which describes the attribute described
	* by the AttributeDefn object at "index" in intF's attribute_vector.
	* If index is a value that will cause an ArrayIndexOutOfBoundsException,
	* then a string like the following will be printed:  "|                 |",
	* with the number of spaces correct to create good ASCII art for the 
	* Interface object intF.
	*/		
	public static String getAttributeLine(Interface intF, int index, boolean shortJava, boolean shortOther)
	{
		String attLine = "| ";
				
		if ((index >=0) && (index < intF.getAttribute_vector().size()))
		{
			AttributeDefn attDefn = ((AttributeDefn)(intF.getAttribute_vector().elementAt(index)));
			attLine += getUMLAttributeLine(attDefn,shortJava,shortOther);
		}
			
		int num = attLine.length();
		for (int i=1; i <= ASCIIPrintFileManager.longestLine(intF, shortJava, shortOther)-num+2; i++)
			attLine = attLine + " ";
				
		attLine = attLine + " |";
			
		return attLine;
	}
		
	/**
	* This prints out the line which describes the constructor described
	* by the ConstructorDefn object at "index" in intF's const_vector.
	* If index is a value that will cause an ArrayIndexOutOfBoundsException,
	* then a string like the following will be printed:  "|                 |",
	* with the number of spaces correct to create good ASCII art for the 
	* Interface object intF.
	*/		
	public static String getConstructorLine(Interface intF, int index, boolean shortJava, boolean shortOther)
	{
		String constLine = "| ";
				
		if ((index >=0) && (index < intF.getConst_vector().size()))
		{	
			ConstructorDefn constDefn = ((ConstructorDefn)(intF.getConst_vector().elementAt(index)));
			constLine += getUMLConstructorLine(constDefn,shortJava,shortOther);
		}
			
		int num = constLine.length();
		for (int i=1; i <= ASCIIPrintFileManager.longestLine(intF, shortJava, shortOther)-num+2; i++)
			constLine = constLine + " ";
				
		constLine = constLine + " |";
			
		return constLine;
	}
		
	/**
	* This prints out the line which describes the method described
	* by the MethodDefn object at "index" in intF's method_vector.
	* If index is a value that will cause an ArrayIndexOutOfBoundsException,
	* then a string like the following will be printed:  "|                 |",
	* with the number of spaces correct to create good ASCII art for the 
	* Interface object intF.
	*/		
	public static String getMethodLine(Interface intF, int index, boolean shortJava, boolean shortOther)
	{
		String methLine = "| ";
				
		if ((index >=0) && (index < intF.getMethod_vector().size()))
		{	
			MethodDefn methDefn = ((MethodDefn)(intF.getMethod_vector().elementAt(index)));
			methLine += getUMLMethodLine(methDefn,shortJava,shortOther);
		}
			
		int num = methLine.length();
		for (int i=1; i <= ASCIIPrintFileManager.longestLine(intF, shortJava, shortOther)-num+2; i++)
			methLine = methLine + " ";
				
		methLine = methLine + " |";
			
		return methLine;
	}
	
	/**
	 * This prints a UML diagram for the class or interface represented by the Interface object supplied to the method.
	 * @param intF The interface whose data is written.
	 * @param shortJava Set this to true if the java names in the data for the interface are to be shortened.  For example, 
	 * java.lang.String will be shortened to String.  If you do not set the correct parameter here the sequence of dashes will 
	 * not be the correct size.
	 * @param shortOther Set this to true if the java names in the data for the interface are to be shortened.  If you do not 
	 * set the correct parameter here the sequence of dashes will not be the correct size.
	 * @return A sequence of dashes.
	 */
	public void printSingleUML(Interface intF, boolean shortJava, boolean shortOther)
	{
		try
		{
			writeBytes(intF.getSingleUMLAsString(shortJava, shortOther));
			}
			catch(IOException e)
			{
				SystemsManager.printStackTrace(e);
			}
	}
}