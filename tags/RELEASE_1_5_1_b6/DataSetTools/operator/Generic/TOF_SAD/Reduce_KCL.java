
/*
 * File:  Reduce_KCL.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson and Alok Chatterjee
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.12  2003/09/18 15:55:35  rmikk
 * -Added a parameter useTransB to the END of the argument list
 *
 * Revision 1.11  2003/09/11 19:52:18  rmikk
 * Added GetField to the new DataSets
 *
 * Revision 1.10  2003/09/11 14:59:45  rmikk
 * -Included the GPL
 * -eliminated the DataSet.Add, Subtract, etc. methods and did
 *   several steps at one time.
 * -Reduced memory requirements by eliminating the declaration
 *  of variables to MaxSlice, etc.  and introducing a memory
 * efficient convert and rebin to one final xscale.
 *
 */
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
import java.io.*;

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

public class Reduce_KCL  extends GenericTOF_SAD{

    public static int Nedge = 1;      //mask off edge detectors or those
    public static float Radmin = 1.5f / 100;//too close or too far from the origin
    public static float Radmax = 100.0f / 100;
    DataSet TransS, TransB, Eff, Sens;
    float[] qu;
    DataSet[] RUNBds = new DataSet[2];
    DataSet[]RUNCds = new DataSet[2];;
    DataSet[]RUNSds = new DataSet[2];
    boolean useTransB;
    public String INSTR;
    public String INAME;
    String str1;
    int MaxSlice;
    /**
     *Parameters
     */
    int[] MonitorInd, MonitorID;
    XScale xscl;
    int MAXDET = 100;
    int MAXCST = 512;
    int MAXMON = 1100;
    int MAXX = 128;
    int MAXY = 128;
    int MAXQBIN = 512;
    int MAXSLC = MAXX * MAXY;
    int MAXCRV = 3;
    int MAXPTS = 512;
    int maxqxbin = 250;
    int maxqybin = 250;

    /**
     *   Variables
     */
    String ANS, ANSDN, ANSCH, ANSNL, ANSSD, ANSCOL, ANISOT, ANSAGN, ANSPLT, ANSCL, ANSD;
    String ansbt;
    String  REDUCEOUT;
    char[]  tttt1 = new char[80];
    char[]  users1 = new char[20];
    byte[] tttt = new byte[80];
    byte[] users = new byte[20];
    byte[] sdt = new byte[9];
    byte[] edt = new byte[9];
    char[] stm1, etm1 = new char[9];
    byte[] stm, etm = new byte[8];
    String LABELX, LABELY;
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

    byte buffer[] = new byte[80];
    String error = null;

    int SLICE, SLICE1, SLICE2, IFDELAY, IF2D, IFNONLIN;
    int LTEMP, IERR, HISTNUM, qbins;
    int NUMX, NUMY, NUMW, smonsum, smonpot;
    int pulses, nleft, nright;
    int OUTUNIT, NDIV, ncenter;
    int MICRO, NHS, NCH1D, NCHTOT;
    int Nx, Ny;
    int NDIVx, NDIVy;
    DataSet RelSamp;
  
    boolean IBIN;
    float CHANWID;

    double MINQ, MAXQ, QMIN, QMAX;
    double[] QMINQ = new double[5];
    double minqx, maxqx, MINQy, MAXQy;
    float Qxmin, Qxmax;
    double[] DIV, QMAXQ = new double[5];
    double  LLOW, LHIGH, LAMAVG, LAMMAX;
    double IOFQMAX, IOFQMIN;
    double[] DELQ = new double[5];
    double THMAX;
    double SINTHMAX;
    double SINxMAX;
    double SINxMin;
    double SINyMAX;
    double SINyMin;
    float THICK;

    float L1, L2, CLK;
    double XDIM, YDIM, Qres;
    int DIVx, DIVy;
    float Qymin, Qymax, xDELTAQ, yDELTAQ;
    int INET = 0;
    int NEDGE = 1;

    /**
     * Arrays
     */
    int[] ITEMP = new int[3];
    int[] NSHIFT = new int[5];
    float[] LAMBDA ;
    
    String NAMCOL = "SN.DAT";
    String NAMLIS = "SMB.DAT";
    String NAMLIS2 = "S.DAT";
    String NAMLIS3 = "B.DAT";
    String namsq = "S2D.dat";
    String namsq1 = "B2D.dat";
    String namsq2 = "SN2D.DAT";
  
    double SOURCEFREQ = 30.0;

    double THMIN;//= 0.5*Math.atan(0.01*RADMIN/L2);
    double SINTHMIN;//= Math.sin(THMIN);

    String TITLE = "      -     -     -(2)";
    double Q0;// = 4.*PI/LAMAVG;

    double QMINS;//= Q0*SINTHMIN;
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
    double TWOPI = 2.0 * PI;
    double ZERO = 0.0;

    float SCALE;
 
    int RUNC, RUNB, RUNS;
    float XOFF, YOFF;

    public Reduce_KCL(){
      super("Reduce");
    }
    /**
    *    Constructor for Reduce_KCL.
    *    @param TransS   The sample Transmission data set
    *    @param TransB   The background Transmission data set
    *    @param  Eff     The Efficiency data set
    *    @param Sens     The sensitivity data set
    *    @param qu       The q bins if 1d or qxmin,qxmax, qymin, qymax
    *    @param RUNSds0   the monitor  for the sample

    *    @param RUNSds1   the Histogram  for the sample
    *    @param RUNBds0  the  monitor for the Background
    *    @param RUNBds1  the  Histogramfor the Background
    *    @param RUCBds0  null or the monitor for the  Cadmium run
    *    @param RUCBds1  null or the  Histogram for the  Cadmium run
    *    @param BETADN  The delayed neutron fraction
    *    @param SCALE   The scale factor to be applied to all data
    *    @param  THICK  The sample thickness in m
    *    @param  XOFF    The Xoffset of beam from the center in meters
    *    @param  YOFF    The Yoffset of beam from center in meters
    *    @param NQxBins  The number of Qx bins if 2D,otherwise use a neg number
    *    @param NQyBins  The number of Qx bins if 2D,otherwise use a neg number 
   */
 

