/*
 * File:  CompressedDataSet.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 *  $Log$
 *  Revision 1.3  2003/07/16 22:36:54  dennis
 *  Added seralVersionUID, so that small changes can be made to this
 *  class without destroying compatibility.  This will break communication
 *  capability with previous versions of the Live/FileDataServers, but
 *  will allow longer term compatibility with future releases.
 *
 *  Revision 1.2  2003/07/09 21:24:31  dennis
 *  Added test main program.
 *
 *  Revision 1.1  2003/02/21 18:35:14  dennis
 *  Initial version of object that keeps a DataSet in compressed form.
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.util.*;
import DataSetTools.retriever.*;

/**
 *  This class records a compressed form of an entire DataSet in an array
 *  of bytes in GZip format. 
 */  

public class CompressedDataSet implements Serializable
{
  // NOTE: any field that is static or transient is NOT serialized.
  //
  // CHANGE THE "serialVersionUID" IF THE SERIALIZATION IS INCOMPATIBLE WITH
  // PREVIOUS VERSIONS, IN WAYS THAT CAN NOT BE FIXED BY THE readObject()
  // METHOD.  SEE "IsawSerialVersion" COMMENTS BELOW.  CHANGING THIS CAUSES
  // JAVA TO REFUSE TO READ DIFFERENT VERSIONS.
  //
  public  static final long serialVersionUID = 1L;

  // NOTE: The following fields are serialized.  If new fields are added that
  //       are not static, reasonable default values should be assigned in the
  //       readObject() method for compatibility with old servers, until the
  //       servers can be updated.

  private int IsawSerialVersion = 1;         // CHANGE THIS WHEN ADDING OR
                                             // REMOVING FIELDS, IF
                                             // readObject() CAN FIX ANY
                                             // COMPATIBILITY PROBLEMS


  private byte   ds_bytes[] = null;         // contains the compressed DataSet
  private String ds_title   = "UNKNOWN";


  /**
   *  Store a compressed form of the specified DataSet in an array of bytes.
   *  The title is also stored separately, so that it can be obtained without
   *  uncompressing the DataSet.
   *
   *  @param ds   The DataSet that is recorded in compressed form.
   */
  public CompressedDataSet( DataSet ds )
  {
    SerialGZip zipper = new SerialGZip();
    ds_bytes = zipper.compress( ds, 3000000 );  

    if ( ds_bytes != null )
      ds_title = ds.getTitle();
  } 

  /**
   *  Return a clone of the original DataSet obtained by uncompressing
   *  compressed form of the DataSet.
   *
   *  @return a new DataSet with the same contents as the compressed DataSet.
   */
  public DataSet getDataSet()
  {
    SerialGZip zipper = new SerialGZip();

    Object obj = zipper.inflate( ds_bytes );
  
    if ( obj instanceof DataSet )
      return (DataSet)obj;
    else
      return null;
  }

  /**
   *  Get the title of the original DataSet, without uncompressing the 
   *  whole DataSet.
   *
   *  @return  the tile of the compressed DataSet
   */
  public String getTitle()
  {
    return ds_title;
  }


  /**
   *  Get the size of the array of bytes that encode this DataSet in GZipForm
   *
   *  @return  the size in bytes of the compressed DataSet.
   */
  public int size()
  {
    if ( ds_bytes == null )
      return 0;

    return ds_bytes.length;
  }


  /**
   *  Get a copy of the array of bytes that encode this DataSet in GZipForm
   *
   *  @return  a copy of the array of bytes holding the compressed DataSet.
   */
  public byte[] getByteArray()
  {
    if ( ds_bytes == null )
      return null;

    byte new_bytes[] = new byte[ ds_bytes.length ];
    System.arraycopy( ds_bytes, 0, new_bytes, 0, ds_bytes.length );
    return ds_bytes;
  }

/* -----------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

/* ---------------------------- readObject ------------------------------- */
/**
 *  The readObject method is called when objects are read from a serialized
 *  ojbect stream, such as a file or network stream.  The non-transient and
 *  non-static fields that are common to the serialized class and the
 *  current class are read by the defaultReadObject() method.  The current
 *  readObject() method MUST include code to fill out any transient fields
 *  and new fields that are required in the current version but are not
 *  present in the serialized version being read.
 */

  private void readObject( ObjectInputStream s ) throws IOException,
                                                        ClassNotFoundException
  {
    s.defaultReadObject();               // read basic information

    if ( IsawSerialVersion != 1 )
    {
      System.out.println("Warning:Data IsawSerialVersion != 1");
                                         // put code here to "fix" the problem
                                         // with different versions if possible.
    }

                                         // set meaningful values for transient
                                         // fields, if any, here.
  }


  /* ----------------------------- main --------------------------------- */
  /** 
   *  main program for test purposes
   */
  static public void main( String args[] )
  { 
    String file_name = null;
    String default_name = "/usr/local/ARGONNE_DATA/SCD_QUARTZ/scd06496.run";

    if ( args.length > 0 )
      file_name = args[0];
    else
      file_name = default_name;

    RunfileRetriever rr = new RunfileRetriever(file_name);
  
    DataSet ds = rr.getFirstDataSet( Retriever.HISTOGRAM_DATA_SET );
    
    CompressedDataSet comp_ds = new CompressedDataSet( ds );
    System.out.println("Compressed DataSet size = " + comp_ds.size() );

    Data d;
    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      d = ds.getData_entry(i);
      float y[] = d.getY_values();
      for ( int j = 0; j < y.length; j++ )
        if ( y[j]  < 3 )
          y[j] = 0;                                  // set background to zero
    }   

    CompressedDataSet comp_ds2 = new CompressedDataSet( ds );
    System.out.println("Reduced, compressed DataSet size = " + comp_ds2.size());
  }

} 

