/*
 * File:  SetField.java 
 *             
 * Copyright (C) 2000, Ruth Mikkelson,
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
 * Revision 1.5  2004/01/22 02:39:55  bouzekc
 * Removed/commented out unused imports/variables.
 *
 * Revision 1.4  2002/11/27 23:16:41  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/11/21 22:40:47  dennis
 * Added getDocumentation() method, documentation on getResult() and
 * simple main program to test getDocumentation(). (Chris Bouzek)
 *
 * Revision 1.2  2002/09/19 16:00:10  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.1  2002/02/22 21:00:12  pfpeterson
 * Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

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
   *  @param   Fieldname   The Field to be set.
   *  @param  new_Value   The new value of the Attribute
   */

  public SetField( DataSet                ds,
                   DSSettableFieldString  Fieldname,
                   Object                 new_Value )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( Fieldname);

    parameter = getParameter( 1 );
    parameter.setValue(new_Value );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, SetField
   */
   public String getCommand()
   {
     return "SetField";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
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

 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator sets a particular field of a DataSet ");
    s.append("to a user specified value.");
    s.append("@assumptions The field is a valid one for the current ");
    s.append("DataSet, and the new value for the attribute is valid.");
    s.append("@algorithm Determines what field to change based on ");
    s.append("the user specifed field parameter.  Changes the field ");
    s.append("value(s) to the user specifed value.");
    s.append("@param The DataSet to which the operation is applied.");
    s.append("@param The Field to be set.");
    s.append("@param The new value of the Attribute.");
    s.append("@return A String telling the user that the field is set ");
    s.append("if the operation was successful.  Note that the DataSet is");
    s.append("modifed, and appropriate messages are added to the log file.");
    s.append("@error Returns an error if the user specified field is ");
    s.append("not found.");
    s.append("@error Returns an error if the field data type does not ");
    s.append("match the one specified by the user.");
    s.append("@error Returns an error if the user specified field ");
    s.append("contains a null value.");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  Determines what field to change based on the user specifed field 
   *  parameter.  Changes the field value(s) to the user specifed value.
   *  @return A String notifying the user that the field has been set.
   */

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

        ds.clearSelections();
        if ( list.length > 0 ) 
        for ( int i = 0; i < list.length ; i++ )
          if( (list[i] >=0) &&( list[i] < ds.getNum_entries() ) )
                 ds.setSelectFlag( list[i], true );

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

    /* --------------------------------- main() ------------------------ */
    /**  
     *  Main method for testing purposes.
     */
    public static void main(String args[])
    {
	SetField sf = new SetField();
	System.out.println("The documentation for this operator is: ");
	System.out.println(sf.getDocumentation());
    }	
}
