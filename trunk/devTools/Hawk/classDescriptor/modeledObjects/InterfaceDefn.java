/*
 * File:  InterfaceDefn.java
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
 * Revision 1.4  2004/05/26 20:37:55  kramer
 * Added the methods:
 *   public String getEnclosingClassName(boolean shortJava)
 *   public String getEnclosingClassName(boolean shortOther)
 * Now if you get a shortened source code representation for this InterfaceDefn
 * object, the package name is never shortened.
 *
 * Revision 1.3  2004/03/12 19:46:18  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:10:06  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.modeledObjects;

import java.awt.GridLayout;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;

/**
 * This class is used to represent the general properties of a class or interface.  These properties
 * include what the class imports, implemnts, extends, its name, what package it is in, and the 
 * characteristics that describe the class or interface (i.e. public, abstract, final, etc.).
 * 
 * @author Dominic Kramer
 */
public class InterfaceDefn
{
	//--------class attributes-----------------------------------------
	
	/**
	 * This is a Vector of Strings each of which is one of the class or interface's characteristics
	 * (i.e. public, abstract, final)
	 */
	protected Vector Interface_char_vector;  //i.e. public, abstract .... a vector of strings
					   //this needs to be a vector because its size is not known at compile time
	/**
	 * The type (i.e. class or interface)
	 */
	protected String Interface_type;  //i.e. class, interface
	/**
	 * The class or interface's name
	 */
	protected String Interface_name;  //the Interface's name
	/**
	 * What the class or interface extends
	 */
	protected String Interface_extends;  //the class/interface extended
	/**
	 * A Vector of Strings, each of which is the name of a class or interface that it implements
	 */
	protected Vector Interface_implements_vector;  //the vector of interfaces implemented
						   //the vectorr can be a vector
						   //of objects of type Interface or Strings
	/**
	 * A Vector of Strings, each of which is a item that the class or interface imports
	 */
	protected Vector Interface_imports_vector;  //the vector of files imported
						//this should be a vector of strings because I have 
						//no classes that define a package imported
	/**
	 * The name of the package the class or interface belongs to.
	 */
	protected String Package_Name;
		
	//---------constructors--------------------------------------------
	
	/**
	 * Creates a default InterfaceDefn object.
	 */
	public InterfaceDefn()
	{
		Interface_char_vector = new Vector();
		Interface_type = "";
		Interface_name = "";
		Interface_extends = "";
		Interface_implements_vector = new Vector();
		Interface_imports_vector = new Vector();
		Package_Name = "";
	}
	
	/**
	 * Creates an InterfaceDefn object given the parameters.
	 * @param cV The Vector of Strings each of which is one of the interface's characteristics (public, private, abstract, etc.).
	 * @param t The interface's type (either class or interface).
	 * @param n The interface's name.
	 * @param e The name of the interface that this interface extends.
	 * @param iV The Vector of Strings each of which is the name of an interface that the class or interface implements.
	 * @param imV The Vector of Strings each of which is the name of a package that the interface imports.
	 * @param pk The name of the package that this interface is a member of.
	 */
	public InterfaceDefn(Vector cV, String t, String n, String e, Vector iV, Vector imV, String pk)
	{
		Interface_char_vector = cV;
		Interface_type = t;
		Interface_name = n;
		Interface_extends = e;
		Interface_implements_vector = iV;
		Interface_imports_vector = imV;
		Package_Name = pk;
	}
	
	//---------methods for setting and getting these attributes---------
	
	/**
	 * Get the Vector of Strings each of which is one of the interface's characteristic (public, abstract, etc.).
	 * @return A Vector of Strings.
	 */
	public Vector getInterface_char_vector()
	{
		return Interface_char_vector;
	}
	
	/**
	 * Set the interface's characteristics (public, abstract, etc.).
	 * @param vec A Vector of Strings.
	 */
	public void setInterface_char_vector(Vector vec)
	{
		Interface_char_vector = vec;
	}
	
