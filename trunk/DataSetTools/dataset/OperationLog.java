/*
 * @(#)OperationLog.java     1.0  98/08/03  Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.4  2000/07/12 19:39:04  dennis
 *  Added method Print() to dump the full text of the log to System.out
 *
 *  Revision 1.3  2000/07/10 22:24:03  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.5  2000/05/12 15:50:13  dennis
 *  removed DOS TEXT  ^M
 *
 *  Revision 1.4  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 *
 */

package  DataSetTools.dataset;

import java.util.Vector;
import java.io.*;

/**
 *  Log of operations performed on a DataSet.
 */

public class OperationLog implements Serializable
{
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
}
