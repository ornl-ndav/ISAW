/*
 * File:  LoadExpression.java 
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 * Revision 1.1  2002/04/17 21:49:34  dennis
 * Operator to Load DataSet using a mathematical expression.
 *
 */
package Operators.Generic.Load;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Load.*;
import DataSetTools.util.*;
import DataSetTools.functions.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import java.util.*;

/** 
 *    This operator constructs a DataSet with a Data block given by an 
 *  expression.
 *  <p>
 *  NOTE: This operator can also be run as a separate program, since it
 *  has a main program for testing purposes.  
 */

public class LoadExpression extends GenericLoad
{
  private static final String TITLE = "Load Expression";

 /* ------------------------- DefaultConstructor -------------------------- */
 /** 
  *  Creates operator with title "Load Expression" and a  default list of
  *  parameters.
  */  
  public LoadExpression()
  {
    super( TITLE );
  }

 /* ----------------------------- Constructor ----------------------------- */
 /** 
  *  Creates operator with title "Load Equation" and the specified list
  *  of parameters.  The getResult method must still be used to execute
  *  the operator.
  *  
  *  @param  expression    String containing the expression specifying the 
  *                        Data.
  *  @param  var_name      String containing the independent variable name 
  *                        for this expression 
  *  @param  par_names     String containing delimited list of parameter names
  *                        for this expression.  Valid delimiters include
  *                        " ,;:\t\n\r\f".  The parameter names must be un-
  *                        broken strings of alpha-numeric characters, or
  *                        underscores.
  *  @param  par_values    String containing initial values for the parameters.
  *  @param  x_min         Minimum argument value
  *  @param  x_max         Maximum argument value
  *  @param  n_steps       Number of evaluation points or bins
  *  @param  is_histogram  Flag indicating whether to construct a histogram or
  *                        function Data block.
  */
  public LoadExpression( String   expression,
                         String   var_name,
                         String   par_names,
                         String   par_values,
                         float    x_min,
                         float    x_max,
                         int      n_steps,
                         boolean  is_histogram )
  {
    this();
    parameters = new Vector();
    addParameter( new Parameter("Expression", expression) );
    addParameter( new Parameter("Argument Name", var_name) );
    addParameter( new Parameter("Parameter Names", par_names) );
    addParameter( new Parameter("Parameter Values", par_values) );
    addParameter( new Parameter("Domain min", new Float(x_min) ) );
    addParameter( new Parameter("Domain max", new Float(x_max) ) );
    addParameter( new Parameter("Number of samples", new Integer(n_steps) ) );
    addParameter( new Parameter("Histogram(or Function)", 
                                 new Boolean(is_histogram) ) );
  }

 /* ------------------------------ getCommand ----------------------------- */
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "LoadExpr", the command used to invoke this operator in Scripts
  */
  public String getCommand()
  {
    return "LoadExpr";
  }

 /* ------------------------ setDefaultParameters ------------------------- */
 /** 
  * Sets default values for the parameters.  The parameters set must match the 
  * data types of the parameters used in the constructor.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("Expression", "a*t*t+b*t*c") );
    addParameter( new Parameter("Argument Name", "t") );
    addParameter( new Parameter("Parameter Names", "a,b,c") );
    addParameter( new Parameter("Parameter Values", "1,-2,1") );
    addParameter( new Parameter("Domain min", new Float(-5) ) );
    addParameter( new Parameter("Domain max", new Float(5) ) );
    addParameter( new Parameter("Number of samples", new Integer(500) ) );
    addParameter( new Parameter("Histogram(or Function)", new Boolean(false) ));
  }

 /* ------------------------------ getResult ------------------------------- */
 /** 
  *  Executes this operator using the current values of the parameters.
  *
  *  @return  If successful, this returns a new DataSet with the data 
  *           given the expression.
  */
  public Object getResult()
  {
    String  expression = (String)(getParameter(0).getValue());
    String  var_name   = (String)(getParameter(1).getValue());
    String  par_names  = (String)(getParameter(2).getValue());
    String  par_values = (String)(getParameter(3).getValue());
    float   x_min      = ((Float)getParameter(4).getValue()).floatValue();
    float   x_max      = ((Float)getParameter(5).getValue()).floatValue();
    int     n_steps    = ((Integer)getParameter(6).getValue()).intValue();
    boolean is_histogram = ((Boolean)getParameter(7).getValue()).booleanValue();

    
    String parameter_names[] = StringUtil.extract_tokens( par_names, 
                                                          " ,;:\t\n\r\f"); 
    String par_val_strings[] = StringUtil.extract_tokens( par_values, 
                                                          " ,;:\t\n\r\f"); 
    float  parameter_values[] = new float[par_val_strings.length];
    try
    {
      for ( int i = 0; i < parameter_values.length; i++ )
        parameter_values[i] = Float.valueOf( par_val_strings[i] ).floatValue();
    }
    catch( NumberFormatException e )
    {
      return new ErrorString( "Bad Number in " + par_values ); 
    }

    XScale x_scale = new UniformXScale( x_min, x_max, n_steps );
    ClosedInterval domain = new ClosedInterval( x_min, x_max );

    Expression f = new Expression( expression, var_name, 
                                   parameter_names, parameter_values ); 
    f.setDomain( domain );

    Data d; 
    if ( is_histogram )
      d = new HistogramModel( x_scale, f, 1 );
    else 
      d = new FunctionModel( x_scale, f, 1 );

    DataSet ds = new DataSet( expression, "Initial Version" ); 
    ds.addData_entry( d );

    ds.addLog_entry("Generated Data:" + expression );
    return ds;
  }

 /* --------------------------------- clone -------------------------------- */
 /** 
  *  Creates a clone of this operator.  ( Operators need a clone method, so 
  *  that Isaw can make copies of them when needed. )
  */
  public Object clone()
  { 
    Operator op = new LoadExpression();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main ----------------------------------- */
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
    System.out.println("Test of LoadExpression starting...");

                                                 // make and run the operator
                                                 // to generate the data
    Operator op  = new LoadExpression("a*t*t+b*t+c", 
                                      "t",
                                      "a,b,c", "1,-2,1",
                                      -5, 5, 100, 
                                       true );
    Object   obj = op.getResult();
                                                 // display any message string
                                                 // that might be returned
    System.out.println("Operator returned: " + obj );

                                                 // if the operator produced a
                                                 // a DataSet, pop up a viewer
    if ( obj instanceof DataSet )
    {
      ViewManager vm = new ViewManager( (DataSet)obj, IViewManager.IMAGE );
    }

    System.out.println("Test of LoadExpression done.");
  }
}
