/*
 * @(#)SpectrometerFrequencyDistributionFunction.java   
 *
 *  $Log$
 *  Revision 1.2  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize the operators.
 *  Existing operators were modified to be derived from one of the new abstract
 *  classes.  The abstract base class hierarchy is now:
 *
 *   Operator
 *
 *    -GenericOperator
 *       --GenericLoad
 *       --GenericBatch
 *
 *    -DataSetOperator
 *      --DS_EditList
 *      --DS_Math
 *         ---ScalarOp
 *         ---DataSetOp
 *         ---AnalyzeOp
 *      --DS_Attribute
 *      --DS_Conversion
 *         ---XAxisConversionOp
 *         ---YAxisConversionOp
 *         ---XYAxesConversionOp
 *      --DS_Special
 *
 *     To allow for automatic generation of hierarchial menus, each new operator
 *  should fall into one of these categories, or a new category should be
 *  constructed within this hierarchy for the new operator.
 *
 *
 *  Revision 1.1  2000/08/10 11:50:00  Dongfeng dennis
 *  Initial version of Frequency Distribution Function for Spectrometers
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;

/**
  *  Compute the Frequency Distribution Function for a direct 
  *  geometry spectrometer based on the result of applying the scattering 
  *  function operator.  
  *
  *  @see SpectrometerScatteringFunction
  *  @see DoubleDifferentialCrossection 
  *  @see DataSetOperator
  *  @see Operator
  */

public class SpectrometerFrequencyDistributionFunction 
             extends    DS_Special 
             implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public SpectrometerFrequencyDistributionFunction( )
  {
    super( "Spectrometer Frequency Distribution Function" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator to calculate the Scattering Function
   *  for a spectrometer DataSet.  It is assumed that the 
   *  DoubleDifferentialCrossection operator has already been applied.
   *
   *  @param  ds               The sample DataSet for which the scattering 
   *                           function is to be calculated 
   *  @param  min_E            The minimum energy loss value to be binned
   *  @param  max_E            The maximum energy loss value to be binned
   *  @param  num_E            The number of "bins" to be used between min_E and
   *                           max_E 
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                           constructed, or the Data blocks of the original 
   *                           DataSet are just altered.
   */

  public SpectrometerFrequencyDistributionFunction( 
                                         DataSet    ds,
                                         float      min_X,
                                         float      max_X,
                                         int        num_X,
                                         boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    Parameter parameter= getParameter( 0 );
    parameter.setValue( new Float( min_X ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( max_X ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Integer( num_X ) );

    parameter = getParameter( 3 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "FFun";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter  parameter = new Parameter( "Min Energy Loss Value", new Float(-1000) );
    addParameter( parameter );

    parameter = new Parameter("Max Energy Loss Value", new Float(1000) );
    addParameter( parameter );

    parameter = new Parameter( "Num of Step of Energy Loss", new Integer( 200 ) );
    addParameter( parameter );
    
    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {       
    System.out.println("Start f_fn_ds now!");
    
    final float XKCON   = 0.086165f; // conversion factor 

                                     // get the current data set
    DataSet ds     = getDataSet();
                                                    // get the parameters
    float   min_X       = ((Float)(getParameter(0).getValue()) ).floatValue();
    float   max_X       = ((Float)(getParameter(1).getValue()) ).floatValue();
    int     num_X       = ((Integer)(getParameter(2).getValue()) ).intValue();
    boolean make_new_ds =((Boolean)getParameter(3).getValue()).booleanValue();
    
    // SpectrometerTofToEnergyLoss will create a new DataSet, so we don't need
    // to clone it first.
    Operator op = new SpectrometerTofToEnergyLoss( ds,
                                                   min_X,
                                                   max_X,
                                                   0    );
    DataSet new_ds =(DataSet)op.getResult();
      
    // ConvertHistogramToFunction for the new energy loss DataSet

    System.out.println("\n\nConvertHistogramToFunction start...");
    op = new ConvertHistogramToFunction( new_ds, false, false );
    op.getResult();
      
    // ResampleDataSet for the new energy loss DataSet
    op = new ResampleDataSet( new_ds, min_X, max_X, num_X, false );
    op.getResult();
      
    //viewmanager = new ViewManager(new_ds, IViewManager.IMAGE);


    new_ds.addLog_entry("Calculated Frequency Distribution Function");

    AttributeList attr_list;
    Float   Float_val;

    DetectorPosition position;
    float spherical_coords[];

    float energy_in,
          energy_final,
          x_vals[],
          y_vals[],
          new_y_vals[],
          new_errors[],
          energy_transfer;
    float scattering_angle,
          Q;
    int   num_data;
    Data  data,
          conversion_data,
          new_data;
    num_data = new_ds.getNum_entries();
    for ( int index = 0; index < num_data; index++ )
    {
      data = new_ds.getData_entry( index );
                                               // get the needed attributes
      attr_list   = data.getAttributeList();
      
      Float_val   = (Float)attr_list.getAttributeValue(Attribute.ENERGY_IN);
      energy_in   = Float_val.floatValue();

      position = (DetectorPosition)
                  attr_list.getAttributeValue(Attribute.DETECTOR_POS);
      spherical_coords = position.getSphericalCoords();

      y_vals = data.getY_values();
      x_vals = data.getX_scale().getXs();

      int num_y = y_vals.length;
      new_y_vals = new float[ num_y ];
      new_errors = new float[ num_y ];

      for ( int i = 0; i < (y_vals.length-1); i++ )
      {
        if ( x_vals.length > y_vals.length )  // histogram
          energy_transfer = (x_vals[i]+x_vals[i+1])/2;
        else                                  // function
          energy_transfer = x_vals[i];

        energy_final     = energy_in-energy_transfer;
        scattering_angle = position.getScatteringAngle() * 
                                  (float)(180.0/Math.PI);
        
        energy_final     = energy_in-energy_transfer;
        Q = tof_calc.SpectrometerQ( energy_in, energy_final, scattering_angle );

        new_y_vals[i] =energy_transfer*energy_transfer/(2.0539802f*Q*Q);
         /*        
        if(index ==( 0))
        System.out.println("new_y_vals[i]="+ new_y_vals[i]+"\n"+
                           "energy_transfer="+ energy_transfer+"\n"+
                           "spherical_coords[1]=" +spherical_coords[1]+"and  "+
                                    + (spherical_coords[1]*180/3.1415926)+"\n"+ 
                           "scattering_angle =" +  scattering_angle +"\n"+ 
                           "Q=" + Q+"\n"); 
           //*/                
      }

      conversion_data = new Data( data.getX_scale(),
                                  new_y_vals,
                                  new_errors,
                                  data.getGroup_ID() );
    
      //now multiply the spectrum by the conversion_data;
      new_data = data.multiply( conversion_data );

      new_ds.replaceData_entry( new_data, index );
    }

    if ( make_new_ds )
      return new_ds;
    else
    {
      ds.copy( new_ds );
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return new String( "Calculated Frequency Distribution Function" );
    }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    SpectrometerFrequencyDistributionFunction new_op = 
                  new SpectrometerFrequencyDistributionFunction( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
}