    public Reduce_KCL(DataSet TransS, DataSet TransB, DataSet Eff, DataSet Sens,
        Vector qu, DataSet RUNSds0, DataSet RUNSds1, DataSet RUNBds0, 
        DataSet RUNBds1,DataSet RUNCds0,DataSet RUNCds1, float BETADN, 
        float SCALE, float THICK,
        float XOFF, float YOFF, int NQxBins, int NQyBins, boolean useTransB) {
        
        super( "Reduce");
        parameters = new Vector();
        addParameter( new Parameter("", TransS));
        addParameter( new Parameter("", TransB));
        addParameter( new Parameter("", Eff));
        addParameter( new Parameter("", Sens));
        addParameter( new Parameter("", qu));
        addParameter( new Parameter("", RUNSds0));
        addParameter( new Parameter("", RUNSds1));
        addParameter( new Parameter("", RUNBds0));
        addParameter( new Parameter("", RUNBds1));
        addParameter( new Parameter("", RUNCds0));
        addParameter( new Parameter("", RUNCds1));
        addParameter( new Parameter("", new Float(BETADN)));
        addParameter( new Parameter("", new Float(SCALE)));
        addParameter( new Parameter("", new Float(THICK)));
        addParameter( new Parameter("", new Float(XOFF)));
        addParameter( new Parameter("", new Float(YOFF)));
        addParameter( new Parameter("", new Integer(NQxBins)));
        addParameter( new Parameter("", new Integer(NQyBins)));
        addParameter( new Parameter("", new Boolean( useTransB)));

      }

    public void setDefaultParameters(){

       parameters = new Vector();
         addParameter( new DataSetPG("Sample Transmission DS", null));
        addParameter( new DataSetPG("Background Transmission DS", null));
        addParameter( new DataSetPG("Efficiency Data Set", null));
        addParameter( new DataSetPG("Sensitivity Data Set", null));
        addParameter( new QbinsPG("Enter Q bins", null));
        addParameter( new MonitorDataSetPG("Sample Monitor DS", null));
        addParameter( new SampleDataSetPG("Sample Histogram", null));
        addParameter( new MonitorDataSetPG("Background Monitor DS", null));
        addParameter( new SampleDataSetPG("Background Histogram", null));
        addParameter( new MonitorDataSetPG("Cadmium Monitor DS", 
                           DataSet.EMPTY_DATA_SET));
        addParameter( new SampleDataSetPG("Cadmium Histogram", 
                           DataSet.EMPTY_DATA_SET));
        addParameter( new FloatPG("Neutron Delay Fraction", new Float(.0011f)));
        addParameter( new FloatPG("Scale Factor",null));
        addParameter( new FloatPG("Thickness in m", null));
        addParameter( new FloatPG("X offset of beam in m", new Float(0)));
        addParameter( new FloatPG("Y offset of beam in m", new Float(0)));
        addParameter( new IntegerPG("# Qx bins", new Integer(-1)));
        addParameter( new IntegerPG("#Qy bins", new Integer(-1)));
        addParameter( new BooleanPG("", new Boolean( true)));

    }
  /* ---------------------------- getResult ------------------------------- */
    
    /**  Executes the operator using the parameters that were set up
     *@return  "Success" if there were no errors otherwise  the ErrorString
     *             "No Data Set Selected" is returned.<P>
     *
     *NOTE: A SelectedGraph View will also pop up
     */
   
