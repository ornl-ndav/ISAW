/*
 * File:  FitExpressionToGroup.java 
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
 * Revision 1.4  2002/11/27 23:18:38  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/11/18 21:42:48  dennis
 * Added getDocumentation() method, trivial main() program, and
 * documentation for getResult() method. (Tyler Stelzer)
 *
 * Revision 1.2  2002/07/16 22:43:16  dennis
 * Now writes parameter estimates, parameter sgimas and Chi squared
 * values to status pane
 *
 * Revision 1.1  2002/07/12 22:26:43  dennis
 * Fit function defined by expression to one Data block.
 *
 */

package DataSetTools.operator.DataSet.Math.Analyze;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;
import  DataSetTools.functions.*;
import  DataSetTools.operator.Parameter;

/**
  *  This operator fits a function defined by an expression to the data 
  *  values of one Data block over a specified domain.  A new DataSet is 
  *  produced that contains a new Data block that contains the fitted
  *  function.
  */

public class  FitExpressionToGroup  extends    AnalyzeOp 
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

  public FitExpressionToGroup( )
  {
    super( "Fit Expression to Group" );
  }

 /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
 /**
  *  Creates operator with title "Fit Expression to Group" and the specified 
  *  list of parameters.  The getResult method must still be used to execute
  *  the operator.
  *
  *  @param  ds            DataSet containing the Data to fit.
  *  @param  group_id      Group_id of group to fit function to.
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
  *  @param  x_min         Minimum value for domain of fitted function. 
  *  @param  x_max         Maximum value for domain of fitted function. 
  *                        function Data block.
  *  @param  make_new_ds   Flag that determines whether a new DataSet is
  *                        constructed, or the new fitted function is just
  *                        added to the original DataSet. 
  */
  public FitExpressionToGroup( DataSet  ds,
                               int      group_id,
                               String   expression,
                               String   var_name,
                               String   par_names,
                               String   par_values,
                               float    x_min,
                               float    x_max,
                               boolean  make_new_ds )
  {
    this();
    parameters = new Vector();
    addParameter( new Parameter("Group ID", new Integer(group_id)) );
    addParameter( new Parameter("Expression", expression) );
    addParameter( new Parameter("Argument Name", var_name) );
    addParameter( new Parameter("Parameter Names", par_names) );
    addParameter( new Parameter("Parameter Values", par_values) );
    addParameter( new Parameter("Domain min", new Float(x_min) ) );
    addParameter( new Parameter("Domain max", new Float(x_max) ) );
    addParameter( new Parameter("Create new DataSet?",
                                 new Boolean(make_new_ds) ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: 
   *            in this case, FitExpr
   */
   public String getCommand()
   {
     return "FitExpr";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("Group ID", new Integer(1)) );
    addParameter( new Parameter("Expression", "m*x+b") );
    addParameter( new Parameter("Argument Name", "x") );
    addParameter( new Parameter("Parameter Names", "m,b") );
    addParameter( new Parameter("Parameter Values", "1,0") );
    addParameter( new Parameter("Domain min", new Float(0) ) );
    addParameter( new Parameter("Domain max", new Float(1000) ) );
    addParameter( new Parameter("Create new DataSet?",
                                 new Boolean(false) ) );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**
  * @return getResult either returns a reference to the current data set, or a
  *       new dataset which contains a new Data block that contains the fitted
  *       function.  If the operation is not succesful, an error string is 
  *       returned.
  *       Errors occur when there is no data entry with the group_ID, 
  *       bad number in par_values, an invalid interval,no Data points in 
  *       specified interval, or not enough data points to fit polynomial
  */ 

  public Object getResult()
  {                                  // get the parameters

    int     group_id   = ( (Integer)(getParameter(0).getValue()) ).intValue();
    String  expression =   (String)(getParameter(1).getValue());
    String  var_name   =   (String)(getParameter(2).getValue());
    String  par_names  =   (String)(getParameter(3).getValue());
    String  par_values =   (String)(getParameter(4).getValue());
    float   min_x      = ( (Float)(getParameter(5).getValue()) ).floatValue();
    float   max_x      = ( (Float)(getParameter(6).getValue()) ).floatValue();
    boolean make_new_ds =( (Boolean)getParameter(7).getValue() ).booleanValue();

                                     // get the current data set and do the 
                                     // operation
    DataSet ds = this.getDataSet();
    DataSet new_ds;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;

    Data data = ds.getData_entry_with_id( group_id );
    if ( data == null )
    {
      ErrorString message = new ErrorString( 
                           "ERROR: no data entry with the group_ID "+group_id );
      SharedData.addmsg( message );
      return message;
    }

    String parameter_names[] = StringUtil.extract_tokens( par_names,
                                                          " ,;:\t\n\r\f");
    String par_val_strings[] = StringUtil.extract_tokens( par_values,
                                                          " ,;:\t\n\r\f");
    double parameter_values[] = new double[par_val_strings.length];
    try
    {
      for ( int i = 0; i < parameter_values.length; i++ )
        parameter_values[i] = Double.valueOf(par_val_strings[i]).doubleValue();
    }
    catch( NumberFormatException e )
    {
      return new ErrorString( "Bad Number in " + par_values );
    }


    boolean is_histogram = data.isHistogram();

                                             // get the data we're approximating
    float x_vals[] = data.getX_scale().getXs();
    float y_vals[] = data.getY_values();
    float errors[] = data.getErrors();
                                             // get valid indices for the points
                                             // to use to find approximation 
    if ( min_x >= max_x    || 
         max_x < x_vals[0] || 
         min_x > x_vals[ x_vals.length-1 ] ) 
    {
      ErrorString message = new ErrorString(
           "ERROR: interval invalid, [a,b] = [" + min_x + "," + max_x +"]");
      SharedData.addmsg( message );
      return message;
    }

    int first_index = arrayUtil.get_index_of( min_x, x_vals );
    if ( first_index < 0 )
      first_index = 0;

    int last_index  = arrayUtil.get_index_of( max_x, x_vals );
    if ( last_index < 0 )
      last_index = x_vals.length-1;

    if ( last_index == -1 || first_index == last_index )
      return new ErrorString("ERROR: no Data points in specified interval");

                                            // Now extract the x values to
                                            // use for the curve fitting
    double x[] = null;
    int n_samples = 0;
    if ( is_histogram )                        // histogram, so treat the
    {                                          // y values as being at bin 
                                               // centers
      n_samples = last_index - first_index;
      if ( n_samples <= parameter_values.length )
        return new 
               ErrorString("ERROR: not enough data points to fit polynomial");

      x = new double[ n_samples ];
      for ( int i = 0; i < n_samples; i++ )
        x[i] = (x_vals[ first_index+i ] + x_vals[ first_index+i+1]) / 2.0;
    }
    else                                       // tabulated function, so
    {                                          // just use the data points 
      n_samples = last_index - first_index + 1;
      if ( n_samples <= parameter_values.length )
        return new 
               ErrorString("ERROR: not enough data points to fit polynomial");
        
      x = new double[ n_samples ];
      for ( int i = 0; i < n_samples; i++ )
        x[i] = x_vals[ first_index+i ];
    }
                                                // Next, get the y values
    double y[]     = new double[ n_samples ];
    double sigma[] = new double[ n_samples ];
    int    k;
    for ( int i = 0; i < n_samples; i++ )
    { 
      k = first_index + i;
      y[i] = y_vals[ k ];
      if ( errors == null )                     // if not specified, assume sqrt
        sigma[i] = Math.sqrt( Math.abs(y_vals[ k ]) ); 
      else
        sigma[i] = errors[ k ];
    }
                                                // Then, do the curve fitting

    Expression model_fun = new Expression( expression, 
                                           var_name, 
                                           parameter_names, 
                                           parameter_values );
    model_fun.setDomain( new ClosedInterval( min_x, max_x ) );
    MarquardtArrayFitter fitter = 
             new MarquardtArrayFitter( model_fun, x, y, sigma, 1.0e-20, 100 );

    double p_sigmas[] = fitter.getParameterSigmas();
    double[] coefs = model_fun.getParameters();
    String[] names = model_fun.getParameterNames();
    for ( int i = 0; i < model_fun.numParameters(); i++ )
    {    
      String info = ""+names[i] + " = ";
      info = info + Format.singleExp( coefs[i], 14 ) + " +- ";
      info = info + Format.singleExp( p_sigmas[i], 14 );
      SharedData.addmsg( info );
//      System.out.println(names[i] + " = " + coefs[i] +
//                         " +- " + p_sigmas[i] ); 
    }
    SharedData.addmsg( "Chi Sq = " + Format.singleExp( fitter.getChiSqr(),14));
    //System.out.println("Chi Sq = " + fitter.getChiSqr() );

    float xf[] = new float[x.length];
    for ( int i = 0; i < x.length; i++ )
      xf[i] = (float)x[i];
    XScale x_scale = XScale.getInstance( xf ); 
    Data model = new FunctionModel( x_scale, model_fun, 1000000+group_id );

    new_ds.addData_entry( model );
    if ( make_new_ds )
      return new_ds;  
    else
    {
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return "Fit complete";
    }
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current FitExpressionToGroup Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    FitExpressionToGroup new_op = new FitExpressionToGroup( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
  
  /*------------------------------ getDocumentation ----------------------*/
  public String getDocumentation()
  {
     StringBuffer Res = new StringBuffer();
     
     Res.append("@overview This operator fits a function to the data values");
      Res.append(" of one Data block over a specified domain.");
      
     Res.append("@algorithm This operator fits a function defined by an");
      Res.append(" expression to the data values of one Data block over a");
      Res.append(" specified domain.  If make_new_ds is true, A new DataSet");
      Res.append(" is produced that contains");
      Res.append(" a new Data block that contains the fitted function.");
     
     Res.append("@param  ds - DataSet containing the Data to fit.");
     Res.append("@param  group_id - Group_id of group to fit function to.");
     Res.append("@param  expression - String containing the expression");
      Res.append(" specifying the Data.");
     Res.append("@param  var_name - String containing the independent");
      Res.append(" variable name for this expression");
     Res.append("@param  par_names - String containing delimited list of"); 
      Res.append(" parameter names for this expression.  Valid delimiters");
      Res.append(" include \" ,;:\\t\\n\\r\\f\".  The parameter names must");
      Res.append(" be un-broken strings of alpha-numeric characters, or");
      Res.append("underscores.");
     Res.append("@param  par_values - String containing initial values for");
      Res.append(" the parameters.");
     Res.append("@param  x_min - Minimum value for domain of fitted function."); 
     Res.append("@param  x_max - Maximum value for domain of fitted function."); 
     Res.append("@param  make_new_ds - Flag that determines whether a new");
      Res.append(" DataSet is constructed, or the new fitted function is just");
      Res.append(" added to the original DataSet.");
     
     Res.append("@return getResult either returns a reference to the current");
      Res.append("data set, or a new dataset which contains a new Data block");
      Res.append(" that contains the fitted function.  If the operation is");
      Res.append(" not succesful, an error string is returned.");
  
     Res.append("@error no data entry with the group_ID");
     Res.append("@error Bad Number in <par_values>");
     Res.append("@error interval invalid, [a,b] = ");
     Res.append("@error no Data points in specified interval");
     Res.append("@error not enough data points to fit polynomial");
     
     return Res.toString();
  
  } 
  /*------------------------------- main ---------------------------------*/
  // this method is only used for testing purposes
   
  public static void main(String[]args)
  {
			       
     FitExpressionToGroup op = new FitExpressionToGroup();
					
     String documentation = op.getDocumentation();
     System.out.println(documentation);
     
     //THE FOLLOWING CALL TO getResult() USING DEFAULT PARAMETERS 
     //CAUSES A NULL-POINTER EXCEPTION
     
     //System.out.println("/n" + op.getResult().toString());
  
  }


}
