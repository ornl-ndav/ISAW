//                                 99/08/16  Removed unused code 

package DataSetTools.operator;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;

public class SpectrometerPlotter extends    DataSetOperator 
                                         implements Serializable
{

  public SpectrometerPlotter( )
  {
    super( "Data Plotter" );
    Parameter parameter;

    parameter = new Parameter( "Data ID", new Integer(5) );
    addParameter( parameter );
  }

  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                     // get the current data set
     DataSetTools.dataset.DataSet ds = this.getDataSet();
     int   det_ID = ( (Integer)(getParameter(0).getValue()) ).intValue() ;
     
    ChopTools.chop_dataDrawer.drawgraphDataEntry(ds, det_ID);
    return null;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerTofToEnergyLoss Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SpectrometerPlotter new_op = new SpectrometerPlotter( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );

    return new_op;
  }
}
