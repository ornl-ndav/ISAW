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
 *  Revision 1.4  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.3  2001/04/26 19:10:04  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.2  2000/11/10 22:41:34  dennis
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
 *  Revision 1.1  2000/07/21 21:40:27  dennis
 *  Operators to load one specified histogram
 *
 */

package DataSetTools.operator;

import java.io.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import IPNS.Runfile.*;


/**
 * Operator to load a specific histogram from an IPNS runfile 
 *
 * @see Operator
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

      Parameter parameter = getParameter(0);
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


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, OneHist
   *
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
   * @return  Returns a DataSet containing the specified histogram from
   *          the runfile, if the runfile could opened and the specified
   *          histogram existed.
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
                                "/IPNShome/dennis/ARGONNE_DATA/hrcs2444.run",
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
        System.out.println( result.toString() );
   } 
} 
