/*
 * @(#)SetField.java   00-07-12  Ruth Mikkelson
 *             
 * This operator sets a DataSet Attribute
 *
 * ---------------------------------------------------------------------------
 *  
 *
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  Allows the user to set attributes
  *
  *  @see DataSetOperator
  *  @see Operator
  */

public class SetField extends    DataSetOperator 
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
    super( "Set Field" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @parm   Fieldname   The Attribute to be set.
   *  @param  new_Value   The new value of the Attribute
   */

  public SetField    ( DataSet    ds,
                        AttributeNameString  Fieldname,
                        Object   new_Value )
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

    

    Parameter parameter = new Parameter( "Field?", new AttributeNameString("") );
    addParameter( parameter ); 
   
    
    parameter = new Parameter( " New Value?", new Object());
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
    { Attribute A;
     DataSet ds = getDataSet();
     String S = ((AttributeNameString)(getParameter(0).getValue())).toString();
     Object O = getParameter(1).getValue();
     try{
     if( S.equals("Title"))
       {ds.setTitle(O.toString());
       }
     else if( S.equals("X_label"))
       {ds.setX_label(O.toString());
       }
     else if( S.equals("X_units")) 
        ds.setX_units(O.toString());     
     else if( S.equals("PointedAtIndex"))
        ds.setPointedAtIndex(((Integer)O).intValue());
     else if( S.equals("SelectFlagOn"))
        ds.setSelectFlag((((Integer)O).intValue()), true);
     else if( S.equals("SelectFlagOff"))
        ds.setSelectFlag((((Integer)O).intValue()), false);
     else if( S.equals("Y_label"))
        ds.setY_label(O.toString());
     else if( S.equals("Y_units"))
        ds.setY_units(O.toString());
    
     else
       return new ErrorString("Improper Field name");     
     ds.addLog_entry( "Operation " + "SetField "+ S +" on " +ds +
             " to " + O);
     return "Field Set"; 
      }
    catch(Exception s)
      {return new ErrorString("Improper Field data type"); 
      }    
    
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SetField Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    SetField new_op    = new SetField( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
