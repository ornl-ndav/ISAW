/*
 * File:  GeneralizedEnergyDistributionFunction.java   
 *        (Generic operator adapted from the corresponding DataSetOperator)
 * Copyright (C) 2000-2002, Dongfeng Chen,
 *                          Dennis Mikkelson
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *             
 *  $Log$
 *  Revision 1.6  2002/12/11 22:31:31  pfpeterson
 *  Removed the '_2' from getCommand() and its javadocs.
 *
 *  Revision 1.5  2002/11/27 23:30:33  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/09/19 15:58:06  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.3  2002/04/19 19:40:41  dennis
 *  Fixed "broken" @see javadoc comments.
 *
 *  Revision 1.2  2002/03/13 16:26:26  dennis
 *  Converted to new abstract Data class.
 *
 *  Revision 1.1  2002/02/22 20:43:50  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.1  2002/01/31 21:02:08  dennis
 *  Initial version as generic operator.
 *  Moved into Operators.TOF_DG_Spectrometer package.
 *
 *  Revision 1.1  2002/01/11 22:10:55  dennis
 *  Generic version of spectrometer DataSet operator
 *
 */

package Operators.TOF_DG_Spectrometer;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;
import  DataSetTools.retriever.*;
import  DataSetTools.operator.*;
import  DataSetTools.operator.Generic.TOF_DG_Spectrometer.*;
import  DataSetTools.viewer.*;
import  DataSetTools.parameter.*;

/**
  *  Compute the Generalized Energy Distribution Function for a direct 
  *  geometry spectrometer based on the result of applying the scattering 
  *  function operator.  
  *
  *  @see ScatteringFunction
  *  @see DoubleDifferentialCrossection 
  */

public class GeneralizedEnergyDistributionFunction 
             extends    GenericTOF_DG_Spectrometer 
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

  public GeneralizedEnergyDistributionFunction( )
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
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                           constructed, or the Data blocks of the original 
   *                           DataSet are just altered.
   */

  public GeneralizedEnergyDistributionFunction( 
                                         DataSet    ds,
                                         float      temperature, 
                                         float      xmass, 
                                         float      alpha, 
                                         boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    IParameter parameter = getParameter( 0 );
    parameter.setValue( ds );

    parameter = getParameter( 1 );
    parameter.setValue( new Float(temperature) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float(xmass) );

    parameter = getParameter( 3 );
    parameter.setValue( new Float(alpha) );
    
    parameter = getParameter( 4 );
    parameter.setValue( new Boolean( make_new_ds ) );
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

    Parameter parameter = new Parameter("Sample Data", DataSet.EMPTY_DATA_SET );
    addParameter( parameter );

    parameter = new Parameter("Sample temperature (K)", new Float(5.0) );
    addParameter( parameter );

    parameter = new Parameter("Sample mass (amu)", new Float(1.0) );
    addParameter( parameter );
    
    parameter = new Parameter("Debye Waller coefficient", new Float(0.00001) );
    addParameter( parameter );
    
    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {       
    // System.out.println("Start g_fn_ds now!");
    
    final float XKCON   = 0.086165f; // conversion factor 

                                                    // get the parameters
    DataSet ds          = (DataSet)(getParameter(0).getValue());
    float   temperature = ((Float)(getParameter(1).getValue()) ).floatValue();
    float   xmass       = ((Float)(getParameter(2).getValue()) ).floatValue();
    float   alpha       = ((Float)(getParameter(3).getValue()) ).floatValue();
    boolean make_new_ds =((Boolean)getParameter(4).getValue()).booleanValue();
    
    DataSet new_ds;
    if ( make_new_ds )
      new_ds = (DataSet)ds.clone();
    else
      new_ds = ds;

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

      conversion_data = Data.getInstance( data.getX_scale(),
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
    Operator new_op = new GeneralizedEnergyDistributionFunction( );
    new_op.CopyParametersFrom( this );
    return new_op;
  }

 /* ------------------------------- main ---------------------------------- */
 /**
  *  Main program for testing purposes.
  */
  public static void main( String args[] )
  {
    System.out.println( "GeneralizedEnergyDistributionFunction" );
  }

}

