/*
 * File: FunctionTable.java 
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.7  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.6  2002/06/19 22:40:01  dennis
 *  Minor cleanup of format.
 *
 *  Revision 1.5  2002/06/14 21:00:17  rmikk
 *  Implements IXmlIO interface
 *
 *  Revision 1.4  2002/04/19 15:42:28  dennis
 *  Revised Documentation
 *
 *  Revision 1.3  2002/04/11 21:07:49  dennis
 *  Fixed bug in resample() method that caused y_values to be
 *  resampled twice if the errors were non-null.
 *  Improved structure of FunctionTable copy constructor.
 *
 *  Revision 1.2  2002/04/04 18:18:36  dennis
 *  The constructor that takes a Data object now also copies
 *  the attributes as well as the selected and hide flags.
 *  Moved stitch() to Data.java
 *  Moved Data add(), subtract(), multiply() and dividc()
 *  to Data.java
 *  Changed resample() to call SmoothResample in DataSetTools.math.Sample.
 *  Moved compatible() to Data.java
 *  Commented out CLSmooth() and ResampleUniformly() since these
 *  should be done using resample()
 *  Removed Rebin() method ( use resample() )
 *
 *  Revision 1.1  2002/03/13 16:08:34  dennis
 *  Data class is now an abstract base class that implements IData
 *  interface. FunctionTable and HistogramTable are concrete derived
 *  classes for storing tabulated functions and frequency histograms
 *  respectively.
 *
 */

package  DataSetTools.dataset;

import java.util.Vector;
import java.io.*;
import DataSetTools.math.*;
import DataSetTools.util.*;

/**
 * The concrete root class for a tabulated function data object.  This class
 * bundles together the basic data necessary to describe a tabulated function 
 * of one variable.  An object of this class contains a list of "X" values 
 * and a list of "Y" values together with an extensible list of attributes 
 * for the object.  A list of errors for the "Y" values can also be kept.
 *  
 * @see DataSetTools.dataset.IData
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.TabulatedData
 * @see DataSetTools.dataset.HistogramTable
 *
 */

