/*
 * File: HistogramTable.java 
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
 *  Revision 1.12  2003/07/09 14:42:54  dennis
 *  The method to get a y-value at a specified x-value no longer gets
 *  the full list of x-values and does a binary search.  It now uses
 *  the XScale method getI_GLB(x).
 *
 *  Revision 1.11  2002/11/27 23:14:07  pfpeterson
 *  standardized header
 *
 *  Revision 1.10  2002/11/12 21:53:24  dennis
 *  Use XScale.getInstance() rather than automatically creating a
 *  variable XScale, so that we use a UniformXScale if possible.
 *
 *  Revision 1.9  2002/10/03 15:42:46  dennis
 *  Changed setSqrtErrors() to setSqrtErrors(boolean) in Data classes.
 *  Added use_sqrt_errors flag to Data base class and changed derived
 *  classes to use this.  Added isSqrtErrors() method to check state
 *  of flag.  Derived classes now check this flag and calculate rather
 *  than store the errors if the use_sqrt_errors flag is set.
 *
 *  Revision 1.8  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.7  2002/07/17 20:35:06  dennis
 *  Now traps invalid index in getY_value()
 *
 *  Revision 1.6  2002/06/19 22:39:06  dennis
 *  Minor cleanup of format and added some docs for XML IO.
 *
 *  Revision 1.5  2002/06/14 21:00:28  rmikk
 *  Implements IXmlIO interface
 *
 *  Revision 1.4  2002/04/19 15:42:30  dennis
 *  Revised Documentation
 *
 *  Revision 1.3  2002/04/11 21:09:21  dennis
 *  Fixed bug in HistogramTable copy constructor.  Must
 *  create new x_scale before using init() to set up the
 *  y_values.
 *
 *  Revision 1.2  2002/04/04 18:21:44  dennis
 *  The constructor that takes a Data object now also copies
 *  the attributes as well as the selected and hide flags.
 *  Moved stitch() to Data.java
 *  Moved Data add(), subtract(), multiply() and dividc()
 *  to Data.java
 *  Moved compatible() to Data.java
 *  Made Rebin() method private ( use resample() which calls Rebin()
 *
 *  Revision 1.1  2002/03/13 16:08:35  dennis
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
 * The concrete root class for a tabulated frequency histogram data object.  
 * This class bundles together the basic data necessary to describe a frequency
 * histogram.  An object of this class contains a list of "X" values
 * and a list of "Y" values together with an extensible list of attributes
 * for the object.  A list of errors for the "Y" values can also be kept.
 *  
 * @see DataSetTools.dataset.IData
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.TabulatedData
 * @see DataSetTools.dataset.FunctionTable
 *
 */

