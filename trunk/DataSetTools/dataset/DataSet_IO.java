/*
 * @(#)DataSet_IO.java   0.1   2000/6/12   Dennis Mikkelson
 *
 * Load/restore DataSets to/from files using object serialization
 *
 * $Log$
 * Revision 1.1  2000/07/10 22:21:09  dennis
 * Serialized I/O for DataSets.  This is needed since the DataSet contains a list of observers, all observers are also serialized if serialization is done directly.
 *
 */

package DataSetTools.dataset;

import DataSetTools.dataset.*;
import DataSetTools.util.*;
import java.io.*;
import java.util.zip.*;

/**
 *  This class will load/restore DataSets to/from files using object
 *  serialization.  
 *
 */

public final class DataSet_IO implements Serializable
{
  /**
   * Store a DataSet as a serialized object without storing the list of
   * observers.  This routine carries out the following steps:
   *   1. get a copy of the list of observers of the DataSet
   *   2. clear all observers from the DataSet's list of observers
   *   3. save the DataSet as a serialized object
   *   4. set the list of observers of the DataSet back into the DataSet
   *
   *  @param   ds        The DataSet to save without the list of observers
   *  @param   file_name The file that DataSet is to be saved to
   *
   *  @return  This returns true if the DataSet was successfully saved to
   *           the specified file.
   */
  public static boolean SaveDataSet( DataSet ds, String file_name )
  {
    IObserverList observer_list = null;

    if ( ds == null )
      return false;

    observer_list = ds.getIObserverList();
    ds.deleteIObservers();
    boolean done_ok; 

    try
    {
      FileOutputStream fos   = new FileOutputStream( file_name );
      GZIPOutputStream gout  = new GZIPOutputStream( fos );
      ObjectOutputStream oos = new ObjectOutputStream( gout );

      oos.writeObject( ds );
      oos.close();
      done_ok = true;
    }
    catch ( Exception e )
    {
      System.out.println("ERROR writing file........." + e);
      done_ok = false;
    }
    ds.setIObserverList( observer_list );
    return done_ok;
  }

  
  /**
   * Load a DataSet that was previously saved as a serialized object.
   *
   *  @param   file_name The file that DataSet is to be loaded from 
   *
   *  @return  This returns the DataSet if it was successfully loaded from
   *           the specified file and returns null otherwise.
   */

  public static DataSet LoadDataSet( String file_name )
  {
    DataSet ds = null;
    try
    {
      FileInputStream fis   = new FileInputStream( file_name );
      GZIPInputStream gin   = new GZIPInputStream( fis );
      ObjectInputStream ois = new ObjectInputStream( gin );

      ds = (DataSet)ois.readObject();
      ois.close();
    }
    catch ( Exception e )
    {
      System.out.println("ERROR reading file:" + file_name );
      System.out.println("Exception is" + e);
    }
    return ds;
  }

}
