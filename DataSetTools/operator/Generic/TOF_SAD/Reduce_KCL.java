package DataSetTools.operator.Generic.TOF_SAD;

import java.io.*;
import java.lang.String;
import java.lang.Math;
import java.math.*;
import java.lang.Object;
import java.util.Vector;
import java.util.*;

import Command.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.components.ui.*;
import DataSetTools.dataset.*;
import DataSetTools.components.containers.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.instruments.*;
import DataSetTools.operator.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import DataSetTools.viewer.Table.*;
import DataSetTools.math.*;
import DataSetTools.operator.DataSet.Math.DataSet.*;
import DataSetTools.operator.DataSet.Math.Analyze.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.DataSet.EditList.*;
import DataSetTools.operator.DataSet.Math.DataSet.*;
import DataSetTools.operator.Parameter;
import DataSetTools.parameter.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import DataSetTools.operator.DataSet.Math.Scalar.*;


/**
 * TITLE:        Convert Fortran To JAVA
 * Description:  SMALL ANGLE NEUTRON SCATTERING ANALYSIS ROUTINE
	PRODUCE either A RADIALLY AVERAGED S(Q) VS Q ARRAY; SN(RUN#).dat
	OR PRODUCING AN S(QX,QY) ARRAY.	SN@D(RUN#).BIN

	PROGRAM REQUIRES
	File AREA DETECTOR SENSITIVITY and CELLS MASK FROM AREADETSEN
	File EFFICIENCY RATIO of AD/M1 FROM EFRATIO
	Files TRANSMISSION COEFFICIENTS FILE
	Scale factor and thickness in cm.

	Putting additional mask due to the nonlinearity near the left
	side of the area detector. Throw first 20 chans in the x direction.
	Search for "nedge" to locate the masking info - 11/15/96- PT,JK

	ANY RANGE OF TIME CHANNELS MAY BE EXAMINED, AND ANY CHANNEL OR
	GROUP OF CHANNELS MAY BE OMITTED.

 * @version 1.0
 */

public class Reduce_KCL {
DataSet TransS, TransB,  Eff,  Sens ;
               float[] qu;
DataSet[] RUNBds = new DataSet[2];
DataSet[]RUNCds= new DataSet[2];;
DataSet[]RUNSds= new DataSet[2];
public String INSTR;
public String INAME;
String str1;
/**
 *Parameters
 */

XScale xscl;
	int MAXDET=100;
	int MAXCST=512;
	int MAXMON=1100;
	int MAXX=128;
	int MAXY=128;
	int MAXQBIN=512;
  int MAXSLC=MAXX*MAXY;
	int MAXCRV=3;
	int MAXPTS=512;
	int maxqxbin=250;
	int maxqybin=250;
/**
 *   Variables
 */
  String ANS,ANSDN,ANSCH,ANSNL,ANSSD,ANSCOL,ANISOT,ANSAGN,ANSPLT,ANSCL,ANSD;
  String ansbt;
	//char[]  NAMCOL,NAMLIS,NAMLIS2,NAMLIS3,namsq2,namsq1,namsq=new char[20];
	String  REDUCEOUT;
	char[]  tttt1=new char[80];
	char[]  users1=new char[20];
  byte[] tttt=new byte[80];
  byte[] users=new byte[20];
	//char[] edt[1]= new char[9];
  byte[] sdt = new byte[9];
   byte[] edt =new byte[9];
  char[] stm1,etm1=new char[9];
	byte[] stm,etm = new byte[8];
	String LABELX,LABELY;
	String PTITLE;
	String MESSAGE;

  FileOutputStream F1;
  FileOutputStream F3;
  FileOutputStream F5;
  FileOutputStream F6;
  FileOutputStream F7;
  FileOutputStream F8;
  FileOutputStream F9;
  FileOutputStream F10;

  byte buffer[]=new byte[80];
  String error=null;

	int SLICE,SLICE1,SLICE2,IFDELAY,IF2D,IFNONLIN;
	int LTEMP,IERR,HISTNUM,qbins;
	int NUMX,NUMY,NUMW,smonsum,smonpot;
	int pulses,nleft,nright;
	int OUTUNIT,NDIV,ncenter;
  int MICRO,NHS,NCH1D,NCHTOT;
	int Nx,Ny;
	int NDIVx,NDIVy;
   DataSet RelSamp;
  
  boolean IBIN;
  float CHANWID;

	double MINQ,MAXQ,QMIN,QMAX;
  double[] QMINQ = new double[5];
	double minqx,maxqx,MINQy,MAXQy,Qxmin,Qxmax;
  double[] DIV,QMAXQ = new double[5];
	double  LLOW,LHIGH,LAMAVG,LAMMAX;
	double IOFQMAX,IOFQMIN;
  double[] DELQ=new double[5];
  double THMAX ;
  double SINTHMAX;
  double SINxMAX;
  double SINxMin;
  double SINyMAX;
  double SINyMin;
  float THICK;

	float L1,L2,CLK;
	double XDIM,YDIM,Qres,DIVx,DIVy;
  double Qymin,Qymax,xDELTAQ,yDELTAQ;
	int INET = 0;
	int NEDGE = 1;
   /**
     * Arrays
     */
        int[] ITEMP = new int[3];
	      int[] NSHIFT=new int[5];
        int[] IFOMIT= new int[MAXCST+MAXCST];
        int[] IFGOOD=new int[MAXSLC+MAXSLC];
        //int[] IFERR=new int[MAXCRV+MAXCRV];

        double[] DETSEN,DETERR=new double[MAXSLC+MAXSLC];
        double[] AREATOM1,AREATOM1ERR=new double[MAXCST+1];
        double[] QVECTOR,IOFQ,ERROR=new double[MAXQBIN+1];

        double[] siofq,serror=new double[MAXQBIN+1];
        double[] biofq,berror=new double[MAXQBIN+1];

        float[] LAMBDA=new float[MAXCST+1];

        double[] TRANS,TRANB,TSERR,TBERR=new double[MAXCST+1];
        double[] XX = new double[MAXX+1];
        double[] YY=new double[MAXY+1];
        double[] QUE1,QUE2,QUE3,QUE4=new double[MAXQBIN+1];
        double[] QUEx1,QUEx2=new double[maxqxbin+1];
        double[] QUEx3,QUEx4=new double[maxqxbin+1];
        double[] QUEY1,QUEY2=new double[maxqybin+1];
        double[] QUEY3,QUEY4=new double[maxqybin+1];
        float[] SAMPM1,BACKM1,CADM1=new float[MAXCST+1];
        double[] SAMPDN,BACKDN,CADDN,SAMPLE=new double[MAXSLC+1];
        double[] SINRAD=new double[MAXSLC+1];
        double[][] sinx=new double[MAXX+1][MAXY+1];
        double[][] siny=new double[MAXX+1][MAXY+1];

        double[] SAMPINT=new double[MAXSLC+MAXSLC];
        double[] WEIGHT=new double[MAXSLC+MAXSLC];;
        double[] BACKINT=new double[MAXSLC+MAXSLC];
        double[] BACKVAR=new double[MAXSLC+MAXSLC];
        double[] SAMPVAR=new double[MAXSLC+MAXSLC];
        double[][] WTQXQY=new double[maxqxbin+1][maxqxbin+1];
        double[] Qxx=new double[maxqxbin+1];
        double[][] SQXQY=new double[maxqxbin+1][maxqxbin+1];
        double[] Qyy=new double[maxqxbin+1];
        double[][] SERRXY=new double[maxqxbin+1][maxqxbin+1];
        double[][] BQXQY=new double[maxqxbin+1][maxqxbin+1];
        double[][] BERRXY=new double[maxqxbin+1][maxqxbin+1];

        double[][] SBQXQY=new double[maxqxbin+1][maxqxbin+1];
        double[][] SBERRXY=new double[maxqxbin+1][maxqxbin+1];

        double[] WEIGHT1=new double[MAXQBIN+1];
        double[] SOFQS=new double[MAXQBIN+1];
        double[] SOFQB=new double[MAXQBIN+1];
        double[] SOQSMB=new double[MAXQBIN+1];
        double[] SMPERR=new double[MAXQBIN+1];
        double[] BKGERR=new double[MAXQBIN+1];
        double[] SMBERR=new double[MAXQBIN+1];

	String NAMCOL ="SN.DAT";
	String NAMLIS ="SMB.DAT";
	String NAMLIS2="S.DAT";
	String NAMLIS3="B.DAT";
	String namsq="S2D.dat";
	String namsq1="B2D.dat";
	String namsq2 ="SN2D.DAT";
  
	double SOURCEFREQ = 30.0;
	//float BETADN = 0.0042f;

	double THMIN ;//= 0.5*Math.atan(0.01*RADMIN/L2);
	double SINTHMIN ;//= Math.sin(THMIN);

	String TITLE ="      -     -     -(2)";
	      double Q0;// = 4.*PI/LAMAVG;

	      double QMINS ;//= Q0*SINTHMIN;
	      double QMAXS;// = Q0*SINTHMAX;


 /**
  * INITIALIZE SOME COUNTERS FOR COUNTING THE EVENTS:
C	TOTAL, NET
  */


	double CNTTOT = 0.0;
	double CNTNET = 0.0;
	double CNT1 = 0.0;
	double CNT2 = 0.0;
	double CNT3 = 0.0;
	double CNT4 = 0.0;
	double PI = 3.14159265;
	double TWOPI = 2.0*PI;
	double ZERO = 0.0;
	double RADMIN = 1.5;
	double RADMAX = 100.0;
  float SCALE;
 
 int RUNC,RUNB,RUNS;
  float XOFF, YOFF;
  public Reduce_KCL( DataSet TransS, DataSet TransB, DataSet Eff, DataSet Sens,
               float[] qu, DataSet[] RUNSds, DataSet[] RUNBds, 
             DataSet[] RUNCds,float BETADN, float SCALE,float THICK, float XOFF, float YOFF) 
               
