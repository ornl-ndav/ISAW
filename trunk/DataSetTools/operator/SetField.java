/*
 * File:  SetField.java 
 *             
 * Copyright (C) 2000, Ruth Mikkelson,
 *                     Dennis Mikkelson
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.5  2001/04/26 19:10:46  dennis
 * Added copyright and GPL info at the start of the file.
 *
 * Revision 1.4  2000/11/10 22:41:34  dennis
 *    Introduced additional abstract classes to better categorize the operators.
 * Existing operators were modified to be derived from one of the new abstract
 * classes.  The abstract base class hierarchy is now:
 *
 *  Operator
 *
 *   -GenericOperator
 *      --GenericLoad
 *      --GenericBatch
 *
 *   -DataSetOperator
 *     --DS_EditList
 *     --DS_Math
 *        ---ScalarOp
 *        ---DataSetOp
 *        ---AnalyzeOp
 *     --DS_Attribute
 *     --DS_Conversion
 *        ---XAxisConversionOp
 *        ---YAxisConversionOp
 *        ---XYAxesConversionOp
 *     --DS_Special
 *
 *    To allow for automatic generation of hierarchial menus, each new operator
 * should fall into one of these categories, or a new category should be
 * constructed within this hierarchy for the new operator.
 *
 * Revision 1.3  2000/11/07 15:46:50  dennis
 * Major rewrite... Only allows setting things that are resonable to set, and
 * properly supports setting selections.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  * This operator sets a DataSet field 
  *
  *  @see DS_Attribute
  */

public class SetField extends    DS_Attribute 
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

  public SetField( )
  {
    super( "Set DataSet Field" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @parm   Fieldname   The Field to be set.
   *  @param  new_Value   The new value of the Attribute
   */

  public SetField( DataSet                ds,
                   DSSettableFieldString  Fieldname,
                   Object                 new_Value )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( Fieldname);

    parameter = getParameter( 1 );
    parameter.setValue(new_Value );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "SetField";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Field?", 
                                          new DSSettableFieldString("Title"));
    addParameter( parameter ); 
    
    parameter = new Parameter( " New Value?", null );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
    { 
     DataSet ds      = getDataSet();
     String  ds_name = ds.toString();

     String S =((DSSettableFieldString)(getParameter(0).getValue())).toString();
     Object O = getParameter(1).getValue();

     if ( O == null )
       return new ErrorString(" null value");

      try
      {
      if( S.equals( DSFieldString.TITLE ))
      {
        ds.setTitle(O.toString());
        ds.notifyIObservers( IObserver.FIELD_CHANGED );
      }  
      else if( S.equals(DSFieldString.X_LABEL))
      {
        ds.setX_label(O.toString());
        ds.notifyIObservers( IObserver.FIELD_CHANGED );
      }  
      else if( S.equals(DSFieldString.X_UNITS)) 
      {
         ds.setX_units(O.toString());     
         ds.notifyIObservers( IObserver.FIELD_CHANGED );
      }
      else if( S.equals(DSFieldString.Y_LABEL))
      {
         ds.setY_label(O.toString());
         ds.notifyIObservers( IObserver.FIELD_CHANGED );
      }
      else if( S.equals(DSFieldString.Y_UNITS))
      {
         ds.setY_units(O.toString());
         ds.notifyIObservers( IObserver.FIELD_CHANGED );
      }
      else if( S.equals(DSFieldString.POINTED_AT_INDEX))
      {
         ds.setPointedAtIndex(((Integer)O).intValue());
         ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
      }
      else if( S.equals(DSFieldString.POINTED_AT_ID))
      {  int      num_data = ds.getNum_entries();
         int      i        = 0;
         int      id       = ((Integer)O).intValue();
         boolean  found    = false;
         while ( !found && i < num_data )
         {
           if (ds.getData_entry(i).getGroup_ID() == id ) 
           { 
             found = true;
             ds.setPointedAtIndex(i);
             ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
           }
           i++;
         }
      }
      else if( S.equals(DSFieldString.SELECTED_GROUPS) )
      {
        int list[] = IntList.ToArray( O.toString() );
        int id, 
            index;

        ds.clearSelections();
        if ( list.length > 0 ) 
        for ( int i = 0; i < ds.getNum_entries(); i++ )
        {
          id = ds.getData_entry(i).getGroup_ID();
          index = arrayUtil.get_index_of( id, list, 0, list.length-1 ); 
                                                        // set the select flag
                                                        // if the group ID is
          if ( index >= 0 )                             // in the list 
            ds.setSelectFlag( i, true );
        }
        ds.notifyIObservers( IObserver.SELECTION_CHANGED );
      }
      else
        return new ErrorString("Can not set field " + S );     

      ds.addLog_entry( "Operation " + "SetField "+ S +" on " +ds_name +
              " to " + O.toString() );
      return "Field Set"; 
     }

     catch(Exception s)
     {
       return new ErrorString("Improper Field data type"); 
     }    
    
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SetField Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    SetField new_op = new SetField( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
