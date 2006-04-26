/*
 * File:  AbstractPreferencesManager.java
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JPanel;

import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * @author Dominic Kramer
 */
public abstract class AbstractPreferencesManager
{
	protected final String filename = SystemsManager.getClassDescriptorPreferencesDirectory()+System.getProperty("file.separator")+getClass().getName()+".dat";

	protected Properties defaultPreferencesList;
	protected Properties currentPreferencesList;
	protected JPanel prefsPanel;
	
	public AbstractPreferencesManager()
	{
		defaultPreferencesList = new Properties();
		try
		{
			currentPreferencesList = loadFromFile(defaultPreferencesList,filename);
		}
		catch(FileNotFoundException e)
		{
			System.out.println("File "+filename+" could not be found.");
			System.out.println("    User preferences could not be loaded .... Using defaults");
			currentPreferencesList = new Properties(defaultPreferencesList);
		}
		catch(IOException e)
		{
			SystemsManager.printStackTrace(e);
			currentPreferencesList = new Properties(defaultPreferencesList);
		}
		prefsPanel = new JPanel();
		
		System.out.println("currentList:");
		Enumeration en = currentPreferencesList.elements();
		while(en.hasMoreElements())
		{
			System.out.println("    "+en.nextElement());
		}
		System.out.println("/currentList:");
	}
	
	/** The the absolute filename for the preferences file. */
	public String getFileName()
	{
		return filename;
	}

	/** 
	 * Get the default preferences. 
	 * @return A reference to the field 'defaultPropertyList'.
	 */
	public Properties getDefaultPreferences()
	{
		return defaultPreferencesList;
	}
	
	/**
	 * Get the current preferences.
	 * @return A reference to the field 'currentPropertyList'.
	 */
	public Properties getCurrentPreferences()
	{
		return currentPreferencesList;
	}
	
	/**
	 * Loads the properties list from the file fileName.  This method is used for loading 
	 * a property list in constructors.  It is also used in the method loadFile().
	 * @param defaultList The default list to use.
	 * @param fileName The filename from which the data is read.
	 * @return The Properties list loaded from the file.
	 */
	protected static Properties loadFromFile(Properties defaultList, String fileName) throws IOException, FileNotFoundException
	{
		Properties prop = new Properties(defaultList);
		FileInputStream stream = new FileInputStream(fileName);
		prop.load(stream);
		stream.close();
		return prop;
	}
		
	//----These methods handle reading and writing from the preferences file----//
	/** 
	 * Loads the preferences from the file specified by getFileName() into the 
	 * current preferences list.
	 * @return True if the loading was successful.  False if the loading was not 
	 * successful, in which case the previous value of 'currentPreferencesList' is 
	 * returned.
	 */
	public boolean loadFile()
	{
		boolean successfullyLoaded = true;
		try
		{
			Properties prop = loadFromFile(defaultPreferencesList, getFileName());
			currentPreferencesList = prop;
		}
		catch (IOException e)
		{
			SystemsManager.printStackTrace(e);
			successfullyLoaded = false;
		}
		return successfullyLoaded;
	}

	/** 
	 * Save the preferences specified by the field 'currentPreferencesList' to the file specified by getFileName().
	 * @return True if the save was successful and false otherwise.
	 */
	public boolean saveToFile()
	{
		boolean successful = true;
		try
		{
			FileOutputStream stream = new FileOutputStream(getFileName());
			currentPreferencesList.store(stream, null);
			stream.close();
		}
		catch (Throwable t)
		{
			successful = false;
			SystemsManager.printStackTrace(t);
		}
		return successful;
	}
	
	//----These methods handle getting a JPanel holding the options for the preferences----//
	//----They also handle getting preferences from a JPanel---------------------------------------//
	/**
	 * Gets a JPanel holding options for the preferences.  The graphical components on 
	 * the JPanel are selected to meet the specifications of the preferences specified by 
	 * the current preferences as specified by getCurrentPreferences().
	 */
	public JPanel getJPanel()
	{
		return prefsPanel;
	}
	
//	/** Get the preferences as specified by the graphical components selected on the JPanel. */
//	public void setPreferencesAsCurrentFromPanel()
//	{
//		saveToFile();
//	}
	
	public void setProperty(String key, String value)
	{
		currentPreferencesList.setProperty(key,value);
	}
	
	public String getProperty(String key)
	{
		return currentPreferencesList.getProperty(key);
	}
	
	public String getProperty(String key, String defaultValue)
	{
		return currentPreferencesList.getProperty(key,defaultValue);
	}
	
	public void setBooleanProperty(String key, boolean value)
	{		
		currentPreferencesList.setProperty(key,convertToString(value));
	}
	
	public boolean getBooleanProperty(String key, boolean defaultValue)
	{
		return convertToBoolean(currentPreferencesList.getProperty(key,convertToString(defaultValue)));
	}
	
	public int getIntegerPropety(String key)
	{
		return convertToInt(currentPreferencesList.getProperty(key));
	}
	
	public int getIntegerPropety(String key, int defaultValue)
	{
		return convertToInt(currentPreferencesList.getProperty(key,convertToString(defaultValue)));
	}
	
	public void setIntegerProperty(String key, int value)
	{
		currentPreferencesList.setProperty(key,convertToString(value));
	}
	
	//----These methods are used to switch from Strings to booleans.  A key has to be a String.----// 
	//----However, it is easy to use the key if it is a boolean.--------------------------------------------------//
	/**
	 * Convert a String into a boolean.
	 * @return True if str equals 'true' and false otherwise.
	 */
	public static boolean convertToBoolean(String str)
	{
		return str.equals(convertToString(true));
	}
	
	/**
	 * Convert a boolean to a String.
	 * @return 'true' if bol is true and 'fasle' if bol 
	 */
	public static String convertToString(boolean bol)
	{
		return (new Boolean(bol)).toString();
	}
	
	public static int convertToInt(String str)
	{
		return (new Integer(str)).intValue();
	}
	
	public static String convertToString(int num)
	{
		return String.valueOf(num);
	}
}
