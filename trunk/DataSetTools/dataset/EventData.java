/*
 * File: EventData.java 
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 *  Revision 1.4  2002/11/27 23:14:07  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/10/03 15:42:45  dennis
 *  Changed setSqrtErrors() to setSqrtErrors(boolean) in Data classes.
 *  Added use_sqrt_errors flag to Data base class and changed derived
 *  classes to use this.  Added isSqrtErrors() method to check state
 *  of flag.  Derived classes now check this flag and calculate rather
 *  than store the errors if the use_sqrt_errors flag is set.
 *
 *  Revision 1.2  2002/06/28 20:54:09  dennis
 *  Removed unneeded import statement.
 *
 *  Revision 1.1  2002/06/19 22:54:06  dennis
 *  Prototype classes for event data.
 *
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import DataSetTools.functions.*;

/**
 * This is the class for a histogram data object whose values are determined 
 * by collection of events.  An object of this class contains an EventList
 * object that records a sequence of events, and a list of "X" values that 
 * are to be used when a list of Y values is to be generated.  
 */

public class EventData extends    Data
                       implements Serializable
{
  protected IEventList      eventlist       = null;
  protected IOneVarFunction errors          = null;
  protected int             smooth_flag     = IData.SMOOTH_NONE;

  /**
   * Constructs an EventData object by specifying an "X" scale, the 
   * EventList that determines the histogram and a group id for this 
   * data object.
   *
   * @param   x_scale    the list of x values for this Data object
   * @param   eventlist  the IEventList object that determines the histogram 
   * @param   group_id   an integer id for this data object
   *
   */
  public EventData( XScale x_scale, IEventList eventlist, int group_id )
  {
    super( x_scale, group_id );
    this.eventlist = eventlist;
  } 

  /**
   * Constructs an EventData object by specifying an "X" scale, the 
   * EventList that determines the histogram, a function that describes the
   * errors in the data, and a group id for this data object.
   *
   * @param   x_scale   the list of x values for this Data object
   * @param   eventlist  the IEventList object that determines the histogram 
   * @param   errors    the IOneVarFunction that describes the errors
   * @param   group_id  an integer id for this data object
   *
   */
  public EventData( XScale          x_scale, 
                    IEventList      eventlist, 
                    IOneVarFunction errors, 
                    int             group_id )
  { 
    super( x_scale, group_id );
    this.eventlist = eventlist;
    this.errors    = errors;
  }


  /**
    * Determine whether or not the current Data block has HISTOGRAM data.
    *
    * @return  true
    */
  public boolean isHistogram()
  {
    return true;
  }


  /**
   * Generate a histogram from the eventlist using the current x_scale.
   *
   *  @return  A new array listing histogram values formed from the eventlist
   *           using the current x-scale to determine the histogram bins. 
   */
  public float[] getY_values()
  {
    return eventlist.histogram( x_scale, smooth_flag );
  }


  /**
   * Generate a histogram from the eventlist using the current x_scale.
   *
   *  @param  x_scale      The XScale to be used for evaluating the histogram.
   *  @param  smooth_flag  Flag indicating the type of smoothing to use,
   *                       as defined in IData. #### not currently implemented
   *
   *  @return  A new array listing histogram values formed from the eventlist
   *           using the current x-scale to determine the histogram bins. 
   */
  public float[] getY_values( XScale x_scale, int smooth_flag )
  {
    return eventlist.histogram( x_scale, smooth_flag );
  }


 /**
  *  Get the "Y" value by generating the event count for the bin containing the
  *  specified x_value.  If smoothing is specified, the histogram will also
  *  be generated for adjacent bins and the values will be smoothed.
  *
  *  @param  x_value      the x value where the histogram is evaluated.
  *
  *  @param  smooth_flag  Flag indicating the type of smoothing to use,
  *                       as defined in IData. #### not currently implemented
  *
  *  @return approximate y value at the specified x value
  *
  *  NOTE: ##### This should efficiently find a small x-scale that contains
  *              the specified x_value and as many adjacent bins as needed
  *              for the smoothing.  It should then use eventlist.histogram
  *              to obtain the needed y-values and perform the smoothing
  *              requested.  Currently, it just returns the number of events
  *              in one histogram bin containing the given x_value.  The
  *              x_scale is assumed to be uniformly spaced. 
  */
  public float getY_value( float x_value, int smooth_flag )
  {
    float a = x_scale.getStart_x();
    float b = x_scale.getEnd_x();
    int   n = x_scale.getNum_x();
    float width;
    if ( n > 1 )
      width = (b-a)/(n-1);
    else
      width = 1;
    UniformXScale new_scale = new UniformXScale( x_value-width/2, 
                                                 x_value+width/2,
                                                 2 );  
    float histogram[] = eventlist.histogram( new_scale, smooth_flag );
    return histogram[0];
  }


 /**
   *  Get a list of error estimates for this Data object, by evaluating the
   *  previously specified error estimate function at the bin centers of
   *  current x_scale. If no error function has been set, this returns null.
   *
   *  @return  array of error estimates for the y values of this histogram,
   *           or null if no error estimate function was specified.
   */
  public float[] getErrors()
  {
    if ( isSqrtErrors() )
    {
      float y_vals[] = getY_values();
      float errs[] = new float[ y_vals.length ];
      for ( int i = 0; i < errs.length; i++ )
        errs[i] = (float)Math.sqrt( Math.abs( y_vals[i] ) );
      return errs;
    }
    else if ( errors != null )
    {
      float x_vals[] = x_scale.getXs();
      float centers[] = new float[ x_vals.length - 1 ];
      for ( int i = 0; i < centers.length; i++ )
        centers[i] = (x_vals[i] + x_vals[i+1])/2.0f;
      return errors.getValues( centers );
    }
    else
      return null;
  }


 /**
   * Set the function defining the errors for this data object.
   *
   * @param   err     New OneVarFunction for the errors to use for this data 
   *                  object. 
   */ 
  public void setErrors( IOneVarFunction err )
  {
    setSqrtErrors(false);
    errors = err;
  }


  /**
   *  Specify whether the errors are to be estimated as the square root of
   *  the number of counts.  If use_sqrt is true, the error estimates will be
   *  calculated "on the fly" using the square root function.
   *
   *  @param use_sqrt If true, error estimates will be calculated as the
   *                  square root of the counts, if false, no error estimates
   *                  will be recorded for this Data block, unless they are
   *                  set using setErrors().
   */
  public void setSqrtErrors( boolean use_sqrt )
  {
    setSqrtErrors( use_sqrt );
    if ( use_sqrt )
      errors = null;
  }


  /**
   *  Resample the Data block on an arbitrarily spaced set of points given by
   *  the new_X scale parameter.  In this case (ModeledData) this just records
   *  a new x_scale that will be used when the function is evaluated.
   *
   *  @param new_X        The x scale giving the set of x values to use for the
   *                      resampling and/or rebinning operation.
   *  @param smooth_flag  Flag indicating the smoothing type to be used, 
   *                      as defined in IData.  #### not currently implemented
   */
  public void resample( XScale new_X, int smooth_flag )
  {
    x_scale = new_X;
    this.smooth_flag = smooth_flag;
  }

  /**
   * Return a new EventData object containing a copy of the x_scale,
   * eventlist, error function, group_id and attributes from the current
   * EventData object.
   *
   * @return a "deep" copy of the current EventData object is returned as
   *         a generic Object.
   */
  public Object clone()
  {
    EventData temp = new EventData(x_scale, eventlist, errors, group_id);

                                      // copy the list of attributes.
    AttributeList attr_list = getAttributeList();
    temp.setAttributeList( attr_list );
    temp.selected = selected;
    temp.hide     = hide;

    return temp;
  }


  /*
   *  Main program for testing purposes
   */
  public static void main( String argv[] )
  {
    int   N_EVENT_BINS = 200;
    float start_time = 100;
    float tick_width = 0.1f; 
    int   time[]     = new int[N_EVENT_BINS];
    int   count[]    = new int[N_EVENT_BINS];

    for ( int i = 0; i < time.length; i++ )
    {
      time[i]  = 11 * i;
      double t = start_time + time[i] * tick_width;
      count[i] = (int)(10000*Math.exp( -(t-150)*(t-150) / 100 )); 
    }

    EventList event_list = new EventList( start_time, tick_width, time, count );
    XScale    x_scale    = new UniformXScale( 0, 300, 101 );
    Data      data       = new EventData( x_scale, event_list, null, 1 );    

    DataSet ds = new DataSet("Demo Data", "");
    ds.addData_entry( data );

    data = TabulatedData.getInstance( data, 2 );
    ds.addData_entry( data );

    data = TabulatedData.getInstance( data, 3, Data.FUNCTION );
    ds.addData_entry( data ); 

    IOneVarFunction f = new Gaussian( 150, 10000, 20 );
    data = new FunctionModel( x_scale, f, 4 );
    ds.addData_entry( data ); 

    f = new Gaussian( 150, 10000, 20 );
    data = new HistogramModel( x_scale, f, 5 );
    ds.addData_entry( data );

    ViewManager vm = new ViewManager( ds, IViewManager.IMAGE );
  }
}
