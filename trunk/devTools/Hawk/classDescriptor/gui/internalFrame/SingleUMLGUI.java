/*
 * File:  SingleUMLGUI.java
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
 * Revision 1.1  2004/02/07 05:09:16  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.internalFrame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.ASCIIPrintFileManager;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/*
 * Created on Nov 18, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SingleUMLGUI extends DesktopInternalFrame implements ActionListener
{
	protected JTextArea textArea;
	protected Interface selectedInterface;
	
	protected JCheckBox shortJavaCheckBox;
	protected JCheckBox shortOtherCheckBox;
	
	public SingleUMLGUI(Interface INTF, String title, boolean shortJava, boolean shortOther, HawkDesktop desk)
	{
		super(desk);
		
		//now to set selectedInterface
			selectedInterface = INTF;
			
		//now to set some of the characteristics of the window
			setTitle(title);
			setLocation(0,0);
			setSize(175,400);
			setClosable(true);
			setIconifiable(true);
			setMaximizable(true);
			setResizable(true);
			
		Container pane = getContentPane();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		Font monoSpaced = new Font("Monospaced", Font.PLAIN, 12);
			
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(monoSpaced);
		fillGUI(INTF, shortJava, shortOther);
		JScrollPane scrollPane = new JScrollPane(textArea);
		
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		pane.add(mainPanel);
		
		//now to make the JMenuBar
			menuBar = new JMenuBar();
				JMenu fileMenu = new JMenu("File");
					JMenuItem saveItem = new JMenuItem("Save");
					saveItem.addActionListener(this);
					saveItem.setActionCommand("save");
					fileMenu.add(saveItem);
					
					JMenuItem closeItem = new JMenuItem("Close");
					closeItem.addActionListener(this);
					closeItem.setActionCommand("close");
					fileMenu.add(closeItem);
				menuBar.add(fileMenu);

				JMenu propertiesMenu = new JMenu("Properties");
					shortJavaCheckBox = new JCheckBox("Shorten Java Classnames");
						shortJavaCheckBox.setSelected(shortJava);
						shortJavaCheckBox.setActionCommand("shorten");
						shortJavaCheckBox.addActionListener(this);
							
					shortOtherCheckBox = new JCheckBox("Shorten Non-Java Classnames");
						shortOtherCheckBox.setSelected(shortOther);
						shortOtherCheckBox.setActionCommand("shorten");
						shortOtherCheckBox.addActionListener(this);
							
					propertiesMenu.add(shortJavaCheckBox);
					propertiesMenu.add(shortOtherCheckBox);
				menuBar.add(propertiesMenu);
				refreshMoveAndCopyMenu();
				windowMenu.addMenuListener(new WindowMenuListener(this,menuBar,windowMenu));
				menuBar.add(windowMenu);
			setJMenuBar(menuBar);
			
			pack();
	}
	
	public DesktopInternalFrame getCopy()
	{
		return new SingleUMLGUI(selectedInterface,getTitle(),shortJavaCheckBox.isSelected(),shortOtherCheckBox.isSelected(),desktop);
	}
	
	public void fillGUI(Interface intF, boolean shortJava, boolean shortOther)
	{
		textArea.setText(intF.getSingleUMLAsString(shortJava, shortOther));
	}
	
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("save"))
		{
			try
			{
				ASCIIPrintFileManager raf = new ASCIIPrintFileManager("rw");
				raf.printSingleUML(selectedInterface, shortJavaCheckBox.isSelected(), shortOtherCheckBox.isSelected());
				raf.close();
			}
			catch(Throwable e)
			{
				System.out.println(e.getClass().getName()+" properly handled");
				SystemsManager.printStackTrace(e);
			}
		}
		else if (event.getActionCommand().equals("shorten"))
		{
			setTitle(selectedInterface.getPgmDefn().getInterface_name(shortJavaCheckBox.isSelected(),shortOtherCheckBox.isSelected()));
			textArea.setText(selectedInterface.getSingleUMLAsString(shortJavaCheckBox.isSelected(), shortOtherCheckBox.isSelected()));
			pack();
		}
		else if (event.getActionCommand().equals("close"))
		{
			dispose();
		}
		else
		{
			SingleUMLGUI copy = (SingleUMLGUI)getCopy();
			copy.setVisible(true);
			processWindowChange(event,copy,this);
		}
	}
	
	public class WindowDestroyer extends WindowAdapter
	{
		public WindowDestroyer()
		{
		}
		
		public void windowClosing(WindowEvent event)
		{
			dispose();
		}
	}
}