	/**
	 * Get the interface's type (either class or interface).
	 * @return Either class or interface.
	 */
	public String getInterface_type()
	{
		return Interface_type;
	}
	
	/**
	 * Set the interface's type (either class or interface).
	 * @param str Either class or interface.
	 */
	public void setInterface_type(String str)
	{
		Interface_type = str;
	}
	/**
	 * Get the interface's name.
	 * @return The interface's name.
	 */	
	public String getInterface_name()
	{
		return Interface_name;
	}
	
	/**
	 * Get the interface's name in a modified format.
	 * @param shortJava If this is true, if the name is a java name it will be returned in a shortened form.  For example, if the name 
	 * is java.lang.String, it will be returned as String.
	 * @param shortOther If this is true, if the name is a non-java name, it will be returned in a shortened form.
	 * @return The interface's name in a modified format.
	 */
	public String getInterface_name(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(Interface_name, shortJava, shortOther);
	}
	
	/**
	 * Set the interface's name.
	 * @param str The interface's new name.
	 */
	public void setInterface_name(String str)
	{
		Interface_name = str;
	}
	
	/**
	 * Get the name of the class or interface this extends.
	 * @return The name of the class or interface this extends.
	 */
	public String getInterface_extends()
	{
		return Interface_extends;
	}
	
	/**
	 * Get the name of the class or interface this extends in a modified form.
	 * @param shortJava If this is true, the name will be shortened if it is a java name.  For example, 
	 * java.lang.String will be returned as String.
	 * @param shortOther If this is true, the name will be shortened if it is a non-java name. 
	 * @return The name of the class or interface this extends.
	 */
	public String getInterface_extends(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(Interface_extends, shortJava, shortOther);
	}
	
	/**
	 * Set the name of the class or interface this extends.
	 * @param pgm
	 */
	public void setInterface_extend(String pgm)
	{
		Interface_extends = pgm;
	}
	
	/**
	 * Get the Vector of Strings each of which is the name of an interface that this class implements.
	 * @return A Vector of Strings.
	 */
	public Vector getInterface_implements_vector()
	{
		return Interface_implements_vector;
	}
	
	/**
	 * Get the Vector of Strings each of which is the name of an interface that this class implements in a modified 
	 * format.
	 * @param shortJava If this is set to true, if the name is a java name it will be shortened.  For example, java.lang.String 
	 * will be returned as String.
	 * @param shortOther If this is set to true, if the name is a non-java name it will be shortened.
	 * @return A Vector of Strings.
	 */
	public Vector getInterface_implements_vector(boolean shortJava, boolean shortOther)
	{
		Vector vec = new Vector();
		for (int i=0; i<Interface_implements_vector.size(); i++)
			vec.add(InterfaceUtilities.getAbbreviatedName((String)Interface_implements_vector.elementAt(i), shortJava, shortOther));
		
		return vec;
	}
	
	/**
	 * Set the interfaces that this class implements.
	 * @param vec A Vector of Strings each of which is the name of an interface that this class implements.
	 */
	public void setInterface_implements_vector(Vector vec)
	{
		Interface_implements_vector = vec;
	}

	/**
	 * Get the Vector of Strings each of which is the name of a package that this class imports.
	 * @return A Vector of Strings.
	 */	
	public Vector getInterface_imports_vector()
	{
		return Interface_imports_vector;
	}
	
	/**
	 * Get the Vector of Strings each of which is the name of a package that this class imports in a modified 
	 * format.
	 * @param shortJava If this is set to true, if the name is a java name it will be shortened.  For example, java.lang.String 
	 * will be returned as String.
	 * @param shortOther If this is set to true, if the name is a non-java name it will be shortened.
	 * @return A Vector of Strings.
	 */
	public Vector getInterface_imports_vector(boolean shortJava, boolean shortOther)
	{
		Vector vec = new Vector();
		for (int i=0; i<Interface_imports_vector.size(); i++)
			vec.add(InterfaceUtilities.getAbbreviatedName((String)Interface_imports_vector.elementAt(i), shortJava, shortOther));
		
		return vec;
	}
	
