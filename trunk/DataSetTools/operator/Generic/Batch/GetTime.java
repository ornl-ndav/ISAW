/*
/bin: Permission denied.

 * File:  GetTime.java 
 *
 * Copyright (C) 2002, Peter Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.3  2003/02/03 18:15:22  dennis
 * Added getDocumentation() method and java docs on getResult().
 * (Shannon Hintzman).
 *
 * Revision 1.2  2002/11/27 23:20:52  pfpeterson
 * standardized header
 *
 *
 */
package DataSetTools.operator.Generic.Batch;

import java.util.*;


/** 
 * This operator returns an integer representing the time, in milliseconds,
 * since ISAW was started.
 */
public class GetTime extends GenericBatch
{
  private static final String     TITLE                 = "Time";

  /**
   *  Creates operator with title "Time" and a default list of
   *  parameters (no parameters).
   */  
  public GetTime()
  {
	super( TITLE );
  }

  /* ---------------------------- getDocumentation -------------------------- */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator returns an integer of the time, in ");
    Res.append("milliseconds, since ISAW was started.");
 
    Res.append("@algorithm Executes this operator using the values of the ");
    Res.append("current parameters to get the current time in milliseconds ");
    Res.append("since ISAW was started.");
    
    Res.append("@return Returns an integer of the time, in milliseconds, ");
    Res.append("since ISAW was started.");
    
    return Res.toString();
  }
  
  /* ---------------------------- getCommand ------------------------------- */
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return "GetTime", the command used to invoke this
   * operator in Scripts
   */
  public String getCommand(){
      return "GetTime";
  }

  /* ------------------------- setDefaultParmeters ------------------------- */
  /** 
   * Sets default values for the parameters. This must match the
   * data types of the parameters.
   */
  public void setDefaultParameters(){
      parameters = new Vector();
  }
    
  /* ---------------------------- getResult ------------------------------- */
  /** 
   *  Executes this operator using the values of the current
   *  parameters.
   *
   *  @return If successful, this operator gives back an Integer giving 
   *  the time, in milliseconds, since ISAW was started.
   */
  public Object getResult(){
      long time=System.currentTimeMillis();
      return new Integer((int)(time-DataSetTools.util.SharedData.start_time));
  }
   
  /* ---------------------------- clone ---------------------------------- */ 
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
      GetTime op = new GetTime();
      op.CopyParametersFrom( this );
      return op;
  }
  
  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    GetTime op = new GetTime();
    op.getResult();
    
   /* 
      In order for this little test program to work properly, op.getResult() 
      must first be called to cause the SharedData start_time variable to be 
      initialized.  Subsequent calls to op.getResult() will then find the
      time since the start_time variable was initialized.
    */    
    
    System.out.println("Start of timing loop");
    double x = 0;
    for ( int i = 0; i < 10000000; i++ )
      x = Math.cos(x);
    System.out.println("End of timing loop"); 
    
    System.out.println(op.getDocumentation() + "\n");
    System.out.println(op.getResult().toString());
  }
}
