/*
 * File:  IntegratedIntensityVsAngle.java 
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2005/08/25 14:51:38  dennis
 * Made/added to category DATA_SET_ANALYZE_MACROS.
 *
 * Revision 1.2  2004/05/10 20:42:28  dennis
 * Test program now just instantiates a ViewManager to diplay
 * calculated DataSet, rather than keeping a reference to it.
 * This removes an Eclipse warning about a local variable that is
 * not read.
 *
 * Revision 1.1  2004/05/07 17:57:01  dennis
 * Moved operators that extend GenericSpecial from Operators
 * to Operators/Special
 *
 * Revision 1.8  2004/03/15 19:36:52  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.7  2004/03/15 03:36:58  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.6  2003/02/14 13:28:32  dennis
 * Added getDocumentation() method. (Tyler Stelzer)
 *
 * Revision 1.5  2002/11/27 23:29:54  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/09/25 22:18:14  pfpeterson
 * Now can integrate with limits being negative as well. Also fixed
 * error when integrating function-table data.
 *
 * Revision 1.3  2002/03/13 16:26:24  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.2  2002/02/22 20:45:03  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.1  2001/11/27 18:23:57  dennis
 * Initial version of sample operator.
 *
 */
package Operators.Special;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.operator.DataSet.EditList.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import java.util.*;

/** 
 *  This operator produces a DataSet with one entry, a Data block giving the
 *  integrated intensity of a histogram over a specified interval, as a 
 *  tabulated function of the scattering angle 2*theta.
 */
public class IntegratedIntensityVsAngle extends GenericSpecial
{
  private static final String TITLE = "Integrated Intensity Vs Angle";

 /* ------------------------ Default constructor ------------------------- */ 
 /** 
  *  Creates operator with title "Integrated Intensity Vs Angle" and a default 
  *  list of parameters.
  */  
  public IntegratedIntensityVsAngle()
  {
    super( TITLE );
  }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  Construct a IntegratedIntensityVsAngle operator that integrates the 
  *  histogram values over the interval [a,b].    
  *  Creates operator with title "Integrated Intensity Vs Angle" and the 
  *  specified list of parameters.  The getResult method must still be used 
  *  to execute the operator.
  *
  *  @param  ds   DataSet for which the integrated intensity vs group 
  *               angle will be calculated. 
  *  @param  a    Left endpoint of interval where the histogram is integrated.
  *  @param  b    Right endpoint of interval where the histogram is integrated.
  */
  public IntegratedIntensityVsAngle( DataSet ds, 
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
  * @return  "IntensityVsAngle", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "IntensityVsAngle";
  }

