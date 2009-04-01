/*
 *File:  Util.java
 *
 * Copyright (C) Ruth Mikkelson 2008
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
 * Contact:Ruth Mikkelson,mikkelsonr@uwstout.edu
 *         Menomonie, WI 54751
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA. 
 *
 *  Last Modified:
 * 
 *  $Author: eu7 $
 *  $Date: 2008-04-08 16:31:08 -0500 (Tue, 08 Apr 2008) $            
 *  $Revision: 19031 $
*/

package Wizard.TOF_SCD;

import IsawGUI.Isaw;

import DataSetTools.dataset.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.operator.DataSet.Math.Analyze.IntegrateGroup;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.retriever.*;

import Operators.Special.Calib;

import java.io.*;
import java.util.*;

import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.File.FileIO;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Operator.Threads.*;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.*;

/**
 * This class contains an assortment of Utility methods for TOF_SCD wizards
 */
public class Util {
   
   public static final String[] CenteringNames = {"primitive" , "a centered" ,
              "b centered" , "c centered" , "[f]ace centered",
            "[i] body centered" , "[r]hombohedral centered"};

   /**
    *  FindCentroidedPeaks method uses separate processes to find and centroid
    *  the peaks from several runs and several data sets per run
    *
    * @param rawpath           The raw data path.
    * @param outpath           The output data path for the .peaks file.
    * @param runnums           The run numbers to load.
    * @param dataSetNums       The data set numbers to load in each run
    * @param expname           The experiment name (i.e. "quartz").
    * @param num_peaks         The maximum number of peaks to return.
    * @param min_intensity     The minimum peak intensity to look for.
    * @param min_time_chan     The minimum time channel to use.
    * @param max_time_chan     The maximum time channel to use. for all.
    * @param append            Append to file (yes/no).
    * @param use_calib_file    Use the calibration file
    * @param calib_file        SCD calibration file.
    * @param calib_file_line   SCD calibration file line to use.
    * @param min_row           The minimum row to use(add peak height)
    * @param max_row           The maximum row to use(subtract peak height)
    * @param min_col           The minimum column to use(add peak width)
    * @param max_col           The maximum column to use(subtract peak width)
    * @param max_Dspacing      Max d spacing between peaks
    * @param use_new_find_peaks Use the new find peaks method
    * @param do_smoothing      Smooth the data in the new find peaks method
    * @param do_validity_test  Do validity test in the new find peaks method
    * @param do_centroid       Perform old centroid on peaks
    * @param extension         The name of the extension on the data file
    * @param fileNamePrefix    The prefix for the filename.
    * @param show_peaks_view   Show image view of peaks
    * @param num_slices        The number of slices around  peak in image view.
    * @param ViewPeaks         Show the Peaks file
    * @param slurm_queue_name  The name of the slurm queue where the
    *                          processes should run.  If this is passed in 
    *                          as null, just execute the prescribed number
    *                          of processes locally.
    * @param max_processes     The maximun number of processes to use, 
    *                          locally, or with slurm.  This should not
    *                          be larger than the number of cores available
    *                          locally (or in the slurm queue if using slurm)
    *                          and may need to be restrained further due to 
    *                          available memory, if running locally.
    * @return a Vector of peaks, Grouped by detector
    */
   public static Vector<IPeak> findCentroidedPeaksUsingProcesses(
            String   rawpath,
            String   outpath,
            Vector   runnums,
            String   dataSetNums,
            String   expname,             // ?
            int      num_peaks,
            int      min_intensity,
            int      min_time_chan,
            int      max_time_chan,
            boolean  append,
            boolean  use_calib_file,
            String   calib_file ,
            int      calib_file_line,
            int      min_row,
            int      max_row,
            int      min_col,
            int      max_col,
            float    max_Dspacing,
            boolean  use_new_find_peaks,
            boolean  do_smoothing,
            boolean  do_validity_test,
            boolean  do_centroid,
            String   extension,
            String   fileNamePrefix,
            boolean  show_peaks_view,
            int      num_slices,
            boolean  ViewPeaks,
            String   slurm_queue_name,
            int      max_processes )
  {
    if( runnums == null )
    {
      System.out.println("Null run numbers");
      return null;
    }

    int[] run_numbers = new int[ runnums.size() ];
    try 
    {
      for( int i = 0 ; i < runnums.size() ; i++ )
        run_numbers[ i ] = ( (Integer) runnums.elementAt( i ) ).intValue();
    }
    catch( Exception s ) 
    {
      System.err.println("Entry not integer in runnums Vector");
      return null;
    }

    min_row = Math.max( 1, min_row );
    max_row = Math.max( max_row, min_row );

    min_col = Math.max( 1,  min_col );
    max_col = Math.max( max_col, min_col );

    String pixel_row = min_row+":"+max_row;
    String pixel_col = min_col+":"+max_col;

    java.util.Arrays.sort( run_numbers );

    int[] ds_numbers = IntList.ToArray( dataSetNums );

    if( !extension.startsWith( "." ) )
       extension = "."+extension;

    if( run_numbers == null || ds_numbers == null )
    {
       System.out.println("NULL ds_numbers or run_numbers");
       return null;
    }

    if( use_calib_file )
       SharedMessages.addmsg( "Calibration File ="+calib_file );

    Vector ops = new Vector();
    Random random = new Random();

    String fout_prefix = System.getProperty("user.home") + "/ISAW/SNAP_";

    int mon_count = 0;           // mon_count is not needed at this point

    for (int run_index = 0; run_index < run_numbers.length; run_index++ )
      for (int ds_index = 0; ds_index < ds_numbers.length; ds_index++ )
      {
        int ds_num  = ds_numbers[ ds_index ];
        int run_num = run_numbers[ run_index ];

        String fin_name  = rawpath + fileNamePrefix + run_num + extension;
        String fout_base = fout_prefix + run_num + "_DS_" + ds_num +
                              "_" + random.nextInt();
        String result = fout_prefix + run_num + "_" + ds_num + "_returned.txt";

        String cp = System.getProperty( "java.class.path" );
        if ( cp == null )
          cp = " ";
        else 
          cp = " -cp " + cp + " ";

        String mem = System.getProperty( "Find_Peaks_Process_Memory" );
        if ( mem == null )
          mem = "2000";

        String cmd  = " java -mx" + mem + "M "     +
                      " -XX:+AggressiveHeap "      +
                      " -XX:+DisableExplicitGC "   +
                      " -XX:ParallelGCThreads=4 "  + cp;

        if ( slurm_queue_name != null )               // use slurm, otherwise
          cmd = " srun -p " + slurm_queue_name +      // just pass in the
                " -J SCD_Find_Peaks -o " + result +   // basic command
                cmd;

        FindPeaksProcessCaller s_caller =
                 new FindPeaksProcessCaller( cmd,
                                             fin_name,
                                             fout_base,
                                             ds_num,
                                             num_peaks,
                                             min_intensity,
                                             min_time_chan,
                                             max_time_chan,

                                             use_calib_file,
                                             calib_file,
                                             calib_file_line,

                                             pixel_row,
                                             pixel_col,
                                             mon_count,
                                             max_Dspacing,
                                             use_new_find_peaks,
                                             do_smoothing,
                                             do_validity_test,
                                             do_centroid,
                                             show_peaks_view,
                                             num_slices );
        ops.add( s_caller );
      }

    ParallelExecutor executor;

    int num_processes = run_numbers.length * ds_numbers.length;
                                            // cap the number of processes to
                                            // avoid overloading slurm or the
                                            // local system.
    if (num_processes > max_processes )
      num_processes = max_processes;

    int max_time = ops.size() * 120000 + 600000;
    executor = new ParallelExecutor( ops, num_processes, max_time );

    Vector results = null;
    try                                     // try to do everything during the
    {                                       // alloted time and return partial
      results = executor.runOperators();    // results if something fails
    }                                       // or we run out of time.
    catch ( ExecFailException fail_ex )
    {
      FailState state = fail_ex.getFailureStatus();

      String reason;
      if ( state == FailState.NOT_DONE )
        reason = "maximum time, " + max_time/1000 + " seconds, elapsed ";
      else
        reason = "a process was interrupted.";

      SharedMessages.addmsg("WARNING: Find peaks did not finish: " + reason);
      SharedMessages.addmsg("The result returned is incomplete.");
      results = fail_ex.getPartialResults();
    }

    if ( fout_prefix.startsWith("/SNS/" ) &&             // do SNS Logging if
         slurm_queue_name != null         )              // using slurm at SNS
    {
      String cmd = "/usr/bin/logger -p local5.notice ISAW ISAW_" +
                   Isaw.getVersion(false) + 
                   " " + System.getProperty("user.name");
                                                        // append year as
                                                        // requested by Jim T.
      Calendar calendar = Calendar.getInstance();
      int      year     = calendar.get( Calendar.YEAR );

      cmd = cmd + " " + year;

      SimpleExec.Exec( cmd );
    }

    String out_file_name = outpath + expname + ".peaks";
    Vector all_peaks = null;

    if ( results != null )
    {
      all_peaks = new Vector();
      for ( int i = 0; i < results.size(); i++ )
      {                                                  // if process did not
        if ( results.elementAt(i) instanceof Vector )    // complete, there is
        {                                                // a FailState object
          Vector peaks = (Vector)results.elementAt(i);   // not a peaks Vector
          if ( peaks != null )
            for ( int k = 0; k < peaks.size(); k++ )
              all_peaks.add( peaks.elementAt(k) );
          else
            System.out.println("ERROR: NULL IN PEAK RESULTS VECTOR AT i = "+i);
        }
      }

      if ( append )
      {                                         // read in any existing peaks
        try                                     // and add to all_peaks Vector 
        {
          File file = new File(out_file_name );
          if ( file.exists() )
          {
            Vector old_peaks = Peak_new_IO.ReadPeaks_new( out_file_name );
            File delete_file = new File( out_file_name );
            delete_file.delete();
            if ( old_peaks != null )
              for ( int i = 0; i < old_peaks.size(); i++ )
                all_peaks.add( old_peaks.elementAt(i) );
          }
        }
        catch ( Exception ex )
        {
          System.out.println( "EXCEPTION reading old peaks file for append:" +
                               out_file_name );
          ex.printStackTrace();
          return null;
        }
      }else 
      {
         String PeakFileName = out_file_name ;
         File PkFile = new File( PeakFileName );
         if( PkFile.exists() )
            try {
               PkFile.delete();
            }
            catch( Exception ss ) {
               SharedMessages
                        .addmsg( "Could NOT delete old " + PeakFileName );
            }
      }

      try
      {
        Peak_new_IO.WritePeaks_new( out_file_name, all_peaks, false );
      }
      catch ( Exception ex )
      {
        System.out.println("Exception writing full file " + out_file_name );
        ex.printStackTrace();
      }
    }
    else
    {
      System.out.println("ERROR: RESULTS VECTOR NULL ");
      return null;
    }
                                        // Sort Vector of peaks so that 
                                        // the peaks appear in order
    System.out.println("TOTAL NUMBER OF PEAKS = " + all_peaks.size() );                                 
    Peak_new[] peak_array = new Peak_new[all_peaks.size()];
    for ( int i = 0; i < peak_array.length; i++ )
      peak_array[i] = (Peak_new)(all_peaks.elementAt(i));
 
    Arrays.sort( peak_array, new Peak_newComparator() );
    all_peaks = new Vector( peak_array.length );

    for ( int i = 0; i < peak_array.length; i++ )
      all_peaks.add( peak_array[i] );

    if( ViewPeaks && (new File( out_file_name).exists())) 
                                                // pause briefly to allow the
    {                                           // file to finish writing
       int     counter     = 0;                 // before we try to read it
       boolean file_exists = false;
       File    new_file;
       while ( counter < 10 && !file_exists )
       {
         new_file = new File( out_file_name );
         file_exists = new_file.exists();

         try
         {
           Thread.sleep( 1000 );
         }
         catch ( Exception ex )
         {
           System.out.println("Exception while waiting for Peaks file " );
           ex.printStackTrace();
         }
       }
       ( new ViewASCII( out_file_name ) ).getResult();
    }

    if ( show_peaks_view )
      PeakArrayPanels.DisplayPeaks( "Test Peaks", null, "", null, -1 );

    return all_peaks; 
  }
   
   
   /**
    *  FindCentroidedPeaks method uses threads to find and centroid
    *  the peaks from several runs and several data sets per run. The
    *  threads are the ones that calculate the peaks per detector. 
    *  
    *
    * @param rawpath        The raw data path.
    * @param outpath        The output data path for the .peaks file.
    * @param runnums        The run numbers to load.
    * @param dataSetNums    The data set numbers to load in each run
    * @param expname        The experiment name (i.e. "quartz").
    * @param num_peaks      The maximum number of peaks to return.
    * @param min_int        The minimum peak intensity to look for.
    * @param min_time_chan  The minimum time channel to use.
    * @param max_time_chan  The maximum time channel to use. for all.
    * @param append         Append to file (yes/no).
    * @param useCalib       Use the calibration file
    * @param calibfile      SCD calibration file.
    * @param line2use       SCD calibration file line to use.
    * @param min_row        The minimum row to use(add peak height)
    * @param max_row        The maximum row to use(subtract peak height)
    * @param min_col        The minimum column to use(add peak width)
    * @param max_col        The maximum column to use(subtract peak width)
    * @param Max_dSpacing   Max d spacing between peaks
    * @param NewFindPeaks   Use the new find peaks method
    * @param SmoothData     Smooth the data in the new find peaks method
    * @param ValidityTest   Do validity test in the new find peaks method
    * @param Centroid       Perform old centroid on peaks
    * @param extension      The name of the extension on the data file
    * @param fileNamePrefix The prefix for the filename.
    * @param ShowPeaksView   Show image view of peaks
    * @param numSlices      The number of slices around  peak in image view.
    * @param ViewPeaks      Show the Peaks file
    * @param maxNumThreads  The maximum number of threads to execute at one time
    * @return a Vector of peaks, Grouped by detector
    */
   public static Vector<IPeak> findCentroidedPeaks( 
            String   rawpath, 
            String   outpath, 
            Vector   runnums, 
            String   dataSetNums,
            String   expname,
            int      num_peaks, 
            int      min_int, 
            int      min_time_chan, 
            int      max_time_chan, 
            boolean  append, 
            boolean  useCalib,
            String   calibfile ,
            int      line2use,
            int      min_row,
            int      max_row,
            int      min_col, 
            int      max_col,
            float    Max_dSpacing,
            boolean  NewFindPeaks,
            boolean  SmoothData,
            boolean  ValidityTest,
            boolean  Centroid,
            String   extension,
            String   fileNamePrefix,
            boolean ShowPeaksView,
            int     numSlices,
            boolean  ViewPeaks,
            int      maxNumThreads )  throws IOException
     {

   String slurm_queue_name = System.getProperty( "Slurm_Queue_Name" );
   System.out.println("SLURM QUEUE NAME = " + slurm_queue_name );

   if ( slurm_queue_name != null )
     return findCentroidedPeaksUsingProcesses(
                       rawpath,
                       outpath,
                       runnums,
                       dataSetNums,
                       expname,      
                       num_peaks,
                       min_int,
                       min_time_chan,
                       max_time_chan,
                       append,
                       useCalib,
                       calibfile,
                       line2use,
                       min_row,
                       max_row,
                       min_col,
                       max_col,
                       Max_dSpacing,
                       NewFindPeaks,
                       SmoothData,
                       ValidityTest,
                       Centroid,
                       extension,
                       fileNamePrefix,
                       ShowPeaksView,
                       numSlices,
                       ViewPeaks,
                       slurm_queue_name,
                       maxNumThreads );
// START WITH THREADS      

      boolean useCache = false;
      if( runnums == null )
         return null;
      min_row = Math.max( 1,  min_row );
      max_row = Math.max(  max_row ,  min_row );
      min_col = Math.max( 1,  min_col );
      max_col = Math.max(  max_col ,  min_col );
      String PixelRow = min_row+":"+max_row;
      String PixelCol = min_col+":"+max_col;
      int[] Runs = new int[ runnums.size() ];
      long currentTime = System.currentTimeMillis();
      try {
         
         for( int i = 0 ; i < runnums.size() ; i++ )
            Runs[ i ] = ( (Integer) runnums.elementAt( i ) ).intValue();
         
      }catch( Exception s ) {
         
         return null;
         
      }
      
      java.util.Arrays.sort( Runs );
      
      int[] DSnums = IntList.ToArray( dataSetNums );
      
      Vector<StringBuffer> LogInfo = new Vector<StringBuffer>();
      
      if( !extension.startsWith( "." ) )
         extension = "."+extension;
      
      if( Runs == null || DSnums == null )
         return null;
      
      if( useCalib )
         SharedMessages.addmsg( "Calibration File ="+calibfile );
      
      Vector operators = new Vector();
      Vector ResultPeaks = new Vector();
      
      String PeakFileName = outpath+expname+".peaks";
      String LogFileName = outpath+"FindPeaks"+expname+".log";
      //String ExpFileName  = outpath+expname+".x";
      
      if( append )if( !( new java.io.File( PeakFileName ) ).exists() )
             // || !( new java.io.File( ExpFileName ) ).exists() )
         append = false;
      
      boolean append1 = false || append;
      if( !append1){
         File PkFile = new File( PeakFileName);
         if( PkFile.exists())
            try{
               PkFile.delete();
            }catch(Exception ss){
               SharedMessages.addmsg( "Could NOT delete old "+PeakFileName );
            }
      }
      
      String cacheFilename = gov.anl.ipns.Util.File.FileIO.CreateDistinctFileName(  
                System.getProperty( "user.home" ), "ISAW/localCache" , ".txt" , 20 );
      if( !useCache)
         cacheFilename = null;
      for( int i = 0; i < Runs.length; i++ ){
         //Replace by FindNexus operator in Operators/Generic/System/findnexus static method.
         String filename = rawpath+fileNamePrefix+Runs[ i ]+extension;

         Retriever retriever = null;
         try {
            retriever = Command.ScriptUtil.getRetriever( filename );
            if( cacheFilename != null)
            if( i == 0 && retriever instanceof NexusRetriever )
               ((NexusRetriever)retriever).SaveSetUpInfo( cacheFilename );
            else
               ((NexusRetriever)retriever).RetrieveSetUpInfo( cacheFilename );
            // ------------Get monitor count ---------------------
            int monCount = 10000;
            int ID = - 1;
            DataSet Monitor = null;
            
            if( retriever.getType( 0 ) == Retriever.MONITOR_DATA_SET ) {
               
               Monitor = retriever.getDataSet( 0 );
               ID = MonitorID_Calc.UpstreamMonitorID( Monitor );
               
               if( ID >= 0 ) {
                  
                  Object Result = ( new IntegrateGroup( Monitor , ID , 0 ,
                           50000 ) ).getResult();
                  
                  if( Result instanceof java.lang.Number )
                     monCount = ( (Number) Result ).intValue();
                  
               }
            }

            // --------------- For each data set in a run---------------------
            SharedMessages.addmsg( "Loading " + filename );

            for( int j = 0 ; j < DSnums.length ; j++ ) {

               DataSet DS = retriever.getDataSet( DSnums[ j ] );
               
               if( DS == null ) {
                  javax.swing.JOptionPane.showMessageDialog( null ,
                           "Could not retrieve " + filename + " dataset "
                                    + DSnums[ j ] );
               }
               else {

                  if( useCalib )
                     Calibrate( DS , calibfile , line2use );

                  if( AttrUtil.getUser( DS ) == null )
                     DS.setAttribute( new StringAttribute( Attribute.USER ,
                              "George User" ) );

                  // ----------------- For each grid in the data set
                  // ----------------
                  java.util.Hashtable gridIDs = Grid_util.getAllDataGrids( DS );
                  
                  if( gridIDs == null )
                     javax.swing.JOptionPane.showMessageDialog( null ,
                              "Grids not set up for " + filename + " dataset "
                                       + DSnums[ j ] );
                  else {
                     
                     Enumeration keys = gridIDs.keys();
                     
                     Object Err = MergeInfo( keys , DS , num_peaks , min_int ,
                              min_time_chan , max_time_chan , PixelRow ,PixelCol,
                              monCount , operators , ResultPeaks , LogInfo ,
                              maxNumThreads , Max_dSpacing ,
                              NewFindPeaks,
                              SmoothData,
                              ValidityTest, Centroid,
                              ShowPeaksView,
                              numSlices );
                     
                     if( Err != null ) {
                        SharedMessages.addmsg( "Error in finding peaks "
                                 + Err.toString() );

                     }                    
                  }// for enumeration
               }// else gridIds == null
                // Took too long on ARCS DATA Set with 20000 time channels 
               // (new WriteExp( DS, Monitor, ExpFileName ,ID,append1
               // ) ).getResult();
               // append1= true;  

            }// for  DS in run

         }
         catch( Exception s ) {
            
            SharedMessages.addmsg( "Could not retrieve " + filename );
         }
         
         if( retriever != null)
            if( retriever instanceof NexusRetriever)
               ((NexusRetriever)retriever).close();
         
      }//for @ run
      if( cacheFilename != null)
      (new File( cacheFilename)).delete();
   
     //--------------------Now finish the rest of the threads ---------------------
     long timeOut = 30000;
       while( operators.size() > 0 && timeOut < 360000 ){
          for( int i = operators.size() - 1 ; i >= 0 ; i-- ){
             OperatorThread opThreadElt = ( OperatorThread )( operators.elementAt( i ) );
             try{
                opThreadElt.join( timeOut );
                if( opThreadElt.getState() == Thread.State.TERMINATED ){
                   operators.remove( i );
                   Object Res = opThreadElt.getResult() ;
                   if( Res != null && Res instanceof Vector )
                       ResultPeaks.addElement( Res  );
                  
                   timeOut = 3000;
                }
             }catch( InterruptedException ss ){
                //Do nothing. Next time join should return and you have a terminated thread
             }
             
             
          }
          timeOut = 2 * timeOut;
       }
       //ToDo  get more info on operators
       // operators Vector could take an Array with more info( run num, detector num
       if( timeOut >= 180000 )
          SharedMessages.addmsg( operators.size()+" detectors did not finish in over 1 minute" );
       
       SortUnPackFix( ResultPeaks );
       try{
       FileOutputStream fout = new FileOutputStream( LogFileName );
       
       for( int i = 0 ; i < LogInfo.size() ; i++ ){
         String S =  LogInfo.elementAt( i ).toString() ;
         S = S+"\n";
         fout.write( S.getBytes() );
       }
       fout.close();
       SharedMessages.addmsg("Log File written to "+LogFileName);
       }catch( Exception t){
          SharedMessages.addmsg("Log info bad :"+t.toString());
       }
       LogInfo.clear();
       Vector<Peak_new> Peak1= new Vector<Peak_new>();
       if( append)
          try{
             Peak1 = Peak_new_IO.ReadPeaks_new( PeakFileName);
         
          }catch( Exception s1){
            SharedMessages.addmsg("Cannot append:"+ s1) ;
            Peak1 = new Vector<Peak_new>();
          }
       
       Peak1.addAll(  ResultPeaks );
       ResultPeaks = Peak1;
       Peak_new_IO.WritePeaks_new( PeakFileName, 
                                   ResultPeaks, 
                                   append1 );

      //----------- Write and View Peaks --------------------
      // (new WritePeaks( PeakFileName, ResultPeaks, append1 ) ).getResult();
      
      System.out.println( "--- find_multiple_peaks is done. ---" );
      if(  ResultPeaks != null && ResultPeaks.size()>0)
         SharedMessages.addmsg( "Peaks are listed in "+PeakFileName );
       else
          SharedMessages.addmsg( "There are no peaks" );
      
      if( ViewPeaks && ResultPeaks != null && ResultPeaks.size()>0)                         // pause briefly to allow the
      {                                       // file to finish writing
         int     counter     = 0;             // before we try to read it
         boolean file_exists = false;
         File    new_file;
         while ( counter < 10 && !file_exists )
         {
           new_file = new File( PeakFileName );
           file_exists = new_file.exists();

           try 
           {
             Thread.sleep( 1000 );
           }
           catch ( Exception ex )
           {
             System.out.println("Exception while waiting for Peaks file " );
             ex.printStackTrace();
           }
         }
         ( new ViewASCII( PeakFileName ) ).getResult();
      }
   
      if( ShowPeaksView&& ResultPeaks != null && ResultPeaks.size()>0)
        PeakArrayPanels.DisplayPeaks( "Peak Images",null,"",".pvw",currentTime);

      return ResultPeaks;

// END WITH THREADS
   }
   
