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
 * Revision 1.2  2004/03/11 19:01:51  bouzekc
 * Documented file using javadoc statements.
 * Added the method resizeAndRelocate() method to handle the size and placement of
 * the window.
 * Added a JTabbedPane which displays the UML diagram in ASCII format in one tab
 * and in HTML format in the other tab.
 *
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

import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.ASCIIPrintFileManager;
import devTools.Hawk.classDescriptor.tools.HTMLPrintFileManager;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * This is a special type of JInternalFrame that displays a UML diagram of an Interface object 
 * in an ASCII format.
 * @author Dominic Kramer
 */
public class SingleUMLGUI extends DesktopInternalFrame implements ActionListener
{
	/**
	 * The text area to add the ASCII version of the UML diagram to.
	 */
	protected JTextArea textArea;
	/**
	 * This is the area which holds an HTML version of the UML diagram.
	 */
	protected JEditorPane htmlPane;
	/**
	 * The Interface object whose data is written to the window.
	 */
	protected Interface selectedInterface;
	/**
	 * The checkbox allowing the user to select if they want to shorten java names.
	 */	
	protected JCheckBox shortJavaCheckBox;
	/**
	 * The checkbox allowing the user to select if they want to shorten non-java names.
	 */
	protected JCheckBox shortOtherCheckBox;

	/**
	 * Create a new SingleUMLGUI.
	 * @param INTF The Interface object whose data is written.
	 * @param title The title of the window.
	 * @param shortJava True if you want a name to be shortened if it is a java name.  For 
	 * example, java.lang.String would be shortened to String.
	 * @param shortOther True if you want a name to be shortened if it is a non-java name.
	 * @param desk The HawkDesktop that this window is on.
	 */
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

		htmlPane = new JEditorPane("text/html","");
		htmlPane.setEditable(false);

		fillGUI(INTF, shortJava, shortOther);
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		JScrollPane htmlScroller = new JScrollPane(htmlPane);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Text Version",scrollPane);
		tabbedPane.addTab("HTML Version",htmlScroller);
		
		mainPanel.add(tabbedPane, BorderLayout.CENTER);
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
//				refreshMoveAndCopyMenu();
//				windowMenu.addMenuListener(new WindowMenuListener(this,menuBar,windowMenu));
				menuBar.add(windowMenu);
			setJMenuBar(menuBar);
			
			resizeAndRelocate();
	}
	
	public void resizeAndRelocate()
	{
		pack();
		int count = desktop.getTabbedPane().getTabCount();
		if (count > 0)
		{
			int maxWidth = ((JDesktopPane)desktop.getTabbedPane().getComponentAt(0)).getWidth();
			int maxHeight = ((JDesktopPane)desktop.getTabbedPane().getComponentAt(0)).getHeight();
			
			int height = getHeight();
			int width = getWidth();

			int newX = getX();
			int newY = getY();
			int newWidth = getWidth();
			int newHeight = getHeight();
			
			if ((maxHeight-height)<=0)
			{
				newHeight = (int)(0.8*maxHeight);
				newY = (int)(0.1*maxHeight);
			}
			else
				newY = (maxHeight-height)/2;
			
			if ((maxWidth-width)<=0)
			{
				newWidth = (int)(0.8*maxWidth);
				newX = (int)(0.1*maxWidth);
			}
			else
				newX = (maxWidth-width)/2;
						
			setLocation(newX,newY);
			setSize(newWidth,newHeight);
		}
	}
	
	/**
	 * Gets a copy of this window.
	 * @return A copy of this window.
	 */
	public DesktopInternalFrame getCopy()
	{
		return new SingleUMLGUI(selectedInterface,getTitle(),shortJavaCheckBox.isSelected(),shortOtherCheckBox.isSelected(),desktop);
	}
	
	/**
	 * This fills in the JTextArea with an ASCII version of the UML diagram representing 
	 * the Interface.
	 * @param intF The Interface object whose data is analyzed.
	 * @param shortJava True if you want a name to be shortened if it is a java name.  For 
	 * example, java.lang.String would be shortened to String.
	 * @param shortOther True if you want a name to be shortened if it is a non-java name.
     */
	public void fillGUI(Interface intF, boolean shortJava, boolean shortOther)
	{
		textArea.setText(intF.getSingleUMLAsString(shortJava, shortOther));
		htmlPane.setText("<html>\n<body>\n"+HTMLPrintFileManager.getHTMLCodeForSingleUML(intF,shortJava,shortOther)+"</body>\n</html>");
	}
	
	/**
	 * Handles ActionEvents.
	 */
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
			fillGUI(selectedInterface,shortJavaCheckBox.isSelected(), shortOtherCheckBox.isSelected());
			pack();
		}
		else if (event.getActionCommand().equals("close"))
		{
			dispose();
		}
		else
		{
/*
			SingleUMLGUI copy = (SingleUMLGUI)getCopy();
			copy.setVisible(true);
			processWindowChange(event,copy,this);
*/
			super.actionPerformed(event);
		}
	}
}
