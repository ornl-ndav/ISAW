/*
 * @(#)ReflectometerTofDataSetFactory.java     0.1  2000/06/01  Dennis Mikkelson
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.1  2000/07/10 22:22:21  dennis
 *  Basic DataSetFactory for specific instrument
 *
 *  Revision 1.1  2000/06/01 21:17:04  dennis
 *  Initial revision
 *
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.operator.*;

/**
 * The concrete class for "Factory" objects that produce properly 
 * configured, empty DataSets with operators suitable for a Time-of-Flight
 * Reflectometer.
 *
 * @version 0.1  
 */

public class ReflectometerTofDataSetFactory extends    DataSetFactory
                                            implements Serializable
{
  /**
   * Constructs a data set "factory" that will produce empty DataSets with 
   * the specified title, units, labels and a list of operators suitable for 
   * a Time-of-Flight Reflectometer. 
   *
   * @param   title     String giving a title for the DataSets produced.
   * @param   x_units   String specifying the units for the "X" axis.  This 
   *                    should be specified in a standard form. 
   * @param   x_label   String identifying the quantity measured in the "X"
   *                    direction. 
   * @param   y_units   String specifying the units for the "Y" axis.  This 
   *                    should be specified in a standard form. 
   * @param   y_label   String identifying the quantity measured in the "Y"
   *                    direction. 
   */
  public ReflectometerTofDataSetFactory( String  title, 
                                         String  x_units,
                                         String  x_label,
                                         String  y_units,
                                         String  y_label )
  {
    super( title, x_units, x_label, y_units, y_label );
  }

  /**
   * Constructs a data set "factory" that will produce empty DataSets with the 
   * specified title, default values for the units and labels together with a 
   * list of operators suitable for a Time-of-Flight Reflectometer.
   * 
   * @param   title     String giving a title for the DataSets produced.
   */
  public ReflectometerTofDataSetFactory( String  title )
  {
    this( title, 
         "Time(us)", "Time-of-flight", 
         "Counts", "Scattering Intensity" );
  }

  /**
   * Get a new empty data set with the title, units, label, ID and initial log 
   * info determined by the parameters stored in the DataSetFactory.  The new 
   * DataSet also contains a list of operators suitable for use with a DataSet 
   * from a Time-of-Flight .
   */
  public DataSet getDataSet()
  {
    DataSet new_ds = super.getDataSet();    // get basic DataSet and list of
                                            // operators from the super class 

                                                    // add the list of TOF
                                                    // Reflectometer operations
    new_ds.addOperator( new TofToChannel() );
    new_ds.addOperator( new TrueAngle() );

    return new_ds;
  }

}
