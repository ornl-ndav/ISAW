/*
 * File:  ExitDialog.java 
 *
 * Copyright (C) 2002, Rut Mikkelson
 *                    
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.2  2002/11/16 14:32:27  rmikk
 * Improved Documentation
 *
 * Revision 1.1  2002/09/23 14:08:56  rmikk
 * Initial checkin
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
 * This operator is used by scripts to make the JParametersDialog box to exit if this
 * is the last operator executed in a script or is the argument of the script Return statement.
 * This is equivalent to pressing the Exit button in the JParametersDialog box.
 */

public class ExitDialog extends  GenericBatch 
                            implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct a default ExitDialog operator.
   */

  public ExitDialog( )
  {
    super( "Exit Dialogue " );
  }



 


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, ExitDialog
   */
   public String getCommand()
   {
     return "ExitDialog";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  ExitDialog has no parameters. Initialize the parameter variable.
  */
  public void setDefaultParameters()
  {
     parameters = new Vector();  // must do this to create empty list of 
                                 // parameters

  }


  /* ---------------------------- getResult ------------------------------- */
  /** Returns an instance of an ExitClass.
  * NOTE: This operator is used only by scripts. Java operators can just return
  * a new ExitClass()
  */
  public Object getResult()
  {
    return new ExitClass();
  }  

 public Object clone()
  {
    return (Object)( new ExitDialog());
  }



}