 {  Object Result;
    this.SCALE = SCALE;
    this.XOFF = XOFF;
    this.YOFF= YOFF;
	 REDUCEOUT="S.OUT";
   this.TransS=TransS;
   this.TransB=TransB;
   this.Eff=Eff;
   this.Sens=Sens;
   this.RUNSds=RUNSds ;
   this.RUNBds= RUNBds;
   this.RUNCds=RUNCds ;
   this.qu=qu;

    

         RUNS=(((IntListAttribute)(RUNSds[0].getAttribute( Attribute.RUN_NUM))).getIntegerValue())[0];

         RUNB=(((IntListAttribute)(RUNBds[0].getAttribute( Attribute.RUN_NUM))).getIntegerValue())[0];

         RUNC=(((IntListAttribute)(RUNCds[0].getAttribute( Attribute.RUN_NUM))).getIntegerValue())[0];
 



 
                
 
      
    
        ITEMP[1]=LTEMP;
        sdt[1]=sdt[1];
        edt[1]=edt[1];
        tttt[1]=tttt[1];
        users[1]=users[1];




	LABELX="Q    (A**-1)";
	LABELY="I(Q)   (CM**-1)";
	int NCHRX=12;
  int NCHRY=15;
  int NCHRM=0;
	int ICURVE=-1;
	int IFERR=1;
  BufferedReader NCHR;

   /***
    * INITIALIZE
    */


	int IUI = 5;
	OUTUNIT = 9;


	HISTNUM = 1;


	IFDELAY = 1;
	ANSDN="Y";

	double PLANK = 6.626176E-27;
	double CMASS = 1.67495E-24;
	double PL = PLANK/CMASS;
	double TOTTIM = 1.0E6/SOURCEFREQ;

	XOFF = 0.0f;
	YOFF = 0.0f     ;

/**
 * INITIALIZE ARRAYS
 */
        for(int k=1;k<=MAXSLC;k++)
        {  
	        WEIGHT[k] = 0.0;

	   			SAMPINT[k] = 0.0;
	        SAMPVAR[k] = 0.0;

	   			BACKINT[k] = 0.0;
	   			BACKVAR[k] = 0.0;

        }

        for(int i=1;i<=maxqxbin;i++)
        {
        	for(int j=1;j<=maxqybin;j++)
          {

	      		WTQXQY[i][j] = 0.0;

	      		SQXQY[i][j] = 0.0;
	      		SERRXY[i][j] = 0.0;

	      		BQXQY[i][j] = 0.0;
            BERRXY[i][j] = 0.0;

	      		SBQXQY[i][j] = 0.0;
	      		SBERRXY[i][j] = 0.0;

            }
         }
        for(int i=1;i<=MAXQBIN;i++)
        {
	   			WEIGHT1[i] = 0.0;

	   			SOFQS[i] = 0.0;
	   			SOFQB[i] = 0.0;
	   			SOQSMB[i] = 0.0;

	   			SMPERR[i] = 0.0;
	   			BKGERR[i] = 0.0;
	   			SMBERR[i] = 0.0;

				}

/**
 * OPEN .OUT FILE
 */

     try{
          F1= new FileOutputStream(REDUCEOUT);
        }
     catch(Exception ex)
        { System.out.println("error") ;}
 
       
   if( BETADN > 0) 
      IFDELAY = 1;
	 IFNONLIN = 0;

/**
 * TYPE SOME PARAMETERS
 */

  System.out.println("CERTAIN STARTING PARAMETERS ARE THE FOLLOWING") ;
  System.out.flush();
  System.out.println ();
  System.out.flush();
	System.out.println("HISTOGRAM NUMBER IS ="+String.valueOf(HISTNUM));
	System.out.println("BEAM STOP RADIUS IN CM  ="+String.valueOf(RADMIN));
	System.out.println(" DELAYED NEUTRON CORRECTION IS MADE =");
	System.out.println("THE DELAYED NEUTRON FRACTION ="+String.valueOf(BETADN));
	System.out.println(" MGO FILTER IS IN THE BEAM ");
	System.out.println("Number of X and Y edge Chans masked for AD="+String.valueOf(NEDGE));
 // System.out.flush();

  PixelInfoListAttribute pAttr= (PixelInfoListAttribute)(
                      RUNSds[1].getData_entry(0).
                     getAttribute(Attribute.PIXEL_INFO_LIST) );
  PixelInfoList pilist =(PixelInfoList)(pAttr.getValue());
  IPixelInfo ipInfo =pilist.pixel(0);
  IDataGrid grid = ipInfo.DataGrid();
  NUMX= grid.num_rows();
  NUMY= grid.num_cols();
  FloatAttribute At =(FloatAttribute)( RUNSds[1].getData_entry(0).getAttribute(Attribute.INITIAL_PATH));
  float L1 = At.getFloatValue();

                           
  L2 = grid.position().length();
	int NSLICE = NUMX*NUMY;

	double TOFDIST = L1 + L2 ;
	LAMMAX = (PL/TOFDIST)*TOTTIM;

	THMIN = 0.5*Math.atan(0.01*RADMIN/L2);
	SINTHMIN = Math.sin(THMIN);

  XDIM = grid.width();
  YDIM = grid.height();
  System.out.println("Xdim,ydim="+XDIM+","+YDIM);
	double SFX = XDIM/NUMX;
	double SFY = YDIM/NUMY;

   tof_data_calc.SubtractDelayedNeutrons( (TabulatedData)RUNSds[0].getData_entry(0),
                                      30f,BETADN);
   tof_data_calc.SubtractDelayedNeutrons( (TabulatedData)RUNSds[1].getData_entry(1),
                                      30f,BETADN);
   tof_data_calc.SubtractDelayedNeutrons( (TabulatedData)RUNBds[0].getData_entry(0),
                                      30f,BETADN);
   tof_data_calc.SubtractDelayedNeutrons( (TabulatedData)RUNBds[1].getData_entry(1),
                                      30f,BETADN);
   tof_data_calc.SubtractDelayedNeutrons( (TabulatedData)RUNCds[0].getData_entry(0),
                                      30f,BETADN);
   tof_data_calc.SubtractDelayedNeutrons( (TabulatedData)RUNCds[1].getData_entry(1),
                                      30f,BETADN);

   
  
    
     RUNSds[1] =convertToLambda(RUNSds[1]);
     RUNSds[0] =convertToLambda(RUNSds[0]);
   
     RUNBds[1] =convertToLambda(RUNBds[1]);
     RUNBds[0] =convertToLambda(RUNBds[0]);
   
     RUNCds[1] =convertToLambda(RUNCds[1]);
     RUNCds[0] =convertToLambda(RUNCds[0]);
    /*PixelInfoList pilist7= (PixelInfoList)(SampleHist.getData_entry(0).
                             getAttribute( Attribute.PIXEL_INFO_LIST).getValue());
    IDataGrid grid7=   (pilist7.pixel(0)).DataGrid();
    */
   xscl =RUNSds[1].getData_entry( NUMX*NUMY/2).getX_scale();
     
      RUNSds[0].getData_entry(0).resample(xscl,IData.SMOOTH_NONE);
 
      RUNBds[0].getData_entry(0).resample(xscl,IData.SMOOTH_NONE);
      if( RUNCds[0] != null)
        RUNCds[0].getData_entry(0).resample(xscl,IData.SMOOTH_NONE);
      RUNSds[0].getData_entry(2).resample(xscl,IData.SMOOTH_NONE);
      RUNBds[0].getData_entry(2).resample(xscl,IData.SMOOTH_NONE);
      if( RUNCds[0] != null)
         RUNCds[0].getData_entry(2).resample(xscl,IData.SMOOTH_NONE);
     float[] yy = RUNSds[0].getData_entry(0).getY_values();

      yy = RUNCds[0].getData_entry(0).getY_values();

     Resample( RUNSds[1], xscl);
      Resample( RUNBds[1], xscl);
      Resample( RUNCds[1], xscl);

    LAMBDA = xscl.getXs();
 


  
	 this.SCALE = this.SCALE/THICK;
        /**
         * SET UP SIN(RADII), sinx(x,y), siny(x,y) AND MASK OUT BEAM STOP
       AND BEYOND ANY SPECIFIED RADIUS.  Set up the azimuthal angle array.
         */
	for(int J=1;J<=NUMY;J++)
        {
	  for (int i=1;i<=NUMX;i++)
          {
	      int K = i + (J-1)*NUMX;
        PixelInfoListAttribute pilistAt=  (PixelInfoListAttribute)(RUNSds[1]
                                      .getData_entry(K-1).
                                      getAttribute(Attribute.PIXEL_INFO_LIST));
        IPixelInfo ipinf =((PixelInfoList)pilistAt.getValue()).pixel(0);
        int row = (int)ipinf.row();
        int col = (int)ipinf.col();
        Vector3D dis = new Vector3D(grid.position((float)row,(float)col));
        Vector3D di = new Vector3D(grid.position() );
         dis.subtract( di );

         XX[row] = dis.dot(ipinf.x_vec() ) ;
         YY[col] = dis.dot( ipinf.y_vec()); 
	      //RAD = SQRT(XX(I)**2 + YY(J)**2)
              double RAD = Math.sqrt((XX[row]*XX[row] )+ (YY[col]*YY[col]));

	      SINRAD[K] = (double)Math.sin(0.5*Math.atan(0.01*RAD/L2));
	      sinx[row][col]=(double)Math.sin(0.5*Math.atan(0.01*XX[row]/L2));
	      siny[row][col]=(double)Math.sin(0.5*Math.atan(0.01*YY[col]/L2));

	   }

	}
/**
 * CALCULATION OF THE SPECTRUM. THIS IS THE PRODUCT OF THE EFFICIENCY
C	RATIO AD-TO-M1 AND THE BEAM MONITOR OF THE SAMPLE SCATTERING RUN FILE
C 	FROM NOW ON ALL THE DATA ARE NORMALISED BASED ON THIS BEAM MONITOR.
C 	ACTUALLY IN THE END THEY GET CANCELLED OUT AS THE SPECTRUM COMES AS
C	DENOMINATOR AND THE AD COUNTS IN INDIVIDUAL CELLS IN THE CASE OF
C	SAMPLE, BACKGROUND AND THE CADMIUM RUNS ARE NORMALISED BY MULTIPLYING
C	THEM BY THE SAME SAMPLE BEAM MONITOR COUNTS IN THE SCATTERING RUN FILE.
C
C	DETERMINE THE WAVELENGTH OF NEUTRONS AT WHICH THE SPECTRUM PEAKS IN
C	INTENSITY. THIS IS SOMEWHAT RELATED TO THE SCALEFACTOR.
C
 */

/*
	System.out.println() ;
	System.out.println("   WAVELENGTH         SPECTRUM       SPEC ERROR");

	double SPMX = 0;
        int KM=0;
	for (int I=1;I<=NUMW;I++)
        {

	   LAMAVG = 0.5*(LAMBDA[I] + LAMBDA[I+1]);
	   double SPEC = AREATOM1[I]*RUNSds[0].getData_entry(0).getX_scale().getX(I);
	   double SPERR = AREATOM1ERR[I]*RUNSds[0].getData_entry(0).getX_scale().getX(I)[I];

	   if(SPEC>SPMX)
              {
	      SPMX = SPEC;
	      KM = I;

	      }

	   System.out.println(String.valueOf( LAMAVG)+" "+String.valueOf(SPEC)+" "+String.valueOf(SPERR));

	  }
        try{
        str1=" ";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;
        str1="WAVELENGTH AND CORRESPONDING MAX-OF-SPECTRUM";
        for (int ii1=0;ii1<str1.length();ii1++)
              F1.write(str1.charAt(ii1)) ;
	 		  LAMAVG = 0.5*(LAMBDA[KM] + LAMBDA[KM+1]);
        str1=String.valueOf(LAMAVG);
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;
        str1=String.valueOf(SPMX);
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=" ";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf("WAVELENGTH AND THE CORRESPONDING  MAX-OF-SPECTRUM");
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf("WAVELENGTH AND THE CORRESPONDING  MAX-OF-SPECTRUM");
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

				System.out.println(String.valueOf(LAMAVG)+" "+ String.valueOf(SPMX) ) ;

       // F1.write(" NORMALIZATION DONE CHANNEL BY CHANNEL") ;
        }catch(IOException e){
        System.out.println("Error writing") ;
        }
*/

	IF2D = 0;


/**
 * ENTER ANY OMITTED TIME CHANNELS
 */
/*	int slice1=0;
	while ((slice1<0)||(slice1>NUMW))
          {
           System.out.print("ENTER FIRST TIME CHANNEL, T1 (1-I2)");
           System.out.println(String.valueOf(NUMW) ) ;
              try
              {
              SLICE1=(int) System.in.read() ;
              }catch (IOException e)
              {
              System.out.println("K:Error Reading") ;
              //System.exit(4);
              }
	}
	int slice2=SLICE1-1;
        int SLICE5;
	while ((slice2<slice1)||(slice2>NUMW))
          {
          System.out.println("ENTER LAST TIME CHANNEL, T2 (>=T1) : ");
              try
              {
              SLICE5=(int) System.in.read() ;
              }catch (IOException e)
              {
              System.out.println("Error Reading") ;
              //System.exit(5);
              }

	}
*/
/*
	for(int II=1;II<NUMW;II++)
          {
	   if(II<SLICE1 || II>SLICE2)
            {
	      IFOMIT[II] = 1;
            }
	   else
            {
	      IFOMIT[II] = 0;
            }


	}

	int IST=-1;
        int IFI=0;
	while (IST!=0)
          {
          System.out.println("DO YOU WANT TO OMIT GROUPS OF TIME CHANNELS? IF SO ENTER first,last OR 0,0 :" ) ;
              try
              {
              IST=(int) System.in.read() ;
              IFI=(int) System.in.read() ;
              }catch (IOException e)
              {
              System.out.println("Error Reading") ;
              System.exit(5);
              }

	   if(IST!=0)
              {
	      for(int ii=IST;ii<=IFI;ii++)
              {
	         IFOMIT[ii] = 1;

	      }

	   }

	}
*/
/*630	FORMAT(/,' Do you wish to analyze the data as if the scattering'
     1	,' were ISOTROPIC (I) or ',/,
     2	' ANISOTROPIC (A) ; ',/,
     3	' ANSWER (I/A) : ',$)
 632	FORMAT(/' ENTER FIRST TIME CHANNEL, T1 (1-',I2,') : ',$)
 634	FORMAT(' ENTER LAST TIME CHANNEL, T2 (>=T1) : ',$)
 636	FORMAT(/,' DO YOU WANT TO OMIT GROUPS OF TIME CHANNELS? '/
     1	' IF SO ENTER first,last OR 0,0 : ',$)
 638	FORMAT (/,' MINQ AND MAXQ ACCESSIBLE ARE (A-1) ',2F15.7,/)
 639	format(/,' Input the number of different Q bin sections'
     1	' desired : ')
 6391	format(/,' Constant dQ Bin Section # ',I1)
 6395	format(/,' Input the Q bin resolution (dQ/Q) : ')
 640	FORMAT(/,' CHOOSE THE RANGE OF Q IN THIS SECTION : ')
 642	FORMAT(' ENTER QMIN IN A-1:  ',$)
 644	FORMAT('    ENTER QMAX in A-1:  ',$)
 646	FORMAT(/,' THE Q SPACE CHOSEN IS SPANNED BY: ',/,
     1	'     QMIN = ',F10.3,10X,'QMAX = ',F10.3)
 648	FORMAT (/,' ENTER NUMBER OF const dq BINS in this section : ',
     1	' [1<= N <=512] : ',/,' (THIS IS ONLY FOR THE S(Q) VS Q ARRAY)')


     */

/**
 * CALCULATE THE GEOMETRICALLY SPECIFIED MINIMUM Q AND
    MAXIMUM Q for isotropic data analysis
 */
/*
	if(IF2D==0)
         {
	   LLOW = 0.5*(LAMBDA[SLICE1+1] + LAMBDA[SLICE1]);

	   LHIGH = 0.5*(LAMBDA[SLICE2+1] + LAMBDA[SLICE2]);

	   THMAX = 0.5*Math.atan(0.01*RADMAX/L2);

	   SINTHMAX = Math.sin(THMAX);

	   MINQ = (4.0*PI/LHIGH)*SINTHMIN;
	   MAXQ = (4.0*PI/LLOW)*SINTHMAX;

	   double THMN = THMIN*180/PI;
	   double THMX = THMAX*180/PI;
           try{
       str1=" ";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;
        str1="Accessible Theta_min & Theta_max ";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(THMN);
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(THMX);
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

	   System.out.println("Accessible theta_min & Theta_max "+String.valueOf(THMN)+" "+String.valueOf(THMX));
        str1="MINQ AND MAXQ ACCESSIBLE ARE (A-1) "+String.valueOf(MINQ)+", "+String.valueOf(MAXQ);
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

           System.out.println("MINQ AND MAXQ ACCESSIBLE ARE (A-1) "+String.valueOf(MINQ)+", "+String.valueOf(MAXQ)) ;
           }catch(IOException e){
           System.out.println("Error write") ;
           }
	   System.out.println("Input the number of different Q bin sections desired :") ;
              try
              {
              qbins=(int) System.in.read() ;
              }catch (IOException e)
              {
              System.out.println("Error Reading") ;
              System.exit(6);
              }

*/
/**
 * Added the option of constant dQ/Q bin as qbins=-99 and input
   the resolution as Qres.  Jamie Ku 5/9/97
 */
/*	   if(qbins==-99)
            {
              System.out.println("Input the Q bin resolution (dQ/Q) :") ;
              try
              {
              Qres=(int) System.in.read() ;
              }catch (IOException e)
              {
              System.out.println("Error Reading") ;
              System.exit(7);
              }
	      QUE1[1] = MINQ;
	      QUE2[1] = (1.+Qres/2.)*MINQ/(1.-Qres/2.);
	      QUE4[1] = (QUE1[1]+QUE2[1])/2.0;
	      QUE3[1] = QUE4[1]*QUE4[1];
	      int n=1;

	      while((n<200)&&(QUE2[n]<MAXQ))
              {
	         n=n+1;
	         QUE1[n] = QUE2[n-1];
	         QUE2[n] = (1.+Qres/2.)*QUE1[n]/(1-Qres/2);
	         QUE4[n] = (QUE1[n]+QUE2[n])/2.0;
	         QUE3[n] = QUE4[n]*QUE4[n];

	      }

	      if(QUE2[n]>MAXQ)
              {

	         QUE2[n] = MAXQ;
	         QUE4[n] = (QUE1[n]+QUE2[n])/2.0;
	         QUE3[n] = QUE4[n]*QUE4[n];

	      }

	      NDIV = n;
            }
	   else
            {
	      NDIV = 0;
	      for(int iii=1;iii<=qbins;iii++)
              {
                 System.out.println("Constant dQ Bin Section # ,I1 "+String.valueOf(iii) ) ;
                 System.out.println("CHOOSE THE RANGE OF Q IN THIS SECTION :") ;

	         if(iii==1)
                 {
                    System.out.println("ENTER QMIN IN A-1:") ;

                   try
                    {
                    QMINQ[iii]=(int) System.in.read() ;
                    }catch (IOException e)
                    {
                    System.out.println("Error Reading") ;
                    System.exit(8);
                    }

	            if(QMINQ[iii]<MINQ)
                      QMINQ[iii] = MINQ;

                  }
	         else
                  {
	            QMINQ[iii]=QMAXQ[iii-1];

	         }
                 System.out.println("ENTER QMAX in A-1:") ;

                   try
                    {
                    QMINQ[iii]=(int) System.in.read() ;
                    }catch (IOException e)
                    {
                    System.out.println("Error Reading") ;
                    System.exit(8);
                    }
	         if(QMAXQ[iii]>MAXQ||QMAXQ[iii]==0.0)
                 	         QMAXQ[iii] = MAXQ;

                 System.out.println("THE Q SPACE CHOSEN IS SPANNED BY: QMIN = "+String.valueOf(QMINQ[iii]) +" QMAX = "+String.valueOf(QMAXQ[iii])) ;
                 System.out.println("ENTER NUMBER OF const dq BINS in this section : [1<= N <=512] :(THIS IS ONLY FOR THE S(Q) VS Q ARRAY)");
                 int N=0;
                 int   NDIVOLD ;

                   try
                    {
                    N=(int) System.in.read() ;
                    }catch (IOException e)
                    {
                    System.out.println("M:Error Reading") ;
                    System.exit(9);
                    }
	         DIV[iii] = (double)(N);
	         NSHIFT[iii] = NDIV;
	         NDIVOLD = NDIV+1;
	         NDIV = NDIV+N;

*/
/**
 * NOTE THAT QMIN AND QMAX ARE THE EXTREME VALUES OF Q AND
	NOT THE MEAN VALUES FOR A GIVEN TIME CHANNEL.
 */
/*         DELQ[iii] = (QMAXQ[iii]-QMINQ[iii])/DIV[iii];

	         for(int n=NDIVOLD;n<=NDIVOLD;NDIV++)
                 {

	      QUE1[N] = QMINQ[iii] + DELQ[iii]*(double)(N-NSHIFT[iii]-1);
	      QUE2[N] = QMINQ[iii] + DELQ[iii]*(double)(N-NSHIFT[iii]);
	      QUE4[N] = (QUE1[N]+QUE2[N])/2.0;
	      QUE3[N] = QUE4[N]*QUE4[N];

	         }

	     //}for(int iii=1;iii<=qbins;iii++)

	 //  } else \n NDIV=0

//	}if(IF2D==0
*/
/**
 * -TWO DIMENSIONAL-start
 */
  IF2D=0;
	if(IF2D==1)
         {
          /**
           * CALCULATE THE GEOMETRICALLY SPECIFIED MINIMUM Qx, qy AND
            C	MAXIMUM Qx, qy for anisotropic data analysis
           */

	   LLOW = 0.5*(LAMBDA[SLICE1+1] + LAMBDA[SLICE1]);

	   LHIGH = 0.5*(LAMBDA[SLICE2+1] + LAMBDA[SLICE2]);
	   THMAX = 0.5*Math.atan(0.01*RADMAX/L2);

	   SINTHMAX =Math.sin(THMAX);
	   QMIN = 0.0035;

	   SINxMAX = Math.sin(0.5*Math.atan(0.01*XX[NUMX]/L2));
	   SINyMAX = Math.sin(0.5*Math.atan(0.01*YY[NUMX]/L2));
	   SINxMin = Math.sin(0.5*Math.atan(0.01*(XX[1])/L2));
	   SINyMin = Math.sin(0.5*Math.atan(0.01*(YY[1])/L2));

	   minqx = (4.0*PI/LLOW)*SINxMin;
	   maxqx = (4.0*PI/LLOW)*SINxMAX;
	   MINQy = (4.0*PI/LLOW)*SINyMin;
	   MAXQy = (4.0*PI/LLOW)*SINyMAX;
     /*      try{
        str1="minqx AND maxqx ACCESSIBLE ARE (A-1) ";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(minqx);
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=",";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(maxqx);
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

           System.out.println("minqx AND maxqx ACCESSIBLE ARE (A-1) "+String.valueOf(minqx)+" "+String.valueOf(maxqx) ) ;
           }catch(IOException e)
           {
             System.out.println("Error writing") ;
            }
     */
	   Qxmin = minqx;
	   Qxmax = maxqx;
      /*
           System.out.println("CHOOSE THE RANGE OF Qx WITHIN THE ABOVE RANGE :") ;
           System.out.println("    ENTER Qxmin IN A-1:  ") ;

                   try
                    {
                    Qxmin=(int) System.in.read() ;
                    }catch (IOException e)
                    {
                    System.out.println("Error Reading") ;
                    System.exit(10);
                    }


	   if(Qxmin<minqx)
            Qxmin = minqx;

	   System.out.println("    ENTER Qxmax in A-1:  ") ;
                   try
                    {
                    Qxmax=(int) System.in.read() ;
                    }catch (IOException e)
                    {
                    System.out.println("Error Reading") ;
                    System.exit(11);
                    }
	   if(Qxmax>maxqx||Qxmax==0.0)
            Qxmax = maxqx;

            System.out.println("THE Qx SPACE CHOSEN IS SPANNED BY: Qxmin = "+String.valueOf(Qxmin) +" "+String.valueOf(Qxmax) );

          System.out.println("ENTER NUMBER OF const dq BINS WITHIN THE Qx-RANGE : [1<= N <=250] ") ;
                   try
                    {
                    Nx=(int) System.in.read() ;
                    }catch (IOException e)
                    {
                    System.out.println("Error Reading") ;
                    System.exit(12);
                    }

     */
	   DIVx = (double)Nx;
	   NDIVx = Nx;
           try{
        str1="MINQy AND MAXQy ACCESSIBLE ARE (A-1) "+String.valueOf(MINQy)+" "+String.valueOf(MAXQy) ;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

           System.out.println("MINQy AND MAXQy ACCESSIBLE ARE (A-1) "+String.valueOf(MINQy)+" "+String.valueOf(MAXQy)  );
           }catch(IOException e)
           {
           System.out.println("Error writing") ;
           }

	   Qymin = MINQy;
	   Qymax = MAXQy;
	   System.out.println("CHOOSE THE RANGE OF Qy WITHIN THE ABOVE RANGE :") ;
           System.out.println("ENTER Qymin IN A-1:") ;

                   try
                    {
                    Qymin=(double) System.in.read() ;
                    }catch (IOException e)
                    {
                    System.out.println("NError Reading") ;
                    //System.exit(12);
                    }
     System.out.println("L1:");
	   if(Qymin<MINQy)
              Qymin = MINQy;

          System.out.println("ENTER Qymax in A-1:") ;

                   try
                    {
                    Qymax=(double) System.in.read() ;
                    }catch (IOException e)
                    {
                    System.out.println("O:Error Reading") ;
                    System.exit(12);
                    }


	   if(Qymax<MAXQy||Qymax==0.0)
              Qymax = MAXQy;

          System.out.println("THE Qy SPACE CHOSEN IS SPANNED BY: Qymin = "+String.valueOf(Qymin)+" Qymax = "+String.valueOf(Qymax) ) ;
                   try
                    {
                      Ny=(int) System.in.read() ;
                    }catch (IOException e)
                    {
                    System.out.println("P:Error Reading") ;
                    System.exit(13);
                    }

	   DIVy = (double)Ny;
	   NDIVy = Ny       ;




/**
 * NOTE THAT Qx,yMIN AND Qx,yMAX ARE THE EXTREME VALUES OF Qx,y AND
C	NOT THE MEAN VALUES FOR A GIVEN TIME CHANNEL.
 */

	   xDELTAQ = (Qxmax-Qxmin)/DIVx;
	   yDELTAQ = (Qymax-Qymin)/DIVy;


/**
 * IN THE FOLLOWING LOOP ( AND FOLLOWING);
C	QUEx,y1(I) IS THE MINIMUM Qx,y VALUE IN THE I'TH TIME CHANNEL,
C	QUEx,y2(I) IS THE MAXIMUM Qx,y VALUE IN THE I'TH TIME CHANNEL,
C	QUEx,y4(I) IS THE MEAN VALUE OF Qx,y IN THE I'TH TIME CHANNEL,
C	QUEx,y3(I) IS JUST Qx,y(MEAN)**2
 */
	   for(int N=1;N<=NDIVx;N++)
            {
	      QUEx1[N] = Qxmin + xDELTAQ*(double)(N-1);
	      QUEx2[N] = Qxmin + xDELTAQ*(double)(N);
	      QUEx4[N] = (QUEx1[N]+QUEx2[N])/2.0;
	      QUEx3[N] = QUEx4[N]*QUEx4[N];

	   }

	   for( int N=1;N<=NDIVy;N++)
           {

	      QUEY1[N] = Qymin + yDELTAQ*(double)(N-1);
	      QUEY2[N] = Qymin + yDELTAQ*(double)(N);
	      QUEY4[N] = (QUEY1[N]+QUEY2[N])/2.0;
	      QUEY3[N] = QUEY4[N]*QUEY4[N];

	   }

	}
/**
 * TWO DIMENSIONAL ---- stop
 */




/**
 * CONSTRUCT THE TITLE
 *
 */
                   try{
        str1=TITLE;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=" ";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(RUNS) ;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;



        str1=TITLE;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=" ";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(RUNB);
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;


	if(RUNC!=0)
        {
        str1=TITLE;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;
        str1=" ";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(RUNC) ;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;



        }

        }catch(IOException e)
        {
        System.out.println("AA:Error Writing") ;
        }

/**
 * GET RUNFILE INFO
 */
//	CALL SETOPNG(RUNS,IERR)


/**
 * LOOP OVER SLICES
 */
	for( SLICE=1;SLICE<=2;SLICE++)
        {

	   if(IFOMIT[SLICE]!=1)
            {
              System.out.println("CALCULATING CHANNEL" +String.valueOf(SLICE) ) ;


  }
}
}
//System.out.println("L2:");
/**
 * GET THE AREA DETECTOR DATA FOR SAMPLE, BACKGROUND, AND CADMIUM
C	RUNS.  NORMALIZE AND CORRECT THIS DATA TO PRODUCE I(X,Y,SLICE)
 */

/*	CALL AREADATA_V3(RUNS,RUNB,RUNC,OUTUNIT,SLICE,NUMX,NUMY,NHS,
     1	NCHTOT,MICRO,HISTNUM,IFDELAY,BETADN,LAMMAX,IFGOOD,
     2	DETSEN,DETERR,TRANS,TRANB,TSERR,TBERR,LAMBDA,
     3	AREATOM1,AREATOM1ERR,SAMPM1,BACKM1,CADM1,SAMPDN,BACKDN,CADDN,
     4	SAMPLE,WEIGHT,SAMPINT,SAMPVAR,BACKINT,BACKVAR,IERR)
     */


