/*
 * File:  LoadOneRunfile.java 
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
 *  Revision 1.8  2004/03/15 19:33:52  dennis
 *  Removed unused imports after factoring out view components,
 *  math and utilities.
 *
 *  Revision 1.7  2004/03/15 03:28:33  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.6  2004/01/24 19:48:52  bouzekc
 *  Removed unused imports.  Removed unused variables in main().
 *
 *  Revision 1.5  2002/12/10 21:56:21  dennis
 *  Added getDocumentation() method. (Shannon Hintzman)
 *
 *  Revision 1.4  2002/11/27 23:21:16  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/09/19 16:05:34  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.2  2002/03/05 19:27:49  pfpeterson
 *  Updated @see references in javadocs.
 *
 *  Revision 1.1  2002/02/22 20:57:56  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.Generic.Load;

import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import java.io.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.operator.Parameter;
import DataSetTools.parameter.*;

/**
 * Operator to load all data sets from one IPNS runfile
 *
 * @see DataSetTools.operator.Operator
 */

public class LoadOneRunfile extends    GenericLoad 
                            implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this constructor
   * is used, meaningful values for the parameters should be set before 
   * calling getResult().
   */
   public LoadOneRunfile( )
   {
     super( "Load One Runfile" );
   }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for with the specified parameter values so 
   *  that the operation can be invoked immediately by calling getResult().
   *
   *  @param  file_name   The fully qualified runfile name
   *  @param  group_mask  A list of group IDs that should be omitted
   *
   */
   public LoadOneRunfile( String   file_name, 
                          String   group_mask )
   {
      super( "Load One Runfile" );

      IParameter parameter = getParameter(0);
      parameter.setValue( file_name );

      parameter = getParameter(1);
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

    parameter = new Parameter("Group IDs to omit", new String("") );
    addParameter( parameter );
  }

  /*----------------------------getDocumentation-----------------------------*/
  
   public String getDocumentation()
   {
   	StringBuffer Res = new StringBuffer();
	
	Res.append("@overview This operator loads all data sets from one IPNS");
	Res.append("runfile.");
	
	Res.append("@algorithm A file is read in.  If there are no DataSets ");
    	Res.append("found in the file, then an ErrorString is returned.  ");
	Res.append("Otherwise an array is created, the DataSets are read in ");
	Res.append("from the file, omitted of any group IDs that should be, ");
	Res.append("and stored in the array.");
	
	Res.append("@param file_name The fully qualified runfile name");
	Res.append("@param group_mask A list of group IDs that should be ");
	Res.append("omitted");
	
	Res.append("@return Returns an array with all DataSets in the ");
        Res.append("runfile, if the runfile could be opened.");
	
	Res.append("@error No DataSets in \"filename\".");
	
	return Res.toString();
   
   }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case,
   *           	OneFile
   *
   */
   public String getCommand()
   {
     return "OneFile";
   }


  /* ----------------------------- getResult ---------------------------- */
  /**
   * Returns the object that is the result of applying this operation.  This
   * should be called after setting the appropriate parameters.
   *
   * @return  Returns an array with all DataSets in the runfile, if the 
   *          runfile could be opened.
   */
   public Object getResult()
   {
                                          // get the parameters specifying the
                                          // runs         
     String    file_name   = (String)getParameter(0).getValue();
     String    group_mask  = (String)getParameter(1).getValue();

     int       masked_ids[] = IntList.ToArray( group_mask ); 

     RunfileRetriever rr;
     rr = new RunfileRetriever( file_name );
     int n_ds      = rr.numDataSets();
     if ( n_ds <= 0 )
     {
       System.out.println("ERROR: no DataSets in " + file_name );
       return new ErrorString("ERROR: no DataSets in " + file_name);
     }

     DataSet[] dss = new DataSet[ n_ds ];
     for (int i = 0; i< n_ds; i++)
     {
       dss[i] = rr.getDataSet(i);
                                         // remove masked detectors for the
                                         // histogram DataSets
       if ( rr.getType( i ) == Retriever.HISTOGRAM_DATA_SET )
         for ( int k = 0; k < masked_ids.length; k++ )
           dss[i].removeData_entry_with_id( masked_ids[k] );
     }

     return dss;
   }


   /* -------------------------------- main ------------------------------ */
   /* 
    * main program for test purposes only  
    */

   public static void main(String[] args)
   {
      String mask = "20:30,40:50";
      LoadOneRunfile loader = new LoadOneRunfile( 
                             "/home/groups/SCD_PROJECT/SampleRuns/hrcs2444.run",
                             mask );


      Object result = loader.getResult();
      if ( result instanceof DataSet[] )
      {
        DataSet datasets[] = (DataSet[])result;

        for ( int i = 0; i < datasets.length; i++ )
          new ViewManager( datasets[i], IViewManager.IMAGE );
      }
      else
        System.out.println( result.toString() );
	
      System.out.println(loader.getDocumentation());
   } 
} 
