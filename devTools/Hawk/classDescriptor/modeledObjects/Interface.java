/*
 * File:  Interface.java
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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import devTools.Hawk.classDescriptor.tools.ASCIIPrintFileManager;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

public class Interface
{
	//--------------------class attributes--------------------------------
	
	protected InterfaceDefn pgmDefn;  //an object of type InterfaceDefn
	protected Vector attribute_vector;  //a vector of objects of type AttributeDefn
	protected Vector const_vector;  //a vector of objects of type ConstructorDefn
	protected Vector method_vector;  //a vector of objects of type MethodDefn
	
	protected String sourceFileName;
	protected String javadocsFileName;
	
	//--------------------constructors------------------------------------
	
	public Interface()
	{
		pgmDefn = new InterfaceDefn();
		attribute_vector = new Vector();
		const_vector = new Vector();
		method_vector = new Vector();
		sourceFileName = "";
		javadocsFileName = "";
	}
	
	public Interface(InterfaceDefn pf, Vector atV, Vector cV, Vector mV, String sourceF, String javaF)
	{
		pgmDefn = pf;
		attribute_vector = atV;
		const_vector = cV;
		method_vector = mV;
		sourceFileName = sourceF;
		javadocsFileName = javaF;
	}	
	
	//--------------------class methods-----------------------------------
	
	//this is used to allow you to add an Interface object to
	//a JTree and have it's name get printed for the node name
	public String toString()
	{
		return pgmDefn.getInterface_name();
	}
	
	public InterfaceDefn getPgmDefn()
	{
		return pgmDefn;
	}
	
	public void setPgmDefn(InterfaceDefn pd)
	{
		pgmDefn = pd;
	}
	
	public Vector getAttribute_vector()
	{
		return attribute_vector;
	}
	
	public void setAttribute_vector(Vector vec)
	{
		attribute_vector = vec;
	}
	
	public Vector getMethod_vector()
	{
		return method_vector;
	}	
	public void setMethod_vector(Vector vec)
	{
		method_vector = vec;
	}
	
	public Vector getConst_vector()
	{
		return const_vector;
	}	
	public void setConst_vector(Vector vec)
	{
		const_vector = vec;
	}

	public String getSourceFileName()
	{
		return sourceFileName;
	}
	
	public void setSourceFileName(String str)
	{
		sourceFileName = str;
	}
	
	public String getSourceCodeAsString()
	{
		String code = "";
		try
		{		
			BufferedReader reader = new BufferedReader(new FileReader(sourceFileName));
			String line = reader.readLine();
			if (line != null)
				line += "\n";
			while (line != null)
			{
				code += line;
				line = reader.readLine();
				if (line != null)
					line += "\n";
			}				
			reader.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("The file "+sourceFileName+" could not be found.");
			System.err.println(e);
		}
		catch(IOException e)
		{
			System.out.println("An IOException was thrown in getSourceCodeAsString().");
			System.err.println(e);
		}
		
		return code;
	}
		
	public String getJavadocsFileName()
	{
		return javadocsFileName;
	}
	
	public void setJavadocsFileName(String str)
	{
		javadocsFileName = str;
	}	

	public String getJavadocAsString()
	{
		String line = "";
				
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(javadocsFileName));
			String ln = "";
		
			while (ln != null)
			{
				ln = reader.readLine();
			
				if (ln != null)
				{
					line = line + "\n" + ln;
				}
			}
				
			reader.close();
		}
		catch(IOException e)
		{
		
		}
				
		return line;
	}
	
	public void printSingleUMLAsString(RandomAccessFile raf, String tab, boolean shortJava, boolean shortOther)
	{
		try
		{
			int attSize = getAttribute_vector().size();
			int constSize = getConst_vector().size();
			int methSize = getMethod_vector().size();
			int i = 0;
			
			raf.writeBytes(tab + ASCIIPrintFileManager.getOuterDivider(this, shortJava, shortOther) + "\n");
			raf.writeBytes(tab + ASCIIPrintFileManager.getInterfaceNameLine(this, shortJava, shortOther) + "\n");
			raf.writeBytes(tab + ASCIIPrintFileManager.getInnerDivider(this, shortJava, shortOther) + "\n");
		
			for (i = 0; i < attSize; i++)
				raf.writeBytes(tab + ASCIIPrintFileManager.getAttributeLine(this, i, shortJava, shortOther) + "\n");
	
			if (attSize>0)
				raf.writeBytes(tab + ASCIIPrintFileManager.getInnerDivider(this, shortJava, shortOther) + "\n");
				
			for (i = 0; i < constSize; i++)
				raf.writeBytes(tab + ASCIIPrintFileManager.getConstructorLine(this, i, shortJava, shortOther) + "\n");
			if (constSize>0)
				raf.writeBytes(tab + ASCIIPrintFileManager.getInnerDivider(this, shortJava, shortOther) + "\n");
		
			for (i = 0; i < methSize; i++)
				raf.writeBytes(tab + ASCIIPrintFileManager.getMethodLine(this, i, shortJava, shortOther) + "\n");
		
			raf.writeBytes(tab + ASCIIPrintFileManager.getOuterDivider(this, shortJava, shortOther));
		}
		catch (Throwable e)
		{
			SystemsManager.printStackTrace(e);
		}
	}
	
	public String getSingleUMLAsString(boolean shortJava, boolean shortOther)
	{
		return getSingleUMLAsString("",shortJava,shortOther);
	}
	
	/**
	 * This prints out a UML diagram for the Interface object.
	 * @param shortJava If this is true then java names will be abbreviated.  For example, String instead of java.lang.String
	 * @param shortOther If this is true then all other names will be abbreviated.
	 * @return The UML diagram in ASCII art
	 */
	public String getSingleUMLAsString(String tab, boolean shortJava, boolean shortOther)
	{
		int attSize = getAttribute_vector().size();
		int constSize = getConst_vector().size();
		int methSize = getMethod_vector().size();
		int i = 0;
		String answer = "";
		
		answer = answer + tab + ASCIIPrintFileManager.getOuterDivider(this, shortJava, shortOther) + "\n";
		answer  = answer + tab + ASCIIPrintFileManager.getInterfaceNameLine(this, shortJava, shortOther) + "\n";
		answer = answer + tab + ASCIIPrintFileManager.getInnerDivider(this, shortJava, shortOther) + "\n";
	
		for (i = 0; i < attSize; i++)
			answer = answer + tab + ASCIIPrintFileManager.getAttributeLine(this, i, shortJava, shortOther) + "\n";

		if (attSize>0)
			answer = answer + tab + ASCIIPrintFileManager.getInnerDivider(this, shortJava, shortOther) + "\n";
			
		for (i = 0; i < constSize; i++)
			answer = answer + tab + ASCIIPrintFileManager.getConstructorLine(this, i, shortJava, shortOther) + "\n";
		if (constSize>0)
			answer = answer + tab + ASCIIPrintFileManager.getInnerDivider(this, shortJava, shortOther) + "\n";
	
		for (i = 0; i < methSize; i++)
			answer = answer + tab + ASCIIPrintFileManager.getMethodLine(this, i, shortJava, shortOther) + "\n";
	
		answer = answer + tab + ASCIIPrintFileManager.getOuterDivider(this, shortJava, shortOther);
		
		return answer;
	}

	
	//-----these methods print to the file specified by the PrintWriter 'writer'
	public void print(DataOutputStream writer)
	{
		int i = 0;
		
		pgmDefn.print(writer);
		
		while (i < attribute_vector.size())
		{
			((AttributeDefn)(attribute_vector.elementAt(i))).print(writer);
			i++;
		}
		
		i=0;
		
		while (i < const_vector.size())
		{
			((ConstructorDefn)(const_vector.elementAt(i))).print(writer);
			i++;
		}
		
		i=0;
		
		while (i < method_vector.size())
		{
			((MethodDefn)(method_vector.elementAt(i))).print(writer);
			i++;
		}
		
		try
		{
			final String del = ":";
			writer.writeUTF("<EXTRA>"+del);
			if ( !(sourceFileName.trim().equals("")) )
			{
				writer.writeUTF("<source_filename>"+del+sourceFileName+del+"<end_source_filename>"+del);
			}
		
			if ( !(javadocsFileName.trim().equals("")) )
			{
				writer.writeUTF("<javadocs_filename>"+del+javadocsFileName+del+"<end_javadocs_filename>"+del);
			}
			writer.writeUTF("<END_EXTRA>"+del);
		
			writer.writeUTF("<end_line>");
		}
		catch(IOException e)
		{
			System.err.println(e);
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
			System.out.println("An IOException was thrown in println(RandomAccessFile) in Interface.java");
		}
	}
	
	public void printExtends(RandomAccessFile writer)
	{
		pgmDefn.printExtends(writer);
	}
	
	public void printlnExtends(RandomAccessFile writer)
	{
		pgmDefn.printlnExtends(writer);
	}

	public void printLink(RandomAccessFile linker, String file)
	{
		pgmDefn.printLink(linker,file);
	}
	
	public void printlnLink(RandomAccessFile linker, String file)
	{
		pgmDefn.printlnLink(linker,file);
	}
	
	public void makeInterfaceObject(InterfaceDefn pgm, Vector att_vec, Vector c_vec, Vector method_vec, String sourceF, String javaF)
	{
		pgmDefn = pgm;
		const_vector = c_vec;
		attribute_vector = att_vec;
		method_vector = method_vec;
		sourceFileName = sourceF;
		javadocsFileName = javaF;
	}
	
	public Interface Clone()
	{
		return (new Interface( (pgmDefn.Clone()), (Vector)(attribute_vector.clone()), (Vector)(const_vector.clone()), (Vector)(method_vector.clone()), (new String(sourceFileName)), (new String(javadocsFileName)) ));
	}
	
	public JPanel getVerticalInterfaceJPanel()
	{

		//this creates the objects
		JPanel pgmJPanel = new JPanel();
		JPanel attributeScrollPanePanel = new JPanel();
		JPanel constructorScrollPanePanel = new JPanel();
		JPanel methodScrollPanePanel = new JPanel();
		JPanel pgmDefnJPanel = new JPanel();
		Vector attJPanelVec = new Vector();
		Vector constJPanelVec = new Vector();
		Vector methodJPanelVec = new Vector();
		
		//now to assign them values
		pgmDefnJPanel = pgmDefn.getInterfaceDefnJPanel();
		
		int i = 0;
		
		while (i < attribute_vector.size())
		{	
			attJPanelVec.add( ( (AttributeDefn)(attribute_vector.elementAt(i))).getAttributeJPanel() );
			i++;
		}
		
		i = 0;
		
		while (i < const_vector.size())
		{	
			constJPanelVec.add( ( (ConstructorDefn)(const_vector.elementAt(i)) ).getConstJPanel() );
			i++;
		}

		i = 0;

		while (i < method_vector.size())
		{	
			methodJPanelVec.add( ( (MethodDefn)(method_vector.elementAt(i)) ).getMethodJPanel() );
			i++;
		}			

		pgmJPanel.setLayout(new GridLayout(8,0));
		
		//this part adds the info for the InterfaceDefn object
		pgmJPanel.add(new JLabel("General Information:  "));
		pgmJPanel.add(pgmDefnJPanel);
		
		int size1 = attJPanelVec.size();
		int size2 = constJPanelVec.size();
		int size3 = methodJPanelVec.size();
		
		if (size1 == 0)
			size1 = 1;
		if (size2 == 0)
			size2 = 1;
		if (size3 == 0)
			size3 = 1;	
		
		//this sets the layout
		attributeScrollPanePanel.setLayout(new GridLayout(size1,0));
		constructorScrollPanePanel.setLayout(new GridLayout(size2,0));
		methodScrollPanePanel.setLayout(new GridLayout(size3,0));
		
		//this adds all of the specific JPanels (for instance the JPanel associated with an AttributeDefnJPanel
		//object) to each panel like attributeScrollPanePanel
		
		int j = 0;
		
		while (j < attJPanelVec.size())
		{
			attributeScrollPanePanel.add( (JPanel)(attJPanelVec.elementAt(j)) );
			j++;
		}	
		
		j = 0;
		
		while (j < constJPanelVec.size())
		{
			constructorScrollPanePanel.add( (JPanel)(constJPanelVec.elementAt(j)) );
			j++;
		}
			
		j = 0;
		
		while (j < methodJPanelVec.size() )
		{
			methodScrollPanePanel.add( (JPanel)(methodJPanelVec.elementAt(j)) );
			j++;
		}
		
		//this part adds the three scrollpanes and a label for each
		pgmJPanel.add(new JLabel("Attributes:  "));
		pgmJPanel.add(new JScrollPane(attributeScrollPanePanel));
		pgmJPanel.add(new JLabel("Constructors:  "));
		pgmJPanel.add(new JScrollPane(constructorScrollPanePanel));
		pgmJPanel.add(new JLabel("Methods:  "));
		pgmJPanel.add(new JScrollPane(methodScrollPanePanel));

		
		pgmJPanel.setVisible(true);
		
		return pgmJPanel;
	}
	
	public JPanel getHorizontalInterfaceJPanel()
	{

		//this creates the objects
		JPanel pgmJPanel = new JPanel();
		JPanel attributeScrollPanePanel = new JPanel();
		JPanel constructorScrollPanePanel = new JPanel();
		JPanel methodScrollPanePanel = new JPanel();
		JPanel pgmDefnJPanel = new JPanel();
		Vector attJPanelVec = new Vector();
		Vector constJPanelVec = new Vector();
		Vector methodJPanelVec = new Vector();
		
		//now to assign them values
		pgmDefnJPanel = pgmDefn.getInterfaceDefnJPanel();
		
		int i = 0;
		
		while (i < attribute_vector.size())
		{	
			attJPanelVec.add( ( (AttributeDefn)(attribute_vector.elementAt(i))).getAttributeJPanel() );
			i++;
		}
		
		i = 0;
		
		while (i < const_vector.size())
		{	
			constJPanelVec.add( ( (ConstructorDefn)(const_vector.elementAt(i)) ).getConstJPanel() );
			i++;
		}

		i = 0;

		while (i < method_vector.size())
		{	
			methodJPanelVec.add( ( (MethodDefn)(method_vector.elementAt(i)) ).getMethodJPanel() );
			i++;
		}			

		int size1 = attJPanelVec.size();
		int size2 = constJPanelVec.size();
		int size3 = methodJPanelVec.size();
		
		if (size1 == 0)
			size1 = 1;
		if (size2 == 0)
			size2 = 1;
		if (size3 == 0)
			size3 = 1;	
		
		//this sets the layout
		attributeScrollPanePanel.setLayout(new GridLayout(size1,0));
		constructorScrollPanePanel.setLayout(new GridLayout(size2,0));
		methodScrollPanePanel.setLayout(new GridLayout(size3,0));
		
		//this adds all of the specific JPanels (for instance the JPanel associated with an AttributeDefnJPanel
		//object) to each panel like attributeScrollPanePanel
		
		int j = 0;
		
		while (j < attJPanelVec.size())
		{
			attributeScrollPanePanel.add( (JPanel)(attJPanelVec.elementAt(j)) );
			j++;
		}	
		
		j = 0;
		
		while (j < constJPanelVec.size())
		{
			constructorScrollPanePanel.add( (JPanel)(constJPanelVec.elementAt(j)) );
			j++;
		}
			
		j = 0;
		
		while (j < methodJPanelVec.size() )
		{
			methodScrollPanePanel.add( (JPanel)(methodJPanelVec.elementAt(j)) );
			j++;
		}
		
		pgmJPanel.setLayout(new GridLayout(4,2));
		
		//now to make the JPanels that hold the JLabels
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BorderLayout());
		infoPanel.add(new JLabel("General Information"), BorderLayout.SOUTH);
		
		JPanel attPanel = new JPanel();
		attPanel.setLayout(new BorderLayout());
		attPanel.add(new JLabel("Attributes"), BorderLayout.SOUTH);
		
		JPanel constPanel = new JPanel();
		constPanel.setLayout(new BorderLayout());
		constPanel.add(new JLabel("Constructors"), BorderLayout.SOUTH);
		
		JPanel methodPanel = new JPanel();
		methodPanel.setLayout(new BorderLayout());
		methodPanel.add(new JLabel("Methods"), BorderLayout.SOUTH);
		
		pgmJPanel.add(infoPanel);
		pgmJPanel.add(attPanel);
		
		JScrollPane pgmDefnJPanelScrollPane = new JScrollPane(pgmDefnJPanel);
		pgmJPanel.add(pgmDefnJPanelScrollPane);
		pgmJPanel.add(new JScrollPane(attributeScrollPanePanel));
		
		pgmJPanel.add(constPanel);
		pgmJPanel.add(methodPanel);
		
		pgmJPanel.add(new JScrollPane(constructorScrollPanePanel));
		pgmJPanel.add(new JScrollPane(methodScrollPanePanel));
		
		pgmJPanel.setVisible(true);
		
		return pgmJPanel;
	}
	
	public String getStringInJavadocFormat(boolean shortJava, boolean shortOther)
	{
		String str = "";
		final String tab = "        ";
		
		str = str + pgmDefn.getStringInJavadocFormat(shortJava, shortOther) + "\n";

		if (attribute_vector.size() == 1)
		{
			str = str + tab + "Attribute\n";
			str = str + tab + "=======\n";
		}
		else
		{
			str = str + tab + "Attributes\n";
			str = str + tab + "=======\n";
		}
		
		for (int i = 0; i < attribute_vector.size(); i++)
		{
			str = str + tab + tab + ((AttributeDefn)(attribute_vector.elementAt(i))).getStringInJavadocFormat(shortJava, shortOther)+"\n";
			str = str + "\n";
		}
		
		str = str + "\n";  //this makes a blank line
		
		if (const_vector.size() == 1)
		{
			str = str + tab + "Constructor\n";
			str = str + tab + "=========\n";
		}
		else
		{
			str = str + tab + "Constructors\n";
			str = str + tab + "=========\n";
		}
		
		for (int i = 0; i < const_vector.size(); i++)
		{
			str = str + tab + tab + ((ConstructorDefn)(const_vector.elementAt(i))).getStringInJavadocFormat(shortJava, shortOther)+"\n";
			str = str + "\n";
		}
		
		str = str + "\n";
		
		if (method_vector.size() == 1)
		{
			str = str + tab + "Method\n";
			str = str + tab + "======\n";
		}
		else
		{
			str = str + tab + "Methods\n";
			str = str + tab + "======\n";
		}
		
		for (int i = 0; i < method_vector.size(); i++)
		{
			str = str + tab + tab + ((MethodDefn)(method_vector.elementAt(i))).getStringInJavadocFormat(shortJava, shortOther)+"\n";
			str = str + "\n";
		}
		
		str = str + "}";
		
		return str;
	}
	
	//this str is assumed to be read from the .inf file so the string
	//should be in the exact format of a line from the .inf file
	//the string is obtained by using infoRAF.readLine() where in dataFileUtilities.java
	//infoRAF is the random access file for the .inf file
	//ln is the string which is parsed to make the Interface object
	//NUM is the line number in the .inf file that the object is located
	public Interface makeInterfaceObject(String ln)
	{
		Interface intF = new Interface();  //this is the InterfaceDefn object that will be returned
		InterfaceDefn intFDefn = new InterfaceDefn();
		Vector attVec = new Vector();
		Vector constVec = new Vector();
		Vector methodVec = new Vector();
		String sourceFile = "";
		String javadocsFile = "";
		
		if (!ln.trim().equals("") && (ln != null))
		{
			StringTokenizer tokenizer = new StringTokenizer(ln,":");
			String token = "";
			while(tokenizer.hasMoreTokens())
			{
				token = tokenizer.nextToken();
				
				if (token.equals("<INTERFACE>"))
				{					
					Vector importsVec = new Vector(); //a vector of strings
					Vector characteristicsVec = new Vector();  //a vector of strings
					Vector implementsVec = new Vector();  //a vector of strings
					String interfaceType = "";
					String interfaceName = "";
					String extendsName = "";
					String packageName = "";
					
					token = tokenizer.nextToken();
					while (!token.equals("<END_INTERFACE>"))
					{
						if (token.equals("<interface_imports>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_interface_imports>"))
							{
								importsVec.add(token);
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<interface_characteristics>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_interface_characteristics>"))
							{
								characteristicsVec.add(token);
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<interface_type>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_interface_type>"))
							{
								interfaceType = token;
								token = tokenizer.nextToken();			
							}
						}
						else if (token.equals("<interface_name>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_interface_name>"))
							{
								interfaceName = token;
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<interface_extends>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_interface_extends>"))
							{
								extendsName = token;
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<interface_implements>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_interface_implements>"))
							{
								implementsVec.add(token);
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<interface_package_name>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_interface_package_name>"))
							{
								packageName = token;
								token = tokenizer.nextToken();
							}
						}
						
						token = tokenizer.nextToken();
					}
					
					intFDefn = new InterfaceDefn(characteristicsVec, interfaceType, interfaceName, extendsName, implementsVec, importsVec, packageName);
				}
				else if (token.equals("<ATTRIBUTE>"))
				{					
					Vector charVec = new Vector();
					String attName = "";
					String attType = "";
					
					token = tokenizer.nextToken();
					while (!token.equals("<END_ATTRIBUTE>"))
					{
						if (token.equals("<attribute_characteristics>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_attribute_characteristics>"))
							{
								charVec.add(token);
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<attribute_type>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_attribute_type>"))
							{
								attType = token;
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<attribute_name>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_attribute_name>"))
							{
								attName = token;
								token = tokenizer.nextToken();
							}
						}
						
						token = tokenizer.nextToken();
					}
					
					attVec.add(new AttributeDefn(charVec, attName, attType));
				}
				else if (token.equals("<CONSTRUCTOR>"))
				{
					Vector charVec = new Vector();  //a vector of strings
					Vector paramVec = new Vector(); //a vector of strings
					String constName = "";
					
					token = tokenizer.nextToken();
					while (!token.equals("<END_CONSTRUCTOR>"))
					{
						if (token.equals("<constructor_characteristics>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_constructor_characteristics>"))
							{
								charVec.add(token);
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<constructor_name>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_constructor_name>"))
							{
								constName = token;
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<constructor_parameters>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_constructor_parameters>"))
							{
								paramVec.add(token);
								token = tokenizer.nextToken();
							}
						}
						
						token = tokenizer.nextToken();
					}
					
					constVec.add(new ConstructorDefn(charVec, paramVec, constName));
				}
				else if (token.equals("<METHOD>"))
				{				
					Vector charVec = new Vector();  //a vector of strings
					Vector paramVec = new Vector(); //a vector of strins
					String methodName = "";
					String returnType = "";
					
					token = tokenizer.nextToken();
					while (!token.equals("<END_METHOD>"))
					{
						if (token.equals("<method_characteristics>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_method_characteristics>"))
							{
								charVec.add(token);
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<method_return_type>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_method_return_type>"))
							{
								returnType = token;
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<method_name>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_method_name>"))
							{
								methodName = token;
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<method_parameters>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_method_parameters>"))
							{
								paramVec.add(token);
								token = tokenizer.nextToken();
							}
						}
						
						token = tokenizer.nextToken();
					}
					
					methodVec.add(new MethodDefn(charVec, paramVec, methodName, returnType));
				}
				else if (token.equals("<EXTRA>"))
				{					
					token = tokenizer.nextToken();
					while (!token.equals("<END_EXTRA>"))
					{
						if (token.equals("<source_filename>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_source_filename>"))
							{
								sourceFile = token;
								token = tokenizer.nextToken();
							}
						}
						else if (token.equals("<javadocs_filename>"))
						{
							token = tokenizer.nextToken();
							while (!token.equals("<end_javadocs_filename>"))
							{
								javadocsFile = token;
								token = tokenizer.nextToken();
							}
						}
						
						token = tokenizer.nextToken();
					}
				}
			}
		}
		intF = new Interface(intFDefn, attVec, constVec, methodVec, sourceFile, javadocsFile);
		return intF;
	}	
}