   /*//Uses parallel Executor(MergeInfo1). Requires reading in files first
   // Then calculations are done in parallel
    if(operators.size()>0 ){//Have some operators not run?

      ParallelExecutor PE = new ParallelExecutor(
                        operators, maxNumThreads, 30000*operators.size() );
      
      Vector Results = null;
      try{
          Results = PE.runOperators();

      }catch( ExecFailException ss ){
         Results = ss.getPartialResults();
         SharedMessages.addmsg( "Timeout error:"+ss );
      }catch(Throwable s1 ){
         SharedMessages.addmsg( "Execute error:"+s1 );
      }
      int seqNum = ResultPeaks.size()+1;
      if(Results != null )
         for( int kk=0 ; kk < Results.size( ) ; kk++){
            Object R = Results.elementAt( kk  );
            if( R != null && R instanceof IPeak){
               ((IPeak)R).seqnum( seqNum  );
               ResultPeaks.addElement( R );
               seqNum++;
           }
           SharedMessages.addmsg( LogInfo.firstElement() );
           LogInfo.remove(0 );
         }
    }
*/ // does not use Parallel Executor.  Uses MergeInfo
   
   
   
   //ResultPeaks is a Vector of Vector of Peaks
   // Sort according to run number and detectorID
   // Unpack means to make into a Vector of Peaks
   // Fix means to adds sequence numbers
   private static void SortUnPackFix( Vector ResultPeaks ){
      
      if( ResultPeaks == null )
         return;
      
      Integer[] rankSort = new Integer[ ResultPeaks.size() ];
      
      for( int i = 0 ; i < ResultPeaks.size() ; i++ )
         rankSort[ i ] = i;
      
      java.util.Arrays.sort(  rankSort, new VComparator( ResultPeaks ) );
      
      Vector V = new Vector( ResultPeaks.size() );
      for( int i = 0 ; i < rankSort.length ; i++ )
         V.addElement(  ResultPeaks.elementAt( rankSort[ i ] ) );
      
      ResultPeaks.clear();
      
      int seqNum =1;
      for( int i = 0 ; i < V.size() ; i++ ){
         
         Vector V1 = ( Vector  )( V.elementAt( i ) );
         for( int j = 0 ; j < V1.size() ; j++ ){
            
            Object R = V1.elementAt( j );
            if( R != null && R instanceof IPeak ){
               
               ( ( IPeak )R ).seqnum( seqNum );
               seqNum++;
               ResultPeaks.addElement( R );
               
            }
         }
            
      }
      
   }
   
   
   // Used to Sort a Vector of Vector of Peaks where the Vector of Peaks
   //    come from the same detector and run number
   private static class VComparator implements java.util.Comparator<Integer>{

