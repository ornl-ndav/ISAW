import gov.anl.ipns.MathTools.Functions.OneVarParameterizedFunction;
import gov.anl.ipns.MathTools.Functions.MarquardtArrayFitter;
import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Parameters.FileChooserPanel;
import gov.anl.ipns.Util.Numeric.ClosedInterval;

import java.awt.GridLayout;
import java.io.FileOutputStream;
import java.util.*;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import DataSetTools.dataset.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.retriever.NexusRetriever;
import Operators.TOF_SCD.IntegrateUtils;
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
            JPanel panel = new JPanel();
            panel.setLayout(  new GridLayout(2,1) );
            panel.add( Peak);
            panel.add( NexFile);
            if( JOptionPane.showConfirmDialog( null, panel,"Input Files",
                  JOptionPane.OK_CANCEL_OPTION)== JOptionPane.OK_OPTION)
            {
               PeaksFile = Peak.getTextField( ).getText( ).trim();
               NeXusFile = NexFile.getTextField( ).getText( ).trim();
            }
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
      float dQ = 1f / 12f / 6f;
      FileOutputStream fout = null;
      NexusRetriever ret = new NexusRetriever(NeXusFile );
      
      try
      {
        fout = new FileOutputStream( LogFile);
       }catch (Exception ss)
       {
          System.exit(1);
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
            fout.write(" chan    back  Intens(P)   mx       my   sigm(Cll) ncells Intens(Tot) Intens(Tot-back) errI\n".getBytes());
            
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

            double[] xs = new double[ slice.ncells( )];
            double[] ys = new double[ xs.length];
            double[]sigs = new double[ xs.length];
            Arrays.fill( ys,0 );
            Arrays.fill( sigs,1 );
            Arrays.fill( sigs ,4,8,15. );
            for( int ii=0; ii< xs.length; ii++)
               xs[ii]=ii;
            
            MarquardtArrayFitter fitter = new MarquardtArrayFitter( slice, xs,ys,sigs,.0001, 200);
            double chiSqr = fitter.getChiSqr( );
         
            double[] errs = fitter.getParameterSigmas_2( );//Use other for cases when params near
                                                           //boundaries
            double[] DD = slice.getParameters();
            try
            {
              
              
              fout.write( 
                    String.format("%5d %7.3f %8.3f %8.3f %8.3f %8.3f %6d %10.2f %10.2f"+
                                                  " %19.3f %8.5f %8.5f %8.5f %8.5f\n",
                    chan+1,
                    DD[0],
                    DD[3],
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
            if ( !Double.isNaN( chiSqr )  && GoodSlice(  DD, errs,nPixels,nPixels ))
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
      System.exit(0);
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
   private static boolean GoodSlice( double[] parameters, double[] errs, int drow, int dcol)
   {
      if( parameters ==null)
         return false;
      
      if( Double.isNaN( parameters[3]  ) || Double.isNaN(  errs[3] ))
         return false;
      
     if( parameters[3]/errs[3] <3)
        return false;
     
     
     if( parameters[3]*parameters[3]/
           (parameters[4]*parameters[5]-parameters[6]*parameters[6])
                               < 2.25*Math.PI*Math.PI)
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
      int Ncols;
      int startRow, startCol;
      double Value;//errors

      double TotBack = 0;
      int  nback =0;
      boolean goodParameters = false;
      double[] BaseValues;
      
      //parameters mx,my,b,I,Sxx,Syy,Sxy 
      
      double expCoef_z;
      
       double coeffNorm;
       
       double expCoeffx2;
       double expCoeffxy ;
       double expCoeffy2;
       int BadEdgeRange =10;
     
       /**
        * The number of cells used in the fitting
        * @return
        */
      public int ncells()
      {
         return (int)BaseValues[S_1];
      }
      
      /**
       * 
       * @return  The average background per cell for the boundary
       *          cells( with background parameter taken off)
       */
      public double getAvBackGroundLeft()
      {
         return TotBack/nback -parameters[0];
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
                                       Math.max( 1,col-dcols-BadEdgeRange)+1;
         startRow= Math.max( BadEdgeRange,row-drows);
         startCol =Math.max( BadEdgeRange,col-dcols);
         
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
         params[0] = TotBack/nback;
         params[1] = col;
         params[2] = row;
         params[3] = BaseValues[0] -params[0]*BaseValues[12];
         if( params[3] < params[0])
         {
            params[0] =0;
            params[3] = BaseValues[0];
         }
         setSigmas( params);
         
         if( params[4] <=0 || params[5] <=0)
         {
            params[0]=0;
            setSigmas( params );
         }
         
         setParameters( params );
         
      }
    

      @Override
      public void setParameters(double[] params)
      {
         super.setParameters((params));
         
         goodParameters = areParametersGood();
         
         // Will return NaN if parameters are out of whack
         //This causes problems with derivatives and with parameters close
         //   to the boundary
         if(!goodParameters)
            return;
         
         double uu = parameters[4]*parameters[5]- parameters[6]*parameters[6];
         expCoef_z= -.5*parameters[4]*parameters[5]/uu;
         
         coeffNorm = .5/Math.PI/Math.sqrt( uu);
         
         expCoeffx2= -parameters[5]/2/uu;
         expCoeffxy = parameters[6]/uu;
         expCoeffy2= -parameters[4]/2/uu;
         //TODO Get array of exp values at @ point, so derivs and values are faster etc.
         
      }
      
      public boolean areParametersGood()
      {
         if( parameters[0] < 0 || parameters[3] < 0)
            return false;
         
         if( Math.abs( parameters[1]-col )> Math.max( dcols/8,1.5))
            return false;
         
         if( Math.abs( parameters[2]-row )> Math.max( drows/8,1.5))
            return false;
         
         if( parameters[1] < BadEdgeRange || parameters[1] > grid.num_cols( )-BadEdgeRange)
            return false;
         
         if( parameters[2] < BadEdgeRange || parameters[2] > grid.num_rows( )-BadEdgeRange)
            return false;
         
         for( int i=4; i<=5;i++)
            if( parameters[i] <=0)
               return false;
         
         if( parameters[4]*parameters[5]-parameters[6]*parameters[6] <=0)
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
        
        double xx = (c-parameters[1]);
        double yy = r-parameters[2];
        
        double exp = xx*xx*expCoeffx2+ xx*yy*expCoeffxy+ yy*yy*expCoeffy2;
     
       
        double err = parameters[0]+ parameters[3]*coeffNorm*Math.pow(Math.E, exp) -
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
      private void setSigmas(double[] parameters)
      {

         double Sxx = BaseValues[S_x2int] - parameters[0] * BaseValues[S_x2]
               - 2 * parameters[1]
               * ( BaseValues[S_xint] - parameters[0] * BaseValues[S_x] )
               + parameters[1] * parameters[1]
               * ( BaseValues[S_int] - parameters[0] * BaseValues[S_1] );

         Sxx = Sxx / ( BaseValues[S_int] - parameters[0] * BaseValues[S_1] );

         double Syy = BaseValues[S_y2int] - parameters[0] * BaseValues[S_y2]
               - 2 * parameters[2]
               * ( BaseValues[S_yint] - parameters[0] * BaseValues[S_y] )
               + parameters[2] * parameters[2]
               * ( BaseValues[S_int] - parameters[0] * BaseValues[S_1] );
         Syy = Syy / ( BaseValues[S_int] - parameters[0] * BaseValues[S_1] );

         double Sxy = BaseValues[S_xyint] - parameters[0] * BaseValues[S_xy]
               - parameters[1]
               * ( BaseValues[S_yint] - parameters[0] * BaseValues[S_y] )
               - parameters[2]
               * ( BaseValues[S_xint] - parameters[0] * BaseValues[S_x] )
               + parameters[2] * parameters[1]
               * ( BaseValues[S_int] - parameters[0] * BaseValues[S_1] );
         Sxy = Sxy / ( BaseValues[S_int] - parameters[0] * BaseValues[S_1] );
         parameters[4]= Sxx;
         parameters[5] = Syy;
         while( Sxx*Syy < Sxy*Sxy  && Sxx >0  && Syy > 0)
            Sxy/=2;
         
         parameters[6]= Sxy;
      }

      public double[] get_dFdai( double x[], int i )
      {
         double[] Res = new double[x.length];
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
      }
      


      public float[] get_dFdai( float x[], int i )
      {
         float[] Res = new float[x.length];
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
      }
      
      
      @Override
      public double get_dFdai(double x, int i)
      {

         if( i==0)
            return 1.0;
         if( i !=3)
         return super.get_dFdai( x , i );
         
         int k = (int) x;
         
         int r = startRow+ (int)(k/Ncols);
         int c = startCol+k%Ncols;
         float Intensity = grid.getData_entry( r , c).getY_values( )[chan];
         double xx = (c-parameters[1]);
         double yy = r-parameters[2];
         double exp = xx*xx*expCoeffx2+ xx*yy*expCoeffxy+ yy*yy*expCoeffy2;
      
        
         return coeffNorm*Math.pow(Math.E, exp);
      }

      
      
      @Override
      public float get_dFdai(float x, int i)
      {
         if( i==0)
            return 1f;
         if( i > 1)
         return super.get_dFdai( x , i );
         
         return (float) get_dFdai( (double)x, i);
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
   
   class Derivb implements Deriv
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
   
   class DerivI implements Deriv
   {
      double[] parameters; 
      int startRow; 
      int startCol; 
      int Nrows;
      int Ncols;
      double[] x;
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

         return this.x[(int)x]*coefNorm;
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
      public void setCommonData(double[] x, double coefNorm, double expCoeffx2,
            double expCoeffy2, double expCoeffxy)
      {


         this.x = x;
         this.coefNorm =coefNorm;
         this.expCoeffx2 =expCoeffx2;
         this.expCoeffy2 =expCoeffy2;
         this.expCoeffxy =expCoeffxy;
         
      }
      
   }
   
   class Derivmx extends DerivI
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
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         return get_dFdp(x,r,c, coefNorm*parameters[3], parameters[5]/uu,-parameters[6]/uu);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx=parameters[5]/uu; 
         double coefy=-parameters[6]/uu;
         
         double[] Res = new double[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
               Res[k++]= get_dFdp(x[k],r,c,coefExp, coefx,coefy);
               

         // TODO Auto-generated method stub
         return Res;
      }
      @Override
      public float[] get_dFdp(float[] x)
      {
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx=parameters[5]/uu; 
         double coefy=-parameters[6]/uu;
         
         float[] Res = new float[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
               Res[k++]= (float)get_dFdp(x[k],r,c,coefExp, coefx,coefy);
         
         return Res;
      }
      
      
      private double get_dFdp( double x, int r, int c,
            double coefExp,double coefx, double coefy)
      {
         return coefExp*this.x[(int)x]*(coefx*(c-parameters[1])+coefy*(r-parameters[2]));
      }
      
   }

   
   
   class Derivmy extends DerivI
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
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         return get_dFdp(x,r,c, coefNorm*parameters[3], parameters[6]/uu,parameters[5]/uu);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx=-parameters[6]/uu; 
         double coefy= parameters[5]/uu;
         
         double[] Res = new double[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
               Res[k++]= get_dFdp(x[k],r,c,coefExp, coefx,coefy);
               

         // TODO Auto-generated method stub
         return Res;
      }
      @Override
      public float[] get_dFdp(float[] x)
      {
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx= -parameters[6]/uu; 
         double coefy= parameters[5]/uu;
         
         float[] Res = new float[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
               Res[k++]= (float)get_dFdp(x[k],r,c,coefExp, coefx,coefy);
         
         return Res;
      }
      
      
      private double get_dFdp( double x, int r, int c,
            double coefExp,double coefx, double coefy)
      {
         return coefExp*this.x[(int)x]*(coefx*(c-parameters[1])+coefy*(r-parameters[2]));
      }
      
   }
   
   class DerivSxx extends DerivI
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
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx2=parameters[5]*parameters[5]/2/uu/uu; 
         double coefy2=parameters[6]*parameters[6]/2/uu/uu;
         double coefxy =-parameters[6]*parameters[5]/uu/uu;
         double C =parameters[5]/2/uu;
         return get_dFdp(x,r,c,coefExp, C,coefx2, coefxy,
               coefy2);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx2=parameters[5]*parameters[5]/2/uu/uu; 
         double coefy2=parameters[6]*parameters[6]/2/uu/uu;
         double coefxy =-parameters[6]*parameters[5]/uu/uu;
         double C =parameters[5]/2/uu;
         
         double[] Res = new double[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
               Res[k++]= get_dFdp(x[k],r,c,coefExp, C,coefx2, coefxy,
                     coefy2);
               

         // TODO Auto-generated method stub
         return Res;
      }
      @Override
      public float[] get_dFdp(float[] x)
      {
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx2=parameters[5]*parameters[5]/2/uu/uu; 
         double coefy2=parameters[6]*parameters[6]/2/uu/uu;
         double coefxy =-parameters[6]*parameters[5]/uu/uu;
         double C =parameters[5]/2/uu;
         
         float[] Res = new float[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)      
               Res[k++]= (float)get_dFdp(x[k],r,c,coefExp, C,coefx2, coefxy,
                  coefy2);
         
         return Res;
      }
      
      
      private double get_dFdp( double x, int r, int c,
            double coefExp,double C,double coefx2, double coefxy,
            double coefy2)
      {
         return coefExp*this.x[(int)x]*(C+coefx2*(c-parameters[1])*(c-parameters[1])
               +coefxy*(r-parameters[2])*(c-parameters[1])+
               coefy2*(r-parameters[2])*(r-parameters[2]));
      }
      
   }
   
   class DerivSyy extends DerivI
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
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx2=parameters[6]*parameters[6]/2/uu/uu; 
         double coefy2=parameters[4]*parameters[4]/2/uu/uu;
         double coefxy =-parameters[6]*parameters[4]/uu/uu;
         double C =parameters[4]/2/uu;
         return get_dFdp(x,r,c,coefExp, C,coefx2, coefxy,
               coefy2);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx2=parameters[6]*parameters[6]/2/uu/uu; 
         double coefy2=parameters[4]*parameters[4]/2/uu/uu;
         double coefxy =-parameters[6]*parameters[4]/uu/uu;
         double C =parameters[4]/2/uu;
         
         double[] Res = new double[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
               Res[k++]= get_dFdp(x[k],r,c,coefExp, C,coefx2, coefxy,
                     coefy2);
               

         // TODO Auto-generated method stub
         return Res;
      }
      @Override
      public float[] get_dFdp(float[] x)
      {
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx2=parameters[6]*parameters[6]/2/uu/uu; 
         double coefy2=parameters[4]*parameters[4]/2/uu/uu;
         double coefxy =-parameters[6]*parameters[4]/uu/uu;
         double C =parameters[4]/2/uu;
         
         float[] Res = new float[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)      
               Res[k++]= (float)get_dFdp(x[k],r,c,coefExp, C,coefx2, coefxy,
                  coefy2);
         
         return Res;
      }
      
      
      private double get_dFdp( double x, int r, int c,
            double coefExp,double C,double coefx2, double coefxy,
            double coefy2)
      {
         return coefExp*this.x[(int)x]*(C+coefx2*(c-parameters[1])*(c-parameters[1])
               +coefxy*(r-parameters[2])*(c-parameters[1])+
               coefy2*(r-parameters[2])*(r-parameters[2]));
      }
      
   }
   
   class DerivSxy extends DerivI
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
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx2=-parameters[5]*parameters[6]/uu/uu; 
         double coefy2=-parameters[4]*parameters[6]/uu/uu;
         double coefxy =2*(uu+parameters[6]*parameters[6])/uu/uu;
         double C =-parameters[6]/uu;
         return get_dFdp(x,r,c,coefExp, C,coefx2, coefxy,
               coefy2);
      }
      @Override
      public double[] get_dFdp(double[] x)
      {
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx2=-parameters[5]*parameters[6]/uu/uu; 
         double coefy2=-parameters[4]*parameters[6]/uu/uu;
         double coefxy =2*(uu+parameters[6]*parameters[6])/uu/uu;
         double C =-parameters[6]/uu;
         
         double[] Res = new double[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)
               Res[k++]= get_dFdp(x[k],r,c,coefExp, C,coefx2, coefxy,
                     coefy2);
               

         // TODO Auto-generated method stub
         return Res;
      }
      @Override
      public float[] get_dFdp(float[] x)
      {
         double uu = parameters[4]*parameters[5]-parameters[6]*parameters[6];
         double coefExp = coefNorm*parameters[3];
         double coefx2=-parameters[5]*parameters[6]/uu/uu; 
         double coefy2=-parameters[4]*parameters[6]/uu/uu;
         double coefxy =2*(uu+parameters[6]*parameters[6])/uu/uu;
         double C =-parameters[6]/uu;
         
         float[] Res = new float[x.length];
         int k=0;
         for( int r=startRow; r < startRow+Nrows; r++)
            for( int c=startCol; c< startCol +Ncols; c++)      
               Res[k++]= (float)get_dFdp(x[k],r,c,coefExp, C,coefx2, coefxy,
                  coefy2);
         
         return Res;
      }
      
      
      private double get_dFdp( double x, int r, int c,
            double coefExp,double C,double coefx2, double coefxy,
            double coefy2)
      {
         return coefExp*this.x[(int)x]*(C+coefx2*(c-parameters[1])*(c-parameters[1])
               +coefxy*(r-parameters[2])*(c-parameters[1])+
               coefy2*(r-parameters[2])*(r-parameters[2]));
      }
      
   }
}
