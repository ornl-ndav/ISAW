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
	
	public Vector getMethod_char_vector()
	{
		return getConst_char_vector();
	}
	
	public void setMethod_char_vector(Vector vec)
	{
		setConst_char_vector(vec);
	}
	
	public String getMethod_return_type()
	{
		return method_return_type;
	}
	
	public String getMethod_return_type(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(method_return_type, shortJava, shortOther);
	}
	
	public void setMethod_return_type(String str)
	{
		method_return_type = str;
	}
	
	public String getMethod_name()
	{
		return getConst_name();
	}
	
	public String getMethod_name(boolean shortJava, boolean shortOther)
	{
		return InterfaceUtilities.getAbbreviatedName(getConst_name(), shortJava, shortOther);
	}
	
	public void setMethod_name(String str)
	{
		setConst_name(str);
	}
	
	public Vector getMethod_parameter_vector()
	{
		return getConst_parameter_vector();
	}
	
	public Vector getMethod_parameter_vector(boolean shortJava, boolean shortOther)
	{
		Vector vec = new Vector();
		for (int i=0; i<getConst_parameter_vector().size(); i++)
			vec.add(InterfaceUtilities.getAbbreviatedName((String)getConst_parameter_vector().elementAt(i), shortJava, shortOther));
		
		return vec;
	}
	
	public void setMethod_parameter_vector(Vector vec)
	{
		setConst_parameter_vector(vec);
	}
	
	/**
	* Prints the data to the file specified by the RandomAccessFile writer 
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
	* Prints the data to the file specified by the RandomAccessFile writer 
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
	* Returns the JPanel which holds all of the information about the method
	* in the form of JLabel and JTextField pairs.
	* @return The JPanel that contains the information
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
		return ""+InterfaceUtilities.makeStringFromVector(const_char_vector, ", ")+getMethod_return_type(shortJava, shortOther)+" "+getMethod_name(shortJava, shortOther)+"( "+InterfaceUtilities.makeStringFromVector(getMethod_parameter_vector(shortJava, shortOther), ", ")+")";
	}
}
