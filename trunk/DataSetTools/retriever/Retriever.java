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
 *  Revision 1.10  2002/11/27 23:23:16  pfpeterson
 *  standardized header
 *
 *  Revision 1.9  2002/10/10 21:10:50  dennis
 *  Added method getDataSet( data_set_num, ids[] ).  At this base class
 *  level, it just calls getDataSet( data_set_num ), and issues a warning
 *  message, so that derived classes can still function without implementing
 *  this method.  All derived classes should eventually implement this.
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

    public static boolean  debug_retriever = false;

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

  
    /* -------------------------- getDataSet ---------------------------- */
    /**
     *  Get a DataSet from the current data source containing only the the 
     *  specified group IDs from within the specified DataSet.
     *  NOTE: The list of group IDs must be in increasing order.
     *
     *  @param  data_set_num  The number of the DataSet in this runfile
     *                        that is to be read from the runfile.  data_set_num
     *                        must be between 0 and numDataSets()-1
     *
     *  @param  ids           The list of group IDs from the specified DataSet
     *                        that are to be read from the runfile and returned
     *                        in the DataSet, in increasing order.
     *
     *  @return a DataSet containing only the specified groups, if the 
     *          data_set_num and ID list specify a non-empty set of 
     *          Data blocks, or null otherwise.
     */
    public DataSet getDataSet( int data_set_num, int ids[] )
    {
      System.out.println("Warning: getDataSet( data_set_num, ids ) not " +
                         "implemented in this retriever, defaulting " +
                         "getting all ids " );
      return getDataSet( data_set_num );
    }


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