	/**
	 * Set the packages that this class imports.
	 * @param vec A Vector of Strings each of which is the name of an interface that this class implements.
	 */
	public void setInterface_imports_vector(Vector vec)
	{
		Interface_imports_vector = vec;
	}
	
	/**
	 * Get the name of the package that this interface is a member of.
	 * @return The interface's package's name.
	 */
	public String getPackage_Name()
	{
		return Package_Name;
	}
	
	/**
	 * Get the name of the package that this interface is a member of.
	 * @param shortJava If this is set to true, if the name is a java name it will be shortened.  For example, java.lang.String 
	 * will be returned as String.
	 * @param shortOther If this is set to true, if the name is a non-java name it will be shortened.
	 * @return The interface's package's name.
	 */
	public String getPackage_Name(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(Package_Name, shortJava, shortOther);
	}

	/**
	 * Set the name of the package that this interface is a member of.
	 * @return The interface's package's name.
	 */	
	public void setPackage_Name(String str)
	{
		Package_Name = str;
	}
	
	/**
	 * True if this is an interface and false otherwise.
	 * @return True for interfaces and false otherwise.
	 */
	public boolean isInterface()
	{
		return Interface_type.equals("interface");
	}
	
	/**
	 * True if this is a class and false otherwise.
	 * @return True for classes and false otherwise.
	 */
	public boolean isClass()
	{
		return Interface_type.equals("class");
	}
	
	/**
	 * True if one of the characteristics is "abstract" and false otherwise.
	 * @return True if abstract and false otherwise.
	 */
	public boolean isAbstract()
	{
		boolean answer = false;
		
		for (int i=0; i<Interface_char_vector.size(); i++)
			if ( ((String)(Interface_char_vector.elementAt(i))).equals("abstract") )
				answer = true;
		
		return answer;
	}
	
	/**
	 * True if this class or interface is not abstract.
	 * @return True for non-abstract classes and interfaces.
	 */
	public boolean isConcrete()
	{
		return !isAbstract();
	}
	
	/**
	 * True if this is an inner class or interface.  The class or interface is assumed to be 
	 * inner if its name includes a $ in it.
	 * @return True for inner classes or interfaces.
	 */
	public boolean isInner()
	{
		int index = Interface_name.indexOf((int)'$');
		return (index > -1);
	}
	
	/**
	 * True if this class or interface is not an inner class or interface.
	 * @return True for non-inner classes or interfaces.
	 */
	public boolean isOuter()
	{
		return !isInner();
	}

	/**
	 * Get the name of the enclosing class for the class or interface represented by this InterfaceDefn object, if it 
	 * is an inner class or interface.  Otherwise the original class or interface's name is returned.
	 * @return This class or interface's enclosing class if it is an inner class/interface or the 
	 * class/interface itself if it is an outer class.
	 */	
	public String getEnclosingClassName()
	{
		return getEnclosingClassName(false,false);
	}
	
	/**
	 * Get the name of the enclosing class (in a formated form) for the class or interface represented by this 
	 * InterfaceDefn object, if it is an inner class or interface.  Otherwise the original class or interface's name is returned.
	 * @param shortJava True if the enclosing class's name is to be shortened if it is a java name.
	 * @param shortOther True if the enclosing class's name is to be shortened if it is a non-java name.
	 * @return This class or interface's enclosing class if it is an inner class/interface or the 
	 * class/interface itself if it is an outer class.
	 */

	public String getEnclosingClassName(boolean shortJava, boolean shortOther)
	{
		String answer = "";
		StringTokenizer tokenizer = new StringTokenizer(getInterface_name(shortJava,shortOther),"$");
		if (tokenizer.hasMoreTokens())
			answer = tokenizer.nextToken();
		
		return answer;
	}
	
	
	//-----these methods print to tile specified by the PrintWriter 'writer'
	
