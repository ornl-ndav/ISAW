/*
 * File:  ShortenedSourcePreferencesManager.java
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
 */
package devTools.Hawk.classDescriptor.tools.preferences;

import java.awt.GridLayout;

import devTools.Hawk.classDescriptor.gui.panel.ShortenedSourceJPanel;

/**
 * @author Dominic Kramer
 */
public class ShortenedSourcePreferencesManager extends AbstractShortenedPreferencesManager
{
	protected ShortenedSourceJPanel shortSourceJPanel;
	
	public ShortenedSourcePreferencesManager(ShortenedSourceJPanel panel)
	{
		super(shortenJavaWordsForInterfacesByDefault, "Shorten Java terms by default", shortenOtherWordsForInterfacesByDefault, "Shorten non-Java terms by default");
		
		shortSourceJPanel = panel;
		
		prefsPanel.setLayout(new GridLayout(8,1));
		shortenJavaBox.setSelected(getBooleanProperty(SHORTEN_JAVA_KEYWORD,shortenJavaWordsForInterfacesByDefault));
		shortenOtherBox.setSelected(getBooleanProperty(SHORTEN_OTHER_KEYWORD,shortenOtherWordsForInterfacesByDefault));
		prefsPanel.add(shortenJavaBox);
		prefsPanel.add(shortenOtherBox);
		prefsPanel.add(keywordPanel);
		prefsPanel.add(sourcePanel);
		prefsPanel.add(slashSlashPanel);
		prefsPanel.add(slashStarPanel);
		prefsPanel.add(javadocsPanel);
		prefsPanel.add(lineNumberPanel);
	}

	public void setPreferencesAsCurrentFromPanel()
	{
		setBooleanProperty(SHORTEN_JAVA_KEYWORD,shortenJavaBox.isSelected());
		setBooleanProperty(SHORTEN_OTHER_KEYWORD,shortenOtherBox.isSelected());
		setIntegerProperty(KEYWORD_COLOR_KEYWORD,keywordButton.getColor().getRGB());
		saveToFile();
		shortSourceJPanel.setJavaWordsShortened(shortenJavaBox.isSelected());
		shortSourceJPanel.setNonJavaWordsShortened(shortenOtherBox.isSelected());
	}
}
