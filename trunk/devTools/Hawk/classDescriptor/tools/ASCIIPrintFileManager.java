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
 * @author kramer
 *
 */
public class ASCIIPrintFileManager extends PrintManager
{
	public ASCIIPrintFileManager(String file, String access) throws FileNotFoundException
	{
		super(file, access);
	}

	public ASCIIPrintFileManager(String access) throws FileNotFoundException
	{
		super(access);
	}

	public void printShortenedSource(int tabSize, Interface intF, boolean shortJava, boolean shortOther)
	{
		try
		{
			String result = "";
			String shortenedSource = "";
			String tab = "";
				
			for (int j = 1; j<=tabSize; j++)
				tab = tab + " ";
				
				shortenedSource = intF.getStringInJavadocFormat(shortJava, shortOther);
				StringTokenizer tokenizer = new StringTokenizer(shortenedSource,"\n");
				while (tokenizer.hasMoreTokens())
				{
					result = result + tab + tokenizer.nextToken() + "\n";
				}
				result = result + "\n";
	
			writeBytes(result);
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
				String result = "";
				for (int i = 1; i <= len; i++)
					result = result + "=";
				
				result = result + "\n";
				
				if (num >= 0)
				{
					result = result + "Section " + num+":";
				}
				
				if (title != null)
				{
					result = result + "  " + title;
				}
				
				result = result + "\n";
				
				for (int i = 1; i <= len; i++)
				result = result + "=";
								
				result = result + "\n";
				
				writeBytes(result);
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
		
		public void printIntroduction(String title, String date, String author, String description)
		{
			try
			{
				String result = "";
				result = result + "Title:  "+title+"\n";
				result = result + "Date:  "+date+"\n";
				result = result + "Author:  "+author+"\n";
				result = result + "Description:  ";
				StringTokenizer tokenizer = new StringTokenizer(description, "\n");
				String tab = "              ";
				boolean writeTab = false;
				while (tokenizer.hasMoreTokens())
				{				
					if (writeTab)
						result = result + tab;
						
					result = result + tokenizer.nextToken()+"\n";
					writeTab = true;
				}
				
				writeBytes(result+"\n");
			}
			catch(IOException e)
			{
				SystemsManager.printStackTrace(e);
			}
		}
		
		public void printSubSectionHeading(int tabSize, int len, String description)
		{
			try
			{
				String result = "";
				String tab = "";
				
				for (int i = 1; i<=tabSize; i++)
					tab = tab + " ";
					
				result = result + tab;
				
				for (int i = 1; i<(len-tabSize); i++)
					result = result + "-";
				
				result = result + "\n"+tab+description+"\n";
				
				for (int i = 1; i<(len-tabSize); i++)
					result = result + "-";
					
				result = result + "\n";
				writeBytes(result);
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
								
				String tab = "";
				for (int k = 1; k<=tabSize; k++)
					tab = tab + " ";
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
		
		public void printShortenedSource(int tabSize, Vector vec, boolean shortJava, boolean shortOther, ProgressGUI gui, int increment)
		{
			try
			{
				String result = "";
				String shortenedSource = "";
				String tab = "";
				
				for (int j = 1; j<=tabSize; j++)
					tab = tab + " ";
				
				int i=0;
				int currentVal = 0;
				String oldProgressText = gui.getText();
				while (i<vec.size() && !gui.isCancelled())
				{
					shortenedSource = ((Interface)vec.elementAt(i)).getStringInJavadocFormat(shortJava, shortOther);
					StringTokenizer tokenizer = new StringTokenizer(shortenedSource,"\n");
					while (tokenizer.hasMoreTokens())
					{
						result = result + tab + tokenizer.nextToken() + "\n";
					}
					result = result + "\n";
				
					writeBytes(result);
					result = "";
					
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
				String results = "";
				String tab = "";			
				for (int i = 1; i <= tabSize; i++)
					tab = tab + " ";
			
				for (int i = 0; i < stringVec.size(); i++)
					results = results + tab + "Section " + numArr[i] + ":  " + (String)(stringVec.elementAt(i)) + "\n";
			
				writeBytes(results);
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
	
	public void printUMLDiagramsAlphabetically(Vector vec, int len, int tabSize, boolean shortJava, boolean shortOther, ProgressGUI gui, int increment)
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

	//this will print
	//    " ------------ "
	public static String getOuterDivider(Interface intF, boolean shortJava, boolean shortOther)
	{
		String line1 = " ";
			
		//here you might think that i<=PrintFileManager.longestLine(intF)-2
		//however, I am adding 2 to that number to accomidate one space 
		//before and after a string is written to make the printout look nicer
		//and adding another 2 for the | before and | after each printed line
		//to make the text look like it is printed in a box
		//ex.  ----------
		//    | Name     |
		for (int i = 1; i <= ASCIIPrintFileManager.longestLine(intF, shortJava, shortOther)+2; i++)
			line1 = line1+"-";
				
		line1 = line1 + " ";
		return line1;
		//ex.  line1 = " --------- "
	}
	
	public static String getInterfaceNameLine(Interface intF, boolean shortJava, boolean shortOther)
	{
		String line2 = "| " + intF.getPgmDefn().getInterface_name(shortJava, shortOther);
		int num = line2.length();
			
		for (int i = 1; i <= ASCIIPrintFileManager.longestLine(intF, shortJava, shortOther)-num+2; i++)
			line2 = line2 + " ";
				
		line2 = line2 + " |";
		return line2;
		
	}
	
	//this will print
	//      "|-------------------------|"
	public static String getInnerDivider(Interface intF, boolean shortJava, boolean shortOther)
	{
		String line3 = "|";
			
		for (int i = 1; i <= ASCIIPrintFileManager.longestLine(intF, shortJava, shortOther)+2; i++)
			line3 = line3+"-";
			
		line3 = line3 + "|";
		return line3;
	}
		
	public static String getBlankLine(Interface intF, boolean shortJava, boolean shortOther)
	{
		String blank = "|";
			
		for (int i = 1; i <= ASCIIPrintFileManager.longestLine(intF, shortJava, shortOther)+2; i++)
			blank = blank+" ";
			
		blank = blank + "|";
		return blank;
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