public class HistogramTable extends    TabulatedData
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
   * Constructs a Data object containing a table of frequency histogram values 
   * specifying an "X" scale, "Y" values and a group id for that data object.
   * The X scale specifies bin boundaries and should have one more value than 
   * the list of Y values.  If too many Y values are given, the extra values 
   * will be ignored.  If too few Y values are given, the saved Y values will 
   * be padded with trailing zero values.
   *
   * @param   x_scale   the list of x values for this data object 
   * @param   y_values  the list of y values for this data object 
   * @param   group_id  an integer id for this data object
   *
   */
  public HistogramTable( XScale x_scale, float y_values[], int group_id )
  {
    super( x_scale, y_values, group_id );
    init( y_values );  
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
  public HistogramTable( XScale x_scale)
  {
    super( x_scale, new float[x_scale.getNum_x()-1], -1);
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
  public HistogramTable( XScale  x_scale, 
                         float   y_values[], 
                         float   errors[], 
                         int     group_id )
  {
    super( x_scale, y_values, group_id );
    init( y_values );  
    this.setErrors( errors );
  }


  /**
   * Constructs a HistogramTable Data object from another Data object.
   *
   *  @param  width_1   Width of the first bin.  For VariableXScales, this is
   *                    is used to determine the bin boundaries.  NOTE: this
   *                    process is subject to rounding errors and should be
   *                    avoided if possible.  If width_1 is less than or equal
   *                    to zero, the distance between the first two x values
   *                    will be used as a default.
   *
   *  @param  multiply  Flag that indicates whether the function values
   *                    should be multiplied by the width of the histogram bin.
   * @param   group_id  an integer id for this data object
   *
   */
  public HistogramTable( Data d, boolean multiply, int group_id )
  {
    super( d.x_scale, null, group_id );

    float new_x_values[] = null;
    int   n_bins = 0;

    if ( !d.isHistogram() )                  // we need to convert to histogram 
    {
      float old_x_values[] = d.getX_values();
      n_bins = old_x_values.length;

      new_x_values = new float[ n_bins + 1 ];
      new_x_values[0] = old_x_values[0] 
                            - (old_x_values[1] - old_x_values[0]) / 2; 
      for ( int i = 1; i < n_bins; i++ )
        new_x_values[i] = (old_x_values[i-1] + old_x_values[i]) / 2.0f;
      new_x_values[n_bins] = old_x_values[n_bins-1] 
                      + (old_x_values[n_bins-1] - old_x_values[n_bins-2]) / 2; 
      x_scale = XScale.getInstance( new_x_values );      // checks if uniform!
    }

    init( d.getY_values() );
    if ( d.isSqrtErrors() )                 // continue using sqrt errors if d
      this.setSqrtErrors(true);             // did, otherwise copy the array
    else
      this.setErrors( d.getErrors() );

    if ( !d.isHistogram() && multiply )
    {
      if ( errors == null )
        for ( int i = 0; i < n_bins; i++ )
        {
          float dx = new_x_values[i+1] - new_x_values[i];
          y_values[i] = y_values[i] * dx;
        }
      else
        for ( int i = 0; i < n_bins; i++ )
        {
          float dx = new_x_values[i+1] - new_x_values[i];
          y_values[i] = y_values[i] * dx;
          errors[i]   = errors[i] * dx;        //#### how should
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
   *  controlled by the smooth_flag.  #### smooth_flag not implemented yet.
   *
   *  @param  x_value      the x value for which the corresponding y value is to
   *                       be interpolated
   *
   *  @param  smooth_flag  Currently, for a HistogramTable, the smooth_flag has
   *                       no effect.  The count within the bin containing the
   *                       the specified x is returned.  #####
   *
   *  @return approximate y value at the specified x value
   */
  public float getY_value( float x_value, int smooth_flag )
  {
    if ( x_value < x_scale.getStart_x() || 
         x_value > x_scale.getEnd_x()    )
      return 0.0f;

    int index = x_scale.getI_GLB( x_value );

    if ( index < 0 || index >= y_values.length )
      return 0.0f;
    else
      return y_values[index]; 
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
    return true;
  }  
 

  /**
   * Return a new Data object containing a copy of the x_scale, y_values
   * errors, group_id and attributes from the current Data object.
   *
   * @return  A "deep copy" clone of the current Data object as a generic 
   *          object.   
   */
  public Object clone()
  {
    Data temp = new HistogramTable( x_scale, y_values, errors, group_id );

                                      // copy the list of attributes.
    AttributeList attr_list = getAttributeList();
    temp.setAttributeList( attr_list );
    temp.selected = selected;
    temp.hide     = hide;

    return temp;
  }


  /**
   *  Resample the Data block on an arbitrarily spaced set of points given by
   *  the new_X scale parameter.  The histogram will be re-binned to form a 
   *  new histogram with the specified bin sizes. #### smooth_flag not 
   *  implemented yet 
   *
   *  @param new_X        The x scale giving the set of x values to use for the
   *                      resampling and/or rebinning operation.
   *
   *  @param smooth_flag  Flag indicating the degree of smoothing to be
   *                      applied. #### smooth_flag not not currently
   *                      implemented.
   */
  public void resample( XScale new_X, int smooth_flag )
  {
     ReBin( new_X );
     return;
  }


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
    int n_bins = x_scale.getNum_x() - 1;
    this.y_values = new float[ n_bins ];

    if ( y_values.length >= n_bins )
      System.arraycopy( y_values, 0, this.y_values, 0, n_bins );
    else
    {
      System.arraycopy( y_values, 0, this.y_values, 0, y_values.length );
      for ( int i = y_values.length; i < n_bins; i++ )
        this.y_values[i] = 0;
    }
  }


  /**
   * Alter this Data object by "rebinning" the y values of the current 
   * object to correspond to a new set of x "bins" given by the parameter
   * "new_X".  Also, rebin the error array.
   *
   * @param  new_X    This specifies the new set of "x" values to be used
   */
  private void ReBin( XScale new_X )
  {
                                           // Rebin the y_values
    float old_ys[] = arrayUtil.getPortion( y_values, x_scale.getNum_x() - 1 );
    float new_ys[] = new float[ new_X.getNum_x() - 1 ];

    if ( errors != null )
    {
      float new_errs[] = new float[ new_X.getNum_x() - 1 ];
      Sample.ReBin( x_scale.getXs(), old_ys, errors,
                    new_X.getXs(),   new_ys, new_errs );
      y_values = new_ys;
      errors   = new_errs;
    }
    else
    {
      Sample.ReBin( x_scale.getXs(), old_ys, new_X.getXs(), new_ys );
      y_values = new_ys;
    }

    x_scale  = new_X;
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
      System.out.println("Warning:HistogramTable IsawSerialVersion != 1");
  }

}
