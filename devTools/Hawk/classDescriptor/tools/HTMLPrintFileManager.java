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
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HTMLPrintFileManager extends PrintManager
{
	public HTMLPrintFileManager(String file, String access) throws FileNotFoundException
	{
		super(file,access);
	}
	
	public HTMLPrintFileManager(String access) throws FileNotFoundException
	{
		super(access);
	}
		
	public void printUML(Interface intf, boolean shortJava, boolean shortOther)
	{
		try
		{
			int longest = longestLine(intf, shortJava, shortOther);
			int percent = (int)((longest / 80.0)*1.5);
			
			percent = 100;
			
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
}
