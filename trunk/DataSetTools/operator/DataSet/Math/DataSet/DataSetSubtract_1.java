/*
 * File:  DataSetSubtract_1.java 
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
 *  $Log$
 *  Revision 1.4  2002/11/27 23:18:49  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/11/19 22:09:14  dennis
 *  Added getDocumentation() method, main test program.  Also,
 *  now checks that units match before subtracting. (Chris Bouzek)
 *
 *  Revision 1.2  2002/09/19 16:02:20  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/07/17 20:28:39  dennis
 *  Mathematical operation between one Data block of a second
 *  DataSet and all Data blocks of the current DataSet.
 *  
 */

package DataSetTools.operator.DataSet.Math.DataSet;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.DataSet.DSOpsImplementation;
import  DataSetTools.parameter.*;
import  DataSetTools.viewer.*;
import  DataSetTools.operator.*;

/**
  *  Subtract one Data "block" of a second DataSet from all Data "blocks"
  *  of the current DataSet.  The resulting Data blocks are placed in a  
  *  new DataSet provided the parameter "make_new_ds" is true, otherwise
  *  the Data blocks of the current DataSet are altered.
  *
  *  The tile of this operator is "DataSet minus one Data block".
  *
  *  The command name for this operator is "Sub".
  *
  *  @see DataSetTools.operator.DataSet.DataSetOperator
  *  @see DataSetTools.operator.Operator
  */

public class DataSetSubtract_1 extends DataSetOp 
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

  public DataSetSubtract_1( )
  {
    super( "DataSet minus one Data block" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  ds_2        The DataSet containing the Data block to be 
   *                      subtracted from all Data blocks of DataSet ds.
   *  @param  id          The id of the Data block to use from DataSet ds_2.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the specified Data block from the 
   *                      second DataSet is just subtracted from the Data 
   *                      blocks of the current DataSet.
   */

  public DataSetSubtract_1( DataSet    ds,
                            DataSet    ds_2,
                            int        id,
                            boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( ds_2 );

    parameter = getParameter( 1 );
    parameter.setValue( new Integer( id ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return  the command name to be used with script processor: 
   *          in this case Sub 
   */
   public String getCommand()
   {
     return "Sub";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "DataSet to Subtract", 
                                          DataSet.EMPTY_DATA_SET );
    addParameter( parameter );

    parameter = new Parameter( "ID of Group to Subtract", new Integer(5) ); 
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
    s.append("@overview This operator subtracts one block of a DataSet from another.");
    s.append("@assumptions The units on the two DataSets are compatible.");
    s.append("@algorithm Uses the binary subtract from DSOpsImplementation.");
    s.append("This is a standard binary subtract.");
    s.append("@param The DataSet for the operation.");
    s.append("@param The DataSet from which to get the data block to subtract.");
    s.append("@param The index of the data block to subtract.");
    s.append("@param A boolean value of true if you want a new DataSet to be ");
    s.append("created, or false if you want the operation performed on the ");
    s.append("original DataSet.");
    s.append("@return The DataSet which is the result of subtracting the ");
    s.append("block of data from the second DataSet.");
    s.append("@return An error if the units of the two DataSets do not match.");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   * @return The result of subtracting the data blocks of one DataSet 
   * from this DataSet.
   */

  public Object getResult()
  {       
    // get the parameters which determine whether a new DataSet should be made
    // and the DataSet to subtract.
    Boolean make_new = (Boolean)this.getParameter(2).getValue();
    Integer data_block = (Integer)this.getParameter(1).getValue();
    DataSet ds_to_subtract = (DataSet)this.getParameter(0).getValue();
    DataSet current_ds = this.getDataSet();

    //if units do not match
    if( !current_ds.getX_units().equals(ds_to_subtract.getX_units()) || 
	!current_ds.getY_units().equals(ds_to_subtract.getY_units()) )
	return 
            new ErrorString("ERROR: The units on the DataSets do not match.");

    if( !make_new.booleanValue() )
    {
      StringBuffer s = new StringBuffer();
      s.append("Subtracted this block ");
      s.append(ds_to_subtract.getData_entry(data_block.intValue()));
      s.append(" of this DataSet ");
      s.append(ds_to_subtract.toString());
      s.append(".");
      current_ds.getOp_log().addEntry(s.toString());
    }

    //if units do match
    return DSOpsImplementation.DoDSOneDataBlockOp( this );
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetSubtract_1 Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    DataSetSubtract_1 new_op = new DataSetSubtract_1( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

  /* ---------------------------- main --------------------------------- */
  /**
   *  Main method for testing purposes.
   */

  public static void main( String[] args )
  {
    DataSet ds1 = DataSetFactory.getTestDataSet(); //create the first test DataSet
    DataSet ds2 = DataSetFactory.getTestDataSet(); //create the second test DataSet
    ViewManager viewer = new ViewManager(ds1, ViewManager.IMAGE);
    Operator op = new DataSetSubtract_1( ds1, ds2, 6, true );
    DataSet new_ds = (DataSet)op.getResult();
    ViewManager new_viewer = new ViewManager(new_ds, ViewManager.IMAGE);
  }//main()

}//DataSetSubtract_1
