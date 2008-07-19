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
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Operator.Threads.*;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.MathTools.Geometry.*;


/**
 * This class contains an assortment of Utility methods for TOF_SCD wizards
 */
public class Util {
   
   public static final String[] CenteringNames = {"primitive" , "a centered" ,
              "b centered" , "c centered" , "[f]ace centered",
            "[i] body centered" , "[r]hombohedral centered"};
   
   //TODO need to load in a peaks file first 
   //Possible option on append: choicelist, append/load/neither
   //
   //Results(parallel)               1 threads          2 threads
   //  with Parallel Executor       14.5 sec        12 sec
   //  w.o. Parallel Executor       13 sec          10 sec
   //                    
   //Conclusion: w. Parallel  Executor did seq reading first then  
   //            parallel calc after reading was through.
   //            w.o. Parallel Executor interspersed reading and
   //            calculations(in threads).  Essential to eliminate 
   //            ALL I/O in the threads.  
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
    * @param extension      The name of the extension on the data file
    * @param fileNamePrefix The prefix for the filename.
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
            String   extension,
            String   fileNamePrefix,
            boolean  ViewPeaks,
            int      maxNumThreads )  throws IOException
     {
      
      if( runnums == null )
         return null;
      min_row =Math.max( 1,  min_row );
      max_row = Math.max(  max_row ,  min_row );
      min_col =Math.max( 1,  min_row );
      max_col = Math.max(  max_col ,  min_col );
      String PixelRow = min_row+":"+max_row;
      String PixelCol = min_col+":"+max_col;
      int[] Runs = new int[ runnums.size() ];
      
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
      String ExpFileName  = outpath+expname+".x";
      
      if( append )if( !( new java.io.File( PeakFileName ) ).exists() )
             // || !( new java.io.File( ExpFileName ) ).exists() )
         append = false;
      
      boolean append1 = false || append;
      
      for( int i = 0; i < Runs.length; i++ ){
         
         String filename = rawpath+fileNamePrefix+Runs[ i ]+extension;

         Retriever retriever = null;
         try {
            retriever = Command.ScriptUtil.getRetriever( filename );

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
                              maxNumThreads , Max_dSpacing );
                     
                     if( Err != null ) {
                        SharedMessages.addmsg( "Error in finding peaks "
                                 + Err.toString() );

                     }                    
                  }// for enumeration
               }// else gridIds == null
                /* Took too long on ARCS DATA Set with 20000 time channels */
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
       
       for( int i = 0 ; i < LogInfo.size() ; i++ )
          SharedMessages.addmsg(  LogInfo.elementAt( i ) );
       
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
                                  (Vector<Peak_new>)ResultPeaks, 
                                   append1 );

      //----------- Write and View Peaks --------------------
      // (new WritePeaks( PeakFileName, ResultPeaks, append1 ) ).getResult();
      
      System.out.println( "--- find_multiple_peaks is done. ---" );
      SharedMessages.addmsg( "Peaks are listed in "+PeakFileName );
      
      if( ViewPeaks )
         ( new ViewASCII( PeakFileName ) ).getResult();
 
      return ResultPeaks;
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
   // Sets up the calibrate operator(Calib) and executes it
   private static void Calibrate( DataSet DS, String calibFileName, 
                                                          int lineNum ){
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
                               PixelRow, PixelCol,monCount, Sbuff, 12f ) );
           
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
            float         Max_dSpacing ){
      
      if( keys == null || DS == null || num_peaks <= 0 )
         return null;
      
      for( ; keys.hasMoreElements() ; ){
         
         Object K = keys.nextElement();

         if( K != null  && K instanceof Integer ){
            
            Object Error = null;
            int timeOut = 3000;
            
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
                               PixelRow,PixelCol, monCount, Sbuff, Max_dSpacing ) );
            

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
            float   Max_dSpacing ){
          
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
          op.getParameter( 10 ).setValue( buff );
         
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
    * @param buff           If this is a non-null StringBuffer, the log 
    *                           information will be appended to it, otherwise
    *                           the log info will be displayed on the Status
    *                           Pane.
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
      Vector Pks = FindPeaks.findDetectorPeaks( DS, DetectorID, min_time_chan,
               max_time_chan, num_peaks, min_int, PixelRow, PixelCol, buff );
           
      if( Pks == null || Pks.size() < 1 ){
        
         return Pks;
      }
     
      float pixelW_H = Math.max( grid.width()/grid.num_cols() , 
                                 grid.height()/grid.num_rows() );
      if( min_time_chan < 0 )
         min_time_chan = 0;
      
      //---------------Centroid Peaks ---------------------------
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
         IDataGrid grid1 = RowColGrid.getUniformDataGrid( ( RowColGrid ) grid , .001f );
         if( grid1 == null )
            throw new IllegalArgumentException( "grid "+grid.ID()+" not uniform enough" );
         grid = grid1;         
      }
      
      grid = grid.clone();//So do not wipe out other grids.
      grid.clearData_entries(); 
     

      //Convert all Peaks to a Peak_new Object so position info can be determined
      Vector<IPeak> ResultantPeak = new Vector<IPeak>( Pks.size() );
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
         ResultantPeak.add(  pk );
      }   
      
      return ResultantPeak;
   }
   
   
   
   //Uses the old centroid peak
   private static IPeak CentroidPeak( IPeak Pk, DataSet DS, IDataGrid grid,
                  float Max_dSpacing, float pixelW_H, int min_time_chan,
                  int max_time_chan , int[] RowColRange ){
      
      return DataSetTools.operator.Generic.TOF_SCD.Util.centroid( Pk , DS, grid );
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
      
    boolean done = Peaks == null || Peaks.size() < 1;
    
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
      
      
      for( int runIndx = 0 ; runIndx < Runs.length ; runIndx++ ){
         
         String filename = path + inst + Runs[ runIndx ] + FileExt;
         SharedMessages.addmsg( "Loading " + filename );
         
         System.out.println( "Integrating peaks in " + filename );
         int dsIndx = -1;
         Retriever retriever = null;
         try{
            
           retriever = Command.ScriptUtil.getRetriever( filename );
           
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
           
         }catch( Exception ss ){
            
            SharedMessages.addmsg( "Error in retrieving " + filename + "::" + ss );
            String S = "";
            
            if( dsIndx >= 0 )
               S = " data set num=" + DSnums[ dsIndx ];
            
            return new gov.anl.ipns.Util.SpecialStrings.ErrorString(
                                       "Error in retrieving " + filename +  S );
         }
         if( retriever != null)
            if( retriever instanceof NexusRetriever) 
              ((NexusRetriever)retriever).close();
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


      try
      {
        Peak_new_IO.WritePeaks_new(integfile, (Vector<Peak_new>)Peaks, false);
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
