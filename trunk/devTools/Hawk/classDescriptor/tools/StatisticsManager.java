/*
 * File:  StatisticsManager.java
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
 * Revision 1.1  2004/02/07 05:10:48  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class StatisticsManager
{
	private Project project;
	
	public StatisticsManager(Project pro)
	{
		project = pro;
	}
	
	public int getTotalNumberOfClassesAndInterfaces()
	{
		return project.getInterfaceVec().size();
	}
	
	public int getNumberOfInterfaces()
	{
		int answer = 0;
		
		for (int i=0; i<project.getInterfaceVec().size(); i++)
			if ( ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isInterface())
				answer++;
		
		return answer;
	}

	public int getNumberOfClasses()
	{
		int answer = 0;
		
		for (int i=0; i<project.getInterfaceVec().size(); i++)
			if ( ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isClass())
				answer++;
		
		return answer;
	}

	public int getNumberOfConcreteNonInnerClasses()
	{
		int answer = 0;
		
		for (int i=0; i<project.getInterfaceVec().size(); i++)
			if ( ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isClass() && !(((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isAbstract()) && !(((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isInnerClass()))
				answer++;
		
		return answer;
	}

	public int getNumberOfConcreteInnerClasses()
	{
		int answer = 0;
		
		for (int i=0; i<project.getInterfaceVec().size(); i++)
			if ( ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isClass() && !(((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isAbstract()) && (((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isInnerClass()))
				answer++;
		
		return answer;
	}

	public int getTotalNumberOfConcreteClasses()
	{
		int answer = 0;
		
		for (int i=0; i<project.getInterfaceVec().size(); i++)
			if ( ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isClass() && !(((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isAbstract()) )
				answer++;
		
		return answer;
	}
	
	public int getTotalNumberOfAbstractClasses()
	{
		int answer = 0;
		
		for (int i=0; i<project.getInterfaceVec().size(); i++)
			if ( ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isClass() && ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isAbstract())
				answer++;
		
		return answer;
	}

	public int getNumberOfAbstractInnerClasses()
	{
		int answer = 0;
		
		for (int i=0; i<project.getInterfaceVec().size(); i++)
			if ( ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isClass() && ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isAbstract() && ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isClass() && ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isInnerClass())
				answer++;
		
		return answer;
	}

	public int getNumberOfAbstractNonInnerClasses()
	{
		int answer = 0;
		
		for (int i=0; i<project.getInterfaceVec().size(); i++)
			if ( ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isClass() && ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isAbstract() && ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isClass() && !((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isInnerClass())
				answer++;
		
		return answer;
	}

	public int getTotalNumberOfInnerClass()
	{
		int answer = 0;
		
		for (int i=0; i<project.getInterfaceVec().size(); i++)
			if ( ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isInnerClass() && !((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isInterface())
				answer++;
		
		return answer;
	}

	public int getTotalNumberOfNonInnerClass()
	{
		int answer = 0;
		
		for (int i=0; i<project.getInterfaceVec().size(); i++)
			if ( !((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isInnerClass() && !((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn().isInterface())
				answer++;
		
		return answer;
	}
}
