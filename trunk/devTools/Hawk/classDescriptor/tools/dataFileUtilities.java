/*
 * File:  dataFileUtilities.java
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
 * Revision 1.3  2004/03/12 19:46:21  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:10:48  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.tools;


/**
 * This class contains the methods used by other classes to read and write native Hawk files.  An object 
 * of this type basically just holds information about how a project is supposed to be saved.
 * @author Dominic Kramer
 */
public class dataFileUtilities
{
		/**
		 * The filename of the native Hawk file.
		 */
		protected String fileName;
		/**
		 * This is set to true if data written to the file should be appended to the end of the 
		 * file if the file already exists.
		 */
		protected boolean append;
		/**
		 * This is set to true if the project that this object is associated with has been saved 
		 * to a native Hawk file.
		 */
		protected boolean alreadySaved;
		
		/**
		* Create a new default dataFileUtilities object without any file associated with it.
		*/
		public dataFileUtilities()
		{
			fileName = "";
			append = false;
		}
		
		/**
		* This makes a dataFileUtilities object with the native Hawk file, file, associated 
		* with it.
		* @param file The filename of the native Hawk file.
		* @param ap True if you want to append to the file and false if you want to 
		* overwrite the file.
		*/
		public dataFileUtilities(String file, boolean ap)
		{
			fileName = file;
			append = ap;
		}
		
		/**
		 * Get the native Hawk file's filename associated with this object.
		 * @return The filename assocaited with this object.
		 */
		public String getFileName()
		{
			return fileName;
		}
		
		/**
		 * Set the Hawk native filename associated with this object.
		 * @param name The filename
		 */
		public void setFileName(String name)
		{
			fileName = name;
		}
		
		/**
		 * Get the answer of whether or not to append to the file.
		 * @return True if the data should be appended to the file.
		 */
		public boolean append()
		{
			return append;
		}
		
		/**
		 * Set whether or not to append data to the end of the file or overwrite the data.
		 * @param bol True if data is to be appended to the end of the file and false if the 
		 * file is to be overwritten.
		 */
		public void setAppend(boolean bol)
		{
			append = bol;
		}
		
		/**
		 * Determine if the project associated with this object has been saved to a native Hawk file.
		 */
		public boolean isAlreadySaved()
		{
			return alreadySaved;
		}
		
		/**
		 * Set if the project associated with this object has been saved to a native Hawk file.
		 */
		public void setAlreadySaved(boolean bol)
		{
			alreadySaved = bol;
		}
}
