/*
 * File:  TimeShift.java
 *
 * Copyright (C) 2004 Tom Worlton
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
 * Contact : Thomas Worlton <tworlton@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           Argonne, IL 60439-4814, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4814, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * Initial version  2004/4/15 tworlon
 *
 *
 */
package Operators;

import gov.anl.ipns.Util.SpecialStrings.*;
import DataSetTools.dataset.*;

import DataSetTools.operator.*;

/**
 * This class is intended to be used to correct for frame overlap on GPPD
 * 
 */
public class TimeShift implements Wrappable {
  //~ Instance fields **********************************************************

  private boolean DEBUG = false;
  /* @param ds Sample DataSet to correct.
   * @param rep_rate  Source repetition rate
   * @param tshift  time(microseconds) of shift point.
   */
  public DataSet ds;
  public float rep_rate = 30.0f;
  public float tshift = 12000.0f;

  //~ Methods ******************************************************************

  /**
   * @return The script name for this Operator.
   */
  public String getCommand(  ) {
    return "TimeShift";
  }

  /**
   * Returns the documentation for this method as a String.  The format follows
   * standard JavaDoc conventions.
   */
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer( "" );
    s.append( "@overview This operator shifts the time scale within a DataSet " );
    s.append( "to correct for frame overlap.\n" );
    s.append( "@assumptions The specified DataSet ds is not null.\n" );
    s.append( "@algorithm  This operator creates an empty clone of the ");
    s.append( "original DataSet.  It then calculates a new time scale ");
    s.append( "which starts at the original start_x plus tshift and" );
    s.append( "and extends for one complete pulse period.  The pulse period" );
    s.append( "is equal to one million over the repetition rate (to " );
    s.append( "convert to microseconds).  It then retrieves the data for " );
    s.append( "each group and shifts points past tshift to the beginning " );
    s.append( "and adds one time period to points prior to tshift." );
    s.append( "  It also appends a log " );
    s.append( "message indicating that the TimeShift operator was applied to " );
    s.append( "the DataSet.\n" );
    s.append( "@param ds Sample DataSet to correct for frame overlap.\n" );
    s.append( "@param rep_rate The source repetition rate.\n" );
    s.append( "@param tshift  The number of microseconds before the end of overlap.\n" );
    s.append( "@return DataSet corrected for frame overlap.\n" );
    s.append( "@error Returns an error if the specified DataSet ds is null.\n" );

    return s.toString(  );
  }

  /**
   * Shifts the time scale of spectra within a dataset to correct for 
   * frame overlap.
   *
   * @return The modified DataSet.
   */
  public Object calculate(  ) {
    if(DEBUG)
      System.out.println("Starting calculate method.");
    if( ds == null ) {
      return new ErrorString( "DataSet is null in TimeShift" );
    }

    // initialize new data set to be the same as the old without the data
    DataSet new_ds = ( DataSet )ds.empty_clone( );

    // Loop through all data blocks
    int MAX_ID    = ds.getMaxGroupID(  );
    if(DEBUG)
      System.out.println("Maximum id = " + MAX_ID);
    int first = -1;
    if(DEBUG)
      first = 4;
    for( int id = 1; id <= MAX_ID; id++ ) {
      Data det = null;
      det = ds.getData_entry_with_id( id );
      if( det == null ) {
        if(DEBUG)
          System.out.println("No detector with ID " + id);
        continue;
      }
      XScale old = det.getX_scale();
      float y_old[] = det.getCopyOfY_values();
      float period = 1000000.0f/rep_rate;
      float old_start_x = old.getStart_x();
      float old_end_x = old.getEnd_x();
      if(id <= first)
        System.out.println("----- " + id + " -----");
      if(id < first)
        System.out.println("old start/end = " + old_start_x + "/" + old_end_x);
      float old_num_x = old.getNum_x();
      float step = (old_end_x - old_start_x);
      if(old_num_x > 1)
        step = (old_end_x-old_start_x)/(old_num_x-1.0f);
      if(id < first)  {
        System.out.println("old num_x =" + old_num_x);
        System.out.println("Step size = " + step);
      }
//      float start_x = old_start_x + tshift;
//      float end_x = tshift + period;
      float start_x = tshift;
      float end_x = period + tshift;
      float deltat = end_x - start_x;
      if(id < first)
        System.out.println("new start/end = " + start_x + "/" + end_x);
      int iframe = (int) (period/step+ 0.5);      
      int num_x = (int) (deltat/step+0.5);
      if(id < first)
        System.out.println("new num_x =" + num_x);
      UniformXScale newx = new UniformXScale(start_x, end_x, num_x);
      int i1 = (int) ((tshift - old_start_x)/step);
      int i2 = (int) ((old_end_x - old_start_x)/step);
//  Get unshifted data and define new array
      float y_values[] = new float[num_x];

// Shift non-overlapped data to the beginning
      if(id < first) {
        System.out.println("Shifting non-overlapped data (" + i1 + " to " + i2 + ")");
        System.out.println("by " + i1 + " channels.");
      }
      for( int i=i1; i<i2; i++) {
        y_values[i-i1] = y_old[i];
      }

// Zero array for measurement gap (between old_end_x and period)
      if(id < first) {
        System.out.println("Zeroing gap area channels (" + i2 + " to " + iframe + ")");
        System.out.println("shifted left by " + i1 + " channels.");
      }
      for(int i = i2; i< iframe; i++) {
        y_values[i-i1] = 0.0f;
      }

// Shift overlapped data to proper time position
      if(id < first) {
         System.out.println("Shifting overlapped channels to end of frame");
         System.out.println("Channels =" + (iframe-i1) + " to " + iframe);
      }
      for( int i = 0; i<i1; i++) {
        y_values[i+iframe-i1] = y_old[i];
      }

      Data newdata = Data.getInstance(newx, y_values, id);
      AttributeList atlist = det.getAttributeList();
      newdata.setAttributeList( atlist );
      new_ds.addData_entry(newdata);
    }
    new_ds.addLog_entry( 
      "Applied TimeShift( " + ds + ", " + rep_rate + ", " + tshift + " )" );
    return new_ds;
  }
}
