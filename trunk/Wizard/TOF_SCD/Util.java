/*
 *File:  Util.java
 *
 * Copyright (C) Ruth Mikkelson 2008
 *
 * This program is free software; you can redistribute it and/or 
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 *Contact:Ruth Mikkelson,mikkelsonr@uwstout.edu
 *        Menomonie, WI 54751
 *
 *This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA. 
 
 * Modified:   Log:DailyPeaksWizard_new.java, v$
*/


package Wizard.TOF_SCD;
import DataSetTools.dataset.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.operator.*;
import java.util.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Numeric.*;
import DataSetTools.retriever.*;
import Operators.Special.Calib;
import gov.anl.ipns.Operator.Threads.*;
import gov.anl.ipns.Util.Sys.*;
import DataSetTools.operator.DataSet.Math.Analyze.IntegrateGroup;
import DataSetTools.operator.Generic.Special.*;
/**
 * This class contains an assortment of Utility methods for TOF_SCD wizards
 */
public class Util {
   //TODO need to load in a peaks file first 
   //Possible option on append: choicelist, append/load/neither
   //TODO: fix the order of the output to the peaks file and the
   //      peaks in the form that uses the ParallelExecutor.
   //Results(parallel)               1proc         2 proc
   //  with Parallel Executor       14 sec          10 sec
   //  w.o. Parallel Executor       13 sec          12 sec
   //
   //Conclusion: w. Parallel  Executor did seq reading first then  
   //            parallel calc after reading was through.
   //            w.o. Parallel Executor interspersed reading and
   //            calculations(in threads). Seems like I/O tied up
   //            one processor with minimal switching for idle time
   //            If have 4 processors, may get better results with
   //            the w.o. Parallel Executor
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
    * @param PixelRow       The Row/Col values to keep. Blank for all
    * @param extension      The name of the extension on the data file
    * @param fileNamePrefix The prefix for the filename.
    * @param maxNumThreads  The maximum number of threads to execute at one time
    * @return a Vector of peaks, Grouped by detector
    */
   public static Vector<IPeak> findCentroidedPeaks( 
            String   rawpath, 
            String   outpath, 
            String   runnums, 
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
            String   PixelRow,
            String   extension,
            String   fileNamePrefix,
            int      maxNumThreads){
      
      int[] Runs = IntList.ToArray( runnums );
      int[] DSnums = IntList.ToArray( dataSetNums );
      if( !extension.startsWith( "." ))
         extension="."+extension;
      if( Runs == null || DSnums == null)
         return null;
      
      if( useCalib)
         SharedMessages.addmsg( "Calibration File ="+calibfile );
      
      Vector operators = new Vector();
      Vector ResultPeaks = new Vector();
      
      String PeakFileName = outpath+expname+".peaks";
      String ExpFileName  = outpath+expname+".x";
      
      if( append )if( !(new java.io.File(PeakFileName)).exists() ||
               !(new java.io.File(ExpFileName)).exists())
         append = false;
      boolean append1 = false || append;
      
      for( int i=0; i< Runs.length; i++){
         
         String filename =rawpath+fileNamePrefix+Runs[i]+extension;
         
         try{
           Retriever retriever = Command.ScriptUtil.getRetriever( filename );
           
           //------------Get monitor count ---------------------
           int monCount = 10000;
           int ID = -1;
           DataSet Monitor = null;
           if( retriever.getType( 0 )== Retriever.MONITOR_DATA_SET){
              Monitor = retriever.getDataSet( 0 );
              ID = MonitorID_Calc.UpstreamMonitorID(Monitor );
              if( ID >= 0){
                 Object Result= (new IntegrateGroup( Monitor,ID,0,50000)).getResult();
                 if( Result instanceof java.lang.Number)
                   monCount =((Number)Result).intValue();
              }
                 
           }
           //--------------- For each data set in a run---------------------
           SharedMessages.addmsg( "Loading "+filename );
           for( int j=0; j< DSnums.length; j++){
              
              DataSet DS = retriever.getDataSet( DSnums[j] );
              if( DS == null){
                 javax.swing.JOptionPane.showMessageDialog( null , "Could not retrieve "+filename+
                                                         " dataset "+DSnums[j] );
              }else {
                 
                 if( useCalib)
                    Calibrate( DS, calibfile, line2use);
                 
                 if( AttrUtil.getUser( DS )== null)
                     DS.setAttribute( new StringAttribute(Attribute.USER,"George User") );
                 
                 //----------------- For each grid in the data set ----------------
                 java.util.Hashtable gridIDs=Grid_util.getAllDataGrids( DS );
                 if( gridIDs == null)
                    javax.swing.JOptionPane.showMessageDialog( null , 
                             "Grids not set up for "+filename+ " dataset "
                             +DSnums[j] );
                 else{
                    Enumeration keys = gridIDs.keys();
                    
                    MergeInfo(keys,  DS,   num_peaks, min_int, min_time_chan,
                             max_time_chan,PixelRow, monCount, operators, 
                             ResultPeaks,  maxNumThreads);
                    }//for enumeration
                    
                 }//else gridIds == null
              
                 (new WriteExp( DS, Monitor, ExpFileName ,ID,append1 )).getResult();
                 append1=true;
                 
              }//else DS == null
           
         }catch(Exception s){
           SharedMessages.addmsg( "Could not retrieve "+filename );
         }
      }//for @ run
   
    /*//Uses parallel Executor. Requires reading in files first
      // Then calculations are done in parallel
        if(operators.size()>0){//Have some operators not run?

         ParallelExecutor PE = new ParallelExecutor(
                           operators,maxNumThreads,3000);
         Vector Results = PE.runOperators();
         int seqNum = ResultPeaks.size()+1;
         if(Results != null)
            for( int kk=0; kk< Results.size(); kk++){
               Object R = Results.elementAt( kk );
               if( R != null && R instanceof IPeak){
                  ((IPeak)R).seqnum( seqNum );
                  ResultPeaks.addElement( R);
                  seqNum++;
              }
            
            }
       }
    */// does not use Parallel Executor.  
       long timeOut =30000;
       while( operators.size() > 0 && timeOut < 180000){
          for( int i=operators.size()-1; i>=0; i-- ){
             OperatorThread opThreadElt =(OperatorThread)(operators.elementAt( i ));
             try{
                opThreadElt.join(timeOut);
                if( opThreadElt.getState() == Thread.State.TERMINATED){
                   operators.remove( i );
                   Object Res =opThreadElt.getResult() ;
                   if( Res != null && Res instanceof Vector)
                       ResultPeaks.addElement( Res  );
                }
             }catch(InterruptedException ss){
                //Do nothing. Next time join should return and you have a terminated thread
             }
             
             
          }
          timeOut = 2*timeOut;
       }
       //ToDo  get more info on operators
       // operators Vector could take an Array with more info( run num, detector num
       if( timeOut >= 180000)
          SharedMessages.addmsg( operators.size()+" detectors did not finish in over 1 minute" );
       SortUnPackFix(ResultPeaks);
      //----------- Write and View Peaks --------------------
      (new WritePeaks( PeakFileName, ResultPeaks,append)).getResult();
      System.out.println("--- find_multiple_peaks is done. ---");
      SharedMessages.addmsg( "Peaks are listed in "+PeakFileName );
      (new ViewASCII(PeakFileName)).getResult();
    
      return ResultPeaks;
   }
   
