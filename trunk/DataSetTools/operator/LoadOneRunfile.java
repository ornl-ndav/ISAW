/*
 * @(#)LoadOneRunfile.java     0.2  2000/07/21  Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.1  2000/07/21 20:38:56  dennis
 *  Generic operator to load all DataSets from one runfile
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
 * Operator to load all data sets from one IPNS runfile
 *
 * @see Operator
 */

public class LoadOneRunfile extends    Operator 
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

      Parameter parameter = getParameter(0);
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
   * Returns the abbreviated command string for this operator.
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
   * @return  The result of carrying out this operation is returned as a Java
   *          Object.
   */
   public Object getResult()
   {
     RunfileRetriever rr;
     DataSet          datasets[] = new DataSet[2];

                                          // get the parameters specifying the
                                          // runs         
     String    file_name   = (String)getParameter(0).getValue();
     String    group_mask  = (String)getParameter(1).getValue();

     int       masked_ids[] = IntList.ToArray( group_mask ); 

                                        // try to bring in the first run and
                                        // record the monitor peaks if needed
     rr = new RunfileRetriever( file_name );
     int n_ds      = rr.numDataSets();
     if ( n_ds <= 0 )
     {
       System.out.println("ERROR: no DataSets in " + file_name );
       return new ErrorString("ERROR: no DataSets in " + file_name);
     }

     DataSet[] dss = new DataSet[ n_ds ];
     for (int i = 0; i< n_ds; i++)
       dss[i] = rr.getDataSet(i);

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
