/*
 * File:  AbstractPrintingPreferencesManager.java
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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * @author Dominic Kramer
 */
public abstract class AbstractPrintingPreferencesManager extends AbstractPreferencesManager implements ActionListener
{
	public static final String PRINT_INRO_KEYWORD = "print.introduction";
	public static final String PRINT_TABLE_OF_CONTENTS_KEYWORD = "print.table.of.contents";
	public static final String PRINT_PACKAGE_LIST_KEYWORD = "print.package.list";
	public static final String PRINT_UML_KEYWORD = "print.uml";
	public static final String PRINT_SHORTENED_SOURCE = "print.shortened.source";
	
	public static final String REPORT_TITLE_KEYWORD = "report.title";
	public static final String REPORT_AUTHOR_KEYWORD = "report.author";
	public static final String REPORT_DATE_KEYWORD = "report.date";
	public static final String USE_CURRENT_DATE_KEYWORD = "use.current.date";
	public static final String REPORT_DESCRIPTION_KEYWORD = "report.description";
	
	protected static final boolean printIntroByDefault = true;
	protected static final boolean printTableOfContentsByDefault = true;
	protected static final boolean printPackageListByDefault = true;
	protected static final boolean printUMLByDefault = true;
	protected static final boolean printShortenedSourceByDefault = true;
	
	protected static final String defaultTitle = "";
	protected static final String defaultAuthor = "";
	protected static final String defaultDate = "";
	protected static final boolean useCurrentDateByDefault = true;
	protected static final String defaultDescription = "";
	
	protected SingleUMLPrintingPreferencesManager umlManager;
	protected ShortenedSourcePrintingPreferencesManager shortenedSourceManager;
	
	protected JPanel printIntroPanel;
		protected JCheckBox printIntroCheckBox;
		protected JButton introOptionsButton;
	protected JCheckBox printTableOfContentsCheckBox;
	protected JCheckBox printPackageListCheckBox;
	protected JPanel printUMLPanel;
		protected JCheckBox printUMLCheckBox;
		protected JButton umlOptionsButton;
	protected JPanel printShortenedSourcePanel;
		protected JCheckBox printShortenedSourceCheckBox;
		protected JButton shortenedSourceOptionsButton;
	
