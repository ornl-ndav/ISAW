/*
 * @(#)SetDataAttribute.java   00-07-12  Ruth Mikkelson
 *             
 * This operator sets a Data Attribute on a particular Data block in a DataSet
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

public class SetDataAttribute extends    DataSetOperator 
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

  public SetDataAttribute( )
  {
    super( "Set Data Attribute" );
  }

  
/* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  index       The index of the data block whose attribute is to 
   *                      be set
   *  @parm   Attrib      The Attribute to be set.
   *  @param  new_Value   The new value of the Attribute
   */

  public SetDataAttribute  ( DataSet    ds,
                         Integer   index,  
                        AttributeNameString  Attrib,
                        Object   new_Value )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( index);

    parameter = getParameter( 1 );
    parameter.setValue( Attrib);

   
    parameter = getParameter(2);
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
     return "SetAttr";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Index?", new Integer(0));
    addParameter(parameter);

    parameter = new Parameter( "Attribute?", new AttributeNameString("") );
    addParameter( parameter ); 
   
    
    parameter = new Parameter( " New Value?", new Object());
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
    { Attribute A;
     DataSet ds = getDataSet();
     int index = ((Integer)getParameter(0).getValue()).intValue();
     String S = ((AttributeNameString)(getParameter(1).getValue())).toString();
     Object O = getParameter(2).getValue();
      
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
     
     Data D = ds.getData_entry( index);
     if( D == null) 
	return new ErrorString("Improper Index"); 
     D.setAttribute( A);
     ds.addLog_entry( "Operation " + "SetDataAttribute "+ S +" on " +ds
              +"["+index+"]"+  " to " + O);
     //ds.notifyIObservers( IObserver.ATTRIBUTE_CHANGED);
     return "Attribute Set";     
    
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SetDataAttribute Operator.  The list of 
   * parameters  and the reference to the DataSet to which it applies is 
   * copied.
   */
  public Object clone()
  {
    SetDataAttribute new_op    = new SetDataAttribute( );
                                                // copy the data set associated
                                                // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
