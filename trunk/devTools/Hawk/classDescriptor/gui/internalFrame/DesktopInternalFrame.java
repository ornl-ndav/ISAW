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
 * Revision 1.2  2004/03/11 18:34:33  bouzekc
 * Documented file using javadoc statements.
 * Modified the constructor such that a window menu is created and its actions are
 * handled in this class.
 * Removed the use of the a MenuListener to add JMenuItems to the window menu if
 * more tabs are created in the HawkDesktop "desktop" to allow the user to select
 * which tab to move/copy the window.
 * Added the MoveToGUI and CopyToGUI inner classes used for allowing the user to
 * select what tab (in a HawkDesktop) they want the window to be moved/copied to.
 * Added the resizeAndRelocate() method which works better than pack() for making
 * the window fit the screen.
 *
 * Revision 1.1  2004/02/07 05:09:15  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.internalFrame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;

/**
 * This is a special type of JInternalFrame that is placed in a HawkDesktop object.  It handles 
 * JMenus that allows the user to copy of move the window to another tab.  The HawkDesktop 
 * associated with this class has a JTabbedPane.  Each tab in that JTabbedPane has a 
 * JDesktopPane that contains DesktopInternalFrames.  These are the tabs that the windows 
 * can be copied or moved to.
 * @author Dominic Kramer
 */
public abstract class DesktopInternalFrame extends JInternalFrame implements ActionListener
{
	/**
	 * The item in menu that allows the user to move the window to the previous tab.
	 */
	protected JMenuItem moveToPreviousTabItem;
	/**
	 * The item in menu that allows the user to copy the window to the previous tab.
	 */
	protected JMenuItem copyToPreviousTabItem;
	/**
	 * The HawkDesktop window associated with this window.
	 */
	protected HawkDesktop desktop;
	/**
	 * The menu that contains the moveToMenu and copyToMenu.
	 */
	protected JMenu windowMenu;
	/**
	 * The menubar that contains the windowMenu.
	 */
	protected JMenuBar menuBar;
	/**
	 * The menu that contains the items to move the window from tab to tab.
	 */
	protected JMenu moveToMenu;
	/**
	 * The menu that contains the items to copy the window from tab to tab.
	 */
	protected JMenu copyToMenu;
	
	public DesktopInternalFrame(HawkDesktop desk)
	{
		desktop = desk;
		windowMenu = new JMenu("Window");
		windowMenu.addMenuListener(new WindowMenuListener(this,menuBar,windowMenu));
		moveToMenu = new JMenu("Move To");
			JMenuItem nextTabItem = new JMenuItem("Next Tab");
			nextTabItem.addActionListener(this);
			nextTabItem.setActionCommand("nextTab.moveTo");
			moveToMenu.add(nextTabItem);
	
			moveToPreviousTabItem = new JMenuItem("Previous Tab");
			moveToPreviousTabItem.addActionListener(this);
			moveToPreviousTabItem.setActionCommand("previousTab.moveTo");
			moveToMenu.add(moveToPreviousTabItem);
			if (desktop.getTabbedPane().getSelectedIndex() == 0)
				moveToPreviousTabItem.setEnabled(false);
							
			JMenuItem newTabItem = new JMenuItem("New Tab");
			newTabItem.addActionListener(this);
			newTabItem.setActionCommand("newTab.moveTo");
			moveToMenu.add(newTabItem);
							
			moveToMenu.add(new JSeparator());
			JMenuItem otherItem = new JMenuItem("Other");
			otherItem.setActionCommand("other.moveTo");
			otherItem.addActionListener(this);
			moveToMenu.add(otherItem);
				
			windowMenu.add(moveToMenu);

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
			JMenuItem otherCopyItem = new JMenuItem("Other");
			otherCopyItem.setActionCommand("other.copyTo");
			otherCopyItem.addActionListener(this);
			copyToMenu.add(otherCopyItem);
			windowMenu.add(copyToMenu);
	}
		
	public void resizeAndRelocate()
	{
		int count = desktop.getTabbedPane().getTabCount();
		System.out.println("count="+count);
		if (count > 0)
		{
			int maxWidth = ((JDesktopPane)desktop.getTabbedPane().getComponentAt(0)).getWidth();
			int maxHeight = ((JDesktopPane)desktop.getTabbedPane().getComponentAt(0)).getHeight();
			
			setLocation((int)(0.1*maxWidth),(int)(0.1*maxHeight));
			setSize((int)(0.8*maxWidth),(int)(0.8*maxHeight));
			
			System.out.println("maxWidth="+maxWidth);
			System.out.println("maxHeight="+maxHeight);
			
			System.out.println("getX()="+getX());
			System.out.println("getY()="+getY());
			System.out.println("getWidth()="+getWidth());
			System.out.println("getHeight()="+getHeight());
			int newX = getX();
			int newY = getY();
			int newWidth = getWidth();
			int newHeight = getHeight();
			
			if (getX()+getWidth() > maxWidth)
			{
				newWidth = (int)(0.8*maxWidth);
				newX = (int)(0.1*maxWidth);
			}
			if (getY()+getHeight() > maxHeight)
			{
				newHeight = (int)(0.8*maxHeight);
				newY = (int)(0.1*maxHeight);
			}
			System.out.println("newX="+newX);
			System.out.println("newY="+newY);
			System.out.println("newWidth="+newWidth);
			System.out.println("newHeight="+newHeight);
			
			setLocation(newX,newY);
			setSize(newWidth,newHeight);
		}
	}
	
