/*
 * @(#)GetField.java   00-07-12  Ruth Mikkelson
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

public class GetField extends    DataSetOperator 
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

  public GetField( )
  {
    super( "Get Field" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @parm   Field        The Field to be gotten.
   *
   */

  public GetField  ( DataSet    ds,
                     AttributeNameString  Field
			   )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( Field);

    

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "GetField";
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
   
   
    
    
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
    { Attribute A;
     DataSet ds = getDataSet();
     String S = ((AttributeNameString)(getParameter(0).getValue())).toString();
    
    if( S.equals("Title"))
       {return ds.getTitle();
       }
     else if( S.equals("X_label"))
       {return ds.getX_label();
       }
     else if( S.equals("X_units")) 
        return ds.getX_units();     
     else if( S.equals( "PointedAtIndex" ))
        return new Integer( ds.getPointedAtIndex());
    
    
     else if( S.equals( "Y_label" ))
       return  ds.getY_label();
     else if( S.equals( "Y_units" ))
        return ds.getY_units();
     else if ( S.equals( "MaxGroupID" ))
        return new Integer ( ds.getMaxGroupID());
     else if ( S.equals("MaxXSteps"))
        return new Integer( ds.getMaxXSteps() );
     else if ( S.equals("MostRecentlySelectedIndex"))
        return new Integer( ds.getMostRecentlySelectedIndex());
     else if ( S.equals( "NumSelected" ))
       return new Integer( ds.getNumSelected());
     
     else if ( S.equals( "XRange" ) )
        return ds.getXRange();
     else if ( S.equals( "YRange" ) )
        return ds.getYRange(); 
     else 
        return new ErrorString( "No such Field");   
     
    
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current GetField Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    
   GetField new_op    = new GetField( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
