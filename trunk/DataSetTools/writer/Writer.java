/*
 * File:  Writer.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.3  2002/11/27 23:26:45  pfpeterson
 *  standardized header
 *
 */

package DataSetTools.writer;

import  DataSetTools.dataset.*;
import  java.io.*;

/**
 * Root class for objects that write DataSet objects to files. 
 * Derived classes for particular types of files must actually implement 
 * the methods write the data sets.
 */

public abstract class Writer implements Serializable
{
    protected String data_destination_name = null;


    /**
     * Construct the Writer for the specified destination name.
     *
     * @param data_destination_name  This identifies the data destination.  
     *                               For file data writers, this should be 
     *                               the fully qualified file name.
     */

    public Writer( String data_destination_name )
    {
      this.data_destination_name = data_destination_name;
    }

    
    /**
     * Send the specified array of data sets to the current data destination.
     * If an array of DataSets includes both monitor and histogram DataSets
     * the recommended convention is to list the monitor DataSet in the array
     * before the list of histogram DataSets to which it applies.  That is
     * M1, H1, H2, H3, M2, H3, H4 would be interpreted to mean that M1 is
     * the monitor DataSet for histograms H1, H2, H3 and the M2 is the monitor
     * DataSet for histograms H3 and H4.
     */
    public abstract void writeDataSets( DataSet ds[] );

}
