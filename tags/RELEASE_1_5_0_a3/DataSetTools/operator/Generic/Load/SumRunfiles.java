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
 *  Revision 1.6  2003/01/13 17:47:54  dennis
 *  Added getDocumentation() method. (Chris Bouzek)
 *  Added javadocs to getResult(). (Dennis Mikkelson)
 *
 *  Revision 1.5  2002/11/27 23:21:16  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/09/19 16:05:36  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.3  2002/07/29 18:56:34  dennis
 *  Fixed bug in forming Log entry in case a DataSet was rejected.
 *
 *  Revision 1.2  2002/03/05 19:27:50  pfpeterson
 *  Updated @see references in javadocs.
 *
 *  Revision 1.1  2002/02/22 20:57:58  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.Generic.Load;

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
import DataSetTools.operator.Parameter;
import DataSetTools.operator.DataSet.DataSetOperator;
import DataSetTools.operator.DataSet.Math.DataSet.DataSetAdd;
import DataSetTools.parameter.*;


/**
 * Operator to load and sum multiple IPNS runfiles to produce a cumulative
 * histogram DataSet and MonitorDataSet.  The runs being loaded must have
 * compatible detector grouping and time field schemes.
 *
 * @see DataSetTools.operator.Operator
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
   *                      Flag that determines whether or not monitor pulses
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

      IParameter parameter = getParameter(0);
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
   * @return the command name to be used with script processor:
   *         in this case, SumFiles
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
        if ( mon_1[i] != null && mon_2[i] != null )
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
   }

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator loads and sums multiple IPNS runfiles ");
    s.append("to produce a cumulative histogram DataSet and MonitorDataSet.\n");
    s.append("@assumptions The runs being loaded must have compatible ");
    s.append("detector grouping and time field schemes.  It is also assumed ");
    s.append("that the monitor pulses in the files are aligned if the ");
    s.append("compare_monitor_pulses parameter is set to true.  ");
    s.append("Furthermore, the files corresponding to the instrument and ");
    s.append("run numbers must exist.\n");
    s.append("@algorithm First this operator loads the first run's monitors ");
    s.append("and histogram.\n");
    s.append("Next, if compare_monitor_pulses is true, this operator records ");
    s.append("the centroid and variance of the first monitor pulse from the ");
    s.append("first runfile.\n");
    s.append("Next it brings in each later run and keeps it if the monitor ");
    s.append("peaks agree.  It then saves the monitor peaks.\n");
    s.append("Finally it sums the runs, placing the information in a ");
    s.append("monitor DataSet and a histogram DataSet.\n");
    s.append("@param path The directory path to the data directory.\n");
    s.append("@param instrument The name of the instrument as used in the ");
    s.append("prefix for the file name.\n");
    s.append("@param run_numbers A list of run numbers to be loaded.\n");
    s.append("@param group_mask A list of group IDs that should be ");
    s.append("omitted.\n");
    s.append("@param compare_monitor_pulses Flag that determines whether ");
    s.append("or not monitor pulses are to be compared and used as a ");
    s.append("criteria to accept a run for summing.  This is normally ");
    s.append("passed in as true for chopper spectrometers such as HRCS, ");
    s.append("and false for other instruments.\n");
    s.append("@return Object array which consists of ");
    s.append("(in the following order):\n");
    s.append("A monitor DataSet\n");
    s.append("A histogram DataSet\n");
    s.append("@error Returns an error if the files corresponding to the ");
    s.append("instrument and run numbers do not exist.\n");
    s.append("@error Returns an error if there are no monitors or ");
    s.append("histogram in the first DataSet.\n");
    s.append("@error Returns an error if the monitor pulses in the files ");
    s.append("are not aligned.\n");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  Loads and sums multiple runfiles.  If the compare_monitor_pulses flag
   *  is true and the instrument type is a Direct Geometry Spectrometer, then
   *  a particular run will only be added to the sum if the centroid of it's
   *  monitor peak is not further from the centroid of the first run's monitor
   *  peak, than the variance of the peak.
   *
   *  @return This returns an array of two DataSets.  The first entry in the
   *          array, datasets[0], is the sum of the monitor DataSets for the
   *          runs used.  The second entry in the array, datasets[1], is the
   *          sum of the sample histogram DataSets for the runs used.
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

     //added by Chris Bouzek
     System.out.println("\nThe results of calling getDocumentation() are:");
     System.out.println(loader.getDocumentation());
   }

}
