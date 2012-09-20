/* 
 * File: IntegrateNorm.java
 *
 * Copyright (C) 2010, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author:$
 *  $Date:$            
 *  $Rev:$
 */
package Operators.TOF_SCD;
import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Functions.OneVarParameterizedFunction;
import gov.anl.ipns.MathTools.Functions.MarquardtArrayFitter;
import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Parameters.FileChooserPanel;
import gov.anl.ipns.Parameters.FilteredPG_TextField;
import gov.anl.ipns.Parameters.IntegerFilter;
import gov.anl.ipns.Util.Numeric.ClosedInterval;

import java.awt.GridLayout;
import java.io.FileOutputStream;
import java.util.*;

import javax.swing.*;

import Command.ScriptUtil;
import DataSetTools.dataset.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.retriever.NexusRetriever;
/**
 * The main program converts a peaks file to an integrated peaks file, integrated
 * using bivariate normal approximations of the peak in each time slice.
 * 
 *  The span of the peaks in terms of rows, cols, and time are automatically calculated
 *  for each peak based on max distance between peaks in Q, Q , scattering angle, and tof.
 *  
 * @author ruth
 *
 */
public class IntegrateNorm {
  
   public static int IBACK = 0;
   public static int IXMEAN =1;
   public static int IYMEAN =2;
   public static int ITINTENS = 3;
   public static int IVXX =4;
   public static int IVYY = 5;
   public static int IVXY =6;
   public static boolean DEBUG = true;
   public static float NSIGMA =4.1f;//# of sigma around center to use
   
   public static String  TIME ="Time";
   public static String CHANNEL="Channel";
   public static String BACKGROUND="Background";
   public static String  BACKGROUND_ERROR="Background Error";
   public static String FITTEDINTENSITY="FittedIntensity";
   public static String  FITTEDINTENSITY_ERROR="FittedIntensity Error";
   public static String  CENTER_COLUMN="Center Column";
   public static String  CENTER_ROW="Center Row";
   public static String  VAR_ROW="Var row";
   public static String VAR_COL="Var col";
   public static String  COVARIANCE="Covariance";
   public static String  CHI_SQUARED="Chi Squared";
   public static String NCELLS="Ncells";
   public static String  TOTAL_INTENSITY="Total Intensity";
   public static String  TOTAL_EDGE_INTENSITY="Total Edge Intensity";
   public static String  N_EDGE_CELLS="N Edge Cells";
   public static String  ISAWINTENSITY="ISAWIntensity";
   public static String ISAWINTENSITY_ERROR="ISAWIntensity Error";
   public static String USE_SHOE_BOX="Use Shoe Box";
   public static String USED_PEAK="Used Peak";
   public static int HH=-2;
   public static int KK=-6;
   public static int LL = 10;

   /**
    * Integrates the peak by fitting the data to background + Intensity* Normal. The Marquardt
    * algorithm is used for fitting.
    * 
    * @param Peak     The Peak to be integrated. Assumes nearEdge is set
    * @param DS       The DataSet with the peak
    * @param MaxCellEdge  The maximum length of the side of a unit cell in real space.
    *                 NOTE: dQ =1/(6*MaxCellEdge) is used to determine number of time 
    *                 channels, and pixels to consider.
         
    * @param NBoundaryPixels  The number of bad boundary pixels to ignore in fitting
    *                    
    * @param logBuffer  The buffer that will contain the log information.
    *                    
    * @return  an error string or an vector of HashTables with info(see below for keys), Also the Peak object has the inti and sigi fields set.
    * Key names for each element of the returned Hashtable vector are
    *   Time, Channel,Background, Background Error, FittedIntensity, FittedIntensity Error, Center Column, Center Row, Var row, Var col, Covariance, Chi Squared,
    *    Ncells, Total Intensity, Total Edge Intensity, N Edge Cells, 
    *    ISAWIntensity,ISAWIntensity error,Use Shoe Box(1/0 for true/false),Used Peak(1/0 for true false)
    * 
    */
   public static Object IntegratePeak( Peak_new Peak, DataSet DS,float MaxCellEdge ,
         int NBoundaryPixels, StringBuffer logBuffer)
   {    
      float dQ = 1f / MaxCellEdge / 6f;
      IDataGrid grid1 = Peak.getGrid();
      Vector3D pos = grid1.position(Peak.y(), Peak.x());
      
      float Q = (new Vector3D(Peak.getUnrotQ( ))).length( );
      float D = pos.length();
      float scatAng = (new DetectorPosition(pos)).getScatteringAngle();
      float w = Math.min(grid1.width(Peak.y(), Peak.x()), grid1.height(Peak
            .y(), Peak.x()));
      
      int nPixels = (int) (.5 + Util.dPixel(dQ, Q, scatAng, D, w));
      nPixels = Math.max( 8 , nPixels );
   
      float Time = Peak.time( );
      XScale xscl = DS.getData_entry( 0 ).getX_scale( );

      int Chan = xscl.getI_GLB( Time );      
  
      float dT_Chan = xscl.getX( Chan + 1 ) - xscl.getX( Chan );
      
      float xtimes = Math.max( 3 , 2 * Util.dTChan( dQ , Q , Time ,
            dT_Chan ) + 1 );
      int nTimes = ( int ) ( xtimes + .5 ) + 1;
      
      nTimes +=6;
      nPixels+=2;

      nPixels = Math.min( 36 , nPixels );
      nPixels = Math.max(6 , nPixels );
      //nTimes = Math.min( 24, nTimes );
      nTimes = Math.max( 5, nTimes );
      pos = null;
      xscl = null;
      return IntegratePeak(Peak,DS, nPixels, nTimes, Chan, NBoundaryPixels, logBuffer);

   
   }

   
   /**
    * Integrates the peak by fitting the data to background + Intensity* Normal. The Marquardt
    * algorithm is used for fitting.
    * 
    * @param Peak     The Peak to be integrated. Assumes nearEdge is set
    * @param DS       The DataSet with the peak
    * @param nPixels  The total width(height) of the data  to be used for fitting.
    *                  It is assumed that the center is close to the Peak centroid.
    * @param nTimeChans The total number of time channels to consider. This is also centered
    *                    around the time associated with the peak.
    *                    
    * @param NBoundaryPixels  The number of bad boundary pixels to ignore in fitting
    *                    
    * @param logBuffer  The buffer that will contain the log information.
    *                    
    * @return A vector of log info for the time slices used or an error string, Also the Peak object has the inti and sigi fields set.
    *          
    */
   public static Object IntegratePeak( Peak_new     Peak, 
                                       DataSet      DS, 
                                       int          nPixels, 
                                       int          nTimeChans,
                                       int          Chan0,
                                       int          NBoundaryPixels, 
                                       StringBuffer logBuffer)
   {


      Vector< Hashtable<String,Float>> Res = new Vector< Hashtable<String,Float>>();
      Vector< Hashtable<String,Float>> SavLog = new Vector< Hashtable<String,Float>>();
      boolean edge =false;
      String[] paramName5 ={"background","x mean", "y mean",  "Intensity"};
      String[] paramName7 ={"background","x mean", "y mean",  "Intensity",
                   "x Variance","y Variance","Covariance"};
      
      try
      {
         float TotIntensity = 0;
         float TotVariance = 0;
         float TotIntensity1 = 0;
         float TotVariance1 = 0;

         int i = Peak.seqnum( );
         int run = Peak.nrun( );
         int det = Peak.detnum( );
         int nTimes = nTimeChans;
         IDataGrid grid = Peak.getGrid( );         
         if( !grid.isData_entered( ))
            grid.setData_entries( DS );         
         Data D = grid.getData_entry( ( int ) ( .5f + Peak.y( ) ) ,
               ( int ) ( .5f + Peak.x( ) ) );
         
         XScale xscl = D.getX_scale( );
         int Chan = FindMaxChannel( Peak, nTimes,  xscl,Chan0, nPixels,  grid, NBoundaryPixels); 
         
         if( Chan <=0)
            Chan = ( int ) Peak.z( );
       
         if ( IntegrateNorm.DEBUG )
            System.out.println( "Peak num #time channels=" + Peak.seqnum( )
                  + "," + nTimeChans );
        int x=0;
        if( Peak.h()==HH && Peak.k()== KK && Peak.l()==LL)
           x=1;
         try
         {
            logBuffer.append( String.format(
               "\nPeak,run,det=%3d %4d %3d   col,row= %5.2f, %5.2f " + 
                           " hkl=%4d,%4d,%4d\n" ,
               i + 1 , run , det , Peak.x( ) , Peak.y( ) ,( int ) Peak.h( ) , 
               ( int ) Peak.k( ) ,( int ) Peak.l( ) ) );
            
            logBuffer.append( String.format(
                  "   Pixel width %3dchan %4d,, chanMin %4d,chanMax %4d\n" ,
                  nPixels + 1 , Chan + 1 , Chan + 1 - ( nTimes - 1 ) / 2 , Chan
                        + 1 + ( nTimes - 1 ) / 2 ) );
            
            logBuffer.append( "   ----------------Slices --------------------\n" );
            logBuffer.append( 
               ( " chan    back  Intens(P)   mx       my   sigm(Cll) ncells" + 
                   " Intens(Tot) Intens(Tot-back) errI     bk_res     Varx" + 
                   "    Vary        Vxy      errI2\n" ) );

         } catch( Exception ss )
         {

         }
         
         boolean done = false;
         // last y values is at xscl.getNum_x( )-2 on a histogram

       
         int BadEdgeWidth = NBoundaryPixels;
         double I_bErrSv = 0;

         int PrevCentX=(int)(.5+Peak.x());
         int PrevCentY=(int)(.5+Peak.y());
         int PrevCentX0=(int)(.5+Peak.x());
         int PrevCentY0=(int)(.5+Peak.y());
         float time = Float.NaN;
         int nPixelsx=-1,
             nPixelsy=-1;

         double time0;
         double[] LastGoodSavedData = new double[9];
         double[] LastGoodSavedData0 = new double[9];
         Arrays.fill(  LastGoodSavedData , -1 );
         Arrays.fill(  LastGoodSavedData0 , -1 );
   
         boolean MergedLast = false;
         boolean case4 = false;//Merged chan=0 and chan = 1 (dir ==1)
         if( DEBUG)
            System.out.print("    "+ "("+Peak.h()+","+Peak.k()+","+Peak.l()+"):");
         for( int dir =1; dir >=-1; dir -= 2)
         {  
            done = false;
            for( int ichan =0; ichan <=( nTimes - 1 ) / 2 &&!done; ichan++)         
     
         if( ichan==0 && dir < 0)
         {   
            done = false;
            PrevCentX=PrevCentX0;
            PrevCentY=PrevCentY0;
            LastGoodSavedData = LastGoodSavedData0;
            MergedLast = false;
            
         }else if( Chan+dir*ichan < 1 ||Chan+dir*ichan >=xscl.getNum_x()-2)
         {
            done = true;
            
         }else if( AllDataInBadArea(grid,PrevCentY,PrevCentX, nPixels , nPixels ,
                  BadEdgeWidth ))
                  done = true;
         else
         {
            if( ichan >2) case4= false;
            int chan = Chan+dir*ichan;
            int nchan = 1;
    
            if( MergedLast || (LastGoodSavedData[0]>=0 && LastGoodSavedData[0] < 10) )
            { 
              
               int chan1 = chan -dir;
               if( ichan > 1)
                  chan1 -=dir;
               int chan2 = chan1 + dir;
               
               nchan =2;
               if( dir < 0 && TotIntensity <=0 && MergedLast)nchan++;
               
               if( case4) nchan++;
               
               chan = Math.min(  chan1 , chan2 );
               MergedLast = true;
               case4 = false;
            }
            
            int pixWidth = nPixels;
            int pixHeight = nPixels;
            if(nPixelsx > 0 && nPixelsy > 0)
            {
               pixWidth = nPixelsx;
               pixHeight = nPixelsy;
            }
            
            OneSlice slice = new OneSlice( grid , chan ,nchan, PrevCentY,PrevCentX, pixHeight, pixWidth  ,
                 4,paramName5 ,BadEdgeWidth );
            
            double[] params = slice.getAllParameters();
            double Vx = Math.max( params[IVXX],slice.StdDevx*slice.StdDevx);
            double Vy = Math.max( params[IVYY],slice.StdDevy*slice.StdDevy);
            if(nPixelsx <= 0 || nPixelsy <= 0)
            {
   
              nPixelsx =(int)( Math.sqrt(Vx)*NSIGMA);
              nPixelsy =(int)( Math.sqrt(Vy)*NSIGMA);
              nPixelsx = Math.max(nPixelsx, 6) ;
              nPixelsy = Math.max(nPixelsy, 6) ; 
              nPixelsx = Math.min(nPixelsx, 36) ;
              nPixelsy = Math.min(nPixelsy, 36) ;   
            }
            
            float intens =(float)slice.getInitialTotIntensity( );
            float Cx;
            float Cy ; 
            Cx= (float)params[IXMEAN];
            Cy =(float)params[IYMEAN];
           
            if( dir ==1 && ichan==0)
            {
               PrevCentX0=(int)(.5+Cx);
               PrevCentY0=(int)(.5+Cy);
            }
            
            if( intens/4/nPixelsx/nPixelsy*2.355*2.355 >50)//large peaks
            {
            
           
            slice = new OneSlice( grid , chan, nchan , ( int ) ( .5f+ Cy
                            ) , ( int ) ( .5f + Cx
                            ) , nPixelsy ,
                      nPixelsx ,4,paramName5, BadEdgeWidth );

            Vx = Math.max( params[IVXX],slice.StdDevx*slice.StdDevx);
            Vy = Math.max( params[IVYY],slice.StdDevy*slice.StdDevy);
            // Update box sizes with fitted parameters 
            params = slice.getAllParameters();
            
            if( dir==1 && ichan ==0)//first time only
            {
              
               nPixelsx =(int)( Math.sqrt(Vx)*NSIGMA*1.2);
               nPixelsy =(int)( Math.sqrt(Vy)*NSIGMA*1.2);
               nPixelsx = Math.max(nPixelsx, 6) ;
               nPixelsy = Math.max(nPixelsy, 6) ; 
               nPixelsx = Math.min(nPixelsx, 45) ;
               nPixelsy = Math.min(nPixelsy, 45) ; 
               
               edge = isEdge(Cx,Cy,nPixelsx,nPixelsy,NBoundaryPixels,grid, Vx*Vx, Vy*Vy);
              
          
               slice = new OneSlice( grid , chan ,nchan, ( int ) ( .5f+ Cy
                               ) , ( int ) ( .5f + Cx
                               ) , nPixelsy ,
                         nPixelsx ,getNParams(edge),getParamNames(edge), BadEdgeWidth );
            }
        
           }else
           {
              edge = isEdge(Cx,Cy,nPixelsx,nPixelsy,NBoundaryPixels,grid, Vx*Vx, Vy*Vy);
            
              slice = new OneSlice( grid , chan , nchan, ( int ) ( .5f+ Cy
                              ) , ( int ) ( .5f + Cx
                              ) , nPixelsy ,
                        nPixelsx ,getNParams(edge),getParamNames(edge), BadEdgeWidth );
           }
            float time1 = xscl.getX( chan);
            float time2 =time1;
            
            if( chan + 1 < xscl.getNum_x( ))
               time2 = xscl.getX( chan+nchan);
            
            time = (time1+time2)/2;
            if( dir ==1 && ichan==0)
               time0=time;
            
            double MaxErrChiSq = 0;
            double chiSqr = Double.NaN;
            double[] errs = new double[ 8 ];
            double[] DD = slice.getAllParameters( );
            char GoodSlicec = ' ';
            double Ierr = 0;
            double I_bErr = Double.NaN;
          
            if ( slice.getInitialTotIntensity( ) > 3
                  && slice.areParametersGood( ) )
            {
             
               double[] xs = new double[ slice.ncells( ) ];
               double[] ys = new double[ xs.length ];
               double[] sigs = new double[ xs.length ];
               sigs = slice.getstDevs( true);
               
               Arrays.fill( ys , 0 );
               Arrays.fill( sigs , 1 );
               for( int xi=0; xi<xs.length; xi++)
                  xs[xi] = xi;

               MaxErrChiSq = .05 / Math.max( slice.ncells( ) ,
                                                  slice.P[ITINTENS] );
               
               MarquardtArrayFitter fitter = new MarquardtArrayFitter( slice ,
                                      xs , ys , sigs , MaxErrChiSq , 200  );

               chiSqr = fitter.getChiSqr( );
              
               errs = fitter.getParameterSigmas_2( );// Use other for cases when
                                                  // params near the boundary
             
               I_bErrSv = errs[IBACK];
               Ierr = errs[3];
               errs = fitter.getParameterSigmas( );
            
               if ( Double.isNaN( Ierr ) )
                  {
                   Ierr = errs[3];
                  }

               if ( Double.isNaN( errs[3] ) )
                  errs[3] = Ierr;

               Ierr = Math.max( Ierr , errs[3] );
               errs[3] = Ierr;

               double sig = Math.sqrt( slice.TotVarn/slice.ncells());
     
               DD = slice.getAllParameters( );

              // double VarIntensities = slice.TotVarn;
             
               I_bErr = I_bErrSv * I_bErrSv * slice.TotVarn
                                  / slice.ncells( );
               I_bErr = 2 * slice.ncells( ) * slice.ncells( ) * I_bErr
                           + slice.TotVarn;
               I_bErr= CalcSliceIntensityVariance( slice, errs[3]*sig,
                     errs[0]*sig);
                  
              Ierr *= Math.sqrt( slice.TotVarn / slice.ncells( ) );
          
              GoodSlicec = GoodSlice( DD , 
                    errs, sig,            
                    nPixelsx ,
                    nPixelsy , 
                    grid , 
                    BadEdgeWidth ,slice);
              if( DEBUG)
                 System.out.print(GoodSlicec);
              if ( Double.isNaN( chiSqr )  
                   )
                  GoodSlicec = 'g';
            }

            // ----- Record info to log file -----------------
            double sig = Math.sqrt( slice.TotVarn/slice.ncells( ) );
            try
            {
               
               Hashtable<String,Float> Stat; 
               Stat = slice.getCurrentStatus( time ,(float)chiSqr  ,I_bErrSv ,
                     Ierr );

               Stat.put(ISAWINTENSITY, (float)CalcSliceIntensity(slice) );
               Stat.put(ISAWINTENSITY_ERROR, (float)Math.sqrt(
                     CalcSliceIntensityVariance(slice, errs[ITINTENS]*sig ,
                     errs[IBACK]*sig )));
               
               Stat.put( USE_SHOE_BOX,(float)0.0);
               
             if( GoodSlicec =='x')
               {
                  Stat.put( USED_PEAK , (float)(int)GoodSlicec );
                  if( dir >0 || Res.size() < 1)
                     Res.add(Stat);
                  else
                     Res.insertElementAt( Stat , 0 );
                  
               }else
               {                 
                
                  Stat.put( USED_PEAK , (float)(int)GoodSlicec );
               }
               
                           
               if( MergedLast && SavLog.size() > 0 && 
                     (GoodSlicec == 'x' || (SavLog.size()==1&& SavLog.elementAt(0).get(USED_PEAK)==0)))//last if all at center
                  if( dir >0 )
                     SavLog.remove( SavLog.size()-1 );
                  else
                     SavLog.remove( 0 );
               
              
               if( dir >0)
                  SavLog.add( Stat );
               else
                  SavLog.add( 0 , Stat );
           
            } catch( Exception s2 )
            {
               s2.printStackTrace( );
            }
         
            if ( GoodSlicec == 'x' )
            {
               case4 = false;//
 //---------------------------------
               if(MergedLast && LastGoodSavedData[0]>=0)
               {
                  TotIntensity1 -=LastGoodSavedData[6]-LastGoodSavedData[4]*LastGoodSavedData[7];
                  TotVariance1 -=LastGoodSavedData[8];
                  TotIntensity -=LastGoodSavedData[0];
                  TotVariance -=LastGoodSavedData[1];
                  
               }
             
               TotIntensity1 += ( slice.getInitialTotIntensity( ) - 
                           slice.getParameters( )[0] * slice.ncells( ) );

               TotVariance1 += I_bErr;
 
               LastGoodSavedData[0] = ( CalcSliceIntensity(slice));

               double Err = errs[ITINTENS]*sig;//Math.sqrt(chiSqr/slice.ncells( ));
               double bErr =errs[IBACK]*sig;//Math.sqrt(chiSqr/slice.ncells( ));
               LastGoodSavedData[1] =( CalcSliceIntensityVariance(slice,Err, bErr));
               LastGoodSavedData[2] = time;
               LastGoodSavedData[3] =chan +nchan/2;
               LastGoodSavedData[4] = slice.getParameters( )[0];
               LastGoodSavedData[5] = slice.TotBack/Math.max( 1 ,slice.nback);
               LastGoodSavedData[6]=slice.getInitialTotIntensity( );
               LastGoodSavedData[7]= slice.ncells( );
               LastGoodSavedData[8] = I_bErr;
               
   //------------------------------------------ 
               TotIntensity +=LastGoodSavedData[0];//DD[3];
             
               TotVariance += LastGoodSavedData[1];// Err * Err * chiSqr / slice.ncells( );
               if( dir ==1 && ichan==0 || (MergedLast && dir==1&&(ichan==2|| ichan==1)))
               {
                  System.arraycopy( LastGoodSavedData , 0 , LastGoodSavedData0 , 0 , 9);
               }
               if( MergedLast)
                  {
                    done = true;
                    MergedLast = false;
                    if( (ichan ==1||ichan==2) && dir ==1)//Merged ichan=0 and ichan=1 successfully
                       case4 = true;
                  }
               
               PrevCentX= (int)(.5+slice.P[IXMEAN]);
               PrevCentY =(int)(.5+ slice.P[IYMEAN]);
               if( dir ==1 && ichan ==0)
               {   PrevCentX0=PrevCentX;
                   PrevCentY0 =PrevCentY;
               }
               
            } else if( MergedLast)
            {
               done = true;
               MergedLast = false;
               
               if( TotIntensity <= 0 && dir >0)  //will merge 3 time slices if original not good
                  MergedLast = true;             
              
                Arrays.fill(  LastGoodSavedData , -1);
             
               
            }else
            {
               MergedLast = true;
               
               if( SavLog.size()>0)
                  if( dir >0 )
                     SavLog.remove( SavLog.size()-1 );
                  else
                     SavLog.remove( 0);
            }
               
         }
         }
         for( int kk=0; kk< SavLog.size( ); kk++)
            ShowStat(logBuffer, SavLog.elementAt( kk ));
         
         SavLog.clear( );
         if( DEBUG)
         {
            System.out.println( );
            System.out.println( "old inti="+Peak.inti()+", new inti="+TotIntensity);
         }
         float stDev = ( float ) Math.sqrt( TotVariance );
         float stDev1 = ( float ) Math.sqrt( TotVariance1 );

         if ( DEBUG )
            System.out.println( "   I,sigI=" + TotIntensity + "," + stDev );

         try
         {

            logBuffer.append( 
                  "   ----------------End Slices --------------------\n" );
            
            logBuffer.append( String.format(
                  "Tot Intensity %7.2f, stDev= %7.3f\n\n" ,
                  TotIntensity , stDev ) );
            
            logBuffer.append( 
                  "---------------------------New Peak------------------------\n" );

         } catch( Exception sss )
         {

         }

         if ( !Float.isNaN( TotIntensity ) && !Float.isNaN( stDev ) )
         {
           
            Peak.inti( ( float ) TotIntensity );         
            Peak.sigi( stDev );         

         } else
         {
            Peak.inti( 0 );
            Peak.sigi( 0 );
         }
      } catch( Throwable ss )
      {
         ss.printStackTrace( );
         
         if ( logBuffer != null )
         {
            String S = "Peak ";

            if ( Peak == null )
               
               S += "is null. \n";
            
            else
               
               S += Peak.toString( ) + "\n";
            
            S += ss.toString( ) + "\n";

            String[] stack = ScriptUtil.GetExceptionStackInfo( ss , true , 4 );
            
            if ( stack != null )
               for( int i = 0 ; i < stack.length ; i++ )
                  S += stack[i] + "\n";

            logBuffer.append( S );
         }
      }
      
      return Res;
   }
   