      Vector ResultPeaks;
      public VComparator( Vector ResultPeaks ){
         this.ResultPeaks = ResultPeaks;
      }
      /* (non-Javadoc)
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      @Override
      public int compare( Integer o1 , Integer o2 ) {
         int i1 = o1.intValue();
         int i2 = o2.intValue();
         if( i1 == i2 )
            return 0;
         if( i1 < 0 ||i1 >= ResultPeaks.size() ){
            if( i2 < 0 || i2 >= ResultPeaks.size() )
               return 0;
             return 1;
            }
         
         if( i2 < 0 ||i2 >= ResultPeaks.size() ){
            if( i1 < 0 || i1 >= ResultPeaks.size() )
               return 0;
            return -1;
         }  
         
         Object R1 = ResultPeaks.elementAt( i1 );
         Object R2 = ResultPeaks.elementAt( i2 );
         if( R1 == null ||  !( R1 instanceof Vector )
                  || ( ( Vector )R1 ).size() < 1 ){
            if( R2 == null ||  !( R2 instanceof Vector )
                     || ( ( Vector )R2 ).size() < 1 )
               return 0;
            return 1;
         }

         if( R2 == null ||  !( R2 instanceof Vector )
                  || ( ( Vector )R2 ).size() < 1 ){
            if(  !( R1 instanceof Vector )
                     || ( ( Vector )R1 ).size() < 1 )
               return 0;
            return -1;
         }
                     
         Object pk1 = ( ( Vector )R1 ).elementAt( 0 );
         Object pk2 = ( ( Vector )R2 ).elementAt( 0 );
         if( pk1 == null || !( pk1 instanceof IPeak ) ){
            if( pk2 == null || !( pk2 instanceof IPeak ) )
               return 0;
            return -1;
         }

         if( pk2 == null || !( pk2 instanceof IPeak ) ){
            if(  !( pk1 instanceof IPeak ) )
               return 0;
            return 1;
         }
         IPeak peak1 = ( IPeak )pk1;
         IPeak peak2 = ( IPeak )pk2;
         if( peak1.nrun() < peak2.nrun() )
            return -1;
         if( peak1.nrun() > peak2.nrun() )
            return 1;
         if( peak1.detnum() < peak2.detnum() )
            return -1;
         if( peak1.detnum() > peak2.detnum() )
            return 1;
         
         
         return 0;
      }
       
   }


   /**
    *  Apply the specified calibration information to the specified
    *  DataSet, using the Calib operator.
    *
    *  @param DS             The DataSet to calibrate.
    *  @param calibFileName  The name of the calibration file
    *  @param lineNum        The line number to use from the IPNS
    *                        style calibration file.
    *
    */
   public static void Calibrate( DataSet DS, 
                                 String  calibFileName, 
                                 int     lineNum ){
      Calib calib = new Calib();
      calib.DS = DS;
      calib.CalibFile1 = new LoadFileString( calibFileName );
      Vector V = new Vector();
      V.add( lineNum );
      calib.otherInformation = V;
      Object Result = calib.calculate();
      if( Result instanceof ErrorString )
         SharedMessages.addmsg( "Error calibrating "+ DS + "::"+Result );
   }
   
   
   
