/*
 * @(#)LoadMonitorDS.java     0.1  2000/07/21  Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.1  2000/07/21 21:39:41  dennis
 *  Operators to load monitors separately
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
 * Operator to the Monitor DataSet from one IPNS runfile
 *
 * @see Operator
 */

public class LoadMonitorDS extends    Operator 
                           implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this constructor
   * is used, meaningful values for the parameters should be set before 
   * calling getResult().
   */
   public LoadMonitorDS( )
   {
     super( "Load Monitor DataSet" );
   }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for with the specified parameter values so 
   *  that the operation can be invoked immediately by calling getResult().
   *
   *  @param  file_name   The fully qualified runfile name
   *
   */
   public LoadMonitorDS( String   file_name )
   {
      super( "Load Monitor DataSet" );

      Parameter parameter = getParameter(0);
      parameter.setValue( file_name );
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
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   *
   */
   public String getCommand()
   {
     return "Mon";
   }


  /* ----------------------------- getResult ---------------------------- */
  /**
   * Returns the object that is the result of applying this operation.  This
   * should be called after setting the appropriate parameters.  
   *
   * @return  A DataSet containing the monitor data for the specified run
   *          is returned as a java Object, or an error string is returned
   *          if the file could not be opened.
   */
   public Object getResult()
   {
     RunfileRetriever rr;
                                          // get the parameters specifying the
                                          // runs         
     String    file_name   = (String)getParameter(0).getValue();

     rr = new RunfileRetriever( file_name );

     DataSet ds = rr.getFirstDataSet( RunfileRetriever.MONITOR_DATA_SET );
     if ( ds == null )
     {
       System.out.println("ERROR: no monitor DataSets in " + file_name );
       return new ErrorString("ERROR: no monitor DataSets in " + file_name);
     }

     return ds;
   }


   /* -------------------------------- main ------------------------------ */
   /* 
    * main program for test purposes only  
    */

   public static void main(String[] args)
   {
     LoadMonitorDS loader = new LoadMonitorDS( 
                                "/IPNShome/dennis/ARGONNE_DATA/hrcs2444.run" );

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
