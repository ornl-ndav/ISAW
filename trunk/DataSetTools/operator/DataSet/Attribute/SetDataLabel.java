/*
 * File:  SetDataLabel.java
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
 *  Revision 1.3  2002/11/19 22:57:05  dennis
 *  Added getDocumentation() method and basic main test program. (Tyler Stelzer)
 *
 *  Revision 1.2  2002/09/19 16:00:09  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/09/10 22:37:57  dennis
 *  Operator to set label on some or all Data blocks in a Data set.
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
  * This operator specifies the label for some or all of the Data blocks in
  * a DataSet.  The group IDs of the Data blocks for which the attribute is
  * set are specified as String parameter.  If the list of group IDs
  * is empty, then the label is applied for all groups in the DataSet.  If
  * the label is the name of an attribute, then the attribute's toString()
  * method will be used for the label.
  */

public class SetDataLabel extends    DS_Attribute 
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

  public SetDataLabel( )
  {
    super( "Set Label on some or all Data blocks" );
  }

  
/* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @parm   label       The new label to use for the specified Data blocks.
   *                      If this is the name of an attribute, then the
   *                      toString() method of the named attribute will be used
   *                      as the label. 
   *  @param  ids         String specifying the list of group IDs of the 
   *                      Data blocks whose labels are to be set.
   */

  public SetDataLabel ( DataSet       ds,
                        String        label,
                        IntListString ids )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( label );

    parameter = getParameter( 1 );
    parameter.setValue( ids );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
             in this case, SetLabel
   */
   public String getCommand()
   {
     return "SetDataLabel";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Label (or Attribute)", new String(""));
    addParameter(parameter);

    parameter = new Parameter( "Group IDs", new IntListString("") );
    addParameter( parameter ); 
  }


  /* ---------------------------- getResult ------------------------------- */
  /** Sets the label for a specified number of groups or the whole dataset and 
  *   returns the log_message if successful. Otherwise it returns an error 
  *   string if the dataSet is null
  */
  
  public Object getResult()
  {  
      DataSet ds = getDataSet();

      String        label      = (String)(getParameter(0).getValue());
      IntListString ids_string = (IntListString)(getParameter(1).getValue());

      String list_string = ids_string.toString();

      if ( ds == null )
        return new ErrorString("DataSet is null");     

      boolean all_set = false;
      if ( list_string == null || list_string.trim().length() == 0 )
        for ( int i = 0; i < ds.getNum_entries(); i++ )    // apply to all
        {
          Data d = ds.getData_entry(i);
          d.setLabel(label);  
          all_set = true;
        } 

       else                                           // apply to specific ones
       {
         int ids[] = IntList.ToArray( list_string );
         for ( int i = 0; i < ds.getNum_entries(); i++ )
         {
           Data d = ds.getData_entry(i);
           int  id = d.getGroup_ID();
           int  position = Arrays.binarySearch( ids, id );
           if ( position >= 0 )
             d.setLabel(label);
         }
       }

       String log_string = "Label set to: " + label;
       if ( all_set )
         log_string = log_string + " on all Data blocks";
       else
         log_string = log_string + " on groups " + list_string;
 
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
    SetDataLabel new_op = new SetDataLabel( );
                                                // copy the data set associated
                                                // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
  
  /* ------------------------------ getDocumentation ------------------- */
  public String getDocumentation()
  {
     StringBuffer Res = new StringBuffer();
     
     Res.append("@overview This operator specifies the label for some or");
      Res.append(" all of the Data blocks in a DataSet.");
     
     Res.append("@algorithm The group IDs of the Data blocks for which the");
      Res.append(" attribute is set are specified as String parameter.  If");
      Res.append(" the list of group IDs is empty, then the label is applied");
      Res.append(" for all groups in the DataSet.  If the label is the name");
      Res.append(" of an attribute, then the attribute's toString() method");
      Res.append(" will be used for the label.");
     
     Res.append("@param ds - The DataSet to which the operation is applied");
     Res.append("@parm label - The new label to use for the specified Data");
      Res.append(" blocks. If this is the name of an attribute, then the");
      Res.append(" toString() method of the named attribute will be used as");
      Res.append(" the label."); 
     Res.append("@param  ids - String specifying the list of group IDs of the"); 
      Res.append(" Data blocks whose labels are to be set.");
     
     Res.append("@return Sets the label for a specified number of groups");
      Res.append(" or the whole dataset and returns the log_message if");
      Res.append(" successful. Otherwise it returns an error string if the");
      Res.append(" dataSet is null.");
     
     Res.append("@error DataSet is null");
     
     return Res.toString();
  }
  
  /* ------------------------------ main ------------------------------- */
  public static void main(String [] args)
  {
  
    SetDataLabel op = new SetDataLabel();
    String documentation = op.getDocumentation();
    
    System.out.println(documentation);
    
    //NOTE:  invalid default parameters
    //getResult returns "DataSet is null"
    System.out.println(op.getResult().toString());
  
  }

}
