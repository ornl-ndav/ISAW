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
 *  Revision 1.3  2002/09/19 16:05:34  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.2  2002/03/05 19:27:49  pfpeterson
 *  Updated @see references in javadocs.
 *
 *  Revision 1.1  2002/02/22 20:57:56  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.6  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.5  2001/04/26 19:10:06  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.4  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize the operators.
 *  Existing operators were modified to be derived from one of the new abstract
 *  classes.  The abstract base class hierarchy is now:
 *
 *   Operator
 *
 *    -GenericOperator
 *       --GenericLoad
 *       --GenericBatch
 *
 *    -DataSetOperator
 *      --DS_EditList
 *      --DS_Math
 *         ---ScalarOp
 *         ---DataSetOp
 *         ---AnalyzeOp
 *      --DS_Attribute
 *      --DS_Conversion
 *         ---XAxisConversionOp
 *         ---YAxisConversionOp
 *         ---XYAxesConversionOp
 *      --DS_Special
 *
 *     To allow for automatic generation of hierarchial menus, each new operator
 *  should fall into one of these categories, or a new category should be
 *  constructed within this hierarchy for the new operator.
 *
 *  Revision 1.3  2000/07/21 21:38:15  dennis
 *  Removed one unused variable and fixed documentation
 *
 *  Revision 1.2  2000/07/21 20:54:38  dennis
 *  Now uses mask to omit specified groups
 *
 *  Revision 1.1  2000/07/21 20:38:56  dennis
 *  Generic operator to load all DataSets from one runfile
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


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, OneFile
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
   *          runfile could opened.
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
                                "/IPNShome/dennis/ARGONNE_DATA/hrcs2444.run",
                                mask );


      Object result = loader.getResult();
      if ( result instanceof DataSet[] )
      {
        DataSet datasets[] = (DataSet[])result;

        ViewManager viewmanager;
        for ( int i = 0; i < datasets.length; i++ )
          viewmanager = new ViewManager( datasets[i], IViewManager.IMAGE );
      }
      else
        System.out.println( result.toString() );
   } 
} 
