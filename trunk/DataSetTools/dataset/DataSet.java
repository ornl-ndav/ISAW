/*
 * @(#)DataSet.java     1.02  99/06/04  Dennis Mikkelson
 *
 * 1.02  99/06/04  Added a vector of operations to the data set.  When a
 *                 data set is constructed, the list of operations that are
 *                 allowed on this data set should also be specified.
 */

package  DataSetTools.dataset;

import java.util.Vector;
import java.io.*;
import java.lang.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;

/**
 * The concrete root class for a set of data objects.  A DataSet object
 * bundles together a vector of Data objects with associated units, labels,
 * title, attributes and log information.  Data objects can be added to or 
 * removed from the data set using the methods of this class.  Also, the
 * list of attributes is extensible and can be accessed through methods in
 * this class.
 *
 * @see DataSetTools.dataset.Data
 *
 * @version 1.02  
 */

public class DataSet implements IAttributeList,
                                Serializable
{
  private String        title;      // NOTE: we force a DataSet to have a title
                                    // and also keep the same title as an
                                    // attribute.  The title can only be 
                                    // changed using the setTitle() method.
  private String        x_units;
  private String        x_label;
  private String        y_units;
  private String        y_label;
  private AttributeList attr_list;
  private Vector        data;
  private Vector        operators;
  private OperationLog  op_log;

  /**
   * Constructs an empty data set with the specified title, initial log
   * object, units and labels.
   *
   * @param   title     String giving a title for the data set.
   * @param   op_log    OperationLog object giving the initial log record
   *                    for this data set. 
   * @param   x_units   String specifying the units for the "X" axis.  This 
   *                    should be specified in a standard form. 
   * @param   x_label   String identifying the quantity measured in the "X"
   *                    direction. 
   * @param   y_units   String specifying the units for the "Y" axis.  This 
   *                    should be specified in a standard form. 
   * @param   y_label   String identifying the quantity measured in the "Y"
   *                    direction. 
   *
   * @see DataSetTools.dataset.Data
   */
  public DataSet( String        title, 
                  OperationLog  op_log,
                  String        x_units,
                  String        x_label,
                  String        y_units,
                  String        y_label )
  {
    this.op_log     = (OperationLog)op_log.clone();
    this.x_units    = x_units;
    this.x_label    = x_label;
    this.y_units    = y_units;
    this.y_label    = y_label;
    this.attr_list  = new AttributeList();
    this.data       = new Vector();
    this.operators  = new Vector();

    setTitle( title );
  }

  /**
   * Constructs an empty data set with the specified title, initial log 
   * string, units and labels. 
   *
   * @param   title     String giving a title for the data set.
   * @param   log_info  String giving log information for the data set.
   * @param   x_units   String specifying the units for the "X" axis.  This 
   *                    should be specified in a standard form. 
   * @param   x_label   String identifying the quantity measured in the "X"
   *                    direction. 
   * @param   y_units   String specifying the units for the "Y" axis.  This 
   *                    should be specified in a standard form. 
   * @param   y_label   String identifying the quantity measured in the "Y"
   *                    direction. 
   *
   * @see DataSetTools.dataset.Data
   */
  public DataSet( String  title, 
                  String  log_info,
                  String  x_units,
                  String  x_label,
                  String  y_units,
                  String  y_label )
  {
    this( title, new OperationLog(), x_units, x_label, y_units, y_label );
    this.op_log.addEntry( log_info );
  }


  /**
   * Constructs an empty data set with the specified title and log information.
   * The units and labels are assigned default values.
   *
   * @param   title     String giving a title for the data set.
   * @param   log_info  String giving log information for the data set.
   *
   * @see DataSetTools.dataset.Data
   */
  public DataSet( String  title, String  log_info )
  {
    this( title, log_info, "X UNITS", "X LABEL", "Y UNITS", "Y LABEL");
  }


  /**
   * Returns the title of the data set 
   */
  public String getTitle() { return title; }


  /**
   * Sets the title of the DataSet both as an instance variable and as
   * an attribute of the DataSet.
   *
   * @param  title   The String to use for the new title of the data set
   */
  public void setTitle( String title ) 
  { 
    this.title = title; 

    StringAttribute title_attr = new StringAttribute( Attribute.TITLE, title );
    attr_list.setAttribute( title_attr, 0 );
  }


  /**
   * Returns the log information for the data set 
   */
  public OperationLog getOp_log() { return op_log; }


  /**
   * Set the entire operation log for the data set
   */
  public void setOp_log( OperationLog op_log ) 
  { 
    this.op_log = (OperationLog)op_log.clone(); 
  }

  /**
   * Copy the operation log from the specified the data set
   */
  public void copyOp_log( DataSet data_set )
  { 
    this.op_log = (OperationLog)data_set.getOp_log().clone();
  }


  /**
   * Adds a new entry to the log information for the data set 
   *
   * @param log_info   The String to be added to the log information for
   *                   this data set.
   */
  public void addLog_entry( String log_info )
  {
    this.op_log.addEntry( log_info );
  }


  /**
   * Returns the number of tabulated data objects in the data set 
   */
  public int getNum_entries() { return data.size(); }


  /**
   * Returns a reference to the first data object in this data set with the 
   * specified group ID.  If there is no data object with the correct ID, this
   * returns null.
   *
   * @param  gourp_id      The group id of the requested Data object in the 
   *                       list of Data objects in this DataSet.
   */
  public Data getData_entry_with_id( int group_id )
  {
     for ( int i = 0; i < data.size(); i++ )
       if ( ((Data)data.elementAt( i )).getGroup_ID() == group_id )
         return (Data)data.elementAt( i );

     return null;     // if we didn't find the right id
  }


  /**
   * Returns a reference to the tabulated data object from the specified 
   * position in the data set if the index is valid, otherwise return null.
   *
   * @param  index   The index of the requested Data object in the list of
   *                 Data objects in this DataSet. 
   */
  public Data getData_entry( int index ) 
  {
     if ( index >= 0 && index < data.size() )
       return (Data)data.elementAt( index ); 
     else
       return null;
  }


  /**
   * Adds a new tabulated data object to the list of data objects.
   *
   * @param  entry   The Data object to be added to the list of Data objects
   *                 in this DataSet.
   */
  public void addData_entry( Data entry )
  {
    data.addElement( entry );
  }


  /**
   * Removes the data entry from the specified position in the data set.
   *
   * @param  index  The index of the Data object to be removed from the list
   *                of Data objects in this DataSet.
   */
  public void removeData_entry( int index )
  {
     data.removeElementAt( index );
  }


  /**
   * Removes the first data object in this data set with the specified group id.
   * If there is no data object with the correct ID, this has no effect.
   *
   * @param  group_id      The group_id of the requested Data object in the 
   *                       list of Data objects in this DataSet.
   */
  public void removeData_entry_with_id( int group_id )
  {
     for ( int i = 0; i < data.size(); i++ )
       if ( ((Data)data.elementAt( i )).getGroup_ID() == group_id )
       {
         data.removeElementAt(i);           // found the id, so remove the
         return;                            // the data block and exit
       }
  }


  /**
   * Returns the number of operators for the DataSet
   */
  public int getNum_operators() { return operators.size(); }


  /**
   * Returns a reference to the specified operation in the list of available
   * operators on this data set.
   *
   * @param  index   The index of the requested operation in the list of
   *                 operators in this DataSet.
   */
  public DataSetOperator getOperator( int index )
  {
     return (DataSetOperator)operators.elementAt( index );
  }


  /**
   * Returns a reference to the operation in the list of available operators 
   * on this data set with the specified TITLE.  If the named operator is not
   * in the list, this method returns null.
   *
   * @param  title   The title of the requested operation 
   */
  public DataSetOperator getOperator( String title )
  {
     int     num_ops = getNum_operators();
     int     i       = 0;

     DataSetOperator op;
     while ( i < num_ops )
     {
       op = (DataSetOperator)operators.elementAt( i );
       if ( title.equalsIgnoreCase( op.getTitle() ) )
         return op;
       else
         i++;
     }
    
     return null;
  }


  /**
   * Adds a new operator to the list of operators for this data set.
   *
   * @param  operator    The operation to be added to the list of operations
   *                     in this DataSet.
   */
  public void addOperator( DataSetOperator operator )
  {
    operator.setDataSet( this );
    operators.addElement( operator );
  }


  /**
   * Get the range of X values for the collection of Data blocks in this
   * data set.
   */
  public UniformXScale getXRange()
  {
    Data           data_block;
    UniformXScale  range;

    if ( this.getNum_entries() < 1 )
      return null;
    else
    {
      data_block = (Data)getData_entry( 0 );
      range      = new UniformXScale( data_block.getX_scale().getStart_x(),
                                      data_block.getX_scale().getEnd_x(),
                                      2                     );
                           // an X "range" is a UniformXScale with only
                           // two entries. 
    }

    for ( int i = 1; i < this.getNum_entries(); i++ )
    {
      data_block = (Data)getData_entry( i );
      range.expand( data_block.getX_scale() );
    }

    return range;
  }


  /**
   * Get the range of Y values for the collection of Data blocks in this
   * data set.
   */
  public UniformXScale getYRange()
  {
    Data           data_block;
    UniformXScale  range;
    float          min_y, max_y, min, max;

    if ( this.getNum_entries() < 1 )
      return null;
    else
    {
      data_block = (Data)getData_entry( 0 );
      min = arrayUtil.getMin( data_block.getY_values() );
      max = arrayUtil.getMax( data_block.getY_values() );

      for ( int i = 0; i < this.getNum_entries(); i++ )
      {
        data_block = (Data)getData_entry( i );
        min_y = arrayUtil.getMin( data_block.getY_values() );
        if ( min_y < min )
          min = min_y;

        max_y = arrayUtil.getMax( data_block.getY_values() );
        if ( max_y > max )
          max = max_y;
      }
      range = new UniformXScale( min, max, 2 );

      return range;
    }
  }




  /**
   * Get the maximum number of X steps for the Data blocks in this DataSet.
   *
   *  @return   An integer giving the largest number of X steps for any 
   *            Data block in this DataSet
   */
  public int getMaxXSteps()
  {
    Data           data_block;
    XScale         x_scale;
    int            num_steps;
    int            max_steps = 0;

    if ( this.getNum_entries() > 0 )
    {
      data_block = (Data)getData_entry( 0 );
      x_scale    = data_block.getX_scale();
      num_steps  = x_scale.getNum_x();

      if ( num_steps > max_steps )
        max_steps = num_steps;
    }

    return max_steps;
  }



  /**
   * Returns true if the current DataSet has the same x and y units as the
   * specified DataSet. 
   *
   * @param  ds  the DataSet whose units are compared to those of the current
   *             DataSet
   *
   * @return  returns true if the x and y units of the DataSets match, 
   *          retruns false otherwise
   */
  public boolean SameUnits( DataSet ds ) 
  { 
     System.out.println("this x_units = " + x_units );
     System.out.println("this y_units = " + y_units );
     System.out.println("ds   x_units = " + ds.x_units );
     System.out.println("ds   y_units = " + ds.y_units );

     if ( x_units.equalsIgnoreCase( ds.x_units )  &&
          y_units.equalsIgnoreCase( ds.y_units )     )
       return true;

     else
       return false;
  }

  /**
   * Returns the units for the "X" axis 
   */
  public String getX_units() { return x_units; }

  /**
   * Sets the units for the "X" axis 
   *
   * @param  units   String giving the units for the "X" axis
   */
  public void setX_units( String units ) { this.x_units = units; }

  /**
   * Returns the label for the "X" axis 
   */
  public String getX_label() { return x_label; }

  /**
   * Sets the label for the "X" axis 
   *
   * @param  label  String giving the label for the "X" axis
   */
  public void setX_label( String label ) { this.x_label = label; }

  /**
   * Returns the units for the "Y" scale 
   */
  public String getY_units() { return y_units; }

  /**
   * Sets the units for the "Y" scale 
   *
   * @param  units   String giving the units for the "Y" axis
   */
  public void setY_units( String units ) { this.y_units = units; }

  /**
   * Returns the label for the "Y" axis 
   */
  public String getY_label() { return y_label; }

  /**
   * Sets the label for the "Y" axis 
   *
   * @param  label  String giving the label for the "Y" axis
   */
  public void setY_label( String label ) { this.y_label = label; }


  /**
   *  Get a copy of the list of attributes for this Data object.
   */
  public AttributeList getAttributeList()
  {
    return (AttributeList)attr_list.clone();
  }

  /**
   *  Set the list of attributes for this Data object to be a COPY of the 
   *  specified list of attributes.
   */
  public void setAttributeList( AttributeList attr_list )
  {
    this.attr_list = (AttributeList)attr_list.clone();

    setTitle( this.title );   // force the attribute list to contain the
                              // correct title
  }


  /**
   *  Sort the list of Data entries based on the specified attribute.
   *
   *  @param  attr_name   The name of the attribute on which to sort.  The
   *                      attr_name parameter must be the name of an attribute
   *                      that is stored with every Data entry in this data
   *                      set.
   *
   *  @return    This returns true if the DataSet entries were sorted and 
   *             returns false otherwise.
   */
  public boolean Sort( String attr_name, boolean sort_increasing )
  {
    int n     = data.size();
    int pass, 
        k;

    if ( n <= 0 )         // an empty list is sorted by default
      return true;

    int       position[] = new int[ n ];
    Attribute attr[]     = new Attribute[ n ];

    AttributeList  attr_list;        // save the required attribute from each
    Attribute      one_attr;         // Data entry and the index of the Data
    for ( int i = 0; i < n; i++ )    // entry in arrays
    {
      attr_list = getData_entry(i).getAttributeList();
      one_attr = attr_list.getAttribute( attr_name );
      if ( one_attr == null )
        return false;               // attribute missing from this Data block
      
      attr[i]     = one_attr;
      position[i] = i;
    }

   if ( sort_increasing )                           // put in increasing order
   {
    for ( pass = 1; pass < n; pass++ )
      for ( k = 0; k < n - pass; k++ )
        if ( attr[k].compare( attr[k+1] ) > 0 )     // swap both attr & index
        {
          one_attr  = attr[k];
          attr[k]   = attr[k+1];
          attr[k+1] = one_attr;

          int temp      = position[k];
          position[k]   = position[k+1];
          position[k+1] = temp;
        }
   }
   else                                              // put in decreasing order
   {
    for ( pass = 1; pass < n; pass++ )
      for ( k = 0; k < n - pass; k++ )
        if ( attr[k].compare( attr[k+1] ) < 0 )      // swap both attr & index
        {
          one_attr  = attr[k];
          attr[k]   = attr[k+1];
          attr[k+1] = one_attr;

          int temp      = position[k];
          position[k]   = position[k+1];
          position[k+1] = temp;
        }
   }
                                                  // copy the data blocks to
    Vector new_data = new Vector();               // a new vector in the right
    for ( int i = 0; i < n; i++ )                 // order
      new_data.addElement( data.elementAt( position[i] ) );

    data = new_data;

    return true;
  }


  /**
   * Combine the attribute list of the specified DataSet with the attribute
   * list of the current DataSet to obtain a new attribute list for the
   * current DataSet.
   *
   *  @param  d    The DataSet whose attribute list is to be combined
   *               with the current object's attribute list
   */
   public void CombineAttributeList( DataSet ds )
   {
     attr_list.combine( ds.getAttributeList() );
   }



  /**
   * Clone the current DataSet, including the operation log, the list of
   * operators and the list of individual Data blocks.
   */
   public Object clone()
  {
    DataSet new_ds = empty_clone();
                                      // now copy the list of Data blocks.
    Data data;
    int num_entries = getNum_entries();
    for ( int i = 0; i < num_entries; i++ )
    {
      data = getData_entry( i );
      new_ds.addData_entry( (Data)data.clone() );
    }

    return new_ds;
  }

  /**
   * Clone an EMPTY DataSet with the same title, units, label, operation log,
   * and operators as the original data set.
   */
   public DataSet empty_clone()
  {
    DataSet new_ds = new DataSet( getTitle(),           // get a new data set
                                  (OperationLog) getOp_log().clone(),
                                  getX_units(),
                                  getX_label(),
                                  getY_units(),
                                  getY_label() );

                                      // copy the list of operations.
    DataSetOperator op;
    int num_ops = getNum_operators();
    for ( int i = 0; i < num_ops; i++ )
    {
      op = (DataSetOperator)getOperator(i).clone();
      new_ds.addOperator( op );
    }
                                      // copy the list of attributes.
    AttributeList attr_list = getAttributeList();
    new_ds.setAttributeList( attr_list );
    
    return new_ds;
  }


  /**
   *  Provide an identifier string for this DataSet
   */
  public String toString()
  {
    Attribute attr = attr_list.getAttribute(Attribute.DS_TAG);
    
    if (attr != null)
    {String tag = attr.getStringValue();
    return tag+":"+title;
    }
    else 
    return title;
  }

}
