/*
 * File:  MethodDefn.java
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
 * Revision 1.2  2004/03/11 18:51:52  bouzekc
 * Documented file using javadoc statements.
 * Added the toString() method.
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
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;

/**
* This class defines the object which describes a method in a class or interface.
* Each method in a class or interface has a unique MethodDefn object.
* 
* @author Dominic Kramer
*/
public class MethodDefn extends ConstructorDefn
{
	//-----------------class attributes-------------------------
	/**
	* The method's return type
	*/
	protected String method_return_type;  //the return type

	//------------------constructors-----------------------------
	/**
	* This makes a new default MethodDefn object
	*/
	public MethodDefn()
	{
		setConst_char_vector(new Vector());
		setConst_parameter_vector(new Vector());
		setConst_name("");
		
		method_return_type = "";
	}
	
	/**
	* This makes a MethodDefn object
	*
	* @param cv A Vector of strings each of which is the method's access level
	* @param pv A Vector of strings each of which is the method's parameter type
	* @param nm The method's name
	* @param rt The method's return type
	*/
	public MethodDefn(Vector cv, Vector pv, String nm, String rt)
	{
		setConst_char_vector(cv);
		setConst_parameter_vector(pv);
		setConst_name(nm);
		
		method_return_type = rt;
	}
						 	
	//------------------class methods----------------------------
	
	/**
	 * Get the method's "characteristics" (public, static, etc.).
	 * @return A Vector of Strings.
	 */
	public Vector getMethod_char_vector()
	{
		return getConst_char_vector();
	}
	
	/**
	 * Set the method's "characteristics" (public, static, etc.).
	 * @param vec A Vector of Strings.
	 */
	public void setMethod_char_vector(Vector vec)
	{
		setConst_char_vector(vec);
	}
	
	/**
	 * Get the method's return type.
	 * @return A String representing what the method returns.
	 */
	public String getMethod_return_type()
	{
		return method_return_type;
	}
	
	/**
	 * Get the method's return type in a modified format.
	 * @param shortJava If this is set to true, if the method returns a java object, its name will be shortened.  For example 
	 * if the method returns java.lang.String, String will be returned from this method.
	 * @param shortOther If this is set to true, if the method returns a non-java object, its name will be shortened.
	 * @return A modified version of the String representing what the method returns.
	 */
	public String getMethod_return_type(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(method_return_type, shortJava, shortOther);
	}
	
	/**
	 * Set the method's return type.
	 * @param str The String representing what the method returns.
	 */
	public void setMethod_return_type(String str)
	{
		method_return_type = str;
	}
	
	/**
	 * Get the method's name.
	 * @return The method's name.
	 */
	public String getMethod_name()
	{
		return getConst_name();
	}
	
	/**
	 * Get the method's name in a modified format.
	 * @param shortJava If this is set to true, if the method's name is a java name its name will be shortened.  For example, if 
	 * the name is java.lang.String, it will be returned as String.
	 * @param shortOther If this is set to true, if the method's name is a non-java name, its name will be shortened.
	 * @return The method's name in a modified format.
	 */
	public String getMethod_name(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(getConst_name(), shortJava, shortOther);
	}
	
	/**
	 * Set the method's name.
	 * @param str The method's new name.
	 */
	public void setMethod_name(String str)
	{
		setConst_name(str);
	}
	
	/**
	 * Get the names of the parameters that this method takes.
	 * @return A Vector of Strings.
	 */
	public Vector getMethod_parameter_vector()
	{
		return getConst_parameter_vector();
	}
	
	/**
	 * Get the names of the paramters (in a modified format) that this method takes.
	 * @param shortJava If this is set to true, if a parameter's name is a java name it will be shortened.  For example 
	 * if java.lang.String is a parameter, it will be returned as String.
	 * @param shortOther If this is set to true, if a parameter's name is a non-java name it will be shortened.
	 * @return A Vector of Strings.
	 */
	public Vector getMethod_parameter_vector(boolean shortJava, boolean shortOther)
	{
		Vector vec = new Vector();
		for (int i=0; i<getConst_parameter_vector().size(); i++)
			vec.add(InterfaceUtilities.getAbbreviatedName((String)getConst_parameter_vector().elementAt(i), shortJava, shortOther));
		
		return vec;
	}
	
	/**
	 * Set the method's parameters.
	 * @param vec A Vector of Strings.
	 */
	public void setMethod_parameter_vector(Vector vec)
	{
		setConst_parameter_vector(vec);
	}
	
