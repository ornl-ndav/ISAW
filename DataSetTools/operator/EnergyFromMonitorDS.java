/*
 * @(#)EnergyFromMonitorDS.java   0.1  2000/07/17   Dennis Mikkelson
 *             
 * $Log$
 * Revision 1.2  2000/11/10 22:41:34  dennis
 *    Introduced additional abstract classes to better categorize the operators.
 * Existing operators were modified to be derived from one of the new abstract
 * classes.  The abstract base class hierarchy is now:
 *
 *  Operator
 *
 *   -GenericOperator
 *      --GenericLoad
 *      --GenericBatch
 *
 *   -DataSetOperator
 *     --DS_EditList
 *     --DS_Math
 *        ---ScalarOp
 *        ---DataSetOp
 *        ---AnalyzeOp
 *     --DS_Attribute
 *     --DS_Conversion
 *        ---XAxisConversionOp
 *        ---YAxisConversionOp
 *        ---XYAxesConversionOp
 *     --DS_Special
 *
 *    To allow for automatic generation of hierarchial menus, each new operator
 * should fall into one of these categories, or a new category should be
 * constructed within this hierarchy for the new operator.
 *
 * Revision 1.1  2000/07/17 21:03:00  dennis
 * Operator to calculate initial beam energy for a chopper spectromater from
 * the monitor DataSet
 *
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;

/**
  *  This operator calculates the incident energy of a neutron beam for a 
  *  chopper spectrometer given a DataSet containing the Data blocks from
  *  two beam monitors.
  */

public class  EnergyFromMonitorDS  extends    DS_Special 
                                   implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  
   */

  public EnergyFromMonitorDS( )
  {
    super( "Calculate initial energy from monitor DataSet" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds    The monitor DataSet used for the energy calculation.
   */

  public EnergyFromMonitorDS( DataSet ds )
  {
    this();  
    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "Emon";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {                                  
                                     // get the current data set and do the 
                                     // operation
    DataSet ds = this.getDataSet();

    Data mon_1 = ds.getData_entry(0);
    Data mon_2 = ds.getData_entry(1);

    if ( mon_1 == null || mon_2 == null )
    {
      ErrorString message = new ErrorString( 
                           "ERROR: Two monitor Data block are needed" );
      System.out.println( message );
      return message;
    }
    else
    {
      float result = tof_data_calc.EnergyFromMonitorData( mon_1, mon_2 );
      return new Float( result );  
    }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current EnergyFromMonitorDS Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    EnergyFromMonitorDS new_op = new EnergyFromMonitorDS( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }


}
