/*
 * File:  WrappedCrunch.java
 *
 * Copyright (C) 2003 Chris Bouzek
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
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.2  2004/01/08 22:30:31  bouzekc
 * Removed several unused variables.
 *
 * Revision 1.1  2003/10/30 18:38:04  bouzekc
 * Added to CVS.
 *
 *
 */
package Operators;

import DataSetTools.dataset.*;

import DataSetTools.operator.*;

import DataSetTools.util.*;


/**
 * This class is a copy of Crunch.java designed to showcase the ability to use
 * wrappers to create Operators indirectly.
 */
public class WrappedCrunch implements Wrappable {
  //~ Instance fields **********************************************************

  private boolean DEBUG = false;
  /* @param ds Sample DataSet to remove dead detectors from.
   * @param min_count Minimum counts to keep bank
   * @param width How many sigma around average to keep
   * @param new_ds Whether to make a new DataSet.
   */
  public DataSet ds;
  public float min_count = 0.0f;
  public float width    = 2.0f;
  public boolean mk_new_ds = false;

  //~ Methods ******************************************************************

  /**
   * @return The script name for this Operator.
   */
  public String getCommand(  ) {
    return "WRAPPED_CRUNCH";
  }

  /**
   * Returns the documentation for this method as a String.  The format follows
   * standard JavaDoc conventions.
   */
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer( "" );
    s.append( "@overview This operator removes detectors from a DataSet " );
    s.append( "according to three criteria, all of which involve the total " );
    s.append( "counts.\n" );
    s.append( "@assumptions The specified DataSet ds is not null.\n" );
    s.append( "@algorithm First this operator removes detectors with zero " );
    s.append( "counts from the specified DataSet. Next it removes detectors " );
    s.append( "below the user specified threshold. Finally the average and " );
    s.append( "standard deviation is found for the total counts, then " );
    s.append( "detectors outside of the user specified number of sigma are " );
    s.append( "removed (generally too many counts).  It also appends a log " );
    s.append( "message indicating that the Crunch operator was applied to " );
    s.append( "the DataSet.\n" );
    s.append( "@param ds Sample DataSet to remove dead detectors from.\n" );
    s.append( "@param min_count Minimum counts to keep.\n" );
    s.append( "@param width How many sigma around the average to keep.\n" );
    s.append( "@param new_ds Whether to make a new DataSet.\n" );
    s.append( "@return DataSet containing the the original DataSet minus the " );
    s.append( "dead detectors.\n" );
    s.append( "@error Returns an error if the specified DataSet ds is null.\n" );

    return s.toString(  );
  }

  /**
   * Removes dead detectors from the specified DataSet.
   *
   * @return The crunched DataSet.
   */
  public Object calculate(  ) {
    if( ds == null ) {
      return new ErrorString( "DataSet is null in Crunch" );
    }

    // initialize new data set to be the same as the old
    DataSet new_ds     = null;

    // initialize new_ds
    if( mk_new_ds ) {
      new_ds = ( DataSet )ds.clone(  );
    } else {
      new_ds = ds;
    }

    // first remove detectors below min_count
    int MAX_ID    = new_ds.getMaxGroupID(  );

    for( int i = 1; i <= MAX_ID; i++ ) {
      Data det = new_ds.getData_entry_with_id( i );

      if( det == null ) {
        continue;
      }

      Float count = ( Float )det.getAttributeList(  )
                                .getAttributeValue( Attribute.TOTAL_COUNT );

      if( count.floatValue(  ) < min_count ) {
        new_ds.removeData_entry_with_id( i );
      }
    }

    // find the average total counts
    float avg     = 0f;
    float num_det = 0f;

    for( int i = 1; i <= MAX_ID; i++ ) {
      Data det = new_ds.getData_entry_with_id( i );

      if( det == null ) {
        continue;
      }

      Float count = ( Float )det.getAttributeList(  )
                                .getAttributeValue( Attribute.TOTAL_COUNT );
      avg = avg + count.floatValue(  );

      if( DEBUG ) {
        System.out.println( i + "  " + count );
      }
      num_det++;
    }

    if( num_det != 0f ) {
      avg = avg / num_det;
    } else {
      avg = 0f;
    }

    float dev = 0f;

    if( avg != 0f ) {
      // find the stddev of the total counts
      for( int i = 1; i <= MAX_ID; i++ ) {
        Data det = new_ds.getData_entry_with_id( i );

        if( det == null ) {
          continue;
        }

        Float count = ( Float )det.getAttributeList(  )
                                  .getAttributeValue( Attribute.TOTAL_COUNT );
        dev = dev +
          ( ( avg - count.floatValue(  ) ) * ( avg - count.floatValue(  ) ) );
      }

      if( avg != 0 ) {
        dev = dev / ( num_det - 1f );
      }
      dev = ( float )Math.sqrt( ( double )dev );

      if( DEBUG ) {
        System.out.println( num_det + "  " + avg + "  " + dev );
      }

      // remove detectors outside of width*sigma
      width = width * dev;

      for( int i = 1; i <= MAX_ID; i++ ) {
        Data det = new_ds.getData_entry_with_id( i );

        if( det == null ) {
          continue;
        }

        Float count = ( Float )det.getAttributeList(  )
                                  .getAttributeValue( Attribute.TOTAL_COUNT );
        float diff  = ( float )Math.abs( 
            ( double )( avg - count.floatValue(  ) ) );

        if( diff > width ) {
          new_ds.removeData_entry_with_id( i );

          if( DEBUG ) {
            System.out.println( 
              "removing det" + i + " with " + count + " total counts" );
          }
        }
      }
    }
    new_ds.addLog_entry( 
      "Applied Crunch( " + ds + ", " + min_count + ", " + ( width / dev ) +
      " )" );

    return new_ds;
  }
}
