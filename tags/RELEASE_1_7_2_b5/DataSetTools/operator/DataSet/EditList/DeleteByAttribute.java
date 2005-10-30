/*
 * File:  DeleteByAttribute.java
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
 * $Log$
 * Revision 1.8  2005/08/19 17:54:37  dennis
 *   Delete will now be done more efficiently, since the DataSet is
 * now scanned one time to find all of the Data blocks that should
 * be deleted, and then DataSet.deleteData_entries(list) is called
 * once to delete the whole list.  When many Data blocks are being
 * deleted, this is much more efficient than deleting the individual
 * Data blocks one at a time.
 *   Added additional logic to deal with the case of missing attributes.
 * A data block that does NOT have the required attribute will now
 * be discarded if and only if the "discard" parameter is false.
 * That is, a false value for "discard" is interpreted to mean that
 * we only keep Data blocks that have the specified attribute in the
 * required range.  A Data block that is missing the specified
 * attribute does not meet that condition.
 *   Also cleaned up the end user documentation in getDocumentation().
 *
 * Revision 1.7  2004/03/15 06:10:47  dennis
 * Removed unused import statements.
 *
 * Revision 1.6  2004/03/15 03:28:29  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.5  2003/10/16 00:03:44  dennis
 * Fixed javadocs to build cleanly with jdk 1.4.2
 *
 * Revision 1.4  2003/02/06 20:32:54  dennis
 * Added getDocumentation() method. (Tyler Stelzer)
 *
 * Revision 1.3  2002/11/27 23:17:40  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/09/19 16:01:11  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.1  2002/02/22 21:01:53  pfpeterson
 * Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.EditList;

import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
  * This operator removes (or keeps) Data blocks with a specified attribute 
  * in a specified range.  The operator can either make a new DataSet, or
  * modify the current DataSet.
  */

