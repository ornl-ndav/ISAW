/*
 * File:  InterfaceUtilities.java
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
 * Revision 1.3  2004/03/12 19:46:20  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:10:46  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;
import java.util.StringTokenizer;
import java.util.Vector;

import devTools.Hawk.classDescriptor.modeledObjects.Interface;

/**
* This class contains extra methods that can be used in Interface.java, InterfaceDefn.java, Attribute.java, ConstructorDefn.java,
* MethodDefn.java.  Thus each of these methods are static methods in this class to allow them to be easily used in other java files.
* 
* @author Dominic Kramer
*/
public class InterfaceUtilities
{
	/**
	* Makes a Vector of Strings from a String, where each token in the String is an element in the Vector.
	* @param str The String to turn into a Vector
	* @return The Vector made from the String
	*/
	//-----------------------these methods take a string and turn it into a vector of strings (it uses tokenization)------------------------------	
	public static Vector makeVectorFromString(String str)
	{
		StringTokenizer tokenizer = new StringTokenizer(str," \n\r");

		Vector answer = new Vector();

		while (tokenizer.hasMoreTokens())
		{
			answer.add(tokenizer.nextToken());
		}

		return answer;
	}
		
	/**
	* Makes a String from a Vector of Strings.  The elements in the Vector are concatenated together with a space
	* between each one.
	* @param vec The Vector to make a String from
	* @return The String made from the Vector
	*/
	public static String makeStringFromVector(Vector vec)
	{
		String temp = "";
		int i = 0;
		
		while ( i < vec.size() )
		{
			temp = temp + (String)(vec.elementAt(i)) + " ";
			i++;
		}
		
		return temp;
	}

	/**
	* Makes a String from a Vector of Strings.  The elements in the Vector are concatenated together with "str"
	* between each one.
	* @param vec The Vector to make a String from
	* @return The String made from the Vector
	*/
	public static String makeStringFromVector(Vector vec, String str)
	{
		String temp = "";
		int i = 0;
		
		while ( i < vec.size() )
		{
			if (i != vec.size()-1)
				temp = temp + (String)(vec.elementAt(i)) + str;
			else
				temp = temp + (String)(vec.elementAt(i))  + " ";
			i++;
		}
		
		return temp;
	}

	/**
	* Gets the UML (Universal Modeling Language) symbol associated with a java keyword.  For example, the UML symbol for "public" is 
	* "+".
	* @param jav The java keyword
	* @return The corresponding UML symbol
	*/
	public static String getUMLTermFromJavaTerm(String jav)
	{
		String uml = "";
		
		if (jav.equals("public"))
			uml = "+";
		else if (jav.equals("private"))
			uml = "-";
		else if (jav.equals("protected"))
			uml = "#";
		else if (jav.equals("package"))
			uml = " ";  //this returns "" because package is the default so doesn't have a uml symbol
		else		    //it returns " " and not "" to keep spacing even when writing ASCII art
		{
			uml = "";  //if the term does not have a UML symbol, then no symbol is used
			//uml = "/* "+jav+" */";
		}
			
		return uml;
	}
	
	/**
	* Gets the java keyword associated with a UML symbol.  For example, the java keyword associated with "-" is "private".
	* @param uml The UML symbol
	* @return The corresponding java keyword
	*/
	public static String getJavaTermFromUMLTerm(String uml)
	{
		String jav = "";
		
		if (uml.equals("+"))
			jav = "public";
		else if (uml.equals("-"))
			jav = "private";
		else if (uml.equals("#"))
			jav = "protected";
		else
		{
			jav = "";  //if the term does not have a corresponding java symbol, then no symbol is used
			//jav = "/* "+uml+" */";
		}
		
		return jav;
	}
	
