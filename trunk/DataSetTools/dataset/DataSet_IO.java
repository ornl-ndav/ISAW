/*
 * File:  DataSet_IO.java
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 * $Log$
 * Revision 1.3  2002/11/27 23:14:06  pfpeterson
 * standardized header
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
