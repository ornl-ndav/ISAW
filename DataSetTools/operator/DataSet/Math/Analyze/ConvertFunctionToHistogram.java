/*
 * File:  ConvertFunctionToHistogram.java
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
 * Revision 1.5  2002/12/03 20:59:24  dennis
 * Added getDocumentation() and simple main() program for testing.
 * (Shannon Hintzman)
 *
 * Revision 1.4  2002/11/27 23:18:38  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/09/19 16:01:52  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.2  2002/03/13 16:19:17  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.1  2002/02/22 21:02:27  pfpeterson
 * Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Math.Analyze;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
  * This operator converts the tabulated functions in a DataSet to
  * histograms.
  */

public class ConvertFunctionToHistogram extends    AnalyzeOp 
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

  public ConvertFunctionToHistogram()
  {
    super( "Convert Tabulated Functions to Histograms" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  width_1     Width of the first histogram bin to be created.
   *                      All later histogram bin widths will be generated from
   *                      this and the current tabluated x-coordinates assuming
   *                      that the tabluated x-coordinates are the centers of
   *                      the corresponding histogram bins.  This process is 
   *                      error prone, so if a first bin width is specified,
   *                      be sure to do it accurately.  If the first bin width
   *                      is specified as zero, the distance between the first
   *                      two x values is used by default.  This works properly
   *                      for uniformly spaced x values.     
   *  @param  multiply    Flag that indicates whether the function values
   *                      should be multiplied by the width of the new
   *                      histogram bins that are created.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the Data blocks of the original
   *                      DataSet are just altered.
   */

  public ConvertFunctionToHistogram( DataSet  ds,
                                     float    width_1,
                                     boolean  multiply,
                                     boolean  make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( new Float(width_1) );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean(multiply) );

    parameter = getParameter( 2 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /*----------------------------getDocumentation-----------------------------*/
  
   public String getDocumentation()
   {
   	StringBuffer Res = new StringBuffer();
	
	Res.append("@overview This operator converts the tabulated functions ");
	Res.append("in a DataSet to histograms.");
	
	Res.append("@algorithm If make a new DataSet is selected, an empty ");
    	Res.append("clone of the DataSet is constructed and the new histogram");
    	Res.append(" data is added to it. If it is not selected the original ");
	Res.append("DataSet values will be altered with the new histogram ");
	Res.append("data.");
	
	Res.append("@param ds - The DataSet to which the operation is applied");
	Res.append("@param width_1 - Width of the first histogram bin to ");
	Res.append("be created. All later histogram bin widths will be ");
	Res.append("generated from this and the current tabluated ");
	Res.append("x-coordinates assuming that the tabluated x-coordinates ");
	Res.append("are the centers of the corresponding histogram bins.  ");
	Res.append("This process is error prone, so if a first bin width is ");
	Res.append("specified, be sure to do it accurately.  If the first bin");
	Res.append(" width is specified as zero, the distance between the ");
	Res.append("first two x values is used by default.  This works ");
	Res.append("properly for uniformly spaced x values.");
	Res.append("@param  multiply - Flag that indicates whether the ");
	Res.append("function values should be multiplied by the width of the ");
	Res.append("new histogram bins that are created.");
	Res.append("@param  make_new_ds - Flag that determines whether a new ");
	Res.append("DataSet is constructed, or the Data blocks of the ");
	Res.append("original DataSet are just altered.");
	
	Res.append("@return Returns a DataSet or a String.  If a new DataSet ");
	Res.append("is made then the return object will be a new DataSet ");
	Res.append("containing only the new histogram data values.  If a new ");
	Res.append("DataSet is not made then the return object will be a ");
	Res.append("String that reads \"Data converted to histograms\".");
	
	return Res.toString();
   
   }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case
   *		 ConvFunc
   */
   public String getCommand()
   {
     return "ConvFunc";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Width of first bin", new Float(0) );
    addParameter( parameter );

    parameter = new Parameter("Multiply by histogram bin width?",
                               new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter("Make new DataSet?", new Boolean(true) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**
  *  @return returns a DataSet or a String
  *  If a new DataSet is made then the return object will be a new DataSet 
  *  containing only the new histogram data values. 
  *  If a new DataSet is not made then the return object will be a String
  *  that says "Data converted to histograms".
  */
  
  
  public Object getResult()
  {
                                  // get the parameters specified by the user 

    float width_1       = ((Float)getParameter(0).getValue()).floatValue();
    boolean multiply    = ((Boolean)getParameter(1).getValue()).booleanValue();
    boolean make_new_ds = ((Boolean)getParameter(2).getValue()).booleanValue();

    DataSet ds     = getDataSet();
    DataSet new_ds = null;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;

    new_ds.addLog_entry( "Converted to histogram" );

    Data             data,
                     new_data;
    int              num_data = ds.getNum_entries();

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );        // get reference to the data entry

      new_data = new HistogramTable( data, multiply, data.getGroup_ID() );
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
      return new String( "Data converted to histograms" );
    }
 }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current ConvertFunctionToHistogram Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    ConvertFunctionToHistogram new_op = new ConvertFunctionToHistogram( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
  
  /*------------------------------ main -----------------------------------*/
  public static void main(String[] args)
  {
  	ConvertFunctionToHistogram op = new ConvertFunctionToHistogram();
      
	System.out.println(op.getDocumentation() + "\n");
	
	//System.out.println(op.getResult().toString());
	//Calling the getResult() method creates a NullPointerException
  }

}