public class DeleteByAttribute extends    DS_EditList 
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

  public DeleteByAttribute( )
  {
    super( "Delete Groups by Attribute" );
  }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *
   *  @param  attr_name   The name of that attribute to be used for the 
   *                      selection criterion from the data set.
   *
   *  @param  min         The lower bound for the selection criteria.  The
   *                      selected Data blocks satisfy:
   *                          min <= attribute value <= max
   *
   *  @param  max         The upper bound for the selection criteria.
   *
   *  @param  discard     Flag that indicates whether Data blocks that meet 
   *                      the selection criteria are to be kept or removed 
   *                      from the data set.
   *                      If discard==true, the selected blocks are deleted.
   *                      If discard==false, the un-selected blocks are deleted.
   *                      If the attribute is missing from a Data block, and
   *                      discard==true, the Data block is kept. 
   *                      If the attribute is missing from a Data block, and 
   *                      discard==false (meaning we only keep the ones with
   *                      the specified property), the Data block is discarded.
   *  @param  make_new_ds Flag that determines whether removing the Data
   *                      blocks makes a new DataSet and returns the new
   *                      DataSet as a value, or just removes the selected
   *                      blocks from the current DataSet and returns a
   *                      message indicating that the remove operation was
   *                      done.
   */

  public DeleteByAttribute( DataSet  ds,
                            String   attr_name,
                            float    min,
                            float    max,
                            boolean  discard,
                            boolean  make_new_ds   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( new AttributeNameString(attr_name) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( min ) );

    parameter = getParameter( 2 );
    parameter.setValue( new Float( max ) );

    parameter = getParameter( 3 );
    parameter.setValue( new Boolean( discard ) );

    parameter = getParameter( 4 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return  The command name to be used with script processor: 
   *          in this case, DelAtt
   */
   public String getCommand()
   {
     return "DelAtt";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Group Attribute to use for selection",
                               new AttributeNameString(Attribute.RAW_ANGLE) );
    addParameter( parameter );

    parameter = new Parameter( "Lower bound", new Float(-1.0) );
    addParameter( parameter );

    parameter = new Parameter( "Upper bound", new Float(1.0) );
    addParameter( parameter );

    parameter = new Parameter("Delete (or keep) selected groups?",
                               new Boolean(true) );
    addParameter( parameter );
    
    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                  // get the parameters specified by the user 

    String  attr_name = 
            ((AttributeNameString)getParameter(0).getValue()).toString();
    float   min       = ( (Float)(getParameter(1).getValue()) ).floatValue();
    float   max       = ( (Float)(getParameter(2).getValue()) ).floatValue();
    boolean discard   = ((Boolean)getParameter(3).getValue()).booleanValue();
    boolean make_new_ds = ((Boolean)getParameter(4).getValue()).booleanValue();

                                     // get the current data set
    DataSet ds = this.getDataSet();

    DataSet new_ds = ds;             // set new_ds to either a reference to ds
    if ( make_new_ds )               // or a clone of ds
      new_ds = (DataSet)ds.clone();

    if ( discard )
      new_ds.addLog_entry( "deleted groups with " + attr_name + 
                           " in [" + min + ", " + max + "]" );
    else
      new_ds.addLog_entry( "kept groups with " + attr_name + 
                           " in [" + min + ", " + max + "]" );
                                            // do the operation
    int num_data = new_ds.getNum_entries();
    Data data;

    boolean delete_flag = false;           // set true if attribute missing AND
                                           // we're only keeping things IN range
    float   val = 0;
    int     index_to_delete[] = new int[ num_data ];
    int     num_to_delete     = 0;
    for ( int i = 0; i < num_data; i++ )   // get list of indices to delete
    {                                      
      delete_flag = false;
      data = new_ds.getData_entry( i ); 
      Attribute attr = data.getAttributeList().getAttribute( attr_name );

      if ( attr != null )                  // keep or discard based on attribute
      {
         val = (float)attr.getNumericValue(); 
         if (attr_name.equals( Attribute.DETECTOR_POS ))  // convert to degrees
           val *= (float) 180.0/Math.PI;

         if ( discard && min <= val && val <= max  ||
              !discard && (min > val || val > max)   ) 
           delete_flag = true;
      }
      else                               // attribute was missing, so ....
        delete_flag = !discard;          // NOTE: If discard is false, then
                                         // we only want to keep the ones in 
                                         // the specified attribute range.  So, 
                                         // in this case, if the attribute is 
                                         // missing, we delete the Data block.
      if ( delete_flag ) 
      {
        index_to_delete[ num_to_delete ] = i;
        num_to_delete++;
      }
    }
                                            // now actually delete the Data 
    if ( num_to_delete > 0 )                // blocs listed.
    {
       if ( num_to_delete == 1 )
         new_ds.removeData_entry( index_to_delete[0] ); 
       else
       {
         int list[] = new int[ num_to_delete ];
         System.arraycopy( index_to_delete, 0, list, 0, num_to_delete );
         new_ds.removeData_entries( list );
       }
    }

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
   * Get a copy of the current DeleteByAttribute Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies are 
   * also copied.
   */
  public Object clone()
  {
    DeleteByAttribute new_op = new DeleteByAttribute( );
                                               // copy the data set associated
                                               // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
  
  public String getDocumentation()
    {
      StringBuffer Res = new StringBuffer();
      Res.append("@overview This operator removes (or keeps) Data blocks");
       Res.append(" with a specified attribute in a specified range.  The");
       Res.append(" operator can either make a new DataSet, or modify the");
       Res.append(" current DataSet.");

      Res.append("@algorithm Step through all of the Data blocks in the ");
       Res.append(" DataSet, recording the indices of all of the Data " );
       Res.append(" blocks that are to be deleted.  After finding the " );
       Res.append(" Data blocks to delete, the DataSet method " );
       Res.append(" deleteData_entries() is called once to delete them " );
       Res.append(" all, and the Observers of the DataSet are notified " );
       Res.append(" that Data was deleted." );
       Res.append(" NOTE: If a Data block does not have the specified");
       Res.append(" attribute, it will be deleted if the discard flag ");
       Res.append(" is false. ");

      Res.append("@param ds   The DataSet to which the operation is applied");   
      Res.append("@param attr_name   The name of that attribute to be used");
       Res.append(" for the selection criterion from the data set.");   

      Res.append("@param  min   The lower bound for the selection criteria.");
       Res.append(" The selected Data blocks satisfy:");
       Res.append(" min <= attribute value <= max");   

      Res.append("@param  max   The upper bound for the selection criteria.");

      Res.append("@param  discard   Flag that indicates whether Data blocks");
       Res.append(" that meet the selection criteria are to be kept or");
       Res.append(" removed from the data set. If discard==true, the selected");
       Res.append(" blocks are deleted. If discard==false, the un-selected");
       Res.append(" blocks are deleted.");
       Res.append(" If the attribute is missing from a Data block, and " );
       Res.append(" discard==true, the Data block is kept. ");
       Res.append(" If the attribute is missing from a Data block, and ");
       Res.append(" discard==false (meaning we only keep the ones with");
       Res.append(" the specified property), the Data block is discarded. ");

      Res.append("@param  make_new_ds   Flag that determines whether removing");
       Res.append(" the Data blocks makes a new DataSet and returns the new");
       Res.append(" DataSet as a value, or just removes the selected blocks");
       Res.append(" from the current DataSet and returns a message indicating");
       Res.append(" that the remove operation was done.");

      Res.append("@return Returns either a new DataSet or the string:");
       Res.append(" Specified Data blocks REMOVED.");
  
     return Res.toString();
    }


}