 /* ---------------------------- getCategoryList -------------------------- */
 /**
  *  Get the list of categories describing where this operator should appear
  *  in the menu system.
  *
  *  @return an array of strings listing the menu where the operator 
  *  should appear.
  */
  public String[] getCategoryList()
  {
    return Operator.DATA_SET_ANALYZE_MACROS;
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
  *  @return  If successful, this operator produces a DataSet containing
  *  the integrated intensity of a spectrum over the specified interval [a,b].
  *  If the original DataSet is null, or the interval is invalid, or some
  *  Data block of the original DataSet does not have a detector position
  *  attribute, an error message is returned.
  */
  public Object getResult()
  {
    DataSet ds =  (DataSet)(getParameter(0).getValue());
    float   a  = ((Float)  (getParameter(1).getValue())).floatValue();
    float   b  = ((Float)  (getParameter(2).getValue())).floatValue();

                                       // check for degenerate cases
    if ( ds == null )
      return new ErrorString("DataSet is null in IntegratedIntensityVsAngle");

    if ( a >= b ) //|| a < 0 || b < 0 )
      return new ErrorString("[a,b] invalid in IntegratedIntensityVsAngle: " + 
                             "[ " + a + ", " + b + " ]" );

                                       // get the original y units and label
    String y_units = ds.getY_units();
    String y_label = ds.getY_label();
    String title   = "Integrated Intensity";
                                          // make DataSet with new title and 
                                          // modified y units and label
    DataSetFactory ds_factory = new DataSetFactory( title,
                                                   "Degrees",
                                                   "Scattering Angle",
                                                    y_units,  
                                                   "Integrated "+ y_label);
    DataSet new_ds = ds_factory.getDataSet();
                                          // copy and update the log
                                          // copy the list of attributes
    new_ds.copyOp_log(ds);
    new_ds.addLog_entry("Calculated Integrated Intensity vs Angle");
    new_ds.addLog_entry("on the interval [ " + a + ", " + b + " ]");
    new_ds.setAttributeList( ds.getAttributeList() );

                                          // Sort the DataSet based on the
                                          // effective position.  This orders it
                                          // by scattering angle.
    Operator sort_op = new DataSetSort(ds, Attribute.DETECTOR_POS, true, false);
    sort_op.getResult();
                                          // for each Data block, find the 
                                          // integrated intensity on [a,b] and 
                                          // the scattering angle 
    float area[]  = new float[ ds.getNum_entries() ];
    float angle[] = new float[ ds.getNum_entries() ];
    float x_vals[],
          y_vals[];
    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      Data d = ds.getData_entry( i );     // use method IntegrateHistogram to
      x_vals = d.getX_scale().getXs();    // take care of partial bins
      y_vals = d.getY_values();
      if(d.isHistogram()){
         area[i] = NumericalAnalysis.IntegrateHistogram( x_vals, y_vals, a, b );
      }else{
         area[i] = NumericalAnalysis.IntegrateFunctionTable( x_vals,y_vals,a,b);
      }

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
    Data new_d = Data.getInstance( x_scale, area, 1 );

    new_ds.addData_entry( new_d );
    return new_ds;
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    Operator op = new IntegratedIntensityVsAngle();
    op.CopyParametersFrom( this );
    return op;
  }
  
  
  public String getDocumentation()
    {
      StringBuffer Res = new StringBuffer();
      Res.append("@overview This operator produces a DataSet with one entry,");
       Res.append(" a Data block giving the integrated intensity of a");
       Res.append(" histogram over a specified interval, as a tabulated");
       Res.append(" function of the scattering angle 2*theta.");

      Res.append("@algorithm Check for degenerate cases.  Get the original y");
       Res.append(" units and label.  Make DataSet with new title and");
       Res.append(" modified y units and label.Copy and update the log.  Copy");
       Res.append(" the list of attributes.  Sort the DataSet based on the");
       Res.append(" effective position.  This orders it  by scattering angle.");
       Res.append("  For each Data block, find the integrated intensity on");
       Res.append(" [a,b] and the scattering angle.Use method");
       Res.append(" IntegrateHistogram to take care of partial bins.  There");
       Res.append(" may be several groups with the same angle, so we need to");
       Res.append(" combine them to keep distinct x's copy non-duplicates");
       Res.append(" into arrays of the proper size.  Make a new Data block");
       Res.append(" with the new x and y values and group ID 1 .  The x");
       Res.append(" values must be increasing, and they will be since the");
       Res.append(" DataSet was sorted on the detector position.");  

      Res.append("@param  ds  DataSet for which the integrated intensity vs");
       Res.append(" group angle will be calculated. ");
      Res.append("@param  a  Left endpoint of interval where the histogram");
       Res.append(" is integrated.");
      Res.append("@param  b  Right endpoint of interval where the histogram");
       Res.append(" is integrated.");

      Res.append("@return If successful, this operator produces a DataSet");
       Res.append(" containing the integrated intensity of a spectrum over");
       Res.append(" the specified interval [a,b]. If the original DataSet");
       Res.append(" is null, or the interval is invalid, or some Data block");
       Res.append(" of the original DataSet does not have a detector position");
       Res.append(" attribute, an error message is returned.");

      Res.append("@error DataSet is null in IntegratedIntensityVsAngle");
      Res.append("@error [a,b] invalid in IntegratedIntensityVsAngle:");
      Res.append("@error NO DetectorPosition for group < group id >");
  
     return Res.toString();
    }

 /* ------------------------------- main --------------------------------- */ 
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
     System.out.println("Test of IntegratedIntensityVsAngle starting...");

                                                              // load a DataSet
     String filename = "/usr/local/ARGONNE_DATA/hrcs2447.run";
     RunfileRetriever rr = new RunfileRetriever( filename );
     DataSet ds = rr.getDataSet(1);
                                                              // make operator
                                                              // and call it
     IntegratedIntensityVsAngle op = 
                                new IntegratedIntensityVsAngle( ds, 800, 880 );
     Object obj = op.getResult();
     if ( obj instanceof DataSet )                   // we got a DataSet back
     {                                               // so show it and original
       DataSet new_ds = (DataSet)obj;
       new ViewManager( ds,     IViewManager.IMAGE );
       new ViewManager( new_ds, IViewManager.IMAGE );
     }
     else
       System.out.println( "Operator returned " + obj );

     System.out.println("Test of IntegratedIntensityVsAngle done.");
  }
}
