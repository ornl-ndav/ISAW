/*
 * File:  LoadASCII.java 
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
 * Revision 1.3  2002/02/22 20:45:04  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.2  2001/11/27 18:19:17  dennis
 * Added operator title to constructor java docs.
 *
 * Revision 1.1  2001/11/21 21:27:39  dennis
 * Example of user-supplied add-on operator.
 *
 *
 */
package Operators;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Load.*;
import DataSetTools.util.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import java.util.*;

/** 
 *    This operator provides an example of an operator that reads data for
 *  one histogram from an ASCII text file and stores the data in a DataSet.  
 *  The data format that is read is basically just x, y pairs in columns 
 *  with some preliminary information lines.
 *
 *    Specifically, the file format is as follows:
 *
 *  <p> 1. Five lines of text listing a title, units and labels in the 
 *         order shown.  Note:  In order for the data set created by this 
 *          operator to be merged with another data set, the units for the x 
 *          and y axes MUST BE THE SAME as for the other DataSet.
 *  <p>    Data Set Title
 *  <p>    Units for the x-axis
 *  <p>    Label for the x-axis
 *  <p>    Units for the y-axis
 *  <p>    Label for the y-axis
 *  <p> 2. One line containing the number of bins in this histogram
 *  <p> 3. The list of x y values.  There must be one more x value than
 *         y value.  To load data for a "tabulated function" instead of 
 *         for a histogram, change the size of the array of x values to be
 *         equal to the size of the array of y values, and remove the line
 *         that reads in the last bin boundary.
 *
 *  <p>  In order to be used from Isaw, this operator must be compiled and the
 *  resulting class file must be placed in one of the directories that Isaw
 *  looks at for operators, such as the ../Operators subdirectory of the 
 *  Isaw home directory.  For details on what directories are searched, see
 *  the Operator-HOWTO file, or the Isaw user manual.
 *
 *  <p>
 *  NOTE: This operator can also be run as a separate program, since it
 *  has a main program for testing purposes.  The main program merely uses 
 *  the operator to load a simple test file and pops up a view of the 
 *  data.
 */

public class LoadASCII extends GenericLoad
{
  private static final String TITLE = "Load ASCII file";

 /* ------------------------- DefaultConstructor -------------------------- */
 /** 
  *  Creates operator with title "Load ASCII file" and a  default list of
  *  parameters.
  */  
  public LoadASCII()
  {
    super( TITLE );
  }

 /* ----------------------------- Constructor ----------------------------- */
 /** 
  *  Creates operator with title "Load ASCII file" and the specified list
  *  of parameters.  The getResult method must still be used to execute
  *  the operator.
  *  
  *  @param  file_name   The fully qualified ASCII file name.
  */
  public LoadASCII( String file_name )
  {
    this();
    parameters = new Vector();
    addParameter( new Parameter("Filename", file_name) );
  }

 /* ------------------------------ getCommand ----------------------------- */
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "LoadASCII", the command used to invoke this operator in Scripts
  */
  public String getCommand()
  {
    return "LoadASCII";
  }

 /* ------------------------ setDefaultParameters ------------------------- */
 /** 
  * Sets default values for the parameters.  The parameters set must match the 
  * data types of the parameters used in the constructor.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("Filename", "C:/") );
  }

 /* ------------------------------ getResult ------------------------------- */
 /** 
  *  Executes this operator using the current values of the parameters.
  *
  *  @return  If successful, this returns a new DataSet with the histogram
  *           that was read from the data file.
  */
  public Object getResult()
  {
    DataSet ds = null;                      
    String  file_name = (String)(getParameter(0).getValue());

    try
    {
      TextFileReader f = new TextFileReader( file_name );

      String title   = f.read_line();
      String x_units = f.read_line();
      String x_label = f.read_line();
      String y_units = f.read_line();
      String y_label = f.read_line();

      int n_bins = f.read_int();
      float y[] = new float[ n_bins ];
      float x[] = new float[ n_bins+1 ]; // histogram, so one extra bin boundary
      for ( int i = 0; i < n_bins; i++ )
      {
        x[i] = f.read_float();
        y[i] = f.read_float();
      }
      x[n_bins] = f.read_float();        // read the last bin boundary value

                                         // Using a DataSetFactory to build the
                                         // DataSet will give the DataSet a
                                         // set of operators.
      DataSetFactory ds_factory = 
            new DataSetFactory( "Sample", x_units, x_label, y_units, y_label );
      ds = ds_factory.getDataSet();
      
      Data    d  = new Data( new VariableXScale(x), y, 1 );
      ds.addData_entry( d );
    }
    catch ( Exception E )
    {
      return new ErrorString( E.toString() );
    } 
                                           // The file was loaded ok, so now
                                           // add a log entry and return the
                                           // new DataSet.
    ds.addLog_entry("Loaded File " + file_name );
    return ds;
  }

 /* --------------------------------- clone -------------------------------- */
 /** 
  *  Creates a clone of this operator.  ( Operators need a clone method, so 
  *  that Isaw can make copies of them when needed. )
  */
  public Object clone()
  { 
    Operator op = new LoadASCII();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main ----------------------------------- */
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
    System.out.println("Test of LoadASCII starting...");

                                                 // make and run the operator
                                                 // to load the data
    Operator op  = new LoadASCII("LoadASCII.dat");
    Object   obj = op.getResult();
                                                 // display any message string
                                                 // that might be returned
    System.out.println("Operator returned: " + obj );

                                                 // if the operator produced a
                                                 // a DataSet, pop up a viewer
    if ( obj instanceof DataSet )
    {
      ViewManager vm = new ViewManager( (DataSet)obj, IViewManager.IMAGE );
    }
    
    System.out.println("Test of LoadASCII done.");
  }
}
