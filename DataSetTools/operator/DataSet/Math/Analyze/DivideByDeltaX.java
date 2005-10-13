/*
 * File:  DivideByDeltaX.java 
 *             
 * Copyright (C) 2005, Dennis Mikkelson
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
 * Revision 1.1  2005/10/13 18:52:25  dennis
 * Initial version of DataSetOperator to divide each y value by a
 * corresponding delta_X value.  Currently units are handled by replacing
 * the Y_Units from the original DataSet with the units: Y_Units/X_Units.
 * This is correct, but no attempt is made to simplify these strings.
 *
 */

package DataSetTools.operator.DataSet.Math.Analyze;

import gov.anl.ipns.Util.Messaging.*;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;
import  DataSetTools.operator.DataSet.*;

/**
  * This operator divides each y value in a Data block, by a corresponding
  * delta_x value.  For Data blocks that are histograms, the counts in 
  * the ith bin are divided by the bin width.  In particular, yi 
  * is divided by (xi+1 - xi).  For Data blocks that are functions, all
  * but the last data value are divided by the NEXT interval length.  That is,
  * for functions, yi is also divided by (xi+1 - xi), for all but the last
  * data point.  For functions, the last data point is divide by the PREVIOUS
  * interval length, since there is not a next interval.   
  */

public class DivideByDeltaX extends    AnalyzeOp 
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

  public DivideByDeltaX( )
  {
    super( "Divide sample values yi, delta_xi" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the Data blocks of the original
   *                      DataSet are just altered.
   */

  public DivideByDeltaX( DataSet  ds,
                         boolean  make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /*----------------------------getDocumentation-----------------------------*/
  /**
   *  Get the documentation string for this operator.
   *
   *  @return A multi-line string describing this operator.
   */ 
   public String getDocumentation()
   {
     StringBuffer Res = new StringBuffer();

     Res.append("@overview This operator divides each y value in a ");
     Res.append("Data block, by a corresponding delta_x value.");

     Res.append("@algorithm If make a new DataSet is selected, an empty ");
     Res.append("clone of the DataSet is constructed and the new ");
     Res.append("data blocks are added to it. If it is not selected ");
     Res.append("the original DataSet values will be altered by dividing ");
     Res.append("the y values by a corresponding delta_x value.");
     Res.append("If a Data block is a histogram, or EventData and is ");
     Res.append("not just a table of values, it will first be replaced by a ");
     Res.append("HistogramTable.  The y values in the HistogramTable are ");
     Res.append("then divided by the bin width." );
     Res.append("If a Data block is function, but is not a just a table ");
     Res.append("of values, it will first be replaced by a FunctionTable. ");
     Res.append("All but the last y-value will then be divided by the ");
     Res.append("length of the NEXT interval.  The last y-value will be ");
     Res.append("divided by the length of the PREVIOUS interval.");
	
     Res.append("@param ds - The DataSet to which the operation is applied");

     Res.append("@param  make_new_ds - Flag that determines whether a new ");
     Res.append("DataSet is constructed, or the Data blocks of the ");
     Res.append("original DataSet are just altered.");

     Res.append("@return Returns a DataSet or a String.  If a new DataSet ");
     Res.append("is made then the return object will be a new DataSet ");
     Res.append("containing only the new data values.  If a new ");
     Res.append("DataSet is not made then the return object will be a ");
     Res.append("String that reads \"Data divided by delta_x\".");

     return Res.toString();
   }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: in this case
   *        DivideByDeltaX.
   */
   public String getCommand()
   {
     return "DivideByDeltaX";
   }


  /* -------------------------- setDefaultParmeters ------------------------- */
  /**
   *  Set the parameters to default values.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = 
                        new Parameter("Make new DataSet?", new Boolean(true) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**
  *  @return returns a DataSet or a String
  *  If a new DataSet is made then the returned object will be a new DataSet 
  *  containing only the new data values. 
  *  If a new DataSet is not made then the returned object will be a String
  *  that says "Data divided by delta_x".
  */
  public Object getResult()
  {                                // get the parameters specified by the user 

    boolean make_new_ds = ((Boolean)getParameter(0).getValue()).booleanValue();

    DataSet ds     = getDataSet();
    DataSet new_ds = null;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;

    Data   data,
           new_data;
    int    num_data = ds.getNum_entries();

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );       // get reference to the data entry
      new_data = null;
                                          // now make sure we have an actual
                                          // table of values
      if ( !(data instanceof HistogramTable) &&
           !(data instanceof FunctionTable )  )
        new_data = TabulatedData.getInstance( data, data.getGroup_ID() );
      else if ( make_new_ds )
        new_data = (Data)(data.clone());
      else
        new_data = data;

      float y[] = new_data.getY_values();    // change y values, by reference
      float x[] = new_data.getX_values();
      for ( int i = 0; i < y.length-1; i++ )
        y[i] /= (x[i+1] - x[i]); 

      if ( new_data.isHistogram() )
        y[y.length-1] /= (x[y.length] - x[y.length-1]);  
      else
        y[y.length-1] /= (x[y.length-1] - x[y.length-2]);  

      if ( make_new_ds )
        new_ds.addData_entry( new_data );
      else
        ds.replaceData_entry( new_data, j );
    }

    new_ds.addLog_entry( "Divided by delta_x" );
    new_ds.setY_units( new_ds.getY_units() + "/" + new_ds.getX_units() );

    if ( make_new_ds )
      return new_ds;
    else
    {
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return new String( "Data divided by delta_x" );
    }
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DivideByDeltaX Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    DataSetOperator new_op = new DivideByDeltaX( );
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
  

  /*------------------------------ main -----------------------------------*/
  public static void main(String[] args)
  {
    DivideByDeltaX op = new DivideByDeltaX();
      
    System.out.println(op.getDocumentation() + "\n");

    //System.out.println(op.getResult().toString());
    //Calling the getResult() method creates a NullPointerException
  }

}
