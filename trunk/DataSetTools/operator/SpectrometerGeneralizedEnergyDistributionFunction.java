/*
 * File:  SpectrometerGeneralizedEnergyDistributionFunction.java   
 *
 * Copyright (C) 2000, Dongfeng Chen,
 *                     Dennis Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *             
 *  $Log$
 *  Revision 1.9  2001/09/27 19:15:04  dennis
 *  After converting the DataSet to energy loss, the "x" values are very
 *  non-uniform.  When the DataSet is subsequently converted to a function,
 *  the "y" values are now divided by the width of the bins, to reflect a
 *  density function (counts per unit x) rather than a raw histogram (counts).
 *
 *  Revision 1.8  2001/09/14 20:50:14  dennis
 *  Fixed calculation of G to use the same expression when e_transf < 0 as
 *  when e_transf > 0.  Clamp exp( -e_transf/kt ) to exp( 10 ) when
 *  e_transf < 0.
 *
 *  Revision 1.7  2001/09/13 22:52:47  dennis
 *  Fixed problem with calculation of Q ( angle should not have been converted
 *  to degrees ).
 *  Fixed calculation of G().
 *
 *  Revision 1.6  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.5  2001/04/26 19:11:03  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.4  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize the 
 *  operators.
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
   * @return the command name to be used with script processor: 
   *         in this case, GFun
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
    op = new ConvertHistogramToFunction( new_ds, true, false );
    op.getResult();
      
    // ResampleDataSet for the new energy loss DataSet
    op = new ResampleDataSet( new_ds, min_X, max_X, num_X, false );
    op.getResult();
      
    //viewmanager = new ViewManager(new_ds, IViewManager.IMAGE);

    if ( temperature <= 0||xmass<=0)
      return new ErrorString(
                "ERROR: temperature and xmass must be greater than 0");

    new_ds.addLog_entry("Calculated Generalized Energy Distribution Function");
    new_ds.addLog_entry("exp(-e_transf/kt) clamped to exp(10) for e_transf<0");

    AttributeList attr_list;
    Float   Float_val;

    DetectorPosition position;

    float x_vals[],
          y_vals[],
          conversion_vals[],
          conversion_errors[],
          e_transf;
    float energy_in,
          energy_final,
          xkt,
          ebykt,
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
      scattering_angle = position.getScatteringAngle();

      y_vals = data.getY_values();
      x_vals = data.getX_scale().getXs();

      int num_y = y_vals.length;
      conversion_vals = new float[ num_y ];
      conversion_errors = new float[ num_y ];

      xkt=XKCON*temperature;
      for ( int i = 0; i < (y_vals.length-1); i++ )
      {            
        if ( data.isHistogram() )                       // use bin centers
          e_transf = (x_vals[i]+x_vals[i+1])/2;
        else                                            // just use x value 
          e_transf = x_vals[i];

        energy_final = energy_in - e_transf;

        Q = tof_calc.SpectrometerQ( energy_in, energy_final, scattering_angle );

        ebykt=e_transf/xkt;

        if ( ebykt >= -10 )
          conversion_vals[i] =xmass * e_transf / (2.0539802f * Q * Q ) *
                              (float)( Math.exp(alpha * Q * Q)) * 
                              ( 1 - (float)Math.exp(-ebykt) );
        else
          conversion_vals[i] =xmass * e_transf / (2.0539802f * Q * Q ) *
                              (float)( Math.exp(alpha * Q * Q)) * 
                              ( 1 - (float)Math.exp(10) );


/*                
        if(i == 200 )
        System.out.println("conversion_vals[i]="+ conversion_vals[i]+"\n"+
                           "energy_transfer="+ e_transf+"\n"+
                           "temperature=" + temperature+"\n"+
                           "alpha=" + alpha+"\n"+
                           "scattering_angle =" +  scattering_angle +"\n"+ 
                           "Q=" + Q+"\n"+ 
                           "xmass=" + xmass+"\n"+ 
                           "XKCON=" + XKCON+"\n"+
                           "xkt=" + xkt+"\n"+
                           "ebykt=" + ebykt+"\n" );
*/                       
      }

      conversion_data = new Data( data.getX_scale(),
                                  conversion_vals,
                                  conversion_errors,
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

