/*
 * File:  FileAlreadyExistsGUI.java
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
 * Revision 1.2  2004/03/11 18:35:48  bouzekc
 * Documented file using javadoc statements.
 *
 * Revision 1.1  2004/02/07 05:08:49  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This window asks the user if he/she wants to overwrite, append, or cancel writing a file if 
 * the user selects to write to a file which already exists.
 * @author Dominic  Kramer
 */
public class FileAlreadyExistsGUI extends JFrame
{
	/**
	 * Default constructor.
	 */
	public FileAlreadyExistsGUI()
	{}
	
	/**
	 * This method displays the window and returns the answer if the user wants to 
	 * overwrite, append, or cancel writing the file.
	 * @param fileName The full filename for the file to be written to.
	 * @return Either "Overwrite", "Append", or "Cancel"
	 */
	public String showQuestionDialog(String fileName)
	{
		String[] optionsArr = {"Overwrite","Append","Cancel"};
		JOptionPane optionPane = new JOptionPane();
		optionPane.setMessage("The file "+fileName+" already exists.");
		optionPane.setOptions(optionsArr);
		JDialog dialog = optionPane.createDialog(this,"Question");
		dialog.show();
		Object result = optionPane.getValue();
		
		return (String)result;
	}
}