	protected JPanel introOptionsPanel;
		protected JPanel titlePanel;
			protected JTextField titleField;
		protected JPanel authorPanel;
			protected JTextField authorField;
		protected JPanel datePanel;
			protected JTextField dateField;
			protected JCheckBox useCurrentDateCheckBox;
		protected JPanel descriptionPanel;
			protected JTextArea descripionArea;
	
	
	public AbstractPrintingPreferencesManager()
	{
		umlManager = new SingleUMLPrintingPreferencesManager(this.getClass());
		shortenedSourceManager = new ShortenedSourcePrintingPreferencesManager(this.getClass());
		
		defaultPreferencesList.setProperty(PRINT_INRO_KEYWORD,convertToString(printIntroByDefault));
		defaultPreferencesList.setProperty(PRINT_PACKAGE_LIST_KEYWORD,convertToString(printPackageListByDefault));
		defaultPreferencesList.setProperty(PRINT_SHORTENED_SOURCE,convertToString(printShortenedSourceByDefault));
		defaultPreferencesList.setProperty(PRINT_TABLE_OF_CONTENTS_KEYWORD,convertToString(printTableOfContentsByDefault));
		defaultPreferencesList.setProperty(PRINT_UML_KEYWORD,convertToString(printUMLByDefault));
		defaultPreferencesList.setProperty(REPORT_AUTHOR_KEYWORD,defaultAuthor);
		defaultPreferencesList.setProperty(REPORT_DATE_KEYWORD,defaultDate);
		defaultPreferencesList.setProperty(REPORT_DESCRIPTION_KEYWORD,defaultDescription);
		defaultPreferencesList.setProperty(REPORT_TITLE_KEYWORD,defaultTitle);
		defaultPreferencesList.setProperty(USE_CURRENT_DATE_KEYWORD,convertToString(useCurrentDateByDefault));
		
		//now to make the panels
		printIntroPanel = new JPanel(new BorderLayout());
			printIntroCheckBox = new JCheckBox("Print introduction",resolveBoolean(currentPreferencesList,PRINT_INRO_KEYWORD,printIntroByDefault));
			introOptionsButton = new JButton("Options");
				introOptionsButton.addActionListener(this);
				introOptionsButton.setActionCommand("intro.options");
			printIntroPanel.add(printIntroCheckBox,BorderLayout.WEST);
			printIntroPanel.add(introOptionsButton,BorderLayout.EAST);
			
		printTableOfContentsCheckBox = new JCheckBox("Print table of contents",resolveBoolean(currentPreferencesList,PRINT_TABLE_OF_CONTENTS_KEYWORD,printTableOfContentsByDefault));
		printPackageListCheckBox = new JCheckBox("Print package list",resolveBoolean(currentPreferencesList,PRINT_PACKAGE_LIST_KEYWORD,printPackageListByDefault));
		
		printUMLPanel = new JPanel(new BorderLayout());
			printUMLCheckBox = new JCheckBox("Print UML diagrams",resolveBoolean(currentPreferencesList,PRINT_UML_KEYWORD,printUMLByDefault));
			umlOptionsButton = new JButton("Options");
				umlOptionsButton.addActionListener(this);
				umlOptionsButton.setActionCommand("uml.options");
			printUMLPanel.add(printUMLCheckBox,BorderLayout.WEST);
			printUMLPanel.add(umlOptionsButton,BorderLayout.EAST);

		printShortenedSourcePanel = new JPanel(new BorderLayout());
			printShortenedSourceCheckBox = new JCheckBox("Print shortened source code",resolveBoolean(currentPreferencesList,PRINT_SHORTENED_SOURCE,printShortenedSourceByDefault));
			shortenedSourceOptionsButton = new JButton("Options");
				shortenedSourceOptionsButton.addActionListener(this);
				shortenedSourceOptionsButton.setActionCommand("shortened.source.options");
			printShortenedSourcePanel.add(printShortenedSourceCheckBox,BorderLayout.WEST);
			printShortenedSourcePanel.add(shortenedSourceOptionsButton,BorderLayout.EAST);
		
		titlePanel = new JPanel(new BorderLayout());
			titlePanel.add(new JLabel("Title:  "),BorderLayout.WEST);
			titleField = new JTextField(currentPreferencesList.getProperty(REPORT_TITLE_KEYWORD,defaultTitle),20);
			titlePanel.add(titleField,BorderLayout.EAST);
			
		authorPanel = new JPanel(new BorderLayout());
			authorPanel.add(new JLabel("Author:  "),BorderLayout.WEST);
			authorField = new JTextField(currentPreferencesList.getProperty(REPORT_AUTHOR_KEYWORD,defaultAuthor),20);
			authorPanel.add(authorField,BorderLayout.EAST);
			
		datePanel = new JPanel(new BorderLayout());
			datePanel.add(new JLabel("Date:  "),BorderLayout.WEST);
			dateField = new JTextField(currentPreferencesList.getProperty(REPORT_DATE_KEYWORD,defaultDate),20);
			datePanel.add(dateField,BorderLayout.CENTER);
			useCurrentDateCheckBox = new JCheckBox("Use current date:  ",resolveBoolean(currentPreferencesList,USE_CURRENT_DATE_KEYWORD,useCurrentDateByDefault));
			datePanel.add(useCurrentDateCheckBox,BorderLayout.EAST);
			
		descriptionPanel = new JPanel(new BorderLayout());
			descriptionPanel.add(new JLabel("Description:  "),BorderLayout.WEST);
			descripionArea = new JTextArea(currentPreferencesList.getProperty(REPORT_DESCRIPTION_KEYWORD,defaultDescription),20,20);
			descriptionPanel.add(descripionArea,BorderLayout.EAST);
			
		introOptionsPanel = new JPanel(new BorderLayout());
			JPanel topPanel = new JPanel(new GridLayout(3,1));
				topPanel.add(titlePanel);
				topPanel.add(authorPanel);
				topPanel.add(datePanel);
			introOptionsPanel.add(topPanel,BorderLayout.NORTH);
			introOptionsPanel.add(descriptionPanel,BorderLayout.CENTER);
	}
	
	private static boolean resolveBoolean(Properties prefsList, String key, boolean defaultValue)
	{
		return convertToBoolean(prefsList.getProperty(key,convertToString(defaultValue)));
	}
	
	public ShortenedSourcePrintingPreferencesManager getShortenedSourcePrefsManager()
	{
		return shortenedSourceManager;
	}
	
	public SingleUMLPrintingPreferencesManager getUMLPrefsManager()
	{
		return umlManager;
	}
	
