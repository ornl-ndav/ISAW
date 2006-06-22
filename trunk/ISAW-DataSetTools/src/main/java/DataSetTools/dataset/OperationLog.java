/*
 * File:  OperationLog.java
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
 *  Revision 1.9  2004/03/15 03:28:08  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.8  2002/11/27 23:14:06  pfpeterson
 *  standardized header
 *
 *  Revision 1.7  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.6  2002/06/14 21:13:10  rmikk
 *  Implements IXmlIO interface
 *
 */

package  DataSetTools.dataset;

import gov.anl.ipns.Util.File.*;

import java.util.Vector;
import java.io.*;

/**
 *  Log of operations performed on a DataSet.
 */

public class OperationLog implements Serializable, IXmlIO
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
  private Vector log;

  /**
   *  Construct an empty log of operations
   */
  public OperationLog( )
  {
    this.log = new Vector();
  }

  /**
   *  Add a one line long log entry to the operation log
   *
   *  @param  str  The text string to be added to the operation log
   */
  public void addEntry( String str )
  {
    log.addElement( str ); 
  }

  /**
   *  Get the number of log entries
   *
   *  @return the number of entries that were made in the log
   */
  public int numEntries( )
  {
    return log.size();
  }

  /**
   *  Get the log entry at the specified location in the log
   *
   *  @param  i  the postion in the log of the entry that is to be obtained
   *
   *  @return  The log entry in posiiton "i"
   */
  public String getEntryAt( int i )
  {
    return (String)(this.log.elementAt(i));
  }


  /**
   *  Write all of the strings from the log to System.out
   *
   */
   public void Print()
   {
     for ( int i = 0; i < this.log.size(); i++ )
       System.out.println( (String)this.log.elementAt( i ));
   }


  /**
  * Implements the IXmlIO interface to let the OperationLog write itself
  *
  * @param stream  the OutputStream to which the data is written in xml format
  * @param mode  either IXmlIO.BASE64 to write spectra in Base63 or
  *               IXmlIO.NORMAL to write it in ASCII
  *
  * @return  true if successful otherwise false
  */
  public boolean XMLwrite( OutputStream stream, int mode)
  { StringBuffer sb = new StringBuffer( 600);
    sb.append("<OperationLog   size=\""+log.size()+"\" >\n");
    for( int i=0; i< log.size(); i++)
    { sb.append("<data>");
      sb.append((String)(log.elementAt(i)));
      sb.append("</data>\n");
    }
    sb.append("</OperationLog>\n");
    try
    {
      stream.write( sb.toString().getBytes());
      return true;
    }
    catch(Exception s)
    { return xml_utils.setError("OpLog Exception="+s.getClass()+":"+
                s.getMessage());
    }
  }
 
  /**
  * Implements the IXmlIO interface to let the OperationLog "read" itself
  *
  * @param stream  the InputStream from which the data in xml format is read
  * 
  *
  * @return  true if successful otherwise false
  */
  public boolean XMLread( InputStream stream )
  { String Tag;
    boolean done=false;
    log = new Vector();
    while( !done)
    {
      Tag = xml_utils.getTag( stream);
      if( Tag == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
      if(!xml_utils.skipAttributes( stream ))
        return xml_utils.setError( xml_utils.getErrorMessage());
      if( Tag.equals("/OperationLog"))
        done=true;
      else if(!Tag.equals("data"))
        return xml_utils.setError( "Improper tag name in OpLog");
      else
      { String val =xml_utils.getValue( stream );
        if( val == null)
          return xml_utils.setError( xml_utils.getErrorMessage());
        log.addElement( val);
      }
    }
    return true;
  }


  /**
   *  Convert the log to a String consisting of the first characters of
   *  the log entries.
   *
   *  @return  A string representing the log entries by the first character
   *           of each log entry. 
   */
  public String toString()           // for now just return first character
  {                                  // of each string in the log
    String  temp = new String( );

    for ( int i = 0; i < this.log.size(); i++ )
      temp = temp + ((String)this.log.elementAt( i )).charAt(0) + "\n";

    return temp;
  }

  /**
   *  Make a deep copy of the log object and return the copy as a generic
   *  object.
   * 
   *  @return An object containing a copy of the log.
   */
  public Object clone( )
  {
    OperationLog  new_log = new OperationLog();

    for ( int i = 0; i < this.numEntries(); i++ )
    {
      new_log.addEntry( this.getEntryAt(i) );
    }

    return new_log;
  }


  /** ---------------------------------------------------------------------
   *  MAIN PROGRAM FOR TESTING
   */
  public static void main( String argv[] )
  {
    OperationLog test_log = new OperationLog();

    for ( int i = 0; i < 5; i++ )
      test_log.addEntry( i + " new entry, " );

    System.out.println( "test_log" );
    System.out.println( test_log );

    OperationLog new_log = (OperationLog) test_log.clone();
    new_log.addEntry( "Entry added to new log" );
    System.out.println( "new_log" );
    System.out.println( new_log );
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
      System.out.println("Warning:OperationLog IsawSerialVersion != 1");
  }

}
