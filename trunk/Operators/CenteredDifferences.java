/*
 * File:  CenteredDifferences.java 
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
 * Revision 1.5  2002/11/27 23:29:54  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/03/13 16:26:22  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.3  2002/02/22 20:45:00  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.2  2001/11/27 18:16:41  dennis
 * Improved documentation.
 * Use average of x[i+1] and x[i-1] for the x position of the
 * centered difference value.
 *
 */
package Operators;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import java.util.*;

/** 
 *  This operator approximates the derivative of all Data blocks in the 
 *  DataSet using centered differences.  The operator returns a new DataSet
 *  containing the derived functions.
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
public class CenteredDifferences extends GenericSpecial
{
  private static final String TITLE = "Centered Differences";

 /* ------------------------ Default constructor ------------------------- */ 
 /** 
  *  Creates operator with title "Centered Differences" and a  default list of
  *  parameters.
  */  
  public CenteredDifferences()
  {
    super( TITLE );
  }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  Creates operator with title "Centered Differences" and the specified list
  *  of parameters.  The getResult method must still be used to execute
  *  the operator.
  *
  *  @param  ds         DataSet to differentiate 
  */
  public CenteredDifferences( DataSet ds )
  {
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("DataSet parameter", ds) );
  }

 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "CenteredDifferences", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "CenteredDifferences";
  }

 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters.  This must match the data types 
  * of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("DataSet parameter", DataSet.EMPTY_DATA_SET ) );
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  *  Approximates the derivative of the Data blocks using a centered
  *  differences approximation.   
  *
  *  @return  If successful, this returns a new DataSet whose Data blocks
  *           contain the derivatives of the original Data blocks.  If there
  *           is an error, and ErrorString is returned. 
  */
  public Object getResult()
  {
    DataSet ds = (DataSet)(getParameter(0).getValue());
 
    if ( ds == null )
      return new ErrorString("DataSet is null in CenteredDifferences");

    String title   = ds.getTitle();       // get the original title, units, etc
    String x_units = ds.getX_units();
    String y_units = ds.getY_units();
    String x_label = ds.getX_label();
    String y_label = ds.getY_label();
                                          // make DataSet with modified title,
                                          // y units and label 
    DataSetFactory ds_factory = new DataSetFactory("Derivative of " + title, 
                                                    x_units, 
                                                    x_label, 
                                                    y_units + "/" + x_units, 
                                                   "Derivative of " + y_label );
    DataSet new_ds = ds_factory.getDataSet();
                                          // copy and update the log  
                                          // copy the list of attributes
    new_ds.copyOp_log(ds); 
    new_ds.addLog_entry("Approximate derivative using CenteredDifferences");
    new_ds.setAttributeList( ds.getAttributeList() );
 
                                          // for each Data block, make a new
                                          // Data block with the derivative
                                          // values
    for ( int j = 0; j < ds.getNum_entries(); j++ ) 
    {
      Data d = ds.getData_entry( j );
      float x[] = d.getX_scale().getXs();   
      float y[] = d.getY_values();
                                          // make new arrays holding table of 
                                          // centered differences and x values
      float new_x[] = new float[y.length - 2];
      float new_y[] = new float[y.length - 2];
      for ( int i = 1; i < y.length-1; i++ )
      {
        new_y[i-1] = (y[i+1] - y[i-1]) / (x[i+1] - x[i-1]);
        new_x[i-1] = (x[i+1] + x[i-1]) / 2;
      }
                                          // make a new Data block with the new
                                          // x and y values and same group ID
      XScale x_scale = new VariableXScale( new_x );
      Data new_d = Data.getInstance( x_scale, new_y, d.getGroup_ID() );

                                          // copy the Data attributes and add
                                          // the Data block to the new DataSet
      new_d.setAttributeList( d.getAttributeList() );
      new_ds.addData_entry( new_d );
    }

    return new_ds; 
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    Operator op = new CenteredDifferences();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main --------------------------------- */ 
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
     System.out.println("Test of CenteredDifferences starting...");
                                                               // load a DataSet
     String filename = "/usr/local/ARGONNE_DATA/hrcs2447.run";
     RunfileRetriever rr = new RunfileRetriever( filename );
     DataSet ds = rr.getDataSet(1);
                                                               // make operator
                                                               // and call it
     CenteredDifferences op = new CenteredDifferences( ds );
     Object obj = op.getResult();
     if ( obj instanceof DataSet )                   // we got a DataSet back
     {                                               // so show it and original
       DataSet new_ds = (DataSet)obj;
       ViewManager vm1 = new ViewManager( ds,     IViewManager.IMAGE );
       ViewManager vm2 = new ViewManager( new_ds, IViewManager.IMAGE );
     }
     else 
       System.out.println( "Operator returned " + obj );

     System.out.println("Test of CenteredDifferences done.");
  }
}
