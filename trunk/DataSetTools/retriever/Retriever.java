/*
 * File:  Retriever.java
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.5  2001/04/25 21:57:55  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.4  2001/01/29 21:12:05  dennis
 *  Now uses CVS revision numbers.
 *
 *  Revision 1.3  2000/10/10 20:24:02  dennis
 *  Added constant for PULSE_HEIGHT_DATASET
 *
 *  Revision 1.2  2000/07/10 22:49:45  dennis
 *  Now Using CVS 
 *
 *  Revision 1.3  2000/05/11 16:19:12  dennis
 *  added RCS logging
 *
 */

package DataSetTools.retriever;

import  DataSetTools.dataset.*;
import  java.io.*;

/**
 * Root class for operators that retrieve DataSet objects from files, or
 * directly from hardware.  Derived classes for particular types of data
 * sources must actually implement the methods to get specified data sets
 * and their types.
 */

public abstract class Retriever implements Serializable
{
    public static final int  INVALID_DATA_SET      = 0;
    public static final int  MONITOR_DATA_SET      = 1;
    public static final int  HISTOGRAM_DATA_SET    = 2;
    public static final int  PULSE_HEIGHT_DATA_SET = 3;

    /** The dataSource from which the retriever retrieves data */
    protected String data_source_name = null;


    /**
     * Construct the retriever for the specified source name.
     *
     * @param dataSourceName   This identifies the data source.  For file
     *                         data retrievers, this will be the fully 
     *                         qualified file name
     */

    public Retriever( String data_source_name )
    {
      this.data_source_name = data_source_name;
    }

    /**
     * Get the number of distinct data sets that can be obtained from the
     * current data source.
     */

    public abstract int numDataSets();
    
    /**
     * Get the specified data set from the current data source.
     *
     */
    public abstract DataSet getDataSet( int data_set_num );

    /**
     * Get the type of the specified data set from the current data source.
     * The type is an integer flag that indicates whether the data set contains
     * monitor data or data from other detectors.
     */

    public abstract int getType( int data_set_num );
}

