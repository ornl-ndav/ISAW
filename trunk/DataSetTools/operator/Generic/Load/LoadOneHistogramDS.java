/*
 * File:  LoadOneHistogramDS.java  
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 *  $Log$
 *  Revision 1.5  2002/12/10 21:56:21  dennis
 *  Added getDocumentation() method. (Shannon Hintzman)
 *
 *  Revision 1.4  2002/11/27 23:21:16  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/09/19 16:05:33  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.2  2002/03/05 19:27:48  pfpeterson
 *  Updated @see references in javadocs.
 *
 *  Revision 1.1  2002/02/22 20:57:55  pfpeterson
 *  Operator reorganization.
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
 * Operator to load a specific histogram from an IPNS runfile 
 *
 * @see DataSetTools.operator.Operator
 */

public class LoadOneHistogramDS extends    GenericLoad 
                                implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this constructor
   * is used, meaningful values for the parameters should be set before 
   * calling getResult().
   */
   public LoadOneHistogramDS( )
   {
     super( "Load One Histogram" );
   }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for with the specified parameter values so 
   *  that the operation can be invoked immediately by calling getResult().
   *
   *  @param  file_name   The fully qualified runfile name
   *  @param  histogram   The histogram number that should be loaded
   *  @param  group_mask  A list of group IDs that should be omitted
   *
   */
   public LoadOneHistogramDS( String   file_name, 
                              int      histogram,
                              String   group_mask )
   {
      super( "Load One Histogram" );

      IParameter parameter = getParameter(0);
      parameter.setValue( file_name );

      parameter = getParameter(1);
      parameter.setValue( new Integer(histogram) );

      parameter = getParameter(2);
      parameter.setValue( group_mask );
   } 

  /* -------------------------- setDefaultParameters ----------------------- */
  /**
   *  Set the parameters to default values.  
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Full File Name:", "" );
    addParameter( parameter );

    parameter = new Parameter("Histogram number", new Integer(1) );
    addParameter( parameter );

    parameter = new Parameter("Group IDs to omit", new String("") );
    addParameter( parameter );
  }

  /*----------------------------getDocumentation-----------------------------*/
  
   public String getDocumentation()
   {
   	StringBuffer Res = new StringBuffer();
	
	Res.append("@overview This operator loads a specific histogram from ");
	Res.append("an IPNS runfile.");
	
	Res.append("@algorithm A file is read in.  If the specified ");
    	Res.append("histogram is found in the file and could be opened, then ");
	Res.append("any group IDs that should be omitted from it are, and ");
	Res.append("the DataSet that contains the specified histogram is ");
    	Res.append("returned, otherwise an ErrorString is returned.");
	
	Res.append("@param file_name The fully qualified runfile name");
	Res.append("@param histogram The histogram number that should be ");
	Res.append("loaded");
	Res.append("@param group_mask A list of group IDs that should be ");
	Res.append("omitted");
	
	Res.append("@return Returns a DataSet containing the specified ");
	Res.append("histogram from the runfile if the runfile could be ");
	Res.append("opened and the specified histogram existed, otherwise an ");
	Res.append("ErrorString is returned.");
	
	Res.append("@error Requested histogram is not in \"filename\".");
	
	return Res.toString();
   
   }
   
  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case,
   *            	OneHist
   */
   public String getCommand()
   {
     return "OneHist";
   }

  /* ----------------------------- getResult ---------------------------- */
  /**
   * Returns the object that is the result of applying this operation.  This
   * should be called after setting the appropriate parameters.
   *
   * @return  Returns a DataSet containing the specified histogram from the
   *          runfile if the runfile could be opened and the specified histogram
   *          existed, otherwise an ErrorString is returned.
   */
   public Object getResult()
   {
                                          // get the parameters specifying the
                                          // runs         
     String    file_name   = (String)getParameter(0).getValue();
     int       hist_num    = 
                          ((Integer)(getParameter(1).getValue()) ).intValue();
     String    group_mask  = (String)getParameter(2).getValue();

     int       masked_ids[] = IntList.ToArray( group_mask ); 

     RunfileRetriever rr;
     rr  = new RunfileRetriever( file_name );

     DataSet ds = null;
     int n_ds   = rr.numDataSets();
     for (int i = 0; i< n_ds; i++)
       if (  rr.getType(i)         == Retriever.HISTOGRAM_DATA_SET &&
             rr.getHistogramNum(i) == hist_num )
       {
         ds = rr.getDataSet(i);
                                         // remove masked detectors for the
                                         // histogram DataSets
         for ( int k = 0; k < masked_ids.length; k++ )
           ds.removeData_entry_with_id( masked_ids[k] );
       }

     if ( ds == null )
     {
       System.out.println("ERROR: requested histogram not in " + file_name );
       return new ErrorString("ERROR: requested histogram not in " +file_name);
     }

     return ds;
   }

   /* -------------------------------- main ------------------------------ */
   /* 
    * main program for test purposes only  
    */

   public static void main(String[] args)
   {
      String mask = "20:30,40:50";
      LoadOneHistogramDS loader = new LoadOneHistogramDS( 
                             "/home/groups/SCD_PROJECT/SampleRuns/hrcs2444.run",
                             1,
                             mask );

      Object result = loader.getResult();
      if ( result instanceof DataSet )
      {
        DataSet ds = (DataSet)result;

        ViewManager viewmanager;
        viewmanager = new ViewManager( ds, IViewManager.IMAGE );
      }
      else
      	System.out.println(result.toString() );
	
      System.out.println(loader.getDocumentation());
   } 
} 
