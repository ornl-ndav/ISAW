/*
 * File:  SourceCodePreferencesManager.java
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

import java.awt.Color;

import devTools.Hawk.classDescriptor.gui.panel.SourceCodeJPanel;

/**
 * @author Dominic Kramer
 */
public class SourceCodePreferencesManager extends AbstractColorfulPreferencesManager
{
	public static final String SOURCE_COLOR_KEYWORD = "source.color";
	public static final String SLASH_SLASH_COMMENT_COLOR_KEYWORD = "slash.slash.color";	
	public static final String SLASH_STAR_COMMENT_COLOR_KEYWORD = "slash.star.color";
	public static final String JAVADOCS_COLOR_KEYWORD = "javadocs.color";
	public static final String LINE_NUMBERS_KEYWORD = "show.line.numbers";
	
	protected static final int defaultSourceColor = Color.BLACK.getRGB();
	protected static final int defaultSlashSlashColor = Color.GREEN.getRGB();
	protected static final int defaultSlashStarColor = defaultSlashSlashColor;
	protected static final int defaultJavadocsColor = Color.BLUE.getRGB();
	protected static final boolean showLineNumbersByDefault = true;
	
	protected SourceCodeJPanel sourcePanel;
	
	public SourceCodePreferencesManager(SourceCodeJPanel panel)
	{
		super();
		sourcePanel = panel;
		
		defaultPreferencesList.setProperty(SOURCE_COLOR_KEYWORD, convertToString(defaultSourceColor));
		defaultPreferencesList.setProperty(SLASH_SLASH_COMMENT_COLOR_KEYWORD, convertToString(defaultSlashSlashColor));
		defaultPreferencesList.setProperty(SLASH_STAR_COMMENT_COLOR_KEYWORD, convertToString(defaultSlashStarColor));
		defaultPreferencesList.setProperty(JAVADOCS_COLOR_KEYWORD, convertToString(defaultJavadocsColor));
		defaultPreferencesList.setProperty(LINE_NUMBERS_KEYWORD, convertToString(showLineNumbersByDefault));
	}
}
