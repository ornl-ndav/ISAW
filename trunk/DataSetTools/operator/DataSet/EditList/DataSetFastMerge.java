/*
 * File:  DataSetFastMerge.java 
 *             
 * Copyright (C) 2008, Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA. 
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Last Modified:
 *
 *  $Author:$
 *  $Date:$            
 *  $Revision:$
 */

package DataSetTools.operator.DataSet.EditList;

import gov.anl.ipns.Parameters.IParameter;
import gov.anl.ipns.Util.SpecialStrings.*;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.viewer.*;
import  DataSetTools.operator.*;

/**
  * This operator creates a new DataSet by combining REFERENCES to the 
  * Data blocks from the current DataSet with REFERENCES to the Data 
  * blocks of a specified DataSet.  That is, this operator only does
  * a SHALLOW COPY of the Data blocks.  This will only be done if the two
  * DataSets have the same X and Y units.  If a * DataSet with N Data
  * blocks is merged with a DataSet with M Data blocks, the new DataSet
  *  will have N+M Data blocks.
  */

public class DataSetFastMerge extends DS_EditList 
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

  public DataSetFastMerge( )
  {
    super( "Merge with a DataSet" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /** 
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *  
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  ds_to_merge The DataSet to merge with DataSet ds.
   */

  public DataSetFastMerge( DataSet  ds,
                           DataSet  ds_to_merge )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( ds_to_merge );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: 
   *            in this case FastMerge
   */
   public String getCommand()
   {
     return "FastMerge";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "DataSet to Merge",
                                          DataSet.EMPTY_DATA_SET ); 
    addParameter( parameter );
  }

  /* ------------------------------ getDocumentation ------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */

  public String getDocumentation()
  {
     StringBuffer s = new StringBuffer();
     s.append("@overview This operator creates a new DataSet by combining ");
     s.append("REFERENCES to Data blocks from the current DataSet with ");
     s.append("REFERENCES to Data blocks of a specified DataSet.  That ");
     s.append("is, this Operator does a SHALLOW COPY of the Data blocks. ");
     s.append("@assumptions The two DataSets have the same X and Y units. ");
     s.append("@algorithm Merges REFERENCES of the Data blocks from ");
     s.append("two DataSet to form a new DataSet.  ");

     s.append("If a DataSet with N Data blocks is merged with a DataSet ");
     s.append("with M Data blocks, the new DataSet will have N+M Data ");
     s.append("blocks. ");
     s.append("@param The DataSet to which the operation is applied.");
     s.append("@param The DataSet to merge with the first DataSet.");
     s.append("@return The DataSet which consists of the merging of the ");
     s.append("first and second DataSets.");
     s.append("@error Returns an ErrorString if the two DataSets' X and Y ");
     s.append("units do not match.");
     return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  Gets the second DataSet from the parameter list and merges its Data
   *  objects with Data objects from the first DataSet. NOTE: This operator
   *  just copies REFERENCES to data blocks into the new DataSet, it does
   *  not do a deep copy of the Data blocks.
   *  @return DataSet which results from merging REFERENCES to the Data 
   *          blocks from the first and second DataSets.
   */
  public Object getResult()
  {                                  // get the DataSet to merge with 
    DataSet ds_to_merge = (DataSet)(getParameter(0).getValue());

                                     // get the current data set
    DataSet ds = this.getDataSet();

    if ( !ds.SameUnits(ds_to_merge)) // DataSets are NOT COMPATIBLE TO COMBINE
      return new ErrorString("Error DataSets have different units " + 
                  ds.getX_units() + " != " + ds_to_merge.getX_units() + " or " +
                  ds.getY_units() + " != " + ds_to_merge.getY_units() ); 
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = ds.empty_clone(); 
    new_ds.addLog_entry( "Fast Merged with " + ds_to_merge );
    new_ds.CombineAttributeList( ds_to_merge );

    Data data, 
         new_data; 
                                           // put in REFERENCES to Data blocks 
                                           // from the current data set
    int num_data = ds.getNum_entries();
    for ( int i = 0; i < num_data; i++ )
    {
      data     = ds.getData_entry( i );    // add reference to the Data entry
      new_ds.addData_entry( data );        // to the new DataSet  
    }
                                           // put in REFERENCES to Data blocks
                                           // from the second data set
    num_data = ds_to_merge.getNum_entries();
    for ( int i = 0; i < num_data; i++ )
    {
      data     = ds_to_merge.getData_entry( i );  // add reference to the Data
      new_ds.addData_entry( data );               // to the new DataSet
    }

    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetFastMerge Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    DataSetFastMerge new_op = new DataSetFastMerge( );
                                                // copy the data set associated
                                                // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    DataSet ds1 = DataSetFactory.getTestDataSet();
    DataSet ds2 = DataSetFactory.getTestDataSet();
    new ViewManager(ds1, ViewManager.IMAGE);
    new ViewManager(ds2, ViewManager.IMAGE);
    
    Operator op = new DataSetFastMerge(ds1, ds2);
    DataSet new_ds = (DataSet)op.getResult();
    new ViewManager(new_ds, ViewManager.IMAGE);
    
    String documentation = op.getDocumentation();
    System.out.println(documentation);
    System.out.println("\n" + op.getResult().toString());
  }

}
