/*
 * @(#)pause.java   1.0  2000/10/19   Dongfeng Chen 
 *                                    Dennis Mikkelson
 *
 * $LOG$ 
 *   
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.*;

/**
 * This operator pauses the program.
 * 
 */

public class pause extends  GenericBatch 
                            implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct a default pause operator to pause one second.
   */

  public pause( )
  {
    super( "pause " );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct a pause operator to pause for a specified number of 
   *  milliseconds.
   *
   *  @param  ms   The number of milliseconds to pause. 
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
  *  Set the pause to a default value of one second.
  */
  public void setDefaultParameters()
  {
     parameters = new Vector();  // must do this to create empty list of 
                                 // parameters

     Parameter parameter= new Parameter("milliseconds to pause ", 
                                         new Integer(1000) );
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
//  System.out.print("Pause for "+time +" millisecond! ");
  try
  { 
    Thread.sleep(time); 
  }
  catch(Exception e)
  { 
    System.out.println("Exception in do_pause from pause operator"); 
  }
}

public static void main(String[] arg)
{
  Operator ps = new pause();
  ps.getResult();

  ps = new pause(3000);
  ps.getResult();
}

}
