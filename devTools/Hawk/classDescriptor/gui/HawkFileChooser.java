/*
 * File:  HawkFileChooser.java
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
 */
package devTools.Hawk.classDescriptor.gui;

import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;

import devTools.Hawk.classDescriptor.tools.SystemsManager;

public class HawkFileChooser extends JFileChooser
{
	/**
	 * If f is a Hawk file a special icon will be returned to make the 
	 * file stand out.  Otherwise, the default action of JFileChooser's 
	 * getIcon(File f) is used.
	 * @param f The file in question.
	 * @return An icon to represent the file.
	 */
	public Icon getIcon(File f)
	{
		Icon icon = null;
		if (SystemsManager.isAHawkNativeFile(f))
		{
			icon = SystemsManager.getImageIconOrBlankIcon("hawk_filechooser_icon.png");
			if (icon == null)
				icon = super.getIcon(f);
		}
		else
			return super.getIcon(f);
			
		return icon;
	}
}
