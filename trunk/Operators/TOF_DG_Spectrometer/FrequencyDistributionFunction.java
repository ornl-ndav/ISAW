/*
 * File:  FrequencyDistributionFunction.java   
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
 *  Revision 1.11  2003/02/17 18:59:06  dennis
 *  Added getDocumentation() method and main test program. (Chris Bouzek)
 *
 *  Revision 1.10  2002/12/11 22:31:31  pfpeterson
 *  Removed the '_2' from getCommand() and its javadocs.
 *
 *  Revision 1.9  2002/11/27 23:30:33  pfpeterson
 *  standardized header
 *
 *  Revision 1.8  2002/09/19 15:58:05  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.7  2002/04/19 19:40:40  dennis
 *  Fixed "broken" @see javadoc comments.
 *
 *  Revision 1.6  2002/03/13 16:26:26  dennis
 *  Converted to new abstract Data class.
 *
 *  Revision 1.5  2002/02/22 20:42:54  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.1  2002/01/31 21:02:07  dennis
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
  *  Compute the Frequency Distribution Function for a direct 
  *  geometry spectrometer based on the result of applying the scattering 
  *  function operator.  
  *
  *  @see ScatteringFunction
  *  @see DoubleDifferentialCrossection 
  */

public class FrequencyDistributionFunction 
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

  public FrequencyDistributionFunction( )
  {
    super( "Spectrometer Frequency Distribution Function" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator to calculate the Frequency Distribution Function
   *  for a spectrometer DataSet.  It is assumed that the 
   *  ScatteringFunction operator has already been applied.
   *
   *  @param  ds               The sample DataSet for which the frequency
   *                           distribution function is to be calculated 
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                           constructed, or the Data blocks of the original 
   *                           DataSet are just altered.
   */

  public FrequencyDistributionFunction( 
                                         DataSet    ds,
                                         boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    IParameter parameter= getParameter( 0 );
    parameter.setValue( ds );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( make_new_ds ) );
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case, FFun
   */
   public String getCommand()
   {
     return "FFun";
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
     s.append("@overview This operator computes the frequency distribution ");
     s.append("function for a direct geometry spectrometer based on the ");
     s.append("result of applying the scattering function operator.\n");
     s.append("@assumptions It is assumed that the ScatteringFunction ");
     s.append("operator has been applied.\n");
     s.append("@algorithm For each data entry in the DataSet, this operator ");
     s.append("first uses the initial energy and the data's x-values to ");
     s.append("calculate the final energy.\n");
     s.append("Then it uses tof_calc's SpectrometerQ method to calculate a ");
     s.append("Q value.\n");
     s.append("Next it calculates new y-values and uses Data's getInstance");
     s.append(" method to calculate conversion data using the data's ");
     s.append("X-scale, the group ID, and the new y-values.\n");
     s.append("Finally it multiplies the spectrum by the conversion data ");
     s.append("and appends a log to the DataSet indicating that a frequency ");
     s.append("distribution function was performed.\n");
     s.append("@param ds The sample DataSet for which the frequency ");
     s.append("distribution function is to be calculated.\n");
     s.append("@param make_new_ds Flag that determines whether a new ");
     s.append("DataSet is constructed, or the Data blocks of the original ");
     s.append("DataSet are just altered.\n");
     s.append("@return If make_new_ds is true, returns the new DataSet to ");
     s.append("which the frequency distribution function has been applied.  ");
     s.append("Otherwise, it returns a String indicating that the frequency ");
     s.append("distribution function was applied.\n");
     s.append("@error No standard errors are returned if invalid parameters ");
     s.append("are entered.\n");
     return s.toString();
    }

  /* ---------------------------- getResult ------------------------------- */
  /** 
    * Computes the frequency distribution function.
    *
    * @return If make_new_ds is true, returns the new DataSet to which the 
    * frequency distribution function has been applied.  
    * Otherwise, it returns a String indicating that the frequency distribution 
    * function was applied.
    */

  public Object getResult()
  {       
    System.out.println("Start f_fn_ds now!");
    
    final float XKCON   = 0.086165f; // conversion factor 

                                                    // get the parameters
    DataSet ds          = (DataSet)(getParameter(0).getValue());
    boolean make_new_ds =((Boolean)getParameter(1).getValue()).booleanValue();
    
    DataSet new_ds;
    if ( make_new_ds )
      new_ds = (DataSet)ds.clone();
    else
      new_ds = ds;
 
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
        if ( data.isHistogram() )                       // histogram
          energy_transfer = (x_vals[i]+x_vals[i+1])/2;
        else                                            // else function
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
    Operator new_op = new FrequencyDistributionFunction( );
    new_op.CopyParametersFrom( this );
    return new_op;
  }

 /* ------------------------------- main ---------------------------------- */
 /**
  *  Main program for testing purposes.
  */
  public static void main( String args[] )
  {
    System.out.println("Test of FrequencyDistributionFunction" );
   
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
    
   op = new FrequencyDistributionFunction( (DataSet)scat_ds, true );
   Object freq_dis_ds = op.getResult();
   if ( freq_dis_ds == null )
      System.out.println("Error in calculating FFUN... returned null");
    else
    {
      System.out.println("FFUN returned:" + freq_dis_ds );
      if ( freq_dis_ds instanceof DataSet )
      {
        ViewManager vm4 = 
                    new ViewManager((DataSet)freq_dis_ds,IViewManager.IMAGE);
      }
    }
    
   System.out.println("Documentation: " + op.getDocumentation()); 
    

   System.out.println("End of test of FrequencyDistributionFunction");
  }

}