	//These methods get the values from the defaultPreferencesList
	
	public boolean printIntroByDefault()
	{
		return printIntroByDefault;
	}
	
	public boolean printTableOfContentsByDefault()
	{
		return printTableOfContentsByDefault;
	}
	
	public boolean printPackageListByDefault()
	{
		return printPackageListByDefault;
	}
	
	public boolean printUMLDiagramsByDefault()
	{
		return printUMLByDefault;
	}
	
	public boolean printShortenedSourceByDefault()
	{
		return printShortenedSourceByDefault;
	}
	
	public String getDefaultTitle()
	{
		return defaultTitle;
	}
	
	public String getDefaultAuthor()
	{
		return defaultAuthor;
	}
	
	public String getDefaultDate()
	{
		return defaultDate;
	}
	
	public boolean useCurrentDateByDefault()
	{
		return useCurrentDateByDefault;
	}
	
	public String getDefaultDescription()
	{
		return defaultDescription;
	}

	//These methods get values from currentPreferencesList
		
	public boolean printIntroduction()
	{
		return getBooleanProperty(PRINT_INRO_KEYWORD,printIntroByDefault);
	}
	
	public boolean printTableOfContents()
	{
		return getBooleanProperty(PRINT_TABLE_OF_CONTENTS_KEYWORD,printTableOfContentsByDefault);
	}
	
	public boolean printPackageList()
	{
		return getBooleanProperty(PRINT_PACKAGE_LIST_KEYWORD,printPackageListByDefault);
	}
	
	public boolean printUMLDiagrams()
	{
		return getBooleanProperty(PRINT_UML_KEYWORD,printUMLByDefault);
	}
	
	public boolean printShortenedSource()
	{
		return getBooleanProperty(PRINT_SHORTENED_SOURCE,printShortenedSourceByDefault);
	}
	
	public String getTitle()
	{
		return getProperty(REPORT_TITLE_KEYWORD,defaultTitle);
	}
	
	public String getAuthor()
	{
		return getProperty(REPORT_AUTHOR_KEYWORD,defaultAuthor);
	}
	
	public String getDate()
	{
		return getProperty(REPORT_DATE_KEYWORD,defaultDate);
	}
	
	public boolean useCurrentDate()
	{
		return getBooleanProperty(USE_CURRENT_DATE_KEYWORD,useCurrentDateByDefault);
	}
	
	public String getDescription()
	{
		return getProperty(REPORT_DESCRIPTION_KEYWORD,defaultDescription);
	}
	
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("intro.options"))
		{
			JFrame frame = new JFrame("Introduction Options");
			JPanel mainPanel = new JPanel(new BorderLayout());
				mainPanel.add(introOptionsPanel);
				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					JButton cancelButton = new JButton();
		}
		else if (event.getActionCommand().equals("uml.options"))
		{
		}
		else if (event.getActionCommand().equals("shortened.source.options"))
		{
		}
	}
	
	public static class SingleUMLPrintingPreferencesManager extends AbstractShortenedPreferencesManager
	{
		private Class outerClass;
		
		protected static final boolean shortenJavaWordsByDefault = true;
		protected static final boolean shortenOtherWordsByDefault = false;
		
		public SingleUMLPrintingPreferencesManager(Class cl)
		{
			super(shortenJavaWordsByDefault, "Shorten Java terms for UML diagrams", shortenOtherWordsByDefault, "Shorten non-Java terms for UML diagrams");
			outerClass = cl;
		}
		
		public String getFileName()
		{
			return SystemsManager.getClassDescriptorPreferencesDirectory()+System.getProperty("file.separator")+getClass().getName()+"#"+outerClass.getName()+".dat";
		}
	}
	
	public static class ShortenedSourcePrintingPreferencesManager extends AbstractShortenedPreferencesManager
	{
		private Class outerClass;
		
		protected static final boolean shortenJavaWordsByDefault = true;
		protected static final boolean shortenOtherWordsByDefault = false;
		
		public ShortenedSourcePrintingPreferencesManager(Class cl)
		{
			super(shortenJavaWordsByDefault, "Shorten Java terms for UML diagrams", shortenOtherWordsByDefault, "Shorten non-Java terms for UML diagrams");
			outerClass = cl;
		}
		
		public String getFileName()
		{
			return SystemsManager.getClassDescriptorPreferencesDirectory()+System.getProperty("file.separator")+getClass().getName()+"#"+outerClass.getName()+".dat";
		}
	}
}