   // Calculates Peaks and adds them to the ResultPeaks. 
   // Uses the Parallel Executor
   //TODO: change 30000 per thread to something related to the size of
   //      the data set.
   // Uses Parallel Executor. Also ResultPeaks not in correct form
   //   if it has to be sorted by the SortUnPackFix method which
   //   requires a Vector of Vector of Peaks where each Vector of
   //    Peaks are from one detector on one run
   private static void MergeInfo1(
            Enumeration keys,
            DataSet     DS, 
            int         num_peaks, 
            int         min_int, 
            int         min_time_chan,
            int         max_time_chan,
            String      PixelRow,
            String      PixelCol,
            int         monCount,
            Vector      operators, 
            Vector      ResultPeaks,
            Vector<StringBuffer> buff,

            boolean NewFindPeaks,
            boolean  SmoothData,
            boolean  ValidityTest,
            boolean Centroid,
            boolean ShowPeaksView,
            int numSlices ,
            int         maxNumThreads ){
      
      if( keys == null || DS == null || num_peaks <= 0 )
         return;
      
     Vector<Integer> keyList = new Vector<Integer>();
     
      for( ; keys.hasMoreElements() ; ){
         
         Object K = keys.nextElement();
         
         if( K != null && K instanceof Integer )
            keyList.addElement( ( Integer )K );
         
      } 
      
      int[] Keys = new int[ keyList.size() ];
      
      for( int i = 0 ; i < keyList.size() ; i++ ){
         Keys[ i ] = keyList.elementAt( i );
      }
      
      java.util.Arrays.sort( Keys );
         
      for( int i = 0 ; i < Keys.length ; i++ ){
         
            int K = Keys[ i ];
            
            StringBuffer Sbuff = new StringBuffer();
            
            buff.addElement( Sbuff );
            
            operators.addElement( SetUpfindPeaksOp( DS,
                         K, num_peaks, 
                               min_int,  min_time_chan, max_time_chan,
                               PixelRow, PixelCol,monCount, Sbuff, 12f,
                               NewFindPeaks,
                               SmoothData,
                               ValidityTest,
                               Centroid,
                              ShowPeaksView,
                               numSlices  ) );
           
            if( operators.size() >= maxNumThreads ){
               ParallelExecutor PE = new ParallelExecutor(
                                  operators, maxNumThreads, 30000 * maxNumThreads );
               
               Vector Results = null;
               System.out.println( "Start run in parallel "+ System.currentTimeMillis() );
               SharedMessages.addmsg( "Start run in parallel" );
               
               try{
                  
                   Results = PE.runOperators();
                   
               }catch( ExecFailException ss ){
                  
                  Results = ss.getPartialResults();
                  SharedMessages.addmsg( "Timeout error:"+ss );
                  
               }catch( Throwable s1 ){
                  
                  SharedMessages.addmsg( "Execute error:"+s1 );
                  
               }

               System.out.println( "End run in parallel"+ System.currentTimeMillis() );
               SharedMessages.addmsg( "End run in parallel" );
               
               //-------- Set sequence numbers-------
               int seqNum = ResultPeaks.size()+1;
               
               if( Results != null )
                  for( int kk = 0 ; kk < operators.size() ; kk++ ){
                     
                     Object RR = Results.elementAt( kk );
                     if( RR != null && RR instanceof Vector ){
                        
                        for( int p =0 ; p < ( ( Vector )RR ).size() ; p++ ){
                           
                           IPeak R = ( IPeak )( ( Vector )RR ).elementAt( p );
                           R.seqnum( seqNum );
                           ResultPeaks.addElement( R );
                           seqNum++;
                           
                        }
                     }
                     
                     StringBuffer sbuff = buff.firstElement();
                     buff.remove( 0 );
                     SharedMessages.addmsg( sbuff );
                     
                  }//For each operator

               operators.removeAllElements();
               buff.clear();
               
            }//if operators.size() >= maxNumThreads
         }
      }
  
   
   // Calculates Peaks and adds them to the ResultPeaks. 
  // Does NOT use the Parallel Executor
   // ResultantPeaks is a Vector of Vector of Peaks where each
   //    Vector of Peaks is from one detector on one run
   //returns false if an error message occurred
   private static Object MergeInfo(
            Enumeration keys,
            DataSet     DS, 
            int         num_peaks, 
            int         min_int, 
            int         min_time_chan,
            int         max_time_chan,
            String      PixelRow,
            String      PixelCol,
            int         monCount,
            Vector      operators, 
            Vector      ResultPeaks,
            Vector<StringBuffer> buff,
            int         maxNumThreads,
            float         Max_dSpacing,

            boolean NewFindPeaks,
            boolean  SmoothData,
            boolean  ValidityTest,
            boolean   Centroid,
            boolean ShowPeaksView,
            int numSlices ){
      
      if( keys == null || DS == null || num_peaks <= 0 )
         return null;
      
      for( ; keys.hasMoreElements() ; ){
         
         Object K = keys.nextElement();

         if( K != null  && K instanceof Integer ){
            
            Object Error = null;
            int timeOut = 3000;
            if( maxNumThreads <=0 ){//do totally sequentially
               StringBuffer Sbuff = new StringBuffer();
               Operator op =SetUpfindPeaksOp( DS,
                        ( ( Integer )K ).intValue(), num_peaks, 
                        min_int,  min_time_chan, max_time_chan,
                        PixelRow,PixelCol, monCount, Sbuff, Max_dSpacing,
                         NewFindPeaks,
                        SmoothData,
                        ValidityTest,
                        Centroid,
                        ShowPeaksView,
                        numSlices  );
               Object Res = op.getResult();

               buff.addElement( Sbuff );
               if( Res != null && Res instanceof Vector )
                  ResultPeaks.addElement(  Res );
               
               else if( Res != null && Res instanceof ErrorString )
                  Error = Res;
               
               return Error;
                  
            }else
            while( operators.size() >= maxNumThreads  && timeOut < 18000 ){
               
               boolean done = false;
               for( int i = 0 ; i < operators.size() && !done ; i++ ){
                  
                  OperatorThread opThreadelt = 
                                  ( OperatorThread )( operators.elementAt( i ) );
                  
                  try{
                     
                    opThreadelt.join( timeOut );
                    
                    if( opThreadelt.getState() == Thread.State.TERMINATED ){
                       
                       Object Res = opThreadelt.getResult();
                       
                       if( Res != null && Res instanceof Vector )
                          ResultPeaks.addElement(  Res );
                       
                       else if( Res != null && Res instanceof ErrorString )
                          Error = Res;
                       
                       operators.remove(  i );
                       
                       timeOut = 3000;
                       done = true;
                       
                    }
                  }catch( Exception ss ){
                     
                     done = false;
                  }
                  
               }
               
               if( !done ) 
                  timeOut = 2 * timeOut;
              
              
               if( Error != null )
                  return Error;
               
            }//while operators.size() >= maxNumThreads
            
            
            if( timeOut >= 180000 ){
               
               System.out.println( "TimeOut problem. Several Threads are hung" );
            }
            
            StringBuffer Sbuff = new StringBuffer();
            OperatorThread opThread = new OperatorThread( SetUpfindPeaksOp( DS,
                         ( ( Integer )K ).intValue(), num_peaks, 
                               min_int,  min_time_chan, max_time_chan,
                               PixelRow,PixelCol, monCount, Sbuff, Max_dSpacing,
                                NewFindPeaks,
                               SmoothData,
                               ValidityTest,
                               Centroid,
                               ShowPeaksView,
                               numSlices  ) );
            

            opThread.start();
            
            operators.addElement(  opThread );
            
            buff.addElement( Sbuff );
           
         }
      }
      return null;
   }
   
