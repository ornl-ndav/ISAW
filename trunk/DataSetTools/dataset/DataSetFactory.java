/*
 * @(#)DataSetFactory.java     0.1  99/06/07  Dennis Mikkelson
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.3  2000/07/10 22:23:55  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.18  2000/06/15 14:12:25  dennis
 *  Replaced 4 operators with renamed versions for consistency:
 *    Integrate()         replaced by    IntegrateGroup()
 *    CalculateMoment()   replaced by    CalculateMomentOfGroup()
 *    SumSelectedData()   replaced by    SumByAttribute()
 *    SelectData()        replaced by    ExtractByAttribute()
 *
 *  Revision 1.17  2000/06/08 15:10:16  dennis
 *  Added new operator DeleteByAttribute
 *
 *  Revision 1.16  2000/05/23 18:51:50  dennis
 *  removed sort on one attribute operator.
 *
 *  Revision 1.15  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 *
 */

package  DataSetTools.dataset;

import java.io.*;
import DataSetTools.operator.*;

/**
 * The concrete root class for "Factory" objects that produce properly 
 * configured, empty DataSets.
 *
 * @version 0.1  
 */

public class DataSetFactory implements Serializable
{
  private String        title;
  private String        x_units;
  private String        x_label;
  private String        y_units;
  private String        y_label;
  private String        log_info;

  /**
   * Constructs a data set "factory" that will produce empty DataSets with 
   * the specified title, units, labels and a list of operators suitable for 
   * any data set. 
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
  public DataSetFactory( String  title, 
                         String  x_units,
                         String  x_label,
                         String  y_units,
                         String  y_label )
  {
    this.title    = title;
    this.x_units  = x_units;
    this.x_label  = x_label;
    this.y_units  = y_units;
    this.y_label  = y_label;
    this.log_info = null;
  }

  /**
   * Constructs a data set "factory" that will produce empty DataSets with the 
   * specified title, default values for the units and labels together with a 
   * list of operators suitable for any data set.
   * 
   * @param   title     String giving a title for the DataSets produced.
   *
   */
  public DataSetFactory( String title )
  {
    this( title, "X UNITS", "X LABEL", "Y UNITS", "Y LABEL" );
  }

  /**
   * Sets the title to be applied to subsequent DataSets produced by this 
   * factory.
   *
   * @param  title   The String to use for the title for new DataSets 
   */
  public void setTitle( String title ) { this.title = title; }


  /**
   * Sets a log message to be used as the first log entry for subsequent 
   * DataSets produced by this factory.
   *
   * @param log_info   The String to be used for the first log entry for new 
   *                   DataSets
   */
  public void setLog_entry( String log_info )
  {
    this.log_info = log_info;
  }

  /**
   * Sets the units for the "X" axis to be applied to subsequent DataSets 
   * produced by this factory. 
   *
   * @param  units   String giving the units for the "X" axis
   */
  public void setX_units( String units ) { this.x_units = units; }

  /**
   * Sets the label for the "X" axis to be applied to subsequent DataSets 
   * produced by this factory.
   *
   * @param  label  String giving the label for the "X" axis
   */
  public void setX_label( String label ) { this.x_label = label; }

  /**
   * Sets the units for the "Y" scale 
   *
   * @param  units   String giving the units for the "Y" axis
   */
  public void setY_units( String units ) { this.y_units = units; }


  /**
   * Sets the label for the "Y" axis to be applied to subsequent DataSets 
   * produced by this factory.
   *
   * @param  label  String giving the label for the "Y" axis
   */
  public void setY_label( String label ) { this.y_label = label; }

  /**
   * Get a new empty data set with the title, units, label, ID and initial log 
   * info determined by the parameters stored in the DataSetFactory.  The new 
   * DataSet also contains a list of operators suitable for use with any 
   * DataSet.
   */
  public DataSet getDataSet()
  {
    DataSet new_ds = new DataSet( title,             // construct new data set
                                  new OperationLog(), 
                                  x_units, 
                                  x_label, 
                                  y_units, 
                                  y_label );
                                                    // set initial log entry
    if ( log_info != null )
      new_ds.addLog_entry( log_info );
                                                    // add the list of generic
                                                    // data set operations
    new_ds.addOperator( new DataSetScalarAdd() );
    new_ds.addOperator( new DataSetScalarSubtract() );
    new_ds.addOperator( new DataSetScalarMultiply() );
    new_ds.addOperator( new DataSetScalarDivide() );

    new_ds.addOperator( new DataSetAdd() );
    new_ds.addOperator( new DataSetSubtract() );
    new_ds.addOperator( new DataSetMultiply() );
    new_ds.addOperator( new DataSetDivide()   );

    new_ds.addOperator( new IntegrateGroup() );
    new_ds.addOperator( new CalculateMomentOfGroup() );
    new_ds.addOperator( new DataSetCrossSection() );
    new_ds.addOperator( new SumByAttribute() );
    new_ds.addOperator( new ExtractByAttribute() );

    new_ds.addOperator( new DataSetSort() );
    new_ds.addOperator( new DataSetMultiSort() );

    new_ds.addOperator( new DeleteByAttribute() );
    new_ds.addOperator( new DeleteCurrentlySelected() );
    new_ds.addOperator( new SumCurrentlySelected() );

    new_ds.addOperator( new DataSetMerge() );

    return new_ds;
  }

}
