/*
 * File:  Attribute.java
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
 *  Revision 1.13  2001/08/16 14:35:39  rmikk
 *  Added IntListString case BEFORE SpecialString so it will
 *  be dealt with correctly
 *
 *  Revision 1.12  2001/07/30 18:46:46  dennis
 *  Added DS_TYPE attribute and some suggested type names.
 *
 *  Revision 1.11  2001/07/10 19:08:39  dennis
 *  Added attributes for Omega, DETECTOR_INFO and
 *  DETECTOR_INFO_LIST.
 *
 *  Revision 1.10  2001/04/25 19:03:26  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.9  2001/02/09 14:18:34  dennis
 *  Changed CURRENT_TIME attribute to UPDATE_TIME.
 *
 *  Revision 1.8  2001/02/02 20:53:05  dennis
 *  Added CURRENT_TIME attribute for LiveDataServer.
 *
 *  Revision 1.7  2000/11/17 23:45:55  dennis
 *  Added method Build( name, value ) to construct an Attribute of the correct
 *  subclass based on the type of the Object "value".
 *
 *  Revision 1.6  2000/11/10 22:50:00  dennis
 *  Added constant for Q_VALUE
 *
 *  Revision 1.5  2000/07/26 14:52:20  dennis
 *  Now includes method to add() attributes.
 *
 *  Revision 1.4  2000/07/13 14:29:33  dennis
 *  Removed redundant TOTAL_COUNTS attribute, leaving only TOTAL_COUNT
 *
 *  Revision 1.3  2000/07/10 22:23:51  dennis
 *  Now using CVS 
 *
 *  Revision 1.12  2000/05/23 18:51:50  dennis
 *  Added attributes for solid angle, efficiency and delta 2 theta
 *
 *  Revision 1.11  2000/05/18 21:27:40  dennis
 *  Added the FILE_NAME and TIME_FIELD_TYPE attribute names.
 *
 *  Revision 1.10  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 *  99/06/02, 1.01, Added methods to set the attribute's name and value.
 *                  Added method "compare" to compare objects.
 *                  Removed method "greater_than" to compare objects.
 */

package  DataSetTools.dataset;

import  java.io.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;

/**
 * The abstract root class for attributes used in data objects.  Attribute
 * is an abstract class that bundles a name and value together.  Each concrete
 * subclass will use a particular type of value.  That is, the value may
 * be a float, a character string, an array of floats, etc.  A method to 
 * compare the values of two attribute objects is also provided. 
 *  
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.IntAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.IntListAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 * @see DataSetTools.dataset.DetPosAttribute
 *
 * @version 1.0  
 */


abstract public class Attribute implements Serializable
{

  // Suggested names for attributes for neutron scattering data sets:

  public static final String  TITLE             = "DataSet Name";
  public static final String  DS_TAG            = "DataSet Tag";

  public static final String  INST_NAME         = "Instrument Name";
  public static final String  INST_TYPE         = "Instrument Type";

  public static final String  FILE_NAME         = "File";
  public static final String  RUN_TITLE         = "Run Title";
  public static final String  RUN_NUM           = "Run Number";
  public static final String  END_DATE          = "End Date";
  public static final String  END_TIME          = "End Time";
  public static final String  UPDATE_TIME       = "Update Time";

  public static final String  DETECTOR_POS      = "Effective Position";
  public static final String  RAW_ANGLE         = "Raw Detector Angle";
  public static final String  SOLID_ANGLE       = "Total Solid Angle";
  public static final String  OMEGA             = "Omega";
  public static final String  DELTA_2THETA      = "\u0394"+"2"+"\u03b8";
  public static final String  EFFICIENCY_FACTOR = "Efficiency";
  public static final String  DETECTOR_IDS      = "Detector IDs";
  public static final String  GROUP_ID          = "Group ID";
  public static final String  TIME_FIELD_TYPE   = "Time Field Type";

  public static final String  INITIAL_PATH      = "Initial Path";
  public static final String  ENERGY_IN         = "Energy In";
  public static final String  NOMINAL_ENERGY_IN = "Nominal Energy In";
  public static final String  ENERGY_OUT        = "Energy Out";

  public static final String  SAMPLE_NAME       = "Sample Name";
  public static final String  TEMPERATURE       = "Temperature";
  public static final String  PRESSURE          = "Pressure";
  public static final String  MAGNETIC_FIELD    = "Magnetic Field";
  public static final String  NUMBER_OF_PULSES  = "Number of Pulses";
  public static final String  TOTAL_COUNT       = "Total Count";

  public static final String  Q_VALUE           = "Q(invA)";

  public static final String  DETECTOR_INFO_LIST = "Det Info List";
  public static final String  DETECTOR_INFO      = "Det Info";

  public static final String  DS_TYPE            = "Data Set Type";

  // Suggested value Strings for DataSet attributes:

  public static final String  UNKNOWN            = "Unknown";
  public static final String  MONITOR_DATA       = "Monitor Data";
  public static final String  SAMPLE_DATA        = "Sample Data";
  public static final String  PULSE_HEIGHT_DATA  = "Pulse Height";

 
  private String name;


