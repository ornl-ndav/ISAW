/*
 * @(#)GenericOperatorList.java   2000/07/19  Dennis Mikkelson
 *             
 *  Maintain list of generic operators and create instance of them as needed.  
 *
 *  $Log$
 *  Revision 1.9  2000/08/03 21:45:49  dennis
 *  Added Dongfeng's utility for quick printing.
 *
 *  Revision 1.8  2000/08/03 15:47:30  dennis
 *  Added $Log to enable recording of comments.
 *
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  This class keeps a list of generic operators that are not associated
  *  with any DataSet and includes methods to get the operator names and
  *  construct instances of the operators.
  *
  *  @see Operator
  */

public class GenericOperatorList implements Serializable
{

  static private final String names[] = { "SumFiles",
                                          "OneFile",
                                          "Mon",
                                          "OneHist",
                                          "FF",
                                          "PrintDS"   };

  /**
   *  Private constructor ... don't let anyone instantiate this class
   */
   private GenericOperatorList()
   {
   }


  /* ---------------------------- getNum_operators ------------------------- */
  /**
   * Get the number of operators in the list of operators. 
   *
   * @returns  The number of generic operators currently available in this
   *           list of operators.
   */
  static public int getNum_operators() 
  { 
    return names.length;
  }

  /* ---------------------------- getCommand ------------------------- */
  /**
   * Get the command name of an operator in the list of operators.    
   *
   * @param  index  The index specifing which operator name is needed from the 
   *                list of operators.
   *   
   * @returns  The command name of the specified operator.
   */
  static public String getCommand( int index )
  { 
    if ( index < 0 || index >= names.length )
      return null;

    return names[index];
  }

  /* ---------------------------- getOperator ------------------------------ */
  /**
   *  Get a reference to a new instance of an operator from the list of
   *  available operators.  If the specified operator is not in the list,
   *  this method returns null.
   *
   *  @param  index  The index specifing which operator is needed from the 
   *                 list of operators.
   *   
   *  @return a reference to a new instance of the type of operator stored
   *          in the specified position in the list.  If the index is invalid,
   *          return null.
   */
   static public Operator getOperator( int index )
   {
      if ( index < 0 || index >= names.length )
        return null;

      return getOperator( names[index] ); 
   }

  /* ---------------------------- getOperator ------------------------------ */
  /**
   * Get a reference to the operation in the list of available operators
   * with the specified op name.  If the named operator is not in the list, 
   * this method returns null.
   *
   * @param  op_name   The name of the requested operation
   *
   * @return a reference to a new instance of the specified type of operator.
   *         If there is no such operator, return null.
   */
  static public Operator getOperator( String op_name )
  {

    if ( op_name.equals( "SumFiles") )
      return new SumRunfiles();

    else if ( op_name.equals( "OneFile" ) )
      return new LoadOneRunfile();

    else if ( op_name.equals( "Mon" ) )
      return new LoadMonitorDS();

    else if ( op_name.equals( "OneHist" ) )
      return new LoadOneHistogramDS();

    else if ( op_name.equals( "FF" ) )
      return new SpectrometerFudgeFactor();

    else if ( op_name.equals( "PrintDS" ) )
      return new DataSetPrint();

     return null;
  }

}
