/*
 * File:  SearchUtilities.java
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
 * Revision 1.1  2004/02/07 05:10:47  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

import java.util.Vector;

import devTools.Hawk.classDescriptor.gui.panel.search.AttributeDefnOptionsJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.BasicOptionsJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.ConstructorDefnOptionsJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.InterfaceDefnOptionsJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.MethodDefnOptionsJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.VectorOptionsJPanel;
import devTools.Hawk.classDescriptor.gui.panel.search.AdvancedOptionsJPanel;
import devTools.Hawk.classDescriptor.modeledObjects.AttributeDefn;
import devTools.Hawk.classDescriptor.modeledObjects.ConstructorDefn;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.InterfaceDefn;
import devTools.Hawk.classDescriptor.modeledObjects.MethodDefn;
/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SearchUtilities
{
	/**
	 * This returns true if str1 is a match for str2 and false if not given the conditions matchMustContainWord, matchEntireWord, caseSensitive.
	 * @param str1 The String you have.
	 * @param str2 The String you want to check against.
	 * @param matchMustContainWord True if str2 must contain str1 and false if not.
	 * @param matchEntireWord For example if str1 = "str" and str2 = "String".  Setting matchEntireWord as false would say that str1 is a match for str2
	 * because "str" is in "String" (notice case does not matter).
	 * @param caseSensitive True if the comparison should be case sensitive.
	 * @return If str1 is a match to str2 or not.
	 */
	public static boolean stringMatches(String str1, String str2, boolean matchMustContainWord, boolean matchEntireWord, boolean caseSensitive)
	{
		boolean result = false;
		
		if (matchEntireWord)
			result = isEqual(str1, str2, caseSensitive);
		else
			result = isSubstring(str1, str2, caseSensitive);

/*		
		System.out.println("     "+str1+" vs. "+str2);
		if (result == matchMustContainWord)
			System.out.println("     +++++++++++SUCCESS");
		else
			System.out.println("     ----------------------FAILURE");
*/			
		return (result == matchMustContainWord);
	}
	
		/**
		 * Returns true if str1 is a substring of str2 and false if it isn't.
		 * @param str1 The String to look for
		 * @param str2 The String to look in
		 * @param caseSensitive Whether or not to be case sensitive
		 * @return
		 */
		public static boolean isSubstring(String str1, String str2, boolean caseSensitive)
		{
			boolean answer = false;
			
			if (!caseSensitive)
			{
				str1 = str1.toUpperCase();
				str2 = str2.toUpperCase();
			}
			
			int diff = str2.length()-str1.length();
			if (diff >=0)
			{
				for (int i=0; i<=diff; i++)
				{
					if (str2.substring(i,i+str1.length()).equals(str1))
						answer = true;
				}
			}
					
			return answer;
		}
		
		/**
		 * Returns true if str1 is equal to str2 and false if it isn't.
		 * @param str1 The String to look for
		 * @param str2 The String to look in
		 * @param caseSensitive Whether or not to be case sensitive
		 * @return
		 */
		public static boolean isEqual(String str1, String str2, boolean caseSensitive)
		{
			if (!caseSensitive)
			{
				str1 = str1.toUpperCase();
				str2 = str2.toUpperCase();
			}
			
			return str1.equals(str2);
		}
	/**
	 * Takes a vector of BasicOptionsJPanel objects and for some index, it compares the string from dataVec and sees if it is a match to what is entered
	 * in the JTextField in the BasicOptionsJPanel given the conditions in the BasicOptionsJPanel.  If the textFrom the JTextField when trimmed is equal to ""
	 * then the string is ingored.
	 * @param panelVec A Vector of BasicOptionsJPanels
	 * @param dataVec A Vector of Strings
	 * @return True if ALL panels pass
	 */
	public static boolean checkBasicSearchOptions(Vector panelVec, Vector dataVec)
	{			
		boolean answer = true;
		int i=0;
		while (i<panelVec.size() && answer)
		{
			BasicOptionsJPanel panel = (BasicOptionsJPanel)panelVec.elementAt(i);
			if (!panel.getTextFieldText().trim().equals(""))
				answer = SearchUtilities.stringMatches(panel.getTextFieldText(), (String)dataVec.elementAt(i), panel.mustMatchContainWord(), panel.matchEntireWord(), panel.matchCaseSensitive());
			i++;
		}
		return answer;
	}
	
	public static boolean checkVectorSearchOptions(VectorOptionsJPanel panel, Vector dataVec)
	{
		int location = 0;
		int expectedLocation = 0;
		int locationVal = 0;
		boolean found = false;
		int i=0;
		int j=0;
		int foundLocation = 0;
		String text = "";
		
		boolean answer = panel.getGlobalOptionsJPanel().isAValidNumberOfItems(dataVec.size());
		if (answer)
		{
//			System.out.println("Passed the vector size check");
			Vector dataVec2 = recreateVector(dataVec);
			
			while (i<panel.getOptionsVec().size() && answer)
			{
				AdvancedOptionsJPanel newPanel = ((AdvancedOptionsJPanel)panel.getOptionsVec().elementAt(i));
				locationVal = newPanel.getLocationBoxSelectedValue();
				if (locationVal == AdvancedOptionsJPanel.ANYWHERE)
					expectedLocation = -1;
				else if (locationVal == AdvancedOptionsJPanel.AT_LAST_ELEMENT)
					expectedLocation = dataVec.size();
				else if (locationVal == AdvancedOptionsJPanel.AT_ELEMENT)
					expectedLocation = newPanel.getNumberSpinnerValue();
				
//				System.out.println("expectedLodation="+expectedLocation);
				
				text = newPanel.getTextFieldText();
				found = false;
				j=0;
				String testText = "";
				while (!found && j<dataVec2.size())
				{
					testText = ((VectorStringElement)dataVec2.elementAt(j)).str;
					if (stringMatches(testText , text, newPanel.mustMatchContainWord(), newPanel.matchEntireWord(), newPanel.matchCaseSensitive()) )
					{
						foundLocation = ((VectorStringElement)dataVec2.elementAt(j)).index+1;
						found = true;
						dataVec2.remove(j);
					}
					j++;
				}

/*				
				System.out.println("found="+found);
				System.out.println("foundLocation="+foundLocation);
				System.out.println(""+testText+" vs "+text);
*/				
				if ( (expectedLocation==-1 || expectedLocation==foundLocation)&&found )
					answer = true;
				else
					answer = false;
					
				i++;
			}
		}
//		System.out.println("anwer="+answer);
//		System.out.println();
		return answer;
	}

	
	/**
	 * This takes a Vector of Strings and creates a Vector of VectorStringElement objects.  This object contains a str member which contains the element's
	 * String and index which is the String's index.
	 * @param vec
	 * @return
	 */
	public static Vector recreateVector(Vector vec)
	{
		Vector newVec = new Vector();
		for (int i=0; i<vec.size(); i++)
		{
			String str = (String)(vec.elementAt(i));
			System.out.println("in recreateVector(Vector), str="+str);
			newVec.add(new VectorStringElement(str,i));
		}
		return newVec;
	}
	
	public static boolean matches(AttributeDefnOptionsJPanel panel, AttributeDefn attD)
	{
		Vector panelsVector = new Vector();
		Vector dataVector = new Vector();
			dataVector.add(attD.getAttribute_name());
			panelsVector.add(panel.getNamePanel());
			dataVector.add(attD.getAttribute_type());
			panelsVector.add(panel.getTypePanel());
		boolean answer = checkBasicSearchOptions(panelsVector, dataVector);
		if (answer)
		{
			answer = checkVectorSearchOptions(panel.getCharPanel(), attD.getAttribute_char_vector());
		}
			
		return answer;
	}

	public static boolean matches(ConstructorDefnOptionsJPanel panel, ConstructorDefn constD)
	{
		Vector panelsVector = new Vector();
		Vector dataVector = new Vector();
			dataVector.add(constD.getConst_name());
			panelsVector.add(panel.getNamePanel());
		boolean answer = checkBasicSearchOptions(panelsVector, dataVector);
		if (answer)
		{
			answer = checkVectorSearchOptions(panel.getCharPanel(), constD.getConst_char_vector());
				
				if (answer)
					answer = checkVectorSearchOptions(panel.getParametersPanel(), constD.getConst_parameter_vector());
		}
			
		return answer;
	}
	
	public static boolean matches(MethodDefnOptionsJPanel panel, MethodDefn methD)
	{
		Vector panelsVector = new Vector();
		Vector dataVector = new Vector();
			dataVector.add(methD.getMethod_name());
			panelsVector.add(panel.getNamePanel());
			dataVector.add(methD.getMethod_return_type());
			panelsVector.add(panel.getTypePanel());
		boolean answer = checkBasicSearchOptions(panelsVector, dataVector);
		if (answer)
		{
			answer = checkVectorSearchOptions(panel.getCharPanel(), methD.getMethod_char_vector());
				
				if (answer)
					answer = checkVectorSearchOptions(panel.getParametersPanel(), methD.getMethod_parameter_vector());
		}
			
		return answer;
	}
	
	public static boolean matches(InterfaceDefnOptionsJPanel panel, InterfaceDefn intfD)
	{
		boolean answer = true;
		
		if (panel.getType() == InterfaceDefnOptionsJPanel.CLASS && intfD.getInterface_type().equals("class"))
			answer = true;
		else if (panel.getType() == InterfaceDefnOptionsJPanel.INTERFACE && intfD.getInterface_type().equals("interface"))
			answer = true;
		else if (panel.getType() == InterfaceDefnOptionsJPanel.ANY)
			answer = true;
		else
			answer = false;
		
		if (answer)
		{
			Vector panelsVector = new Vector();
			Vector dataVector = new Vector();
				dataVector.add(intfD.getInterface_name());
				panelsVector.add(panel.getNamePanel());
				dataVector.add(intfD.getInterface_extends());
				panelsVector.add(panel.getExtendsPanel());
				dataVector.add(intfD.getPackage_Name());
				panelsVector.add(panel.getPackageNamePanel());
				answer = checkBasicSearchOptions(panelsVector, dataVector);
				
				if (answer)
				{
					answer = checkVectorSearchOptions(panel.getCharPanel(), intfD.getInterface_char_vector());
						
						if (answer)
							answer = checkVectorSearchOptions(panel.getImplementsPanel(), intfD.getInterface_implements_vector());
				}
		}
		
		return answer;
	}
	
	public static boolean matches(Interface intF, AttributeDefnOptionsJPanel attPanel, ConstructorDefnOptionsJPanel constPanel, MethodDefnOptionsJPanel methPanel, InterfaceDefnOptionsJPanel intfPanel)
	{
		int i=0;
		boolean answer = matches(intfPanel, intF.getPgmDefn());
		if (answer)
		{
			answer = false;
			i=0;
			while (i<intF.getAttribute_vector().size() && !answer)
			{
				answer = matches(attPanel, (AttributeDefn)intF.getAttribute_vector().elementAt(i));
				i++;
			}
			
			if (answer)
			{
				answer = false;
				i=0;
				while (i<intF.getConst_vector().size() && !answer)
				{
					answer = matches(constPanel, (ConstructorDefn)intF.getConst_vector().elementAt(i));
					i++;
				}
				
				if (answer)
				{
					answer = false;
					i=0;
					while (i<intF.getMethod_vector().size() && !answer)
					{
						answer = matches(methPanel, (MethodDefn)intF.getMethod_vector().elementAt(i));
						i++;
					}
				}
			}
		}
		
		return answer;
	}
	
	//intFVec is a Vector of Interface objects
	public static Vector findMatches(Vector intFVec, AttributeDefnOptionsJPanel attPanel, ConstructorDefnOptionsJPanel constPanel, MethodDefnOptionsJPanel methPanel, InterfaceDefnOptionsJPanel intfPanel)
	{
		Vector foundVec = new Vector();
		Interface foundIntf = new Interface();
		for (int i=0; i<intFVec.size(); i++)
		{
			foundIntf = (Interface)intFVec.elementAt(i);
			System.out.println("ON CLASS "+foundIntf.getPgmDefn().getInterface_name());
			if (matches(foundIntf, attPanel, constPanel, methPanel, intfPanel))
				foundVec.add(foundIntf);
		}
		
		return foundVec;
	}
	
	private static class VectorStringElement
	{
		public String str;
		public int index;
		
		public VectorStringElement(String s, int i)
		{
			str = s;
			index = i;
		}
		
		public String display()
		{
			return "(str="+str+", inedex="+index+")";
		}
	}
}