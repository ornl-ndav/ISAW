/*
 * File:  LoadOneDS.java  
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 *  $Log$
 *  Revision 1.2  2002/10/11 13:49:41  pfpeterson
 *  Now has the filename specified using a LoadFileString.
 *
 *  Revision 1.1  2002/10/10 21:12:46  dennis
 *  Initial form of operator to load selected IDs from one DataSet.
 *
 */

package DataSetTools.operator.Generic.Load;

import java.io.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import IPNS.Runfile.*;
import DataSetTools.operator.Parameter;
import DataSetTools.parameter.*;

/**
 * Operator to load specific IDs from a specific DataSet from a NeXus file 
 * or IPNS runfile 
 *
 * @see DataSetTools.operator.Operator
 */

public class LoadOneDS extends    GenericLoad 
                       implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this constructor
   * is used, meaningful values for the parameters should be set before 
   * calling getResult().
   */
   public LoadOneDS( )
   {
     super( "Load One Data Set" );
   }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for with the specified parameter values so 
   *  that the operation can be invoked immediately by calling getResult().
   *
   *  @param  file_name   The fully qualified runfile name
   *  @param  ds_num      The DataSet number that should be loaded
   *  @param  ids         A list of IDs for the Data blocks that should be 
   *                      loaded 
   *
   */
   public LoadOneDS( String   file_name, 
                     int      ds_num,
                     String   ids      )
   {
      this();

      IParameter parameter = getParameter(0);
      parameter.setValue( new LoadFileString(file_name) );

      parameter = getParameter(1);
      parameter.setValue( new Integer(ds_num) );

      parameter = getParameter(2);
      parameter.setValue( ids );
   } 

  /* -------------------------- setDefaultParameters ----------------------- */
  /**
   *  Set the parameters to default values.  
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter;

    parameter=new Parameter("Full File Name:", new LoadFileString("") );
    addParameter( parameter );

    parameter = new Parameter("DataSet number (>=0)", new Integer(0) );
    addParameter( parameter );

    parameter = new Parameter("Data IDs to load", new String("") );
    addParameter( parameter );
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor:
   *            in this case, OneDS
   *
   */
   public String getCommand()
   {
     return "OneDS";
   }


  /* ----------------------------- getResult ---------------------------- */
  /**
   * Load the requested Data blocks from the file.
   *
   * @return  Returns a DataSet containing the specified histogram from
   *          the runfile, if the runfile could opened and the specified
   *          histogram existed.
   */
   public Object getResult()
   {                               
     String  file_name  = getParameter(0).getValue().toString();
     int     ds_num     = ((Integer)(getParameter(1).getValue()) ).intValue();
     String  ids_string = getParameter(2).getValue().toString();

     int     ids[] = IntList.ToArray( ids_string ); 

     Retriever rr = null;

     String temp = file_name.toUpperCase ();

     if ( temp.endsWith( "RUN" ) )
       rr = new RunfileRetriever( file_name );
     else if ( temp.endsWith("NXS") || temp.endsWith("HDF") )
       rr = new NexusRetriever( file_name );

     if ( rr == null )
       return new ErrorString("Unsupported file type: " +
                              "must be .run, .nxs or .hdf");

     DataSet ds;
     if ( ids.length == 0 )
       ds = rr.getDataSet( ds_num );
     else
       ds = rr.getDataSet( ds_num, ids );

     if ( ds == null )
       return new ErrorString("requested DataSet not in " + file_name);

     return ds;
   }


   /* -------------------------------- main ------------------------------ */
   /* 
    * main program for test purposes only  
    */

   public static void main(String[] args)
   {
      LoadOneDS loader = new LoadOneDS( 
                                "/usr/local/ARGONNE_DATA/hrcs2444.run",
                                 1,
                                "2:10,20:30" );

      Object result = loader.getResult();
      if ( result instanceof DataSet )
      {
        DataSet ds = (DataSet)result;

        ViewManager viewmanager;
        viewmanager = new ViewManager( ds, IViewManager.IMAGE );
      }
      else
        System.out.println( result.toString() );
   } 
} 
