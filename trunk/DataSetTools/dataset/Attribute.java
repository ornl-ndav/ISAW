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
 *  Revision 1.40  2003/08/11 22:17:06  dennis
 *  Added T0_SHIFT attribute, to record shift in T0 from SCD
 *  calibration.
 *
 *  Revision 1.39  2003/02/19 21:55:49  dennis
 *  Added flags for attribute "level of detail"
 *
 *  Revision 1.38  2003/02/18 18:58:26  dennis
 *  Added SAMPLE_ORIENTATION attribute name.
 *
 *  Revision 1.37  2003/02/12 20:05:14  dennis
 *  Removed SEGMENT_INFO and SEGMENT_INFO_LIST.
 *  Added RAW_DISTANCE for TOF_DG_SPECTROMETERs.
 *
 *  Revision 1.36  2003/02/07 19:45:06  dennis
 *  Added DETECTOR_DATA_GRID
 *
 *  Revision 1.35  2003/02/07 19:05:51  dennis
 *  Added PIXEL_INFO_LIST String.
 *
 *  Revision 1.34  2003/01/15 20:54:25  dennis
 *  Changed to use SegmentInfo, SegInfoListAttribute, etc.
 *
 *  Revision 1.33  2002/12/20 20:24:59  pfpeterson
 *  Added a new static String for user name.
 *
 *  Revision 1.32  2002/12/04 14:53:22  pfpeterson
 *  Added string constants for the start date and start time.
 *
 *  Revision 1.31  2002/11/27 23:14:06  pfpeterson
 *  standardized header
 *
 *  Revision 1.30  2002/11/12 00:20:18  dennis
 *  Made immutable by:
 *  1. remove setValue() method
 *  2. add() & combine() methods now return a new Attribute
 *
 *  Also:
 *  3. Since it is now immutable, clone() method is not needed and
 *     was removed
 *  4. Default constructor is now private, since the value can't
 *     be set from outside of the class
 *
 *  Revision 1.29  2002/09/25 16:49:17  pfpeterson
 *  Added string constants for SCD calibration information.
 *
 *  Revision 1.28  2002/08/22 15:08:22  pfpeterson
 *  Added string constants for lattice parameter, orientation matrix,
 *  and unit cell volume.
 *
 *  Revision 1.27  2002/08/06 14:00:15  pfpeterson
 *  Added constants for pressure and temperature data.
 *
 *  Revision 1.26  2002/08/01 22:36:55  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *  Added NeXus string constant names for analysis types.
 *  Removed redundant "transient" qualifier from static field.
 *
 *  Revision 1.25  2002/07/10 20:57:52  pfpeterson
 *  All string constants are now transient and finally define the
 *  serialVersionUID=1L (the first version when we are counting).
 *
 *  Revision 1.24  2002/07/10 16:00:44  pfpeterson
 *  Added new string constants for GSAS handling.
 *
 *  Revision 1.23  2002/06/05 20:26:35  dennis
 *  Now implements the IXmlIO interface using "stubs" that just return false
 *  for the read and write methods.  These MUST be overidden in derived classes.
 *  Also, now includes a maximum label length that is used when concatenating
 *  label attributes.
 *
 *  Revision 1.22  2002/06/05 13:47:14  dennis
 *  Made "name" a protected field rather than private, since the name
 *  will have to be read/written to XML files by subclasses.
 *
 *  Revision 1.21  2002/03/28 19:34:39  pfpeterson
 *  Added new string constant for TIME_OFFSET attribute.
 *
 *  Revision 1.20  2002/02/26 21:17:26  pfpeterson
 *  Added some string constants for SDDS data.
 *
 *  Revision 1.19  2002/02/25 17:35:51  pfpeterson
 *  Added DETECTOR_CEN_HEIGHT string constant.
 *
 *  Revision 1.18  2002/02/25 14:25:51  dennis
 *  Un-commented "constant" names for LABEL, SEGMENT_IDS, CRATE, SLOT, etc.
 *
 *  Revision 1.17  2002/02/04 22:59:41  dennis
 *  Temporarily added strings for Sample Chi, Phi and Omega for SCD.
 *  These are currently commented out.
 *
 *  Revision 1.16  2001/12/21 17:29:33  dennis
 *  Added attribute for Segment IDS.  This is currently commented
 *  out to avoud breaking the server<->client communication.
 *
 *  Revision 1.15  2001/12/14 22:19:19  dennis
 *  Added attributes for LABEL and nominal and actual SOURCE_TO_SAMPLE_TOF.
 *  These attributes are currently commented out to avoid breaking the
 *  server<->client communication.
 *
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
 */