   /**
    * Calculates the shoebox inti and sigi given the slice bounds. ASSUMES Poisson.(Can be fixed to work
    * with non-Poisson data using slice.stDevs)
    * @param slice  The slice an all stats
    * @return   An array of two doubles. The first element is inti and the second is sigi
    */
   private static double[] getPeakData( OneSlice slice)
   {
      double[] Res = new double[2];
      Res[0]=Res[1]=0;
      
      if( slice == null)
         return Res;
      
      double TotBack = slice.TotBack;
      int nBack = slice.nback;
      Res[0] = slice.getInitialTotIntensity( )- TotBack/nBack*slice.ncells( ) ;
      Res[1] = slice.getInitialTotIntensity( ) + TotBack/nBack/nBack*slice.ncells()*slice.ncells( );
    
      return Res;
   }
   
   /**
    * Determines if peak is near an edge
    * @param Cx  Peak x center
    * @param Cy  Peak y center
    * @param nPixelsx  #pixels in x direction in panel
    * @param nPixelsy  #pixels in y direction in panel
    * @param NBoundaryPixels  # of bad boundary pixels in the panel with peak
    * @param grid           The data grid for the panel
    * @param Varx           The variation of x values near peak
    * @param Vary           The variation of the y values near the peak
    * @return   true if peak with extent is outside panel - bad boundary pixels, 
    *           otherwise false is returned.
    */
   private static boolean  isEdge(float Cx,float Cy,int nPixelsx,int nPixelsy,
         int NBoundaryPixels,IDataGrid grid, double Varx, double Vary)
   {
      double Sx = Math.sqrt( Varx )*2;
      double Sy = Math.sqrt( Vary )*2;
      if( Cx -nPixelsx < NBoundaryPixels)
         return true;
      
      if( Cy -nPixelsy < NBoundaryPixels)
         return true;
      

      if( Cx +nPixelsx +NBoundaryPixels > grid.num_cols( ))
         return true;
      

      if( Cy +nPixelsy +NBoundaryPixels > grid.num_rows( ))
         return true;
      

      if( Cx -Sx < NBoundaryPixels)
         return true;
      
      if( Cy -Sy < NBoundaryPixels)
         return true;
      

      if( Cx +Sx +NBoundaryPixels > grid.num_cols( ))
         return true;
      

      if( Cy +Sy +NBoundaryPixels > grid.num_rows( ))
         return true;
      
      
      return false;
   }
   
   /**
    * Returns the number of parameters to fit.
    * @param isEdge  if true 7 will be returned(the (Co)Variances will be fit)
    *                otherwise 4 is returned
    * @return  The number of parameters to fit.
    */
   private static int getNParams( boolean isEdge)
   {
      if( isEdge)
         return 7;
      return 4;
   }
   
   /**
    * Returns the names of the parameters to fit
    * @param isEdge   Is the peak within NSIGMA std devns of the edge of the panel-
    *                  bad boundary pixels
    * @return   The names of the parameters to be fit.
    */
   private static String[] getParamNames( boolean isEdge)
   {
      if( isEdge)
         return new String[]{"background","x mean", "y mean",  "Intensity",
            "x Variance","y Variance","Covariance"};
      
      return new String[]{"background","x mean", "y mean",  "Intensity"};
   }
   
   /**
    * Determines the channel with the maximum total count
    * @param Peak      The peak
    * @param nTimes    The # of time channels to use
    * @param xscl      The XScale for the times
    * @param Chan0     The starting channel
    * @param nPixels   The number of pixels to include around the peak for determining
    *                  counts
    * @param grid      The data grid for the panel
    * @param BadEdgeWidth  The number of bad edge pixels
    * @return
    */
   private static int FindMaxChannel( IPeak Peak, int nTimes, XScale xscl,int Chan0, int nPixels, IDataGrid grid, int BadEdgeWidth)
   {
      
      int Chan= Chan0;// z may differ depending on binning (int)(.5+Peak.z());
      int PrevCentX=(int)(.5+Peak.x());
      int PrevCentY=(int)(.5+Peak.y());
      int PrevCentX0=(int)(.5+Peak.x());
      int PrevCentY0=(int)(.5+Peak.y());
    
      int nPixelsx=nPixels,
          nPixelsy=nPixels;
      boolean done;
      int iMax = -1;
      double MaxIntensity = Double.MIN_VALUE;
      for( int dir =1; dir >=-1; dir -= 2)
      { 
         done = false;
         for( int ichan =0; ichan <=( nTimes - 1 ) / 2 &&!done; ichan++)         
  
      if( ichan==0 && dir < 0)
      {   
         done = false;
         PrevCentX=PrevCentX0;
         PrevCentY=PrevCentY0;
      }else if( Chan+dir*ichan < 1 ||Chan+dir*ichan >=xscl.getNum_x()-2)
      {
         done = true;
      }else if( AllDataInBadArea(grid,PrevCentY,PrevCentX, nPixels , nPixels ,
               BadEdgeWidth ))
               done = true;
      else
      {

         int chan = Chan+dir*ichan;
         int pixWidth = nPixels;
         int pixHeight = nPixels;
         if(nPixelsx > 0 && nPixelsy > 0)
         {
            pixWidth = nPixelsx;
            pixHeight = nPixelsy;
         }
         boolean edge=isEdge(PrevCentX,PrevCentY, pixWidth,  pixHeight,BadEdgeWidth,grid,0.0f,0.0f);
         OneSlice slice = new OneSlice( grid , chan ,1, PrevCentY,PrevCentX, pixHeight, pixWidth  ,
              getNParams(edge),getParamNames(edge) , BadEdgeWidth );
         
         double[] params = slice.getAllParameters();

         double Vx= params[IVXX];
         double Vy = params[IVYY];
        
         
         //if( intens/4/nPixelsx/nPixelsy*2.355*2.355 >2)
         {
         
         float Cx= (float)params[IXMEAN];
         float Cy =(float)params[IYMEAN];

         edge=isEdge(Cx,Cy,nPixelsx,  nPixelsy ,BadEdgeWidth,grid, Vx*Vx, Vy*Vy);
         slice = new OneSlice( grid , chan , 1,( int ) ( .5f+ Cy
                         ) , ( int ) ( .5f + Cx
                         ) , nPixelsy ,
                   nPixelsx , getNParams(edge), getParamNames(edge),BadEdgeWidth );
         
         // Update box sizes with fitted parameters 
         params = slice.getAllParameters();
         PrevCentX= (int)params[IXMEAN];
         PrevCentY =(int)params[IYMEAN];
         }
         if( slice.getInitialTotIntensity( )> MaxIntensity)
         {
            iMax =chan;
            MaxIntensity = slice.getInitialTotIntensity();
         }else if(  slice.getInitialTotIntensity( )== MaxIntensity  && Math.abs( iMax-Chan )> Math.abs( chan-Chan ) )
         {

            iMax =chan;
            MaxIntensity = slice.getInitialTotIntensity();
         }
      }
      }
     
     return iMax;
   }
  
