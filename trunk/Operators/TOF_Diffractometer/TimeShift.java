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
 * $Log$
 * Revision 1.2  2005/01/10 15:35:59  dennis
 * Added getCategoryList method to place operator in menu system.
 *
 * Revision 1.1  2004/05/07 17:42:49  dennis
 * Moved TimeShift from Operators to Operators/TOF_Diffractometer
 *
 * Revision 1.2  2004/04/28 19:12:18  dennis
 * Revised version, with debug flag false. (Tom Worlton)
 *
 * Initial version  2004/4/15 tworlon
 *
 */
package Operators.TOF_Diffractometer;

import gov.anl.ipns.Util.SpecialStrings.*;
import DataSetTools.dataset.*;

import DataSetTools.operator.*;

/**
 * This class is intended to be used to correct for frame overlap on GPPD
 * 
 */
public class TimeShift implements Wrappable, IWrappableWithCategoryList {
  //~ Instance fields **********************************************************

  private boolean DEBUG = false;
  /* @param ds Sample DataSet to correct.
   * @param rep_rate  Source repetition rate
   * @param tshift  time(microseconds) of shift point.
   * @param new_ds Whether to make a new DataSet.
   */
  public DataSet ds;
  public float rep_rate = 30.0f;
  public float tshift = 12000.0f;
  public boolean mk_new_ds = true;

  //~ Methods ******************************************************************

  /* ------------------------ getCategoryList ------------------------------ */
  /**
   * Get an array of strings listing the operator category names  for 
   * this operator. The first entry in the array is the 
   * string: Operator.OPERATOR. Subsequent elements of the array determine
   * which submenu this operator will reside in.
   * 
   * @return  A list of Strings specifying the category names for the
   *          menu system 
   *        
   */
  public String[] getCategoryList()
  {
    return Operator.UTILS_DATA_SET;
  }

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
    s.append( "@param new_ds  Whether to make a new data set (must be true).\n");
    s.append( "@return DataSet corrected for frame overlap.\n" );
    s.append( "@error Returns an error if the specified DataSet ds is null.\n" );

    return s.toString(  );
  }

  /**
   * Shifts the time scale of spectra within a dataset to correct for frame overlap.
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
    int last = 10000;
    if(DEBUG) {
      first = 2;
      last=MAX_ID - 2;
    }
    float period = 1000000.0f/rep_rate;

    for( int id = 1; id <= MAX_ID; id++ ) {
      Data det = null;
      det = ds.getData_entry_with_id( id );
      if( det == null ) {
        if(DEBUG)
          System.out.println("No detector with ID " + id);
        continue;
      }
      XScale old = det.getX_scale();
      float y_old[]     = det.getCopyOfY_values();
      int old_num_y = y_old.length;
      if(DEBUG && id < first)
        System.out.println("old_num_y = " + old_num_y);
      float old_start_x = old.getStart_x();
      float old_end_x   = old.getEnd_x();
//      int   old_num_x   = old.getNum_x();
      float step        = (old_end_x - old_start_x);
      if(old_num_y > 1)
        step = (old_end_x-old_start_x)/(old_num_y);
      if(DEBUG && id < first)
        System.out.println("step size = " + step);
//  We now have all data from the old Data/XScale
      if(id < first || id > last)  {
        System.out.println(id + " old start_x, end_x, num_y =" + 
                           old_start_x + "-" + old_end_x + "/" + old_num_y);
      }


//  Get unshifted data and define new array, XScale and Data
      float y_values[];
      UniformXScale newx;
      Data newdata = null;
//    Shift sections of data
      if(tshift > old_start_x && tshift < old_end_x) {
//      Calculate new Xscale to cover a shifted full time frame
        float start_x = tshift;
        float end_x = period + tshift;
        int num_y = (int) ((end_x - start_x)/step);
        end_x = step*num_y + start_x;  //  make end_x end on a channel boundary
        num_y = (int) ((end_x - start_x)/step);  //recalculate num_y 
        y_values = new float[num_y];
        if(id < first  || id > last) {
          System.out.println(id + " Creating new XScale with start_x, end_x, num_y = "
                              + start_x + " - " + end_x + ", " + num_y);
        }
        newx = new UniformXScale(start_x, end_x, num_y+1);  //there is one more x than y
        int ns1 = (int) ((tshift - old_start_x)/step);
        int ns2 = old_num_y - ns1;
        int ngap = (int) (period/step + 0.5) - old_num_y;
        if(id< first || id > last)
          System.out.println("Shifting the first " + ns1 + " channels by " + period + " microseconds");
//      Shift non-overlapped data ( tshift < t < old_end_x) to start of array
        for( int i=0; i<ns2; i++) {
          y_values[i] = y_old[i+ns1];
        }
//      put overlapped data after non-overlapped data and gap data
        for( int i = ns2+ngap; i < num_y; i++) {
          y_values[i] = y_old[i-ns2-ngap];
        }
        newdata = Data.getInstance(newx, y_values, id);
      }
      else {  //just add a time shift to x values and don't change y_values
        float start_x = old_start_x + tshift;
        float end_x = old_end_x + tshift;
        int num_y = old_num_y;
        newx = new UniformXScale(start_x, end_x, num_y+1);  //there is one more x than y
        y_values = y_old;
//        y_values = new float[num_y];
//        for( int i = 0; i< num_y; i++) {
//          y_values[i] = y_old[i];
//        }
        newdata = Data.getInstance(newx, y_values, id);
        if(DEBUG && id < first) {
          System.out.println("Shifting data by " + tshift);
          System.out.println(id + " new start_x, end_x, num_y =" + 
                           start_x + "-" + end_x + "/" + num_y);
        }
      }
      AttributeList atlist = det.getAttributeList();
      newdata.setAttributeList( atlist );
      new_ds.addData_entry(newdata);
    }
    new_ds.addLog_entry( 
      "Applied TimeShift( " + ds + ", " + rep_rate + ", " + tshift + " )" );
    return new_ds;
  }
}