   // creates the operator findDetectorCentroidPeaks and sets up the parameter
   // list. It returns the set up operator.
   private static Operator SetUpfindPeaksOp( DataSet DS, 
            int     DetectorID,
            int     num_peaks, 
            int     min_int, 
            int     min_time_chan,
            int     max_time_chan,
            String  PixelRow,
            String  PixelCol,
            int     monCount,
            StringBuffer buff,
            float   Max_dSpacing,
            boolean NewFindPeaks,
            boolean  SmoothData,
            boolean  ValidityTest,
            boolean  Centroid,
            boolean ShowPeaksView,
            int numSlices ){
          
          findDetectorCentroidedPeaks op = new findDetectorCentroidedPeaks();
          op.getParameter( 0 ).setValue( DS );
          op.getParameter( 1 ).setValue( DetectorID );
          op.getParameter( 2 ).setValue( num_peaks );
          op.getParameter( 3 ).setValue( min_int );
          op.getParameter( 4 ).setValue( min_time_chan );
          op.getParameter( 5 ).setValue( max_time_chan );
          op.getParameter( 6 ).setValue( PixelRow );
          op.getParameter( 7 ).setValue( PixelCol );
          op.getParameter( 8 ).setValue( monCount );
          op.getParameter( 9 ).setValue( Max_dSpacing );

          op.getParameter( 10 ).setValue(NewFindPeaks );
          op.getParameter( 11).setValue(SmoothData );
          op.getParameter( 12).setValue(ValidityTest );
          op.getParameter( 13).setValue(Centroid );
          op.getParameter( 14).setValue(ShowPeaksView );
          op.getParameter( 15).setValue(numSlices );
          op.getParameter( 16 ).setValue( buff );
          
         
          return op;
   }
   
   
   
