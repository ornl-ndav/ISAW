/*
 * File:  ShortenedSourceJPanel.java
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
package devTools.Hawk.classDescriptor.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import devTools.Hawk.classDescriptor.gui.ColorfulTextUtilities;
import devTools.Hawk.classDescriptor.gui.ExternallyControlledFrame;
import devTools.Hawk.classDescriptor.gui.MouseNotifiable;
import devTools.Hawk.classDescriptor.gui.NonWrappedJTextPane;
import devTools.Hawk.classDescriptor.modeledObjects.AttributeDefn;
import devTools.Hawk.classDescriptor.modeledObjects.ConstructorDefn;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.MethodDefn;
import devTools.Hawk.classDescriptor.tools.ASCIIPrintFileManager;
import devTools.Hawk.classDescriptor.tools.SystemsManager;
import devTools.Hawk.classDescriptor.tools.preferences.AbstractColorfulPreferencesManager;
import devTools.Hawk.classDescriptor.tools.preferences.ShortenedSourcePreferencesManager;

/**
 * This is a specialized JPanel that displays shortened source code information for a class or interface.  
 * This class in under construction.
 * @author Dominic Kramer
 */
public class ShortenedSourceJPanel extends JPanel implements ActionListener, MouseNotifiable
{
	/**
	 * The pane which the text is written on.
	 */
	protected NonWrappedJTextPane textPane;
	/**
	 * The StyledDocument which supports colored text.
	 */
	protected StyledDocument doc;
	/**
	 * The Interface whose data is to be written.
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
	 * The container on which this panel is placed.
	 */
	protected ExternallyControlledFrame frame;
	/**
	 * The manager that handles the user's preferences.
	 */
	protected ShortenedSourcePreferencesManager prefsManager;
	
	/**
	 * Create a new ShortenedSourceGUI.
	 * @param INTF The Interface object whose data is written.
	 * @param frm The frame that this panel can control (ie dispose).
	 */
	public ShortenedSourceJPanel(Interface INTF, ExternallyControlledFrame frm)
	{
		selectedInterface = INTF;
		frame = frm;
		prefsManager = new ShortenedSourcePreferencesManager(this);

		shortJavaCheckBox = new JCheckBox("Shorten Java Classnames");
			shortJavaCheckBox.setSelected(prefsManager.getShortenJavaTermsForInterfaces());
			shortJavaCheckBox.setActionCommand("shorten");
			shortJavaCheckBox.addActionListener(this);
					
		shortOtherCheckBox = new JCheckBox("Shorten Non-Java Classnames");
			shortOtherCheckBox.setSelected(prefsManager.getShortenNonJavaTermsForInterfaces());
			shortOtherCheckBox.setActionCommand("shorten");
			shortOtherCheckBox.addActionListener(this);
					
		setLayout(new GridLayout(1,1));
		
		textPane = new NonWrappedJTextPane();
		doc = textPane.getStyledDocument();
		addStylesToDocument(doc,prefsManager);
		fillInTextArea();
		JScrollPane scrollPane = new JScrollPane(textPane);
		
		add(scrollPane);
	}
	
	public boolean areJavaWordsShortened()
	{
		return shortJavaCheckBox.isSelected();
	}
	
	public boolean areNonJavaWordsShortened()
	{
		return shortOtherCheckBox.isSelected();
	}
	
	public void setJavaWordsShortened(boolean bol)
	{
		shortJavaCheckBox.setSelected(bol);
		fillInTextArea();
	}
	
	public void setNonJavaWordsShortened(boolean bol)
	{
		shortOtherCheckBox.setSelected(bol);
		fillInTextArea();
	}
	
	public Interface getInterface()
	{
		return selectedInterface;
	}
	
	public JMenuBar createMenuBar()
	{
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
					propertiesMenu.add(shortJavaCheckBox);
					propertiesMenu.add(shortOtherCheckBox);

			JMenuItem preferencesItem = new JMenuItem("Preferences");
				preferencesItem.setActionCommand("preferences");
				preferencesItem.addActionListener(this);

					propertiesMenu.add(preferencesItem);
				ssMenuBar.add(propertiesMenu);
				
				return ssMenuBar;
	}
	
	/**
	 * This method calls the static method ColorfulTextUtilities.addStylesToDocument(StyledDocument) 
	 * to add styles to the document.  Then, it adds an extra style with the name "header".  If text is added 
	 * to the document using this style the text will be colored black and will be bold.   
	 * @param doc The document to add the styles to.
	 */
	public void addStylesToDocument(StyledDocument doc, AbstractColorfulPreferencesManager manager)
	{
		ColorfulTextUtilities.addStylesToDocument(doc,manager);
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style header = doc.addStyle("header",def);
		StyleConstants.setForeground(header,Color.BLACK);
		StyleConstants.setBold(header,true);
	}
	