   /**
    * Determines if there is enough data in a time slice
    * @param grid
    * @param PrevCentY
    * @param PrevCentX
    * @param nPixelsx
    * @param nPixelsy
    * @param BadEdgeWidth
    * @return
    */
   private static boolean  AllDataInBadArea(IDataGrid grid,
                                     float PrevCentY,
                                     float PrevCentX, 
                                     int nPixelsx , 
                                     int nPixelsy ,
                                     int BadEdgeWidth )
   {
      if( PrevCentY<BadEdgeWidth || PrevCentX < BadEdgeWidth)
         return true;
      if( PrevCentY > grid.num_rows( )-BadEdgeWidth || PrevCentX > grid.num_cols( )-BadEdgeWidth)
         return true;
     float lowRow = Math.max( BadEdgeWidth, PrevCentY-nPixelsy );
     float lowCol = Math.max(BadEdgeWidth, PrevCentX-nPixelsx );
     float highRow = Math.min( grid.num_rows( )-BadEdgeWidth, PrevCentY+nPixelsy );
     float highCol = Math.min( grid.num_cols( )-BadEdgeWidth, PrevCentX+nPixelsx );
     if( highRow-lowRow+1 <=0)
        return true;
     if( highCol-lowCol+1 <=0)
        return true;
     
      return false;
      
   }
   
   /**
    * Calculates the intensity of the slice
    * @param slice
    * @return  The calculated intensity
    */
   private static double CalcSliceIntensity( OneSlice slice)
   {
     double[] params = slice.getAllParameters( );
     double ExperimentalIntensity  =slice.getInitialTotIntensity( )-
         params[IBACK]*slice.ncells( );
     
     if( !slice.edge)
        return ExperimentalIntensity;
     //Use combination of  fit and Intensity*(r[>1]). 
     double r=1;
     float[] probs={.5f,.5987f,.6915f,.7734f,.8413f,.8944f,.9322f,.9599f,.9772f};
     float alpha =1;//percentage of fitted intensity used
     double NstdX = 4*Math.min(  params[IXMEAN]-1 , slice.grid.num_cols( )-params[IXMEAN] )
                      /Math.sqrt(params[IVXX]);

     double NstdY = 4*Math.min(  params[IYMEAN]-1 , slice.grid.num_rows( )-params[IYMEAN] )
                   /Math.sqrt(params[IVYY]);
     float sgn=1;
     if( NstdX <0){sgn=-1;}
     
     if( NstdX >= 7.5 )r =1.0; 
     else if( sgn >0)r =1/probs[(int)(NstdX+.5)]; 
     else  r = 1/(1-probs[(int)(-NstdX+.5)]);
     
    if( NstdY <0){sgn=-1;}
     
     if( NstdY >= 7.5 )r *=1.0; 
     else if( sgn >0)r *=1/probs[(int)(NstdY+.5)]; 
     else  r *= 1/(1-probs[(int)(-NstdY+.5)]);
     
     r = Math.max( r , 1.0 );
     alpha =(float)( 0+.5*(r-1));
     alpha = Math.min( 1.0f , alpha );
     
     return ExperimentalIntensity*r*(1-alpha)+ alpha*params[ITINTENS];
   
   }
   
   /**
    * Calculates the variation in the Intensity calculated via CalcSliceIntensity
    * @param slice
    * @param IntensityFit_err
    * @param IntensityBackError
    * @return  The variation in the intensity
    */
   private static double CalcSliceIntensityVariance( OneSlice slice, double IntensityFit_err,
           double IntensityBackError)
   {
      double[] stDevs = slice.stDevs;
      double[] params = slice.getAllParameters( );      
         
      double Var=0;
       for( int i=0;i<stDevs.length;i++)
          Var +=stDevs[i]*stDevs[i];
      if( Var != slice.TotVarn) 
         System.out.println("************************");
      Var=slice.TotVarn;
      Var += 2*IntensityBackError*IntensityBackError*slice.ncells( )*slice.ncells( );
      
      if( !slice.edge)
         return ( Var );
     
      double r=1;
                   // 0, .25,    .50,   .75   ...... std devs
      float[] probs={.5f,.5987f,.6915f,.7734f,.8413f,.8944f,.9322f,.9599f,.9772f};
      float alpha =1;//percentage of fitted intensity used
      double NstdX = 4*Math.min(  params[IXMEAN]-1 , slice.grid.num_cols( )-params[IXMEAN] )
                       /Math.sqrt(params[IVXX]);

      double NstdY =4* Math.min(  params[IYMEAN]-1 , slice.grid.num_rows( )-params[IYMEAN] )
                    /Math.sqrt(params[IVYY]);
      float sgn=1;
      if( NstdX <0){sgn=-1;}
      
      if( NstdX >= 7.5 )r =1.0; 
      else if( sgn >0)r =1/probs[(int)(NstdX+.5)]; 
      else  r = 1/(1-probs[(int)(-NstdX+.5)]);
      
     if( NstdY <0){sgn=-1;}
      
      if( NstdY >= 7.5 )r *=1.0; 
      else if( sgn >0)r *=1/probs[(int)(NstdY+.5)]; 
      else  r *= 1/(1-probs[(int)(-NstdY+.5)]);
      
      r = Math.max( r , 1.0 );
      alpha =(float)( 0+.5*(r-1));
      alpha = Math.min( 1.0f , alpha );
      
      return Var*r*r*(1-alpha)+ alpha* IntensityFit_err* IntensityFit_err;
  
   }
  
   private static void ShowStat( StringBuffer logBuffer, Hashtable<String,Float>Stat)
   {
      float chan= Stat.get( CHANNEL ).floatValue( );
      char GoodSlicec = (char)Stat.get( USED_PEAK ).intValue( );
     /* if( Stat.get( USE_SHOE_BOX ) > 0)
         if( Stat.get( USED_PEAK )> 0)
            GoodSlicec ='S';
         else
            GoodSlicec='s';
      else if( Stat.get(USED_PEAK) > 0)
         GoodSlicec ='x';
         */
      float AvBackGroundLeft = Stat.get( TOTAL_EDGE_INTENSITY )/Stat.get( N_EDGE_CELLS )
                                   -Stat.get(BACKGROUND);
      
   
      logBuffer.append( String.format(
            "%6.1f %7.3f %8.3f%c %8.3f %8.3f %8.3f %6d %10.2f %10.2f"
                   + " %13.3f %8.5f %9.5f %9.5f %9.5f %9.5f\n" ,
             chan + 1 , Stat.get( BACKGROUND ) , Stat.get( FITTEDINTENSITY ) , GoodSlicec ,
             Stat.get( CENTER_COLUMN ) ,Stat.get(CENTER_ROW),
            Math.sqrt( Stat.get(CHI_SQUARED) / Stat.get(NCELLS) ) ,
            Stat.get(NCELLS).intValue() , Stat.get(TOTAL_INTENSITY) ,Stat.get(ISAWINTENSITY),
            
             Stat.get(FITTEDINTENSITY_ERROR)  ,
           AvBackGroundLeft , Stat.get(VAR_COL) , Stat.get(VAR_ROW) ,
             Stat.get( COVARIANCE ), Stat.get( ISAWINTENSITY_ERROR) ) );
      //System.out.println(logBuffer.toString( ));
     
   }
 
