/*
 * File:  AbstractShortenedPreferencesManager.java
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

import javax.swing.JCheckBox;

/**
 * Classes which extend this class have the following keys already defined:<br>
 * SHORTEN_JAVA_KEYWORD = 'shorten.java.interface'<br>
 * SHORTEN_OTHER_KEYWORD = 'shorten.other.interface'<br>
 * Also, the two JCheckBoxes are already defined:<br>
 * shortenJavaBox<br>
 * shortenOtherBox<br>
 * When invoking the constructor, the parameters passed to the constructor are used 
 * to customize the values of the shortening java words by default, shortening non-java 
 * words by default, and whether the checkboxes should be selected.
 * @author Dominic Kramer
 */
public abstract class AbstractShortenedPreferencesManager extends AbstractColorfulPreferencesManager
{
	public static final String SHORTEN_JAVA_KEYWORD = "shorten.java.interface";
	public static final String SHORTEN_OTHER_KEYWORD = "shorten.other.interface";
	
	protected static boolean shortenJavaWordsForInterfacesByDefault;
	protected static boolean shortenOtherWordsForInterfacesByDefault;
	
	protected JCheckBox shortenJavaBox;
	protected JCheckBox shortenOtherBox;
	
	protected boolean shortenJavaBoxSavedState;
	protected boolean shortenOtherBoxSavedState;
	
	/** This forces sub-classes to use the other constructor. */
	private AbstractShortenedPreferencesManager() {}
	
	/**
	 * This constructor defines the properties for whether or not to shorten java and non-java words by default.  The Properties 
	 * list, 'defaultPreferencesList' is assigned the corresponding values specified using the keys SHORTEN_JAVA_KEYWORD 
	 * and SHORTEN_OTHER_KEYWORD.  Also, the two JCheckBoxes, 'shortenJavaBox' and 'shortenOtherBox' are 
	 * instantiated.
	 * @param shortenJavaWordsByDefault      True if java words should be shortened by default.
	 * @param shortenJavaText                          The text shown on the JCheckBox, shortenJavaBox.
	 * @param shortenOtherWordsByDefault     True if non-java words should be shortened by default.
	 * @param shortenOtherText                        The text shown on the JCheckBox, shortenOtherBox.
	 */
	public AbstractShortenedPreferencesManager(boolean shortenJavaWordsByDefault, String shortenJavaText, boolean shortenOtherWordsByDefault, String shortenOtherText)
	{
		super();
		defaultPreferencesList.setProperty(SHORTEN_JAVA_KEYWORD, convertToString(shortenJavaWordsByDefault));
		defaultPreferencesList.setProperty(SHORTEN_OTHER_KEYWORD, convertToString(shortenOtherWordsByDefault));
		
		shortenJavaWordsForInterfacesByDefault = shortenJavaWordsByDefault;
		shortenOtherWordsForInterfacesByDefault = shortenOtherWordsByDefault;
		
		boolean shortenJava = convertToBoolean(currentPreferencesList.getProperty(SHORTEN_JAVA_KEYWORD, convertToString(shortenJavaWordsByDefault)));
		boolean shortenOther = convertToBoolean(currentPreferencesList.getProperty(SHORTEN_OTHER_KEYWORD, convertToString(shortenOtherWordsByDefault)));
		
		shortenJavaBox = new JCheckBox(shortenJavaText,shortenJava);
		shortenOtherBox = new JCheckBox(shortenOtherText,shortenOther);
		
		shortenJavaBoxSavedState = shortenJava;
		shortenOtherBoxSavedState = shortenOther;
		
		System.out.println("shortenJava = "+shortenJava);
		System.out.println("shortenOther="+shortenOther);
	}
	
	public static boolean shortenJavaTermsForInterfacesByDefault()
	{
		return shortenJavaWordsForInterfacesByDefault;
	}
	
	public static boolean shortenNonJavaTermsForInterfacesByDefault()
	{
		return shortenOtherWordsForInterfacesByDefault;
	}
	
	public boolean getShortenJavaTermsForInterfaces()
	{
		return getBooleanProperty(SHORTEN_JAVA_KEYWORD,shortenJavaWordsForInterfacesByDefault);
	}
	
	public void setShortenJavaTermsForInterfaces(boolean bol)
	{
		setBooleanProperty(SHORTEN_JAVA_KEYWORD, bol);
	}
	
	public boolean getShortenNonJavaTermsForInterfaces()
	{
		return getBooleanProperty(SHORTEN_OTHER_KEYWORD,shortenOtherWordsForInterfacesByDefault);
	}
	
	public void setShortenNonJavaTermsForInterfaces(boolean bol)
	{
		setBooleanProperty(SHORTEN_OTHER_KEYWORD,bol);
	}

	public void saveCheckBoxState()
	{
		shortenJavaBoxSavedState = getShortenJavaTermsForInterfaces();
		shortenOtherBoxSavedState = getShortenNonJavaTermsForInterfaces();
	}
	
	public void restoreCheckBoxState()
	{
		shortenJavaBox.setSelected(shortenJavaBoxSavedState);
		shortenOtherBox.setSelected(shortenOtherBoxSavedState);
	}
}