   /**
    * Finds all Peaks on one detector
    * @param DS             The data set with the detector
    * @param DetectorID     The detectorID
    * @param num_peaks      The maximum number of peaks to return.
    * @param min_int        The minimum peak intensity to look for.
    * @param min_time_chan  The minimum time channel to use.
    * @param max_time_chan  The maximum time channel to use.
    * @param PixelRow       The row/col to keep
    * @param monCount       Monitor Count
    * @param Max_dSpacing   Maximum d-spacing between peaks
    * @param NewFindPeaks   Use new find peaks method
    * @param SmoothData     Use smoothed date in new find peaks method
    * @param ValidityTest   Use a validity check in the new find peaks
    * @param Centroid       Use old Centroid peaks method on each peak
    * @param ShowPeaksView  Show image view of peaks
    * @param numSlices      The number of slices around a peak to show/
    * @param buff           If this is a non-null StringBuffer, the log 
    *                           information will be appended to it, otherwise
    *                           the log info will be displayed on the Status
    *                          Pane.
    * @return               A Vector of peaks
    */
   public static Vector<IPeak> findDetectorCentroidedPeaks( 
            DataSet DS, 
            int     DetectorID,
            int     num_peaks, 
            int     min_int, 
            int     min_time_chan,
            int     max_time_chan,
            String  PixelRow,
            String  PixelCol,
            int     monCount,
            float  Max_dSpacing,
            boolean NewFindPeaks,
            boolean  SmoothData,
            boolean  ValidityTest,
            boolean  Centroid,
            boolean ShowPeaksView,
            int numSlices,
            StringBuffer buff ){
      
      IDataGrid grid = Grid_util.getAreaGrid( DS , DetectorID );
      if( grid == null )
         return null;    
      
      if( DS.getNum_entries() < 1 )
         return null;
     
      int[] RowColRange = {1 , grid.num_rows() , 1 , grid.num_cols()};
      
      if( PixelRow != null && PixelRow.trim().length() > 1 ){
         
         int[] rr = IntList.ToArray( PixelRow );
        
         if( rr != null && rr.length >= 2 && rr[ 0 ] > 0 && rr[ rr.length - 1 ] > 0 ){
            
           RowColRange[ 0 ] = RowColRange[ 2 ] = Math.min( rr[ 0 ], RowColRange[ 0 ] );
           RowColRange[ 1 ] = RowColRange[ 3 ] =
                      Math.min( rr[ rr.length - 1 ], RowColRange[ 1 ] );
           
         }
            
      }else
         PixelRow="1:"+ Math.max( grid.num_rows() , grid.num_cols() );
      
      
      //------------------ Find Peaks ------------------
      Vector Pks = null;
      if( !NewFindPeaks)
      Pks = FindPeaks.findDetectorPeaks( DS,
                                                DetectorID,  
                                                min_time_chan, 
                                                max_time_chan, 
                                                num_peaks, 
                                                min_int,
                                                PixelRow, 
                                                PixelCol, 
                                                buff );
     
      else{
        boolean smooth_data = SmoothData;
        Pks = FindPeaks.findDetectorPeaks_new( DS, 
                                                    DetectorID,
                                                    min_time_chan,
                                                    max_time_chan,
                                                    num_peaks, 
                                                    min_int,
                                                    PixelRow, 
                                                    PixelCol,
                                                    smooth_data,
                                                    ValidityTest,
                                                    buff );
      }
      if( Pks == null || Pks.size() < 1 ){
        
         return Pks;
      }
      
      float pixelW_H = Math.max( grid.width()/grid.num_cols() , 
                                 grid.height()/grid.num_rows() );
      if( min_time_chan < 0 )
         min_time_chan = 0;
      
      //---------------Centroid Peaks ---------------------------
      if( Centroid)
      for( int i = 0 ; i < Pks.size() ; i++ ){
         IPeak Pk = ( IPeak )( Pks.elementAt( i ) );       
 
         Pk = CentroidPeak( Pk, DS, grid, Max_dSpacing, pixelW_H, min_time_chan,
                            max_time_chan, RowColRange );
        
         Pks.set( i , Pk );
         
      }
     
      EliminateDuplicatePeaks( Pks );
      
      
      //------------------ Replace IPeak by Peak_new ----------------------

      XScale xscl = grid.getData_entry( 1 , 1  ).getX_scale();
      DataSetTools.instruments.SampleOrientation sampOrient = AttrUtil.
                                                   getSampleOrientation( DS );

      float InitialPath = AttrUtil.getInitialPath( DS.getData_entry( 0 ) );
      float T0 = AttrUtil.getT0Shift( DS.getData_entry( 0 ) );
      
      if( Float.isNaN( T0 ) )
         T0 = 0;
     
      //Now get a Peak_new
      if( grid instanceof RowColGrid ){
         IDataGrid grid1 = RowColGrid.getUniformDataGrid( ( RowColGrid ) grid,
                                                            .001f );
         if( grid1 == null )
            throw new IllegalArgumentException( "grid "+grid.ID()+
                                                " not uniform enough" );
         grid = grid1;         
      }
      
      IDataGrid gridSave = grid;
      gridSave.setData_entries( DS );
      grid = grid.clone();//So do not wipe out other grids.
      grid.clearData_entries(); 

      // Convert all Peaks to a Peak_new Object so position info can be 
      // determined
      Vector<IPeak> ResultantPeak = new Vector<IPeak>( Pks.size() );
      PeakDisplayInfo[] infos = null;
      if( ShowPeaksView) infos =new PeakDisplayInfo[ Pks.size() ];
      int NOffset = 21;
      for( int i = 0 ; i < Pks.size() ; i++ ){
         
         IPeak pk1 = ( IPeak )Pks.elementAt( i );
         Peak_new pk = new Peak_new( pk1.nrun(),
                                     monCount,
                                     pk1.x(), 
                                     pk1.y(), 
                                     pk1.z(), 
                                     grid, 
                                     sampOrient, 
                                     xscl.getInterpolatedX( pk1.z() ) + T0, 
                                     InitialPath,
                                     T0 );

         pk.setFacility( AttrUtil.getFacilityName( DS ) );
         pk.setInstrument( AttrUtil.getInstrumentName( DS ) );
         pk.seqnum( pk1.seqnum() );
         pk.ipkobs( pk1.ipkobs() );
         pk.inti( pk1.inti() );
         pk.sigi( pk1.sigi() );
         pk.reflag( pk1.reflag() );

         if( ShowPeaksView && infos!= null)
            infos[i]= ShowOnePeakImageView( pk, 
                                            gridSave, 
                                            NOffset, 
                                            numSlices, 
                                            i );
         ResultantPeak.add(  pk );
      }   

      if( ShowPeaksView ){
         //PeaksDisplayPanel main_panel= new PeaksDisplayPanel( infos);
  
         
         String outFilename = FileIO.appendPath(System.getProperty( "user.home") ,
                      "ISAW"+File.separator+"tmp"+File.separator);
         
         File F = new File(outFilename.replace( '/' , File.separatorChar));
         if(!F.exists() ||  !F.isDirectory())
            if( ! F.mkdir())
               return ResultantPeak;
         
         //Now get the part of the filename after the path and before the extension
         // this will be part of the filename along with "_" followed by detector ID
         //  and then the extension
         String fname = AttrUtil.getFileName( DS );
         int k = fname.lastIndexOf( '.' );
         fname = fname.replace( '\\' , '/' );
         if( k >=0)
            fname = fname.substring(0,k);
         k= fname.lastIndexOf( '/' );
         if( k >=0)
            fname = fname.substring(k+1);
         
         //
         //outfilename is {user.home}/ISAW/tmp/PeakV{filename}_{detectorID}.pvw
         outFilename +=fname +"_"+String.format( "%4d" , DetectorID).replace( ' ' , '0' )+".pvw";
        
         try{
             ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream( outFilename));
             out.writeObject( infos );
             out.close();
         }catch(Exception s){
            s.printStackTrace();
         }
      }

      return ResultantPeak;
   }
   

   /**
    * Creates a PeakDisplayInfoElement from a peak.  
    * NOTE: The number of slices before and after the peak is constrained
    *       to be between one and 20.
    * @param pk1       The Peak from which the PeakDisplayInfo Element is 
    *                  created
    * @param gridSave  The Data Grid. It must have the data in the data 
    *                  set entered
    * @param NOffset   Number of pixels around the peak to include in the image
    *                  view
    * @param numSlices Number of slices to include before and and after the
    *                  peak
    * @param seq       The sequence information for the peak
    * @return          The PeakDisplayInfoElement corresponding to the peak
    */
   public static PeakDisplayInfo ShowOnePeakImageView(
                         Peak_new  pk1, 
                         IDataGrid gridSave, 
                         int       NOffset,
                         int       numSlices,
                         int       seq ) {
      if ( numSlices < 1 ) 
        numSlices = 1;
      if ( numSlices > 20 )
        numSlices = 20;

      float[][][] data = new float[2*numSlices+1][NOffset*2+1][NOffset*2+1];
      
      int data_row;
      int data_col;
      int data_chan;
      float[] ys;
      for( int t = -numSlices; t <= numSlices; t++ )
         for( int r = -NOffset; r <= NOffset; r++ )
            for( int c = -NOffset; c <= NOffset; c++ ){
              data_col  = (int)(c+pk1.x());    
              data_row  = (int)(r+pk1.y());    
              data_chan = (int)(t+pk1.z());    
              if( data_chan < 0                   || 
                  data_row  < 1                   || 
                  data_row  > gridSave.num_rows() || 
                  data_col  < 1                   ||
                  data_col  > gridSave.num_cols()    )
                data[t+numSlices][r+NOffset][c+NOffset] = 0;
              else   
              {
                ys = gridSave.getData_entry(data_row, data_col).getY_values();
                if ( ys == null || data_chan > ys.length - 1 )
                  data[t+numSlices][r+NOffset][c+NOffset] = 0;
                else
                  data[t+numSlices][r+NOffset][c+NOffset] = ys[data_chan];
              }
            }

     String name ="" + (seq+1) + ": " + (int)pk1.x() + 
                                 ", " + (int)pk1.y() + 
                                 ", " + (int)pk1.z();

     boolean valid = ( pk1.reflag() <= 11 );
     if ( !valid )                                     // mark invalid peaks
       name = "-" + name;                              // with "-" even if
                                                       // we display them.
//   System.out.println( "reflag = " + pk1.reflag() );
     return new PeakDisplayInfo( name,
                                 data, 
                                 (int)pk1.y()-NOffset,
                                 (int)pk1.x()-NOffset,
                                 (int)pk1.z()-2, 
                                 valid );     
   }
   
