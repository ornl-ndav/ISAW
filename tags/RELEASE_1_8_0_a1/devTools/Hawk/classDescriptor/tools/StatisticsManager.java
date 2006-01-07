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
 * Revision 1.3  2004/03/12 19:46:20  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:10:48  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;

import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.InterfaceDefn;
import devTools.Hawk.classDescriptor.modeledObjects.Project;

/**
 * This class is used to obtain statistical information about a project such as the number 
 * of interfaces, number of classes, number of inner classes, number of outer classes, etc.
 * @author Dominic Kramer
 */
public class StatisticsManager
{
	/** The project that this manager is analyzing. */
	private Project project;
	
	/**
	 * Create a new StatisticsManager.
	 * @param pro The project to analyze.
	 */
	public StatisticsManager(Project pro)
	{
		project = pro;
	}
	
	//--------------------Methods to obtain class data---------------------------------------
		//----------------Abstract---------------------------------------------------------------------
		/**
		 * Get the number of Interface objects that are abstract, inner, and 
		 * are classes.
		 */
		public int getNumberOfAbstractInnerClasses()
		{
			int answer = 0;
			InterfaceDefn intf = null;
				
			for (int i=0; i<project.getInterfaceVec().size(); i++)
			{
				intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
				if ((intf != null) && intf.isAbstract() && intf.isClass() && intf.isInner())
					answer++;
			}
				
			return answer;
		}
		
		/**
		 * Get the number of Interface objects that are abstract, outer, and 
		 * are classes.
		 */
		public int getNumberOfAbstractOuterClasses()
		{
			int answer = 0;
			InterfaceDefn intf = null;
				
			for (int i=0; i<project.getInterfaceVec().size(); i++)
			{
				intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
				if ((intf != null) && intf.isAbstract() && intf.isClass() && intf.isOuter())
					answer++;
			}
				
			return answer;
		}
		
		//----------------Concrete---------------------------------------------------------------------		
		/**
		 * Get the number of Interface objects that are concrete, inner, and 
		 * are classes.
		 */
		public int getNumberOfConcreteInnerClasses()
		{
			int answer = 0;
			InterfaceDefn intf = null;
				
			for (int i=0; i<project.getInterfaceVec().size(); i++)
			{
				intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
				if ((intf != null) && intf.isConcrete() && intf.isClass() && intf.isInner())
					answer++;
			}
				
			return answer;
		}

		/**
		 * Get the number of Interface objects that are concrete, outer, and 
		 * are classes.
		 */
		public int getNumberOfConcreteOuterClasses()
		{
			int answer = 0;
			InterfaceDefn intf = null;
				
			for (int i=0; i<project.getInterfaceVec().size(); i++)
			{
				intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
				if ((intf != null) && intf.isConcrete() && intf.isClass() && intf.isOuter())
					answer++;
			}
				
			return answer;
		}
	
	//--------------------Methods to obtain interface data----------------------------------
		//----------------Abstract---------------------------------------------------------------------
		/**
		* Get the number of Interface objects that are abstract, inner, and 
		* are interfaces.
		*/
		public int getNumberOfAbstractInnerInterfaces()
		{
			int answer = 0;
			InterfaceDefn intf = null;
				
			for (int i=0; i<project.getInterfaceVec().size(); i++)
			{
				intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
				if ((intf != null) && intf.isAbstract() && intf.isInterface() && intf.isInner())
					answer++;
			}
				
			return answer;
		}
		
		/**
		* Get the number of Interface objects that are abstract, outer, and 
		* are interfaces.
		*/
		public int getNumberOfAbstractOuterInterfaces()
		{
			int answer = 0;
			InterfaceDefn intf = null;
				
			for (int i=0; i<project.getInterfaceVec().size(); i++)
			{
				intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
				if ((intf != null) && intf.isAbstract() && intf.isInterface() && intf.isOuter())
					answer++;
			}
				
			return answer;
		}
			
		//----------------Concrete---------------------------------------------------------------------		
		/**
		* Get the number of Interface objects that are concrete, inner, and 
		* are interfaces.
		*/
		public int getNumberOfConcreteInnerInterfaces()
		{
			int answer = 0;
			InterfaceDefn intf = null;
				
			for (int i=0; i<project.getInterfaceVec().size(); i++)
			{
				intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
				if ((intf != null) && intf.isConcrete() && intf.isInterface() && intf.isInner())
					answer++;
			}
				
			return answer;
		}
		/**
		* Get the number of Interface objects that are concrete, outer, and 
		* are interfaces.
		*/
		public int getNumberOfConcreteOuterInterfaces()
		{
			int answer = 0;
			InterfaceDefn intf = null;
				
			for (int i=0; i<project.getInterfaceVec().size(); i++)
			{
				intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
				if ((intf != null) && intf.isConcrete() && intf.isInterface() && intf.isOuter())
					answer++;
			}
				
			return answer;
		}
	