   public Object getResult(){
        DataSet TransS=(DataSet)(getParameter(0).getValue());
        DataSet TransB=(DataSet)(getParameter(1).getValue());
        DataSet Eff=(DataSet)(getParameter(2).getValue());
        DataSet Sens=(DataSet)(getParameter(3).getValue());
        
        Vector Qu=(Vector)(getParameter(4).getValue());
        DataSet RUNSds0=(DataSet)(getParameter(5).getValue());
        DataSet RUNSds1=(DataSet)(getParameter(6).getValue());
        DataSet RUNBds0=(DataSet)(getParameter(7).getValue());
        DataSet RUNBds1=(DataSet)(getParameter(8).getValue());
        DataSet RUNCds0=(DataSet)(getParameter(9).getValue());
        DataSet RUNCds1=(DataSet)(getParameter(10).getValue());
        float BETADN=((Float)(getParameter(11).getValue())).floatValue();
        float SCALE=((Float)(getParameter(12).getValue())).floatValue();
        float THICK=((Float)(getParameter(13).getValue())).floatValue();
        float XOFF=((Float)(getParameter(14).getValue())).floatValue();
        float YOFF=((Float)(getParameter(15).getValue())).floatValue();
        int NQxBins=((Integer)(getParameter(16).getValue())).intValue();
        int NQyBins=((Integer)(getParameter(17).getValue())).intValue();
        useTransB =((Boolean)(getParameter(18).getValue())).booleanValue();
        this.SCALE = SCALE;
        this.XOFF = XOFF;
        this.YOFF = YOFF;
        REDUCEOUT = "S.OUT";
        DIVx = NQxBins;
        DIVy = NQyBins;
        RUNSds[0]= RUNSds0;
        RUNSds[1]= RUNSds1;
        RUNBds[0]= RUNBds0;
        RUNBds[1]= RUNBds1;
        if( RUNCds0 != null){
          RUNCds[0]= RUNCds0;
          RUNCds[1]= RUNCds1;
         }
        qu = new float[ Qu.size()];
        for( int i=0; i < Qu.size(); i++){
          qu[i] = ((Float)Qu.elementAt(i)).floatValue();
        }
        
        if ((NQxBins < 1) || (NQyBins < 1))
            IF2D = 0;
        else
            IF2D = 1;
        MaxSlice = RUNSds[1].getData_entry(0).getX_scale().getXs().length;
        this.TransS = TransS;
        this.TransB = TransB;
        this.Eff = Eff;
        this.Sens = Sens;
        this.RUNSds = RUNSds;
        this.RUNBds = RUNBds;
        this.RUNCds = RUNCds;
        this.qu = qu;

        MonitorInd = CalcTransmission.setMonitorInd( RUNSds0);
        MonitorID = new int[MonitorInd.length];
        for( int i = 0; i< MonitorInd.length; i++)
          MonitorID[i]= RUNSds0.getData_entry( MonitorInd[i]).getGroup_ID();

        RUNS = (((IntListAttribute) (RUNSds[0].getAttribute(Attribute.RUN_NUM))).getIntegerValue())[0];

        RUNB = (((IntListAttribute) (RUNBds[0].getAttribute(Attribute.RUN_NUM))).getIntegerValue())[0];
        if( RUNCds != null)
          RUNC = (((IntListAttribute) (RUNCds[0].getAttribute(Attribute.RUN_NUM))).getIntegerValue())[0];
        else RUNC = -1;
        ITEMP[1] = LTEMP;
        sdt[1] = sdt[1];
        edt[1] = edt[1];
        tttt[1] = tttt[1];
        users[1] = users[1];

        LABELX = "Q    (A**-1)";
        LABELY = "I(Q)   (CM**-1)";
        int NCHRX = 12;
        int NCHRY = 15;
        int NCHRM = 0;
        int ICURVE = -1;
        int IFERR = 1;
        BufferedReader NCHR;

        /***
         * INITIALIZE
         */


        int IUI = 5;

        OUTUNIT = 9;

        HISTNUM = 1;

        IFDELAY = 1;
        ANSDN = "Y";

        double PLANK = 6.626176E-27;
        double CMASS = 1.67495E-24;
        double PL = PLANK / CMASS;
        double TOTTIM = 1.0E6 / SOURCEFREQ;

        /**
         * OPEN .OUT FILE
         */

        try {
            F1 = new FileOutputStream(REDUCEOUT);
        } catch (Exception ex) {
            System.out.println("error");
        }
 
        if (BETADN > 0) 
            IFDELAY = 1;
        IFNONLIN = 0;

        /**
         * TYPE SOME PARAMETERS
         */

        System.out.println("CERTAIN STARTING PARAMETERS ARE THE FOLLOWING");
        System.out.flush();
        System.out.println();
        System.out.flush();
        System.out.println("HISTOGRAM NUMBER IS =" + String.valueOf(HISTNUM));
        System.out.println("BEAM STOP RADIUS IN CM  =" + String.valueOf(Radmin));
        System.out.println(" DELAYED NEUTRON CORRECTION IS MADE =");
        System.out.println("THE DELAYED NEUTRON FRACTION =" + String.valueOf(BETADN));
        System.out.println(" MGO FILTER IS IN THE BEAM ");
        System.out.println("Number of X and Y edge Chans masked for AD=" + String.valueOf(NEDGE));
        // System.out.flush();

        PixelInfoListAttribute pAttr = (PixelInfoListAttribute) (
                RUNSds[1].getData_entry(0).getAttribute(Attribute.PIXEL_INFO_LIST));
        PixelInfoList pilist = (PixelInfoList) (pAttr.getValue());
        IPixelInfo ipInfo = pilist.pixel(0);
        IDataGrid grid = ipInfo.DataGrid();
        
        NUMX = grid.num_rows();
        NUMY = grid.num_cols();
        FloatAttribute At = (FloatAttribute) (RUNSds[1].getData_entry(0).getAttribute(Attribute.INITIAL_PATH));
        float L1 = At.getFloatValue();

        L2 = grid.position().length();
        int NSLICE = NUMX * NUMY;

        double TOFDIST = L1 + L2;
        
        LAMMAX = (PL / TOFDIST) * TOTTIM;

        THMIN = 0.5 * Math.atan(0.01 * Radmin / L2);
        SINTHMIN = Math.sin(THMIN);

        XDIM = grid.width();
        YDIM = grid.height();
        double SFX = XDIM / NUMX;
        double SFY = YDIM / NUMY;
        for( int i=0;i<1;i++){
          tof_data_calc.SubtractDelayedNeutrons((TabulatedData) RUNSds[0].getData_entry(
               MonitorInd[i]),30f, BETADN);
          tof_data_calc.SubtractDelayedNeutrons((TabulatedData) RUNBds[0].getData_entry(
               MonitorInd[i]),30f, BETADN);
          
          tof_data_calc.SubtractDelayedNeutrons((TabulatedData) RUNCds[0].getData_entry(
               MonitorInd[i]),30f, BETADN);
         }      
        for( int i=0; i< RUNSds[1].getNum_entries(); i++){
          tof_data_calc.SubtractDelayedNeutrons((TabulatedData) RUNSds[1].getData_entry(
               i),30f, BETADN);
          tof_data_calc.SubtractDelayedNeutrons((TabulatedData) RUNBds[1].getData_entry(
              i),30f, BETADN);
          
          tof_data_calc.SubtractDelayedNeutrons((TabulatedData) RUNCds[1].getData_entry(
               i),30f, BETADN);

         }
       
        float[] tofs=RUNSds[1].getData_entry(NUMX * NUMY / 2).getX_scale().getXs();
        tofs = cnvertToWL(tofs, (XAxisConversionOp)RUNSds[1].
                 getOperator("Convert to WaveLength"),NUMX*NUMY/2);
        if( tofs[0] > tofs[1]) Reverse(tofs);
        xscl = new VariableXScale( tofs);
        
       ConvertToWL( RUNSds[0],xscl);
       ConvertToWL( RUNSds[1],xscl);
       System.gc();
       ConvertToWL( RUNBds[0],xscl);
       ConvertToWL( RUNBds[1],xscl);
       System.gc();
       ConvertToWL( RUNCds[0],xscl);
       ConvertToWL( RUNCds[1],xscl);
       System.gc();
      
       LAMBDA = xscl.getXs();
        this.SCALE = this.SCALE / THICK;

        /**
         * CONSTRUCT THE TITLE
         *
         */
        try {
            str1 = TITLE+" "+RUNS;
            F1.write( str1.getBytes());

            
           
            str1 = TITLE+" "+RUNB;
            F1.write( str1.getBytes());

            if (RUNC != 0) {
                str1 = TITLE+" "+RUNC;
            F1.write( str1.getBytes());

            }

        } catch (IOException e) {
        }
         return init();
       }
      /**
      *    Had to do this so global variables were used in 2nd code
      */
       public Object init(){
        AdjustGrid(RUNSds[1], XOFF, YOFF) ;
        AdjustGrid(RUNBds[1], XOFF, YOFF) ;
        UniformGrid SampGrid = SetUpGrid( RUNSds[1]);
        UniformGrid BackGrid = SetUpGrid( RUNBds[1]);
        UniformGrid CadGrid = SetUpGrid( RUNCds[1]);
        UniformGrid SensGrid = SetUpGrid( Sens);
        Grid_util.setEffectivePositions( RUNSds[1], SampGrid.ID());
        Grid_util.setEffectivePositions(RUNBds[1], BackGrid.ID());
        ZeroSens( SensGrid,SampGrid);
        ErrorString X = CalcRatios( SampGrid,CadGrid,TransS,RUNSds[0],
               RUNCds[0], SensGrid, Eff, SCALE, true);
        X = CalcRatios( BackGrid,CadGrid,TransB,
               RUNBds[0],RUNCds[0], SensGrid, Eff,SCALE, useTransB);
        Object Res = null;

        
        DataSet RelSamp = RUNSds[1];
        DataSet RelBackground = RUNBds[1];
        if (IF2D != 1) {
             
	    xscl = new VariableXScale(qu);
            
            DataSet SSampQ = SumQs( SampGrid, xscl, RUNSds[0], SensGrid, Eff);
            DataSet SBackQ  = SumQs( BackGrid, xscl, RUNSds[0], SensGrid, Eff);
            DataSet SDifQ = (DataSet)((new DataSetSubtract( SSampQ,SBackQ,true)).getResult());
            DataSetFactory.addOperators( SSampQ);
            DataSetFactory.addOperators( SBackQ);
            DataSetFactory.addOperators( SDifQ);
            SSampQ.setTitle("s"+RUNS);
            SBackQ.setTitle("b"+ RUNS);

            SDifQ.setTitle("sn" + RUNS);
            SampGrid = BackGrid=SensGrid = null;
            RelSamp = RelBackground = Sens = Eff=RUNSds[0]=null;
            Vector V = new Vector();
    
            int[] RunNums;
            if( RUNC >0)
              RunNums = new int[3];
            else
              RunNums = new int[2];
            RunNums[0] = RUNS;
            RunNums[1] = RUNB;
            if( RUNC > 0)
             RunNums[2] = RUNC;
            SSampQ.setAttribute( new IntListAttribute( Attribute.RUN_NUM,RunNums));
            SSampQ.getData_entry(0).setAttribute( new IntListAttribute( Attribute.RUN_NUM,RunNums));
            SBackQ.setAttribute( new IntListAttribute( Attribute.RUN_NUM,RunNums));
            SBackQ.getData_entry(0).setAttribute( new IntListAttribute( Attribute.RUN_NUM,RunNums));
            SDifQ.setAttribute( new IntListAttribute( Attribute.RUN_NUM,RunNums));
            SDifQ.getData_entry(0).setAttribute( new IntListAttribute( Attribute.RUN_NUM,RunNums));
            V.addElement(SSampQ);
            V.addElement(SBackQ);
            V.addElement(SDifQ);
            return V;
        }
       Res = (new DataSetSubtract(RelSamp, RelBackground, true)).getResult();
        if (Res instanceof ErrorString)
            return Res;
        DataSet RelDiff = (DataSet) Res;
        LLOW = .5*(LAMBDA[0]+  //Change to subrange of times
               LAMBDA[1]);
        PixelInfoList pilistx = ((PixelInfoList)RelSamp.getData_entry(0).
              getAttributeValue( Attribute.PIXEL_INFO_LIST));
        IPixelInfo ipinfx =pilistx.pixel(0);
        
        float[] mins = SampGrid.position(1f,1f).get();
        float[] maxs = SampGrid.position( (float) NUMX, (float) NUMY).get();

        double sinxMax = java.lang.Math.sin(.5*java.lang.Math.atan(-maxs[1]/L2));
        double sinyMax = java.lang.Math.sin(.5*java.lang.Math.atan(maxs[2]/L2));
        double sinxMin = java.lang.Math.sin(.5*java.lang.Math.atan(-mins[1]/L2));
        double sinyMin = java.lang.Math.sin(.5*java.lang.Math.atan(mins[2]/L2));
        Qxmin = (float)(4*java.lang.Math.PI*sinxMin/LLOW);
        Qymin = (float)(4*java.lang.Math.PI*sinyMin/LLOW);
        Qxmax = (float)(4*java.lang.Math.PI*sinxMax/LLOW);
        Qymax = (float)(4*java.lang.Math.PI*sinyMax/LLOW);
	if( qu != null)
	  if( qu.length >=4){
	     Qxmin =(float) java.lang.Math.max(Qxmin, qu[0]);
	     Qxmax = (float)java.lang.Math.min( Qxmax, qu[1]);
	     Qymin = (float)java.lang.Math.max(Qymin, qu[2]);
	     Qymax = (float)java.lang.Math.min( Qymax, qu[3]);
	  }
        xDELTAQ = ((Qxmax - Qxmin)/DIVx);
        yDELTAQ =((Qymax - Qymin)/DIVy);
        float[][] WTQXQY,SQXQY,SERRXY,BQXQY,BERRXY,SBQXQY, SBERRXY;
        WTQXQY=sss(DIVx,DIVy);SQXQY=sss(DIVx,DIVy);SERRXY=sss(DIVx,DIVy);
        BQXQY=sss(DIVx,DIVy);BERRXY=sss(DIVx,DIVy);SBQXQY=sss(DIVx,DIVy); 
        SBERRXY=sss(DIVx,DIVy);;
                
        
        
	int nn =0;
        float[] eff = Eff.getData_entry(0).getY_values();
        float[] effErr = Eff.getData_entry(0).getErrors();
        float[] Mon = RUNSds[0].getData_entry(MonitorInd[0]).getY_values();
        float[] MonErr = RUNSds[0].getData_entry(MonitorInd[0]).getErrors();
        Data Dback,Dsamp;
        DetectorPosition detPos ;
        float[]Qxy,SampYvals,BackYvals,SampErrs,BackErrs,wlvals;
        float scatAngle,Len,sens,sensErr,lambdaAv,Q,Qx,Qy,DNx,DNy ;
        int row,col,k,Nx,Ny;
	for( int i = 0; i< RelSamp.getNum_entries(); i++){
	   Dsamp =RelSamp.getData_entry(i);
	   IPixelInfo ipx = ((PixelInfoList)(RelSamp.getData_entry(i).
	          getAttributeValue(Attribute.PIXEL_INFO_LIST))).pixel(0);
	   row = (int)(ipx.row());
	   col = (int)(ipx.col());
	   Dback = RelBackground.getData_entry(i);
                          
	   detPos = ((DetectorPosition)Dsamp.getAttributeValue
	                          ( Attribute.DETECTOR_POS));
	   Qxy = tof_calc.DiffractometerVecQ( detPos,L1,1000f).
	            getCartesianCoords();
	   Len = (float)(java.lang.Math.sqrt( Qxy[0]*Qxy[0]+
	      Qxy[1]*Qxy[1]+Qxy[2]*Qxy[2]));
	   Qxy[0] = Qxy[0]/Len; Qxy[1] = Qxy[1]/Len;Qxy[2] = Qxy[2]/Len;
           scatAngle = detPos.getScatteringAngle();

	   SampYvals = Dsamp.getY_values();
	   BackYvals = Dback.getY_values();
           sens = SensGrid.getData_entry( row, col).getY_values()[0];
           sensErr =SensGrid.getData_entry( row, col).getErrors()[0];	   
	   SampErrs = Dsamp.getErrors();
	   BackErrs = Dback.getErrors(); 
	   wlvals = Dsamp.getX_scale().getXs();
   
            
	   for( int w =0; w+1 < wlvals.length;w++){
	      k=w;
	      lambdaAv = .5f*(wlvals[w]+wlvals[w+1]);
              Q = tof_calc.DiffractometerQofWavelength( scatAngle, lambdaAv); 
	      Qx = -Q*Qxy[1]; 
	      Qy = Q * Qxy[2];
             
              DNx = ((Qx -Qxmin)/xDELTAQ);
	      DNy = ((Qy -Qymin)/xDELTAQ);
              Nx=-1; Ny=-1;
              Nx = (int)java.lang.Math.floor(DNx);
              Ny = (int) java.lang.Math.floor(DNy);
	      if( Nx >=0)if(Ny>=0)if(Qx <Qxmax)if(Qy<Qymax){
                float W =sens*eff[k]*Mon[k];
                WTQXQY[Nx][Ny] = WTQXQY[Nx][Ny] + //weightYvals[k];
                          W;       
                SQXQY[Nx][Ny]=SQXQY[Nx][Ny]+SampYvals[k]*Mon[k];
                BQXQY[Nx][Ny]=BQXQY[Nx][Ny]+BackYvals[k]*Mon[k];
                SBQXQY[Nx][Ny] = SQXQY[Nx][Ny]-BQXQY[Nx][Ny];
                float U = SampErrs[k]*W;
                SERRXY[Nx][Ny]=SERRXY[Nx][Ny]+
                                  (float)Math.pow(U,2.0);
                U = BackErrs[k]*W;
                BERRXY[Nx][Ny]=BERRXY[Nx][Ny]+(
                                 float)Math.pow(U,2.0);
                SBERRXY[Nx][Ny]=BERRXY[Nx][Ny]+SERRXY[Nx][Ny];
	      }
	      else{
	        //System.out.println("out of bounds"+Qxmin+","+Q+","+Qxmax+"::"+
		         //  Qymin+","+Qy+","+Qymax);
	      }
	   }
	   
	  }//for( int i = 0; i< RelSamp.getNum_entries(); i++)
        
	
        for( int i = 0; i < DIVx; i++)
	  for( int j = 0; j < DIVy; j++){
	    if(WTQXQY[i][j] == 0){
	      SQXQY[i][j] = 0f;
	      BQXQY[i][j] =0f;
	      SBQXQY[i][j] =0f;
	      SERRXY[i][j] =0f;
	      BERRXY[i][j] =0f;
	      SBERRXY[i][j] = 0f;
	    }else{
	      SQXQY[i][j] = SQXQY[i][j]/WTQXQY[i][j];
	      BQXQY[i][j] =BQXQY[i][j]/WTQXQY[i][j];
	      SBQXQY[i][j] = SBQXQY[i][j]/WTQXQY[i][j];
	      SERRXY[i][j] =(float) java.lang.Math.sqrt(SERRXY[i][j])/WTQXQY[i][j];
	      BERRXY[i][j] =(float) java.lang.Math.sqrt(BERRXY[i][j])/WTQXQY[i][j];
	      SBERRXY[i][j] = (float)java.lang.Math.sqrt(SBERRXY[i][j])/WTQXQY[i][j];
            }
	  }
        WTQXQY = null;
        eff = effErr= Mon=MonErr = null;
        Qxy= SampYvals= BackYvals= SampErrs= BackErrs= wlvals= null;
        Dback = Dsamp = null;
        System.gc(); 
        Vector  V = new Vector();
	Object O1=show( Qxmin,Qymin,xDELTAQ,yDELTAQ,DIVx,DIVy, SQXQY,SERRXY,"s2d19990");
        SQXQY = null;
        SERRXY = null;
        System.gc();
	Object O2=show( Qxmin,Qymin,xDELTAQ,yDELTAQ,DIVx,DIVy, BQXQY,BERRXY,"b2d19990");
        BQXQY= null;BERRXY= null;
	Object O3=show( Qxmin,Qymin,xDELTAQ,yDELTAQ,DIVx,DIVy, SBQXQY,SBERRXY,"sn2d19990");
        SBQXQY= null;SBERRXY= null;
        System.gc();
	if( O1 instanceof ErrorString)
          return O1;
	if( O2 instanceof ErrorString)
          return O2;
	if( O3 instanceof ErrorString)
          return O3;
    
        V.addElement( O1); V.addElement(O2); V.addElement( O3);
        
        return V;

    }//end of getResult

   
    public void AdjustGrid(DataSet ds, float xoff, float yoff) { 

        int ids[] = Grid_util.getAreaGridIDs(ds);

        if (ids.length != 1)
            System.out.println("ERROR: wrong number of data grids " + ids.length);
        IDataGrid grid = Grid_util.getAreaGrid(ds, ids[0]);

        ((UniformGrid)grid).setDataEntriesInAllGrids(ds);
        Vector3D pos = grid.position();

        pos.add(new Vector3D(0, xoff, -yoff));
        ((UniformGrid) grid).setCenter(pos);
    
        Grid_util.setEffectivePositions(ds, grid.ID());
    }

public  Object show( float Qxmin,float Qymin,float Dx, float Dy, int Nx, int Ny,
    float[][] list, float[][] err, String DataSetName){
    DataSet DS = new DataSet(DataSetName,new OperationLog(),"per Angstrom",
          "","Rel Counts", "Rel Counts");
    DataSetFactory.addOperators( DS);
    DS.addOperator( new GetPixelInfo_op() );
    UniformGrid grid = new UniformGrid(47,"per Angstrom",new Vector3D(0f,0f,0f),
            new Vector3D(0f,Dy,0f), new Vector3D(0f,0f,Dx), Dx*Nx,Dy*Ny, 0.0001f,Ny,Nx);

    UniformXScale xscl = new UniformXScale( 0,1,2);
    int[] RunNums;
    if( RUNC <= 0)
      RunNums = new int[2];
    else 
      RunNums = new int[3];
    RunNums[0]= RUNS;
    RunNums[1] = RUNB;
    if( RUNC >0)
       RunNums[2] = RUNC;
    float[] yvals,errs;
    for(int row = 1; row<= Ny; row++)
      for( int col = 1; col <= Nx; col++){
       yvals = new float[1];
       errs = new float[1];
       yvals[0] = list[row-1][col-1];
       errs[0] = err[row-1][col-1];
       if( col == Nx) list[row-1] = null;
       HistogramTable Dat = new HistogramTable( xscl, yvals, errs, (row-1)*Nx+col);

       DetectorPixelInfo dpi = new DetectorPixelInfo((row-1)*Nx+col,(short)row,
                                                     (short)col,grid);
       Dat.setAttribute( new PixelInfoListAttribute(Attribute.PIXEL_INFO_LIST,
               new PixelInfoList( dpi)));


       Dat.setAttribute( new FloatAttribute( Attribute.INITIAL_PATH, 3));
       Dat.setAttribute( new FloatAttribute( Attribute.TOTAL_COUNT, yvals[0]));
       Dat.setAttribute( new IntListAttribute(Attribute.RUN_NUM, RunNums));
       DS.addData_entry(Dat);
    }
    DS.addOperator( new DataSetTools.operator.DataSet.Attribute.GetPixelInfo_op());
    DS.setAttribute( new StringAttribute(Attribute.INST_NAME,"SAND"));

    DS.setAttribute( new IntListAttribute(Attribute.RUN_NUM, RunNums));
    
    grid.setDataEntriesInAllGrids( DS);
   
    Grid_util.setEffectivePositions( DS, 47);
    return DS;
   
    }