   //Uses the old centroid peak
   private static IPeak CentroidPeak( IPeak Pk, DataSet DS, IDataGrid grid,
                  float Max_dSpacing, float pixelW_H, int min_time_chan,
                  int max_time_chan , int[] RowColRange ){
      
     return DataSetTools.operator.Generic.TOF_SCD.Util.centroid( Pk, DS, grid );
   } 
   
   
   //Uses the experimental Centroid that can span more cells
   private static IPeak CentroidPeak1( IPeak Pk, DataSet DS, IDataGrid grid, 
                      float Max_dSpacing, float pixelW_H, int min_time_chan, 
                      int max_time_chan , int[] RowColRange ){
      
      float[] Centroid = new float[ 3 ];
      Arrays.fill( Centroid , -1f );
      
      int reflag = Pk.reflag();
      reflag = ( reflag/100 ) * 100 + reflag % 10;
      
      Vector3D pos = grid.position( Pk.y() , Pk.x() );
      float Q = ( new Vector3D( Pk.getQ() ) ).length();
      
      Data D = grid.getData_entry( (int)( Pk.y() ) , (int)( Pk.x() ) );
      XScale xscl = D.getX_scale();
     
      float dT_Chan = xscl.getX( (int) Pk.z()+1 ) - xscl.getX( (int)Pk.z() );
      
      int PDelta =(int)( .5+ DataSetTools.operator.Generic.TOF_SCD.Util.dPixel( 
               .1f/Max_dSpacing , Q, 
               ( new DetectorPosition( pos ) ).getScatteringAngle()
                , pos.length() , pixelW_H ) );
      
      int DtimeChan = (int)( .5+DataSetTools.operator.Generic.TOF_SCD.Util.
          dTChan( .1f/Max_dSpacing , Q , Pk.time() , dT_Chan ) );
      
      PDelta = Math.min( Math.max( 3 , PDelta ), 
           Math.max( 3 , Math.min( grid.num_rows() , grid.num_cols() )/10 ) );
      
      DtimeChan = Math.max( 1 , Math.min( DtimeChan, xscl.getNum_x()/10 ) );
      
      if( max_time_chan <= min_time_chan || 
                           ( max_time_chan > xscl.getNum_x() - 1 ) )
         
         max_time_chan = xscl.getNum_x() - 1;
      
      int Six = Math.max( PDelta , DtimeChan );  
    
   
      if( !NearEdge( PDelta, DtimeChan, Pk, RowColRange,  min_time_chan,
                                                      max_time_chan ) )
         Operators.TOF_SCD.GetCentroidPeaks1.CentroidPeakDS(  DS ,  
                    grid.ID() , (int)( Pk.y()+.5 ) , (int)( Pk.x()+.5 ) , 
                    (int )( Pk.z()+.5 ) , Centroid , Six );
      
      else
         Pk.reflag( reflag+20 );
     
      if( Centroid[ 0 ] > 0 &&Centroid[ 1 ] > 0 &&Centroid[ 2 ] >= min_time_chan &&
          Centroid[ 0 ] <= grid.num_rows() &&Centroid[ 1  ] <= grid.num_cols()
          &&Centroid[ 2 ] <= max_time_chan ){
      
            float tof = xscl.getInterpolatedX( Centroid[ 2 ] );
            IPeak Pk1 = Pk.createNewPeakxyz( Centroid[ 1 ], 
                                             Centroid[ 0 ],
                                             Centroid[ 2 ],
                                             tof );
           
            Pk1.reflag( 10 + reflag );
            return Pk1;
            
      }else{
        
         Pk.reflag( reflag + 20 );
         return Pk;
         
      }
   }
   
   
   //Determines whether a peaks is too near the edge
   private static boolean NearEdge( int PDelta, int DtimeChan,
             IPeak Pk, int[]RowColRange, int minTchan, int maxTchan ){
      
      if( Pk.x() - RowColRange[ 2 ] - PDelta < 0 )
         return true;
      
      if( Pk.y() - RowColRange[ 0 ] - PDelta < 0 )
         return true;
      
      if( RowColRange[ 3 ] - Pk.x() - PDelta < 0 )
         return true;
      
      if( RowColRange[ 1 ] - Pk.y() - PDelta < 0 )
         return true;
      
      if( Pk.z() - DtimeChan  - minTchan < 0 )
         return true;
      
      if(maxTchan  - Pk.z() - DtimeChan < 0 )
         return true;
      
      return false;
      
   }
   
   
   
