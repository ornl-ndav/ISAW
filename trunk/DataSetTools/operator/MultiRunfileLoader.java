/*
 * @(#)MultiRunfileLoader.java     0.1  2000/06/13  Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.1  2000/07/10 22:36:11  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.2  2000/06/14 21:20:58  dennis
 *  replaced debug prints with calls to Peak.PrintPeakinfo()
 *
 *  Revision 1.1  2000/06/14 16:46:57  dennis
 *  Initial revision
 *
 *
 */

package DataSetTools.operator;

import java.util.Vector;
import java.io.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import DataSetTools.peak.*;
import IPNS.Runfile.*;


/**
 * Operator to load and sum multiple IPNS runfiles to produce a cummulative
 * histogram DataSet and MonitorDataSet.  The runs being loaded must have 
 * compatible detector grouping and time field schemes.
 *
 * @see Operator
 */

public class MultiRunfileLoader extends    Operator 
                                implements Serializable
{
   public MultiRunfileLoader( String   path, 
                              String   instrument, 
                              int      run_numbers[],
                              boolean  compare_monitor_pulses  )
   {
      super( "Load Multiple Runfiles" );

      Parameter parameter = getParameter(0);
      parameter.setValue( path );

      parameter = getParameter(1);
      parameter.setValue( instrument );

      int runs[] = new int[ run_numbers.length ];
      System.arraycopy( run_numbers, 0, runs, 0, runs.length );
      parameter = getParameter(2);
      parameter.setValue( runs );

      parameter = getParameter( 3 );
      parameter.setValue( new Boolean( compare_monitor_pulses ) );
   } 

  /* -------------------------- setDefaultParameters ----------------------- */
  /**
   *  Set the parameters to default values.  
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Path to runfiles:", new String("") );
    addParameter( parameter );

    parameter = new Parameter("Instrument file prefix (eg. hrcs)", 
                               new String("hrcs") );
    addParameter( parameter );

    parameter = new Parameter("List of run numbers", new int[1] );
    addParameter( parameter );

    parameter = new Parameter("Compare monitor pulses?",new Boolean(false));
    addParameter( parameter );
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   *
   */
   public String getCommand()
   {
     return "LoadMulti";
   }


  /* ----------------------------- getResult ---------------------------- */
  /**
   * Returns the object that is the result of applying this operation.  This
   * should be called after setting the appropriate parameters.  Derived classes
   * will override this method with code that will carry out the required
   * operation.
   *
   * @return  The result of carrying out this operation is returned as a Java
   *          Object.
   */
   public Object getResult()
   {
     Runfile          first_runfile,
                      current_runfile;

     RunfileRetriever rr;
     DataSet          datasets[] = new DataSet[2];

     String    path       = (String)getParameter(0).getValue();
     String    instrument = (String)getParameter(1).getValue();
     int       runs[]     = (int[])getParameter(2).getValue();
     boolean   compare_monitor_pulses
                        = ((Boolean)getParameter(3).getValue()).booleanValue(); 
     float     centroid_1 = 0,
               variance_1 = 0;
     float     centroid;

     String run_name = path+instrument+runs[0]+".run";
     System.out.println( "Opening " + run_name );

     try
     {
       first_runfile = new Runfile( run_name );
       rr            = new RunfileRetriever( run_name );

                             // load the first run's monitors and histogram
       datasets[0] = rr.getFirstDataSet(Retriever.MONITOR_DATA_SET);
       datasets[1] = rr.getFirstDataSet(Retriever.HISTOGRAM_DATA_SET);
       rr = null;

       if ( datasets[0] == null || datasets[1] == null )
         return new ErrorString(
                    "ERROR: no monitors or histogram in " + run_name);

       if ( compare_monitor_pulses )  // record the centroid and variance of
                                      // the first monitor pulse from the first
                                      // runfile
       {
         HistogramDataPeak peak_1 = new HistogramDataPeak( 
                                             datasets[0].getData_entry(0) );
         float position = peak_1.getPosition();
         float fwhm     = peak_1.getFWHM();

         peak_1.setEvaluationMode( IPeak.PEAK_ONLY );
         float area_1 = peak_1.Area( position-2.5f*fwhm, position+2.5f*fwhm); 
         centroid_1 = peak_1.Moment( position-2.5f*fwhm, 
                                     position+2.5f*fwhm, 0, 1) / area_1; 
         variance_1 = peak_1.Moment( position-2.5f*fwhm, 
                                     position+2.5f*fwhm, 2) / area_1; 
         peak_1.PrintPeakInfo( "First Run, Monitor 1", IPeak.PEAK_ONLY );
       } 
     }
     catch ( Exception e )
     {
       System.out.println("ERROR: exception accessing first runfile. " + e );
       return new ErrorString("ERROR: Can not access file: " + run_name);
     }

     DataSet monitor_ds,
             hist_ds;
     String  current_name;
     DataSetOperator adder;
     boolean         ok_to_add;

     for ( int i = 1; i < runs.length; i++ )
     {      
       current_name = path+instrument+runs[i]+".run";
       System.out.println( "i="+i+"...Opening... " + current_name );
       try
       { 
         current_runfile = new Runfile( current_name );
         if ( !first_runfile.isEqual( current_runfile ) )
           System.out.println("WARNING: "+current_name+" was rejected");
         else
         {
           rr = new RunfileRetriever( current_name );
           monitor_ds = rr.getFirstDataSet(Retriever.MONITOR_DATA_SET);

           if ( compare_monitor_pulses )  // find the centroid and variance 
                                          // of the first monitor pulse from the
                                          // current runfile and compare to 
                                          // the values from the first runfile
           {
             HistogramDataPeak peak = new HistogramDataPeak(
                                                monitor_ds.getData_entry(0) );
             float position = peak.getPosition();
             float fwhm     = peak.getFWHM();

             peak.setEvaluationMode( IPeak.PEAK_ONLY );
             float area = peak.Area( position-2.5f*fwhm, position+2.5f*fwhm);
             centroid = peak.Moment( position-2.5f*fwhm, 
                                     position+2.5f*fwhm, 0, 1) / area;

             peak.PrintPeakInfo( "Current Run, Monitor 1", IPeak.PEAK_ONLY );

             
             if ( Math.abs( centroid - centroid_1 ) < Math.sqrt(variance_1) )
               ok_to_add = true;
             else
             {
               ok_to_add = false;
               System.out.println("WARNING: "+current_name+" was rejected");
               System.out.println("           monitor pulses NOT aligned");
             }
           }
           else
             ok_to_add = true;

           if ( ok_to_add )
           {
             hist_ds    = rr.getFirstDataSet(Retriever.HISTOGRAM_DATA_SET);
             rr = null;

             adder = new DataSetAdd( datasets[0], monitor_ds, false ); 
             adder.getResult();
             adder = new DataSetAdd( datasets[1], hist_ds, false );
             adder.getResult();

             hist_ds    = null;
             monitor_ds = null;
           }
         }
       }
       catch ( Exception e )
       {
         System.out.println("ERROR: exception accessing runfile. " + e );
         System.out.println("WARNING: "+current_name+" was rejected");
       }
     }
     
     return datasets;
   }


   /* -------------------------------- main ------------------------------ */
   /* 
    * main program for test purposes only  
    */

   public static void main(String[] args)
   {
/*
      int runs[] = new int[3];
      runs[0] = 9898;
      runs[1] = 9899;
      runs[2] = 6100;

      MultiRunfileLoader loader = new MultiRunfileLoader( 
                                      "/IPNShome/dennis/ARGONNE_DATA/",
                                      "gppd",
                                       runs,
                                       true );
*/
      int runs[] = new int[2];
      runs[0] = 2444;
      runs[1] = 2451;

      MultiRunfileLoader loader = new MultiRunfileLoader(
                                      "/IPNShome/dennis/ARGONNE_DATA/",
                                      "hrcs",
                                       runs,
                                       true );

      Object result = loader.getResult();
      if ( result instanceof DataSet[] )
      {
        DataSet datasets[] = (DataSet[])result;

        ViewManager viewmanager;
        viewmanager = new ViewManager( datasets[0], IViewManager.IMAGE );
        viewmanager = new ViewManager( datasets[1], IViewManager.IMAGE );

        float area[] = new float[2];
        for ( int mon = 0; mon < 2; mon++ )
        {
          HistogramDataPeak peak = new HistogramDataPeak(
                                             datasets[0].getData_entry(mon) );
          peak.PrintPeakInfo( "SUM: Monitor " + (mon+1), IPeak.PEAK_ONLY );
  
          float position = peak.getPosition();
          float fwhm     = peak.getFWHM();
          area[mon] = peak.Area( position-2.5f*fwhm, position+2.5f*fwhm);
        }

        System.out.println("Ratio A2/A1 = " + area[1]/area[0] );

      }
      else
        System.out.println( result.toString() );
   } 
} 
