/*
 * File: SetGroupIDs.java 
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * and by the National Science Foundation, Award No. DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2002/09/19 16:00:11  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/09/17 20:28:12  dennis
 *  Operator to set Group ID on all Data blocks in a Data set.
 *
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import  java.io.*;
import  java.util.Vector;
import  java.util.*;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
  * This operator sets the Group IDs for all Data blocks in a DataSet.  The
  * Group IDs to use can be specified or default values will be used.
  * Group IDs must be non-negative.  If the list of Group IDs has negative
  * values, or is empty, the position of the Data block in the DataSet's
  * list of Data blocks will be used as the Group ID.  If the list of Group IDs
  * is non-empty and valid, then the Group IDs will be assigned to the Data
  * blocks in order.  If there are too many Group IDs in the list, the extra
  * IDs are ignored.  If there are not enough Group IDs in the list to assign
  * an ID to every Data block, then the remaining Data blocks will be assigned
  * IDs sequentially, starting with the first un-used ID. 
  */

public class SetGroupIDs extends    DS_Attribute 
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

  public SetGroupIDs( )
  {
    super( "Set Group ID for all Data blocks" );
  }

  
/* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet whose Data block's IDs are to be set.
   *  @param  ids         String specifying the list of group IDs to set on 
   *                      the Data blocks.  If this is empty or invalid, the
   *                      indices 0,1,2,3... will be assigned to the Data
   *                      blocks in order.
   */

  public SetGroupIDs ( DataSet       ds,
                       IntListString ids )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( ids );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case, SetIDs
   */
   public String getCommand()
   {
     return "SetIDs";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Group IDs", new IntListString("") );
    addParameter( parameter ); 
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {  
     DataSet ds = getDataSet();

     IntListString ids_string = (IntListString)(getParameter(0).getValue());

     String list_string = ids_string.toString();

     if ( ds == null )
       return new ErrorString("DataSet is null");     

    
     int ids[] = null;
     if ( list_string != null && list_string.trim().length() != 0 )
     {
       ids = IntList.ToArray( list_string );
       if ( ids.length <= 0 || ids[0] < 0 )                // invalid, so
         ids = null;                                       // ignore list
     }

     int index = -1;
     for ( int i = 0; i < ds.getNum_entries(); i++ )    // apply to all
     {
       Data d = ds.getData_entry(i);
          
       if ( ids != null && i < ids.length )             // use specified id
         index = ids[i];
       else
         index++;

       d.setGroup_ID(index);  
     } 

     String log_string;
     if ( ids == null )
       log_string = "IDs set to default 0, 1, 2, ...";
     else
       log_string = "IDs set starting with " + list_string;
 
     ds.addLog_entry( log_string );
     return log_string;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SetDataLabel Operator.  The list of 
   * parameters  and the reference to the DataSet to which it applies is 
   * copied.
   */
  public Object clone()
  {
    SetGroupIDs new_op = new SetGroupIDs( );
                                                // copy the data set associated
                                                // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
