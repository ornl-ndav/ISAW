/*
 * File:  GetDate.java 
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
 */
package DataSetTools.operator.Generic.Batch;

import java.util.*;


/** 
 * This operator returns a string representing the current date and
 * time.
 */
 
public class GetDate extends GenericBatch
{
  private static final String     TITLE                 = "Date";
  /**
   *  Creates operator with title "Date" and a default list of
   *  parameters (no parameters).
   */  
  public GetDate(){/*DataSetTools/operator/Generic/Batch*/

	super( TITLE );
  }

  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return "GetDate", the command used to invoke this
   * operator in Scripts
   */
  /* ---------------------------- getDocumentation -------------------------- */
 
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator returns a string representing the ");
    Res.append("current date and time, which will be printed in the ");
    Res.append("StatusPane.");
 
    Res.append("@algorithm Executes this operator using the values of the ");
    Res.append("current parameters to get the current date and time to be ");
    Res.append("displayed in the StatusPane.");
    
    Res.append("@return Returns a string of the current date and time.");
    
    return Res.toString();
  }
  
  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case GetDate
   */    
  public String getCommand()
  {
      return "GetDate";
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
   *  parameters. This will print the current date and time in the
   *  StatusPane as well as return the string to the caller.
   *
   *  @return If successful, this operator gives back DataSetTools/operator/
   *          Generic/Batch a string of the current date and time.
   */
  public Object getResult(){
      Date date=new Date(System.currentTimeMillis());
      DataSetTools.util.SharedData.addmsg(date.toString());
      return date.toString();
  }
    
  /* ---------------------------- clone ---------------------------------- */
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
      GetDate op = new GetDate();
      op.CopyParametersFrom( this );
      return op;
  }
    
  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    GetDate op = new GetDate();
     
    System.out.println(op.getDocumentation() + "\n");
    System.out.println(op.getResult());
  }
}

