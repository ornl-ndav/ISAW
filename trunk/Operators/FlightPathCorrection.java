/*
 * File:  FlightPathCorrection.java 
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
 * Revision 1.1  2001/12/21 17:14:13  dennis
 * Operator to apply energy dependent flight path correction by
 * adjusting time bin boundaries. This is similar to "time focussing"
 * accept the times are adjusted based on the time-varying final
 * path length, rather and the angle to the detector.
 *
 *
 */
package Operators;

import DataSetTools.operator.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
import java.util.*;

/** 
 *  This operator does an energy dependent flight path correction to a DataSet.
 *  The calculation of the flight path correction is done by the static method
 *  DataSetTools.math.tof_data_calc.getEfficiencyFactor that was adapted from
 *  Chun Loong's FORTRAN data analysis codes.  In principle, the flight path
 *  correction alters the effective flight path for each time bin.  That is,
 *  the final velocity for time t should be calculated as:
 *
 *  v_final(t)=(d+fpcorr(t))/t
 *
 *  This operator instead adjusts the value of the time bin boundaries, t,
 *  using 
 *
 *  t = t * d / (d + fpcorr(t))
 *
 *  and so will give the same values for v_final
 *
 *  v_final(t) = d / ( t * d /(d + fpcorr(t)) )
 */
public class FlightPathCorrection extends GenericSpecial
{
  private static final String TITLE = "Spectrometer Flight Path Correction";

