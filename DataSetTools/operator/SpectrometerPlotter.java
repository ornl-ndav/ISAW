//                                 99/08/16  Removed unused code 

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;

public class SpectrometerPlotter extends    DataSetOperator 
                                         implements Serializable
{

  public SpectrometerPlotter( )
  {
    super( "Data Plotter" );
  }

 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter;

    parameter = new Parameter( "Data ID", new Integer(5) );
    addParameter( parameter );
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "SpecPlot";
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
    new_op.CopyParametersFrom( this );

    return new_op;
  }
}