public class FunctionTable extends    TabulatedData
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
  /**
   * Constructs a Data object containing a table of function values by 
   * specifying an "X" scale, "Y" values and a group id for that data object.  
   * The X scale and list of Y values should have the same number of points.
   * If too many Y values are given, the extra values will be ignored.  If
   * too few Y values are given, the saved Y values will be padded with trailing
   * zero values. 
   *
   * @param   x_scale   the list of x values for this data object 
   * @param   y_values  the list of y values for this data object 
   * @param   group_id  an integer id for this data object
   *
   */
  public FunctionTable( XScale x_scale, float y_values[], int group_id )
  {
    super( x_scale, y_values, group_id );
    init( y_values );
  }

  /**
   * Constructs a Data object by specifying an "X" scale, 
   * "Y" values and an array of error values.
   *
   * @param   x_scale   the list of x values for this data object 
   * @param   y_values  the list of y values for this data object 
   * @param   errors    the list of error values for this data object.  The
   *                    length of the error list should be the same as the
   *                    length of the list of y_values.  
   * @param   group_id  an integer id for this data object
   *
   */
  public FunctionTable( XScale  x_scale, 
                        float   y_values[], 
                        float   errors[], 
                        int     group_id )
  {
    super( x_scale, y_values, group_id );
    init( y_values );
    this.setErrors( errors );
  }


  /**
   * Constructs a Data object with the specified X scale.  The Y values are 
   * all zero, and the errors are null, and the group id's are -1. This 
   * constructor is only needed for the XMLread to read a Data Object.
   *
   * @param   x_scale   the list of x values for this data object 
   * 
   * @see TabulatedData#XMLread( java.io.InputStream ) 
   */
  public FunctionTable( XScale x_scale )
  { 
    super( x_scale, new float[x_scale.getNum_x()], -1); 
  }


  /**
   * Constructs a FunctionTable Data object from another Data object.
   *
   * @param   d         the data object from which to obtain the x_values,
   *                    y_values and errors.
   * @param   divide    if the given Data block is a Histogram, this flag 
   *                    indicates whether the histogram values should be 
   *                    divided by the width of the histogram bin.
   * @param   group_id  an integer id for this data object
   *
   */
  public FunctionTable( Data d, boolean divide, int group_id )
  {
    super( d.x_scale, null, group_id );
    float old_x_values[] = null;
    
    if ( d.isHistogram() )                  // we need to convert to function
    {                                       // using values at bin centers 
      old_x_values = d.getX_values();
      float new_x_values[]  = new float[ old_x_values.length - 1 ];
      for ( int i = 0; i < new_x_values.length; i++ )
        new_x_values[i] = (old_x_values[i] + old_x_values[i+1]) / 2.0f; 

      x_scale = new VariableXScale( new_x_values );     //#### check if uniform?
    }

    init( d.getY_values() );
    this.setErrors( d.getErrors() );

    if ( d.isHistogram() && divide )
    {
      if ( errors == null )
        for ( int i = 0; i < y_values.length; i++ )
        {                                                           
          float dx = old_x_values[i+1] - old_x_values[i];
          y_values[i] = y_values[i] / dx; 
        }
      else
        for ( int i = 0; i < y_values.length; i++ )
        {                                                           
          float dx = old_x_values[i+1] - old_x_values[i];
          y_values[i] = y_values[i] / dx; 
          errors[i]   = errors[i] / dx;        //#### how should
        }                                      // errors be treated?
    }
    AttributeList attr_list = d.getAttributeList();
    setAttributeList( attr_list );
    selected = d.selected;
    hide     = d.hide;
  }


  /**
   *  Get an approximate y value corresponding to the specified x_value in this 
   *  Data block. If the x_value is outside of the interval of x values
   *  for the Data, this returns 0.  In other cases, the approximation used is
   *  controlled by the smooth_flag.  
   *
   *  @param  x_value      the x value for which the corresponding y value is to
   *                       be interpolated
   *
   *  @param  smooth_flag  SMOOTH_NONE means that we will just use the value
   *                       at the last tabulated x that is less than or equal
   *                       to the specified x_value.  Currently, the only other
   *                       smoothing supported is linear interpolation, which
   *                       will be used if the smooth flag has any value other
   *                       than SMOOTH_NONE. ####
   *
   *  @return approximate y value at the specified x value
   */
  public float getY_value( float x_value, int smooth_flag )
  {
    if ( x_value < x_scale.getStart_x() || 
         x_value > x_scale.getEnd_x()    )
      return 0;

    float x_vals[] = x_scale.getXs();
    int index = arrayUtil.get_index_of( x_value, x_vals );

    if ( smooth_flag == IData.SMOOTH_NONE )
      return y_values[index];

    if ( index == y_values.length - 1 )               // last value, so can't
      return y_values[ y_values.length - 1 ];         // interpolate.
  
    float x1 = x_vals[index];  
    float x2 = x_vals[index+1];

    if ( x1 == x2 )        
      return y_values[index];                          // duplicate x values

    float y1 = y_values[index];                        // otherwise, interpolate
    float y2 = y_values[index+1];
    return y1 + ( x_value - x1 )*( y2 - y1 ) / ( x2 - x1 );
  }


  /**
   * Determine whether or not the current Data block has HISTOGRAM data.
   * HISTOGRAM data records bin boundaries and a number of counts in each
   * bin, so the number of x-values is one more than the number of y-values.
   *
   * @return  true if the number of x-values is one more than the number
   *          of y-values.
   */
  public boolean isHistogram()
  {
    return false;
  }  
 

  /**
   * Return a new FunctionTable object containing a copy of the x_scale, 
   * y_values errors, group_id and attributes from the current Data object.
   *
   * @return  A "deep copy" clone of the current Data object as a generic 
   *          object. 
   */
  public Object clone()
  {
    Data temp = new FunctionTable( x_scale, y_values, errors, group_id );

                                      // copy the list of attributes.
    AttributeList attr_list = getAttributeList();
    temp.setAttributeList( attr_list );
    temp.selected = selected;
    temp.hide     = hide;

    return temp;
  }


  /**
   *  Resample the Data block on an arbitrarily spaced set of points given by
   *  the new_X scale parameter.  If the Data block is a tabulated function,
   *  the function will just be interpolated at the specified points.  If the 
   *  Data block is a histogram, the histogram will be re-binned to form a 
   *  new histogram with the specified bin sizes.  #### smooth_flag not 
   *  implmented. 
   *
   *  @param new_X  The x scale giving the set of x values to use for the
   *                 resampling and/or rebinning operation.
   *
   *  @param smooth_flag  Flag indicating the degree of smoothing to be 
   *                      applied. #### smooth_flag not not currently 
   *                      implemented. 
   */
  public void resample( XScale new_X, int smooth_flag )
  {
    float x[]  = x_scale.getXs();
    float nX[] = new_X.getXs();

    if ( errors != null )
    {
      float result[][] = Sample.SmoothResample( x, y_values, errors, 
                                                nX, smooth_flag );
      y_values = result[0];
      errors   = result[1];
    }
    else
      y_values = Sample.SmoothResample( x, y_values, nX, smooth_flag );

    x_scale = (XScale)new_X.clone();
  }

  /*
   *  Resample the Data block on a uniformly spaced set of points given by
   *  the new_X scale parameter.  The function will be interpolated and 
   *  smoothed by averaging nearby points.  
   *
   *  @param new_X  The x scale giving the set of x values to use for the
   *                 resampling and/or rebinning operation. 
   */
