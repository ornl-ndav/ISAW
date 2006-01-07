/*
 * File:  AbstractColorfulPreferencesManager.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;

import devTools.Hawk.classDescriptor.gui.ColorChooserJFrame;
import devTools.Hawk.classDescriptor.gui.ColorfulJButton;

/**
 * @author Dominic Kramer
 */
public abstract class AbstractColorfulPreferencesManager extends AbstractWindowPreferencesManager implements ActionListener
{
	public static final String KEYWORD_COLOR_KEYWORD = "keyword.color";
	public static final String SOURCE_COLOR_KEYWORD = "source.color";
	public static final String SLASH_SLASH_COLOR_KEYWORD = "slash.slash.color";
	public static final String SLASH_STAR_COLOR_KEYWORD = "slash.star.color";
	public static final String JAVADOCS_COLOR_KEYWORD = "javadocs.color";
	public static final String LINE_NUMBER_COLOR_KEYWORD = "line.number.color";
	
	protected static final Color defaultKeywordColor = Color.BLUE;
	protected static final Color defaultSourceColor = Color.BLACK;
	protected static final Color defaultSlashSlashColor = Color.GREEN;
	protected static final Color defaultSlashStarColor = Color.GREEN;
	protected static final Color defaultJavadocsColor = Color.RED;
	protected static final Color defaultLineNumberColor = Color.GRAY;
	
	protected JPanel keywordPanel;
		protected ColorfulJButton keywordButton;
	protected JPanel sourcePanel;
		protected ColorfulJButton sourceButton;
	protected JPanel slashSlashPanel;
		protected ColorfulJButton slashSlashButton;
	protected JPanel slashStarPanel;
		protected ColorfulJButton slashStarButton;
	protected JPanel javadocsPanel;
		protected ColorfulJButton javadocsButton;
	protected JPanel lineNumberPanel;
		protected ColorfulJButton lineNumberButton;
	
	public AbstractColorfulPreferencesManager()
	{
		super();
		defaultPreferencesList.setProperty(KEYWORD_COLOR_KEYWORD,convertToString(defaultKeywordColor.getRGB()));
		defaultPreferencesList.setProperty(SOURCE_COLOR_KEYWORD,convertToString(defaultSourceColor.getRGB()));
		defaultPreferencesList.setProperty(SLASH_SLASH_COLOR_KEYWORD,convertToString(defaultSlashSlashColor.getRGB()));
		defaultPreferencesList.setProperty(SLASH_STAR_COLOR_KEYWORD,convertToString(defaultSlashStarColor.getRGB()));
		defaultPreferencesList.setProperty(JAVADOCS_COLOR_KEYWORD,convertToString(defaultJavadocsColor.getRGB()));
		defaultPreferencesList.setProperty(LINE_NUMBER_COLOR_KEYWORD,convertToString(defaultLineNumberColor.getRGB()));
		
		
		
		//now to instantiate the panels and buttons
			keywordPanel = new JPanel(new BorderLayout());
				keywordButton = new ColorfulJButton(getCurrentColor(currentPreferencesList,KEYWORD_COLOR_KEYWORD,defaultKeywordColor));
			sourcePanel = new JPanel(new BorderLayout());
				sourceButton = new ColorfulJButton(getCurrentColor(currentPreferencesList,SOURCE_COLOR_KEYWORD,defaultSourceColor));
			slashSlashPanel = new JPanel(new BorderLayout());
				slashSlashButton = new ColorfulJButton(getCurrentColor(currentPreferencesList,SLASH_SLASH_COLOR_KEYWORD,defaultSlashSlashColor));
			slashStarPanel = new JPanel(new BorderLayout());
				slashStarButton = new ColorfulJButton(getCurrentColor(currentPreferencesList,SLASH_STAR_COLOR_KEYWORD,defaultSlashStarColor));
			javadocsPanel = new JPanel(new BorderLayout());
				javadocsButton = new ColorfulJButton(getCurrentColor(currentPreferencesList,KEYWORD_COLOR_KEYWORD,defaultKeywordColor));
			lineNumberPanel = new JPanel(new BorderLayout());
				lineNumberButton = new ColorfulJButton(getCurrentColor(currentPreferencesList,LINE_NUMBER_COLOR_KEYWORD,defaultLineNumberColor));
		
		//now to add everything to the panels
			createPanel(keywordPanel,keywordButton,"Select the color for java keywords",KEYWORD_COLOR_KEYWORD,defaultKeywordColor,currentPreferencesList,this);
			createPanel(sourcePanel,sourceButton,"Select the color for source code",SOURCE_COLOR_KEYWORD,defaultSourceColor,currentPreferencesList,this);
			createPanel(slashSlashPanel,slashSlashButton,"Select the color of comments of the form //--",SLASH_SLASH_COLOR_KEYWORD,defaultSlashSlashColor,currentPreferencesList,this);
			createPanel(slashStarPanel,slashStarButton,"Select the color of comments of the form /*--*/",SLASH_STAR_COLOR_KEYWORD,defaultSlashStarColor,currentPreferencesList,this);
			createPanel(javadocsPanel,javadocsButton,"Select the color of comments of the form /**--*/",JAVADOCS_COLOR_KEYWORD,defaultJavadocsColor,currentPreferencesList,this);
			createPanel(lineNumberPanel,lineNumberButton,"Select the color for line numbers",LINE_NUMBER_COLOR_KEYWORD,defaultLineNumberColor,currentPreferencesList,this);
	}
	
