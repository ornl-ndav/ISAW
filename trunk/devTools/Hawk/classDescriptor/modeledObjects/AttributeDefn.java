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
 * Revision 1.3  2004/03/12 19:46:18  bouzekc
 * Changes since 03/10.
 *
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

/**
 * This class is used to represent an attribute (field) in a class or interface.
 * @author Dominic Kramer
 *
 */
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
	
	/**
	 * Get the attributes characteristics (public, static, final, abstract, etc.) as a Vector of Strings.
	 * @return A Vector of Strings
	 */
	public Vector getAttribute_char_vector()
	{
		return attribute_char_vector;
	}
	
	/**
	 * Set the attribute's characteristics (public, static, final, abstract, etc.)
	 * @param vec A Vector of Strings.  They can be anything, not just Java keywords as long as they are
	 * Strings.
	 */
	public void setAttribute_char_vector(Vector vec)
	{
		attribute_char_vector = vec;
	}
	
	/**
	 * Get the attribute's type (int or String) exactly as it is stored
	 * @return The attribute's type
	 */
	public String getAttribute_type()
	{
		return attribute_type;
	}
	
	/**
	 * Get the attribute's type (int or String) possibly shortened.
	 * javax.swing.JTree when shortened will be returned as JTree
	 * @param shortJava True if you want the name to be shortened if it is a java name
	 * @param shortOther True if you want the name to be shortened if it is not a java name
	 * @return The possibly shortened version of the attribute's type
	 */
	public String getAttribute_type(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(attribute_type, shortJava, shortOther);
	}
	
	/**
	 * Set the attributes type
	 * @param str For example, int, String, etc.
	 */
	public void setAttribute_type(String str)
	{
		attribute_type = str;
	}
	
	/**
	 * Get the attribute's name exactly as it is stored
	 * @return The attributes name
	 */
	public String getAttribute_name()
	{
		return attribute_name;
	}

	/**
	 * Get the attribute's name possibly shortened.
	 * If the name contains any . in it.  This will return the last . separated word
	 * @param shortJava True if you want the name to be shortened if it is a java name
	 * @param shortOther True if you want the name to be shortened if it is not a java name
	 * @return The possibly shortened version of the attribute's name
	 */
	public String getAttribute_name(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(attribute_name, shortJava, shortOther);
	}
	
	/**
	 * Set the attribute's name
	 * @param str The new name
	 */
	public void setAttribute_name(String str)
	{
		attribute_name = str;
	}
	
	//-----these methods print to tile specified by the PrintWriter 'writer'
	
	/**
	 * Prints the data corresponding to this attribute using the DataOutputStream writer.
	 * This method is used to save the save the attribute part of a class and is used by 
	 * the class dataFileUtilities to save class information.
	 */
	public void print(DataOutputStream writer)
	{
		String del = ":";
		int i=0;
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
	
	/**
	 * Prints the data corresponding to this attribute using the DataOutputStream writer.
	 * Then move the cursor to the next line.  This method is used to save the save the 
	 * attribute part of a class and is used by the class dataFileUtilities to save class information.
	 */
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
	
	/**
	 * This sets all of the fields for the AttributeDefn object with the supplied data.  This method is used
	 * because it sets field data but doesn't make a whole new object in memory.  So it is more 
	 * memory conservative.
	 * @param vec A Vector of Strings each of which is one of the attribute's characteristics (i.e. public, static, fina, etc.)
	 * @param type The attribute's type
	 * @param name The attribute's name
	 */
	public void defineAttributeDefnObject(Vector vec, String type, String name)
	{
		attribute_char_vector = vec;
		attribute_type = type;
		attribute_name = name;
	}
	
	/**
	 * This creates a JPanel with all of the textfields and labels that display the information for this AttributeDefn object.  The textfields 
	 * and labels are positioned vertically on the JPanel.  The panel created does not display information in a 
	 * compact or effecient way and may be removed or significantly changed.
	 * @deprecated
	 * @return A JPanel.
	 */
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
	
	/**
	 * This returns a String representation of the the attribute object in a specialized form.  For example it will return 
	 * public static final int;
	 * @param shortJava True if you want java names to be shortened
	 * @param shortOther True if you want non java names to be shortened
	 * @return A String representation of the attribute
	 */
	public String getStringInJavadocFormat(boolean shortJava, boolean shortOther)
	{
		return ""+InterfaceUtilities.makeStringFromVector(attribute_char_vector, " ")+getAttribute_type(shortJava, shortOther)+" "+getAttribute_name(shortJava, shortOther);
	}
	
	/**
	 * Get the attribute's name.
	 */
	public String toString()
	{
		return attribute_name;
	}
}