/*
  public void ResampleUniformly( UniformXScale new_X )
  {
      float x_start = new_X.getStart_x();
      float x_end   = new_X.getEnd_x();

      float x[]     = x_scale.getXs();
      if ( x_start > x[ x.length-1 ] || x_end < x[0] || x.length ==0 )  
      {                                                 // degenerate case
        x_scale = (UniformXScale)new_X.clone();         // intervals don't 
        y_values = new float[x_scale.getNum_x()];       // overlap

        for ( int i = 0; i < y_values.length; i++ )     // assume function is 0
          y_values[i] = 0;                              // outside

        if ( errors != null )
        {
          errors = new float[y_values.length];
          for ( int i = 0; i < y_values.length; i++ )
            errors[i] = 0;
        } 
      }
      else if ( x.length == 1 )                        // only one point
      { 
        float x_val = x[0];
        float y_val = y_values[0];
        
        x_scale = (UniformXScale)new_X.clone();
        x        = x_scale.getXs();
        y_values = new float[x_scale.getNum_x()];  

        for ( int i = 0; i < y_values.length; i++ )
          y_values[i] = 0;

        int x_index = arrayUtil.get_index_of( x_val, x );        
        y_values[ x_index ] = y_val;
        if ( x_val != x[x_index] )                     // the one point affects 
          y_values[ x_index+1 ] = y_val;               // value at both
                                                       // adjacent grid points
        if ( errors != null )
        {
          float err = errors[0];
          errors = new float[y_values.length];
          for ( int i = 0; i < y_values.length; i++ )
            errors[i] = 0;

          errors[ x_index ] = err;
          if ( x_val != x[x_index] )                   // the one point affects
            errors[ x_index+1 ] = err;                 // error at both
                                                       // adjacent grid points
        }
      }
      else    // the intervals overlap, so get the portions of the x&y arrays 
      {       // that we are dealing with and pass those into the smooth op

         int i_start = arrayUtil.get_index_of( x_start, x );
         if ( i_start < 0 )
           i_start = 0;

         int i_end = arrayUtil.get_index_of( x_end, x );
         if ( i_end < 0 )
           i_end = x.length-1;

         int num_new_x = i_end - i_start + 1;       // extract part of the
         float new_x[] = new float[ num_new_x ];    // existing x,y values
         float new_y[] = new float[ num_new_x ];
         for ( int i = 0; i < num_new_x; i++ )
         {
           new_x[i] = x[ i + i_start ];
           new_y[i] = y_values[ i + i_start ];
         }

         x_scale = new VariableXScale( new_x );     // keep only part of the
         y_values = new_y;                          // existing x,y values

         if ( errors != null )                      // keep part of existing 
         {                                          // errors if not null.
           float new_err[] = new float[ num_new_x ];
           for ( int i = 0; i < num_new_x; i++ )
             new_err[i] = errors[ i + i_start ];
           errors = new_err;
         }
         CLSmooth( new_X.getNum_x() );             // changes current Data
                                                   // block x, y, errors
        
         x = x_scale.getXs();
         float nX[] = new_X.getXs();
         y_values = Sample.Resample( x, y_values, nX );   
       
         if ( errors != null ) 
           errors   = Sample.Resample( x, errors, nX );
         x_scale = (UniformXScale)new_X.clone();
      }
  }
*/

  /*
   * Smooth this Data object by resampling/averaging the y values and errors
   * of the current data so that there are roughly the specified number of
   * samples of the data.
   *
   * @param  num_X    This specifies the approximate number of "x" values to 
   *                  be used
   */
/*
  public void CLSmooth( int num_X )
  {
    float old_x[] = x_scale.getXs();
                                                        // now do the smoothing
    if ( errors != null )
      num_X = Sample.CLSmooth( old_x, y_values, errors, num_X );
    else
      num_X = Sample.CLSmooth( old_x, y_values, num_X );

                                                // put the y values and
                                                // errors in right size arrays
    float new_y[]   = new float[num_X];
    System.arraycopy( y_values, 0, new_y, 0, num_X );
    y_values = new_y;

    if ( errors != null )
    {
      float new_err[]   = new float[num_X];
      System.arraycopy( errors, 0, new_err, 0, num_X );
      errors = new_err;
    }
                                                // copy over the x values, or
                                                // synthesize bin boundaries
                                                // as needed and make XScale
    float new_x[] = new float[num_X];
    System.arraycopy( old_x, 0, new_x, 0, num_X );
    x_scale = new VariableXScale( new_x );
  }
*/

/* -----------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

  /**
   *  Set the specified array of y_values as the y_values for this Data
   *  object.  If there are more y_values than there are x_values in the
   *  XScale for this object, the excess y_values are discarded.  If there
   *  are not as many y_values as there are x_values, the new y_values array
   *  is padded with 0s.
   */
  private void init( float y_values[] )
  {
    int n_samples = x_scale.getNum_x();
    this.y_values = new float[ n_samples ];

    if ( y_values.length >= n_samples )
      System.arraycopy( y_values, 0, this.y_values, 0, n_samples );
    else
    {
      System.arraycopy( y_values, 0, this.y_values, 0, y_values.length );
      for ( int i = y_values.length; i < n_samples; i++ )
        this.y_values[i] = 0;
    }
  }


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
      System.out.println("Warning:FunctionTable IsawSerialVersion != 1");
  }

}
