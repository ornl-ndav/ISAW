/*
 * @(#)Attribute.java     1.01 99/06/02  Dennis Mikkelson
 *
 *  99/06/02, 1.01, Added methods to set the attribute's name and value.
 *                  Added method "compare" to compare objects.
 *                  Removed method "greater_than" to compare objects.
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.4  2000/07/13 14:29:33  dennis
 *  Removed redundant TOTAL_COUNTS attribute, leaving only TOTAL_COUNT
 *
 *  Revision 1.3  2000/07/10 22:23:51  dennis
 *  July 10, 2000 version... many changes
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
 *
 */

package  DataSetTools.dataset;

import java.io.*;

/**
 * The abstract root class for attributes used in data objects.  Attribute
 * is an abstract class that bundles a name and value together.  Each concrete
 * subclass will use a particular type of value.  That is, the value may
 * be a float, a character string, an array of floats, etc.  A method to 
 * compare the values of two attribute objects is also provided. 
 *  
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.IntAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 * @see DataSetTools.dataset.DetPosAttribute
 *
 * @version 1.0  
 */


abstract public class Attribute implements Serializable
{

  // Suggested names for attributes for neutron scattering data sets.

  public static final String  TITLE             = "DataSet Name";
  public static final String  DS_TAG            = "DataSet Tag";

  public static final String  INST_NAME         = "Instrument Name";
  public static final String  INST_TYPE         = "Instrument Type";

  public static final String  FILE_NAME         = "File";
  public static final String  RUN_TITLE         = "Run Title";
  public static final String  RUN_NUM           = "Run Number";
  public static final String  END_DATE          = "End Date";
  public static final String  END_TIME          = "End Time";

  public static final String  DETECTOR_POS      = "Effective Position";
  public static final String  RAW_ANGLE         = "Raw Detector Angle";
  public static final String  SOLID_ANGLE       = "Total Solid Angle";
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

}
