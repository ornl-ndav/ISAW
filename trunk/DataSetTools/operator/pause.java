/*
 * @(#)print.java   0.1  00/08/02   Dongfeng Chen 
 *                                                    Dennis Mikkelson
 *
 * 
 *   
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;

/**
 * This operator converts Print data information.
 * 
 */

public class pause extends  Operator 
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

  public pause( )
  {
    super( "pause " );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   */

  public pause(   int      ms        )
  {
    this();

    Parameter parameter = getParameter(0);
    parameter.setValue( new Integer( ms ) );
    
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "Pause";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
     parameters = new Vector();  // must do this to create empty list of 
                                 // parameters

     Parameter parameter= new Parameter( " Pause for second: ", new Integer( 1 ) );
     addParameter( parameter );
     
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
    int   ms  =( (Integer)(getParameter(0).getValue()) ).intValue() ;
     System.out.print("Pause for "+(ms) +" milli-second! Please wait...\n ");

    do_pause(ms);

    return "Pause for "+ms+" milli-seconds";
  }  


public static void do_pause(int time)
{ 
 // System.out.print("Pause for "+time +" second! ");
  try{Thread.sleep(time);}catch(Exception e){}
}
public static void main(String[] arg)
{
	Operator ps = new pause(3);
      ps.getResult();
}

}
