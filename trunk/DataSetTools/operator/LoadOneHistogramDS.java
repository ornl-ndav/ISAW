/*
 * @(#)LoadOneHistogramDS.java     0.2  2000/07/21  Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.1  2000/07/21 21:40:27  dennis
 *  Operators to load one specified histogram
 *
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

public class LoadOneHistogramDS extends    Operator 
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
   * Returns the abbreviated command string for this operator.
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
