/*
 * File:  DataSetMerge.java 
 *             
 * Copyright (C) 1999, Dennis Mikkelson
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
 * This operator merges two DataSets by putting a copy of all spectra from
 * both DataSets into a new DataSet.  This will only be done if the X and Y
 * units match for the two DataSets being merged.
 *
 *  $Log$
 *  Revision 1.4  2002/12/03 17:33:51  dennis
 *  Added getDocumentation() method, simple main() test program and
 *  added java docs to getResult().  (Chris Bouzek)
 *
 *  Revision 1.3  2002/11/27 23:17:40  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/09/19 16:01:08  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 21:01:49  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.EditList;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;
import  DataSetTools.viewer.*;
import  DataSetTools.operator.*;

/**
  * This operator creates a new DataSet by combining the Data blocks from the 
  * current DataSet with the Data blocks of a specified DataSet.  This can 
  * only be done if the two DataSets have the same X and Y units.  If a 
  * DataSet with N Data blocks is merged with a DataSet with M Data blocks,
  * the new DataSet will have N+M Data blocks.
  */

public class DataSetMerge extends    DS_EditList 
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

  public DataSetMerge( )
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

  public DataSetMerge( DataSet             ds,
                       DataSet             ds_to_merge )
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
   *            in this case Merge
   */
   public String getCommand()
   {
     return "Merge";
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
     s.append("the Data blocks from the current DataSet with the Data ");
     s.append("blocks of a specified DataSet.");
     s.append("@assumptions The two DataSets have the same X and Y units.");
     s.append("@algorithm Merges the two DataSet's Data blocks.  ");
     s.append("If a DataSet with N Data blocks is merged with a DataSet ");
     s.append("with M Data blocks, the new DataSet will have N+M Data ");
     s.append("blocks.");
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
   *  objects with Data objects from the first DataSet.
   *  @return DataSet which consists of the merging of the first
   *          and second DataSets.
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
    new_ds.addLog_entry( "Merged with " + ds_to_merge );
    new_ds.CombineAttributeList( ds_to_merge );

                                           // do the operation
    Data data, 
         new_data; 
                                           // put in Data blocks from the 
                                           // current data set
    int num_data = ds.getNum_entries();
    for ( int i = 0; i < num_data; i++ )
    {
      data     = ds.getData_entry( i );    // get reference to the data entry
      new_data = (Data)data.clone( );      // clone it and add it to the new 
      new_ds.addData_entry( new_data );    // DataSet  
    }
                                           // put in Data blocks from the 
                                           // second data set
    num_data = ds_to_merge.getNum_entries();
    for ( int i = 0; i < num_data; i++ )
    {
      data     = ds_to_merge.getData_entry( i );   // get reference to the data
      new_data = (Data)data.clone( );              // clone it and add it to 
      new_ds.addData_entry( new_data );            // the new DataSet
    }

    return new_ds;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DataSetMerge Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    DataSetMerge new_op = new DataSetMerge( );
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
    ViewManager viewer1 = new ViewManager(ds1, ViewManager.IMAGE);
    ViewManager viewer2 = new ViewManager(ds2, ViewManager.IMAGE);
    
    Operator op = new DataSetMerge(ds1, ds2);
    DataSet new_ds = (DataSet)op.getResult();
    ViewManager new_viewer = new ViewManager(new_ds, ViewManager.IMAGE);
    
    String documentation = op.getDocumentation();
    System.out.println(documentation);
    System.out.println("\n" + op.getResult().toString());
  }

}
