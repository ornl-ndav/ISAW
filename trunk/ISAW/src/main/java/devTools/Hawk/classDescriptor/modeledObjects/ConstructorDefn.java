/*
 * File:  ConstructorDefn.java
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
* This class defines the object which describes a constructor
* in a class or interface.  Each constructor in a class or 
* interface has a unique ConstructorDefn object.
* 
* @author Dominic Kramer
*/
public class ConstructorDefn
{
	//-----------------class attributes-------------------------
	/**
	* A Vector of Strings each of which is one of the constructor's access levels
	*/
	protected Vector const_char_vector;  //i.e. public, static
	/**
	* A Vector of Srings each of which is one of the constructor's paramters
	*/
	protected Vector const_parameter_vector;  //the method's parameters
						 //this is a vector of strings
						 //for instance int, int, double
						//if that is what the method takes
	/**
	* The constructor's name
	*/
	protected String const_name;  //the constructors's name

	//------------------constructors-----------------------------
	
	/**
	* Creates a new default ConstructorDefn object
	*/
	
	public ConstructorDefn()
	{
		const_char_vector = new Vector();
		const_parameter_vector = new Vector();
		const_name = "";
	}
	
	/**
	* Creates a new ConstructorDefn object
	*
	* @param chV A Vector of strings each of which is the constructor's access level
	* @param pV A Vector of strings each of which is the constructor's parameter type
	* @param nm The constructor's name
	*/
	
	public ConstructorDefn(Vector chV, Vector pV, String nm)
	{
		const_char_vector = chV;
		const_parameter_vector = pV;
		const_name = nm;
	}
						 	
	//------------------class methods----------------------------
	
	/**
	 * Get the Vector of Strings representing the constructor's "characteristics" (public, static, etc.).
	 * @return A Vector of Strings
	 */
	public Vector getConst_char_vector()
	{
		return const_char_vector;
	}
	
	/**
	 * Set the constructor's "characteristics" (public, static, etc.)
	 * @param vec A Vector of Strings
	 */
	public void setConst_char_vector(Vector vec)
	{
		const_char_vector = vec;
	}
	
	/**
	 * Get the constructor's parameters.
	 * @return A Vector of Strings.
	 */
	public Vector getConst_parameter_vector()
	{
		return const_parameter_vector;
	}
	
	/**
	 * Get the constructor's parametes returned in a modified format.
	 * @param shortJava If this is set to true then any parameter that is a java type will have its name shortened.  For 
	 * example java.lang.String will be returned as String.
	 * @param shortOther If this is set to true then any parameter that is a non-java type will have its name shortened.
	 * @return A Vector of Strings.
	 */
	public Vector getConst_parameter_vector(boolean shortJava, boolean shortOther)
	{
		Vector vec = new Vector();
		for (int i=0; i<const_parameter_vector.size(); i++)
			vec.add(InterfaceUtilities.getAbbreviatedName((String)const_parameter_vector.elementAt(i), shortJava, shortOther));
		
		return vec;
	}
	
	/**
	 * Set the constructor's parameters.
	 * @param vec A Vector of Strings.
	 */
	public void setConst_parameter_vector(Vector vec)
	{
		const_parameter_vector = vec;
	}
	
	/**
	 * Get the constructor's name.
	 * @return The constructor's name.
	 */
	public String getConst_name()
	{
		return const_name;
	}
	
	/**
	 * Get the constructor's name returned in a modified format.
	 * @param shortJava If this is set to true, then if the name is a java name it will be returned in a shortened form.  For 
	 * example java.io.RandomAccessFile will be returned as RandomAccessFile
	 * @param shortOther If this is set to true, then if the name is a non-java name it will be returned in a shortened form.
	 * @return The constructor's name (in a modified format).
	 */
	public String getConst_name(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(const_name, shortJava, shortOther);
	}
	
	/**
	 * Set the constructor's name.
	 * @param str The constructor's new name.
	 */
	public void setConst_name(String str)
	{
		const_name = str;
	}

	/**
	* Prints the data to the file specified by the DataOutputStream writer 
	* starting where writer's file pointer is currently located.  This method
	* works only if const_parameter_vector is a vector of Strings.
	*
	* @param writer The DataOutputStream used to write the data
	*/
	