abstract public class Attribute implements Serializable, 
                                           IXmlIO
{
  // NOTE: any field that is static or transient is NOT serialized.
  //
  // CHANGE THE "serialVersionUID" IF THE SERIALIZATION IS INCOMPATIBLE WITH
  // PREVIOUS VERSIONS, IN WAYS THAT CAN NOT BE FIXED BY THE readObject()
  // METHOD.  SEE "IsawSerialVersion" COMMENTS BELOW.  CHANGING THIS CAUSES
  // JAVA TO REFUSE TO READ DIFFERENT VERSIONS.
  //
  public  static final long serialVersionUID = 1L;

  public static final int     MAX_LABEL_LENGTH  = 80; 

  // integer codes for the type of attributes that should be included in a
  // DataSet or Data block.
  public static final int  NO_ATTRIBUTES       = 0; 
  public static final int  ANALYSIS_ATTRIBUTES = 1; 
  public static final int  FULL_ATTRIBUTES     = 2; 

  // Suggested names for attributes for neutron scattering data sets:

  public static final String  TITLE             = "DataSet Name";
  public static final String  LABEL             = "Label";
  public static final String  DS_TAG            = "DataSet Tag";
  public static final String  USER              = "User Name";

  public static final String  INST_NAME         = "Instrument Name";
  public static final String  INST_TYPE         = "Instrument Type";

  public static final String  FILE_NAME         = "File";
  public static final String  RUN_TITLE         = "Run Title";
  public static final String  RUN_NUM           = "Run Number";
  public static final String  END_DATE          = "End Date";
  public static final String  END_TIME          = "End Time";
  public static final String  START_DATE        = "Start Date";
  public static final String  START_TIME        = "Start Time";
  public static final String  UPDATE_TIME       = "Update Time";

  public static final String  DETECTOR_POS      = "Effective Position";
  public static final String  RAW_ANGLE         = "Raw Detector Angle";
  public static final String  RAW_DISTANCE      = "Ave. Raw Detector Distance";
  public static final String  SOLID_ANGLE       = "Total Solid Angle";
  public static final String  OMEGA             = "Omega";
  public static final String  DELTA_2THETA      = "\u0394"+"2"+"\u03b8";
  public static final String  EFFICIENCY_FACTOR = "Efficiency";
  public static final String  DETECTOR_IDS      = "Detector IDs";
  public static final String  SEGMENT_IDS       = "Segment IDs";
  public static final String  GROUP_ID          = "Group ID";
  public static final String  TIME_FIELD_TYPE   = "Time Field Type";
  public static final String  CRATE             = "Crate";
  public static final String  SLOT              = "Slot";
  public static final String  INPUT             = "Input";

  public static final String  DETECTOR_CEN_DISTANCE = 
                                                  "Detector center distance";
  public static final String  DETECTOR_CEN_ANGLE = 
                                                  "Detector center angle";
  public static final String  DETECTOR_CEN_HEIGHT = 
                                                  "Detector center height";
  public static final String  DETECTOR_DATA_GRID = "Detector data grid";

  public static final String  INITIAL_PATH      = "Initial Path";
  public static final String  ENERGY_IN         = "Energy In";
  public static final String  NOMINAL_ENERGY_IN =
                                                           "Nominal Energy In";
  public static final String  ENERGY_OUT        = "Energy Out";
  public static final String  NOMINAL_SOURCE_TO_SAMPLE_TOF = 
                                                "Nominal Source to Sample TOF";
  public static final String  SOURCE_TO_SAMPLE_TOF = 
                                                  "Source to Sample TOF";
  public static final String  T0_SHIFT          = "T0 shift";

  public static final String  SAMPLE_CHI        = "Sample Chi";
  public static final String  SAMPLE_PHI        = "Sample Phi";
  public static final String  SAMPLE_OMEGA      = "Sample Omega";
  public static final String  SAMPLE_ORIENTATION = "Sample Orientation";
  public static final String  SAMPLE_NAME       = "Sample Name";
  public static final String  TEMPERATURE       = "Temperature";
  public static final String  PRESSURE          = "Pressure";
  public static final String  MAGNETIC_FIELD    = "Magnetic Field";
  public static final String  NUMBER_OF_PULSES  = "Number of Pulses";
  public static final String  TOTAL_COUNT       = "Total Count";

  public static final String  Q_VALUE           = "Q(invA)";
  public static final String  GSAS_CALIB        = "GSAS calibration";
  public static final String  GSAS_IPARM        =
                                              "GSAS Instrument Parameter File";
  public static final String LATTICE_PARAM      = "Lattice Parameters";
  public static final String ORIENT_MATRIX      = "Orientation Matrix";
  public static final String ORIENT_FILE        = "Orientation File";
  public static final String CELL_VOLUME        = "Unit Cell Volume";
  public static final String SCD_CALIB          = "SCD Calibration";
  public static final String SCD_CALIB_FILE     = "SCD Calibration File";

  public static final String  PIXEL_INFO_LIST   = "Pixel Info List";
//  public static final String  SEGMENT_INFO_LIST = "Seg Info List";
//  public static final String  SEGMENT_INFO      = "Seg Info";

  public static final String  DS_TYPE            = "Data Set Type";

  // software grouping and time focusing
  public static final String  TIME_OFFSET        ="Time Offset";

  // stuff for SDDS files
  public static final String  START_TIME_SEC     = "Start Time(sec)";
  public static final String  TIME_OF_DAY        = "Time of Day";
  public static final String  DAY_OF_MONTH       = "Day of Month";


  // Suggested value Strings for DataSet attributes:

  public static final String  UNKNOWN            = "Unknown";
  public static final String  INVALID_DATA_SET   = "Invalid Data Set";
  public static final String  MONITOR_DATA       = "Monitor Data";
  public static final String  SAMPLE_DATA        = "Sample Data";
  public static final String  PULSE_HEIGHT_DATA  = "Pulse Height";
  public static final String  TEMPERATURE_DATA   = "Temperature Data";
  public static final String  PRESSURE_DATA      = "Pressure Data";

  public static final String TOF_DIFFRACTOMETER_S        = "TOFNPD";
  public static final String TOF_SCD_S                   = "TOFNSCD";
  public static final String TOF_SAD_S                   = "TOFNSAS"; 
  public static final String TOF_REFLECTROMETER_S        = "TOFNREF";
  public static final String TOF_DG_SPECTROMETER_S       = "TOFNDGS";
  public static final String TOF_IDG_SPECTROMETER_S      = "TOFNIGS";

  public static final String TRIPLE_AXIS_SPECTROMETER_S  = "MONONXTAS";
  public static final String MONO_CHROM_DIFFRACTOMETER_S = "MONONXPD";
  public static final String MONO_CHROM_SCD_S            = "MONONXSCD";
  public static final String MONO_CHROM_SAD_S            = "MONONXSAS";
  public static final String MONO_CHROM_REFLECTROMETER_S = "MONONXREF";
 
  // NOTE: The following fields are serialized.  If new fields are added that
  //       are not static, reasonable default values should be assigned in the
  //       readObject() method for compatibility with old servers, until the
  //       servers can be updated.

  private int IsawSerialVersion = 1;         // CHANGE THIS WHEN ADDING OR
                                             // REMOVING FIELDS, IF
                                             // readObject() CAN FIX ANY
                                             // COMPATIBILITY PROBLEMS
  protected String name;

  /**
   *  Since Attribute is an abstract class, this constructor is never called
   *  directly.
   *
   *  @param name  The name to give to this attribute.
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
   *
   * @param second_attribute  The second attribute whose value is compared to
   *                          the current attribute value.
   *
   * @return 1,0, or -1 based on comparing the value of the current attribute
   *         with the value of the second attribute.
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
   *
   * @return A reference to the String containing the attribute name. 
   */
  public String getName() { return name; }


  /**
   * Set the name for the attribute
   *
   * @param  name  The new name to use for this attribute. 
   */
  public void setName( String name )
  {
    this.name = name;
  }


  /**
   * Returns the value of the attribute, as a generic object
   *
   * @return the value of this attribute as a generic object.
   */
  abstract public Object getValue(); 


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
  public Attribute combine( Attribute attr )
  {
     // by default, no operation is done... the value of the combined attribute
     // is just the current value
     return this;
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
  public Attribute add( Attribute attr )
  {
    // by default, add will be the same as combine, unless this is overridden
    // in derived classes.
    return combine(attr);
  }


  /**
   * Get a numeric value to be used for sorting based on this attribute.
   *
   * @return Double.MAX_VALUE, unless this method is overridden in the
   *         concrete derived class.
   */
  public double getNumericValue()
  {
    return Double.MAX_VALUE;
  }


  /**
   * Returns a string representation of the value for this attribute
   *
   * @return String form of attribute value only.
   */
  abstract public String getStringValue();


  /**
   * Returns a string representation of the (name,value) pair for this
   * attribute.
   *
   * @return String form of attribute name, value.
   */
  abstract public String toString();


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

  /**
   *  Write the state of this object out to the specified stream in XML format.
   *
   *  @param  stream   The stream to write to.
   *  @param  mode     Flag indicating whether or not to write the value in
   *                   base 64 encoding.
   *
   *  @return false, derived classes must override this to actually be written
   *          to the stream. 
   */
  public boolean XMLwrite( OutputStream stream, int mode )
  {
    return false;
  }


  /**
   *  Read the state of this object from the specified stream in XML format.
   *
   *  @param  stream   The stream to read from.
   *
   *  @return false, derived classes must override this to actually be written
   *          to the stream. 
   */
  public boolean XMLread( InputStream stream )
  {
    return false;
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
      System.out.println("Warning:Attribute IsawSerialVersion != 1");
  }
}