	/**
	 * If bol is true the method moves the selected internal window in front of the other windows 
	 * on the JDesktop.  If bol is false it moves the window behind the other windows on the JDesktopPane.
	 * @param bol True to move the window to the front and false to move the window to the back.
	 */
	public void setAsSelected(boolean bol)
	{
		if (bol)
			moveToFront();
		else
			moveToBack();
	}
	
	/**
	 * Gets a copy of this window.
	 * @return A copy of this window.
	 */
	public abstract DesktopInternalFrame getCopy();
	
	/**
	 * Gets this windows HawkDesktop it is on.
	 * @return The HawkDesktop.
	 */
	public HawkDesktop getHawkDesktop()
	{
		return desktop;
	}
	
	/**
	 * This sets the menu item that allows the user to move or copy the window 
	 * to the previous tab in  the HawkDesktop's JTabbedPane enabled.  When it 
	 * is enabled the user can select it.  When it is not enabled the user cannot select 
	 * it.
	 * @param bol Whether or not to enable the menu item.
	 */	
	public void setPreviousMenuItemEnabled(boolean bol)
	{
		moveToPreviousTabItem.setEnabled(bol);
		copyToPreviousTabItem.setEnabled(bol);
	}

	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
		System.out.println("In DesktopInternalFrame event.getActionCommand()="+event.getActionCommand());
		