    public static void main(String[] args) {

        IsawGUI.Util util = new IsawGUI.Util();

        DataSet[] RUNSds = null, RUNBds = null, RUNCds = null;
        DataSet[] TransS = null, TransB = null, Eff = null, Sens = null;
        float[] qu = new float[117];
        String Path, Instrument;
        float BETADN, SCALE;

        BETADN = 0.0011f;
        SCALE = 843000f;
        //
        qu[0] = 0.0035f;
        for (int i = 1; i < 117; i++) {
            qu[i] = qu[i - 1] * 1.05f;
            //System.out.println("qu ....." +qu[i]);
        }
       
        RUNSds = util.loadRunfile("C:\\Argonne\\sand\\wrchen03\\sand19990.run");
        RUNBds = util.loadRunfile("C:\\Argonne\\sand\\wrchen03\\sand19935.run");
        RUNCds = util.loadRunfile("C:\\Argonne\\sand\\wrchen03\\sand19936.run");
        System.out.println("After loading runfiles, before loading isds");
        try {
            TransS = ScriptUtil.load("C:\\ISAW\\DataSetTools\\operator\\Generic\\TOF_SAD\\tr1999019934.isd");
            TransB = ScriptUtil.load("C:\\ISAW\\DataSetTools\\operator\\Generic\\TOF_SAD\\tr1993519934.isd");
            Eff = ScriptUtil.load("C:\\ISAW\\DataSetTools\\operator\\Generic\\TOF_SAD\\efr19452.isd");
            Sens = ScriptUtil.load("C:\\ISAW\\SampleRuns\\sens19878.isd");
        } catch (Exception sss) {
            System.out.println("Error:" + sss);
        }

        System.out.println("Before calling Reduce_KCLxxxxxxxxxxxxxxxxxxxxxxxxxx");
        Reduce_KCL Reduce_KCL = new Reduce_KCL(TransS[0], TransB[0], 
                Eff[0], Sens[0],toVec(qu), RUNSds[0], RUNSds[1], 
		RUNBds[0],RUNBds[1], RUNCds[0],RUNCds[1], BETADN, SCALE, .1f,
                //     0f,0f);
                .000725f, .006909f, -200, -200, true);
        Object O = Reduce_KCL.getResult();
//new float[]{-.5f,.5f,-.5f,.5f}
        System.out.println("Finished O=" + O);
        Vector V = (Vector) O;
        ScriptUtil.display(((DataSet)(V.elementAt(0))).getAttributeValue(Attribute.RUN_NUM));
        ScriptUtil.display(V.elementAt(0));
        ScriptUtil.display(V.elementAt(1));
        ScriptUtil.display(V.elementAt(2));
       

    }
  private static Vector toVec( float[] list){
     if( list == null)
       return new Vector();
     Vector Res = new Vector();
     for( int i = 0; i< list.length; i++)
         Res.addElement( new Float( list[i]));
     return Res;
  }

 
    
