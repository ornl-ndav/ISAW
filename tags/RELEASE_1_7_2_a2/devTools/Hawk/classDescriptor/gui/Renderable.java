/*
 * File:  Renderable.java
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
 */
package devTools.Hawk.classDescriptor.gui;

import java.awt.Font;

import javax.swing.ImageIcon;

/**
 * A renderer (for example for a JList or JTree) can more easily get information 
 * used to render the items in the list or tree if the items in added to the list or 
 * tree implements this interface.
 * @author Dominic Kramer
 */
public interface Renderable
{
	/** Get the image that is to be displayed in the GUI being rendered. */
	public ImageIcon getIcon();
	/** Get the text's font on the GUI being rendered. */
	public Font getFont();
	/** Get the tool tip text for the item on the GUI being rendered. */
	public String getToolTipText();
	/**
	 * Get the text the item is supposed to have.
	 * @param shortJava True if Java terms are to be shortened.
	 * @param shortOther True if non-Java terms are to be shortened.
	 */
	public String getText(boolean shortJava, boolean shortOther);
}
