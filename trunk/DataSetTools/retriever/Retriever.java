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
 *  Revision 1.7  2001/08/03 21:37:53  dennis
 *  Improved the docs and now allow numDataSets() to return a negative
 *  value as an error code.
 *
 *  Revision 1.6  2001/07/30 18:47:28  dennis
 *  Minor documentation improvements.
 *
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
 * Base class for objects that retrieve DataSet objects from files, or via 
 * a network connection.  Derived classes for particular types of data
 * sources must actually implement the methods to get specified DataSets
 * and their types.
 */

public abstract class Retriever implements Serializable
{
    public static final int  INVALID_DATA_SET      = 0;
    public static final int  MONITOR_DATA_SET      = 1;
    public static final int  HISTOGRAM_DATA_SET    = 2;
    public static final int  PULSE_HEIGHT_DATA_SET = 3;

    protected String data_source_name = null;


    /* ------------------------ Constructor -------------------------- */
    /**
     * Construct the retriever for the specified source name.
     *
     * @param data_source_name   This identifies the data source.  For file
     *                           data retrievers, this should be the fully 
     *                           qualified file name
     */
    public Retriever( String data_source_name )
    {
      this.data_source_name = data_source_name;
    }


    /* ------------------------ numDataSets -------------------------- */
    /**
     * Get the number of distinct DataSets that can be obtained from the
     * current data source.
     *
     *  @return The number of distinct DataSets available.  This function
     *          may return values < 0 as an error code if there are no
     *          DataSets available.
     */
    public abstract int numDataSets();

    
    /* -------------------------- getDataSet ---------------------------- */
    /**
     * Get the specified DataSet from the current data source.
     *
     * @param data_set_num  The number of the DataSet in this runfile
     *                      that is to be read from the runfile.  data_set_num
     *                      must be between 0 and numDataSets()-1
     *
     * @return The specified DataSet, if it exists, or null if no such
     *         DataSet exists.
     */
    public abstract DataSet getDataSet( int data_set_num );


    /* ---------------------------- getType ------------------------------ */
    /**
     *  Get the type code of a particular DataSet in this runfile.
     *  The type codes include:
     *
     *     Retriever.INVALID_DATA_SET
     *     Retriever.MONITOR_DATA_SET
     *     Retriever.HISTOGRAM_DATA_SET
     *     Retriever.PULSE_HEIGHT_DATA_SET
     *
     *  @param  data_set_num  The number of the DataSet in this runfile whose
     *                        type code is needed.  data_set_num must be between
     *                        0 and numDataSets()-1
     *
     *  @return the type code for the specified DataSet.
     */
    public abstract int getType( int data_set_num );
}