 private DataSet SumQs(UniformGrid SampGrid, XScale xscl, DataSet RunsDs, 
                 UniformGrid SensGrid, DataSet Effx){
       float[] Resy = new float[ xscl.getNum_x()-1];
       float[] ErrSq = new float[ Resy.length];
       float [] weight = new float[ Resy.length];
       float sens, sensErr;
       float[] yvals, errs, xvals, eff,eff1,eff2,effMon;
       Data D, D1, D2;
       Arrays.fill( Resy,0.0f);
       Arrays.fill( ErrSq, 0.0f);
       Arrays.fill( weight, 0.0f);
       float[] monit = RunsDs.getData_entry(MonitorInd[0]).getY_values();
      
       eff = Eff.getData_entry(0).getY_values();
       float[] effErr = Eff.getData_entry(0).getErrors();
       eff2 = new float[ eff.length];
       for( int i = 0; i< eff.length; i++)
          eff2[i]= eff[i]*monit[i];
      
       boolean done = false;
       for( int row = 1; row <= SampGrid.num_rows(); row++)
         for( int col = 1; col <= SampGrid.num_cols(); col++){
            sens = SensGrid.getData_entry(row, col).getY_values()[0];
            sensErr = SensGrid.getData_entry(row, col).getErrors()[0];
            if( sens != 0.0f){
               D = SampGrid.getData_entry( row, col);
               xvals = convt_toQ( D);
               yvals = D.getY_values();
               errs =  AdjustErrs(D.getErrors(), yvals,eff,effErr, sens,
                          sensErr, monit) ;
               for( int i = 0; i< yvals.length; i++)
                  yvals[i] = yvals[i]*monit[i];
               yvals =Rebin( yvals,xvals, xscl);
               errs =Rebin( errs,xvals, xscl);
               eff1 =Rebin( eff2,xvals, xscl);
            
               SqErrors( errs);
               for( int chan = 0; chan < yvals.length; chan++)
                 if( eff1[chan] !=0){
                   Resy[chan] += yvals[chan];
                   ErrSq[chan] += errs[chan];
                   weight[chan] += sens*eff1[chan];
                 }
               
            }//sens !=0
           

       }//for rows and cols
       for( int i = 0; i< Resy.length;i++)
           if( weight[i] > 0){
             Resy[i] = Resy[i]/weight[i];
             ErrSq[i] = (float)java.lang.Math.sqrt( ErrSq[i])/weight[i];
           }else
             Resy[i]=ErrSq[i]=0.0f;

      DataSet Result = new DataSet("S of Q",new OperationLog(),"per Angstrom",
              "Q","Rel Intensity", "Intensity");  
      D = new HistogramTable( xscl, (Resy), (ErrSq), 0);
      Result.addData_entry( D); 
      return Result; 

  }//SumQs 

