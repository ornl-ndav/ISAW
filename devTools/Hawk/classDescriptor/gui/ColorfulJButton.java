/*
 * File:  ColorfulJButton.java
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JButton;

/**
 * @author Dominic Kramer
 */
public class ColorfulJButton extends JButton
{
	private Color color;
    
	public ColorfulJButton(Color col)
	{
	  super(" ");
	  color = col;
	  //setSize(new Dimension(10,10));
	}
    
	public void setColor(Color col)
	{
	  color = col;
	}
	
	public Color getColor()
	{
		return color;
	}
    
	protected void paintComponent(Graphics g)
	{
		System.out.println("  in  PAINT COMPONENT color="+color);
		super.paintComponent(g);
		Dimension dim = getSize();
		//g.setColor(Color.BLACK);
		//g.drawRect((int)(0.1*dim.getWidth())-1,(int)(0.1*dim.getHeight())-1,(int)(0.8*dim.getWidth()),(int)(0.8*dim.getHeight()));
		g.setColor(color);
		g.fillRect((int)(0.1*dim.getWidth()),(int)(0.1*dim.getHeight()),(int)(0.8*dim.getWidth()),(int)(0.8*dim.getHeight()));
	}
}
