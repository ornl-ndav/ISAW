/*
 * File:  GsasFileFilter.java
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.2  2002/11/27 23:15:00  pfpeterson
 * standardized header
 *
 */
package DataSetTools.gsastools;

import javax.swing.*;
import java.io.*;

/**
 * Filters out SDDS files, I guess!!
 */
public class GsasFileFilter extends javax.swing.filechooser.FileFilter{
    public boolean accept(File f){
        if(f==null) return false;
        if(f.isDirectory()) return true;

        String suffix = getSuffix(f);
        if( suffix==null ) return false;
        suffix=suffix.toUpperCase();
        if( suffix.equals("GSA") ) return true;
        if( suffix.equals("GDA") ) return true;
        if( suffix.equals("DAT") ) return true;

	return false;
    }

    /**
     * gets the description of what files this filter shows
     */ 
    public String getDescription(){
	return "GSAS (*.gsa, *.gda, *.dat)";
    }
    
    /**
     * returns a file extension
     */
    public String getSuffix(File f){
	String s = f.getPath(), suffix = null;
	int i = s.lastIndexOf('.');
	if (i>0 && i<s.length() -1)
	    suffix = s.substring(i+1).toLowerCase();
	return suffix;
    }
}
