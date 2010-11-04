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

import javax.swing.JOptionPane;
import javax.swing.*;

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
    * @return void, but the Peak object has the inti and sigi fields set.
    */
   public static void IntegratePeak( Peak_new Peak, DataSet DS, int nPixels, int nTimeChans,
        int NBoundaryPixels, StringBuffer logBuffer)
   {
      float TotIntensity =0;
      float TotVariance =0;
      
      int i= Peak.seqnum( );
      int run = Peak.nrun( );
      int det = Peak.detnum( );
      int Chan = (int) Peak.z( );
      int nTimes = nTimeChans;
      System.out.println("Peak num #time channels="+Peak.seqnum( )+","+nTimeChans);
      try
      {
         logBuffer.append( String.format("Peak,run,det=%3d %4d %3d\n",i+1,run,det).getBytes());
         logBuffer.append(  String.format("   Pixel width %3dchan %4d,, chanMin %4d,chanMax %4d\n",
                        nPixels+1,Chan+1, Chan+1-(nTimes-1)/2, Chan+1+(nTimes-1)/2).getBytes() );
         logBuffer.append( "   ----------------Slices --------------------\n".getBytes());
         logBuffer.append((" chan    back  Intens(P)   mx       my   sigm(Cll) ncells Intens(Tot) Intens(Tot-back) errI"
               +"     bk_res     Varx    Vary        Vxy \n").getBytes());
         
      }catch(Exception ss)
      {
         
      }
      boolean done = false;
      //last y values is at xscl.getNum_x( )-2 on a histogram
      IDataGrid grid = Peak.getGrid( );
      
      Data D = grid.getData_entry((int)(.5f+Peak.y()),  (int)(.5f+Peak.x()) );
      XScale xscl = D.getX_scale( );
      int BadEdgeWidth =NBoundaryPixels;
      for ( int chan = Math.max( 0 , Chan-(nTimes-1)/2); !done && chan <= 
         Math.min( Chan+(nTimes-1)/2,xscl.getNum_x( )-2); chan++)
      {
         
       
         
         OneSlice slice = new OneSlice(grid, chan,(int)(.5f+Peak.y()),
              (int)(.5f+Peak.x()), nPixels,nPixels);
         slice.BadEdgeRange = BadEdgeWidth;
         double[] xs = new double[ slice.ncells( )];
         double[] ys = new double[ xs.length];
         double[]sigs = new double[ xs.length];
         Arrays.fill( ys,0 );
         Arrays.fill( sigs,1 );
         
         //sigs[3] =.2;//Integrated intensity should be able to change faster
                     // than other paramters. It is larger
         for( int ii=0; ii< xs.length; ii++)
            xs[ii]=ii;
         double MaxErrChiSq = .00001*slice.ncells( );
         MarquardtArrayFitter fitter = new MarquardtArrayFitter( slice, xs,ys,sigs,MaxErrChiSq, 200);
         double chiSqr = fitter.getChiSqr( );
      
         double[] errs = fitter.getParameterSigmas( );//Use other for cases when params near
                                                        //boundaries
         
         
         double[] DD = slice.getParameters();
         //AdjustDD( DD, slice.startRow, slice.nrows(), slice.startCol,slice.ncols() );
         char GoodSlicec =' ';
         if(!Double.isNaN( chiSqr )  && 
                GoodSlice(  DD, errs[3]*Math.sqrt( chiSqr/slice.ncells() ),nPixels,nPixels,grid,10 ))
            GoodSlicec ='x';
         
         if( chan == Chan  && GoodSlicec == 'x')
         {
            System.out.print((i+1)+","+(1/Peak.d())+","+
                 new DetectorPosition( Peak.getGrid( ).position(Peak.y( ),Peak.x())).getScatteringAngle( )
                 *180/Math.PI+","+Math.sqrt( DD[4] )+","+Math.sqrt( DD[5] ));            
            //slice.Test( );
         }
        
         try
         {
           
           
           logBuffer.append( 
                 String.format("%5d %7.3f %8.3f%c %8.3f %8.3f %8.3f %6d %10.2f %10.2f"+
                                               " %13.3f %8.5f %9.5f %9.5f %9.5f\n",
                 chan+1,
                 DD[0],
                 DD[3],GoodSlicec,
                 DD[1],
                 DD[2],
                 Math.sqrt( chiSqr/slice.ncells()),
                 slice.ncells( ),
                 slice.getInitialTotIntensity( ),
                 (slice.getInitialTotIntensity( )-slice.getParameters( )[0]*
                       slice.ncells( )),errs[3]*Math.sqrt( chiSqr/slice.ncells() ),
                 slice.getAvBackGroundLeft( ),
                 DD[4],
                 DD[5],
                 DD[6])
                      .getBytes() );
         }catch(Exception s2)
         {
            
         }
         if ( GoodSlicec=='x')
         {
            TotIntensity += DD[3];
            double Err = errs[3];
            TotVariance += Err * Err * chiSqr / slice.ncells( );
            
         }else if( chan < Chan)
         {
            TotIntensity =0;
            TotVariance = 0;
         }else
            done = true;
         
         
      }
      
      
      float stDev = (float)Math.sqrt( TotVariance );
      System.out.println("   I,sigI="+ TotIntensity+","+stDev);
      try
      {

         logBuffer.append( "   ----------------End Slices --------------------\n".getBytes());
         logBuffer.append( String.format(
               "Tot Intensity(-back) %7.2f, stDev= %7.3f\n\n", TotIntensity, stDev).getBytes() );
         logBuffer.append("---------------------------New Peak------------------------\n".getBytes());
         
      }catch(Exception sss)
      {
         
      }
      
      if ( !Float.isNaN( TotIntensity ) && !Float.isNaN( stDev ) )
      {
         Peak.inti( ( float ) TotIntensity );
         Peak.sigi( stDev );
         
      }else
      {
         Peak.inti( 0);
         Peak.sigi(0); 
      }
     
   }
   
 
   /**
    *  This can be started with two arguments, the peaks file and the nexus 
    *  file corresponding to the peak file.  The peak file must only span one
    *   run, currently, and its peaks must be in the "correct" order
    *  
    * @param args  
    */
   public static void main( String[] args)
   {
      String PeaksFile ="C:/ISAW/SampleRuns/SNS/TOPAZ/WSF/top1172/nickel.peaks";
      String NeXusFile = "C:/ISAW/SampleRuns/SNS/TOPAZ/TOPAZ_1172.nxs";
      int BadEdgeWidth = 10;
      float MaxCellEdge =12;
      if( args != null && args.length >0)
         {
         PeaksFile = args[0];
         NeXusFile = args[1];
        
         }else
         {
            FileChooserPanel Peak = new FileChooserPanel(FileChooserPanel.LOAD_FILE ,
                  "Peaks File");
            FileChooserPanel NexFile = new FileChooserPanel(FileChooserPanel.LOAD_FILE ,
            "NeXus File");
            JPanel BadEdgePanel = new JPanel( new GridLayout(1,2));
            BadEdgePanel.add( new JLabel("# Bad Edge Pixels") );
            JTextField BadEdge = new FilteredPG_TextField(  new IntegerFilter() );
            BadEdgePanel.add(BadEdge);
            

            JPanel CellSidePanel = new JPanel( new GridLayout(1,2));
            CellSidePanel.add( new JLabel("Max real cell side") );
            JTextField CellSide = new FilteredPG_TextField(  new IntegerFilter() );
            CellSidePanel.add(CellSide);
            
            BadEdge.setText("10");
            CellSide.setText("12");
            JPanel panel = new JPanel();
            panel.setLayout(  new GridLayout(4,1) );
            panel.add( Peak);
            panel.add( NexFile);
            panel.add( BadEdgePanel);
            panel.add( CellSidePanel);
            int OptChoice = JOptionPane.showConfirmDialog( null, panel,"Input Files",
                  JOptionPane.OK_CANCEL_OPTION);
            if(OptChoice == JOptionPane.OK_OPTION)
            {
               PeaksFile = Peak.getTextField( ).getText( ).trim();
               NeXusFile = NexFile.getTextField( ).getText( ).trim();
               BadEdgeWidth = Integer.parseInt( BadEdge.getText( ).trim());
               MaxCellEdge =Integer.parseInt(CellSide.getText( ).trim());
               
            }else if( OptChoice == JOptionPane.CANCEL_OPTION)
               return;
            
            panel.removeAll( );
            Peak = null;
            NexFile = null;
            BadEdgePanel = CellSidePanel = panel = null;
            BadEdge= CellSide = null;
         }
      
      int k= PeaksFile.lastIndexOf( '.' );
      if( k < 0)
         k = PeaksFile.length( );
      
      String LogFile = PeaksFile.substring( 0,k )+".integrateLog";
      String OutFile = PeaksFile.substring( 0,k )+".integrate";
      
      if( PeaksFile.toUpperCase().endsWith(".INTEGRATE"))
         OutFile = OutFile +"1";
      //-----------------------------------------------------------
         
      Vector Peaks = null;
      try 
      {
         Peaks = Peak_new_IO
               .ReadPeaks_new(PeaksFile);
      } catch (Exception s) 
      {
         s.printStackTrace();
         System.exit(0);
      }
      
      int currentRun = -1;//in prep for more runs
      int currentds = -1;
      NexusRetriever nret = null;
      DataSet DS = null;
      XScale xscl = null;
      int nTimeChan = -1;
      int[][] ids = null;
      float dQ = 1f / MaxCellEdge / 6f;
      FileOutputStream fout = null;
      NexusRetriever ret = new NexusRetriever(NeXusFile );
      
      try
      {
        fout = new FileOutputStream( LogFile);
       }catch (Exception ss)
       {
          ss.printStackTrace( );
          fout= null;
          ret = null;
          Peaks = null;
         PeaksFile =NeXusFile = null;
         // System.exit(1);
       }
       
       
      int startDet = 0;
      int lastGrid = -1;
      IDataGrid grid = null;//grid with dataset entered
      
      
      for (int i = 0; i < Peaks.size(); i++) 
      {
         Peak_new Peak = (Peak_new) (Peaks.elementAt(i));
         
         float[] Qs = Peak.getUnrotQ();         
         float Q = (new Vector3D(Qs)).length();

         int run = Peak.nrun();
         int det = Peak.detnum();
         
         IDataGrid grid1 = Peak.getGrid();
         Vector3D pos = grid1.position(Peak.y(), Peak.x());
         
         float D = pos.length();
         float scatAng = (new DetectorPosition(pos)).getScatteringAngle();
         float w = Math.min(grid1.width(Peak.y(), Peak.x()), grid1.height(Peak
               .y(), Peak.x()));
         
         int nPixels = (int) (.5 + Util.dPixel(dQ, Q, scatAng, D, w));
         nPixels = Math.max( 5 , nPixels );
         
         if( grid1.ID() != lastGrid)
            DS = null;
         
         
         while( (DS == null) || (grid.ID() != lastGrid ) && 
                            (startDet < ret.numDataSets( )))
         {
            DS = ret.getDataSet(  startDet++ );
            IDataGrid grid2 = DataSetTools.dataset.Grid_util.getAreaGrid( DS , det );
            if( grid2 == null)
               DS = null;
            else
            {
               lastGrid = det;
               grid=grid2;
            }
            
         }
         
         float Time = Peak.time( );
         xscl =DS.getData_entry(0).getX_scale( );
         
         int Chan = xscl.getI_GLB( Time );
         float dT_Chan = xscl.getX( Chan+1 )- xscl.getX( Chan );
         
         float xtimes =Math.max( 3 , 2*Util.dTChan( dQ,Q, Time, dT_Chan)+1);
         int nTimes = (int)(xtimes +.5)+1;
         
         float TotIntensity =0;
         float TotVariance =0;
         
         System.out.println("Peak num #time channels="+i+","+nTimes);
         try
         {
            fout.write( String.format("Peak,run,det=%3d %4d %3d\n",i+1,run,det).getBytes());
            fout.write(  String.format("   Pixel width %3dchan %4d,, chanMin %4d,chanMax %4d\n",
                           nPixels+1,Chan+1, Chan+1-(nTimes-1)/2, Chan+1+(nTimes-1)/2).getBytes() );
            fout.write( "   ----------------Slices --------------------\n".getBytes());
            fout.write((" chan    back  Intens(P)   mx       my   sigm(Cll) ncells Intens(Tot) Intens(Tot-back) errI"
                  +"     bk_res     Varx    Vary        Vxy \n").getBytes());
            
         }catch(Exception ss)
         {
            
         }
         nPixels++;
         nTimes+=2;
         boolean done = false;
         //last y values is at xscl.getNum_x( )-2 on a histogram
         for ( int chan = Math.max( 0 , Chan-(nTimes-1)/2); !done && chan <= 
            Math.min( Chan+(nTimes-1)/2,xscl.getNum_x( )-2); chan++)
         {
            
            int x=0;
            
            OneSlice slice = new OneSlice(grid, chan,(int)(.5f+Peak.y()),
                 (int)(.5f+Peak.x()), nPixels,nPixels);
            slice.BadEdgeRange = BadEdgeWidth;
            double[] xs = new double[ slice.ncells( )];
            double[] ys = new double[ xs.length];
            double[]sigs = new double[ xs.length];
            Arrays.fill( ys,0 );
            Arrays.fill( sigs,1 );
            
            //sigs[3] =.2;//Integrated intensity should be able to change faster
                        // than other paramters. It is larger
            for( int ii=0; ii< xs.length; ii++)
               xs[ii]=ii;
            double MaxErrChiSq = .00001*slice.ncells( );
            MarquardtArrayFitter fitter = new MarquardtArrayFitter( slice, xs,ys,sigs,MaxErrChiSq, 200);
            double chiSqr = fitter.getChiSqr( );
         
            double[] errs = fitter.getParameterSigmas( );//Use other for cases when params near
                                                           //boundaries
            
            
            double[] DD = slice.getParameters();
            //AdjustDD( DD, slice.startRow, slice.nrows(), slice.startCol,slice.ncols() );
            char GoodSlicec =' ';
            if(!Double.isNaN( chiSqr )  && 
                   GoodSlice(  DD, errs[3]*Math.sqrt( chiSqr/slice.ncells() ),nPixels,nPixels,grid,10 ))
               GoodSlicec ='x';
            
            if( chan == Chan  && GoodSlicec == 'x')
            {
               System.out.print((i+1)+","+(1/Peak.d())+","+
                    new DetectorPosition( Peak.getGrid( ).position(Peak.y( ),Peak.x())).getScatteringAngle( )
                    *180/Math.PI+","+Math.sqrt( DD[4] )+","+Math.sqrt( DD[5] ));            
               //slice.Test( );
            }
           
            try
            {
              
              
              fout.write( 
                    String.format("%5d %7.3f %8.3f%c %8.3f %8.3f %8.3f %6d %10.2f %10.2f"+
                                                  " %13.3f %8.5f %9.5f %9.5f %9.5f\n",
                    chan+1,
                    DD[0],
                    DD[3],GoodSlicec,
                    DD[1],
                    DD[2],
                    Math.sqrt( chiSqr/slice.ncells()),
                    slice.ncells( ),
                    slice.getInitialTotIntensity( ),
                    (slice.getInitialTotIntensity( )-slice.getParameters( )[0]*
                          slice.ncells( )),errs[3]*Math.sqrt( chiSqr/slice.ncells() ),
                    slice.getAvBackGroundLeft( ),
                    DD[4],
                    DD[5],
                    DD[6])
                         .getBytes() );
            }catch(Exception s2)
            {
               
            }
            if ( GoodSlicec=='x')
            {
               TotIntensity += DD[3];
               double Err = errs[3];
               TotVariance += Err * Err * chiSqr / slice.ncells( );
               
            }else if( chan < Chan)
            {
               TotIntensity =0;
               TotVariance = 0;
            }else
               done = true;
            
            
         }
         
         
         float stDev = (float)Math.sqrt( TotVariance );
         System.out.println("   I,sigI="+ TotIntensity+","+stDev);
         try
         {

            fout.write( "   ----------------End Slices --------------------\n".getBytes());
            fout.write( String.format(
                  "Tot Intensity(-back) %7.2f, stDev= %7.3f\n\n", TotIntensity, stDev).getBytes() );
            fout.write("---------------------------New Peak------------------------\n".getBytes());
            
         }catch(Exception sss)
         {
            
         }
         
         if ( !Float.isNaN( TotIntensity ) && !Float.isNaN( stDev ) )
         {
            Peak.inti( ( float ) TotIntensity );
            Peak.sigi( stDev );
            
         }else
         {
            Peak.inti( 0);
            Peak.sigi(0); 
         }
        
      }
      
      try
      {
         Peak_new_IO.WritePeaks_new( OutFile , Peaks , false );
         fout.close( );
      } catch( Exception ss )
      {

      }
      
      JOptionPane.showMessageDialog( null , "<html><body> The integrate file is in " +
                               OutFile+"<P>  There is a log file in "+LogFile+
                               "</body></html>");
      DS = null;

      fout= null;
      Peaks.clear( );
      Peaks =  null;
      PeaksFile =NeXusFile = null;
      ret.close( );
      ret = null;
      //System.exit(0);
   }
   

   private static void  AdjustDD( double[] DD, int startRow, int nrows, int startCol,int ncols )
   {
      DD[IXMEAN] -=DD[IBACK]*(startCol +ncols/2);
      DD[IYMEAN] -=DD[IBACK]*(startRow+nrows/2);
      DD[IVXX]    -=DD[IBACK]*(ncols*ncols-1)/12;
      DD[IVYY]    -=DD[IBACK]*(nrows*nrows-1)/12;      
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
   private static boolean GoodSlice( double[] parameters, double errs, int drow, int dcol,
         IDataGrid grid, int BadEdgeRange)
   {
      if( parameters ==null)
         return false;
      
      if( Double.isNaN( parameters[ITINTENS]  ) || Double.isNaN(  errs ))
         return false;
      
     if( parameters[ITINTENS]/errs <3)
        return false;
     
     
     if( parameters[ITINTENS]*parameters[ITINTENS]/
           (parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY])
                               < 2.25*Math.PI*Math.PI)
        return false;
     
     if( parameters[IXMEAN] <= BadEdgeRange*1.2 || parameters[IYMEAN] <= BadEdgeRange*1.2)
        return false;
     
     if( parameters[IXMEAN] >=  grid.num_cols()-BadEdgeRange*1.2 ||
           parameters[IYMEAN] >= grid.num_rows()- BadEdgeRange*1.2)
        return false;
     
     return true;
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
     
      //------------Indecies in BaseValues ------------------
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
      //double[] paramValues = new double[7];//b,mx,my,Intensity,Sxx,Syy,Sxy
      IDataGrid grid; 
      int row,col;
      int chan; 
      int drows;
      int dcols;
      int Ncols,Nrows;
      int startRow, startCol;
      double Value;//errors

      double TotBack = 0;
      int  nback =0;
      double[] P; //parameters just for the Normal part of the distribution
                  //the variable, parameters, are the corresponding entries for 
                  //    back + normal.
      boolean goodParameters = false;
      double[] BaseValues;
      
      //parameters mx,my,b,I,Sxx,Syy,Sxy 
      
      double expCoef_z;
      
       double coeffNorm;
       
       double expCoeffx2;
       double expCoeffxy ;
       double expCoeffy2;
       int BadEdgeRange =10;
       double[] expVals;
       double[] dVdmx,dVdmy,dVdsx,dVdsy,dVdsxy;
       Deriv[] derivativeRules;
       double BaseVxx;
       double BaseVyy ;
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
         /*
         double[] Res = new double[7];
         System.arraycopy( params,0,Res,0,7);
         double mx = params[IXMEAN];
         double my = params[IYMEAN];
         double Mx = (mx*(params[ITINTENS])+
                         params[IBACK]*BaseValues[S_x])/
                            (params[ITINTENS]+ params[IBACK]*ncells());
         double My =(my*(params[ITINTENS])+
               params[IBACK]*BaseValues[S_y])/
               (params[ITINTENS]+ params[IBACK]*ncells());
         
         Res[IXMEAN] = Mx;
         Res[IYMEAN]= My;
         
         // ------ Change to variation from different mean -------
         double Sxxo = params[IVXX];
         double Sxxn = Sxxo +2*(mx-Mx)*mx -(mx*mx-Mx*Mx);
         double Syyo = params[IVYY];
         double Syyn = Syyo +2*(my-My)*my -(my*my-My*My);
         double Sxyo = params[IVXY];
         double Sxyn = Sxyo+(my-My)*mx+(mx-Mx)*my-(mx*my-Mx*My);
         
         //--- Now add in background ------
         
         double Sxx = Sxxn*(params[ITINTENS])+
                  params[IBACK]*(BaseValues[S_x2]-2*mx*BaseValues[S_x]+mx*mx*ncells());

         double Syy = Syyn*(params[ITINTENS])+
                  params[IBACK]*(BaseValues[S_y2]-2*my*BaseValues[S_y]+my*my*ncells());
         
         double Sxy = Sxyn*(params[ITINTENS])+
                       params[IBACK]*(BaseValues[S_xy]-my*BaseValues[S_x]-mx*BaseValues[S_y]+
                               mx*my*(ncells()));
         
         Res[IVXX]= Sxx/(params[ITINTENS]+ params[IBACK]*ncells());
         Res[IVYY] = Syy/(params[ITINTENS]+ params[IBACK]*ncells());
         Res[IVXY] = Sxy/(params[ITINTENS]+ params[IBACK]*ncells());
         
         return Res;
         */
      }
      public double[] RemoveBackground( double[] params)
      {
         return params;
         /*
         double[] Res = new double[7];
         System.arraycopy( params,0,Res,0,7);
         double Mx = params[IXMEAN];
         double My = params[IYMEAN];
         double mx = (Mx*(params[IBACK]*ncells()+params[ITINTENS])-
                         params[IBACK]*BaseValues[S_x])/params[ITINTENS];
         double my =(My*(params[IBACK]*ncells()+params[ITINTENS])-
               params[IBACK]*BaseValues[S_y])/params[ITINTENS];
         
         Res[IXMEAN] = mx;
         Res[IYMEAN]= my;
         
         // ------ Change to variation from different mean -------
         double Sxxo = params[IVXX];
         double Sxxn = Sxxo -2*(mx-Mx)*Mx +(mx*mx-Mx*Mx);
         double Syyo = params[IVYY];
         double Syyn = Syyo -2*(my-My)*My +(my*my-My*My);
         double Sxyo = params[IVXY];
         double Sxyn = Sxyo-(my-My)*Mx-(mx-Mx)*My+(mx*my-Mx*My);
         
         //--- Now subtract out background ------
         
         double Sxx = Sxxn*(params[ITINTENS]+params[IBACK]*ncells())-
                  params[IBACK]*(BaseValues[S_x2]-2*mx*BaseValues[S_x]+mx*mx*ncells());

         double Syy = Syyn*(params[ITINTENS]+params[IBACK]*ncells())-
                  params[IBACK]*(BaseValues[S_y2]-2*my*BaseValues[S_y]+my*my*ncells());
         
         double Sxy = Sxyn*(params[ITINTENS]+params[IBACK]*ncells())-
                       params[IBACK]*(BaseValues[S_xy]-my*BaseValues[S_x]-mx*BaseValues[S_y]+
                               mx*my*(ncells()));
         
         Res[IVXX]= Sxx/params[ITINTENS];
         Res[IVYY] = Syy/params[ITINTENS];
         Res[IVXY] = Sxy/params[ITINTENS];
         
         return Res;
         */
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
                       int       row,
                       int       col, 
                       int       drows,
                       int       dcols)
      {
         super("Slice Errors",new double[7], 
                     new String[]{"x mean, y mean, background, Intensity"});
        
         this.grid=grid; 
         this.chan=chan; 
         this.drows=drows;
         this.dcols=dcols;
         this.row = row;
         this.col = col;
         
         BaseValues = new double[13];
         Arrays.fill( BaseValues, 0);
         TotBack = 0;
         nback =0;
         
         // To translate from the linear x to the corresponding
         //    row and column
         Ncols =  Math.min( col+dcols ,grid.num_cols( )-BadEdgeRange)- 
                                       Math.max( BadEdgeRange,col-dcols)+1;
         Nrows = Math.min( row+drows ,grid.num_rows( )-BadEdgeRange)-
                                        Math.max( BadEdgeRange,row-drows) +1;
         startRow= Math.max( BadEdgeRange,row-drows);
         startCol =Math.max( BadEdgeRange,col-dcols);

         expVals = new double[Nrows*Ncols];
         
         //Update totals
         for( int r = Math.max( BadEdgeRange,row-drows); r <= Math.min( row+drows ,grid.num_rows( )-BadEdgeRange);r++)

            for( int c = Math.max( BadEdgeRange,col-dcols); c <= Math.min( col+dcols ,grid.num_cols( )-BadEdgeRange);c++)
            {
               float Intensity = grid.getData_entry( r , c).getY_values( )[chan];
               
               BaseValues[0] += Intensity;
               BaseValues[1] += Intensity*Intensity;
               BaseValues[2] += c*Intensity;
               BaseValues[3] += r*Intensity;
               BaseValues[4] += c*c*Intensity;
               BaseValues[5] += r*r*Intensity;
               BaseValues[6] += r*c*Intensity;
               BaseValues[7] += c;
               BaseValues[8] += r;
               BaseValues[9] += c*c;
               BaseValues[10] += r*r;
               BaseValues[11] += r*c;
               BaseValues[12] +=1;
               if( r==row-drows || r==row+drows || c==col-dcols || c==col+dcols)
               {
                  TotBack+=Intensity;
                  nback++;
               }
               
            }
         
         
         double[] params = new double[7];
         params[IBACK] = TotBack/nback;
         params[IXMEAN] = col;
         params[IYMEAN] = row;
         params[ITINTENS] = BaseValues[S_int] -params[IBACK]*BaseValues[S_1];
         params[0]=0;
         setSigmas( params);
         BaseVxx= params[IVXX];
         BaseVyy = params[IVYY];
         params[0] = TotBack/nback;
         setSigmas(params);
         if( params[ITINTENS] < params[IBACK])
         {
            params[IBACK] =0;
            params[ITINTENS] = BaseValues[S_int];
            setSigmas(params);
         }
         
         
         if( params[4] <=0 || params[5] <=0 )
         {
            params[IBACK]=0;
            setSigmas( params );
         }
         P = new double[7];
         System.arraycopy( params,0,P,0,7);
         if(! areParametersGood())
         {
            params[IBACK]=0;
            setSigmas( params );
         }
         derivativeRules = new Deriv[7];
         derivativeRules[IBACK] = new Derivb();
         derivativeRules[IXMEAN] = new DerivI(P,startRow,startCol,Nrows,Ncols);
         derivativeRules[IYMEAN] = new Derivmx(P,startRow,startCol,Nrows,Ncols);
         derivativeRules[ITINTENS] = new Derivmy(P,startRow,startCol,Nrows,Ncols);
         derivativeRules[4] = new DerivSxx(P,startRow,startCol,Nrows,Ncols);
         derivativeRules[5] = new DerivSyy(P,startRow,startCol,Nrows,Ncols);
         derivativeRules[6] = new DerivSxy(P,startRow,startCol,Nrows,Ncols);
         
         
         
         setParameters( AddBackground(params) );

         
         
      }
    
      /**
       * Parameters are means, stdev, etc. for sum of background +  normal
       */
      @Override
      public void setParameters(double[] params)
      {
         super.setParameters((params));
         
         /*double background = parameters[IBACK];
         P[IXMEAN] = parameters[IXMEAN] -background*(startCol +Ncols/2);
         P[IYMEAN] = parameters[IYMEAN] -background*(startRow +Nrows/2);
         P[IVXX] = parameters[IVXX] -background*(Ncols*Ncols -1)/12;
         P[IVYY] = parameters[IVYY] -background*(Nrows*Nrows -1)/12;
         P[IBACK]= background;
         P[IVXY ]= parameters[IVXY];
         P[ITINTENS]=parameters[ITINTENS];
         */
         System.arraycopy( RemoveBackground(params),0,P,0,7);
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
       
         
      }
      
      public boolean areParametersGood()
      {
         if( P[IBACK] < 0 || P[ITINTENS] < 0)
            return false;
         
         if( P[IYMEAN]<=startRow+Math.min( 8 , Nrows/16) || 
               P[IXMEAN] < Math.min( 8 ,startCol+Ncols/16))
            return false;
         
         if( P[IYMEAN] >=startRow+ Nrows -Math.min(8,Nrows*.25) ||
               P[IXMEAN] > startCol +Ncols-Math.min(8,Ncols*.25))
            return false;
       
         
         if( P[IXMEAN] < BadEdgeRange+2 || P[IXMEAN] > grid.num_cols( )-BadEdgeRange-2)
            return false;
         
         if( P[IYMEAN] < BadEdgeRange+2|| P[IYMEAN] > grid.num_rows( )-BadEdgeRange-2)
            return false;
         
         for( int i=4; i<=5;i++)
            if( P[i] <=0)
               return false;
         
         if( P[IVXX]*P[IVYY]-P[IVXY]*P[IVXY] <=0)
            return false;
         
         if( P[IVXX] >=1.2*(BaseVxx*(BaseValues[S_int])
                      -P[IBACK]*(BaseValues[S_x2]-2*P[IXMEAN]*BaseValues[S_x]+
                      P[IXMEAN]*P[IXMEAN]*ncells()))/
                      (BaseValues[S_int]-P[IBACK]*ncells()))
            return false;
         
         
         if( P[IVYY] >=1.2*(BaseVyy*(BaseValues[S_int])
                      -P[IBACK]*(BaseValues[S_y2]-2*P[IYMEAN]*BaseValues[S_y]+
                      P[IYMEAN]*P[IYMEAN]*ncells()))/
                      (BaseValues[S_int]-P[IBACK]*ncells()))
            return false;
         return true;
         
      }
      
      //Not used
      private double[] RangeFix( double[] parameters)
      {
         double[] Res = new double[7];
         System.arraycopy(  parameters, 0, Res,0,7);
         
         if( Res[0] < 0)
            Res[0]=0;
         
         if(Res[3] < 0)
            Res[3] =0;
         
         if( Res[1] <1)
            Res[1] =1;
         
         if( Res[2] <1)
            Res[2] =1;
         
         if( Res[1] > grid.num_cols( ))
            Res[1] =grid.num_cols( );
         
         if( Res[2] >grid.num_rows( ))
            Res[2] =grid.num_rows( );
         
         if( Res[0]*BaseValues[S_1] > BaseValues[S_int])
            {
              Res[0]=0;
            }
        
         
         return Res;
      }

      public void Test()
      {
         double x = (row-startRow)*Ncols +(col-startCol);
         double[] pp = getParameters();
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
        
        float Intensity = grid.getData_entry( r , c).getY_values( )[chan];
        /* 
        double xx = (c-P[IXMEAN]);
        double yy = r-P[IYMEAN];
        
        double exp = xx*xx*expCoeffx2+ xx*yy*expCoeffxy+ yy*yy*expCoeffy2;
       */
       
        double err = P[IBACK]+ P[ITINTENS]*coeffNorm*expVals[(int)x] -
                    Intensity;
        
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
      private void setSigmas(double[] P)
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
         while( Sxx*Syy <= Sxy*Sxy  && Sxx >0  && Syy > 0)
            Sxy/=2;
         
         P[IVXY]= Sxy;
      }
      
      private void setSigmas1(double[] parameters)
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

      private void updateAllDerivs( double[] x )
      {
         if( dVdmx == null)
            dVdmx =derivativeRules[1].get_dFdp( x );
         if( dVdmy == null)
            dVdmy =derivativeRules[1].get_dFdp( x );
         if( dVdsx == null)
            dVdsx =derivativeRules[4].get_dFdp( x );
         if( dVdsy == null)
            dVdsy =derivativeRules[5].get_dFdp( x );
         
      }
      
      public double[] get_dFdai( double x[], int i )
      {
         return derivativeRules[i].get_dFdp( x );
      }
      
      public double get_dFdai( double x, int i )
      {
         return derivativeRules[i].get_dFdp( x );
      }
      public float[] get_dFdai( float x[], int i )
      {
         return derivativeRules[i].get_dFdp( x );
      }
      public float get_dFdai( float x, int i )
      {
         return derivativeRules[i].get_dFdp( x );
      }
      public double[] xget_dFdai( double x[], int i )
      {
         
            updateAllDerivs(x);
            if( i ==1 )
               return dVdmx;
            else if( i==2 )
               return dVdmy;
            else if( i==4 )
               return dVdsx;
            else if( i==5)
               return dVdsy;
            else if( i !=0)
               return derivativeRules[i].get_dFdp( x );
         
           double[] Res = new double[x.length];
           for( int k=0; k< Res.length; k++)
              Res[k]= 1+dVdmx[k]*(startCol+Ncols/2)
                       +dVdmy[k]*(startRow+Nrows/2)+
                       dVdsx[k]*(Ncols*Ncols-1)/12+
                       dVdsy[k]*(Nrows*Nrows-1)/12;
           return Res;
         
             
        /* double[] Res = new double[x.length];
         if( i==0)
         {
            Arrays.fill( Res , 1.0 );
            return Res;
         }
         if( i==3)
         {
            for( int j=0; j< x.length; j++)
               Res[j]= get_dFdai(x[j],i);
            return Res;
         }
         return super.get_dFdai( x , i );
         */
      }
      


      public float[] xget_dFdai( float x[], int i )
      {
         /*float[] Res = new float[x.length];
         if( i==0)
         {
            Arrays.fill( Res , 1.0f );
            return Res;
         }
         if( i==3)
         {
            for( int j=0; j< x.length; j++)
               Res[j]= get_dFdai(x[j],i);
            
            return Res;
         }
         return super.get_dFdai( x , i );
         
         
         return derivativeRules[i].get_dFdp( x );
         */
         float[] Res = new float[x.length];
         for( int k=0; k< Res.length;k++)
            Res[i] = get_dFdai(x[k],i);
         
         return Res;
      }
      
      
     
      public double xget_dFdai(double x, int i)
      {
         if(i==1 && dVdmx != null)
            return dVdmx[(int)x];
         else if( i==2 && dVdmy != null)
            return dVdmy[(int)x];
         else if( i==4 && dVdsx != null)
            return dVdsx[(int)x];
         else if( i==5 && dVdsy != null)
            return dVdsy[(int)x];
         else if( i!=0)
            return derivativeRules[i].get_dFdp( x );
        
         return 1+ get_dFdai(x,1)*(startCol+Ncols/2) + 
                           get_dFdai(x,2)*(startRow+Nrows/2)+
                           get_dFdai(x,4)*(Ncols*Ncols-1)/12+
                           get_dFdai(x,5)*(Nrows*Nrows-1)/12;

         /*if( i==0)
            return 1.0;
         if( i !=3)
         return super.get_dFdai( x , i );
         
         int k = (int) x;
         
         int r = startRow+ (int)(k/Ncols);
         int c = startCol+k%Ncols;
         float Intensity = grid.getData_entry( r , c).getY_values( )[chan];
         double xx = (c-P[IXMEAN]);
         double yy = r-P[IYMEAN];
         double exp = xx*xx*expCoeffx2+ xx*yy*expCoeffxy+ yy*yy*expCoeffy2;
      
        
         return coeffNorm*Math.pow(Math.E, exp);
         */
      }

      
      
      
      public float xget_dFdai(float x, int i)
      {
         return (float)get_dFdai((double)x,i);
        /* if( i==0)
            return 1f;
         if( i > 1)
         return super.get_dFdai( x , i );
         
         return (float) get_dFdai( (double)x, i);
         */
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
         return get_dFdp(x,r,c, coefNorm*parameters[ITINTENS], parameters[IVXY]/uu,parameters[IVYY]/uu);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx=-parameters[IVXY]/uu; 
         double coefy= parameters[IVYY]/uu;
         
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
         double C =parameters[IVYY]/2/uu;
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
         double coefxy =-parameters[IVXY]*parameters[IVYY]/uu/uu;
         double C =parameters[IVYY]/2/uu;
         
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
         double C =parameters[IVYY]/2/uu;
         
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
         double C =parameters[IVXX]/2/uu;
         return get_dFdp(x,r,c,coefExp, C,coefx2, coefxy,
               coefy2);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx2=parameters[IVXY]*parameters[IVXY]/2/uu/uu; 
         double coefy2=parameters[IVXX]*parameters[IVXX]/2/uu/uu;
         double coefxy =-parameters[IVXY]*parameters[IVXX]/uu/uu;
         double C =parameters[IVXX]/2/uu;
         
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
         double C =parameters[IVXX]/2/uu;
         
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
         double coefx2=-parameters[IVYY]*parameters[IVXY]/uu/uu; 
         double coefy2=-parameters[IVXX]*parameters[IVXY]/uu/uu;
         double coefxy =2*(uu+parameters[IVXY]*parameters[IVXY])/uu/uu;
         double C =-parameters[IVXY]/uu;
         return get_dFdp(x,r,c,coefExp, C,coefx2, coefxy,
               coefy2);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[IVXX]*parameters[IVYY]-parameters[IVXY]*parameters[IVXY];
         double coefExp = coefNorm*parameters[ITINTENS];
         double coefx2=-parameters[IVYY]*parameters[IVXY]/uu/uu; 
         double coefy2=-parameters[IVXX]*parameters[IVXY]/uu/uu;
         double coefxy =2*(uu+parameters[IVXY]*parameters[IVXY])/uu/uu;
         double C =-parameters[IVXY]/uu;
         
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
         double coefx2=-parameters[IVYY]*parameters[IVXY]/uu/uu; 
         double coefy2=-parameters[IVXX]*parameters[IVXY]/uu/uu;
         double coefxy =2*(uu+parameters[IVXY]*parameters[IVXY])/uu/uu;
         double C =-parameters[IVXY]/uu;
         
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