  /**
   *  Since Attribute is an abstract class, this constructor is never called
   *  directly.
   */

  protected Attribute( String name )
  {
    this.name = name;
  }

  /**
   * Returns an integer flag to indicate if the value of the current Attribute
   * is "greater", "equal" or "less" than the value of the specified 
   * second_attribute.  The meaning of "greater", "equal" and "less" will 
   * depend on the specific concrete attributes.  The attribute parameter
   * "second_attribute" should have the same class as the current attribute.
   * The return value is:
   *         +1       if the current value is greater than the second value
   *          0       if the current value is equal to the second value
   *         -1       if the current value is less than the second value
   */

  public int compare( Attribute second_attribute )

  {                                               // compare strings, if that's
                                                  // what we have
    if ( this  instanceof StringAttribute && 
         second_attribute instanceof StringAttribute      )
      return ((String)getValue()).compareTo(
              (String)second_attribute.getValue() );

                                              // otherwise compare attributes 
                                              // based on numeric value

    double this_key   = getNumericValue(); 
    double second_key = second_attribute.getNumericValue();

    if ( this_key > second_key )
      return( 1 );
    else if ( this_key == second_key )
      return( 0 );
    else
      return( -1 );
  }


  /**
   * Returns the name of the attribute 
   */
  public String getName() { return name; }


  /**
   * Set the name for the attribute
   */
  public void setName( String name )
  {
    this.name = name;
  }


  /**
   * Returns the value of the attribute, as a generic object
   */
  abstract public Object getValue(); 


  /**
   * Set the value for the attribute using a generic object.  The actual
   * class of the object must be appropriate to the concrete attribute class
   * used.
   */
  abstract public boolean setValue( Object obj );


  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  Any 
   * non-trivial behavior will be specified in derived classes.  For example,
   * combining StringAttributes could mean concatenating the strings while 
   * combining FloatAttributes could mean averaging the float values. 
   *
   *  @param   attr   An attribute whose value is to be "combined" with the 
   *                  value of the this attribute.
   *                  
   */
  public void combine( Attribute attr )
  {
     // by default, no operation is done... the value of the combined attribute
     // is just the current value
  }

  /**
   * Add the value of the specified attribute to the value of this
   * attribute obtain a new value for this attribute.  The default behavior
   * is just to call combine().  This will be overidden in derived classes,
   * when special behavior needed. 
   *
   *  @param   attr   An attribute whose value is to be "added" to the
   *                  value of the this attribute.
   *
   */
  public void add( Attribute attr )
  {
    // by default, add will be the same as combine, unless this is overridden
    // in derived classes.
    combine(attr);
  }



  /**
   * Get a numeric value to be used for sorting based on this attribute.
   */
  public double getNumericValue()
  {
    return Double.MAX_VALUE;
  }


  /**
   * Returns a string representation of the value for this attribute
   */
  abstract public String getStringValue();


  /**
   * Returns a string representation of the (name,value) pair for this
   * attribute
   */
  abstract public String toString();


  /**
   * Returns a copy of the current attribute
   */
  abstract public Object clone();


  /**
   *  Build an Attribute object, if possible, given a title string and an
   *  object holding the value for the Attribute.
   *
   *  @param  name     String holding the name of the attribute.
   *  @param  value    An Object holding the value for the attribute.  
   *                   Integer, Float, Double, String, Integer[] and
   *                   DetectorPosition are supported.
   *
   *  @return  If it is possible to construct an Attribute using the specified
   *           Object, this returns an Attribute of the correct type.  If not,
   *           this returns an ErrorString object. 
   */
  public static Object Build( String name, Object value )
  {
    Attribute A = null;

    if ( value == null )
      return new ErrorString(" null value object");

    if ( name == null )
      return new ErrorString(" null name object");

    else if( value instanceof Integer)
      A = new IntAttribute(name, ((Integer)value).intValue());

    else if( value instanceof Float)
      A = new FloatAttribute( name , ((Float)value).floatValue());

    else if( value instanceof Double)
      A = new DoubleAttribute( name , ((Double)value).doubleValue());

    else if( value instanceof String)
      A = new StringAttribute( name , (String) value);

  
    else if( value instanceof DetectorPosition )
      A = new DetPosAttribute( name, (DetectorPosition)value );

    else if( value instanceof int[] )
      A = new IntListAttribute( name , (int[])value );
    
    else if( value instanceof IntListString)
	{ 
          A = new IntListAttribute( name, 
            IntList.ToArray(((IntListString)value).toString()));
        }
    else if( value instanceof Integer[] )          // copy values into an int[]
    {
      int n_vals = ((Integer[])value).length;
      int vals[] = new int[ n_vals ];
      for ( int i = 0; i < n_vals; i++ )
        vals[i] = ((Integer[])value)[i].intValue();
      A = new IntListAttribute( name , vals );
    }

    else if( value instanceof SpecialString)
      A = new StringAttribute( name, value.toString());

    else
      return new ErrorString(" can't build Attribute for " + 
                               value.getClass().getName()  );
   
    return A;
  }

}