	/**
	 * Prints data about this InterfaceDefn object to the DataOutputStream writer and does not write a new line character 
	 * at the end of the data.  This method prints the data in the native Hawk format to save the interfaceDefn object to 
	 * a file.  This method is used in the print(DataOutputStream) method in the class Interface.
	 */	
	public void print(DataOutputStream writer)
	{
		String del = ":";
		int i = 0;
		try
		{
			writer.writeUTF("<INTERFACE>"+del);
			
			if (Interface_imports_vector.size() > 0)
				writer.writeUTF("<interface_imports>"+del);
			while(i < Interface_imports_vector.size())
			{
				writer.writeUTF(Interface_imports_vector.elementAt(i)+del);
				i++;
			}
			if (Interface_imports_vector.size() > 0)
				writer.writeUTF("<end_interface_imports>"+del);
				
			i=0;
			
			if (Interface_char_vector.size() > 0)
				writer.writeUTF("<interface_characteristics>"+del);
			while(i < Interface_char_vector.size())
			{
				writer.writeUTF(Interface_char_vector.elementAt(i)+del);
				i++;
			}
			if (Interface_char_vector.size() > 0)
				writer.writeUTF("<end_interface_characteristics>"+del);
												
			if ( !((Interface_type.trim()).equals("")) )
			{
				writer.writeUTF("<interface_type>"+del+Interface_type+del+"<end_interface_type>"+del);
			}
			
			if ( !((Interface_name.trim()).equals("")) )
			{
				writer.writeUTF("<interface_name>"+del+Interface_name+del+"<end_interface_name>"+del);
			}
			
			if ( !((Interface_extends.trim()).equals("")) )
			{	
				writer.writeUTF("<interface_extends>"+del+Interface_extends+del+"<end_interface_extends>"+del);
			}
			
			i=0;
			
			if (Interface_implements_vector.size() > 0)
				writer.writeUTF("<interface_implements>"+del);
			while(i < Interface_implements_vector.size())
			{
				writer.writeUTF(Interface_implements_vector.elementAt(i)+del);
				i++;
			}
			if (Interface_implements_vector.size() > 0)
				writer.writeUTF("<end_interface_implements>"+del);
			
			if ( !(Package_Name.equals("")) )
			{
				writer.writeUTF("<interface_package_name>"+del+Package_Name+del+"<end_interface_package_name>"+del);
			}
			writer.writeUTF("<END_INTERFACE>"+del);			
		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in print(RandomAccessFile) in InterfaceDefn.java");
		}
	}

	/**
	 * Prints data about this InterfaceDefn object to the DataOutputStream writer and writes a new line character 
	 * at the end of the data.  This method prints the data in the native Hawk format to save the interfaceDefn object to 
	 * a file.  This method is used in the print(DataOutputStream) method in the class Interface.
	 */	
	public void println(DataOutputStream writer)
	{
		print(writer);
		try
		{
			writer.writeUTF("\n");
		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in println(RandomAccessFile) in InterfaceDefn.java");
		}
	}
	
	/**
	 * Makes a clone of the InterfaceDefn object supplied.
	 * @return A clone of the InterfaceDefn object supplied.
	 */
	public InterfaceDefn getClone()
	{
		return (new InterfaceDefn( (Vector)(Interface_char_vector.clone()), (new String(Interface_type)) ,(new String(Interface_name)), (new String(Interface_extends)), (Vector)(Interface_implements_vector.clone()), (Vector)(Interface_imports_vector.clone()), (new String(Package_Name)) ));
	}
	
	public void defineInterfaceDefnObject(Vector chara, String type, String name, String ext, Vector imple, Vector imp, String pk)
	{
		Interface_char_vector = chara;
		Interface_type = type; 
		Interface_name = name; 
	
		Interface_extends = ext;
		Interface_implements_vector = imple;
		Interface_imports_vector = imp;	
		Package_Name = pk;
	}
	
