/*
 * @(#)MultiRunfileLoader.java     0.1  2000/06/13  Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.6  2000/07/19 21:49:33  dennis
 *  Added some docs, a default constructor and changed full name to
 *  Sum Multiple Runfiles
 *
 *  Revision 1.5  2000/07/17 20:58:12  dennis
 *  Simplified some calculations for log message and removed some extra prints.
 *
 *  Revision 1.4  2000/07/14 19:08:39  dennis
 *  Added code to calculate additional information such as incident energy,
 *  wavelength and wavenumber
 *
 *  Revision 1.3  2000/07/13 22:22:04  dennis
 *  Made improvements to calculation and formatting of Monitor Statistics
 *
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
import DataSetTools.instruments.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import DataSetTools.math.*;
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
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this constructor
   * is used, meaningful values for the parameters should be set before 
   * calling getResult().
   */
   public MultiRunfileLoader( )
   {
     super( "Sum Multiple Runfiles" );
   }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for with the specified parameter values so 
   *  that the operation can be invoked immediately by calling getResult().
   *
   *  @param  path        The directory path to the data directory 
   *  @param  instrument  The name of the instrument, as used in the prefix
   *                      for the file name.
   *  @param  run_numbers The list of run numbers to be loaded
   *
   *  @param  compare_monitor_pulses
   *                      Flag that determines wheter or not monitor pulses
   *                      are to be compared and used as a criteria to 
   *                      accept a run for summing.  This is normally passed
   *                      in as true for chopper spectrometers such as HRCS,
   *                      and false for other instruments.
   */
   public MultiRunfileLoader( String   path, 
                              String   instrument, 
                              int      run_numbers[],
                              boolean  compare_monitor_pulses  )
   {
      super( "Sum Multiple Runfiles" );

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
     return "SumMulti";
   }


  /* ------------------------- MakeLogEntries --------------------------- */
  /**
   *
   */
   void MakeLogEntries( DataSet           ds, 
                        String            run_names[],
                        HistogramDataPeak mon_1[], 
                        HistogramDataPeak mon_2[]  )
   {
                                                   // Monitor #1 statistics
      float area_1, 
            centroid_1,
            std_1;

      float position_1      = mon_1[0].getPosition();
      float fwhm_1          = mon_1[0].getFWHM();
      float extent_factor_1 = mon_1[0].getExtent_factor();

      float a1 = position_1 - extent_factor_1/2 * fwhm_1;
      float b1 = position_1 + extent_factor_1/2 * fwhm_1;

                                                   // Monitor #2 statistics
      float area_2,
            centroid_2,
            std_2,
            ratio;

      float position_2      = mon_2[0].getPosition();
      float fwhm_2          = mon_2[0].getFWHM();
      float extent_factor_2 = mon_2[0].getExtent_factor();

      float a2 = position_2 - extent_factor_2/2 * fwhm_2;
      float b2 = position_2 + extent_factor_2/2 * fwhm_2;
                                                   // Now add the data from
                                                   // from all the monitors to
                                                   // the DataSet log

      ds.addLog_entry("M1: On interval [ " + a1 + ", " + b1 + " ]");
      ds.addLog_entry("M2: On interval [ " + a2 + ", " + b2 + " ]");
      ds.addLog_entry(
  "   RUN     M1:AREA  CENTROID     STD    M2:AREA  CENTROID     STD   A2/A1");
      for ( int i = 0; i < mon_1.length; i++ )
      {
         mon_1[i].setEvaluationMode( IPeak.PEAK_ONLY );
         area_1     = mon_1[i].Area( a1, b1);
         centroid_1 = mon_1[i].Moment( a1, b1, 0, 1) / area_1;
         std_1      = (float)Math.sqrt( mon_1[i].Moment( a1, b1, 2) / area_1 );

         mon_2[i].setEvaluationMode( IPeak.PEAK_ONLY );
         area_2     = mon_2[i].Area( a2, b2);
         centroid_2 = mon_2[i].Moment( a2, b2, 0, 1) / area_2;
         std_2      = (float)Math.sqrt( mon_2[i].Moment( a2, b2, 2) / area_2 );
         ratio      = area_2/area_1;

         ds.addLog_entry( run_names[i] + " " +
                          Format.integer(area_1, 8)       + "  " +
                          Format.real(centroid_1, 8, 2)   + "  " +
                          Format.real(std_1, 6, 2)        + "   " +
                          Format.integer(area_2, 8)       + "  " +
                          Format.real(centroid_2, 8, 2)   + "  " +
                          Format.real(std_2, 6, 2)        + "  " +
                          Format.real(ratio, 6, 3)   ); 
      }  
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

                                          // get the parameters specifying the
                                          // runs         
     String    path        = (String)getParameter(0).getValue();
     String    instrument  = (String)getParameter(1).getValue();
     int       runs[]      = (int[])getParameter(2).getValue();
     String    file_name;
     String    run_names[] = new String[ runs.length ];

                                          // allocate and mark as un-used 
                                          // space for monitor peaks from each
                                          // DataSet.  Record the run names 
     HistogramDataPeak mon_1[] = new HistogramDataPeak[ runs.length ]; 
     HistogramDataPeak mon_2[] = new HistogramDataPeak[ runs.length ]; 
     for ( int i = 0; i < mon_1.length; i++ )
     {
       mon_1[i] = null;
       mon_2[i] = null;
       run_names[i] = instrument + " " + runs[i];
     }
                                        // try to bring in the first run and
                                        // record the monitor peaks if needed
     boolean   compare_monitor_pulses
                        = ((Boolean)getParameter(3).getValue()).booleanValue(); 
     float     centroid_1 = 0,
               variance_1 = 0;
     float     centroid;

     file_name = path + InstrumentType.formIPNSFileName( instrument, runs[0] );
     System.out.println( "Opening " + file_name );

     try
     {
       first_runfile = new Runfile( file_name );
       rr            = new RunfileRetriever( file_name );

                             // load the first run's monitors and histogram
       datasets[0] = rr.getFirstDataSet(Retriever.MONITOR_DATA_SET);
       datasets[1] = rr.getFirstDataSet(Retriever.HISTOGRAM_DATA_SET);
       rr = null;

       if ( datasets[0] == null || datasets[1] == null )
         return new ErrorString(
                    "ERROR: no monitors or histogram in " + file_name);

       if ( compare_monitor_pulses )  // record the centroid and variance of
                                      // the first monitor pulse from the first
                                      // runfile
       {
         mon_1[0] = new HistogramDataPeak( datasets[0].getData_entry(0), 
                               tof_data_calc.MONITOR_PEAK_EXTENT_FACTOR );
         mon_2[0] = new HistogramDataPeak( datasets[0].getData_entry(1), 
                               tof_data_calc.MONITOR_PEAK_EXTENT_FACTOR );

         mon_1[0].setEvaluationMode( IPeak.PEAK_ONLY );
         float area_1 = mon_1[0].Area( ); 
         centroid_1 = mon_1[0].Moment( 0, 1) / area_1; 
         variance_1 = mon_1[0].Moment( 2) / area_1; 
       } 
     }
     catch ( Exception e )
     {
       System.out.println("ERROR: exception accessing first runfile. " + e );
       return new ErrorString("ERROR: Can not access file: " + file_name);
     }

                                  // now bring in each later run, keep it if
                                  // the monitor peaks agree and save the
                                  // monitor peaks
     DataSet monitor_ds,
             hist_ds;
     String  current_name;
     DataSetOperator adder;
     boolean         ok_to_add;

     for ( int i = 1; i < runs.length; i++ )
     {      
       current_name = path + InstrumentType.formIPNSFileName( instrument,
                                                              runs[i] );
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
             mon_1[i] = new HistogramDataPeak(monitor_ds.getData_entry(0), 
                                  tof_data_calc.MONITOR_PEAK_EXTENT_FACTOR );
             mon_2[i] = new HistogramDataPeak(monitor_ds.getData_entry(1), 
                                  tof_data_calc.MONITOR_PEAK_EXTENT_FACTOR );

             mon_1[i].setEvaluationMode( IPeak.PEAK_ONLY );
             float area = mon_1[i].Area( );
             centroid = mon_1[i].Moment( 0, 1) / area;

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
     
     if ( compare_monitor_pulses )
       MakeLogEntries( datasets[1], run_names, mon_1, mon_2 );

     return datasets;
   }


   /* -------------------------------- main ------------------------------ */
   /* 
    * main program for test purposes only  
    */

   public static void main(String[] args)
   {
/*  Test case 1 ........................
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
/*  Test case 2 ..........................
*/
      int runs[] = new int[1];
      runs[0] = 2444;

      MultiRunfileLoader loader = new MultiRunfileLoader(
                                      "/IPNShome/dennis/ARGONNE_DATA/",
                                      "hrcs",
                                       runs,
                                       true );


/*  Test case 3 .......................... 
      int runs[] = new int[3];
      runs[0] = 979;
      runs[1] = 980;
      runs[2] = 981;
  
      MultiRunfileLoader loader = new MultiRunfileLoader(
                                      "/IPNShome/dennis/ARGONNE_DATA/",
                                      "HRCS",
                                       runs,
                                       true );
*/


      Object result = loader.getResult();
      if ( result instanceof DataSet[] )
      {
        DataSet datasets[] = (DataSet[])result;

        ViewManager viewmanager;
        viewmanager = new ViewManager( datasets[0], IViewManager.IMAGE );
        viewmanager = new ViewManager( datasets[1], IViewManager.IMAGE );

        float area[] = new float[2];
        float time[] = new float[2];
        for ( int mon = 0; mon < 2; mon++ )
        {
          HistogramDataPeak peak = new HistogramDataPeak(
                                   datasets[0].getData_entry(mon), 
                                   tof_data_calc.MONITOR_PEAK_EXTENT_FACTOR );
          peak.PrintPeakInfo( "SUM: Monitor " + (mon+1), IPeak.PEAK_ONLY );
  
          float position = peak.getPosition();
          float fwhm     = peak.getFWHM();
          float extent_f = peak.getExtent_factor();
          float a        = position - extent_f/2 * fwhm; 
          float b        = position + extent_f/2 * fwhm; 
          System.out.println("MONITOR "+ (mon+1)+": a = " + a + " b = " + b );
          area[mon]      = peak.Area( a, b);
          float centroid = peak.Moment( a, b, 0, 1) / area[mon];

          time[mon] = centroid;
        }

        // calculate the input energy from the monitor peak positions........

        Data mon_1_data = datasets[0].getData_entry( 0 ); 
        DetectorPosition position = (DetectorPosition)
                         mon_1_data.getAttributeValue(Attribute.DETECTOR_POS);
        float coords[] = position.getCartesianCoords();
        float mon_1_x = coords[0];
        
        Data mon_2_data = datasets[0].getData_entry( 1 );
        position = (DetectorPosition)
                         mon_2_data.getAttributeValue(Attribute.DETECTOR_POS);
        coords = position.getCartesianCoords();
        float mon_2_x = coords[0];
        
        System.out.println("----------------------------------");
        System.out.println("Mon 1 at x = "+mon_1_x+" Mon 2 at x = "+mon_2_x );
        System.out.println("Mon 1 time = "+time[0]+" Mon 2 time = "+time[1] );
        float energy = tof_calc.Energy( mon_2_x-mon_1_x, time[1]-time[0] );
        float wave_len = tof_calc.Wavelength(mon_2_x-mon_1_x, time[1]-time[0]);
        float wave_num = (float)(2*Math.PI/wave_len);
        System.out.println("Energy     = "+ energy );
        System.out.println("Wavelength = "+ wave_len );
        System.out.println("Wavenumber = "+ wave_num );
        System.out.println("----------------------------------");

        System.out.println("Mon 1 to Mon 2 distance ="+(mon_2_x-mon_1_x));
        System.out.println("Mon 1 to Mon 2 TOF      ="+(time[1]-time[0]));
        System.out.println("Velocity                ="+
                            ((mon_2_x-mon_1_x)/(time[1]-time[0])));
        float velocity = tof_calc.VelocityFromEnergy(energy);
        System.out.println("Velocity from E = " + velocity );
        System.out.println("E from Velocity = " + 
                            tof_calc.EnergyFromVelocity(velocity) );
        System.out.println( "Wavelength from Velocity = " + 
                             tof_calc.WavelengthFromVelocity( velocity ));
        System.out.println( "Velocity from Wavelength = " + 
                             tof_calc.VelocityFromWavelength( wave_len ));

        System.out.println("Ratio A2/A1 = " + area[1]/area[0] );
        System.out.println("----------------------------------");

        System.out.println("Monitor Data Set Log...................."); 
        datasets[0].getOp_log().Print();
        System.out.println("Histogram Data Set Log...................."); 
        datasets[1].getOp_log().Print();
      }
      else
        System.out.println( result.toString() );
   } 
} 