  private float[] AdjustErrs( float[] err, float[] yvals, float[] eff, float[] effErr,
          float sens, float sensErr,float[]monit){
      for( int i=0; i< err.length; i++){
       /* float errprod =(float) java.lang.Math.sqrt(effErr[i]*sens*effErr[i]*sens + 
                       sensErr*eff[i]*sensErr*eff[i]);
        if( eff[i] == 0) err[i] = 0;
        else if( sens == 0) err[i] = 0;
        else if( yvals[i] == 0)
             err[i]=0;
       else
        err[i] =(float) Math.sqrt(err[i]*err[i]/yvals[i]/yvals[i] + 
            errprod*errprod/eff[i]/eff[i]/sens/sens);
       */
        err[i] = err[i]*monit[i]*eff[i]*sens;
      }               
      
     
      return err;

  }
  private float prodErr( float Fac1, float Fac1Err, float Fac2, float Fac2Err){
      return (float)Math.sqrt(Fac1*Fac2Err*Fac1*Fac2Err+Fac2*Fac1Err*Fac2*Fac1Err);

  }
  private float SumDiffErr(  float Term1Err, float Term2Err){
    return (float)Math.sqrt( Term1Err*Term1Err+Term2Err*Term2Err);
  }
 

 //  Calcs   (S/Ms-C/Mc)/Ts  with errs for That/sens/eff
 private ErrorString CalcRatios( IDataGrid SampGr, IDataGrid CadmiumGr, 
           DataSet Transm, DataSet SampMon, DataSet CadmMon, IDataGrid SensGr,
           DataSet Eff, float SCALE, boolean useTransmission){
     float[] sampy,samperr,Cadmy,Cadmerr;
  
     float[] Transmy ;
     if( useTransmission)
        Transmy= Transm.getData_entry(0).getY_values();
     else
        Transmy = null;
     float[] SampMony = SampMon.getData_entry(MonitorInd[0]).getY_values();
     float[]  CadmMony =  CadmMon.getData_entry(MonitorInd[0]).getY_values();
     float[]  Effy = Eff.getData_entry(0).getY_values();
     float[] Transmerr=null;
     if( useTransmission)
         Transmerr = Transm.getData_entry(0).getErrors();
     float[] SampMonerr = SampMon.getData_entry(MonitorInd[0]).getErrors();
     float[]  CadmMonerr =  CadmMon.getData_entry(MonitorInd[0]).getErrors();
     float[]  Efferr = Eff.getData_entry(0).getErrors();
     Data D;
     float err1,err2,err3, Num,Den, sens,senserr;
     int row,col;
     for( row = 1; row <= SampGr.num_rows(); row++)
       for( col = 1; col <= SampGr.num_cols(); col++){
         sens = SensGr.getData_entry( row,col).getY_values()[0];
         senserr = SensGr.getData_entry( row,col).getErrors()[0];
          D = SampGr.getData_entry( row,col);
         sampy = D.getY_values();
         samperr = D.getErrors();
         if( sens == 0){
             Arrays.fill( sampy,0.0f);
             Arrays.fill( samperr, 0.0f);
         }else {
          D = CadmiumGr.getData_entry( row,col);
         Cadmy = D.getY_values();
         Cadmerr = D.getErrors();
         D = null;
         for( int i=0; i< sampy.length; i++){
            err1= quoErr(sampy[i],samperr[i],SampMony[i],SampMonerr[i]);
            
            err2= quoErr(Cadmy[i],Cadmerr[i],CadmMony[i],CadmMonerr[i]);
            err3 = SumDiffErr( err1,  err2);
           
            Num = sampy[i]/SampMony[i] -Cadmy[i]/CadmMony[i]; 
            sampy[i] = Num;
            if( useTransmission){
              samperr[i] = quoErr( sampy[i], err3,Transmy[i],Transmerr[i]);
               sampy[i] =sampy[i]/Transmy[i];
            }
            
            samperr[i]= quoErr( sampy[i],samperr[i], sens*Effy[i],
                     prodErr( sens, senserr, Effy[i], Efferr[i]));
            sampy[i]= SCALE*sampy[i];
            samperr[i] = samperr[i]*SCALE;

        }
       }//else sens ==0
     }
    return null;
 }
 private float quoErr( float Num, float NumErr, float Den, float DenErr){
   if( Den ==0){DenErr=0;Den=1;}
   
   float V = Num/Den/Den;
   return (float)Math.sqrt(NumErr*NumErr/Den/Den+ V*V*DenErr*DenErr );
 }
  private  static float[]  Reverse(  float[] X){
        if( X == null)
          return null;
        float sav;
        int n= X.length ;
        for( int i = 0; i < X.length/2; i++){
          sav = X[i];
          X[i] = X[n-1-i];
          X[n-1-i] = sav;
        }

       return X;

  }
  //assumes in wave length
  private float[] convt_toQ( Data D){
      float[] Res = D.getX_scale().getXs();
      float scatAngle =((DetectorPosition) D.getAttributeValue( 
                    Attribute.DETECTOR_POS)).getScatteringAngle();
       float a = Res[Res.length-1];
       for( int i = 0; i< Res.length; i++){
          Res[i] = tof_calc.DiffractometerQofWavelength( scatAngle, Res[i]);

       }
      PixelInfoList pilist =(PixelInfoList)(D.getAttributeValue(
                  Attribute.PIXEL_INFO_LIST));
      return Res;
  }
 
