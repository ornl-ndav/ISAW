/*
 * @(#)GetField.java   00-07-12  Ruth Mikkelson
 *             
 * This operator gets a DataSet Field value 
 *
 *  $Log$
 *  Revision 1.3  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize the operators.
 *  Existing operators were modified to be derived from one of the new abstract
 *  classes.  The abstract base class hierarchy is now:
 *
 *   Operator
 *
 *    -GenericOperator
 *       --GenericLoad
 *       --GenericBatch
 *
 *    -DataSetOperator
 *      --DS_EditList
 *      --DS_Math
 *         ---ScalarOp
 *         ---DataSetOp
 *         ---AnalyzeOp
 *      --DS_Attribute
 *      --DS_Conversion
 *         ---XAxisConversionOp
 *         ---YAxisConversionOp
 *         ---XYAxesConversionOp
 *      --DS_Special
 *
 *     To allow for automatic generation of hierarchial menus, each new operator
 *  should fall into one of these categories, or a new category should be
 *  constructed within this hierarchy for the new operator.
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
  *  @see  DS_Attribute
  */

public class GetField extends    DS_Attribute 
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
    super( "Get DataSet Field" );
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

  public GetField  ( DataSet ds, DSFieldString  Field )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 0 );
    parameter.setValue( Field );

    

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

    Parameter parameter = new Parameter( "Field?", new DSFieldString("Title") );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
    { 
     DataSet ds = getDataSet();
     String S = ((DSFieldString)(getParameter(0).getValue())).toString();
    
    if( S.equals( DSFieldString.TITLE ))
        return ds.getTitle();
       
     else if( S.equals( DSFieldString.X_LABEL ))
        return ds.getX_label();
       
     else if( S.equals( DSFieldString.X_UNITS )) 
        return ds.getX_units();     

     else if( S.equals( DSFieldString.Y_LABEL ))
       return  ds.getY_label();

     else if( S.equals( DSFieldString.Y_UNITS ))
        return ds.getY_units();

     else if ( S.equals(DSFieldString.MAX_SAMPLES) )
        return new Integer( ds.getMaxXSteps() );

     else if ( S.equals( DSFieldString.X_RANGE ) )
        return ds.getXRange();

     else if ( S.equals( DSFieldString.Y_RANGE ) )
        return ds.getYRange();

     else if( S.equals( DSFieldString.POINTED_AT_INDEX ))
        return new Integer( ds.getPointedAtIndex());

     else if( S.equals( DSFieldString.POINTED_AT_ID ))
     {
        int index = ds.getPointedAtIndex();
        if ( index < 0 )
          return new Integer( index );
        else
        {
          int id = ds.getData_entry(index).getGroup_ID();
          return new Integer( id );
        }
     }

     else if ( S.equals( DSFieldString.NUM_GROUPS ))
        return new Integer ( ds.getNum_entries() );

     else if ( S.equals( DSFieldString.GROUP_IDS  ) )
     {
       int num_data     = ds.getNum_entries();

       int ids[] = new int[ num_data ];
       for ( int i = 0; i < num_data; i++ )
         ids[i] = ds.getData_entry(i).getGroup_ID();

       String result = IntList.ToString( ids );
       return result;
     }

     else if ( S.equals( DSFieldString.NUM_SELECTED ))
       return new Integer( ds.getNumSelected() );
     
     else if ( S.equals( DSFieldString.SELECTED_GROUPS  ) )
     {
       int num_selected = ds.getNumSelected();
       int num_data     = ds.getNum_entries();

       int index = 0;
       int ids[] = new int[ num_selected ];
       for ( int i = 0; i < num_data; i++ )
         if ( ds.isSelected(i) )
         {
           ids[index] = i;
           index++;
         }
       String result = IntList.ToString( ids );
       return result;
     }

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
