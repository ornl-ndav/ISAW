/*
 * File: TabulatedData.java 
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
 *  Revision 1.6  2002/06/19 22:37:00  dennis
 *  Minor cleanup of format.
 *
 *  Revision 1.5  2002/06/14 21:13:27  rmikk
 *  Implements IXmlIO interface
 *
 *  Revision 1.4  2002/04/19 15:42:33  dennis
 *  Revised Documentation
 *
 *  Revision 1.3  2002/04/04 18:25:10  dennis
 *  Added getInstance() methods to create HistogramTable or FunctionTable
 *  objects from any Data object.
 *  Moved scalar add(), subtract(), multiply() and divide() methods
 *  to Data.java
 *
 *  Revision 1.2  2002/03/18 21:38:21  dennis
 *  Minor fix to documentation.
 *
 *  Revision 1.1  2002/03/13 16:08:37  dennis
 *  Data class is now an abstract base class that implements IData
 *  interface. FunctionTable and HistogramTable are concrete derived
 *  classes for storing tabulated functions and frequency histograms
 *  respectively.
 */

package  DataSetTools.dataset;

import java.util.Vector;
import java.io.*;
import DataSetTools.math.*;
import DataSetTools.util.*;

/**
 * The abstract base class for a tabulated function data object.  This class
 * bundles together the basic data necessary to describe a tabulated function 
 * or frequency histogram of one variable.  An object of this class contains 
 * a list of "X" values and a list of "Y" values together with an extensible 
 * list of attributes for the object.  A list of errors for the "Y" values 
 * can also be kept.
 *  
 * @see DataSetTools.dataset.IData
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.FunctionTable
 * @see DataSetTools.dataset.HistogramTable
 *
 * @version 1.0  
 */

public abstract class TabulatedData extends    Data
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
  protected float  y_values[];
  protected float  errors[];

  /**
   * Constructs a Data object by specifying an "X" scale, "Y" values
   * and a group id for that data object.  
   *
   * @param   x_scale   the list of x values for this Data object 
   * @param   y_values  the list of y values for this Data object
   * @param   group_id  an integer id for this data object
   *
   */
  public TabulatedData( XScale  x_scale, float y_values[], int group_id )
  {
    super( x_scale, group_id );
    this.y_values = y_values;
    this.errors   = null;
  }


  /**
   * Constructs a Data object by specifying an "X" scale, "Y" values, an array 
   * of error values and a group_id. 
   *
   * @param   x_scale   the list of x values for this data object 
   * @param   y_values  the list of y values for this Data object
   * @param   errors    the list of error values for this data object.  The
   *                    length of the error list should be the same as the
   *                    length of the list of y_values.  
   * @param   group_id  an integer id for this data object
   *
   */
  public TabulatedData( XScale x_scale, 
                        float  y_values[], 
                        float  errors[], 
                        int    group_id )
  {
    this( x_scale, y_values, group_id );
    this.setErrors( errors );
  }


  /**
   * Get the table of y values for this TabulatedData object.
   *
   * @return a reference to the list of "Y" values for this Data object.
   */
  public float[] getY_values()
  { 
    return y_values;
  }


  /**
   *  Create an instance of a FunctionTable or HistogramTable object, using 
   *  values from given Data object.   The type of object constructed  
   *  (histogram or function) is determined by the "type" parameter.  If
   *  the requested type does not match the type of the given Data object,
   *  a conversion will be made.  For conversion purposes, histograms are 
   *  considered to be frequency histograms and functions are considered to
   *  be "density" functions.  That is, histograms are assumed to represent
   *  the number of events in each bin and functions are assumed to represent
   *  events per unit x.  Consequently, when a conversion is done, the 
   *  y values are scaled by the bin width.  Specifically, if "d" is a 
   *  function, the new histogram will have y_values given by:
   *  y_histogram = y_function * delta_x  where delta_x is the bin width. 
   *  A new x scale will be generated with bin boundaries centered between
   *  the original x_values.  Conversely, if "d" is a histogram, the new
   *  function will have y_values given by: y_function = y_histogram / delta_x.
   *  In this case, a new x scale will be generated using the bin centers. 
   *
   *  @param  d         The Data object that provides the y-values, x_scale,
   *                    errors and attributes for the new TabulatedData object
   *  @param  group_id  The group_id to use for the new TabulatedData object
   *  @param  type      String specifying the type of Tabulated data object 
   *                    to construct, Data.FUNCTION or Data.HISTOGRAM
   *
   *  @return  a new TabulatedData object with values determined by d.  
   */
  public static TabulatedData getInstance( Data d, int group_id, String type ) 
  {
    if ( type.equalsIgnoreCase( Data.FUNCTION ) )
      return new FunctionTable( d, true, group_id );
    else
      return new HistogramTable( d, true, group_id );
  }


  /**
   *  Create an instance of a FunctionTable or HistogramTable object, using 
   *  values from the given Data object.  If the given Data object is a
   *  histogram, this will construct a HistogramTable, otherwise it will
   *  construct a FunctionTable. 
   *
   *  @param  d         The Data object that provides the y-values, x_scale,
   *                    errors and attributes for the new TabulatedData object
   *  @param  group_id  The group_id to use for the new TabulatedData object
   *
   *  @return  a new TabulatedData object with values determined by d.
   *           If the given Data object, d, is a histogram, a HistogramTable
   *           will be returned, otherwise a FunctionTable will be returned. 
   */
  public static TabulatedData getInstance( Data d, int group_id )
  {
    if ( d.isHistogram() )
      return new HistogramTable( d, true, group_id );
    else
      return new FunctionTable( d, true, group_id );
  }


