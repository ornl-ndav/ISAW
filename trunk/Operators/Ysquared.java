/*
 * File:  Ysquared.java 
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.5  2003/02/03 18:52:38  dennis
 * Added getDocumentation() operator. (Joshua Olson)
 *
 * Revision 1.4  2002/11/27 23:29:54  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/02/22 20:45:07  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.2  2001/11/27 18:22:49  dennis
 * Fixed getCommand to return proper command name and added
 * operator title to constructor java docs.
 *
 */
package Operators;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import java.util.*;

/** 
 *    This operator provides a simple example of an operator that accesses 
 *  the data stored in a DataSet.  In order to demonstrate access to the 
 *  data, it just alters the current y values for the Data blocks to be the 
 *  squares of the y values originally there.<p>
 *
 *    In order to be used from Isaw, this operator must be compiled and the
 *  resulting class file must be placed in one of the directories that Isaw
 *  looks at for operators, such as the ../Operators subdirectory of the 
 *  Isaw home directory.  For details on what directories are searched, see
 *  the Operator-HOWTO file, or the Isaw user manual.<p>
 *
 *  NOTE: This only works for TabulatedData objects such as "
 *        "HistogramTable or FunctionTable Data objects."
 *
 *  NOTE: This operator can also be run as a separate program, since it
 *  has a main program for testing purposes.  The main program merely loads
 *  a DataSet, applies the operator and then shows both the original and
 *  new DataSet.
 */

public class Ysquared extends GenericSpecial
{
  private static final String TITLE = "Square all Y-values";

 /* ------------------------- DefaultConstructor -------------------------- */
 /** 
  *  Creates operator with title "Square all Y-values" and a  default list of
  *  parameters.
  */  
  public Ysquared()
  {
    super( TITLE );
  }


 /* ----------------------------- Constructor ----------------------------- */
 /** 
  *  Creates operator with title "Square all Y-values" and the specified list
  *  of parameters.  The getResult method must still be used to execute
  *  the operator.
  *  
  *  @param  p_ds   The DataSet to process.
  */
  public Ysquared( DataSet p_ds )
  {
    this();
    parameters = new Vector();
    addParameter( new Parameter("Data Set to Process", p_ds) );
  }


 /* ------------------------------ getCommand ----------------------------- */
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "Ysquared", the command used to invoke this operator in Scripts
  */
  public String getCommand()
  {
    return "Ysquared";
  }

 /* ------------------------ setDefaultParameters ------------------------- */
 /** 
  * Sets default values for the parameters.  The parameters set must match the 
  * data types of the parameters used in the constructor.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter( "Data Set to Process", new DataSet("","") ));
  }

 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */                                                                                 
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");                                                 
    s.append("@overview  This operator provides a simple example of an ");
    s.append("operator that accesses the data stored in a DataSet.  In ");
    s.append("order to demonstrate access to the data, it just alters the ");
    s.append("current y values for the Data blocks to be the squares of ");
    s.append("the y values originally there. \n \n In order to be used from ");
    s.append("Isaw, this operator must be compiled and the resulting class ");
    s.append("file must be placed in one of the directories that Isaw looks ");
    s.append("at for operators, such as the .../Operators subdirectory of ");
    s.append("the Isaw home directory.  For details on what directories ");
    s.append("are searched, see the Operator-HOWTO file, or the Isaw ");
    s.append("user manual. \n \n NOTE: This operator can also be run as a ");
    s.append("separate program, since it has a main program for testing ");
    s.append("purposes.  The main program merely loads a DataSet, applies ");
    s.append("the operator and then shows both the original and new DataSet.");
    s.append("@assumptions The given data set p_ds is not empty. \n");                                                                
    s.append("@algorithm Each entry in the given data set p_ds is a ");
    s.append("spectrum.  Each spectrum has channel(s), and each channel ");
    s.append("has a y value.  Every y value is squared.  \n\n This is ");
    s.append("recorded in the DataSet's log by adding the string 'Squared ");
    s.append("the Y-values' as an entry in the log.  A string is returned ");
    s.append("indicating success.");
    s.append("NOTE: This only works for TabulatedData objects such as ");
    s.append("HistogramTable or FunctionTable Data objects.");
    s.append("@param p_ds The DataSet to process");
    s.append("@return Returns the string 'Operator completed successfully'.");
    return s.toString();
  }

 /* ------------------------------ getResult ------------------------------- */
 /** 
  *  Replaces all y values of all Data blocks by the original value squared.
  *
  *  @return  This returns a string indicating that the DataSet was altered.
  */
  public Object getResult()
  {
    DataSet ds = (DataSet)(getParameter(0).getValue());

    Data   spectrum;                           // variables to hold one data
    float  y[];                                // block and its y values 

                                               // for each spectrum.....
    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      spectrum = ds.getData_entry( i );        // get REFERENCE to a spectrum
      y        = spectrum.getY_values();       // then get REFERENCE to array
                                               // of y_values
  
      for ( int j = 0; j < y.length; j++ )     // for each channel.....
        y[j] = y[j] * y[j];                    // change the y values in the
                                               // Data block's array.
    }
                                               // record the operation in the
                                               // DataSet's log and return
    ds.addLog_entry("Squared the Y-values");
    return new String("Operator completed successfully");
  }


 /* --------------------------------- clone -------------------------------- */
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    Operator op = new Ysquared();
    op.CopyParametersFrom( this );
    return op;
  }

 
 /* ------------------------------- main ----------------------------------- */
 /** 
  * Test program to verify that this will compile and run ok.  
  *
  */
  public static void main( String args[] )
  {
    System.out.println("Test of Ysquared starting...");

                                                 // make a new RunfileRetriever
                                                 // and load the first DataSet
                                                 // YOU MUST MODIFY THE RUN 
                                                 // NAME TO LOAD A FILE ON YOUR
                                                 // SYSTEM
    String    run_name = "/home/dennis/ARGONNE_DATA/hrcs2447.run";
    Retriever rr       = new RunfileRetriever( run_name );
    DataSet   ds       = rr.getDataSet(1);

    if ( ds == null )                            // check for successful load
    {
      System.out.println("ERROR: file not found");
      System.exit(1);
    }
                                                 // make a clone and pop up a
                                                 // view of the clone so that
                                                 // we can compare them after
                                                 // the operator changes the
                                                 // original.
    DataSet     new_ds = (DataSet)ds.clone();
    ViewManager vm1 = new ViewManager( new_ds, IViewManager.IMAGE );

                                                 // make and run the operator
                                                 // to alter the original ds
                                                 // and display it after it's 
    Operator op  = new Ysquared( ds );           // altered
    Object   obj = op.getResult();
    ViewManager vm2 = new ViewManager( ds, IViewManager.IMAGE );
                                                 // display any message string
                                                 // that might be returned
    System.out.println("Operator returned: " + obj );
    System.out.println("Test of Ysquared done.");
  }
}
