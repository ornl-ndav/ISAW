/*
 * File:  AbstractWindowPreferencesManager.java
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

import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author Dominic Kramer
 */
public abstract class AbstractWindowPreferencesManager extends AbstractPreferencesManager
{
	public static final String WINDOW_WIDTH_KEYWORD = "window.width";
	public static final String WINDOW_HEIGHT_KEYWORD = "window.height";
	public static final String WINDOW_X_LOCATION = "window.x.location";
	public static final String WINDOW_Y_LOCATION = "window.y.location";
	
	protected static final int defaultWindowWidth = 200;
	protected static final int defaultWindowHeight = 200;
	protected static final int defaultXLocation = 10;
	protected static final int defaultYLocation = 10;
	
	protected SpinnerNumberModel heightModel;
	protected JSpinner heightNumberSpinner;
	protected SpinnerNumberModel widthModel;
	protected JSpinner widthNumberSpinner;
	protected SpinnerNumberModel xLocationModel;
	protected JSpinner xLocationNumberSpinner;
	protected SpinnerNumberModel yLocationModel;
	protected JSpinner yLocationNumberSpinner;
	
	public AbstractWindowPreferencesManager()
	{
		super();
		defaultPreferencesList.setProperty(WINDOW_WIDTH_KEYWORD,convertToString(defaultWindowWidth));
		defaultPreferencesList.setProperty(WINDOW_HEIGHT_KEYWORD,convertToString(defaultWindowHeight));
		defaultPreferencesList.setProperty(WINDOW_X_LOCATION,convertToString(defaultXLocation));
		defaultPreferencesList.setProperty(WINDOW_Y_LOCATION,convertToString(defaultYLocation));
		
		int value = convertToInt(currentPreferencesList.getProperty(WINDOW_HEIGHT_KEYWORD,convertToString(defaultWindowHeight)));
		heightModel = new SpinnerNumberModel();
		heightModel.setMinimum(new Integer(1));
		heightModel.setValue(new Integer(value));
		heightNumberSpinner = new JSpinner(heightModel);
		
		value = convertToInt(currentPreferencesList.getProperty(WINDOW_WIDTH_KEYWORD,convertToString(defaultWindowWidth)));
		widthModel = new SpinnerNumberModel();
		widthModel.setMinimum(new Integer(1));
		widthModel.setValue(new Integer(value));
		widthNumberSpinner = new JSpinner(widthModel);
		
		value = convertToInt(currentPreferencesList.getProperty(WINDOW_X_LOCATION,convertToString(defaultXLocation)));
		xLocationModel = new SpinnerNumberModel();
		xLocationModel.setMinimum(new Integer(1));
		xLocationModel.setValue(new Integer(value));
		xLocationNumberSpinner = new JSpinner(xLocationModel);
		
		value = convertToInt(currentPreferencesList.getProperty(WINDOW_Y_LOCATION,convertToString(defaultYLocation)));
		yLocationModel = new SpinnerNumberModel();
		yLocationModel.setMinimum(new Integer(1));
		yLocationModel.setValue(new Integer(value));
		yLocationNumberSpinner = new JSpinner(yLocationModel);
	}
	
	public Dimension getDefaultLocation()
	{
		return (new Dimension(defaultXLocation,defaultYLocation));
	}
	
	public Dimension getDefaultSize()
	{
		return new Dimension(defaultWindowWidth,defaultWindowHeight);
	}
	
	public int getWidth()
	{
		return getIntegerPropety(WINDOW_WIDTH_KEYWORD,defaultWindowWidth);
	}
	
	public int getHeight()
	{
		return getIntegerPropety(WINDOW_HEIGHT_KEYWORD,defaultWindowHeight);
	}
	
	public int getXLocation()
	{
		return getIntegerPropety(WINDOW_X_LOCATION,defaultXLocation);
	}
	
	public int getYLocation()
	{
		return getIntegerPropety(WINDOW_Y_LOCATION,defaultYLocation);
	}
	
	public Dimension getLocation()
	{
		return (new Dimension(getXLocation(),getYLocation()));
	}
	
	public Dimension getSize()
	{
		return (new Dimension(getWidth(),getHeight()));
	}
}
