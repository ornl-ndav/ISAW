/*
 * @(#)SetDSDataAttribute.java   00-07-17  Dennis Mikkelson
 *                                 ( modified from SetDataAttribute.java)
 *             
 * This operator sets a Data Attribute on ALL Data blocks of a DataSet
 *
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  Sets an attribute value on all Data blocks in a DataSet 
  *
  *  @see DataSetOperator
  *  @see Operator
  */

public class SetDSDataAttributes extends    DataSetOperator 
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

  public SetDSDataAttributes( )
  {
    super( "Set Data Attribute on ALL Data blocks" );
  }

  
/* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @parm   Attrib      The Attribute to be set.
   *  @param  new_Value   The new value of the Attribute
   */

  public SetDSDataAttributes  ( DataSet              ds,
                                AttributeNameString  Attrib,
                                Object               new_Value )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( Attrib);
   
    parameter = getParameter(1);
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
     return "SetAttrs";
   }


 /* -------------------------- setDefaultParameters ------------------------ */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = 
              new Parameter( "Attribute?", new AttributeNameString("") );
    addParameter( parameter ); 
    
    parameter = new Parameter( " New Value?", new Object() );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
    { Attribute A;
      DataSet ds = getDataSet();
      String S = ((AttributeNameString)(getParameter(0).getValue())).toString();
      Object O = getParameter(1).getValue();
      
      if( O instanceof Integer) 
 	 A = new IntAttribute(S, ((Integer)O).intValue());
      else if( O instanceof Float)
         A = new FloatAttribute( S , ((Float)O).floatValue());
      else if( O instanceof String)
         A = new StringAttribute(S , (String) O);
      else if( O instanceof AttributeNameString)
         A = new StringAttribute( S , ((AttributeNameString) O).toString());
      else
 	 return new ErrorString(" new Value improper Data Type");

      for ( int i = 0; i < ds.getNum_entries(); i++ )
      {
        Data d = ds.getData_entry( i );
        if( d != null) 
         d.setAttribute( A);
      }
      if (ds.getNum_entries() > 0 )
      {
        ds.addLog_entry( "Operation " + "SetDataAttribute "+ S +
                         " on ALL Data blocks to " + O );

        return "Attribute Set";
      }
      else
        return "NO DATA BLOCKS";     
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SetDataAttribute Operator.  The list of 
   * parameters  and the reference to the DataSet to which it applies is 
   * copied.
   */
  public Object clone()
  {
    SetDSDataAttributes new_op    = new SetDSDataAttributes( );
                                                // copy the data set associated
                                                // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