 /* ------------------------ Default constructor ------------------------- */ 
 /** 
  */  
  public FlightPathCorrection()
  {
    super( TITLE );
  }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  @param  ds           DataSet to do the flight path correction for. 
  *  @param  make_new_ds  Flag that determines whether a new DataSet is
  *                       constructed, or the Data blocks of the original
  *                       DataSet are just altered.
  */
  public FlightPathCorrection( DataSet ds, boolean make_new_ds )
  {
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("Spectrometer TOF DataSet", ds) );
    addParameter( new Parameter( "Create new DataSet?", 
                                  new Boolean(make_new_ds) ) );
  }

 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "FPCorr", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "FPCorr";
  }

 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters.  This must match the data types 
  * of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("Spectrometer TOF DataSet", 
                                 DataSet.EMPTY_DATA_SET)   );

    addParameter( new Parameter( "Create new DataSet?", new Boolean(false) ) );
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  * 
  *  Alter the time channels of each spectrum to compensate for the 
  *  energy dependent flight path correction.  
  *
  *  @return  If make_new_ds is true, this returns a new DataSet with adjusted 
  *           time channels, otherwise it just returns the same DataSet with
  *           adjusted time channels.
  */
  public Object getResult()
  {
    DataSet ds          = (DataSet)(getParameter(0).getValue());
    boolean make_new_ds = ((Boolean)getParameter(1).getValue()).booleanValue();
 
                                              // check for proper DataSet
    if ( ds == null )
      return new ErrorString("DataSet is null in FlightPathCorrection");

    if ( !ds.getX_units().equalsIgnoreCase( "Time(us)" ) )
      return new ErrorString(
                        "DataSet X units not Time(us) in FlightPathCorrection");

    if ( ds.getNum_entries() <= 0 )
      return new ErrorString("DataSet empty in FlightPathCorrection");

    DataSet new_ds;
    if ( make_new_ds )
      new_ds = (DataSet)ds.clone();
    else
      new_ds = ds;
                                             // find range of final velocities
                                             // and table of final path lengths 
    XScale tof_range =  new_ds.getXRange();
    float  min_path  =  Float.MAX_VALUE; 
    float  max_path  = -Float.MAX_VALUE;
    float  path[]    =  new float[ ds.getNum_entries() ];
    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      Data d   = ds.getData_entry(i);
      DetectorPosition pos = (DetectorPosition)
                              d.getAttributeValue(Attribute.DETECTOR_POS);
      if ( pos == null )
        return new ErrorString("No Detector Position for Data block " + i );

      float spherical_coords[] = pos.getSphericalCoords();
      path[i] = spherical_coords[0];
      if ( path[i] < min_path )
        min_path = path[i];
      if ( path[i] > max_path )
        max_path = path[i];
    }
    float min_E = tof_calc.Energy( min_path, tof_range.getEnd_x() );
    float max_E = tof_calc.Energy( max_path, tof_range.getStart_x() );
    float min_V = tof_calc.VelocityFromEnergy( min_E );
    float max_V = tof_calc.VelocityFromEnergy( max_E );

    System.out.println("min_path = " + min_path );
    System.out.println("max_path = " + max_path );
    System.out.println("min_time = " + tof_range.getStart_x() );
    System.out.println("max_time = " + tof_range.getEnd_x() );
    System.out.println("min_E    = " + min_E );
    System.out.println("max_E    = " + max_E );
    System.out.println("min_V    = " + min_V );
    System.out.println("max_V    = " + max_V );
                                            
                                           // make table of fpcorr values for
                                           // velocities from min_V to max_V
    int N_INTERPOLATION_POINTS = 201;
    float speed_arr[]  = new float[N_INTERPOLATION_POINTS];
    float fpcorr_arr[] = new float[N_INTERPOLATION_POINTS];
    float delta_v      = (max_V - min_V)/ (N_INTERPOLATION_POINTS - 1);
    float result[];
    for ( int j = 0; j < N_INTERPOLATION_POINTS; j++ )
    {
      float v = min_V + j*delta_v;
      speed_arr[j] = v;
      result = tof_data_calc.getEfficiencyFactor( v, 1 );   //#### det type = 1
      fpcorr_arr[j] = result[1]; 
      System.out.println( "fpcorr[i] = " + fpcorr_arr[j] );
    }
                                          // for each spectrum, modify each
                                          // time channel to compensate for the
                                          // flight path correction
    for ( int i = 0; i < ds.getNum_entries(); i++ ) 
    {
      float fpcorr;
      Data d = ds.getData_entry( i );
      float x[] = d.getX_scale().getXs();   

      for ( int j = 0; j < x.length; j++ )
      {
        fpcorr = arrayUtil.interpolate( path[i]/x[j], speed_arr, fpcorr_arr );
        x[j] *= path[i] / (path[i] + fpcorr);
      }
                                          // make a new Data block with the new
                                          // x values and same group ID, y
                                          // values and attributes.
      float y[] = d.getY_values();
      XScale x_scale = new VariableXScale( x );
      Data new_d = new Data( x_scale, y, d.getGroup_ID() );
      new_d.setAttributeList( d.getAttributeList() );
      new_d.setSqrtErrors();
      new_ds.replaceData_entry( new_d, i );   
    }

    new_ds.addLog_entry("Energy Dependent Flight Path Correction Applied");
    return new_ds; 
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    Operator op = new FlightPathCorrection();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main --------------------------------- */ 
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
     System.out.println( "Test of FlightPathCorrection starting...");
                                                               // load a DataSet
     String filename = "/usr/local/ARGONNE_DATA/hrcsdata/hrcs3005.run";
     RunfileRetriever rr = new RunfileRetriever( filename );
     DataSet ds = rr.getDataSet(1);
                                                               // make operator
                                                               // and call it
     FlightPathCorrection op = new FlightPathCorrection( ds, true );
     Object obj = op.getResult();
     if ( obj instanceof DataSet )                   // we got a DataSet back
     {                                               // so show it and original
       DataSet new_ds = (DataSet)obj;
       ViewManager vm1 = new ViewManager( ds,     IViewManager.IMAGE );
       ViewManager vm2 = new ViewManager( new_ds, IViewManager.IMAGE );
     }
     else 
       System.out.println( "Operator returned " + obj );

     System.out.println("Test of FlightPathCorrection done.");
  }
}
