/*
 * File:  ImaginaryGeneralizedSusceptibility.java   
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
 *  Revision 1.4  2002/09/19 15:58:07  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.3  2002/04/19 19:40:42  dennis
 *  Fixed "broken" @see javadoc comments.
 *
 *  Revision 1.2  2002/03/13 16:26:26  dennis
 *  Converted to new abstract Data class.
 *
 *  Revision 1.1  2002/02/22 20:43:51  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.1  2002/01/31 21:02:10  dennis
 *  Initial version as generic operator.
 *  Moved into Operators.TOF_DG_Spectrometer package.
 *
 */

package Operators.TOF_DG_Spectrometer;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;
import  DataSetTools.operator.*;
import  DataSetTools.operator.Generic.TOF_DG_Spectrometer.*;
import  DataSetTools.parameter.*;

/**
  *  Compute the Imaginary Generalized Susceptibility Function for a direct 
  *  geometry spectrometer based on the result of applying the scattering 
  *  function operator.  
  *
  *  @see ScatteringFunction
  *  @see DoubleDifferentialCrossection 
  */

public class ImaginaryGeneralizedSusceptibility 
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

  public ImaginaryGeneralizedSusceptibility( )
  {
    super( "Imaginary Part of Generalized Susceptibility" );
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
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                           constructed, or the Data blocks of the original 
   *                           DataSet are just altered.
   */

  public ImaginaryGeneralizedSusceptibility( 
                                         DataSet    ds,
                                         float      temperature, 
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
    parameter.setValue( new Boolean( make_new_ds ) );
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case, ImChi
   */
   public String getCommand()
   {
     return "ImChi";
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
    
    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {       
    System.out.println("Start ImChi_fn_ds now!");
    
    final float XKCON   = 0.086165f; // conversion factor 

                                                    // get the parameters
    DataSet ds          = (DataSet)(getParameter(0).getValue());
    float   temperature = ((Float)(getParameter(0).getValue()) ).floatValue();
    boolean make_new_ds =((Boolean)getParameter(4).getValue()).booleanValue();
    
    DataSet new_ds;
    if ( make_new_ds )
      new_ds = (DataSet)ds.clone();
    else
      new_ds = ds;

    //viewmanager = new ViewManager(new_ds, IViewManager.IMAGE);

    if ( temperature <= 0)
      return new ErrorString(
                "ERROR: temperature must be greater than 0");

    new_ds.addLog_entry(
              "Calculated Imaginary Part of Generalized Susceptibility");

    Float   Float_val;

    float x_vals[],
          y_vals[],
          new_y_vals[],
          new_errors[],
          energy_transfer;
    float xkt,
          ebykt,
          boseinv;
    int   num_data;
    Data  data,
          conversion_data,
          new_data;
    num_data = new_ds.getNum_entries();
    for ( int index = 0; index < num_data; index++ )
    {
      data = new_ds.getData_entry( index );

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

        
        xkt=XKCON*temperature;
        ebykt=energy_transfer/xkt;
        boseinv=0.0f;//Variable have to be initialized first.
        if ( ebykt >=0) 
           boseinv=1.0f - (float)Math.exp(-ebykt);       //1/[n(E)+1] for E>0

        else if( ebykt <0) 
           boseinv=(float)Math.exp(-ebykt)-1.0f;//1/n(E) for E<0
       
        // if(boseinv>1000.0f)boseinv=1000.0f; 
        // add by Dongfeng Chen for low temperature
        
        new_y_vals[i] =boseinv;
        /*        
        if(index ==( 0))
        System.out.println("new_y_vals[i]="+ new_y_vals[i]+"\n"+
                           "energy_transfer="+ energy_transfer+"\n"+
                           "temperature=" + temperature+"\n"+
                           "XKCON=" + XKCON+"\n"+
                           "xkt=" + xkt+"\n"+
                           "ebykt=" + ebykt+"\n"+
                           "boseinv=" + boseinv+"\n");                    
         //*/                
      }

      conversion_data = Data.getInstance( data.getX_scale(),
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
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return new String( "Calculated Imaginary Generalized Susceptibility" );
    }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    Operator new_op = new ImaginaryGeneralizedSusceptibility();
    new_op.CopyParametersFrom( this );
    return new_op;
  }

  /* ------------------------------- main ---------------------------------- */
  /**
   *  Main program for testing purposes.
   */
   public static void main( String args[] )
   {
     System.out.println( "SpectrometerGeneralizedEnergyDistributionFunction" );
   }

}