   /**
    *  This can be started with 2 to 4 arguments, the peaks file and the nexus 
    *  file corresponding to the peak file.  The peak file must can span more
    *  than one run but only the run from the NeXus file will be integrated.
    *  Also,the peaks in the peaks file must be in the "correct" order. The order
    *  of the grids read from NeXus files must be the same order as in the peaks
    *  files. Currently at SNS for non event NeXus files, this means they are in 
    *  order of the grid ID's. 
    *  
    * @param args 
    *         args[0] - the name of the peaks file
    *         args[1] - the name of the NeXus file with the corresponding data
    *         args[2] -(optional) the number of bad edges on all detectors
    *         args[3] -(optional) The maximum length of the unit cell in real 
    *                              space 
    * NEEDS updating.
    */
   public static void main( String[] args)
   {  
      
      String PeaksFile ="C:/ISAW/SampleRuns/SNS/TOPAZ/WSF/top1172/nickel.peaks";
      String NeXusFile = "C:/ISAW/SampleRuns/SNS/TOPAZ/TOPAZ_1172.nxs";
      int BadEdgeWidth = 9;
      float MaxCellEdge =12;

      if ( args != null && args.length > 0 )
      {
         PeaksFile = args[0];
         NeXusFile = args[1];

         if ( args.length > 2 )
            BadEdgeWidth = Integer.parseInt( args[2].trim( ) );

         if ( args.length > 3 )
            MaxCellEdge = Float.parseFloat( args[3].trim( ) );

      } else
      {
         FileChooserPanel Peak = new FileChooserPanel(
               FileChooserPanel.LOAD_FILE , "Peaks File" );
         FileChooserPanel NexFile = new FileChooserPanel(
               FileChooserPanel.LOAD_FILE , "NeXus File" );
         JPanel BadEdgePanel = new JPanel( new GridLayout( 1 , 2 ) );
         BadEdgePanel.add( new JLabel( "# Bad Edge Pixels" ) );
         JTextField BadEdge = new FilteredPG_TextField( new IntegerFilter( ) );
         BadEdgePanel.add( BadEdge );

         JPanel CellSidePanel = new JPanel( new GridLayout( 1 , 2 ) );
         CellSidePanel.add( new JLabel( "Max real cell side" ) );
         JTextField CellSide = new FilteredPG_TextField( new IntegerFilter( ) );
         CellSidePanel.add( CellSide );

         BadEdge.setText( "10" );
         CellSide.setText( "12" );
         JPanel panel = new JPanel( );
         panel.setLayout( new GridLayout( 4 , 1 ) );
         panel.add( Peak );
         panel.add( NexFile );
         panel.add( BadEdgePanel );
         panel.add( CellSidePanel );
         int OptChoice = JOptionPane.showConfirmDialog( null , panel ,
               "Input Files" , JOptionPane.OK_CANCEL_OPTION );
         if ( OptChoice == JOptionPane.OK_OPTION )
         {
            PeaksFile = Peak.getTextField( ).getText( ).trim( );
            NeXusFile = NexFile.getTextField( ).getText( ).trim( );
            BadEdgeWidth = Integer.parseInt( BadEdge.getText( ).trim( ) );
            MaxCellEdge = Float.parseFloat( CellSide.getText( ).trim( ) );

         } else if ( OptChoice == JOptionPane.CANCEL_OPTION )
            return;

         panel.removeAll( );
         Peak = null;
         NexFile = null;
         BadEdgePanel = CellSidePanel = panel = null;
         BadEdge = CellSide = null;
      }

      int k = PeaksFile.lastIndexOf( '.' );
      if ( k < 0 )
         k = PeaksFile.length( );

      String LogFile = PeaksFile.substring( 0 , k ) + ".integrateLog";
      String OutFile = PeaksFile.substring( 0 , k ) + ".integrate";

      if ( PeaksFile.toUpperCase( ).endsWith( ".INTEGRATE" ) )
         OutFile = OutFile + "1";
      // -----------------------------------------------------------

      Vector Peaks = null;
      try
      {
         Peaks = Peak_new_IO.ReadPeaks_new( PeaksFile );
      } catch( Exception s )
      {
         s.printStackTrace( );
         return;
      }
      DEBUG = true;
      //int currentRun = -1;// in prep for more runs
      //int currentds = -1;
      //NexusRetriever nret = null;
      DataSet DS = null;
      XScale xscl = null;
      //int nTimeChan = -1;
      //int[][] ids = null;
      float dQ = 1f / MaxCellEdge / 6f;
      FileOutputStream fout = null;
      NexusRetriever ret = new NexusRetriever( NeXusFile );

      try
      {
         fout = new FileOutputStream( LogFile );
      } catch( Exception ss )
      {
         ss.printStackTrace( );
         fout = null;
         ret = null;
         Peaks = null;
         PeaksFile = NeXusFile = null;
         // System.exit(1);
      }

      int startDet = 0;
      int lastGrid = -1;
      IDataGrid grid = null;// grid with dataset entered
      int DSrunNum = -1;

      for( int i = 0 ; i < Peaks.size( ) ; i++ )
      {
         Peak_new Peak = ( Peak_new ) ( Peaks.elementAt( i ) );

         float[] Qs = Peak.getUnrotQ( );
         float Q = ( new Vector3D( Qs ) ).length( );

         int run = Peak.nrun( );
         int det = Peak.detnum( );

         IDataGrid grid1 = Peak.getGrid( );
         Vector3D pos = grid1.position( Peak.y( ) , Peak.x( ) );

         float D = pos.length( );
         float scatAng = ( new DetectorPosition( pos ) ).getScatteringAngle( );
         float w = Math.min( grid1.width( Peak.y( ) , Peak.x( ) ) , grid1
               .height( Peak.y( ) , Peak.x( ) ) );

         int nPixels = ( int ) ( .5 + Util.dPixel( dQ , Q , scatAng , D , w ) );
         nPixels = Math.max( 5 , nPixels );

         if ( grid1.ID( ) != lastGrid )
            DS = null;

         boolean wrongRunNum = false;
         if ( DSrunNum > 0 && DSrunNum != run )
         {
            wrongRunNum = true;
            startDet = 0;
            DS = null;
         }
         while( ( ( DS == null ) || ( grid.ID( ) != lastGrid )
               && ( startDet < ret.numDataSets( ) ) )
               && !wrongRunNum )
         {
            DS = ret.getDataSet( startDet++ );
            int[] runNums = AttrUtil.getRunNumber( DS );
            if ( runNums != null && runNums.length > 0 )
               DSrunNum = runNums[0];

            IDataGrid grid2 = null;

            if ( runNums == null || runNums.length < 1 || runNums[0] != run )
            {
               DS = null;
               startDet = 0;
               wrongRunNum = true;
            } else

               grid2 = DataSetTools.dataset.Grid_util.getAreaGrid( DS , det );
            if ( grid2 == null )
               DS = null;
            else
            {
               lastGrid = det;
               grid = grid2;
            }

         }
         if ( !wrongRunNum && DS != null )
         {
            float Time = Peak.time( );
            xscl = DS.getData_entry( 0 ).getX_scale( );

            int Chan = xscl.getI_GLB( Time );
            float dT_Chan = xscl.getX( Chan + 1 ) - xscl.getX( Chan );

            float xtimes = Math.max( 3 , 2 * Util.dTChan( dQ , Q , Time ,
                  dT_Chan ) + 1 );
            int nTimes = ( int ) ( xtimes + .5 ) + 1;

            float TotIntensity = 0;
            float TotVariance = 0;

            System.out.println( "Peak num #time channels=" + i + "," + nTimes );
            try
            {
               fout.write( String.format( "Peak,run,det=%3d %4d %3d\n" , i + 1 ,
                     run , det ).getBytes( ) );
               fout.write( String.format(
                     "   Pixel width %3dchan %4d,, chanMin %4d,chanMax %4d\n" ,
                     nPixels + 1 , Chan + 1 , Chan + 1 - ( nTimes - 1 ) / 2 ,
                     Chan + 1 + ( nTimes - 1 ) / 2 ).getBytes( ) );
               fout.write( "   ----------------Slices --------------------\n"
                     .getBytes( ) );
               fout
                     .write( ( " chan    back  Intens(P)   mx       my   sigm(Cll) ncells Intens(Tot) Intens(Tot-back) errI"
                           + "     bk_res     Varx    Vary        Vxy \n" )
                           .getBytes( ) );

            } catch( Exception ss )
            {

            }
            nPixels++ ;
            nTimes += 2;
            boolean done = false;
            // last y values is at xscl.getNum_x( )-2 on a histogram
            for( int chan = Math.max( 0 , Chan - ( nTimes - 1 ) / 2 ) ; !done
                  && chan <= Math.min( Chan + ( nTimes - 1 ) / 2 , xscl
                        .getNum_x( ) - 2 ) ; chan++ )
            {
               boolean edge = isEdge(Peak.x( ),Peak.y( ),nPixels , nPixels , BadEdgeWidth ,grid,0,0);
               OneSlice slice = new OneSlice( grid , chan , 1,
                     ( int ) ( .5f + Peak.y( ) ) , ( int ) ( .5f + Peak.x( ) ) ,
                     nPixels , nPixels ,getNParams(edge),getParamNames(edge), BadEdgeWidth );

               double[] xs = new double[ slice.ncells( ) ];
               double[] ys = new double[ xs.length ];
               double[] sigs = new double[ xs.length ];
               Arrays.fill( ys , 0 );
               Arrays.fill( sigs , 1 );

               // sigs[3] =.2;//Integrated intensity should be able to change
               // faster
               // than other paramters. It is larger
               for( int ii = 0 ; ii < xs.length ; ii++ )
                  xs[ii] = ii;
               double MaxErrChiSq = .00001 * slice.ncells( );
               MarquardtArrayFitter fitter = new MarquardtArrayFitter( slice ,
                     xs , ys , sigs , MaxErrChiSq , 200 );
               double chiSqr = fitter.getChiSqr( );

               double[] errs = fitter.getParameterSigmas( );// Use other for
             

               double[] DD = slice.getParameters( );
               char GoodSlicec = GoodSlice( DD , errs, Math.sqrt( chiSqr / slice.ncells( ) ) , 
                     nPixels ,
                     nPixels , grid , BadEdgeWidth ,slice);
               if ( Double.isNaN( chiSqr )
                     )
                  GoodSlicec = 'g';

     

               try
               {

                  fout.write( String.format(
                        "%5d %7.3f %8.3f%c %8.3f %8.3f %8.3f %6d %10.2f %10.2f"
                              + " %13.3f %8.5f %9.5f %9.5f %9.5f\n" ,
                        chan + 1 ,
                        DD[0] ,
                        DD[3] ,
                        GoodSlicec ,
                        DD[1] ,
                        DD[2] ,
                        Math.sqrt( chiSqr / slice.ncells( ) ) ,
                        slice.ncells( ) ,
                        slice.getInitialTotIntensity( ) ,
                        ( slice.getInitialTotIntensity( ) - slice
                              .getParameters( )[0]
                              * slice.ncells( ) ) ,
                        errs[3] * Math.sqrt( chiSqr / slice.ncells( ) ) ,
                        slice.getAvBackGroundLeft( ) , DD[4] , DD[5] , DD[6] )
                        .getBytes( ) );
               } catch( Exception s2 )
               {

               }
               if ( GoodSlicec == 'x' )
               {
                  double sig = Math.sqrt( slice.TotVarn/slice.ncells( ));
                  TotIntensity += CalcSliceIntensity(slice);
                  double Err = errs[ITINTENS]*sig;
                  double bErr =errs[IBACK]*sig;
                  TotVariance += CalcSliceIntensityVariance(slice,Err, bErr);

               } else if ( chan < Chan )//trying if main peak rejected
               {
                  TotIntensity = 0;
                  TotVariance = 0;
                  
               } else if(chan <=Chan+2 && TotIntensity > 0)
                
                   done = true;
               else if( chan <=Chan+2)
               {
                  TotIntensity = 0;
                  TotVariance = 0;
               }
                  
               else
                  done = true;

            }

            float stDev = ( float ) Math.sqrt( TotVariance );
            System.out.println( "   I,sigI=" + TotIntensity + "," + stDev );
            try
            {

               fout
                     .write( "   ----------------End Slices --------------------\n"
                           .getBytes( ) );
               fout.write( String.format(
                     "Tot Intensity(-back) %7.2f, stDev= %7.3f\n\n" ,
                     TotIntensity , stDev ).getBytes( ) );
               fout
                     .write( "---------------------------New Peak------------------------\n"
                           .getBytes( ) );

            } catch( Exception sss )
            {

            }

            if ( !Float.isNaN( TotIntensity ) && !Float.isNaN( stDev ) )
            {
               Peak.inti( ( float ) TotIntensity );
               Peak.sigi( stDev );

            } else
            {
               Peak.inti( 0 );
               Peak.sigi( 0 );
            }
         }
      }

      try
      {
         Peak_new_IO.WritePeaks_new( OutFile , Peaks , false );
         fout.close( );
      } catch( Exception ss )
      {

      }

      JOptionPane.showMessageDialog( null , 
            "<html><body> The integrate file is in " + OutFile + 
            "<P>  There is a log file in " + LogFile + "</body></html>" );
      
      DS = null;

      fout = null;
      Peaks.clear( );
      Peaks = null;
      PeaksFile = NeXusFile = null;
      ret.close( );
      ret = null;
      DEBUG = false;
     
   }
   

  
   /**
    * Determines whether a time slice is a good slice that will contribute to the
    * Intensity and variance of a peak
    * 
    * @param parameters
    * @param errs
    * @param drow
    * @param dcol
    * @return
    */
   private static char GoodSlice( double[] parameters, double[] errs, double sigma,int drow, int dcol,
         IDataGrid grid, int BadEdgeRange, OneSlice slice) 
   {
      double ExperimentalIntensity = slice.getInitialTotIntensity( )-parameters[IBACK]*
          slice.ncells( );
      
      double AvHeight=slice.getInitialTotIntensity( )/slice.ncells( );
      boolean isEdge = slice.edge;
      
      for( int i=0; i<errs.length; i++)
         if( Double.isNaN( errs[i] )|| Double.isInfinite( errs[i] ))
            return 'a';
      
          
      double errI = errs[ITINTENS]*sigma;
      if( parameters ==null)
         return 'b';
      
      if( Double.isNaN( parameters[ITINTENS]  ) || Double.isNaN(  errI ))
         return 'c';
      
     double IsawIntensity = CalcSliceIntensity( slice);
     double IsawIntensityVariance = CalcSliceIntensityVariance( slice, errI, errs[IBACK]*sigma);
    
     if( IsawIntensity*IsawIntensity/IsawIntensityVariance < 9)       
           return 'h';
     
     if( parameters[ITINTENS]/errI < 3  )
        return 'j';
     
     if( ( ExperimentalIntensity < 0)  || 
           (parameters[ITINTENS]>0 && Math.abs(ExperimentalIntensity/parameters[ITINTENS]-1)>.25))
           if( !isEdge)
              return '8';
     //  Peak too close to edge
     if( parameters[IXMEAN] <= BadEdgeRange || parameters[IYMEAN] <= BadEdgeRange)
        return '3';
     
     if( parameters[IXMEAN] >=  grid.num_cols()-BadEdgeRange||
           parameters[IYMEAN] >= grid.num_rows()- BadEdgeRange)
        return '3';
     
     if( parameters.length <IVXX+1)
        return 'x';
     
     double factor =1;
     if( isEdge) factor=2;
     
     if( errs[IYMEAN]*sigma >5*factor)
        return 'd';
     
     if( errs[IXMEAN]*sigma > 5*factor)
        return 'e';
     
     if( isEdge)factor=1.5;
     
     if( errs.length>=7)
     if( errs[IVXX]*sigma/parameters[IVXX] >.5*factor)
        return 'f';
     
     if( errs.length>=7)
     if( errs[IVYY]*sigma/parameters[IVYY] >.5*factor)
        return 'g';
     //------------- eliminate flat theoretical cases-----------------------------
     //     --- Av Height(-back) less than 20% of MaxHeight(-back)------
     double XX = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
     double MaxPeakHeight = parameters[ITINTENS]/2/Math.PI/
                            Math.sqrt(XX);
     
     AvHeight -= parameters[IBACK];
    
     if( AvHeight <=0 || MaxPeakHeight <=0|| AvHeight > MaxPeakHeight)
        return 'A';
     
     
     
     if( MaxPeakHeight < 2*AvHeight)
        return 'B';
     
     if( MaxPeakHeight <1 && (parameters[IVYY] >slice.nrows( )*slice.nrows()/9 ||
           parameters[IVXX] >slice.ncols( )*slice.ncols()/9 ))
        return 'C';
     //--------------------------------------------------------------------
     // Eliminate minor case where theoretical intensity falls to .3 of
     //  max height(-back) when 1 pixel away from max(i.e. if fit row/col non integers may
     //  cause essentially flat(0) results.
     if( parameters[IVXX]+parameters[IVYY] > 
          2.6*(parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY]))
     {
        return 'B';
     }
     
