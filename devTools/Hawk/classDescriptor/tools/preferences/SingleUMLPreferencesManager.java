/*
 * File:  SingleUMLPreferencesManager.java
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
package devTools.Hawk.classDescriptor.tools.preferences;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import devTools.Hawk.classDescriptor.gui.panel.SingleUMLJPanel;

/**
 * @author Dominic Kramer
 */
public class SingleUMLPreferencesManager extends AbstractShortenedPreferencesManager
{
	public static final String DIAGRAM_KEYWORD = "default.diagram";
	
	public static final String ASCII_DIAGRAM = "ascii";
	public static final String HTML_DIAGRAM = "html";
	
	protected static final boolean shortenJavaWordsByDefault = true;
	protected static final boolean shortenOtherWordsByDefault = false;
	protected static final String defaultDiagram = ASCII_DIAGRAM;
	
	protected JComboBox diagramComboBox;
	protected final String[] diagramArray = {"ASCII diagram","HTML diagram"};
	
	protected int diagramSavedState;
	
	protected SingleUMLJPanel umlPanel;
	
	public SingleUMLPreferencesManager(SingleUMLJPanel panel)
	{
		super(shortenJavaWordsByDefault, "Shorten Java terms by default", shortenOtherWordsByDefault, "Shorten non-Java terms by default");
		defaultPreferencesList.setProperty(DIAGRAM_KEYWORD,defaultDiagram);
		umlPanel = panel;
		
		prefsPanel.setLayout(new GridLayout(3,1));
		shortenJavaBox.setSelected(getBooleanProperty(SHORTEN_JAVA_KEYWORD,shortenJavaWordsForInterfacesByDefault));
		shortenOtherBox.setSelected(getBooleanProperty(SHORTEN_OTHER_KEYWORD,shortenOtherWordsForInterfacesByDefault));
		JPanel comboBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			comboBoxPanel.add(new JLabel("By default use the "));
			diagramComboBox = new JComboBox(diagramArray);
			diagramComboBox.setSelectedIndex(getIndex(currentPreferencesList.getProperty(DIAGRAM_KEYWORD,defaultDiagram)));
			comboBoxPanel.add(diagramComboBox);
		prefsPanel.add(shortenJavaBox);
		prefsPanel.add(shortenOtherBox);
		prefsPanel.add(comboBoxPanel);
		
		String currentDiagram = currentPreferencesList.getProperty(DIAGRAM_KEYWORD,defaultDiagram);
		diagramSavedState = getIndex(currentDiagram);
	}
	
	public String getDiagram()
	{
		return getProperty(DIAGRAM_KEYWORD,defaultDiagram);
	}
	
	public void saveCheckBoxState()
	{
		diagramSavedState = diagramComboBox.getSelectedIndex();
		super.saveCheckBoxState();
	}
	
	public void restoreCheckBoxState()
	{
		diagramComboBox.setSelectedIndex(diagramSavedState);
		super.restoreCheckBoxState();
	}
	
	protected String getComboBoxValue()
	{
		if (diagramComboBox.getSelectedIndex() == 0)
			return ASCII_DIAGRAM;
		else
			return HTML_DIAGRAM;
	}
	
	protected static int getIndex(String value)
	{
		if (value.equals(ASCII_DIAGRAM))
			return 0;
		else
			return 1;
	}
	
	public void setPreferencesAsCurrentFromPanel()
	{
		setBooleanProperty(SHORTEN_JAVA_KEYWORD,shortenJavaBox.isSelected());
		setBooleanProperty(SHORTEN_OTHER_KEYWORD,shortenOtherBox.isSelected());
		setProperty(DIAGRAM_KEYWORD, getComboBoxValue());
		saveToFile();
		umlPanel.setJavaNamesShortened(shortenJavaBox.isSelected());
		umlPanel.setNonJavaNamesShortened(shortenOtherBox.isSelected());
		umlPanel.fillGUI();
		if (getComboBoxValue().equals(ASCII_DIAGRAM))
			umlPanel.setSelectedDiagram(SingleUMLJPanel.ASCII_DIAGRAM);
		else
			umlPanel.setSelectedDiagram(SingleUMLJPanel.HTML_DIAGRAM);
	}
}