	//----------------------Get totals------------------------------------------------------------------
		//------------------For classes---------------------------------------------------------------
			/**
			* Get the number of Interface objects that are abstract and represent classes.
			*/
			public int getTotalNumberOfAbstractClasses()
			{
				return (getNumberOfAbstractInnerClasses()+getNumberOfAbstractOuterClasses());
			}
			/**
			* Get the number of Interface objects that are concrete and represent classes.
			*/			
			public int getTotalNumberOfConcreteClasses()
			{
				return (getNumberOfConcreteInnerClasses()+getNumberOfConcreteOuterClasses());
			}
			
			/**
			* Get the number of Interface objects that are inner and represent classes.
			*/
			public int getTotalNumberOfInnerClasses()
			{
				return (getNumberOfAbstractInnerClasses()+getNumberOfConcreteInnerClasses());
			}
			
			/**
			* Get the number of Interface objects that are outer and represent classes.
			*/
			public int getTotalNumberOfOuterClasses()
			{
				return (getNumberOfAbstractOuterClasses()+getNumberOfConcreteOuterClasses());
			}
			
			/**
			* Get the number of Interface objects that represent classes.
			*/
			public int getTotalNumberOfClasses()
			{
				int answer = 0;
				InterfaceDefn intf = null;
				
				for (int i=0; i<project.getInterfaceVec().size(); i++)
				{
					intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
					if ((intf != null) && intf.isClass())
						answer++;
				}
				
				return answer;
			}
		//------------------For interfaces---------------------------------------------------------------
			/**
			* Get the number of Interface objects that are abstract and represent interfaces.
			*/
			public int getTotalNumberOfAbstractInterfaces()
			{
				return (getNumberOfAbstractInnerInterfaces()+getNumberOfAbstractOuterInterfaces());
			}

			/**
			* Get the number of Interface objects that are concrete and represent interfaces.
			*/
			public int getTotalNumberOfConcreteInterfaces()
			{
				return (getNumberOfConcreteInnerInterfaces()+getNumberOfConcreteOuterInterfaces());
			}

			/**
			* Get the number of Interface objects that are inner and represent interfaces.
			*/
			public int getTotalNumberOfInnerInterfaces()
			{
				return (getNumberOfAbstractInnerInterfaces()+getNumberOfConcreteInnerInterfaces());
			}
			
			/**
			* Get the number of Interface objects that are outer and represent interfaces.
			*/
			public int getTotalNumberOfOuterInterfaces()
			{
				return (getNumberOfAbstractOuterInterfaces()+getNumberOfConcreteOuterInterfaces());
			}

			/**
			* Get the number of Interface objects that represent interfaces.
			*/			
			public int getTotalNumberOfInterfaces()
			{
				int answer = 0;
				InterfaceDefn intf = null;
			
				for (int i=0; i<project.getInterfaceVec().size(); i++)
				{
					intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
					if ((intf != null) && intf.isInterface())
						answer++;
				}
			
				return answer;
			}
	
	//------------Get Grand Totals------------------------------------------------------------
	/**
	* Get the number of Interface objects that are outer.
	*/
	public int getTotalNumberOfOuterClassesAndInterfaces()
	{
		int answer = 0;
		InterfaceDefn intf = null;
			
		for (int i=0; i<project.getInterfaceVec().size(); i++)
		{
			intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
			if ((intf != null) && intf.isOuter())
				answer++;
		}
		
		return answer;
	}
	
	/**
	* Get the number of Interface objects that are inner.
	*/
	public int getTotalNumberOfInnerClassesAndInterfaces()
	{
		int answer = 0;
		InterfaceDefn intf = null;
			
		for (int i=0; i<project.getInterfaceVec().size(); i++)
		{
			intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
			if ((intf != null) && intf.isInner())
				answer++;
		}
		
		return answer;
	}
	
	/**
	* Get the number of Interface objects that are abstract.
	*/
	public int getTotalNumberOfAbstractClassesAndInterfaces()
	{
		int answer = 0;
		InterfaceDefn intf = null;
			
		for (int i=0; i<project.getInterfaceVec().size(); i++)
		{
			intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
			if ((intf != null) && intf.isAbstract())
				answer++;
		}
		
		return answer;
	}
	
	/**
	* Get the number of Interface objects that are concrete.
	*/
	public int getTotalNumberOfConcreteClassesAndInterfaces()
	{
		int answer = 0;
		InterfaceDefn intf = null;
			
		for (int i=0; i<project.getInterfaceVec().size(); i++)
		{
			intf = ((Interface)(project.getInterfaceVec().elementAt(i))).getPgmDefn();
			if ((intf != null) && intf.isConcrete())
				answer++;
		}
		
		return answer;
	}
	
	/**
	* Get the number of Interface objects.
	*/
	public int getTotalNumberOfClassesAndInterfaces()
	{
			return project.getInterfaceVec().size();
	}
}
