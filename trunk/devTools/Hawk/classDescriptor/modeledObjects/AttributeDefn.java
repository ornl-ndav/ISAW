/*
 * File:  AttributeDefn.java
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
 * Revision 1.1  2004/02/07 05:10:05  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.modeledObjects;
import java.awt.GridLayout;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;

public class AttributeDefn
{
	//-----------class attributes----------------------------
	/**
	* The Vector of Strings each of which is the attribute's access level
	*/
	protected Vector attribute_char_vector;   //i.e. public, private, static, final
	/**
	* The attribute's type
	*/
	protected String attribute_type;  //i.e. int, bool, string, double, ....
	/**
	* The attribute's name
	*/
	protected String attribute_name;  //the attributes name
	
	//----------constructors---------------------------------
	/**
	* Makes a new default AttributeDefn object
	*/
	public AttributeDefn()
	{
		attribute_char_vector = new Vector();
		attribute_type = "";
		attribute_name = "";
	}
	
	/**
	* Makes a new AttributeDefn object
	* @param cv The Vector of Strings each of which is the attribute's access level
	* @param strName The attribute's name
	* @param strType The attribute's type
	*/
	public AttributeDefn(Vector cv, String strName, String strType)
	{
		attribute_char_vector = cv;
		attribute_type = strType;
		attribute_name = strName;
	}
	
	//----------class methods--------------------------------
	
	public Vector getAttribute_char_vector()
	{
		return attribute_char_vector;
	}
	
	public void setAttribute_char_vector(Vector vec)
	{
		attribute_char_vector = vec;
	}
	
	public String getAttribute_type()
	{
		return attribute_type;
	}
	
	public String getAttribute_type(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(attribute_type, shortJava, shortOther);
	}
	
	public void setAttribute_type(String str)
	{
		attribute_type = str;
	}
	
	public String getAttribute_name()
	{
		return attribute_name;
	}
	
	public String getAttribute_name(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(attribute_name, shortJava, shortOther);
	}
	
	public void setAttribute_name(String str)
	{
		attribute_name = str;
	}
	
	//-----these methods print to tile specified by the PrintWriter 'writer'
	
	int i = 0;
	
	public void print(DataOutputStream writer)
	{
		String del = ":";
		
		try
		{
			writer.writeUTF("<ATTRIBUTE>"+del);
			
			if (attribute_char_vector.size() > 0) //then there was is at least one "characteristic"
				writer.writeUTF("<attribute_characteristics>" +del);
			
			while(i < attribute_char_vector.size())
			{
				writer.writeUTF(attribute_char_vector.elementAt(i) + del);
			
				i++;
			}
				if (attribute_char_vector.size() > 0) //then there was at least one "characteristic"
					writer.writeUTF("<end_attribute_characteristics>"+del);
				
			if ( !((attribute_type.trim()).equals("")) )
				writer.writeUTF("<attribute_type>"+del+attribute_type+del+"<end_attribute_type>"+del);
			
			if ( !((attribute_name.trim()).equals("")) )
				writer.writeUTF("<attribute_name>"+del+ attribute_name+del+"<end_attribute_name>"+del);
				
			writer.writeUTF("<END_ATTRIBUTE>"+del);
		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in print(RandomAccessFile) in AttributeDefn.java");
		}
	}
	
	public void println(DataOutputStream writer)
	{
		try
		{
			print(writer);
			writer.writeUTF("\n");
		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in println(RandomAccessFile) in AttributeDefn.java");
		}
	}
	
	public void defineAttributeDefnObject(Vector vec, String type, String name)
	{
		attribute_char_vector = vec;
		attribute_type = type;
		attribute_name = name;
	}
	
	public JPanel getAttributeJPanel()
	{
		JPanel attributeJPanel = new JPanel();
		JTextField attribute_char_vectorTextField = new JTextField(InterfaceUtilities.makeStringFromVector(attribute_char_vector));
		JTextField attribute_typeTextField = new JTextField(attribute_type);
		JTextField attribute__nameTextField = new JTextField(attribute_name);	
		
		attributeJPanel.setLayout(new GridLayout(0,2));
		
		attributeJPanel.add(new JLabel("Characteristics: "));
		attributeJPanel.add(attribute_char_vectorTextField);		
		attributeJPanel.add(new JLabel("Type: "));
		attributeJPanel.add(attribute_typeTextField);
		attributeJPanel.add(new JLabel("Name: "));
		attributeJPanel.add(attribute__nameTextField);
		attributeJPanel.add(new JSeparator());
		
		attributeJPanel.setVisible(true);
		
		return attributeJPanel;
	}
	
	public String getStringInJavadocFormat(boolean shortJava, boolean shortOther)
	{
		return ""+InterfaceUtilities.makeStringFromVector(attribute_char_vector, ", ")+getAttribute_type(shortJava, shortOther)+" "+getAttribute_name(shortJava, shortOther);
	}
}
