/*
 * File:  PeakVsAngle.java 
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
 * Revision 1.1  2001/11/21 21:27:44  dennis
 * Example of user-supplied add-on operator.
 *
 *
 */
package Operators;

import DataSetTools.operator.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import DataSetTools.math.*;
import DataSetTools.peak.*;
import java.util.*;

/** 
 *  This operator provides a "template" for writing operator "plug-ins" for 
 *  Isaw.  To make a custom operator for use with ISAW, rename this file, then
 *  modify the class name, and title to an appropriate name, such as 
 *  MyOperator.java for the file and MyOperator for the class. Place the 
 *  new file in the Operators subdirectory of the Isaw home directory, or of
 *  your home directory.  Next, modify the parameters, command name and 
 *  the calculation performed as needed.  This template includes a main 
 *  program that can be used to test the operator separately.  You should also
 *  modify the main program appropriately, to separately test your operator.
 *  The operator must be compiled before Isaw is started, so that Isaw can 
 *  load the class file.  PLEASE ALSO REPLACE THE TEMPLATE COMMENTS WITH 
 *  COMMENTS APPROPRIATE FOR YOUR OPERATOR.
 */
public class PeakVsAngle extends GenericSpecial
{
  private static final String TITLE = "Peak Vs Angle";

 /* ------------------------ Default constructor ------------------------- */ 
 /** 
  *   Default constructor that is used when the parameters will be
  *   set later
  */  
  public PeakVsAngle()
  {
    super( TITLE );
  }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  Construct a PeakVsAngle operator that integrates the peak values over
  *  the interval [a,b].
  *
  *  @param  ds     DataSet for which the integrated peak intensity vs group 
  *                 angle will be calculated. 
  *  @param  a      Left endpoint of interval where the peak is integrated.
  *  @param  b      Right endpoint of interval where the peak is integrated.
  */
  public PeakVsAngle( DataSet ds, 
                      float   a,
                      float   b )
  {
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("DataSet parameter", ds) );
    addParameter( new Parameter("Left endpoint", new Float(a) ) );
    addParameter( new Parameter("Right endpoint", new Float(b) ) );
  }

 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "PeakVsAngle", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "PeakVsAngle";
  }

 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters.  This must match the data types 
  * of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("DataSet parameter", DataSet.EMPTY_DATA_SET) );
    addParameter( new Parameter("Left endpoint", new Float(0) ) );
    addParameter( new Parameter("Right endpoint", new Float(30000) ) );
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return  If successful, this template just returns a String indicating
  *           what the paramters were, and that the operator executed.  The
  *           code that does the work of the operator goes here. 
  */
  public Object getResult()
  {
    DataSet ds =  (DataSet)(getParameter(0).getValue());
    float   a  = ((Float)  (getParameter(1).getValue())).floatValue();
    float   b  = ((Float)  (getParameter(2).getValue())).floatValue();

                                       // check for degenerate cases
    if ( ds == null )
      return new ErrorString("DataSet is null in PeakVsAngle");

    if ( a >= b || a < 0 || b < 0 )
      return new ErrorString("[a,b] invalid in PeakVsAngle: " + 
                             "[ " + a + ", " + b + " ]" );

                                       // get the original y units and label
    String y_units = ds.getY_units();
    String y_label = ds.getY_label();
    String title   = "Integrated Peak Intensity";
                                          // make DataSet with new title and 
                                          // modified y units and label
    DataSetFactory ds_factory = new DataSetFactory( title,
                                                   "Degrees",
                                                   "Scattering Angle",
                                                    y_units,  
                                                   "Integrated Peak "+ y_label);
    DataSet new_ds = ds_factory.getDataSet();
                                          // copy and update the log
                                          // copy the list of attributes
    new_ds.copyOp_log(ds);
    new_ds.addLog_entry("Calculated Integrated Peak vs Angle");
    new_ds.setAttributeList( ds.getAttributeList() );

                                          // Sort the DataSet based on the
                                          // effective position.  This orders it
                                          // by scattering angle.
    Operator sort_op = new DataSetSort(ds, Attribute.DETECTOR_POS, true, false);
    sort_op.getResult();
                                          // for each Data block, find the 
                                          // integrated peak intensity and 
                                          // the scattering angle 
    float area[]  = new float[ ds.getNum_entries() ];
    float angle[] = new float[ ds.getNum_entries() ];
    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      Data d = ds.getData_entry( i );
      HistogramDataPeak peak = new HistogramDataPeak( d );
      peak.setEvaluationMode( IPeak.PEAK_PLUS_BACKGROUND );
      area[i] = peak.Area( a, b );
                                          // NOTE: a DetectorPosition object 
                                          // can provide the position in 
                                          // cartesian, cylindrical or polar
                                          // coordinates, as well as the
                                          // scattering angle 2*theta
      DetectorPosition pos = 
             (DetectorPosition)d.getAttributeValue( Attribute.DETECTOR_POS );

      if ( pos == null )
        return new ErrorString("NO DetectorPosition for group " + 
                                d.getGroup_ID() );

      angle[i] = pos.getScatteringAngle() * 180/(float)Math.PI; 
    }
                                          // There may be several groups with
                                          // the same angle, so we need to
                                          // combine them to keep distinct x's 
    float average_area[]  = new float[area.length];       
    float average_angle[] = new float[area.length];       
    int   n_used = 0;
    int   i      = 0;
    float x;
    float y;
    float sum;
    int   n_sum;
    while ( i < angle.length )
    {
      x     = angle[i];
      y     = area[i];
      sum   = area[i];
      n_sum = 1;
      i++;
      while ( i < angle.length && angle[i] == x ) 
      {
        sum += y;
        n_sum++;
        i++; 
      }
      average_angle[n_used] = x;
      average_area [n_used] = sum/n_sum;
      n_used++;
    }
                                         // copy non-duplicates into arrays of
                                         // the proper size
    area  = new float[ n_used ];
    angle = new float[ n_used ];
    System.arraycopy( average_angle, 0, angle, 0, n_used );
    System.arraycopy( average_area,  0, area,  0, n_used );
    
                                          // make a new Data block with the new
                                          // x and y values and group ID 1 
                                          // the x values must be increasing,
                                          // and they will be since the DataSet
                                          // was sorted on the detector position
    XScale x_scale = new VariableXScale( angle );
    Data new_d = new Data( x_scale, area, 1 );

    new_ds.addData_entry( new_d );
    return new_ds;
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    Operator op = new PeakVsAngle();
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
     System.out.println("Test of PeakVsAngle starting...");

                                                              // load a DataSet
     String filename = "/usr/local/ARGONNE_DATA/hrcs2447.run";
     RunfileRetriever rr = new RunfileRetriever( filename );
     DataSet ds = rr.getDataSet(1);
                                                              // make operator
                                                              // and call it
     PeakVsAngle op = new PeakVsAngle( ds, 800, 880 );
     Object obj = op.getResult();
     if ( obj instanceof DataSet )                   // we got a DataSet back
     {                                               // so show it and original
       DataSet new_ds = (DataSet)obj;
       ViewManager vm1 = new ViewManager( ds,     IViewManager.IMAGE );
       ViewManager vm2 = new ViewManager( new_ds, IViewManager.IMAGE );
     }
     else
       System.out.println( "Operator returned " + obj );

     System.out.println("Test of PeakVsAngle done.");
  }
}