	private static void createPanel(JPanel panel, ColorfulJButton button, String text, String key, Color defaultColor, Properties prefsList, ActionListener actList)
	{
		panel.add(new JLabel(text),BorderLayout.WEST);
		int rgbColor = convertToInt(prefsList.getProperty(key,convertToString(defaultColor.getRGB())));
			button.addActionListener(actList);
			button.setActionCommand(key);
		panel.add(button,BorderLayout.EAST);
	}
	
	private static Color getCurrentColor(Properties prefsList, String key, Color defaultColor)
	{
		return (new Color(convertToInt(prefsList.getProperty(key,convertToString(defaultColor.getRGB())))));
	}
	
	public Color getDefaultKeywordColor()
	{
		return defaultKeywordColor;
	}
	
	public Color getDefaultSourceColor()
	{
		return defaultSourceColor;
	}
	
	public Color getDefaultSlashSlashColor()
	{
		return defaultSlashSlashColor;
	}
	
	public Color getDefaultSlashStarColor()
	{
		return defaultSlashStarColor;
	}
	
	public Color getDefaultJavadocsColor()
	{
		return defaultJavadocsColor;
	}
	
	public Color getDefaultLineNumberColor()
	{
		return defaultLineNumberColor;
	}
	
	public void setKeywordColor(Color col)
	{
		setIntegerProperty(KEYWORD_COLOR_KEYWORD,col.getRGB());
	}

	public Color getKeywordColor()
	{
		int rgbColor = getIntegerPropety(KEYWORD_COLOR_KEYWORD,defaultKeywordColor.getRGB());
		return (new Color(rgbColor));
	}
		
	public void setSourceColor(Color col)
	{
		setIntegerProperty(SOURCE_COLOR_KEYWORD,col.getRGB());
	}

	public Color getSourceColor()
	{
		int rgbColor = getIntegerPropety(SOURCE_COLOR_KEYWORD,defaultSourceColor.getRGB());
		return (new Color(rgbColor));
	}
		
	public void setSlashSlashColor(Color col)
	{
		setIntegerProperty(SLASH_SLASH_COLOR_KEYWORD,col.getRGB());
	}
	
	public Color getSlashSlashColor()
	{
		int rgbColor = getIntegerPropety(SLASH_SLASH_COLOR_KEYWORD,defaultSlashSlashColor.getRGB());
		return (new Color(rgbColor));
	}
	
	public Color getSlashStarColor()
	{
		int rgbColor = getIntegerPropety(SLASH_STAR_COLOR_KEYWORD,defaultSlashStarColor.getRGB());
		return (new Color(rgbColor));
	}
	
	public void setSlashStarColor(Color col)
	{
		setIntegerProperty(SLASH_STAR_COLOR_KEYWORD,col.getRGB());
	}
	
	public Color getJavadocsColor()
	{
		int rgbColor = getIntegerPropety(JAVADOCS_COLOR_KEYWORD,defaultJavadocsColor.getRGB());
		return (new Color(rgbColor));
	}
	
	public void setJavadocsColor(Color col)
	{
		setIntegerProperty(JAVADOCS_COLOR_KEYWORD,col.getRGB());
	}
	
	public Color getLineNumberColor()
	{
		int rgbColor = getIntegerPropety(LINE_NUMBER_COLOR_KEYWORD,defaultLineNumberColor.getRGB());
		return (new Color(rgbColor));
	}
	
	public void setLineNumberColor(Color col)
	{
		setIntegerProperty(LINE_NUMBER_COLOR_KEYWORD,col.getRGB());
	}
	
	private static void displayColorChooser(ColorfulJButton button, String title)
	{
		ColorChooserJFrame colorFrame = new ColorChooserJFrame(title,button.getColor(),button);
		colorFrame.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent event)
	{
		ColorChooserJFrame colorFrame = null;
		if (event.getActionCommand().equals(KEYWORD_COLOR_KEYWORD))
			colorFrame = new ColorChooserJFrame("Select the color for java keywords",keywordButton.getColor(),keywordButton);
		else if (event.getActionCommand().equals(SOURCE_COLOR_KEYWORD))
			colorFrame = new ColorChooserJFrame("Select the color for source code",sourceButton.getColor(),sourceButton);
		else if (event.getActionCommand().equals(SLASH_SLASH_COLOR_KEYWORD))
			colorFrame = new ColorChooserJFrame("Select the color of comments of the form //--",slashSlashButton.getColor(),slashSlashButton);
		else if (event.getActionCommand().equals(SLASH_STAR_COLOR_KEYWORD))
			colorFrame = new ColorChooserJFrame("Select the color of comments of the form /*--*/",slashStarButton.getColor(),slashStarButton);
		else if (event.getActionCommand().equals(JAVADOCS_COLOR_KEYWORD))
			colorFrame = new ColorChooserJFrame("Select the color of comments of the form /**--*/",javadocsButton.getColor(),javadocsButton);
		else if (event.getActionCommand().equals(LINE_NUMBER_COLOR_KEYWORD))
			colorFrame = new ColorChooserJFrame("Select the color for line numbers",lineNumberButton.getColor(),lineNumberButton);
		if (colorFrame != null)
			colorFrame.setVisible(true);
	}
}
