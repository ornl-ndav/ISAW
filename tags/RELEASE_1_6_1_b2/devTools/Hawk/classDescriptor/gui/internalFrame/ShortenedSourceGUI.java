/*
 * File:  ShortenedSourceGUI.java
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
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.modeledObjects.AttributeDefn;
import devTools.Hawk.classDescriptor.modeledObjects.ConstructorDefn;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.MethodDefn;
import devTools.Hawk.classDescriptor.tools.ASCIIPrintFileManager;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

public class ShortenedSourceGUI extends ColorfulTextGUI implements ActionListener
{
	protected JTextPane textPane;
	protected StyledDocument doc;
	protected Interface selectedInterface;
	
	protected JCheckBox shortJavaCheckBox;
	protected JCheckBox shortOtherCheckBox;
	
	public ShortenedSourceGUI(Interface INTF, String title, boolean shortJava, boolean shortOther, HawkDesktop desk)
	{
		super(desk);
		
		selectedInterface = INTF;
		
		setTitle(title);
		setLocation(0,0);
		setSize(175,400);
		setClosable(true);
		setIconifiable(true);
		setMaximizable(true);
		setResizable(true);
		Container pane = getContentPane();
		
		//this allows to close the window
		//addActionListener(new WindowDestroyer());
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());		
		
		textPane = new JTextPane();
		doc = textPane.getStyledDocument();
		addStylesToDocument(doc);
		fillInTextArea(INTF, shortJava, shortOther);
		JScrollPane scrollPane = new JScrollPane(textPane);
		
		mainPanel.add(scrollPane,BorderLayout.CENTER);
		pane.add(mainPanel);
		
		//now to create the JMenuBar
			JMenuBar ssMenuBar = new JMenuBar();
				JMenu fileMenu = new JMenu("File");
					JMenuItem save = new JMenuItem("Save");
					save.setActionCommand("save");
					save.addActionListener(this);
				
					JMenuItem close = new JMenuItem("Close");
					close.setActionCommand("close");
					close.addActionListener(this);
				fileMenu.add(save);
				fileMenu.add(close);
			ssMenuBar.add(fileMenu);
				
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
				ssMenuBar.add(propertiesMenu);
				refreshMoveAndCopyMenu();
				windowMenu.addMenuListener(new WindowMenuListener(this,menuBar,windowMenu));
				ssMenuBar.add(windowMenu);
				menuBar = ssMenuBar;
		setJMenuBar(ssMenuBar);
		pack();
	}
	
	public DesktopInternalFrame getCopy()
	{
		return new ShortenedSourceGUI(selectedInterface,getTitle(),shortJavaCheckBox.isSelected(),shortOtherCheckBox.isSelected(),desktop);
	}
	
	public void addStylesToDocument(StyledDocument doc)
	{
		super.addStylesToDocument(doc);
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style header = doc.addStyle("header",def);
		StyleConstants.setForeground(header,Color.BLACK);
		StyleConstants.setBold(header,true);
	}
			
	public void fillInTextArea(Interface intF, boolean shortJava, boolean shortOther)
	{
		try
		{
			doc.remove(0,doc.getLength());
			final String tab = "    ";
			addProcessedText(intF.getPgmDefn().getStringInJavadocFormat(shortJava,shortOther)+"\n",doc);
			
			if (intF.getAttribute_vector().size() == 1)
				doc.insertString(doc.getLength(),tab+"Attribute\n",doc.getStyle("header"));
			else
				doc.insertString(doc.getLength(),tab+"Attributes\n",doc.getStyle("header"));
				
				for (int i=0; i<intF.getAttribute_vector().size(); i++)
					addProcessedText(tab+tab+((AttributeDefn)intF.getAttribute_vector().elementAt(i)).getStringInJavadocFormat(shortJava,shortOther)+"\n",doc);
				doc.insertString(doc.getLength(),tab+"\n",null);
				
			if (intF.getConst_vector().size() == 1)
				doc.insertString(doc.getLength(),tab+"Constructor\n",doc.getStyle("header"));
			else
				doc.insertString(doc.getLength(),tab+"Constructors\n",doc.getStyle("header"));
				
				for (int i=0; i<intF.getConst_vector().size(); i++)
					addProcessedText(tab+tab+((ConstructorDefn)intF.getConst_vector().elementAt(i)).getStringInJavadocFormat(shortJava,shortOther)+"\n",doc);
				doc.insertString(doc.getLength(),tab+"\n",null);

			if (intF.getMethod_vector().size() == 1)
				doc.insertString(doc.getLength(),tab+"Method\n",doc.getStyle("header"));
			else
				doc.insertString(doc.getLength(),tab+"Methods\n",doc.getStyle("header"));
				
				for (int i=0; i<intF.getMethod_vector().size(); i++)
					addProcessedText(tab+tab+((MethodDefn)intF.getMethod_vector().elementAt(i)).getStringInJavadocFormat(shortJava,shortOther)+"\n",doc);

			doc.insertString(doc.getLength(),"}",null);
		}
		catch (BadLocationException e)
		{
			SystemsManager.printStackTrace(e);
		}
	}
	
	public void actionPerformed( ActionEvent event)
	{
		if (event.getActionCommand().equals("save"))
		{
			try
			{
				ASCIIPrintFileManager raf = new ASCIIPrintFileManager("rw");
				raf.printShortenedSource(0, selectedInterface, shortJavaCheckBox.isSelected(), shortOtherCheckBox.isSelected());
				raf.close();
			}
			catch(Exception e)
			{
				SystemsManager.printStackTrace(e);
			}
		}
		else if (event.getActionCommand().equals("shorten"))
		{
			fillInTextArea(selectedInterface,shortJavaCheckBox.isSelected(), shortOtherCheckBox.isSelected());
			pack();
		}
		else if (event.getActionCommand().equals("close"))
		{
			dispose();
		}
		else
		{
			ShortenedSourceGUI copy = (ShortenedSourceGUI)getCopy();
			copy.setVisible(true);
			processWindowChange(event,copy,this);
		}
	}
}