   //ResultPeaks is a Vector of Vector of Peaks
   // Sort according to run number and detectorID
   // Unpack means to make into a Vector of Peaks
   // add sequence numbers
   private static void SortUnPackFix( Vector ResultPeaks){
      if(ResultPeaks == null)
         return;
      
      Integer[] rankSort = new Integer[ResultPeaks.size()];
      for( int i=0; i< ResultPeaks.size(); i++)
         rankSort[i]=i;
      java.util.Arrays.sort(  rankSort, new VComparator(ResultPeaks) );
      Vector V = new Vector( ResultPeaks.size());
      for( int i=0; i< rankSort.length; i++)
         V.addElement(  ResultPeaks.elementAt(rankSort[i]) );
      ResultPeaks.clear();
      int seqNum=1;
      for( int i=0; i< V.size(); i++){
         Vector V1 =(Vector)(V.elementAt( i ));
         for( int j=0; j< V1.size(); j++){
            Object R = V1.elementAt(j);
            if( R != null && R instanceof IPeak){
               ((IPeak)R).seqnum( seqNum );
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
      public VComparator( Vector ResultPeaks){
         this.ResultPeaks = ResultPeaks;
      }
      /* (non-Javadoc)
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      @Override
      public int compare( Integer o1 , Integer o2 ) {
         int i1 = o1.intValue();
         int i2 = o2.intValue();
         if( i1 == i2)
            return 0;
         if( i1 < 0 ||i1>= ResultPeaks.size())
            if( i2<0 || i2 >= ResultPeaks.size())
               return 0;
            else
               return 1;
         
         if( i2 < 0 ||i2>= ResultPeaks.size())
            if( i1<0 || i1 >= ResultPeaks.size())
               return 0;
            else
               return -1;
         
         Object R1 = ResultPeaks.elementAt(i1);
         Object R2 = ResultPeaks.elementAt(i2);
         if( R1 == null ||  !(R1 instanceof Vector)
                  || ((Vector)R1).size() < 1)
            if( R2 == null ||  !(R2 instanceof Vector)
                     || ((Vector)R2).size() < 1)
               return 0;
            else 
               return 1;
         

         if( R2 == null ||  !(R2 instanceof Vector)
                  || ((Vector)R2).size() < 1)
            if( R1 == null ||  !(R1 instanceof Vector)
                     || ((Vector)R1).size() < 1)
               return 0;
            else 
               return -1;
         
                     
         Object pk1 = ((Vector)R1).elementAt( 0 );
         Object pk2 =((Vector)R2).elementAt( 0 );
         if( pk1 == null || !(pk1 instanceof IPeak))
            if( pk2 == null || !(pk2 instanceof IPeak))
               return 0;
            else
               return -1;
         

         if( pk2 == null || !(pk2 instanceof IPeak))
            if( pk1 == null || !(pk1 instanceof IPeak))
               return 0;
            else
               return 1;
         
         IPeak peak1 = (IPeak)pk1;
         IPeak peak2 = (IPeak)pk2;
         if( peak1.nrun() < peak2.nrun())
            return -1;
         if( peak1.nrun() > peak2.nrun())
            return 1;
         if( peak1.detnum()< peak2.detnum())
            return -1;
         if( peak1.detnum()> peak2.detnum())
            return 1;
         
         
         return 0;
      }
       
   }
   // Sets up the calibrate operator(Calib) and executes it
   private static void Calibrate( DataSet DS, String calibFileName, 
                                                          int lineNum){
      Calib calib= new Calib();
      calib.DS = DS;
      calib.CalibFile1 = new LoadFileString( calibFileName);
      Vector V = new Vector();
      V.add( lineNum );
      calib.otherInformation = V;
      Object Result = calib.calculate();
      if(Result instanceof ErrorString)
         SharedMessages.addmsg( "Error calibrating "+ DS +"::"+Result );
      
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
            int         monCount,
            Vector      operators, 
            Vector      ResultPeaks,
            int         maxNumThreads){
      
      if( keys == null || DS == null || num_peaks <=0)
         return;
      
      for(;keys.hasMoreElements();){
         
         Object K = keys.nextElement();

         if( K != null  && K instanceof Integer){
            
            operators.addElement( SetUpfindPeaksOp(DS,
                         ((Integer)K).intValue(),num_peaks, 
                               min_int,  min_time_chan, max_time_chan,
                               PixelRow, monCount));
           
            if( operators.size() >= maxNumThreads){
               ParallelExecutor PE = new ParallelExecutor(
                                  operators,maxNumThreads,30000*maxNumThreads);
               
               Vector Results = null;
               try{
                   Results = PE.runOperators();
               }catch( ExecFailException ss){
                  Results = ss.getPartialResults();
                  SharedMessages.addmsg( "Timeout error:"+ss );
               }catch(Throwable s1){
                  SharedMessages.addmsg( "Execute error:"+s1 );
               }
               //-------- Set sequence numbers-------
               int seqNum = ResultPeaks.size()+1;
               if(Results != null)
                  for( int kk=0; kk< operators.size(); kk++){
                     
                     Object RR = Results.elementAt( kk );
                     if( RR != null && RR instanceof Vector){
                        for( int p=0; p < ((Vector)RR).size();p++){
                           
                           IPeak R =(IPeak)((Vector)RR).elementAt(p);
                           R.seqnum( seqNum );
                           ResultPeaks.addElement( R);
                           seqNum++;
                        }
                     }
                  }//For each operator

               operators.removeAllElements();

            }//if operators.size() >= maxNumThreads
         }
      }
   }
   
   // Calculates Peaks and adds them to the ResultPeaks. 
  // Does NOT use the Parallel Executor
   // ResultantPeaks is a Vector of Vector of Peaks where each
   //    Vector of Peaks is from one detector on one run
   private static void MergeInfo(
            Enumeration keys,
            DataSet     DS, 
            int         num_peaks, 
            int         min_int, 
            int         min_time_chan,
            int         max_time_chan,
            String      PixelRow,
            int         monCount,
            Vector      operators, 
            Vector      ResultPeaks,
            int         maxNumThreads){
      
      if( keys == null || DS == null || num_peaks <=0)
         return;
      
      for(;keys.hasMoreElements();){
         
         Object K = keys.nextElement();

         if( K != null  && K instanceof Integer){
            
            
            int timeOut = 30000;
            while( operators.size() >= maxNumThreads  && timeOut < 180000){
               boolean done = false;
               for( int i=operators.size()-1; i>=0 && !done; i--){
                  OperatorThread opThreadelt = 
                                  (OperatorThread)(operators.elementAt(i));
                  try{
                    opThreadelt.join(timeOut);
                    if( opThreadelt.getState() == Thread.State.TERMINATED){
                       Object Res = opThreadelt.getResult();
                       if( Res != null && Res instanceof Vector)
                          ResultPeaks.addElement(  Res );
                       operators.remove(  i);
                       done = true;
                    }
                  }catch(Exception ss){
                     done = false;
                  }
                  
               }
               timeOut = 2*timeOut;
            }//while operators.size() >= maxNumThreads
            if( timeOut >= 180000){
               System.out.println("TimeOut problem. Several Threads are hung");
            }
            OperatorThread opThread = new OperatorThread( SetUpfindPeaksOp(DS,
                         ((Integer)K).intValue(),num_peaks, 
                               min_int,  min_time_chan, max_time_chan,
                               PixelRow, monCount));
            opThread.start();
            operators.addElement(  opThread );
         }
      }
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
            int     monCount){
      
          findDetectorCentroidedPeaks op = new findDetectorCentroidedPeaks();
          op.getParameter( 0 ).setValue( DS );
          op.getParameter( 1 ).setValue( DetectorID );
          op.getParameter( 2 ).setValue( num_peaks );
          op.getParameter( 3 ).setValue( min_int );
          op.getParameter( 4 ).setValue( min_time_chan );
          op.getParameter( 5 ).setValue( max_time_chan );
          op.getParameter( 6 ).setValue( PixelRow );
          op.getParameter( 7 ).setValue( monCount );
          
          return op;
   }
   
   
   
   /**
    * Finds all Peaks on one detector
    * @param DataSet        The data set with the detector
    * @param DetectorID     The detectorID
    * @param num_peaks      The maximum number of peaks to return.
    * @param min_int        The minimum peak intensity to look for.
    * @param min_time_chan  The minimum time channel to use.
    * @param max_time_chan  The maximum time channel to use.
    * @param PixelRow       The row/col to keep
    * @param monCount       Monitor Count
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
            int     monCount){

      IDataGrid grid = Grid_util.getAreaGrid( DS , DetectorID );
      if( grid == null)
         return null;
      if( DS.getNum_entries()<1)
         return null;
      
      //------------------ Find Peaks ------------------
      Vector Pks = FindPeaks.findDetectorPeaks( DS , DetectorID , min_time_chan ,
               max_time_chan , num_peaks , min_int , PixelRow );
      
      
      if(Pks == null || Pks.size() < 1)
         return Pks;
      
      //---------------Centroid Peaks ---------------------------
      for( int i=0; i< Pks.size(); i++){
         IPeak Pk =(IPeak)(Pks.elementAt( i ));
         Pk = DataSetTools.operator.Generic.TOF_SCD.Util.centroid( Pk , DS , grid );
         Pks.set( i,Pk);
      }
      
      //------------------ Replace IPeak by Peak_new ----------------------
      XScale xscl = grid.getData_entry( 1,1 ).getX_scale();
      DataSetTools.instruments.SampleOrientation sampOrient = AttrUtil.getSampleOrientation( DS );
      float InitialPath = AttrUtil.getInitialPath( DS.getData_entry( 0 ) );
      float T0 = AttrUtil.getT0Shift( DS.getData_entry( 0 ) );
      if( Float.isNaN( T0 ))
         T0 = 0;
      //Now get a Peak_new
      if( grid instanceof RowColGrid){
         IDataGrid grid1 = RowColGrid.getUniformDataGrid((RowColGrid) grid , .001f);
         if( grid1 == null)
            throw new IllegalArgumentException( "grid "+grid.ID()+" not uniform enough");
         grid = grid1;  
        
      }
      grid.clearData_entries(); 
     

      Vector<IPeak> ResultantPeak= new Vector<IPeak>( Pks.size());
      for( int i=0; i< Pks.size(); i++){
         IPeak pk1 = (IPeak)Pks.elementAt( i );
         
         Peak_new pk = new Peak_new( pk1.x(), pk1.y(),pk1.z(),grid, sampOrient, T0,
                  xscl, InitialPath);
         pk.ipkobs( pk1.ipkobs() );
         pk.inti( pk1.inti() );
         pk.sigi(pk1.sigi() );
         pk.reflag(pk1.reflag());
         pk.monct( monCount );
         pk.nrun( pk1.nrun() );
         ResultantPeak.add(  pk );
         
      }
      return ResultantPeak;
   }
           
}