	/**
	* This takes in a Vector of objects and organizes the objects
	* in Lexicographic order based on the String returned from the toString() method.  
	* Lexicographic order is similar to alphabetical order except A, B, C, D, ...., Z 
	* comes before a, b, c, d, ...., z.  Note that the vec is passed into the method 
	* by reference.  Therefore, when the program leaves the method the elements are 
	* still ordered correctly.
	* @param vec1 The Vector of objects.
	 * @param shortJava If this is true, then if a string is a java name it will be shortened.  
	 * For example, java.lang.String will be shortened to String.
	 * @param shortOther If this is true, then if a string is a non-java name it will be shortened.  
	*/
	public static void alphabatizeVector(Vector vec1, boolean shortJava, boolean shortOther)
	{
		int n = vec1.size();
		int smallIndex = 0;
		int pass = 0;
		int j = 0;
		Object temp = new Object();
		String string1 = "";
		String string2 = "";
					
		for (pass = 0; pass < n-1; pass++)
		{
			smallIndex = pass;
			for (j = pass+1; j<n; j++)
			{
				string1 = getAbbreviatedName(vec1.elementAt(j).toString(), shortJava, shortOther);
				string2= getAbbreviatedName(vec1.elementAt(smallIndex).toString(), shortJava,  shortOther);
				
				if ( (string1).compareTo(string2) < 0 )
					smallIndex = j;
					
				if (smallIndex != pass)
				{
					temp = vec1.elementAt(pass);
					vec1.setElementAt(vec1.elementAt(smallIndex), pass);
					vec1.setElementAt(temp, smallIndex);
				}
			}
		}
	}
	
	
	/**
	 * Returns Vector containing all of the package names that the Interfaces in the Vector vec
	 * are part of.
	 * @param vec A Vector of Interface objects
	 * @return A Vector of Strings each of which is a package name.  The packages
	 * are in Lexicographic ordering (This is a lot like alphabetical ordering).
	 */
	public static Vector getPackageListVector(Vector vec)
	{
		Vector packageVec = new Vector();
		String name = "";
		for (int i=0; i<vec.size(); i++)
		{
			name = ((Interface)vec.elementAt(i)).getPgmDefn().getPackage_Name();
			if (!foundInVector(name, packageVec))
				packageVec.add(name);
		}
		
		//now to alphabatize the vector (technically it is Lexicographic ordering)
		alphabatizeVector(packageVec,false,false);
						
		return packageVec;
	}
	
	/**
	 * This takes a Vector of Interfaces and returns a Vector of Vectors.  Each Vector inside the returned Vector corresponds 
	 * to a package.  The objects in these Vectors are Interfaces.  The returned Vector is organized alphabetical order.  Each vector
	 * inside that Vector is also alphabetically ordered.
	 * @param vec A Vector of Interfaces
	 * @return A Vector of Vectors
	 */
	public static Vector getVectorOfVectorOfInterfaces(Vector vec, boolean packageShortJava, boolean packageShortOther, boolean classShortJava, boolean classShortOther)
	{
		Vector answer = new Vector();
		String currentName = "";
		int location = -2;
		
		for (int i=0; i<vec.size(); i++)
		{
			currentName = ((Interface)vec.elementAt(i)).getPgmDefn().getPackage_Name();
			location = getPackageNameLocation(currentName, answer);
			
			if (location != -1)
			{
				((Vector)answer.elementAt(location)).add((Interface)vec.elementAt(i));
			}
			else
			{
				Vector newVec = new Vector();
				newVec.add((Interface)vec.elementAt(i));
				answer.add(newVec);
			}
			
		}

		//now to alphabatize the vector (technically it is Lexicographic ordering)
		int n = answer.size();
		int smallIndex = 0;
		int pass = 0;
		int j = 0;
		Vector temp = new Vector();
					
		for (pass = 0; pass < n-1; pass++)
		{
			smallIndex = pass;
		
			for (j = pass+1; j<n; j++)
			{
			
				if ( ((Interface)( (Vector)answer.elementAt(j) ).elementAt(0)).getPgmDefn().getPackage_Name(packageShortJava, packageShortOther).compareTo( ( (Interface)( (Vector)answer.elementAt(smallIndex) ).elementAt(0) ).getPgmDefn().getPackage_Name(packageShortJava, packageShortOther)) < 0 )
					smallIndex = j;
			
				if (smallIndex != pass)
				{
					temp = (Vector)(answer.elementAt(pass));
					answer.setElementAt((Vector)(answer.elementAt(smallIndex)), pass);
					answer.setElementAt(temp, smallIndex);
				}
			}
		}
		
		for (int i=0; i<answer.size(); i++)
			alphabatizeVector( (Vector)answer.elementAt(i), classShortJava, classShortOther);
				
		return answer;
	}
	
