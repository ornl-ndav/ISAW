/*
 * File:  DataSetMultiply_1.java 
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
 *  Revision 1.3  2002/11/12 23:28:54  dennis
 *  Added getDocumentation and main methods.  Added JavaDoc comments for the
 *  getResult() method.  (modified by: Tyler Stelzer)
 *
 *  Revision 1.2  2002/09/19 16:02:18  pfpeterson
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
  *  Multiply one Data "block" from a second DataSet times all Data "blocks"
  *  of the current DataSet.  The resulting Data blocks are placed in a  
  *  new DataSet provided the parameter "make_new_ds" is true, otherwise
  *  the Data blocks of the current DataSet are altered.
  *
  *  The title of this operator is "DataSet times one Data block".
  *
  *  The command name for this operator is "Mult".
  *
  *  @see DataSetTools.operator.DataSet.DataSetOperator
  *  @see DataSetTools.operator.Operator
  */

public class DataSetMultiply_1 extends DataSetOp 
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

  public DataSetMultiply_1( )
  {
    super( "DataSet times one Data block" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  ds_2        The DataSet containing the Data block to be 
   *                      multiplied times all Data blocks of DataSet ds.
   *  @param  id          The id of the Data block to use from DataSet ds_2.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the specified Data block from the 
   *                      second DataSet is just multiplied times the Data 
   *                      blocks of the current DataSet.
   */

  public DataSetMultiply_1( DataSet    ds,
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
   *          in this case Mult 
   */
   public String getCommand()
   {
     return "Mult";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "DataSet to Multiply", 
                                          DataSet.EMPTY_DATA_SET );
    addParameter( parameter );

    parameter = new Parameter( "ID of Group to Multiply", new Integer(5) ); 
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**
  * @return returns a new DataSet or an Error String.
  *      If "create a new DataSet" is selected and operation is successful, a
  *      reference to a new DataSet will be returned.  If it is successful with out
  *      creating a new DataSet, a reference to the current DataSet will be returned.  If
  *      the operation is not successful, an error string will be returned.  Possible errors
  *      include "unsupported operation", "DataSets have different units", and 
  *      "no compatible Data blocks to combine in.
  *
  */
  public Object getResult()
  {       
    return DSOpsImplementation.DoDSOneDataBlockOp( this );
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetMultiply_1 Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    DataSetMultiply_1 new_op = new DataSetMultiply_1( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
  
  public String getDocumentation(){
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator multiplies one block of two DataSets together. ");
     Res.append("When the operation ");
     Res.append("is successful and a new DataSet is created, a reference to this new DataSet ");
     Res.append("is returned.  If a new DataSet is NOT created, the result is stored in the ");
     Res.append("current DataSet and a reference to the current DataSet is returned. If the ");
     Res.append("operation is NOT successful, an error string is returned.");
     
    Res.append("@algorithm Construct a new DataSet with the same title, units and operations");
     Res.append("as the current DataSet. Multiply the values in the DataSet blocks.  If make a new ");
     Res.append("DataSet is selected, the new values will be stored in a new DataSet.  If ");
     Res.append("it is not selected, the new values will be stored in the current DataSet.");
    
    Res.append("@param ds - the current DataSet on which the operator will be performed.");
    Res.append("@param ds_to_add - the DataSet to add to the current DataSet.");
    Res.append("@param  id - The id of the Data block to use from DataSet ds_2.");
    Res.append("@param make_new_ds - a boolean value which determines if a new DataSet is ");
     Res.append("created or not.");
        
    Res.append("@return returns a new DataSet or an Error String.");
     Res.append("If \"create a new DataSet\" is selected and operation is successful, a ");
     Res.append("reference to a new DataSet will be returned.  If it is successful without ");
     Res.append("creating a new DataSet, a reference to the current DataSet will bereturned. ");  
     Res.append("If the operation is not successful, an error string will be returned");
    
    Res.append("@error Unsupported operation");
    Res.append("@error DataSets have different units");
    Res.append("@error No compatible Data blocks");
     
     return Res.toString();
  }
  
  /* ---------------------------- main --------------------------------- */
  /**
   *  Main method for testing purposes.  Subtracts the 3rd "block"
   *  of ds2 (DataSet2) from ds1 (DataSet1) and displays each in a 
   *  viewer window.
   */

  public static void main( String[] args )
  {
    DataSet ds1 = DataSetFactory.getTestDataSet(); //create the first test DataSet
    DataSet ds2 = DataSetFactory.getTestDataSet(); //create the second test DataSet
    ViewManager viewer = new ViewManager(ds1, ViewManager.IMAGE);
    Operator op = new DataSetMultiply_1( ds1, ds2, 3, true );
    DataSet new_ds = (DataSet)op.getResult();
    ViewManager new_viewer = new ViewManager(new_ds, ViewManager.IMAGE);
    
    String documentation = op.getDocumentation();
    System.out.println(documentation);
    System.out.println("\n" + op.getResult().toString());
  }//main()
  
}