  public static float[] Rebin( float[] yvals,float[] xvals, XScale qu){
    float[] xx= qu.getXs();
    float[] Res = new float[ xx.length-1];
    Arrays.fill( Res, 0.0f);
    int i, j,k;
    i = xvals.length - 1;
    //xvals are in reverse order
    for( j=0; j + 1< xx.length; j++){
      
       while( (i-1 >=0) &&( xvals[i-1] < xx[j]) )i--;
       if( i < 1)
          return Res;
       if( xvals[i] < xx[j+1])
       Res[j] += yvals[i-1]* (java.lang.Math.min( xvals[i-1],xx[j+1])-
                 java.lang.Math.max( xx[j],xvals[i]))/(xvals[i-1]-xvals[i]);
       i--;
       if( i > 0)
       while( (i >=0)&&( xvals[i] <xx[j+1])){
         Res[j]+= yvals[i-1]*( java.lang.Math.min( xvals[i-1],xx[j+1])-
                 java.lang.Math.max( xx[j],xvals[i]))/(xvals[i-1]-xvals[i]);
         i--;
       }     
       if( i < 0) return Res;
       if( xvals[i] >= xx[j+1]) i++;
       if( i >= xvals.length) i =xvals.length-1;        

    }
  
   return Res;

  }
 