/**
 *  Get a list of "Y" values for this Data object, resampled at the x
 *  values specified by the XScale.
 *
 *  @param  x_scale  The XScale to be used for resampling the Data block.
 *
 *  @return  A new array listing approximate y-values corresponding to the
 *           given x-scale.  If the Data block is a histogram, the x-scale
 *           is considered to list the bin-boundaries and there will be one
 *           more bin-boundary than y-values.  If the Data block is a function,
 *           there will be the same number of y-values as x-values.
 */
public float[] getY_values( XScale x_scale, int smooth_flag ) //#############
{
  float y_vals[];

  int   num_x   = x_scale.getNum_x();
  if ( num_x > 1 )                          // resample over non-degenerate
  {                                         // interval
    Data data = (Data)this.clone();
    data.resample( x_scale, smooth_flag );
    y_vals = data.getY_values();
  }
  else                                      // just one point, so evaluate
  {                                         // at that point
    y_vals = new float[1];
    y_vals[0] = getY_value( x_scale.getStart_x(), smooth_flag );  
  }

  return y_vals;
}


  /**
   * Returns a reference to the list of the error values.  If no error values 
   * have been set, this returns null.
   *
   * @return the list of error estimates for this Data object. 
   */
  public float[] getErrors()
  { 
    return errors;
  }


 /**
   * Set the error array for this data object by copying the values from
   * the specified array.  If there are more error values than y values in
   * the data object, only the first "y_values.length" entries from the
   * error list will be used.  If there are fewer error values than y values,
   * the errors for the remaining y values will be set to 0. 
   *
   * @param   err        Array of error bounds to be used for this data 
   *                     object. 
   */ 
  public void setErrors( float  err[] )
  {
    if ( err == null )
    {
      this.errors = null;
      return;
    }

    this.errors = new float[ this.y_values.length ];
    int length = Math.min( y_values.length, err.length );
    System.arraycopy( err, 0, this.errors, 0, length ); 
    for ( int i = length; i < this.errors.length; i++ )
      this.errors[i] = 0;
  }

  /**
    * Set the error array for this data object to the square root of the
    * corresponding y value.
    */ 
  public void setSqrtErrors( )
  {
    this.errors = new float[ this.y_values.length ];
    for ( int i = 0; i < this.errors.length; i++ )
    {
      if ( this.y_values[i] >= 0 )
        this.errors[i] = (float)Math.sqrt( this.y_values[i] );
      else
        this.errors[i] = (float)Math.sqrt( -this.y_values[i] );
    }
  }


 /**
  *  Dump the index, x, y and error value to standard output
  *  
  *  @param first_index  The index of the first data point to dump
  *  @param last_index   The index of the last data point to dump
  *
  */
  public void print( int first_index, int last_index )
  {
    if ( first_index < 0 )
      first_index = 0;
    if ( first_index >= y_values.length )
      first_index = y_values.length - 1;

    if ( last_index < first_index )
      last_index = first_index;

    if ( last_index >= y_values.length )
      last_index = y_values.length - 1;

    float x[] = x_scale.getXs();
    for ( int i = first_index; i <= last_index; i++ )
    {
      System.out.print( Format.integer( i, 6 ) + " ");
      System.out.print( Format.real( x[i], 15, 6 ) + " " );
      System.out.print( Format.real( y_values[i], 15, 6 )+ " " );
      if ( errors != null )
        System.out.println( Format.real( errors[i], 15, 6 ) );
      else
        System.out.println();
    }
  } 


 /** Implements the IXmlIO interface so a Data Object can write itself
  *
  * @param stream  the OutputStream to which the data is written
  * @param mode    either IXmlIO.BASE64 or IXmlOP.NORMAL. This indicates 
  *                how a Data's xvals, yvals and errors are written
  * @return true if successful otherwise false<P>
  *
  * NOTE: This routine writes all of the begin and end tags.  These tag names
  *       are NOT TabulatedData but either HistogramTable or FunctionTable
  */
  public boolean XMLwrite( OutputStream stream, int mode)
  { 
    StringBuffer sb = new StringBuffer(1000);
    String DT = getClass().toString();
    DT = DT.substring(DT.lastIndexOf('.')+1);
    sb.append( "<"+DT+" ID=");
    sb.append("\""+getGroup_ID()+"\"");
    sb.append("  ysize=\"");
    sb.append(""+getY_values().length+"\" >\n<yvals>");
    float[] yvals=getY_values();
    byte[] b = xml_utils.convertToB64(yvals);

    if( b== null)
      return xml_utils.setError( xml_utils.getErrorMessage());

    try
    {
      stream.write( sb.toString().getBytes());
      stream.write( b);
      sb.delete(0,sb.length());
      sb.append("</yvals>\n<errors>");
      yvals=getErrors();
   
      b = xml_utils.convertToB64(yvals);
      if( b== null)
        return xml_utils.setError( xml_utils.getErrorMessage());
        
      stream.write( sb.toString().getBytes());
      stream.write( b);
      sb.delete(0,sb.length());
      sb.append("</errors>\n"); 
      stream.write( sb.toString().getBytes());
      AttributeList al = getAttributeList();
      if(!al.XMLwrite(stream, mode)    )
        return false;
      stream.write(("</"+DT+">\n").getBytes());
    }
    catch( Exception s)
    { 
      return xml_utils.setError("Exception3="+s.getClass()+"::"+
                       s.getMessage());
    }
    return true;  
  }


 /** Implements the IXmlIO interface so a Data Object can read itself
  *
  * @param stream  the InStream to which the data is written

  * @return true if successful otherwise false<P>
  *
  * NOTE: This routine assumes the begin tag has been completely read.  It reads
  *       the end tag.  The tag names 
  *       are NOT TabulatedData but either HistogramTable or FunctionTable
  */
  public boolean XMLread( InputStream stream )
  { 
    String Tag = xml_utils.getTag( stream);
    if(Tag == null)
      return xml_utils.setError( xml_utils.getErrorMessage());
    if(!Tag.equals("yvals"))
      return xml_utils.setError( "Improper tag in DATA. "+Tag+
               " should be yvals");
    if(!xml_utils.skipAttributes( stream ))
      return xml_utils.setError( xml_utils.getErrorMessage());
      
    String v = xml_utils.getValue( stream);
    if( v== null)
      return xml_utils.setError( xml_utils.getErrorMessage());
    Tag = xml_utils.getEndTag( stream);
    if( !Tag.equals("/yvals"))
      return xml_utils.setError( "Improper End Tag in Data. "+Tag+
           " should be /yvals");
      
    float[] yy =xml_utils.convertB64Tofloat(v.getBytes());
    if(yy.length != y_values.length)
      return xml_utils.setError( "incorrect yval lengths in DATA"+
                  "should be "+y_values.length+" is "+yy.length);
       
      
    System.arraycopy( yy,0, y_values,0, 
               java.lang.Math.max(y_values.length,yy.length));
        
 
//--------------errors
    Tag = xml_utils.getTag( stream);
    if(Tag == null)
      return xml_utils.setError( xml_utils.getErrorMessage());
    if(Tag.equals("errors"))
    {   
      if(!xml_utils.skipAttributes( stream))
        return xml_utils.setError( xml_utils.getErrorMessage());

      v = xml_utils.getValue( stream);
      if( v== null)
        return xml_utils.setError( xml_utils.getErrorMessage());
          
      Tag = xml_utils.getEndTag( stream);
      if( !Tag.equals("/errors"))
        return xml_utils.setError( "Improper End Tag in Data. "+Tag+
              " should be /yvals");
          
      yy =xml_utils.convertB64Tofloat(v.getBytes());
          
      if(yy.length != y_values.length)
        return xml_utils.setError( "incorrect error lengths in DATA");
         
         
      errors = yy;
      Tag = xml_utils.getTag( stream);
    }
  
//--------------Attributes
    if(Tag == null)
      return xml_utils.setError( xml_utils.getErrorMessage());
        
    if(Tag.equals( "AttributeList"))
    { if(!xml_utils.skipAttributes( stream ) )
        return xml_utils.setError( xml_utils.getErrorMessage());
         
      attr_list = new AttributeList();
      if(!attr_list.XMLread( stream ))
        return false;
          
      Tag = xml_utils.getTag( stream);
    }
       
//----------End tag
    if(Tag == null)
      return xml_utils.setError( xml_utils.getErrorMessage()); 
    if(!xml_utils.skipAttributes( stream ) )
      return xml_utils.setError( xml_utils.getErrorMessage());  
    String DT = getClass().toString();
    DT = DT.substring(DT.lastIndexOf('.')+1);
        
    if(!Tag.equals( "/"+DT))
    { return xml_utils.setError("Improper end tag. "+Tag+
             " should be /"+DT);
    }
      
    return true;
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
      System.out.println("Warning:TabulatedData IsawSerialVersion != 1");
  }

}