     return 'x';
   }
   
   /**
    * The class that calculates values and keeps up the parameters for the
    * Marquardt algorithm
    * 
    * @author ruth
    *
    */
   static class OneSlice extends OneVarParameterizedFunction
   {
     
      //------------Indices in BaseValues ------------------
      int S_int =0;
      int S_xint =2;
      int S_yint =3;
      int S_x2int =4;
      int S_y2int =5;
      int S_xyint =6;
      int S_x =7;
      int S_y =8;
      int S_x2 =9;
      int S_y2 =10;
      int S_xy =11;
      int S_1 =12;
      boolean average= false;
      IDataGrid grid; 
      int row,col;
      int chan; 
      int nchans;
      int drows;
      int dcols;
      int Ncols,Nrows;
      int startRow, startCol;
      double Value;//errors

      double TotBack = 0;
      double VarTotBack =0;
      int  nback =0;
      double[] P; //parameters just for the Normal part of the distribution
                  //the variable, parameters, are the corresponding entries for 
                  //    back + normal.
      boolean goodParameters = false;
      double[] BaseValues;
      float StdDevx ; 
      float StdDevy; 
      float StDev;
      boolean edge;
      //parameters mx,my,b,I,Sxx,Syy,Sxy 
      
      double expCoef_z;
      
       double coeffNorm;
       
       double expCoeffx2;
       double expCoeffxy ;
       double expCoeffy2;
       int BadEdgeRange=10;
       double[] expVals;
       double[] dVdmx,dVdmy,dVdsx,dVdsy,dVdsxy;
       Deriv[] derivativeRules;
       double BaseVxx;
       double BaseVyy ;
       float[][] Intensity;
       double[] stDevs;
       double TotVarn;
       //Derivatives of Sx,Sy,Sxy wrt to 4 params. For use when only 4 parameters
       double dSxdmx,
              dSxdmy,
              dSxdb,
              dSydmx,
              dSydmy,
              dSydb,
              dSxydmx,
              dSxydmy,
              dSxydb;
       /**
        * The number of cells used in the fitting
        * @return
        */
      public int ncells()
      {
         return (int)BaseValues[S_1];
      }
      
      public int nrows()
      {
         return Nrows;
      }
      
      public int ncols()
      {
         return Ncols;
      }
      
      public double[] AddBackground( double[] params)
      {
         return params;
       
      }
      public double[] RemoveBackground( double[] params)
      {
         return params;
        
      }
      
      void showTestData()
      {
        System.out.println("Base_Values=");
        for( int i=0; i<BaseValues.length; i++)
           if( i != 1)
              System.out.println( BaseValues[i]);
        System.out.println("--------------------------------");
        System.out.println("Parameters");
        double[] PP = getAllParameters();
        for( int i=0; i <PP.length; i++)
           System.out.println(PP[i]);
        System.out.println("--------------------------------");
        System.out.println("Derivatives");
        double[] x= new double[ncells()];
        for( int j=0; j< x.length; j++)
           x[j]=j;
        for( int i=1; i<PP.length; i++)
        {
           double[] drv= derivativeRules[i].get_dFdp( x );
          System.out.println( i+","+drv[0]+","+drv[(int)(ncells()/4)]+","
                +drv[(int)(2*ncells()/4)]+","+drv[(int)(3*ncells()/4)]  );
        }
        System.out.println("startRow,Nrows="+startRow+","+Nrows);
        System.out.println("startCol,Ncols="+startCol+","+Ncols);
        double[]vals = getValues(x);
        int j=0;
        for( int r=0; r< Nrows; r++)
           for( int c=0; c<Ncols;c++)
           { 
              vals[j]+= Intensity[r][c];
              j++;
           
           }
        System.out.println( "vals="+vals[0]+","+vals[(int)(ncells()/4)]+","
              +vals[(int)(2*ncells()/4)]+","+vals[(int)(3*ncells()/4)]  );
      }
      /**
       * 
       * @return  The average background per cell for the boundary
       *          cells( with background parameter taken off)
       */
      public double getAvBackGroundLeft()
      {
         return TotBack/nback -P[IBACK];
      }
      
      /**
       * Total of all the intensities in the slice and rectangle
       * @return
       */
      public double getInitialTotIntensity()
      {
         return BaseValues[S_int];
      }
   /*   if( row < startRow+Stdy*NSIGMA || col <startCol+Stdx*NSIGMA ||
            row <grid.num_rows( )-Stdy*NSIGMA ||
            col < grid.num_cols( )-Stdx*NSIGMA)
           edge = true;
           */
      /**
       * Constructor
       * @param grid   The data grid with the intensities
       * @param chan   The time channel(slice) of interest
       * @param row    The row on grid with the center of the peak
       * @param col    The col on the grid with the center of the peak
       * @param drows   The max # of rows around row to include
       * @param dcols   The max # of cols around col to include
       */
      public OneSlice( IDataGrid grid, 
                       int       chan,
                       int       nchans,
                       int       row,
                       int       col, 
                       int       drows,
                       int       dcols,
                       int       nparams,
                       String[] paramNames,
                       int       BadEdgeRange)
      {
         super("Slice Errors",new double[nparams], //TODO changed from 7
                     paramNames);
         edge= nparams>5;
         this.nchans=nchans;
         this.grid=grid; 
         this.chan=chan; 
         this.drows=drows;
         this.dcols=dcols;
         this.row = row;
         this.col = col;
         this.BadEdgeRange = BadEdgeRange;
         BaseValues = new double[13];
         Arrays.fill( BaseValues, 0);
         TotBack = 0;
         VarTotBack =0;
         nback =0;
         TotVarn =0;
         // To translate from the linear x to the corresponding
         //    row and column

         startRow= Math.min(  Math.max( BadEdgeRange+1,row-drows), grid.num_rows()-BadEdgeRange-drows);
         startCol =Math.min( Math.max( BadEdgeRange+1,col-dcols),grid.num_cols()-BadEdgeRange-dcols);
         
         Ncols =  Math.min( col+dcols ,grid.num_cols( )-BadEdgeRange)- 
                                       startCol+1;
         Nrows = Math.min( row+drows ,grid.num_rows( )-BadEdgeRange)-
                                        startRow +1;
   
         if( col >250)
            System.out.println("Woops");
         expVals = new double[Nrows*Ncols];
         Intensity = CreateClearfloatArray( Nrows,Ncols);
         
                
         stDevs = new double[Nrows*Ncols];
         Arrays.fill( stDevs , 0. );
         float MaxIntensity= Float.NaN;
         float MinIntensity =Float.NaN;
         for( int r = Math.max( BadEdgeRange+1,row-drows); r <= Math.min( row+drows ,grid.num_rows( )-BadEdgeRange);r++)

            for( int c = Math.max( BadEdgeRange+1,col-dcols); c <= Math.min( col+dcols ,grid.num_cols( )-BadEdgeRange);c++)
            {
               Data D = grid.getData_entry( r , c);
               float [] yvals =D.getY_values( );
               //float intensity = D.getY_values( )[chan];
               float intensity =0;
               
               float[] stDevL = D.getErrors( );
               float stDev = 0;
               for( int cc =chan; cc<chan+nchans;cc++)
               {
               if( stDevL != null && stDevL.length >chan)
                  stDev += stDevL[chan];
               else
                  stDev += (float) Math.sqrt( intensity );
               
               intensity += yvals[cc];
               }
               
               
               AddToIntensity( intensity,stDev, r,c);
               TotVarn+=stDev*stDev;
               BaseValues[0] += intensity;
               BaseValues[1] += intensity*intensity;
               BaseValues[2] += c*intensity;
               BaseValues[3] += r*intensity;
               BaseValues[4] += c*c*intensity;
               BaseValues[5] += r*r*intensity;
               BaseValues[6] += r*c*intensity;
               BaseValues[7] += c;
               BaseValues[8] += r;
               BaseValues[9] += c*c;
               BaseValues[10] += r*r;
               BaseValues[11] += r*c;
               BaseValues[12] +=1;
               if( r==row-drows || r==row+drows || c==col-dcols || c==col+dcols)
               {
                  TotBack+=intensity;
                  VarTotBack +=stDev*stDev;
                  nback++;
               }
               if( Float.isNaN( MaxIntensity )|| intensity >MaxIntensity)
                  MaxIntensity = intensity;

               if( Float.isNaN( MinIntensity )|| intensity <MinIntensity)
                  MinIntensity = intensity;
               
            }
         FindWidthsHalfHeight( MinIntensity, MaxIntensity);
         FinishIntensity(grid, startRow,startCol, chan);
         
         double[] params = new double[nparams];
         P = new double[7];
         P[ITINTENS] = BaseValues[S_int];// -params[IBACK]*BaseValues[S_1];
         P[0]=0;
         setSigmas( P,false);
         BaseVxx= P[IVXX];
         BaseVyy = P[IVYY];
         CalcInitialParams(P);

       
         System.arraycopy( P,0,params,0,nparams);
              
         derivativeRules = new Deriv[7];
         derivativeRules[IBACK] = new Derivb();
         derivativeRules[ITINTENS] = new DerivI(P,startRow,startCol,Nrows,Ncols);
         derivativeRules[IXMEAN] = new Derivmx(P,startRow,startCol,Nrows,Ncols);
         derivativeRules[IYMEAN] = new Derivmy(P,startRow,startCol,Nrows,Ncols);
         derivativeRules[4] = new DerivSxx(P,startRow,startCol,Nrows,Ncols);
         derivativeRules[5] = new DerivSyy(P,startRow,startCol,Nrows,Ncols);
         derivativeRules[6] = new DerivSxy(P,startRow,startCol,Nrows,Ncols);
         
         
         
         setParameters((params) );

         
         
      }
      
      private void FindWidthsHalfHeight( float MinIntensity, float MaxIntensity)
      {
         //Check if Not enough cols to get to background
         
         //Get average
         float dCount = Math.max(.001f, (MaxIntensity-MinIntensity)/20);
         float TotMaxIntensity=0;
         int nMaxIntensity =0;
         float TotMinIntensity =0;
         int nMinIntensity =0;
         float Totx =0;
         float Toty =0;
         for( int row =0; row < nrows();row++)
            for( int col =0; col <ncols();col++)
            {
               if( Intensity[row][col] > MaxIntensity-dCount)
               {
                  TotMaxIntensity +=Intensity[row][col];
                  nMaxIntensity++;
                  Totx +=col;
                  Toty +=row;
               }
               if( Intensity[row][col] < MinIntensity+dCount)
               {
                  TotMinIntensity +=Intensity[row][col];
                  nMinIntensity++;
               }
            }
         
         MaxIntensity = TotMaxIntensity/nMaxIntensity;
         MinIntensity = TotMinIntensity/nMinIntensity;
         float Centx = Totx/nMaxIntensity;
         float Centy = Toty/nMaxIntensity;
         
        //Check if not enuf pixels to get to boundary
         float factor = 1;
         double[] pp = new double[ 7 ];
         pp[IXMEAN] = Centx;
         pp[IYMEAN] = Centy;
         pp[IBACK] = TotBack / nback;
         setSigmas( pp , false );
         
         if (Double.isNaN( pp[IVXX] )|| Double.isNaN( pp[IVYY] )||
                Double.isInfinite( pp[IVXX] )|| Double.isInfinite( pp[IVYY] )
                || pp[IVXX] <= 0 || pp[IVYY] <= 0 )
         {
            MinIntensity /= 2; // ??? make a percentage
            factor = 1.2f;
         } else if ( pp[IVXX] * NSIGMA * NSIGMA * 4 > Ncols * Ncols )
         {
            MinIntensity /= 2; // ??? make a percentage
            factor = 1.2f;
         } else if ( pp[IVYY] * NSIGMA * NSIGMA * 4 > Nrows * Nrows )
         {
            MinIntensity /= 2; // ??? make a percentage
            factor = 1.2f;
         }
            
         //If row and col are not inside detector must adjust max somehow.
        //  we will ignore that for now
         
         float halfMaxMin = (MaxIntensity+MinIntensity)/2f;
         double MaxDistx=0;;
         double MaxDisty =0;
         double MinDistx=0;;
         double MinDisty =0;
         double TotDist =0;
         int nDist =0;
    
         while( nDist <2 && dCount/1.2 <halfMaxMin && (MaxDistx <=0 || MaxDisty<=0))
         {  
            MaxDistx = MinDistx =MaxDisty= MinDisty =-1;
            nDist =0;
            for( int row =0; row < nrows();row++)
               for( int col =0; col <ncols();col++)
               if( Intensity[row][col] > halfMaxMin-dCount &&
                     Intensity[row][col]<halfMaxMin+dCount)
               {
                  double distx =Math.abs( Centx-col );
                  double disty=Math.abs( Centy-row );
                  if( MinDistx <0)
                     MinDistx = MaxDistx = distx;
                  else if ( distx < MinDistx)
                     MinDistx = distx;
                  else if( distx > MaxDistx)
                     MaxDistx = distx;

                  if( MinDisty <0)
                     MinDisty = MaxDisty = disty;
                  else if ( disty < MinDisty)
                     MinDisty = disty;
                  else if( disty > MaxDisty)
                     MaxDisty = disty;
                  
                  TotDist +=Math.sqrt(( Centx-col )*( Centx-col )+( Centy-row )*( Centy-row ) );
                  nDist++;
                  
               }
            dCount *=1.2;
         }
        
         StdDevx =(float) MaxDistx/.833f*factor;
         StdDevy =(float)MaxDisty/.833f*factor;
         StDev = (float)TotDist/nDist/.833f*factor;
      }
     
   
      public Hashtable<String,Float> getCurrentStatus( float time,float chiSq, double errBack,
               double errIntensity)
      {
     
         Hashtable<String,Float> Res = new Hashtable<String,Float> ();
         Res.put( TIME , time );
         float errMult = (float)Math.sqrt( TotVarn/ncells() );
         //float errMult1 = (float)Math.sqrt( getInitialTotIntensity()/ncells() );
         Res.put( CHANNEL , (float)chan +(nchans-1)/2f);
         Res.put( BACKGROUND , (float)P[IBACK] );
         Res.put(BACKGROUND_ERROR, (float)(errBack*errMult));
         Res.put(FITTEDINTENSITY,(float)P[ITINTENS]);
         Res.put(FITTEDINTENSITY_ERROR,(float)errIntensity );
         Res.put(CENTER_COLUMN,(float)P[IXMEAN]);
         Res.put(CENTER_ROW,(float)P[IYMEAN]);
         Res.put(VAR_ROW,(float)P[IVYY]);
         Res.put(VAR_COL,(float)P[IVXX]);
         Res.put(COVARIANCE,(float)P[IVXY]);
         Res.put(CHI_SQUARED,(float)chiSq);
         Res.put(NCELLS,(float)ncells());
         Res.put(TOTAL_INTENSITY,(float)getInitialTotIntensity());
         Res.put(TOTAL_EDGE_INTENSITY,(float)TotBack);
         Res.put(N_EDGE_CELLS,(float)nback);
         
         return Res;
         
      }
      
      //did not set chi sq
      public Hashtable<String,Float> getBestShoeBoxInfo( float time )
      {
         Hashtable<String,Float> Res = new Hashtable<String,Float> ();
         Res.put( TIME , time );
         Res.put( CHANNEL , (float)chan );
         Res.put(CENTER_COLUMN,(float)P[IXMEAN]);
         Res.put(CENTER_ROW,(float)P[IYMEAN]);
         double Sx = Math.sqrt( P[IVXX] );
         double Sy = Math.sqrt(P[IVYY]);
         int firstCol =(int) Math.max( startCol , P[IXMEAN]-2.3*Sx+.5 );
         int firstRow =(int) Math.max(  startRow , P[IYMEAN]-2.3*Sy+.5 );
         int lastCol =(int) Math.min(  startCol+Ncols-1 , P[IXMEAN]+2.3*Sx+.5 );
         int lastRow =(int) Math.min( startRow+Nrows-1, P[IYMEAN]+2.3*Sy+.5 );
         float TotIntensity=0;
         float TotBackIntensity =0;
         float TotIntensityVar=0;
         float TotBackIntensityVar =0;
         int nback =0;
         int ncells=0;
         int i= (firstRow-startRow)*Ncols+(firstCol-startCol);
         int i1=i;
         for( int row = firstRow-startRow; row <=lastRow-startRow; row++)
            for( int col = firstCol-startCol; col <= lastCol-startCol; col++)
            {
               TotIntensity +=Intensity[row][col];
               TotIntensityVar += stDevs[i]*stDevs[i];
               
               if( row ==(firstRow-startRow) || col==(firstCol-startCol)|| 
                     row==(lastRow-startRow)||  col ==(lastCol-startCol))
               {
                  nback++;
                  TotBackIntensity +=Intensity[row][col];
                  TotBackIntensityVar += stDevs[i]*stDevs[i];
               }else
                  ncells++;
               i++;
               if( col ==lastCol-startCol)
               {
                  i = i1+Ncols ;
                  i1+=Ncols;
               }
            }
         if( nback <=0)
            nback = 1;
         Res.put(BACKGROUND, TotBackIntensity/nback);
         Res.put(BACKGROUND_ERROR, (float)Math.sqrt( TotBackIntensityVar)/nback);

         double intens =TotIntensity - TotBackIntensity-ncells*TotBackIntensity/nback;
         double sigsq =intens + ncells*ncells*TotBackIntensityVar/nback/nback;
         if( intens <0)
            intens = sigsq =0;
         
         Res.put(FITTEDINTENSITY,(float)intens);
         Res.put(FITTEDINTENSITY_ERROR,(float)Math.sqrt( sigsq   ));
         Res.put(VAR_ROW,(float)P[IVYY]);
         Res.put(VAR_COL,(float)P[IVXX]);
         Res.put(COVARIANCE,(float)P[IVXY]);
         Res.put(NCELLS,(float)ncells);
         Res.put(TOTAL_INTENSITY,(float)getInitialTotIntensity());
         Res.put(TOTAL_EDGE_INTENSITY,(float)TotBack);
         Res.put(N_EDGE_CELLS,(float)nback);
         return Res;
         
      }
     
    
      private void CalcInitialParams( double[] params)
      {
         if( BaseValues[S_int]<=0)
         {
            params[IXMEAN]=col;
            params[IYMEAN]=row;
          
            params[IVXX] =StdDevx*StdDevx;
            params[IVYY] =StdDevy*StdDevy;
            params[IVXY]=0;
            return;
         }
         params[IBACK] = TotBack/nback;
         params[ITINTENS]= BaseValues[S_int]- params[IBACK]*BaseValues[S_1];
         while( params[ITINTENS] < 0)
         {
            params[IBACK] /=2;
            params[ITINTENS]= BaseValues[S_int]- params[IBACK]*BaseValues[S_1];
         }
            
         setMxMyParams( params[IBACK], params);
         setSigmas(params, false);
         
         if( !edge)
         while( params[IVXX] <0 || params[IVYY] < 0 ||
               Double.isInfinite( params[IVXX] )||Double.isInfinite( params[IVYY] )
               || Double.isNaN( params[IVXX] )||Double.isNaN( params[IVYY] ))
         {
            params[IBACK] /=2;
            params[ITINTENS]= BaseValues[S_int]- params[IBACK]*BaseValues[S_1];
            setMxMyParams( params[IBACK], params);
            setSigmas(params, false);
         } else
         {
            params[IVXX] =StdDevx*StdDevx;
            params[IVYY] =StdDevy*StdDevy;
            params[IVXY]=0;
         }
         if(!edge)
            setSigmas( params, true);
         
         double Stdx = Math.sqrt(Math.max( 0 , params[IVXX]));
         double Stdy = Math.sqrt( Math.max( 0,params[IVYY]) );
         
         if(Stdx/StdDevx < .7)
            Stdx = params[IVXX]=StdDevx;
         
         if(Stdy/StdDevy < .7)
            Stdy = params[IVYY]=StdDevy;
        
         
         
         if( edge)
         {
            params[IXMEAN]=col;
            params[IYMEAN]=row;
          
            params[IVXX] =StdDevx*StdDevx;
            params[IVYY] =StdDevy*StdDevy;
            params[IVXY]=0;
            return;
         }
         
         while( params[IVXX]*params[IVYY] <= params[IVXY]*params[IVXY]  && params[IVXX] >0  && params[IVYY] > 0)
            params[IVXY]= params[IVXY]/2;
         
      }
      private void setMxMyParams( double background, double[] params)
      {
      
         if( BaseValues[S_int]!=0)
         {
            params[IXMEAN] = (BaseValues[S_xint]-background*BaseValues[S_x])
                          /(BaseValues[S_int]-background*BaseValues[S_1]);//col;
      
            params[IYMEAN] = (BaseValues[S_yint]-background*BaseValues[S_y])
                             /(BaseValues[S_int]-background*BaseValues[S_1]);//row;
         }else
         {
      
            params[IXMEAN] = col;
      
            params[IYMEAN] = row;
         }
      }

  

      private void setMxMyParams( float background, double[] params)
      {

         if( (BaseValues[S_int]-background*BaseValues[S_1])!=0)
         {
            params[IXMEAN] = (BaseValues[S_xint]-background*BaseValues[S_x])
                          /(BaseValues[S_int]-background*BaseValues[S_1]);//col;
       
            params[IYMEAN] = (BaseValues[S_yint]-background*BaseValues[S_y])
                             /(BaseValues[S_int]-background*BaseValues[S_1]);//row;
         }else
         {

            params[IXMEAN] = col;
       
            params[IYMEAN] = row;
         }
      }
      
      private void updateBV( int row, int col, float intensity)
      {

         BaseValues[S_int] += intensity;
         BaseValues[S_xint] += intensity*(startCol+col);
         BaseValues[S_yint ] += intensity*(startRow+row);
         BaseValues[S_x2int ] += intensity*(startCol+col)*(startCol+col);
         BaseValues[S_y2int ] += intensity*(startRow+row)*(startRow+row);
         BaseValues[S_xyint ] += intensity*(startCol+col)*(startRow+row);
         BaseValues[S_x ] +=(startCol+col);
         BaseValues[S_y ] +=(startRow+row);
         BaseValues[S_x2 ] +=(startCol+col)*(startCol+col);
         BaseValues[S_y2 ] +=1*(startRow+row)*(startRow+row);
         BaseValues[S_xy ] +=1*(startRow+row)*(startCol+col);
         BaseValues[S_1 ]++;
      }
      private void FinishIntensity( IDataGrid grid, int startRow, int startCol, int chan)
      {
         if( !average )
         {        
            for(int i=0; i< stDevs.length; i++)
               stDevs[i]= Math.sqrt(stDevs[i]);
            
            return;
         }
         
         Arrays.fill( BaseValues , 0 );
         int top,
             bottom, 
             left,
             right;
         
         top = bottom = left = right = 1;
         
         if( startRow-1 <1) 
            bottom = 0;
         
         if( startRow+Intensity.length-1 >= grid.num_rows())
            top = 0;
         
         if( startCol-1 < 1) 
            left=0;
         
         if( startCol+ Intensity[0].length -1 >= grid.num_cols( ))
            right =0;
         
         //boolean show = false;
         
         int lastRow = Intensity.length-1;
         
         int[] nIntensities = new int[4];
         Arrays.fill( nIntensities , 0 );

         int lastCol = Intensity[0].length -1;
         
         if( startRow-1 >=1)
         {
            for( int c =0; c< Intensity[0].length; c++)      
            {
              Data D =grid.getData_entry( startRow-1 , startCol+c );
              float intens = D.getY_values()[chan];
              
              float err =0;
              if( D.getErrors( )== null || D.getErrors().length <chan)
                 
                 err = intens;
              
              else
              {
                 err = D.getErrors()[chan];
                 err = err*err;
              }
             
              
              Intensity[0][c] +=intens;
           
              stDevs[c] += err;
              if( c-1>=0)
              {
                 Intensity[0][c-1] +=intens;
                 stDevs[c-1] +=err;
              }
              if( c+1 < Intensity[0].length)
              {
                 Intensity[0][c+1] +=intens;
                 stDevs[c+1] +=err;
              }
                 
            }
            if( left ==1 )
            {
               Data D =grid.getData_entry( startRow-1 , startCol-1 );
               
               float intens = D.getY_values()[chan];
               float err =0;
               
               if( D.getErrors( )== null || D.getErrors().length <chan)
                  err = intens;
               else
               {
                  err = D.getErrors()[chan];
                  err = err*err;
               }
               
               Intensity[0][0]+=intens;
               stDevs[0] +=err;
            }
            
            
            if ( right == 1 )
            {
               Data D = grid.getData_entry( startRow - 1 , startCol + lastCol
                     + 1 );
               
               float intens = D.getY_values( )[chan];
               float err = 0;
               
               if ( D.getErrors( ) == null || D.getErrors( ).length < chan )
                  err = intens;
               else
               {
                  err = D.getErrors( )[chan];
                  err = err * err;
               }
               
               Intensity[0][lastCol] += intens;
               stDevs[lastCol] += err;
            }
         }
        

         int lastRowStart = Ncols*(Intensity.length-1);
         if( startRow+1+lastRow <= grid.num_rows( ))
         {
            for( int c =0; c<= lastCol; c++)
            {

               Data D = grid.getData_entry(  startRow + 1 + lastRow ,
                     startCol + c );
               float intens = D.getY_values( )[chan];
               float err = 0;
               
               if ( D.getErrors( ) == null || D.getErrors( ).length < chan )
                  err = intens;
               else
               {
                  err = D.getErrors( )[chan];
                  err = err * err;
               }
              
               
               Intensity[Intensity.length - 1][c] += intens;
               stDevs[lastRowStart+c] +=err;
               if ( c - 1 >= 0 )
               {
                  Intensity[Intensity.length - 1][c - 1] += intens;
                  stDevs[lastRowStart+c-1] +=err;
               }
               if ( c + 1 < Intensity[Intensity.length - 1].length )
               {
                  Intensity[Intensity.length - 1][c + 1] += intens;
                  stDevs[lastRowStart+c+1] +=err;
               }

            }
            
            if( left ==1 )
            {
               Data D = grid.getData_entry(  startRow+lastRow+1 , startCol-1 );
               float intens = D.getY_values( )[chan];
               float err = 0;
               
               if ( D.getErrors( ) == null || D.getErrors( ).length < chan )
                  err = intens;
               else
               {
                  err = D.getErrors( )[chan];
                  err = err * err;
               }
              
               Intensity[Intensity.length-1][0]+=intens;
               stDevs[lastRowStart]+=err;
            }
            
            if( right==1)
            {
               Data D = grid.getData_entry(  startRow+lastRow+1 , startCol+lastCol+1);
               float intens = D.getY_values( )[chan];
               float err = 0;
               
               if ( D.getErrors( ) == null || D.getErrors( ).length < chan )
                  err = intens;
               else
               {
                  err = D.getErrors( )[chan];
                  err = err * err;
               }
              
               Intensity[Intensity.length-1][lastCol]+=intens;
               stDevs[lastRowStart+lastCol]+=err;
            }
        
         }
         
         if( startCol-1 >=1)
            for( int r =0; r< Intensity.length; r++)
            {

               Data D = grid.getData_entry(  startRow+r , startCol-1);
               float intens = D.getY_values( )[chan];
               float err = 0;
               
               if ( D.getErrors( ) == null || D.getErrors( ).length < chan )
                  err = intens;
               else
               {
                  err = D.getErrors( )[chan];
                  err = err * err;
               }
              
               
               Intensity[r][0] +=intens;
               stDevs[r*Ncols]+=err;
               
               if ( r - 1 >= 0 )
               {
                  Intensity[r - 1][0] += intens;
                  stDevs[( r - 1 ) * Ncols] += err;
               }
               if ( r + 1 < Intensity.length )
               {
                  Intensity[r + 1][0] += intens;
                  stDevs[( r + 1 ) * Ncols] += err;
               }
               
            }
        
         
         if( startCol+lastCol+1 <= grid.num_cols( ))
            for( int r =0; r< Intensity.length; r++)
            {
               Data D = grid.getData_entry(  startRow+r , startCol+lastCol+1);
               float intens = D.getY_values( )[chan];
               float err = 0;
               if ( D.getErrors( ) == null || D.getErrors( ).length < chan )
                  err = intens;
               else
               {
                  err = D.getErrors( )[chan];
                  err = err * err;
               }
              
               Intensity[r][lastCol] +=intens;
               stDevs[r*Ncols+lastCol] +=err;
 
               if( r-1>=0)
               {
                  Intensity[r-1][lastCol] +=intens;
                  stDevs[(r-1)*Ncols+lastCol] +=err;
               }
               if( r+1 < Intensity.length)
               {
                  Intensity[r+1][lastCol] +=intens;  
                  stDevs[(r+1)*Ncols+lastCol] +=err;    
               }
               
            }
       
         
         for( int r=0+1-bottom; r < Intensity.length -(1-top);r++)
            for( int c=0+1-left; c<=lastCol-(1-right); c++ )
            {
               Intensity[r][c]/=9;
               stDevs[r*Ncols+c]/=9;
               updateBV(r,c,Intensity[r][c]);
            }
         
         if( bottom ==0)
            for( int c=1-left; c<=lastCol-(1-right); c++ )
            {
               Intensity[0][c]/=6;
               updateBV(0,c,Intensity[0][c]);
               stDevs[c]/=6;
            }

         if( top ==0)
            for( int c=1-left; c<=lastCol-(1-right); c++ )
            {
               Intensity[Intensity.length-1][c]/=6;
               updateBV(Intensity.length-1,c,Intensity[Intensity.length-1][c]);

               stDevs[(Intensity.length-1)*Ncols+c]/=6;
            }
         

         if( left==0)
         {
            for( int r=1-bottom; r< Intensity.length-(1-top); r++ )        
            {
               Intensity[r][0]/=6;
               updateBV(r,0,Intensity[r][0]);
               stDevs[r*Ncols+0]/=6;
            }
         }

         if(right ==0)
         {
            for( int r=1-bottom; r< Intensity.length-(1-top); r++ )
            {
               Intensity[r][lastCol]/=6;

               updateBV(r,lastCol,Intensity[r][lastCol]);
               stDevs[r*Ncols+lastCol]/=6;
            }
         }
         
         if( left==0 && bottom ==0)
         {
            Intensity[0][0]/=4;
            updateBV(0,0,Intensity[0][0]);

            stDevs[0]/=4;
         }
         
         if( right==0 && bottom ==0)
         {
            Intensity[0][lastCol]/=4;
            updateBV(0,lastCol,Intensity[0][lastCol]);
            stDevs[lastCol]/=4;
         }
         
         if( left==0 && top ==0)
         {
            Intensity[Intensity.length-1][0]/=4;
            updateBV(Intensity.length-1,0,Intensity[Intensity.length-1][0]);
            stDevs[(Intensity.length-1)*Ncols+0]/=4;
         }
         
         if( right==0 && top ==0)
         {
            Intensity[Intensity.length-1][lastCol]/=4;
            updateBV(Intensity.length-1,lastCol,Intensity[Intensity.length-1][lastCol]);
            stDevs[(Intensity.length-1)*Ncols+lastCol]/=4;
         }
         
         for(int i=0; i< stDevs.length; i++)
            stDevs[i]= Math.sqrt(stDevs[i]);
         
         
        
     
      }
      
      public double[] getstDevs( boolean nonzeroMin)
      {
         double minStdev =0;
         if( nonzeroMin && average)
            minStdev =.1;
         else
             minStdev =1;
         
         double[] res = new double[stDevs.length];
         for( int i=0; i< res.length; i++)
            if( stDevs[i]< minStdev)
               res[i]= minStdev;
            else
               res[i]= stDevs[i];
         return res;
      }
      
      private float[][] CreateClearfloatArray( int Nrows, int Ncols)
      {
         float[][] Res = new float[Nrows][Ncols];
         for( int i=0; i< Nrows; i++)
            Arrays.fill( Res[i],0f );
         
         return Res;
      }
      
      private void AddToIntensity( float intensity,float stDev, int row, int col)
      {
         row = row-startRow;
         col = col-startCol;
         stDev = stDev*stDev;
         int x = row*Ncols+col;
         
        /* for( int  x1=Math.max(x-1 , 0 ); x1<= Math.min( x+1 , stDevs.length-1 ); x1++)
            for( int x2 = Math.max( x1-Ncols , x1 );x2 <= Math.min( x1+Ncols , stDevs.length-1 );x2+=Ncols)
               stDevs[x2] += stDev;
        */
         stDevs[x] += stDev;
         if( !average)
         {
            Intensity[row][col] += intensity;
            return;
         }
         if( x-Ncols >=0)
            stDevs[x-Ncols] += stDev;
         if( x+Ncols < stDevs.length)
            stDevs[x+Ncols] += stDev;
         if( col>0)//x-1 is at neg col which would wrap around
         {
            stDevs[x-1] +=stDev;

            if( x-1-Ncols >=0)
               stDevs[x-1-Ncols] += stDev;
            if( x-1+Ncols < stDevs.length)
               stDevs[x-1+Ncols] += stDev;
         }
         if( col+1 < Ncols)
         {

            stDevs[x+1] +=stDev;

            if( x+1-Ncols >=0)
               stDevs[x+1-Ncols] += stDev;
            if( x+1+Ncols < stDevs.length)
               stDevs[x+1+Ncols] += stDev;
         }
         
         
         if( Intensity == null || Intensity.length < 1 ||
               Intensity[0].length < 1)
            return;
         
         if( row <0 || col <0 || row >= Intensity.length ||
               col >=Intensity[0].length)
            return;
         int nrows = Intensity.length;
         int ncols = Intensity[0].length;
         for( int r = Math.max( 0,row-1); r<=Math.min( nrows-1 ,row+1); r++)
            for( int c=Math.max( 0,col-1); c<=Math.min( ncols-1 , col+1);c++)
               Intensity[r][c] +=intensity;
         
      }
      
      public double[] getAllParameters()
      {
         double[] P1 = super.getParameters();
         double[] Res = new double[7];
         System.arraycopy( P1,0,Res,0,4);
         System.arraycopy(P,4,Res,4,3);
         return Res;
      }
      
      //Using Henri Gavin paper-Duke University
      public double[] calcErrors()
      {
         double[] xs = new double[ ncells()];
         for( int xi=0; xi<xs.length; xi++)
            xs[xi] = xi;

         double[][]JTWJ= new double[this.numParameters( )][this.numParameters( )];
         for( int p=0; p<numParameters(); p++)
         {  double[] deriv1= get_dFdai(xs , p);
            double[] vals = getValues( xs);// in this case these are also the errs
                                           // to generalize, must get predicted y vals
            
            Arrays.fill( JTWJ[p],0.);
            for(int p2=p; p2< numParameters(); p2++)
            {
               double[] deriv2 = deriv1;
               if( p2 > p )
                  deriv2= get_dFdai(xs,p2);
                for( int i=0; i< ncells();i++)
                  JTWJ[p][p2] += vals[i]*vals[i]*deriv1[i]*deriv2[i];
                JTWJ[p2][p]= JTWJ[p][p2];
               
            }
         }
         
         double[][] invJTWJ= LinearAlgebra.getInverse( JTWJ );
         
         double[] Res = new double[numParameters()];
         for( int i=0; i< numParameters(); i++)
              Res[i] = Math.sqrt(  invJTWJ[i][i] /(ncells()-numParameters()+1));
           return Res;
      }
    
      /**
       * Parameters are means, stdev, etc. for sum of background +  normal
       */
      @Override
      public void setParameters(double[] params)
      {
         super.setParameters(params);
         
         int nparams = 4;
         if( edge) nparams =7;
         System.arraycopy( (params),0,P,0,nparams);//TODO 7);
         if( !edge)
            setSigmas(P,false);
         goodParameters = areParametersGood();
         
         // Will return NaN if parameters are out of whack
         //This causes problems with derivatives and with parameters close
         //   to the boundary
         if(!goodParameters)
            return;
         
         double uu = P[IVXX]*P[IVYY]- P[IVXY]*P[IVXY];
         //expCoef_z= -.5*P[IVXX]*P[IVYY]/uu;
         
         coeffNorm = .5/Math.PI/Math.sqrt( uu);
         
         expCoeffx2= -P[IVYY]/2/uu;
         expCoeffxy = P[IVXY]/uu;
         expCoeffy2= -P[IVXX]/2/uu;
         int x=0;
         for( int r = startRow; r < startRow+Nrows; r++)
            for( int c= startCol; c < startCol+Ncols; c++)
            {
               double dx = c-P[IXMEAN];
               double dy = r-P[IYMEAN];
               expVals[x]=Math.exp( expCoeffx2*dx*dx+expCoeffxy*dx*dy+expCoeffy2*dy*dy);
               x++;
            }
         dVdmx = dVdmy = dVdsx = dVdsy =dVdsxy = null;
         
         for( int p=0; p<7; p++)
         {
            derivativeRules[p].setCommonData( expVals , coeffNorm , expCoeffx2 , expCoeffy2 , expCoeffxy );
         }
         //For converting to 4 parameters
         double denom = BaseValues[S_int] - P[IBACK] * BaseValues[S_1];
         double NumSx =P[IVXX]*denom;
         double NumSy =P[IVYY]*denom;
         double NumSxy =P[IVXY]*denom;
         
         if( edge)
         {
            dSxdmx = dSxdmy = dSxdb =  dSydmx =dSydmy =dSydb =dSxydmx = dSxydmy=0;
            
         }else
         {
         
         dSxdmx=2*(-BaseValues[S_xint]+P[IXMEAN]*BaseValues[S_int]+
                 P[IBACK]*BaseValues[S_x]-P[IBACK]*P[IXMEAN]*BaseValues[S_1])
                 /denom;
         
         dSxdmy=0;
         
         dSxdb=-(BaseValues[S_x2]-2*P[IXMEAN]*BaseValues[S_x]+P[IXMEAN]*P[IXMEAN]*BaseValues[S_1])
                    /denom  +NumSx*BaseValues[S_1]/denom/denom;
         dSydmx=0;
         
         dSydmy=2*(-BaseValues[S_yint]+P[IYMEAN]*BaseValues[S_int]+
               P[IBACK]*BaseValues[S_y]-P[IBACK]*P[IYMEAN]*BaseValues[S_1])
               /denom;
         
         dSydb=-(BaseValues[S_y2]-2*P[IYMEAN]*BaseValues[S_y]+P[IYMEAN]*P[IYMEAN]*BaseValues[S_1])
                                        /denom + NumSy*BaseValues[S_1]/denom/denom                                                                             ;
         
         dSxydmx=(-BaseValues[S_yint] +P[IBACK]*BaseValues[S_y]+
                                P[IYMEAN]*(BaseValues[S_int]-P[IBACK]*BaseValues[S_1]) )/denom;
         
         dSxydmy=(-BaseValues[S_xint] +P[IBACK]*BaseValues[S_x]+
               P[IXMEAN]*(BaseValues[S_int]-P[IBACK]*BaseValues[S_1]) )/denom;
         
         dSxydb= (-BaseValues[S_xy]+P[IYMEAN]*BaseValues[S_x]+
                   P[IXMEAN]*BaseValues[S_y]-P[IXMEAN]*P[IYMEAN]*BaseValues[S_1])
                   /denom    +NumSxy*BaseValues[S_1]/denom/denom;
         }
         
      }
      
      public boolean areParametersGood()
      {
         if( P[IBACK] < 0 || P[ITINTENS] < 0)
            return false;
         
         if( P[IYMEAN]<=startRow-1 || 
               P[IXMEAN] <startCol-1)
            return false;
         
         if( P[IYMEAN] >=startRow+ Nrows +1 ||
               P[IXMEAN] > startCol +Ncols+1)
            return false;
       
         
         if( P[IXMEAN] < BadEdgeRange+2 || P[IXMEAN] > grid.num_cols( )-BadEdgeRange-2)
            return false;
         
         if( P[IYMEAN] < BadEdgeRange+2|| P[IYMEAN] > grid.num_rows( )-BadEdgeRange-2)
            return false;
         
         for( int i=4; i<=5;i++)
            if( P[i] <=0)
               return false;
         
         double XX = P[IVXX]*P[IVYY]-P[IVXY]*P[IVXY];
         
         if( XX <=0)
            return false;
         
        
         if( P[IVXX]+P[IVYY]> 2.6*XX) //Too steep of normal
         {              // 1 pixel away from peak is 30% max 
           //height in theory.
            return false;
         }
         
         if( !edge)  //Variances are calculated from the data
            return true;
         
         //clamp down on standard deviations
         if( P[IVXX] < .5*StdDevx*StdDevx)
            return false;
         

         if( P[IVXX] > 2*StdDevx*StdDevx)
            return false;
         

         if( P[IVYY] < .5*StdDevy*StdDevy)
            return false;
         

         if( P[IVYY] > 2*StdDevy*StdDevy)
            return false;
         
         return true;
         
      }
      
   
      public void Test()
      {
         double x = (row-startRow)*Ncols +(col-startCol);
         //double[] pp = getParameters();
         P[IBACK]=0;
         P[ITINTENS]=0;
         
         System.out.println( "TTT= Int at rc=("+row+","+col+")="+getValue(x));
      }
      @Override
      public ClosedInterval getDomain()
      {

         
         return new ClosedInterval(0,ncells());
      }

     

      @Override
      public double getValue(double x)
      {
         if( !goodParameters)
            return Double.NaN;
         
        int i = (int) x;
        
        int r = startRow+ (int)(i/Ncols);
        int c = startCol+i%Ncols;
        
        float intensity = Intensity[r-startRow][c-startCol]; 
       
        double err = P[IBACK]+ P[ITINTENS]*coeffNorm*expVals[(int)x] -
                    intensity;
        
        return err;
        
      }

      @Override
      public float getValue(float x)
      {
         return (float)getValue((double)x);
      }

      @Override
      public double[] getValues(double[] x)
      {

         if( x == null )
            return null;
         
        double[] Res = new double[x.length];
        
        for( int i=0; i< x.length; i++)
           Res[i]= getValue(x[i]);
         
         return Res;
      }

      @Override
      public float[] getValues(float[] x)
      {

         if( x == null )
            return null;
         
         float[] Res = new float[x.length];
         
         for( int i=0; i< x.length; i++)
            Res[i]= getValue(x[i]);
          
          return Res;
      }

      @Override
      public void setDomain(ClosedInterval interval)
      {

        
         
      }
      /**
       * Estimates "good" values for initial standard deviations and 
       * correlations and sets them into the parameters array
       * 
       * @param parameters
       */
      private void setSigmas(double[] P, boolean FixSxy)
      {
         //use only full  quadrants. Get GOOD Sxx and Syy values and limits
         
         double Sxx = BaseValues[S_x2int] - P[IBACK] * BaseValues[S_x2]
               - 2 * P[IXMEAN]
               * ( BaseValues[S_xint] - P[IBACK] * BaseValues[S_x] )
               + P[IXMEAN] * P[IXMEAN]
               * ( BaseValues[S_int] - P[IBACK] * BaseValues[S_1] );

         Sxx = Sxx / ( BaseValues[S_int] - P[IBACK] * BaseValues[S_1] );

         double Syy = BaseValues[S_y2int] - P[IBACK] * BaseValues[S_y2]
               - 2 * P[IYMEAN]
               * ( BaseValues[S_yint] - P[IBACK] * BaseValues[S_y] )
               + P[IYMEAN] * P[IYMEAN]
               * ( BaseValues[S_int] - P[IBACK] * BaseValues[S_1] );
         Syy = Syy / ( BaseValues[S_int] - P[IBACK] * BaseValues[S_1] );

         double Sxy = BaseValues[S_xyint] - P[IBACK] * BaseValues[S_xy]
               - P[IXMEAN]
               * ( BaseValues[S_yint] - P[IBACK] * BaseValues[S_y] )
               - P[IYMEAN]
               * ( BaseValues[S_xint] - P[IBACK] * BaseValues[S_x] )
               + P[IYMEAN] * P[IXMEAN]
               * ( BaseValues[S_int] - P[IBACK] * BaseValues[S_1] );
         Sxy = Sxy / ( BaseValues[S_int] - P[IBACK] * BaseValues[S_1] );
         
         
         P[IVXX]= Sxx;
         P[IVYY] = Syy;
         if( FixSxy)
         while( Sxx*Syy <= Sxy*Sxy  && Sxx >0  && Syy > 0)
            Sxy/=2;
         
         P[IVXY]= Sxy;
      }
      
     /* private void setSigmas1(double[] parameters)
      {
         double Sxx =0;
         double Syy = 0;
         double Sxy = 0;
         int N, N1;
         int[] Quad = null;
         double[][] BaseValues = null;
         int Nquadrants = 4;
         N=N1=0;
         //use only full  quadrants. Get GOOD Sxx and Syy values and limits
         for( int i = 1; i< Nquadrants; i++)
         {
            
           int q= Quad[i];
           Sxx += BaseValues[S_x2int][q] - P[IBACK] * BaseValues[S_x2][q]
               - 2 * P[IXMEAN]
               * ( BaseValues[S_xint][q] - P[IBACK] * BaseValues[S_x][q] )
               + P[IXMEAN] * P[IXMEAN]
               * ( BaseValues[S_int][q] - P[IBACK] * BaseValues[S_1][q] );

         N+= ( BaseValues[S_int] [q]- P[IBACK] * BaseValues[S_1][q] );

         Syy += BaseValues[S_y2int][q] - P[IBACK] * BaseValues[S_y2][q]
               - 2 * P[IYMEAN]
               * ( BaseValues[S_yint][q] - P[IBACK] * BaseValues[S_y][q] )
               + P[IYMEAN] * P[IYMEAN]
               * ( BaseValues[S_int][q] - P[IBACK] * BaseValues[S_1][q] );
        

         double u = BaseValues[S_xyint][q] - P[IBACK] * BaseValues[S_xy][q]
               - P[IXMEAN]
               * ( BaseValues[S_yint][q] - P[IBACK] * BaseValues[S_y][q] )
               - P[IYMEAN]
               * ( BaseValues[S_xint][q] - P[IBACK] * BaseValues[S_x][q] )
               + P[IYMEAN] * P[IXMEAN]
               * ( BaseValues[S_int][q] - P[IBACK] * BaseValues[S_1][q] );
         Sxy +=u;
         N1+= ( BaseValues[S_int] [q]- P[IBACK] * BaseValues[S_1][q] );
         boolean hasXmirror = false;
         boolean hasYmirror = false;
         for( int qq =0; qq<Nquadrants; qq++)
         {
            if( Quad[i] != q && Math.abs( Quad[i]-q )==2)
               hasXmirror = true;
            if( Quad[i] != q && Math.abs( Quad[i]-q )==1)
               hasYmirror = true;
         }
         if( !hasXmirror)
         {
            Sxy +=-u;
            N1+=( BaseValues[S_int] [q]- P[IBACK] * BaseValues[S_1][q] );;
         }

         if( !hasYmirror)
         {
            Sxy +=-u;
            N1+=( BaseValues[S_int] [q]- P[IBACK] * BaseValues[S_1][q] );;
         }
         
         }
         Sxx /=N;
         Sxy/=N;
         Sxy /=N1;
         
         P[IVXX]= Sxx;
         P[IVYY] = Syy;
      
         while( Sxx*Syy < Sxy*Sxy  && Sxx >0  && Syy > 0)
            Sxy/=2;
         
         P[IVXY]= Sxy;
      }

      */
      public double[] get_dFdai( double x[], int i )
      {
         
         double[] Res = derivativeRules[i].get_dFdp( x );
         updateDerivsSigmas();
         double a,b,c;
         if( i==0)
         {
            a= dSxdb;
            b= dSydb;
            c= dSxydb;
         }else if( i==1)
         {
            a= dSxdmx;
            b=dSydmx;
            c=dSxydmx;
            
         }else if( i==2)
         {

            a= dSxdmy;
            b=dSydmy;
            c=dSxydmy;
         }else
         {

            a= 0;
            b=0;
            c=0;
         }
         for( int j=0; j< x.length; j++)
            Res[j] +=dVdsx[j]*a+dVdsy[j]*b+dVdsxy[j]*c;
         return Res;
      }
      
      public double get_dFdai( double x, int i )
      {
         double Res= derivativeRules[i].get_dFdp( x );
         updateDerivsSigmas();

         double a,b,c;
         if( i==0)
         {
            a= dSxdb;
            b=dSydb;
            c=dSxydb;
         }else if( i==1)
         {
            a= dSxdmx;
            b=dSydmx;
            c=dSxydmx;
            
         }else if( i==2)
         {

            a= dSxdmy;
            b=dSydmy;
            c=dSxydmy;
         }else
         {

            a= 0;
            b=0;
            c=0;
         }

            Res +=dVdsx[(int)x]*a+dVdsy[(int)x]*b+dVdsxy[(int)x]*c;
         
         
         return Res;
      }
      public float[] get_dFdai( float x[], int i )
      {
         float[] Res = derivativeRules[i].get_dFdp( x );
         updateDerivsSigmas();

         double a,b,c;
         if( i==0)
         {
            a= dSxdb;
            b=dSydb;
            c=dSxydb;
         }else if( i==1)
         {
            a= dSxdmx;
            b=dSydmx;
            c=dSxydmx;
            
         }else if( i==2)
         {

            a= dSxdmy;
            b=dSydmy;
            c=dSxydmy;
         }else
         {

            a= 0;
            b=0;
            c=0;
         }
         for( int j=0; j< x.length; j++)
            Res[j] +=(float)(dVdsx[j]*a+dVdsy[j]*b+dVdsxy[j]*c);

         
         return Res;
      }
      public float get_dFdai( float x, int i )
      {
         float Res = derivativeRules[i].get_dFdp( x );
         updateDerivsSigmas();

         double a,b,c;
         if( i==0)
         {
            a= dSxdb;
            b=dSydb;
            c=dSxydb;
         }else if( i==1)
         {
            a= dSxdmx;
            b=dSydmx;
            c=dSxydmx;
            
         }else if( i==2)
         {

            a= dSxdmy;
            b=dSydmy;
            c=dSxydmy;
         }else
         {

            a= 0;
            b=0;
            c=0;
         }
         int j = (int)x;
         Res +=dVdsx[j]*a+dVdsy[j]*b+dVdsxy[j]*c;
         
         
         return Res;
      }
      
    private void  updateDerivsSigmas()
    {
       if( dVdsx != null && dVdsy != null && dVdsxy != null)
          return;
       double[] x = new double[ expVals.length];
       for( int i=0; i< x.length; i++)
          x[i]=i;
       dVdsx =derivativeRules[4].get_dFdp( x );
       dVdsy =derivativeRules[5].get_dFdp( x );
       dVdsxy =derivativeRules[6].get_dFdp( x );
       
       
    }
   }
  
   interface Deriv
   {
      public float get_dFdp( float x);
      public double get_dFdp( double x);
      public float[] get_dFdp( float[] x);
      public double[] get_dFdp( double[] x);
      public void setCommonData( double[]x, double coefNorm, 
            double expCoeffx2, double expCoeffy2, double expCoeffxy);
      
   }
   
   static class Derivb implements Deriv
   {

      @Override
      public double get_dFdp(double x)
      {

         
         return 1.0;
      }

      @Override
      public double[] get_dFdp(double[] x)
      {

         double[] Res = new double[x.length];
         Arrays.fill( Res , 1 );
         return Res;
      }

      @Override
      public float get_dFdp(float x)
      {

         
         return 1f;
      }

      @Override
      public float[] get_dFdp(float[] x)
      {


         float[] Res = new float[x.length];
         Arrays.fill( Res , 1 );
         return Res;
      }

      @Override
      public void setCommonData(double[] x, double coefNorm, double expCoeffx2,
            double expCoeffy2, double expCoeffxy)
      {

         // TODO Auto-generated method stub
         
      }
      
   }
   
   static class DerivI implements Deriv
   {
      double[] parameters; 
      int startRow; 
      int startCol; 
      int Nrows;
      int Ncols;
      double[] xx;
      double coefNorm;
       double expCoeffx2;
      double expCoeffy2;
       double expCoeffxy;
      
      public DerivI( double[] parameters, int startRow, int startCol, int Nrows,int Ncols)
      {
         this.parameters=parameters;
         this.startRow=startRow;
         this.startCol=startCol;
         this.Nrows = Nrows;
         this.Ncols=Ncols;
      }
      @Override
      public double get_dFdp(double x)
      {
         if( xx == null || x < 0 || (int)x >= xx.length)
            return Double.NaN;
         return xx[(int)x]*coefNorm;
      }

      @Override
      public double[] get_dFdp(double[] x)
      {

         double[] Res = new double[x.length];
         for( int i=0; i< Res.length; i++)
            Res[i]=get_dFdp( x[i]);
         return Res;
      }

      @Override
      public float get_dFdp(float x)
      {
         if( xx == null || x < 0 || (int)x >= xx.length)
            return Float.NaN;
        return (float)(get_dFdp((double)x));
      }

      @Override
      public float[] get_dFdp(float[] x)
      {

         float[] Res = new float[x.length];
         for( int i=0; i< Res.length; i++)
            Res[i]=get_dFdp( x[i]);
         return Res;
      }

     
      @Override
      public void setCommonData(double[] xx, double coefNorm, double expCoeffx2,
            double expCoeffy2, double expCoeffxy)
      {


         this.xx = xx;
         this.coefNorm =coefNorm;
         this.expCoeffx2 =expCoeffx2;
         this.expCoeffy2 =expCoeffy2;
         this.expCoeffxy =expCoeffxy;
         
      }
      
   }
   
   static class Derivmx extends DerivI
   {
      
      
      public Derivmx(double[] parameters, int startRow, int startCol, int Nrows, int Ncols)
      {
         super( parameters, startRow,  startCol, Nrows, Ncols);
        
      }
      @Override
      public double get_dFdp(double x)
      {
         int r = ((int) x)/Ncols;
         int c = ((int)x) %Ncols;
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         return get_dFdp(x,r,c, coefNorm*parameters[ITINTENS], parameters[IVYY]/uu,-parameters[IVXY]/uu);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx=parameters[IVYY]/uu; 
         double coefy=-parameters[IVXY]/uu;
         
         double[] Res = new double[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
            {
               Res[k]= get_dFdp(x[k],r,c,coefExp, coefx,coefy);
               k++;
            }
               

         // TODO Auto-generated method stub
         return Res;
      }
      @Override
      public float[] get_dFdp(float[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx=parameters[IVYY]/uu; 
         double coefy=-parameters[IVXY]/uu;
         
         float[] Res = new float[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
            {
               Res[k]= (float)get_dFdp(x[k],r,c,coefExp, coefx,coefy);
               k++;
            }
         
         return Res;
      }
      
      
      private double get_dFdp( double x, int r, int c,
            double coefExp,double coefx, double coefy)
      {
         try
         {
             return coefExp*this.xx[(int)x]*(coefx*(c-parameters[IXMEAN])+coefy*(r-parameters[IYMEAN]));
         }catch(Exception ss)
         {
            return Double.NaN;
         }
      }
      
   }

   
   
   static class Derivmy extends DerivI
   {
      
      
      public Derivmy(double[] parameters, int startRow, int startCol, int Nrows, int Ncols)
      {
         super( parameters, startRow,  startCol, Nrows, Ncols);
        
      }
      @Override
      public double get_dFdp(double x)
      {
         int r = ((int) x)/Ncols;
         int c = ((int)x) %Ncols;
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         return get_dFdp(x,r,c, coefNorm*parameters[ITINTENS], -parameters[IVXY]/uu,parameters[IVXX]/uu);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx=-parameters[IVXY]/uu; 
         double coefy= parameters[IVXX]/uu;
         
         double[] Res = new double[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
            {
               Res[k]= get_dFdp(x[k],r,c,coefExp, coefx,coefy);
               k++;
            }
               

         // TODO Auto-generated method stub
         return Res;
      }
      @Override
      public float[] get_dFdp(float[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx= -parameters[IVXY]/uu; 
         double coefy= parameters[IVYY]/uu;
         
         float[] Res = new float[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
            {
               Res[k]= (float)get_dFdp(x[k],r,c,coefExp, coefx,coefy);
               k++;
            }
         
         return Res;
      }
      
      
      private double get_dFdp( double x, int r, int c,
            double coefExp,double coefx, double coefy)
      {
         try
         {
         return coefExp*this.xx[(int)x]*(coefx*(c-parameters[IXMEAN])+coefy*(r-parameters[IYMEAN]));
         }catch( Exception s)
         {
            return Double.NaN;
         }
         
      }
      
   }
   
   static class DerivSxx extends DerivI
   {
      
      
      public DerivSxx(double[] parameters, int startRow, int startCol, int Nrows, int Ncols)
      {
         super( parameters, startRow,  startCol, Nrows, Ncols);
        
      }
      @Override
      public double get_dFdp(double x)
      {
         int r = ((int) x)/Ncols;
         int c = ((int)x) %Ncols;
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx2=parameters[IVYY]*parameters[IVYY]/2/uu/uu; 
         double coefy2=parameters[IVXY]*parameters[IVXY]/2/uu/uu;
         double coefxy =-parameters[IVXY]*parameters[IVYY]/uu/uu;
         double C =-parameters[IVYY]/2/uu;
         return get_dFdp(x,r,c,coefExp, C,coefx2, coefxy,
               coefy2);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx2=parameters[IVYY]*parameters[IVYY]/2/uu/uu; 
         double coefy2=parameters[IVXY]*parameters[IVXY]/2/uu/uu;
         //Should be coefy2=(+parameters[IVYY]*parameters[IVYY]-uu)/2/uu/uu;
         double coefxy =-parameters[IVXY]*parameters[IVYY]/uu/uu;
         double C =-parameters[IVYY]/2/uu;
         
         double[] Res = new double[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
            {
               Res[k]= get_dFdp(x[k],r,c,coefExp, C,coefx2, coefxy,
                     coefy2);
               k++;
            }
               

         // TODO Auto-generated method stub
         return Res;
      }
      @Override
      public float[] get_dFdp(float[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx2=parameters[IVYY]*parameters[IVYY]/2/uu/uu; 
         double coefy2=parameters[IVXY]*parameters[IVXY]/2/uu/uu;
         double coefxy =-parameters[IVXY]*parameters[IVYY]/uu/uu;
         double C =-parameters[IVYY]/2/uu;
         
         float[] Res = new float[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)  
            {
               Res[k]= (float)get_dFdp(x[k],r,c,coefExp, C,coefx2, coefxy,
                  coefy2);
               k++;
            }
         
         return Res;
      }
      
      
      private double get_dFdp( double x, int r, int c,
            double coefExp,double C,double coefx2, double coefxy,
            double coefy2)
      {
         try
         {
         return coefExp*this.xx[(int)x]*(C+coefx2*(c-parameters[IXMEAN])*(c-parameters[IXMEAN])
               +coefxy*(r-parameters[IYMEAN])*(c-parameters[IXMEAN])+
               coefy2*(r-parameters[IYMEAN])*(r-parameters[IYMEAN]));
         }catch(Exception s)
         {
            return Double.NaN;
         }
      }
      
   }
   
   static class DerivSyy extends DerivI
   {
      
      
      public DerivSyy(double[] parameters, int startRow, int startCol, int Nrows, int Ncols)
      {
         super( parameters, startRow,  startCol, Nrows, Ncols);
        
      }
      @Override
      public double get_dFdp(double x)
      {
         int r = ((int) x)/Ncols;
         int c = ((int)x) %Ncols;
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx2=parameters[IVXY]*parameters[IVXY]/2/uu/uu; 
         double coefy2=parameters[IVXX]*parameters[IVXX]/2/uu/uu;
         double coefxy =-parameters[IVXY]*parameters[IVXX]/uu/uu;
         double C =-parameters[IVXX]/2/uu;
         return get_dFdp(x,r,c,coefExp, C,coefx2, coefxy,
               coefy2);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx2=parameters[IVXY]*parameters[IVXY]/2/uu/uu;
         //should be  coefx2=(-uu+parameters[IVXX]*parameters[IVYY])/2/uu/uu;
         double coefy2=parameters[IVXX]*parameters[IVXX]/2/uu/uu;
         
         double coefxy =-parameters[IVXY]*parameters[IVXX]/uu/uu;
         double C =-parameters[IVXX]/2/uu;//*-1??
         
         double[] Res = new double[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
            {
               Res[k]= get_dFdp(x[k],r,c,coefExp, C,coefx2, coefxy,
                     coefy2);
               k++;
            }
               

         // TODO Auto-generated method stub
         return Res;
      }
      @Override
      public float[] get_dFdp(float[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx2=parameters[IVXY]*parameters[IVXY]/2/uu/uu; 
         double coefy2=parameters[IVXX]*parameters[IVXX]/2/uu/uu;
         double coefxy =-parameters[IVXY]*parameters[IVXX]/uu/uu;
         double C =-parameters[IVXX]/2/uu;
         
         float[] Res = new float[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++) 
            {
               Res[k]= (float)get_dFdp(x[k],r,c,coefExp, C,coefx2, coefxy,
                  coefy2);
               k++;
            }
         
         return Res;
      }
      
      
      private double get_dFdp( double x, int r, int c,
            double coefExp,double C,double coefx2, double coefxy,
            double coefy2)
      {
         try
         {
         return coefExp*this.xx[(int)x]*(C+coefx2*(c-parameters[IXMEAN])*(c-parameters[IXMEAN])
               +coefxy*(r-parameters[IYMEAN])*(c-parameters[IXMEAN])+
               coefy2*(r-parameters[IYMEAN])*(r-parameters[IYMEAN]));
         }catch(Exception s)
         {
            return Double.NaN;
         }
      }
      
   }
   
   static class DerivSxy extends DerivI
   {
      //May be a little off Check again
      
      public DerivSxy(double[] parameters, int startRow, int startCol, int Nrows, int Ncols)
      {
         super( parameters, startRow,  startCol, Nrows, Ncols);
        
      }
      @Override
      public double get_dFdp(double x)
      {
         int r = ((int) x)/Ncols;
         int c = ((int)x) %Ncols;
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx2= -parameters[IVYY]*parameters[IVXY]/uu/uu; 
         double coefy2=  -parameters[IVXX]*parameters[IVXY]/uu/uu;
         double coefxy =(uu+2*parameters[IVXY]*parameters[IVXY])/uu/uu;
         double C = parameters[IVXY]/uu;
         return get_dFdp(x,r,c,coefExp, C,coefx2, coefxy,
               coefy2);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx2= -parameters[IVYY]*parameters[IVXY]/uu/uu; 
         double coefy2=  -parameters[IVXX]*parameters[IVXY]/uu/uu;
         double coefxy =(uu+2*parameters[IVXY]*parameters[IVXY])/uu/uu;
         double C = parameters[IVXY]/uu;
         
         double[] Res = new double[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
            {
               Res[k]= get_dFdp(x[k],r,c,coefExp, C,coefx2, coefxy,
                     coefy2);
               k++;
            }
               

         // TODO Auto-generated method stub
         return Res;
      }
      @Override
      public float[] get_dFdp(float[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx2= -parameters[IVYY]*parameters[IVXY]/uu/uu; 
         double coefy2=  -parameters[IVXX]*parameters[IVXY]/uu/uu;
         double coefxy =(uu+2*parameters[IVXY]*parameters[IVXY])/uu/uu;
         double C = parameters[IVXY]/uu;
         
         float[] Res = new float[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)  
            {
               Res[k++]= (float)get_dFdp(x[k],r,c,coefExp, C,coefx2, coefxy,
                  coefy2);
               k++;
            }
         
         return Res;
      }
      
      
      private double get_dFdp( double x, int r, int c,
            double coefExp,double C,double coefx2, double coefxy,
            double coefy2)
      {
         try
         {
         return coefExp*this.xx[(int)x]*(C+coefx2*(c-parameters[IXMEAN])*(c-parameters[IXMEAN])
               +coefxy*(r-parameters[IYMEAN])*(c-parameters[IXMEAN])+
               coefy2*(r-parameters[IYMEAN])*(r-parameters[IYMEAN]));
         }catch(Exception s)
         {
            return Double.NaN;
         }
      }
      
   }
}