  private UniformGrid SetUpGrid( DataSet DS){
         int[] Ids= Grid_util.getAreaGridIDs( DS);
        UniformGrid SampGrid = (UniformGrid)Grid_util.getAreaGrid( DS, Ids[0]);
        SampGrid.clearData_entries();
        SampGrid.setData_entries(DS);
        return SampGrid; 
      }

  private void SqErrors( float[] errs){
    if( errs == null) return;
    for( int i=0; i< errs.length; i++)
       errs[i]= errs[i]*errs[i];
  
   }

  private void ZeroSens( UniformGrid SensGrid, UniformGrid SampGrid){

     for( int row = 1; row <= SensGrid.num_rows(); row++)
        for( int col = 1; col <= SensGrid.num_cols(); col++){
            boolean Z = false;
            if ((row < Nedge) || (row > SensGrid.num_rows() - Nedge) || (col < Nedge) ||
                (col > SensGrid.num_cols() - Nedge))
               Z = true;    

            Data DD =SampGrid.getData_entry(row, col);
            DetectorPosition dp = (DetectorPosition)(DD.getAttributeValue(Attribute.DETECTOR_POS));
             
            float[] pos = dp.getCartesianCoords();
    

            float rad = pos[1] * pos[1] + pos[2] * pos[2];
            if ((rad < Radmin * Radmin) || (rad > Radmax * Radmax))
                Z = true;
            if( Z)
               SensGrid.getData_entry(row,col).getY_values()[0]= 0.0f;
   }
  }
  private float[][] sss( int nrows, int ncols){

    float[][]Res = new float[nrows][ncols];

    for( int i=0; i< nrows; i++)
      Arrays.fill(Res[i],0.0f);
    return Res;
  }
  public static float[] cnvertToWL( float[] x, XAxisConversionOp op, int DataIndex){
      for(int i= 0; i< x.length; i++)
        x[i]= op.convert_X_Value(x[i], DataIndex);
      return x;
  }
  /**
  *     This method is "memory efficient".  Since all have a common XScale there
  *     is little extra space
  */
  public static DataSet ConvertToWL( DataSet ds, XScale wlScale){
   
    XAxisConversionOp op =(XAxisConversionOp)( ds.getOperator("Convert to Wavelength"));
   
    if( op == null)
      op =(XAxisConversionOp)( ds.getOperator("Monitor to Wavelength"));
    Data D, D1;
    for( int i = 0; i< ds.getNum_entries(); i++){
      D = ds.getData_entry(i);
      float[] xvals = D.getX_scale().getXs();
      float[] yvals = D.getY_values();
      float[] errs = D.getErrors();
      AttributeList alist = D.getAttributeList();
      xvals = cnvertToWL( xvals, op, i);
      if( xvals[0] > xvals[1]){
        Reverse(xvals);
        Reverse(yvals);
        Reverse(errs);
      }
     D1= Data.getInstance(new VariableXScale( xvals), yvals,errs, D.getGroup_ID());
     D1.setAttributeList( alist);
     D1.resample( wlScale, IData.SMOOTH_NONE);
     ds.replaceData_entry( D1, i);

    }
   ds.setX_units("Angstrom");
   ds.setX_label("WaveLength");
   return ds;
 }
}