   //Eliminate peaks whose centroid is 
   private static void EliminateDuplicatePeaks( Vector Peaks ){
      
    if( Peaks == null || Peaks.size() < 1)
       return;
    boolean done = false;
    
    for( int i = 0 ; !done ; i++ ){
       
       IPeak Pk = ( IPeak )Peaks.elementAt( i );
       for( int j = Peaks.size() - 1 ; j > i ; j-- ){
          
          IPeak Pk1 = ( IPeak )Peaks.elementAt( j );
          if( Pk1.x() < Pk.x() - 2  || Pk1.x() > Pk.x() + 2||
              Pk1.y() < Pk.y() - 2  || Pk1.y() > Pk.y() + 2||
              Pk1.z() < Pk.z() - 2  || Pk1.z() > Pk.z() + 2
                   )
           {}
          else
             Peaks.remove( j );
       }
       
      if( i + 1 >= Peaks.size() )
         done = true;
    }
      
   }
   
   
   public static int getMonCount( Retriever retriever, int dsnum ){
      
      int monCount = 10000;
      int ID = -1;
      DataSet Monitor = null;
      
      if( retriever.getType( 0 )== Retriever.MONITOR_DATA_SET ){
         
         Monitor = retriever.getDataSet( 0 );
         ID = MonitorID_Calc.UpstreamMonitorID( Monitor );
         
         if( ID >= 0 ){
            
            Object Result = ( new IntegrateGroup( Monitor, ID, 0, 50000 ) ).getResult();
            if( Result instanceof java.lang.Number )
              monCount = ( ( Number )Result ).intValue();
            
         }
            
      }
      return monCount;
   }

    
   /**
    * For each detector in multiple files, find theoretical positions of peaks
    * and integrates them.
    * The matrix files are stored in outpath +"ls"+expName+runnum+".mat"
    * 
    * @param path                 The path where the multiple data set files 
    *                                are stored 
    * @param outpath              The path where all the outputs go
    * @param run_numbers          The Run numbers of the data set files
    * @param DataSetNums          The data set numbers in a file to "integrate" 
    * @param expname              The name of the experiment
    * @param centeringName        The centering type:primitive,a centered,
    *                                b centered,c centered, [f]ace centered,
    *                                [i] body centered,[r]hombohedral centered
    * @param useCalibFile         Calibrate the data sets(yes/no)
    * @param calibfile            The calibration file used to calibrate the 
    *                               data sets
    * @param line2use             The line in the calibration file to use
    * @param time_slice_range     Time-slice range around peak center
    * @param increase             Increment slice size by          
    * @param inst                 Instrument name(Prefix after path for a file)
    * @param FileExt              Extension for filename
    * @param d_min                minimum d-spacing to consider
    * @param PeakAlg              Peak Algorithm:MaxIToSigI,Shoe Box, 
    *                                MaxIToSigI-old,TOFINT,or EXPERIMENTAL
    * @param Xrange               Range of offsets around a peak's 
    *                                  x value(-1:3)
    * @param Yrange               Range of offsets around a peak's
    *                                  y value(-1:3)
    * @param maxThreads           The maximum number of threads to run
    * @param ShowLog              Pop up the log file
    * @param ShowPeaks            Pop up the Peaks file
    * @return  nothing though a .integrate and a .log file are created.
    */
   public static Object IntegrateMultipleRuns(
           String  path,
           String  outpath,
           Vector  run_numbers, 
           String  DataSetNums,
           String  expname,
           String  centeringName,
           boolean useCalibFile,
           String  calibfile,
           int     line2use,
           String  time_slice_range,
           int     increase,
           String  inst,
           String  FileExt,
           float   d_min,
           String  PeakAlg,
           String  Xrange,
           String  Yrange,
           int     maxThreads,
           boolean ShowLog,
           boolean ShowPeaks
            ){
      boolean useCache = false;
      SharedMessages.addmsg( "Instrument = " + inst );
      
      if( run_numbers == null )
         return new ErrorString( "No run numbers to integrate" );
      
      int[] Runs = new int[ run_numbers.size() ];
      
      try {

         for( int i = 0 ; i < run_numbers.size() ; i++ )
            Runs[ i ] = ( (Integer) run_numbers.elementAt( i ) ).intValue();
         
      }catch( Exception s ) {
         
         return new ErrorString( "Improper format for run numbers" );
      }
      
      java.util.Arrays.sort( Runs );
      
      int[] DSnums = IntList.ToArray( DataSetNums );
      
      Vector<StringBuffer> LogInfo = new Vector<StringBuffer>();
      
      Vector<OperatorThread> operators = new Vector<OperatorThread>();
      
      if( !FileExt.startsWith( "."  ) )
         FileExt = "." + FileExt;
      
      if( Runs == null || DSnums == null )
         return new ErrorString( "No Data Sets to process" );
      
      Vector Peaks = new Vector();
      int[] timeZrange = getMaxMin( time_slice_range );
      int[] colXrange  = getMaxMin( Xrange );
      int[] rowYrange =  getMaxMin( Yrange );
      int centering = getCenterIndex( centeringName );
      
      if( timeZrange == null || colXrange == null || rowYrange == null )
         return new ErrorString( "x,y, or time offsets not set" );
      

      String cacheFilename = gov.anl.ipns.Util.File.FileIO.CreateDistinctFileName(  
                System.getProperty( "user.home" ), "ISAW/localCache" , ".txt" , 20 );
      if( !useCache)
         cacheFilename = null;
      for( int runIndx = 0 ; runIndx < Runs.length ; runIndx++ ){
         
         String filename = path + inst + Runs[ runIndx ] + FileExt;
         SharedMessages.addmsg( "Loading " + filename );
         
         System.out.println( "Integrating peaks in " + filename );
         int dsIndx = -1;
         Retriever retriever = null;
         try{
            
           retriever = Command.ScriptUtil.getRetriever( filename );

           if( cacheFilename != null)
           if( runIndx == 0 && retriever instanceof NexusRetriever )
              ((NexusRetriever)retriever).SaveSetUpInfo( cacheFilename );
           else
              ((NexusRetriever)retriever).RetrieveSetUpInfo( cacheFilename );
           int monCount = getMonCount( retriever , 0 );
           String matFileName = outpath + "ls" + expname + Runs[ runIndx ] + ".mat";
           
           for( dsIndx = 0 ; dsIndx < DSnums.length ; dsIndx++ ){
              
              DataSet ds = retriever.getDataSet( DSnums[ dsIndx ] );
              
              if( ds == null ){
                 return new ErrorString( "Error in retrieving " + filename +  
                          "dataset num " + DSnums[ dsIndx ] );
              }
              
              if( useCalibFile )
                 Calibrate( ds, calibfile, line2use );
              
              Object Result = ( new LoadOrientation( ds, matFileName ) ).
                                                             getResult();
              if( Result != null && Result instanceof gov.anl.ipns.Util.
                                  SpecialStrings.ErrorString )
                 return Result;
              
              Object Res = RunOperators( operators, Peaks, maxThreads );
              
              if( Res != null && Res instanceof ErrorString )
                 return Res;
              
              StringBuffer sbuff = new StringBuffer();
              
              OperatorThread opThrd = getIntegOpThread( ds, centering,
                       timeZrange, increase, d_min, 1, PeakAlg, colXrange,
                       rowYrange, monCount, sbuff );
              
              opThrd.setName( filename + " ds num=" + DSnums[ dsIndx ] );//For error reporting
              
              LogInfo.addElement(  sbuff );
              
              operators.addElement( opThrd );
              
              opThrd.start();
           }//fro dsIndex
           

           if( retriever != null)
              if( retriever instanceof NexusRetriever) 
                ((NexusRetriever)retriever).close();
         }catch( Exception ss ){
            
            SharedMessages.addmsg( "Error in retrieving " + filename + "::" + ss );
            String S = "";
            
            if( dsIndx >= 0 )
               S = " data set num=" + DSnums[ dsIndx ];
            
            return new gov.anl.ipns.Util.SpecialStrings.ErrorString(
                                       "Error in retrieving " + filename +  S );
         }
      }
      
      
      Object Res = RunOperators( operators, Peaks, 1 ); 
      
      if( Res != null && Res instanceof ErrorString )
         return Res;
      
      if( operators.size() > 0 )
         Res = RunOperators( operators, Peaks, 1 ); 
      
      if( Res != null && Res instanceof ErrorString )
         return Res;
      
      if( operators.size() > 0 )
         return new ErrorString( " timeout for " + operators.size() + " threads" );
      
      SortUnPackFix( Peaks );
      
      String logFile = outpath + "integrate.log";
      java.io.FileOutputStream fout = null;
      
      try{
         
        fout = new java.io.FileOutputStream( logFile );
        for( int i = 0 ; i < LogInfo.size() ; i++ ){
           
           if( LogInfo.elementAt( i ) != null )
             fout.write( LogInfo.elementAt( i ).toString().getBytes()  );
           
        }
        fout.flush();
        fout.close();
      
      }catch( Exception s ){
         
         SharedMessages.addmsg( "Could not write out the integrate.log file" );
      }

      int n_peaks = Peaks.size();           // Only use peaks with reflag = 10
      Peak_new peak;
      Vector filtered_peaks = new Vector();
      for (int i = 0; i < n_peaks; i++ )
      {
        peak = (Peak_new)Peaks.elementAt(i);
        if ( peak.reflag() == 10 )
          filtered_peaks.add( peak );
      }
      Peaks = filtered_peaks;
      
      String integfile = outpath + expname + ".integrate";
      if( cacheFilename != null )
      ( new File( cacheFilename)).delete();
      try
      {
        Peak_new_IO.WritePeaks_new(integfile, Peaks, false);
      }
      catch (IOException ex )
      {
        return new ErrorString("Could not write integrate file " + integfile );
      }

//      WritePeaks writer = new WritePeaks( integfile, Peaks, false );
//      Res =  writer.getResult();
      if( ShowPeaks )
      ( new ViewASCII( outpath + expname + ".integrate" ) ).getResult();

      if( ShowLog )
         
         (new ViewASCII( outpath + "integrate.log" ) ).getResult();
      
      else
         
        SharedMessages.addmsg( "The log file is in integrate.log. Use the" + 
                                                   " View menu to open it" );
      return Res;
   }
   
   //Sets up the operator thread
   private static OperatorThread getIntegOpThread( DataSet ds , int centering ,
            int[] timeZrange , int increase , float d_min , int listNthPeak ,
            String PeakAlg , int[] colXrange , int[] rowYrange ,
            float monCount , StringBuffer sbuff ) {

      integrate Int = new integrate();
      Int.getParameter( 0 ).setValue( ds );
      Int.getParameter( 1 ).setValue( centering );
      Int.getParameter( 2 ).setValue( timeZrange );
      Int.getParameter( 3 ).setValue( increase );
      Int.getParameter( 4 ).setValue( d_min );
      Int.getParameter( 5 ).setValue( listNthPeak );
      Int.getParameter( 6 ).setValue( PeakAlg );
      Int.getParameter( 7 ).setValue( colXrange );
      Int.getParameter( 8 ).setValue( rowYrange );
      Int.getParameter( 9 ).setValue( monCount );
      Int.getParameter( 10 ).setValue( sbuff );
      OperatorThread Res = new OperatorThread( Int );
      
      return Res;

   }
   
   
   
   private static int getCenterIndex( String centeringName ){
      
      for( int i = 0 ; i < 6 ; i++ )
         
         if( CenteringNames[ i ].equals( centeringName ) )
            return i;
      
      return 0;
   }
   
   
   
   // Gets min, max from the string form of a range
   private static int[] getMaxMin( String range ){
      
       int[] list = IntList.ToArray( range );
       if( list == null || list.length < 1 )
          return null;
       
       int[] Res = new int[ 2 ];
       
       Res[ 0 ] = list[ 0 ];
       Res[ 1 ] = list[ list.length - 1 ];
       
       return Res;
   }
   
 
   
   //returns null or ErrorString
   private static Object RunOperators( Vector<OperatorThread> operators, 
                                       Vector Peaks, int maxNumThreads ){
      
      if( operators.size() < maxNumThreads )
         return null;
      
      int timeOut = 300000;
      Vector<Integer> Finished = new Vector<Integer>();
      
      while( timeOut < 1200000 && Finished.size() < 1 ) {
         
         for( int i = 0 ; i < operators.size() ; i++ ) {
            OperatorThread opThrd = operators.elementAt( i );
            
            try {
               opThrd.join( timeOut );
            }
            catch( Exception s ) {
               return new ErrorString( "Thread error for " + opThrd.getName()
                        + "::" + s );
            }
            
            if( opThrd.getState() == Thread.State.TERMINATED ) {
               
               Object Res = opThrd.getResult();
               
               if( Res instanceof Vector )
                  
                  Peaks.addElement(  Res );
               
               else if( Res instanceof ErrorString )
                  
                  return Res;
               
               else
                  
                  return new ErrorString( "Thread " + opThrd.getName()
                           + " did not finish-->" + Res );
               
               Finished.addElement( i );
            }

         }//for
         
         timeOut += 300000;
      }//while
      
      
      for( int i = Finished.size() - 1 ; i >= 0 ; i-- )
         
         operators.remove(  Finished.elementAt( i ).intValue() );
      
      return null;
   }
}
