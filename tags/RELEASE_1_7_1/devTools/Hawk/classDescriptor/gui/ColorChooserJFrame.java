/*
 * File:  ColorChooserJFrame.java
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * @author Dominic Kramer
 */
public class ColorChooserJFrame implements ActionListener
{
	protected ColorfulJButton colorfulButton;
	protected JDialog colorDialog;
	protected JColorChooser colorChooser;
	protected JFrame colorFrame;
	
	public ColorChooserJFrame(String title, Color initialColor, ColorfulJButton colButton)
	{
		colorfulButton = colButton;
		
		colorFrame = new JFrame();
		colorChooser = new JColorChooser(initialColor);
		colorDialog = JColorChooser.createDialog(colorFrame,title,false,colorChooser,this,this);
	}
	
	public void setVisible(boolean bol)
	{
		colorDialog.setVisible(bol);
	}
	
	public ColorfulJButton getColorulJButton()
	{
		return colorfulButton;
	}
	
	public Color getCurrentColor()
	{
		return colorChooser.getColor();
	}
	
	public void setCurrentColor(Color col)
	{
		colorChooser.setColor(col);
	}
	
	public void actionPerformed(ActionEvent event)
	{
		System.out.println("---->"+event.getActionCommand());
		if (event.getActionCommand().equals("OK"))
		{
			System.out.println("    colorChooser.getColor()="+colorChooser.getColor());
			colorfulButton.setColor(colorChooser.getColor());
			System.out.println("     Done");
		}
		
		//colorFrame.dispose();
	}
}
