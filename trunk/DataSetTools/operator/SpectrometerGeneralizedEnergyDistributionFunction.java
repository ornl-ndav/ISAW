/*
 * @(#)SpectrometerGeneralizedEnergyDistributionFunction.java   0.1  2000/08/07   Dongfeng Chen Dennis Mikkelson
 *             
 *  $Log$
 *  Revision 1.1  2000/08/08 21:21:43  dennis
 *  Initial version of Generalized Energy Distribution Function for
 *  Spectrometers.  Not yet working correctly.
 *
 *  Revision 1.4  
 *
 *  Revision 1.3  
 *
 *  Revision 1.2  
 *
 *  Revision 1.1  2000/08/07 2210:40:00  Dongfeng dennis
 *  Initial version of Scattering Crossection function for Spectrometers
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
  *  Compute the Generalized Energy Distribution Function for a direct 
  *  geometry spectrometer based on the result of applying the scattering 
  *  function operator.  
  *
  *  @see SpectrometerScatteringFunction
  *  @see DoubleDifferentialCrossection 
  *  @see DataSetOperator
  *  @see Operator
  */

public class SpectrometerGeneralizedEnergyDistributionFunction extends    DataSetOperator 
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
   *
   *  @param  temperature      The sample temperature
   *
   *  @param  xmass            The 
   *
   *  @param  alpha            The 
   *
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                           constructed, or the Data blocks of the original 
   *                           DataSet are just altered.
   */

  public SpectrometerGeneralizedEnergyDistributionFunction( DataSet    ds,
                                         float      temperature, 
                                         float      xmass, 
                                         float      alpha, 
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

    parameter = new Parameter("XMASS",
                                         new Float(1.0) );
    addParameter( parameter );
    
    parameter = new Parameter("ALPHA",
                                         new Float(0.00001) );
    addParameter( parameter );
    
    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {       
    
    System.out.println("Start g_fn_ds now!");
    
    final float XKCON   = 0.086165f; // conversion factor 
                                                   // get the current data set
    DataSet ds  = getDataSet();
                                                    // get the parameters
    float   temperature = ((Float)(getParameter(0).getValue()) ).floatValue();
    float   xmass       = ((Float)(getParameter(1).getValue()) ).floatValue();
    float   alpha       = ((Float)(getParameter(2).getValue()) ).floatValue();
    boolean make_new_ds =((Boolean)getParameter(3).getValue()).booleanValue();

    if ( temperature <= 0||xmass<=0)
      return new ErrorString(
                "ERROR: temperature and xmass must be greater than 0");

    DataSet new_ds = null;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;

    new_ds.addLog_entry( "Calculated Generalized Energy Distribution Function" );

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
    num_data = ds.getNum_entries();
    for ( int index = 0; index < num_data; index++ )
    {
      data = ds.getData_entry( index );
                                               // get the needed attributes
      attr_list   = data.getAttributeList();

      Float_val   = (Float)attr_list.getAttributeValue(Attribute.ENERGY_IN);
      energy_in   = Float_val.floatValue();

      position = (DetectorPosition)
                  attr_list.getAttributeValue(Attribute.DETECTOR_POS);
      spherical_coords = position.getSphericalCoords();

     
      new_data = (Data)data.clone();

      y_vals = new_data.getY_values();
      x_vals = new_data.getX_scale().getXs();

      int num_y = y_vals.length;
      new_y_vals = new float[ num_y ];
      new_errors = new float[ num_y ];

      for ( int i = 0; i < y_vals.length; i++ )
      {
        if ( x_vals.length > y_vals.length )  // histogram
          energy_transfer = (x_vals[i]+x_vals[i+1])/2;
        else                                  // function
          energy_transfer = x_vals[i];


        energy_final = energy_in-energy_transfer;
        scattering_angle = position.getScatteringAngle() * (float)(180.0/Math.PI);
        
        Q           =  // 1.0f;
        //*
                                              tof_calc.SpectrometerQ( energy_in, 
                                              energy_final, 
                                              scattering_angle );
        //*/
        
        xkt=XKCON*temperature;
        ebykt=energy_transfer/xkt;
        if(Math.abs(ebykt)>35.0f) ebykt=10.0f*(float)Math.abs(ebykt)/ebykt;
        if(energy_transfer<-1.0f) ebykt=1.0f;
        popinv= 1.0f-(float)Math.exp(-ebykt);
        if(energy_transfer<0)
        popinv= -popinv;
        new_y_vals[i] =energy_transfer/(2.0539802f*Q*Q)*
                            (float)( Math.exp(alpha*Q*Q))*xmass*popinv;
        
        
        if(index ==( 0))
        System.out.println("new_y_vals[i]="+ new_y_vals[i]+"\n"+
                           "energy_transfer="+ energy_transfer+"\n"+
                           "temperature=" + temperature+"\n"+
                           "alpha=" + alpha+"\n"+
                           "spherical_coords[1]=" + spherical_coords[1]+"and  "+ (spherical_coords[1]*180/3.1415926)+"\n"+ 
                           "scattering_angle =" +  scattering_angle +"\n"+ 
                           "Q=" + Q+"\n"+ 
                           "xmass=" + xmass+"\n"+ 
                           "XKCON=" + XKCON+"\n"+
                           "xkt=" + xkt+"\n"+
                           "ebykt=" + ebykt+"\n"+
                           "popinv=" + popinv+"\n");                    
      }

      conversion_data = new Data( data.getX_scale(),
                                  new_y_vals,
                                  new_errors,
                                  data.getGroup_ID() );
    
      new_data = //conversion_data;
            data.multiply( conversion_data );

      if ( make_new_ds )
        new_ds.addData_entry( new_data );
      else
        new_ds.replaceData_entry( new_data, index );
    }

    if ( make_new_ds )
      return new_ds;
    else
    {
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

