/*
 * File:  SumRunfiles.java 
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
 *  Revision 1.11  2001/09/13 22:53:39  dennis
 *  Added call to fixCase() on the file name in one place where it was missing.
 *
 *  Revision 1.10  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.9  2001/04/26 19:11:51  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.8  2001/03/01 20:57:43  dennis
 *  Modified import statments to JUST import IPNS.Runfile.Runfile
 *  and to explicitly import DataSetTools.instruments.InstrumentType.
 *  This resolved conflicting names between the InstrumentType
 *  class in IPNS.Runfile and in DataSetTools.instruments.
 *
 *  Revision 1.7  2000/11/10 22:41:34  dennis
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
 *  Revision 1.6  2000/11/07 15:40:02  dennis
 *  Fixed print statements to print DataSet not Data Set.
 *
 *  Revision 1.5  2000/08/03 21:42:10  dennis
 *  Now calls FilenameUtil.fixCase() to check for case errors in basic
 *  filename
 *
 *  Revision 1.4  2000/07/31 15:44:48  dennis
 *  Now uses SpecialStrings for the paramters so that the GUI can create
 *  appropriate components.
 *
 *  Revision 1.3  2000/07/21 19:52:01  dennis
 *  Now only do monitor pulse checking for direct geometry spectrometers
 *
 *  Revision 1.2  2000/07/21 19:17:38  dennis
 *  Now includes a group mask parameter to mask off certain groups of detectors.
 *
 *  Revision 1.1  2000/07/21 14:52:57  dennis
 *  Copied from MultiRunfileLoader.java and changed to use String form for
 *  the list of run numbers
 *
 */

package DataSetTools.operator;

import java.util.Vector;
import java.io.*;
import DataSetTools.dataset.*;
import DataSetTools.instruments.InstrumentType;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import DataSetTools.math.*;
import DataSetTools.peak.*;
import IPNS.Runfile.Runfile;


/**
 * Operator to load and sum multiple IPNS runfiles to produce a cummulative
 * histogram DataSet and MonitorDataSet.  The runs being loaded must have 
 * compatible detector grouping and time field schemes.
 *
 * @see Operator
 */

