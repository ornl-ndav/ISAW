/*
 * File:  SourceCodeJPanel.java
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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

import devTools.Hawk.classDescriptor.gui.ColorfulTextUtilities;
import devTools.Hawk.classDescriptor.gui.ExternallyControlledFrame;
import devTools.Hawk.classDescriptor.gui.MouseNotifiable;
import devTools.Hawk.classDescriptor.gui.NonWrappedJTextPane;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.preferences.SourceCodePreferencesManager;

/**
 * This is a specialized JPanel that displays source code information for a class or interface.  
 * This class in under construction.
 * @author Dominic Kramer
 */
public class SourceCodeJPanel extends JPanel  implements ActionListener, MouseNotifiable
{
	/**
	 * The pane to which the source code is written.
	 */
	protected NonWrappedJTextPane sourcePane;
	/**
	 * The Interface whose source code is written.
	 */
	protected Interface selectedInterface;
	/**
	 * The document associated with the JTextPane which supports writing colored text.
	 */
	protected StyledDocument document;
	/**
	 * The manager which keeps track of user preferences.
	 */
	protected SourceCodePreferencesManager prefsManager;
	/**
	 * The frame that contains this panel.  Because frame is an ExternallyControlledFrame, 
	 * this panel can handle window actions such as closing or resizing the window.
	 */
	protected ExternallyControlledFrame frame;
	
	/**
	 * Create a new SourceCodeGUI.
	 * @param INT The Interface object whose source code is to be displayed.
	 * @param title The window's title.
	 * @param comp The largest component onto which this panel is placed.
	 */
	public SourceCodeJPanel(Interface INT, ExternallyControlledFrame conFrame)
	{
		setLayout(new GridLayout(1,1));
		
		//now to instantiate selectedInterface
			selectedInterface = INT;
			frame = conFrame;
			prefsManager = new SourceCodePreferencesManager(this);
			
		//now to create the area for placing the source code
		sourcePane = new NonWrappedJTextPane();
			document = sourcePane.getStyledDocument();
			ColorfulTextUtilities.addStylesToDocument(document,prefsManager);
			System.out.println("About to add styles to the document.");
			long num = ColorfulTextUtilities.styleDocument(INT.getSourceFileName(),document);
			JTextPane numPane = ColorfulTextUtilities.getNumberedJPanel(num,prefsManager);
		
		JPanel textPanel = new JPanel(new BorderLayout());
			textPanel.add(numPane, BorderLayout.WEST);
			textPanel.add(sourcePane, BorderLayout.CENTER);
		
		//now to create the JScrollPane to put the JEditorPane on
			JScrollPane scrollPane = new JScrollPane(textPanel);
		
		//now to add the components to the main panel
			add(scrollPane, BorderLayout.CENTER);
	}
	
	public JMenuBar createJMenuBar()
	{
		//Now to make the JMenuBar
			JMenuBar sourceMenuBar = new JMenuBar();
				JMenu fileMenu = new JMenu("File");
					JMenuItem closeItem = new JMenuItem("Close");
					closeItem.setActionCommand("Close");
					closeItem.addActionListener(this);
				fileMenu.add(closeItem);
			sourceMenuBar.add(fileMenu);
		
		return sourceMenuBar;
	}
	
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Close"))
			frame.dispose();
	}
	
	/**
	 * The Components in the array returned from this method are the Components that should have the 
	 * mouse use the waiting animation when an operation is in progress.
	 */
	public Component[] determineWaitingComponents()
	{
		Component[] compArr = new Component[3];
		compArr[0] = sourcePane;
		compArr[1] = frame.getControlledComponent();
		compArr[2] = this;
		return compArr;
	}
}
