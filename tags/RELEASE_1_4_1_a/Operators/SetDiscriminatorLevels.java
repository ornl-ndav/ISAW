/*
 * File:  SetDiscriminatorLevels.java 
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 * $Log$
 * Revision 1.5  2002/02/22 20:45:06  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.4  2001/11/27 18:21:54  dennis
 * Added operator title to constructor java docs.
 *
 * Revision 1.3  2001/11/21 21:31:50  dennis
 * First attempt at an operator to automatically determine discriminator
 * levels.
 *
 *
 */
package Operators;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import DataSetTools.peak.*;
import java.util.*;

/** 
 */
public class SetDiscriminatorLevels extends GenericSpecial
{

  private static final String TITLE = "Set Discriminator Levels";

 /** 
  *  Creates operator with title "Set Discriminator Levels" and a 
  *  default list of parameters. 
  */  
  public SetDiscriminatorLevels()
  {
    super( TITLE );
  }

 /** 
  *  Creates operator with title "Set Discriminator Levels" and the  
  *  specified list of parameters.  The getResult method must still be 
  *  used to execute the operator.
  *
  *  @param  p_ds        The pulse height DataSet.
  *  @param  lower_frac  The fraction of the the pulse position to set for
  *                      the lower threshold. 
  *  @param  upper_frac  The fraction of the pulse amplitude to set for the
  *                      upper threshold.
  *  @param  width       Number of channels over which the peak will be fit.
  */
  public SetDiscriminatorLevels( DataSet p_ds, 
                                 float   lower_frac,
                                 float   upper_frac,
                                 float   width )
  {
    super( TITLE );
    parameters = new Vector();
    addParameter( new Parameter( "Pulse Height Data Set", p_ds) );
    addParameter( new Parameter( "lower (fraction of peak position)", 
                                  new Float(lower_frac) ) );
    addParameter( new Parameter( "upper (fraction of peak amplitude)", 
                                  new Float(upper_frac) ) );
    addParameter( new Parameter( "width of interval for fit", 
                                  new Float(width) ) );
  }

 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "SetDisc", the command used to invoke this operator in Scripts
  */
  public String getCommand()
  {
    return "SetDisc";
  }

