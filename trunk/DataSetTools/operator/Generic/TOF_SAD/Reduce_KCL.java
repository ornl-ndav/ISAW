/*
 * File:  Reduce_KCL.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson, Alok Chatterjee and Dennis Mikkelson
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
 * Modified:
 *
 * $Log$
 * Revision 1.27  2004/04/26 18:57:41  dennis
 *   Major restructuring of previous "prototype" version.  All low level
 * routines are now static methods in the SAD_Util class, so that they
 * can be reused and maintained more easily.
 *
 *
 * $Log$
 * Revision 1.27  2004/04/26 18:57:41  dennis
 *   Major restructuring of previous "prototype" version.  All low level
 * routines are now static methods in the SAD_Util class, so that they
 * can be reused and maintained more easily.
 *
 * Revision 1.26  2004/04/22 20:33:53  dennis
 * Removed unused variables
 *
 * Revision 1.25  2004/04/21 20:45:33  dennis
 * Changed default value for beamstop size to 1.5, instead of 0.
 * This allows the new version to work with old scripts for
 * SAND.
 *
 * Revision 1.24  2004/04/21 19:42:28  dennis
 * Added beamstop size to the test code in the main method.
 *
 * Revision 1.23  2004/04/21 15:48:44  chatterjee
 * Fixed 2D case binning out-of-bounds error
 *
 * Revision 1.22  2004/04/21 14:27:24  chatterjee
 * Added a new parameter beam_stop dimension (bs_dim).
 * Scripts calling Reduce_KCL will need to provide this number in cms.
 * For SAND the value is 1.5 and for SASI 2.1.
 *
 * Revision 1.21  2004/04/08 15:17:11  dennis
 * Now uses "new" DataSetPGs consistently and calls clear() after getting the
 * value from the DataSetPG, to avoid memory leaks.
 * Replaced all parameters with new ParameterGUI's for consistency.
 * Removed calls to System.gc() to improve performance.
 * Renamed methods to Build2D_DS() and CalculateResults() to improve
 * readability.
 *
 * Revision 1.20  2004/03/19 20:28:16  dennis
 * Fixed output file name in 2D case, so it uses the
 * correct run number, not always 19990.
 * Added correct javadocs on getResult() method.
 *
 * Revision 1.19  2004/03/15 03:28:37  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.18  2004/01/24 20:05:19  bouzekc
 * Removed unused imports.
 *
 * Revision 1.17  2003/12/02 15:58:18  rmikk
 * Changed (Float) to (Number) to eliminate a Class Cast exception when
 *   dealing with Python
 *
 * Revision 1.16  2003/10/28 19:56:40  rmikk
 * Fixed javadoc error
 *
 * Revision 1.15  2003/10/22 20:21:48  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.14  2003/10/02 15:38:17  rmikk
 * Fixed an error when the background transmission run is
 *    being used.
 *
 * Revision 1.13  2003/10/02 14:40:52  rmikk
 * Eliminated? an array out of bounds error
 *
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

import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.ViewTools.Panels.Transforms.*;

import java.util.Vector;

import Command.*;
import DataSetTools.dataset.*;
import DataSetTools.math.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.operator.DataSet.Math.DataSet.*;


public class Reduce_KCL  extends GenericTOF_SAD{

    public static final int DEFAULT_NEDGE = 1;  // mask off edge detectors 

    private boolean debug = false; 

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *  Default Constructor
    */
    public Reduce_KCL()
    {
      super("Reduce");
    }

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *    Constructor for Reduce_KCL.
    *    @param TransS    The sample Transmission data set
    *    @param TransB    The background Transmission data set
    *    @param  Eff      The Efficiency data set
    *    @param Sens      The sensitivity data set
    *    @param quV       The q bins if 1d or qxmin,qxmax, qymin, qymax
    *    @param RUNSds0   The monitor for the sample
    *    @param RUNSds1   The Histogram for the sample
    *    @param RUNBds0   The  monitor for the Background
    *    @param RUNBds1   The  Histogramfor the Background
    *    @param RUNCds0   null or the monitor for the Cadmium run
    *    @param RUNCds1   null or the  Histogram for the Cadmium run
    *    @param BETADN    The delayed neutron fraction
    *    @param SCALE     The scale factor to be applied to all data
    *    @param THICK     The sample thickness in m
    *    @param XOFF      The Xoffset of beam from the center in meters
    *    @param YOFF      The Yoffset of beam from center in meters
    *    @param NQxBins   The number of Qx bins if 2D,otherwise use a neg number
    *    @param NQyBins   The number of Qx bins if 2D,otherwise use a neg number
    *    @param useTransB Use the background Transmission run
    *    @param bs_dim    New beam stop dimensions
   */
   public Reduce_KCL( DataSet TransS, 
                      DataSet TransB, 
                      DataSet Eff, 
                      DataSet Sens,
                      Vector  quV, 
                      DataSet RUNSds0, 
                      DataSet RUNSds1, 
                      DataSet RUNBds0, 
                      DataSet RUNBds1, 
                      DataSet RUNCds0,
                      DataSet RUNCds1, 
                      float   BETADN, 
                      float   SCALE, 
                      float   THICK,
                      float   XOFF, 
                      float   YOFF, 
                      int     NQxBins, 
                      int     NQyBins, 
                      boolean useTransB, 
                      float   bs_dim ) 
     {
        
        super( "Reduce");
        parameters = new Vector();
        addParameter( new DataSetPG("Sample Transmission DS", TransS));
        addParameter( new DataSetPG("Background Transmission DS", TransB));
        addParameter( new DataSetPG("Efficiency Data Set", Eff));
        addParameter( new DataSetPG("Sensitivity Data Set", Sens));
        addParameter( new QbinsPG  ("Enter Q bins", quV));
        addParameter( new MonitorDataSetPG("Sample Monitor DS", RUNSds0));
        addParameter( new SampleDataSetPG ("Sample Histogram DS", RUNSds1));
        addParameter( new MonitorDataSetPG("Background Monitor DS", RUNBds0));
        addParameter( new SampleDataSetPG ("Backgrond Histogram DS", RUNBds1));
        addParameter( new MonitorDataSetPG("Cadmium Monitor DS", RUNCds0));
        addParameter( new SampleDataSetPG ("Cadmium Histogram DS", RUNCds1));
        addParameter( new FloatPG("Neutron Delay Fraction", new Float(BETADN)));
        addParameter( new FloatPG("Scale Factor", new Float(SCALE)));
        addParameter( new FloatPG("Thickness in m", new Float(THICK)));
        addParameter( new FloatPG("X offset of beam in m", new Float(XOFF)));
        addParameter( new FloatPG("Y offset of beam in m", new Float(YOFF)));
        addParameter( new IntegerPG("# Qx bins", new Integer(NQxBins)));
        addParameter( new IntegerPG("# Qy bins", new Integer(NQyBins)));
        addParameter( new BooleanPG("Use Background Transmission Run?", 
                                     new Boolean( useTransB)));
        addParameter( new FloatPG("Beam Stop Size", new Float(bs_dim)));
      }


   /* ----------------------- setDefaultParameters  ------------------------ */

    public void setDefaultParameters()
    {
       parameters = new Vector();
       addParameter( new DataSetPG("Sample Transmission DS", null));
       addParameter( new DataSetPG("Background Transmission DS", null));
       addParameter( new DataSetPG("Efficiency Data Set", null));
       addParameter( new DataSetPG("Sensitivity Data Set", null));
       addParameter( new QbinsPG("Enter Q bins", null));
       addParameter( new MonitorDataSetPG("Sample Monitor DS", null));
       addParameter( new SampleDataSetPG ("Sample Histogram DS", null));
       addParameter( new MonitorDataSetPG("Background Monitor DS", null));
       addParameter( new SampleDataSetPG ("Backgrond Histogram DS", null));
       addParameter( new MonitorDataSetPG("Cadmium Monitor DS", 
                         DataSet.EMPTY_DATA_SET));
       addParameter( new SampleDataSetPG("Cadmium Histogram DS", 
                         DataSet.EMPTY_DATA_SET));
       addParameter( new FloatPG("Neutron Delay Fraction", new Float(.0011f)));
       addParameter( new FloatPG("Scale Factor",null));
       addParameter( new FloatPG("Thickness in m", null));
       addParameter( new FloatPG("X offset of beam in m", new Float(0)));
       addParameter( new FloatPG("Y offset of beam in m", new Float(0)));
       addParameter( new IntegerPG("# Qx bins", new Integer(-1)));
       addParameter( new IntegerPG("# Qy bins", new Integer(-1)));
       addParameter( new BooleanPG("Use Background Transmission Run?", 
                                    new Boolean( true ) ) );
       addParameter( new FloatPG("Beam Stop Size", 
                                  new Float(1.5f)));
    }

  /* ---------------------------- getResult ------------------------------- */
    /**  
     *  Builds three DataSets containing the corrected scattering results
     *  for the sample, background and sample minus background, respectively.
     *  These DataSets are returned in a java Vector.   If either of the
     *  "size" parameters NQxBins or NQyBins are less than 1, the one
     *  dimensional S(Q) will be calculated and returned in the three
     *  DataSets.  If both NQxBins and NQyBins are at least 1, then the
     *  two dimensional S(Qx,Qy) will be calculated instead.
     *
     *  @return The vector of three DataSets will be returned if 
     *          no errors are encountered, otherwise an ErrorString
     *          will be returned. 
     */
     public Object getResult()
     {
        ElapsedTime timer = null;
        if ( debug )
        { 
          System.out.println("Start of getResult()");
          timer = new ElapsedTime();
        }

        DataSet TransS=(DataSet)(getParameter(0).getValue());
        ((DataSetPG)getParameter(0)).clear();

        DataSet TransB=(DataSet)(getParameter(1).getValue());
        ((DataSetPG)getParameter(1)).clear();

        DataSet Eff=(DataSet)(getParameter(2).getValue());
        ((DataSetPG)getParameter(2)).clear();

        DataSet Sens=(DataSet)(getParameter(3).getValue());
        ((DataSetPG)getParameter(3)).clear();
        
        Vector Qu=(Vector)(getParameter(4).getValue());

        DataSet RUNSds0=(DataSet)(getParameter(5).getValue());
        ((DataSetPG)getParameter(5)).clear();

        DataSet RUNSds1=(DataSet)(getParameter(6).getValue());
        ((DataSetPG)getParameter(6)).clear();

        DataSet RUNBds0=(DataSet)(getParameter(7).getValue());
        ((DataSetPG)getParameter(7)).clear();

        DataSet RUNBds1=(DataSet)(getParameter(8).getValue());
        ((DataSetPG)getParameter(8)).clear();

        DataSet RUNCds0=(DataSet)(getParameter(9).getValue());
        ((DataSetPG)getParameter(9)).clear();

        DataSet RUNCds1=(DataSet)(getParameter(10).getValue());
        ((DataSetPG)getParameter(10)).clear();

        float   BETADN    = ((Float)(getParameter(11).getValue())).floatValue();
        float   SCALE     = ((Float)(getParameter(12).getValue())).floatValue();
        float   THICK     = ((Float)(getParameter(13).getValue())).floatValue();
        float   XOFF      = ((Float)(getParameter(14).getValue())).floatValue();
        float   YOFF      = ((Float)(getParameter(15).getValue())).floatValue();
        int     NQxBins   = ((Integer)(getParameter(16).getValue())).intValue();
        int     NQyBins   = ((Integer)(getParameter(17).getValue())).intValue();
        boolean useTransB = 
                        ((Boolean)(getParameter(18).getValue())).booleanValue();
        float   bs_dim    = ((Float)(getParameter(19).getValue())).floatValue();

        SCALE = SCALE / THICK;

        float Radmin = bs_dim/100;
        float Radmax = 100.0f / 100; 
        int   nedge  = DEFAULT_NEDGE;

        DataSet[] RUNSds = new DataSet[2];
        DataSet[] RUNBds = new DataSet[2];
        DataSet[] RUNCds = new DataSet[2];

        RUNSds[0]= RUNSds0;
        RUNSds[1]= RUNSds1;
        RUNBds[0]= RUNBds0;
        RUNBds[1]= RUNBds1;
        if( RUNCds0 != null)
        {
          RUNCds[0]= RUNCds0;
          RUNCds[1]= RUNCds1;
         }

        float qu[] = new float[ Qu.size() ];
        for( int i=0; i < Qu.size(); i++){
          qu[i] = ((Number)Qu.elementAt(i)).floatValue();
        }
        
        boolean make_2D;
        if ((NQxBins < 1) || (NQyBins < 1))
            make_2D = false;
        else
            make_2D = true;

        int MonitorInd[] = CalcTransmission.setMonitorInd( RUNSds0 );

        DataSet ds_list[] = new DataSet[3];
        ds_list[0] = RUNSds0;
        ds_list[1] = RUNBds0;
        ds_list[2] = RUNCds0;
        int RunNums[] = SAD_Util.BuildRunNumList( ds_list );

        System.out.println("CERTAIN STARTING PARAMETERS ARE THE FOLLOWING");
        System.out.println();
        System.out.println(" BEAM STOP RADIUS IN CM  =" + Radmin);
        System.out.println(" DELAYED NEUTRON CORRECTION IS MADE =");
        System.out.println(" THE DELAYED NEUTRON FRACTION =" + BETADN);
        System.out.println(" MGO FILTER IS IN THE BEAM ");
        System.out.println(" Number of X and Y edge Chans masked for AD=" + 
                             String.valueOf(nedge));

        for( int i = 0; i < 1; i++ )
        {
          tof_data_calc.SubtractDelayedNeutrons(
           (TabulatedData)RUNSds[0].getData_entry( MonitorInd[i]), 30f, BETADN);
          
          tof_data_calc.SubtractDelayedNeutrons(
           (TabulatedData)RUNBds[0].getData_entry( MonitorInd[i]), 30f, BETADN);
          
          if ( RUNCds0 != null )
            tof_data_calc.SubtractDelayedNeutrons(
           (TabulatedData)RUNCds[0].getData_entry( MonitorInd[i]), 30f, BETADN);
         }      

        for( int i = 0; i < RUNSds[1].getNum_entries(); i++ )
        {
          tof_data_calc.SubtractDelayedNeutrons(
                      (TabulatedData)RUNSds[1].getData_entry(i),30f, BETADN);
          
          tof_data_calc.SubtractDelayedNeutrons(
                      (TabulatedData)RUNBds[1].getData_entry(i),30f, BETADN);
          
          if ( RUNCds0 != null )
            tof_data_calc.SubtractDelayedNeutrons(
                      (TabulatedData) RUNCds[1].getData_entry(i),30f, BETADN);
         }

        int num_data = RUNSds[1].getNum_entries();
        float[] tofs = RUNSds[1].getData_entry(num_data/2).getX_scale().getXs();
        tofs = SAD_Util.ConvertXsToWL(tofs, RUNSds[1], num_data / 2, false);

        if( tofs[0] > tofs[1]) 
          arrayUtil.Reverse(tofs);

        if ( debug )
        {
          System.out.println("After ConvertXsToWL, time used = " 
                             + timer.elapsed() );
          timer.reset();
        }

        XScale xscl = new VariableXScale( tofs );
        
        SAD_Util.ConvertToWL( RUNSds[0], xscl, true );   // monitor
        SAD_Util.ConvertToWL( RUNSds[1], xscl, false );  // sample

        SAD_Util.ConvertToWL( RUNBds[0], xscl, true );   // monitor
        SAD_Util.ConvertToWL( RUNBds[1], xscl, false );  // backgron 

        SAD_Util.ConvertToWL( RUNCds[0], xscl, true );   // monitor
        SAD_Util.ConvertToWL( RUNCds[1], xscl, false );  // cadmium 
      
        if ( debug )
        {
          System.out.println("After ConvertToWL RUNSds[0]...RUNCds[1], " +
                             "time used = " + timer.elapsed());
          System.out.println("xscl has " + xscl.getNum_x() );
          timer.reset();
        }

        float[] lambda = xscl.getXs();

        UniformGrid SampGrid = SAD_Util.SetUpGrid( RUNSds[1] );
        UniformGrid BackGrid = SAD_Util.SetUpGrid( RUNBds[1] );
        UniformGrid CadGrid  = SAD_Util.SetUpGrid( RUNCds[1] );
        UniformGrid SensGrid = SAD_Util.SetUpGrid( Sens );

        SAD_Util.FixGroupIDs( BackGrid, SampGrid );
        SAD_Util.FixGroupIDs( CadGrid,  SampGrid );
        SAD_Util.FixGroupIDs( SensGrid, SampGrid );

        int sensIndex[] = SAD_Util.BuildIndexOfID_Table( Sens );
        int cadIndex[]  = SAD_Util.BuildIndexOfID_Table( RUNCds[1] );

        SAD_Util.AdjustGrid( RUNSds[1], XOFF, YOFF );
        SAD_Util.AdjustGrid( RUNBds[1], XOFF, YOFF );

        CoordBounds bounds = SAD_Util.GetQRegion( SampGrid, lambda, qu );

        SAD_Util.ZeroAreaDetSens( SensGrid, SampGrid, Radmin, Radmax, nedge );

        SAD_Util.CalcRatios( RUNSds, RUNCds, cadIndex, TransS, true, 
                             Eff, Sens, sensIndex, MonitorInd, SCALE );
        SAD_Util.CalcRatios( RUNBds, RUNCds, cadIndex, TransB, useTransB, 
                             Eff, Sens, sensIndex, MonitorInd, SCALE );
        
        DataSet SampSQ,
                BackSQ,
                DiffSQ;

        if ( make_2D ) 
        {
          SampSQ = SAD_Util.SumQs_2D( RUNSds, Eff, Sens, sensIndex, MonitorInd,
                                      bounds, NQxBins, NQyBins );
          BackSQ = SAD_Util.SumQs_2D( RUNBds, Eff, Sens, sensIndex, MonitorInd,
                                      bounds, NQxBins, NQyBins );
          DiffSQ = SAD_Util.Build2D_Difference_DS( SampSQ, BackSQ );
        }
        else
        {              
          xscl = new VariableXScale(qu);
          SampSQ = SAD_Util.SumQs_1D( RUNSds, Eff, Sens, sensIndex, MonitorInd,
                                      xscl );
          BackSQ = SAD_Util.SumQs_1D( RUNBds, Eff, Sens, sensIndex, MonitorInd,
                                      xscl );
          DiffSQ = (DataSet)((new DataSetSubtract( SampSQ, BackSQ, true ))
                                                                 .getResult());
        }

        String titles[] = new String[3];
        titles[0] = "s";
        titles[1] = "b";
        titles[2] = "sn";
        if ( make_2D )
          for ( int i = 0; i < 3; i++ )
            titles[i] += "2d";

        Vector V = new Vector();
        V.addElement( SampSQ );
        V.addElement( BackSQ );
        V.addElement( DiffSQ );

        for ( int i = 0; i < 3; i++ )
        {
          DataSet SQds = (DataSet)V.elementAt(i);
          DataSetFactory.addOperators( SQds );
          SQds.addOperator( new GetPixelInfo_op() );
 
          Attribute attr = new IntListAttribute( Attribute.RUN_NUM, RunNums );
          SQds.setAttribute( attr );
          for ( int k = 0; k < SQds.getNum_entries(); k++ )
            SQds.getData_entry(k).setAttribute( attr );
          
          SQds.setTitle( titles[i] + RunNums[0] );
        }

        return V;
    }
     


  /**
   *  Utility for main program that just puts an array of floats
   *  into a Vector
   */
  private static Vector toVec( float[] list)
  {
     if( list == null)
       return new Vector();
     Vector Res = new Vector();
     for( int i = 0; i< list.length; i++)
         Res.addElement( new Float( list[i]));
     return Res;
  }


  /* ----------------------------- Main ---------------------------------- */
    /**
     *   Main program for testing purposes
     */
    public static void main(String[] args) 
    {
        IsawGUI.Util util = new IsawGUI.Util();

        DataSet[] RUNSds = null, RUNBds = null, RUNCds = null;
        DataSet[] TransS = null, TransB = null, Eff = null, Sens = null;
        float[] qu = new float[117];
        float BETADN, SCALE;

        BETADN = 0.0011f;
        SCALE = 843000f;
        //
        qu[0] = 0.0035f;
        for (int i = 1; i < 117; i++) {
            qu[i] = qu[i - 1] * 1.05f;
            //System.out.println("qu ....." +qu[i]);
        }
 
        String PATH_1 = "C:\\Argonne\\sand\\wrchen03\\";
        String PATH_2 = "C:\\ISAW\\DataSetTools\\operator\\Generic\\TOF_SAD\\";
        RUNSds = util.loadRunfile(PATH_1 + "sand19990.run");
        RUNBds = util.loadRunfile(PATH_1 + "sand19935.run");
        RUNCds = util.loadRunfile(PATH_1 + "sand19936.run");
        System.out.println("After loading runfiles, before loading isds");
        try {
            TransS = ScriptUtil.load(PATH_2 + "tr1999019934.isd");
            TransB = ScriptUtil.load(PATH_2 + "sr1993519934.isd");
            Eff = ScriptUtil.load(PATH_2 + "efr19452.isd");
            Sens = ScriptUtil.load("C:\\ISAW\\SampleRuns\\sens19878.isd");
        } catch (Exception sss) {
            System.out.println("Error:" + sss);
        }

        System.out.println("Before calling Reduce_KCLxxxxxxxxxxxxxxxxxxxxxxxx");
        Reduce_KCL reduce_KCL = new Reduce_KCL(TransS[0], TransB[0],
                Eff[0], Sens[0],toVec(qu), RUNSds[0], RUNSds[1],
                RUNBds[0],RUNBds[1], RUNCds[0],RUNCds[1], BETADN, SCALE, .1f,
                //     0f,0f);
                .000725f, .006909f, -200, -200, true, 1.5f );
        Object O = reduce_KCL.getResult();
        //new float[]{-.5f,.5f,-.5f,.5f}
        System.out.println("Finished O=" + O);
        Vector V = (Vector) O;
        ScriptUtil.display(
             ((DataSet)(V.elementAt(0))).getAttributeValue(Attribute.RUN_NUM));
        ScriptUtil.display(V.elementAt(0));
        ScriptUtil.display(V.elementAt(1));
        ScriptUtil.display(V.elementAt(2));
    }

}