 /* ---------------------------- getResult ------------------------------- */
  /**  Executes the operator using the parameters that were set up
  *@return  "Success" if there were no errors otherwise  the ErrorString
  *             "No Data Set Selected" is returned.<P>
  *
  *NOTE: A SelectedGraph View will also pop up
  */
  public Object getResult()
  { 
  Object Res = (new DataSetDivide_1(RUNSds[1], RUNSds[0],1,true)).getResult();
       if( Res instanceof ErrorString)
        return new ErrorString("AA1:"+Res.toString());
     if( Res instanceof String)
          return new ErrorString( "AB"+(String)Res);
    RelSamp = (DataSet) Res;
   System.out.println( "ration samp to monitor"+RelSamp.getData_entry(2).getY_values()[4]);
   RelSamp.setTitle("Samp to MOnitor");

   Res = (new DataSetDivide_1(RUNCds[1], RUNCds[0],1,true)).getResult();
       if( Res instanceof ErrorString)
        return new ErrorString("AB:"+Res.toString());;
     if( Res instanceof String)
          return new ErrorString( "AC:"+(String)Res);
    DataSet RelCadmium = (DataSet) Res;
   System.out.println( "ratio cadmi to monitor"+RelCadmium.getData_entry(2).getY_values()[4]);
   Res = (new DataSetDivide_1(RUNBds[1], RUNBds[0],1,true)).getResult();
       if( Res instanceof ErrorString)
        return new ErrorString("AD:"+Res.toString());;
     if( Res instanceof String)
          return new ErrorString( "AE:"+(String)Res);
    DataSet RelBackground= (DataSet) Res;

   
   Res = (new DataSetSubtract(RelSamp, RelCadmium,true)).getResult();
       if( Res instanceof ErrorString)
        return new ErrorString("AF:"+Res.toString());;
     if( Res instanceof String)
          return new ErrorString( "AG:"+(String)Res);
   RelSamp = (DataSet) Res;
     RelSamp.setTitle("Sample rel Monitor - CAdmium");
      System.out.println( "ratio Samp-cad to monitor"+RelSamp.getData_entry(2).getY_values()[4]);
      Res = (new DataSetSubtract(RelBackground, RelCadmium,true)).getResult();
       if( Res instanceof ErrorString)
        return new ErrorString("AH:"+Res.toString());;
     if( Res instanceof String)
          return new ErrorString( "AI:"+(String)Res);
     RelBackground = (DataSet) Res;

    TransS.setX_units( RelSamp.getX_units());
    System.out.println("Trans is"+ TransS.getData_entry(0).getY_values()[4]);
    Res = (new DataSetDivide_1(RelSamp, TransS,TransS.getData_entry(0).getGroup_ID(),true)).getResult();
       if( Res instanceof ErrorString)
        return new ErrorString("AJ:"+Res.toString());;
     if( Res instanceof String)
          return new ErrorString( "AK:"+(String)Res);
     RelSamp = (DataSet) Res;
     RelSamp.setTitle( "Adj Samp div by Transm");
     System.out.println( "ratio Samp-cad dvi trans to monitor"+RelSamp.getData_entry(2).getY_values()[4]);
    TransB.setX_units( RelBackground.getX_units());
     Res = (new DataSetDivide_1(RelBackground, TransB,TransB.getData_entry(0).getGroup_ID(),true)).getResult();
       if( Res instanceof ErrorString)
        return new ErrorString("AL:"+Res.toString());;
     if( Res instanceof String)
          return new ErrorString( "AM:"+(String)Res);
     RelBackground = (DataSet) Res;


    System.out.println("L3:");
    // float[] eff = Eff.getData_entry(0).getY_values();
     //  Divide Sample by sens and efficiency
     DataSet weight = makeNewSensEffDS( Sens, Eff, xscl, RelSamp);
     //ScriptUtil.display(weight.clone() );
     RelSamp.setTitle("XXXXXXXXXXXXXXXx");
    System.out.println( "Eff"+Eff.getData_entry(0).getY_values()[4]);
     System.out.println( "Sens"+Sens.getData_entry(2).getY_values()[0]);
      System.out.println( "weight"+weight.getData_entry(2).getY_values()[4]);
     //ScriptUtil.display( RelSamp.clone());
     Res = (new DataSetDivide(RelSamp, weight,true)).getResult();
       if( Res instanceof ErrorString)
        return new ErrorString("AL:"+Res.toString());;
     if( Res instanceof String)
          return new ErrorString( "AM:"+(String)Res);
     RelSamp = (DataSet) Res;
    System.out.println( "ratio Samp-cad div weight"+RelSamp.getData_entry(2).getY_values()[4]);
    Res = (new DataSetDivide(RelBackground, weight,true)).getResult();
       if( Res instanceof ErrorString)
        return new ErrorString("AL:"+Res.toString());;
     if( Res instanceof String)
          return new ErrorString( "AM:"+(String)Res);
     RelBackground = (DataSet) Res;

    //ScriptUtil.display( RelSamp);
    //ScriptUtil.display(RelBackground);
   
    RelSamp.setTitle( "Sample Intensities.corrected -cadmium,eta, trans");
    RelBackground.setTitle( "Backgound Intensities.corrected -cadmium,eta, trans");
   //ScriptUtil.display( RelSamp.clone());
   //ScriptUtil.display( RelBackground.clone());
   //----------------------- After MonGet--------------------
    //QUE1 is the "xscale" for 1 to N 
  


    (new DataSetMultiply_1(weight, RUNSds[0],1,false)).getResult();
    System.out.println("weight[2][5]="+weight.getData_entry(2).getY_values()[5]);
   
     XScale xscll = new VariableXScale( qu);

     Operator opp = null;
    try{
    ScriptUtil.save(  "SampN0XOFF.isd",RelSamp);
    ScriptUtil.save( "weightNOXoff.isd",weight);
    AdjustGrid( RelSamp, XOFF,YOFF);
    AdjustGrid( weight, XOFF,YOFF);
    AdjustGrid( weight, XOFF,YOFF);

    ScriptUtil.save( "SampXOFF.isd",RelSamp);
    ScriptUtil.save( "weightXoff.isd",weight);
    }catch( Exception ss1){System.out.println("save errora"+ss1);}
    Res= (new DiffractometerWavelengthToQ(RelSamp, 0f,100f,0)).getResult();
    if( Res instanceof ErrorString)
      return Res;
    RelSamp = (DataSet)Res;
    
    Res= (new DiffractometerWavelengthToQ(RelBackground, 0f,100f,0)).getResult();
    if( Res instanceof ErrorString)
      return Res;
    RelBackground = (DataSet)Res;

   Res= (new DiffractometerWavelengthToQ(weight, 0f,100f,0)).getResult();
    if( Res instanceof ErrorString)
      return Res;
    weight = (DataSet)Res;
   try{
    ScriptUtil.save(  "SampXOFFQ.isd",RelSamp);
    ScriptUtil.save( "weightXoffQ.isd",weight);
   Resample( RelSamp, xscll);
   Resample( RelBackground, xscll);
   Resample( weight, xscll);
   
    ScriptUtil.save(  "SampXOFFQ1.isd",RelSamp);
    ScriptUtil.save( "weightXoffQ1.isd",weight);
    }catch(Exception ss2){System.out.println("save exception B"+ss2);}
   //ScriptUtil.display( RelSamp.clone());

   //ScriptUtil.display(RelBackground.clone());
   //ScriptUtil.display( weight.clone());
   //if( 3==3) return null;
   
   Res =(new DataSetMultiply(RelSamp, weight, false)).getResult();
   System.out.println("SCALE="+SCALE);
 
   Res =(new DataSetScalarMultiply(RelSamp, SCALE, false)).getResult();

   if(Res instanceof ErrorString)
     return new ErrorString( "AA:"+Res.toString());
   else if( Res instanceof String)
     System.out.println("Res of Scalar mult="+Res.toString());
   Res =(new DataSetMultiply(RelBackground, weight, false)).getResult();

   Res =(new DataSetScalarMultiply(RelBackground, SCALE, false)).getResult();
   if(Res instanceof ErrorString)
     return new ErrorString( "BB:"+Res.toString());
   else if( Res instanceof String)
     System.out.println("Res of Scalar mult="+Res.toString());

    RelSamp.setTitle("Weighted Resamp Q for Sample");
    //ScriptUtil.display( RelSamp.clone());
   // if( 3==3) return null;
      EliminateBadDetectors( RelSamp, Sens);
      EliminateBadDetectors( RelBackground, Sens);
      EliminateBadDetectors( weight, Sens);
      Res = (new DataSetSubtract( RelSamp, RelBackground, true)).getResult();
      if(Res instanceof ErrorString)
          return Res;
      DataSet RelDiff = (DataSet)Res;
 
  
  if(IF2D !=1){

    
     
      EliminateBadDetectors( RelSamp, Sens);
      EliminateBadDetectors( RelBackground, Sens);
      EliminateBadDetectors( weight, Sens);
      Res = (new DataSetSubtract( RelSamp, RelBackground, true)).getResult();
      if(Res instanceof ErrorString)
          return Res;
      RelDiff = (DataSet)Res;
      DataSet SSampQ = SumAllDetectors(RelSamp);
      DataSet SBackQ = SumAllDetectors(RelBackground);
      DataSet SDifQ = SumAllDetectors(RelDiff);
      DataSet Sweight =SumAllDetectors(weight);

      Res=(new DataSetDivide( SSampQ,Sweight,false)).getResult();
      if(Res instanceof ErrorString)
          return Res;      
     Res=(new DataSetDivide( SBackQ,Sweight,false)).getResult();
      if(Res instanceof ErrorString)
          return Res;      
     Res=(new DataSetDivide( SDifQ,Sweight,false)).getResult();
      if(Res instanceof ErrorString)
          return Res;      
    
    SSampQ.setTitle("Neutron Corrected Sample-"+StringUtil.toString( SSampQ.getData_entry(0).
                                 getAttributeValue( Attribute.RUN_NUM)));

    SBackQ.setTitle("Neutron Corrected Background-"+StringUtil.toString( SBackQ.getData_entry(0).
                                 getAttributeValue( Attribute.RUN_NUM)));

   SDifQ.setTitle("Neutron Corrected Sample-Background-"+StringUtil.toString(SDifQ.getData_entry(0).
                                 getAttributeValue( Attribute.RUN_NUM)));
  
    Vector V = new Vector();
    
    V.addElement( SSampQ);
    V.addElement( SBackQ);
    V.addElement( SDifQ);
    return V;
 }

  for( int SLICE = 0; SLICE < xscl.getNum_x(); SLICE++)
   for( int k=0; k< RelSamp.getNum_entries(); k++){
      if( SLICE<3)if(k<2)System.out.println("in Big loop"+xscl.getNum_x()+","+RelSamp.getNum_entries());
/**
 * SET UP SOME USEFUL CONSTANTS
 */
     
	      //LAMAVG = 0.5*(LAMBDA[SLICE] + LAMBDA[SLICE+1]);
        LAMAVG = .5f*(xscl.getX(SLICE)+xscl.getX(SLICE+1));
	      Q0 = 4.*PI/LAMAVG;

	      QMINS = Q0*SINTHMIN;
	      QMAXS = Q0*SINTHMAX;
/**
 * WRITE OUT SOME SLICE-BY-SLICE INFO
 */


 /*       try{
        str1=TITLE;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;
        str1=" ";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(RUNS) ;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1="Lambda=";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(LAMAVG);
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(" QMIN=") ;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(QMINS);
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(" QMAXS=");
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(QMAXS) ;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;



              //WRITE(OUTUNIT,654) LAMAVG,QMINS,QMAXS
	      System.out.println(" Lambda="+String.valueOf(LAMAVG)+" QMIN="+String.valueOf(QMINS)+" qmax="+String.valueOf(QMAXS));
      }catch (IOException e)
      {
        System.out.println("Error writing") ;
      }
*/
//654	FORMAT(' Lambda=',F8.4,5X,' QMIN=',F8.4,5X,' qmax='F8.4)

/*********************
C
C	LOOP OVER ALL AREA DETECTOR CELLS, AND ADD THE ASSOCIATED
C	SCATTERING INTENSITY TO THE APPROPRIATE Q BIN
C
C********************/
 /*             int    indyy,indxx;
	      for(int k=1; k<=NSLICE;k++)
              {

                 CNTTOT = CNTTOT + SAMPLE[k];
	         indyy=k/NUMX+1;
	         indxx=k-(indyy-1)*NUMX;

	         if(IFGOOD[k]==0) {
                  continue;//GO TO 390
                }

*/

/*/////////////////
C
C	FIND Q, AND IF Q IS OUTSIDE THE SPECIFIED RANGE, IGNORE THIS
C	DATA POINT.
C
C///////////////// */
           int indyy=k/NUMX+1;
	         int indxx=k-(indyy-1)*NUMX;
	         double Q = Q0*SINRAD[k];
	         double Qx = (double)Q0*(double)sinx[indxx][indyy];
	         double Qy = (double)Q0*(double)siny[indxx][indyy];

          if(IF2D==0) {
        QMIN = qu[0];
        QMAX = qu[ qu.length-1];
	      if((Q>=qu[0])&&(Q <=qu[qu.length-1])){
                
              if(IF2D==1) {
                double Qxy = Math.sqrt(Qx*Qx+Qy*Qy) ;
                if(Qxy<QMIN) continue; //GO TO 390
	              if((Qx<Qxmin)||(Qx>Qxmax)) continue; //go to 390
	              if((Qy<Qymin)||(Qy>Qymax)) continue; //go to 390

              }

              CNTNET = CNTNET + RUNSds[1].getData_entry(k).getY_values()[SLICE];

/*C//////////////////
C
C	DEAL WITH THE ISOTROPIC CASE
C
C//////////////////*/

          if(IF2D==0){
         qbins = -99;
	      if(qbins==-99){
	        nleft = 1;
	        nright = NDIV+1;

	       /* do {

                    ncenter = (nleft+nright)/2;

                    if(Q<QUE1[ncenter]){
                      nright = ncenter;
                    }
                    else {
                    nleft = ncenter;
                    }
              }while ((nright-nleft)>1);

	   int N = nleft;
  */
     int N = Arrays.binarySearch(qu,(float) Q);
     if( N<0) N=-N-1;
     float wt =weight.getData_entry(k).getY_values()[SLICE];
     float yval = RelSamp.getData_entry(k).getY_values()[SLICE];
     float bval = RelBackground.getData_entry(k).getY_values()[SLICE];
     float sampVar =(RelSamp.getData_entry(k).getErrors()[SLICE]);
     sampVar = sampVar*sampVar;
     if( SLICE< 15) if( k< 12)
         System.out.println("adding to Q's "+N+","+wt+","+yval+","+SCALE);
     float backVar =(RelBackground.getData_entry(k).getErrors()[SLICE]);
     backVar=backVar*backVar;
	   WEIGHT1[N] = WEIGHT1[N] + wt;

	   SOFQS[N] = SOFQS[N] + SCALE*wt*yval;
	   SOFQB[N] = SOFQB[N] + SCALE*bval*wt;
	   SOQSMB[N] = SOFQS[N]-SOFQB[N];
           int K=k;
	   SMPERR[N] = SMPERR[N] + ((Math.pow(SCALE*wt,2.0)))*sampVar;
	   BKGERR[N] = BKGERR[N] + (Math.pow(SCALE*wt,2.0))*backVar;
           SMBERR[N] = SMPERR[N] + BKGERR[N];
        }
        else {
      /*    int N;
	   for(int ii=1;ii<=qbins;ii++) {

	      if((Q>QMINQ[ii])&& (Q<QMAXQ[ii])) {

	         N = (int)NSHIFT[ii]+(int)(Q-QMINQ[ii])/(int)DELQ[ii] + 1;

	   WEIGHT1[N] = WEIGHT1[N] + WEIGHT[k];

	   SOFQS[N] = SOFQS[N] + SCALE*SAMPINT[k]*WEIGHT[k];
	   SOFQB[N] = SOFQB[N] + SCALE*BACKINT[k]*WEIGHT[k];
	   SOQSMB[N] = SOFQS[N]-SOFQB[N];

	   SMPERR[N] = SMPERR[N] + (Math.pow(SCALE*WEIGHT[k],2))*SAMPVAR[k];
	   BKGERR[N] = BKGERR[N] + (Math.pow(SCALE*WEIGHT[k],2))*BACKVAR[k];
           SMBERR[N] = SMPERR[N] + BKGERR[N];

	}

        }
*/
       //end do

      }//endif
  }// END IF


	     

   

/*c//////////////////
c
c       Deal with the anisotropic case
c
c//////////////////*/

/*
      if(IF2D==1) {

	      Nx = (int)((Qx-Qxmin)/xDELTAQ) + 1;
	      Ny = (int)((Qy-Qymin)/yDELTAQ) + 1;

	      WTQXQY[Nx][Ny] = WTQXQY[Nx][Ny] + WEIGHT[k];

	      SQXQY[Nx][Ny]=SQXQY[Nx][Ny]+SCALE*SAMPINT[k]*WEIGHT[k];
	      BQXQY[Nx][Ny]=BQXQY[Nx][Ny]+SCALE*BACKINT[k]*WEIGHT[k];
	      SBQXQY[Nx][Ny] = SQXQY[Nx][Ny]-BQXQY[Nx][Ny];

	      SERRXY[Nx][Ny]=SERRXY[Nx][Ny]+Math.pow(SCALE*WEIGHT[k],2)*SAMPVAR[k];
	      BERRXY[Nx][Ny]=BERRXY[Nx][Ny]+Math.pow(SCALE*WEIGHT[k],2)*BACKVAR[k];
              SBERRXY[Nx][Ny]=BERRXY[Nx][Ny]+SERRXY[Nx][Ny];

              }

///390	      CONTINUE

              }
              //enddo
*/
}//for SLICE and for k < getNum_entries


 }
 /*(  StringChoiceList scl = new StringChoiceList();
   StringChoiceList scl1 = new StringChoiceList();
   scl1.setString( SelectGroups.INSIDE);
   scl1.setString(SelectGroups.SET);
  (new SelectGroups( RelSamp, new AttributeNameString("Group ID"),-10.0f, 60000.0f,scl , scl1)).getResult();

        (new DataSetTools.operator.DataSet.Math.DataSet.SumCurrentlySelected( RelSamp,true,true)).getResult();
*/   
//662	FORMAT(I5,4F10.3)

/*C*************************
C
C	END OF PROCESSING FOR THIS SLICE
C
C**************************/

//	  }
          // endif

//	} 
            //enddo

/*C********************
C
C	END OF MAIN LOOP
C
C****************************************/


/*C*********************************
C
C	EVALUATE MEANS FOR DATA ISOTROPICALLY
C
C*******************/

	if(IF2D==0) {

 	   //TYPE 664, CNTTOT,CNTNET
	   //WRITE(OUTUNIT,664) CNTTOT,CNTNET
           try{
           System.out.println("TOTAL COUNT ="+String.valueOf(CNTTOT)+ ",NET COUNT = "+String.valueOf(CNTNET)) ;
        str1=String.valueOf("TOTAL COUNT ="+String.valueOf(CNTTOT)+ ",NET COUNT = "+String.valueOf(CNTNET)) ;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

         //  F1.write("TOTAL COUNT ="+String.valueOf(CNTTOT)+ ",NET COUNT = "+String.valueOf(CNTNET)) ;
           }catch(IOException e){
           System.out.println("Error writing") ;
           }
	   for(int N=1;N<=NDIV;N++) {

	      if(WEIGHT1[N]<=0){

	         SOFQS[N] = 0.0;
	         SOFQB[N] = 0.0;
	         SOQSMB[N] = 0.0;
	         SMPERR[N] = 0.0;
	         BKGERR[N] = 0.0;
                }
                else {

	         SOFQS[N] = SOFQS[N]/WEIGHT1[N];
	         SOFQB[N] = SOFQB[N]/WEIGHT1[N];
	         SOQSMB[N] = SOQSMB[N]/WEIGHT1[N];

	         SMBERR[N] = (Math.sqrt(SMBERR[N]))/WEIGHT1[N];
	         SMPERR[N] = (Math.sqrt(SMPERR[N]))/WEIGHT1[N];
	         BKGERR[N] = (Math.sqrt(BKGERR[N]))/WEIGHT1[N];

	      }

	   }

    System.out.println("SOFQS     SOFQB     SMBERR");
    for( int N=0; N< qu.length;N++)
     System.out.println(SOFQS[N]+","+ SOFQB[N]+","+SMBERR[N]);

	   INET = 0;

	   for(int ij=1; ij<=NDIV;ij++) {

	      int JNET = INET + ij;
	      QVECTOR[JNET] = QUE4[JNET];

	      siofq[JNET]    = SOFQS[ij];
	      serror[JNET]   = SMPERR[ij];

	      biofq[JNET]    = SOFQB[ij];
	      berror[JNET]   = BKGERR[ij];

	      IOFQ[JNET]    = SOQSMB[ij];
	      ERROR[JNET]   = SMBERR[ij];

	   }

	   INET = NDIV;
	   IOFQMIN = IOFQ[1];
	   IOFQMAX = IOFQ[1];

	   for(int i=1; i<=NDIV; i++) {

	      if(IOFQ[i]<IOFQMIN) IOFQMIN = IOFQ[i];

	      if(IOFQ[i]>IOFQMAX) IOFQMAX = IOFQ[i];

	   }

	   if(IOFQMIN<=0)
            IOFQMIN = 0.001*IOFQMAX;

/*C*********************
C
C	WRITE RESULTS TO FILES
C
C**********************/

/*           System.out.println("SAVE I(Q) VS Q FOR ANALYSIS? (Y/N) :") ;
                   try
                    {
                    ANS=String.valueOf(System.in.read()) ;
                    }catch (IOException e)
                    {
                    System.out.println("Error Reading") ;
                    //System.exit(12);
                    }
*/
     ANS = "N";

	   if((ANS=="Y")||(ANS=="y")){

/*C//////////////////
C
C	SET UP FILE NAMES
C
C//////////////////*/
             try{
        str1=String.valueOf(NAMLIS) ;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

//              F1.write(NAMLIS) ;
        str1=" ";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(NAMLIS2) ;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        //      F1.write(NAMLIS2) ;
        str1=" ";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

        str1=String.valueOf(NAMLIS3) ;
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

       //       F1.write(NAMLIS3) ;
        str1=" ";
       for (int ii1=0;ii1<str1.length();ii1++)
        F1.write(str1.charAt(ii1)) ;

            }catch(IOException e)
            {
               System.out.println("Error Writing") ;
            }

         ANSSD ="";
	  }

/*C**********************
C
C	THIS FILE IS FOR WRITING THE DATA IN A THREE COLUMN FILE
C	FOR THIS SECTION TO WORK WELL YOU MUST CHOOSE Q-RANGE IN THE
C	COARSER BINNING TO START AT THE QMAX OF FINER BINNING
C
C***********************/

            System.out.println("Want net scattering data in a 3 COLUMN file (Y/N)?") ;
                   try
                    {
                    ANSCOL=String.valueOf(System.in.read()) ;
                    }catch (IOException e)
                    {
                    System.out.println("Q:Error Reading") ;
                    System.exit(12);
                    }
     ANSCOL="";
     System.out.println("R");
	   if((ANSCOL=="Y")||(ANSCOL=="y")){
              try{
                str1=" ";
                for (int ii1=0;ii1<str1.length();ii1++)
                F1.write(str1.charAt(ii1)) ;
              FileOutputStream F4= new FileOutputStream(NAMCOL);
                 str1=" ";
                for (int ii1=0;ii1<str1.length();ii1++)
                F4.write(str1.charAt(ii1)) ;



	      for(int ij=1;ij<=NDIV;ij++) {
              str1=String.valueOf(QVECTOR[ij])+" "+String.valueOf(IOFQ[ij])+" "+String.valueOf(ERROR[ij]) ;
              for (int ii1=0;str1.length()>ii1;ii1++)
                      F4.write(str1.charAt(ii1)) ;

	      }
              str1=" ";
              for (int ii1=0;str1.length()>ii1;ii1++)
                      F4.write(str1.charAt(ii1)) ;

              str1="INAME"+" ARGONNE NATIONAL LABORATORY";
              for (int ii1=0;str1.length()>ii1;ii1++)
                      F4.write(str1.charAt(ii1)) ;

              str1=String.valueOf(users);
              for (int ii1=0;str1.length()>ii1;ii1++)
                      F4.write(str1.charAt(ii1)) ;
              str1=String.valueOf(tttt[1]);
              for (int ii1=0;str1.length()>ii1;ii1++)
                      F4.write(str1.charAt(ii1)) ;


              str1=String.valueOf(RUNS)+" "+String.valueOf(RUNB)+" "+String.valueOf(RUNC);
              for (int ii1=0;str1.length()>ii1;ii1++)
                      F4.write(str1.charAt(ii1)) ;

              str1=String.valueOf(stm[1])+" "+String.valueOf(sdt[1])+" "+String.valueOf(etm[1])+" "+String.valueOf(edt[1]);
              for (int ii1=0;str1.length()>ii1;ii1++)
                      F4.write(str1.charAt(ii1)) ;

              str1=String.valueOf(smonsum)+" "+String.valueOf(smonpot)+" "+String.valueOf(CNTTOT);
              for (int ii1=0;str1.length()>ii1;ii1++)
                      F4.write(str1.charAt(ii1)) ;

              str1=String.valueOf(pulses);
              for (int ii1=0;str1.length()>ii1;ii1++)
                      F4.write(str1.charAt(ii1)) ;


	      if(qbins!=-99) {
              str1=" ";
              for (int ii1=0;str1.length()>ii1;ii1++)
                      F4.write(str1.charAt(ii1)) ;



	         for(int ij=1;ij<=qbins;ij++) {

              str1=String.valueOf(ij)+" "+String.valueOf(QMINQ[ij])+" "+String.valueOf(QMAXQ[ij])+" "+String.valueOf(DELQ[ij]);
              for (int ii1=0;str1.length()>ii1;ii1++)
                      F4.write(str1.charAt(ii1)) ;

	         }

	      }
              F4.close() ;
            }catch(IOException e){
            System.out.println("S:Error Writing") ;
            }
	      //TYPE 696, NAMCOL(1:11)
	      //WRITE(OUTUNIT,696) NAMCOL(1:11)
              try{
              System.out.println("NET SCATTERING WRITTEN TO A 3-COLUMN FILE : "+String.valueOf(NAMCOL)) ;
                str1="NET SCATTERING WRITTEN TO A 3-COLUMN FILE : "+String.valueOf(NAMCOL) ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F1.write(str1.charAt(ii1)) ;

              }catch(IOException e)
              {
              System.out.println("T:Error Writing") ;
              }

	   }

           System.out.println("Want to look at another time-SLICE range? (Y/N)? ") ;
                   try
                    {
                    ANSAGN=String.valueOf(System.in.read()) ;
                    }catch (IOException e)
                    {
                    System.out.println("U:Error Reading") ;
                    System.exit(12);
                    }

	}

/*

 173	format(/,' Save the file or files in binary or text? ',//,
     1	'      ANSWER (BIN/TXT) : ',$)
 664	FORMAT(/,' TOTAL COUNT = ',F12.1,5X,' NET COUNT = ',F12.1)
 666	FORMAT(/,' SAVE I(Qx,Qy) VS Qx,y FOR ANALYSIS? (Y/N) : ',$)
 671	FORMAT(/,' SAVE I(Q) VS Q FOR ANALYSIS? (Y/N) : ',$)
 672	FORMAT(/,' Do you wish to save ; ',/,
     1	' (SN). net S(q) only,  ',/,
     2	' (ALL). gross S(q), gross B(q), and net S(q) : ',//,
     3	'                 ANSWER (SN/SO/BO/SB/ALL) : ',$)
 673	FORMAT(/,' Do you wish to save (2D data); '  ,/,
     1	' (SN). net S(Qx,Qy) only,  ',/,
     2	' (ALL). gross S, gross B, and net S : ',//,
     3	'                 ANSWER (SN/SO/BO/SB/ALL) : ',$)
 674	FORMAT(F10.3)
 676	FORMAT(I6)
 678	FORMAT(E14.6)
 680	FORMAT (8E14.6)
 682	FORMAT(A)
 686	FORMAT(/,' SAMPLE SCATTERING "ONLY" WRITTEN TO FILE: ',A)
 688	FORMAT(/,' BACKGROUND SCATTERING "ONLY" WRITTEN TO FILE: ',A)
 690	FORMAT(/,' NET DIFFERENCE SCATTERING WRITTEN TO FILE: ',A)
 696	FORMAT(/,' NET SCATTERING WRITTEN TO A 3-COLUMN FILE: ',A)
 692	FORMAT(/,' Want net scattering data in a 3 COLUMN file (Y/N)? ',$)
 693	FORMAT(' ''B''  0.0  0.0')
 694	FORMAT(F10.5,5X,E13.5,5X,E13.5)
 695	FORMAT(' -999.0	-999.0	-999.0 ')
 6910	format(/,1x,a4,'   ARGONNE NATIONAL LABORATORY')
 6911	format(1x,a20)
 6912	format(1x,'Sample Run# ',i5,3x,'Background Run# ',i5,3x,
     1         'Cd Run# ',i5)
 6913	format(1x,'Started ',a8,2x,a9,3x,'Ended ',a8,2x,a9)
 6914	format(1x,'Beam Monitor : ',i10,'  POT : ',i10,
     1         '  Area Detector : ',f12.1)
 6915	format(1x,'Total Pulses for this sample : ',i10,/)
 6916	format(1x,a80)
 6917	format(1x,'Section#',4x,'QMIN',6x,'Qmax',6x,'DELQ')
 6918	format(4x,i1,7x,f6.4,4x,f6.4,4x,f6.4)
 699	FORMAT(Q,A)
 700	FORMAT(/,' Want to look at another time-SLICE range? (Y/N)? ',$)

*/
/*C*********************
C
C	Evalute means and WRITE RESULTS TO FILE for anisotroipic case
C
C**********************/
  IF2D =0;  
	if(IF2D==1) {

	   for(int j=1;j<=NDIVy;j++) {

	      Qyy[j]=Qymin +yDELTAQ/2+j-1*yDELTAQ;

	   }

	   for(int i=1;i<=NDIVx;i++) {

	      Qxx[i]=Qxmin +xDELTAQ/2+i-1*xDELTAQ;

	      for(int j=1;j<=NDIVy;j++) {

	         if(WTQXQY[i][j]<=0) {

                      SQXQY[i][j] = 0.0;
                      BQXQY[i][j] = 0.0;
                      SBQXQY[i][j] = 0.0;
                      SERRXY[i][j] = 0.0;
                      BERRXY[i][j] = 0.0;
                      SBERRXY[i][j] = 0.0;
                  }else {

	      SQXQY[i][j] = SQXQY[i][j]/WTQXQY[i][j];
	      BQXQY[i][j] = BQXQY[i][j]/WTQXQY[i][j];
	      SBQXQY[i][j] = SBQXQY[i][j]/WTQXQY[i][j];

	      SBERRXY[i][j] = (Math.sqrt(SBERRXY[i][j]))/WTQXQY[i][j];
	      SERRXY[i][j] = (Math.sqrt(SERRXY[i][j]))/WTQXQY[i][j];
	      BERRXY[i][j] = (Math.sqrt(BERRXY[i][j]))/WTQXQY[i][j];

              }

          }

	  }
}

	   //TYPE 666
	   //READ(5,614) ANS
           System.out.println("SAVE I(Qx,Qy) VS Qx,y FOR ANALYSIS? (Y/N) :") ;
                   try
                    {
                    ANS=String.valueOf(System.in.read()) ;
                    }catch (IOException e)
                    {
                    System.out.println("V:Error Reading") ;
                    System.exit(12);
                    }

     ANS="N";
	   if((ANS=="Y")||(ANS=="y")) {

/*C//////////////////
C
C	SET UP FILE NAMES
C
C//////////////////*/
                try{

                str1="NET SCATTERING WRITTEN TO A 3-COLUMN FILE : "+String.valueOf(NAMCOL) ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F1.write(str1.charAt(ii1)) ;

                str1=String.valueOf(namsq1);
                for (int ii1=0;ii1<str1.length();ii1++)
                F1.write(str1.charAt(ii1)) ;

                str1=String.valueOf(namsq2);
                for (int ii1=0;ii1<str1.length();ii1++)
                F1.write(str1.charAt(ii1)) ;

          }catch(IOException e){
          System.out.println("Error writing") ;
          }


/*C////////////////////
C
C	WRITE FILE OR FILES, DEPENDING ON OPTION
C
C////////////////////*/
             System.out.println("Save the file or files in binary or text?");
                  try
                    {
                    ansbt=String.valueOf(System.in.read()) ;
                    }catch (IOException e)
                    {
                    System.out.println("Error Reading") ;
                    System.exit(12);
                    }
              System.out.println("Do you wish to save (2D data)");
              System.out.println("(SN). net S(Qx,Qy) only, ");
              System.out.println("(ALL). gross S, gross B, and net S : ");
     	      System.out.println(" ANSWER (SN/SO/BO/SB/ALL) : ") ;
            try
                    {
                    ANSSD=String.valueOf(System.in.read()) ;
                    }catch (IOException e)
                    {
                    System.out.println("Error Reading") ;
                    System.exit(12);
                    }

	      if((ansbt=="TXT")||(ansbt=="txt")) {

	         if((ANSSD=="ALL")||(ANSSD=="all") ||
                (ANSSD=="SO")||(ANSSD=="so")||
     	         (ANSSD=="SB")||(ANSSD=="sb")) {
                 try{
              F5= new FileOutputStream(namsq);



	      for(int i=1;i<=NDIVx;i++) {

	        for(int j=1;j<=NDIVy;j++) {
                str1=String.valueOf(namsq2);
                for (int ii1=0;ii1<str1.length();ii1++)
                F5.write(str1.charAt(ii1)) ;


                /*str1=String.valueOf(namsq2);
                for (int ii1=0;ii1<str1.length();ii1++)
                F1.write(str1.charAt(ii1)) ;
                F5.write() ;*/

	   }

	      }
              F5.close() ;
              }catch(IOException e){
              System.out.println("Error write") ;
              }
              System.out.println() ;
	      //CLOSE(UNIT=4,STATUS='KEEP')
              System.out.println("SAMPLE SCATTERING WRITTEN TO FILE: "+String.valueOf(namsq) ) ;
              try{
                str1="SAMPLE SCATTERING WRITTEN TO FILE: "+String.valueOf(namsq) ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F1.write(str1.charAt(ii1)) ;

              }catch(IOException e){
              System.out.println("Error Writing") ;
              }

	       }

	         if((ANSSD=="ALL")||(ANSSD=="all")||
     	         (ANSSD=="BO")||(ANSSD=="bo")||
     	         (ANSSD=="SB")||(ANSSD=="sb")) {
                 try{
                  F6= new FileOutputStream(namsq1);


	      for(int i=1;i<=NDIVx;i++) {

	   for(int j=1;j<=NDIVy;j++) {
/*                str1=String.valueOf(namsq2);
                for (int ii1=0;ii1<str1.length();ii1++)
                F1.write(str1.charAt(ii1)) ;*/
	//write(4,394) Qxx(i),Qyy(j),BQXQY(i,j),BERRXY(i,j)

	   }

	     }
              F6.close() ;
              }catch(IOException e)
              {
              System.out.println("Error Writing") ;
              }
              System.out.println("BACKGROUND SCATTERING ONLY WRITTEN TO FILE :"+String.valueOf(namsq1) ) ;
	      //WRITE(OUTUNIT,688) namsq1(1:12)
              try{
                str1="BACKGROUND SCATTERING ONLY WRITTEN TO FILE :"+String.valueOf(namsq1) ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F1.write(str1.charAt(ii1)) ;

              }catch(IOException e){
              System.out.println("Error Writing") ;
              }
                }

	         if((ANSSD=="ALL")||(ANSSD=="all")||
     	         (ANSSD=="SN")||(ANSSD=="sn")) {
                  try{
                 F7= new FileOutputStream(namsq2);



	      for(int i=1;i<=NDIVx;i++) {

	   for(int j=1;j<=NDIVy;j++) {
                str1=String.valueOf(Qxx[i])+" "+String.valueOf(Qyy[j]+" "+String.valueOf(SBQXQY[i][j])+" "+String.valueOf(SBERRXY[i][j]));
                for (int ii1=0;ii1<str1.length();ii1++)
                F7.write(str1.charAt(ii1)) ;


	   }

	     }
              F7.close() ;
              System.out.println("NET DIFFERENCE SCATTERING WRITTEN TO FILE:"+String.valueOf(namsq2) ) ;
                str1="NET DIFFERENCE SCATTERING WRITTEN TO FILE:"+String.valueOf(namsq2)  ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F1.write(str1.charAt(ii1)) ;

              }catch(IOException e){
              System.out.println("error Writing") ;
	         }
                 }

	      else if((ansbt=="BIN")||(ansbt=="bin")){

	         if((ANSSD=="ALL")||(ANSSD=="all")||
     	         (ANSSD=="SO")||(ANSSD=="so")||
     	         (ANSSD=="SB")||(ANSSD=="sb")) {
              try{
              F8= new FileOutputStream(namsq);
      	      for(int i=1;i<=NDIVx;i++){
                str1=String.valueOf(tttt[i]);
                for (int ii1=0;ii1<str1.length();ii1++)
                F8.write(str1.charAt(ii1)) ;

                 str1=String.valueOf(NDIVx)+" "+String.valueOf(NDIVy)+" 1 "+String.valueOf(Qxmin)+" "+String.valueOf(Qxmax)+" "+String.valueOf(xDELTAQ)+" "+String.valueOf(Qymin)+" "+String.valueOf(Qymax)+" "+String.valueOf(yDELTAQ)  ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F8.write(str1.charAt(ii1)) ;


	      for(int j=NDIVy;j>=1;j--) {
                 str1=String.valueOf(SQXQY[i][j]) ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F8.write(str1.charAt(ii1)) ;


	   //write(4) (SQXQY(i,j),i=1,NDIVx)

	      }

	      for(int j=NDIVy;j>=1;j--) {
	   //write(4) (SERRXY(i,j),i=1,NDIVx)
                 str1=String.valueOf(SQXQY[i][j]) ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F8.write(str1.charAt(ii1)) ;
              }
	     }
                F8.close() ;
                }catch(IOException e){
                System.out.println("Error Writing") ;
                }
              System.out.println("NET DIFFERENCE SCATTERING WRITTEN TO FILE:"+String.valueOf(namsq2)) ;
              try{
                str1="NET DIFFERENCE SCATTERING WRITTEN TO FILE:"+String.valueOf(namsq2)  ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F1.write(str1.charAt(ii1)) ;

              }catch(IOException e){
              System.out.println("Error Writing") ;
              }
	         }

	         if((ANSSD=="ALL")||(ANSSD=="all")||
     	         (ANSSD=="BO")||(ANSSD=="bo")||
     	         (ANSSD=="SB")||(ANSSD=="sb")) {
                 try{
                 F9= new FileOutputStream(namsq1);
      	      for(int i=1;i<=NDIVx;i++){
                str1=String.valueOf(tttt[i]) ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F9.write(str1.charAt(ii1)) ;

                str1=String.valueOf(NDIVx)+" "+String.valueOf(NDIVy)+" 1 "+String.valueOf(Qxmin)+" "+String.valueOf(Qxmax)+" "+String.valueOf(xDELTAQ)+" "+String.valueOf(Qymin)+" "+String.valueOf(Qymax)+" "+String.valueOf(yDELTAQ)  ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F9.write(str1.charAt(ii1)) ;

	      for(int j=NDIVy;j>=1;j--) {
                str1=String.valueOf(SQXQY[i][j])  ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F9.write(str1.charAt(ii1)) ;


	   //write(4) (BQXQY(i,j),i=1,NDIVx)

	      }

	      for(int j=NDIVy;j>=1;j--) {
                str1=String.valueOf(BERRXY[i][j])  ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F9.write(str1.charAt(ii1)) ;


	   //write(4) (BERRXY(i,j),i=1,NDIVx)
              }
	      }
            F9.close() ;
              }catch(IOException e){
              System.out.println("Error Writing") ;
              }

              System.out.println("NET DIFFERENCE SCATTERING WRITTEN TO FILE:"+String.valueOf(namsq2)) ;
              try{
                str1="NET DIFFERENCE SCATTERING WRITTEN TO FILE:"+String.valueOf(namsq2) ;
                for (int ii1=0;ii1<str1.length();ii1++)
                F1.write(str1.charAt(ii1)) ;

              }catch(IOException e){
              System.out.println("Error Writing") ;
              }

	        }

	         if((ANSSD=="ALL")||(ANSSD=="all")||
    	         (ANSSD=="SN")||(ANSSD=="sn")) {
           try{
              F10= new FileOutputStream(namsq2);
      	  for(int i=1;i<=NDIVx;i++)
          {
                str1=String.valueOf(tttt[i]);
                for (int ii1=0;ii1<str1.length();ii1++)
                F10.write(str1.charAt(ii1)) ;

                str1=String.valueOf(NDIVx)+" "+String.valueOf(NDIVy)+" 1 "+String.valueOf(Qxmin)+" "+String.valueOf(Qxmax)+" "+String.valueOf(xDELTAQ)+" "+String.valueOf(Qymin)+" "+String.valueOf(Qymax)+" "+String.valueOf(yDELTAQ);
                for (int ii1=0;ii1<str1.length();ii1++)
                F10.write(str1.charAt(ii1)) ;


	      				for(int j=NDIVy;j>=1;j--) 
                {
                	str1=String.valueOf(SQXQY[i][j]);
                	for (int ii1=0;ii1<str1.length();ii1++)
                		F10.write(str1.charAt(ii1)) ;
                    //write(4) (SBQXQY(i,j),i=1,NDIVx)
								}

	      				for(int j=NDIVy;j>=1;j--) 
								{
									str1=String.valueOf(SBERRXY[i][j]);
                	for (int ii1=0;ii1<str1.length();ii1++)
                	F10.write(str1.charAt(ii1)) ;

                }
	   						//write(4) (SBERRXY(i,j),i=1,NDIVx)

	     			}

            F10.close() ;
              }catch(IOException e){
              System.out.println("Error Writing") ;
              }

              System.out.println("NET DIFFERENCE SCATTERING WRITTEN TO FILE:"+String.valueOf(namsq2)) ;
              try{
              str1="NET DIFFERENCE SCATTERING WRITTEN TO FILE:"+String.valueOf(namsq2) ;
              for (int ii1=0;ii1<str1.length();ii1++)
                      F1.write(str1.charAt(ii1)) ;
              }catch(IOException e){
              System.out.println("Error Writing") ;
              }

	       }//if( ANS="ALL"

	     }//if((ansbt=="TXT")||(ansbt=="txt")) {

	}// if((ANS=="Y")||(ANS=="y")) {

}


}


     return RelSamp;
    

  }//end of getResult


private DataSet convertToLambda( DataSet ds){
 DataSetOperator opBackground;
  Object Result;
  
 
    opBackground =   ds.getOperator( "Convert to Wavelength");
   if(opBackground == null)
     opBackground = ds.getOperator( "Monitor to Wavelength");
   if( opBackground!= null){
        opBackground.setDefaultParameters();
        opBackground.getParameter(0).setValue(new Float( -1.0f));
        opBackground.getParameter(1).setValue(new Float( -1.0f));
        opBackground.getParameter(2).setValue( new Integer(0));
        Result = opBackground.getResult();
        if( (Result  instanceof ErrorString)  ){
           error = ((ErrorString)Result).toString();
           System.out.println("C:"+error);
           return  null;
        }
        if( Result  == null){
          error=( "Could not Convert Sample to Llamda");
           System.out.println("D:"+error);
          return null;
        }
        DataSet ds1 = (DataSet) Result;
        ds1.setTitle(ds.getTitle()+"-lambda and scaled");
        ds1.setX_units("Angstroms");
         return ds1;
      }
   return null;

}


private void Resample( DataSet DS, XScale xscl){
  //System.out.println("in void Resample, xscl="+StringUtil.toString( xscl.getXs()));
  for(int i=0; i< DS.getNum_entries(); i++)
    DS.getData_entry(i).resample( xscl,IData.SMOOTH_NONE);

}


private  DataSet makeNewSensEffDS( DataSet Sens, DataSet Eff, XScale xscl, DataSet RelSamp){
  DataSet Res = new DataSet("Sens_Eff_Prod",new OperationLog(),RelSamp.getX_units(),
            RelSamp.getX_label(), RelSamp.getY_units(), RelSamp.getY_label());

  DataSetFactory.addOperators( Res);
  float[] Eff_yvals = Eff.getData_entry(0).getY_values();
  float[] Eff_errors =Eff.getData_entry(0).getErrors();
  System.out.println("Sens="+Sens);
  PixelInfoList pxinflist = (PixelInfoList)(Sens.getData_entry(0).getAttribute( Attribute.PIXEL_INFO_LIST).getValue());
  IPixelInfo ipinf =pxinflist.pixel(0);
  IDataGrid Sensgrid = ipinf.DataGrid();
  IPixelInfo ipinf1 =((PixelInfoList)RelSamp.getData_entry(0).getAttributeValue( Attribute.PIXEL_INFO_LIST)).pixel(0);
  IDataGrid Sampgrid = ipinf.DataGrid();

  UniformGrid SensEffgrid = new UniformGrid(77,Sampgrid.units(),Sampgrid.position(),
                    Sampgrid.x_vec(), Sampgrid.y_vec(),Sampgrid.width(),Sampgrid.height(),
                    Sampgrid.depth(), Sensgrid.num_rows(), Sensgrid.num_cols());
  for( int i=0; i< Sens.getNum_entries(); i++){
    
     float[] yvals = new float[Eff_yvals.length];
     float[] errors = new float[Eff_errors.length];
     System.arraycopy( Eff_yvals,0,yvals,0,Eff_yvals.length);
     
     System.arraycopy( Eff_errors,0,errors,0,Eff_errors.length);
     float this_sens= Sens.getData_entry(i).getY_values()[0];
     for( int j=0; j<Eff_yvals.length; j++){
          yvals[j]=yvals[j]*this_sens;
          errors[j]=errors[j]*this_sens;
     }
    /* Object O =(new DataSetTools.operator.DataSet.Attribute.GetPixelInfo_op( Sens,i)).getResult();
     if( O instanceof ErrorString)
       return null;
     int col = ((Integer)((Vector)O).firstElement()).intValue();
    
     int row = ((Integer)((Vector)O).lastElement()).intValue();
     */
     PixelInfoList pilist9 =(PixelInfoList)(Sens.getData_entry(i).getAttributeValue( 
                            Attribute.PIXEL_INFO_LIST));
     IPixelInfo pinf9 = pilist9.pixel(0);
     int row =(int) pinf9.row();
     int col =(int) pinf9.col();
     int GroupID =Sampgrid.getData_entry(row,col).getGroup_ID();
     HistogramTable D = new HistogramTable(xscl,yvals,errors, GroupID);
    DetectorPixelInfo dpi = new DetectorPixelInfo(GroupID, (short)row, (short)col, SensEffgrid);
    DetectorPixelInfo[] pilist = new DetectorPixelInfo[1];
    pilist[0]= dpi;
    D.setAttribute( new PixelInfoListAttribute(Attribute.PIXEL_INFO_LIST, 
             new PixelInfoList(pilist)));
    Data Dsamp = RelSamp.getData_entry_with_id( GroupID);
    D.setAttribute( Dsamp.getAttribute( Attribute.DETECTOR_POS));
    Res.addData_entry( D);
  
  }
  return Res;

}//makeNewSensEffDS
 private void EliminateBadDetectors( DataSet ds, DataSet Sens){
  Object Res =(new ClearSelect( Sens)).getResult() ;
  if( Res instanceof ErrorString)
     return;
  StringChoiceList sl1 = new StringChoiceList();
   sl1.setString("Between Max and Min");

   StringChoiceList sl2= new StringChoiceList();
   sl2.setString("Set Selected");
  DataSet Sens1 = (DataSet)(Sens.clone());
  Res = (new SelectGroups( Sens1, new AttributeNameString("TOTAL COUNT"), 0f,.0001f,
            sl1,sl2)).getResult();

  if( Res instanceof ErrorString)
     return;
 
  int[] selInd = Sens1.getSelectedIndices();
  for( int i = 0; i< selInd.length; i++)
     ds.setSelectFlag(selInd[i], true);
 (new ExtractCurrentlySelected( ds, false, false)).getResult();
  
 }//EliminateBadDetectors



private DataSet SumAllDetectors( DataSet ds){
  
  Object Res = ( new SumByAttribute( ds,"Group ID",true, 0f,20f+ ds.getNum_entries() )).getResult();
   if( Res instanceof ErrorString)
     return null;
  ds.clearSelections();
  System.out.println( "End sunAll Det Res class="+Res.getClass()+","+Res.toString());
  return (DataSet)Res;

}

public void AdjustGrid( DataSet ds, float xoff, float yoff){

    int ids[] = Grid_util.getAreaGridIDs( ds );
    if ( ids.length != 1 )
      System.out.println("ERROR: wrong number of data grids " + ids.length );
    IDataGrid grid = Grid_util.getAreaGrid( ds, ids[0] );
    Vector3D pos = grid.position();
    pos.add( new Vector3D(0,-xoff,yoff));

    ((UniformGrid)grid).setCenter(pos);
    Grid_util.setEffectivePositions( ds, grid.ID() );

}

