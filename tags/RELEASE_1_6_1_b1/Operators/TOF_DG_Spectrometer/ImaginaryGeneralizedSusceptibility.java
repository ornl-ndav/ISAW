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
 *  Revision 1.7  2003/02/17 19:19:22  dennis
 *  Added getDocumentation() method. (Chris Bouzek)
 *
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
import  DataSetTools.retriever.*;
import  DataSetTools.viewer.*;

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
   *  Construct an operator to calculate the Imaginary Generalized Susceptibility
   *  Function for a spectrometer DataSet.  It is assumed that the 
   *  Scattering Function operator has already been applied.
   *
   *  @param  ds               The sample DataSet for which the imaginary  
   *                           generalized susceptibility function is to be 
   *                           calculated 
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


 /* -------------------------- setDefaultParameters ------------------------- */
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

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
   public String getDocumentation()
   {
     StringBuffer s = new StringBuffer("");
     s.append("@overview This operator computes the imaginary generalized ");
     s.append("susceptibility function for a direct geometry spectrometer ");
     s.append("based on the result of applying the scattering function ");
     s.append("operator.\n");
     s.append("@assumptions It is assumed that the ScatteringFunction ");
     s.append("operator has been applied.\n");
     s.append("@algorithm For each data entry in the DataSet, this operator ");
     s.append("first uses the initial energy and the data's x-values to ");
     s.append("calculate the energy transfer.\n");
     s.append("Next it uses the conversion factor XKCON, the sample ");
     s.append("temperature, and the energy transfer to calculate ");
     s.append("conversion values.\n");
     s.append("These conversion values, along with conversion error data ");
     s.append("based on the data's y-values, are used by Data's getInstance ");
     s.append("method to calculate conversion data.\n");
     s.append("Finally it multiplies the spectrum by the conversion data ");
     s.append("and appends a log to the DataSet indicating that the ");
     s.append("imaginary part of generalized susceptibility was ");
     s.append("calculated.\n");
     s.append("@param ds The sample DataSet for which the imaginary ");
     s.append("generalized susceptibility function is to be calculated.\n");
     s.append("@param temperature The sample temperature.\n");
     s.append("@param make_new_ds Flag that determines whether a new ");
     s.append("DataSet is constructed, or the Data blocks of the original ");
     s.append("DataSet are just altered.\n");
     s.append("@return If make_new_ds is true, returns the new DataSet to ");
     s.append("which the imaginary generalized susceptibility function has ");
     s.append("been applied.  Otherwise, it returns a String indicating ");
     s.append("that the imaginary generalized susceptibility function was ");
     s.append("applied.\n");
     s.append("@error An error message is returned if temperature ");
     s.append("is not be greater than 0.\n");
     return s.toString();
    }

  /* ---------------------------- getResult ------------------------------- */
  /** 
    * Computes the imaginary generalized susceptibility function.
    *
    * @return If make_new_ds is true, returns the new DataSet to which the 
    * imaginary generalized susceptibility function has been applied.  
    * Otherwise, it returns a String indicating that the imaginary generalized 
    * susceptibility function was applied.
    */

  public Object getResult()
  {       
    System.out.println("Start ImChi_fn_ds now!");
    
    final float XKCON   = 0.086165f; // conversion factor 

                                                    // get the parameters
    DataSet ds          = (DataSet)(getParameter(0).getValue());
    float   temperature = ((Float)(getParameter(1).getValue()) ).floatValue();
    boolean make_new_ds =((Boolean)getParameter(2).getValue()).booleanValue();
    
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
        if ( data.isHistogram() )             // histogram
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
    System.out.println("Test of ImaginaryGeneralizedSusceptibilityFunction" );
   
    String    run_name = "/home/groups/SCD_PROJECT/SampleRuns/hrcs2447.run";
    //String    run_name = "d:\\SCD_PROJECT\\SampleRuns\\hrcs2447.run";
    Retriever rr       = new RunfileRetriever( run_name );
    DataSet   ds       = rr.getDataSet(1);

    Operator op = new DoubleDifferentialCrossection( ds, null, false,
                                                     10000, 1, true );

    Object ddif_ds = op.getResult();
    if ( ddif_ds == null )
      System.out.println("Error in calculating DSDODE... returned null");
    else
    {
      System.out.println("DSDODE returned:" + ddif_ds );
      if ( ddif_ds instanceof DataSet )
      {
        ViewManager vm1 = new ViewManager((DataSet)ddif_ds,IViewManager.IMAGE);
      }
    }

   op = new ScatteringFunction( (DataSet)ddif_ds, 1, true );
   Object scat_ds = op.getResult();
   if ( scat_ds == null )
      System.out.println("Error in calculating SCAT... returned null");
    else
    {
      System.out.println("SCAT returned:" + scat_ds );
      if ( scat_ds instanceof DataSet )
      {
        ViewManager vm3 = new ViewManager((DataSet)scat_ds,IViewManager.IMAGE);
      }
    }
    
   op = new ImaginaryGeneralizedSusceptibility( (DataSet)scat_ds, 30.0f, true );
   Object imchi_ds = op.getResult();
   if ( imchi_ds == null )
      System.out.println("Error in calculating IMCHI... returned null");
    else
    {
      System.out.println("IMCHI returned:" + imchi_ds );
      if ( imchi_ds instanceof DataSet )
      {
        ViewManager vm4 = new ViewManager((DataSet)imchi_ds,IViewManager.IMAGE);
      }
    }
    
    System.out.println("Documentation: " + op.getDocumentation());

    System.out.println("End of test of ImaginaryGeneralizedSusceptibilityFunction");
  }

}
