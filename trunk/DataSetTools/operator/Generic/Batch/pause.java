/*
 * File:  pause.java 
 *
 * Copyright (C) 1999, Dongfeng Chen,
 *                     Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2002/09/19 16:05:19  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.1  2002/02/22 20:57:21  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.5  2001/06/01 21:18:00  rmikk
 * Improved documentation for getCommand() method
 *
 * Revision 1.4  2001/04/26 19:12:15  dennis
 * Added copyright and GPL info at the start of the file.
 * 
 *   
 */

package DataSetTools.operator.Generic.Batch;

import  java.io.*;
import  java.util.*;
import  DataSetTools.operator.Operator;
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.Generic.Batch.GenericBatch;
import  DataSetTools.parameter.*;

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
    IParameter parameter = getParameter(0);
    parameter.setValue( new Integer( ms ) );
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, Pause
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
