/*
 * @(#)GetDataAttribute.java   00-07-12  Ruth Mikkelson
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

public class GetDataAttribute extends    DataSetOperator 
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

  public GetDataAttribute( )
  {
    super( "Get Data Attribute" );
  }

  
/* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  index       The index of the data block whose attribute is to be set
   *  @parm   Attrib      The Attribute to be set.
   * 
   */

  public GetDataAttribute  ( DataSet               ds,
                             Integer               index,  
                              AttributeNameString  Attrib
                           )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter ;

   parameter = getParameter( 0 );
    parameter.setValue( index);

    parameter= getParameter( 1 );
    parameter.setValue( Attrib);   

    
    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "GetDataAttr";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters
    Parameter parameter;
    
    parameter = new Parameter( "Data block index?", new Integer( 0 ));
    addParameter( parameter);

    parameter = new Parameter( "Attribute?", new AttributeNameString("") );
    addParameter( parameter );
   
   
    
    
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
    { Attribute A;
     DataSet ds = getDataSet();
     int index = ((Integer) (getParameter(0).getValue())).intValue();
     String S = ((AttributeNameString)(getParameter(1).getValue())).toString();
     Data D = ds.getData_entry(index);
     if( D == null) return new ErrorString(" Improper index ");
     
     Object O = D.getAttributeValue( S );
     if ( O == null)
	 return new ErrorString(" Attribute "+ S + " is not in the List" );
     return O;
     
    
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current GetDataAttribute Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    
   GetDataAttribute new_op    = new GetDataAttribute( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