	/**
	* Prints the data to the file specified by the DataOutputStream writer 
	* starting where writer's file pointer is currently located.  This method
	* works only if method_parameter_vector is a vector of Strings.
	*
	* @param writer The RandomAccessFile used to write the data
	*/	
	//---------method prints the data to the file specified by the PrintWriter 'writer'
	//this method works by assuming that the elements in method_parameter_vector are strings	
	public void print(DataOutputStream writer)
	{
		try
		{
			String del = ":";
			int i = 0;
		
			writer.writeUTF("<METHOD>"+del);
			
			if (getConst_char_vector().size() > 0)
				writer.writeUTF("<method_characteristics>"+del);
			while(i < getConst_char_vector().size())
			{
				writer.writeUTF(getConst_char_vector().elementAt(i)+del);
				i++;
			}
			if (getConst_char_vector().size() > 0)
				writer.writeUTF("<end_method_characteristics>"+del);
			
			if ( !((method_return_type.trim()).equals("")) )
				writer.writeUTF("<method_return_type>"+del+method_return_type+del+"<end_method_return_type>"+del);
			
			if ( !((const_name.trim()).equals("")) )
				writer.writeUTF("<method_name>"+del+const_name+del+"<end_method_name>"+del);
			
			i=0;  //reset i
			if (getConst_parameter_vector().size() > 0)
				writer.writeUTF("<method_parameters>"+del);
			while(i < getConst_parameter_vector().size())
			{
				writer.writeUTF(getConst_parameter_vector().elementAt(i)+del);
				i++;
			}
			if (getConst_parameter_vector().size() > 0)
				writer.writeUTF("<end_method_parameters>"+del);
			
			writer.writeUTF("<END_METHOD>"+del);
		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in print(RandomAccessFile) in MethodDefn.java");
		}
	}
	
	/**
	* Prints the data to the file specified by the DataOutputStream writer 
	* starting where writer's file pointer is currently located.  Then it 
	* moves the file pointer to the start of the next line in the file.
	* This method works only if method_parameter_vector is a vector of Strings.
	*
	* @param writer The RandomAccessFile used to write the data
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
			System.out.println("An IOException was thrown in println(RandomAccessFile) in MethodDefn.java");
		}
	}
	
	/**
	* Sets the values for attributes of a MethodDefn object
	*
	* @param chara1 The Vector of Strings that holds the method's access levels
	* @param type1 The method's return type
	* @param name1 The method's name
	* @param parameter1 The Vector of Strings that holds the method's parameters
	*/
	public void defineMethodDefnObject(Vector chara1, String type1, String name1, Vector parameter1)
	{
		defineConstructorDefnObject(chara1, name1, parameter1);
		
		method_return_type = type1;
	}

	/**
	 * This creates a JPanel with all of the textfields and labels that display the information for this MethodDefn object.  The textfields 
	 * and labels are positioned vertically on the JPanel.  The panel created does not display information in a 
	 * compact or effecient way and may be removed or significantly changed.
	 * @deprecated
	 * @return A JPanel.
	 */
	public JPanel getMethodJPanel()
	{
		JPanel methodJPanel = new JPanel();
		JTextField charTextField = new JTextField(InterfaceUtilities.makeStringFromVector(const_char_vector));
		JTextField paramTextField = new JTextField(InterfaceUtilities.makeStringFromVector(const_parameter_vector));
		JTextField nameTextField = new JTextField(const_name);
		JTextField return_type_TextField = new JTextField(method_return_type);
		
		methodJPanel.setLayout(new GridLayout(0,2));
		
		methodJPanel.add(new JLabel("Characteristics: "));
		methodJPanel.add(charTextField);
		methodJPanel.add(new JLabel("Return type:  "));
		methodJPanel.add(return_type_TextField);
		methodJPanel.add(new JLabel("Name:  "));
		methodJPanel.add(nameTextField);
		methodJPanel.add(new JLabel("Parameters:  "));
		methodJPanel.add(paramTextField);
		methodJPanel.add(new JSeparator());
		
		methodJPanel.setVisible(true);
		
		return methodJPanel;
	}
	
	/**
	* Returns a String which describes a method similar to how a
	* method looks in the source code
	* 
	* @return The String that describes the method
	*/
	public String getStringInJavadocFormat(boolean shortJava, boolean shortOther)
	{
		return ""+InterfaceUtilities.makeStringFromVector(const_char_vector, " ")+getMethod_return_type(shortJava, shortOther)+" "+getMethod_name(shortJava, shortOther)+"( "+InterfaceUtilities.makeStringFromVector(getMethod_parameter_vector(shortJava, shortOther), ", ")+")";
	}
	
	/**
	 * Get a the method's name.
	 */
	public String toString()
	{
		return const_name;
	}
}
