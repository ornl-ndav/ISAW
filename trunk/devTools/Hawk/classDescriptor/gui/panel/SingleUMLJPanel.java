/*
 * File:  SingleUMLJPanel.java
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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import devTools.Hawk.classDescriptor.gui.ExternallyControlledFrame;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.ASCIIPrintFileManager;
import devTools.Hawk.classDescriptor.tools.HTMLPrintFileManager;
import devTools.Hawk.classDescriptor.tools.SystemsManager;
import devTools.Hawk.classDescriptor.tools.preferences.SingleUMLPreferencesManager;

/**
 * This is a specialized JPanel that displays a UML diagram for a class or interface.  
 * This class in under construction.
 * @author Dominic Kramer
 */
public class SingleUMLJPanel extends JPanel implements ActionListener
{
	public static final int ASCII_DIAGRAM = 0;
	public static final int HTML_DIAGRAM = 1;
	
	/**
	 * The text area to add the ASCII version of the UML diagram to.
	 */
	protected JTextArea textArea;
	/**
	 * This is the area which holds an HTML version of the UML diagram.
	 */
	protected JEditorPane htmlPane;
	/**
	 * The tabbed pane on which the panes are placed.
	 */
	protected JTabbedPane tabbedPane;
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
	 * The frame that contains this panel.  By being an ExternallyControlledFrame (implementing 
	 * the ExternallyControlled interface), this panel can handle window actions such as closing 
	 * or resizing the window.
	 */
	protected ExternallyControlledFrame frame;
	/**
	 * The manager which hancles the preferences for this GUI.
	 */
	protected SingleUMLPreferencesManager prefsManager;

	/**
	 * Create a new SingleUMLJPanel.
	 * @param INTF The Interface object whose data is written.
	 * @param title The title of the window.
	 * @param shortJava True if you want a name to be shortened if it is a java name.  For 
	 * example, java.lang.String would be shortened to String.
	 * @param shortOther True if you want a name to be shortened if it is a non-java name.
	 * @param desk The HawkDesktop that this window is on.
	 */
	public SingleUMLJPanel(Interface INTF, boolean shortJava, boolean shortOther, ExternallyControlledFrame ecf)
	{		
		//now to set selectedInterface
			selectedInterface = INTF;
		    frame = ecf;
		    
		//these are to be used if and when the createJMenuBar() method is called.		
		shortJavaCheckBox = new JCheckBox("Shorten Java Classnames");
			shortJavaCheckBox.setSelected(shortJava);
			shortJavaCheckBox.setActionCommand("shorten");
			shortJavaCheckBox.addActionListener(this);
							
		shortOtherCheckBox = new JCheckBox("Shorten Non-Java Classnames");
			shortOtherCheckBox.setSelected(shortOther);
			shortOtherCheckBox.setActionCommand("shorten");
			shortOtherCheckBox.addActionListener(this);

		prefsManager = new SingleUMLPreferencesManager(this);
			shortJavaCheckBox.setSelected(prefsManager.getShortenJavaTermsForInterfaces());
			shortOtherCheckBox.setSelected(prefsManager.getShortenNonJavaTermsForInterfaces());
		
		setLayout(new GridLayout(1,1));
		Font monoSpaced = new Font("Monospaced", Font.PLAIN, 12);
			
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(monoSpaced);

		htmlPane = new JEditorPane("text/html","");
		htmlPane.setEditable(false);

		fillGUI();
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		JScrollPane htmlScroller = new JScrollPane(htmlPane);

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Text Version",scrollPane);
		tabbedPane.addTab("HTML Version",htmlScroller);
		
		add(tabbedPane);
		String diagramToDisplay = prefsManager.getDiagram();
		if (diagramToDisplay.equals(SingleUMLPreferencesManager.ASCII_DIAGRAM))
			tabbedPane.setSelectedIndex(ASCII_DIAGRAM);
		else if (diagramToDisplay.equals(SingleUMLPreferencesManager.HTML_DIAGRAM))
			tabbedPane.setSelectedIndex(HTML_DIAGRAM);
	}
	
	public void setJavaNamesShortened(boolean bol)
	{
		shortJavaCheckBox.setSelected(bol);
	}
	
	public void setNonJavaNamesShortened(boolean bol)
	{
		shortOtherCheckBox.setSelected(bol);
	}
	
	public void setSelectedDiagram(int index)
	{
		tabbedPane.setSelectedIndex(index);
	}
	
	public int getSelectedDiagram()
	{
		return tabbedPane.getSelectedIndex();
	}
	
	public boolean areJavaNamesShortened()
	{
		return shortJavaCheckBox.isSelected();
	}
	
	public boolean areNonJavaNamesShortened()
	{
		return shortOtherCheckBox.isSelected();
	}
	
	public JCheckBox getShortenOtherCheckBox()
	{
		return shortOtherCheckBox;
	}
		
	public JMenuBar createJMenuBar()
	{
		//now to make the JMenuBar
			JMenuBar menuBar = new JMenuBar();
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
					JMenuItem preferencesItem = new JMenuItem("Preferences");
						preferencesItem.setActionCommand("preferences");
						preferencesItem.addActionListener(this);						
					propertiesMenu.add(shortJavaCheckBox);
					propertiesMenu.add(shortOtherCheckBox);
					propertiesMenu.add(preferencesItem);
				menuBar.add(propertiesMenu);
				
			return menuBar;
	}

	/**
	 * This fills in the JTextArea with an ASCII version of the UML diagram representing 
	 * the Interface.
	 * @param intF The Interface object whose data is analyzed.
	 * @param shortJava True if you want a name to be shortened if it is a java name.  For 
	 * example, java.lang.String would be shortened to String.
	 * @param shortOther True if you want a name to be shortened if it is a non-java name.
	 */
	public void fillGUI()
	{
		textArea.setText(selectedInterface.getSingleUMLAsString(shortJavaCheckBox.isSelected(), shortOtherCheckBox.isSelected()));
		htmlPane.setText("<html>\n<body>\n"+HTMLPrintFileManager.getHTMLCodeForSingleUML(selectedInterface,shortJavaCheckBox.isSelected(),shortOtherCheckBox.isSelected())+"</body>\n</html>");
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
			frame.setTitle(selectedInterface.getPgmDefn().getInterface_name(shortJavaCheckBox.isSelected(),shortOtherCheckBox.isSelected()));
			fillGUI();
		}
		else if (event.getActionCommand().equals("preferences"))
		{
			prefsManager.saveCheckBoxState();
			PreferencesFrame frame = new PreferencesFrame();
			frame.setVisible(true);
		}
		else if (event.getActionCommand().equals("close"))
			frame.dispose();
	}
	
	public Component[] determineWaitingComponents()
	{
		Component[] compArr = new Component[4];
		compArr[0] = htmlPane;
		compArr[1] = textArea;
		compArr[2] = frame.getControlledComponent();
		compArr[3] = this;
		return compArr;
	}
	
	private class PreferencesFrame extends JFrame implements ActionListener
	{
		public PreferencesFrame()
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
