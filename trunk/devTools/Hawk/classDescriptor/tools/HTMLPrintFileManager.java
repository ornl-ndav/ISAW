/*
 * File:  HTMLPrintFileManager.java
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
 * Revision 1.2  2004/03/11 18:42:27  bouzekc
 * Documented file using javadoc statements.
 *
 * Revision 1.1  2004/02/07 05:10:46  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

import java.io.FileNotFoundException;

import devTools.Hawk.classDescriptor.modeledObjects.AttributeDefn;
import devTools.Hawk.classDescriptor.modeledObjects.ConstructorDefn;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.MethodDefn;

/**
 * This class is used to write information about classes and interfaces to a file in HTML format.  
 * Currently, this class is still under construction.
 * @author Dominic Kramer
 */
public class HTMLPrintFileManager extends PrintManager
{
	/**
	 * Constructor which creates an HTMLPrintFileManager object to print to the file "file" with access "access"
	 * @param file The file to print to
	 * @param access The file's read-write access (i.e. "r", "w", or "rw")
	 * @throws FileNotFoundException
	 */
	public HTMLPrintFileManager(String file, String access) throws FileNotFoundException
	{
		super(file,access);
	}
	
	/**
	 * Constructor which creates an HTMLPrintFileManager object.  This constructor opens a file chooser to allow the 
	 * user to choose where to print the information to.
	 * @param access The read-write access (i.e. "r", "w", or "rw")
	 * @throws FileNotFoundException
	 */
	public HTMLPrintFileManager(String access) throws FileNotFoundException
	{
		super(access);
	}
	
	/**
	 * This method writes a UML diagram to the file for the class or interface supecified by the Interface 
	 * object supplied.
	 * @param intf The Interface object whose data is to be written.
	 * @param shortJava Set this to true if java names are to be shortened.  For example, 
	 * java.lang.String will be shortened to String.
	 * @param shortOther Set this to true if non-java names are to be shortened.
	 */
	public void printUML(Interface intf, boolean shortJava, boolean shortOther)
	{
		try
		{
			int longest = longestLine(intf, shortJava, shortOther);
			int percent = (int)((longest / 77.0));
			
			writeBytes("<table style=\"text-align: left; width: "+ percent+ "%;\" border=\"1\" cellspacing=\"2\" cellpadding=\"2\">\n  <tbody>\n");
			writeBytes("    <tr><td style=\"vertical-align: top;\">"+getUMLNameLine(intf.getPgmDefn(),shortJava,shortOther)+"</td></tr>\n");
			writeBytes("    <tr><td style=\"vertical-align: top;\">\n");
			for (int i=0; i<intf.getAttribute_vector().size(); i++)
				writeBytes("        "+getUMLAttributeLine((AttributeDefn)intf.getAttribute_vector().elementAt(i),shortJava,shortOther)+"<br>\n");			
			writeBytes("  </td></tr>");
			
			writeBytes("    <tr><td style=\"vertical-align: top;\">\n");
			for (int i=0; i<intf.getConst_vector().size(); i++)
				writeBytes("        "+getUMLConstructorLine((ConstructorDefn)intf.getConst_vector().elementAt(i),shortJava,shortOther)+"<br>\n");
			writeBytes("  </td></tr>");
						
			writeBytes("    <tr><td style=\"vertical-align: top;\">\n");
			for (int i=0; i<intf.getMethod_vector().size(); i++)
				writeBytes("        "+getUMLMethodLine((MethodDefn)intf.getMethod_vector().elementAt(i),shortJava,shortOther)+"<br>\n");
			writeBytes("  </td></tr>");			
			
			writeBytes("  </tbody>\n</table>\n");
		}
		catch (Exception e)
		{
			SystemsManager.printStackTrace(e);
		}
	}
	
	/**
	 * Returns the HTML code for a single UML diagram.  Note:  This method only returns the code 
	 * for the diagram (represented as a table) and does not include the html, body, end-html, and end-body flags.
	 * @param intf This Interface whose data is to be analyzed.
	 * @param shortJava Set this to true if java names are to be shortened.
	 * @param shortOther Set this to true if non-java names are to be shortened.
	 * @return The HTML code for a single UML diagram for the Interface supplied.
	 */
	public static String getHTMLCodeForSingleUML(Interface intf, boolean shortJava, boolean shortOther)
	{
		String str = "";
		int longest = longestLine(intf, shortJava, shortOther);
//		int percent = (int)((longest / 77.0));
		
		str += "<table style=\"text-align: left; width: "+ "100"+ "%;\" border=\"1\" cellspacing=\"2\" cellpadding=\"2\">\n  <tbody>\n";
		str += "    <tr><td style=\"text-align: center;\"><b>"+getUMLNameLine(intf.getPgmDefn(),shortJava,shortOther)+"</b></td></tr>\n";
		str += "    <tr><td style=\"vertical-align: top;\">\n";
		for (int i=0; i<intf.getAttribute_vector().size(); i++)
		{
			if (i==intf.getAttribute_vector().size()-1)
				str += "        "+getUMLAttributeLine((AttributeDefn)intf.getAttribute_vector().elementAt(i),shortJava,shortOther)+"\n";
			else
				str += "        "+getUMLAttributeLine((AttributeDefn)intf.getAttribute_vector().elementAt(i),shortJava,shortOther)+"<br>\n";
		}
		
		str += "  </td></tr>";
			
		str += "    <tr><td style=\"vertical-align: top;\">\n";
		for (int i=0; i<intf.getConst_vector().size(); i++)
		{
			if (i==intf.getConst_vector().size()-1)
				str += "        "+getUMLConstructorLine((ConstructorDefn)intf.getConst_vector().elementAt(i),shortJava,shortOther)+"\n";
			else
				str += "        "+getUMLConstructorLine((ConstructorDefn)intf.getConst_vector().elementAt(i),shortJava,shortOther)+"<br>\n";
		}
		str += "  </td></tr>";
		
		str += "    <tr><td style=\"vertical-align: top;\">\n";
		for (int i=0; i<intf.getMethod_vector().size(); i++)
		{
			if (i==intf.getMethod_vector().size()-1)
				str += "        "+getUMLMethodLine((MethodDefn)intf.getMethod_vector().elementAt(i),shortJava,shortOther)+"\n";
			else
				str += "        "+getUMLMethodLine((MethodDefn)intf.getMethod_vector().elementAt(i),shortJava,shortOther)+"<br>\n";
		}
		str += "  </td></tr>";
			
		str += "  </tbody>\n</table>\n";
		
		return str;
	}
}