 /** 
  * Sets default values for the parameters.  This must match the data types 
  * of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter( "Pulse Height Data Set", 
                                  new DataSet("","") ));
    addParameter( new Parameter( "lower (fraction of peak position)", 
                                  new Float(0.25f) ) );
    addParameter( new Parameter( "upper (fraction of peak amplitude)",
                                  new Float(1.0e-8) ) );
    addParameter( new Parameter( "width of interval for fit", 
                                  new Float(40) ) );
  }

 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return  If successful, this returns an array of DataSets, the first 
  *           containing the Gaussian peaks fit to the data, and the second
  *           containing the discriminator levels and a third which is
  *           the sum of the discriminator levels and the original data.  
  */
  public Object getResult()
  {
    DataSet p_ds = (DataSet)(getParameter(0).getValue());
    float lower_frac = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float upper_frac = ( (Float)(getParameter(2).getValue()) ).floatValue();
    float width      = ( (Float)(getParameter(3).getValue()) ).floatValue();

    String title = p_ds.getTitle();

    DataSet result_ds[] = new DataSet[2];
    result_ds[0] = (DataSet)p_ds.clone();
    result_ds[1] = FitPeaks( result_ds[0], lower_frac, upper_frac, width );
    
    result_ds[0].setTitle( title + "_Levels( "+lower_frac+", "+width+")" );
    result_ds[1].setTitle( title + "_Gaussians( "+lower_frac+", "+width+")" );
    return result_ds;
  }

 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    SetDiscriminatorLevels sl = new SetDiscriminatorLevels();
    sl.CopyParametersFrom( this );
    return sl;
  }

 /* ------------------------------------------------------------------------
  *
  *  PRIVATE METHODS
  *
  */

  private DataSet FitPeaks( DataSet p_ds, 
                            float   lower_frac, 
                            float   upper_frac, 
                            float   peak_width )
  {
    Data         data,
                 peak_data;
    XScale       x_scale;

    GaussianPeak   peak   = new GaussianPeak( 0, 1, 1, 0, 0 );
    DataSet        new_ds = p_ds.empty_clone();
    int            n_data = p_ds.getNum_entries();
    int            lower,
                   upper;
    int            lower_list[],        // record the discriminator levels in
                   upper_list[];        // arrays that could be written out
    float          delta  = 
             (float) Math.sqrt( -2 * Math.log(Math.sqrt(2*Math.PI)*upper_frac));

    ClosedInterval range  = p_ds.getYRange();   

    lower_list = new int[ n_data ];
    upper_list = new int[ n_data ];
    for ( int i = 0; i < n_data; i++ )
    {
      float x1 = 50;
      float x2 = 255;
      float width = peak_width/2;

      data = p_ds.getData_entry(i);

      int count = 0;
      boolean done   = false;
      boolean fit_ok = false;
      float last_position = -1000;
      float position = 0;
      while ( count < 40 && !done && !fit_ok )
      {
        if ( !peak.FitPeakToData( data, x1, x2 ) )
          done = true;
        else
        {
          position = peak.getPosition();
          if ( Math.abs( position - last_position ) < 0.1 )
            fit_ok = true;
          else
          {
            width = peak.getFWHM();
            if ( width > peak_width/2 )
              width = peak_width/2;
            x1 = peak.getPosition() - 1.1f * width;
            x2 = peak.getPosition() + 1.1f * width;
            last_position = position;
            count++;
          }
        }
      }
      x_scale = data.getX_scale();
      peak.setEvaluationMode( IPeak.PEAK_ONLY );
      peak_data = peak.PeakData( x_scale );
      peak_data.setGroup_ID( data.getGroup_ID() );
      new_ds.addData_entry( peak_data );
      if ( !fit_ok )
      {
        float y_vals[] = peak_data.getY_values();
        for ( int j = 0; j < y_vals.length; j++ )
          y_vals[j] = 0;

        lower_list[i] = 0;
        upper_list[i] = y_vals.length-1;
      }
      else
      {
        lower = (int)( lower_frac * peak.getPosition() );
        upper = (int)( peak.getPosition() + delta * peak.getSigma() );
        
        lower_list[i] = lower;
        upper_list[i] = upper;

        float y_vals[] = data.getY_values();
        if ( lower < 0 ) 
          lower = 0;
        if ( lower >= y_vals.length )
          lower = y_vals.length - 1;
        y_vals[lower] = range.getEnd_x();

        if ( upper < 0 )
          upper = 0;
        if ( upper >= y_vals.length )
          upper = y_vals.length - 1;
        y_vals[upper] = range.getEnd_x();
      }
    }

    for ( int i = 0; i < lower_list.length; i++ )
      System.out.println("i, lower, upper = " + i + 
                         ", " + lower_list[i] +
                         ", " + upper_list[i] );
    return new_ds;
  }

 
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
    System.out.println("SetDsicriminatorLevels compiled OK ");
/*
    String    run_A = "/home/dennis/ARGONNE_DATA/hrcs2937.run";
    Retriever rr    = new RunfileRetriever( run_A );
*/
    String    run_B = "/home/dennis/ARGONNE_DATA/pulseheight.nxs";
    Retriever rr    = new NexusRetriever( run_B );

    DataSet   p_ds  = rr.getDataSet(1);
    if ( p_ds == null )
    {
      System.out.println("ERROR: file not found");
      System.exit(1);
    }

    Operator op = new SetDiscriminatorLevels( p_ds, 0.25f, 1e-8f, 40 );
    Object obj = op.getResult();
    ViewManager vm;
    if ( obj instanceof DataSet[] )
    {
      DataSet ds_a[] = (DataSet[])obj;

      for ( int i = 0; i < ds_a.length; i++ )
        vm = new ViewManager( ds_a[i], IViewManager.IMAGE );
    }
    else
      System.out.println("Operator returned " + obj );
    
  }
}
