/*
 * File:  DesktopInternalFrame.java
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
 * Revision 1.1  2004/02/07 05:09:15  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.internalFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class DesktopInternalFrame extends JInternalFrame implements ActionListener
{
	protected JMenuItem previousTabItem;
	protected JMenuItem copyToPreviousTabItem;
	protected HawkDesktop desktop;
	protected JMenu windowMenu;
	protected JMenuBar menuBar;
	protected JMenu moveToMenu;
	protected JMenu copyToMenu;
	
	public DesktopInternalFrame(HawkDesktop desk)
	{
		desktop = desk;
		windowMenu = new JMenu("Window");
		moveToMenu = new JMenu("Move To");
		copyToMenu = new JMenu("Copy To");
	}
	
	public abstract DesktopInternalFrame getCopy();
	
	public void refreshMoveMenu()
	{
		String[] nameArr = desktop.getTabNames();
					
		moveToMenu = new JMenu("Move To");
			moveToMenu.addMenuListener(new WindowMenuListener(this,menuBar,windowMenu));
			JMenuItem nextTabItem = new JMenuItem("Next Tab");
			nextTabItem.addActionListener(this);
			nextTabItem.setActionCommand("nextTab.moveTo");
			moveToMenu.add(nextTabItem);

			previousTabItem = new JMenuItem("Previous Tab");
			previousTabItem.addActionListener(this);
			previousTabItem.setActionCommand("previousTab.moveTo");
			moveToMenu.add(previousTabItem);
			if (desktop.getTabbedPane().getSelectedIndex() == 0)
				previousTabItem.setEnabled(false);
						
			JMenuItem newTabItem = new JMenuItem("New Tab");
			newTabItem.addActionListener(this);
			newTabItem.setActionCommand("newTab.moveTo");
			moveToMenu.add(newTabItem);
						
			moveToMenu.add(new JSeparator());
				for (int i=0; i<nameArr.length; i++)
				{
					JMenuItem newItem = new JMenuItem(nameArr[i]);
					newItem.addActionListener(this);
					newItem.setActionCommand(nameArr[i]+".moveTo");
					moveToMenu.add(newItem); 
				}
				
			windowMenu.add(moveToMenu);
	}
	
	public HawkDesktop getHawkDesktop()
	{
		return desktop;
	}
	
	public void setPreviousMenuItemEnabled(boolean bol)
	{
		previousTabItem.setEnabled(bol);
		copyToPreviousTabItem.setEnabled(bol);
	}

	public void refreshCopyMenu()
	{
		String[] nameArr = desktop.getTabNames();

		System.out.println("nameArr.length="+nameArr.length);

		copyToMenu = new JMenu("Copy To");
			copyToMenu.addMenuListener(new WindowMenuListener(this,menuBar,windowMenu));
			JMenuItem copyToNextTabItem = new JMenuItem("Next Tab");
			copyToNextTabItem.addActionListener(this);
			copyToNextTabItem.setActionCommand("nextTab.copyTo");
			copyToMenu.add(copyToNextTabItem);
				
			copyToPreviousTabItem = new JMenuItem("Previous Tab");
			copyToPreviousTabItem.addActionListener(this);
			copyToPreviousTabItem.setActionCommand("previousTab.copyTo");
			copyToMenu.add(copyToPreviousTabItem);
			if (desktop.getTabbedPane().getSelectedIndex() == 0)
				copyToPreviousTabItem.setEnabled(false);
								
			JMenuItem copyToNewTabItem = new JMenuItem("New Tab");
			copyToNewTabItem.addActionListener(this);
			copyToNewTabItem.setActionCommand("newTab.copyTo");
			copyToMenu.add(copyToNewTabItem);

			copyToMenu.add(new JSeparator());
				for (int i=0; i<nameArr.length; i++)
				{
					JMenuItem newItem = new JMenuItem(nameArr[i]);
					System.out.println("Adding "+nameArr[i]);
					newItem.addActionListener(this);
					newItem.setActionCommand(nameArr[i]+".copyTo");
					copyToMenu.add(newItem); 
				}
			windowMenu.add(copyToMenu);
	}
	
	public void refreshMoveAndCopyMenu()
	{
		windowMenu = new JMenu("Window");
		refreshMoveMenu();
		refreshCopyMenu();
	}
	
	public void processWindowChange(ActionEvent event,DesktopInternalFrame copy, DesktopInternalFrame thisFrame)
	{
		if (event.getActionCommand().endsWith(".copyTo"))
		{
			StringTokenizer tokenizer = new StringTokenizer(event.getActionCommand(), ".");
			String str = tokenizer.nextToken();
			int total = desktop.getTabbedPane().getTabCount();
			int sel = desktop.getTabbedPane().getSelectedIndex();
			copy.setVisible(true);
			if (str.equals("nextTab"))
			{
				if (sel == total-1) //then we have to make a new tab
				{
					desktop.getTabbedPane().add(new JDesktopPane());
					total++;
					desktop.getTabbedPane().setTitleAt(total-1,"Tab"+total);
				}
				desktop.setSelectedDesktop(sel+1);
				desktop.getSelectedDesktop().add(copy);
			}
			else if (str.equals("previousTab"))
			{
				desktop.setSelectedDesktop(sel-1);
				desktop.getSelectedDesktop().add(copy);
			}
			else if (str.equals("newTab"))
			{
				desktop.getTabbedPane().add(new JDesktopPane());
				total++;
				desktop.getTabbedPane().setTitleAt(total-1,"Tab"+total);
				desktop.setSelectedDesktop(total-1);
				desktop.getSelectedDesktop().add(copy);
			}
			else
			{
				System.out.println("str (processed in else statement)="+str);
			}
		}
		else if (event.getActionCommand().endsWith(".moveTo"))
		{
			StringTokenizer tokenizer = new StringTokenizer(event.getActionCommand(), ".");
			String str = tokenizer.nextToken();
			int total = desktop.getTabbedPane().getTabCount();
			int sel = desktop.getTabbedPane().getSelectedIndex();
			System.out.println("str="+str);
			if (str.equals("nextTab"))
			{
				if (sel == total-1) //then we have to make a new tab
				{
					desktop.getTabbedPane().add(new JDesktopPane());
					total++;
					desktop.getTabbedPane().setTitleAt(total-1,"Tab"+total);
				}
				desktop.setSelectedDesktop(sel+1);
				desktop.getSelectedDesktop().add(thisFrame);
			}
			else if (str.equals("previousTab"))
			{
				desktop.setSelectedDesktop(sel-1);
				desktop.getSelectedDesktop().add(thisFrame);
			}
			else if (str.equals("newTab"))
			{
				desktop.getTabbedPane().add(new JDesktopPane());
				total++;
				desktop.getTabbedPane().setTitleAt(total-1,"Tab"+total);
				desktop.setSelectedDesktop(total-1);
				desktop.getSelectedDesktop().add(thisFrame);
			}
			else
			{
				System.out.println("str (processed in else statement)="+str);
			}
		}
	}

	public void actionPerformed(ActionEvent event)
	{

	}
}