	//---------method prints the data to the file specified by the PrintWriter 'writer'
	//this method works by assuming that the elements in const_parameter_vector are strings	
	public void print(DataOutputStream writer)
	{
		String del = ":";
		int i = 0;
		try
		{
			writer.writeUTF("<CONSTRUCTOR>"+del);
			
			if (const_char_vector.size() > 0)
				writer.writeUTF("<constructor_characteristics>"+del);
			while(i < const_char_vector.size())
			{
				writer.writeUTF(const_char_vector.elementAt(i)+del);
				i++;
			}
			if (const_char_vector.size() > 0)
				writer.writeUTF("<end_constructor_characteristics>"+del);
			
			if ( !((const_name.trim()).equals("")) )
				writer.writeUTF("<constructor_name>"+del+const_name+del+"<end_constructor_name>"+del);
		
			i=0;  //reset i
			
			if (const_parameter_vector.size() > 0)
				writer.writeUTF("<constructor_parameters>"+del);
			while(i < const_parameter_vector.size())
			{
				writer.writeUTF(const_parameter_vector.elementAt(i)+del);
				i++;
			}
			if (const_parameter_vector.size() > 0)
				writer.writeUTF("<end_constructor_parameters>"+del);
			
			writer.writeUTF("<END_CONSTRUCTOR>"+del);
		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in print(RandomAccessFile) in ConstructorDefn.java");
		}
	}
	
	/**
	* Prints the data to the file specified by the DataOutputStream writer 
	* starting where writer's file pointer is currently located.  Then it 
	* moves the file pointer to the start of the next line in the file.
	* This method works only if const_parameter_vector is a vector of Strings.
	*
	* @param writer The DataOutputStream used to write the data
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
			System.out.println("An IOException was thrown in print(RandomAccessFile) in ConstructorDefn.java");
		}
		
	}
	
	/**
	* Sets the values for attributes of a ConstructorDefn object
	*
	* @param chara The Vector of Strings that holds the constructor's access levels
	* @param name The constructor's name
	* @param parameter The Vector of Strings that holds the constructor's parameters
	*/
	
	public void defineConstructorDefnObject(Vector chara, String name, Vector parameter)
	{
		const_char_vector = chara;
		const_name = name;
		const_parameter_vector = parameter;
	}
	
	/**
	 * This creates a JPanel with all of the textfields and labels that display the information for this ConstructorDefn object.  The textfields 
	 * and labels are positioned vertically on the JPanel.  The panel created does not display information in a 
	 * compact or effecient way and may be removed or significantly changed.
	 * @deprecated
	 * @return A JPanel.
	 */	public JPanel getConstJPanel()
	{
		JPanel constJPanel = new JPanel();
		
		JTextField charTextField = new JTextField(InterfaceUtilities.makeStringFromVector(const_char_vector));
		JTextField paramTextField = new JTextField(InterfaceUtilities.makeStringFromVector(const_parameter_vector));
		JTextField nameTextField = new JTextField(const_name);		
		
		constJPanel.setLayout(new GridLayout(0,2));
		
		constJPanel.add(new JLabel("Characteristics: "));
		constJPanel.add(charTextField);
		constJPanel.add(new JLabel("Name:  "));
		constJPanel.add(nameTextField);
		constJPanel.add(new JLabel("Parameters:  "));
		constJPanel.add(paramTextField);
		constJPanel.add(new JSeparator());
		
		constJPanel.setVisible(true);
		
		return constJPanel;
	}

	/**
	* Returns a String which describes a constructor similar to how a
	* constructor looks in the source code
	* 
	* @return The String that describes the constructor
	*/
	
	public String getStringInJavadocFormat(boolean shortJava, boolean shortOther)
	{
		return ""+InterfaceUtilities.makeStringFromVector(const_char_vector, " ")+getConst_name(shortJava, shortOther)+"( "+InterfaceUtilities.makeStringFromVector(getConst_parameter_vector(shortJava, shortOther), ", ")+")";
	}
	
	/**
	 * Get the constructor's name.
	 */
	public String toString()
	{
		return const_name;
	}
}