	/**
	 *  This method is made exclusively for the method getVectorOfVectorOfInterfaces(Vector, boolean, boolean).  
	 *  This method returns the location in the Vector vec which contains the Vector of Interfaces which are
	 *  all from package str.  
	 * @param str The package name
	 * @param vec The Vector of Vectors of Interface objects from the method getVectorOfVectorOfInterfaces(Vector, boolean, boolean)
	 * @return The location in vec where the Interfaces are from the package named str.  Returns -1 if the
	 * location was not found.
	 */
	private static int getPackageNameLocation(String str, Vector vecOfVec)
	{
		int answer = -1;
		boolean found = false;
		int i=0;
		
		while (i<vecOfVec.size() && !found)
		{
			if ( ((Vector)vecOfVec.elementAt(i)).size() > 0)
			{	
				if ( ((Interface) ((Vector)vecOfVec.elementAt(i)).elementAt(0)).getPgmDefn().getPackage_Name().equals(str) )
				{
					found = true;
					answer = i;
				}
			}
			
			i++;
		}
		
		if (!found)
			answer = -1;
		
		return answer;
	}
	
	/**
	 * This returns true if str is an element in the Vector vec.  Therefore, vec must
	 * be a Vector of Strings.  This method is only used for the method getPackageListVector(Vector).
	 * @param str The String to use
	 * @param vec The Vector to use
	 * @return A boolean
	 */
	private static boolean foundInVector(String str, Vector vec)
	{
		boolean answer = false;
		
		for (int i=0; i<vec.size(); i++)
			if (((String)vec.elementAt(i)).equals(str))
				answer=true;
				
		return answer;
	}
	
	/**
	 * This method always shortens the String supplied to the method.  This method is used by 
	 * the method getAbbreviatedName(String, boolean, boolean).  For example, if the String is 
	 * of the form "string1"."string2"."string3"."string4", the shortened version is "string4".
	 * @param name The String to shorten.
	 * @return The shortened version of the String.
	 */
	public static String getAbbreviatedName(String name)
	{
		StringTokenizer tokenizer = new StringTokenizer(name,".");
		String token = "";
		while (tokenizer.hasMoreTokens())
		{
			token = tokenizer.nextToken();
		}
		return token;
	}

	/**
	 * This method shortens the String supplied to the method depending on the boolean parameters 
	 * supplied.  This method is used by all of the classes in the package 
	 * devTools.Hawk.classDescriptor.modeledObjects to shorten Strings.  For example, if the String 
	 * is of the form "string1"."string2"."string3"."string4", the shortened version is "string4".
	 * @param name The String to shorten.
	 * @param shortJava If this is true, then the String supplied will be shortened if it is a java name.  For 
	 * example, java.lang.String will be shortened to String.
	 * @param shortOther If this is true, then the String supplied will be shortened if it is a non-java name.
	 * @return The shortened version of the String.
	 */	
	public static String getAbbreviatedName(String name, boolean shortJava, boolean shortOther)
	{
		String answer = name;
		
		if (shortJava)
		{
			StringTokenizer tokenizer = new StringTokenizer(name, ".");
			String start = tokenizer.nextToken();
			
			if (start.equals("java") || start.equals("javax"))
			{
				answer = getAbbreviatedName(name);
			}
		}
		
		if (shortOther)
		{
			StringTokenizer tokenizer = new StringTokenizer(name, ".");
			String start = tokenizer.nextToken();
			
			if (!start.equals("java") && !start.equals("javax"))
			{
				answer = getAbbreviatedName(name);
			}		
		}
						
		return answer;
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
	public static boolean isAJavaKeyword(String str)
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