  public static void main(String[] args) {

         IsawGUI.Util util = new IsawGUI.Util();

         DataSet[] RUNSds=null, RUNBds=null, RUNCds=null;
         DataSet[] TransS=null, TransB=null, Eff=null, Sens=null;
         float[] qu = new float[117];
         String Path, Instrument;
         float BETADN, SCALE;
         BETADN = 0.0011f;
         SCALE = 843000f;
//
         qu[0] = 0.00359f;
				 for(int i = 1; i<117; i++)
         {
            qu[i] = qu[i-1]*1.05f ;
   //System.out.println("qu ....." +qu[i]);
         }
 



         RUNSds = util.loadRunfile(  "C:\\Argonne\\sand\\wrchen03\\sand19990.run"  );
         RUNBds = util.loadRunfile(  "C:\\Argonne\\sand\\wrchen03\\sand19935.run" );
         RUNCds = util.loadRunfile(  "C:\\Argonne\\sand\\wrchen03\\sand19936.run" );
       try{
         TransS = ScriptUtil.load( "C:\\ISAW\\DataSetTools\\operator\\Generic\\TOF_SAD\\tr1999019934.isd");
         TransB = ScriptUtil.load( "C:\\ISAW\\DataSetTools\\operator\\Generic\\TOF_SAD\\tr1993519934.isd");
         Eff    = ScriptUtil.load( "C:\\ISAW\\DataSetTools\\operator\\Generic\\TOF_SAD\\efr19452.isd");
         Sens   = ScriptUtil.load( "C:\\ISAW\\SampleRuns\\sens19878.isd" );
       }catch(Exception sss){
        System.out.println("Error:"+sss);
       }
    System.out.println("Trans="+TransS+"::"+TransB+"::"+Eff+"::"+Sens);
    Reduce_KCL Reduce_KCL = new Reduce_KCL( TransS[0], TransB[0], 
             Eff[0], Sens[0], qu, RUNSds, RUNBds, RUNCds,BETADN,SCALE, .1f,
              .000725f,.006909f);
    Object O= Reduce_KCL.getResult();
    System.out.println("Finished O="+O);
    Vector V =(Vector)O;
    ScriptUtil.display ( V.elementAt(0));
    ScriptUtil.display ( V.elementAt(1));
    ScriptUtil.display ( V.elementAt(2));

   }
}

