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
import java.io.RandomAccessFile;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;

public class InterfaceDefn
{
	//--------class attributes-----------------------------------------
	
	protected Vector Interface_char_vector;  //i.e. public, abstract .... a vector of strings
					   //this needs to be a vector because its size is not known at compile time
	protected String Interface_type;  //i.e. class, interface
	protected String Interface_name;  //the Interface's name
	
	protected String Interface_extends;  //the class/interface extended
	protected Vector Interface_implements_vector;  //the vector of interfaces implemented
						   //the vectorr can be a vector
						   //of objects of type Interface or Strings
	protected Vector Interface_imports_vector;  //the vector of files imported
						//this should be a vector of strings because I have 
						//no classes that define a package imported
	protected String Package_Name;
		
	//---------constructors--------------------------------------------
	
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
	
	public Vector getInterface_char_vector()
	{
		return Interface_char_vector;
	}
	
	public void setInterface_char_vector(Vector vec)
	{
		Interface_char_vector = vec;
	}
	
	public String getInterface_type()
	{
		return Interface_type;
	}
	
	public String getInterface_type(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(Interface_type, shortJava, shortOther);
	}
	
	public void setInterface_type(String str)
	{
		Interface_type = str;
	}
	
	public String getInterface_name()
	{
		return Interface_name;
	}
	
	public String getInterface_name(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(Interface_name, shortJava, shortOther);
	}
	
	public void setInterface_name(String str)
	{
		Interface_name = str;
	}
	
	public String getInterface_extends()
	{
		return Interface_extends;
	}
	
	public String getInterface_extends(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(Interface_extends, shortJava, shortOther);
	}
	
	public void setInterface_extend(String pgm)
	{
		Interface_extends = pgm;
	}
	
	public Vector getInterface_implements_vector()
	{
		return Interface_implements_vector;
	}
	
	public Vector getInterface_implements_vector(boolean shortJava, boolean shortOther)
	{
		Vector vec = new Vector();
		for (int i=0; i<Interface_implements_vector.size(); i++)
			vec.add(InterfaceUtilities.getAbbreviatedName((String)Interface_implements_vector.elementAt(i), shortJava, shortOther));
		
		return vec;
	}
	
	public void setInterface_implements_vector(Vector vec)
	{
		Interface_implements_vector = vec;
	}
	
	public Vector getInterface_imports_vector()
	{
		return Interface_imports_vector;
	}
	
	public Vector getInterface_imports_vector(boolean shortJava, boolean shortOther)
	{
		Vector vec = new Vector();
		for (int i=0; i<Interface_imports_vector.size(); i++)
			vec.add(InterfaceUtilities.getAbbreviatedName((String)Interface_imports_vector.elementAt(i), shortJava, shortOther));
		
		return vec;
	}
	
	public void setInterface_imports_vector(Vector vec)
	{
		Interface_imports_vector = vec;
	}

	public String getPackage_Name()
	{
		return Package_Name;
	}
	
	public String getPackage_Name(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(Package_Name, shortJava, shortOther);
	}
	
	public void setPackage_Name(String str)
	{
		Package_Name = str;
	}
	
	public boolean isInterface()
	{
		return Interface_type.equals("interface");
	}

	public boolean isClass()
	{
		return Interface_type.equals("class");
	}

	public boolean isAbstract()
	{
		boolean answer = false;
		
		for (int i=0; i<Interface_char_vector.size(); i++)
			if ( ((String)(Interface_char_vector.elementAt(i))).equals("abstract") )
				answer = true;
		
		return answer;
	}
	
	public boolean isInnerClass()
	{
		int index = Interface_name.indexOf((int)'$');
		return (index > -1);
	}

	//-----these methods print to tile specified by the PrintWriter 'writer'
	
	int i = 0;
	
	public void print(DataOutputStream writer)
	{
		String del = ":";
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
	
	public void printlnLink(RandomAccessFile linker, String file)
	{
		printLink(linker, file);
		try
		{
			linker.writeBytes("\n");
		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in printlnLink(RandomAccessFile, String) in InterfaceDefn.java");
		}
	}	

	public void printLink(RandomAccessFile linker, String file)
	{
		printLink(linker, file);
	}

	public void printExtends(RandomAccessFile writer)
	{
		try
		{
			writer.writeBytes(""+Interface_name+" "+Interface_extends);
		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in printExtends(RandomAccessFile) in InterfaceDefn.java");
		}
	}
	
	public void printlnExtends(RandomAccessFile writer)
	{
		printExtends(writer);
		try
		{
			writer.writeBytes("\n");
		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in prinlnExtends(RandomAccessFile) in InterfaceDefn.java");
		}
	}
	
	public InterfaceDefn Clone()
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

	public String getStringInJavadocFormat(boolean shortJava, boolean shortOther)
	{
		String str = "";
		String impStr = "";
		
		if ( !(Package_Name.equals("")) )
		{
			impStr = impStr + "package "+getPackage_Name(shortJava, shortOther)+"\n\n";
		}
						
		for (int i = 0; i < Interface_imports_vector.size(); i++)
		{
			impStr = impStr + "imports " + (String)(getInterface_imports_vector(shortJava, shortOther).elementAt(i))+"\n";
		}
		
		str = InterfaceUtilities.makeStringFromVector(Interface_char_vector, ", ")+getInterface_type(shortJava, shortOther)+" "+getInterface_name(shortJava, shortOther);
		
		if ( !( (Interface_extends.equals("")) || (Interface_extends.equals(null)) ) )
			str = str + " extends " + getInterface_extends(shortJava, shortOther);
		
		if (Interface_implements_vector.size() != 0)
			str = str + " implements " + InterfaceUtilities.makeStringFromVector(getInterface_implements_vector(shortJava, shortOther), ", ");
		
		str = str + "\n{";
		
		return (impStr + "\n" + str);
	}
}
