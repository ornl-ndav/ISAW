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
 * Revision 1.1  2001/11/21 21:27:47  dennis
 * Example of user-supplied add-on operator.
 *
 *
 */
package Operators;

import DataSetTools.operator.*;
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
  *   Default constructor that is used when the parameters will be
  *   set later
  */  
  public Ysquared()
  {
    super( TITLE );
  }


 /* ----------------------------- Constructor ----------------------------- */
 /** 
  *  This form of the constructor specifies the parameters at construction 
  *  time.  The getResult method must be called to actually run the operator.
  *  
  *  @param  ds   The DataSet to process.
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
  * @return  "Eg1", the command used to invoke this operator in Scripts
  */
  public String getCommand()
  {
    return "Eg1";
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


 /* ------------------------------ getResult ------------------------------- */
 /** 
  *  Executes this operator using the current values of the parameters.
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
