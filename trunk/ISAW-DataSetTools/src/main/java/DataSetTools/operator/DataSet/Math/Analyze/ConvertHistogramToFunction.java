/*
 * File:  ConvertHistogramToFunction.java 
 *             
 * Copyright (C) 2000, Dennis Mikkelson
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
 * Revision 1.7  2004/03/15 06:10:49  dennis
 * Removed unused import statements.
 *
 * Revision 1.6  2004/03/15 03:28:30  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.5  2002/12/03 20:59:24  dennis
 * Added getDocumentation() and simple main() program for testing.
 * (Shannon Hintzman)
 *
 * Revision 1.4  2002/11/27 23:18:38  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/09/19 16:01:53  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.2  2002/03/13 16:19:17  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.1  2002/02/22 21:02:28  pfpeterson
 * Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Math.Analyze;

import gov.anl.ipns.Util.Messaging.*;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
  * This operator converts the histograms in a DataSet to tabulated functions.  
  */

public class ConvertHistogramToFunction extends    AnalyzeOp 
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

  public ConvertHistogramToFunction( )
  {
    super( "Convert Histograms to Tabulated Functions" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  divide      Flag that indicates whether the histogram values
   *                      should be divided by the width of the histogram bin.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the Data blocks of the original
   *                      DataSet are just altered.
   */

  public ConvertHistogramToFunction( DataSet  ds,
                                     boolean  divide,
                                     boolean  make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( new Boolean(divide) );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /*----------------------------getDocumentation-----------------------------*/
  
   public String getDocumentation()
   {
   	StringBuffer Res = new StringBuffer();
	
	Res.append("@overview This operator converts the histograms ");
	Res.append("in a DataSet to tabulated functions.");
	
	Res.append("@algorithm If make a new DataSet is selected, an empty ");
    	Res.append("clone of the DataSet is constructed and the new function");
    	Res.append(" data is added to it. If it is not selected the original ");
	Res.append("DataSet values will be altered with the new function ");
	Res.append("data.");
	
	Res.append("@param ds - The DataSet to which the operation is applied");
	Res.append("@param divide - Flag that indicates whether the histogram");
	Res.append(" values should be divided by the width of the histogram ");
	Res.append("bin.");
	Res.append("@param  make_new_ds - Flag that determines whether a new ");
	Res.append("DataSet is constructed, or the Data blocks of the ");
	Res.append("original DataSet are just altered.");
	
	Res.append("@return Returns a DataSet or a String.  If a new DataSet ");
	Res.append("is made then the return object will be a new DataSet ");
	Res.append("containing only the new function data values.  If a new ");
	Res.append("DataSet is not made then the return object will be a ");
	Res.append("String that reads \"Data converted to functions\".");
	
	return Res.toString();
   
   }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case
   *		 ConvHist
   */
   public String getCommand()
   {
     return "ConvHist";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Divide by histogram bin width?",
                                         new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter("Make new DataSet?", new Boolean(true) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**
  *  @return returns a DataSet or a String
  *  If a new DataSet is made then the return object will be a new DataSet 
  *  containing only the new function data values. 
  *  If a new DataSet is not made then the return object will be a String
  *  that says "Data converted to functions".
  */
  public Object getResult()
  {
                                  // get the parameters specified by the user 

    boolean divide      = ((Boolean)getParameter(0).getValue()).booleanValue();
    boolean make_new_ds = ((Boolean)getParameter(1).getValue()).booleanValue();

    DataSet ds     = getDataSet();
    DataSet new_ds = null;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;

    new_ds.addLog_entry( "Converted to function" );

    Data             data,
                     new_data;
    int              num_data = ds.getNum_entries();

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );        // get reference to the data entry

      new_data = new FunctionTable( data, divide, data.getGroup_ID() );
      if ( make_new_ds )
        new_ds.addData_entry( new_data );
      else
        ds.replaceData_entry( new_data, j );
    }

    if ( make_new_ds )
      return new_ds;
    else
    {
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return new String( "Data converted to functions" );
    }
 }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current ConvertHistogramToFunction Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    ConvertHistogramToFunction new_op = new ConvertHistogramToFunction( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
  
  /*------------------------------ main -----------------------------------*/
  public static void main(String[] args)
  {
  	ConvertHistogramToFunction op = new ConvertHistogramToFunction();
      
	System.out.println(op.getDocumentation() + "\n");
	
	//System.out.println(op.getResult().toString());
  	//Calling the getResult() method creates a NullPointerException
  }
}
