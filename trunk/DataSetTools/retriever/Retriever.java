/*
 * @(#)Retriever.java     0.1  99/07/06  Dennis Mikkelson
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.3  2000/10/10 20:24:02  dennis
 *  Added constant for PULSE_HEIGHT_DATASET
 *
 *  Revision 1.2  2000/07/10 22:49:45  dennis
 *  July 10, 2000 version...many changes
 *
 *  Revision 1.3  2000/05/11 16:19:12  dennis
 *  added RCS logging
 *
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

