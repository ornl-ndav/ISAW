/*
 * File:  DeleteCurrentlySelected.java 
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
 *  $Log$
 *  Revision 1.5  2004/01/24 19:30:52  bouzekc
 *  Removed unused variables in testbed main().
 *
 *  Revision 1.4  2002/12/03 17:33:51  dennis
 *  Added getDocumentation() method, simple main() test program and
 *  added java docs to getResult().  (Chris Bouzek)
 *
 *  Revision 1.3  2002/11/27 23:17:40  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/09/19 16:01:13  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 21:01:54  pfpeterson
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
  *  Remove Data blocks that are marked as selected, or that are not marked
  *  as selected, depending on the parameters. 
  */

public class DeleteCurrentlySelected  extends    DS_EditList 
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

  public DeleteCurrentlySelected( )
  {
    super( "Delete currently selected Groups" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *
   *  @param  status      Flag that determines whether the selected or 
   *                      un-selected Data blocks are deleted.  
   *                      If status==true, the selected blocks are deleted.
   *                      If status==false, the un-selected blocks are deleted.
   *
   *  @param  make_new_ds Flag that determines whether removing the Data
   *                      blocks makes a new DataSet and returns the new 
   *                      DataSet as a value, or just removes the selected
   *                      blocks from the current DataSet and returns a 
   *                      message indicating that the remove operation was
   *                      done.
   */

  public DeleteCurrentlySelected( DataSet  ds,
                                  boolean  status,
                                  boolean  make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    IParameter parameter = getParameter( 0 );
    parameter.setValue( new Boolean( status ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: 
   *            in this case, DelSel
   */
   public String getCommand()
   {
     return "DelSel";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Delete (or keep) selected groups?",
                               new Boolean(true) );
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
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
     s.append("@overview This operator removes Data blocks that are marked ");
     s.append("as selected, or that are not marked as selected, depending ");
     s.append("on the parameters.");
     s.append("@assumptions The DataSet is not null.");
     s.append("@algorithm Removes specified Data blocks from the DataSet ");
     s.append("based on the following rules: If status == true, the selected ");
     s.append("blocks are deleted.  If status == false, the un-selected ");
     s.append("blocks are deleted.  If make_new_ds is true, creates a new ");
     s.append("DataSet.  If make_new_ds is false, removes the Data blocks ");
     s.append("from the original DataSet and appends an entry to the DataSet log.");
     s.append("@param The DataSet to which the operation is applied.");
     s.append("@param status: flag that determines whether the selected or ");
     s.append("un-selected Data blocks are deleted.");
     s.append("@param make_new_ds: flag that determines whether removing the ");
     s.append("Data blocks makes a new DataSet and returns the new DataSet ");
     s.append("as a value, or just removes the selected blocks from the current ");
     s.append("DataSet.");
     s.append("@return A new DataSet which consists of the original DataSet ");
     s.append("minus the Data blocks if make_new_ds is true.  Returns a ");
     s.append("String indicating that the Data blocks have been removed ");
     s.append("from the original DataSet if make_new_ds is false.");
     return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  Removes specified Data blocks from the DataSet based on the following 
   *  rules: If status == true, the selected blocks are deleted.  If 
   *  status == false, the un-selected blocks are deleted.  If make_new_ds 
   *  is true, creates a new DataSet.  
   *  @return A new DataSet which consists of the original DataSet minus 
   *  the Data blocks if make_new_ds is true.  Returns a String indicating 
   *  that the Data blocks have been removed from the original DataSet if 
   *  make_new_ds is false.
   */
  public Object getResult()
  {                                  // get the parameters

    boolean status      = ((Boolean)getParameter(0).getValue()).booleanValue();
    boolean make_new_ds = ((Boolean)getParameter(1).getValue()).booleanValue();

    DataSet ds     = this.getDataSet();

    DataSet new_ds = ds;             // set new_ds to either a reference to ds
    if ( make_new_ds )               // or a clone of ds
      new_ds = (DataSet)ds.clone();

    int selected_indices[] = ds.getSelectedIndices();
    int selected_groups[] = new int[selected_indices.length];

    int n_selected = selected_indices.length;
    for ( int i = 0; i < n_selected; i++ )
      selected_groups[i] = ds.getData_entry(selected_indices[i]).getGroup_ID();

    if ( n_selected > 0 )
      arrayUtil.sort( selected_groups );
      
    String selected = IntList.ToString( selected_groups );

    new_ds.removeSelected( status );
    new_ds.clearSelections();

    if ( status )
      if ( n_selected > 0 )
        new_ds.addLog_entry( "Deleted selected Data, IDs: " + selected );
      else
        new_ds.addLog_entry( "Deleted selected Data, (none selected)");
    else
      if ( n_selected > 0 )
        new_ds.addLog_entry( "Deleted un-selected Data, leaving IDs: " +
                              selected );
      else
        new_ds.addLog_entry( "Deleted un-selected Data blocks, leaving none" );

    if ( make_new_ds )                           
      return new_ds;
    else
    {
      new_ds.notifyIObservers( IObserver.DATA_DELETED );
      return new String("Specified Data blocks REMOVED");
    }                           
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DeleteCurrentlySelected Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    DeleteCurrentlySelected new_op = new DeleteCurrentlySelected( );
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
    new ViewManager(ds1, ViewManager.IMAGE);
    
    Operator op = new DeleteCurrentlySelected(ds1, false, true);
    DataSet new_ds = (DataSet)op.getResult();
    new ViewManager(new_ds, ViewManager.IMAGE);
    
    String documentation = op.getDocumentation();
    System.out.println("\n\nDocumentation:\n" + documentation);
  }

}