public class SumRunfiles extends    GenericLoad 
                         implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this constructor
   * is used, meaningful values for the parameters should be set before 
   * calling getResult().
   */
   public SumRunfiles( )
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
   *  @param  run_numbers A list of run numbers to be loaded
   *  @param  group_mask  A list of group IDs that should be omitted
   *
   *  @param  compare_monitor_pulses
   *                      Flag that determines wheter or not monitor pulses
   *                      are to be compared and used as a criteria to 
   *                      accept a run for summing.  This is normally passed
   *                      in as true for chopper spectrometers such as HRCS,
   *                      and false for other instruments.
   */
   public SumRunfiles(  DataDirectoryString    path, 
                        InstrumentNameString   instrument, 
                        IntListString          runs,
                        IntListString          group_mask,
                        boolean                compare_monitor_pulses  )
   {
      super( "Sum Multiple Runfiles" );

      Parameter parameter = getParameter(0);
      parameter.setValue( path );

      parameter = getParameter(1);
      parameter.setValue( instrument );

      parameter = getParameter(2);
      parameter.setValue( runs );

      parameter = getParameter(3);
      parameter.setValue( group_mask );

      parameter = getParameter( 4 );
      parameter.setValue( new Boolean( compare_monitor_pulses ) );
   } 

  /* -------------------------- setDefaultParameters ----------------------- */
  /**
   *  Set the parameters to default values.  
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Path to runfiles:", 
                          new DataDirectoryString("") );
    addParameter( parameter );

    parameter = new Parameter("Instrument file prefix (eg. hrcs)", 
                               new InstrumentNameString("hrcs") );
    addParameter( parameter );

    parameter = new Parameter("List of run numbers", 
                               new IntListString("") );
    addParameter( parameter );

    parameter = new Parameter("Group IDs to omit", 
                               new IntListString("") );
    addParameter( parameter );

    parameter = new Parameter("Compare monitor pulses?",new Boolean(false));
    addParameter( parameter );
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: in this case, SumFiles
   *
   */
   public String getCommand()
   {
     return "SumFiles";
   }


  /* ------------------------- MakeLogEntries --------------------------- */
  /**
   *
   */
   private void MakeSpectrometerLogEntries( DataSet           ds, 
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
     String    path        =
                 ((DataDirectoryString)getParameter(0).getValue()).toString();
     String    instrument  = 
                 ((InstrumentNameString)getParameter(1).getValue()).toString();
     String    run_nums    = 
                 ((IntListString)getParameter(2).getValue()).toString();
     String    group_mask  = 
                 ((IntListString)getParameter(3).getValue()).toString();
     boolean   compare_monitor_pulses
                        = ((Boolean)getParameter(4).getValue()).booleanValue(); 

                                         // only compare monitor pulses for
                                         // direct geometry spectrometers, if
                                         // desired

     if ( InstrumentType.getIPNSInstrumentType( instrument ) != 
          InstrumentType.TOF_DG_SPECTROMETER )
       compare_monitor_pulses = false;

     int       runs[]       = IntList.ToArray( run_nums ); 
     int       masked_ids[] = IntList.ToArray( group_mask ); 
     String    file_name;
     String    run_names[]  = new String[ runs.length ];

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
     float     centroid_1 = 0,
               variance_1 = 0;
     float     centroid;

     String temp_file_name = path + 
                  InstrumentType.formIPNSFileName( instrument, runs[0] );
     file_name = FilenameUtil.fixCase( temp_file_name );
     if ( file_name == null )
       return new ErrorString("ERROR: "+temp_file_name+" not found");

     System.out.println( "Opening " + file_name );
     
     try
     {
       rr            = new RunfileRetriever( file_name );
       first_runfile = new Runfile( file_name );

                             // load the first run's monitors and histogram
       datasets[0] = rr.getFirstDataSet(Retriever.MONITOR_DATA_SET);
       datasets[1] = rr.getFirstDataSet(Retriever.HISTOGRAM_DATA_SET);
       rr = null;
     
       for ( int k = 0; k < masked_ids.length; k++ )
         datasets[1].removeData_entry_with_id( masked_ids[k] );

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
       current_name = FilenameUtil.fixCase( current_name );
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

             for ( int k = 0; k < masked_ids.length; k++ )
               hist_ds.removeData_entry_with_id( masked_ids[k] );

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
       MakeSpectrometerLogEntries( datasets[1], run_names, mon_1, mon_2 );

     return datasets;
   }


   /* -------------------------------- main ------------------------------ */
   /* 
    * main program for test purposes only  
    */

   public static void main(String[] args)
   {
/*  Test case 1 ........................
      String runs = "9898,9899,6100";
      String mask = "";
      SumRunfiles loader = new SumRunfiles(
                 new DataDirectoryString("/IPNShome/dennis/ARGONNE_DATA/"),
                 new InstrumentNameString("gppd"),
                 new IntListString(runs),
                 new IntListString(mask),
                     true );
*/
/*  Test case 2 ..........................
*/
      String runs = "2444";
      String mask = "20:30,40:50";
      SumRunfiles loader = new SumRunfiles(
                 new DataDirectoryString("/IPNShome/dennis/ARGONNE_DATA/"),
                 new InstrumentNameString("hrcs"),
                 new IntListString(runs),
                 new IntListString(mask),
                     true );


/*  Test case 3 .......................... 
      String runs = "979,980,981";
      String mask = "";
      SumRunfiles loader = new SumRunfiles(
                 new DataDirectoryString("/IPNShome/dennis/ARGONNE_DATA/"),
                 new InstrumentNameString("hrcs"),
                 new IntListString(runs),
                 new IntListString(mask),
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

        System.out.println("Monitor DataSet Log...................."); 
        datasets[0].getOp_log().Print();
        System.out.println("Histogram DataSet Log...................."); 
        datasets[1].getOp_log().Print();
      }
      else
        System.out.println( result.toString() );
   } 
} 