	/**
	 * Writes information in the GUI in a format similar to the following:
	 * <br> package a.b.c.d
	 * <br>
	 * <br>public class classA extends JFrame implements ActionListener
	 * <br> {
	 * <br>       Attribute
	 * <br>       public int num
	 * <br>
	 * <br>       Constructor
	 * <br>       public classA(int)
	 * <br>
	 * <br>       Method
	 * <br>       public int getNum()
	 * <br>       public void setNum(int)
	 * <br> }
	 * <br> with Attribute, Constructor, and Method colored black and bold.  Also, keywords are 
	 * colored.
	 */
	public void fillInTextArea()
	{
		boolean shortJava = shortJavaCheckBox.isSelected();
		boolean shortOther = shortOtherCheckBox.isSelected();
		Interface intF = selectedInterface;
		try
		{
			doc.remove(0,doc.getLength());
			final String tab = "    ";
			ColorfulTextUtilities.addProcessedText(intF.getPgmDefn().getStringInJavadocFormat(shortJava,shortOther)+"\n",doc);
			
			if (intF.getAttribute_vector().size() == 1)
				doc.insertString(doc.getLength(),tab+"Attribute\n",doc.getStyle("header"));
			else
				doc.insertString(doc.getLength(),tab+"Attributes\n",doc.getStyle("header"));
				
				for (int i=0; i<intF.getAttribute_vector().size(); i++)
					ColorfulTextUtilities.addProcessedText(tab+tab+((AttributeDefn)intF.getAttribute_vector().elementAt(i)).getStringInJavadocFormat(shortJava,shortOther)+"\n",doc);
				doc.insertString(doc.getLength(),tab+"\n",null);
				
			if (intF.getConst_vector().size() == 1)
				doc.insertString(doc.getLength(),tab+"Constructor\n",doc.getStyle("header"));
			else
				doc.insertString(doc.getLength(),tab+"Constructors\n",doc.getStyle("header"));
				
				for (int i=0; i<intF.getConst_vector().size(); i++)
					ColorfulTextUtilities.addProcessedText(tab+tab+((ConstructorDefn)intF.getConst_vector().elementAt(i)).getStringInJavadocFormat(shortJava,shortOther)+"\n",doc);
				doc.insertString(doc.getLength(),tab+"\n",null);

			if (intF.getMethod_vector().size() == 1)
				doc.insertString(doc.getLength(),tab+"Method\n",doc.getStyle("header"));
			else
				doc.insertString(doc.getLength(),tab+"Methods\n",doc.getStyle("header"));
				
				for (int i=0; i<intF.getMethod_vector().size(); i++)
					ColorfulTextUtilities.addProcessedText(tab+tab+((MethodDefn)intF.getMethod_vector().elementAt(i)).getStringInJavadocFormat(shortJava,shortOther)+"\n",doc);

			doc.insertString(doc.getLength(),"}",null);
		}
		catch (BadLocationException e)
		{
			SystemsManager.printStackTrace(e);
		}
	}
		
	/**
	 * Handles ActionEvent.
	 */
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
			fillInTextArea();
			frame.setTitle(selectedInterface.getPgmDefn().getInterface_name(shortJavaCheckBox.isSelected(), shortOtherCheckBox.isSelected()));
		}
		else if (event.getActionCommand().equals("preferences"))
		{
			prefsManager.saveCheckBoxState();
			PreferencesJFrame frame = this.new PreferencesJFrame();
			frame.setVisible(true);
		}
		else if (event.getActionCommand().equals("close"))
			frame.dispose();
	}
	
	/**
	 * The Components in the array returned from this method are the Components that should have the 
	 * mouse use the waiting animation when an operation is in progress.
	 */
	public Component[] determineWaitingComponents()
	{
		Component[] compArr = new Component[3];
		compArr[0] = textPane;
		compArr[1] = frame.getControlledComponent();
		compArr[2] = this;
		return compArr;
	}
	
	private class PreferencesJFrame extends JFrame implements ActionListener
	{
		public PreferencesJFrame()
		{
			setTitle("Shortened Source Preferences");
			JPanel mainPanel = new JPanel(new BorderLayout());
			JPanel prefsPanel = prefsManager.getJPanel();
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				JButton cancelButton = new JButton("Cancel");
					cancelButton.setActionCommand("cancel");
					cancelButton.addActionListener(this);
				JButton okButton = new JButton("Ok");
					okButton.setActionCommand("ok");
					okButton.addActionListener(this);
				buttonPanel.add(cancelButton);
				buttonPanel.add(okButton);
				
			mainPanel.add(prefsPanel,BorderLayout.CENTER);
			mainPanel.add(buttonPanel,BorderLayout.SOUTH);
			getContentPane().add(mainPanel);
			pack();
		}
		
		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("ok"))
				prefsManager.setPreferencesAsCurrentFromPanel();
			else if (event.getActionCommand().equals("cancel"))
				prefsManager.restoreCheckBoxState();
			dispose();
		}
	}
}
