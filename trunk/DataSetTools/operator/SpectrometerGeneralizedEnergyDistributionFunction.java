/*
 * @(#)SpectrometerGeneralizedEnergyDistributionFunction.java   
 *     1.3  2000/08/09   Dongfeng Chen Dennis Mikkelson
 *             
 *  $Log$
 *  Revision 1.3  2000/08/10 15:02:15  dennis
 *  Finished javadoc comments, improved prompts for input parameters,
 *  commented out some System.out.println statements.
 *
 *  Revision 1.3  2000/08/09 17:11:35  dennis
 *  Many small changes... removed un-needed clones of Data blocks and DataSets.
 *
 *  Revision 1.2  2000/08/08 15:15:00   Dongfeng dennis  
 *  Take scattering function's dataset and use ToEL, ConvHist and Resample 
 *  in this operator.
 *
 *  Revision 1.1  2000/08/07 10:40:00  Dongfeng dennis
 *  Initial version of Scattering Crossection function for Spectrometers
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;

/**
  *  Compute the Generalized Energy Distribution Function for a direct 
  *  geometry spectrometer based on the result of applying the scattering 
  *  function operator.  
  *
  *  @see SpectrometerScatteringFunction
  *  @see DoubleDifferentialCrossection 
  *  @see DataSetOperator
  *  @see Operator
  */

public class SpectrometerGeneralizedEnergyDistributionFunction 
             extends    DataSetOperator 
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

  public SpectrometerGeneralizedEnergyDistributionFunction( )
  {
    super( "Spectrometer Generalized Energy Distribution Function" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator to calculate the Scattering Function
   *  for a spectrometer DataSet.  It is assumed that the 
   *  DoubleDifferentialCrossection operator has already been applied.
   *
   *  @param  ds               The sample DataSet for which the scattering 
   *                           function is to be calculated 
   *  @param  temperature      The sample temperature
   *  @param  xmass            The sample mass (amu)
   *  @param  alpha            The Debye Waller coefficient
   *  @param  min_E            The minimum energy loss value to be binned
   *  @param  max_E            The maximum energy loss value to be binned
   *  @param  num_E            The number of "bins" to be used between min_E and
   *                           max_E 
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                           constructed, or the Data blocks of the original 
   *                           DataSet are just altered.
   */

  public SpectrometerGeneralizedEnergyDistributionFunction( 
                                         DataSet    ds,
                                         float      temperature, 
                                         float      xmass, 
                                         float      alpha, 
                                         float      min_X,
                                         float      max_X,
                                         int        num_X,
                                         boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    Parameter parameter = getParameter( 0 );
    parameter.setValue( new Float(temperature) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float(xmass) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float(alpha) );
    
    parameter = getParameter( 3 );
    parameter.setValue( new Float( min_X ) );

    parameter = getParameter( 4 );
    parameter.setValue( new Float( max_X ) );

    parameter = getParameter( 5 );
    parameter.setValue( new Integer( num_X ) );

    parameter = getParameter( 6 );
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
     return "GFun";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Sample temperature (K)",
                                         new Float(5.0) );
    addParameter( parameter );

    parameter = new Parameter("Sample mass (amu)", new Float(1.0) );
    addParameter( parameter );
    
    parameter = new Parameter("Debye Waller coefficient", new Float(0.00001) );
    addParameter( parameter );
    
    parameter = new Parameter( "Min Energy Loss Value", new Float(0) );
    addParameter( parameter );

    parameter = new Parameter("Max Energy Loss Value", new Float(1000) );
    addParameter( parameter );

    parameter = new Parameter( "Num Steps of Energy Loss", new Integer( 200 ) );
    addParameter( parameter );
    
    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {       
    // System.out.println("Start g_fn_ds now!");
    
    final float XKCON   = 0.086165f; // conversion factor 

                                     // get the current data set
    DataSet ds     = getDataSet();
                                                    // get the parameters
    float   temperature = ((Float)(getParameter(0).getValue()) ).floatValue();
    float   xmass       = ((Float)(getParameter(1).getValue()) ).floatValue();
    float   alpha       = ((Float)(getParameter(2).getValue()) ).floatValue();
    float   min_X       = ((Float)(getParameter(3).getValue()) ).floatValue();
    float   max_X       = ((Float)(getParameter(4).getValue()) ).floatValue();
    int     num_X       = ((Integer)(getParameter(5).getValue()) ).intValue();
    boolean make_new_ds =((Boolean)getParameter(6).getValue()).booleanValue();
    
    // SpectrometerTofToEnergyLoss will create a new DataSet, so we don't need
    // to clone it first.
    Operator op = new SpectrometerTofToEnergyLoss( ds,
                                                   min_X,
                                                   max_X,
                                                   0    );
    DataSet new_ds =(DataSet)op.getResult();
      
    // ConvertHistogramToFunction for the new energy loss DataSet

    // System.out.println("\n\nConvertHistogramToFunction start...");
    op = new ConvertHistogramToFunction( new_ds, false, false );
    op.getResult();
      
    // ResampleDataSet for the new energy loss DataSet
    op = new ResampleDataSet( new_ds, min_X, max_X, num_X, false );
    op.getResult();
      
    //viewmanager = new ViewManager(new_ds, IViewManager.IMAGE);

    if ( temperature <= 0||xmass<=0)
      return new ErrorString(
                "ERROR: temperature and xmass must be greater than 0");

    new_ds.addLog_entry("Calculated Generalized Energy Distribution Function");

    AttributeList attr_list;
    Float   Float_val;

    DetectorPosition position;
    float spherical_coords[];

    float x_vals[],
          y_vals[],
          new_y_vals[],
          new_errors[],
          energy_transfer;
    float energy_in,
          energy_final,
          xkt,
          ebykt,
          popinv,
          scattering_angle,
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
        
        Q = tof_calc.SpectrometerQ( energy_in, energy_final, scattering_angle );
        
        xkt=XKCON*temperature;
        ebykt=energy_transfer/xkt;
        if(Math.abs(ebykt)>35.0f) ebykt=10.0f*(float)Math.abs(ebykt)/ebykt;
        if(energy_transfer<-1.0f) ebykt=1.0f;
        popinv= 1.0f-(float)Math.exp(-ebykt);
        if(energy_transfer<0)
        popinv= -popinv;
        new_y_vals[i] =energy_transfer/(2.0539802f*Q*Q)*
                            (float)( Math.exp(alpha*Q*Q))*xmass*popinv;
         /*        
        if(index ==( 0))
        System.out.println("new_y_vals[i]="+ new_y_vals[i]+"\n"+
                           "energy_transfer="+ energy_transfer+"\n"+
                           "temperature=" + temperature+"\n"+
                           "alpha=" + alpha+"\n"+
                           "spherical_coords[1]=" +spherical_coords[1]+"and  "+
                                    + (spherical_coords[1]*180/3.1415926)+"\n"+ 
                           "scattering_angle =" +  scattering_angle +"\n"+ 
                           "Q=" + Q+"\n"+ 
                           "xmass=" + xmass+"\n"+ 
                           "XKCON=" + XKCON+"\n"+
                           "xkt=" + xkt+"\n"+
                           "ebykt=" + ebykt+"\n"+
                           "popinv=" + popinv+"\n");                    
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
      return new String( "Calculated Scattering Function" );
    }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    SpectrometerGeneralizedEnergyDistributionFunction new_op = 
                  new SpectrometerGeneralizedEnergyDistributionFunction( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
}

