
/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2001/07/09 21:55:57  neffk
 * selects based on a open or closed interval.
 *
 */

package DataSetTools.operator;

import DataSetTools.dataset.DataSet;
import java.io.Serializable;
import java.lang.String;
import java.lang.Object;
import java.util.Vector;

/**
 * selects Data objects in a single DataSet object based on an attribute
 * name and value.
 */
public class IntervalSelectionOp
  extends    DataSetOp
  implements Serializable
{

  public static final String DATA_SET = "DataSet object";
  public static final String INTERVAL = "Interval object";



  public IntervalSelectionOp()
  {
    super( "Add a DataSet" );
  }


  public IntervalSelectionOp( DataSet ds )
  {
    this();
    setDataSet( ds );
  }


  public String getCommand()
  {
    return new String( "Select by Interval" );
  }


  public void setDefaultParameters()
  {
    parameters = new Vector();  //clear old parameters

    Parameter parameter = new Parameter( DATA_SET, "GROUP_ID[1:2]" );
    addParameter( parameter );
  }


  /**
   * creates Interval objects from the string parameter and returns
   * a Vector object of results, where each element of the Vector is
   * the group id of selected Data objects.
   */
  public Object getResult()
  {
    System.out.println( "getting result..." );
    return new Object();
  }


  public Object clone()
  {
    IntervalSelectionOp new_op = new IntervalSelectionOp();

    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );
  
    return new_op;
  }
}
