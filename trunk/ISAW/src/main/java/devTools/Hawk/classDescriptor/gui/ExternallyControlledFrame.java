/*
 * File:  ExternallyControlledFrame.java
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

import java.awt.Component;
import java.awt.Dimension;

/**
 * JFrame and JInternalFrame are meant to implement this interface if they want to add a from the 
 * devTools.Hawk.classDescriptor.gui.panel package to the frame.  Then the panel can handle closing 
 * the frame, changing the frame's title, and accessing the size of the frame.  Then the frame does
 * not have to handle any actions caused by the user selecting menu items from the panel's menu or 
 * buttons from the panel etc.
 * @author Dominic Kramer
 */
public interface ExternallyControlledFrame
{
	/** Handles disposing the frame. */
	public void dispose();
	/** Handles setting the frame's title. */
	public void setTitle(String title);
	/** Handles getting the frame's size. */
	public Dimension getSize();
	/** Get the Component this object is controlling or null 
	 * if it isn't controlling a Component.
	 */
	public Component getControlledComponent();
}
