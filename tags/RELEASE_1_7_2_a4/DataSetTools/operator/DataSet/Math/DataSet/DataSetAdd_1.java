/*
 * File:  DataSetAdd_1.java 
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
 *  Revision 1.5  2004/01/24 19:38:29  bouzekc
 *  Removed unused variables from main() and removed unused imports.
 *
 *  Revision 1.4  2002/11/27 23:18:49  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/11/12 23:28:54  dennis
 *  Added getDocumentation and main methods.  Added JavaDoc comments for the
 *  getResult() method.  (modified by: Tyler Stelzer)
 *
 *  Revision 1.2  2002/09/19 16:02:14  pfpeterson
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
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.DataSet.DSOpsImplementation;
import  DataSetTools.parameter.*;
import  DataSetTools.viewer.*;
import  DataSetTools.operator.*;

/**
  *  Add one Data "block" from a second DataSet to all Data "blocks"
  *  of the current DataSet.  The resulting Data blocks are placed in a  
  *  new DataSet provided the parameter "make_new_ds" is true, otherwise
  *  the Data blocks of the current DataSet are altered.
  *
  *  The title of this operator is "DataSet plus one Data block".
  *
  *  The command name for this operator is "Add".
  *
  *  @see DataSetTools.operator.DataSet.DataSetOperator
  *  @see DataSetTools.operator.Operator
  */

public class DataSetAdd_1 extends    DataSetOp 
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

  public DataSetAdd_1( )
  {
    super( "DataSet plus one Data block" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  ds_2        The DataSet containing the Data block to be added 
   *                      to all Data blocks of DataSet ds.
   *  @param  id          The id of the Data block to use from DataSet ds_2.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                      constructed, or the specified Data block from the 
   *                      second DataSet is just added to the Data blocks 
   *                      of the current DataSet.
   */

  public DataSetAdd_1( DataSet    ds,
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
   *          in this case Add
   */
   public String getCommand()
   {
     return "Add";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "DataSet to Add", 
                                          DataSet.EMPTY_DATA_SET );
    addParameter( parameter );

    parameter = new Parameter( "ID of Group to Add", new Integer(5) ); 
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**
   *  @return    An Object which represents the DataSet which is the result 
   *             of Adding one "block" of a DataSet from this DataSet.  
   *             Returns an error message if the units from the two DataSets 
   *             are not compatible.
   */ 
  public Object getResult()
  {       
    return DSOpsImplementation.DoDSOneDataBlockOp( this );
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetAdd_1 Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    DataSetAdd_1 new_op    = new DataSetAdd_1( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
  
  public String getDocumentation(){
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator adds one block of two DataSets");
     Res.append(" together. When the operation is successful and a new");
     Res.append(" DataSet is created, a reference to this new DataSet is");
     Res.append(" returned.  If a new DataSet is NOT created, the result is");
     Res.append(" stored in the current DataSet and a reference to the");
     Res.append(" current DataSet is returned. If the operation is NOT");
     Res.append(" successful, an error string is returned.");
     
    Res.append("@algorithm Construct a new DataSet with the same title, units");
     Res.append(" and operations as the current DataSet. Add the values in");
     Res.append(" the DataSet blocks.  If make a new DataSet is selected, the");
     Res.append(" new values will be stored in a new DataSet.  If it is not");
     Res.append(" selected, the new values will be stored in the current");
     Res.append(" DataSet.");
    
    Res.append("@param ds - the current DataSet on which the operator will be");
    Res.append(" performed.");
    Res.append("@param ds_to_add - the DataSet to add to the current DataSet.");
    Res.append("@param  id - The id of the Data block to use from DataSet");
     Res.append(" ds_2.");
    Res.append("@param make_new_ds - a boolean value which determines if a");
     Res.append(" new DataSet is created or not.");    
     
    Res.append("@return returns a new DataSet or an Error String.");
     Res.append("If \"create a new DataSet\" is selected and operation is");
     Res.append(" successful, a reference to a new DataSet will be returned.");
     Res.append("  If it is successful without creating a new DataSet, a");
     Res.append(" reference to the current DataSet will bereturned. If the");
     Res.append(" operation is not successful, an error string will be");
     Res.append(" returned");
     
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
    new ViewManager(ds1, ViewManager.IMAGE);
    Operator op = new DataSetAdd_1( ds1, ds2, 3, true );
    DataSet new_ds = (DataSet)op.getResult();
    new ViewManager(new_ds, ViewManager.IMAGE);
    
    String documentation = op.getDocumentation();
    System.out.println(documentation);
    System.out.println("\n" + op.getResult().toString());
  }//main()
}