	/**
	 * This creates a JPanel with all of the textfields and labels that display the information for this InterfaceDefn object.  The textfields 
	 * and labels are positioned vertically on the JPanel.  The panel created does not display information in a 
	 * compact or effecient way and may be removed or significantly changed.
	 * @deprecated
	 * @return A JPanel.
	 */
	public JPanel getInterfaceDefnJPanel()
	{
		JPanel InterfaceDefnJPanel = new JPanel();
		
		JTextField Interface_char_vectorTextField = new JTextField(InterfaceUtilities.makeStringFromVector(Interface_char_vector));
		JTextField Interface_typeTextField = new JTextField(Interface_type);

		JTextField Interface_nameTextField = new JTextField(Interface_name);
		JTextField Interface_extendsTextField = new JTextField(Interface_extends);
		JTextField Interface_implements_vectorTextField = new JTextField(InterfaceUtilities.makeStringFromVector(Interface_implements_vector));
		JTextField Interface_imports_vectorTextField = new JTextField(InterfaceUtilities.makeStringFromVector(Interface_imports_vector));	
		JTextField Interface_package_nameTextField = new JTextField(Package_Name);
		
		InterfaceDefnJPanel.setLayout(new GridLayout(0,2));
		
		InterfaceDefnJPanel.add(new JLabel("Characteristics: "));
		InterfaceDefnJPanel.add(Interface_char_vectorTextField);
		InterfaceDefnJPanel.add(new JLabel("Type: "));
		InterfaceDefnJPanel.add(Interface_typeTextField);
		InterfaceDefnJPanel.add(new JLabel("Name: "));
		InterfaceDefnJPanel.add(Interface_nameTextField);
		InterfaceDefnJPanel.add(new JLabel("Extends: "));
		InterfaceDefnJPanel.add(Interface_extendsTextField);
		InterfaceDefnJPanel.add(new JLabel("Implements: "));
		InterfaceDefnJPanel.add(Interface_implements_vectorTextField);
		InterfaceDefnJPanel.add(new JLabel("Imports: "));
		InterfaceDefnJPanel.add(Interface_imports_vectorTextField);
		InterfaceDefnJPanel.add(new JLabel("Package:  "));
		InterfaceDefnJPanel.add(Interface_package_nameTextField);
		InterfaceDefnJPanel.add(new JSeparator());
		
		InterfaceDefnJPanel.setVisible(true);
		
		return InterfaceDefnJPanel;
	}
	
	/**
	 * This returns a String describing this InterfaceDefn object in a format similar to source code.
	 * @param shortJava If this is set to true, if a name is a java name it will be shortened.  For example, java.lang.String 
	 * will be returned as String.
	 * @param shortOther If this is set to true, if a name is a non-java name it will be shortened.
	 * @return A String.
	 */
	public String getStringInJavadocFormat(boolean shortJava, boolean shortOther)
	{
		String str = "";
		String impStr = "";
		
		if ( !(Package_Name.equals("")) )
		{
			impStr = impStr + "package "+getPackage_Name()+"\n\n";
		}
						
		for (int i = 0; i < Interface_imports_vector.size(); i++)
		{
			impStr = impStr + "imports " + (String)(getInterface_imports_vector(shortJava, shortOther).elementAt(i))+"\n";
		}
		
		str = InterfaceUtilities.makeStringFromVector(Interface_char_vector, ", ")+getInterface_type()+" "+getInterface_name(shortJava, shortOther);
		
		if ( !( (Interface_extends == null) || (Interface_extends.equals("")) ) )
			str = str + " extends " + getInterface_extends(shortJava, shortOther);
		
		if (Interface_implements_vector.size() != 0)
			str = str + " implements " + InterfaceUtilities.makeStringFromVector(getInterface_implements_vector(shortJava, shortOther), ", ");
		
		str = str + "\n{";
		
		return (impStr + "\n" + str);
	}
	
	/**
	 * Get the interface's name.
	 */
	public String toString()
	{
		return Interface_name;
	}
}