		if (event.getActionCommand().endsWith(".copyTo"))
		{
			StringTokenizer tokenizer = new StringTokenizer(event.getActionCommand(), ".");
			String str = tokenizer.nextToken();
			int total = desktop.getTabbedPane().getTabCount();
			int sel = desktop.getTabbedPane().getSelectedIndex();
			if (str.equals("nextTab"))
			{
				if (sel == total-1) //then we have to make a new tab
				{
					desktop.getTabbedPane().add(new JDesktopPane());
					total++;
					desktop.getTabbedPane().setTitleAt(total-1,"Tab"+total);
				}
				desktop.setSelectedDesktop(sel+1);
				DesktopInternalFrame frame = this.getCopy();
				desktop.getSelectedDesktop().add(frame);
				frame.setAsSelected(true);
				frame.setVisible(true);
			}
			else if (str.equals("previousTab"))
			{
				desktop.setSelectedDesktop(sel-1);
				DesktopInternalFrame frame = this.getCopy();
				desktop.getSelectedDesktop().add(frame);
				frame.setAsSelected(true);
				frame.setVisible(true);
			}
			else if (str.equals("newTab"))
			{
				desktop.getTabbedPane().add(new JDesktopPane());
				total++;
				desktop.getTabbedPane().setTitleAt(total-1,"Tab"+total);
				desktop.setSelectedDesktop(total-1);
				DesktopInternalFrame frame = this.getCopy();
				desktop.getSelectedDesktop().add(frame);
				frame.setAsSelected(true);
				frame.setVisible(true);
			}
			else if (str.equals("other"))
			{
				CopyToGUI gui = new CopyToGUI(desktop,this);//(DesktopInternalFrame)desktop.getSelectedDesktop().getSelectedFrame());
				gui.setVisible(true);
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
				desktop.getSelectedDesktop().add(this);
				this.setAsSelected(true);
				this.setVisible(true);
			}
			else if (str.equals("previousTab"))
			{
				desktop.setSelectedDesktop(sel-1);
				desktop.getSelectedDesktop().add(this);
				this.setAsSelected(true);
				this.setVisible(true);
			}
			else if (str.equals("newTab"))
			{
				desktop.getTabbedPane().add(new JDesktopPane());
				total++;
				desktop.getTabbedPane().setTitleAt(total-1,"Tab"+total);
				desktop.setSelectedDesktop(total-1);
				desktop.getSelectedDesktop().add(this);
				this.setAsSelected(true);
				this.setVisible(true);
			}
			else if (str.equals("other"))
			{
				MoveToGUI gui = new MoveToGUI(desktop,this);//(DesktopInternalFrame)desktop.getSelectedDesktop().getSelectedFrame());
				gui.setVisible(true);
			}
			else
			{
				System.out.println("str (processed in else statement)="+str);
			}
		}
	}
	
	class MoveToGUI extends JFrame implements ActionListener
	{
		protected HawkDesktop hDesk;
		protected DesktopInternalFrame frame;
		protected Vector checkBoxVec;
		protected JLabel label;
		protected JPanel mainPanel;
		
		public MoveToGUI(HawkDesktop d, DesktopInternalFrame fr)
		{
			setTitle("Moving the Window");
			
			hDesk = d;
			frame = fr;
			
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			String[] tabNameArr = hDesk.getTabNames();
			JPanel checkBoxPanel = new JPanel();
			checkBoxPanel.setLayout(new GridLayout(tabNameArr.length,0));
			checkBoxVec = new Vector();
			for (int i=0; i<tabNameArr.length; i++)
			{
				JCheckBox checkBox = new JCheckBox(tabNameArr[i]);
				checkBox.setActionCommand(""+i);
				checkBox.addActionListener(this);
				if (i == hDesk.getTabbedPane().getSelectedIndex())
					checkBox.setEnabled(false);
				checkBoxVec.add(checkBox);
				checkBoxPanel.add(checkBox);
			}
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(this);
				buttonPanel.add(cancelButton);
			JButton okButton = new JButton("Ok");
				okButton.addActionListener(this);
				buttonPanel.add(okButton);
			
			label = new JLabel("Select the tab that you want to move the window to.");
			mainPanel.add(label, BorderLayout.NORTH);
			mainPanel.add(checkBoxPanel, BorderLayout.CENTER);
			mainPanel.add(buttonPanel, BorderLayout.SOUTH);
			getContentPane().add(mainPanel);
			this.pack();
		}
		
		public void actionPerformed(ActionEvent event)
		{
			System.out.println("In MoveToGUI:  event.getActionCommand()="+event.getActionCommand());
			
			if (event.getActionCommand().equals("Ok"))
			{
				int i=0;
				boolean found = false;
				while (i<checkBoxVec.size() && !found)
				{
					if (((JCheckBox)checkBoxVec.elementAt(i)).isSelected())
					{
						((JDesktopPane)hDesk.getTabbedPane().getComponentAt(i)).add(frame);
						frame.setVisible(true);
						frame.setAsSelected(true);
						hDesk.getTabbedPane().setSelectedIndex(i);
						found = true;
					}
					i++; 
				}
				
				dispose();
			}
			else if (event.getActionCommand().equals("Cancel"))
			{
				dispose();
			}
			else
			{
				String num = "";
				for (int i=0; i<checkBoxVec.size(); i++)
				{
					num = "" + i;
					if (!num.equals(event.getActionCommand()))
						((JCheckBox)checkBoxVec.elementAt(i)).setSelected(false);
				}
			}
		}
	}
	
	class CopyToGUI extends MoveToGUI
	{
		public CopyToGUI(HawkDesktop d, DesktopInternalFrame fr)
		{
			super(d,fr);
			setTitle("Copying the Window");
			mainPanel.remove(label);
			((JCheckBox)checkBoxVec.elementAt(hDesk.getTabbedPane().getSelectedIndex())).setEnabled(true);
			label = new JLabel("Select the tab(s) to copy the window to.");
			mainPanel.add(label,BorderLayout.NORTH);
		}
		
		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("Ok"))
			{
				for (int i=0; i<checkBoxVec.size(); i++)
				{
					if (((JCheckBox)checkBoxVec.elementAt(i)).isSelected())
					{
						DesktopInternalFrame copy = frame.getCopy();
						((JDesktopPane)hDesk.getTabbedPane().getComponentAt(i)).add(copy);
						copy.setVisible(true);
						copy.setAsSelected(true);
						hDesk.getTabbedPane().setSelectedIndex(i);
					}
				}
				
				dispose();
			}
			else if (event.getActionCommand().equals("Cancel"))
			{
				dispose();
			}
		}
	}
}

/*
	
/**
 * This refreshes the menu on the window that allows the user to copy the window 
 * to another tab.  It adds items to the menu for each tab in the HawkDesktop object's 
 * JTabbedPane.
 /
public void refreshCopyMenu()
{
	String[] nameArr = desktop.getTabNames();

	System.out.println("nameArr.length="+nameArr.length);

	copyToMenu = new JMenu("Copy To");
}
	
/**
 * This refreshes the menu on the window that allows the user to move the window 
 * to another tab.  It also refreshes the menu on the window that allows the user to 
 * copy the window to andother tab.  It adds items to the menu for each tab in the 
 * HawkDesktop object's JTabbedPane.
 /
public void refreshMoveAndCopyMenu()
{
	windowMenu = new JMenu("Window");
	refreshMoveMenu();
	refreshCopyMenu();
}
	
/**
 * This handles the ActionEvents that are thrown when the user selects a item from 
 * the copy menu or move menu.
 * @param event The ActionEvent that is caught.
 * @param copy The copy of the DesktopInternalFrame to possibly put in another tab.
 * @param thisFrame The original DesktopInternalFrame.
 /
public void processWindowChange(ActionEvent event,DesktopInternalFrame copy, DesktopInternalFrame thisFrame)
{

}

/**
 * This refreshes the menu on the window that allows the user to move the window 
 * to another tab.  It adds items to the menu for each tab in the HawkDesktop object's 
 * JTabbedPane.
 /
public void refreshMoveMenu()
{
	String[] nameArr = desktop.getTabNames();
					
	moveToMenu = new JMenu("Move To");
}

*/